package com.example.worldcup.dashboard.dto;

public record PredictionStatistics(
        int totalPredictions,
        int scoredPredictions,
        int pendingPredictions,
        int totalPredictionPoints,
        double averagePointsPerScoredPrediction,
        int bestPredictionPoints
) {
}
