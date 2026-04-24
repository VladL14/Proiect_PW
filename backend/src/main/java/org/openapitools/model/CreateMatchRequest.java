package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateMatchRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public class CreateMatchRequest {

  private @Nullable String hostPlayerId;

  private @Nullable Integer maxPlayers;

  public CreateMatchRequest hostPlayerId(@Nullable String hostPlayerId) {
    this.hostPlayerId = hostPlayerId;
    return this;
  }

  /**
   * Get hostPlayerId
   * @return hostPlayerId
   */
  
  @Schema(name = "hostPlayerId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hostPlayerId")
  public @Nullable String getHostPlayerId() {
    return hostPlayerId;
  }

  @JsonProperty("hostPlayerId")
  public void setHostPlayerId(@Nullable String hostPlayerId) {
    this.hostPlayerId = hostPlayerId;
  }

  public CreateMatchRequest maxPlayers(@Nullable Integer maxPlayers) {
    this.maxPlayers = maxPlayers;
    return this;
  }

  /**
   * Get maxPlayers
   * @return maxPlayers
   */
  
  @Schema(name = "maxPlayers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxPlayers")
  public @Nullable Integer getMaxPlayers() {
    return maxPlayers;
  }

  @JsonProperty("maxPlayers")
  public void setMaxPlayers(@Nullable Integer maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateMatchRequest createMatchRequest = (CreateMatchRequest) o;
    return Objects.equals(this.hostPlayerId, createMatchRequest.hostPlayerId) &&
        Objects.equals(this.maxPlayers, createMatchRequest.maxPlayers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hostPlayerId, maxPlayers);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateMatchRequest {\n");
    sb.append("    hostPlayerId: ").append(toIndentedString(hostPlayerId)).append("\n");
    sb.append("    maxPlayers: ").append(toIndentedString(maxPlayers)).append("\n");
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

