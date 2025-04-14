package com.reversi.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;

/**
 * A clock implementation based on the Fischer time control mechanism.
 * <p>
 * Each side (white and black) is given an initial time, and every move results
 * in a bonus time added to the clock of the player who just moved.
 * </p>
 * <p>
 * This class supports serialization and deserialization using custom Jackson
 * serializers. Note that transient fields (ticker, lock, running, eventBus) are
 * not serialized.
 * </p>
 *
 * <p><b>Usage:</b> In production a real ticker (e.g., {@link Ticker})
 * should be provided; for testing purposes, a dummy ticker (e.g., {@link
 * NoOpTicker}) can be used.</p>
 */
@JsonSerialize(using = FischerClock.FischerClockSerializer.class)
@JsonDeserialize(using = FischerClock.FischerClockDeserializer.class)
public class FischerClock {
  /**
   * Remaining time (in milliseconds) for the white player.
   */
  private long whiteTimeMillis;

  /**
   * Remaining time (in milliseconds) for the black player.
   */
  private long blackTimeMillis;

  /**
   * Bonus time (in milliseconds) added to a player's clock after each move.
   */
  private final long bonusMillis;

  /**
   * Indicates whether it is currently the white player's turn.
   */
  private boolean isWhiteTurn;

  /**
   * The ticker used to simulate or measure time ticks.
   * This field is transient and will not be serialized.
   */
  private transient ITicker ticker;

  /**
   * A lock object to synchronize access to mutable clock state.
   * This is used to ensure thread safety.
   */
  private final transient Object lock = new Object();

  /**
   * A flag indicating whether the clock is currently running.
   */
  private transient boolean running = false;

  /**
   * An event bus for dispatching clock-related events (e.g., timeouts).
   */
  private transient EventBus eventBus;

  /**
   * Constructs a new FischerClock with the specified initial time, bonus time,
   * and starting side. <p> In production, a real ticker should be supplied via
   * {@link #setTicker(ITicker)}. During testing, it may be replaced with a dummy
   * implementation.
   * </p>
   *
   * @param initialTimeMillis the initial time (in milliseconds) for both
   *     players
   * @param bonusMillis       the bonus time (in milliseconds) added on each
   *     move
   * @param whiteStarts       true if the white player starts; false if black
   *     starts
   */
  public FischerClock(long initialTimeMillis, long bonusMillis,
                      boolean whiteStarts) {
    this.whiteTimeMillis = initialTimeMillis;
    this.blackTimeMillis = initialTimeMillis;
    this.bonusMillis = bonusMillis;
    this.isWhiteTurn = whiteStarts;
    this.ticker = new NoOpTicker();
  }

  /**
   * Sets the ticker responsible for generating time ticks.
   *
   * @param ticker the ticker implementation to be used
   */
  public void setTicker(ITicker ticker) { this.ticker = ticker; }

  /**
   * Sets the event bus which will receive clock events such as timeouts.
   *
   * @param eventBus the event bus implementation for dispatching events
   */
  public void setEventBus(EventBus eventBus) { this.eventBus = eventBus; }

  /**
   * Starts the clock. This method initiates the ticker to generate tick events
   * at regular intervals. <p> If the clock is already running, calling this
   * method has no effect.
   * </p>
   */
  public void start() {
    synchronized (lock) {
      if (running) {
        return;
      }
      running = true;
    }
    // Start the ticker; the tick method is scheduled to be called every 100
    // milliseconds.
    ticker.start(() -> tick(100), 100);
  }

  /**
   * Stops the clock. This stops the ticker and marks the clock as no longer
   * running.
   */
  public void stop() {
    synchronized (lock) {
      ticker.stop();
      running = false;
    }
  }

  /**
   * Advances the clock by the given time delta, affecting the player whose turn
   * is active. <p> If a player's time falls below or reaches zero, the clock is
   * stopped and a timeout event is triggered.
   * </p>
   *
   * @param deltaMs the time in milliseconds to subtract from the current
   *     player's clock
   */
  private void tick(long deltaMs) {
    synchronized (lock) {
      if (isWhiteTurn) {
        whiteTimeMillis -= deltaMs;
        if (whiteTimeMillis <= 0) {
          whiteTimeMillis = 0;
          stop();
          notifyTimeout(true);
        }
      } else {
        blackTimeMillis -= deltaMs;
        if (blackTimeMillis <= 0) {
          blackTimeMillis = 0;
          stop();
          notifyTimeout(false);
        }
      }
    }
  }

  /**
   * Swaps the active player after a move has been made.
   * <p>
   * When this method is called, a bonus is added to the clock of the player who
   * just moved, and the turn is toggled to the other player.
   * </p>
   */
  public void swap() {
    synchronized (lock) {
      if (isWhiteTurn) {
        whiteTimeMillis += bonusMillis;
      } else {
        blackTimeMillis += bonusMillis;
      }
      // Toggle the turn.
      isWhiteTurn = !isWhiteTurn;
    }
  }

  /**
   * Returns the current remaining time for the white player.
   *
   * @return white player's remaining time in milliseconds
   */
  public long getWhiteTimeMillis() {
    synchronized (lock) { return whiteTimeMillis; }
  }

