package com.example.worldcup.player.dto;

import com.example.worldcup.player.PlayerPosition;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Create/update payload for a player. {@code saves} is optional and only
 * meaningful for {@link PlayerPosition#GOALKEEPER}.
 */
public record PlayerRequest(
        @NotNull Long nationalTeamId,
        @NotBlank @Size(max = 100) String name,
        @NotNull PlayerPosition position,
        @NotNull @Min(0) Integer goalsScored,
        @NotNull @Min(0) Integer yellowCards,
        @Min(0) Integer saves
) {
}
