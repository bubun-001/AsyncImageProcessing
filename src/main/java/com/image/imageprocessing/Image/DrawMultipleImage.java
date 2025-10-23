package com.image.imageprocessing.Image;

public class DrawMultipleImage {

    private static DrawMultipleImage instance;

    public static DrawMultipleImage getInstance() {
        if (instance == null) {
            instance = new DrawMultipleImage();
        }
        return instance;

    }
}
