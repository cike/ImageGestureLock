package com.example.cike.imagegesturelockview;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
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
                    if (lastBean != null) {
                        int[] lastInterArray = computeDistance(lastBean, event.getX(), event.getY());
                        tempPath.moveTo(lastInterArray[0], lastInterArray[1]);
                        mPath.addPath(tempPath);
                        mPath.moveTo(lastInterArray[0], lastInterArray[1]);
                    }
                    if (bean != null && !bean.isSelect()) {
                        bean.setSelect(true);
                        if (lastBean != null) {
                            int[] interArray = computeDistance(bean, lastBean.getCenterX(), lastBean.getCenterY());
                            tempPath.lineTo(interArray[0], interArray[1]);
                        }
                        lastBean = bean;
                        selectedQueue.add(bean);
                    }
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
    private int[] computeDistance(ImageGestureCircleBean bean, float touchX, float touchY) {

        int[] indexArray = new int[2];
        float subx = touchX - bean.getCenterX();
        float suby = touchY - bean.getCenterY();
        double sinResult = (Math.abs((float) subx) / Math.sqrt(Math.pow(subx, 2) + Math.pow(suby, 2)));
        double xresult = (bean.getRadius() * sinResult);
        double yresult = Math.sqrt(Math.pow(bean.getRadius(), 2) - Math.pow(xresult, 2));
        if (subx >= 0) {
            indexArray[0] = (int) (bean.getCenterX() + xresult);
        } else {
            indexArray[0] = (int) (bean.getCenterX() - xresult);
        }
        if (suby >= 0) {
            indexArray[1] = (int) (bean.getCenterY() + yresult);
        } else {
            indexArray[1] = (int) (bean.getCenterY() - yresult);
        }
        return indexArray;
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
