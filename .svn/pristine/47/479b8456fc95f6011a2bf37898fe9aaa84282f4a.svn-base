package com.sina.book.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.LinearLayout;

/**
 * 支持触底反馈的LinearLayout
 * 
 * @author Tsimle
 * 
 */
public class EdgeEffectLinearLayout extends LinearLayout {

    private boolean mIsBeingDragged = false;
    private int mLastMotionY;
    private int mTouchSlop;

    private EdgeEffectCopy mEdgeGlowBottom;

    public EdgeEffectLinearLayout(Context context) {
        super(context);
        initView(context);
    }

    public EdgeEffectLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mEdgeGlowBottom = new EdgeEffectCopy(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            // Remember where the motion event started
            mLastMotionY = (int) event.getY();
            break;
        }
        case MotionEvent.ACTION_MOVE:
            final int y = (int) event.getY();
            int deltaY = mLastMotionY - y;
            if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                mIsBeingDragged = true;
                if (deltaY > 0) {
                    deltaY -= mTouchSlop;
                } else {
                    deltaY += mTouchSlop;
                }
            }
            if (mIsBeingDragged && mEdgeGlowBottom != null) {
                // Scroll to follow the motion event
                mLastMotionY = y;

                final int pulledToY = deltaY;
                if (pulledToY < 0) {
                    if (!mEdgeGlowBottom.isFinished()) {
                        mEdgeGlowBottom.onRelease();
                    }
                } else if (pulledToY > 0) {
                    if (mEdgeGlowBottom != null) {
                        mEdgeGlowBottom.onPull((float) deltaY / getHeight());
                    }
                }
                if (!mEdgeGlowBottom.isFinished()) {
                    postInvalidate();
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mIsBeingDragged) {
                endDrag();
            }
            break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_MOVE: {

            final int y = (int) ev.getY();
            final int yDiff = Math.abs(y - mLastMotionY);
            if (yDiff > mTouchSlop) {
                mIsBeingDragged = true;
                mLastMotionY = y;
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }
            break;
        }

        case MotionEvent.ACTION_DOWN: {
            final int y = (int) ev.getY();
            mLastMotionY = y;
            mIsBeingDragged = false;
            break;
        }

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            /* Release the drag */
            mIsBeingDragged = false;
            break;
        }

        return mIsBeingDragged;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mEdgeGlowBottom != null) {
            if (!mEdgeGlowBottom.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth() - getPaddingLeft()
                        - getPaddingRight();
                final int height = getHeight();

                canvas.translate(-width + getPaddingLeft(), height);
                canvas.rotate(180, width, 0);
                mEdgeGlowBottom.setSize(width, height);
                if (mEdgeGlowBottom.draw(canvas)) {
                    postInvalidate();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;

        if (mEdgeGlowBottom != null) {
            mEdgeGlowBottom.onRelease();
        }
    }

}
