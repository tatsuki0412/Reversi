package com.reversi.common;

/**
 * The generic listener interface for events.
 * @param <T> the type of event this listener handles
 */
public interface EventListener<T extends Event> {
  /**
   * Called when the event is triggered.
   * @param event the event object containing relevant data
   */
  void onEvent(T event);
}
