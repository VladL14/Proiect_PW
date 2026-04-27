package com.diceduel.service.impl;

import com.diceduel.dto.AddPlayerAbilityRequest;
import com.diceduel.dto.AbilityResponse;
import com.diceduel.dto.CreatePlayerRequest;
import com.diceduel.dto.PatchPlayerRequest;
import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.PlayerStatsResponse;
import com.diceduel.dto.UpdatePlayerRequest;
import com.diceduel.entity.AbilityEntity;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.exception.BadRequestException;
import com.diceduel.exception.ResourceNotFoundException;
import com.diceduel.mapper.AbilityMapper;
import com.diceduel.mapper.PlayerMapper;
import com.diceduel.repository.AbilityRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.service.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private static final int DEFAULT_HEARTS = 3;
    private static final int DEFAULT_TOKENS = 0;

    private final PlayerRepository playerRepository;
    private final AbilityRepository abilityRepository;
    private final PlayerMapper playerMapper;
    private final AbilityMapper abilityMapper;

    public PlayerServiceImpl(
            PlayerRepository playerRepository,
            AbilityRepository abilityRepository,
            PlayerMapper playerMapper,
            AbilityMapper abilityMapper
    ) {
        this.playerRepository = playerRepository;
        this.abilityRepository = abilityRepository;
        this.playerMapper = playerMapper;
        this.abilityMapper = abilityMapper;
    }

    @Override
    public PlayerResponse createPlayer(CreatePlayerRequest request) {
        PlayerEntity player = new PlayerEntity(
                UUID.randomUUID().toString(),
                normalizeName(request.name()),
                DEFAULT_HEARTS,
                DEFAULT_TOKENS,
                0,
                0
        );
        player.getAbilities().addAll(abilityRepository.findAll());

        return playerMapper.toResponse(playerRepository.save(player));
    }

    @Override
    public PlayerResponse replacePlayer(String playerId, UpdatePlayerRequest request) {
        PlayerEntity player = findPlayerEntity(playerId);
        player.setName(normalizeName(request.name()));
        player.setHearts(request.hearts());
        player.setTokens(request.tokens());
        return playerMapper.toResponse(playerRepository.save(player));
    }

    @Override
    public PlayerResponse patchPlayer(String playerId, PatchPlayerRequest request) {
        PlayerEntity player = findPlayerEntity(playerId);

        if (request.name() != null) {
            player.setName(normalizeName(request.name()));
        }
        if (request.hearts() != null) {
            player.setHearts(request.hearts());
        }
        if (request.tokens() != null) {
            player.setTokens(request.tokens());
        }

        return playerMapper.toResponse(playerRepository.save(player));
    }

    @Override
    public void deletePlayer(String playerId) {
        PlayerEntity player = findPlayerEntity(playerId);
        playerRepository.delete(player);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> findAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerResponse findPlayer(String playerId) {
        return playerMapper.toResponse(findPlayerEntity(playerId));
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerStatsResponse findPlayerStats(String playerId) {
        return playerMapper.toStatsResponse(findPlayerEntity(playerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityResponse> findPlayerAbilities(String playerId) {
        PlayerEntity player = findPlayerEntity(playerId);
        return player.getAbilities()
                .stream()
                .map(abilityMapper::toResponse)
                .toList();
    }

    @Override
    public void uploadAvatar(String playerId, MultipartFile file) {
        validateAvatar(file);

        PlayerEntity player = findPlayerEntity(playerId);
        player.setAvatarFileName(file.getOriginalFilename());
        player.setAvatarContentType(file.getContentType());
        playerRepository.save(player);
    }

    @Override
    public void deleteAvatar(String playerId) {
        PlayerEntity player = findPlayerEntity(playerId);
        player.setAvatarFileName(null);
        player.setAvatarContentType(null);
        playerRepository.save(player);
    }

    @Override
    public List<AbilityResponse> addPlayerAbility(String playerId, AddPlayerAbilityRequest request) {
        PlayerEntity player = findPlayerEntity(playerId);
        AbilityEntity ability = findAbilityEntity(request.abilityId());

        boolean alreadyAssigned = player.getAbilities().stream()
                .anyMatch(existing -> existing.getId().equals(ability.getId()));
        if (!alreadyAssigned) {
            player.getAbilities().add(ability);
            playerRepository.save(player);
        }

        return player.getAbilities()
                .stream()
                .map(abilityMapper::toResponse)
                .toList();
    }

    @Override
    public List<AbilityResponse> removePlayerAbility(String playerId, String abilityId) {
        PlayerEntity player = findPlayerEntity(playerId);
        findAbilityEntity(abilityId);

        player.getAbilities().removeIf(ability -> ability.getId().equals(abilityId));
        playerRepository.save(player);

        return player.getAbilities()
                .stream()
                .map(abilityMapper::toResponse)
                .toList();
    }

    /**
     * Loads a player entity or raises a domain-specific not found exception.
     *
     * @param playerId player identifier
     * @return existing player entity
     */
    private PlayerEntity findPlayerEntity(String playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    /**
     * Loads an ability entity or raises a domain-specific not found exception.
     *
     * @param abilityId ability identifier
     * @return existing ability entity
     */
    private AbilityEntity findAbilityEntity(String abilityId) {
        return abilityRepository.findById(abilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Ability not found: " + abilityId));
    }

    /**
     * Trims and validates a player display name before persistence.
     *
     * @param name raw display name
     * @return normalized display name
     */
    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new BadRequestException("Player name is required");
        }
        return normalized;
    }

    /**
     * Validates that the uploaded avatar is present and has an image content type.
     *
     * @param file uploaded multipart file
     */
    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Avatar file is required");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestException("Avatar must be an image file");
        }
    }
}
