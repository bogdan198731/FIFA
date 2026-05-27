package com.example.worldcup.admin.dto;

public record ScoreRecalculationResponse(
        int matchesScored,
        int predictionsScored,
        int answersScored,
        int usersUpdated
) {
}
