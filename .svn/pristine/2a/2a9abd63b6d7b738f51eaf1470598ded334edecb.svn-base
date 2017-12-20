package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * 
 * 主页最近阅读布局支持进入书房的手势<br>
 * 
 * @author Tsimle
 * 
 */
public class LastReadRelativeLayout extends RelativeLayout {

    private GestureDetector mDetector = new GestureDetector(getContext(),
            new DefinedGestureListener());
    private OnEnterBookhomeListener mOnEnterBookhomeListener;
    /**
     * Gesture 参数
     */
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private int mMinimumFlingVelocity;

    public LastReadRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public LastReadRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LastReadRelativeLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setOnEnterBookhomeListener(
            OnEnterBookhomeListener onEnterBookhomeListener) {
        this.mOnEnterBookhomeListener = onEnterBookhomeListener;
    }

    private void init(Context context) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private class DefinedGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if (mOnEnterBookhomeListener == null
                    || Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            // right to left swipe
            if (e1.getX() - e2.getX() > 30
                    && Math.abs(velocityX) > mMinimumFlingVelocity) {
                mOnEnterBookhomeListener.onEnterBookhome();
                return true;
            }

            return false;
        }
    }

    public static interface OnEnterBookhomeListener {
        public void onEnterBookhome();
    }

}
