package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class IgnorePressedLinearLayout extends LinearLayout {

    public IgnorePressedLinearLayout(Context context) {
        super(context);
    }

    public IgnorePressedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setPressed(boolean pressed) {
        // 忽略上层派发的pressed动作
    }
}
