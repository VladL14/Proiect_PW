package com.diceduel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePlayerRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull @Min(0) Integer hearts,
        @NotNull @Min(0) Integer tokens
) {
}
