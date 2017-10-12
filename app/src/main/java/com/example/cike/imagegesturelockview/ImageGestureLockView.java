package com.example.cike.imagegesturelockview;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by cike on 2017/10/10.
 */
public class ImageGestureLockView extends View {
    private Paint normalPaint;              //正常圆画笔
    private Paint touchPaint;               //触摸状画笔
    private Paint linePaint;                //画线笔
    private Paint trianglePaint;            //画三角形
    private ImageGestureCircleBean[] matrix;        //关键点位置
    private Bitmap bitmapBuffer;                //画布承载图
    private Canvas bitmapBufferCanvas;          //承载图画布
    private Bitmap centerBitmap;
    private int centerImgWidth;                 //中心图片宽度
    private int unitSize;                       //组件宽度的1/6
    private Path tempPath;                      //存储路径, 之前绘制的路径
    private Path mPath;                         //用户绘制的线
    private boolean isStart = false;            //标识是否开始绘制手势
    private Queue<ImageGestureCircleBean> selectedQueue;        //存储已经选择的圆
    private GestureDrawLisenter gestureDrawLisenter;        //会话监听接口
    private ImageGestureCircleBean lastBean;                //上一个被选中的bean
    private final static double trianglePlace = 3f/4f;        //圆内三角形中心所在的位置

    public ImageGestureLockView(Context context) {
        super(context);
        init();
    }

