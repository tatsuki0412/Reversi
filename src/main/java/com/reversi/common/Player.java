package com.reversi.common;

public enum Player {
  None('.'),
  Black('B'),
  White('W');

  private final char c;
  Player(char c) { this.c = c; }

  public char toChar() { return c; }

  public boolean equals(Player other) { return c == other.c; }

  public Player opponent() {
    return switch (c) {
      case 'B' -> White;
      case 'W' -> Black;
      default -> None;
    };
  }

  public static Player from(char c) {
    return switch (c) {
      case 'b', 'B' -> Black;
      case 'w', 'W' -> White;
      default -> None;
    };
  }
}
