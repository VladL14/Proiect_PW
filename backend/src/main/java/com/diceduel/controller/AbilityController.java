package com.diceduel.controller;

import com.diceduel.dto.ActivateAbilityRequest;
import com.diceduel.dto.AddPlayerAbilityRequest;
import com.diceduel.dto.AbilityResponse;
import com.diceduel.dto.CreateAbilityRequest;
import com.diceduel.dto.PatchAbilityRequest;
import com.diceduel.dto.UpdateAbilityRequest;
import com.diceduel.service.AbilityService;
import com.diceduel.service.MatchService;
import com.diceduel.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api")
public class AbilityController {

    private final AbilityService abilityService;
    private final PlayerService playerService;
    private final MatchService matchService;

    public AbilityController(AbilityService abilityService, PlayerService playerService, MatchService matchService) {
        this.abilityService = abilityService;
        this.playerService = playerService;
        this.matchService = matchService;
    }

    @GetMapping("/abilities")
    public ResponseEntity<List<AbilityResponse>> findAllAbilities() {
        return ResponseEntity.ok(abilityService.findAllAbilities());
    }

    @PostMapping("/abilities")
    public ResponseEntity<AbilityResponse> createAbility(@Valid @RequestBody CreateAbilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(abilityService.createAbility(request));
    }

    @GetMapping("/abilities/{abilityId}")
    public ResponseEntity<AbilityResponse> findAbility(@PathVariable String abilityId) {
        return ResponseEntity.ok(abilityService.findAbility(abilityId));
    }

    @PutMapping("/abilities/{abilityId}")
    public ResponseEntity<AbilityResponse> replaceAbility(
            @PathVariable String abilityId,
            @Valid @RequestBody UpdateAbilityRequest request
    ) {
        return ResponseEntity.ok(abilityService.replaceAbility(abilityId, request));
    }

    @PatchMapping("/abilities/{abilityId}")
    public ResponseEntity<AbilityResponse> patchAbility(
            @PathVariable String abilityId,
            @Valid @RequestBody PatchAbilityRequest request
    ) {
        return ResponseEntity.ok(abilityService.patchAbility(abilityId, request));
    }

    @DeleteMapping("/abilities/{abilityId}")
    public ResponseEntity<Void> deleteAbility(@PathVariable String abilityId) {
        abilityService.deleteAbility(abilityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/players/{playerId}/abilities")
    public ResponseEntity<List<AbilityResponse>> findPlayerAbilities(@PathVariable String playerId) {
        return ResponseEntity.ok(playerService.findPlayerAbilities(playerId));
    }

    @PostMapping("/players/{playerId}/abilities")
    public ResponseEntity<List<AbilityResponse>> addPlayerAbility(
            @PathVariable String playerId,
            @Valid @RequestBody AddPlayerAbilityRequest request
    ) {
        return ResponseEntity.ok(playerService.addPlayerAbility(playerId, request));
    }

    @DeleteMapping("/players/{playerId}/abilities/{abilityId}")
    public ResponseEntity<List<AbilityResponse>> removePlayerAbility(
            @PathVariable String playerId,
            @PathVariable String abilityId
    ) {
        return ResponseEntity.ok(playerService.removePlayerAbility(playerId, abilityId));
    }

    @PostMapping("/matches/{matchId}/rounds/{roundId}/abilities/activate")
    public ResponseEntity<Void> activateAbility(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody ActivateAbilityRequest request
    ) {
        matchService.activateAbility(matchId, roundId, request);
        return ResponseEntity.ok().build();
    }
}
