package com.android.dongnaovip2017.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.dongnaovip2017.R;

/**
 * Created by user on 2017/6/5.
 * Xfermode
 */

public class Lsn5XfermodeView extends View {

    //////////// 橡皮擦
    private Paint mEraserPaint;
    // 有涂鸦的Bitmap，这个Bitmap的涂鸦就是用来给橡皮擦擦除的
    private Bitmap mEsGraffitiBitmap;
    // 路径
    private Path mEraserPath;


    ////////////////// 刮刮卡
    private Paint mScratchPaint;
    private TextPaint mScratchTextPaint;
    // 覆盖住中奖信息的Bitmap
    private Bitmap mScratchBitmap;
    // 路径
    private Path mScratchPath;


    public Lsn5XfermodeView(Context context) {
        this(context, null);
    }

    public Lsn5XfermodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 橡皮擦
        initEraser();

        // 刮刮卡
        initScratch();
    }

    private void initScratch() {
        mScratchPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mScratchTextPaint = new TextPaint();
        // 设置文字画笔
        mScratchTextPaint.setColor(Color.BLACK);
        mScratchTextPaint.setTextSize(40);

        mScratchPath = new Path();

        // 覆盖住中奖信息的Bitmap，压缩一下图片，压缩到原图的1/2
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        mScratchBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.guaguaka, options);
    }

    private void initEraser() {
        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mEraserPath = new Path();

        // 有涂鸦的Bitmap，这个Bitmap的涂鸦就是用来给橡皮擦擦除的,这里View的宽高还没有计算出来，所以用getMeasuredWidth()
        mEsGraffitiBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        // 创建一个画布，用来装载mEsGraffitiBitmap
        Canvas eraserCanvas = new Canvas(mEsGraffitiBitmap);
        // 在路径上添加一些涂鸦路径
        mEraserPaint.setColor(Color.WHITE);
        mEraserPaint.setStyle(Paint.Style.FILL);
        // 画个圆
        eraserCanvas.drawCircle(100, 200, 60, mEraserPaint);
        // 画个矩形
        eraserCanvas.drawRect(180, 100, 450, 300, mEraserPaint);
        // 写一些字
        mEraserPaint.setTextSize(60);
        eraserCanvas.drawText("这些好像写错了~~~！", 80, 460, mEraserPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 橡皮擦
//        eraserDraw(canvas);

        // 刮刮卡
        scrathDraw(canvas);
    }

    private void scrathDraw(Canvas canvas) {
        // 设置一个背景色
        canvas.drawColor(Color.GRAY);

        // 写入中奖信息
        String str = "谢谢参与";
        // 文字宽度
        float strWidth = mScratchTextPaint.measureText(str);
        // 绘制中奖信息
        canvas.drawText(str, (mScratchBitmap.getWidth() - strWidth) / 2.0f, mScratchBitmap.getHeight() / 2.0f, mScratchTextPaint);

        // 先保存一下画布，下面开始要混合图形了
        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

        // 绘制覆盖在奖品上边的图片
        canvas.drawBitmap(mScratchBitmap, 0, 0, null);

        // 设置混合模式，用手指刮过的路径做混合，所以路径经过的地方把覆盖在中奖信息上的图片的像素清除就可以了
        mScratchPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // 绘制手指刮过的路径
        mScratchPaint.setStyle(Paint.Style.STROKE);
        mScratchPaint.setStrokeWidth(10);
        mScratchPaint.setStrokeCap(Paint.Cap.ROUND);
        mScratchPaint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawPath(mScratchPath, mScratchPaint);

        // 上边保存了画布，这里记得释放
        canvas.restoreToCount(layerId);
    }

    private void eraserDraw(Canvas canvas) {
        // 设置一个背景颜色，把屏幕当做黑板
        canvas.drawColor(Color.BLACK);

        // 保存一下画布，因为下边需要混合图片了
        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

        // 绘制涂鸦，涂鸦已经涂在一张图片上，图片是透明的，这里只需将这张图片绘制在黑板上
        canvas.drawBitmap(mEsGraffitiBitmap, 0, 0, null);

        // 设置图形混合模式，只需要把路径经过的响应部分像素清除
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeWidth(18);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);

        // 绘制路径
        canvas.drawPath(mEraserPath, mEraserPaint);

        // 上边保存了画布，这里记得释放
        canvas.restoreToCount(layerId);

        // 清除图片混合模式
        mEraserPaint.setXfermode(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        final float x = event.getX();
        final float y = event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            mEraserPath.moveTo(x, y);
            mScratchPath.moveTo(x, y);
        } else if (action == MotionEvent.ACTION_MOVE) {
            mEraserPath.lineTo(x, y);
            mScratchPath.lineTo(x, y);
            invalidate();
        } else if (action == MotionEvent.ACTION_UP) {
            invalidate();
        }

        return true;
    }
}
