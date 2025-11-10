package com.image.imageprocessing.processor.common;

import com.image.imageprocessing.Image.DrawMultipleImage;

import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;

public interface ImageProcessor {

    void processImage(BufferedImage image, int num, ImageFilter imageFilter, DrawMultipleImage drawFn);
}
