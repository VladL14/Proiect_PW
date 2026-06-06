package com.diceduel.config;

import com.diceduel.entity.AbilityEntity;
import com.diceduel.entity.AccountStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.Role;
import com.diceduel.repository.AbilityRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.security.PasswordHasher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DataSeeder {

    private final AbilityRepository abilityRepository;
    private final PlayerRepository playerRepository;
    private final PasswordHasher passwordHasher;

    public DataSeeder(
            AbilityRepository abilityRepository,
            PlayerRepository playerRepository,
            PasswordHasher passwordHasher
    ) {
        this.abilityRepository = abilityRepository;
        this.playerRepository = playerRepository;
        this.passwordHasher = passwordHasher;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDefaultAbilities() {
        if (abilityRepository.count() == 0) {
            abilityRepository.saveAll(List.of(
                    new AbilityEntity("power-strike", "Power Strike", 2),
                    new AbilityEntity("shield-wall", "Shield Wall", 1),
                    new AbilityEntity("token-steal", "Token Steal", 3)
            ));
        }

        seedDefaultAdmin();
    }

    /**
     * Creates a default administrator account so the admin dashboard and the
     * ACL-protected endpoints can be exercised out of the box.
     * Credentials: {@code admin / admin123}.
     */
    private void seedDefaultAdmin() {
        if (playerRepository.existsByUsernameIgnoreCase("admin")) {
            return;
        }
        PlayerEntity admin = new PlayerEntity(
                UUID.randomUUID().toString(),
                "Administrator",
                3,
                3,
                0,
                0
        );
        admin.setUsername("admin");
        admin.setEmail("admin@diceduel.local");
        admin.setPasswordHash(passwordHasher.hash("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setMatchesPlayed(0);
        playerRepository.save(admin);
    }
}
