package com.android.dongnaovip2017.activity.superui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.dongnaovip2017.R;
import com.android.dongnaovip2017.lib.ReBoundDrawer;

public class Lsn16Task2 extends AppCompatActivity {

    private RelativeLayout vRl;

    private ReBoundDrawer mDrawer;

    private boolean isSetup = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lsn16_task2);

        vRl = (RelativeLayout) findViewById(R.id.lsn16Task2_rl);
    }

    public void startGo(View view) {
        int duration = 400;
        float scale = 0.8f;

        if (isSetup) {
            setupStart(duration, scale);
            openDrawer(view);
        } else {
            reverseStart(duration, scale);
        }

        isSetup = !isSetup;
    }

    public void openDrawer(View view) {
        if (mDrawer == null) {
            mDrawer = ReBoundDrawer.makeDrawer(view, R.layout.lsn16_task2_drawer).setReBound(false).show();
            ((ImageView) mDrawer.getDrawerContainer().findViewById(R.id.lsn16Task2Drawer_iv)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawer.close();
                    startGo(null);
                }
            });
        } else {
            mDrawer.show();
        }
    }

    private void reverseStart(int duration, float scale) {
        // 缩小宽度
        ObjectAnimator scaleXAnima = ObjectAnimator.ofFloat(vRl, "ScaleX", scale, 1.0f);
        scaleXAnima.setInterpolator(new LinearInterpolator());

        // 缩小高度
        ObjectAnimator scaleYAnima = ObjectAnimator.ofFloat(vRl, "ScaleY", scale, 1.0f);
        scaleYAnima.setInterpolator(new LinearInterpolator());

        // 移动Y值
        // 算出缩小后减小的高度然后向上移动1/2的高度，因为缩小过程中是以vRl高度中心点缩小，所有只需要移到1/2
        float subHeight = vRl.getHeight() * (1.0f - scale);
        ObjectAnimator transYAnima = ObjectAnimator.ofFloat(vRl, "Y", -subHeight / 2.0f, 0);
        transYAnima.setInterpolator(new LinearInterpolator());

        // 绕X轴旋转
        ObjectAnimator rotationXAnima = ObjectAnimator.ofFloat(vRl, "RotationX", 0f, 30.0f, 0f);
        rotationXAnima.setInterpolator(new AccelerateInterpolator());

        // 属性动画集
        AnimatorSet as = new AnimatorSet();
        as.setDuration(duration);
        as.playTogether(scaleXAnima, scaleYAnima, transYAnima, rotationXAnima);
        // 开始执行动画集
        as.start();
    }

    private void setupStart(int duration, float scale) {
        // 缩小宽度
        ObjectAnimator scaleXAnima = ObjectAnimator.ofFloat(vRl, "ScaleX", 1.0f, scale);
        scaleXAnima.setInterpolator(new LinearInterpolator());

        // 缩小高度
        ObjectAnimator scaleYAnima = ObjectAnimator.ofFloat(vRl, "ScaleY", 1.0f, scale);
        scaleYAnima.setInterpolator(new LinearInterpolator());

        // 移动Y值
        // 算出缩小后减小的高度然后向上移动1/2的高度，因为缩小过程中是以vRl高度中心点缩小，所有只需要移到1/2
        float subHeight = vRl.getHeight() * (1.0f - scale);
        ObjectAnimator transYAnima = ObjectAnimator.ofFloat(vRl, "Y", 0, -subHeight / 2.0f);
        transYAnima.setInterpolator(new LinearInterpolator());

        // 绕X轴旋转
        ObjectAnimator rotationXAnima = ObjectAnimator.ofFloat(vRl, "RotationX", 0f, 30.0f, 0f);
        rotationXAnima.setInterpolator(new AccelerateInterpolator());

        // 属性动画集
        AnimatorSet as = new AnimatorSet();
        as.setDuration(duration);
        as.playTogether(scaleXAnima, scaleYAnima, transYAnima, rotationXAnima);
        // 开始执行动画集
        as.start();
    }


}
