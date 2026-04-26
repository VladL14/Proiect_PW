package com.diceduel.repository;

import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, String> {

    List<MatchEntity> findByStatus(MatchStatus status);
}
