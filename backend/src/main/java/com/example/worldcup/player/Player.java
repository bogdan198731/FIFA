package com.example.worldcup.player;

import com.example.worldcup.team.NationalTeam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A player belonging to a {@link NationalTeam}. Maps to {@code players}
 * (V8 migration).
 *
 * <p>{@link #saves} is a goalkeeper-only stat — it stays {@code null} for
 * outfield players.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "national_team_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_players_national_team")
    )
    private NationalTeam nationalTeam;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlayerPosition position;

    @Column(name = "goals_scored", nullable = false)
    private int goalsScored;

    @Column(name = "yellow_cards", nullable = false)
    private int yellowCards;

    /** Goalkeeper-only. {@code null} for outfield players. */
    @Column
    private Integer saves;

    protected Player() {
    }

    public Player(NationalTeam nationalTeam, String name, PlayerPosition position) {
        this.nationalTeam = nationalTeam;
        this.name = name;
        this.position = position;
        this.goalsScored = 0;
        this.yellowCards = 0;
    }

    public Long getId() {
        return id;
    }

    public NationalTeam getNationalTeam() {
        return nationalTeam;
    }

    public void setNationalTeam(NationalTeam nationalTeam) {
        this.nationalTeam = nationalTeam;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerPosition getPosition() {
        return position;
    }

    public void setPosition(PlayerPosition position) {
        this.position = position;
    }

    public int getGoalsScored() {
        return goalsScored;
    }

    public void setGoalsScored(int goalsScored) {
        this.goalsScored = goalsScored;
    }

    public int getYellowCards() {
        return yellowCards;
    }

    public void setYellowCards(int yellowCards) {
        this.yellowCards = yellowCards;
    }

    public Integer getSaves() {
        return saves;
    }

    public void setSaves(Integer saves) {
        this.saves = saves;
    }
}
