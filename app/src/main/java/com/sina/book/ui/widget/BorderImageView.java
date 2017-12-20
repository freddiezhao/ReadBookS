package com.sina.book.ui.widget;

import com.sina.book.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 带边框的圆角ImageView
 * 
 * @author MarkMjw
 * @date 2013-2-17
 */
public class BorderImageView extends ImageView {
//    private static final String TAG = "BorderImageView";
    
    private Bitmap mSelectBitmap;
    private boolean mSelected = false;

    public BorderImageView(Context context) {
        super(context);
    }

    public BorderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BorderImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void createSelectBitmap() {
        if (null == mSelectBitmap) {
            Bitmap selectBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.read_theme_selected);
            mSelectBitmap = selectBitmap;
        }
    }
    
    /**
     * 是否选中
     * 
     * @return
     */
    public boolean isSelect() {
        return this.mSelected;
    }

    /**
     * 设置是否选中
     * 
     * @param selected
     */
    public void setSelect(boolean selected) {
        this.mSelected = selected;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.mSelected) {
            createSelectBitmap();

            canvas.drawBitmap(mSelectBitmap, 0, 0, null);
        }
    }
}