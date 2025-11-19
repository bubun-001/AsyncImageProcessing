package com.image.imageprocessing;

import com.image.imageprocessing.Filters.GreyScaleFilter;
import com.image.imageprocessing.Filters.ImageFilter;
import com.image.imageprocessing.Image.DrawMultipleImage;
import com.image.imageprocessing.io.ImageRead;
import com.image.imageprocessing.io.ImageReadInf;
import com.image.imageprocessing.metrics.MetricsServer;
import com.image.imageprocessing.processor.Virtual.VirtualImageProcessor;
import com.image.imageprocessing.processor.common.ImageProcessor;
import com.image.imageprocessing.processor.hybrid.HybridImageProcessor;
import com.image.imageprocessing.processor.os.OSImageProcessor;
import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        MetricsServer.start(9100);

        //  Read image from the disk
        ImageReadInf imageRead = new ImageRead();
        BufferedImage image = imageRead.readImage(
                "/Users/lalaamartyachand/IdeaProjects/ImageProcessing/src/main/java/com/image/imageprocessing/Image/test1.jpg"
        );

        // Initialize drawing utility (the shared canvas)
        DrawMultipleImage drawMultipleImage = DrawMultipleImage.getInstance();
        drawMultipleImage.initialize(stage);

        // Choose filter
        ImageFilter imageFilter = new GreyScaleFilter();

        String processorMode = System.getProperty("processor.mode", "virtual").toLowerCase();
        ImageProcessor processor = switch (processorMode) {
            case "os" -> new OSImageProcessor();
            case "hybrid" -> new HybridImageProcessor();
            case "virtual" -> new VirtualImageProcessor();
            default -> throw new IllegalArgumentException("Unsupported processor.mode: " + processorMode);
        };
        System.out.println("Using processor mode: " + processorMode);

        // Process the image
        processor.processImage(image, 10, imageFilter, drawMultipleImage);
    }

    public static void main(String[] args) {
        launch();
    }
}
