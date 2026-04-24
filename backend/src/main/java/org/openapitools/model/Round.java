package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.model.DiceFace;
import org.openapitools.model.RoundStatus;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Round
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-04-24T18:09:55.929218400+03:00[Europe/Bucharest]", comments = "Generator version: 7.21.0")
public class Round {

  private @Nullable String id;

  private @Nullable RoundStatus status;

  @Valid
  private List<DiceFace> dice = new ArrayList<>();

  @Valid
  private List<Boolean> locked = new ArrayList<>();

  public Round id(@Nullable String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public @Nullable String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(@Nullable String id) {
    this.id = id;
  }

  public Round status(@Nullable RoundStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable RoundStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable RoundStatus status) {
    this.status = status;
  }

  public Round dice(List<DiceFace> dice) {
    this.dice = dice;
    return this;
  }

  public Round addDiceItem(DiceFace diceItem) {
    if (this.dice == null) {
      this.dice = new ArrayList<>();
    }
    this.dice.add(diceItem);
    return this;
  }

  /**
   * Get dice
   * @return dice
   */
  @Valid 
  @Schema(name = "dice", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dice")
  public List<DiceFace> getDice() {
    return dice;
  }

  @JsonProperty("dice")
  public void setDice(List<DiceFace> dice) {
    this.dice = dice;
  }

  public Round locked(List<Boolean> locked) {
    this.locked = locked;
    return this;
  }

  public Round addLockedItem(Boolean lockedItem) {
    if (this.locked == null) {
      this.locked = new ArrayList<>();
    }
    this.locked.add(lockedItem);
    return this;
  }

  /**
   * Get locked
   * @return locked
   */
  
  @Schema(name = "locked", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("locked")
  public List<Boolean> getLocked() {
    return locked;
  }

  @JsonProperty("locked")
  public void setLocked(List<Boolean> locked) {
    this.locked = locked;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Round round = (Round) o;
    return Objects.equals(this.id, round.id) &&
        Objects.equals(this.status, round.status) &&
        Objects.equals(this.dice, round.dice) &&
        Objects.equals(this.locked, round.locked);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, dice, locked);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Round {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    dice: ").append(toIndentedString(dice)).append("\n");
    sb.append("    locked: ").append(toIndentedString(locked)).append("\n");
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

