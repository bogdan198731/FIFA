package com.example.worldcup.team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A national team present in the tournament. Maps to {@code national_teams}
 * (V8 migration): an auto-increment id and a unique team name.
 */
@Entity
@Table(
        name = "national_teams",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_national_teams_name",
                columnNames = "name"
        )
)
public class NationalTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    protected NationalTeam() {
    }

    public NationalTeam(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
