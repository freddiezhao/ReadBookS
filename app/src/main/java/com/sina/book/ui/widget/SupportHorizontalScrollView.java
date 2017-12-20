package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * 搭配HorizontalListView使用
 * 
 * @author Tsimle
 * 
 */
public class SupportHorizontalScrollView extends ScrollView {

    private boolean mEventInHorizontal;
    private boolean mNotIntercept = true;

    private float mLastMotionX;
    private float mLastMotionY;

    public SupportHorizontalScrollView(Context context) {
        super(context);
    }

    public SupportHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SupportHorizontalScrollView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean superHandledOut = super.onInterceptTouchEvent(ev);

        final int action = ev.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mEventInHorizontal = inAHorizontalChild(ev.getX(), ev.getY());
            mLastMotionX = ev.getX();
            mLastMotionY = ev.getY();
            mNotIntercept = true;
            break;
        case MotionEvent.ACTION_MOVE:
            final int yDiff = (int) Math.abs(ev.getY() - mLastMotionY);
            final int xDiff = (int) Math.abs(ev.getX() - mLastMotionX);
            if (xDiff > 1.6 * yDiff && mEventInHorizontal) {
                mNotIntercept = false;
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mEventInHorizontal = false;
            mNotIntercept = true;
            break;
        }
        return superHandledOut && mNotIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            mEventInHorizontal = false;
            mNotIntercept = true;
        }
        return super.onTouchEvent(ev);
    }

    private boolean inAHorizontalChild(float x, float y) {
        View inChild = null;
        for (int i = 0; i < getChildCount(); i++) {
            final int scrollY = getScrollY();
            final View child = getChildAt(i);
            boolean isIn = !(y < child.getTop() - scrollY
                    || y >= child.getBottom() - scrollY || x < child.getLeft() || x >= child
                    .getRight());
            if (isIn) {
                inChild = child;
                break;
            }
        }
        return inChild != null && findAHorizontalChildInViewTree(inChild);
    }

    private static boolean findAHorizontalChildInViewTree(View v) {
        if (v instanceof HorizontalListView) {
            return true;
        }
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for (int i = 0; i < group.getChildCount(); i++) {
                final View child = group.getChildAt(i);
                if (findAHorizontalChildInViewTree(child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
