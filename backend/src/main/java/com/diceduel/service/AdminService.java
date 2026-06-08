package com.diceduel.service;

import com.diceduel.dto.AccountResponse;
import com.diceduel.dto.MatchHistoryResponse;
import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.ServerStatusResponse;
import com.diceduel.entity.AccountStatus;

import java.util.List;

/**
 * Administrative read/maintenance operations. Every method is intended to be
 * reachable only behind the ADMIN role; the controller enforces this with the
 * {@code @RequireRole(ADMIN)} ACL guard.
 */
public interface AdminService {

    List<AccountResponse> listAccounts();

    AccountResponse findAccount(String accountId);

    AccountResponse updateAccountStatus(String accountId, AccountStatus status);

    List<MatchResponse> listAllMatches();

    MatchStateResponse findMatchDetail(String matchId);

    List<MatchHistoryResponse> listGlobalHistory();

    ServerStatusResponse serverStatus();
}
