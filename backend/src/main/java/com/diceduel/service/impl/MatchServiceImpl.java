package com.diceduel.service.impl;

import com.diceduel.dto.ActivateAbilityRequest;
import com.diceduel.dto.CreateMatchRequest;
import com.diceduel.dto.JoinMatchRequest;
import com.diceduel.dto.LockDiceRequest;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.RollDiceRequest;
import com.diceduel.dto.RoundResponse;
import com.diceduel.entity.AbilityEntity;
import com.diceduel.entity.DiceFace;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.MatchStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.RoundEntity;
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
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class MatchServiceImpl implements MatchService {

    private static final int DEFAULT_MAX_PLAYERS = 2;
    private static final int DICE_COUNT = 5;
    private static final int MAX_ROUNDS = 3;

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
        match.setCreatedAt(LocalDateTime.now());
        match.getPlayers().add(host);

        return matchMapper.toResponse(matchRepository.save(match));
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
    public void startMatch(String matchId) {
        MatchEntity match = findMatchEntity(matchId);
        validateMatchStartable(match);

        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setCurrentRoundNumber(1);
        RoundEntity round = createRound(match, 1);
        match.getRounds().add(round);
        matchRepository.save(match);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchStateResponse findMatchState(String matchId) {
        return matchMapper.toStateResponse(findMatchEntity(matchId));
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse findRound(String matchId, String roundId) {
        return roundMapper.toResponse(findRoundEntity(matchId, roundId));
    }

    @Override
    public void rollDice(String matchId, String roundId, RollDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        validatePlayerInMatch(round.getMatch(), request.playerId());
        validateRoundCanBePlayed(round);

        List<DiceFace> dice = buildRolledDice(round);
        round.setDice(dice);
        round.setLocked(ensureLockList(round.getLocked()));
        round.setStatus(RoundStatus.ROLLING);
        roundRepository.save(round);
    }

    @Override
    public void lockDice(String matchId, String roundId, LockDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        validatePlayerInMatch(round.getMatch(), request.playerId());
        validateDiceAlreadyRolled(round);

        List<Boolean> locked = new ArrayList<>(ensureLockList(round.getLocked()));
        request.lockedIndexes().forEach(index -> lockIndex(locked, index));

        round.setLocked(locked);
        round.setStatus(RoundStatus.TARGET_SELECTION);
        roundRepository.save(round);
    }

    @Override
    public void resolveRound(String matchId, String roundId) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        validateDiceAlreadyRolled(round);

        if (round.getStatus() == RoundStatus.RESOLVED) {
            throw new BadRequestException("Round is already resolved");
        }

        rewardPlayers(round);
        round.setStatus(RoundStatus.RESOLVED);

        MatchEntity match = round.getMatch();
        if (round.getRoundNumber() >= MAX_ROUNDS) {
            finishMatch(match);
        } else {
            createNextRound(match, round.getRoundNumber() + 1);
        }

        matchRepository.save(match);
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
        return round;
    }

    /**
     * Ensures that the player belongs to the selected match.
     *
     * @param match match containing the allowed players
     * @param playerId player identifier
     */
    private void validatePlayerInMatch(MatchEntity match, String playerId) {
        boolean found = match.getPlayers().stream()
                .anyMatch(player -> player.getId().equals(playerId));
        if (!found) {
            throw new BadRequestException("Player is not part of this match");
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
     * @param round current round
     * @return list of dice faces after rolling
     */
    private List<DiceFace> buildRolledDice(RoundEntity round) {
        List<DiceFace> currentDice = new ArrayList<>(round.getDice());
        List<Boolean> locked = ensureLockList(round.getLocked());
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
            normalized.add(i < locked.size() && Boolean.TRUE.equals(locked.get(i)));
        }
        return normalized;
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

    /**
     * Applies a small token reward based on attack dice rolled during the round.
     *
     * @param round resolved round
     */
    private void rewardPlayers(RoundEntity round) {
        long attackDice = round.getDice().stream()
                .filter(face -> face == DiceFace.ATTACK)
                .count();

        round.getMatch().getPlayers()
                .forEach(player -> player.setTokens(player.getTokens() + Math.toIntExact(attackDice)));
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
    private void finishMatch(MatchEntity match) {
        match.setStatus(MatchStatus.FINISHED);
        if (match.getPlayers().isEmpty()) {
            return;
        }

        PlayerEntity winner = match.getPlayers().stream()
                .max((left, right) -> left.getTokens().compareTo(right.getTokens()))
                .orElse(match.getPlayers().get(0));

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
