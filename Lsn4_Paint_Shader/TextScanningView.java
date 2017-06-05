package com.android.dongnaovip2017.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.LinearInterpolator;

/**
 * Created by user on 2017/6/3.
 * 霓虹灯、文本扫描
 */

public class TextScanningView extends AppCompatTextView {

    private String mText = "〉滑动来解锁";

    private float mTextWidth;
    /**
     * 扫描字体的宽度，用字体宽度*百分系数
     */
    private final float mScanPencent = 0.5f;

    private float mStartScan;

    private TextPaint mTextPaint;

    // 填充颜色数组，照亮的部分由那些颜色组成
    // 扫描过后的字体颜色会被第一个颜色替代，扫描前的颜色会被最后一个颜色替代
    private final int[] mColors = new int[]{Color.parseColor("#ff111111"), Color.parseColor("#ffffffff"), Color.parseColor("#ff111111")};
    // 填充颜色比例
    private final float[] mPositions = new float[]{0.3f, 0.7f, 1.0f};

    private LinearGradient mLinearGradient;

    private Matrix mMatrix;

    private boolean isDetach = true;

    public TextScanningView(Context context) {
        this(context, null);
    }

    public TextScanningView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // 文本大小
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20.0f, getResources().getDisplayMetrics());

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(textSize);

        mMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 计算文本宽度和初始化开始扫描位置，意味着调用此方法，就会从头开始扫描
        computeText();

        // 线性渲染
        mLinearGradient = new LinearGradient(
                mStartScan, 0, mStartScan + mTextWidth * mScanPencent, 0,
                mColors, mPositions,
                Shader.TileMode.CLAMP
        );

        startScanning();
    }

    private void computeText() {
        // 计算出字体的宽度，然后才能从字体宽度中计算出扫描开始的位置
        mTextWidth = mTextPaint.measureText(mText);

        // 让扫描从字体的外边开始
        mStartScan = -mTextWidth * mScanPencent;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 设置一个黑色的背景
        canvas.drawColor(Color.parseColor("#dd000000"));

        // 设置mMatrix， 主要是改变扫射的开始位置
        mLinearGradient.setLocalMatrix(mMatrix);

        // 设置Shader
        mTextPaint.setShader(mLinearGradient);

        // 绘制文本
        canvas.drawText(mText, 0, mText.length(), 0, getHeight() / 2.0f, mTextPaint);
    }

    private void startScanning() {
        ValueAnimator va = ValueAnimator.ofFloat(0, 1.0f);
        va.setDuration(2000);
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

                // 一共完成的路程，总路程为文本宽度 + 2个扫描宽度 ，因为要从文本外边开始扫描，最后也扫到外边
                float finish = (mTextWidth * (2 + mScanPencent)) * ((float) animation.getAnimatedValue());

                // 完成路程 - 1个扫描宽度 就是开始扫描的地方
                mStartScan = finish - mTextWidth * mScanPencent;
                Log.e("TextScanningView", "startScanning mStartScan = " + mStartScan);

                // 改变Matrix
                mMatrix.setTranslate(mStartScan, 0);
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
