package com.example.worldcup.prediction;

import com.example.worldcup.common.projection.UserPointsAggregation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);

    List<Prediction> findByUserId(Long userId);

    List<Prediction> findByMatchId(Long matchId);

    /**
     * Used by the sync agent to decide whether it's safe to overwrite a
     * match's {@code kickoffAt} with an upstream value. If any user has
     * already locked in a prediction for the match, the kickoff is left
     * alone so we don't move the goalpost on them.
     */
    boolean existsByMatchId(Long matchId);

    /**
     * Fetches every prediction belonging to a user with the parent match joined
     * eagerly. Used by the dashboard so iterating {@code prediction.getMatch()}
     * doesn't trigger N+1 lazy loads.
     */
    @Query("SELECT p FROM Prediction p JOIN FETCH p.match WHERE p.user.id = :userId")
    List<Prediction> findByUserIdFetchMatch(@Param("userId") Long userId);

    /**
     * Aggregates points awarded per user in one round-trip — used by the
     * recalculation job in place of {@code findAll().stream().groupingBy(...)}.
     */
    @Query("""
            SELECT p.user.id AS userId, COALESCE(SUM(p.pointsAwarded), 0) AS total
            FROM Prediction p
            GROUP BY p.user.id
            """)
    List<UserPointsAggregation> sumPointsByUser();
}
