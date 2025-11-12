package com.image.imageprocessing;

import com.image.imageprocessing.Filters.GreyScaleFilter;
import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.io.ImageRead;
import com.image.imageprocessing.io.ImageReadInf;

// choose which processor you want
import com.image.imageprocessing.processor.common.ImageProcessor;
import com.image.imageprocessing.processor.os.OSImageProcessor;
import com.image.imageprocessing.processor.Virtual.VirtualImageProcessor;
import com.image.imageprocessing.processor.hybrid.HybridImageProcessor;

import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //  Read image from disk
        ImageReadInf imageRead = new ImageRead();
        BufferedImage image = imageRead.readImage(
                "/Users/lalaamartyachand/IdeaProjects/ImageProcessing/src/main/java/com/image/imageprocessing/Image/test1.jpg"
        );

        // Initialize drawing utility (shared canvas)
        DrawMultipleImage drawMultipleImage = DrawMultipleImage.getInstance();
        drawMultipleImage.initialize(stage);

        // Choose filter
        ImageFilter imageFilter = new GreyScaleFilter();

        // Choose which processor to test
        // You can switch these lines to compare performance:
        // ImageProcessor processor = new OSImageProcessor();
         ImageProcessor processor = new VirtualImageProcessor();
          // ImageProcessor processor = new HybridImageProcessor();

        // Process the image
        processor.processImage(image, 10, imageFilter, drawMultipleImage);
    }

    public static void main(String[] args) {
        launch();
    }
}
