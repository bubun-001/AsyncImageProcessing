package com.image.imageprocessing.processor.hybrid;

import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.Image.ImageData;
import com.image.imageprocessing.processor.common.ImageProcessor;
import com.image.imageprocessing.Filters.ImageFilter;
import java.awt.image.BufferedImage;
//import java.awt.image.ImageFilter;
import com.image.imageprocessing.Filters.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class HybridImageProcessor implements ImageProcessor {

    //Limited os threads for the heavy processing
    private final ExecutorService osExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2

    );
 @Override
    public void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn){

     try(ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()){
         int numHorizontalImages = image.getWidth()/num;
         int numVerticalImages = image.getHeight()/num;
         int totalTasks = numHorizontalImages + numVerticalImages;


         List<Future< ImageData>> futures = new ArrayList<>();
         AtomicLong totalTaskTimeMs = new AtomicLong(0);
         AtomicBoolean anyVirtual = new AtomicBoolean(false);

         long overallStartNs = System.nanoTime();

         for(int i=0;i<numHorizontalImages;i++){
             for(int j=0;j<numVerticalImages;j++){
                 int finalI =i;
                 int finalJ = j;

                 BufferedImage subImage = image.getSubimage(i*num, j*num, num , num);

                 //Each subimage task submission is run in a virtual thread
                 Future<ImageData>future = virtualExecutor.submit(()->{
                     if(Thread.currentThread().isVirtual()){
                         anyVirtual.set(true);
                     }
                 // providing the tasks to the actual os threads for the processing
                     return osExecutor.submit(()-> {
                         long startMs = System.currentTimeMillis();
                         BufferedImage result = imageFilter.filter(subImage);
                         ImageData imageData = new ImageData(result, finalI * num, finalJ * num, num, num);
                         drawFn.addImageToQueue(imageData);
                         long endMs = System.currentTimeMillis();
                         totalTaskTimeMs.addAndGet(endMs - startMs);
                         return imageData;

                     }).get();

                     });

                 futures.add(future);

             }
         }

         // After all the tasks get completed
         for(Future<ImageData>future : futures){
             try{
                future.get();
             } catch (Exception e) {
                 System.err.println("Error processing image: " + e.getMessage());
             }
         }
         long overallEndNs = System.nanoTime();
         long avgPerTaskMs = totalTasks > 0 ? totalTaskTimeMs.get() / totalTasks : 0;
         System.out.println("Threads Type\tNumber of Sub-Images\tAvg Time (ms)");
         System.out.println("Hybrid (Virtual + OS Threads)\t" + totalTasks + "\t" + avgPerTaskMs);

     } catch (Exception e) {
         e.printStackTrace();
     }
 }


}
