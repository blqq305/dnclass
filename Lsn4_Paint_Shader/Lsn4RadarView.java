package com.android.dongnaovip2017.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by user on 2017/6/3.
 * 雷达效果
 */

public class Lsn4RadarView extends View {

    // 默认半径，需要转换成dp
    private static final int DEFAULT_RADIUS = 80;

    private int mRadius;

    private Paint mPaint;

    private RectF mRect;

    private SweepGradient mSweepGradient;

    // 开始绘制的角度
    private float mStartAngle;

    private Matrix mMatrix;

    // 填充颜色数组，照亮的部分由那些颜色组成
    private final int[] mColors = new int[]{Color.parseColor("#ffff0000"), Color.parseColor("#ff00ff00"), Color.parseColor("#ff0000ff"), Color.parseColor("#ffff0000")};
    // 填充颜色比例
    private final float[] mPositions = new float[]{0.3f, 0.5f, 0.7f, 1.0f};

    private boolean isDetach;


    ///////////////////////  第二种方式 /////////////////////////////
    // 梯度渲染
    private SweepGradient mSweepGradient2;
    // 画笔
    private Paint mPaint2;
    ///////////////////////  第二种方式 /////////////////////////////

    public Lsn4RadarView(Context context) {
        this(context, null);
    }

    public Lsn4RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_RADIUS * 1.0f, getResources().getDisplayMetrics());

        mStartAngle = 0;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mRect = new RectF();

        mMatrix = new Matrix();

        mSweepGradient = new SweepGradient(mRadius, mRadius, mColors, null);

        // 给画笔设置Shader
        mPaint.setShader(mSweepGradient);


        ///////////////////////////  第二种方式
        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        // 创建梯度渲染实例, 红色到透明
        mSweepGradient2 = new SweepGradient(mRadius, mRadius, new int[]{0xee0000ff, 0x330000ff}, null);

        ///////////////////////////   第二种方式


        startScanning();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 第一种实现方式
//        ideaOne(canvas);

        // 第二种实现方式
        ideaTwo(canvas);
    }

    private void ideaTwo(Canvas canvas) {
        // 先绘制一个圆，这个圆就是一个纯色的圆
        mPaint2.setColor(Color.GREEN);// 纯色
        mPaint2.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint2);

        // 在上面这个纯色圆上，绘制一些圆环，看起来更加有立体感
        // 让圆环间隔大小相等，所有画他们的时候设置不同的半径，定义一个半径变量，画不同的圆环的时候给不同的值
        int radius = mRadius / 4; // 一共画4个圆环, 最小一个半径就是1/4
        // 设置一个跟纯色圆的颜色不一样的颜色，这样才能看得到圆环
        mPaint2.setColor(Color.WHITE);
        // 只要圆环，所以设置样色
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(2);
        // 循环起画
        for (int i = 0; i < 4; i++) {
            // 开始画圆环
            canvas.drawCircle(mRadius, mRadius, radius * (i + 1), mPaint2);
        }

        // 在纯色圆环上再加一个十字切割，一条横线和一条竖线
        canvas.drawLine(0, mRadius, mRadius * 2, mRadius, mPaint2); // 横线
        canvas.drawLine(mRadius, 0, mRadius, mRadius * 2, mPaint2); // 竖线

        // 在纯色圆上绘制一个从红色透明度从0到1的圆，大小一致，覆盖住，然后不选旋转这个圆就可达到雷达扫描效果
        // mMatrix的旋转向量改变了，给Shader重新设置Matrix，让shader填充跟着角度变化
        mSweepGradient2.setLocalMatrix(mMatrix);

        // 绘制圆弧,这个圆弧用Shader填充
        mPaint.setShader(mSweepGradient2);
        // 圆弧的区域
        mRect = new RectF(0, 0, mRadius * 2, mRadius * 2);
        canvas.drawArc(mRect, 0, 360, true, mPaint);
    }

    private void ideaOne(Canvas canvas) {
        // mMatrix的旋转向量改变了，给Shader重新设置Matrix，让shader填充跟着角度变化
        mSweepGradient.setLocalMatrix(mMatrix);

        // 绘制圆弧,这个圆弧用Shader填充
        // 圆弧的区域
        mRect = new RectF(0, 0, mRadius * 2, mRadius * 2);
        canvas.drawArc(mRect, 0, 360, true, mPaint);
    }

    private void startScanning() {
        ValueAnimator va = ValueAnimator.ofFloat(0, 360.0f);
        va.setDuration(1500);
        va.setInterpolator(new LinearInterpolator());
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.RESTART);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetach) {
                    animation.cancel();
                    return;
                }

                mStartAngle = (float) animation.getAnimatedValue();
                Log.e("Lsn4RadarView", "startScanning mStartAngle = " + mStartAngle);

                // 改变mMatrix的旋转向量
                mMatrix.setRotate(mStartAngle, mRadius, mRadius);

                // 重新绘制
                postInvalidate();
            }
        });
        va.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isDetach = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDetach = true;
    }
}
