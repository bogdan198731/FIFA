package com.example.worldcup.match.dto;

import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchStage;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.match.PredictionLockOverride;

import java.time.Instant;

public record MatchResponse(
        Long id,
        String homeTeam,
        String awayTeam,
        Instant kickoffAt,
        String venue,
        MatchType matchType,
        MatchStage stage,
        String groupName,
        Integer homeScore,
        Integer awayScore,
        String knockoutWinner,
        boolean finished,
        PredictionLockOverride predictionLockOverride,
        boolean locked
) {

    public static MatchResponse from(Match match, Instant now) {
        return new MatchResponse(
                match.getId(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getKickoffAt(),
                match.getVenue(),
                match.getType(),
                match.getStage(),
                match.getGroupName(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getKnockoutWinner(),
                match.isFinished(),
                match.getPredictionLockOverride(),
                match.isPredictionLocked(now)
        );
    }
}
