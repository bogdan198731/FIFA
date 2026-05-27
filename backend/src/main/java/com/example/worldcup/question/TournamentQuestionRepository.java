package com.example.worldcup.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TournamentQuestionRepository extends JpaRepository<TournamentQuestion, Long> {

    List<TournamentQuestion> findByDeadlineAfter(Instant after);
}
