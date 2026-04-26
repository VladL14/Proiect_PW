package com.diceduel.controller;

import com.diceduel.dto.AbilityResponse;
import com.diceduel.service.AbilityService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AbilityController {

    private final AbilityService abilityService;

    public AbilityController(AbilityService abilityService) {
        this.abilityService = abilityService;
    }

    @GetMapping("/abilities")
    public ResponseEntity<List<AbilityResponse>> findAllAbilities() {
        return ResponseEntity.ok(abilityService.findAllAbilities());
    }

    @PostMapping(value = "/ability-packs/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> importAbilityPack(@RequestPart("file") MultipartFile file) {
        abilityService.importAbilityPack(file);
        return ResponseEntity.ok().build();
    }
}
