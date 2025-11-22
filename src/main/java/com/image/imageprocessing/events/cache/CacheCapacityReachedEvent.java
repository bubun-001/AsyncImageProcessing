package com.image.imageprocessing.events.cache;

import com.image.imageprocessing.events.Event;

public record CacheCapacityReachedEvent(int maxSize) implements Event {
}

