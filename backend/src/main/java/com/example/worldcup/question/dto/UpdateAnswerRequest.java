package com.example.worldcup.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAnswerRequest(
        @NotBlank @Size(max = 200) String answer
) {
}
