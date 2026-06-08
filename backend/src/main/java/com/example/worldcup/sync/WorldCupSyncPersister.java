package com.example.worldcup.sync;

import com.example.worldcup.admin.MatchResultUpdatedEvent;
import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.match.MatchStage;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.player.Player;
import com.example.worldcup.player.PlayerRepository;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class WorldCupSyncPersister {

    private static final Logger log = LoggerFactory.getLogger(WorldCupSyncPersister.class);

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final ApplicationEventPublisher eventPublisher;

    WorldCupSyncPersister(MatchRepository matchRepository,
                          PlayerRepository playerRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    SyncResult persistAll(List<JsonNode> fixtures, Map<String, String> teamGroups) {
        int created = 0, updated = 0, unchanged = 0;

        for (JsonNode fixture : fixtures) {
            try {
                switch (upsertFixture(fixture, teamGroups)) {
                    case CREATED   -> created++;
                    case UPDATED   -> updated++;
                    case UNCHANGED -> unchanged++;
                }
            } catch (Exception ex) {
                log.warn("Skipped fixture {}: {}", fixture.at("/fixture/id").asLong(), ex.getMessage());
            }
        }

        log.info("WC sync complete — created={}, updated={}, unchanged={}", created, updated, unchanged);
        return new SyncResult(created, updated, unchanged);
    }

    private SyncOutcome upsertFixture(JsonNode f, Map<String, String> teamGroups) {
        JsonNode fixNode = f.get("fixture");
        if (fixNode == null) return SyncOutcome.UNCHANGED;

        // Fix 3: path() returns MissingNode (never null), avoiding NPE on absent "id" field
        long apiFixtureId = fixNode.path("id").asLong(0);
        if (apiFixtureId == 0) return SyncOutcome.UNCHANGED;

        String dateStr = fixNode.at("/date").asText(null);
        if (dateStr == null || dateStr.isBlank()) return SyncOutcome.UNCHANGED;

        String homeTeam = f.at("/teams/home/name").asText(null);
        String awayTeam = f.at("/teams/away/name").asText(null);
        if (homeTeam == null || awayTeam == null) return SyncOutcome.UNCHANGED;

        Instant kickoffAt = OffsetDateTime.parse(dateStr).toInstant();
        String venueStr = fixNode.at("/venue/name").asText(null);
        String venue = (venueStr == null || venueStr.isBlank()) ? null : venueStr;

        String round = f.at("/league/round").asText(null);
        MatchStage stage = toStage(round);
        MatchType type = stage == MatchStage.GROUP ? MatchType.REGULAR : MatchType.KNOCKOUT;
        String groupName = toGroup(round, stage);
        // The provider's round is a generic "Group Stage - N" (no letter), so
        // fall back to the team → group map fetched from /standings.
        if (groupName == null && stage == MatchStage.GROUP) {
            groupName = groupForTeams(teamGroups, homeTeam, awayTeam);
        }

        String status = fixNode.at("/status/short").asText("");
        boolean finished = isFinished(status);

        Integer homeScore = null;
        Integer awayScore = null;
        String knockoutWinner = null;

        if (finished) {
            JsonNode goalsHome = f.at("/goals/home");
            JsonNode goalsAway = f.at("/goals/away");
            if (!goalsHome.isNull() && !goalsAway.isNull()) {
                homeScore = goalsHome.asInt();
                awayScore = goalsAway.asInt();
            }
            if (type == MatchType.KNOCKOUT) {
                knockoutWinner = determineWinner(f, homeTeam, awayTeam, homeScore, awayScore, status);
            }
        }

        Optional<Match> existing = matchRepository.findByApiFixtureId(apiFixtureId);
        if (existing.isEmpty()) {
            Match match = new Match(homeTeam, awayTeam, kickoffAt, type, stage);
            match.setVenue(venue);
            match.setApiFixtureId(apiFixtureId);
            match.setGroupName(groupName);
            // Fix 1: don't mark finished when knockoutWinner is missing — wait for complete data
            if (canMarkFinished(type, finished, homeScore, knockoutWinner)) {
                match.setHomeScore(homeScore);
                match.setAwayScore(awayScore);
                match.setKnockoutWinner(knockoutWinner);
                match.setFinished(true);
            }
            matchRepository.save(match);
            if (match.isFinished()) {
                eventPublisher.publishEvent(new MatchResultUpdatedEvent(match.getId()));
            }
            return SyncOutcome.CREATED;
        }

        Match match = existing.get();
        // Fix 5: always sync identity fields so reschedules and corrections are picked up
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setKickoffAt(kickoffAt);
        match.setVenue(venue);
        match.setStage(stage);
        match.setType(type);
        match.setGroupName(groupName);

        // Fix 1: only mark finished when result data is complete
        if (!match.isFinished() && canMarkFinished(type, finished, homeScore, knockoutWinner)) {
            match.setHomeScore(homeScore);
            match.setAwayScore(awayScore);
            match.setKnockoutWinner(knockoutWinner);
            match.setFinished(true);
            eventPublisher.publishEvent(new MatchResultUpdatedEvent(match.getId()));
            return SyncOutcome.UPDATED;
        }

        return SyncOutcome.UNCHANGED;
    }

    // Fix 1: knockout matches require a non-null winner before being marked finished
    private boolean canMarkFinished(MatchType type, boolean finished, Integer homeScore, String knockoutWinner) {
        if (!finished || homeScore == null) return false;
        return type != MatchType.KNOCKOUT || knockoutWinner != null;
    }

    private MatchStage toStage(String round) {
        if (round == null) return MatchStage.GROUP;
        return switch (round) {
            case "Round of 32"   -> MatchStage.ROUND_OF_32;
            case "Round of 16"   -> MatchStage.ROUND_OF_16;
            case "Quarter-finals" -> MatchStage.QUARTER_FINAL;
            case "Semi-finals"   -> MatchStage.SEMI_FINAL;
            case "3rd Place Final" -> MatchStage.THIRD_PLACE;
            case "Final"         -> MatchStage.FINAL;
            default              -> MatchStage.GROUP;
        };
    }

    // API-Football group rounds look like "Group A - 1" — capture the single
    // group label. The trailing lookahead requires that label to stand alone,
    // so a generic round like "Group Stage - 1" does NOT match (otherwise it
    // would yield "STAGE"). Returns null when there's no real group letter.
    private static final Pattern GROUP_PATTERN =
            Pattern.compile("\\bGroup\\s+([A-Za-z0-9])(?![A-Za-z0-9])", Pattern.CASE_INSENSITIVE);

    static String toGroup(String round, MatchStage stage) {
        if (stage != MatchStage.GROUP) {
            return null;
        }
        return extractGroupLabel(round);
    }

    /** Pull the single group label out of a "Group A …" string (else null). */
    static String extractGroupLabel(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = GROUP_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1).toUpperCase() : null;
    }

    /** Group of either team from the standings-derived map (home wins ties). */
    static String groupForTeams(Map<String, String> teamGroups, String homeTeam, String awayTeam) {
        if (teamGroups == null) {
            return null;
        }
        String group = homeTeam == null ? null : teamGroups.get(homeTeam.toLowerCase());
        if (group == null && awayTeam != null) {
            group = teamGroups.get(awayTeam.toLowerCase());
        }
        return group;
    }

    private boolean isFinished(String status) {
        return "FT".equals(status) || "AET".equals(status) || "PEN".equals(status);
    }

    private String determineWinner(JsonNode f, String homeTeam, String awayTeam,
                                   Integer homeScore, Integer awayScore, String status) {
        if ("PEN".equals(status)) {
            int penHome = f.at("/score/penalty/home").asInt(-1);
            int penAway = f.at("/score/penalty/away").asInt(-1);
            // Fix 4: equal penalty scores means invalid/missing data — return null, not a wrong winner
            if (penHome >= 0 && penAway >= 0 && penHome != penAway) {
                return penHome > penAway ? homeTeam : awayTeam;
            }
        }
        if (homeScore != null && awayScore != null && !homeScore.equals(awayScore)) {
            return homeScore > awayScore ? homeTeam : awayTeam;
        }
        return null;
    }

    /**
     * Upserts all players from a squads API response (one response per team).
     * Each element of {@code squadResponses} is the API's {@code response[n]} node:
     * <pre>{ "team": { "id": 1, "name": "..." }, "players": [ { "id": 1, "name": "...",
     * "position": "Goalkeeper", "photo": "..." }, ... ] }</pre>
     */
    @Transactional
    SquadSyncResult persistSquads(List<JsonNode> squadResponses) {
        int created = 0, updated = 0;

        for (JsonNode teamEntry : squadResponses) {
            String teamName = teamEntry.at("/team/name").asText(null);
            int apiTeamId = teamEntry.at("/team/id").asInt(0);
            if (teamName == null) continue;

            JsonNode players = teamEntry.get("players");
            if (players == null || !players.isArray()) continue;

            for (JsonNode p : players) {
                long apiPlayerId = p.at("/id").asLong(0);
                String name = p.at("/name").asText(null);
                String position = p.at("/position").asText(null);
                String photo = p.at("/photo").asText(null);

                if (name == null || position == null) continue;

                Optional<Player> existing = apiPlayerId > 0
                        ? playerRepository.findByApiPlayerId(apiPlayerId)
                        : Optional.empty();

                if (existing.isPresent()) {
                    Player ent = existing.get();
                    ent.setName(name);
                    ent.setTeamName(teamName);
                    ent.setPosition(position);
                    ent.setPhoto(photo);
                    ent.setApiTeamId(apiTeamId > 0 ? apiTeamId : null);
                    updated++;
                } else {
                    Player ent = new Player(
                            name,
                            teamName,
                            position,
                            (photo != null && !photo.isBlank()) ? photo : null,
                            apiPlayerId > 0 ? apiPlayerId : null,
                            apiTeamId > 0 ? apiTeamId : null
                    );
                    playerRepository.save(ent);
                    created++;
                }
            }
        }

        log.info("Squad sync complete — created={}, updated={}", created, updated);
        return new SquadSyncResult(squadResponses.size(), created, updated);
    }

    private enum SyncOutcome { CREATED, UPDATED, UNCHANGED }
}
