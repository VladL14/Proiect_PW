package com.diceduel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Immutable record of a finished match, written once when a match completes.
 *
 * <p>The history is intentionally stored separately from the live
 * {@link MatchEntity} so that it survives even if the match (and its rounds)
 * are deleted, and so that the replay can be regenerated without replaying the
 * authoritative game logic. Participants and the chronological event log are
 * stored as simple text columns to keep the schema small; richer structures are
 * reconstructed by the mapper in the service layer.
 */
@Entity
@Table(name = "match_history")
public class MatchHistoryEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String matchId;

    @Lob
    private String participantIdsCsv;

    @Lob
    private String participantNamesCsv;

    private String winnerId;

    private String winnerName;

    private String finalStatus;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long durationSeconds;

    @Lob
    private String events;

    @Lob
    private String replayJson;

    public MatchHistoryEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getParticipantIdsCsv() {
        return participantIdsCsv;
    }

    public void setParticipantIdsCsv(String participantIdsCsv) {
        this.participantIdsCsv = participantIdsCsv;
    }

    public String getParticipantNamesCsv() {
        return participantNamesCsv;
    }

    public void setParticipantNamesCsv(String participantNamesCsv) {
        this.participantNamesCsv = participantNamesCsv;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public String getReplayJson() {
        return replayJson;
    }

    public void setReplayJson(String replayJson) {
        this.replayJson = replayJson;
    }
}
