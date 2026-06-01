package com.example.worldcup.admin.dto;

/**
 * Read-only snapshot of effective server configuration for the admin
 * "Configurations" screen.
 *
 * <p>Secrets are never returned — the API key is surfaced only as a boolean
 * ({@code apiFootballKeyConfigured}) so an admin can confirm whether it's
 * wired up without the value leaking to the browser.
 */
public record AdminConfigResponse(
        String bootstrapAdminName,
        long userCount,
        long adminCount,
        boolean apiFootballKeyConfigured
) {
}
