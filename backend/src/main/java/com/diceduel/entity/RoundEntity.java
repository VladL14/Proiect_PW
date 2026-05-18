package com.diceduel.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

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

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<DiceFace> dice = new ArrayList<>();

    @ElementCollection
    private List<Boolean> locked = new ArrayList<>();

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundPlayerStateEntity> playerStates = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "round_action_logs", joinColumns = @JoinColumn(name = "round_id"))
    @OrderColumn(name = "log_index")
    @Column(name = "message", length = 500)
    private List<String> actionLogs = new ArrayList<>();

    @Column(length = 1000)
    private String roundSummary;

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

    public List<DiceFace> getDice() {
        return dice;
    }

    public void setDice(List<DiceFace> dice) {
        this.dice = dice;
    }

    public List<Boolean> getLocked() {
        return locked;
    }

    public void setLocked(List<Boolean> locked) {
        this.locked = locked;
    }

    public List<RoundPlayerStateEntity> getPlayerStates() {
        return playerStates;
    }

    public void setPlayerStates(List<RoundPlayerStateEntity> playerStates) {
        this.playerStates = playerStates;
    }

    public List<String> getActionLogs() {
        return actionLogs;
    }

    public void setActionLogs(List<String> actionLogs) {
        this.actionLogs = actionLogs;
    }

    public String getRoundSummary() {
        return roundSummary;
    }

    public void setRoundSummary(String roundSummary) {
        this.roundSummary = roundSummary;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public void setMatch(MatchEntity match) {
        this.match = match;
    }
}
