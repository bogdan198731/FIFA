package com.example.worldcup.admin.dto;

import com.example.worldcup.match.MatchStage;
import com.example.worldcup.match.MatchType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AdminMatchRequest(
        @NotBlank @Size(max = 64) String homeTeam,
        @NotBlank @Size(max = 64) String awayTeam,
        @NotNull Instant kickoffTime,
        @Size(max = 128) String venue,
        @NotNull MatchStage stage,
        @NotNull MatchType matchType,
        @Min(0) Integer homeScore,
        @Min(0) Integer awayScore,
        @Size(max = 64) String qualifiedTeam,
        @NotNull Boolean isFinished
) {
}
