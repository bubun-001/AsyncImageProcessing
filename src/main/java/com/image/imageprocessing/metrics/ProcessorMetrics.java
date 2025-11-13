package com.image.imageprocessing.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class ProcessorMetrics {

    private static final Map<String, Timer> TILE_TIMERS = new ConcurrentHashMap<>();
    private static final Map<String, Counter> TILE_COUNTERS = new ConcurrentHashMap<>();
    private static final Map<String, Counter> RUN_COUNTERS = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> AVG_TIME_GAUGES = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> TASK_COUNT_GAUGES = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> TOTAL_TIME_GAUGES = new ConcurrentHashMap<>();

    private ProcessorMetrics() {
    }

    public static void recordTile(String processorType, long durationNanos) {
        timer(processorType).record(durationNanos, TimeUnit.NANOSECONDS);
        tileCounter(processorType).increment();
    }

    public static void recordSummary(String processorType, int totalTasks, long totalTimeMs) {
        runCounter(processorType).increment();
        avgGauge(processorType).set(totalTasks > 0 ? totalTimeMs / totalTasks : 0);
        tasksGauge(processorType).set(totalTasks);
        totalTimeGauge(processorType).set(totalTimeMs);
    }

    private static Timer timer(String processorType) {
        return TILE_TIMERS.computeIfAbsent(processorType, key -> Timer.builder("image_processing_tile_duration")
                .description("Time spent processing individual image tiles")
                .tag("processor", key)
                .register(MetricsRegistry.registry()));
    }

    private static Counter tileCounter(String processorType) {
        return TILE_COUNTERS.computeIfAbsent(processorType, key -> Counter.builder("image_processing_tiles_total")
                .description("Number of tiles processed")
                .tag("processor", key)
                .register(MetricsRegistry.registry()));
    }

    private static Counter runCounter(String processorType) {
        return RUN_COUNTERS.computeIfAbsent(processorType, key -> Counter.builder("image_processing_runs_total")
                .description("Number of image processing runs")
                .tag("processor", key)
                .register(MetricsRegistry.registry()));
    }

    private static AtomicLong avgGauge(String processorType) {
        return AVG_TIME_GAUGES.computeIfAbsent(processorType, key -> registerGauge(
                "image_processing_last_avg_ms",
                "Rolling average processing time per tile in the most recent run",
                key));
    }

    private static AtomicLong tasksGauge(String processorType) {
        return TASK_COUNT_GAUGES.computeIfAbsent(processorType, key -> registerGauge(
                "image_processing_last_tile_count",
                "Number of tiles processed during the most recent run",
                key));
    }

    private static AtomicLong totalTimeGauge(String processorType) {
        return TOTAL_TIME_GAUGES.computeIfAbsent(processorType, key -> registerGauge(
                "image_processing_last_total_time_ms",
                "Total processing time (sum of tile durations) during the most recent run",
                key));
    }

    private static AtomicLong registerGauge(String gaugeName, String description, String processorType) {
        AtomicLong holder = new AtomicLong(0);
        Gauge.builder(gaugeName, holder, AtomicLong::doubleValue)
                .description(description)
                .tag("processor", processorType)
                .register(MetricsRegistry.registry());
        return holder;
    }
}
