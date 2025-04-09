package com.reversi.common;

import java.util.*;

/**
 * A simple generic event bus for registering and dispatching events.
 */
public class EventBus {

  // Map event types to their registered listeners
  private Map<Class<? extends Event>, List<EventListener<? extends Event>>>
      listeners = new HashMap<>();

  /**
   * Registers a listener for a specific type of event.
   *
   * @param eventType the class type of the event to listen for
   * @param listener  the listener implementation
   * @param <T>       the type of the event
   */
  public <T extends Event> EventBus register(Class<T> eventType,
                                             EventListener<T> listener) {
    listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    return this;
  }

  /**
   * Posts an event to all registered listeners that handle this event type.
   *
   * @param event the event to dispatch
   * @param <T>   the type of the event
   */
  @SuppressWarnings("unchecked")
  public <T extends Event> void post(T event) {
    List<EventListener<? extends Event>> registeredListeners =
        listeners.get(event.getClass());
    if (registeredListeners != null) {
      for (EventListener<? extends Event> listener : registeredListeners) {
        // Cast listener to proper type and invoke the handler.
        ((EventListener<T>)listener).onEvent(event);
      }
    }
  }
}
