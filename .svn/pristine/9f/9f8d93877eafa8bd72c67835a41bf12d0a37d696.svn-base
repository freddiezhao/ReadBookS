package com.sina.book.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.sina.book.R;

public class ImageFlowIndicator extends View implements FlowIndicator {
    private ViewFlow viewFlow;
    private int mPostion = 0;
    private Paint mPaint;
    private Bitmap mNormal;
    private Bitmap mIndex;
    private int diameterW;
    private int diameterH;

    public ImageFlowIndicator(Context context) {
        super(context);
        initView(context);
    }

    public ImageFlowIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mPaint = new Paint();
        Resources resources = context.getResources();
        mNormal = BitmapFactory.decodeResource(resources, R.drawable.indicator_circle_normal);
        mIndex = BitmapFactory.decodeResource(resources, R.drawable.indicator_circle_index);

        diameterW = mNormal.getWidth();
        diameterH = mNormal.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // We were told how big to be
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        // Measure the height
        else {
            result = diameterH + getPaddingTop() + getPaddingBottom() + 1;
            // Respect AT_MOST value if that was what is called for by
            // measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // We were told how big to be
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        // Calculate the width according the views count
        else {
            int count = 4;
            if (viewFlow != null) {
                count = viewFlow.getViewsCount();
            }
            result = getPaddingLeft() + getPaddingRight() + (count * diameterW) + (count - 1) * diameterW + 1;
            // Respect AT_MOST value if that was what is called for by
            // measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = 3;
        if (viewFlow != null) {
            count = viewFlow.getViewsCount();
        }
        if (count == 0) {
            return;
        }
        for (int iLoop = 0; iLoop < count; iLoop++) {
            canvas.drawBitmap(mNormal, getPaddingLeft() + (iLoop * (2 * diameterW)), getPaddingTop(), mPaint);
            if (iLoop == mPostion) {
                canvas.drawBitmap(mIndex, getPaddingLeft() + (iLoop * (2 * diameterW)), getPaddingTop(), mPaint);
            }
        }
    }

    @Override
    public void setViewFlow(ViewFlow view) {
        viewFlow = view;
        invalidate();
    }

    @Override
    public void onSwitched(View view, int position) {
        mPostion = position;
        invalidate();
    }

}
