package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivateAbilityRequest(
        @NotBlank String playerId,
        @NotBlank String abilityId,
        String targetId
) {
}
