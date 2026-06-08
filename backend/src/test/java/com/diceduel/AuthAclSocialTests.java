package com.diceduel;

import com.diceduel.entity.AccountStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.Role;
import com.diceduel.repository.MatchHistoryRepository;
import com.diceduel.repository.MatchRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.security.PasswordHasher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stage 4 tests: authentication, ACL enforcement, emote anti-spam,
 * match-history ownership and structured replay export.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthAclSocialTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchHistoryRepository matchHistoryRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @BeforeEach
    void cleanDatabase() {
        matchHistoryRepository.deleteAll();
        matchRepository.deleteAll();
        playerRepository.deleteAll();
    }

    // ----------------------------------------------------------------- auth ---

    @Test
    void registerLoginMeAndLogoutFlowWorks() throws Exception {
        String username = "alice-" + suffix();
        String token = register(username, username + "@diceduel.test", "secret123", "Alice");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"));

        // /me requires authentication.
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        // Login with the same credentials returns a fresh token.
        String loginToken = login(username, "secret123");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk());

        // After logout the token is revoked.
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateUsernameIsRejectedWithConflict() throws Exception {
        String username = "bob-" + suffix();
        register(username, username + "@diceduel.test", "secret123", "Bob");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"other-" + suffix()
                                + "@diceduel.test\",\"password\":\"secret123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        String username = "carol-" + suffix();
        register(username, username + "@diceduel.test", "secret123", "Carol");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\":\"" + username + "\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ acl ---

    @Test
    void adminEndpointsEnforceRoleBasedAccess() throws Exception {
        // Anonymous -> 401.
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        // Regular user -> 403.
        String username = "dan-" + suffix();
        String userToken = register(username, username + "@diceduel.test", "secret123", "Dan");
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Admin -> 200.
        String adminToken = seedAdminAndLogin();
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/server-status").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminCount").value(1));
    }

    @Test
    void playerHistoryIsOwnerOrAdminOnly() throws Exception {
        String username = "erin-" + suffix();
        String token = register(username, username + "@diceduel.test", "secret123", "Erin");
        String ownId = accountId(token);

        // Owner can read own (empty) history.
        mockMvc.perform(get("/api/players/{id}/history", ownId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Reading someone else's history is forbidden for a regular user.
        mockMvc.perform(get("/api/players/{id}/history", "someone-else")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void suspendedAccountCannotCreateMatch() throws Exception {
        String username = "suspended-host-" + suffix();
        String token = register(username, username + "@diceduel.test", "secret123", "Suspended Host");
        String playerId = accountId(token);
        suspendAccount(playerId);

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hostPlayerId\":\"" + playerId + "\",\"maxPlayers\":2}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Suspended accounts cannot create or join matches"));
    }

    @Test
    void suspendedAccountCannotJoinMatch() throws Exception {
        String hostId = createPlayer("Active Host");
        String matchId = createMatch(hostId);
        String username = "suspended-guest-" + suffix();
        String token = register(username, username + "@diceduel.test", "secret123", "Suspended Guest");
        String suspendedPlayerId = accountId(token);
        suspendAccount(suspendedPlayerId);

        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"" + suspendedPlayerId + "\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Suspended accounts cannot create or join matches"));
    }

    // --------------------------------------------------------------- emotes ---

    @Test
    void emoteRequiresMembershipWhitelistAndRespectsCooldown() throws Exception {
        String hostId = createPlayer("Host");
        String guestId = createPlayer("Guest");
        String outsiderId = createPlayer("Outsider");
        String matchId = createMatch(hostId);
        joinMatch(matchId, guestId);

        // Valid emote is accepted.
        mockMvc.perform(sendEmote(matchId, hostId, "GG"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emote").value("GG"))
                .andExpect(jsonPath("$.playerName").value("Host"));

        // Immediate second emote from the same player hits the cooldown.
        mockMvc.perform(sendEmote(matchId, hostId, "WAVE"))
                .andExpect(status().isTooManyRequests());

        // Unknown emote code is rejected.
        mockMvc.perform(sendEmote(matchId, guestId, "NOT_A_REAL_EMOTE"))
                .andExpect(status().isBadRequest());

        // A player that is not part of the match cannot emote into it.
        mockMvc.perform(sendEmote(matchId, outsiderId, "GG"))
                .andExpect(status().isBadRequest());

        // Recent emotes can be polled back.
        mockMvc.perform(get("/api/matches/{matchId}/emotes", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].emote").value("GG"));
    }

    // -------------------------------------------------------------- replay ----

    @Test
    void replayJsonContainsParticipantsAndInitialState() throws Exception {
        String hostId = createPlayer("ReplayHost");
        String guestId = createPlayer("ReplayGuest");
        String matchId = createMatch(hostId);
        joinMatch(matchId, guestId);

        mockMvc.perform(get("/api/matches/{matchId}/replay", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(matchId))
                .andExpect(jsonPath("$.participants", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.initialState", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.initialState[0].hearts").value(3));
    }

    // ---------------------------------------------------------------- helpers --

    private String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String register(String username, String email, String password, String displayName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"" + email
                                + "\",\"password\":\"" + password + "\",\"displayName\":\"" + displayName + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String login(String identifier, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\":\"" + identifier + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String accountId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void suspendAccount(String playerId) {
        PlayerEntity player = playerRepository.findById(playerId).orElseThrow();
        player.setAccountStatus(AccountStatus.SUSPENDED);
        playerRepository.save(player);
    }

    private String seedAdminAndLogin() throws Exception {
        String username = "admin-" + suffix();
        PlayerEntity admin = new PlayerEntity(UUID.randomUUID().toString(), "Admin", 3, 0, 0, 0);
        admin.setUsername(username);
        admin.setEmail(username + "@diceduel.test");
        admin.setPasswordHash(passwordHasher.hash("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setMatchesPlayed(0);
        playerRepository.save(admin);
        return login(username, "admin123");
    }

    private String createPlayer(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createMatch(String hostId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hostPlayerId\":\"" + hostId + "\",\"maxPlayers\":2}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void joinMatch(String matchId, String playerId) throws Exception {
        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"" + playerId + "\"}"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder sendEmote(
            String matchId, String playerId, String emote) {
        return post("/api/matches/{matchId}/emotes", matchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerId\":\"" + playerId + "\",\"emote\":\"" + emote + "\"}");
    }
}
