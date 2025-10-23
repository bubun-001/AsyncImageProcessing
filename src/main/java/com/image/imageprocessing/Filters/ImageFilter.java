package com.image.imageprocessing.Filters;

import java.awt.image.BufferedImage;

public interface ImageFilter {
    BufferedImage filter(BufferedImage image);
}
