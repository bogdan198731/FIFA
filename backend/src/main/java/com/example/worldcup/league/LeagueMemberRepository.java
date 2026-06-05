package com.example.worldcup.league;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueMemberRepository extends JpaRepository<LeagueMember, Long> {

    boolean existsByLeagueIdAndUserId(Long leagueId, Long userId);

    List<LeagueMember> findByLeagueId(Long leagueId);

    @Query("SELECT m FROM LeagueMember m JOIN FETCH m.league l JOIN FETCH l.owner WHERE m.user.id = :userId")
    List<LeagueMember> findByUserIdWithLeague(@Param("userId") Long userId);

    int countByLeagueId(Long leagueId);
}
