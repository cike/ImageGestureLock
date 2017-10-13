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
    private double angle;                               //相对于X轴正方向的夹角

    private final static double trianglePlace = 3f / 4f;        //三角形外切圆的中心位置

    /**
     * 计算三角形三个角的坐标
     * @return
     */
    public synchronized int computetriangleIndex() {
        double[] triangle1 = new double[2];
        double[] triangle2 = new double[2];
        //三角形外切圆的半径
        double circleRadius = getRadius() * (1f / 5f);
        //三角形其中一个角的位置
        double[] triangle0 = computeDistance((float) (getCenterX() + getRadius()), (float) getCenterY(), (float) (getRadius() * trianglePlace + circleRadius));
        //划线和三角形一条边的交点
        double[] triangleLine = computeDistance((float) (getCenterX() + getRadius()), (float) getCenterY(), (float) (getRadius() * trianglePlace - (1f / 2f) * circleRadius));
        double chazhi = circleRadius * Math.sqrt(3) / 2f;
        triangle1[0] = triangleLine[0];
        triangle1[1] = triangleLine[1] + chazhi;
        triangle2[0] = triangleLine[0];
        triangle2[1] = triangleLine[1] - chazhi;
        setTriangle0(triangle0);
        setTriangle1(triangle1);
        setTriangle2(triangle2);
        return 0;
    }

    /**
     * 计算手势划线和bean外圆的交点
     *
     * @param touchX
     * @param touchY
     * @return int[0]  横坐标的； int【1】 纵坐标
     */
    public double[] computeDistance(float touchX, float touchY) {
        return computeDistance(touchX, touchY, getRadius());
    }

    /**
     * 计算手势划线和bean外圆的交点
     *
     * @param touchX
     * @param touchY
     * @return int[0]  横坐标的； int【1】 纵坐标
     */
    public double[] computeDistance( float touchX, float touchY, float radius) {
        double[] indexArray = new double[2];
        float subx = touchX - getCenterX();
        float suby = touchY - getCenterY();
        double sinResult = (Math.abs((float) subx) / Math.sqrt(Math.pow(subx, 2) + Math.pow(suby, 2)));
        double xresult = (radius * sinResult);
        double yresult = Math.sqrt(Math.pow(radius, 2) - Math.pow(xresult, 2));
        if (subx >= 0) {
            indexArray[0] = getCenterX() + xresult;
        } else {
            indexArray[0] = getCenterX() - xresult;
        }
        if (suby >= 0) {
            indexArray[1] = getCenterY() + yresult;
        } else {
            indexArray[1] = getCenterY() - yresult;
        }
        return indexArray;
    }

    /**
     * 获取两条线的夹角
     *
     * @param centerX
     * @param centerY
     * @param xInView
     * @param yInView
     * @return
     */
    public float rotationBetweenLines(float centerX, float centerY, float xInView, float yInView) {
        double rotation = 0;

        double k2 = (double) (yInView - centerY) / (xInView - centerX);
        double tmpDegree = Math.atan((Math.abs(k2))) / Math.PI * 180;

        if (xInView > centerX && yInView < centerY) {  //第一象限
            rotation = tmpDegree;
        } else if (xInView > centerX && yInView > centerY) //第二象限
        {
            rotation = 90 + tmpDegree;
        } else if (xInView < centerX && yInView > centerY) { //第三象限
            rotation = 270 - tmpDegree;
        } else if (xInView < centerX && yInView < centerY) { //第四象限
            rotation = 270 + tmpDegree;
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 90;
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180;
        } else if (yInView == centerY && xInView < centerX) {
            rotation = 270;
        } else if (yInView == centerY && xInView > centerX) {
            rotation = 0;
        }
        setAngle(rotation);
        return (float) rotation;
    }


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

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
