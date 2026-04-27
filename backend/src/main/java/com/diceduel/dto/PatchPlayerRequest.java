package com.diceduel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PatchPlayerRequest(
        @Size(max = 80) String name,
        @Min(0) Integer hearts,
        @Min(0) Integer tokens
) {
}
