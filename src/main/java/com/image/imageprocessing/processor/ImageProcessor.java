package com.image.imageprocessing.processor;

import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageProcessor {

    private ExecutorService executorService;
    private DrawMultipleImage drawMultipleImage;

   ImageProcessor(){
       executorService = Executors.newFixedThreadPool(100);
       drawMultipleImage =DrawMultipleImage.getInstance();
   }
   public void processImage(BufferedImage image , int num , ImageFilter imageFilter){
        int numHorizontalImages = image.getWidth() / num;
        int numVerticalImages = image.getHeight()/num;

         for(int i = 0; i < numHorizontalImages;i++){
             for(int j=0; j< numVerticalImages;j++){
                 BufferedImage subImage = image.getSubimage(i*num , j*num , num , num);

             }
         }

   }

}
