package com.reversi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Captures the state of a player inside a {@link LobbyRoom},
 * including whether they are ready and what role they have.
 */
public class PlayerStatus {
  private final Integer id;
  private boolean readiness;
  private Player role;

  @JsonCreator
  public PlayerStatus(@JsonProperty("id") Integer id,
                      @JsonProperty("role") Player role,
                      @JsonProperty("readiness") boolean ready) {
    this.id = id;
    this.role = role;
    this.readiness = ready;
  }

  // Convenience constructors for normal instantiation:
  public PlayerStatus(Integer id) { this(id, Player.Black, false); }
  public PlayerStatus(Integer id, Player role) { this(id, role, false); }

  public Integer getId() { return id; }

  public boolean getReadiness() { return readiness; }

  public void setReadiness(boolean ready) { this.readiness = ready; }

  public Player getRole() { return role; }

  public void setRole(Player role) { this.role = role; }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof PlayerStatus))
      return false;
    PlayerStatus other = (PlayerStatus)obj;
    return this.readiness == other.readiness && this.role == other.role;
  }

  @Override
  public int hashCode() {
    int result = Boolean.hashCode(readiness);
    result = 31 * result + (role != null ? role.hashCode() : 0);
    return result;
  }
}
