package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LobbyRoomTest {

  private LobbyRoom lobby;

  @BeforeEach
  public void setUp() {
    lobby = new LobbyRoom("TestRoom");
  }

  @Test
  public void testRoomNameAndEmptyStatus() {
    // Verify the room name, emptiness, and initial size.
    assertEquals("TestRoom", lobby.getRoomName());
    assertTrue(lobby.isEmpty());
    assertEquals(0, lobby.size());
  }

  @Test
  public void testAddPlayerSuccessAndRoleAssignment() {
    // Add the first player.
    PlayerStatus ps1 = new PlayerStatus(1);
    assertTrue(lobby.addPlayer(ps1));
    // The first player's role should be set to Black.
    assertEquals(Player.Black, ps1.getRole());
    assertFalse(lobby.isEmpty());
    assertTrue(lobby.contains(1));
    assertEquals(1, lobby.size());

    // Add the second player.
    PlayerStatus ps2 = new PlayerStatus(2);
    assertTrue(lobby.addPlayer(ps2));
    // The second player's role should be set to White.
    assertEquals(Player.White, ps2.getRole());
    assertEquals(2, lobby.size());
  }

  @Test
  public void testAddPlayerFailsWhenFullOrDuplicate() {
    // Add two players.
    PlayerStatus ps1 = new PlayerStatus(1);
    PlayerStatus ps2 = new PlayerStatus(2);
    PlayerStatus ps3 = new PlayerStatus(3);
    assertTrue(lobby.addPlayer(ps1));
    assertTrue(lobby.addPlayer(ps2));

    // The lobby is full (size >= 2), so adding another should fail.
    assertFalse(lobby.addPlayer(ps3));

    // Attempt to add a duplicate player (same id) should also fail.
    PlayerStatus duplicate = new PlayerStatus(1);
    assertFalse(lobby.addPlayer(duplicate));
  }

  @Test
  public void testUpdatePlayerStatusNoChangeAndNullInput() {
    // Add a player.
    PlayerStatus ps1 = new PlayerStatus(1);
    lobby.addPlayer(ps1);

    // Update with a new status that is equal to the current status.
    // By default, new PlayerStatus(1) has ready=false and role will be later
    // set to Black by addPlayer.
    PlayerStatus sameStatus = new PlayerStatus(1);
    // No change is applied because equals compares the ready state and role.
    assertFalse(lobby.updatePlayerStatus(1, sameStatus));

    // Test update when newStatus is null.
    assertFalse(lobby.updatePlayerStatus(1, null));

    // Test update for a non-existent player.
    assertFalse(lobby.updatePlayerStatus(3, new PlayerStatus(3)));
  }

  @Test
  public void testUpdatePlayerStatusWithChangeAndCancelReadiness() {
    // Create and add two players.
    PlayerStatus ps1 = new PlayerStatus(1);
    PlayerStatus ps2 = new PlayerStatus(2);
    lobby.addPlayer(ps1);
    lobby.addPlayer(ps2);

    // Set both players as ready.
    ps1.setReady(true);
    ps2.setReady(true);

    // Update one player's status such that it differs from its current status.
    // Note: The default constructor creates a status with ready=false.
    PlayerStatus newStatus = new PlayerStatus(1);
    // The player originally was ready, so this is a change.
    assertTrue(lobby.updatePlayerStatus(1, newStatus));

    // After updating, the cancelReadiness method should have been called:
    // All players' readiness should now be set to false.
    assertFalse(lobby.getPlayerStatus(1).isReady());
    assertFalse(lobby.getPlayerStatus(2).isReady());
  }

  @Test
  public void testIsReadyToStart() {
    // When only one player is in the lobby, it should not be ready to start.
    PlayerStatus ps1 = new PlayerStatus(1);
    lobby.addPlayer(ps1);
    ps1.setReady(true);
    assertFalse(lobby.isReadyToStart());

    // Add a second player.
    PlayerStatus ps2 = new PlayerStatus(2);
    lobby.addPlayer(ps2);

    // When both players are ready and roles are correctly set, the lobby is
    // ready to start.
    ps2.setReady(true);
    assertTrue(lobby.isReadyToStart());

    // If one of the players becomes unready, the lobby should not be ready.
    ps2.setReady(false);
    assertFalse(lobby.isReadyToStart());

    // If roles are not properly balanced (simulate by manually setting both
    // players to the same role), the lobby should not be ready.
    ps2.setRole(ps1.getRole()); // both players now have the same role
    ps1.setReady(true);
    ps2.setReady(true);
    assertFalse(lobby.isReadyToStart());
  }

  @Test
  public void testRemovePlayerAndCancelReadiness() {
    // Add two players and mark them as ready.
    PlayerStatus ps1 = new PlayerStatus(1);
    PlayerStatus ps2 = new PlayerStatus(2);
    lobby.addPlayer(ps1);
    lobby.addPlayer(ps2);
    ps1.setReady(true);
    ps2.setReady(true);

    // Remove one player.
    assertTrue(lobby.removePlayer(1));
    assertFalse(lobby.contains(1));
    assertEquals(1, lobby.size());

    // The removal should cancel readiness for all players.
    assertFalse(ps2.isReady());

    // Removing a non-existent player should return false.
    assertFalse(lobby.removePlayer(3));
  }

  @Test
  public void testGetPlayersAndGetPlayerStatus() {
    // Add two players.
    PlayerStatus ps1 = new PlayerStatus(1);
    PlayerStatus ps2 = new PlayerStatus(2);
    lobby.addPlayer(ps1);
    lobby.addPlayer(ps2);

    // Retrieve the map of players.
    Map<Integer, PlayerStatus> playersMap = lobby.getPlayers();
    assertEquals(2, playersMap.size());
    assertTrue(playersMap.containsKey(1));
    assertTrue(playersMap.containsKey(2));

    // Retrieve a single player's status.
    PlayerStatus retrieved = lobby.getPlayerStatus(1);
    assertNotNull(retrieved);

    // Since LobbyRoom adds the player and sets the role automatically,
    // we construct an expected PlayerStatus. Remember that equals compares only
    // the ready status and role.
    PlayerStatus expected = new PlayerStatus(1);
    expected.setRole(Player.Black);
    // By default, ready is false which is what we expect.
    assertEquals(expected, retrieved);
  }
}
