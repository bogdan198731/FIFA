package com.example.worldcup.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Bound to {@code app.football-sync.*} in {@code application.yml}.
 *
 * <p>Default values come from {@code application.yml} so a fresh dev or test
 * environment doesn't need any env vars; the sync agent stays {@code
 * enabled=false} until someone explicitly opts in.
 *
 * @param enabled        master switch — when {@code false}, the sync beans
 *                       aren't created at all (see {@code @ConditionalOnProperty}
 *                       on {@code FootballSyncService}).
 * @param provider       identifies which {@code FootballDataClient}
 *                       implementation to wire in (default
 *                       {@code football-data-org}).
 * @param apiKey         vendor API key — sent as the auth header. Required
 *                       when {@code enabled=true}.
 * @param competitionId  vendor competition identifier (e.g. {@code WC} for
 *                       the FIFA World Cup on football-data.org).
 * @param baseUrl        vendor base URL.
 * @param matchInterval  how often {@code syncMatches()} runs.
 * @param teamCron       cron expression for the daily team refresh.
 */
@ConfigurationProperties("app.football-sync")
public record FootballSyncProperties(
        boolean enabled,
        String provider,
        String apiKey,
        String competitionId,
        String baseUrl,
        Duration matchInterval,
        String teamCron
) {
}
