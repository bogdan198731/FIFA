package com.example.worldcup.leaderboard;

public record LeaderboardEntry(int rank, Long userId, String username, long totalPoints) {
}
