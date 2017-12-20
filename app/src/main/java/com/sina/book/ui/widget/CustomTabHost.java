package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabHost;

/**
 * 解决tabHost初始化默认设置第0项选项卡并加载
 * @author Tsimle
 *
 */
public class CustomTabHost extends TabHost {
    private boolean mInInitStep;

    public CustomTabHost(Context context) {
        super(context);
    }

    public CustomTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInInitStep(boolean inInitStep) {
        mInInitStep = inInitStep;
    }

    @Override
    public void setCurrentTab(int index) {
        if (mInInitStep) {
            return;
        }
        super.setCurrentTab(index);
    }
}
