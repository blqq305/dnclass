package com.android.dongnaovip2017.activity.superui;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.android.dongnaovip2017.R;
import com.android.dongnaovip2017.lib.LoveFlowerBuilder;
import com.android.dongnaovip2017.view.Lsn16LoveBezierView;

import java.util.ArrayList;
import java.util.List;

public class Lsn16Task1 extends AppCompatActivity implements LoveFlowerBuilder.LoveFlower.LoveFlowerListener {

    private int mScreenHeight;
    private int mScreenWidth;

    private List<LoveFlowerBuilder.LoveFlower> mLoveFlowerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lsn16_task1);

        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;

        mLoveFlowerList = new ArrayList<>();
    }

    public void startLove(View view) {

        int width = (int) (50 + (70 * Math.random()));
        int height = width;

        Lsn16LoveBezierView lsn16LoveBezierView = new Lsn16LoveBezierView(this);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.width = width;
        lp.height = height;

        lsn16LoveBezierView.setLayoutParams(lp);

        lsn16LoveBezierView.setColor(getColor(), getColor());

        ((ViewGroup) view.getParent()).addView(lsn16LoveBezierView);


        // 指定FloveFlower动画执行的范围区域
        Rect rect = new Rect(0, 0, mScreenWidth, mScreenHeight);

        // 通过LoveFlowerBuilder制造器，制造FloveFlower
        LoveFlowerBuilder.LoveFlower lf = LoveFlowerBuilder.make(lsn16LoveBezierView, rect, 6000).send();
        lf.setLoveFlowerListener(Lsn16Task1.this);

        mLoveFlowerList.add(lf);
    }

    public int getColor() {
        return (int) ((int) (255 * (float) Math.random()) << 24) |
                (int) ((int) ((float) Math.random() * 255) << 16) |
                (int) ((int) ((float) Math.random() * 255) << 8) |
                (int) ((int) ((float) Math.random() * 255));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoveFlowerList != null) {
            for (LoveFlowerBuilder.LoveFlower lf : mLoveFlowerList) {
                lf.setLoveFlowerListener(null);
                lf.recycle();
            }
            mLoveFlowerList.clear();
        }
        mLoveFlowerList = null;
    }

    @Override
    public void onRecycle(LoveFlowerBuilder.LoveFlower loveFlower) {
        if (mLoveFlowerList != null && mLoveFlowerList.contains(loveFlower)) {
            mLoveFlowerList.remove(loveFlower);
//            Log.e("Lsn16Task1", "onRecycle " + mLoveFlowerList.contains(loveFlower));
        }
    }
}
