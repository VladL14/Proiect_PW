package com.diceduel.repository;

import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, String> {

    Optional<PlayerEntity> findByUsernameIgnoreCase(String username);

    Optional<PlayerEntity> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<PlayerEntity> findByRole(Role role);
}
