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
                state(round.playerB(), "[\"SHIELD\"]", "[true, false, false, false, false]", "[\"\"]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(2, player(result, round.playerB()).get("hearts").asInt());
        assertLogContains(result, "Player A attacked Player B. 1 attack(s) blocked, 1 damage dealt.");
        assertEquals(0, roundPlayerState(result, round.playerB()).get("shieldCount").asInt());
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
                state(round.playerB(), "[\"SHIELD\"]", "[true, false, false, false, false]", "[\"\"]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(3, player(result, round.playerB()).get("hearts").asInt());
        assertLogContains(result, "Player A attacked Player B. 1 attack(s) blocked, 0 damage dealt.");
        assertEquals(0, roundPlayerState(result, round.playerB()).get("shieldCount").asInt());
    }

    @Test
    void unlockedShieldDoesNotBlockAttack() throws Exception {
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

        assertEquals(2, player(result, round.playerB()).get("hearts").asInt());
        assertLogContains(result, "Player A attacked Player B. 0 attack(s) blocked, 1 damage dealt.");
    }

    @Test
    void unusedLockedShieldCarriesToNextRound() throws Exception {
        StartedRound round = createStartedRound();

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"SHIELD\"]", "[true, false, false, false, false]", "[\"\"]"),
                state(round.playerB(), "[]", "[]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals(1, roundPlayerState(result, round.playerA()).get("shieldCount").asInt());
        assertEquals("SHIELD", roundPlayerState(result, round.playerA()).get("dice").get(0).asText());
        assertEquals(true, roundPlayerState(result, round.playerA()).get("locked").get(0).asBoolean());
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

        assertEquals(4, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(0, player(result, round.playerC()).get("tokens").asInt());
        assertLogContains(result, "Player A stole 1 token from Player C.");
    }

    @Test
    void stealDoesNothingWhenTargetHasNoTokens() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerC(), 3, 0);

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

        assertEquals(3, player(result, round.playerA()).get("tokens").asInt());
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

        assertEquals(4, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(0, player(result, round.playerC()).get("tokens").asInt());
    }

    @Test
    void finishedMatchKeepsFinalHeartsAndTokensUntilPlayerLeaves() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerA(), 2, 1);
        patchPlayer(round.playerB(), 1, 0);
        patchPlayer(round.playerC(), 1, 2);

        patchRoundStates(round, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[\"ATTACK\", \"ATTACK\"]", "[\"%s\", \"%s\"]".formatted(round.playerB(), round.playerC())),
                state(round.playerB(), "[]", "[]"),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(round);

        assertEquals("FINISHED", result.get("matchStatus").asText());
        assertEquals(round.playerA(), result.get("winnerPlayerId").asText());
        assertEquals(2, player(result, round.playerA()).get("hearts").asInt());
        assertEquals(1, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(0, player(result, round.playerB()).get("hearts").asInt());
        assertEquals(0, player(result, round.playerB()).get("tokens").asInt());
        assertEquals(0, player(result, round.playerC()).get("hearts").asInt());
        assertEquals(2, player(result, round.playerC()).get("tokens").asInt());

        leaveMatch(round.matchId(), round.playerB());

        MvcResult playerResult = mockMvc.perform(get("/api/players/{playerId}", round.playerB()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode resetPlayer = read(playerResult);
        assertEquals(3, resetPlayer.get("hearts").asInt());
        assertEquals(3, resetPlayer.get("tokens").asInt());
    }

    @Test
    void matchStateIncludesBattleLogFromAllResolvedRounds() throws Exception {
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
        resolve(round);

        String secondRoundId = latestRoundId(round.matchId());
        StartedRound secondRound = new StartedRound(round.matchId(), secondRoundId, round.playerA(), round.playerB(), round.playerC());
        patchRoundStates(secondRound, """
                [
                  %s,
                  %s,
                  %s
                ]
                """.formatted(
                state(round.playerA(), "[]", "[]"),
                state(round.playerB(), "[\"ATTACK\"]", "[\"%s\"]".formatted(round.playerA())),
                state(round.playerC(), "[]", "[]")
        ));

        JsonNode result = resolve(secondRound);

        assertEquals(2, result.get("actionLogs").size());
        assertEquals("Player A attacked Player B. 0 attack(s) blocked, 1 damage dealt.", result.get("actionLogs").get(0).asText());
        assertEquals("Player B attacked Player A. 0 attack(s) blocked, 1 damage dealt.", result.get("actionLogs").get(1).asText());
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
    void attackAndStealWithoutTargetsResolveWithoutCombat() throws Exception {
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

        JsonNode result = resolve(round);

        assertEquals(3, player(result, round.playerA()).get("hearts").asInt());
        assertEquals(3, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(3, player(result, round.playerB()).get("hearts").asInt());
        assertEquals(3, player(result, round.playerB()).get("tokens").asInt());
        assertEquals(3, player(result, round.playerC()).get("hearts").asInt());
        assertEquals(3, player(result, round.playerC()).get("tokens").asInt());
        assertEquals(0, result.get("actionLogs").size());
    }

    @Test
    void powerStrikeAbilityDealsDamageAndCostsTokens() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerA(), 3, 3);
        patchPlayer(round.playerB(), 3, 3);

        activateAbility(round, round.playerA(), "power-strike", round.playerB());

        JsonNode result = matchState(round.matchId());
        assertEquals(1, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(2, player(result, round.playerB()).get("hearts").asInt());
        assertLogContains(result, "Player A used Power Strike on Player B. 1 damage dealt.");
    }

    @Test
    void shieldWallAbilityRestoresOneHeartAndCostsTokens() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerA(), 2, 3);

        activateAbility(round, round.playerA(), "shield-wall", null);

        JsonNode result = matchState(round.matchId());
        assertEquals(2, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(3, player(result, round.playerA()).get("hearts").asInt());
        assertLogContains(result, "Player A used Shield Wall and restored 1 HP.");
    }

    @Test
    void tokenStealAbilityTransfersOneTokenAfterCost() throws Exception {
        StartedRound round = createStartedRound();
        patchPlayer(round.playerA(), 3, 3);
        patchPlayer(round.playerB(), 3, 2);

        activateAbility(round, round.playerA(), "token-steal", round.playerB());

        JsonNode result = matchState(round.matchId());
        assertEquals(1, player(result, round.playerA()).get("tokens").asInt());
        assertEquals(1, player(result, round.playerB()).get("tokens").asInt());
        assertLogContains(result, "Player A used Token Steal and stole 1 token from Player B.");
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

    private void leaveMatch(String matchId, String playerId) throws Exception {
        mockMvc.perform(post("/api/matches/{matchId}/leave", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(playerId)))
                .andExpect(status().isNoContent());
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

    private void activateAbility(StartedRound round, String playerId, String abilityId, String targetId) throws Exception {
        String targetJson = targetId == null ? "" : ",\"targetId\":\"%s\"".formatted(targetId);
        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/abilities/activate", round.matchId(), round.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\",\"abilityId\":\"%s\"%s}".formatted(playerId, abilityId, targetJson)))
                .andExpect(status().isOk());
    }

    private JsonNode matchState(String matchId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/matches/{matchId}/state", matchId))
                .andExpect(status().isOk())
                .andReturn();
        return read(result);
    }

    private String state(String playerId, String dice, String targets) {
        return state(playerId, dice, "[false, false, false, false, false]", targets);
    }

    private String state(String playerId, String dice, String locked, String targets) {
        return """
                {
                  "playerId": "%s",
                  "dice": %s,
                  "locked": %s,
                  "targetPlayerIds": %s
                }
                """.formatted(playerId, dice, locked, targets);
    }

    private JsonNode player(JsonNode result, String playerId) {
        for (JsonNode player : result.get("players")) {
            if (player.get("id").asText().equals(playerId)) {
                return player;
            }
        }
        throw new AssertionError("Player not found: " + playerId);
    }

    private JsonNode roundPlayerState(JsonNode result, String playerId) {
        for (JsonNode playerState : result.get("currentRoundState").get("playerStates")) {
            if (playerState.get("playerId").asText().equals(playerId)) {
                return playerState;
            }
        }
        throw new AssertionError("Round player state not found: " + playerId);
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

    private String latestRoundId(String matchId) throws Exception {
        MvcResult roundsResult = mockMvc.perform(get("/api/matches/{matchId}/rounds", matchId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rounds = read(roundsResult);
        return rounds.get(rounds.size() - 1).get("id").asText();
    }

    private record StartedRound(String matchId, String roundId, String playerA, String playerB, String playerC) {
    }
}
