package com.example.worldcup.sync.dto;

/**
 * Vendor-neutral view of a team returned by a {@link
 * com.example.worldcup.sync.FootballDataClient}.
 */
public record ExternalTeam(
        String externalId,
        String name,
        String shortCode
) {
}
