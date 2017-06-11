package com.android.dongnaovip2017.lib;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.dongnaovip2017.view.ReBoundView;

/**
 * Created by Lingo on 2017/5/22.
 * 回弹抽屉
 */

public class ReBoundDrawer implements ReBoundView.BoundListenner {

    // 抽屉布局
    private View vDrawerLayout;

    // 包裹抽屉容器
    private FrameLayout vContainer;

    // 创建一个View，作为遮罩层
    private View mDiscover;

    // 布局容器
    private FrameLayout vContentParent;

    // 回弹View
    private ReBoundView vReBoundView;

    private boolean isShow;

    // 回弹View的回弹幅度高度
    private int mBoundHeight;
    // 回弹View的高度
    private int mReboundViewHeight;
    // 包裹抽屉容器顶部内边距高度
    private int mContainerPaddingTop;

    private ReBoundDrawer(@NonNull View activityView, @LayoutRes int drawerLayout) {
        Context context = activityView.getContext();
        // 查找布局容器，有了布局容器才能显示抽屉
        vContentParent = findContentParent(activityView);
        // 解析抽屉layout，传入vContentParent仅仅是为了解析出来的vDrawerLayout被设置了LayoutParams
        vDrawerLayout = LayoutInflater.from(context).inflate(drawerLayout, vContentParent, false);
        ((FrameLayout.LayoutParams) vDrawerLayout.getLayoutParams()).gravity = Gravity.BOTTOM;
        if (vDrawerLayout.getLayoutParams().height == LinearLayout.LayoutParams.WRAP_CONTENT) {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(vContentParent.getWidth(), View.MeasureSpec.EXACTLY);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(vContentParent.getHeight(), View.MeasureSpec.AT_MOST);
            vDrawerLayout.measure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(vContentParent.getWidth(), View.MeasureSpec.EXACTLY);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(vDrawerLayout.getLayoutParams().height, View.MeasureSpec.EXACTLY);
            vDrawerLayout.measure(widthMeasureSpec, heightMeasureSpec);
        }
        // 创建包裹抽屉容器
        FrameLayout.LayoutParams lyParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vContainer = new FrameLayout(context);
        lyParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        vContainer.setLayoutParams(lyParams);
        mContainerPaddingTop = getPx(context, 30);
//        vContainer.setPadding(0, mContainerPaddingTop, 0, 0);

        // 创建一个View，作为遮罩层，可用来设置一个有一定透明度的遮罩层
        mDiscover = new View(context);
        mDiscover.setAlpha(0f);
        mDiscover.setBackgroundColor(Color.parseColor("#88333333"));

        // 创建回弹View
        lyParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vReBoundView = new ReBoundView(context);
        mBoundHeight = getPx(context, 60);
        mReboundViewHeight = mBoundHeight + vDrawerLayout.getMeasuredHeight();
        lyParams.height = mReboundViewHeight;
        lyParams.gravity = Gravity.BOTTOM;
        vReBoundView.setLayoutParams(lyParams);
        vReBoundView.setMaxBoundHeight(mBoundHeight);
        vReBoundView.setClickable(true);
        vReBoundView.setBoundListenner(this);
        // 把遮罩层、抽屉layout 和回弹View添加进包裹抽屉容器
        vContainer.addView(mDiscover);
        vContainer.addView(vReBoundView);
        vContainer.addView(vDrawerLayout);
    }

    private FrameLayout findContentParent(View activityView) {
        if (vContentParent != null) {
            return vContentParent;
        }

        // 从Activity布局的中的一个View向上查找父View，直到找到ContentParent或父View==null
        View view = activityView;
        while (view != null) {
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (FrameLayout) view;
                }
            }

            // parent可能是ViewRootImpl
            ViewParent parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }

        return null;
    }

    public static ReBoundDrawer makeDrawer(@NonNull View activityView, @LayoutRes int drawerLayout) {
        return new ReBoundDrawer(activityView, drawerLayout);
    }

    public ReBoundDrawer show() {
        if (vContentParent == null || isShow) {
            return this;
        }
        // 把抽屉容器添加到布局容器
        if (vContainer.getParent() == vContentParent) {
            vContentParent.removeView(vContainer);
        }
        isShow = true;
        vDrawerLayout.setVisibility(View.INVISIBLE);
        vContentParent.addView(vContainer);
        vReBoundView.startBound(ReBoundView.BOUND_ACTION_OPEN);
        drawerLayoutIn();
        return this;
    }

    /**
     * 设置背景颜色，回弹效果的颜色
     *
     * @param color
     */
    public ReBoundDrawer setReBoundBgColor(@ColorInt int color) {
        if (vReBoundView != null) {
            vReBoundView.setBgColor(color);
        }
        return this;
    }

    /**
     * 设置是否带效果，如果设置false，就看不到ReBound效果，默认true
     * @param reBound
     * @return
     */
    public ReBoundDrawer setReBound(boolean reBound){
        if (vReBoundView != null) {
            vReBoundView.setReBound(reBound);
        }
        return this;
    }

    public void close() {
        // 移除抽屉
        if (vContentParent != null) {
            vReBoundView.startBound(ReBoundView.BOUND_ACTION_CLOSE);
            drawerLayoutOut();
        }
    }

    private void drawerLayoutOut() {
        Animation animation = new AlphaAnimation(1.0f, 0f);
        animation.setDuration(400);

        TranslateAnimation ta = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1.0f
        );
        ta.setDuration(400);

        AnimationSet as = new AnimationSet(false);
        as.addAnimation(animation);
        as.addAnimation(ta);
        as.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vDrawerLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        vDrawerLayout.startAnimation(as);
    }

    private void drawerLayoutIn() {
        Animation animation = new AlphaAnimation(0f, 1.0f);
        animation.setDuration(200);
        animation.setStartOffset(100);

        TranslateAnimation ta = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0f
        );
        ta.setDuration(300);
        ta.setStartOffset(150);
        ta.setInterpolator(new BounceInterpolator());

        AnimationSet as = new AnimationSet(false);
        as.addAnimation(animation);
        as.addAnimation(ta);

        vDrawerLayout.startAnimation(as);
    }

    public View getDrawerContainer() {
        return vContainer;
    }

    public boolean isShow() {
        return isShow;
    }

    public int getPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp * 1.0f, context.getResources().getDisplayMetrics());
    }

    @Override
    public void onBoundFinish(int action) {
        if (isShow()) {
            if (action == ReBoundView.BOUND_ACTION_OPEN) {
                vDrawerLayout.setVisibility(View.VISIBLE);
                mDiscover.setAlpha(0.8f);
            } else {
                mDiscover.setAlpha(0f);
                // 移除抽屉
                if (vContentParent != null) {
                    vContentParent.removeView(vContainer);
                }
                isShow = false;
            }
        }
    }

    @Override
    public void onBoundFraction(int action, float fraction) {
        Log.e("ReBoundDrawer", "onBoundFraction fraction = " + fraction);
        if (action == ReBoundView.BOUND_ACTION_OPEN) {
            mDiscover.setAlpha(fraction * 0.8f);
        } else {
            mDiscover.setAlpha((1.0f - fraction) * 0.8f);
        }
    }
}