    public ImageGestureLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public ImageGestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        unitSize = 0;
        if (matrix == null && (unitSize = w / 6) > 0) {
            matrix = new ImageGestureCircleBean[9];
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++) {
                    ImageGestureCircleBean bean = new ImageGestureCircleBean();
                    bean.setNumber(i * 3 + j);
                    bean.setCenterX(unitSize * (j * 2 + 1));
                    bean.setCenterY(unitSize * (i * 2 + 1));
                    bean.setRadius(unitSize * 0.5f);
                    bean.setImgX((int) (bean.getCenterX() - (bean.getRadius() / 2) / Math.sqrt(2)));
                    bean.setImgY((int) (bean.getCenterY() - (bean.getRadius() / 2) / Math.sqrt(2)));
                    bean.setImageWidth((int) (((bean.getRadius() / 2) / Math.sqrt(2)) * 2));
                    matrix[i * 3 + j] = bean;
                }
        }
        //中心图片宽度
        centerImgWidth = (int) matrix[0].getImageWidth();

        bitmapBuffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapBufferCanvas = new Canvas(bitmapBuffer);
        bitmapBufferCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        //获取压缩后中心图片
        centerBitmap = getScaledCenterBitmap();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * 初始化笔和canvas
     */
    private void init() {
        //设置画圆的画笔
        normalPaint = new Paint();
        normalPaint.setAntiAlias(true);
        normalPaint.setFilterBitmap(true);
        normalPaint.setStyle(Paint.Style.STROKE);
        normalPaint.setColor(Color.parseColor("#d2d2d2"));
        normalPaint.setStrokeJoin(Paint.Join.ROUND);
        normalPaint.setStrokeCap(Paint.Cap.ROUND);
        normalPaint.setStrokeWidth(2f);
        normalPaint.setDither(true);

        //设置选中状态画笔
        touchPaint = new Paint();
        touchPaint.setAntiAlias(true);
        touchPaint.setFilterBitmap(true);
        touchPaint.setStyle(Paint.Style.STROKE);
        touchPaint.setStrokeJoin(Paint.Join.ROUND);
        touchPaint.setStrokeCap(Paint.Cap.ROUND);
        touchPaint.setColor(Color.parseColor("#df4400"));
        touchPaint.setStrokeWidth(2f);
        touchPaint.setDither(true);

        //设置画线笔
        linePaint = new Paint(touchPaint);
        linePaint.setStrokeWidth(4f);

        /**
         * 设置画三角形的画笔
         */
        trianglePaint = new Paint();
        trianglePaint.setAntiAlias(true);
        trianglePaint.setFilterBitmap(true);
        trianglePaint.setStyle(Paint.Style.FILL);
        trianglePaint.setStrokeJoin(Paint.Join.ROUND);
        trianglePaint.setStrokeCap(Paint.Cap.ROUND);
        trianglePaint.setColor(Color.parseColor("#df4400"));
        trianglePaint.setStrokeWidth(2f);
        trianglePaint.setDither(true);


        tempPath = new Path();
        mPath = new Path();
        selectedQueue = new LinkedBlockingQueue<>();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        bitmapBufferCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        for (int i = 0; i < matrix.length; i++) {
            drawCircleImage(matrix[i]);
        }
        canvas.drawBitmap(bitmapBuffer, 0, 0, touchPaint);
        canvas.drawPath(mPath, linePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ImageGestureCircleBean bean = checkInImgCircle((int) event.getX(), (int) event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (gestureDrawLisenter != null)
                    gestureDrawLisenter.onStart();
                if (bean != null) {
                    lastBean = bean;                //上一个bean
                    bean.setSelect(true);           //设置圆圈被选中
                    selectedQueue.add(bean);
                    isStart = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isStart) {
                    mPath.reset();
                    double[] lastInterArray = new double[2];
                    if (bean != null && !bean.isSelect()) {
                        bean.setSelect(true);
                        if (lastBean != null) {
                            lastInterArray = computeDistance(lastBean, bean.getCenterX(), bean.getCenterY());
                            //设置交点位置
                            lastBean.setInter(lastInterArray);
                            compute2(lastBean);
                            tempPath.moveTo((float) lastInterArray[0], (float) lastInterArray[1]);
                            double[] interArray = computeDistance(bean, lastBean.getCenterX(), lastBean.getCenterY());
                            tempPath.lineTo((float) interArray[0], (float) interArray[1]);
                        }
                        lastBean = bean;
                        selectedQueue.add(bean);
                    } else {
                        lastInterArray = computeDistance(lastBean, event.getX(), event.getY());
                        tempPath.moveTo((float) lastInterArray[0], (float) lastInterArray[1]);
                    }
                    mPath.addPath(tempPath);
                    mPath.moveTo((float) lastInterArray[0], (float) lastInterArray[1]);
                    mPath.lineTo((int) event.getX(), (int) event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                isStart = false;                //标识此时路径绘制结束
                String gestureResult = this.reset();
                if (gestureDrawLisenter != null)
                    gestureDrawLisenter.onFinish(gestureResult);
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 保证组件是方的
         */
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width <= height) {
            setMeasuredDimension(width, width);
        } else {
            setMeasuredDimension(height, height);
        }
    }

    private String reset() {
        StringBuffer resultBuffer = new StringBuffer();
        ImageGestureCircleBean bean;
        while (selectedQueue != null && !selectedQueue.isEmpty()) {
            bean = selectedQueue.poll();
            if (bean != null) {
                bean.setSelect(false);
                resultBuffer.append(bean.getNumber());
            }
        }
        mPath.reset();
        tempPath.reset();
        lastBean = null;
        return resultBuffer.toString();
    }

    /**
     * 画圆和图片
     *
     * @param bean
     */
    private void drawCircleImage(ImageGestureCircleBean bean) {
        bitmapBufferCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        if (bean.isSelect()) {          //选中状态
            /**
             * 绘制外圆和中心图片
             */
            bitmapBufferCanvas.drawBitmap(centerBitmap, bean.getImgX(), bean.getImgY(), null);
            bitmapBufferCanvas.drawCircle(bean.getCenterX(), bean.getCenterY(), bean.getRadius(), touchPaint);
            Path path = new Path();
            path.moveTo((float) bean.getTriangle0()[0], (float) bean.getTriangle0()[1]);
            path.lineTo((float) bean.getTriangle1()[0], (float) bean.getTriangle1()[1]);
            path.lineTo((float) bean.getTriangle2()[0], (float) bean.getTriangle2()[1]);
            path.close();
            bitmapBufferCanvas.save();
            bitmapBufferCanvas.rotate((float) Math.toDegrees(bean.getAngle()), bean.getCenterX(), bean.getCenterY());
            bitmapBufferCanvas.drawPath(path, trianglePaint);
            bitmapBufferCanvas.restore();
        } else {                        //未选中状态
            bitmapBufferCanvas.drawCircle(bean.getCenterX(), bean.getCenterY(), bean.getRadius(), normalPaint);
        }
    }

    /**
     * 画线
     */
    private void drawLine() {
    }


    /**
     * 计算手势划线和bean外圆的交点
     *
     * @param bean
     * @param touchX
     * @param touchY
     * @return int[0]  横坐标的； int【1】 纵坐标
     */
    private double[] computeDistance(ImageGestureCircleBean bean, float touchX, float touchY) {
        return computeDistance(bean, touchX, touchY, bean.getRadius());
    }

    /**
     * 计算手势划线和bean外圆的交点
     *
     * @param bean
     * @param touchX
     * @param touchY
     * @return int[0]  横坐标的； int【1】 纵坐标
     */
    private double[] computeDistance(ImageGestureCircleBean bean, float touchX, float touchY, float radius) {
        double[] indexArray = new double[2];
        float subx = touchX - bean.getCenterX();
        float suby = touchY - bean.getCenterY();
        double sinResult = (Math.abs((float) subx) / Math.sqrt(Math.pow(subx, 2) + Math.pow(suby, 2)));
        double angle = Math.toDegrees(Math.asin(sinResult));
        double xresult = (radius * sinResult);
        double yresult = Math.sqrt(Math.pow(radius, 2) - Math.pow(xresult, 2));
        if (subx >= 0) {
            indexArray[0] = bean.getCenterX() + xresult;
            //计算旋转角度
            if(suby < 0) {
                angle = -angle;
            }
        } else {
            indexArray[0] = bean.getCenterX() - xresult;
            //计算旋转角度
            if(suby >= 0) {
                angle = 180 - angle;
            } else {
                angle = -(180 - angle);
            }
        }
        if (suby >= 0) {
            indexArray[1] = bean.getCenterY() + yresult;
        } else {
            indexArray[1] = bean.getCenterY() - yresult;
        }
        bean.setAngle(angle);
        return indexArray;
    }


    private double getangle(ImageGestureCircleBean bean) {
        return 0;
    }

    /**
     * 获取调整大小后的图片
     *
     * @return
     */
    private Bitmap getScaledCenterBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        centerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gesturecenterimage, options);
        int imageWidth = centerBitmap.getWidth();
        int imageHeight = centerBitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = centerImgWidth > imageWidth ? (float) centerImgWidth / (float) imageWidth : (float) imageWidth / (float) centerImgWidth;
        float scaleHeight = centerImgWidth > imageHeight ? (float) centerImgWidth / (float) imageHeight : (float) imageHeight / (float) centerImgWidth;
        matrix.postScale(scaleWidth, scaleHeight);
        centerBitmap = Bitmap.createBitmap(centerBitmap,
                0, 0, centerBitmap.getWidth(), centerBitmap.getHeight(), matrix, true);
        return centerBitmap;
    }

    /**
     * 判断触点是否在圆内，并返回对应圆Bean
     *
     * @param touchX
     * @param touchY
     * @return
     */
    private ImageGestureCircleBean checkInImgCircle(int touchX, int touchY) {
        int i = 0, j = 0;
        i = getSectionId(touchX / unitSize);
        j = getSectionId(touchY / unitSize);
        if (j == -1 || i == -1)
            return null;
        i = j * 3 + i;
        if (Math.pow((touchX - matrix[i].getCenterX()), 2) + Math.pow(touchY - matrix[i].getCenterY(), 2) <= Math.pow(matrix[i].getRadius(), 2)) {
            return matrix[i];
        }
        return null;
    }

    /**
     * 计算触点在6等分中的区间
     *
     * @param i
     * @return
     */
    private int getSectionId(int i) {
        if (0 <= i && i <= 2) {
            i = 0;
        } else if (2 < i && i <= 4) {
            i = 1;
        } else if (4 < i && i <= 6) {
            i = 2;
        } else {
            i = -1;
        }
        return i;
    }

    private synchronized int compute1(ImageGestureCircleBean bean) {
        double[] triangle1 = new double[2];
        double[] triangle2 = new double[2];
        //三角形外切圆的半径
        double circleRadius = bean.getRadius() * (1f/5f);
        //三角形中心位置
        double[] triangleCenter = computeDistance(bean, (float) bean.getInter()[0], (float) bean.getInter()[1], (float) (bean.getRadius() * trianglePlace));
        //三角形其中一个角的位置
        double[] triangle0 =  computeDistance(bean, (float) bean.getInter()[0], (float) bean.getInter()[1], (float) (bean.getRadius() * trianglePlace + circleRadius));
        //划线和三角形一条边的交点
        double[] triangleLine = computeDistance(bean, (float) bean.getInter()[0], (float) bean.getInter()[1], (float) (bean.getRadius() * trianglePlace - (1f/2f) * circleRadius ));
        //手划线的斜率
        double lineSlope = (triangle0[1] - triangleCenter[1]) / (triangle0[0] = triangleCenter[0]);
        //不知道如何描述 y = kx + b中的b
        double b = triangleLine[1] - (-lineSlope * triangleLine[0]);
        //代表一元二次方程中的参数abc
        double equationA = 1 + Math.pow(-lineSlope, 2);
        double equationB = 2 * (-lineSlope) * (b - triangleLine[1]) - 2 * triangleLine[0];
        double equationC = Math.pow(triangleLine[0], 2) + Math.pow((b - triangleLine[1]), 2) - Math.pow(circleRadius, 2);

        double key = Math.sqrt(Math.pow(equationB, 2) - 4 * equationA * equationC);

        double x1 = (-equationB + key) / (2 * equationA);
        double x2 = (-equationB - key) / (2 * equationA);
        double y1 = -lineSlope * x1 + b;
        double y2 = -lineSlope * x2 + b;
        triangle1[0] = x1;
        triangle1[1] = y1;
        triangle2[0] = x2;
        triangle2[1] = y2;
        bean.setTriangle0(triangle0);
        bean.setTriangle1(triangle1);
        bean.setTriangle2(triangle2);

        Log.e("x1", String.valueOf(x1));
        Log.e("x2", String.valueOf(x2));
        Log.e("key", String.valueOf(key));
        Log.e("lineSlope", String.valueOf(lineSlope));

        Log.e("y1", String.valueOf(y1));
        Log.e("y2", String.valueOf(y2));


        return 0;
    }

    private synchronized int compute2(ImageGestureCircleBean bean) {
        double[] triangle1 = new double[2];
        double[] triangle2 = new double[2];
        //三角形外切圆的半径
        double circleRadius = bean.getRadius() * (1f/5f);
        //三角形中心位置
        double[] triangleCenter = computeDistance(bean, (float) (bean.getCenterX() + bean.getRadius()), (float) bean.getCenterY(), (float) (bean.getRadius() * trianglePlace));
        //三角形其中一个角的位置
        double[] triangle0 =  computeDistance(bean, (float) (bean.getCenterX() + bean.getRadius()), (float) bean.getCenterY(), (float) (bean.getRadius() * trianglePlace + circleRadius));
        //划线和三角形一条边的交点
        double[] triangleLine = computeDistance(bean, (float) (bean.getCenterX() + bean.getRadius()), (float) bean.getCenterY(), (float) (bean.getRadius() * trianglePlace - (1f/2f) * circleRadius ));


        double chazhi = circleRadius * Math.sqrt(3) / 2f;

        triangle1[0] = triangleLine[0];
        triangle1[1] = triangleLine[1] + chazhi;
        triangle2[0] = triangleLine[0];
        triangle2[1] = triangleLine[1] - chazhi;
        bean.setTriangle0(triangle0);
        bean.setTriangle1(triangle1);
        bean.setTriangle2(triangle2);


        return 0;
    }




    /**
     * 手势回执回调接口
     */
    interface GestureDrawLisenter {
        void onStart();

        void onFinish(String gesturePassword);
    }

    public void setOnGestureDrawListener(GestureDrawLisenter listener) {
        this.gestureDrawLisenter = listener;
    }
}
