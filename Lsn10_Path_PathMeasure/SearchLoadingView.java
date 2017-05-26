package com.android.dongnaovip2017.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by user on 2017/5/25.
 * 搜索Loading
 */

public class SearchLoadingView extends View {

    // 初始状态 或 静止状态
    private final static int SEARCH_STATE_STILL = 0;
    // 响应点击action状态
    private final static int SEARCH_STATE_ACTION = 1;
    // Loading状态
    private final static int SEARCH_STATE_LOADING = 2;

    /**
     * 当前状态
     */
    private int mState;

    /**
     * 搜索图标圆圈半径
     */
    private int mRadiu = -1;
    /**
     * 手柄长度
     */
    private int mHandleLen;
    /**
     * Loading圆圈半径
     */
    private int mLoadingRadiu;
    /**
     * 边框宽度
     */
    private int mStrokeWidth = -1;

    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * 路径
     */
    private Path mPath;
    /**
     * 截获目标路径
     */
    private Path mDistPath;

    // View中心坐标
    private int mCenterX;
    private int mCenterY;

    private float mFraction;
    private boolean isDetached;

    /**
     * PathMeasure实例
     */
    private PathMeasure mPathMeasure;

    public SearchLoadingView(Context context) {
        this(context, null);
    }

    public SearchLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        init();
    }

    private void init() {
        // 默认状态
        mState = SEARCH_STATE_STILL;

        // 边框宽度设为1dp
        if (mStrokeWidth < 0) {
            mStrokeWidth = dp2px(1);
        }

        // 初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mPath = new Path();
        // 新建一个Path实例用来显示截获的路径片段
        mDistPath = new Path();

        // 搜索图片半径初始化
        if (mRadiu < 0) {
            mRadiu = dp2px(16);
        }
        // 设置手柄长度等于搜索图标圆圈半径
        mHandleLen = mRadiu;
        // 让Loading圆圈半径 == 搜索图标圆圈半径 + 手柄长度
        mLoadingRadiu = mRadiu + mHandleLen;

        // 实例化PathMeasure
        mPathMeasure = new PathMeasure();
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp * 1.0f, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 重置路径
        mPath.reset();
        mDistPath.reset();
        mDistPath.rLineTo(0, 0); // 为了解决硬件加速问题而添加

        // 画静止状态
        if (mState == SEARCH_STATE_STILL) {
            drawStillState(canvas);
        }
        // 画ACTION状态
        else if (mState == SEARCH_STATE_ACTION) {
            drawActionState(canvas);
        }
        // 画LOADING状态
        else if (mState == SEARCH_STATE_LOADING) {
            drawLoadingState(canvas);
        }
    }

    private void drawLoadingState(Canvas canvas) {
        // 添加Loading圆圈路径
        mPath.addCircle(mCenterX, mCenterY, mLoadingRadiu, Path.Direction.CCW);

        // 关联Path
        mPathMeasure.setPath(mPath, false);
        // pathMeasure中搜索图标圆圈的路径长度
        float pathLength = mPathMeasure.getLength();

        // 根据进度截获Loading圆圈的路径
        float stopD = pathLength * mFraction;
        float startD = stopD - pathLength / 15;
        startD = startD < 0 ? 0 : startD;
        mPathMeasure.getSegment(startD, stopD, mDistPath, true);

        // 绘制路径
        canvas.save();
        canvas.rotate(45f, mCenterX, mCenterY);
        canvas.drawPath(mDistPath, mPaint);
        canvas.restore();
    }

    private void drawActionState(Canvas canvas) {
        // 添加搜索图标圆圈路径
        mPath.addCircle(mCenterX, mCenterY, mRadiu, Path.Direction.CW);
        mPath.lineTo(mCenterX + mRadiu + mHandleLen, mCenterY);

        // 关联Path
        mPathMeasure.setPath(mPath, false);
        // pathMeasure中搜索图标圆圈的路径长度
        float pathLength1 = mPathMeasure.getLength();

        // 跳过搜索图标圆圈，去获取手柄路径长度
        mPathMeasure.nextContour();

        // 获取到手柄的路径长度
        float pathLength2 = mPathMeasure.getLength();

        // 重新给PathMeasure设置会mPath，让当前的路径重新指向搜索图标圆圈
        mPathMeasure.setPath(mPath, false);

        // 搜索图标路径长度，圆圈的路径长度+手柄的路径长度
        float pathLength = pathLength1 + pathLength2;

        // 进度*路径长度小于 圆圈的路径长度 时，截获圆圈的路径 和 截获 手柄所有路径
        if (pathLength * mFraction < pathLength1) {
            // 根据进度截获圆圈的路径
            mPathMeasure.getSegment((pathLength1 + pathLength2) * mFraction, pathLength1, mDistPath, true);

            // 跳到手柄路径
            mPathMeasure.nextContour();

            // 截获手柄所有路径
            mPathMeasure.getSegment(0, pathLength2, mDistPath, true);
        } else {
            // 仅仅是为了pathMeasure.nextContour()调用起效，因为调用一次PathMeasure的api后调用nextContour()才有用
            mPathMeasure.getLength();
            // 跳到手柄路径
            mPathMeasure.nextContour();

            // 根据进度截获手柄路径
            mPathMeasure.getSegment(0, pathLength2 - ((pathLength1 + pathLength2) * mFraction - pathLength1), mDistPath, true);
        }

        // 绘制路径
        canvas.save();
        canvas.rotate(45f, mCenterX, mCenterY);
        canvas.drawPath(mDistPath, mPaint);
        canvas.restore();
    }

    public void actionStateAnima() {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1.0f);
        va.setDuration(2000);
//        va.setStartDelay(1000);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached || mState != SEARCH_STATE_ACTION) {
                    animation.cancel();
                    return;
                }
                mFraction = (float) animation.getAnimatedValue();
                if (mFraction >= 1.0f) {
                    animation.cancel();
                    mState = SEARCH_STATE_LOADING;
                    loadingStateAnima();
                }
                postInvalidate();
            }
        });
        va.start();
    }

    public void loadingStateAnima() {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1.0f);
        va.setDuration(2500);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.REVERSE);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached || mState != SEARCH_STATE_LOADING) {
                    animation.cancel();
                    return;
                }
                mFraction = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        va.start();
    }

    private void drawStillState(Canvas canvas) {
        // 添加搜索图标圆圈路径
        mPath.addCircle(mCenterX, mCenterY, mRadiu, Path.Direction.CW);
        mPath.lineTo(mCenterX + mRadiu + mHandleLen, mCenterY);

        // 绘制路径
        canvas.save();
        canvas.rotate(45f, mCenterX, mCenterY);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    public int getRadiu() {
        return mRadiu;
    }

    public void setRadiu(int mRadiu) {
        this.mRadiu = mRadiu;
        mHandleLen = mRadiu;
        mLoadingRadiu = mRadiu + mHandleLen;
        invalidate();
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

    public void actionStart() {
        if (mState == SEARCH_STATE_STILL) {
            mState = SEARCH_STATE_ACTION;
            actionStateAnima();
        }
    }

    public void reset() {
        mState = SEARCH_STATE_STILL;
        invalidate();
    }
}
