package com.example.worldcup.admin;

/**
 * Published by {@code AdminMatchService} whenever a match's scoring fields are
 * touched. {@code AdminScoreService} reacts after the publishing transaction
 * commits to recompute points for that match.
 */
public record MatchResultUpdatedEvent(Long matchId) {
}
