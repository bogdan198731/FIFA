package com.example.worldcup.sync.dto;

/**
 * Provider-agnostic match status used by the sync agent. Each
 * {@link com.example.worldcup.sync.FootballDataClient} implementation is
 * responsible for mapping the vendor's status strings to one of these.
 */
public enum ExternalMatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    POSTPONED,
    CANCELED,
    UNKNOWN
}
