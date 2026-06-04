package com.example.worldcup.match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dev-only demo data: gives a fresh local database a grouped set of
 * group-stage fixtures so the foldable "group by group" predictions view has
 * something to show out of the box.
 *
 * <p>Runs only under the {@code dev} profile (never in production). It's
 * idempotent — it backfills the group label on matching existing matches (e.g.
 * the ones from {@code V2__seed_data.sql}) and inserts the rest of each group's
 * round-robin only when missing, so repeated restarts don't duplicate.
 *
 * <p>The 12 groups (A–L) of four match the WC 2026 format. Team→group
 * assignments are illustrative — adjust the names below to match the official
 * draw / the exact team names your synced fixtures use if you need it precise.
 */
@Component
@Profile("dev")
public class DevGroupMatchSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevGroupMatchSeeder.class);

    /** 12 groups → their four teams (48 teams total, WC 2026 format). */
    private static final Map<String, List<String>> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put("A", List.of("Brazil", "Serbia", "Switzerland", "Cameroon"));
        GROUPS.put("B", List.of("France", "Australia", "Denmark", "Tunisia"));
        GROUPS.put("C", List.of("Argentina", "Mexico", "Poland", "Saudi Arabia"));
        GROUPS.put("D", List.of("England", "USA", "Netherlands", "Senegal"));
        GROUPS.put("E", List.of("Spain", "Germany", "Japan", "Costa Rica"));
        GROUPS.put("F", List.of("Portugal", "Uruguay", "South Korea", "Ghana"));
        GROUPS.put("G", List.of("Belgium", "Croatia", "Morocco", "Canada"));
        GROUPS.put("H", List.of("Italy", "Colombia", "Ecuador", "Qatar"));
        GROUPS.put("I", List.of("Nigeria", "Egypt", "Iran", "Wales"));
        GROUPS.put("J", List.of("Sweden", "Norway", "Austria", "Ukraine"));
        GROUPS.put("K", List.of("Peru", "Chile", "Algeria", "Ivory Coast"));
        GROUPS.put("L", List.of("Paraguay", "Scotland", "Turkey", "Greece"));
    }

    private final MatchRepository matchRepository;

    public DevGroupMatchSeeder(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Match> existingByPair = new HashMap<>();
        for (Match match : matchRepository.findByStage(MatchStage.GROUP)) {
            existingByPair.put(pairKey(match.getHomeTeam(), match.getAwayTeam()), match);
        }

        Instant kickoff = Instant.parse("2026-06-14T16:00:00Z");
        int inserted = 0;
        int backfilled = 0;

        for (Map.Entry<String, List<String>> group : GROUPS.entrySet()) {
            String label = group.getKey();
            List<String> teams = group.getValue();
            for (int i = 0; i < teams.size(); i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    String home = teams.get(i);
                    String away = teams.get(j);
                    Match existing = existingByPair.get(pairKey(home, away));
                    if (existing != null) {
                        if (existing.getGroupName() == null) {
                            existing.setGroupName(label);
                            backfilled++;
                        }
                    } else {
                        Match match = new Match(home, away, kickoff, MatchType.REGULAR, MatchStage.GROUP);
                        match.setGroupName(label);
                        matchRepository.save(match);
                        inserted++;
                    }
                    kickoff = kickoff.plus(Duration.ofHours(3));
                }
            }
        }

        if (inserted > 0 || backfilled > 0) {
            log.info("Dev group-stage demo: inserted {} matches, backfilled {} group labels.",
                    inserted, backfilled);
        }
    }

    private static String pairKey(String home, String away) {
        return home + "|" + away;
    }
}
