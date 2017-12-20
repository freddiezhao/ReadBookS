package com.sina.book.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleImageView extends ImageView {

    private Paint mPaint;

    public CircleImageView(Context context) {
        super(context);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (mPaint == null) {
            mPaint =  new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Style.STROKE);
            mPaint.setColor(Color.WHITE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int saveCount = canvas.getSaveCount();
        canvas.save();
        Path path = new Path();
        int w = (getWidth() - 1) / 2;
        int h = (getHeight() - 1) / 2;
        int c = Math.min(w, h);
        path.addCircle(w, h, c, Path.Direction.CCW);
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
        //canvas.drawCircle(w, h, c, mPaint);
    }

}
