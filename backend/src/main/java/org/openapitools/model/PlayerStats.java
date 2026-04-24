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
 * PlayerStats
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public class PlayerStats {

  private @Nullable Integer wins;

  private @Nullable Integer losses;

  public PlayerStats wins(@Nullable Integer wins) {
    this.wins = wins;
    return this;
  }

  /**
   * Get wins
   * @return wins
   */
  
  @Schema(name = "wins", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("wins")
  public @Nullable Integer getWins() {
    return wins;
  }

  @JsonProperty("wins")
  public void setWins(@Nullable Integer wins) {
    this.wins = wins;
  }

  public PlayerStats losses(@Nullable Integer losses) {
    this.losses = losses;
    return this;
  }

  /**
   * Get losses
   * @return losses
   */
  
  @Schema(name = "losses", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("losses")
  public @Nullable Integer getLosses() {
    return losses;
  }

  @JsonProperty("losses")
  public void setLosses(@Nullable Integer losses) {
    this.losses = losses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlayerStats playerStats = (PlayerStats) o;
    return Objects.equals(this.wins, playerStats.wins) &&
        Objects.equals(this.losses, playerStats.losses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wins, losses);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlayerStats {\n");
    sb.append("    wins: ").append(toIndentedString(wins)).append("\n");
    sb.append("    losses: ").append(toIndentedString(losses)).append("\n");
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

