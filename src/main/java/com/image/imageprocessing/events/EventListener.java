package com.image.imageprocessing.events;

@FunctionalInterface
public interface EventListener<T extends Event> {

    void onEvent(T event);
}

