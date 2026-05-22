package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

public record AddPlayerAbilityRequest(
        @NotBlank String abilityId
) {
}
