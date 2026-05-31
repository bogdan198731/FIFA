package com.example.worldcup.question.dto;

import com.example.worldcup.question.TournamentQuestion;

import java.time.Instant;
import java.util.List;

public record TournamentQuestionResponse(
        Long id,
        String text,
        Instant deadline,
        Integer points,
        boolean locked,
        String correctAnswer,
        List<String> options
) {

    public static TournamentQuestionResponse from(TournamentQuestion q, Instant now) {
        boolean locked = !now.isBefore(q.getDeadline());
        String correct = locked ? q.getCorrectAnswer() : null;
        return new TournamentQuestionResponse(
                q.getId(),
                q.getText(),
                q.getDeadline(),
                q.getPoints(),
                locked,
                correct,
                q.getOptions()
        );
    }
}
