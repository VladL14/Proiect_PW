package com.diceduel.service.impl;

import com.diceduel.dto.AccountResponse;
import com.diceduel.dto.AuthResponse;
import com.diceduel.dto.LoginRequest;
import com.diceduel.dto.RegisterRequest;
import com.diceduel.entity.AccountStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.Role;
import com.diceduel.exception.ConflictException;
import com.diceduel.exception.ForbiddenException;
import com.diceduel.exception.UnauthorizedException;
import com.diceduel.mapper.PlayerMapper;
import com.diceduel.repository.AbilityRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.security.AuthSession;
import com.diceduel.security.CurrentUser;
import com.diceduel.security.PasswordHasher;
import com.diceduel.security.TokenStore;
import com.diceduel.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final int DEFAULT_HEARTS = 3;
    private static final int DEFAULT_TOKENS = 6;

    private final PlayerRepository playerRepository;
    private final AbilityRepository abilityRepository;
    private final PlayerMapper playerMapper;
    private final PasswordHasher passwordHasher;
    private final TokenStore tokenStore;

    public AuthServiceImpl(
            PlayerRepository playerRepository,
            AbilityRepository abilityRepository,
            PlayerMapper playerMapper,
            PasswordHasher passwordHasher,
            TokenStore tokenStore
    ) {
        this.playerRepository = playerRepository;
        this.abilityRepository = abilityRepository;
        this.playerMapper = playerMapper;
        this.passwordHasher = passwordHasher;
        this.tokenStore = tokenStore;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim();

        if (playerRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username is already taken");
        }
        if (playerRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }

        String displayName = request.displayName() != null && !request.displayName().isBlank()
                ? request.displayName().trim()
                : username;

        PlayerEntity player = new PlayerEntity(
                UUID.randomUUID().toString(),
                displayName,
                DEFAULT_HEARTS,
                DEFAULT_TOKENS,
                0,
                0
        );
        player.setUsername(username);
        player.setEmail(email);
        player.setPasswordHash(passwordHasher.hash(request.password()));
        player.setRole(Role.USER);
        player.setAccountStatus(AccountStatus.ACTIVE);
        player.setCreatedAt(LocalDateTime.now());
        player.setMatchesPlayed(0);
        player.getAbilities().addAll(abilityRepository.findAll());

        PlayerEntity saved = playerRepository.save(player);
        return issue(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.usernameOrEmail().trim();
        PlayerEntity player = playerRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> playerRepository.findByEmailIgnoreCase(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (player.getPasswordHash() == null || !passwordHasher.matches(request.password(), player.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        if (player.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new ForbiddenException("Account is suspended");
        }
        return issue(player);
    }

    @Override
    public void logout(String token) {
        tokenStore.revoke(token);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse currentAccount() {
        AuthSession session = CurrentUser.get()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
        PlayerEntity player = playerRepository.findById(session.playerId())
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists"));
        return playerMapper.toAccountResponse(player);
    }

    private AuthResponse issue(PlayerEntity player) {
        AuthSession session = tokenStore.issue(player);
        return new AuthResponse(
                session.token(),
                session.expiresAt(),
                session.role(),
                playerMapper.toAccountResponse(player)
        );
    }
}
