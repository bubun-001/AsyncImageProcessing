// com/image/imageprocessing/processor/hybrid/HybridImageProcessor.java
package com.image.imageprocessing.processor.hybrid;

import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.metrics.ProcessorMetrics;
import com.image.imageprocessing.processor.common.ImageProcessor;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class HybridImageProcessor implements ImageProcessor {

    private static final String PROCESSOR_TYPE = "hybrid";

    private final ExecutorService osExecutor = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1)
    );

    @Override
    public void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn) {
        try (ExecutorService virtualExec = Executors.newVirtualThreadPerTaskExecutor()) {
            int widthChunks = image.getWidth() / num;
            int heightChunks = image.getHeight() / num;
            int totalTasks = widthChunks * heightChunks;

            CompletionService<ImageData> completionService = new ExecutorCompletionService<>(virtualExec);
            AtomicLong totalTimeMs = new AtomicLong(0);

            for (int i = 0; i < widthChunks; i++) {
                for (int j = 0; j < heightChunks; j++) {
                    int fi = i, fj = j;
                    BufferedImage sub = image.getSubimage(i * num, j * num, num, num);

                    completionService.submit(() ->
                            osExecutor.submit(() -> {
                                long startNs = System.nanoTime();
                                BufferedImage result = imageFilter.filter(sub);
                                ImageData data = new ImageData(result, fi * num, fj * num, num, num);
                                drawFn.addImageToQueue(data);
                                long durationNs = System.nanoTime() - startNs;
                                totalTimeMs.addAndGet(TimeUnit.NANOSECONDS.toMillis(durationNs));
                                ProcessorMetrics.recordTile(PROCESSOR_TYPE, durationNs);
                                return data;
                            }).get()
                    );
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

            System.out.println("Threads Type\tNumber of Sub-Images\tAvg Time (ms)");
            System.out.println("Hybrid (Virtual + OS)\t" + totalTasks + "\t" + avg);
        } catch (Exception e) {
            throw new IllegalStateException("Hybrid processing failed", e);
        }
    }
}
