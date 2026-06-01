package com.example.worldcup.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByNationalTeamId(Long nationalTeamId);

    List<Player> findByPosition(PlayerPosition position);

    List<Player> findAllByOrderByGoalsScoredDesc();
}
