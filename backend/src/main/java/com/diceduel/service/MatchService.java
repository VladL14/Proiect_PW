package com.diceduel.service;

import com.diceduel.dto.ActivateAbilityRequest;
import com.diceduel.dto.CreateMatchRequest;
import com.diceduel.dto.JoinMatchRequest;
import com.diceduel.dto.LockDiceRequest;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.PatchMatchRequest;
import com.diceduel.dto.PatchRoundRequest;
import com.diceduel.dto.RollDiceRequest;
import com.diceduel.dto.RoundResponse;
import com.diceduel.dto.UpdateLockedDiceRequest;
import com.diceduel.dto.UpdateMatchRequest;
import com.diceduel.dto.UpdateMatchStatusRequest;
import com.diceduel.dto.UpdateRoundRequest;
import com.diceduel.entity.MatchStatus;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

/**
 * Defines match, round, dice and ability activation operations.
 */
public interface MatchService {

    /**
     * Creates a match and attaches the host player to it.
     *
     * @param request match creation request
     * @return created match representation
     */
    MatchResponse createMatch(CreateMatchRequest request);

    MatchResponse replaceMatch(String matchId, UpdateMatchRequest request);

    MatchResponse patchMatch(String matchId, PatchMatchRequest request);

    void deleteMatch(String matchId);

    /**
     * Retrieves matches, optionally filtered by status.
     *
     * @param status optional match status
     * @return list of matches
     */
    List<MatchResponse> findMatches(MatchStatus status);

    /**
     * Retrieves a match by identifier.
     *
     * @param matchId match identifier
     * @return match representation
     */
    MatchResponse findMatch(String matchId);

    /**
     * Adds a player to a waiting match.
     *
     * @param matchId match identifier
     * @param request join request
     */
    void joinMatch(String matchId, JoinMatchRequest request);

    void removePlayerFromMatch(String matchId, String playerId);

    /**
     * Starts a ready match and creates its first round.
     *
     * @param matchId match identifier
     */
    void startMatch(String matchId);

    MatchResponse updateMatchStatus(String matchId, UpdateMatchStatusRequest request);

    /**
     * Retrieves the current state of a match.
     *
     * @param matchId match identifier
     * @return current match state
     */
    MatchStateResponse findMatchState(String matchId);

    /**
     * Retrieves one round from a match.
     *
     * @param matchId match identifier
     * @param roundId round identifier
     * @return round representation
     */
    RoundResponse findRound(String matchId, String roundId);

    RoundResponse replaceRound(String matchId, String roundId, UpdateRoundRequest request);

    RoundResponse patchRound(String matchId, String roundId, PatchRoundRequest request);

    void deleteRound(String matchId, String roundId);

    /**
     * Rolls the dice for the selected round.
     *
     * @param matchId match identifier
     * @param roundId round identifier
     * @param request roll request
     */
    void rollDice(String matchId, String roundId, RollDiceRequest request);

    /**
     * Locks selected dice indexes for the selected round.
     *
     * @param matchId match identifier
     * @param roundId round identifier
     * @param request lock request
     */
    void lockDice(String matchId, String roundId, LockDiceRequest request);

    RoundResponse updateLockedDice(String matchId, String roundId, UpdateLockedDiceRequest request);

    /**
     * Resolves a round and either creates the next round or finishes the match.
     *
     * @param matchId match identifier
     * @param roundId round identifier
     */
    void resolveRound(String matchId, String roundId);

    /**
     * Activates an ability during the ability phase.
     *
     * @param matchId match identifier
     * @param roundId round identifier
     * @param request ability activation request
     */
    void activateAbility(String matchId, String roundId, ActivateAbilityRequest request);

    /**
     * Exports a text replay for one match.
     *
     * @param matchId match identifier
     * @return replay file resource
     */
    ByteArrayResource exportReplay(String matchId);
}
