package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.AdminMatchRequest;
import com.example.worldcup.common.ApiException;
import com.example.worldcup.match.Match;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.match.MatchStage;
import com.example.worldcup.match.MatchType;
import com.example.worldcup.match.dto.MatchResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMatchServiceGroupTest {

    @Mock MatchRepository matchRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    private AdminMatchService service;

    @BeforeEach
    void setUp() {
        service = new AdminMatchService(matchRepository, eventPublisher);
    }

    @Test
    void rejectsGroupStageMatchWithoutAGroup() {
        AdminMatchRequest req = request(MatchStage.GROUP, null, MatchType.REGULAR);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void storesGroupForGroupStageMatch() {
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchResponse res = service.create(request(MatchStage.GROUP, "A", MatchType.REGULAR));

        assertThat(res.groupName()).isEqualTo("A");
    }

    @Test
    void clearsGroupForKnockoutMatchEvenIfProvided() {
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        MatchResponse res = service.create(request(MatchStage.FINAL, "A", MatchType.KNOCKOUT));

        assertThat(res.groupName()).isNull();
    }

    private static AdminMatchRequest request(MatchStage stage, String groupName, MatchType type) {
        return new AdminMatchRequest(
                "Brazil",
                "Serbia",
                Instant.parse("2026-06-14T18:00:00Z"),
                null,
                stage,
                groupName,
                type,
                null,
                null,
                null,
                false
        );
    }
}
