package com.rsen.viewgroup;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * 波纹过渡效果
 * Created by angcyo on 2016-09-05.
 */
public class CircleTransitionLayout extends RelativeLayout {

    Path clipPath = new Path();
    float clipStartX = 0f, clipStartY = 0f, clipStartRadius = 100f;
    ValueAnimator mClipValueAnimator;
    boolean enableClip = false;

    public CircleTransitionLayout(Context context) {
        super(context);
    }

    public CircleTransitionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleTransitionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleTransitionLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setClip() {
        enableClip = true;
        post(new Runnable() {
            @Override
            public void run() {
                setClip(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            }
        });
    }

    public void setClip(float x, float y) {
        enableClip = true;
        setClip(x, y, clipStartRadius);
    }

    public void setClip(float x, float y, float radius) {
        enableClip = true;
        clipStartX = x;
        clipStartY = y;
        clipStartRadius = radius;
        startClip();
    }

    @Override
    public void draw(Canvas canvas) {
        if (enableClip) {
            canvas.clipPath(clipPath);
        }
        super.draw(canvas);
    }

    //------------------------------

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mClipValueAnimator.cancel();
        mClipValueAnimator = null;
    }

    private void startClip(){
        post(new Runnable() {
            @Override
            public void run() {
                initAnimator();
                mClipValueAnimator.start();
            }
        });
    }

    private void updateClipPath(float radius) {
        clipPath.reset();
        clipPath.addCircle(clipStartX, clipStartY, radius, Path.Direction.CW);
        invalidate();
    }

    private void initAnimator() {
        if (mClipValueAnimator == null) {
            final float endRadius = calcEndRadius();
            mClipValueAnimator = ObjectAnimator.ofFloat(clipStartRadius, endRadius);
            mClipValueAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
//            mClipValueAnimator.setDuration(5000);
            mClipValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float progress = (float) valueAnimator.getAnimatedValue();
                    Log.e("angcyo", "progress-->" + progress);
                    updateClipPath(clipStartRadius + progress);
                }
            });
        }
    }

    private float calcEndRadius() {
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();
        //开始点与左上角的距离
        float rLT = c(Math.abs(clipStartX), Math.abs(clipStartY));
        float rRT = c(Math.abs(viewWidth - clipStartX), Math.abs(clipStartY));
        float rLB = c(Math.abs(clipStartX), Math.abs(viewHeight - clipStartY));
        float rRB = c(Math.abs(viewWidth - clipStartX), Math.abs(viewHeight - clipStartY));

        return Math.max(Math.max(Math.max(rLT, rRT), rLB), rRB);
    }

    //勾股定理
    private float c(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
    }
}
