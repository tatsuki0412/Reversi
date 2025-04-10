package com.reversi.common;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple generic event bus for registering and dispatching events.
 */
public class EventBus {

  // Map event types to their registered listeners, stored as WeakReferences.
  private Map<Class<? extends Event>,
              List<WeakReference<EventListener<? extends Event>>>> listeners =
      new ConcurrentHashMap<>();

  /**
   * Registers a listener for a specific type of event.
   *
   * @param eventType the class type of the event to listen for
   * @param listener  the listener implementation
   * @param <T>       the type of the event
   */
  public <T extends Event> EventBus register(Class<T> eventType,
                                             EventListener<T> listener) {
    // Wrap the listener in a WeakReference.
    WeakReference<EventListener<? extends Event>> weakListener =
        new WeakReference<>(listener);

    listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
        .add(weakListener);
    return this;
  }

  /**
   * Posts an event to all registered listeners that handle this event type.
   * Expired listeners are removed during traversal.
   *
   * @param event the event to dispatch
   * @param <T>   the type of the event
   */
  @SuppressWarnings("unchecked")
  public <T extends Event> EventBus post(T event) {
    List<WeakReference<EventListener<? extends Event>>> registeredListeners =
        listeners.get(event.getClass());

    if (registeredListeners != null) {
      // Remove expired (garbage collected) listeners before invoking any
      // callbacks.
      // registeredListeners.removeIf(ref -> ref.get() == null);

      for (WeakReference<EventListener<? extends Event>> ref :
           registeredListeners) {
        EventListener<? extends Event> listener = ref.get();
        if (listener != null) {
          // Cast the listener to the proper type and invoke the event handler.
          ((EventListener<T>)listener).onEvent(event);
        }
      }
    }
    return this;
  }

  /**
   * Returns the number of active (non-expired) listeners registered for the
   * specified event type. This method is provided for testing purposes.
   *
   * @param eventType the class type of the event
   * @return the number of active listeners
   */
  int getActiveListenersCount(Class<? extends Event> eventType) {
    List<WeakReference<EventListener<? extends Event>>> registeredListeners =
        listeners.get(eventType);
    if (registeredListeners == null) {
      return 0;
    }
    int count = 0;
    for (WeakReference<EventListener<? extends Event>> ref :
         registeredListeners) {
      if (ref.get() != null) {
        count++;
      }
    }
    return count;
  }
}
