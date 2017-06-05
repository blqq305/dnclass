package com.android.dongnaovip2017.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.android.dongnaovip2017.R;

/**
 * Created by user on 2017/6/3.
 * 倒影
 */

public class Lsn4ReflectionView extends View {

    // Bitmap
    private Bitmap mBitmap;

    private BitmapShader mBitmapShder;
    private LinearGradient mLinearGradient;

    // 填充颜色数组，照亮的部分由那些颜色组成
    private final int[] mColors = new int[]{Color.parseColor("#00ffffff"), Color.parseColor("#11ffffff"), Color.parseColor("#88ffffff")};
    // 填充颜色比例
    private final float[] mPositions = new float[]{0.5f, 0.6f, 1.0f};

    private Paint mPaint;

    ///////////////////////////  第二种 独有   ///////////////////////
    // 用来给倒影图片做混合模式的图片，通过图片混合模式实现有过滤的效果
    private Bitmap mXfermodeBitmap;
    // 颠倒状态图片，用原图生成一张颠倒的图片
    private Bitmap mReverBitmap;

    // 另外定义一个画笔吧
    private Paint mPaint2;
    ///////////////////////////  第二种 独有   ///////////////////////

    public Lsn4ReflectionView(Context context) {
        this(context, null);
    }

    public Lsn4ReflectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.xyjy2);

        mBitmapShder = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.MIRROR);
        mLinearGradient = new LinearGradient(0, 0, 0, mBitmap.getHeight() * 2, mColors, mPositions, Shader.TileMode.MIRROR);

        mPaint = new Paint();
        // 给画笔设置Shader,用Shader的内容来填充区域
        mPaint.setShader(mBitmapShder);


        mPaint2 = new Paint();
        // 颠倒状态图片，用原图生成一张颠倒的图片
        // 用一个Matrix，设置Matrix的scaleY向量为-1，就得到一张颠倒原图效果的图片
        Matrix matrix = new Matrix();
        matrix.setScale(1.0f, -1.0f); // y向量设为-1，图片就会在Y方向上颠倒
        mReverBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        // 用来给倒影图片做混合模式的图片
        mXfermodeBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        // 把图片嵌入到一个图片上，因为我们要在图片上用一个shader来填充它的透明度
        Canvas xfCanvas = new Canvas(mXfermodeBitmap);
        // 先创建一个LinearGradient线性渲染，用来渲染mXfermodeBitmap图片
        LinearGradient xfLinearGradient = new LinearGradient(0, 0, 0, mBitmap.getHeight(), new int[]{Color.parseColor("#66ffffff"), Color.parseColor("#00ffffff")}, new float[]{0.3f, 0.8f}, Shader.TileMode.MIRROR);
        // 先把xfLinearGradient设置到画笔上
        mPaint2.setShader(xfLinearGradient);
        // 把xfLinearGradient内容填充这个mXfermodeBitmap空图片，mXfermodeBitmap就变成一张渐变透明度的图片了
        xfCanvas.drawRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mPaint2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight() * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 第一种实现方式
//        idearOne(canvas);

        // 第二种实现方式
        idearTwo(canvas);
    }

    private void idearTwo(Canvas canvas) {
        // 先画原图
        canvas.drawBitmap(mBitmap, 0, 0, null);

        // 先保存一下画布，因为下边要做图片混合了
        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

        // 在原图下边话一张颠倒原图的图片
        canvas.drawBitmap(mReverBitmap, 0, mBitmap.getHeight(), null);

        //设置图片混合模式
        mPaint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        // 绘制图片混合模式中的源图片
        canvas.drawBitmap(mXfermodeBitmap, 0, mBitmap.getHeight(), mPaint2);

        // 恢复保存画布
        canvas.restoreToCount(layerId);
    }

    private void idearOne(Canvas canvas) {
        // 绘制镜像
        canvas.drawRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight() * 2, mPaint);

        // 绘制倒影渐变透明
        mPaint.setShader(mLinearGradient);
        canvas.drawRect(0, mBitmap.getHeight(), mBitmap.getWidth(), mBitmap.getHeight() * 2, mPaint);
    }


}
