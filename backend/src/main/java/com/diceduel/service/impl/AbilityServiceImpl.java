package com.diceduel.service.impl;

import com.diceduel.dto.AbilityResponse;
import com.diceduel.entity.AbilityEntity;
import com.diceduel.exception.BadRequestException;
import com.diceduel.mapper.AbilityMapper;
import com.diceduel.repository.AbilityRepository;
import com.diceduel.service.AbilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
public class AbilityServiceImpl implements AbilityService {

    private final AbilityRepository abilityRepository;
    private final AbilityMapper abilityMapper;

    public AbilityServiceImpl(AbilityRepository abilityRepository, AbilityMapper abilityMapper) {
        this.abilityRepository = abilityRepository;
        this.abilityMapper = abilityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbilityResponse> findAllAbilities() {
        return abilityRepository.findAll()
                .stream()
                .map(abilityMapper::toResponse)
                .toList();
    }

    @Override
    public void importAbilityPack(MultipartFile file) {
        validateAbilityPack(file);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<AbilityEntity> abilities = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .map(this::parseAbilityPackLine)
                    .toList();

            abilityRepository.saveAll(abilities);
        } catch (IOException exception) {
            throw new BadRequestException("Could not read ability pack file");
        }
    }

    /**
     * Validates that an imported ability pack exists and is not empty.
     *
     * @param file uploaded ability pack file
     */
    private void validateAbilityPack(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Ability pack file is required");
        }
    }

    /**
     * Converts one CSV line into an ability entity using the format id,name,cost.
     *
     * @param line CSV line from the uploaded ability pack
     * @return parsed ability entity
     */
    private AbilityEntity parseAbilityPackLine(String line) {
        String[] values = line.split(",");
        if (values.length != 3) {
            throw new BadRequestException("Invalid ability pack line: " + line);
        }

        try {
            return new AbilityEntity(
                    values[0].trim(),
                    values[1].trim(),
                    Integer.parseInt(values[2].trim())
            );
        } catch (NumberFormatException exception) {
            throw new BadRequestException("Ability cost must be a number: " + line);
        }
    }
}
