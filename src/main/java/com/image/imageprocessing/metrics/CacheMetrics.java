package com.image.imageprocessing.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class CacheMetrics {
    
    private static final AtomicLong cacheHits = new AtomicLong(0);
    private static final AtomicLong cacheMisses = new AtomicLong(0);
    private static final AtomicLong cacheEvictions = new AtomicLong(0);
    private static final AtomicLong cacheSize = new AtomicLong(0);
    
    private static final Map<String, Counter> COUNTERS = new ConcurrentHashMap<>();
    
    static {
        MeterRegistry registry = MetricsRegistry.registry();
        
        COUNTERS.put("hits", Counter.builder("cache_hits_total")
            .description("Total number of cache hits")
            .register(registry));
        
        COUNTERS.put("misses", Counter.builder("cache_misses_total")
            .description("Total number of cache misses")
            .register(registry));
            
        COUNTERS.put("evictions", Counter.builder("cache_evictions_total")
            .description("Total number of cache evictions")
            .register(registry));
            
        Gauge.builder("cache_size", cacheSize, AtomicLong::doubleValue)
            .description("Current cache size")
            .register(registry);
            
        Gauge.builder("cache_hit_rate", () -> {
            long hits = cacheHits.get();
            long misses = cacheMisses.get();
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        })
        .description("Cache hit rate (0.0 to 1.0)")
        .register(registry);
    }
    
    public static void recordHit() {
        cacheHits.incrementAndGet();
        COUNTERS.get("hits").increment();
    }
    
    public static void recordMiss() {
        cacheMisses.incrementAndGet();
        COUNTERS.get("misses").increment();
    }
    
    public static void recordEviction() {
        cacheEvictions.incrementAndGet();
        COUNTERS.get("evictions").increment();
    }
    
    public static void updateSize(int size) {
        cacheSize.set(size);
    }
    
    public static long getHits() {
        return cacheHits.get();
    }
    
    public static long getMisses() {
        return cacheMisses.get();
    }
    
    public static double getHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }
}

