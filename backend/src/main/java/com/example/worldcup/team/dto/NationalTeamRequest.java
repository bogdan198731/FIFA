package com.example.worldcup.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NationalTeamRequest(
        @NotBlank @Size(max = 64) String name
) {
}
