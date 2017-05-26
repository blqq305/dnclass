package com.android.dongnaovip2017.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.android.dongnaovip2017.R;

/**
 * Created by user on 2017/5/26.
 * 波纹View  上边浪小船
 */

public class WaveView extends View {

    private final static int WAVE_LENGTH = 300;

    private Bitmap mBitMap;

    private Paint mPaint;

    private Path mPath;

    /**
     * 进度
     */
    private float mFraction;
    /**
     * 小船的位移
     */
    private float mShipFraction;

    private boolean isDetached;

    /**
     * PathMeasure实例
     */
    private PathMeasure mPathMeasure;

    private Matrix mMatrix;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        mBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.timg, options);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#8833b5e5"));

        mPath = new Path();

        // 实例化PathMeasure
        mPathMeasure = new PathMeasure();

        // 实例化Matrix
        mMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        startWaveAnima();
        startShipAnima();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 重置Path
        mPath.reset();
        mMatrix.reset();

        // 添加波浪和水的路径
        int orginY = 550;
        // 把起始点移到屏幕外，从屏幕外开始画，然后不停移动起始点，让水波有移动的感觉
        // 每次移动距离从0到WAVE_LENGTH的长度，然后不停的重复
        // 根据进度算出当前移动的距离
        float moveLen = (1.0f - mFraction) * WAVE_LENGTH;
        //  设置一个小船的偏移量，因为小船同样是从屏幕外边进入，所以水波开始的地方不能》小船开始的地方
        float moveTo = moveLen + mBitMap.getWidth() * 1.0f;
        mPath.moveTo(-moveTo, orginY);
        // 从起始点开始重复画波长，n*WAVE_LENGTH 》= WAVE_LENGTH + getWidth() + mBitMap.getWidth()
        for (int i = 0; i < WAVE_LENGTH + getWidth() + mBitMap.getWidth(); i += WAVE_LENGTH) {
            // 使用rQuadTo相对位置的方法，控制点都在1/4个波长的位置，刚好在波峰和波谷的正上/下方
            mPath.rQuadTo(WAVE_LENGTH / 4, -30, WAVE_LENGTH / 2, 0);
            mPath.rQuadTo(WAVE_LENGTH / 4, 30, WAVE_LENGTH / 2, 0);
        }
        // 下边全部花水
        mPath.lineTo(getWidth(), getHeight());
        mPath.lineTo(0, getHeight());
        mPath.close();

        // mPathMeasure关联mPath
        mPathMeasure.setPath(mPath, false);

        // 取得mPath的路径长度
        float mPathLength = mPathMeasure.getLength();

        // 小船当前位置的x值
        float shipX = mShipFraction;

        // 截取mPath的路径
        // 截取的距离，就是 当前移动的距离 + 小船的当前位移值 + 小船宽度的一半
        float distance = moveLen + (shipX + mBitMap.getWidth() * 1.0f) + mBitMap.getWidth() / 2.0f;
        boolean cutRes = mPathMeasure.getMatrix(
                distance,
                mMatrix,
                PathMeasure.TANGENT_MATRIX_FLAG | PathMeasure.POSITION_MATRIX_FLAG
        );

        // 如果截取成功
        if (cutRes) {
            // 更新小船Matrix的位移
            mMatrix.preTranslate(-mBitMap.getWidth() / 2.0f, -mBitMap.getHeight() + 14);
        } else {
            // 更新小船Matrix的位移
            mMatrix.postTranslate(shipX, (orginY - mBitMap.getHeight() + 14) * 1.0f);
        }

        // 绘制小船
        canvas.drawBitmap(mBitMap, mMatrix, mPaint);

        // 绘制波浪和水
        canvas.drawPath(mPath, mPaint);
    }

    public void startWaveAnima() {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1.0f);
        va.setDuration(1000);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached) {
                    animation.cancel();
                }
                mFraction = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        va.start();
    }

    public void startShipAnima() {
        ValueAnimator va = ValueAnimator.ofFloat(-mBitMap.getWidth() * 1.0f, (getWidth() + mBitMap.getWidth()) * 1.0f);
        va.setDuration(20000);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached) {
                    animation.cancel();
                }
                mShipFraction = (float) animation.getAnimatedValue();
//                postInvalidate();
            }
        });
        va.start();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isDetached = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDetached = true;
    }
}
