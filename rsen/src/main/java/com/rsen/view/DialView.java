package com.rsen.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 大转盘
 * Created by angcyo on 15-12-18 018 15:09 下午.
 */
public class DialView extends View {
    @ColorInt
    private int[] mColors = new int[]{Color.RED, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GREEN, Color.YELLOW};//转盘每个块区域对应的颜色

    private String[] mTexts = new String[]{"一等奖", "二12等奖", "三阿萨德等奖", "四adf等奖", "五12sdf等奖", "六1ff等奖"};//转盘每个块区域对应的文本
    private float mTextSize = 60f;
    private float[] mRatios = new float[]{1, 2, 3, 2, 1, 3};//转盘每个块区域对应的大小比例

    private TextPaint mTextPaint;//文本画笔

    //转换之后的角度
    private float[] angles;
    //可绘制区域
    private Rect dialRect;
    //文本绘制偏移比例
    private float mTextOffset = 0.3f;

    private int mDialStartDegree = 15;//转盘开始时,旋转的角度
    private boolean mDialStart = true;
    private boolean mDialEnd = false;//开始/结束旋转的状态


    public DialView(Context context) {
        this(context, null);
    }

    public DialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    /**
     * 测量文本范围
     */
    public static void getTextBounds(Paint paint, String text, Rect bounds) {
        Rect textRound = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRound);
        float width = paint.measureText(text);
        bounds.set(textRound.left, textRound.top, (int) (textRound.left + width), textRound.bottom);
    }

    /**
     * @see com.rsen.view.DialView#getTextBounds(Paint, String, Rect)
     */
    public static Rect getTextBounds(Paint paint, String text) {
        Rect textRound = new Rect();
        getTextBounds(paint, text, textRound);
        return textRound;
    }

    private void init() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
//        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
    }

    /**
     * 需要滚动到那个色块,不受当前是哪个色块的影响
     */
    public void startDial(int index) {
        if (index < 0 || index >= mRatios.length) {
            throw new IllegalArgumentException("index is nullity");
        }

        if (!mDialStart && mDialEnd) {
            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    Log.e("tag", "" + interpolatedTime);
                }
            };
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);

        //固定大小
        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(size, size);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        testDraw(canvas);

//        mTextPaint.setColor(mColors[0]);
//        Rect dialRect = new Rect();
//        getDialRect(dialRect);
//        canvas.drawRect(dialRect, mTextPaint);
        //测量一些值
        angles = rationsToAngle();
        dialRect = getDialRect();

        //开始时有一个角度
        tranToCenter(canvas, dialRect);
        canvas.rotate(mDialStartDegree);


        drawDialArea(canvas);
        drawDialText(canvas);

//        if (mDialStart) {
//            mDialStartDegree += 20;//闪的很快
//            postInvalidateDelayed(40);//转的很快
////            invalidate();
//        }
    }

    private void drawDialText(Canvas canvas) {
        //绘制色块上对应的文本
        canvas.save();
//        tranToCenter(canvas, dialRect);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);//居中绘制文本

        int radius = dialRect.width() / 2;//半径
        float textCenterX = radius / 2 + radius / 2 * mTextOffset;//文本偏移之后的横向中心坐标

        float angle = 0;//需要旋转的角度
        for (int i = 0; i < mTexts.length; i++) {
            angle = angles[i] / 2;
            canvas.rotate(angle);
            String text = mTexts[i];
            Rect textBound = getTextBounds(mTextPaint, text);
            canvas.drawText(text, textCenterX, textBound.height() / 2, mTextPaint);
            canvas.rotate(angle);
        }
        canvas.restore();
    }

    private void drawDialArea(Canvas canvas) {
        //绘制色块区域
        canvas.save();
//        tranToCenter(canvas, dialRect);

        RectF rectF = new RectF(-dialRect.width() / 2, -dialRect.height() / 2, dialRect.width() / 2, dialRect.height() / 2);//扇形绘制区域
        float startAngle = 0;
        for (int i = 0; i < angles.length; i++) {
            float endAngle = angles[i];
            mTextPaint.setColor(mColors[i]);
            canvas.drawArc(rectF, startAngle, endAngle, true, mTextPaint);
            startAngle += endAngle;
        }

        canvas.restore();
    }

    private Rect tranToCenter(Canvas canvas) {
        Rect dialRect = getDialRect();
        tranToCenter(canvas, dialRect);
        return dialRect;
    }

    private void tranToCenter(Canvas canvas, Rect dialRect) {
        //讲绘图坐标移至view的中心点
        canvas.translate(dialRect.centerX(), dialRect.centerY());
    }

    private float[] rationsToAngle() {
        //讲色块比例,转换成 角度
        float sum = 0;
        float avg = 0;
        for (float ratio : mRatios) {
            sum += ratio;
        }
        avg = Math.round(360f / sum);

        int len = mRatios.length;
        float[] angles = new float[len];
        for (int i = 0; i < len; i++) {
            angles[i] = mRatios[i] * avg;
        }

        return angles;
    }

    private void testDraw(Canvas canvas) {
        Rect textRound = new Rect();
        getTextBounds(mTextPaint, mTexts[0], textRound);

        mTextPaint.setColor(mColors[0]);
        canvas.drawRect(new Rect(100, 100 - textRound.height(), 100 + textRound.width(), 100), mTextPaint);

        canvas.translate(100, 100);
        canvas.save();
        canvas.rotate(-45f);
        mTextPaint.setColor(mColors[4]);
        canvas.drawText(mTexts[0], 100, 100, mTextPaint);

        mTextPaint.setColor(mColors[1]);
        canvas.drawCircle(100, 100, 1, mTextPaint);

        mTextPaint.setColor(mColors[2]);
        canvas.drawRect(new Rect(10, 10, textRound.width(), textRound.height()), mTextPaint);

        canvas.restore();
//        mTextPaint.setColor(Color.BLACK);
        canvas.drawRect(new Rect(0, 0, 800, 800), mTextPaint);
        mTextPaint.setColor(Color.BLACK);

        canvas.drawArc(new RectF(-400, -400, 400, 400), 0, 30, true, mTextPaint);
    }

    private Rect getDialRect() {
        //获取转盘可绘制rect
        int width, height;
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        return new Rect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom);
    }


}