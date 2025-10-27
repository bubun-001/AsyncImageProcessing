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

import static java.lang.reflect.Array.get;

public class ImageProcessor {

    private ExecutorService executorService;
    private DrawMultipleImage drawMultipleImage;

 public  ImageProcessor(){
       executorService = Executors.newFixedThreadPool(100);
       drawMultipleImage =DrawMultipleImage.getInstance();
   }
   public void processImage(BufferedImage image , int num , ImageFilter imageFilter){
        int numHorizontalImages = image.getWidth() / num;
        int numVerticalImages = image.getHeight()/num;

        List<Future<ImageData>> futures = new ArrayList<>();
         for(int i = 0; i < numHorizontalImages;i++){
             for(int j=0; j< numVerticalImages;j++){
                 BufferedImage subImage = image.getSubimage(i*num , j*num , num , num);
                 int finalI = i;
                 int finalJ = j;
                 Future<ImageData>future = executorService.submit(new Callable<ImageData>() {

                     @Override
                    public ImageData call(){
                         long subStart = System.currentTimeMillis();
                       BufferedImage result =   imageFilter.filter(subImage);
                       ImageData imageData =  new ImageData(result , finalI *num , finalJ *num , num , num);
                         drawMultipleImage.addImageToQueue(imageData);
                         long subEnd = System.currentTimeMillis();
                         System.out.println("Sub-image (" + finalI + "," + finalJ + ") processed by "
                                 + Thread.currentThread() + " in " + (subEnd - subStart) + " ms");
                         return imageData;
                     }
                 });
                 futures.add(future);

             }
         }






   }




}
