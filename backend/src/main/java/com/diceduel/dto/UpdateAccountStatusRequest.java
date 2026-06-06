package com.diceduel.dto;

import com.diceduel.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Admin payload to activate or suspend an account.
 */
public record UpdateAccountStatusRequest(
        @NotNull AccountStatus status
) {
}
