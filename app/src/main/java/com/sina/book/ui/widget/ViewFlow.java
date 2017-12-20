package com.sina.book.ui.widget;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller;

import com.sina.book.R;
import com.sina.book.util.PixelUtil;

//import com.sina.book.util.LogUtil;

/**
 * A horizontally scrollable {@link ViewGroup} with items populated from an
 * {@link Adapter}. The ViewFlow uses a buffer to store loaded {@link View}s in.
 * The default size of the buffer is 3 elements on both sides of the currently
 * visible {@link View}, making up a total buffer size of 3 * 2 + 1 = 7. The
 * buffer size can be changed using the {@code sidebuffer} xml attribute.
 * 
 */
public class ViewFlow extends AdapterView<Adapter>
{

	private static final int		SNAP_VELOCITY				= 1000;
	private static final int		INVALID_SCREEN				= -1;
	private final static int		TOUCH_STATE_REST			= 0;
	private final static int		TOUCH_STATE_SCROLLING		= 1;

	private final int				DEFAULT_SIDE_BUFFER_SIZE	= 1;

	private LinkedList<View>		mLoadedViews;
	private int						mCurrentBufferIndex			= 0;
	private int						mCurrentAdapterIndex		= 0;
	private int						mSideBuffer					= DEFAULT_SIDE_BUFFER_SIZE;
	private Scroller				mScroller;
	private VelocityTracker			mVelocityTracker;
	private int						mTouchState					= TOUCH_STATE_REST;
	private float					mLastMotionX;
	private int						mTouchSlop;
	private int						mMaximumVelocity;
	private int						mCurrentScreen;
	private int						mNextScreen					= INVALID_SCREEN;
	private boolean					mFirstLayout				= true;
	private ViewSwitchListener		mViewSwitchListener;
	private Adapter					mAdapter;
	private int						mLastScrollDirection;
	private AdapterDataSetObserver	mDataSetObserver;
	private FlowIndicator			mIndicator;

	// 新添加的属性
	private boolean					mSupportOffsetItemViewWidth	= false;					// 是否支持子Item的宽度偏移
	private int						mOffsetItemViewWidthLength;							// 偏移量

	private OnGlobalLayoutListener	orientationChangeListener	= new OnGlobalLayoutListener()
																{

																	@Override
																	public void onGlobalLayout()
																	{
																		getViewTreeObserver().removeGlobalOnLayoutListener(orientationChangeListener);

																		setSelection(mCurrentAdapterIndex);
																	}
																};

	private Handler					mHandler;
	private boolean					mAutoTimeUsed				= false;
	private int						mAutoTimeInterval			= 5000;
	private Runnable				mAutoRunnable				= new Runnable()
																{
																	@Override
																	public void run()
																	{
																		if (mAdapter != null && mAdapter.getCount() > 0) {
																			snapToScreen((mCurrentScreen + 1) % mAdapter.getCount());
																			mHandler.removeCallbacks(this);
																			mHandler.postDelayed(this, mAutoTimeInterval);
																		}
																	}
																};

	/**
	 * Receives call backs when a new {@link View} has been scrolled to.
	 */
	public static interface ViewSwitchListener
	{

		/**
		 * This method is called when a new View has been scrolled to.
		 * 
		 * @param view
		 *            the {@link View} currently in focus.
		 * @param position
		 *            The position in the adapter of the {@link View} currently
		 *            in focus.
		 */
		void onSwitched(View view, int position);

	}

	public ViewFlow(Context context)
	{
		this(context, null);
	}

	// public ViewFlow(Context context, int sideBuffer) {
	// this(context, null);
	// }

