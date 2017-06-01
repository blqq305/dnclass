package com.android.dongnaovip2017.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.dongnaovip2017.R;
import com.android.dongnaovip2017.lib.PathParser;
import com.android.dongnaovip2017.modle.MapPathItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by user on 2017/6/1.
 * 台湾地图
 */

public class TaiwanMapView extends View {

    /**
     * 地图所有路径的集合
     */
    private List<MapPathItem> mPathItem;
    /**
     * 选中的item
     */
    private MapPathItem mMapPathItem;

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
    private float mSelStrokeWidth = 2.0f;
    /**
     * 画笔*
     */
    private Paint mPaint;
    /**
     * 区域
     */
    private RectF mRect;
    /**
     * 地图宽度
     */
    private int right;
    /**
     * 地图高度
     */
    private int bottom;

    /**
     * 地图缩放
     * 实际地图都很大，如果不缩放，地图会显示不全
     */
    private float mScale;

    /**
     * 是否解析完成
     */
    private boolean isParseFinish = false;

    private GestureDetector mGestureDetector;

    private Handler mHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            requestLayout();
            invalidate();
        }
    };

    public TaiwanMapView(Context context) {
        this(context, null);
    }

    public TaiwanMapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaiwanMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        // 首先一些初始化
        init();

        // 解析svg文件，解析完后就会得到 mPathItem 列表
        parseMap();
    }

    private void init() {
        mPathItem = new ArrayList<>();
        mMapPathItem = null;

        mPaint = new Paint();
        mRect = new RectF();
        right = 0;
        bottom = 0;
        mScale = 1.0f;

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onDown(MotionEvent e) {
                // 必须重新此方法，并且要返回true，否则不会回调其它一切回调方法
                // 不重写此方法或返回false，onSingleTapConfirmed 和 onDoubleTap 就不会被回调
                return true;
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                // 单击确认回调，如果没有双击，则回调此方法，如果回调了onDoubleTap方法就不会回调此方法

                // 检查点击是否落在某个城市的区域
                // 因为显示时乘上了一个缩放值，所以在判断坐标点前要先除以缩放值
                checkSelected(e.getX() / mScale, e.getY() / mScale);

                // 吐司提示点击地名
                if(mMapPathItem != null) {
                    Toast.makeText(getContext(), "" + mMapPathItem.getName(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            public boolean onDoubleTap(MotionEvent e) {
                // 双击回调方法，如果单击会回调onSingleTapConfirmed方法，则不回调此方法

                return true;
            }

        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 宽度测量规格 和 高度测量规格
        int wMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMeasureMode = MeasureSpec.getMode(heightMeasureSpec);

        // 宽度测量值 和 高度测量值
        int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
        int heightMeasure = MeasureSpec.getSize(heightMeasureSpec);

        // View自己的宽度和高度
        int width = 0;
        int height = 0;

        // 根据测量模式和地图大小计算出View的大小
        width = measureSize(wMeasureMode, widthMeasure, true);
        height = measureSize(hMeasureMode, heightMeasure, true);
//        Log.e("TaiwanMapView", "onMeasure width = " + width);
//        Log.e("TaiwanMapView", "onMeasure height = " + height);
//        Log.e("TaiwanMapView", "onMeasure right = " + right);
//        Log.e("TaiwanMapView", "onMeasure bottom = " + bottom);

        // 最终根据View大小和地图大小确定一个缩放值
        float scaleW = right > 0 ? width * 1.0f / right : mScale;
        float scaleH = bottom > 0 ? height * 1.0f / bottom : mScale;
        mScale = Math.min(scaleW, scaleH);
        Log.e("TaiwanMapView", "onMeasure mScale = " + mScale);

        // 设置MeasuredDimension
        setMeasuredDimension(width, height);
    }

    private int measureSize(int mode, int size, boolean isWidth) {
        int result;
        int compare = isWidth ? right : bottom;
        // 如果测量模式是 EXACTLY，就用测量值作为View的宽度/高度值
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        }
        // 如果宽度测量模式是 AT_MOST，就看地图的大小是否大于测量值，大于就用测量值，否则用地图大小
        else if (mode == MeasureSpec.AT_MOST) {
            if (size > compare) {
                result = compare;
            } else {
                result = size;
            }
        }
        // 未确定，就等于地图大小
        else {
            result = compare;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isParseFinish) {
            return;
        }
        canvas.scale(mScale, mScale);
        int pathLen = mPathItem.size();
        // 遍历路径，全部画出来，遍历完成后全部路径都绘制完毕
        for (int i = 0; i < pathLen; i++) {
            MapPathItem item = mPathItem.get(i);
            // 是否选中了此Item
            boolean isSelected = mMapPathItem != null && mMapPathItem == item;

            // 调用MapPathItem对象的drawPath方法，让它绘制自己
            mPathItem.get(i).drawPath(canvas, mPaint, isSelected);
        }
    }

    private void checkSelected(float x, float y) {
        // 检查点击是否落在某个城市的区域，如果在则表示选中此城市
        // 如果点击已经选中了的城市，就不用重头遍历检查了
        if (mMapPathItem != null) {
            if (mMapPathItem.isContains(x, y)) {
                return;
            }
        }

        if (mPathItem == null) {
            return;
        }

        int pathLen = mPathItem.size();
        // 遍历检查
        for (int i = 0; i < pathLen; i++) {
            // 等于上一个选中的城市，也不用检查了，因为上边已经检查过，确定不是它
            if (mPathItem.get(i) == mMapPathItem) {
                continue;
            }

            // 如果选择的是它，把它保存在mMapPathItem中，然后发送一个重绘消息，界面上显示选中它
            if (mPathItem.get(i).isContains(x, y)) {
                // 把它保存在mMapPathItem中
                mMapPathItem = mPathItem.get(i);

                // 发送重绘
                invalidate();

                // 退出遍历
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 把事件交给 GestureDetector 来处理
        // mGestureDetector会适时调用 onDown、onSingleTapUp、onDoubleTap等回调方法
        // 我们实现onDown、onSingleTapUponDoubleTap等回调方法做我们自己想做的事情
        return mGestureDetector.onTouchEvent(event);
    }

    private void parseMap() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 把地图svg raw转换成InputStream
                    InputStream inputStream = getResources().openRawResource(R.raw.taiwanhigh);

                    // 使用DocumentBuilder来解析inputStream，所有首先创建一个DocumentBuilder对象
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                    // 开始使用DocumentBuilder对象解析inputStream，解析后会返回一个Document对象
                    Document document = documentBuilder.parse(inputStream);

                    // 从document中查找path节点，把节点放入列表然后返回，nodeList就是返回的path节点列表
                    NodeList nodeList = document.getElementsByTagName("path");

                    int nodeListSize = nodeList.getLength(); // path节点列表长度

                    // 遍历path节点列表，从每个path节点中取出android:pathData属性值，
                    // 遍历完成后把所有pathData转换成MapPathItem对象并缓存在列表中
                    for (int i = 0; i < nodeListSize; i++) {
                        // 当前path节点
                        Element node = (Element) nodeList.item(i);

                        // 从节点中得到它所有的属性集合
                        NamedNodeMap namedNodeMap = node.getAttributes();

                        // 再从属性集合取出它的android:pathData属性，android:pathData属性也是用一个Node对象来描述
                        Node pathDataNode = namedNodeMap.getNamedItem("android:pathData");
                        if (pathDataNode == null) {
                            continue;
                        }

                        // 从pathDataNode属性节点就可以得到属性名称和属性值了
                        String pathData = pathDataNode.getNodeValue(); // 真正的pathData值

                        // 再从属性集合取出它的android:name属性，android:name属性也是用一个Node对象来描述
                        Node nameNode = namedNodeMap.getNamedItem("android:name");

                        // 从nameNode属性节点就可以得到属性名称和属性值了
                        String name = nameNode.getNodeValue(); // 真正的name值
                        Log.e("TaiwanMapView", "parseMap name = " + name);

                        // 得到了pathData值，其实pathData值不过是一个字符串，所以需要解析这个字符串，然后转换成对应的Path对象
                        Path path = PathParser.createPathFromPathData(pathData);

                        // 用路径计算出方形区域
                        path.computeBounds(mRect, false);
                        // 从区域中得到最右边和最下边，就是地图的大小
                        right = Math.max(right, dp2px((int) mRect.right));
                        bottom = Math.max(bottom, dp2px((int) mRect.bottom));

                        // 用Path对象封装MapPathItem对象
                        MapPathItem mapPathItem = new MapPathItem(path, name);
                        mapPathItem.setBgColor(mBgColor);
                        mapPathItem.setSelBgColor(mSelBgColor);
                        mapPathItem.setStrokeColor(mStrokeColor);
                        mapPathItem.setStrokeWidth(mStrokeWidth);
                        mapPathItem.setSelStrokeWidth(mSelStrokeWidth);

                        // 把mapPathItem放入列表缓存
                        mPathItem.add(mapPathItem);
                    }
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 把解析完的先显示
                isParseFinish = true;
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    public int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp * 1.0f, getResources().getDisplayMetrics());
    }
}
