package com.example.worldcup.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatchResultRequest(
        @Min(0) Integer homeScore,
        @Min(0) Integer awayScore,
        @Size(max = 64) String qualifiedTeam,
        @NotNull Boolean isFinished
) {
}
