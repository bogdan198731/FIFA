package com.example.worldcup.sync;

import com.example.worldcup.match.MatchStage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parsing the group label out of API-Football's fixture round.
 */
class WorldCupSyncPersisterGroupTest {

    @Test
    void extractsTheGroupLetter() {
        assertThat(WorldCupSyncPersister.toGroup("Group A - 1", MatchStage.GROUP)).isEqualTo("A");
        assertThat(WorldCupSyncPersister.toGroup("Group L - 3", MatchStage.GROUP)).isEqualTo("L");
        assertThat(WorldCupSyncPersister.toGroup("group b - 2", MatchStage.GROUP)).isEqualTo("B");
    }

    @Test
    void returnsNullForGenericGroupStageRound() {
        // The bug this guards against: "Group Stage - 1" must not become "STAGE".
        assertThat(WorldCupSyncPersister.toGroup("Group Stage - 1", MatchStage.GROUP)).isNull();
    }

    @Test
    void returnsNullForKnockoutOrMissingRound() {
        assertThat(WorldCupSyncPersister.toGroup("Round of 16", MatchStage.ROUND_OF_16)).isNull();
        assertThat(WorldCupSyncPersister.toGroup("Group A - 1", MatchStage.FINAL)).isNull();
        assertThat(WorldCupSyncPersister.toGroup(null, MatchStage.GROUP)).isNull();
    }
}
