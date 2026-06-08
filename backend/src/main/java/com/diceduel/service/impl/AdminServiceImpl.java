package com.diceduel.service.impl;

import com.diceduel.dto.AccountResponse;
import com.diceduel.dto.MatchHistoryResponse;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.ServerStatusResponse;
import com.diceduel.entity.AccountStatus;
import com.diceduel.entity.MatchStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.Role;
import com.diceduel.exception.ResourceNotFoundException;
import com.diceduel.mapper.PlayerMapper;
import com.diceduel.repository.MatchHistoryRepository;
import com.diceduel.repository.MatchRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.service.AdminService;
import com.diceduel.service.MatchHistoryService;
import com.diceduel.service.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final PlayerMapper playerMapper;
    private final MatchService matchService;
    private final MatchHistoryService matchHistoryService;

    public AdminServiceImpl(
            PlayerRepository playerRepository,
            MatchRepository matchRepository,
            MatchHistoryRepository matchHistoryRepository,
            PlayerMapper playerMapper,
            MatchService matchService,
            MatchHistoryService matchHistoryService
    ) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchHistoryRepository = matchHistoryRepository;
        this.playerMapper = playerMapper;
        this.matchService = matchService;
        this.matchHistoryService = matchHistoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        return playerRepository.findAll()
                .stream()
                .map(playerMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findAccount(String accountId) {
        return playerMapper.toAccountResponse(findPlayer(accountId));
    }

    @Override
    public AccountResponse updateAccountStatus(String accountId, AccountStatus status) {
        PlayerEntity player = findPlayer(accountId);
        player.setAccountStatus(status);
        return playerMapper.toAccountResponse(playerRepository.save(player));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> listAllMatches() {
        return matchService.findMatches(null);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchStateResponse findMatchDetail(String matchId) {
        return matchService.findMatchState(matchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchHistoryResponse> listGlobalHistory() {
        return matchHistoryService.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ServerStatusResponse serverStatus() {
        return new ServerStatusResponse(
                playerRepository.count(),
                playerRepository.findByRole(Role.ADMIN).size(),
                matchRepository.count(),
                matchRepository.findByStatus(MatchStatus.WAITING).size(),
                matchRepository.findByStatus(MatchStatus.READY).size(),
                matchRepository.findByStatus(MatchStatus.IN_PROGRESS).size(),
                matchRepository.findByStatus(MatchStatus.FINISHED).size(),
                matchHistoryRepository.count(),
                Instant.now()
        );
    }

    private PlayerEntity findPlayer(String accountId) {
        return playerRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }
}
