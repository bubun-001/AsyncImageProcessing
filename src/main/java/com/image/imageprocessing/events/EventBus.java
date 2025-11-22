package com.image.imageprocessing.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple in-memory event bus based on the observer pattern.
 */
public final class EventBus {

    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<? extends Event>, CopyOnWriteArrayList<EventListener<? extends Event>>> listeners =
            new ConcurrentHashMap<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void publish(Event event) {
        if (event == null) {
            return;
        }
        List<EventListener<? extends Event>> registered = listeners.get(event.getClass());
        if (registered == null) {
            return;
        }
        for (EventListener listener : registered) {
            listener.onEvent(event);
        }
    }
}

