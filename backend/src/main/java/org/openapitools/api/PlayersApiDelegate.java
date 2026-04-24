package org.openapitools.api;

import org.openapitools.model.Ability;
import org.openapitools.model.CreatePlayerRequest;
import org.springframework.lang.Nullable;
import org.openapitools.model.Player;
import org.openapitools.model.PlayerStats;
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
 * A delegate to be called by the {@link PlayersApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public interface PlayersApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /players : Get all players
     *
     * @return List of players (status code 200)
     * @see PlayersApi#playersGet
     */
    default ResponseEntity<List<Player>> playersGet() {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "[ { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }, { \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /players/{playerId}/abilities : Get player abilities
     *
     * @param playerId  (required)
     * @return Player abilities (status code 200)
     * @see PlayersApi#playersPlayerIdAbilitiesGet
     */
    default ResponseEntity<List<Ability>> playersPlayerIdAbilitiesGet(String playerId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "[ { \"cost\" : 0, \"name\" : \"name\", \"id\" : \"id\" }, { \"cost\" : 0, \"name\" : \"name\", \"id\" : \"id\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /players/{playerId}/avatar : Upload avatar
     *
     * @param playerId  (required)
     * @param file  (optional)
     * @return Avatar uploaded (status code 200)
     * @see PlayersApi#playersPlayerIdAvatarPost
     */
    default ResponseEntity<Void> playersPlayerIdAvatarPost(String playerId,
        MultipartFile file) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /players/{playerId} : Get player
     *
     * @param playerId  (required)
     * @return Player found (status code 200)
     * @see PlayersApi#playersPlayerIdGet
     */
    default ResponseEntity<Player> playersPlayerIdGet(String playerId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /players/{playerId}/stats : Get player stats
     *
     * @param playerId  (required)
     * @return Player stats (status code 200)
     * @see PlayersApi#playersPlayerIdStatsGet
     */
    default ResponseEntity<PlayerStats> playersPlayerIdStatsGet(String playerId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"wins\" : 0, \"losses\" : 6 }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /players : Create player
     *
     * @param createPlayerRequest  (required)
     * @return Player created (status code 201)
     * @see PlayersApi#playersPost
     */
    default ResponseEntity<Player> playersPost(CreatePlayerRequest createPlayerRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"name\" : \"name\", \"hearts\" : 0, \"tokens\" : 6, \"id\" : \"id\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
