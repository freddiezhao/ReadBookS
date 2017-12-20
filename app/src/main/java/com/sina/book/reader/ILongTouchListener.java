package com.sina.book.reader;

import android.view.MotionEvent;

/**
 * 阅读控件长按接口
 * 
 * @author MarkMjw
 * @date 2013-3-7
 */
public interface ILongTouchListener {
    /**
     * 长按事件接口
     * 
     * @param longTouchMode
     */
    public void onLongTouch(boolean longTouchMode);
    
    /**
     * 长按状态后的touch up
     * 
     * @param event
     */
    public void onTouchUp(MotionEvent event);
}
