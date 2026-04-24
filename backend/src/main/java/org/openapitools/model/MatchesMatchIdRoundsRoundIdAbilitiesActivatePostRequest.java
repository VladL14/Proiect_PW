package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest
 */

@JsonTypeName("_matches__matchId__rounds__roundId__abilities_activate_post_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public class MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest {

  private String playerId;

  private String abilityId;

  private @Nullable String targetId;

  public MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest(String playerId, String abilityId) {
    this.playerId = playerId;
    this.abilityId = abilityId;
  }

  public MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest playerId(String playerId) {
    this.playerId = playerId;
    return this;
  }

  /**
   * Get playerId
   * @return playerId
   */
  @NotNull 
  @Schema(name = "playerId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("playerId")
  public String getPlayerId() {
    return playerId;
  }

  @JsonProperty("playerId")
  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest abilityId(String abilityId) {
    this.abilityId = abilityId;
    return this;
  }

  /**
   * Get abilityId
   * @return abilityId
   */
  @NotNull 
  @Schema(name = "abilityId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("abilityId")
  public String getAbilityId() {
    return abilityId;
  }

  @JsonProperty("abilityId")
  public void setAbilityId(String abilityId) {
    this.abilityId = abilityId;
  }

  public MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest targetId(@Nullable String targetId) {
    this.targetId = targetId;
    return this;
  }

  /**
   * Get targetId
   * @return targetId
   */
  
  @Schema(name = "targetId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("targetId")
  public @Nullable String getTargetId() {
    return targetId;
  }

  @JsonProperty("targetId")
  public void setTargetId(@Nullable String targetId) {
    this.targetId = targetId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest matchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest = (MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest) o;
    return Objects.equals(this.playerId, matchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest.playerId) &&
        Objects.equals(this.abilityId, matchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest.abilityId) &&
        Objects.equals(this.targetId, matchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest.targetId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerId, abilityId, targetId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MatchesMatchIdRoundsRoundIdAbilitiesActivatePostRequest {\n");
    sb.append("    playerId: ").append(toIndentedString(playerId)).append("\n");
    sb.append("    abilityId: ").append(toIndentedString(abilityId)).append("\n");
    sb.append("    targetId: ").append(toIndentedString(targetId)).append("\n");
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

