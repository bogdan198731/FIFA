package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.AdminMatchRequest;
import com.example.worldcup.admin.dto.MatchResultRequest;
import com.example.worldcup.common.ApiException;
import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.match.PredictionLockOverride;
import com.example.worldcup.match.dto.MatchResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdminMatchService {

    private final MatchRepository matchRepository;

    public AdminMatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional
    public MatchResponse create(AdminMatchRequest req) {
        Match match = new Match(
                req.homeTeam().trim(),
                req.awayTeam().trim(),
                req.kickoffTime(),
                req.matchType(),
                req.stage()
        );
        applyMatchFields(match, req);
        matchRepository.save(match);
        return MatchResponse.from(match, Instant.now());
    }

    @Transactional
    public MatchResponse update(Long matchId, AdminMatchRequest req) {
        Match match = getMatch(matchId);
        applyIdentityFields(match, req);
        applyResultFields(match, req.homeScore(), req.awayScore(), req.qualifiedTeam(), req.isFinished());
        return MatchResponse.from(match, Instant.now());
    }

    @Transactional
    public void delete(Long matchId) {
        Match match = getMatch(matchId);
        matchRepository.delete(match);
    }

    @Transactional
    public MatchResponse updateResult(Long matchId, MatchResultRequest req) {
        Match match = getMatch(matchId);
        applyResultFields(match, req.homeScore(), req.awayScore(), req.qualifiedTeam(), req.isFinished());
        return MatchResponse.from(match, Instant.now());
    }

    @Transactional
    public MatchResponse lock(Long matchId) {
        Match match = getMatch(matchId);
        match.setPredictionLockOverride(PredictionLockOverride.LOCKED);
        return MatchResponse.from(match, Instant.now());
    }

    @Transactional
    public MatchResponse unlock(Long matchId) {
        Match match = getMatch(matchId);
        match.setPredictionLockOverride(PredictionLockOverride.UNLOCKED);
        return MatchResponse.from(match, Instant.now());
    }

    private Match getMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
    }

    private void applyMatchFields(Match match, AdminMatchRequest req) {
        applyIdentityFields(match, req);
        applyResultFields(match, req.homeScore(), req.awayScore(), req.qualifiedTeam(), req.isFinished());
    }

    private void applyIdentityFields(Match match, AdminMatchRequest req) {
        match.setHomeTeam(req.homeTeam().trim());
        match.setAwayTeam(req.awayTeam().trim());
        match.setKickoffAt(req.kickoffTime());
        match.setVenue(trimToNull(req.venue()));
        match.setStage(req.stage());
        match.setType(req.matchType());
    }

    private void applyResultFields(Match match,
                                   Integer homeScore,
                                   Integer awayScore,
                                   String qualifiedTeam,
                                   boolean finished) {
        if (finished && (homeScore == null || awayScore == null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Finished matches require a final score");
        }

        String normalizedQualifiedTeam = trimToNull(qualifiedTeam);
        if (match.getType() == MatchType.KNOCKOUT && finished) {
            if (normalizedQualifiedTeam == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Knockout results require a qualified team");
            }
            if (!normalizedQualifiedTeam.equals(match.getHomeTeam())
                    && !normalizedQualifiedTeam.equals(match.getAwayTeam())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Qualified team must be one of the match teams");
            }
        }

        match.setHomeScore(finished ? homeScore : null);
        match.setAwayScore(finished ? awayScore : null);
        match.setKnockoutWinner(match.getType() == MatchType.KNOCKOUT && finished
                ? normalizedQualifiedTeam
                : null);
        match.setFinished(finished);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
