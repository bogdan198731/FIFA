package com.example.worldcup.question.dto;

import com.example.worldcup.question.TournamentAnswer;

import java.time.Instant;

public record TournamentAnswerResponse(
        Long id,
        Long questionId,
        String answer,
        int pointsAwarded,
        boolean locked,
        Instant createdAt,
        Instant updatedAt
) {

    public static TournamentAnswerResponse from(TournamentAnswer answer, Instant now) {
        boolean locked = !now.isBefore(answer.getQuestion().getDeadline());
        return new TournamentAnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getAnswer(),
                answer.getPointsAwarded(),
                locked,
                answer.getCreatedAt(),
                answer.getUpdatedAt()
        );
    }
}
