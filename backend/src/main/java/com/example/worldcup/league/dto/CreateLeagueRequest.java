package com.example.worldcup.league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLeagueRequest(
        @NotBlank @Size(min = 2, max = 100) String name
) {}
