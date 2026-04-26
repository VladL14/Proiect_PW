package com.diceduel.controller;

import com.diceduel.dto.AbilityResponse;
import com.diceduel.dto.CreatePlayerRequest;
import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.PlayerStatsResponse;
import com.diceduel.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/{playerId}/stats")
    public ResponseEntity<PlayerStatsResponse> findPlayerStats(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.findPlayerStats(playerId));
    }

    @GetMapping("/{playerId}/abilities")
    public ResponseEntity<List<AbilityResponse>> findPlayerAbilities(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.findPlayerAbilities(playerId));
    }

    @PostMapping(value = "/{playerId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(
            @PathVariable String playerId,
            @RequestPart("file") MultipartFile file
    ) {
        playerService.uploadAvatar(playerId, file);
        return ResponseEntity.ok().build();
    }
}
