package com.diceduel.service;

import com.diceduel.dto.AccountResponse;
import com.diceduel.dto.AuthResponse;
import com.diceduel.dto.LoginRequest;
import com.diceduel.dto.RegisterRequest;

/**
 * Authentication and account lifecycle operations.
 */
public interface AuthService {

    /**
     * Registers a new account, hashing the password and issuing a token.
     *
     * @param request registration payload
     * @return authentication result with token and account snapshot
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates an existing account and issues a token.
     *
     * @param request login payload
     * @return authentication result with token and account snapshot
     */
    AuthResponse login(LoginRequest request);

    /**
     * Revokes the supplied bearer token.
     *
     * @param token bearer token value
     */
    void logout(String token);

    /**
     * Returns the account snapshot for the currently authenticated session.
     *
     * @return current account
     */
    AccountResponse currentAccount();
}
