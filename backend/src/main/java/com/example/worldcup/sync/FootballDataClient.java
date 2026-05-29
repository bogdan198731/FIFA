package com.example.worldcup.sync;

import com.example.worldcup.sync.dto.ExternalMatch;
import com.example.worldcup.sync.dto.ExternalTeam;

import java.util.List;

/**
 * Vendor-agnostic gateway to an upstream football data provider. Each
 * implementation is responsible for translating the vendor's response shape
 * into the canonical {@link ExternalTeam} / {@link ExternalMatch} records.
 *
 * <p>New providers can be added by implementing this interface and toggling
 * {@code app.football-sync.provider} to the value matched by the new bean's
 * {@code @ConditionalOnProperty}.
 */
public interface FootballDataClient {

    List<ExternalTeam> fetchTeams(String competitionId);

    List<ExternalMatch> fetchMatches(String competitionId);
}
