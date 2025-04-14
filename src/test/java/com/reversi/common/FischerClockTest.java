package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FischerClockTest {

  /**
   * A FakeTicker for testing purposes. Instead of scheduling ticks on a
   * background thread, it allows the test to manually trigger ticks.
   */
  private static class FakeTicker implements ITicker {
    private Runnable onTick;
    private boolean running = false;

    @Override
    public void start(Runnable onTick, long tickIntervalMillis) {
      this.onTick = onTick;
      running = true;
    }

    @Override
    public void stop() {
      running = false;
    }

    // Manually trigger a tick.
    public void triggerTick() {
      if (running && onTick != null) {
        onTick.run();
      }
    }

    public boolean isRunning() { return running; }
  }

  /**
   * A simple test listener that captures a TimeoutEvent.
   */
  public static class TestTimeoutEventListener
      implements EventListener<FischerClock.TimeoutEvent> {
    private FischerClock.TimeoutEvent receivedEvent = null;

    @Override
    public void onEvent(FischerClock.TimeoutEvent event) {
      receivedEvent = event;
    }

    public FischerClock.TimeoutEvent getReceivedEvent() {
      return receivedEvent;
    }
  }

  // Test configuration values.
  private final long initialTime = 300000; // 5 minutes in milliseconds
  private final long bonusTime = 2000;     // 2 seconds in milliseconds

  private FakeTicker fakeTicker;
  private FischerClock clock;
  private TestTimeoutEventListener listener;
  private EventBus eventBus;

  @BeforeEach
  void setUp() {
    fakeTicker = new FakeTicker();
    // Start with White's turn.
    clock = new FischerClock(initialTime, bonusTime, true);
    clock.setTicker(fakeTicker);

    listener = new TestTimeoutEventListener();
    eventBus = new EventBus();
    eventBus.register(FischerClock.TimeoutEvent.class, listener);
    clock.setEventBus(eventBus);
  }

  @Test
  void testTickReducesActivePlayerTime() {
    clock.start();
    // Simulate one tick (100ms)
    fakeTicker.triggerTick();

    // Since White is active, white's time should decrease by 100 ms,
    // while Black's time remains unchanged.
    assertEquals(initialTime - 100, clock.getWhiteTimeMillis(),
                 "White's time should be reduced by 100ms after one tick");
    assertEquals(initialTime, clock.getBlackTimeMillis(),
                 "Black's time should remain unchanged when it's not active");
  }

  @Test
  void testSwapAddsBonusAndSwitchesTurn() {
    clock.start();
    // Trigger a tick for White.
    fakeTicker.triggerTick();

    // Simulate a move. This should award White a bonus and switch turn.
    clock.swap();

    // After swapping, White should have their remaining time plus bonus,
    // and it is now Black's turn.
    assertEquals(initialTime - 100 + bonusTime, clock.getWhiteTimeMillis(),
                 "White's time should have the bonus added after swap");

    // Now trigger a tick and verify that Black's time decreases.
    fakeTicker.triggerTick();
    assertEquals(
        initialTime - 100, clock.getBlackTimeMillis(),
        "Black's time should decrease after tick now that it's their turn");
  }

  @Test
  void testStopWhenTimeRunsOutForWhite() {
    // Create a clock where White's remaining time is low.
    clock = new FischerClock(50, bonusTime, true);
    clock.setTicker(fakeTicker);
    clock.start();

    // Trigger a tick that will reduce White's time below zero.
    fakeTicker.triggerTick();

    // The clock should set White's time to 0 and stop the ticker.
    assertEquals(0, clock.getWhiteTimeMillis(),
                 "White's time should be set to 0 when time runs out");
    assertFalse(fakeTicker.isRunning(),
                "Ticker should be stopped after White runs out of time");
  }

  @Test
  void testStopWhenTimeRunsOutForBlack() {
    // Create a clock where Black's turn is active and time is low.
    // First, swap so that Black's turn is active.
    clock.swap();

    // Re-create the clock with a low time for Black.
    // For simplicity, we set initialTime to a low value; now Black's time is
    // 50ms.
    clock = new FischerClock(50, bonusTime, false);
    clock.setTicker(fakeTicker);
    clock.start();

    // Trigger a tick; Black's time should drop to 0.
    fakeTicker.triggerTick();

    assertEquals(0, clock.getBlackTimeMillis(),
                 "Black's time should be set to 0 when time runs out");
    assertFalse(fakeTicker.isRunning(),
                "Ticker should be stopped after Black runs out of time");
  }

  @Test
  void testFormatTime() {
    // For example, 125000ms should format to "02:05".
    String formatted = clock.formatTime(125000);
    assertEquals("02:05", formatted,
                 "formatTime should correctly format 125000ms as 02:05");
  }

  /**
   * Tests the serialization of a FischerClock.
   */
  @Test
  public void testSerialization() throws Exception {
    // Create a clock with custom times using a NoOpTicker.
    FischerClock clock = new FischerClock(1500, 200, false);
    // Modify times to be different between players.
    // (For example, suppose Black has less time remaining.)
    clock.swap(); // perform a swap to change state

    clock.setWhiteTimeMillis(1600);
    clock.setBlackTimeMillis(1400);

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(clock);
    // The JSON should include the whiteTimeMillis, blackTimeMillis,
    // bonusMillis, and isWhiteTurn. For example:
    // {"whiteTimeMillis":1600,"blackTimeMillis":1400,"bonusMillis":200,"isWhiteTurn":true/false}
    System.out.println("Serialized clock: " + json);
    assertTrue(json.contains("\"whiteTimeMillis\":1600"));
    assertTrue(json.contains("\"blackTimeMillis\":1400"));
    assertTrue(json.contains("\"bonusMillis\":200"));
  }

  /**
   * Tests deserialization, ensuring that the clock state is preserved.
   */
  @Test
  public void testDeserialization() throws Exception {
    String json = "{\"whiteTimeMillis\":1700,"
                  + "\"blackTimeMillis\":1300,"
                  + "\"bonusMillis\":150,"
                  + "\"isWhiteTurn\":false}";
    ObjectMapper mapper = JacksonObjMapper.get();
    FischerClock clock = mapper.readValue(json, FischerClock.class);
    clock.setTicker(fakeTicker);

    assertEquals(1700, clock.getWhiteTimeMillis(),
                 "White time should be 1700 ms");
    assertEquals(1300, clock.getBlackTimeMillis(),
                 "Black time should be 1300 ms");

    clock.start();
    fakeTicker.triggerTick();
    clock.swap();
    fakeTicker.triggerTick();

    assertEquals(1350, clock.getBlackTimeMillis());
    assertEquals(1600, clock.getWhiteTimeMillis());
  }
  /**
   * Test that when White's time runs out, a TimeoutEvent is posted.
   */
  @Test
  public void testWhiteTimeoutNotification() {
    // Set up a FischerClock where white starts and has a very low amount of
    // time. We use 50 ms so that a single tick of 100ms will push white's time
    // to 0.
    FischerClock clock = new FischerClock(50, 0, true);
    clock.setTicker(fakeTicker);
    clock.setEventBus(eventBus);

    // Start the clock (which arranges the fake ticker callback).
    clock.start();

    // Manually trigger one tick.
    fakeTicker.triggerTick();

    // After one tick, since white was active and 50 - 100 <= 0,
    // a timeout event should have been posted.
    FischerClock.TimeoutEvent received = listener.getReceivedEvent();
    assertNotNull(
        received,
        "A timeout event should be posted when White runs out of time.");
    assertTrue(received.isWhiteTimeout,
               "The timeout event should indicate a timeout (true).");
    assertTrue(clock.equals(received.getClock()));
  }

  /**
   * Test that when Black's time runs out, a TimeoutEvent is posted.
   */
  @Test
  public void testBlackTimeoutNotification() {
    // Set up a FischerClock with black's turn active.
    // Create the clock so that whiteStarts is false (meaning black is active).
    // Also set black's time to 50 ms.
    FischerClock clock = new FischerClock(100, 0, false);
    clock.setTicker(fakeTicker);
    clock.setEventBus(eventBus);

    // Adjust black's time to be low, so it times out on one tick.
    clock.setBlackTimeMillis(50);

    // Start the clock.
    clock.start();

    // Manually trigger one tick.
    fakeTicker.triggerTick();

    // Verify that an event was posted.
    FischerClock.TimeoutEvent received = listener.getReceivedEvent();
    assertNotNull(
        received,
        "A timeout event should be posted when Black runs out of time.");
    assertTrue(clock.equals(received.getClock()));
  }
}
