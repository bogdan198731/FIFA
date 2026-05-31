package com.example.worldcup.sync;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
public class WorldCupSyncController {

    private final WorldCupSyncService syncService;

    public WorldCupSyncController(WorldCupSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    public SyncResult sync() {
        return syncService.sync();
    }
}
