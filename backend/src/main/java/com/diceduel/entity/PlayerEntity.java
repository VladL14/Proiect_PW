package com.diceduel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
public class PlayerEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private Integer hearts;

    @Column(nullable = false)
    private Integer tokens;

    @Column(nullable = false)
    private Integer wins;

    @Column(nullable = false)
    private Integer losses;

    // --- Stage 4: account / authentication fields ---------------------------
    // These columns are nullable so that legacy players created through the
    // plain /api/players endpoint (without registration) keep working.

    @Column(unique = true, length = 60)
    private String username;

    @Column(unique = true, length = 120)
    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    private LocalDateTime createdAt;

    private Integer matchesPlayed;

    private String avatarFileName;

    private String avatarContentType;
    private String avatarStyle;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] avatarData;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "player_abilities",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "ability_id")
    )
    private List<AbilityEntity> abilities = new ArrayList<>();

    public PlayerEntity() {
    }

    public PlayerEntity(String id, String name, Integer hearts, Integer tokens, Integer wins, Integer losses) {
        this.id = id;
        this.name = name;
        this.hearts = hearts;
        this.tokens = tokens;
        this.wins = wins;
        this.losses = losses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHearts() {
        return hearts;
    }

    public void setHearts(Integer hearts) {
        this.hearts = hearts;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public String getAvatarFileName() {
        return avatarFileName;
    }

    public void setAvatarFileName(String avatarFileName) {
        this.avatarFileName = avatarFileName;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }

    public byte[] getAvatarData() {
        return avatarData;
    }

    public void setAvatarData(byte[] avatarData) {
        this.avatarData = avatarData;
    }

    public List<AbilityEntity> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<AbilityEntity> abilities) {
        this.abilities = abilities;
    }
    public String getAvatarStyle() {
    return avatarStyle;
}

public void setAvatarStyle(String avatarStyle) {
    this.avatarStyle = avatarStyle;
}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(Integer matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }
}
