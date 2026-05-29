package com.example.worldcup.sync;

import com.example.worldcup.sync.dto.ExternalMatch;
import com.example.worldcup.sync.dto.ExternalMatchStatus;
import com.example.worldcup.sync.dto.ExternalTeam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * {@link FootballDataClient} backed by <a
 * href="https://www.football-data.org">football-data.org</a> v4.
 *
 * <p>Auth is a single {@code X-Auth-Token} header. The free tier permits
 * 10 requests per minute, which is plenty given the sync agent's default
 * 5-minute cadence.
 */
@Component
@ConditionalOnProperty(
        name = "app.football-sync.provider",
        havingValue = "football-data-org",
        matchIfMissing = true
)
public class FootballDataOrgClient implements FootballDataClient {

    private static final Logger log = LoggerFactory.getLogger(FootballDataOrgClient.class);

    private final RestClient restClient;

    public FootballDataOrgClient(FootballSyncProperties props) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new IllegalStateException(
                    "app.football-sync.api-key is not configured. Set FOOTBALL_API_KEY "
                            + "(get one at https://www.football-data.org/client/register).");
        }
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("X-Auth-Token", props.apiKey())
                .defaultHeader("Accept", "application/json")
                .build();
        log.info("FootballDataOrgClient initialised against {}", props.baseUrl());
    }

    @Override
    public List<ExternalTeam> fetchTeams(String competitionId) {
        TeamsEnvelope body = restClient.get()
                .uri("/competitions/{id}/teams", competitionId)
                .retrieve()
                .body(TeamsEnvelope.class);
        if (body == null || body.teams() == null) {
            return Collections.emptyList();
        }
        return body.teams().stream()
                .map(t -> new ExternalTeam(
                        String.valueOf(t.id()),
                        t.name(),
                        t.tla() != null ? t.tla() : t.shortName()
                ))
                .toList();
    }

    @Override
    public List<ExternalMatch> fetchMatches(String competitionId) {
        MatchesEnvelope body = restClient.get()
                .uri("/competitions/{id}/matches", competitionId)
                .retrieve()
                .body(MatchesEnvelope.class);
        if (body == null || body.matches() == null) {
            return Collections.emptyList();
        }
        return body.matches().stream()
                .map(this::toExternalMatch)
                .toList();
    }

    private ExternalMatch toExternalMatch(ApiMatch m) {
        Integer home = (m.score() != null && m.score().fullTime() != null) ? m.score().fullTime().home() : null;
        Integer away = (m.score() != null && m.score().fullTime() != null) ? m.score().fullTime().away() : null;
        String winner = resolveWinnerName(m);

        return new ExternalMatch(
                String.valueOf(m.id()),
                m.homeTeam() != null ? m.homeTeam().name() : null,
                m.awayTeam() != null ? m.awayTeam().name() : null,
                m.utcDate(),
                mapStatus(m.status()),
                home,
                away,
                winner
        );
    }

    /**
     * Football-Data exposes the knockout winner via {@code score.winner} as one
     * of {@code HOME_TEAM} / {@code AWAY_TEAM} / {@code DRAW}. Translate it to
     * the actual team name so the sync agent can store it directly.
     */
    private String resolveWinnerName(ApiMatch m) {
        if (m.score() == null || m.score().winner() == null) {
            return null;
        }
        return switch (m.score().winner()) {
            case "HOME_TEAM" -> m.homeTeam() != null ? m.homeTeam().name() : null;
            case "AWAY_TEAM" -> m.awayTeam() != null ? m.awayTeam().name() : null;
            default -> null;
        };
    }

    private ExternalMatchStatus mapStatus(String status) {
        if (status == null) {
            return ExternalMatchStatus.UNKNOWN;
        }
        return switch (status) {
            case "SCHEDULED", "TIMED" -> ExternalMatchStatus.SCHEDULED;
            case "IN_PLAY", "PAUSED" -> ExternalMatchStatus.LIVE;
            case "FINISHED", "AWARDED" -> ExternalMatchStatus.FINISHED;
            case "POSTPONED", "SUSPENDED" -> ExternalMatchStatus.POSTPONED;
            case "CANCELLED", "CANCELED" -> ExternalMatchStatus.CANCELED;
            default -> ExternalMatchStatus.UNKNOWN;
        };
    }

    // ----- Wire records mirroring the vendor's JSON shape ----------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TeamsEnvelope(List<ApiTeam> teams) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MatchesEnvelope(List<ApiMatch> matches) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiTeam(long id, String name, String shortName, String tla) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiMatch(
            long id,
            String status,
            Instant utcDate,
            ApiTeam homeTeam,
            ApiTeam awayTeam,
            ApiScore score
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiScore(String winner, ApiFullTime fullTime) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiFullTime(Integer home, Integer away) {}
}
