package com.example.worldcup.league;

import com.example.worldcup.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "private_leagues")
public class PrivateLeague {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "invite_code", nullable = false, length = 8, unique = true)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PrivateLeague() {}

    public PrivateLeague(String name, String inviteCode, User owner) {
        this.name = name;
        this.inviteCode = inviteCode;
        this.owner = owner;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getInviteCode() { return inviteCode; }
    public User getOwner() { return owner; }
    public Instant getCreatedAt() { return createdAt; }
}
