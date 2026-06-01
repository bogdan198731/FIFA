package com.example.worldcup.player.dto;

import com.example.worldcup.player.Player;
import com.example.worldcup.player.PlayerPosition;

/**
 * View of a {@link Player}. {@code saves} is {@code null} for non-goalkeepers.
 */
public record PlayerResponse(
        Long id,
        Long nationalTeamId,
        String nationalTeamName,
        String name,
        PlayerPosition position,
        int goalsScored,
        int yellowCards,
        Integer saves
) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getNationalTeam().getId(),
                player.getNationalTeam().getName(),
                player.getName(),
                player.getPosition(),
                player.getGoalsScored(),
                player.getYellowCards(),
                player.getSaves()
        );
    }
}
