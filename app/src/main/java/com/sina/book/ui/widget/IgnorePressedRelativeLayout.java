package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * ListView Gallery item的button中相应item的点击效果
 * @author Tsimle
 *
 */
public class IgnorePressedRelativeLayout extends RelativeLayout {

    public IgnorePressedRelativeLayout(Context context) {
        super(context);
    }

    public IgnorePressedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IgnorePressedRelativeLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public void setPressed(boolean pressed) {
        //忽略上层派发的pressed动作
    }
}
