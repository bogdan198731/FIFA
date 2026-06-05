package com.example.worldcup.league.dto;

import java.time.Instant;

public record PrivateLeagueResponse(
        Long id,
        String name,
        String inviteCode,
        String ownerUsername,
        boolean isOwner,
        int memberCount,
        Instant createdAt
) {}
