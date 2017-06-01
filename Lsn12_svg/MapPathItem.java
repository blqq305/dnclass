package com.android.dongnaovip2017.modle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * Created by user on 2017/6/1.
 * 地图路径Item
 */

public class MapPathItem {
    /**
     * 地名
     */
    private String name;
    /**
     * 描述一个可绘制路径对象
     */
    private Path mPath;

    /**
     * 描述mPath对应的不规则区域
     */
    private Region mPathRegion;

    /**
     * 地图背景颜色
     */
    @ColorInt
    private int mBgColor = Color.parseColor("#ff66b5e5");
    /**
     * 选中时地图背景颜色
     */
    @ColorInt
    private int mSelBgColor = Color.parseColor("#ff0099cc");
    /**
     * 地图描边颜色
     */
    @ColorInt
    private int mStrokeColor = Color.parseColor("#BB666666");

    /**
     * 描边大小
     */
    private float mStrokeWidth = 1.0f;

    /**
     * 选中的地图描边大小
     */
    private float mSelStrokeWidth = 1.0f;

    public MapPathItem(Path path, String name) {
        this.mPath = path;
        this.name = name;
    }

    /**
     * 路径mPath是否包含坐标点x、y
     *
     * @param x
     * @param y
     * @return 包含返回true，否则返回false
     */
    public boolean isContains(float x, float y) {
        // 如果路径对象不为null，判断是否包含坐标点x、y
        if (mPath != null) {
            if (mPathRegion == null) {
                // 把mPath转换成不规则的区域Region mPathRegion，就可以用mPathRegion判断是否包含坐标点
                computePathRegion();
            }

            return mPathRegion.contains((int) x, (int) y);
        }

        return false;
    }

    /**
     * 绘制地图，根据是否选中地图使用不同的颜色绘制区别
     *
     * @param canvas
     * @param paint
     * @param isSelected
     */
    public void drawPath(@NonNull Canvas canvas, @NonNull Paint paint, boolean isSelected) {
        // 绘制自身的Path mPath
        if (mPath != null) {
            // 需要看清不同地区的边界，所以先绘制地图，然后绘制一层描边
            paint.setAntiAlias(true); // 抗锯齿
            paint.setStyle(Paint.Style.FILL);
            // 地图选中与没选中设置不同的颜色
            if (isSelected) {
                paint.setColor(mSelBgColor);
            } else {
                paint.setColor(mBgColor);
            }
            // 绘制地图背景
            canvas.drawPath(mPath, paint);

            // 绘制了地图背景，在地图上绘制一层描边
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(mStrokeColor);
            // 地图选中与没选中设置不同的描边宽度
            if (isSelected) {
                paint.setStrokeWidth(mSelStrokeWidth);
            } else {
                paint.setStrokeWidth(mStrokeWidth);
            }
            // 绘制地图描边
            canvas.drawPath(mPath, paint);
        }
    }

    /**
     * 把mPath所描述的路径转换成不规则的Region区域并保存在mPathRegion中
     */
    private void computePathRegion() {
        // 需要将路径mPath转换成Region描述的不规则区域
        // 要得到一个包含mPath所描述的不规则区域的Region，需要一个空Region用来保存不规则区域 和 一个指定了mPath最大区域的矩形区域Region
        // 空Region，用来保存不规则区域
        mPathRegion = new Region();

        // 指定了mPath最大区域的矩形区域Region
        Region rectRegion = new Region();

        // 指定mPath最大区域的矩形区域Region，需要给它设置一个矩形Rect rect
        Rect rect = new Rect(); // 描述mPath最大区域的矩形区域

        // 从mPath计算出一个RectF，然后用RectF设置rect
        RectF rectf = new RectF();
        mPath.computeBounds(rectf, false);

        // 用rectf设置rect，这样rect才真正包含了mPath所描述的路径区域
        rect.set((int) rectf.left, (int) rectf.top, (int) rectf.right, (int) rectf.bottom);

        // 把rect设置到rectRegion上，rectRegion才真正关联了mPath
        rectRegion.set(rect);

        // 从rectRegion所描述的mPath最大区域中剪切出不规则的Region区域保存于mPathRegion中
        mPathRegion.setPath(mPath, rectRegion);
    }

    public String getName() {
        return this.name;
    }

    public void setBgColor(@ColorInt int color) {
        this.mBgColor = color;
    }

    public void setSelBgColor(@ColorInt int color) {
        this.mSelBgColor = color;
    }

    public void setStrokeColor(@ColorInt int color) {
        this.mStrokeColor = color;
    }

    public void setStrokeWidth(float width) {
        this.mStrokeWidth = width;
    }

    public void setSelStrokeWidth(float width) {
        this.mSelStrokeWidth = width;
    }
}
