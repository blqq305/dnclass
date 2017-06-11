package com.android.dongnaovip2017.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by user on 2017/6/10.
 * 爱心花制造器
 */

public class LoveFlowerBuilder {

    /**
     * 暴露一个make方法，制造器通过这个make方法制造出真正的LoveFlower对象
     *
     * @param flower
     * @return
     */
    public static LoveFlower make(@NonNull View flower, @NonNull Rect rect, int duration) {
        // 创建一个LoveFlower对象，并且返回
        LoveFlower loveFlower = new LoveFlower(flower, rect, duration);
        return loveFlower;
    }

    public static class LoveFlower {

        /**
         * 视图对象
         */
        private View mFlower;

        /**
         * 指定一个花朵飘动的范围
         */
        private Rect mRect;
        /**
         * 动画执行时长
         */
        private int mDuration;
        /**
         * 动画是否在执行
         */
        private boolean isAnimaRunning = false;

        private Random mR = new Random();

        /**
         * 动画列表
         */
        private List<Animator> mAnimaList;

        private ObjectAnimator mXoa;
        private ObjectAnimator mYoa;

        private LoveFlowerListener mLoveFlowerListener;

        public LoveFlower(View flower, Rect rect, int duration) {
            // 需要一个视图View来实例化，描述一朵花
            this.mFlower = flower;

            this.mRect = rect;
            this.mDuration = duration;

            mAnimaList = new ArrayList<>();
        }

        public LoveFlower send() {
            if (mFlower == null) {
                return this;
            }

            // 初始化动画，准备执行
            initYobjectAnimator();
            // 因为是从头开始，不是接着上一个动画进行下一轮动画，所以第一个参数填false
            // 第二个参数填什么都不重要了，从头开始，不会用到这个值
            initXobjectAnimator(false, 0);

            // 设置一个动画集
            AnimatorSet as = new AnimatorSet();
            // 开始播放动画列表
            as.playTogether(mAnimaList);
            // 记得要调用AnimatorSet的start开始播放动画集的所有动画
            as.start();

            return this;
        }

        private void initYobjectAnimator() {
            // 开始值，从底端开始
            float start = mRect.bottom;
            // 结束值，到完成超过顶端
            float end = mRect.top;

            mYoa = ObjectAnimator.ofFloat(mFlower, "Y", start, end);
            mYoa.setDuration(mDuration);
            mYoa.setInterpolator(new LinearInterpolator());
            mYoa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float pass = (float) animation.getAnimatedValue();

                    // 剩下mFlower高度的距离时开始设置透明度，当到达终点时，变成完全透明
                    if (pass - mFlower.getMeasuredHeight() < mRect.top) {
                        float alpha = (pass - mRect.top) / mFlower.getMeasuredHeight();
                        mFlower.setAlpha(alpha);
                    }
                }
            });
            mYoa.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    isAnimaRunning = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isAnimaRunning = false;
                    if (mXoa != null) {
                        mXoa.cancel();
                    }

                    // 执行完毕，移除View
                    ViewParent parent = mFlower.getParent();
                    if (parent != null && parent instanceof ViewGroup) {
                        ((ViewGroup) parent).removeView(mFlower);
                    }
                    mFlower = null;
                    mAnimaList.clear();
                    mXoa = null;
                    mYoa = null;
                    mRect = null;
                    mXoaListener = null;
                    if (mLoveFlowerListener != null) {
                        mLoveFlowerListener.onRecycle(LoveFlower.this);
                    }
                    mLoveFlowerListener = null;
                }
            });

            // 添加进列表，一起并行执行
            mAnimaList.add(mYoa);
        }

        private void initXobjectAnimator(boolean restart, float restartStart) {
            // 让花从底端的随机某个位置出来，也随机向左或向右漂
            // 随机某个位置进入
            float start = (float) (mRect.left + mRect.width() * Math.random());

            // 如果是动画执行完成并重复继续开始执行动画，就从上次动画结束点继续开始下一轮动画
            if (restart) {
                start = restartStart;
            }

            // 产生一个随机数，能整除2就飘向左边，否则飘向右边
            int r = mR.nextInt(100);
            float end = start;
            // 飘向左边，终点为起始点左边的随机某个点
            if (r % 2 == 0) {
                // start左边的可飘动范围 * 随机百分数
                end = mRect.left + (float) ((start - mRect.left) * Math.random());
            } else {
                // 飘向右边，终点为起始点右边的随机某个点
                end = start + (float) ((mRect.right - start) * Math.random());

                // 不要超出Right
                end = Math.min(mRect.right - mFlower.getMeasuredWidth(), end);
            }
//            Log.e("LoveFlower", "initXobjectAnimator restart = " + restart + " restartStart = " + restartStart);
//            Log.e("LoveFlower", "initXobjectAnimator start = " + start + " end = " + end);

            mXoa = ObjectAnimator.ofFloat(mFlower, "X", start, end);
            mXoa.setDuration(mDuration / 4);
            mXoa.setInterpolator(new LinearInterpolator());
            mXoa.addListener(mXoaListener);

            // 添加进列表，一起并行执行
            mAnimaList.add(mXoa);
        }

        private AnimatorListenerAdapter mXoaListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 动画执行完毕
                if (!isAnimaRunning) {
                    return;
                }

                // 动画还在执行，继续重复产生并执行动画，所以restart参数填true，
                // restartStart参数填这次动画结束时的值，因为下一轮动画就要从这个值开始
                initXobjectAnimator(true, (float) mXoa.getAnimatedValue());

                // 重新设置值，重新开始
                mXoa.start();
            }
        };

        public void recycle() {
            // mYoa是全场动画，最先开始，最后结束，他结束的时候，会取消所有动画 并回收资源
            if (mYoa != null) {
                mYoa.cancel();
            }
        }

        public void setLoveFlowerListener(LoveFlowerListener listener) {
            mLoveFlowerListener = listener;
        }

        public interface LoveFlowerListener {
            void onRecycle(LoveFlower loveFlower);
        }
    }
}
