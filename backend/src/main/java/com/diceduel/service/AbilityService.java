package com.diceduel.service;

import com.diceduel.dto.AbilityResponse;
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

    /**
     * Imports abilities from a text pack uploaded through multipart form data.
     *
     * @param file ability pack file
     */
    void importAbilityPack(MultipartFile file);
}
