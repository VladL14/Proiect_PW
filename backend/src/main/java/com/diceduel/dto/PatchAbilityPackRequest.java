package com.diceduel.dto;

import jakarta.validation.constraints.Size;

public record PatchAbilityPackRequest(
        @Size(max = 120) String name,
        @Size(max = 500) String description
) {
}
