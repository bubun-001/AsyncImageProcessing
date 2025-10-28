package com.image.imageprocessing.processor;

import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.Image.ImageData;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class ImageProcessor {

    private ExecutorService executorService;
    private DrawMultipleImage drawFn;

    public ImageProcessor(){
        // Use virtual threads for better performance and resource utilization
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn){
        int numHorizontalImages = image.getWidth() / num;
        int numVerticalImages = image.getHeight() / num;
        int totalTasks = numHorizontalImages * numVerticalImages;

        System.out.println("Starting image processing with virtual threads...");
        System.out.println("Total tasks: " + totalTasks);
        System.out.println("Using virtual threads: " + Thread.currentThread().isVirtual());

        List<Future<ImageData>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i<numHorizontalImages; i++){
            for(int j=0; j<numVerticalImages; j++){
                BufferedImage subImage = image.getSubimage(i*num, j*num, num, num);
                int finalI = i;
                int finalJ = j;
                Future<ImageData> future = executorService.submit(new Callable<ImageData>() {
                    @Override
                    public ImageData call(){
                        long taskStart = System.currentTimeMillis();
                        String threadName = Thread.currentThread().getName();
                        boolean isVirtual = Thread.currentThread().isVirtual();
                        
                        BufferedImage result = imageFilter.filter(subImage);
                        ImageData imageData = new ImageData(result, finalI *num, finalJ *num, num, num);
                        
                        long taskEnd = System.currentTimeMillis();
                        System.out.println(String.format("Task (%d,%d) completed by %s (Virtual: %s) in %d ms", 
                            finalI, finalJ, threadName, isVirtual, taskEnd - taskStart));
                        
                        // Add to queue immediately when processing is complete
                        drawFn.addImageToQueue(imageData);
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

        long endTime = System.currentTimeMillis();
        System.out.println(String.format("All %d tasks completed in %d ms using virtual threads", 
            totalTasks, endTime - startTime));
    }

}