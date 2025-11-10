package com.image.imageprocessing;

import com.image.imageprocessing.Filters.GreyScaleFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.io.ImageRead;
import com.image.imageprocessing.io.ImageReadInf;
import com.image.imageprocessing.processor.common.ImageProcessor;
import javafx.application.Application;
import javafx.stage.Stage;
import com.image.imageprocessing.Filters.ImageFilter;


import java.awt.image.BufferedImage;
//import java.awt.image.ImageFilter;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        ImageReadInf imageRead = new ImageRead();
        BufferedImage image = imageRead.readImage("/Users/lalaamartyachand/IdeaProjects/ImageProcessing/src/main/java/com/image/imageprocessing/Image/test.jpg");
        DrawMultipleImage drawMultipleImage = DrawMultipleImage.getInstance();
        drawMultipleImage.initialize(stage);
        ImageProcessor processor = new ImageProcessor();
        ImageFilter imageFilter = new GreyScaleFilter();
        processor.processImage(image, 10,imageFilter , drawMultipleImage);


    }

    public static void main(String[] args){
        launch();
    }
}
