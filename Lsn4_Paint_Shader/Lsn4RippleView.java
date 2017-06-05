package com.android.dongnaovip2017.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by user on 2017/6/3.
 * 水波纹效果
 */

public class Lsn4RippleView extends View {

    // 默认最大半径，需要转换成dp
    private static final int DEF_MAX_RADIUS = 60;

    private int mMaxRadius;

    // 画笔
    private Paint mPaint;

    // Radio shader
    private RadialGradient mRadialGradient;

    // 填充颜色数组，照亮的部分由那些颜色组成
    private final int[] mColors = new int[]{Color.parseColor("#ffff0000"), Color.parseColor("#ff00ff00"), Color.parseColor("#ff0000ff"), Color.parseColor("#ffff0000")};
    // 填充颜色比例
    private final float[] mPositions = new float[]{0.2f, 0.5f, 0.8f, 1.0f};

    private Matrix mMatrix;

    private float mScale;

    private boolean isDetach;

    public Lsn4RippleView(Context context) {
        this(context, null);
    }

    public Lsn4RippleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mMaxRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_MAX_RADIUS * 1.0f, getResources().getDisplayMetrics());

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mMatrix = new Matrix();


        startScanning();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 把修改mMatrix的缩放向量后的Matrix设置到shader上，让shader根据mMtrix改变填充绘图区域
//        mRadialGradient.setLocalMatrix(mMatrix);

        mRadialGradient = new RadialGradient(mMaxRadius, mMaxRadius, mMaxRadius * mScale, mColors, mPositions, Shader.TileMode.MIRROR);

        // 把mRadialGradient设置到画笔上，画笔就会用shader设置的内容填充绘图区域
        mPaint.setShader(mRadialGradient);

        mPaint.setAlpha((int) ((1.0f - mScale) * 255));

        canvas.drawCircle(mMaxRadius, mMaxRadius, mMaxRadius * mScale, mPaint);
    }

    private void startScanning() {
        ValueAnimator va = ValueAnimator.ofFloat(0.1f, 1.0f);
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

                mScale = (float) animation.getAnimatedValue();
                Log.e("Lsn4RippleView", "startScanning mScale = " + mScale);

                // 改变mMatrix的缩放向量
                mMatrix.setScale(mScale, mScale);
//                float trans = mMaxRadius - (mMaxRadius * mScale);
//                mMatrix.setTranslate(trans, trans);

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
