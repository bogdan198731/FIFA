package com.example.worldcup.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByOrderByKickoffAtAsc();

    List<Match> findByFinishedTrue();

    List<Match> findByStage(MatchStage stage);

    List<Match> findByFinishedFalseAndKickoffAtAfterOrderByKickoffAtAsc(Instant after);

    Optional<Match> findByApiFixtureId(Long apiFixtureId);
}
