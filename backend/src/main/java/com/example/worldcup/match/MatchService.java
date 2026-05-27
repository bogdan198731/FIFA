package com.example.worldcup.match;

import com.example.worldcup.match.dto.MatchResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> listAll() {
        Instant now = Instant.now();
        return matchRepository.findAllByOrderByKickoffAtAsc().stream()
                .map(match -> MatchResponse.from(match, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> listUpcoming() {
        Instant now = Instant.now();
        return matchRepository.findByFinishedFalseAndKickoffAtAfterOrderByKickoffAtAsc(now).stream()
                .map(match -> MatchResponse.from(match, now))
                .toList();
    }
}
