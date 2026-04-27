package com.diceduel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PatchAbilityRequest(
        @Size(max = 120) String name,
        @Min(0) Integer cost
) {
}
