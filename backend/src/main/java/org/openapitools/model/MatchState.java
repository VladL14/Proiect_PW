package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.model.Player;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MatchState
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public class MatchState {

  private @Nullable Integer currentRound;

  @Valid
  private List<@Valid Player> players = new ArrayList<>();

  public MatchState currentRound(@Nullable Integer currentRound) {
    this.currentRound = currentRound;
    return this;
  }

  /**
   * Get currentRound
   * @return currentRound
   */
  
  @Schema(name = "currentRound", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("currentRound")
  public @Nullable Integer getCurrentRound() {
    return currentRound;
  }

  @JsonProperty("currentRound")
  public void setCurrentRound(@Nullable Integer currentRound) {
    this.currentRound = currentRound;
  }

  public MatchState players(List<@Valid Player> players) {
    this.players = players;
    return this;
  }

  public MatchState addPlayersItem(Player playersItem) {
    if (this.players == null) {
      this.players = new ArrayList<>();
    }
    this.players.add(playersItem);
    return this;
  }

  /**
   * Get players
   * @return players
   */
  @Valid 
  @Schema(name = "players", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("players")
  public List<@Valid Player> getPlayers() {
    return players;
  }

  @JsonProperty("players")
  public void setPlayers(List<@Valid Player> players) {
    this.players = players;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MatchState matchState = (MatchState) o;
    return Objects.equals(this.currentRound, matchState.currentRound) &&
        Objects.equals(this.players, matchState.players);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currentRound, players);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MatchState {\n");
    sb.append("    currentRound: ").append(toIndentedString(currentRound)).append("\n");
    sb.append("    players: ").append(toIndentedString(players)).append("\n");
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

