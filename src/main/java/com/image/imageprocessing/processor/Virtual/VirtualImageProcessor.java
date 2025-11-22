package com.image.imageprocessing.processor.Virtual;

import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.cache.TileCache;
import com.image.imageprocessing.cache.TileKey;
import com.image.imageprocessing.events.EventBus;
import com.image.imageprocessing.events.processing.ProcessingCompleteEvent;
import com.image.imageprocessing.events.processing.ProcessingErrorEvent;
import com.image.imageprocessing.events.processing.ProcessingStartedEvent;
import com.image.imageprocessing.events.processing.TileLoadedFromCacheEvent;
import com.image.imageprocessing.events.processing.TileProcessedEvent;
import com.image.imageprocessing.metrics.ProcessorMetrics;
import com.image.imageprocessing.processor.common.ImageProcessor;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class VirtualImageProcessor implements ImageProcessor {

    private static final String PROCESSOR_TYPE = "virtual";
    private final TileCache cache = TileCache.getInstance();
    private final EventBus eventBus = EventBus.getInstance();

    @Override
    public void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn) {
        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())) {
            int widthChunks = image.getWidth() / num;
            int heightChunks = image.getHeight() / num;
            int totalTasks = widthChunks * heightChunks;
            String imageId = Integer.toHexString(System.identityHashCode(image));
            long imageVersion = image.getRaster().hashCode();
            eventBus.publish(new ProcessingStartedEvent(PROCESSOR_TYPE, imageId, totalTasks));

            CompletionService<ImageData> completionService = new ExecutorCompletionService<>(executor);
            AtomicLong totalTimeMs = new AtomicLong(0);

            for (int i = 0; i < widthChunks; i++) {
                for (int j = 0; j < heightChunks; j++) {
                    int fi = i;
                    int fj = j;
                    BufferedImage sub = image.getSubimage(i * num, j * num, num, num);

                    completionService.submit(() -> {
                        long startNs = System.nanoTime();

                        TileKey cacheKey = new TileKey(
                                imageId,
                                imageVersion,
                                fi,
                                fj,
                                num,
                                imageFilter.getClass().getName(),
                                PROCESSOR_TYPE
                        );
                        ImageData cachedData = cache.get(cacheKey);
                        if (cachedData != null) {
                            drawFn.addImageToQueue(cachedData);
                            eventBus.publish(new TileLoadedFromCacheEvent(cacheKey, cachedData));
                            return cachedData;
                        }
                        BufferedImage result = imageFilter.filter(sub);
                        ImageData data = new ImageData(result, fi * num, fj * num, num, num);
                        cache.put(cacheKey, data);
                        drawFn.addImageToQueue(data);
                        long durationNs = System.nanoTime() - startNs;
                        totalTimeMs.addAndGet(TimeUnit.NANOSECONDS.toMillis(durationNs));
                        ProcessorMetrics.recordTile(PROCESSOR_TYPE, durationNs);
                         eventBus.publish(new TileProcessedEvent(cacheKey, data, durationNs));
                        return data;
                    });
                }
            }

            for (int k = 0; k < totalTasks; k++) {
                try {
                    completionService.take();
                } catch (InterruptedException ignored) {
                }
            }

            long totalMs = totalTimeMs.get();
            long avg = totalTasks > 0 ? totalMs / totalTasks : 0;
            ProcessorMetrics.recordSummary(PROCESSOR_TYPE, totalTasks, totalMs);
            eventBus.publish(new ProcessingCompleteEvent(PROCESSOR_TYPE, totalTasks, totalMs, avg));

            System.out.println("Threads Type\tNumber of Sub-Images\tAvg Time (ms)");
            System.out.println("Virtual Threads\t" + totalTasks + "\t" + avg);
        } catch (Exception e) {
            eventBus.publish(new ProcessingErrorEvent(PROCESSOR_TYPE, e));
            throw new IllegalStateException("Virtual processing failed", e);
        }
    }
}
