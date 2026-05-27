package com.example.worldcup.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentAnswerRepository extends JpaRepository<TournamentAnswer, Long> {

    Optional<TournamentAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);

    List<TournamentAnswer> findByUserId(Long userId);

    List<TournamentAnswer> findByQuestionId(Long questionId);
}
