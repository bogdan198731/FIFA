package com.example.worldcup.question.dto;

import com.example.worldcup.question.TournamentQuestion;

import java.time.Instant;

public record TournamentQuestionResponse(
        Long id,
        String text,
        Instant deadline,
        Integer points,
        boolean locked,
        String correctAnswer
) {

    public static TournamentQuestionResponse from(TournamentQuestion q, Instant now) {
        boolean locked = !now.isBefore(q.getDeadline());
        // Correct answers are only revealed once the question is locked, so
        // users can't peek before submitting.
        String correct = locked ? q.getCorrectAnswer() : null;
        return new TournamentQuestionResponse(
                q.getId(),
                q.getText(),
                q.getDeadline(),
                q.getPoints(),
                locked,
                correct
        );
    }
}
