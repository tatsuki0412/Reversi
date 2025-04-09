package com.reversi.common;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * A custom event implementation that holds a message.
 */
class CustomEvent implements Event {
  private final LocalTime timestamp;
  private final String message;

  /**
   * Constructs a CustomEvent with the given message.
   *
   * @param message the message associated with this event
   */
  public CustomEvent(LocalTime time, String message) {
    this.timestamp = time;
    this.message = message;
  }

  @Override
  public LocalTime getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the message of this event.
   * @return the event message
   */
  public String getMessage() { return message; }
}

public class EventSystemTest {
  @Test
  public void testSingleListenerIsInvoked() {
    // Create a new EventBus instance
    EventBus eventBus = new EventBus();

    // Use an AtomicBoolean to capture if the event was received
    AtomicBoolean eventReceived = new AtomicBoolean(false);
    String testMessage = "Test Message";
    LocalTime time = LocalTime.now();
    CustomEvent event = new CustomEvent(time, testMessage);

    // Register a listener for the CustomEvent that checks the event's message
    // and timestamp
    eventBus.register(CustomEvent.class, new EventListener<CustomEvent>() {
      @Override
      public void onEvent(CustomEvent event) {
        eventReceived.set(true);
        assertEquals(testMessage, event.getMessage());
        // Verify that the timestamp is set to a positive value
        assertEquals(time, event.getTimestamp());
      }
    });

    eventBus.post(event);

    // Verify that the listener was triggered
    assertTrue(eventReceived.get(),
               "The event listener should have been invoked.");
  }

  @Test
  public void testMultipleListenersAreInvoked() {
    EventBus eventBus = new EventBus();

    // Use an AtomicInteger to count how many listeners have been invoked
    AtomicInteger invocationCount = new AtomicInteger(0);
    String testMessage = "Another Test Message";
    LocalTime time = LocalTime.now();
    CustomEvent event = new CustomEvent(time, testMessage);

    // Create a single listener that increments the count when invoked
    EventListener<CustomEvent> listener = new EventListener<CustomEvent>() {
      @Override
      public void onEvent(CustomEvent event) {
        invocationCount.incrementAndGet();
      }
    };

    // Register the same listener twice (or you could register two different
    // listeners)
    eventBus.register(CustomEvent.class, listener);
    eventBus.register(CustomEvent.class, listener);

    eventBus.post(event);

    // Verify that both listener invocations have been counted
    assertEquals(2, invocationCount.get(),
                 "Both listeners should have been invoked.");
  }

  @Test
  public void testEventWithNoListenerDoesNotThrow() {
    EventBus eventBus = new EventBus();

    // Post an event for which no listener is registered.
    // The system should not throw an exception.
    CustomEvent event = new CustomEvent(LocalTime.now(), "No Listener");
    assertDoesNotThrow(
        ()
            -> eventBus.post(event),
        "Posting an event with no listeners should not throw an exception.");
  }

  @Test
  public void testFilterEventType() {
    EventBus eventBus = new EventBus();

    class MyEvent implements Event {
      @Override
      public LocalTime getTimestamp() {
        return LocalTime.now();
      }
    }

    AtomicInteger myListenerCnt = new AtomicInteger(0);
    AtomicInteger customListenerCnt = new AtomicInteger(0);

    eventBus
        .register(MyEvent.class,
                  new EventListener<MyEvent>() {
                    @Override
                    public void onEvent(MyEvent e) {
                      myListenerCnt.incrementAndGet();
                    }
                  })
        .register(CustomEvent.class, new EventListener<CustomEvent>() {
          @Override
          public void onEvent(CustomEvent e) {
            customListenerCnt.incrementAndGet();
          }
        });

    eventBus.post(new MyEvent());
    assertEquals(1, myListenerCnt.get());
    assertEquals(0, customListenerCnt.get());

    eventBus.post(new CustomEvent(LocalTime.now(), "whatever"));
    assertEquals(1, myListenerCnt.get());
    assertEquals(1, customListenerCnt.get());
  }

  @RepeatedTest(3)
  public void testConcurrentPostingAndRegistrationStressTest()
      throws InterruptedException {
    // Setup
    final int threadCount = 10;
    final int postsPerThread = 1000;
    EventBus eventBus = new EventBus();
    AtomicInteger invocationCount = new AtomicInteger(0);

    // Use a CountDownLatch to synchronize thread start.
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch registrationLatch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);

    // Pre-register several listeners concurrently.
    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          startLatch.await();
          eventBus.register(CustomEvent.class,
                            new EventListener<CustomEvent>() {
                              @Override
                              public void onEvent(CustomEvent event) {
                                invocationCount.incrementAndGet();
                              }
                            });
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          registrationLatch.countDown();
        }
      });
    }

    // Create tasks that post events concurrently
    CountDownLatch postLatch = new CountDownLatch(threadCount);
    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          registrationLatch.await();
          for (int j = 0; j < postsPerThread; j++) {
            eventBus.post(new CustomEvent(LocalTime.now(), "Stress Test"));
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          postLatch.countDown();
        }
      });
    }

    // Release all threads to start concurrently.
    startLatch.countDown();

    // Wait for all posts to finish.
    boolean postsCompleted = postLatch.await(10, TimeUnit.SECONDS);
    executor.shutdownNow();
    assertTrue(postsCompleted, "Not all posts completed in the expected time");

    // With threadCount listeners and threadCount*postsPerThread posted events,
    // the expected invocation count is threadCount * threadCount *
    // postsPerThread.
    int expectedCount = threadCount * threadCount * postsPerThread;
    assertEquals(expectedCount, invocationCount.get(),
                 "The total number of event invocations should match the "
                     + "expected count.");
  }
}
