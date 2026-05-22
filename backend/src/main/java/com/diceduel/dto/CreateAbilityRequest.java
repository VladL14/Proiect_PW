package com.diceduel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAbilityRequest(
        String id,
        @NotBlank @Size(max = 120) String name,
        @NotNull @Min(0) Integer cost
) {
}
