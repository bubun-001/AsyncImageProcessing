package com.image.imageprocessing.Filters;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GreyScaleFilter implements ImageFilter {
    public GreyScaleFilter() {

    }

    @Override
    public BufferedImage filter(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                Color color = new Color(rgb, true);

                int gray = (int)(0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());

                Color grayColor = new Color(gray, gray, gray, color.getAlpha());
                grayscale.setRGB(x, y, grayColor.getRGB());
            }
        }

        return grayscale;
    }
}
