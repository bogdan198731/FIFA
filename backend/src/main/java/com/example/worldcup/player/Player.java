package com.example.worldcup.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "team_name", nullable = false, length = 64)
    private String teamName;

    @Column(nullable = false, length = 30)
    private String position;

    @Column(length = 500)
    private String photo;

    @Column(name = "api_player_id")
    private Long apiPlayerId;

    @Column(name = "api_team_id")
    private Integer apiTeamId;

    public Player() {}

    public Player(String name, String teamName, String position, String photo,
                  Long apiPlayerId, Integer apiTeamId) {
        this.name = name;
        this.teamName = teamName;
        this.position = position;
        this.photo = photo;
        this.apiPlayerId = apiPlayerId;
        this.apiTeamId = apiTeamId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public Long getApiPlayerId() { return apiPlayerId; }
    public Integer getApiTeamId() { return apiTeamId; }
    public void setApiTeamId(Integer apiTeamId) { this.apiTeamId = apiTeamId; }
}