  /**
   * Sets the remaining time for the white player.
   *
   * @param t the new time in milliseconds for the white player
   */
  public void setWhiteTimeMillis(long t) {
    synchronized (lock) { whiteTimeMillis = t; }
  }

  /**
   * Returns the current remaining time for the black player.
   *
   * @return black player's remaining time in milliseconds
   */
  public long getBlackTimeMillis() {
    synchronized (lock) { return blackTimeMillis; }
  }

  /**
   * Sets the remaining time for the black player.
   *
   * @param t the new time in milliseconds for the black player
   */
  public void setBlackTimeMillis(long t) {
    synchronized (lock) { blackTimeMillis = t; }
  }

  /**
   * Formats a time value in milliseconds into a string in "MM:SS" format.
   *
   * @param millis the time value in milliseconds
   * @return a formatted string representing minutes and seconds
   */
  public String formatTime(long millis) {
    long totalSeconds = millis / 1000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  // --------------------- Events ---------------------

  /**
   * Represents a timeout event that is triggered when a player's time runs out.
   * <p>
   * The event holds information about which side (white or black) has timed
   * out.
   * </p>
   */
  public class TimeoutEvent extends Event {
    /**
     * Flag indicating if the timeout is for the white player.
     */
    public boolean isWhiteTimeout;

    /**
     * Constructs a new TimeoutEvent.
     *
     * @param isWhiteTimeout true if the white player's time has expired, false
     *     for black
     */
    TimeoutEvent(boolean isWhiteTimeout) {
      this.isWhiteTimeout = isWhiteTimeout;
    }

    /**
     * Retrieves the FischerClock instance that generated this timeout event.
     *
     * @return the FischerClock instance associated with this event
     */
    public FischerClock getClock() { return FischerClock.this; }
  }

  /**
   * Notifies the event bus of a timeout.
   * <p>
   * This method is called internally when a player's time reaches zero.
   * </p>
   *
   * @param isWhiteTimeout true if the timeout occurred for the white player,
   *                       false if for the black player
   */
  void notifyTimeout(boolean isWhiteTimeout) {
    if (eventBus != null) {
      eventBus.post(this.new TimeoutEvent(isWhiteTimeout));
    }
  }

  // --------------------- Serialization Support ---------------------

  /**
   * Custom Jackson serializer for FischerClock.
   * <p>
   * Serializes only the persistent fields of the FischerClock, excluding
   * transient fields.
   * </p>
   */
  public static class FischerClockSerializer
      extends JsonSerializer<FischerClock> {
    @Override
    public void serialize(FischerClock clock, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      gen.writeNumberField("whiteTimeMillis", clock.whiteTimeMillis);
      gen.writeNumberField("blackTimeMillis", clock.blackTimeMillis);
      gen.writeNumberField("bonusMillis", clock.bonusMillis);
      gen.writeBooleanField("isWhiteTurn", clock.isWhiteTurn);
      gen.writeEndObject();
    }
  }

  /**
   * Custom Jackson deserializer for FischerClock.
   * <p>
   * Reconstructs a FischerClock from JSON by reading the persistent fields.
   * A dummy ticker (NoOpTicker) is assigned by default.
   * </p>
   */
  public static class FischerClockDeserializer
      extends JsonDeserializer<FischerClock> {
    @Override
    public FischerClock deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      long whiteTime = 0;
      long blackTime = 0;
      long bonus = 0;
      boolean isWhiteTurn = true;

      if (p.currentToken() != JsonToken.START_OBJECT) {
        throw new IOException("Expected START_OBJECT token, but got: " +
                              p.currentToken());
      }
      // Parse fields from JSON.
      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.getCurrentName();
        p.nextToken();
        if ("whiteTimeMillis".equals(fieldName)) {
          whiteTime = p.getLongValue();
        } else if ("blackTimeMillis".equals(fieldName)) {
          blackTime = p.getLongValue();
        } else if ("bonusMillis".equals(fieldName)) {
          bonus = p.getLongValue();
        } else if ("isWhiteTurn".equals(fieldName)) {
          isWhiteTurn = p.getBooleanValue();
        } else {
          throw new IOException("Unexpected field: " + fieldName);
        }
      }
      // Instantiate the clock with dummy initial time; values will be
      // overridden.
      FischerClock clock = new FischerClock(0, bonus, isWhiteTurn);
      clock.whiteTimeMillis = whiteTime;
      clock.blackTimeMillis = blackTime;
      return clock;
    }
  }

  // ---------------------------------------------------------------
  /**
   * A no-operation ticker implementation.
   * <p>
   * This ticker does nothing when its {@code start()} or {@code stop()} methods
   * are called. It is particularly useful during deserialization to ensure that
   * no timing operations are inadvertently started.
   * </p>
   */
  public static class NoOpTicker implements ITicker {
    @Override
    public void start(Runnable onTick, long tickIntervalMillis) {
      // Intentionally does nothing.
    }

    @Override
    public void stop() {
      // Intentionally does nothing.
    }
  }
}
