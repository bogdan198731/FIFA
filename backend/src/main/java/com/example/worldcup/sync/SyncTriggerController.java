package com.example.worldcup.sync;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-triggered manual sync. Path lives under {@code /api/admin/**} so
 * {@code SecurityConfig}'s existing {@code ADMIN} rule applies — no extra
 * security wiring needed.
 *
 * <p>Conditional on the sync flag so when the agent's disabled the endpoint
 * isn't exposed at all (rather than existing and returning a confusing 404
 * from the service tier).
 */
@RestController
@RequestMapping("/api/admin/sync")
@ConditionalOnProperty(name = "app.football-sync.enabled", havingValue = "true")
public class SyncTriggerController {

    private final FootballSyncService syncService;

    public SyncTriggerController(FootballSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/run")
    public ResponseEntity<Void> runNow() {
        syncService.runOnce();
        return ResponseEntity.accepted().build();
    }
}
