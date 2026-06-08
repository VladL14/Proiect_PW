package com.diceduel.controller;

import com.diceduel.dto.AccountResponse;
import com.diceduel.dto.AuthResponse;
import com.diceduel.dto.LoginRequest;
import com.diceduel.dto.RegisterRequest;
import com.diceduel.entity.Role;
import com.diceduel.security.RequireRole;
import com.diceduel.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints: registration, login, logout and the
 * "who am I" lookup used by the frontend to restore a session.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @RequireRole({Role.USER, Role.ADMIN})
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            authService.logout(header.substring(BEARER_PREFIX.length()).trim());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @RequireRole({Role.USER, Role.ADMIN})
    public ResponseEntity<AccountResponse> me() {
        return ResponseEntity.ok(authService.currentAccount());
    }
}
