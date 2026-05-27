package com.example.worldcup.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByOrderByKickoffAtAsc();

    List<Match> findByStage(MatchStage stage);

    List<Match> findByFinishedFalseAndKickoffAtAfter(Instant after);
}
