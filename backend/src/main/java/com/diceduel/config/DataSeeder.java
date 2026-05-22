package com.diceduel.config;

import com.diceduel.entity.AbilityEntity;
import com.diceduel.repository.AbilityRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder {

    private final AbilityRepository abilityRepository;

    public DataSeeder(AbilityRepository abilityRepository) {
        this.abilityRepository = abilityRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDefaultAbilities() {
        if (abilityRepository.count() > 0) {
            return;
        }

        abilityRepository.saveAll(List.of(
                new AbilityEntity("power-strike", "Power Strike", 2),
                new AbilityEntity("shield-wall", "Shield Wall", 1),
                new AbilityEntity("token-steal", "Token Steal", 3)
        ));
    }
}
