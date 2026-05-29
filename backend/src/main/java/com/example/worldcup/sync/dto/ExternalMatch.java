package com.example.worldcup.sync.dto;

import java.time.Instant;

/**
 * Vendor-neutral view of a match returned by a {@link
 * com.example.worldcup.sync.FootballDataClient}. {@code homeScore} and
 * {@code awayScore} may be {@code null} for matches that haven't kicked off
 * yet; {@code winningTeamName} is only populated for finished knockout
 * matches.
 */
public record ExternalMatch(
        String externalId,
        String homeTeamName,
        String awayTeamName,
        Instant kickoffAt,
        ExternalMatchStatus status,
        Integer homeScore,
        Integer awayScore,
        String winningTeamName
) {
}
