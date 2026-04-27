package com.diceduel.service;

import com.diceduel.dto.AbilityPackResponse;
import com.diceduel.dto.AbilityResponse;
import com.diceduel.dto.CreateAbilityRequest;
import com.diceduel.dto.PatchAbilityPackRequest;
import com.diceduel.dto.PatchAbilityRequest;
import com.diceduel.dto.UpdateAbilityRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Defines business operations for game abilities and imported ability packs.
 */
public interface AbilityService {

    /**
     * Retrieves all abilities known by the game.
     *
     * @return list of ability representations
     */
    List<AbilityResponse> findAllAbilities();

    AbilityResponse createAbility(CreateAbilityRequest request);

    AbilityResponse findAbility(String abilityId);

    AbilityResponse replaceAbility(String abilityId, UpdateAbilityRequest request);

    AbilityResponse patchAbility(String abilityId, PatchAbilityRequest request);

    void deleteAbility(String abilityId);

    /**
     * Imports abilities from a text pack uploaded through multipart form data.
     *
     * @param file ability pack file
     */
    AbilityPackResponse importAbilityPack(MultipartFile file);

    AbilityPackResponse findAbilityPack(String packId);

    AbilityPackResponse replaceAbilityPack(String packId, MultipartFile file, String name, String description);

    AbilityPackResponse patchAbilityPack(String packId, PatchAbilityPackRequest request);

    void deleteAbilityPack(String packId);
}