	public ViewFlow(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	public void startAutoFlow(int timeInterval)
	{
		if (mHandler != null) {
			mAutoTimeUsed = true;
			if (timeInterval <= 0) {
				timeInterval = 5000;
			}
			mHandler.removeCallbacks(mAutoRunnable);
			mHandler.postDelayed(mAutoRunnable, mAutoTimeInterval);
		}
	}

	public void startAutoFlow()
	{
		startAutoFlow(0);
	}

	public void stopAutoFlow()
	{
		if (mHandler != null) {
			mAutoTimeUsed = false;
			mHandler.removeCallbacks(mAutoRunnable);
		}
	}

	private void init(Context context, AttributeSet attrs)
	{
		mHandler = new Handler();
		mLoadedViews = new LinkedList<View>();
		mScroller = new Scroller(getContext());
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

		// 加载xml属性值
		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ViewFlow);
		mSupportOffsetItemViewWidth = arr.getBoolean(R.styleable.ViewFlow_support_offset, false);
		mOffsetItemViewWidthLength = arr.getInt(R.styleable.ViewFlow_offset_width, 0);
		mSideBuffer = arr.getInt(R.styleable.ViewFlow_side_buffer, DEFAULT_SIDE_BUFFER_SIZE);
		arr.recycle();

		if (mSupportOffsetItemViewWidth) {
			// 转换成px
			mOffsetItemViewWidthLength = PixelUtil.dp2px(mOffsetItemViewWidthLength);
		} else {
			mOffsetItemViewWidthLength = 0;
		}
	}

	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		getParent().requestDisallowInterceptTouchEvent(true);
		super.dispatchTouchEvent(ev);
		return true;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		getViewTreeObserver().addOnGlobalLayoutListener(orientationChangeListener);
	}

	public int getViewsCount()
	{
		return mAdapter.getCount();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec) - mOffsetItemViewWidthLength;
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ViewFlow can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			// throw new
			// IllegalStateException("ViewFlow can only be used in EXACTLY mode.");
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			scrollTo(mCurrentScreen * width, 0);
			mFirstLayout = false;
		}
	}

	// private final int OFFSET_ITEM_VIEW_WIDTH = PixelUtil.dp2px(50.0f);

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth() - mOffsetItemViewWidthLength;
				int left = childLeft;
				int top = 0;
				int right = childLeft + childWidth;
				int bottom = child.getMeasuredHeight();
				// Log.i("ViewFlow", "left >> " + left + ", top >> " + top +
				// ", right >> " + right + ", bottom >> "
				// + bottom);
				child.layout(left, top, right, bottom);
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if (getViewsCount() == 1)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX
							- (getWidth() - mOffsetItemViewWidthLength);
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (getViewsCount() == 1)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX
							- (getWidth() - mOffsetItemViewWidthLength);
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}
		return true;
	}

	private void snapToDestination()
	{
		final int screenWidth = (getWidth() - mOffsetItemViewWidthLength);
		final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;

		snapToScreen(whichScreen);
	}

	private void snapToScreen(int whichScreen)
	{
		// Log.d("ouyang", "snapToScreen---whichScreen: " + whichScreen);
		// Log.d("ouyang", "snapToScreen---mCurrentScreen: " + mCurrentScreen);

		mLastScrollDirection = whichScreen - mCurrentScreen;
		if (!mScroller.isFinished())
			return;

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));

		mNextScreen = whichScreen;
		// Log.d("ouyang", "snapToScreen---mNextScreen: " + mNextScreen);
		// Log.d("ouyang", "snapToScreen---mOffsetItemViewWidthLength: " +
		// mOffsetItemViewWidthLength);

		final int newX = whichScreen * (getWidth() - mOffsetItemViewWidthLength);
		// Log.d("ouyang", "snapToScreen---newX: " + newX);
		// Log.d("ouyang", "snapToScreen---newX: " + getScrollX());

		final int delta = newX - getScrollX();
		// Log.d("ouyang", "snapToScreen---delta: " + delta);

		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 5 / 2);
		invalidate();
	}

	@Override
	public void computeScroll()
	{
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			ViewCompat.postInvalidateOnAnimation(this);
		} else if (mNextScreen != INVALID_SCREEN) {

			mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
			mNextScreen = INVALID_SCREEN;

			// Log.d("ouyang", "computeScroll---mCurrentScreen: " +
			// mCurrentScreen);

			post(new Runnable()
			{
				@Override
				public void run()
				{
					postViewSwitched(mLastScrollDirection);
				}
			});
		}
	}

	/**
	 * Scroll to the {@link View} in the view buffer specified by the index.
	 * 
	 * @param indexInBuffer
	 *            Index of the view in the view buffer.
	 */
	private void setVisibleView(int indexInBuffer, boolean uiThread)
	{
		// 布局视图还未测量完成数据就回来了，此时计算会出错。
		if (mSupportOffsetItemViewWidth && getWidth() <= 0) {
			return;
		}
		mCurrentScreen = Math.max(0, Math.min(indexInBuffer, getChildCount() - 1));
		// Log.d("ouyang", "setVisibleView---mCurrentScreen: " +
		// mCurrentScreen);

		int dx = (mCurrentScreen * (getWidth() - mOffsetItemViewWidthLength)) - mScroller.getCurrX();
		// Log.d("ouyang", "setVisibleView---mScroller.getCurrX(): " +
		// mScroller.getCurrX());
		// Log.d("ouyang", "setVisibleView---dx: " + dx);

		scrollTo(mScroller.getCurrX() + dx, mScroller.getCurrY());
		if (uiThread)
			invalidate();
		else
			postInvalidate();
	}

	@Override
	public void scrollTo(int x, int y)
	{
		super.scrollTo(x, y);
	}

	/**
	 * Set the listener that will receive notifications every time the {code
	 * ViewFlow} scrolls.
	 * 
	 * @param l
	 *            the scroll listener
	 */
	public void setOnViewSwitchListener(ViewSwitchListener l)
	{
		mViewSwitchListener = l;
	}

	@Override
	public Adapter getAdapter()
	{
		return mAdapter;
	}

	@Override
	public void setAdapter(Adapter adapter)
	{
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		mAdapter = adapter;

		if (mAdapter != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

		}
		if (mAdapter == null || mAdapter.getCount() == 0)
			return;

		setSelection(0);
	}

	@Override
	public View getSelectedView()
	{
		return (mCurrentAdapterIndex < mLoadedViews.size() ? mLoadedViews.get(mCurrentBufferIndex) : null);
	}

	/**
	 * Set the FlowIndicator
	 * 
	 * @param flowIndicator
	 */
	public void setFlowIndicator(FlowIndicator flowIndicator)
	{
		mIndicator = flowIndicator;
		mIndicator.setViewFlow(this);
	}

	@Override
	public void setSelection(int position)
	{
		if (mAdapter == null || position >= mAdapter.getCount())
			return;

		ArrayList<View> recycleViews = new ArrayList<View>();
		View recycleView;
		while (!mLoadedViews.isEmpty()) {
			recycleViews.add(recycleView = mLoadedViews.remove());
			detachViewFromParent(recycleView);
		}

		for (int i = Math.max(0, position - mSideBuffer); i < Math.min(mAdapter.getCount(), position + mSideBuffer + 1); i++) {
			int transI = i;
			if (transI < 0) {
				while (transI < 0) {
					transI = transI + mAdapter.getCount();
				}
			} else if (transI >= mAdapter.getCount()) {
				transI = transI % mAdapter.getCount();
			}

			mLoadedViews
					.addLast(makeAndAddView(transI, true, (recycleViews.isEmpty() ? null : recycleViews.remove(0))));
			if (transI == position) {
				mCurrentBufferIndex = mLoadedViews.size() - 1;
			}
		}
		mCurrentAdapterIndex = position;

		for (View view : recycleViews) {
			removeDetachedView(view, false);
		}

		requestLayout();
		setVisibleView(mCurrentBufferIndex, false);

		performOnSwitched(mLoadedViews.get(mCurrentBufferIndex), mCurrentAdapterIndex);
	}

	private void resetFocus()
	{
		mLoadedViews.clear();
		removeAllViewsInLayout();

		for (int i = mCurrentAdapterIndex - mSideBuffer; i < mCurrentAdapterIndex + mSideBuffer + 1; i++) {
			int transI = i;
			if (transI < 0) {
				while (transI < 0) {
					transI = transI + mAdapter.getCount();
				}
			} else if (transI >= mAdapter.getCount()) {
				transI = transI % mAdapter.getCount();
			}
			mLoadedViews.addLast(makeAndAddView(transI, true, null));

			if (transI == mCurrentAdapterIndex) {
				mCurrentBufferIndex = mLoadedViews.size() - 1;
			}
		}
		requestLayout();

		setVisibleView(mCurrentBufferIndex, false);
		performOnSwitched(mLoadedViews.get(mCurrentBufferIndex), mCurrentAdapterIndex);
	}

	private void postViewSwitched(int direction)
	{
		if (direction == 0)
			return;

		if (direction > 0) { // to the right
			mCurrentAdapterIndex++;
			mCurrentAdapterIndex = mCurrentAdapterIndex % mAdapter.getCount();
			mCurrentBufferIndex++;

			View recycleView = null;

			// Remove view outside buffer range
			recycleView = mLoadedViews.removeFirst();
			detachViewFromParent(recycleView);
			mCurrentBufferIndex--;

			// Add new view to buffer
			int newBufferIndex = mCurrentAdapterIndex + mSideBuffer;
			if (newBufferIndex < mAdapter.getCount()) {
				mLoadedViews.addLast(makeAndAddView(newBufferIndex, true, recycleView));
			} else {
				newBufferIndex = newBufferIndex % mAdapter.getCount();
				mLoadedViews.addLast(makeAndAddView(newBufferIndex, true, recycleView));
			}
		} else { // to the left
			mCurrentAdapterIndex--;
			while (mCurrentAdapterIndex < 0) {
				mCurrentAdapterIndex = mCurrentAdapterIndex + mAdapter.getCount();
			}
			mCurrentBufferIndex--;
			View recycleView = null;

			// Remove view outside buffer range
			recycleView = mLoadedViews.removeLast();
			detachViewFromParent(recycleView);

			// Add new view to buffer
			int newBufferIndex = mCurrentAdapterIndex - mSideBuffer;
			if (newBufferIndex > -1) {
				mLoadedViews.addFirst(makeAndAddView(newBufferIndex, false, recycleView));
				mCurrentBufferIndex++;
			} else {
				while (newBufferIndex < 0) {
					newBufferIndex = newBufferIndex + mAdapter.getCount();
				}
				mLoadedViews.addFirst(makeAndAddView(newBufferIndex, false, recycleView));
				mCurrentBufferIndex++;
			}

		}
		requestLayout();

		setVisibleView(mCurrentBufferIndex, true);
		if (mCurrentAdapterIndex < mAdapter.getCount() && mCurrentBufferIndex < mLoadedViews.size()
				&& mCurrentBufferIndex > -1) {
			performOnSwitched(mLoadedViews.get(mCurrentBufferIndex), mCurrentAdapterIndex);
		}
	}

	private View setupChild(View child, boolean addToEnd, boolean recycle)
	{
		ViewGroup.LayoutParams p = (ViewGroup.LayoutParams) child.getLayoutParams();
		if (p == null) {
			p = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
					0);
		}
		if (recycle)
			attachViewToParent(child, (addToEnd ? -1 : 0), p);
		else
			addViewInLayout(child, (addToEnd ? -1 : 0), p, true);
		return child;
	}

	private View makeAndAddView(int position, boolean addToEnd, View convertView)
	{
		View view = mAdapter.getView(position, convertView, this);
		view.setTag(R.id.activity_view, position);
		return setupChild(view, addToEnd, convertView != null);
	}

	private void performOnSwitched(final View view, final int position)
	{
		if (mIndicator != null) {
			mIndicator.onSwitched(view, position);
		}

		if (mViewSwitchListener != null) {
			mViewSwitchListener.onSwitched(view, position);
		}

		// restart auto
		if (mAutoTimeUsed) {
			startAutoFlow(mAutoTimeInterval);
		}
	}

	class AdapterDataSetObserver extends DataSetObserver
	{

		@Override
		public void onChanged()
		{
			View v = getChildAt(mCurrentBufferIndex);
			if (v != null) {
				for (int index = 0; index < mAdapter.getCount(); index++) {
					if (v.equals(mAdapter.getItem(index))) {
						mCurrentAdapterIndex = index;
						break;
					}
				}
			}
			resetFocus();
		}

		@Override
		public void onInvalidated()
		{
			// Not yet implemented!
		}

	}

	@SuppressWarnings("unused")
	private void logBuffer()
	{
		// LogUtil.d("viewflow", "==================");
		// LogUtil.d("viewflow", "-mCurrentAdapterIndex:" +
		// mCurrentAdapterIndex);
		// LogUtil.d("viewflow", "-mCurrentBufferIndex:" + mCurrentBufferIndex);
		// LogUtil.d("viewflow", "-mCurrentScreen:" + mCurrentScreen);
		// LogUtil.d("viewflow", "-mNextScreen:" + mNextScreen);
		//
		// for (View v : mLoadedViews) {
		// LogUtil.d("viewflow", "-v:" + v.getTag(R.id.activity_view));
		// }
		//
		// for (int i = 0; i < getChildCount(); i++) {
		// LogUtil.d("viewflow", "-real v:" +
		// getChildAt(i).getTag(R.id.activity_view));
		// }
		// Log.d("viewflow", "X: " + mScroller.getCurrX() + ", Y: " +
		// mScroller.getCurrY());
	}
}
