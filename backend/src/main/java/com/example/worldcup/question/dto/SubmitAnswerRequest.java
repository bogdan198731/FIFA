package com.example.worldcup.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitAnswerRequest(
        @NotNull Long questionId,
        @NotBlank @Size(max = 200) String answer
) {
}
