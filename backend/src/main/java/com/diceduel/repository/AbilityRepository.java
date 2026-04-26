package com.diceduel.repository;

import com.diceduel.entity.AbilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbilityRepository extends JpaRepository<AbilityEntity, String> {
}
