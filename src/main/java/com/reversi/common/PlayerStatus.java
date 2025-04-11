package com.reversi.common;

/**
 * Captures the state of a player inside a {@link LobbyRoom},
 * including whether they are ready and what role they have.
 */
public class PlayerStatus {
  private final Integer id;
  private boolean ready;
  private Player role;

  public PlayerStatus(Integer id) { this(id, Player.Black); }
  public PlayerStatus(Integer id, Player role) {
    this.id = id;
    this.ready = false;
    this.role = role;
  }

  public Integer getId() { return id; }

  public boolean isReady() { return ready; }

  public void setReady(boolean ready) { this.ready = ready; }

  public Player getRole() { return role; }

  public void setRole(Player role) { this.role = role; }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof PlayerStatus))
      return false;
    PlayerStatus other = (PlayerStatus)obj;
    return this.ready == other.ready && this.role == other.role;
  }

  @Override
  public int hashCode() {
    int result = Boolean.hashCode(ready);
    result = 31 * result + (role != null ? role.hashCode() : 0);
    return result;
  }
}
