package com.example.worldcup.dashboard.dto;

import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchStage;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.prediction.Prediction;

import java.time.Instant;

public record RecentPredictionResult(
        Long predictionId,
        Long matchId,
        String homeTeam,
        String awayTeam,
        Instant kickoffAt,
        MatchStage stage,
        MatchType matchType,
        Integer predictedHomeScore,
        Integer predictedAwayScore,
        String predictedQualifiedTeam,
        Integer actualHomeScore,
        Integer actualAwayScore,
        String actualQualifiedTeam,
        int pointsAwarded
) {

    public static RecentPredictionResult from(Prediction prediction) {
        Match match = prediction.getMatch();
        return new RecentPredictionResult(
                prediction.getId(),
                match.getId(),
                match.getHomeTeam(),
                match.getAwayTeam(),
                match.getKickoffAt(),
                match.getStage(),
                match.getType(),
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore(),
                prediction.getPredictedWinner(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getKnockoutWinner(),
                prediction.getPointsAwarded()
        );
    }
}
