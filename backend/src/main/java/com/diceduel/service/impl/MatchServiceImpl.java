package com.diceduel.service.impl;

import com.diceduel.dto.ActivateAbilityRequest;
import com.diceduel.dto.CreateMatchRequest;
import com.diceduel.dto.JoinMatchRequest;
import com.diceduel.dto.LockDiceRequest;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.PatchMatchRequest;
import com.diceduel.dto.PatchRoundRequest;
import com.diceduel.dto.RollDiceRequest;
import com.diceduel.dto.RoundPlayerStateRequest;
import com.diceduel.dto.RoundResponse;
import com.diceduel.dto.SetDiceTargetsRequest;
import com.diceduel.dto.UpdateLockedDiceRequest;
import com.diceduel.dto.UpdateMatchRequest;
import com.diceduel.dto.UpdateMatchStatusRequest;
import com.diceduel.dto.UpdateRoundRequest;
import com.diceduel.entity.AbilityEntity;
import com.diceduel.entity.DiceFace;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.MatchStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.RoundEntity;
import com.diceduel.entity.RoundPlayerStateEntity;
import com.diceduel.entity.RoundStatus;
import com.diceduel.exception.BadRequestException;
import com.diceduel.exception.ResourceNotFoundException;
import com.diceduel.mapper.MatchMapper;
import com.diceduel.mapper.RoundMapper;
import com.diceduel.repository.AbilityRepository;
import com.diceduel.repository.MatchRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.repository.RoundRepository;
import com.diceduel.service.MatchService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class MatchServiceImpl implements MatchService {

    private static final int DEFAULT_MAX_PLAYERS = 2;
    private static final int DICE_COUNT = 5;

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final RoundRepository roundRepository;
    private final AbilityRepository abilityRepository;
    private final MatchMapper matchMapper;
    private final RoundMapper roundMapper;
    private final Random random = new Random();

    public MatchServiceImpl(
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            RoundRepository roundRepository,
            AbilityRepository abilityRepository,
            MatchMapper matchMapper,
            RoundMapper roundMapper
    ) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.roundRepository = roundRepository;
        this.abilityRepository = abilityRepository;
        this.matchMapper = matchMapper;
        this.roundMapper = roundMapper;
    }

    @Override
    public MatchResponse createMatch(CreateMatchRequest request) {
        PlayerEntity host = findPlayer(request.hostPlayerId());

        MatchEntity match = new MatchEntity();
        match.setId(UUID.randomUUID().toString());
        match.setStatus(MatchStatus.WAITING);
        match.setMaxPlayers(resolveMaxPlayers(request.maxPlayers()));
        match.setCurrentRoundNumber(0);
        match.setWinnerPlayerId(null);
        match.setCreatedAt(LocalDateTime.now());
        match.getPlayers().add(host);

        return matchMapper.toResponse(matchRepository.save(match));
    }

    @Override
    public MatchResponse replaceMatch(String matchId, UpdateMatchRequest request) {
        MatchEntity match = findMatchEntity(matchId);
        validateMatchConfigEditable(match);
        validateMaxPlayers(match, request.maxPlayers());

        match.setMaxPlayers(request.maxPlayers());
        refreshLobbyStatus(match);
        return matchMapper.toResponse(matchRepository.save(match));
    }

    @Override
    public MatchResponse patchMatch(String matchId, PatchMatchRequest request) {
        MatchEntity match = findMatchEntity(matchId);

        if (request.maxPlayers() != null) {
            validateMatchConfigEditable(match);
            validateMaxPlayers(match, request.maxPlayers());
            match.setMaxPlayers(request.maxPlayers());
            refreshLobbyStatus(match);
        }
        if (request.status() != null) {
            applyMatchStatus(match, request.status());
        }

        return matchMapper.toResponse(matchRepository.save(match));
    }

    @Override
    public void deleteMatch(String matchId) {
        MatchEntity match = findMatchEntity(matchId);
        if (match.getStatus() == MatchStatus.IN_PROGRESS) {
            throw new BadRequestException("In-progress matches cannot be deleted");
        }
        matchRepository.delete(match);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> findMatches(MatchStatus status) {
        List<MatchEntity> matches = status == null
                ? matchRepository.findAll()
                : matchRepository.findByStatus(status);

        return matches.stream()
                .map(matchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResponse findMatch(String matchId) {
        return matchMapper.toResponse(findMatchEntity(matchId));
    }

    @Override
    public void joinMatch(String matchId, JoinMatchRequest request) {
        MatchEntity match = findMatchEntity(matchId);
        PlayerEntity player = findPlayer(request.playerId());

        validateMatchJoinable(match, player);
        match.getPlayers().add(player);

        if (match.getPlayers().size() >= match.getMaxPlayers()) {
            match.setStatus(MatchStatus.READY);
        }

        matchRepository.save(match);
    }

    @Override
    public void removePlayerFromMatch(String matchId, String playerId) {
        MatchEntity match = findMatchEntity(matchId);
        validateMatchConfigEditable(match);
        boolean removed = match.getPlayers().removeIf(player -> player.getId().equals(playerId));
        if (!removed) {
            throw new ResourceNotFoundException("Player not found in match: " + playerId);
        }
        refreshLobbyStatus(match);
        matchRepository.save(match);
    }

    @Override
    public void startMatch(String matchId) {
        MatchEntity match = findMatchEntity(matchId);
        validateMatchStartable(match);

        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setWinnerPlayerId(null);
        match.setCurrentRoundNumber(1);
        RoundEntity round = createRound(match, 1);
        match.getRounds().add(round);
        matchRepository.save(match);
    }

    @Override
    public MatchResponse updateMatchStatus(String matchId, UpdateMatchStatusRequest request) {
        MatchEntity match = findMatchEntity(matchId);
        applyMatchStatus(match, request.status());
        return matchMapper.toResponse(matchRepository.save(match));
    }

    @Override
    @Transactional(readOnly = true)
    public MatchStateResponse findMatchState(String matchId) {
        return matchMapper.toStateResponse(findMatchEntity(matchId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResponse> findRounds(String matchId) {
        MatchEntity match = findMatchEntity(matchId);
        return match.getRounds()
                .stream()
                .sorted(Comparator.comparing(RoundEntity::getRoundNumber))
                .map(roundMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse findRound(String matchId, String roundId) {
        return roundMapper.toResponse(findRoundEntity(matchId, roundId));
    }

    @Override
    public RoundResponse replaceRound(String matchId, String roundId, UpdateRoundRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        round.setStatus(request.status());
        round.setDice(validateDice(request.dice()));
        round.setLocked(validateLockedDice(request.locked()));
        if (request.playerStates() != null) {
            replacePlayerStates(round, request.playerStates());
        }
        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse patchRound(String matchId, String roundId, PatchRoundRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);

        if (request.status() != null) {
            round.setStatus(request.status());
        }
        if (request.dice() != null) {
            round.setDice(validateDice(request.dice()));
        }
        if (request.locked() != null) {
            round.setLocked(validateLockedDice(request.locked()));
        }
        if (request.playerStates() != null) {
            replacePlayerStates(round, request.playerStates());
        }

        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public void deleteRound(String matchId, String roundId) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        MatchEntity match = round.getMatch();
        match.getRounds().removeIf(existing -> existing.getId().equals(roundId));
        roundRepository.delete(round);

        int latestRoundNumber = match.getRounds().stream()
                .map(RoundEntity::getRoundNumber)
                .max(Integer::compareTo)
                .orElse(0);
        match.setCurrentRoundNumber(latestRoundNumber);
        matchRepository.save(match);
    }

    @Override
    public RoundResponse rollDice(String matchId, String roundId, RollDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        PlayerEntity player = validatePlayerInMatch(round.getMatch(), request.playerId());
        validatePlayerAlive(player);
        validateRoundCanBePlayed(round);

        RoundPlayerStateEntity playerState = findOrCreatePlayerState(round, player);
        List<DiceFace> dice = buildRolledDice(playerState);
        playerState.setDice(dice);
        playerState.setLocked(ensureLockList(playerState.getLocked()));
        playerState.setTargetPlayerIds(targetsAfterRoll(playerState.getTargetPlayerIds(), dice));
        playerState.setRollsCount(playerState.getRollsCount() + 1);
        mirrorLegacyRoundDice(round, playerState);
        round.setStatus(RoundStatus.ROLLING);
        round.setRoundSummary(player.getName() + " rolled the dice.");
        round.getActionLogs().add(player.getName() + " rolled " + dice.size() + " dice.");
        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse lockDice(String matchId, String roundId, LockDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        PlayerEntity player = validatePlayerInMatch(round.getMatch(), request.playerId());
        validatePlayerAlive(player);
        RoundPlayerStateEntity playerState = findRequiredPlayerState(round, request.playerId());
        validateDiceAlreadyRolled(playerState);

        List<Boolean> locked = emptyLockList();
        request.lockedIndexes().forEach(index -> lockIndex(locked, index));

        playerState.setLocked(locked);
        mirrorLegacyRoundDice(round, playerState);
        round.setStatus(RoundStatus.TARGET_SELECTION);
        round.setRoundSummary(player.getName() + " locked " + request.lockedIndexes().size() + " dice.");
        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse updateLockedDice(String matchId, String roundId, UpdateLockedDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        validateDiceAlreadyRolled(round);
        round.setLocked(validateLockedDice(request.locked()));
        round.setStatus(RoundStatus.TARGET_SELECTION);
        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse setDiceTargets(String matchId, String roundId, SetDiceTargetsRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        PlayerEntity player = validatePlayerInMatch(round.getMatch(), request.playerId());
        validatePlayerAlive(player);
        RoundPlayerStateEntity playerState = findRequiredPlayerState(round, request.playerId());
        validateDiceAlreadyRolled(playerState);

        List<String> targetPlayerIds = new ArrayList<>(ensureTargetList(playerState.getTargetPlayerIds()));
        for (Map.Entry<Integer, String> entry : request.diceTargets().entrySet()) {
            int dieIndex = validateDieIndex(entry.getKey());
            DiceFace face = playerState.getDice().get(dieIndex);
            if (face == DiceFace.SHIELD) {
                throw new BadRequestException("Shield dice do not take targets");
            }
            if (face != DiceFace.ATTACK && face != DiceFace.STEAL) {
                throw new BadRequestException("Only attack and steal dice can target players");
            }
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                targetPlayerIds.set(dieIndex, "");
                continue;
            }
            validateTarget(round.getMatch(), request.playerId(), entry.getValue());
            targetPlayerIds.set(dieIndex, entry.getValue());
        }

        for (int i = 0; i < playerState.getDice().size(); i++) {
            if (playerState.getDice().get(i) == DiceFace.SHIELD) {
                targetPlayerIds.set(i, "");
            }
        }

        playerState.setTargetPlayerIds(targetPlayerIds);
        round.setStatus(RoundStatus.TARGET_SELECTION);
        round.setRoundSummary(player.getName() + " chose targets.");
        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public MatchStateResponse resolveRound(String matchId, String roundId) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        validateAnyDiceRolled(round);

        if (round.getStatus() == RoundStatus.RESOLVED) {
            throw new BadRequestException("Round is already resolved");
        }

        List<String> actionLogs = resolveRoundCombat(round);
        round.setActionLogs(actionLogs);
        round.setRoundSummary(buildRoundSummary(actionLogs));
        round.setStatus(RoundStatus.RESOLVED);

        MatchEntity match = round.getMatch();
        Optional<PlayerEntity> winner = determineWinner(match);
        if (winner.isPresent()) {
            finishMatch(match, winner.get());
            actionLogs.add("The match ended. " + winner.get().getName() + " won the duel.");
            round.setActionLogs(actionLogs);
            round.setRoundSummary(buildRoundSummary(actionLogs));
        } else {
            createNextRound(match, round.getRoundNumber() + 1);
        }

        return matchMapper.toStateResponse(matchRepository.save(match));
    }

    @Override
    public void activateAbility(String matchId, String roundId, ActivateAbilityRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        PlayerEntity player = findPlayer(request.playerId());
        AbilityEntity ability = abilityRepository.findById(request.abilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ability not found: " + request.abilityId()));

        validatePlayerInMatch(round.getMatch(), player.getId());
        validateAbilityCanBeActivated(round, player, ability);

        player.setTokens(player.getTokens() - ability.getCost());
        round.setStatus(RoundStatus.ABILITY_PHASE);
        playerRepository.save(player);
        roundRepository.save(round);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayResource exportReplay(String matchId) {
        MatchEntity match = findMatchEntity(matchId);
        String replay = buildReplay(match);
        return new ByteArrayResource(replay.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Loads a match entity by identifier or fails with a not found exception.
     *
     * @param matchId match identifier
     * @return existing match entity
     */
    private MatchEntity findMatchEntity(String matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));
    }

    /**
     * Loads a player entity by identifier or fails with a not found exception.
     *
     * @param playerId player identifier
     * @return existing player entity
     */
    private PlayerEntity findPlayer(String playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    /**
     * Loads a round only when it belongs to the requested match.
     *
     * @param matchId match identifier
     * @param roundId round identifier
     * @return existing round entity
     */
    private RoundEntity findRoundEntity(String matchId, String roundId) {
        return roundRepository.findByMatch_IdAndId(matchId, roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found: " + roundId));
    }

    /**
     * Decides the max player value when the request omits it.
     *
     * @param requestedMaxPlayers value received from the client
     * @return resolved max player value
     */
    private Integer resolveMaxPlayers(Integer requestedMaxPlayers) {
        return requestedMaxPlayers == null ? DEFAULT_MAX_PLAYERS : requestedMaxPlayers;
    }

    /**
     * Ensures that lobby configuration changes do not affect active matches.
     *
     * @param match match being modified
     */
    private void validateMatchConfigEditable(MatchEntity match) {
        if (match.getStatus() == MatchStatus.IN_PROGRESS || match.getStatus() == MatchStatus.FINISHED) {
            throw new BadRequestException("Only waiting or ready matches can be modified");
        }
    }

    /**
     * Ensures the configured player limit can still contain the current lobby.
     *
     * @param match match being modified
     * @param maxPlayers requested maximum player count
     */
    private void validateMaxPlayers(MatchEntity match, Integer maxPlayers) {
        if (maxPlayers < match.getPlayers().size()) {
            throw new BadRequestException("Max players cannot be lower than current player count");
        }
    }

    /**
     * Keeps the lobby status aligned with the configured capacity.
     *
     * @param match match whose lobby status may change
     */
    private void refreshLobbyStatus(MatchEntity match) {
        if (match.getStatus() == MatchStatus.IN_PROGRESS || match.getStatus() == MatchStatus.FINISHED) {
            return;
        }
        match.setStatus(match.getPlayers().size() >= match.getMaxPlayers()
                ? MatchStatus.READY
                : MatchStatus.WAITING);
    }

    /**
     * Applies a direct status patch while preserving required side effects.
     *
     * @param match match being patched
     * @param status requested status
     */
    private void applyMatchStatus(MatchEntity match, MatchStatus status) {
        if (status == MatchStatus.IN_PROGRESS) {
            validateMatchStartable(match);
            if (match.getRounds().isEmpty()) {
                match.setCurrentRoundNumber(1);
                match.getRounds().add(createRound(match, 1));
            }
        }
        if (status == MatchStatus.WAITING || status == MatchStatus.READY) {
            validateMatchConfigEditable(match);
        }
        match.setStatus(status);
    }

    /**
     * Ensures that a player can join the selected match.
     *
     * @param match match being joined
     * @param player player attempting to join
     */
    private void validateMatchJoinable(MatchEntity match, PlayerEntity player) {
        if (match.getStatus() != MatchStatus.WAITING && match.getStatus() != MatchStatus.READY) {
            throw new BadRequestException("Only waiting or ready matches can be joined");
        }
        if (match.getPlayers().stream().anyMatch(existing -> existing.getId().equals(player.getId()))) {
            throw new BadRequestException("Player already joined this match");
        }
        if (match.getPlayers().size() >= match.getMaxPlayers()) {
            throw new BadRequestException("Match is full");
        }
    }

    /**
     * Ensures that a match has enough players and is not already running.
     *
     * @param match match that should be started
     */
    private void validateMatchStartable(MatchEntity match) {
        if (match.getStatus() == MatchStatus.IN_PROGRESS || match.getStatus() == MatchStatus.FINISHED) {
            throw new BadRequestException("Match cannot be started from status " + match.getStatus());
        }
        if (match.getPlayers().size() < 2) {
            throw new BadRequestException("A match needs at least two players to start");
        }
    }

    /**
     * Creates a new round and attaches it to a match.
     *
     * @param match parent match
     * @param roundNumber round number
     * @return created round entity
     */
    private RoundEntity createRound(MatchEntity match, int roundNumber) {
        RoundEntity round = new RoundEntity();
        round.setId(UUID.randomUUID().toString());
        round.setRoundNumber(roundNumber);
        round.setStatus(RoundStatus.INITIALIZED);
        round.setDice(new ArrayList<>());
        round.setLocked(new ArrayList<>());
        round.setMatch(match);
        for (int i = 0; i < match.getPlayers().size(); i++) {
            PlayerEntity player = match.getPlayers().get(i);
            if (player.getHearts() > 0) {
                round.getPlayerStates().add(createPlayerState(round, player, i));
            }
        }
        return round;
    }

    /**
     * Ensures that the player belongs to the selected match.
     *
     * @param match match containing the allowed players
     * @param playerId player identifier
     * @return player in the match
     */
    private PlayerEntity validatePlayerInMatch(MatchEntity match, String playerId) {
        return match.getPlayers().stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Player is not part of this match"));
    }

    private void validatePlayerAlive(PlayerEntity player) {
        if (player.getHearts() <= 0) {
            throw new BadRequestException("Eliminated players cannot take actions");
        }
    }

    /**
     * Ensures that the round is still active and belongs to an in-progress match.
     *
     * @param round round that should be played
     */
    private void validateRoundCanBePlayed(RoundEntity round) {
        if (round.getMatch().getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BadRequestException("Match is not in progress");
        }
        if (round.getStatus() == RoundStatus.RESOLVED) {
            throw new BadRequestException("Resolved rounds cannot be changed");
        }
    }

    /**
     * Builds a five-dice roll while preserving locked dice values.
     *
     * @param playerState current player state
     * @return list of dice faces after rolling
     */
    private List<DiceFace> buildRolledDice(RoundPlayerStateEntity playerState) {
        List<DiceFace> currentDice = new ArrayList<>(playerState.getDice());
        List<Boolean> locked = ensureLockList(playerState.getLocked());
        List<DiceFace> diceFaces = Arrays.asList(DiceFace.values());
        List<DiceFace> rolledDice = new ArrayList<>();

        for (int i = 0; i < DICE_COUNT; i++) {
            if (i < currentDice.size() && Boolean.TRUE.equals(locked.get(i))) {
                rolledDice.add(currentDice.get(i));
            } else {
                rolledDice.add(diceFaces.get(random.nextInt(diceFaces.size())));
            }
        }

        return rolledDice;
    }

    /**
     * Normalizes the lock list so that it always contains one value for each die.
     *
     * @param locked current lock list
     * @return normalized lock list
     */
    private List<Boolean> ensureLockList(List<Boolean> locked) {
        List<Boolean> normalized = new ArrayList<>();
        for (int i = 0; i < DICE_COUNT; i++) {
            normalized.add(locked != null && i < locked.size() && Boolean.TRUE.equals(locked.get(i)));
        }
        return normalized;
    }

    private List<Boolean> emptyLockList() {
        List<Boolean> locked = new ArrayList<>();
        for (int i = 0; i < DICE_COUNT; i++) {
            locked.add(false);
        }
        return locked;
    }

    private List<String> ensureTargetList(List<String> targets) {
        List<String> normalized = new ArrayList<>();
        for (int i = 0; i < DICE_COUNT; i++) {
            normalized.add(targets != null && i < targets.size() && targets.get(i) != null
                    ? targets.get(i)
                    : "");
        }
        return normalized;
    }

    private List<String> targetsAfterRoll(List<String> currentTargets, List<DiceFace> dice) {
        List<String> targets = ensureTargetList(currentTargets);
        for (int i = 0; i < DICE_COUNT; i++) {
            if (i >= dice.size() || (dice.get(i) != DiceFace.ATTACK && dice.get(i) != DiceFace.STEAL)) {
                targets.set(i, "");
            }
        }
        return targets;
    }

    private RoundPlayerStateEntity createPlayerState(RoundEntity round, PlayerEntity player, int turnOrder) {
        RoundPlayerStateEntity playerState = new RoundPlayerStateEntity();
        playerState.setId(UUID.randomUUID().toString());
        playerState.setPlayerId(player.getId());
        playerState.setRollsCount(0);
        playerState.setTurnOrder(turnOrder);
        playerState.setDice(new ArrayList<>());
        playerState.setLocked(emptyLockList());
        playerState.setTargetPlayerIds(ensureTargetList(null));
        playerState.setRound(round);
        return playerState;
    }

    private RoundPlayerStateEntity findOrCreatePlayerState(RoundEntity round, PlayerEntity player) {
        return round.getPlayerStates()
                .stream()
                .filter(state -> state.getPlayerId().equals(player.getId()))
                .findFirst()
                .orElseGet(() -> {
                    RoundPlayerStateEntity playerState = createPlayerState(
                            round,
                            player,
                            round.getPlayerStates().size()
                    );
                    round.getPlayerStates().add(playerState);
                    return playerState;
                });
    }

    private RoundPlayerStateEntity findRequiredPlayerState(RoundEntity round, String playerId) {
        return round.getPlayerStates()
                .stream()
                .filter(state -> state.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Player has no dice state in this round"));
    }

    private void mirrorLegacyRoundDice(RoundEntity round, RoundPlayerStateEntity playerState) {
        round.setDice(new ArrayList<>(playerState.getDice()));
        round.setLocked(new ArrayList<>(playerState.getLocked()));
    }

    /**
     * Validates a directly supplied dice list for round update endpoints.
     *
     * @param dice dice values from the request
     * @return copied dice values
     */
    private List<DiceFace> validateDice(List<DiceFace> dice) {
        if (dice == null) {
            return new ArrayList<>();
        }
        if (dice.size() > DICE_COUNT) {
            throw new BadRequestException("A round cannot contain more than " + DICE_COUNT + " dice");
        }
        return new ArrayList<>(dice);
    }

    /**
     * Validates a directly supplied lock list for round update endpoints.
     *
     * @param locked locked dice values from the request
     * @return copied lock values
     */
    private List<Boolean> validateLockedDice(List<Boolean> locked) {
        if (locked == null) {
            return emptyLockList();
        }
        if (locked.size() != DICE_COUNT) {
            throw new BadRequestException("Locked dice list must contain exactly " + DICE_COUNT + " values");
        }
        return new ArrayList<>(locked);
    }

    private void replacePlayerStates(RoundEntity round, List<RoundPlayerStateRequest> requests) {
        round.getPlayerStates().clear();
        for (int i = 0; i < requests.size(); i++) {
            RoundPlayerStateRequest request = requests.get(i);
            PlayerEntity player = validatePlayerInMatch(round.getMatch(), request.playerId());
            RoundPlayerStateEntity playerState = createPlayerState(round, player, i);
            List<DiceFace> dice = validateDice(request.dice());
            playerState.setDice(dice);
            playerState.setLocked(request.locked() == null ? emptyLockList() : validateLockedDice(request.locked()));
            playerState.setTargetPlayerIds(validateTargetsForDice(round.getMatch(), request.playerId(), dice, request.targetPlayerIds()));
            round.getPlayerStates().add(playerState);
            if (i == 0) {
                mirrorLegacyRoundDice(round, playerState);
            }
        }
    }

    private List<String> validateTargetsForDice(
            MatchEntity match,
            String playerId,
            List<DiceFace> dice,
            List<String> targetPlayerIds
    ) {
        List<String> targets = ensureTargetList(targetPlayerIds);
        for (int i = 0; i < dice.size(); i++) {
            DiceFace face = dice.get(i);
            String targetPlayerId = targets.get(i);
            if (face == DiceFace.SHIELD) {
                targets.set(i, "");
                continue;
            }
            if ((face == DiceFace.ATTACK || face == DiceFace.STEAL) && targetPlayerId != null && !targetPlayerId.isBlank()) {
                validateTarget(match, playerId, targetPlayerId);
            }
        }
        return targets;
    }

    /**
     * Ensures that dice exist before lock, resolve or ability operations.
     *
     * @param round selected round
     */
    private void validateDiceAlreadyRolled(RoundEntity round) {
        if (round.getDice() == null || round.getDice().isEmpty()) {
            throw new BadRequestException("Dice must be rolled first");
        }
    }

    private void validateDiceAlreadyRolled(RoundPlayerStateEntity playerState) {
        if (playerState.getDice() == null || playerState.getDice().isEmpty()) {
            throw new BadRequestException("Dice must be rolled first");
        }
    }

    private void validateAnyDiceRolled(RoundEntity round) {
        boolean hasRolledDice = round.getPlayerStates()
                .stream()
                .anyMatch(state -> state.getDice() != null && !state.getDice().isEmpty());
        if (!hasRolledDice && (round.getDice() == null || round.getDice().isEmpty())) {
            throw new BadRequestException("Dice must be rolled first");
        }
    }

    /**
     * Locks a valid dice index and rejects invalid positions.
     *
     * @param locked current lock list
     * @param index index requested by the player
     */
    private void lockIndex(List<Boolean> locked, Integer index) {
        if (index == null || index < 0 || index >= DICE_COUNT) {
            throw new BadRequestException("Locked dice index must be between 0 and " + (DICE_COUNT - 1));
        }
        locked.set(index, true);
    }

    private int validateDieIndex(Integer index) {
        if (index == null || index < 0 || index >= DICE_COUNT) {
            throw new BadRequestException("Dice index must be between 0 and " + (DICE_COUNT - 1));
        }
        return index;
    }

    private PlayerEntity validateTarget(MatchEntity match, String actingPlayerId, String targetPlayerId) {
        if (targetPlayerId == null || targetPlayerId.isBlank()) {
            throw new BadRequestException("Target player is required");
        }
        if (actingPlayerId.equals(targetPlayerId)) {
            throw new BadRequestException("A player cannot target themselves");
        }
        PlayerEntity target = validatePlayerInMatch(match, targetPlayerId);
        if (target.getHearts() <= 0) {
            throw new BadRequestException("Eliminated players cannot be targeted");
        }
        return target;
    }

    private List<String> resolveRoundCombat(RoundEntity round) {
        MatchEntity match = round.getMatch();
        Map<String, PlayerEntity> playersById = new LinkedHashMap<>();
        match.getPlayers().forEach(player -> playersById.put(player.getId(), player));

        validateRequiredTargets(round);

        Map<String, Map<String, Integer>> attacksByTargetAndAttacker = new LinkedHashMap<>();
        Map<String, Integer> shieldsByPlayer = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> stealsByTargetAndAttacker = new LinkedHashMap<>();

        for (RoundPlayerStateEntity playerState : sortedPlayerStates(round)) {
            PlayerEntity actingPlayer = playersById.get(playerState.getPlayerId());
            if (actingPlayer == null || actingPlayer.getHearts() <= 0) {
                continue;
            }
            List<String> targets = ensureTargetList(playerState.getTargetPlayerIds());
            for (int i = 0; i < playerState.getDice().size(); i++) {
                DiceFace face = playerState.getDice().get(i);
                if (face == DiceFace.SHIELD) {
                    shieldsByPlayer.merge(playerState.getPlayerId(), 1, Integer::sum);
                } else if (face == DiceFace.ATTACK) {
                    addTargetedAction(attacksByTargetAndAttacker, targets.get(i), playerState.getPlayerId());
                } else if (face == DiceFace.STEAL) {
                    addTargetedAction(stealsByTargetAndAttacker, targets.get(i), playerState.getPlayerId());
                }
            }
        }

        List<String> logs = new ArrayList<>();
        resolveAttacks(playersById, attacksByTargetAndAttacker, shieldsByPlayer, logs);
        resolveSteals(playersById, stealsByTargetAndAttacker, logs);
        resolveEliminations(match, logs);
        return logs;
    }

    private void validateRequiredTargets(RoundEntity round) {
        for (RoundPlayerStateEntity playerState : sortedPlayerStates(round)) {
            PlayerEntity player = validatePlayerInMatch(round.getMatch(), playerState.getPlayerId());
            if (player.getHearts() <= 0) {
                continue;
            }
            List<String> targets = ensureTargetList(playerState.getTargetPlayerIds());
            for (int i = 0; i < playerState.getDice().size(); i++) {
                DiceFace face = playerState.getDice().get(i);
                if (face == DiceFace.ATTACK || face == DiceFace.STEAL) {
                    validateTarget(round.getMatch(), playerState.getPlayerId(), targets.get(i));
                }
            }
        }
    }

    private List<RoundPlayerStateEntity> sortedPlayerStates(RoundEntity round) {
        return round.getPlayerStates()
                .stream()
                .sorted(Comparator.comparing(RoundPlayerStateEntity::getTurnOrder))
                .toList();
    }

    private void addTargetedAction(
            Map<String, Map<String, Integer>> actionsByTargetAndAttacker,
            String targetPlayerId,
            String actingPlayerId
    ) {
        actionsByTargetAndAttacker
                .computeIfAbsent(targetPlayerId, ignored -> new LinkedHashMap<>())
                .merge(actingPlayerId, 1, Integer::sum);
    }

    private void resolveAttacks(
            Map<String, PlayerEntity> playersById,
            Map<String, Map<String, Integer>> attacksByTargetAndAttacker,
            Map<String, Integer> shieldsByPlayer,
            List<String> logs
    ) {
        attacksByTargetAndAttacker.forEach((targetPlayerId, attacksByAttacker) -> {
            PlayerEntity target = playersById.get(targetPlayerId);
            int shieldsRemaining = shieldsByPlayer.getOrDefault(targetPlayerId, 0);
            int totalDamage = 0;

            for (Map.Entry<String, Integer> entry : attacksByAttacker.entrySet()) {
                PlayerEntity attacker = playersById.get(entry.getKey());
                int attacks = entry.getValue();
                int blocked = Math.min(attacks, shieldsRemaining);
                int damage = attacks - blocked;
                shieldsRemaining -= blocked;
                totalDamage += damage;
                logs.add(attacker.getName() + " attacked " + target.getName() + ". "
                        + blocked + " attack(s) blocked, " + damage + " damage dealt.");
            }

            target.setHearts(Math.max(0, target.getHearts() - totalDamage));
        });
    }

    private void resolveSteals(
            Map<String, PlayerEntity> playersById,
            Map<String, Map<String, Integer>> stealsByTargetAndAttacker,
            List<String> logs
    ) {
        stealsByTargetAndAttacker.forEach((targetPlayerId, stealsByAttacker) -> {
            PlayerEntity target = playersById.get(targetPlayerId);
            for (Map.Entry<String, Integer> entry : stealsByAttacker.entrySet()) {
                PlayerEntity attacker = playersById.get(entry.getKey());
                for (int i = 0; i < entry.getValue(); i++) {
                    if (target.getTokens() > 0) {
                        target.setTokens(target.getTokens() - 1);
                        attacker.setTokens(attacker.getTokens() + 1);
                        logs.add(attacker.getName() + " stole 1 token from " + target.getName() + ".");
                    } else {
                        logs.add(attacker.getName() + " tried to steal from " + target.getName()
                                + ", but " + target.getName() + " had no tokens.");
                    }
                }
            }
        });
    }

    private void resolveEliminations(MatchEntity match, List<String> logs) {
        match.getPlayers()
                .stream()
                .filter(player -> player.getHearts() <= 0)
                .forEach(player -> {
                    String message = player.getName() + " was eliminated.";
                    if (!logs.contains(message)) {
                        logs.add(message);
                    }
                });
    }

    private String buildRoundSummary(List<String> actionLogs) {
        if (actionLogs.isEmpty()) {
            return "No combat actions were resolved.";
        }
        return actionLogs.get(actionLogs.size() - 1);
    }

    private Optional<PlayerEntity> determineWinner(MatchEntity match) {
        List<PlayerEntity> alivePlayers = match.getPlayers()
                .stream()
                .filter(player -> player.getHearts() > 0)
                .toList();
        return alivePlayers.size() == 1 ? Optional.of(alivePlayers.get(0)) : Optional.empty();
    }

    /**
     * Creates the next initialized round after resolving the current one.
     *
     * @param match match receiving the next round
     * @param nextRoundNumber next round number
     */
    private void createNextRound(MatchEntity match, int nextRoundNumber) {
        match.setCurrentRoundNumber(nextRoundNumber);
        RoundEntity nextRound = createRound(match, nextRoundNumber);
        match.getRounds().add(nextRound);
    }

    /**
     * Finishes a match and updates the basic win/loss counters.
     *
     * @param match match to finish
     */
    private void finishMatch(MatchEntity match, PlayerEntity winner) {
        match.setStatus(MatchStatus.FINISHED);
        match.setWinnerPlayerId(winner.getId());
        match.getPlayers().forEach(player -> {
            if (player.getId().equals(winner.getId())) {
                player.setWins(player.getWins() + 1);
            } else {
                player.setLosses(player.getLosses() + 1);
            }
        });
    }

    /**
     * Ensures that a player has enough tokens to activate an ability in a valid round.
     *
     * @param round selected round
     * @param player player activating the ability
     * @param ability selected ability
     */
    private void validateAbilityCanBeActivated(RoundEntity round, PlayerEntity player, AbilityEntity ability) {
        if (round.getMatch().getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BadRequestException("Abilities can be activated only during an active match");
        }
        if (round.getStatus() == RoundStatus.RESOLVED) {
            throw new BadRequestException("Abilities cannot be activated on resolved rounds");
        }
        if (player.getTokens() < ability.getCost()) {
            throw new BadRequestException("Not enough tokens to activate ability");
        }
    }

    /**
     * Builds a plain text replay from the persisted match and round data.
     *
     * @param match match to export
     * @return replay content
     */
    private String buildReplay(MatchEntity match) {
        StringBuilder replay = new StringBuilder();
        replay.append("Dice Duel Replay\n");
        replay.append("Match: ").append(match.getId()).append('\n');
        replay.append("Status: ").append(match.getStatus()).append('\n');
        replay.append("Players:\n");
        match.getPlayers().forEach(player -> replay
                .append("- ")
                .append(player.getName())
                .append(" tokens=")
                .append(player.getTokens())
                .append(" wins=")
                .append(player.getWins())
                .append(" losses=")
                .append(player.getLosses())
                .append('\n'));

        replay.append("Rounds:\n");
        match.getRounds().stream()
                .sorted((left, right) -> left.getRoundNumber().compareTo(right.getRoundNumber()))
                .forEach(round -> replay
                        .append("- Round ")
                        .append(round.getRoundNumber())
                        .append(" status=")
                        .append(round.getStatus())
                        .append(" dice=")
                        .append(round.getDice())
                        .append(" locked=")
                        .append(round.getLocked())
                        .append('\n'));

        return replay.toString();
    }
}
