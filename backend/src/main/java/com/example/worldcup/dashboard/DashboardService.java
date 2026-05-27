package com.example.worldcup.dashboard;

import com.example.worldcup.common.ApiException;
import com.example.worldcup.dashboard.dto.DashboardResponse;
import com.example.worldcup.dashboard.dto.PredictionStatistics;
import com.example.worldcup.dashboard.dto.RecentPredictionResult;
import com.example.worldcup.leaderboard.LeaderboardEntry;
import com.example.worldcup.leaderboard.LeaderboardService;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.match.dto.MatchResponse;
import com.example.worldcup.prediction.Prediction;
import com.example.worldcup.prediction.PredictionRepository;
import com.example.worldcup.question.TournamentAnswerRepository;
import com.example.worldcup.question.TournamentQuestionRepository;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final TournamentQuestionRepository questionRepository;
    private final TournamentAnswerRepository answerRepository;
    private final LeaderboardService leaderboardService;

    public DashboardService(UserRepository userRepository,
                            MatchRepository matchRepository,
                            PredictionRepository predictionRepository,
                            TournamentQuestionRepository questionRepository,
                            TournamentAnswerRepository answerRepository,
                            LeaderboardService leaderboardService) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.leaderboardService = leaderboardService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User no longer exists"));
        Instant now = Instant.now();

        Integer rank = leaderboardService.getLeaderboard().stream()
                .filter(entry -> entry.userId().equals(userId))
                .findFirst()
                .map(LeaderboardEntry::rank)
                .orElse(null);

        List<Prediction> userPredictions = predictionRepository.findByUserId(userId);
        PredictionStatistics statistics = buildPredictionStatistics(userPredictions);

        var upcomingMatches = matchRepository.findByFinishedFalseAndKickoffAtAfterOrderByKickoffAtAsc(now)
                .stream()
                .limit(5)
                .map(match -> MatchResponse.from(match, now))
                .toList();

        var recentResults = userPredictions.stream()
                .filter(prediction -> prediction.getMatch().isFinished())
                .sorted(Comparator.comparing((Prediction p) -> p.getMatch().getKickoffAt()).reversed())
                .limit(5)
                .map(RecentPredictionResult::from)
                .toList();

        Set<Long> answeredQuestionIds = answerRepository.findByUserId(userId).stream()
                .map(answer -> answer.getQuestion().getId())
                .collect(Collectors.toSet());
        long unansweredCount = questionRepository.findAll().stream()
                .filter(question -> !answeredQuestionIds.contains(question.getId()))
                .count();

        return new DashboardResponse(
                rank,
                user.getTotalPoints(),
                statistics,
                upcomingMatches,
                recentResults,
                unansweredCount
        );
    }

    private PredictionStatistics buildPredictionStatistics(List<Prediction> predictions) {
        int total = predictions.size();
        int scored = 0;
        int points = 0;
        int best = 0;

        for (Prediction prediction : predictions) {
            if (prediction.getMatch().isFinished()) {
                scored++;
                points += prediction.getPointsAwarded();
                best = Math.max(best, prediction.getPointsAwarded());
            }
        }

        double average = scored == 0 ? 0.0 : (double) points / scored;
        return new PredictionStatistics(
                total,
                scored,
                total - scored,
                points,
                average,
                best
        );
    }
}
