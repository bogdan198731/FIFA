package com.example.worldcup.common.scoring;

import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchStage;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.prediction.Prediction;
import com.example.worldcup.question.TournamentAnswer;
import com.example.worldcup.question.TournamentQuestion;
import com.example.worldcup.user.Role;
import com.example.worldcup.user.User;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();
    private final User user = new User("alice", "alice@example.com", "hash", Role.USER);

    @Test
    void regularMatchAwardsFivePointsForExactScore() {
        Match match = regularMatch(2, 1);
        Prediction prediction = prediction(match, 2, 1);

        assertThat(scoringService.calculateRegularMatchPoints(prediction, match)).isEqualTo(5);
    }

    @Test
    void regularMatchAwardsThreePointsForCorrectWinner() {
        Match match = regularMatch(2, 1);
        Prediction prediction = prediction(match, 3, 0);

        assertThat(scoringService.calculateRegularMatchPoints(prediction, match)).isEqualTo(3);
    }

    @Test
    void regularMatchAwardsThreePointsForCorrectDrawOutcome() {
        Match match = regularMatch(1, 1);
        Prediction prediction = prediction(match, 0, 0);

        assertThat(scoringService.calculateRegularMatchPoints(prediction, match)).isEqualTo(3);
    }

    @Test
    void regularMatchAwardsZeroForWrongOutcome() {
        Match match = regularMatch(2, 1);
        Prediction prediction = prediction(match, 0, 1);

        assertThat(scoringService.calculateRegularMatchPoints(prediction, match)).isZero();
    }

    @Test
    void knockoutMatchAwardsSevenForExactScoreAndCorrectQualifiedTeam() {
        Match match = knockoutMatch(2, 2, "Brazil");
        Prediction prediction = prediction(match, 2, 2, "Brazil");

        assertThat(scoringService.calculateKnockoutMatchPoints(prediction, match)).isEqualTo(7);
    }

    @Test
    void knockoutMatchAwardsFourForCorrectQualifiedTeamOnly() {
        Match match = knockoutMatch(2, 2, "Brazil");
        Prediction prediction = prediction(match, 1, 0, "Brazil");

        assertThat(scoringService.calculateKnockoutMatchPoints(prediction, match)).isEqualTo(4);
    }

    @Test
    void knockoutMatchAwardsTwoForExactScoreOnly() {
        Match match = knockoutMatch(2, 2, "Brazil");
        Prediction prediction = prediction(match, 2, 2, "France");

        assertThat(scoringService.calculateKnockoutMatchPoints(prediction, match)).isEqualTo(2);
    }

    @Test
    void knockoutMatchAwardsZeroForWrongScoreAndWrongQualifiedTeam() {
        Match match = knockoutMatch(2, 2, "Brazil");
        Prediction prediction = prediction(match, 1, 0, "France");

        assertThat(scoringService.calculateKnockoutMatchPoints(prediction, match)).isZero();
    }

    @Test
    void questionAwardsConfiguredPointsForMatchingAnswerIgnoringCaseAndWhitespace() {
        TournamentQuestion question = question("Brazil", 20);
        TournamentAnswer answer = new TournamentAnswer(user, question, " brazil ");

        assertThat(scoringService.calculateQuestionPoints(answer, question)).isEqualTo(20);
    }

    @Test
    void questionAwardsZeroForWrongAnswer() {
        TournamentQuestion question = question("Brazil", 20);
        TournamentAnswer answer = new TournamentAnswer(user, question, "France");

        assertThat(scoringService.calculateQuestionPoints(answer, question)).isZero();
    }

    @Test
    void unfinishedMatchesScoreZero() {
        Match match = regularMatch(null, null);
        Prediction prediction = prediction(match, 2, 1);

        assertThat(scoringService.calculateRegularMatchPoints(prediction, match)).isZero();
    }

    private Match regularMatch(Integer homeScore, Integer awayScore) {
        Match match = new Match("Brazil", "France", Instant.now(), MatchType.REGULAR, MatchStage.GROUP);
        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setFinished(homeScore != null && awayScore != null);
        return match;
    }

    private Match knockoutMatch(Integer homeScore, Integer awayScore, String winner) {
        Match match = new Match("Brazil", "France", Instant.now(), MatchType.KNOCKOUT, MatchStage.FINAL);
        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setKnockoutWinner(winner);
        match.setFinished(homeScore != null && awayScore != null);
        return match;
    }

    private Prediction prediction(Match match, int homeScore, int awayScore) {
        return new Prediction(user, match, homeScore, awayScore);
    }

    private Prediction prediction(Match match, int homeScore, int awayScore, String winner) {
        Prediction prediction = prediction(match, homeScore, awayScore);
        prediction.setPredictedWinner(winner);
        return prediction;
    }

    private TournamentQuestion question(String correctAnswer, int points) {
        TournamentQuestion question = new TournamentQuestion(
                "Who wins?",
                Instant.now().minusSeconds(60),
                points
        );
        question.setCorrectAnswer(correctAnswer);
        return question;
    }
}
