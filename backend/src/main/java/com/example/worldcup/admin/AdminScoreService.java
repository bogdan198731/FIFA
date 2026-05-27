package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.ScoreRecalculationResponse;
import com.example.worldcup.common.ApiException;
import com.example.worldcup.common.scoring.ScoringService;
import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.prediction.Prediction;
import com.example.worldcup.prediction.PredictionRepository;
import com.example.worldcup.question.TournamentAnswer;
import com.example.worldcup.question.TournamentAnswerRepository;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminScoreService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final TournamentAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ScoringService scoringService;

    public AdminScoreService(MatchRepository matchRepository,
                             PredictionRepository predictionRepository,
                             TournamentAnswerRepository answerRepository,
                             UserRepository userRepository,
                             ScoringService scoringService) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.scoringService = scoringService;
    }

    @Transactional
    public ScoreRecalculationResponse recalculateAll() {
        int matchesScored = 0;
        int predictionsScored = 0;

        for (Match match : matchRepository.findAll()) {
            if (match.isFinished()) {
                matchesScored++;
            }
            for (Prediction prediction : predictionRepository.findByMatchId(match.getId())) {
                prediction.setPointsAwarded(match.isFinished() ? calculatePredictionPoints(prediction, match) : 0);
                predictionsScored++;
            }
        }

        int answersScored = recalculateQuestionPoints();
        int usersUpdated = recalculateUserTotals();
        return new ScoreRecalculationResponse(matchesScored, predictionsScored, answersScored, usersUpdated);
    }

    @Transactional
    public ScoreRecalculationResponse recalculateMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));

        List<Prediction> predictions = predictionRepository.findByMatchId(match.getId());
        for (Prediction prediction : predictions) {
            prediction.setPointsAwarded(match.isFinished() ? calculatePredictionPoints(prediction, match) : 0);
        }

        int usersUpdated = recalculateUserTotals();
        return new ScoreRecalculationResponse(
                match.isFinished() ? 1 : 0,
                predictions.size(),
                0,
                usersUpdated
        );
    }

    private int recalculateQuestionPoints() {
        List<TournamentAnswer> answers = answerRepository.findAll();
        for (TournamentAnswer answer : answers) {
            answer.setPointsAwarded(scoringService.calculateQuestionPoints(answer, answer.getQuestion()));
        }
        return answers.size();
    }

    private int recalculateUserTotals() {
        List<User> users = userRepository.findAll();
        Map<Long, Integer> predictionPoints = predictionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        prediction -> prediction.getUser().getId(),
                        Collectors.summingInt(Prediction::getPointsAwarded)
                ));
        Map<Long, Integer> answerPoints = answerRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        answer -> answer.getUser().getId(),
                        Collectors.summingInt(TournamentAnswer::getPointsAwarded)
                ));

        for (User user : users) {
            int total = predictionPoints.getOrDefault(user.getId(), 0)
                    + answerPoints.getOrDefault(user.getId(), 0);
            user.setTotalPoints(total);
        }
        return users.size();
    }

    private int calculatePredictionPoints(Prediction prediction, Match match) {
        if (match.getType() == MatchType.KNOCKOUT) {
            return scoringService.calculateKnockoutMatchPoints(prediction, match);
        }
        return scoringService.calculateRegularMatchPoints(prediction, match);
    }
}
