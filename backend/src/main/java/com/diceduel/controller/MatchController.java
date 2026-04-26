package com.diceduel.controller;

import com.diceduel.dto.ActivateAbilityRequest;
import com.diceduel.dto.CreateMatchRequest;
import com.diceduel.dto.JoinMatchRequest;
import com.diceduel.dto.LockDiceRequest;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.RollDiceRequest;
import com.diceduel.dto.RoundResponse;
import com.diceduel.entity.MatchStatus;
import com.diceduel.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/{matchId}/join")
    public ResponseEntity<Void> joinMatch(
            @PathVariable String matchId,
            @Valid @RequestBody JoinMatchRequest request
    ) {
        matchService.joinMatch(matchId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/start")
    public ResponseEntity<Void> startMatch(@PathVariable String matchId) {
        matchService.startMatch(matchId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{matchId}/state")
    public ResponseEntity<MatchStateResponse> findMatchState(@PathVariable String matchId) {
        return ResponseEntity.ok(matchService.findMatchState(matchId));
    }

    @GetMapping("/{matchId}/rounds/{roundId}")
    public ResponseEntity<RoundResponse> findRound(
            @PathVariable String matchId,
            @PathVariable String roundId
    ) {
        return ResponseEntity.ok(matchService.findRound(matchId, roundId));
    }

    @PostMapping("/{matchId}/rounds/{roundId}/roll")
    public ResponseEntity<Void> rollDice(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody RollDiceRequest request
    ) {
        matchService.rollDice(matchId, roundId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/rounds/{roundId}/lock")
    public ResponseEntity<Void> lockDice(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody LockDiceRequest request
    ) {
        matchService.lockDice(matchId, roundId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/rounds/{roundId}/resolve")
    public ResponseEntity<Void> resolveRound(
            @PathVariable String matchId,
            @PathVariable String roundId
    ) {
        matchService.resolveRound(matchId, roundId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/rounds/{roundId}/abilities/activate")
    public ResponseEntity<Void> activateAbility(
            @PathVariable String matchId,
            @PathVariable String roundId,
            @Valid @RequestBody ActivateAbilityRequest request
    ) {
        matchService.activateAbility(matchId, roundId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{matchId}/replay/export")
    public ResponseEntity<ByteArrayResource> exportReplay(@PathVariable String matchId) {
        ByteArrayResource replay = matchService.exportReplay(matchId);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("match-" + matchId + "-replay.txt")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(replay.contentLength())
                .body(replay);
    }
}
