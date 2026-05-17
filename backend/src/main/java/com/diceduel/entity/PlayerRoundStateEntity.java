package com.diceduel.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player_round_states")
public class PlayerRoundStateEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private RoundEntity round;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<DiceFace> dice = new ArrayList<>();

    @ElementCollection
    private List<Boolean> locked = new ArrayList<>();

    private Integer rollsCount = 0;

    @ElementCollection
    private java.util.Map<Integer, String> diceTargets = new java.util.HashMap<>();

    public PlayerRoundStateEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoundEntity getRound() {
        return round;
    }

    public void setRound(RoundEntity round) {
        this.round = round;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
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

    public Integer getRollsCount() {
        return rollsCount;
    }

    public void setRollsCount(Integer rollsCount) {
        this.rollsCount = rollsCount;
    }

    public java.util.Map<Integer, String> getDiceTargets() {
        return diceTargets;
    }

    public void setDiceTargets(java.util.Map<Integer, String> diceTargets) {
        this.diceTargets = diceTargets;
    }
}
