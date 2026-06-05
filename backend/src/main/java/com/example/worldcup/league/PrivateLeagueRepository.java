package com.example.worldcup.league;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivateLeagueRepository extends JpaRepository<PrivateLeague, Long> {
    Optional<PrivateLeague> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
}
