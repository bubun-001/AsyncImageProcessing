package com.image.imageprocessing.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static javafx.scene.input.KeyCode.T;

public class FileImageIO implements ImageReadInf  {

    @Override
    public  <T> BufferedImage readImage(T src){

        try{
            String path = (String)src;
            File file = new File(path);
            return ImageIO.read(file);
        } catch (IOException e) {
            System.err.print("Not able to read the image");
            return null;
        }
    }

    @Override
    public void saveImage(BufferedImage src){
        //Todo later; ( used to store the image )
    }
}

