package com.example.worldcup.team;

import com.example.worldcup.player.Player;
import com.example.worldcup.player.PlayerPosition;
import com.example.worldcup.player.PlayerRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the V8 entities map cleanly: the national_teams/players tables,
 * the players → national_teams foreign key, enum persistence, and the
 * goalkeeper-only {@code saves} column (set for keepers, null for outfielders).
 *
 * <p>Runs against H2 with Flyway disabled (the real migrations are
 * PostgreSQL-specific) and Hibernate generating the schema from the entities.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // application.yml pins PostgreSQLDialect; override to H2 so Hibernate
        // emits H2-compatible SQL (the base config's dialect would otherwise
        // generate `INSERT ... RETURNING id`, which H2 rejects).
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class NationalTeamAndPlayerJpaTest {

    @Autowired TestEntityManager entityManager;
    @Autowired NationalTeamRepository nationalTeamRepository;
    @Autowired PlayerRepository playerRepository;

    @Test
    void persistsTeamWithPlayersAndGoalkeeperSaves() {
        NationalTeam brazil = nationalTeamRepository.save(new NationalTeam("Brazil"));

        Player keeper = new Player(brazil, "Alisson", PlayerPosition.GOALKEEPER);
        keeper.setSaves(12);

        Player forward = new Player(brazil, "Neymar", PlayerPosition.FORWARD);
        forward.setGoalsScored(5);
        forward.setYellowCards(2);

        playerRepository.save(keeper);
        playerRepository.save(forward);

        entityManager.flush();
        entityManager.clear();

        List<Player> squad = playerRepository.findByNationalTeamId(brazil.getId());
        assertThat(squad).hasSize(2);

        Player loadedKeeper = playerRepository.findByPosition(PlayerPosition.GOALKEEPER).get(0);
        assertThat(loadedKeeper.getSaves()).isEqualTo(12);
        assertThat(loadedKeeper.getNationalTeam().getName()).isEqualTo("Brazil");

        Player loadedForward = playerRepository.findByPosition(PlayerPosition.FORWARD).get(0);
        assertThat(loadedForward.getSaves()).isNull();
        assertThat(loadedForward.getGoalsScored()).isEqualTo(5);
        assertThat(loadedForward.getYellowCards()).isEqualTo(2);
    }

    @Test
    void nationalTeamNameLookupIsCaseInsensitive() {
        nationalTeamRepository.saveAndFlush(new NationalTeam("France"));

        assertThat(nationalTeamRepository.existsByNameIgnoreCase("france")).isTrue();
        assertThat(nationalTeamRepository.findByNameIgnoreCase("FRANCE")).isPresent();
    }
}
