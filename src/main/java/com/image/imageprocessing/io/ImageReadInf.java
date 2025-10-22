package com.image.imageprocessing.io;
import java.awt.image.BufferedImage;

public interface ImageReadInf {

    <T>BufferedImage readImage(T src); // T can be any input type like file , url , stream etc

    void saveImage(BufferedImage image );
}
