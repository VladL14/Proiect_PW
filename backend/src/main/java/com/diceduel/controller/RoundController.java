package com.diceduel.controller;

import com.diceduel.dto.LockDiceRequest;
import com.diceduel.dto.PatchRoundRequest;
import com.diceduel.dto.RollDiceRequest;
import com.diceduel.dto.RoundResponse;
import com.diceduel.dto.UpdateLockedDiceRequest;
import com.diceduel.dto.UpdateRoundRequest;
import com.diceduel.service.MatchService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/matches/{matchId}/rounds")
public class RoundController {

    private final MatchService matchService;

    public RoundController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/{roundId}")
    public ResponseEntity<RoundResponse> findRound(
            @PathVariable String matchId,
            @PathVariable String roundId
    ) {
        return ResponseEntity.ok(matchService.findRound(matchId, roundId));
    }

    @PutMapping("/{roundId}")
    public ResponseEntity<RoundResponse> replaceRound(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody UpdateRoundRequest request
    ) {
        return ResponseEntity.ok(matchService.replaceRound(matchId, roundId, request));
    }

    @PatchMapping("/{roundId}")
    public ResponseEntity<RoundResponse> patchRound(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody PatchRoundRequest request
    ) {
        return ResponseEntity.ok(matchService.patchRound(matchId, roundId, request));
    }

    @DeleteMapping("/{roundId}")
    public ResponseEntity<Void> deleteRound(
            @PathVariable String matchId,
            @PathVariable String roundId
    ) {
        matchService.deleteRound(matchId, roundId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roundId}/roll")
    public ResponseEntity<Void> rollDice(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody RollDiceRequest request
    ) {
        matchService.rollDice(matchId, roundId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roundId}/lock")
    public ResponseEntity<Void> lockDice(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody LockDiceRequest request
    ) {
        matchService.lockDice(matchId, roundId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{roundId}/locked-dice")
    public ResponseEntity<RoundResponse> updateLockedDice(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody UpdateLockedDiceRequest request
    ) {
        return ResponseEntity.ok(matchService.updateLockedDice(matchId, roundId, request));
    }

    @PostMapping("/{roundId}/resolve")
    public ResponseEntity<Void> resolveRound(
            @PathVariable String matchId,
            @PathVariable String roundId
    ) {
        matchService.resolveRound(matchId, roundId);
        return ResponseEntity.ok().build();
    }
}
