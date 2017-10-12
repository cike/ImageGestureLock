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
    private int imageWidth;           //图片宽度（宽度和长度相同）
    private double[] inter = new double[2];           //圆和手指划线的交点
    private double[] triangle0 = new double[2];       //三角形其中一角坐标
    private double[] triangle1 = new double[2];       //
    private double[] triangle2 = new double[2];

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

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public double[] getInter() {
        return inter;
    }

    public double[] getTriangle0() {
        return triangle0;
    }

    public void setTriangle0(double[] triangle0) {
        this.triangle0 = triangle0;
    }

    public double[] getTriangle1() {
        return triangle1;
    }

    public void setTriangle1(double[] triangle1) {
        this.triangle1 = triangle1;
    }

    public double[] getTriangle2() {
        return triangle2;
    }

    public void setTriangle2(double[] triangle2) {
        this.triangle2 = triangle2;
    }

    public void setInter(double[] inter) {
        this.inter = inter;
    }

}
