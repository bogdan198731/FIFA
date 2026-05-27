package com.example.worldcup.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tournament_questions")
public class TournamentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "deadline", nullable = false)
    private Instant deadline;

    @Column(name = "correct_answer", length = 200)
    private String correctAnswer;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TournamentQuestion() {
    }

    public TournamentQuestion(String text, Instant deadline, Integer points) {
        this.text = text;
        this.deadline = deadline;
        this.points = points;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
