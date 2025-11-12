// com/image/imageprocessing/processor/hybrid/HybridImageProcessor.java
package com.image.imageprocessing.processor.hybrid;

import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.processor.common.ImageProcessor;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class HybridImageProcessor implements ImageProcessor {

    private final ExecutorService osExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
    );

    @Override
    public void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn) {
        try (ExecutorService virtualExec = Executors.newVirtualThreadPerTaskExecutor()) {
            int widthChunks = image.getWidth() / num;
            int heightChunks = image.getHeight() / num;
            int totalTasks = widthChunks * heightChunks;

            CompletionService<ImageData> completionService = new ExecutorCompletionService<>(virtualExec);
            AtomicLong totalTime = new AtomicLong(0);

            for (int i = 0; i < widthChunks; i++) {
                for (int j = 0; j < heightChunks; j++) {
                    int fi = i, fj = j;
                    BufferedImage sub = image.getSubimage(i * num, j * num, num, num);

                    completionService.submit(() ->
                            osExecutor.submit(() -> {
                                long t1 = System.currentTimeMillis();
                                BufferedImage result = imageFilter.filter(sub);
                                ImageData data = new ImageData(result, fi * num, fj * num, num, num);
                                drawFn.addImageToQueue(data);
                                totalTime.addAndGet(System.currentTimeMillis() - t1);
                                return data;
                            }).get()
                    );
                }
            }

            for (int k = 0; k < totalTasks; k++) {
                try { completionService.take(); } catch (InterruptedException ignored) {}
            }

            long avg = totalTasks > 0 ? totalTime.get() / totalTasks : 0;
            System.out.println("Threads: Hybrid (Virtual + OS) | Tasks: " + totalTasks + " | Avg: " + avg + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
