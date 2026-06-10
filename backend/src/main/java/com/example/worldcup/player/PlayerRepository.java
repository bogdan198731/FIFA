package com.example.worldcup.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findAllByOrderByNameAsc();

    List<Player> findByPositionOrderByNameAsc(String position);

    Optional<Player> findByApiPlayerId(Long apiPlayerId);
}
