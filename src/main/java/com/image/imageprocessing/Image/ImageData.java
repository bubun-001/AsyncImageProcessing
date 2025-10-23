package com.image.imageprocessing.Image;

import java.awt.image.BufferedImage;

public class ImageData {


    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
    public BufferedImage getImage() {
        return image;
    }



    public ImageData(BufferedImage image, int i, int j, int x, int y, int totalNum) {
        this.image = image;
        this.i = i;
        this.j = j;
        this.x = x;
        this.y = y;
        this.totalNum = totalNum;
    }
    private int i , j , x , y , totalNum;
    private BufferedImage image;





}
