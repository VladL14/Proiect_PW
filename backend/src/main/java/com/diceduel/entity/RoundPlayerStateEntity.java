package com.diceduel.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "round_player_states")
public class RoundPlayerStateEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String playerId;

    @Column(nullable = false)
    private Integer rollsCount;

    @Column(nullable = false)
    private Integer turnOrder;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "round_player_state_dice", joinColumns = @JoinColumn(name = "state_id"))
    @OrderColumn(name = "dice_index")
    @Column(name = "face")
    private List<DiceFace> dice = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "round_player_state_locked", joinColumns = @JoinColumn(name = "state_id"))
    @OrderColumn(name = "locked_index")
    @Column(name = "locked")
    private List<Boolean> locked = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "round_player_state_targets", joinColumns = @JoinColumn(name = "state_id"))
    @OrderColumn(name = "target_index")
    @Column(name = "target_player_id")
    private List<String> targetPlayerIds = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private RoundEntity round;

    public RoundPlayerStateEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Integer getRollsCount() {
        return rollsCount;
    }

    public void setRollsCount(Integer rollsCount) {
        this.rollsCount = rollsCount;
    }

    public Integer getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(Integer turnOrder) {
        this.turnOrder = turnOrder;
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

    public List<String> getTargetPlayerIds() {
        return targetPlayerIds;
    }

    public void setTargetPlayerIds(List<String> targetPlayerIds) {
        this.targetPlayerIds = targetPlayerIds;
    }

    public RoundEntity getRound() {
        return round;
    }

    public void setRound(RoundEntity round) {
        this.round = round;
    }
}
