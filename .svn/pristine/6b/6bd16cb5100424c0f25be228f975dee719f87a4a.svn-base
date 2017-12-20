package com.sina.book.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.sina.book.R;

/**
 * A FlowIndicator which draws circles (one for each view). The current view
 * position is filled and others are only striked.<br/>
 * <br/>
 * Availables attributes are:<br/>
 * <ul>
 * fillColor: Define the color used to fill a circle (default to white)
 * </ul>
 * <ul>
 * strokeColor: Define the color used to stroke a circle (default to white)
 * </ul>
 * <ul>
 * radius: Define the circle radius (default to 4)
 * </ul>
 */
public class CircleFlowIndicator extends View implements FlowIndicator {
	private int radius = 4;
	private final Paint mPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
	private ViewFlow viewFlow;
	private int mPostion = 0;

	/**
	 * Default constructor
	 * 
	 * @param context
	 */
	public CircleFlowIndicator(Context context) {
		super(context);
		initColors(0xFFFFFFFF, 0xFFFFFFFF);
	}

	/**
	 * The contructor used with an inflater
	 * 
	 * @param context
	 * @param attrs
	 */
	public CircleFlowIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Retrieve styles attributs
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleFlowIndicator);
		// Retrieve the colors to be used for this view and apply them.
		int fillColor = a.getColor(R.styleable.CircleFlowIndicator_fillColor, 0xFFFFFFFF);
		int strokeColor = a.getColor(R.styleable.CircleFlowIndicator_strokeColor, 0xFFFFFFFF);
		// Retrieve the radius
		radius = a.getInt(R.styleable.CircleFlowIndicator_radius, 4);
		initColors(fillColor, strokeColor);
		a.recycle();
	}

	private void initColors(int fillColor, int strokeColor) {
		mPaintStroke.setStyle(Style.STROKE);
		mPaintStroke.setColor(strokeColor);
		mPaintFill.setStyle(Style.FILL);
		mPaintFill.setColor(fillColor);
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
			canvas.drawCircle(getPaddingLeft() + radius + (iLoop * (2 * radius + radius)), getPaddingTop() + radius,
					radius, mPaintStroke);
			if (iLoop == mPostion) {
				canvas.drawCircle(getPaddingLeft() + radius + (iLoop * (2 * radius + radius)),
						getPaddingTop() + radius, radius, mPaintFill);
			}
		}
	}

	@Override
	public void onSwitched(View view, int position) {
		mPostion = position;
		invalidate();
	}

	@Override
	public void setViewFlow(ViewFlow view) {
		viewFlow = view;
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
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
			int count = 3;
			if (viewFlow != null) {
				count = viewFlow.getViewsCount();
			}
			result = getPaddingLeft() + getPaddingRight() + (count * 2 * radius) + (count - 1) * radius + 1;
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
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
			result = 2 * radius + getPaddingTop() + getPaddingBottom() + 1;
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Sets the fill color
	 * 
	 * @param color
	 *            ARGB value for the text
	 */
	public void setFillColor(int color) {
		mPaintFill.setColor(color);
		invalidate();
	}

	/**
	 * Sets the stroke color
	 * 
	 * @param color
	 *            ARGB value for the text
	 */
	public void setStrokeColor(int color) {
		mPaintStroke.setColor(color);
		invalidate();
	}
}