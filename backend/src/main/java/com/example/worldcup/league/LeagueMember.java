package com.example.worldcup.league;

import com.example.worldcup.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "league_members")
public class LeagueMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private PrivateLeague league;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected LeagueMember() {}

    public LeagueMember(PrivateLeague league, User user) {
        this.league = league;
        this.user = user;
        this.joinedAt = Instant.now();
    }

    public Long getId() { return id; }
    public PrivateLeague getLeague() { return league; }
    public User getUser() { return user; }
    public Instant getJoinedAt() { return joinedAt; }
}
