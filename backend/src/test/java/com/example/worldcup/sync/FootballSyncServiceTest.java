package com.example.worldcup.sync;

import com.example.worldcup.admin.MatchResultUpdatedEvent;
import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.match.MatchStage;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.prediction.PredictionRepository;
import com.example.worldcup.sync.dto.ExternalMatch;
import com.example.worldcup.sync.dto.ExternalMatchStatus;
import com.example.worldcup.sync.dto.ExternalTeam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the per-record application logic of {@link
 * FootballSyncService}. We don't spin up Spring or the scheduler — we
 * exercise the {@code applyMatchUpdate} / {@code applyTeamUpdate} entry
 * points directly with mocked collaborators so the business rules from the
 * task spec are pinned down individually.
 */
@ExtendWith(MockitoExtension.class)
class FootballSyncServiceTest {

    @Mock FootballDataClient client;
    @Mock MatchRepository matchRepository;
    @Mock TeamRepository teamRepository;
    @Mock PredictionRepository predictionRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    private FootballSyncService service;

    @BeforeEach
    void setUp() {
        FootballSyncProperties props = new FootballSyncProperties(
                true, "football-data-org", "test-key", "WC",
                "https://api.football-data.org/v4", Duration.ofMinutes(5),
                "0 0 4 * * *"
        );
        service = new FootballSyncService(
                client, matchRepository, teamRepository, predictionRepository,
                eventPublisher, props
        );
    }

