package com.example.cike.imagegesturelockview;

/**
 * Created by cike on 2017/10/10.
 */
public class ImageGestureCircleBean {
    private int centerX;                //圆心横坐标
    private int centerY;                //圆心
    private float radius;               //圆半径
    private Integer number;             //所代表数值
    private boolean select = false;             //是否选中
    private String centerImgSrc;                //中心图片uri
    private int imgX;                   //中心图片的坐标
    private int imgY;                   //中心图片的纵坐标
    private float imageWidth;           //图片宽度（宽度和长度相同）

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getCenterImgSrc() {
        return centerImgSrc;
    }

    public void setCenterImgSrc(String centerImgSrc) {
        this.centerImgSrc = centerImgSrc;
    }

    public int getImgX() {
        return imgX;
    }

    public void setImgX(int imgX) {
        this.imgX = imgX;
    }

    public int getImgY() {
        return imgY;
    }

    public void setImgY(int imgY) {
        this.imgY = imgY;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(float imageWidth) {
        this.imageWidth = imageWidth;
    }
}
