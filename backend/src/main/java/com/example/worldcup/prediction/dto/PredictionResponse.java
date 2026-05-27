package com.example.worldcup.prediction.dto;

import com.example.worldcup.prediction.Prediction;

import java.time.Instant;

public record PredictionResponse(
        Long id,
        Long matchId,
        Integer homeScore,
        Integer awayScore,
        String qualifiedTeam,
        int pointsAwarded,
        boolean locked,
        Instant createdAt,
        Instant updatedAt
) {

    public static PredictionResponse from(Prediction prediction, Instant now) {
        return new PredictionResponse(
                prediction.getId(),
                prediction.getMatch().getId(),
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore(),
                prediction.getPredictedWinner(),
                prediction.getPointsAwarded(),
                prediction.getMatch().isPredictionLocked(now),
                prediction.getCreatedAt(),
                prediction.getUpdatedAt()
        );
    }
}