    @Test
    void skipsExternalMatchesThatHaveNoMatchingRow() {
        when(matchRepository.findByExternalId("xyz")).thenReturn(Optional.empty());

        FootballSyncService.ApplyResult result = service.applyMatchUpdate(
                external("xyz", ExternalMatchStatus.SCHEDULED, null, null, null)
        );

        assertThat(result.changed()).isFalse();
        assertThat(result.nowFinished()).isFalse();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void leavesResultsAloneWhenAdminHasManuallyOverridden() {
        Match match = newMatch("ext-1", false);
        match.setResultManualOverride(true);
        match.setHomeScore(3);
        match.setAwayScore(2);
        when(matchRepository.findByExternalId("ext-1")).thenReturn(Optional.of(match));

        service.applyMatchUpdate(
                external("ext-1", ExternalMatchStatus.FINISHED, 1, 1, null)
        );

        // Scores stay at the admin's values; finished flag isn't flipped by the
        // feed; no scoring event is fired.
        assertThat(match.getHomeScore()).isEqualTo(3);
        assertThat(match.getAwayScore()).isEqualTo(2);
        assertThat(match.isFinished()).isFalse();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void marksMatchFinishedAndEmitsEventOnFirstFinishTransition() {
        Match match = newMatch("ext-2", false);
        when(matchRepository.findByExternalId("ext-2")).thenReturn(Optional.of(match));

        service.applyMatchUpdate(
                external("ext-2", ExternalMatchStatus.FINISHED, 2, 1, null)
        );

        assertThat(match.isFinished()).isTrue();
        assertThat(match.getHomeScore()).isEqualTo(2);
        assertThat(match.getAwayScore()).isEqualTo(1);

        ArgumentCaptor<MatchResultUpdatedEvent> captor =
                ArgumentCaptor.forClass(MatchResultUpdatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().matchId()).isEqualTo(match.getId());
    }

    @Test
    void doesNotReFireEventWhenAlreadyFinishedMatchSyncsAgain() {
        Match match = newMatch("ext-3", true);
        match.setHomeScore(2);
        match.setAwayScore(1);
        when(matchRepository.findByExternalId("ext-3")).thenReturn(Optional.of(match));

        service.applyMatchUpdate(
                external("ext-3", ExternalMatchStatus.FINISHED, 2, 1, null)
        );

        // Same scores, same finished state — no event.
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updatesLiveScoresWithoutMarkingFinished() {
        Match match = newMatch("ext-4", false);
        when(matchRepository.findByExternalId("ext-4")).thenReturn(Optional.of(match));

        FootballSyncService.ApplyResult result = service.applyMatchUpdate(
                external("ext-4", ExternalMatchStatus.LIVE, 1, 0, null)
        );

        assertThat(match.isFinished()).isFalse();
        assertThat(match.getHomeScore()).isEqualTo(1);
        assertThat(match.getAwayScore()).isZero();
        assertThat(result.changed()).isTrue();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void persistsKnockoutWinnerOnFinish() {
        Match match = newMatch("ext-5", false);
        match.setType(MatchType.KNOCKOUT);
        match.setStage(MatchStage.QUARTER_FINAL);
        when(matchRepository.findByExternalId("ext-5")).thenReturn(Optional.of(match));

        service.applyMatchUpdate(
                external("ext-5", ExternalMatchStatus.FINISHED, 2, 2, "Brazil")
        );

        assertThat(match.getKnockoutWinner()).isEqualTo("Brazil");
        assertThat(match.isFinished()).isTrue();
    }

    @Test
    void leavesKickoffAloneOncePredictionsExist() {
        Match match = newMatch("ext-6", false);
        Instant originalKickoff = match.getKickoffAt();
        when(matchRepository.findByExternalId("ext-6")).thenReturn(Optional.of(match));
        when(predictionRepository.existsByMatchId(any())).thenReturn(true);

        Instant newKickoff = originalKickoff.plus(Duration.ofHours(2));
        service.applyMatchUpdate(new ExternalMatch(
                "ext-6", "Brazil", "France", newKickoff,
                ExternalMatchStatus.SCHEDULED, null, null, null
        ));

        // Predictions exist → kickoff stays put even though the feed moved it.
        assertThat(match.getKickoffAt()).isEqualTo(originalKickoff);
    }

    @Test
    void updatesKickoffWhenNoPredictionsExistYet() {
        Match match = newMatch("ext-7", false);
        Instant originalKickoff = match.getKickoffAt();
        when(matchRepository.findByExternalId("ext-7")).thenReturn(Optional.of(match));
        when(predictionRepository.existsByMatchId(any())).thenReturn(false);

        Instant newKickoff = originalKickoff.plus(Duration.ofHours(3));
        service.applyMatchUpdate(new ExternalMatch(
                "ext-7", "Brazil", "France", newKickoff,
                ExternalMatchStatus.SCHEDULED, null, null, null
        ));

        assertThat(match.getKickoffAt()).isEqualTo(newKickoff);
    }

    @Test
    void teamUpdateInsertsNewTeamWhenAbsent() {
        when(teamRepository.findByExternalId("t-1")).thenReturn(Optional.empty());

        service.applyTeamUpdate(new ExternalTeam("t-1", "Brazil", "BRA"));

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        assertThat(captor.getValue().getExternalId()).isEqualTo("t-1");
        assertThat(captor.getValue().getName()).isEqualTo("Brazil");
        assertThat(captor.getValue().getShortCode()).isEqualTo("BRA");
    }

    @Test
    void teamUpdateRefreshesExistingTeam() {
        Team existing = new Team("t-2", "Old Name", "OLD");
        when(teamRepository.findByExternalId("t-2")).thenReturn(Optional.of(existing));

        service.applyTeamUpdate(new ExternalTeam("t-2", "New Name", "NEW"));

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getShortCode()).isEqualTo("NEW");
        verify(teamRepository).save(existing);
    }

    // ----- helpers --------------------------------------------------------

    private static Match newMatch(String externalId, boolean finished) {
        Match match = new Match(
                "Brazil", "France",
                Instant.parse("2026-06-14T18:00:00Z"),
                MatchType.REGULAR, MatchStage.GROUP
        );
        match.setExternalId(externalId);
        match.setFinished(finished);
        // We can't set the id directly via reflection-free API; tests don't
        // need it set, but a couple use it via the event payload — null is
        // fine for ArgumentCaptor assertions.
        return match;
    }

    private static ExternalMatch external(String externalId,
                                          ExternalMatchStatus status,
                                          Integer home,
                                          Integer away,
                                          String winner) {
        return new ExternalMatch(
                externalId, "Brazil", "France",
                Instant.parse("2026-06-14T18:00:00Z"),
                status, home, away, winner
        );
    }
}
