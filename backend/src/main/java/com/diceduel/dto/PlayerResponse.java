package com.diceduel.dto;

public record PlayerResponse(
        String id,
        String name,
        Integer hearts,
        Integer tokens
) {
}
