package com.image.imageprocessing.events.cache;

import com.image.imageprocessing.events.Event;

public record CacheStatsEvent(long hitCount, long missCount, double hitRate) implements Event {
}

