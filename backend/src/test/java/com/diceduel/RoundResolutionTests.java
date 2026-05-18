package com.diceduel;

import com.diceduel.repository.AbilityPackRepository;
import com.diceduel.repository.MatchRepository;
import com.diceduel.repository.PlayerRepository;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoundResolutionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AbilityPackRepository abilityPackRepository;

    @BeforeEach
    void cleanDatabase() {
        matchRepository.deleteAll();
        abilityPackRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    void attackDamageUsesDefenderShields() throws Exception {
        StartedRound round = createStartedRound();

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"ATTACK\", \"ATTACK\"]", "[\"%s\", \"%s\"]".formatted(round.playerB(), round.playerB())),
                state(round.playerB(), "[\"SHIELD\"]", "[\"\"]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(2, player(result, round.playerB()).get("hearts").asInt());
        assertLogContains(result, "Player A attacked Player B. 1 attack(s) blocked, 1 damage dealt.");
    }

    @Test
    void attackDamageWithoutShieldsRemovesHearts() throws Exception {
        StartedRound round = createStartedRound();

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"ATTACK\"]", "[\"%s\"]".formatted(round.playerB())),
                state(round.playerB(), "[]", "[]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(2, player(result, round.playerB()).get("hearts").asInt());
        assertLogContains(result, "Player A attacked Player B. 0 attack(s) blocked, 1 damage dealt.");
    }

    @Test
    void attackCanBeFullyBlockedByShield() throws Exception {
        StartedRound round = createStartedRound();

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"ATTACK\"]", "[\"%s\"]".formatted(round.playerB())),
                state(round.playerB(), "[\"SHIELD\"]", "[\"\"]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(3, player(result, round.playerB()).get("hearts").asInt());
        assertLogContains(result, "Player A attacked Player B. 1 attack(s) blocked, 0 damage dealt.");
    }

    @Test
    void stealTransfersOneTokenWhenAvailable() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerC(), 3, 1);

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"STEAL\"]", "[\"%s\"]".formatted(round.playerC())),
                state(round.playerB(), "[]", "[]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(1, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(0, player(result, round.playerC()).get("tokens").asInt());
        assertLogContains(result, "Player A stole 1 token from Player C.");
    }

    @Test
    void stealDoesNothingWhenTargetHasNoTokens() throws Exception {
        StartedRound round = createStartedRound();

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"STEAL\"]", "[\"%s\"]".formatted(round.playerC())),
                state(round.playerB(), "[]", "[]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(0, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(0, player(result, round.playerC()).get("tokens").asInt());
        assertLogContains(result, "Player A tried to steal from Player C, but Player C had no tokens.");
    }

    @Test
    void multipleStealsCannotTakeMoreTokensThanAvailable() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerC(), 3, 1);

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"STEAL\", \"STEAL\"]", "[\"%s\", \"%s\"]".formatted(round.playerC(), round.playerC())),
                state(round.playerB(), "[]", "[]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(1, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(0, player(result, round.playerC()).get("tokens").asInt());
    }

    @Test
    void eliminatedPlayersCannotBeTargeted() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerC(), 0, 0);

        patchRoundStates(round, """
                [
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"ATTACK\"]", "[\"\"]"),
                state(round.playerB(), "[]", "[]")
        ));

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/target", round.matchId(), round.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerId": "%s",
                                  "diceTargets": { "0": "%s" }
                                }
                                """.formatted(round.playerA(), round.playerC())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resolveFailsWhenAttackOrStealTargetsAreMissing() throws Exception {
        StartedRound round = createStartedRound();

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"ATTACK\", \"STEAL\"]", "[\"\", \"\"]"),
                state(round.playerB(), "[]", "[]"),
                state(round.playerC(), "[]", "[]")
        ));

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/resolve", round.matchId(), round.roundId()))
                .andExpect(status().isBadRequest());
    }

    private StartedRound createStartedRound() throws Exception {
        String playerA = createPlayer("Player A");
        String playerB = createPlayer("Player B");
        String playerC = createPlayer("Player C");
        String matchId = createMatch(playerA);

        joinMatch(matchId, playerB);
        joinMatch(matchId, playerC);
        mockMvc.perform(post("/api/matches/{matchId}/start", matchId))
                .andExpect(status().isOk());

        MvcResult roundsResult = mockMvc.perform(get("/api/matches/{matchId}/rounds", matchId))
                .andExpect(status().isOk())
                .andReturn();

        return new StartedRound(matchId, read(roundsResult).get(0).get("id").asText(), playerA, playerB, playerC);
    }

    private String createPlayer(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("id").asText();
    }

    private String createMatch(String hostId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostPlayerId": "%s",
                                  "maxPlayers": 3
                                }
                                """.formatted(hostId)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("id").asText();
    }

    private void joinMatch(String matchId, String playerId) throws Exception {
        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(playerId)))
                .andExpect(status().isOk());
    }

    private void patchPlayer(String playerId, int hearts, int tokens) throws Exception {
        mockMvc.perform(patch("/api/players/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hearts\":%d,\"tokens\":%d}".formatted(hearts, tokens)))
                .andExpect(status().isOk());
    }

    private void patchRoundStates(StartedRound round, String playerStates) throws Exception {
        mockMvc.perform(patch("/api/matches/{matchId}/rounds/{roundId}", round.matchId(), round.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerStates\":%s}".formatted(playerStates)))
                .andExpect(status().isOk());
    }

    private JsonNode resolve(StartedRound round) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/resolve", round.matchId(), round.roundId()))
                .andExpect(status().isOk())
                .andReturn();
        return read(result);
    }

    private String state(String playerId, String dice, String targets) {
        return """
                {
                  "playerId": "%s",
                  "dice": %s,
                  "locked": [false, false, false, false, false],
                  "targetPlayerIds": %s
                }
                """.formatted(playerId, dice, targets);
    }

    private JsonNode player(JsonNode result, String playerId) {
        for (JsonNode player : result.get("players")) {
            if (player.get("id").asText().equals(playerId)) {
                return player;
            }
        }
        throw new AssertionError("Player not found: " + playerId);
    }

    private void assertLogContains(JsonNode result, String expected) {
        for (JsonNode log : result.get("actionLogs")) {
            if (log.asText().equals(expected)) {
                return;
            }
        }
        assertTrue(false, "Expected log not found: " + expected + " in " + result.get("actionLogs"));
    }

    private JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record StartedRound(String matchId, String roundId, String playerA, String playerB, String playerC) {
    }
}
