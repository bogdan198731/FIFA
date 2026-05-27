package com.example.worldcup.common.scoring;

import com.example.worldcup.match.Match;
import com.example.worldcup.prediction.Prediction;
import com.example.worldcup.question.TournamentAnswer;
import com.example.worldcup.question.TournamentQuestion;

import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    public int calculateRegularMatchPoints(Prediction prediction, Match match) {
        if (!hasScore(prediction, match)) {
            return 0;
        }

        if (isExactScore(prediction, match)) {
            return 5;
        }

        return sameOutcome(
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore(),
                match.getHomeScore(),
                match.getAwayScore()
        ) ? 3 : 0;
    }

    public int calculateKnockoutMatchPoints(Prediction prediction, Match match) {
        if (!hasScore(prediction, match)) {
            return 0;
        }

        boolean exactScore = isExactScore(prediction, match);
        boolean qualifiedTeam = sameText(prediction.getPredictedWinner(), match.getKnockoutWinner());

        if (exactScore && qualifiedTeam) {
            return 7;
        }
        if (qualifiedTeam) {
            return 4;
        }
        if (exactScore) {
            return 2;
        }
        return 0;
    }

    public int calculateQuestionPoints(TournamentAnswer answer, TournamentQuestion question) {
        if (answer == null || question == null || question.getPoints() == null) {
            return 0;
        }
        return sameText(answer.getAnswer(), question.getCorrectAnswer()) ? question.getPoints() : 0;
    }

    private boolean hasScore(Prediction prediction, Match match) {
        return prediction != null
                && match != null
                && prediction.getPredictedHomeScore() != null
                && prediction.getPredictedAwayScore() != null
                && match.getHomeScore() != null
                && match.getAwayScore() != null;
    }

    private boolean isExactScore(Prediction prediction, Match match) {
        return prediction.getPredictedHomeScore().equals(match.getHomeScore())
                && prediction.getPredictedAwayScore().equals(match.getAwayScore());
    }

    private boolean sameOutcome(int predictedHome, int predictedAway, int actualHome, int actualAway) {
        return Integer.compare(predictedHome, predictedAway) == Integer.compare(actualHome, actualAway);
    }

    private boolean sameText(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }
}
