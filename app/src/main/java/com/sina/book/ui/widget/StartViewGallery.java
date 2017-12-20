package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * 
 * 使用gallery的方式显示引导图
 * @author Tsimle
 *
 */
public class StartViewGallery extends Gallery {
    
    public StartViewGallery(Context context) {
        super(context);
        setStaticTransformationsEnabled(true);
    }

    public StartViewGallery(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        setStaticTransformationsEnabled(true);
    }

    public StartViewGallery(Context paramContext,
            AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        setStaticTransformationsEnabled(true);
    }

    public boolean onFling(MotionEvent paramMotionEvent1,
            MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
        return true;
    }

    public boolean onScroll(MotionEvent paramMotionEvent1,
            MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
        boolean bool;
        if (paramMotionEvent1.getX() - paramMotionEvent2.getX() <= 10.0F) {
            if (paramMotionEvent1.getX() - paramMotionEvent2.getX() >= 10.0F)
                bool = true;
            else
                bool = super.onScroll(paramMotionEvent1, paramMotionEvent2,
                        paramFloat1, paramFloat2);
        } else
            bool = super.onScroll(paramMotionEvent1, paramMotionEvent2,
                    paramFloat1, paramFloat2);
        return bool;
    }
}
