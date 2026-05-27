package com.example.worldcup.prediction.dto;

import com.example.worldcup.prediction.Prediction;

import java.time.Instant;

public record PredictionResponse(
        Long id,
        Long matchId,
        Integer homeScore,
        Integer awayScore,
        String qualifiedTeam,
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
                !now.isBefore(prediction.getMatch().getKickoffAt()),
                prediction.getCreatedAt(),
                prediction.getUpdatedAt()
        );
    }
}
