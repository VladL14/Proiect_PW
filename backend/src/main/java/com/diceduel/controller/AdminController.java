package com.diceduel.controller;

import com.diceduel.dto.AccountResponse;
import com.diceduel.dto.MatchHistoryResponse;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.ServerStatusResponse;
import com.diceduel.dto.UpdateAccountStatusRequest;
import com.diceduel.entity.Role;
import com.diceduel.security.RequireRole;
import com.diceduel.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Administration endpoints. The whole controller is gated by the ACL: only an
 * authenticated ADMIN session may reach any of these resources. A regular user
 * receives 403 and an anonymous caller 401, regardless of what the frontend
 * chooses to display.
 */
@RestController
@RequestMapping("/api/admin")
@RequireRole(Role.ADMIN)
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AccountResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listAccounts());
    }

    @GetMapping("/users/{accountId}")
    public ResponseEntity<AccountResponse> findUser(@PathVariable String accountId) {
        return ResponseEntity.ok(adminService.findAccount(accountId));
    }

    @PatchMapping("/users/{accountId}/status")
    public ResponseEntity<AccountResponse> updateUserStatus(
            @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.updateAccountStatus(accountId, request.status()));
    }

    @GetMapping("/matches")
    public ResponseEntity<List<MatchResponse>> listMatches() {
        return ResponseEntity.ok(adminService.listAllMatches());
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchStateResponse> findMatch(@PathVariable String matchId) {
        return ResponseEntity.ok(adminService.findMatchDetail(matchId));
    }

    @GetMapping("/match-history")
    public ResponseEntity<List<MatchHistoryResponse>> listHistory() {
        return ResponseEntity.ok(adminService.listGlobalHistory());
    }

    @GetMapping("/server-status")
    public ResponseEntity<ServerStatusResponse> serverStatus() {
        return ResponseEntity.ok(adminService.serverStatus());
    }
}
