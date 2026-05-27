package com.example.worldcup.prediction;

import com.example.worldcup.common.ApiException;
import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.prediction.dto.PredictionRequest;
import com.example.worldcup.prediction.dto.PredictionResponse;
import com.example.worldcup.prediction.dto.UpdatePredictionRequest;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public PredictionService(PredictionRepository predictionRepository,
                             MatchRepository matchRepository,
                             UserRepository userRepository) {
        this.predictionRepository = predictionRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PredictionResponse> myPredictions(Long userId) {
        Instant now = Instant.now();
        return predictionRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(p -> p.getMatch().getKickoffAt()))
                .map(prediction -> PredictionResponse.from(prediction, now))
                .toList();
    }

    @Transactional
    public PredictionResponse submit(Long userId, PredictionRequest req) {
        Match match = matchRepository.findById(req.matchId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
        Instant now = Instant.now();
        ensureNotLocked(match, now);

        if (predictionRepository.findByUserIdAndMatchId(userId, match.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A prediction already exists for this match - update it instead");
        }

        String qualifiedTeam = validateQualifiedTeam(match, req.qualifiedTeam());
        User user = userRepository.getReferenceById(userId);
        Prediction prediction = new Prediction(user, match, req.homeScore(), req.awayScore());
        prediction.setPredictedWinner(qualifiedTeam);
        predictionRepository.save(prediction);
        return PredictionResponse.from(prediction, now);
    }

    @Transactional
    public PredictionResponse update(Long userId, Long predictionId, UpdatePredictionRequest req) {
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prediction not found"));

        if (!Objects.equals(prediction.getUser().getId(), userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot edit another user's prediction");
        }

        Instant now = Instant.now();
        ensureNotLocked(prediction.getMatch(), now);

        String qualifiedTeam = validateQualifiedTeam(prediction.getMatch(), req.qualifiedTeam());
        prediction.setPredictedHomeScore(req.homeScore());
        prediction.setPredictedAwayScore(req.awayScore());
        prediction.setPredictedWinner(qualifiedTeam);
        return PredictionResponse.from(prediction, now);
    }

    private void ensureNotLocked(Match match, Instant now) {
        if (match.isPredictionLocked(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This match is locked");
        }
    }

    private String validateQualifiedTeam(Match match, String qualifiedTeam) {
        if (match.getType() == MatchType.REGULAR) {
            return null;
        }

        if (qualifiedTeam == null || qualifiedTeam.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Qualified team is required for knockout matches");
        }

        String normalized = qualifiedTeam.trim();
        if (!normalized.equals(match.getHomeTeam()) && !normalized.equals(match.getAwayTeam())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Qualified team must be one of the match teams");
        }
        return normalized;
    }
}
