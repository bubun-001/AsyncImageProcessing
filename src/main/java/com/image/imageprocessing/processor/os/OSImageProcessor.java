package com.image.imageprocessing.processor.os;

import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.processor.common.ImageProcessor;
import com.image.imageprocessing.Image.*;
import java.awt.image.BufferedImage;
import com.image.imageprocessing.Filters.*;
import java.awt.image.ImageFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class OSImageProcessor implements ImageProcessor {


    private ExecutorService executorService;
    private DrawMultipleImage drawFn;

    public OSImageProcessor(){
        executorService = Executors.newFixedThreadPool(100);
    }

   @Override
    public void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn){
        int numHorizontalImages = image.getWidth() / num;
        int numVerticalImages = image.getHeight() / num;
        int totalTasks = numHorizontalImages * numVerticalImages;

        List<Future<ImageData>> futures = new ArrayList<>();
        AtomicLong totalTaskTimeMs = new AtomicLong(0);
        AtomicBoolean anyVirtual = new AtomicBoolean(false);

        long overallStartNs = System.nanoTime();

        for (int i = 0; i<numHorizontalImages; i++){
            for(int j=0; j<numVerticalImages; j++){
                BufferedImage subImage = image.getSubimage(i*num, j*num, num, num);
                int finalI = i;
                int finalJ = j;
                Future<ImageData> future = executorService.submit(new Callable<ImageData>() {
                    @Override
                    public ImageData call(){
                        long startMs = System.currentTimeMillis();
                        if (Thread.currentThread().isVirtual()) {
                            anyVirtual.set(true);
                        }
                        BufferedImage result = imageFilter.filter(subImage);
                        ImageData imageData = new ImageData(result, finalI *num, finalJ *num, num, num);
                        // Add to queue immediately when processing is complete
                        drawFn.addImageToQueue(imageData);
                        long endMs = System.currentTimeMillis();
                        totalTaskTimeMs.addAndGet(endMs - startMs);
                        return imageData;
                    }
                });
                futures.add(future);
            }
        }

        for (Future<ImageData> future : futures) {
            try {
                future.get();
            } catch (Exception ex) {
                System.err.println("Error processing image: " + ex.getMessage());
            }
        }

        long overallEndNs = System.nanoTime();
        long avgPerTaskMs = totalTasks > 0 ? totalTaskTimeMs.get() / totalTasks : 0;
        String threadType = anyVirtual.get() ? "Virtual Threads" : "OS Threads";

        System.out.println("Threads Type\tNumber of Sub-Images\tAvg Time (ms)");
        System.out.println(threadType + "\t" + totalTasks + "\t" + avgPerTaskMs);

    }
}
