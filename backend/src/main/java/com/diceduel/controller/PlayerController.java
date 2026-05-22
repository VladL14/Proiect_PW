package com.diceduel.controller;

import com.diceduel.dto.CreatePlayerRequest;
import com.diceduel.dto.PatchPlayerRequest;
import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.PlayerStatsResponse;
import com.diceduel.dto.UpdatePlayerRequest;
import com.diceduel.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(request));
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> findAllPlayers() {
        return ResponseEntity.ok(playerService.findAllPlayers());
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> findPlayer(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.findPlayer(playerId));
    }

    @PutMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> replacePlayer(
            @PathVariable String playerId,
            @Valid @RequestBody UpdatePlayerRequest request
    ) {
        return ResponseEntity.ok(playerService.replacePlayer(playerId, request));
    }

    @PatchMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> patchPlayer(
            @PathVariable String playerId,
            @Valid @RequestBody PatchPlayerRequest request
    ) {
        return ResponseEntity.ok(playerService.patchPlayer(playerId, request));
    }

    @DeleteMapping("/{playerId}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String playerId) {
        playerService.deletePlayer(playerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{playerId}/stats")
    public ResponseEntity<PlayerStatsResponse> findPlayerStats(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.findPlayerStats(playerId));
    }
}
