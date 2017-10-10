package com.example.cike.imagegesturelockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by cike on 2017/10/10.
 */
public class ImageGestureLockView extends View {
    private Paint normalPaint;              //正常圆画笔
    private Paint touchPaint;               //触摸状画笔
    private ImageGestureCircleBean[] matrix;        //关键点位置

    public ImageGestureLockView(Context context) {
        this(context, null);
    }

    public ImageGestureLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ImageGestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int unitSize = 0;
        if (matrix == null && (unitSize = w / 6) > 0) {
            matrix = new ImageGestureCircleBean[9];
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++) {
                    ImageGestureCircleBean bean = new ImageGestureCircleBean();
                    bean.setNumber(i * 3 + j);
                    bean.setCenterX(unitSize * (j * 2 + 1));
                    bean.setCenterY(unitSize * (i * 2 + 1));
                    bean.setRadius(unitSize * 0.5f);
                    matrix[i * 3 + j] = bean;
                }
        }
            super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 初始化笔和canvas
     */
    private void init() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 画圆
     */
    private void drawCircle(ImageGestureCircleBean bean) {

    }

    /**
     * 画圆和图片
     * @param bean
     */
    private void drawCircleImage(ImageGestureCircleBean bean) {

    }

    /**
     * 画线
     */
    private void drawLine() {}
}
