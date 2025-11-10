package com.image.imageprocessing.processor.hybrid;

import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.processor.common.ImageProcessor;

import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.nio.Buffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HybridImageProcessor implements ImageProcessor {

    //Limited os threads for the heavy processing
    private final ExecutorService osExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2

    );
 @Override
    public void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn){

     try(ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()){

     }
 }


}
