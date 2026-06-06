package com.example.worldcup.question.dto;

import com.example.worldcup.question.OptionSource;
import jakarta.validation.constraints.NotNull;

public record UpdateQuestionOptionSourceRequest(
        @NotNull OptionSource optionSource
) {}
