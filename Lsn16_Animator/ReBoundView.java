package com.android.dongnaovip2017.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Lingo on 2017/5/22.
 * 回弹View
 */

public class ReBoundView extends View {
    private final String TAG = "ReBoundView";

    public final static int BOUND_ACTION_OPEN = 0;
    public final static int BOUND_ACTION_CLOSE = 1;

    private Path mPath;
    private Paint mPaint;

    // 作为bound高度大小范围值，实际上是贝塞尔曲线的控制点Y值，绘制出的弹出高度会比这个小
    private int mMaxBoundHeight;
    // 动画执行到的进度值，转换成对应的距离
    private int mPass;
    // Path起始点
    private int mMoveTo;
    // 控制定Y值
    private int mControlY;

    private BoundListenner mBoundListenner;
    // 是否detached
    private boolean isDetached;

    // 背景色
    @ColorInt
    private int mBgColor;

    /**
     * 是否设置ReBound效果，当false的时候就不绘制任务东西
     */
    private boolean isReBound = true;

    public ReBoundView(Context context) {
        this(context, null);
    }

    public ReBoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mBgColor = ContextCompat.getColor(getContext(), android.R.color.holo_green_light);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isReBound) {
            return;
        }

        mPath.reset();
        mPath.moveTo(0, mMoveTo);
        mPath.quadTo(getWidth() / 2, mControlY, getWidth(), mMoveTo);
        mPath.lineTo(getWidth(), getHeight());
        mPath.lineTo(0, getHeight());
        mPath.close();

        mPaint.setColor(mBgColor);
        canvas.drawPath(mPath, mPaint);
    }

    public void startBound(int action) {
        if (action == BOUND_ACTION_OPEN) {
            startBoundOpen();
        } else {
            startBoundClose();
        }
    }

    private void startBoundOpen() {
        ValueAnimator animator = ValueAnimator.ofInt(getLayoutParams().height, mMaxBoundHeight * 2 / 3);
        animator.setDuration(400);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached) {
                    animation.cancel();
                    return;
                }
                mMoveTo = (int) animation.getAnimatedValue();
                mControlY = mMoveTo - mMaxBoundHeight * 2 / 3;
                Log.e(TAG, "startBoundOpen mMoveTo = " + mMoveTo);
                if (mMoveTo == mMaxBoundHeight * 2 / 3) {
                    if (mBoundListenner != null) {
                        mBoundListenner.onBoundFinish(BOUND_ACTION_OPEN);
                    }
                    reBoundStart();
                }
                invalidate();
                if (mBoundListenner != null) {
                    mBoundListenner.onBoundFraction(BOUND_ACTION_OPEN, animation.getAnimatedFraction());
                }
            }
        });
        animator.start();
    }

    private void startBoundClose() {
        ValueAnimator animator = ValueAnimator.ofInt(mMaxBoundHeight * 2 / 3, getLayoutParams().height);
        animator.setDuration(400);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached) {
                    animation.cancel();
                    return;
                }
                mMoveTo = (int) animation.getAnimatedValue();
                mControlY = mMoveTo - mMaxBoundHeight * 2 / 3;
                Log.e(TAG, "startBoundClose mMoveTo = " + mMoveTo);
                if (mMoveTo == getLayoutParams().height) {
                    if (mBoundListenner != null) {
                        mBoundListenner.onBoundFinish(BOUND_ACTION_CLOSE);
                    }
                }
                invalidate();
                if (mBoundListenner != null) {
                    mBoundListenner.onBoundFraction(BOUND_ACTION_CLOSE, animation.getAnimatedFraction());
                }
            }
        });
        animator.start();
    }

    private void reBoundStart() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, mMaxBoundHeight * 1.0f, mMaxBoundHeight * 2.0f / 3, mMaxBoundHeight * 1.0f / 3, mMaxBoundHeight * 2.0f / 3);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached) {
                    animation.cancel();
                    return;
                }
                float v = (float) animation.getAnimatedValue();
                mControlY = (int) v;
                mMoveTo = (int) (mMaxBoundHeight * 2.0f / 3);
                Log.e(TAG, "reBound mControlY = " + mControlY);
                invalidate();
            }
        });
        animator.start();
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

    /**
     * 设置背景颜色，回弹效果的颜色
     *
     * @param color
     */
    public void setBgColor(@ColorInt int color) {
        mBgColor = color;
    }

    /**
     * 设置是否带效果，如果设置false，就看不到ReBound效果，默认true
     *
     * @param reBound
     * @return
     */
    public void setReBound(boolean reBound) {
        isReBound = reBound;
    }

    public int getMaxBoundHeight() {
        return mMaxBoundHeight;
    }

    public void setMaxBoundHeight(int mMaxBoundHeight) {
        this.mMaxBoundHeight = mMaxBoundHeight;
    }

    public void setBoundListenner(BoundListenner listenner) {
        mBoundListenner = listenner;
    }

    public interface BoundListenner {
        void onBoundFinish(int action);

        void onBoundFraction(int action, float fraction);
    }
}
