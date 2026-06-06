package com.diceduel.service;

import com.diceduel.dto.MatchHistoryResponse;
import com.diceduel.entity.MatchEntity;

import java.util.List;

/**
 * Persists and exposes the history of finished matches.
 */
public interface MatchHistoryService {

    /**
     * Records a finished match into the permanent history. Called once, at the
     * moment a match transitions to FINISHED.
     *
     * @param match  the finished match
     * @param events important chronological events of the match
     */
    void recordFinishedMatch(MatchEntity match, List<String> events);

    /**
     * Returns the full global history, newest first. Admin-only view.
     *
     * @return all history records
     */
    List<MatchHistoryResponse> findAll();

    /**
     * Returns the history of one player. The caller must be the player itself
     * or an administrator; otherwise a {@code ForbiddenException} is raised.
     *
     * @param playerId player whose history is requested
     * @return history records that include the player
     */
    List<MatchHistoryResponse> findForPlayer(String playerId);
}
