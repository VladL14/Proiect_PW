package com.diceduel;

import com.diceduel.entity.AbilityEntity;
import com.diceduel.repository.AbilityPackRepository;
import com.diceduel.repository.AbilityRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowIntegrationTests {

    private static final String TEST_PREFIX = "presentation-test-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AbilityRepository abilityRepository;

    @Autowired
    private AbilityPackRepository abilityPackRepository;

    @BeforeEach
    void cleanDatabase() {
        matchRepository.deleteAll();
        abilityPackRepository.deleteAll();
        playerRepository.deleteAll();
        abilityRepository.findAll()
                .stream()
                .filter(ability -> ability.getId().startsWith(TEST_PREFIX))
                .forEach(abilityRepository::delete);
    }

    @Test
    void playerResourceSupportsFullCrudFlowAndValidation() throws Exception {
        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/players/missing-player"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        String playerId = createPlayer("Presentation Player");

        mockMvc.perform(get("/api/players/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playerId))
                .andExpect(jsonPath("$.name").value("Presentation Player"));

        mockMvc.perform(put("/api/players/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Incomplete\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/players/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Presentation Player Full\",\"hearts\":12,\"tokens\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Presentation Player Full"))
                .andExpect(jsonPath("$.hearts").value(12))
                .andExpect(jsonPath("$.tokens").value(3));

        mockMvc.perform(patch("/api/players/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hearts\":-1}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/players/{playerId}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tokens\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens").value(7));

        mockMvc.perform(get("/api/players/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens").value(7));

        mockMvc.perform(get("/api/players/{playerId}/stats", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wins").value(0))
                .andExpect(jsonPath("$.losses").value(0));

        mockMvc.perform(delete("/api/players/{playerId}", playerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/players/{playerId}", playerId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/players/{playerId}", playerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void abilityResourceSupportsFullCrudFlowAndValidation() throws Exception {
        mockMvc.perform(get("/api/abilities"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/abilities/missing-ability"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/abilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Invalid Ability\"}"))
                .andExpect(status().isBadRequest());

        String abilityId = TEST_PREFIX + UUID.randomUUID();
        mockMvc.perform(post("/api/abilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "%s",
                                  "name": "Presentation Strike",
                                  "cost": 2
                                }
                                """.formatted(abilityId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(abilityId));

        mockMvc.perform(post("/api/abilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "%s",
                                  "name": "Duplicate Strike",
                                  "cost": 2
                                }
                                """.formatted(abilityId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(get("/api/abilities/{abilityId}", abilityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Presentation Strike"));

        mockMvc.perform(put("/api/abilities/{abilityId}", abilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Incomplete Ability\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/abilities/{abilityId}", abilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Presentation Strike Full\",\"cost\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Presentation Strike Full"))
                .andExpect(jsonPath("$.cost").value(4));

        mockMvc.perform(patch("/api/abilities/{abilityId}", abilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cost\":-1}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/abilities/{abilityId}", abilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cost\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cost").value(1));

        mockMvc.perform(delete("/api/abilities/{abilityId}", abilityId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/abilities/{abilityId}", abilityId))
                .andExpect(status().isNotFound());
    }

    @Test
    void matchAndRoundResourcesSupportDatabaseBackedFlow() throws Exception {
        String hostId = createPlayer("Host Player");
        String guestId = createPlayer("Guest Player");

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hostPlayerId\":\"missing-player\",\"maxPlayers\":2}"))
                .andExpect(status().isNotFound());

        String matchId = createMatch(hostId, 2);

        mockMvc.perform(get("/api/matches/{matchId}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        mockMvc.perform(get("/api/matches?status=WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(matchId));

        mockMvc.perform(put("/api/matches/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/matches/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxPlayers\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"));

        mockMvc.perform(patch("/api/matches/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxPlayers\":1}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(guestId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches/{matchId}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.players", hasSize(2)));

        mockMvc.perform(post("/api/matches/{matchId}/start", matchId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches/{matchId}/state", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRound").value(1));

        MvcResult roundsResult = mockMvc.perform(get("/api/matches/{matchId}/rounds", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
        String roundId = read(roundsResult).get(0).get("id").asText();

        mockMvc.perform(get("/api/matches/{matchId}/rounds/{roundId}", matchId, roundId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roundId))
                .andExpect(jsonPath("$.status").value("INITIALIZED"));

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/roll", matchId, roundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(hostId)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/matches/{matchId}/rounds/{roundId}", matchId, roundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/matches/{matchId}/rounds/{roundId}", matchId, roundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ROLLING",
                                  "dice": ["ATTACK", "SHIELD", "STEAL", "ATTACK", "SHIELD"],
                                  "locked": [false, false, false, false, false]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dice", hasSize(5)));

        mockMvc.perform(patch("/api/matches/{matchId}/rounds/{roundId}", matchId, roundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locked\":[true]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/matches/{matchId}/rounds/{roundId}", matchId, roundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ABILITY_PHASE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABILITY_PHASE"));

        mockMvc.perform(get("/api/matches/{matchId}/replay/export", matchId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("replay.txt")))
                .andExpect(content().string(containsString(matchId)));

        mockMvc.perform(delete("/api/matches/{matchId}/rounds/{roundId}", matchId, roundId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/matches/{matchId}/rounds/{roundId}", matchId, roundId))
                .andExpect(status().isNotFound());
    }

    @Test
    void matchLobbyStatusRemovalAndDeleteEndpointsAreCovered() throws Exception {
        String hostId = createPlayer("Lobby Host");
        String guestId = createPlayer("Lobby Guest");
        String matchId = createMatch(hostId, 3);

        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(hostId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(guestId)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/matches/{matchId}/players/{playerId}", matchId, "missing-player"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/matches/{matchId}/players/{playerId}", matchId, guestId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/matches/{matchId}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players", hasSize(1)));

        mockMvc.perform(patch("/api/matches/{matchId}/status", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/matches/{matchId}/status", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"READY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        mockMvc.perform(post("/api/matches/{matchId}/start", matchId))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/matches/{matchId}", matchId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/matches/{matchId}", matchId))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/matches/{matchId}", matchId))
                .andExpect(status().isNotFound());
    }

    @Test
    void roundActionEndpointsCoverRollLockLockedDiceResolveAndValidation() throws Exception {
        StartedRound startedRound = createStartedRound();

        mockMvc.perform(get("/api/matches/{matchId}/rounds/missing-round", startedRound.matchId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/roll", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"missing-player\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/roll", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(startedRound.hostId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/lock", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\",\"lockedIndexes\":[0,2]}".formatted(startedRound.hostId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches/{matchId}/rounds/{roundId}", startedRound.matchId(), startedRound.roundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TARGET_SELECTION"))
                .andExpect(jsonPath("$.locked[0]").value(true))
                .andExpect(jsonPath("$.locked[2]").value(true));

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/lock", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\",\"lockedIndexes\":[9]}".formatted(startedRound.hostId())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/matches/{matchId}/rounds/{roundId}/locked-dice", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locked\":[true,false,true,false,true]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked[4]").value(true));

        mockMvc.perform(patch("/api/matches/{matchId}/rounds/{roundId}/locked-dice", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locked\":[true]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/matches/{matchId}/rounds/{roundId}", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerStates": [
                                    {
                                      "playerId": "%s",
                                      "dice": ["SHIELD", "SHIELD", "SHIELD", "SHIELD", "SHIELD"],
                                      "locked": [true, true, true, true, true],
                                      "targetPlayerIds": ["", "", "", "", ""]
                                    }
                                  ]
                                }
                                """.formatted(startedRound.hostId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/resolve", startedRound.matchId(), startedRound.roundId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches/{matchId}/rounds", startedRound.matchId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/resolve", startedRound.matchId(), startedRound.roundId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fileAndAbilityPackResourceSupportsDatabaseBackedFlow() throws Exception {
        String playerId = createPlayer("Avatar Player");

        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/players/{playerId}/avatar", playerId).file(avatar))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/api/players/{playerId}/avatar", playerId)
                        .file(avatar)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/players/{playerId}/avatar", playerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ability-packs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        MockMultipartFile emptyPack = new MockMultipartFile(
                "file",
                "empty.csv",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );
        mockMvc.perform(multipart("/api/ability-packs/import").file(emptyPack))
                .andExpect(status().isBadRequest());

        String abilityId = TEST_PREFIX + UUID.randomUUID();
        MockMultipartFile pack = abilityPackFile("pack.csv", abilityId + ",Pack Ability,1\n");
        MvcResult importResult = mockMvc.perform(multipart("/api/ability-packs/import").file(pack))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("pack.csv"))
                .andReturn();
        String packId = read(importResult).get("id").asText();

        mockMvc.perform(get("/api/ability-packs/{packId}", packId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(packId));

        mockMvc.perform(get("/api/abilities/{abilityId}", abilityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pack Ability"));

        MockMultipartFile replacement = abilityPackFile("replacement.csv", abilityId + ",Pack Ability Replacement,2\n");
        mockMvc.perform(multipart("/api/ability-packs/{packId}", packId)
                        .file(replacement)
                        .param("name", "Replacement Pack")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Replacement Pack"));

        mockMvc.perform(patch("/api/ability-packs/{packId}", packId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/ability-packs/{packId}", packId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Demo metadata\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Demo metadata"));

        mockMvc.perform(delete("/api/ability-packs/{packId}", packId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ability-packs/{packId}", packId))
                .andExpect(status().isNotFound());

        abilityRepository.findById(abilityId).ifPresent(abilityRepository::delete);
    }

    @Test
    void playerAbilitiesUseNestedResourceTransfer() throws Exception {
        String playerId = createPlayer("Ability Owner");
        String abilityId = TEST_PREFIX + UUID.randomUUID();
        abilityRepository.save(new AbilityEntity(abilityId, "Nested Ability", 0));

        mockMvc.perform(get("/api/players/missing-player/abilities"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/players/{playerId}/abilities", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/players/{playerId}/abilities", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"abilityId\":\"%s\"}".formatted(abilityId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/players/{playerId}/abilities", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(abilityId)).exists());

        mockMvc.perform(delete("/api/players/{playerId}/abilities/{abilityId}", playerId, abilityId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/players/{playerId}/abilities", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"abilityId\":\"missing-ability\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void abilityActivationEndpointCoversSuccessAndValidation() throws Exception {
        StartedRound startedRound = createStartedRound();
        String freeAbilityId = TEST_PREFIX + UUID.randomUUID();
        String costlyAbilityId = TEST_PREFIX + UUID.randomUUID();
        abilityRepository.save(new AbilityEntity(freeAbilityId, "Free Activation", 0));
        abilityRepository.save(new AbilityEntity(costlyAbilityId, "Costly Activation", 99));

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/abilities/activate", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/abilities/activate", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerId": "%s",
                                  "abilityId": "missing-ability"
                                }
                                """.formatted(startedRound.hostId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/abilities/activate", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerId": "%s",
                                  "abilityId": "%s"
                                }
                                """.formatted(startedRound.hostId(), costlyAbilityId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/matches/{matchId}/rounds/{roundId}/abilities/activate", startedRound.matchId(), startedRound.roundId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerId": "%s",
                                  "abilityId": "%s",
                                  "targetId": "%s"
                                }
                                """.formatted(startedRound.hostId(), freeAbilityId, startedRound.guestId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches/{matchId}/rounds/{roundId}", startedRound.matchId(), startedRound.roundId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABILITY_PHASE"));
    }

    @Test
    void fileEndpointsCoverInvalidUploadsMissingResourcesAndGetAllAfterImport() throws Exception {
        String playerId = createPlayer("File Validation Player");
        MockMultipartFile textAvatar = new MockMultipartFile(
                "file",
                "avatar.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not-an-image".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/players/{playerId}/avatar", playerId).file(textAvatar))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/players/missing-player/avatar"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/ability-packs/missing-pack"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/ability-packs/missing-pack"))
                .andExpect(status().isNotFound());

        String firstAbilityId = TEST_PREFIX + UUID.randomUUID();
        String secondAbilityId = TEST_PREFIX + UUID.randomUUID();
        MvcResult firstPack = mockMvc.perform(multipart("/api/ability-packs/import")
                        .file(abilityPackFile("first-pack.csv", firstAbilityId + ",First Pack Ability,1\n")))
                .andExpect(status().isCreated())
                .andReturn();
        mockMvc.perform(multipart("/api/ability-packs/import")
                        .file(abilityPackFile("second-pack.csv", secondAbilityId + ",Second Pack Ability,1\n")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/ability-packs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        String firstPackId = read(firstPack).get("id").asText();
        mockMvc.perform(multipart("/api/ability-packs/missing-pack")
                        .file(abilityPackFile("replacement.csv", firstAbilityId + ",Replacement,1\n"))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/ability-packs/missing-pack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Missing\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/ability-packs/{packId}", firstPackId))
                .andExpect(status().isNoContent());
    }

    private String createPlayer(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("id").asText();
    }

    private String createMatch(String hostPlayerId, int maxPlayers) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostPlayerId": "%s",
                                  "maxPlayers": %d
                                }
                                """.formatted(hostPlayerId, maxPlayers)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("id").asText();
    }

    private StartedRound createStartedRound() throws Exception {
        String hostId = createPlayer("Started Host " + UUID.randomUUID());
        String guestId = createPlayer("Started Guest " + UUID.randomUUID());
        String matchId = createMatch(hostId, 2);

        mockMvc.perform(post("/api/matches/{matchId}/join", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"%s\"}".formatted(guestId)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/matches/{matchId}/start", matchId))
                .andExpect(status().isOk());

        MvcResult roundsResult = mockMvc.perform(get("/api/matches/{matchId}/rounds", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();

        return new StartedRound(matchId, read(roundsResult).get(0).get("id").asText(), hostId, guestId);
    }

    private MockMultipartFile abilityPackFile(String fileName, String content) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record StartedRound(String matchId, String roundId, String hostId, String guestId) {
    }
}
