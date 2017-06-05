package com.android.dongnaovip2017.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.dongnaovip2017.R;

/**
 * Created by user on 2017/6/2.
 * 高级渲染 Shader
 */

public class MyGradientView extends View {

    private Bitmap mBitmap;
    private Bitmap mHeartBitmap;
    private Bitmap mZoomBitmap;
    private Bitmap mSmallBitmap;

    // 图片着色器
    private BitmapShader mBitmapShader;
    // 心形图片着色器
    private BitmapShader mHeartBitmapShader;
    // 放大图片着色器
    private BitmapShader mZoomBitmapShader;
    // 线性渲染着色器
    private LinearGradient mLinearGradient;

    // 画笔
    private Paint mPaint;

    // 放大镜的半径
    private int mZoomRadius;

    // 缩放
    private float mScale = 1.0f;

    private Matrix mMatrix;
    private float mX;
    private float mY;

    public MyGradientView(Context context) {
        this(context, null);
    }

    public MyGradientView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyGradientView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获得资源图片的drawable，图片的drawable就是BitmaptDrawable，所以可以强转成BitmapDrawable，
        // 就可以从BitmapDrawable的getBitmpa方法得到Bitmap对象
        mBitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.xyjy2)).getBitmap();

        mHeartBitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.heart)).getBitmap();

        mZoomBitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.yifu)).getBitmap();

        // 把mZoomBitmap按照等比例缩小
        // 设置Option的压缩比例，mScale保存压缩比例
        mScale = 4.0f;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = (int) mScale;
        // 生成一张小图
        mSmallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yifu, options);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mZoomRadius = 100;

        mMatrix = new Matrix();

        // 图片着色器
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // 心形图片着色器
        mHeartBitmapShader = new BitmapShader(mHeartBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // 放大图片着色器
        mZoomBitmapShader = new BitmapShader(mZoomBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // 线性渲染着色器
        mLinearGradient = new LinearGradient(
                0, 0, mHeartBitmap.getWidth(), mHeartBitmap.getHeight(),
                Color.RED, Color.parseColor("#ffffff00"),
                Shader.TileMode.CLAMP
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 圆形图片
//        drawCircleBitmap(canvas);

        // 圆角图片
//        drawRoundConerBitmap(canvas);

        // 红心
//        drawRedHeart(canvas);

        // 放大镜
        drawZoomImage(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mX = event.getX();
        mY = event.getY();

        // 放大镜是shader图片的参照物，相对于放大镜来说，就是相对移动shader图片
        mMatrix.setTranslate(-mX * mScale + mZoomRadius, -mY * mScale + mZoomRadius);
        invalidate();
        return true;
    }

    private void drawZoomImage(Canvas canvas) {
        // 绘制显示的小图片，把放大镜发到小图片上时，放大响应区域
        canvas.drawBitmap(mSmallBitmap, 0, 0, mPaint);

        /////////////////////////////// 第一种方法 自己画圆  //////////////////////////

        // 把图片着色器设置到画笔上，画笔绘制的时候，就会使用这个着色器填充图形
//        mZoomBitmapShader.setLocalMatrix(mMatrix);
//        mPaint.setShader(mZoomBitmapShader);
//
//        // 绘制放大镜，放大镜用圆形
//        canvas.drawCircle(mX, mY, mZoomRadius, mPaint);


        /////////////////////////////// 第二种方法 使用ShapeDrawable  //////////////////////////

        // 使用图形Drawable ShapeDrawable，就像XML定义一样，
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        // 得到它的画笔,再把shader设置到画笔中
        shapeDrawable.getPaint().setShader(mZoomBitmapShader);
        mZoomBitmapShader.setLocalMatrix(mMatrix);

        // 设置他的区域
        shapeDrawable.setBounds((int) mX - mZoomRadius, (int)mY - mZoomRadius, (int)mX + mZoomRadius, (int)mY + mZoomRadius);

        // 绘制放大镜，调用drawable的draw方法，drawable shapeDrawable就会绘制自身
        shapeDrawable.draw(canvas);
    }

    private void drawRedHeart(Canvas canvas) {
        // 需要两个着色器，所以需要一个混合着色器来设置一个着色器集合
        ComposeShader composeShader = new ComposeShader(mHeartBitmapShader, mLinearGradient, PorterDuff.Mode.SRC_IN);
        // 把图片着色器设置到画笔上，画笔绘制的时候，就会使用这个着色器填充图形
        mPaint.setShader(composeShader);

        canvas.drawRect(0, 0, mHeartBitmap.getWidth(), mHeartBitmap.getHeight(), mPaint);
    }

    private void drawRoundConerBitmap(Canvas canvas) {
        // 把图片着色器设置到画笔上，画笔绘制的时候，就会使用这个着色器填充图形
        mPaint.setShader(mBitmapShader);

        // 画一个圆，传入的画笔就会用设置给它的着色器填充这个圆角矩形区域，因为着色器的内容是图片，所以就会使用图片填充这个圆角矩形区域
        // 如果我们的圆角矩形比图片小，那么就会从图片截出一个圆角矩形区域
        RectF rect = new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        canvas.drawRoundRect(rect, 40, 40, mPaint);
    }

    private void drawCircleBitmap(Canvas canvas) {
        // 把图片着色器设置到画笔上，画笔绘制的时候，就会使用这个着色器填充图形
        mPaint.setShader(mBitmapShader);

        // 画一个圆，传入的画笔就会用设置给它的着色器填充这个圆形区域，因为着色器的内容是图片，所以就会使用图片填充这个圆形区域
        // 如果我们的圆比图片小，那么就会从图片截出一个圆形区域
        canvas.drawCircle(mBitmap.getWidth() / 2, mBitmap.getWidth() / 2, mBitmap.getWidth() / 2, mPaint);

        // 如果圆比图片大，那么剩余的部分就会用图片上下左右最外的一个像素填充,因为我们Shader的填充模式是Shader.TileMode.CLAMP
//        int min = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
//        int max = Math.max(mBitmap.getWidth(), mBitmap.getHeight());
//        canvas.drawCircle(max, max, min, mPaint);
    }
}
