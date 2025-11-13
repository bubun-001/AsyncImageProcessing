package com.image.imageprocessing.metrics;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

public final class MetricsRegistry {

    private static final PrometheusMeterRegistry REGISTRY = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    private MetricsRegistry() {
    }

    public static PrometheusMeterRegistry registry() {
        return REGISTRY;
    }
}
