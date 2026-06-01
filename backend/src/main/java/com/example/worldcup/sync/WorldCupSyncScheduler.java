package com.example.worldcup.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorldCupSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(WorldCupSyncScheduler.class);

    private final WorldCupSyncService syncService;

    public WorldCupSyncScheduler(WorldCupSyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "UTC")
    public void scheduledSync() {
        log.info("Starting scheduled WC sync...");
        try {
            SyncResult result = syncService.sync();
            log.info("Scheduled WC sync done: {}", result);
        } catch (Exception ex) {
            log.error("Scheduled WC sync failed", ex);
        }
    }
}
