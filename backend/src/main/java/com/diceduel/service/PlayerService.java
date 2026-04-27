package com.diceduel.service;

import com.diceduel.dto.AddPlayerAbilityRequest;
import com.diceduel.dto.AbilityResponse;
import com.diceduel.dto.CreatePlayerRequest;
import com.diceduel.dto.PatchPlayerRequest;
import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.PlayerStatsResponse;
import com.diceduel.dto.UpdatePlayerRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Defines player-related business operations used by the REST layer.
 */
public interface PlayerService {

    /**
     * Creates a new player with the default game values.
     *
     * @param request request containing the player name
     * @return created player representation
     */
    PlayerResponse createPlayer(CreatePlayerRequest request);

    PlayerResponse replacePlayer(String playerId, UpdatePlayerRequest request);

    PlayerResponse patchPlayer(String playerId, PatchPlayerRequest request);

    void deletePlayer(String playerId);

    /**
     * Retrieves all players registered in the game.
     *
     * @return list of player representations
     */
    List<PlayerResponse> findAllPlayers();

    /**
     * Retrieves one player by identifier.
     *
     * @param playerId player identifier
     * @return player representation
     */
    PlayerResponse findPlayer(String playerId);

    /**
     * Retrieves the win and loss statistics for one player.
     *
     * @param playerId player identifier
     * @return player statistics
     */
    PlayerStatsResponse findPlayerStats(String playerId);

    /**
     * Retrieves the abilities available to a player.
     *
     * @param playerId player identifier
     * @return list of abilities available to the player
     */
    List<AbilityResponse> findPlayerAbilities(String playerId);

    /**
     * Stores avatar metadata for the selected player.
     *
     * @param playerId player identifier
     * @param file uploaded avatar file
     */
    void uploadAvatar(String playerId, MultipartFile file);

    void deleteAvatar(String playerId);

    List<AbilityResponse> addPlayerAbility(String playerId, AddPlayerAbilityRequest request);

    List<AbilityResponse> removePlayerAbility(String playerId, String abilityId);
}
