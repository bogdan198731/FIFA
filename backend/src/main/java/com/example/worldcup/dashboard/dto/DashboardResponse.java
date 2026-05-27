package com.example.worldcup.dashboard.dto;

import com.example.worldcup.match.dto.MatchResponse;

import java.util.List;

public record DashboardResponse(
        Integer currentRank,
        long totalPoints,
        List<MatchResponse> upcomingMatches,
        List<RecentPredictionResult> recentPredictionResults,
        long unansweredTournamentQuestionsCount
) {
}
