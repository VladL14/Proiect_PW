package com.diceduel.dto;

import com.diceduel.entity.DiceFace;
import com.diceduel.entity.RoundStatus;

import java.util.List;

public record RoundResponse(
        String id,
        RoundStatus status,
        List<DiceFace> dice,
        List<Boolean> locked
) {
}
