package com.diceduel.controller;

import com.diceduel.dto.CreateMatchRequest;
import com.diceduel.dto.JoinMatchRequest;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.PatchMatchRequest;
import com.diceduel.dto.UpdateMatchRequest;
import com.diceduel.dto.UpdateMatchStatusRequest;
import com.diceduel.entity.MatchStatus;
import com.diceduel.service.MatchService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(request));
    }

    @GetMapping
    public ResponseEntity<List<MatchResponse>> findMatches(@RequestParam(required = false) MatchStatus status) {
        return ResponseEntity.ok(matchService.findMatches(status));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponse> findMatch(@PathVariable String matchId) {
        return ResponseEntity.ok(matchService.findMatch(matchId));
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<MatchResponse> replaceMatch(
            @PathVariable String matchId,
            @Valid @RequestBody UpdateMatchRequest request
    ) {
        return ResponseEntity.ok(matchService.replaceMatch(matchId, request));
    }

    @PatchMapping("/{matchId}")
    public ResponseEntity<MatchResponse> patchMatch(
            @PathVariable String matchId,
            @Valid @RequestBody PatchMatchRequest request
    ) {
        return ResponseEntity.ok(matchService.patchMatch(matchId, request));
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable String matchId) {
        matchService.deleteMatch(matchId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{matchId}/join")
    public ResponseEntity<Void> joinMatch(
            @PathVariable String matchId,
            @Valid @RequestBody JoinMatchRequest request
    ) {
        matchService.joinMatch(matchId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{matchId}/players/{playerId}")
    public ResponseEntity<Void> removePlayerFromMatch(
            @PathVariable String matchId,
            @PathVariable String playerId
    ) {
        matchService.removePlayerFromMatch(matchId, playerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{matchId}/start")
    public ResponseEntity<Void> startMatch(@PathVariable String matchId) {
        matchService.startMatch(matchId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchId}/status")
    public ResponseEntity<MatchResponse> updateMatchStatus(
            @PathVariable String matchId,
            @Valid @RequestBody UpdateMatchStatusRequest request
    ) {
        return ResponseEntity.ok(matchService.updateMatchStatus(matchId, request));
    }

    @GetMapping("/{matchId}/state")
    public ResponseEntity<MatchStateResponse> findMatchState(@PathVariable String matchId) {
        return ResponseEntity.ok(matchService.findMatchState(matchId));
    }
}
