package com.example.worldcup.sync;

import com.example.worldcup.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorldCupSyncService {

    private static final Logger log = LoggerFactory.getLogger(WorldCupSyncService.class);
    private static final String API_BASE = "https://v3.football.api-sports.io";
    private static final int LEAGUE_ID = 1;
    private static final int SEASON = 2026;

    private final WorldCupSyncPersister persister;
    private final RestClient restClient;

    @Value("${app.api-football.key:}")
    private String apiKey;

    public WorldCupSyncService(WorldCupSyncPersister persister) {
        this.persister = persister;
        this.restClient = RestClient.create();
    }

    // Fix 2: no @Transactional here — HTTP fetch runs outside the DB transaction.
    // persister.persistAll() opens its own transaction only for the DB writes.
    public SyncResult sync() {
        if (apiKey == null || apiKey.isBlank()) {
            // Surface a clear, actionable message instead of a generic 500.
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "External API sync is not configured. Set the API_FOOTBALL_KEY "
                            + "environment variable on the server.");
        }
        // The fixtures endpoint's round is a generic "Group Stage - N" with no
        // group letter, so we pull the real team → group map from /standings.
        Map<String, String> teamGroups = fetchTeamGroups();
        List<JsonNode> fixtures = fetchAllFixtures();
        return persister.persistAll(fixtures, teamGroups);
    }

    /**
     * Team name (lower-cased) → group label (e.g. "A") from API-Football's
     * standings, which carry each team's actual group. Degrades to an empty
     * map (matches stay ungrouped) if standings aren't available on the plan,
     * rather than failing the whole sync.
     */
    private Map<String, String> fetchTeamGroups() {
        Map<String, String> teamGroups = new HashMap<>();
        JsonNode body;
        try {
            body = restClient.get()
                    .uri(API_BASE + "/standings?league=" + LEAGUE_ID + "&season=" + SEASON)
                    .header("x-apisports-key", apiKey)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            log.warn("Could not fetch standings for group assignments: {}", ex.getMessage());
            return teamGroups;
        }
        if (body == null) {
            return teamGroups;
        }
        JsonNode errors = body.get("errors");
        if (errors != null && !errors.isEmpty() && !(errors.isObject() && errors.isEmpty())) {
            log.warn("Standings unavailable for group assignments: {}", errors);
            return teamGroups;
        }

        // response[0].league.standings is an array of groups; each entry has a
        // team.name and a "group" like "Group A".
        JsonNode standings = body.at("/response/0/league/standings");
        if (standings.isArray()) {
            for (JsonNode group : standings) {
                for (JsonNode entry : group) {
                    String teamName = entry.at("/team/name").asText(null);
                    String label = WorldCupSyncPersister.extractGroupLabel(entry.at("/group").asText(null));
                    if (teamName != null && label != null) {
                        teamGroups.put(teamName.toLowerCase(), label);
                    }
                }
            }
        }
        log.info("Fetched group assignments for {} teams from standings.", teamGroups.size());
        return teamGroups;
    }

    /**
     * Fetches squads for every team in the tournament and persists them to the
     * players table. Calls /fixtures once to discover team IDs, then calls
     * /players/squads?team={id} for each unique team (48 calls for WC 2026).
     * Intended as a one-time admin operation at tournament start.
     */
    public SquadSyncResult syncSquads() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "External API sync is not configured. Set the API_FOOTBALL_KEY "
                            + "environment variable on the server.");
        }

        // Collect unique team IDs from the fixtures we already know about.
        // LinkedHashMap preserves insertion order for predictable logging.
        Map<Integer, String> teamIdToName = new LinkedHashMap<>();
        List<JsonNode> fixtures = fetchAllFixtures();
        for (JsonNode f : fixtures) {
            int homeId   = f.at("/teams/home/id").asInt(0);
            String homeN = f.at("/teams/home/name").asText(null);
            int awayId   = f.at("/teams/away/id").asInt(0);
            String awayN = f.at("/teams/away/name").asText(null);
            if (homeId > 0 && homeN != null) teamIdToName.putIfAbsent(homeId, homeN);
            if (awayId > 0 && awayN != null) teamIdToName.putIfAbsent(awayId, awayN);
        }
        log.info("Fetched {} unique teams from fixtures for squad sync.", teamIdToName.size());

        List<JsonNode> squadEntries = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : teamIdToName.entrySet()) {
            try {
                List<JsonNode> squads = fetchSquadForTeam(entry.getKey());
                squadEntries.addAll(squads);
            } catch (Exception ex) {
                log.warn("Could not fetch squad for team {} ({}): {}", entry.getValue(), entry.getKey(), ex.getMessage());
            }
        }

        return persister.persistSquads(squadEntries);
    }

    private List<JsonNode> fetchSquadForTeam(int teamId) {
        String url = API_BASE + "/players/squads?team=" + teamId;
        JsonNode body;
        try {
            body = restClient.get()
                    .uri(url)
                    .header("x-apisports-key", apiKey)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Could not reach API-Football for team " + teamId + ": " + ex.getMessage());
        }
        if (body == null) return List.of();
        JsonNode errors = body.get("errors");
        if (errors != null && !errors.isEmpty() && !(errors.isObject() && errors.isEmpty())) {
            log.warn("Squads error for team {}: {}", teamId, errors);
            return List.of();
        }
        List<JsonNode> result = new ArrayList<>();
        JsonNode response = body.get("response");
        if (response != null) response.forEach(result::add);
        return result;
    }

    private List<JsonNode> fetchAllFixtures() {
        List<JsonNode> all = new ArrayList<>();

        // API-Football's /fixtures endpoint returns every fixture for a
        // league+season in a single response — it does NOT paginate, and
        // sending a `page` parameter fails with
        // {"page":"The Page field do not exist."}. So we make one request.
        String url = API_BASE + "/fixtures?league=" + LEAGUE_ID + "&season=" + SEASON;
        JsonNode body;
        try {
            body = restClient.get()
                    .uri(url)
                    .header("x-apisports-key", apiKey)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            // Timeout, connection refused, non-2xx from API-Football, etc.
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Could not reach API-Football: " + ex.getMessage());
        }

        if (body == null) {
            return all;
        }

        JsonNode errors = body.get("errors");
        if (errors != null && !errors.isEmpty() && !(errors.isObject() && errors.isEmpty())) {
            // e.g. invalid key, or the plan doesn't cover league/season.
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "API-Football returned an error: " + errors);
        }

        JsonNode response = body.get("response");
        if (response != null) {
            response.forEach(all::add);
        }
        return all;
    }
}
