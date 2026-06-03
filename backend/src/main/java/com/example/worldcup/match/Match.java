package com.example.worldcup.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "home_team", nullable = false, length = 64)
    private String homeTeam;

    @Column(name = "away_team", nullable = false, length = 64)
    private String awayTeam;

    @Column(name = "kickoff_at", nullable = false)
    private Instant kickoffAt;

    @Column(length = 128)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 20)
    private MatchStage stage;

    /**
     * Group label for group-stage matches (e.g. "A", "B"). {@code null} for
     * knockout matches.
     */
    @Column(name = "group_name", length = 16)
    private String groupName;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "knockout_winner", length = 64)
    private String knockoutWinner;

    @Column(nullable = false)
    private boolean finished;

    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_lock_override", length = 10)
    private PredictionLockOverride predictionLockOverride;

    @Column(name = "api_fixture_id", unique = true)
    private Long apiFixtureId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Match() {
    }

    public Match(String homeTeam, String awayTeam, Instant kickoffAt,
                 MatchType type, MatchStage stage) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.kickoffAt = kickoffAt;
        this.type = type;
        this.stage = stage;
        this.finished = false;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Instant getKickoffAt() {
        return kickoffAt;
    }

    public void setKickoffAt(Instant kickoffAt) {
        this.kickoffAt = kickoffAt;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    public MatchStage getStage() {
        return stage;
    }

    public void setStage(MatchStage stage) {
        this.stage = stage;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public String getKnockoutWinner() {
        return knockoutWinner;
    }

    public void setKnockoutWinner(String knockoutWinner) {
        this.knockoutWinner = knockoutWinner;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public PredictionLockOverride getPredictionLockOverride() {
        return predictionLockOverride;
    }

    public void setPredictionLockOverride(PredictionLockOverride predictionLockOverride) {
        this.predictionLockOverride = predictionLockOverride;
    }

    public boolean isPredictionLocked(Instant now) {
        if (predictionLockOverride == PredictionLockOverride.LOCKED) {
            return true;
        }
        if (predictionLockOverride == PredictionLockOverride.UNLOCKED) {
            return false;
        }
        return !now.isBefore(kickoffAt);
    }

    public Long getApiFixtureId() {
        return apiFixtureId;
    }

    public void setApiFixtureId(Long apiFixtureId) {
        this.apiFixtureId = apiFixtureId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
