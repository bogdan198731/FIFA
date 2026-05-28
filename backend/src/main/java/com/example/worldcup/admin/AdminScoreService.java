package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.ScoreRecalculationResponse;
import com.example.worldcup.common.ApiException;
import com.example.worldcup.common.projection.UserPointsAggregation;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminScoreService {

    private static final Logger log = LoggerFactory.getLogger(AdminScoreService.class);

    /** Postgres advisory lock key used to serialise the recalculation job. */
    private static final long SCORE_RECALC_LOCK_KEY = 0x5C0E_CA1CL;

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final TournamentAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ScoringService scoringService;

    @PersistenceContext
    private EntityManager entityManager;

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
        acquireRecalculationLock();

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
        acquireRecalculationLock();

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

    /**
     * Listens for match result changes published by {@code AdminMatchService}
     * and rescores the affected match after the publisher's transaction commits.
     * Failures here are logged but don't roll back the admin's edit — they can
     * always click "Recalculate scores" manually to retry.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMatchResultUpdated(MatchResultUpdatedEvent event) {
        try {
            recalculateMatch(event.matchId());
        } catch (RuntimeException ex) {
            log.error("Failed to auto-recalculate scores for match {}", event.matchId(), ex);
        }
    }

    private int recalculateQuestionPoints() {
        List<TournamentAnswer> answers = answerRepository.findAll();
        for (TournamentAnswer answer : answers) {
            answer.setPointsAwarded(scoringService.calculateQuestionPoints(answer, answer.getQuestion()));
        }
        return answers.size();
    }

    private int recalculateUserTotals() {
        // Flush the prediction/answer pointsAwarded changes so the aggregate
        // queries below see the new values rather than the pre-recalc totals.
        entityManager.flush();

        Map<Long, Long> predictionPoints = toMap(predictionRepository.sumPointsByUser());
        Map<Long, Long> answerPoints = toMap(answerRepository.sumPointsByUser());

        List<User> users = userRepository.findAll();
        for (User user : users) {
            long total = predictionPoints.getOrDefault(user.getId(), 0L)
                    + answerPoints.getOrDefault(user.getId(), 0L);
            user.setTotalPoints(total);
        }
        return users.size();
    }

    private Map<Long, Long> toMap(List<UserPointsAggregation> rows) {
        Map<Long, Long> result = new HashMap<>(rows.size());
        for (UserPointsAggregation row : rows) {
            if (row.getUserId() != null) {
                result.put(row.getUserId(), row.getTotal() == null ? 0L : row.getTotal());
            }
        }
        return result;
    }

    private void acquireRecalculationLock() {
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(?1)")
                .setParameter(1, SCORE_RECALC_LOCK_KEY)
                .getSingleResult();
    }

    private int calculatePredictionPoints(Prediction prediction, Match match) {
        if (match.getType() == MatchType.KNOCKOUT) {
            return scoringService.calculateKnockoutMatchPoints(prediction, match);
        }
        return scoringService.calculateRegularMatchPoints(prediction, match);
    }
}
