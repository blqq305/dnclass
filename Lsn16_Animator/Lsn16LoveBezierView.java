package com.android.dongnaovip2017.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.android.dongnaovip2017.R;

/**
 * Created by user on 2017/6/10.
 * 爱心
 */

public class Lsn16LoveBezierView extends View {

    private Bitmap mHeartBitmap;

    // 心形图片着色器
    private BitmapShader mHeartBitmapShader;
    // 线性渲染着色器
    private LinearGradient mLinearGradient;

    // 画笔
    private Paint mPaint;

    private Matrix mMatrix;

    // 颜色
    @ColorInt
    private int mColor1 = Color.RED;
    @ColorInt
    private int mColor2 = Color.parseColor("#ffffff00");

    public Lsn16LoveBezierView(Context context) {
        this(context, null);
    }

    public Lsn16LoveBezierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMatrix = new Matrix();

        // 获得资源图片的drawable，图片的drawable就是BitmaptDrawable，所以可以强转成BitmapDrawable，
        // 就可以从BitmapDrawable的getBitmpa方法得到Bitmap对象
        mHeartBitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.heart)).getBitmap();
    }

    private void initLinearGradient() {
        // 线性渲染着色器
        mLinearGradient = new LinearGradient(
                0, 0, getWidth(), getHeight(), mColor1
                , mColor2,
                Shader.TileMode.CLAMP
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 测量模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 测量值
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);

        // View大小，先初始化
        int width = 0;
        int height = 0;

        // 按照自定义规则计算View的大小
        width = getSize(widthMode, measureWidth, true);
        height = getSize(heightMode, measureHeight, false);

        // 设置dimension，必须设置
        setMeasuredDimension(width, height);
    }

    private int getSize(int mode, int size, boolean isWidth) {
        int result = 0;
        // 如果是EXACTLY模式，直接用测量值size
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else if (mode == MeasureSpec.AT_MOST) {
            // 如果是AT_MOST，就是不能超过测量值
            if (mHeartBitmap != null) {
                result = Math.min(size, isWidth ? mHeartBitmap.getWidth() : mHeartBitmap.getHeight());
            } else {
                result = 0;
            }
        } else {
            // 等于mHeartBitmap的大小
            if (mHeartBitmap != null) {
                result = isWidth ? mHeartBitmap.getWidth() : mHeartBitmap.getHeight();
            } else {
                result = 0;
            }
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mHeartBitmap != null) {
            Bitmap bitmap = mHeartBitmap;
            // 如果图片的大小大于View的大小，那么需要修改图片大小，否则图片显示不完
            if (mHeartBitmap.getWidth() > w || mHeartBitmap.getHeight() > h) {
                // 按比例缩小
                float scaleX = w * 1.0f / mHeartBitmap.getWidth();
                float scaleY = h * 1.0f / mHeartBitmap.getHeight();

                // 如果scaleX 小于 scaleY，说明x缩放比较大，就按scalX来缩放
                float scale = scaleX < scaleY ? scaleX : scaleY;

                mMatrix.setScale(scale, scale);
                bitmap = Bitmap.createBitmap(mHeartBitmap, 0, 0, mHeartBitmap.getWidth(), mHeartBitmap.getHeight(), mMatrix, false);
            }
            // 心形图片着色器
            mHeartBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }

        initLinearGradient();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mHeartBitmap == null) {
            return;
        }
        // 画爱心
        drawLoveHeart(canvas);
    }

    private void drawLoveHeart(Canvas canvas) {
        // 需要两个着色器，所以需要一个混合着色器来设置一个着色器集合
        ComposeShader composeShader = new ComposeShader(mHeartBitmapShader, mLinearGradient, PorterDuff.Mode.SRC_IN);
        // 把图片着色器设置到画笔上，画笔绘制的时候，就会使用这个着色器填充图形
        mPaint.setShader(composeShader);

        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
    }

    public void setColor(@ColorInt int color1, @ColorInt int color2) {
        mColor1 = color1;
        mColor2 = color2;

        // 线性渲染着色器
        initLinearGradient();
        postInvalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        Log.e("Lsn16LoveBezierView", "onDetachedFromWindow");
    }
}
