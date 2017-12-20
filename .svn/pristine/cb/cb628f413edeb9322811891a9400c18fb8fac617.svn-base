package com.sina.book.reader.model;

import android.view.MotionEvent;

import com.sina.book.data.BookSummary;

public class BookSummaryPostion {
    public BookSummary relateBookSummary;
    public float startTop = -1;
    public float startLeft = -1;
    public float endRight;
    public float endBottom;
    public float oneLineHeight;
    
    public boolean isLegal() {
        return startTop > 0 && startLeft > 0 && endRight > 0 && endBottom > 0;
    }

    public void setPostion(float startTop, float startLeft, float endRight,
            float endBottom) {
        if (isStartNotSeted()) {
            setStart(startTop, startLeft);
        }

        setEnd(endRight, endBottom);
    }

    public boolean contain(MotionEvent e) {
        if (e == null) {
            return false;
        }
        float x = e.getX();
        float y = e.getY();
        if (y >= startTop && y <= endBottom) {
            // 第一行的情况
            if (y <= startTop + oneLineHeight) {
                if (x > startLeft) {
                    return true;
                } else {
                    return false;
                }
            }

            // 最后一行的情况
            if (y >= endBottom - oneLineHeight) {
                if (x < endRight) {
                    return true;
                } else {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean isStartNotSeted() {
        return startTop == -1 && startLeft == -1;
    }

    private void setStart(float startTop, float startLeft) {
        this.startTop = startTop;
        this.startLeft = startLeft;
    }

    private void setEnd(float endRight, float endBottom) {
        this.endRight = endRight;
        this.endBottom = endBottom;
    }
    
    @Override
    public String toString() {
        return "[startTop:" + startTop + ",startLeft:" + startLeft
                + ",endRight:" + endRight + ",endBottom:" + endBottom + "]";
    }

}
