package com.diceduel.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rounds")
public class RoundEntity {

    @Id
    private String id;

    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    private RoundStatus status;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerRoundStateEntity> playerStates = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    public RoundEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
        this.status = status;
    }

    public List<PlayerRoundStateEntity> getPlayerStates() {
        return playerStates;
    }

    public void setPlayerStates(List<PlayerRoundStateEntity> playerStates) {
        this.playerStates = playerStates;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public void setMatch(MatchEntity match) {
        this.match = match;
    }
}
