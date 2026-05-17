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
import com.diceduel.dto.RoundResponse;
import com.diceduel.dto.SetTargetRequest;
import com.diceduel.dto.UpdateLockedDiceRequest;
import com.diceduel.dto.UpdateMatchRequest;
import com.diceduel.dto.UpdateMatchStatusRequest;
import com.diceduel.dto.UpdateRoundRequest;
import com.diceduel.entity.AbilityEntity;
import com.diceduel.entity.DiceFace;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.MatchStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.PlayerRoundStateEntity;
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
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

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
        match.setCurrentRoundNumber(1);
        match.setCurrentTurnPlayerId(match.getPlayers().get(0).getId());
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
        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public RoundResponse patchRound(String matchId, String roundId, PatchRoundRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        if (request.status() != null) {
            round.setStatus(request.status());
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
    public void rollDice(String matchId, String roundId, RollDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        MatchEntity match = round.getMatch();
        validatePlayerInMatch(match, request.playerId());
        validateRoundCanBePlayed(round);
        
        if (!request.playerId().equals(match.getCurrentTurnPlayerId())) {
            throw new BadRequestException("It is not your turn!");
        }

        PlayerRoundStateEntity state = getPlayerState(round, request.playerId());
        if (state.getRollsCount() >= 2) {
            throw new BadRequestException("Maximum rolls reached for this round.");
        }

        List<DiceFace> currentDice = new ArrayList<>(state.getDice());
        List<Boolean> locked = ensureLockList(state.getLocked());
        List<DiceFace> diceFaces = Arrays.asList(DiceFace.values());
        List<DiceFace> rolledDice = new ArrayList<>();

        for (int i = 0; i < DICE_COUNT; i++) {
            if (i < currentDice.size() && Boolean.TRUE.equals(locked.get(i))) {
                rolledDice.add(currentDice.get(i));
            } else {
                rolledDice.add(diceFaces.get(random.nextInt(diceFaces.size())));
            }
        }

        state.setDice(rolledDice);
        state.setRollsCount(state.getRollsCount() + 1);
        
        if (state.getRollsCount() >= 2) {
            List<Boolean> allLocked = new ArrayList<>();
            for (int i = 0; i < DICE_COUNT; i++) allLocked.add(true);
            state.setLocked(allLocked);
            advanceTurn(match, round);
        } else {
            state.setLocked(ensureLockList(state.getLocked()));
            round.setStatus(RoundStatus.ROLLING);
        }
        
        roundRepository.save(round);
    }

    private void advanceTurn(MatchEntity match, RoundEntity round) {
        List<PlayerEntity> players = match.getPlayers().stream().filter(p -> p.getHearts() > 0).collect(Collectors.toList());
        int currentIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(match.getCurrentTurnPlayerId())) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex != -1 && currentIndex < players.size() - 1) {
            match.setCurrentTurnPlayerId(players.get(currentIndex + 1).getId());
        } else {
            round.setStatus(RoundStatus.TARGET_SELECTION);
            match.setCurrentTurnPlayerId(null);
        }
    }

    @Override
    public void lockDice(String matchId, String roundId, LockDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        MatchEntity match = round.getMatch();
        validatePlayerInMatch(match, request.playerId());
        
        if (!request.playerId().equals(match.getCurrentTurnPlayerId())) {
            throw new BadRequestException("It is not your turn!");
        }

        PlayerRoundStateEntity state = getPlayerState(round, request.playerId());
        if (state.getDice() == null || state.getDice().isEmpty()) {
            throw new BadRequestException("Dice must be rolled first");
        }

        List<Boolean> locked = new ArrayList<>(ensureLockList(state.getLocked()));
        request.lockedIndexes().forEach(index -> lockIndex(locked, index));
        state.setLocked(locked);
        
        long lockCount = locked.stream().filter(b -> b != null && b).count();
        if (lockCount == DICE_COUNT) {
            state.setRollsCount(2);
            advanceTurn(match, round);
        }
        
        roundRepository.save(round);
    }

    @Override
    public void setTarget(String matchId, String roundId, SetTargetRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        validatePlayerInMatch(round.getMatch(), request.playerId());
        
        PlayerRoundStateEntity state = getPlayerState(round, request.playerId());
        if (request.diceTargets() != null) {
            state.getDiceTargets().putAll(request.diceTargets());
        }
        roundRepository.save(round);
    }

    @Override
    public RoundResponse updateLockedDice(String matchId, String roundId, UpdateLockedDiceRequest request) {
        RoundEntity round = findRoundEntity(matchId, roundId);
        return roundMapper.toResponse(roundRepository.save(round));
    }

    @Override
    public void resolveRound(String matchId, String roundId) {
        RoundEntity round = findRoundEntity(matchId, roundId);

        if (round.getStatus() == RoundStatus.RESOLVED) {
            throw new BadRequestException("Round is already resolved");
        }

        applyOrlogResolve(round);
        round.setStatus(RoundStatus.RESOLVED);

        MatchEntity match = round.getMatch();
        
        long alivePlayers = match.getPlayers().stream().filter(p -> p.getHearts() > 0).count();
        if (alivePlayers <= 1) {
            finishMatch(match);
        } else {
            createNextRound(match, round.getRoundNumber() + 1);
            match.setCurrentTurnPlayerId(match.getPlayers().stream().filter(p -> p.getHearts() > 0).findFirst().get().getId());
        }

        matchRepository.save(match);
    }

    private void applyOrlogResolve(RoundEntity round) {
        MatchEntity match = round.getMatch();
        List<PlayerEntity> players = match.getPlayers();
        
        for (PlayerRoundStateEntity state : round.getPlayerStates()) {
            long goldCount = state.getDice().stream().filter(d -> d.name().endsWith("_GOLD")).count();
            PlayerEntity p = state.getPlayer();
            p.setTokens(p.getTokens() + (int) goldCount);
        }
        
        for (PlayerRoundStateEntity state : round.getPlayerStates()) {
            for (int i = 0; i < state.getDice().size(); i++) {
                DiceFace d = state.getDice().get(i);
                if (d == DiceFace.STEAL || d == DiceFace.STEAL_GOLD) {
                    String targetId = state.getDiceTargets().get(i);
                    if (targetId != null) {
                        PlayerEntity target = players.stream().filter(p -> p.getId().equals(targetId)).findFirst().orElse(null);
                        if (target != null && target.getTokens() > 0) {
                            target.setTokens(target.getTokens() - 1);
                            state.getPlayer().setTokens(state.getPlayer().getTokens() + 1);
                        }
                    }
                }
            }
        }
        
        java.util.Map<String, Integer> incomingAttacks = new java.util.HashMap<>();
        for (PlayerRoundStateEntity state : round.getPlayerStates()) {
            for (int i = 0; i < state.getDice().size(); i++) {
                DiceFace d = state.getDice().get(i);
                if (d == DiceFace.ATTACK || d == DiceFace.ATTACK_GOLD) {
                    String targetId = state.getDiceTargets().get(i);
                    if (targetId != null) {
                        incomingAttacks.put(targetId, incomingAttacks.getOrDefault(targetId, 0) + 1);
                    }
                }
            }
        }
        
        for (PlayerRoundStateEntity targetState : round.getPlayerStates()) {
            String pId = targetState.getPlayer().getId();
            int attacks = incomingAttacks.getOrDefault(pId, 0);
            if (attacks > 0) {
                long shieldCount = targetState.getDice().stream().filter(td -> td == DiceFace.SHIELD || td == DiceFace.SHIELD_GOLD).count();
                int damage = Math.max(0, attacks - (int)shieldCount);
                PlayerEntity targetPlayer = targetState.getPlayer();
                targetPlayer.setHearts(Math.max(0, targetPlayer.getHearts() - damage));
            }
        }
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

    private PlayerRoundStateEntity getPlayerState(RoundEntity round, String playerId) {
        return round.getPlayerStates().stream()
                .filter(s -> s.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Player state not found in round"));
    }

    private PlayerRoundStateEntity getPlayerStateOrNull(RoundEntity round, String playerId) {
        return round.getPlayerStates().stream()
                .filter(s -> s.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    private MatchEntity findMatchEntity(String matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));
    }

    private PlayerEntity findPlayer(String playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    private RoundEntity findRoundEntity(String matchId, String roundId) {
        return roundRepository.findByMatch_IdAndId(matchId, roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found: " + roundId));
    }

    private Integer resolveMaxPlayers(Integer requestedMaxPlayers) {
        return requestedMaxPlayers == null ? DEFAULT_MAX_PLAYERS : requestedMaxPlayers;
    }

    private void validateMatchConfigEditable(MatchEntity match) {
        if (match.getStatus() == MatchStatus.IN_PROGRESS || match.getStatus() == MatchStatus.FINISHED) {
            throw new BadRequestException("Only waiting or ready matches can be modified");
        }
    }

    private void validateMaxPlayers(MatchEntity match, Integer maxPlayers) {
        if (maxPlayers < match.getPlayers().size()) {
            throw new BadRequestException("Max players cannot be lower than current player count");
        }
    }

    private void refreshLobbyStatus(MatchEntity match) {
        if (match.getStatus() == MatchStatus.IN_PROGRESS || match.getStatus() == MatchStatus.FINISHED) {
            return;
        }
        match.setStatus(match.getPlayers().size() >= match.getMaxPlayers()
                ? MatchStatus.READY
                : MatchStatus.WAITING);
    }

    private void applyMatchStatus(MatchEntity match, MatchStatus status) {
        if (status == MatchStatus.IN_PROGRESS) {
            validateMatchStartable(match);
            if (match.getRounds().isEmpty()) {
                match.setCurrentRoundNumber(1);
                match.setCurrentTurnPlayerId(match.getPlayers().get(0).getId());
                match.getRounds().add(createRound(match, 1));
            }
        }
        if (status == MatchStatus.WAITING || status == MatchStatus.READY) {
            validateMatchConfigEditable(match);
        }
        match.setStatus(status);
    }

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

    private void validateMatchStartable(MatchEntity match) {
        if (match.getStatus() == MatchStatus.IN_PROGRESS || match.getStatus() == MatchStatus.FINISHED) {
            throw new BadRequestException("Match cannot be started from status " + match.getStatus());
        }
        if (match.getPlayers().size() < 2) {
            throw new BadRequestException("A match needs at least two players to start");
        }
    }

    private RoundEntity createRound(MatchEntity match, int roundNumber) {
        RoundEntity round = new RoundEntity();
        round.setId(UUID.randomUUID().toString());
        round.setRoundNumber(roundNumber);
        round.setStatus(RoundStatus.INITIALIZED);
        round.setMatch(match);
        
        List<PlayerRoundStateEntity> states = new ArrayList<>();
        for (PlayerEntity player : match.getPlayers()) {
            PlayerRoundStateEntity state = new PlayerRoundStateEntity();
            state.setId(UUID.randomUUID().toString());
            state.setPlayer(player);
            state.setRound(round);
            state.setDice(new ArrayList<>());
            state.setLocked(new ArrayList<>());
            state.setRollsCount(0);
            states.add(state);
        }
        round.setPlayerStates(states);
        return round;
    }

    private void validatePlayerInMatch(MatchEntity match, String playerId) {
        boolean found = match.getPlayers().stream()
                .anyMatch(player -> player.getId().equals(playerId));
        if (!found) {
            throw new BadRequestException("Player is not part of this match");
        }
    }

    private void validateRoundCanBePlayed(RoundEntity round) {
        if (round.getMatch().getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BadRequestException("Match is not in progress");
        }
        if (round.getStatus() == RoundStatus.RESOLVED) {
            throw new BadRequestException("Resolved rounds cannot be changed");
        }
    }

    private List<Boolean> ensureLockList(List<Boolean> locked) {
        List<Boolean> normalized = new ArrayList<>();
        for (int i = 0; i < DICE_COUNT; i++) {
            normalized.add(locked != null && i < locked.size() && Boolean.TRUE.equals(locked.get(i)));
        }
        return normalized;
    }

    private void lockIndex(List<Boolean> locked, Integer index) {
        if (index == null || index < 0 || index >= DICE_COUNT) {
            throw new BadRequestException("Locked dice index must be between 0 and " + (DICE_COUNT - 1));
        }
        locked.set(index, true);
    }

    private void createNextRound(MatchEntity match, int nextRoundNumber) {
        match.setCurrentRoundNumber(nextRoundNumber);
        RoundEntity nextRound = createRound(match, nextRoundNumber);
        match.getRounds().add(nextRound);
    }

    private void finishMatch(MatchEntity match) {
        match.setStatus(MatchStatus.FINISHED);
        if (match.getPlayers().isEmpty()) {
            return;
        }

        PlayerEntity winner = match.getPlayers().stream()
                .filter(p -> p.getHearts() > 0)
                .findFirst()
                .orElse(match.getPlayers().get(0));

        match.getPlayers().forEach(player -> {
            if (player.getId().equals(winner.getId())) {
                player.setWins(player.getWins() + 1);
            } else {
                player.setLosses(player.getLosses() + 1);
            }
        });
    }

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
                .sorted(Comparator.comparing(RoundEntity::getRoundNumber))
                .forEach(round -> replay
                        .append("- Round ")
                        .append(round.getRoundNumber())
                        .append(" status=")
                        .append(round.getStatus())
                        .append('\n'));

        return replay.toString();
    }
}
