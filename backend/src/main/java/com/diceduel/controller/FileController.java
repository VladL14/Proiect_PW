package com.diceduel.controller;

import com.diceduel.dto.AbilityPackResponse;
import com.diceduel.dto.PatchAbilityPackRequest;
import com.diceduel.service.AbilityService;
import com.diceduel.service.MatchService;
import com.diceduel.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FileController {

    private final AbilityService abilityService;
    private final MatchService matchService;
    private final PlayerService playerService;

    public FileController(AbilityService abilityService, MatchService matchService, PlayerService playerService) {
        this.abilityService = abilityService;
        this.matchService = matchService;
        this.playerService = playerService;
    }

    @PostMapping(value = "/ability-packs/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AbilityPackResponse> importAbilityPack(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(abilityService.importAbilityPack(file));
    }

    @GetMapping("/ability-packs/{packId}")
    public ResponseEntity<AbilityPackResponse> findAbilityPack(@PathVariable String packId) {
        return ResponseEntity.ok(abilityService.findAbilityPack(packId));
    }

    @PutMapping(value = "/ability-packs/{packId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AbilityPackResponse> replaceAbilityPack(
            @PathVariable String packId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ) {
        return ResponseEntity.ok(abilityService.replaceAbilityPack(packId, file, name, description));
    }

    @PatchMapping("/ability-packs/{packId}")
    public ResponseEntity<AbilityPackResponse> patchAbilityPack(
            @PathVariable String packId,
            @Valid @RequestBody PatchAbilityPackRequest request
    ) {
        return ResponseEntity.ok(abilityService.patchAbilityPack(packId, request));
    }

    @DeleteMapping("/ability-packs/{packId}")
    public ResponseEntity<Void> deleteAbilityPack(@PathVariable String packId) {
        abilityService.deleteAbilityPack(packId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/matches/{matchId}/replay/export")
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

    @PostMapping(value = "/players/{playerId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(
            @PathVariable String playerId,
            @RequestPart("file") MultipartFile file
    ) {
        playerService.uploadAvatar(playerId, file);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/players/{playerId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> replaceAvatar(
            @PathVariable String playerId,
            @RequestPart("file") MultipartFile file
    ) {
        playerService.uploadAvatar(playerId, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/players/{playerId}/avatar")
    public ResponseEntity<Void> deleteAvatar(@PathVariable String playerId) {
        playerService.deleteAvatar(playerId);
        return ResponseEntity.noContent().build();
    }
}
