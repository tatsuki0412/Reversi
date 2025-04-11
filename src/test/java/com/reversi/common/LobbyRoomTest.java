package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LobbyRoomTest {

  private LobbyRoom room;

  @BeforeEach
  void setUp() {
    room = new LobbyRoom("TestRoom");
  }

  @Test
  void testSize() {
    assertEquals(0, room.size());
    assertTrue(room.isEmpty());
  }

  @Test
  void testAddPlayer() {
    // Add first player
    assertTrue(room.addPlayer(1), "Should be able to add first player");
    // Adding the same player should fail
    assertFalse(room.addPlayer(1),
                "Should not be able to add a duplicate player");
    // Add second player successfully
    assertTrue(room.addPlayer(2), "Should be able to add second player");
    // Cannot add a third player - room limit is 2 players
    assertFalse(room.addPlayer(3), "Should not be able to add a third player");

    // Verify that the player list size is 2
    List<Integer> players = room.getPlayers();
    assertEquals(2, players.size(), "Room should contain exactly 2 players");
  }

  @Test
  void testSetReadiness() {
    room.addPlayer(1);
    room.addPlayer(2);

    // Initially, all players are not ready; room should not be ready to start
    assertFalse(room.isReadyToStart(),
                "Room should not be ready to start initially");

    // Set both players to ready
    assertTrue(room.setReadiness(1, true),
               "Should successfully set player 1 as ready");
    assertTrue(room.setReadiness(2, true),
               "Should successfully set player 2 as ready");
    assertTrue(room.isReadyToStart(),
               "Room should be ready to start when both players are ready");

    // Toggling one player back to not ready resets all readiness.
    assertTrue(room.setReadiness(1, false),
               "Toggling readiness should succeed");
    assertFalse(room.isReadyToStart(),
                "Room should not be ready after a player cancels ready");
  }

  @Test
  void testSetReadiness_InvalidPlayer() {
    // Attempting to set readiness for a player that hasn't been added should
    // fail.
    assertFalse(room.setReadiness(1, true),
                "Setting readiness for an unknown player should fail");
  }

  @Test
  void testSetRole() {
    room.addPlayer(1);
    room.addPlayer(2);

    // Setting role to Player.None should fail
    assertFalse(room.setRole(1, Player.None),
                "Setting role to None should fail");

    // Set both players to ready before changing role
    room.setReadiness(1, true);
    room.setReadiness(2, true);
    assertTrue(room.isReadyToStart(), "Room should be ready to start");

    // Change role for player 1
    // Changing role resets readiness for all players
    assertTrue(room.setRole(1, Player.White),
               "Should be able to change the role for player 1");
    assertFalse(
        room.isReadyToStart(),
        "Room should not be ready after a role change resets readiness");

    // Change back to the original role for player 1
    assertTrue(room.setRole(1, Player.Black),
               "Should be able to change the role back");
    // Re-set readiness to mark room ready to start
    room.setReadiness(1, true);
    room.setReadiness(2, true);
    assertTrue(
        room.isReadyToStart(),
        "Room should be ready after both players are ready with valid roles");
  }

  @Test
  void testSetRole_InvalidPlayer() {
    // Attempt to set role for a player who is not present should return false.
    assertFalse(room.setRole(1, Player.Black),
                "Setting role for a non-existent player should fail");
  }

  @Test
  void testRemovePlayer() {
    room.addPlayer(1);
    room.addPlayer(2);
    room.setReadiness(1, true);
    room.setReadiness(2, true);
    assertTrue(room.isReadyToStart(), "Room should be ready before removal");

    // Remove one player
    assertTrue(room.removePlayer(1),
               "Should successfully remove an existing player");
    assertFalse(room.isReadyToStart(),
                "Room should not be ready after a player is removed");

    // Ensure getPlayers returns correct list size after removal
    List<Integer> players = room.getPlayers();
    assertEquals(1, players.size(),
                 "Room should contain 1 player after removal");

    // Attempting to remove the same player again should fail
    assertFalse(room.removePlayer(1),
                "Removing a non-existent player should fail");
  }

  @Test
  void testGetPlayersImmutability() {
    room.addPlayer(1);
    List<Integer> playersCopy = room.getPlayers();
    playersCopy.add(2); // Mutate the returned list
    // The actual players list inside LobbyRoom should remain unchanged.
    assertEquals(
        1, room.getPlayers().size(),
        "Internal players list should remain immutable from external changes");
  }
}
