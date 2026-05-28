package com.example.worldcup.question;

import com.example.worldcup.common.projection.UserPointsAggregation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentAnswerRepository extends JpaRepository<TournamentAnswer, Long> {

    Optional<TournamentAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);

    List<TournamentAnswer> findByUserId(Long userId);

    List<TournamentAnswer> findByQuestionId(Long questionId);

    /**
     * Aggregates question points awarded per user without entity hydration.
     */
    @Query("""
            SELECT a.user.id AS userId, COALESCE(SUM(a.pointsAwarded), 0) AS total
            FROM TournamentAnswer a
            GROUP BY a.user.id
            """)
    List<UserPointsAggregation> sumPointsByUser();
}
