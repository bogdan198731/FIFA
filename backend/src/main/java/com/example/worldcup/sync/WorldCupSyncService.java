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
import java.util.List;

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
        List<JsonNode> fixtures = fetchAllFixtures();
        return persister.persistAll(fixtures);
    }

    private List<JsonNode> fetchAllFixtures() {
        List<JsonNode> all = new ArrayList<>();
        int page = 1;
        while (true) {
            String url = API_BASE + "/fixtures?league=" + LEAGUE_ID + "&season=" + SEASON + "&page=" + page;
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

            if (body == null) break;

            JsonNode errors = body.get("errors");
            if (errors != null && !errors.isEmpty() && !(errors.isObject() && errors.isEmpty())) {
                // e.g. invalid key, or the plan doesn't cover league/season.
                throw new ApiException(HttpStatus.BAD_GATEWAY,
                        "API-Football returned an error: " + errors);
            }

            JsonNode response = body.get("response");
            if (response == null || response.isEmpty()) break;
            response.forEach(all::add);

            int totalPages = body.at("/paging/total").asInt(1);
            if (page >= totalPages) break;
            page++;

            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return all;
    }
}
