package com.image.imageprocessing.cache;

import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.events.EventBus;
import com.image.imageprocessing.events.cache.CacheCapacityReachedEvent;
import com.image.imageprocessing.events.cache.CacheEvictionEvent;
import com.image.imageprocessing.events.cache.CacheStatsEvent;
import com.image.imageprocessing.metrics.CacheMetrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple LRU cache for processed tiles.
 */
public final class TileCache {

    private static final int DEFAULT_CAPACITY =
            Integer.getInteger("tile.cache.capacity", 512);

    private static final TileCache INSTANCE = new TileCache(DEFAULT_CAPACITY);

    private final int maxSize;
    private final LinkedHashMap<TileKey, ImageData> cache;
    private final EventBus eventBus;
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    private TileCache(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
        System.out.println("TileCache initialized with capacity: " + this.maxSize);
        this.eventBus = EventBus.getInstance();
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<TileKey, ImageData> eldest) {
                boolean shouldEvict = size() > TileCache.this.maxSize;
                if (shouldEvict) {
                    CacheMetrics.recordEviction();
                    eventBus.publish(new CacheEvictionEvent(eldest.getKey(), eldest.getValue()));
                }
                return shouldEvict;
            }
        };
    }

    public static TileCache getInstance() {
        return INSTANCE;
    }

    public synchronized ImageData get(TileKey key) {
        Objects.requireNonNull(key, "key must not be null");
        ImageData value = cache.get(key);
        if (value != null) {
            CacheMetrics.recordHit();
            publishStats(hitCount.incrementAndGet(), missCount.get());
        } else {
            CacheMetrics.recordMiss();
            publishStats(hitCount.get(), missCount.incrementAndGet());
        }
        return value;
    }

    public synchronized void put(TileKey key, ImageData value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");

        boolean willExceedCapacity = !cache.containsKey(key) && cache.size() >= maxSize;
        if (willExceedCapacity) {
            eventBus.publish(new CacheCapacityReachedEvent(maxSize));
        }
        cache.put(key, value);
        CacheMetrics.updateSize(cache.size());
    }

    private void publishStats(long hits, long misses) {
        long total = hits + misses;
        if (total == 0) {
            return;
        }
        double hitRate = hits / (double) total;
        eventBus.publish(new CacheStatsEvent(hits, misses, hitRate));
    }
}

