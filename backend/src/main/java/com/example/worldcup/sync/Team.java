package com.example.worldcup.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Cache of upstream team metadata, populated by the sync agent. We keep team
 * names as plain strings on {@link com.example.worldcup.match.Match} so the
 * domain model isn't tangled into the integration, but this table gives the
 * agent a stable mapping from the provider's external IDs back to a team
 * record we control.
 */
@Entity
@Table(
        name = "teams",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_teams_external_id",
                columnNames = "external_id"
        )
)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, length = 64)
    private String externalId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "short_code", length = 8)
    private String shortCode;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    protected Team() {
    }

    public Team(String externalId, String name, String shortCode) {
        this.externalId = externalId;
        this.name = name;
        this.shortCode = shortCode;
        this.lastSyncedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
