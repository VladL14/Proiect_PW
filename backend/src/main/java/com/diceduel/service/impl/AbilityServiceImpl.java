package com.diceduel.service.impl;

import com.diceduel.dto.AbilityPackResponse;
import com.diceduel.dto.AbilityResponse;
import com.diceduel.dto.CreateAbilityRequest;
import com.diceduel.dto.PatchAbilityPackRequest;
import com.diceduel.dto.PatchAbilityRequest;
import com.diceduel.dto.UpdateAbilityRequest;
import com.diceduel.entity.AbilityEntity;
import com.diceduel.entity.AbilityPackEntity;
import com.diceduel.exception.BadRequestException;
import com.diceduel.exception.ResourceNotFoundException;
import com.diceduel.mapper.AbilityMapper;
import com.diceduel.repository.AbilityPackRepository;
import com.diceduel.repository.AbilityRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.service.AbilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AbilityServiceImpl implements AbilityService {

    private final AbilityRepository abilityRepository;
    private final AbilityPackRepository abilityPackRepository;
    private final PlayerRepository playerRepository;
    private final AbilityMapper abilityMapper;

    public AbilityServiceImpl(
            AbilityRepository abilityRepository,
            AbilityPackRepository abilityPackRepository,
            PlayerRepository playerRepository,
            AbilityMapper abilityMapper
    ) {
        this.abilityRepository = abilityRepository;
        this.abilityPackRepository = abilityPackRepository;
        this.playerRepository = playerRepository;
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
    public AbilityResponse createAbility(CreateAbilityRequest request) {
        String abilityId = request.id() == null || request.id().isBlank()
                ? UUID.randomUUID().toString()
                : request.id().trim();
        if (abilityRepository.existsById(abilityId)) {
            throw new BadRequestException("Ability already exists: " + abilityId);
        }

        AbilityEntity ability = new AbilityEntity(abilityId, normalizeName(request.name()), request.cost());
        return abilityMapper.toResponse(abilityRepository.save(ability));
    }

    @Override
    @Transactional(readOnly = true)
    public AbilityResponse findAbility(String abilityId) {
        return abilityMapper.toResponse(findAbilityEntity(abilityId));
    }

    @Override
    public AbilityResponse replaceAbility(String abilityId, UpdateAbilityRequest request) {
        AbilityEntity ability = findAbilityEntity(abilityId);
        ability.setName(normalizeName(request.name()));
        ability.setCost(request.cost());
        return abilityMapper.toResponse(abilityRepository.save(ability));
    }

    @Override
    public AbilityResponse patchAbility(String abilityId, PatchAbilityRequest request) {
        AbilityEntity ability = findAbilityEntity(abilityId);

        if (request.name() != null) {
            ability.setName(normalizeName(request.name()));
        }
        if (request.cost() != null) {
            ability.setCost(request.cost());
        }

        return abilityMapper.toResponse(abilityRepository.save(ability));
    }

    @Override
    public void deleteAbility(String abilityId) {
        AbilityEntity ability = findAbilityEntity(abilityId);
        playerRepository.findAll().forEach(player -> {
            player.getAbilities().removeIf(existing -> existing.getId().equals(abilityId));
            playerRepository.save(player);
        });
        abilityRepository.delete(ability);
    }

    @Override
    public AbilityPackResponse importAbilityPack(MultipartFile file) {
        validateAbilityPack(file);
        saveAbilitiesFromPack(file);

        AbilityPackEntity pack = new AbilityPackEntity(
                UUID.randomUUID().toString(),
                resolvePackName(file.getOriginalFilename()),
                null,
                file.getOriginalFilename(),
                file.getContentType(),
                LocalDateTime.now()
        );
        return toPackResponse(abilityPackRepository.save(pack));
    }

    @Override
    @Transactional(readOnly = true)
    public AbilityPackResponse findAbilityPack(String packId) {
        return toPackResponse(findAbilityPackEntity(packId));
    }

    @Override
    public AbilityPackResponse replaceAbilityPack(String packId, MultipartFile file, String name, String description) {
        AbilityPackEntity pack = findAbilityPackEntity(packId);
        validateAbilityPack(file);
        saveAbilitiesFromPack(file);

        pack.setName(name == null || name.isBlank() ? resolvePackName(file.getOriginalFilename()) : name.trim());
        pack.setDescription(description);
        pack.setFileName(file.getOriginalFilename());
        pack.setContentType(file.getContentType());
        return toPackResponse(abilityPackRepository.save(pack));
    }

    @Override
    public AbilityPackResponse patchAbilityPack(String packId, PatchAbilityPackRequest request) {
        AbilityPackEntity pack = findAbilityPackEntity(packId);

        if (request.name() != null) {
            pack.setName(normalizeName(request.name()));
        }
        if (request.description() != null) {
            pack.setDescription(request.description());
        }

        return toPackResponse(abilityPackRepository.save(pack));
    }

    @Override
    public void deleteAbilityPack(String packId) {
        AbilityPackEntity pack = findAbilityPackEntity(packId);
        abilityPackRepository.delete(pack);
    }

    private void saveAbilitiesFromPack(MultipartFile file) {
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

    private AbilityEntity findAbilityEntity(String abilityId) {
        return abilityRepository.findById(abilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Ability not found: " + abilityId));
    }

    private AbilityPackEntity findAbilityPackEntity(String packId) {
        return abilityPackRepository.findById(packId)
                .orElseThrow(() -> new ResourceNotFoundException("Ability pack not found: " + packId));
    }

    private AbilityPackResponse toPackResponse(AbilityPackEntity pack) {
        return new AbilityPackResponse(
                pack.getId(),
                pack.getName(),
                pack.getDescription(),
                pack.getFileName(),
                pack.getContentType()
        );
    }

    private String resolvePackName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "Imported ability pack";
        }
        return fileName;
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new BadRequestException("Name is required");
        }
        return normalized;
    }
}
