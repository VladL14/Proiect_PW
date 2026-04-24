package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MatchesMatchIdRoundsRoundIdLockPostRequest
 */

@JsonTypeName("_matches__matchId__rounds__roundId__lock_post_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public class MatchesMatchIdRoundsRoundIdLockPostRequest {

  private @Nullable String playerId;

  @Valid
  private List<Integer> lockedIndexes = new ArrayList<>();

  public MatchesMatchIdRoundsRoundIdLockPostRequest playerId(@Nullable String playerId) {
    this.playerId = playerId;
    return this;
  }

  /**
   * Get playerId
   * @return playerId
   */
  
  @Schema(name = "playerId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("playerId")
  public @Nullable String getPlayerId() {
    return playerId;
  }

  @JsonProperty("playerId")
  public void setPlayerId(@Nullable String playerId) {
    this.playerId = playerId;
  }

  public MatchesMatchIdRoundsRoundIdLockPostRequest lockedIndexes(List<Integer> lockedIndexes) {
    this.lockedIndexes = lockedIndexes;
    return this;
  }

  public MatchesMatchIdRoundsRoundIdLockPostRequest addLockedIndexesItem(Integer lockedIndexesItem) {
    if (this.lockedIndexes == null) {
      this.lockedIndexes = new ArrayList<>();
    }
    this.lockedIndexes.add(lockedIndexesItem);
    return this;
  }

  /**
   * Get lockedIndexes
   * @return lockedIndexes
   */
  
  @Schema(name = "lockedIndexes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lockedIndexes")
  public List<Integer> getLockedIndexes() {
    return lockedIndexes;
  }

  @JsonProperty("lockedIndexes")
  public void setLockedIndexes(List<Integer> lockedIndexes) {
    this.lockedIndexes = lockedIndexes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MatchesMatchIdRoundsRoundIdLockPostRequest matchesMatchIdRoundsRoundIdLockPostRequest = (MatchesMatchIdRoundsRoundIdLockPostRequest) o;
    return Objects.equals(this.playerId, matchesMatchIdRoundsRoundIdLockPostRequest.playerId) &&
        Objects.equals(this.lockedIndexes, matchesMatchIdRoundsRoundIdLockPostRequest.lockedIndexes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerId, lockedIndexes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MatchesMatchIdRoundsRoundIdLockPostRequest {\n");
    sb.append("    playerId: ").append(toIndentedString(playerId)).append("\n");
    sb.append("    lockedIndexes: ").append(toIndentedString(lockedIndexes)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

