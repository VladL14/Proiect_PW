package com.diceduel.repository;

import com.diceduel.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoundRepository extends JpaRepository<RoundEntity, String> {

    Optional<RoundEntity> findByMatch_IdAndId(String matchId, String roundId);

    Optional<RoundEntity> findFirstByMatch_IdAndRoundNumberOrderByRoundNumberDesc(String matchId, Integer roundNumber);
}
