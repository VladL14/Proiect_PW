package org.openapitools.api;

import org.openapitools.model.CreateMatchRequest;
import org.openapitools.model.Match;
import org.openapitools.model.MatchState;
import org.openapitools.model.MatchStatus;
import org.openapitools.model.MatchesMatchIdJoinPostRequest;
import org.openapitools.model.MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest;
import org.openapitools.model.MatchesMatchIdRoundsRoundIdLockPostRequest;
import org.openapitools.model.MatchesMatchIdRoundsRoundIdRollPostRequest;
import org.springframework.lang.Nullable;
import org.openapitools.model.Round;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

/**
 * A delegate to be called by the {@link MatchesApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public interface MatchesApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /matches : Get matches (optional filter)
     *
     * @param status  (optional)
     * @return Match list (status code 200)
     * @see MatchesApi#matchesGet
     */
    default ResponseEntity<List<Match>> matchesGet(MatchStatus status) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "[ { \"players\" : [ { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }, { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" } ], \"id\" : \"id\", \"status\" : \"WAITING\" }, { \"players\" : [ { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }, { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" } ], \"id\" : \"id\", \"status\" : \"WAITING\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /matches/{matchId} : Get match
     *
     * @param matchId  (required)
     * @return Match found (status code 200)
     * @see MatchesApi#matchesMatchIdGet
     */
    default ResponseEntity<Match> matchesMatchIdGet(String matchId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"players\" : [ { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }, { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" } ], \"id\" : \"id\", \"status\" : \"WAITING\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /matches/{matchId}/join : Join match
     *
     * @param matchId  (required)
     * @param matchesMatchIdJoinPostRequest  (optional)
     * @return Player joined (status code 200)
     * @see MatchesApi#matchesMatchIdJoinPost
     */
    default ResponseEntity<Void> matchesMatchIdJoinPost(String matchId,
        MatchesMatchIdJoinPostRequest matchesMatchIdJoinPostRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /matches/{matchId}/replay/export : Export replay
     *
     * @param matchId  (required)
     * @return Replay file (status code 200)
     * @see MatchesApi#matchesMatchIdReplayExportGet
     */
    default ResponseEntity<Void> matchesMatchIdReplayExportGet(String matchId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /matches/{matchId}/rounds/{roundId}/abilities/activate : Activate ability
     *
     * @param matchId  (required)
     * @param roundId  (required)
     * @param matchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest  (optional)
     * @return Ability activated (status code 200)
     * @see MatchesApi#matchesMatchIdRoundsRoundIdAbilitiesActivatePost
     */
    default ResponseEntity<Void> matchesMatchIdRoundsRoundIdAbilitiesActivatePost(String matchId,
        String roundId,
        MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest matchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /matches/{matchId}/rounds/{roundId} : Get round
     *
     * @param matchId  (required)
     * @param roundId  (required)
     * @return Round data (status code 200)
     * @see MatchesApi#matchesMatchIdRoundsRoundIdGet
     */
    default ResponseEntity<Round> matchesMatchIdRoundsRoundIdGet(String matchId,
        String roundId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"dice\" : [ \"ATTACK\", \"ATTACK\" ], \"id\" : \"id\", \"locked\" : [ true, true ], \"status\" : \"INITIALIZED\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /matches/{matchId}/rounds/{roundId}/lock : Lock dice
     *
     * @param matchId  (required)
     * @param roundId  (required)
     * @param matchesMatchIdRoundsRoundIdLockPostRequest  (optional)
     * @return Dice locked (status code 200)
     * @see MatchesApi#matchesMatchIdRoundsRoundIdLockPost
     */
    default ResponseEntity<Void> matchesMatchIdRoundsRoundIdLockPost(String matchId,
        String roundId,
        MatchesMatchIdRoundsRoundIdLockPostRequest matchesMatchIdRoundsRoundIdLockPostRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /matches/{matchId}/rounds/{roundId}/resolve : Resolve round
     *
     * @param matchId  (required)
     * @param roundId  (required)
     * @return Round resolved (status code 200)
     * @see MatchesApi#matchesMatchIdRoundsRoundIdResolvePost
     */
    default ResponseEntity<Void> matchesMatchIdRoundsRoundIdResolvePost(String matchId,
        String roundId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /matches/{matchId}/rounds/{roundId}/roll : Roll dice
     *
     * @param matchId  (required)
     * @param roundId  (required)
     * @param matchesMatchIdRoundsRoundIdRollPostRequest  (optional)
     * @return Dice rolled (status code 200)
     * @see MatchesApi#matchesMatchIdRoundsRoundIdRollPost
     */
    default ResponseEntity<Void> matchesMatchIdRoundsRoundIdRollPost(String matchId,
        String roundId,
        MatchesMatchIdRoundsRoundIdRollPostRequest matchesMatchIdRoundsRoundIdRollPostRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /matches/{matchId}/start : Start match
     *
     * @param matchId  (required)
     * @return Match started (status code 200)
     * @see MatchesApi#matchesMatchIdStartPost
     */
    default ResponseEntity<Void> matchesMatchIdStartPost(String matchId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /matches/{matchId}/state : Get match state
     *
     * @param matchId  (required)
     * @return Match state (status code 200)
     * @see MatchesApi#matchesMatchIdStateGet
     */
    default ResponseEntity<MatchState> matchesMatchIdStateGet(String matchId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"currentRound\" : 0, \"players\" : [ { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }, { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" } ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /matches : Create match
     *
     * @param createMatchRequest  (optional)
     * @return Match created (status code 201)
     * @see MatchesApi#matchesPost
     */
    default ResponseEntity<Match> matchesPost(CreateMatchRequest createMatchRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"players\" : [ { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }, { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" } ], \"id\" : \"id\", \"status\" : \"WAITING\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
