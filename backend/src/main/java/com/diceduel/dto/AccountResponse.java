package com.diceduel.dto;

import com.diceduel.entity.AccountStatus;
import com.diceduel.entity.Role;

import java.time.LocalDateTime;

/**
 * Administrative / profile view of an account. Never exposes the password hash.
 *
 * @param id            player/account identifier
 * @param username      login handle (may be null for legacy players)
 * @param email         contact email (may be null for legacy players)
 * @param name          in-game display name
 * @param role          access-control role
 * @param status        account lifecycle status
 * @param createdAt     account creation timestamp
 * @param matchesPlayed number of finished matches the account took part in
 * @param wins          number of wins
 * @param losses        number of losses
 */
public record AccountResponse(
        String id,
        String username,
        String email,
        String name,
        Role role,
        AccountStatus status,
        LocalDateTime createdAt,
        Integer matchesPlayed,
        Integer wins,
        Integer losses
) {
}
