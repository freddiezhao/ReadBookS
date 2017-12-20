package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class DispatchPressedRelativeLayout extends RelativeLayout {
    public DispatchPressedRelativeLayout(Context context) {
        super(context);
    }

    public DispatchPressedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DispatchPressedRelativeLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
        dispatchSetPressed(isPressed());
    }
}
