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
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.android.dongnaovip2017.R;

/**
 * Created by user on 2017/5/25.
 * 飞机转圈 Loading
 */

public class PlaneCircleLoadingView extends View {

    // Loading状态
    private final static int LOADING_STATE_LOADING = 0;
    // 暂停状态
    private final static int LOADING_STATE_PAUSE = 1;
    // 停止状态
    private final static int LOADING_STATE_STOP = 2;

    /**
     * 当前状态
     */
    private int mState;

    /**
     * 搜索图标圆圈半径
     */
    private int mRadiu = -1;

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

    private Bitmap mPlaneBitmap;

    /**
     * 截获路径的某个位置坐标
     */
    private float[] mPos;
    /**
     * 截获路径的某个位置切线值
     */
    private float[] mTans;

    private Matrix mMatrix;

    private ValueAnimator mAnima;

    public PlaneCircleLoadingView(Context context) {
        this(context, null);
    }

    public PlaneCircleLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaneCircleLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        int resId = R.drawable.arrow;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        mPlaneBitmap = BitmapFactory.decodeResource(getResources(), resId, options);
    }

    private void init() {
        // 默认状态
        mState = LOADING_STATE_STOP;

        // 边框宽度设为1dp
        if (mStrokeWidth < 0) {
            mStrokeWidth = dp2px(1);
        }

        // 初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mPath = new Path();
        // 新建一个Path实例用来显示截获的路径片段
        mDistPath = new Path();

        // 搜索图片半径初始化
        if (mRadiu < 0) {
            mRadiu = dp2px(64);
        }

        // 实例化PathMeasure
        mPathMeasure = new PathMeasure();

        //  初始化坐标点和切线值
        mPos = new float[2];
        mTans = new float[2];

        // 实例化mMatrix
        mMatrix = new Matrix();
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

        // 重置mMatrix
        mMatrix.reset();

        // 绘制Loading状态
        if (mState == LOADING_STATE_LOADING) {
            drawLoadingState(canvas);
        }
        // 绘制暂停状态
        else if (mState == LOADING_STATE_PAUSE) {
            drawPauseState(canvas);
        }
        // 绘制停止状态
        else if (mState == LOADING_STATE_STOP) {
            drawStopState(canvas);
        }
    }

    private void drawStopState(Canvas canvas) {

    }

    private void drawPauseState(Canvas canvas) {
        drawLoadingState(canvas);
    }

    private void drawLoadingState(Canvas canvas) {
        // 添加圆圈轨道路径
        mPath.addCircle(mCenterX, mCenterY, mRadiu, Path.Direction.CW);

        // 绘制圆圈轨道
        canvas.drawPath(mPath, mPaint);

        // PathMeasure关联Path
        mPathMeasure.setPath(mPath, false);

        // 获得圆圈轨道路径长度
        float pathLength = mPathMeasure.getLength();
        // 根据当前进度计算距离
        float distance = pathLength * mFraction;

        // 根据位置获取初始化Matrix
        boolean getRes = mPathMeasure.getMatrix(
                distance,
                mMatrix,
                PathMeasure.POSITION_MATRIX_FLAG | PathMeasure.TANGENT_MATRIX_FLAG
        );

        // 如果获取成功，在指定坐标点绘制飞机，并根据相应点的切线值旋转飞机
        if (getRes) {
            mMatrix.preTranslate(-mPlaneBitmap.getWidth()/2.0f, -mPlaneBitmap.getHeight() / 2.0f);
            mMatrix.preRotate(4.2f);
            canvas.drawBitmap(mPlaneBitmap, mMatrix, mPaint);
        }
    }

    public void startLoadingAnima() {
        mAnima = ValueAnimator.ofFloat(mFraction, mFraction + 1.0f);
        mAnima.setDuration(2000);
        mAnima.setRepeatMode(ValueAnimator.RESTART);
        mAnima.setRepeatCount(ValueAnimator.INFINITE);
        mAnima.setInterpolator(new LinearInterpolator());
        mAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isDetached || mState == LOADING_STATE_STOP) {
                    mState = LOADING_STATE_STOP;
                    animation.cancel();
                    return;
                } else if (mState == LOADING_STATE_PAUSE) {
                    animation.cancel();
                }else {
                    mFraction = ((float) animation.getAnimatedValue()) % 1.0f;
                    postInvalidate();
                }
            }
        });
        mAnima.start();
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

    public void cancle() {
        mState = LOADING_STATE_STOP;
        mFraction = 0f;
        invalidate();
    }

    public void pause() {
        mState = LOADING_STATE_PAUSE;
        invalidate();
    }

    public void start() {
        if (mState == LOADING_STATE_STOP) {
            mState = LOADING_STATE_LOADING;
            startLoadingAnima();
        } else if (mState == LOADING_STATE_PAUSE) {
            reStart();
        }
    }

    public void reStart() {
        if (mState == LOADING_STATE_PAUSE) {
            mState = LOADING_STATE_LOADING;
            startLoadingAnima();
        } else if (mState == LOADING_STATE_STOP) {
            start();
        }
    }
}
