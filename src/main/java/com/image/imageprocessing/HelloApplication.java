package com.image.imageprocessing;

import com.image.imageprocessing.Filters.GreyScaleFilter;
import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.io.ImageRead;
import com.image.imageprocessing.io.ImageReadInf;
import com.image.imageprocessing.events.EventBus;
import com.image.imageprocessing.events.cache.CacheStatsEvent;
import com.image.imageprocessing.events.cache.CacheEvictionEvent;
import com.image.imageprocessing.events.cache.CacheCapacityReachedEvent;
import com.image.imageprocessing.events.processing.TileLoadedFromCacheEvent;
import com.image.imageprocessing.events.processing.ProcessingStartedEvent;
import com.image.imageprocessing.events.processing.ProcessingCompleteEvent;
import com.image.imageprocessing.metrics.CacheMetrics;
import com.image.imageprocessing.metrics.MetricsServer;
import com.image.imageprocessing.processor.Virtual.VirtualImageProcessor;
import com.image.imageprocessing.processor.common.ImageProcessor;
import com.image.imageprocessing.processor.hybrid.HybridImageProcessor;
import com.image.imageprocessing.processor.os.OSImageProcessor;
import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        MetricsServer.start(9100);

        // Setup event listeners to verify events are working
        setupEventListeners();

        // Read image from the disk
        ImageReadInf imageRead = new ImageRead();
        BufferedImage image = imageRead.readImage(
                "/Users/lalaamartyachand/IdeaProjects/ImageProcessing/src/main/java/com/image/imageprocessing/Image/test1.jpg");

        // Initialize drawing utility (the shared canvas)
        DrawMultipleImage drawMultipleImage = DrawMultipleImage.getInstance();
        drawMultipleImage.initialize(stage);

        // Choose filter
        ImageFilter imageFilter = new GreyScaleFilter();

        String processorMode = System.getProperty("processor.mode", "virtual").toLowerCase();
        ImageProcessor processor = switch (processorMode) {
            case "os" -> new OSImageProcessor();
            case "hybrid" -> new HybridImageProcessor();
            case "virtual" -> new VirtualImageProcessor();
            default -> throw new IllegalArgumentException("Unsupported processor.mode: " + processorMode);
        };
        System.out.println("Using processor mode: " + processorMode);

        // Process the image
        processor.processImage(image, 10, imageFilter, drawMultipleImage);
    }

    private void setupEventListeners() {
        EventBus eventBus = EventBus.getInstance();

        // Cache statistics events
        eventBus.subscribe(CacheStatsEvent.class, event -> {
            System.out.printf("✓ [CACHE STATS] Hits: %d, Misses: %d, Hit Rate: %.2f%%%n",
                    event.hitCount(), event.missCount(), event.hitRate() * 100);
        });

        eventBus.subscribe(CacheEvictionEvent.class, event -> {
            System.out.printf("✓ [CACHE EVICTION] Evicted tile at (%d, %d)%n",
                    event.key().tileX(), event.key().tileY());
        });

        eventBus.subscribe(CacheCapacityReachedEvent.class, event -> {
            System.out.printf("✓ [CACHE CAPACITY] Reached max capacity: %d tiles%n", event.maxSize());
        });

        eventBus.subscribe(TileLoadedFromCacheEvent.class, event -> {
            // Only print occasionally to avoid spam (every 100th hit)
            if (CacheMetrics.getHits() % 100 == 0) {
                System.out.printf("✓ [CACHE HIT] Loaded tile from cache at (%d, %d) - Total hits: %d%n",
                        event.key().tileX(), event.key().tileY(), CacheMetrics.getHits());
            }
        });

        // Processing events
        eventBus.subscribe(ProcessingStartedEvent.class, event -> {
            System.out.printf("✓ [PROCESSING STARTED] Processor: %s, Image: %s, Total Tiles: %d%n",
                    event.processorType(), event.imageId(), event.totalTiles());
        });

        eventBus.subscribe(ProcessingCompleteEvent.class, event -> {
            System.out.printf("✓ [PROCESSING COMPLETE] Processor: %s, Tiles: %d, Total Time: %d ms, Avg: %d ms%n",
                    event.processorType(), event.totalTiles(), event.totalTimeMs(), event.averageMs());
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
