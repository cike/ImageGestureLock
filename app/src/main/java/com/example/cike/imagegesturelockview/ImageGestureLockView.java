package com.example.cike.imagegesturelockview;

import android.content.Context;
import android.content.res.TypedArray;
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

    private float circleWide = 2f;               //外圆默认粗细
    private int selectedColor = Color.parseColor("#df4400");        //选中状态默认颜色
    private int unSelectedColor = Color.parseColor("#d2d2d2");      //未选中状态默认颜色
    private int lineColor = Color.parseColor("#df4400");           //默认画线颜色和选中颜色相同
    private float lineWide = 4f;                                              //默认画线粗细
    private int triangleColor = Color.parseColor("#df4400");        //默认三角形颜色和选中状态一致
    private int centerImageSrc = R.drawable.gesturecenterimage;                //默认中心图片
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
    private GestureDrawLisenter gestureDrawLisenter;            //会话监听接口
    private ImageGestureCircleBean lastBean;                    //上一个被选中的bean

    public ImageGestureLockView(Context context) {
        this(context, null);
    }

    public ImageGestureLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ImageGestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.gestureLock);
            circleWide = typedArray.getFloat(R.styleable.gestureLock_circleWide, 2f);
            selectedColor = typedArray.getColor(R.styleable.gestureLock_selectedColor, Color.parseColor("#df4400"));
            unSelectedColor = typedArray.getColor(R.styleable.gestureLock_unSelectedColor, Color.parseColor("#d2d2d2"));
            lineColor = typedArray.getColor(R.styleable.gestureLock_lineColor, Color.parseColor("#df4400"));
            lineWide = typedArray.getFloat(R.styleable.gestureLock_lineWide, 4f);
            triangleColor = typedArray.getColor(R.styleable.gestureLock_trangleColor, Color.parseColor("#df4400"));
            centerImageSrc = typedArray.getResourceId(R.styleable.gestureLock_centerImageSrc, R.drawable.gesturecenterimage);
        }
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("onSizeChanged", String.valueOf(w));
        Log.e("onSizeChanged", String.valueOf(h));
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
        centerBitmap = getScaledCenterBitmap(centerImageSrc);
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
        normalPaint.setColor(unSelectedColor);
        normalPaint.setStrokeJoin(Paint.Join.ROUND);
        normalPaint.setStrokeCap(Paint.Cap.ROUND);
        normalPaint.setStrokeWidth(circleWide);
        normalPaint.setDither(true);

        //设置选中状态画笔
        touchPaint = new Paint();
        touchPaint.setAntiAlias(true);
        touchPaint.setFilterBitmap(true);
        touchPaint.setStyle(Paint.Style.STROKE);
        touchPaint.setStrokeJoin(Paint.Join.ROUND);
        touchPaint.setStrokeCap(Paint.Cap.ROUND);
        touchPaint.setColor(selectedColor);
        touchPaint.setStrokeWidth(circleWide);
        touchPaint.setDither(true);

        //设置画线笔
        linePaint = new Paint(touchPaint);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWide);

        /**
         * 设置画三角形的画笔
         */
        trianglePaint = new Paint();
        trianglePaint.setAntiAlias(true);
        trianglePaint.setFilterBitmap(true);
        trianglePaint.setStyle(Paint.Style.FILL);
        trianglePaint.setStrokeJoin(Paint.Join.ROUND);
        trianglePaint.setStrokeCap(Paint.Cap.ROUND);
        trianglePaint.setColor(triangleColor);
        trianglePaint.setStrokeWidth(circleWide);
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
        ImageGestureCircleBean bean = checkInImgCircle(event.getX(), event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (gestureDrawLisenter != null)
                    gestureDrawLisenter.onStart();
                if (bean != null) {
                    lastBean = bean;                //上一个bean
                    bean.setSelect(true);           //设置圆圈被选中
                    lastBean.computetriangleIndex();
                    lastBean.computeDistance(event.getX(), event.getY());
                    lastBean.rotationBetweenLines(lastBean.getCenterX(), lastBean.getCenterY(), event.getX(), event.getY());
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
                            //计算三角形坐标
                            lastBean.computetriangleIndex();
                            //计算圆点与触点直线和圆的交点
                            lastInterArray = lastBean.computeDistance(bean.getCenterX(), bean.getCenterY());
                            //设置交点位置
                            lastBean.setInter(lastInterArray);
                            //计算旋转角度
                            lastBean.rotationBetweenLines(lastBean.getCenterX(), lastBean.getCenterY(), bean.getCenterX(), bean.getCenterY());
                            tempPath.moveTo((float) lastInterArray[0], (float) lastInterArray[1]);
                            double[] interArray = bean.computeDistance(lastBean.getCenterX(), lastBean.getCenterY());
                            tempPath.lineTo((float) interArray[0], (float) interArray[1]);
                        }
                        lastBean = bean;
                        selectedQueue.add(bean);
                    } else {
                        lastBean.computetriangleIndex();
                        lastInterArray = lastBean.computeDistance(event.getX(), event.getY());
                        lastBean.rotationBetweenLines(lastBean.getCenterX(), lastBean.getCenterY(), event.getX(), event.getY());
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
                bean.setAngle(0);
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
    private synchronized void drawCircleImage(ImageGestureCircleBean bean) {
        bitmapBufferCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        if (bean.isSelect()) {          //选中状态
            /**
             * 绘制外圆和中心图片
             */
            bitmapBufferCanvas.drawBitmap(centerBitmap, bean.getImgX(), bean.getImgY(), null);
            bitmapBufferCanvas.drawCircle(bean.getCenterX(), bean.getCenterY(), bean.getRadius(), touchPaint);
            /**
             * 绘制三角
             */
            Path path = new Path();
            path.moveTo((float) bean.getTriangle0()[0], (float) bean.getTriangle0()[1]);
            path.lineTo((float) bean.getTriangle1()[0], (float) bean.getTriangle1()[1]);
            path.lineTo((float) bean.getTriangle2()[0], (float) bean.getTriangle2()[1]);
            path.close();
            bitmapBufferCanvas.save();
            float rotation = 0;
            if (bean.getAngle() <= 90) {
                rotation = (float) -bean.getAngle();
            } else {
                rotation = (float) (bean.getAngle() - 90);
            }
            bitmapBufferCanvas.rotate(rotation, bean.getCenterX(), bean.getCenterY());
            bitmapBufferCanvas.drawPath(path, trianglePaint);
            bitmapBufferCanvas.restore();
        } else {                        //未选中状态  绘制外圆
            bitmapBufferCanvas.drawCircle(bean.getCenterX(), bean.getCenterY(), bean.getRadius(), normalPaint);
        }
    }

    /**
     * 获取调整大小后的图片
     *
     * @return
     */
    private Bitmap getScaledCenterBitmap(int imageId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        centerBitmap = BitmapFactory.decodeResource(getResources(), imageId, options);
        int imageWidth = centerBitmap.getWidth();
        int imageHeight = centerBitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = (float) centerImgWidth / (float) imageWidth;
        float scaleHeight = (float) centerImgWidth / (float) imageHeight;
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
    private ImageGestureCircleBean checkInImgCircle(float touchX, float touchY) {
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
     * @param i
     * @return
     */
    private int getSectionId(float i) {
        if (0 <= i && i <= 2) {
            i = 0;
        } else if (2 < i && i <= 4) {
            i = 1;
        } else if (4 < i && i <= 6) {
            i = 2;
        } else {
            i = -1;
        }
        return (int) i;
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
