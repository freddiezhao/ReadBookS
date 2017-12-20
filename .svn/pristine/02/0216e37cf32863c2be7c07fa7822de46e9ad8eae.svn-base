package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.sina.book.util.PixelUtil;

/**
 * 拦截下方进入书城条的fling事件
 * 
 * @author Tsimle
 * 
 */
public class NewMainRelativeLayout extends RelativeLayout {

    private OnFlingUpListener mUpListener;
    private OnFlingDownListener mDownListener;
    private GestureDetector mUpDetector = new GestureDetector(getContext(), new DefinedUpGestureListener());
    private GestureDetector mDownDetector = new GestureDetector(getContext(), new DefinedDownGestureListener());
    /**
     * Gesture 参数
     */
    private float mMinimumFling;

    public NewMainRelativeLayout(Context context) {
        super(context);
        init();
    }

    public NewMainRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NewMainRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mMinimumFling = PixelUtil.dp2px(50);
    }

    public void setOnFlingUpListener(OnFlingUpListener listener) {
        this.mUpListener = listener;
    }
    
    public void setOnFlingDownListener(OnFlingDownListener listener) {
        this.mDownListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mUpDetector.onTouchEvent(event)) {
            return true;
        }
        if (mDownDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mUpDetector.onTouchEvent(event)) {
            return true;
        }
        if (mDownDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private class DefinedUpGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mUpListener == null) {
                return false;
            }

            if (e1.getY() - e2.getY() > mMinimumFling) {
                mUpListener.onFlingUp();
                return true;
            }

            return false;
        }
    }
    
    private class DefinedDownGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mDownListener == null) {
                return false;
            }

            if (e2.getY() - e1.getY() > mMinimumFling) {
                mDownListener.onFlingDown();
                return true;
            }

            return false;
        }
    }

    public static interface OnFlingUpListener {
        public void onFlingUp();
    }
    
    public static interface OnFlingDownListener {
        public void onFlingDown();
    }
}
