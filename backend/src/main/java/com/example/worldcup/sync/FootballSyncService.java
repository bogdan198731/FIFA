package com.example.worldcup.sync;

import com.example.worldcup.admin.MatchResultUpdatedEvent;
import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.prediction.PredictionRepository;
import com.example.worldcup.sync.dto.ExternalMatch;
import com.example.worldcup.sync.dto.ExternalMatchStatus;
import com.example.worldcup.sync.dto.ExternalTeam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Live football data sync agent.
 *
 * <p>Two scheduled jobs:
 * <ul>
 *   <li>{@link #syncMatches()} — polls the upstream provider at the configured
 *       cadence (default every 5 minutes), and reconciles each upstream match
 *       with the matching row in {@code matches} (looked up by
 *       {@code external_id}).</li>
 *   <li>{@link #syncTeams()} — runs daily on a cron schedule and refreshes the
 *       {@code teams} cache.</li>
 * </ul>
 *
 * <p>Three guard rails keep the sync from stepping on user data:
 * <ol>
 *   <li>Matches without an {@code external_id} are left alone — admin-created
 *       fixtures stay admin-owned.</li>
 *   <li>If {@code match.resultManualOverride} is true, the agent doesn't
 *       touch the result fields. An admin who hand-corrects a score always
 *       wins over the feed.</li>
 *   <li>Kickoff times only get updated while no prediction exists for the
 *       match — once users have locked in picks, we don't move the goalpost.</li>
 * </ol>
 *
 * <p>When a match transitions from "not finished" to "finished" via the feed,
 * we emit a {@link MatchResultUpdatedEvent}, which the existing {@code
 * AdminScoreService} listens for to rescore that match's predictions.
 *
 * <p>The whole bean is conditional on {@code app.football-sync.enabled=true},
 * so dev/test environments without an upstream provider stay completely
 * untouched.
 */
@Service
@ConditionalOnProperty(name = "app.football-sync.enabled", havingValue = "true")
public class FootballSyncService {

    private static final Logger log = LoggerFactory.getLogger(FootballSyncService.class);

    private final FootballDataClient client;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PredictionRepository predictionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FootballSyncProperties props;

    public FootballSyncService(FootballDataClient client,
                               MatchRepository matchRepository,
                               TeamRepository teamRepository,
                               PredictionRepository predictionRepository,
                               ApplicationEventPublisher eventPublisher,
                               FootballSyncProperties props) {
        this.client = client;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.predictionRepository = predictionRepository;
        this.eventPublisher = eventPublisher;
        this.props = props;
    }

    /**
     * Polls the upstream provider for the configured competition and applies
     * any deltas. Default cadence: 5 minutes.
     *
     * <p>Wrapped in a try/catch so a transient upstream failure (timeout, 429,
     * 5xx) just logs and lets the next tick retry — it doesn't crash the
     * scheduler or roll back the rest of the application context.
     */
    @Scheduled(
            fixedDelayString = "#{@footballSyncProperties.matchInterval().toMillis()}",
            initialDelayString = "#{@footballSyncProperties.matchInterval().toMillis()}"
    )
    @Transactional
    public void syncMatches() {
        try {
            List<ExternalMatch> matches = client.fetchMatches(props.competitionId());
            int updated = 0;
            int finished = 0;
            for (ExternalMatch external : matches) {
                ApplyResult result = applyMatchUpdate(external);
                if (result.changed) updated++;
                if (result.nowFinished) finished++;
            }
            log.info("Match sync complete: {} external matches, {} updated, {} newly finished",
                    matches.size(), updated, finished);
        } catch (Exception ex) {
            log.error("Match sync failed; will retry on next tick", ex);
        }
    }

    /**
     * Refreshes the {@code teams} cache. Default cadence: once a day at 04:00.
     */
    @Scheduled(cron = "#{@footballSyncProperties.teamCron()}")
    @Transactional
    public void syncTeams() {
        try {
            List<ExternalTeam> teams = client.fetchTeams(props.competitionId());
            int saved = 0;
            for (ExternalTeam external : teams) {
                applyTeamUpdate(external);
                saved++;
            }
            log.info("Team sync complete: {} teams refreshed", saved);
        } catch (Exception ex) {
            log.error("Team sync failed; will retry on next tick", ex);
        }
    }

    /**
     * Runs both syncs back-to-back. Exposed for the admin-triggered endpoint so
     * an operator can pull fresh data on demand without waiting for the next
     * scheduled tick.
     */
    @Transactional
    public void runOnce() {
        syncTeams();
        syncMatches();
    }

    // ----- per-record application logic --------------------------------------

    ApplyResult applyMatchUpdate(ExternalMatch external) {
        if (external == null || external.externalId() == null) {
            return ApplyResult.noChange();
        }
        Match match = matchRepository.findByExternalId(external.externalId()).orElse(null);
        if (match == null) {
            // Sync agent doesn't create matches — that's an admin action. Skip.
            return ApplyResult.noChange();
        }

        boolean changed = false;
        boolean nowFinished = false;

        // (1) Kickoff: only writable while no prediction exists for the match.
        if (external.kickoffAt() != null
                && !external.kickoffAt().equals(match.getKickoffAt())
                && !predictionRepository.existsByMatchId(match.getId())) {
            match.setKickoffAt(external.kickoffAt());
            changed = true;
        }

        // (2) Result: only writable when the admin hasn't taken manual control.
        if (!match.isResultManualOverride()) {
            switch (external.status()) {
                case FINISHED -> {
                    if (!match.isFinished()) {
                        nowFinished = true;
                    }
                    if (writeScores(match, external.homeScore(), external.awayScore())) {
                        changed = true;
                    }
                    if (external.winningTeamName() != null
                            && !Objects.equals(external.winningTeamName(), match.getKnockoutWinner())) {
                        match.setKnockoutWinner(external.winningTeamName());
                        changed = true;
                    }
                    if (!match.isFinished()) {
                        match.setFinished(true);
                        changed = true;
                    }
                }
                case LIVE -> {
                    // Live score updates while the match is in progress.
                    if (writeScores(match, external.homeScore(), external.awayScore())) {
                        changed = true;
                    }
                }
                case POSTPONED, CANCELED, SCHEDULED, UNKNOWN -> {
                    // Nothing to do — kickoff handling above covers reschedules
                    // and we don't propagate cancellations into result fields.
                }
            }
        }

        if (nowFinished) {
            eventPublisher.publishEvent(new MatchResultUpdatedEvent(match.getId()));
        }

        return new ApplyResult(changed, nowFinished);
    }

    private boolean writeScores(Match match, Integer home, Integer away) {
        boolean changed = false;
        if (!Objects.equals(home, match.getHomeScore())) {
            match.setHomeScore(home);
            changed = true;
        }
        if (!Objects.equals(away, match.getAwayScore())) {
            match.setAwayScore(away);
            changed = true;
        }
        return changed;
    }

    void applyTeamUpdate(ExternalTeam external) {
        if (external == null || external.externalId() == null) {
            return;
        }
        Team team = teamRepository.findByExternalId(external.externalId())
                .orElseGet(() -> new Team(external.externalId(), external.name(), external.shortCode()));
        team.setName(external.name());
        team.setShortCode(external.shortCode());
        team.setLastSyncedAt(Instant.now());
        teamRepository.save(team);
    }

    /** Returned from {@link #applyMatchUpdate} so tests can assert behaviour. */
    record ApplyResult(boolean changed, boolean nowFinished) {
        static ApplyResult noChange() {
            return new ApplyResult(false, false);
        }
    }
}
