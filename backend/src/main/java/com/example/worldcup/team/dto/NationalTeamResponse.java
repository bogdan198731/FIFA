package com.example.worldcup.team.dto;

import com.example.worldcup.team.NationalTeam;

public record NationalTeamResponse(Long id, String name) {

    public static NationalTeamResponse from(NationalTeam team) {
        return new NationalTeamResponse(team.getId(), team.getName());
    }
}
