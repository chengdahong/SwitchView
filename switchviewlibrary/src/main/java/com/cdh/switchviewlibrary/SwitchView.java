package com.cdh.switchviewlibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwitchView extends View {

    private Paint mBgPaint;

    private Paint mTextPaint;

    private Paint mThumbPaint;

    private int mWidth;
    private int mHeight;

    private RectF mRoundRectF;

    private float mThumbX;
    private float mThumbY;
    private int mThumbR;

    private float mThumbStartX;

    private String mText;

    private Rect mTextBound;

    private int mTextX;
    private int mTextY;

    private String mTextOn; // 打开是文字
    private String mTextOff; // 关闭文字

    private int mBgColor;
    private int mThumbColor;
    private int mTextColor;

    private Status mCurrStatus = Status.OFF;

    private boolean mIsChanging = false;

    private enum Status {
        ON,
        OFF
    }

    private ValueAnimator mValueAnimator;

    private OnCheckedChangeListener mListener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(View view, boolean changed);
    }

    public SwitchView(Context context) {
        this(context, null);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwitchView);
        mBgColor = ta.getColor(R.styleable.SwitchView_bgcolor, Color.GRAY);
        mThumbColor = ta.getColor(R.styleable.SwitchView_thumbcolor, Color.LTGRAY);
        mTextColor = ta.getColor(R.styleable.SwitchView_textcolor, Color.WHITE);
        mTextOff = ta.getString(R.styleable.SwitchView_off);
        mTextOn = ta.getString(R.styleable.SwitchView_on);
        Log.d("chengdh", "On: " + mTextOn + ", oFF: " + mTextOff);
        ta.recycle();

        mWidth = 200;//DensityUtil.px2dip(context, 150);
        mHeight = 100; //DensityUtil.px2dip(context, 130);

        mThumbR = mHeight / 2;
        mThumbX = mThumbR;
        mThumbY = mThumbR;

        mBgPaint = new Paint();
        mBgPaint.setColor(mBgColor);
        mBgPaint.setAntiAlias(true);

        mThumbPaint = new Paint();
        mThumbPaint.setColor(mThumbColor);
        mThumbPaint.setAntiAlias(true);

        int textSize = mHeight * 2 / 5;
        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);

        mRoundRectF = new RectF(0, 0, mWidth, mHeight);
        mTextBound = new Rect();
        changeTextStatus();
        mTextY = mHeight / 2 + mTextBound.height() / 2;

        mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        mValueAnimator.setDuration(200);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (mCurrStatus == Status.OFF) { // 要从右到左
                    value = -value;
                }
                mThumbX = mThumbStartX + (mWidth - mThumbR - mThumbR) * value; // 初始位置和结束位置的距离
                invalidate();
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsChanging = false;
                changeTextStatus();
                if (null != mListener) {
                    if (mCurrStatus == Status.OFF) {
                        mListener.onCheckedChanged(SwitchView.this, false);
                    } else {
                        mListener.onCheckedChanged(SwitchView.this, true);
                    }
                }
            }
        });
    }

    private void changeTextStatus() {
        if (mCurrStatus == Status.ON) { // 如果是off状态
            if (mTextOff == null) {
                mTextOff = getResources().getString(R.string.off);
            }
            mText = mTextOff;
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
            mTextX = (mWidth - mThumbR * 2 - mTextBound.width()) / 2;
        } else {
            if (mTextOn == null) {
                mTextOn = getResources().getString(R.string.on);
            }
            mText = mTextOn;
            mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
            mTextX = mThumbR * 2 + (mWidth - mThumbR * 2 - mTextBound.width()) / 2;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        setMeasuredDimension(widthMode == MeasureSpec.AT_MOST ? mWidth : widthSize
                , heightMode == MeasureSpec.AT_MOST ? mWidth : heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        // 画背景
        canvas.drawRoundRect(mRoundRectF, mHeight / 2, mHeight / 2, mBgPaint);
        // 画thumb
        canvas.drawCircle(mThumbX, mThumbY, mThumbR, mThumbPaint);
        // 写字 打开、关闭
        if (!mIsChanging) {
            canvas.drawText(mText, mTextX, mTextY, mTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mIsChanging) {
                    changeStatus();
                }
                break;
        }
        return true;
    }

    private void changeStatus() {
        mIsChanging = true;
        if (mCurrStatus == Status.OFF) { // 如果是off状态
            mCurrStatus = Status.ON;
        } else {
            mCurrStatus = Status.OFF;
        }
        mThumbStartX = mThumbX;
        mValueAnimator.start();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mListener = listener;
    }
}
