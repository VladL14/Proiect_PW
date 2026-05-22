package com.diceduel.dto;

import com.diceduel.entity.DiceFace;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RoundPlayerStateRequest(
        @NotBlank String playerId,
        List<DiceFace> dice,
        List<Boolean> locked,
        List<String> targetPlayerIds
) {
}
