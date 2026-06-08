package com.diceduel.controller;

import com.diceduel.dto.EmoteResponse;
import com.diceduel.dto.SendEmoteRequest;
import com.diceduel.social.EmoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Social emote endpoints scoped to a match.
 */
@RestController
@RequestMapping("/api/matches/{matchId}/emotes")
public class EmoteController {

    private final EmoteService emoteService;

    public EmoteController(EmoteService emoteService) {
        this.emoteService = emoteService;
    }

    @PostMapping
    public ResponseEntity<EmoteResponse> sendEmote(
            @PathVariable String matchId,
            @Valid @RequestBody SendEmoteRequest request
    ) {
        EmoteResponse emote = emoteService.sendEmote(matchId, request.playerId(), request.emote());
        return ResponseEntity.status(HttpStatus.CREATED).body(emote);
    }

    @GetMapping
    public ResponseEntity<List<EmoteResponse>> getRecentEmotes(
            @PathVariable String matchId,
            @RequestParam(required = false, defaultValue = "0") long since
    ) {
        return ResponseEntity.ok(emoteService.getRecentEmotes(matchId, since));
    }
}
