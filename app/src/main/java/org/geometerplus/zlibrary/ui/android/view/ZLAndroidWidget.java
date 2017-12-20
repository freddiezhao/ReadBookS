/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.view;

import java.util.List;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.util.ZLog;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.fbreader.BookmarkHighlighting;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLView.PageIndex;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.view.ZLTextHighlighting;
import org.geometerplus.zlibrary.text.view.ZLTextPage;
import org.geometerplus.zlibrary.text.view.ZLTextView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.reader.PageWidgetListener;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;

public class ZLAndroidWidget extends View implements ZLViewWidget, View.OnLongClickListener
{
	private final Paint			myPaint			= new Paint();
	private final BitmapManager	myBitmapManager	= new BitmapManager(this);
	private Bitmap				myFooterBitmap;

	private boolean				mTouchEnable	= true;
	
	public void clear(){
		if(myBitmapManager != null){
			myBitmapManager.clear();
		}
		if(myFooterBitmap != null){
			myFooterBitmap.recycle();
			myFooterBitmap = null;
		}
		
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public ZLAndroidWidget(Context context)
	{
		super(context);
		init();
	}

	public void setTouchEnable(boolean touchEnable)
	{
		this.mTouchEnable = touchEnable;
	}

	private void init()
	{
		MARK_CHANGE_HEIGHT = ResourceUtil.getDimens(R.dimen.mark_pull_change);
		MARK_MAX_HEIGHT = ResourceUtil.getDimens(R.dimen.mark_pull_height);

		// next line prevent ignoring first onKeyDown DPad event
		// after any dialog was closed
		setFocusableInTouchMode(true);
		setDrawingCacheEnabled(false);
		setOnLongClickListener(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		getAnimationProvider().terminate();
		if (myScreenIsTouched) {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			myScreenIsTouched = false;
			view.onScrollingFinished(ZLView.PageIndex.current);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		if(ZLApplication.Instance()==null)
			return;
		final Context context = getContext();
		if (context instanceof FBReader) {
			((FBReader) context).createWakeLock();
		} else {
			System.err.println("A surprise: view's context is not an FBReader");
		}
		super.onDraw(canvas);

		// final int w = getWidth();
		// final int h = getMainAreaHeight();

		if (getAnimationProvider().inProgress()) {
			onDrawInScrolling(canvas);
		} else {
			onDrawStatic(canvas);
			ZLApplication.Instance().onRepaintFinished();
			if (mListener != null) {
				mListener.onPageTurned();
			}
		}
	}

	public void deleteCurrPageAllBookmark()
	{
		// DEBUG CJL
		// computePageHasBookmarkOrNot(((FBReaderApp) ZLApplication.Instance())
		// .getTextView());
		final ZLTextView text = ((FBReaderApp) ZLApplication.Instance()).getTextView();
		text.deleteAllBookmark();
		ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ deleteAllBookmarkFlag > true");
		reset();
		ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ deleteCurrPageAllBookmark > reset.");
		repaint();
		ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ deleteCurrPageAllBookmark > repaint.");
		// DEBUG CJL 临时策略
		postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				// 删除完书签后要还原回去
				text.setDeleteAllBookmarkFlag(false);
				ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ deleteAllBookmarkFlag > false");
			}
		}, 1000);
	}

	private ZLTextPage getCurrentZLTextPage()
	{
		final ZLTextView text = ((FBReaderApp) ZLApplication.Instance()).getTextView();
		ZLTextPage textPage = text.getPage(PageIndex.current);
		return textPage;
	}

	// 判断书签的显示与隐藏（DEBUG CJL 暂时保留该方法）
	private void computePageHasBookmarkOrNot(ZLTextView view)
	{
		// ZLTextWordCursor startCursor = view.getStartCursor();
		// ZLTextWordCursor endCursor = view.getEndCursor();

		ZLTextPage textPage = view.getPage(PageIndex.current);
		// int size = textPage.TextElementMap.size();
		// int lineInfoSize = textPage.LineInfos.size();

		// 判断startCursor是不是段落首字
		// if (startCursor.isStartOfParagraph()) {
		// ZLTextParagraphCursor paragraphCursor = startCursor
		// .getParagraphCursor();
		// paragraphCursor.
		// }

		// ZLTextElement startElements = startCursor.getElement();
		// ZLTextElement endElements = endCursor.getElement();
		// ZLog.i(ZLog.ZLAndroidWidget, "startElements > " + startElements);
		// ZLog.i(ZLog.ZLAndroidWidget, "endElements > " + endElements);

		boolean isHasDeleteAction = false;

		FBReaderApp RBReaderApp = (FBReaderApp) ZLApplication.Instance();
		for (BookmarkQuery query = new BookmarkQuery(RBReaderApp.Model.Book, 20);; query = query.next()) {
			final List<Bookmark> bookmarks = RBReaderApp.Collection.bookmarks(query);
			if (bookmarks.isEmpty()) {
				break;
			}
			int i = 0;
			for (Bookmark b : bookmarks) {
				if (b.getEnd() == null) {
					b.findEnd(view);
				}
				ZLTextHighlighting textHighlighting = new BookmarkHighlighting(view, RBReaderApp.Collection, b);
				boolean intersects = textHighlighting.intersects(textPage);
				ZLog.i(ZLog.ZLAndroidWidget, "b[" + i + "] > " + b + ", text > " + b.getText() + ", intersects > " + intersects);
				i++;

				if (intersects) {
					isHasDeleteAction = true;
					RBReaderApp.Collection.deleteBookmark(b);
				}
			}
		}

		if (isHasDeleteAction) {
			postInvalidate();
		}
	}

	private AnimationProvider	myAnimationProvider;
	private ZLView.Animation	myAnimationType;

	private AnimationProvider getAnimationProvider()
	{
		if(ZLApplication.Instance() == null){
			myAnimationProvider = new NoneAnimationProvider(myBitmapManager);
			return myAnimationProvider;
		}
		
		final ZLView.Animation type = ZLApplication.Instance().getCurrentView().getAnimationType();
		if (myAnimationProvider == null || myAnimationType != type) {
			myAnimationType = type;
			switch (type) {
			case none:
				myAnimationProvider = new NoneAnimationProvider(myBitmapManager);
				break;
			case curl:
				// myAnimationProvider = new
				// CurlAnimationProvider(myBitmapManager);
				myAnimationProvider = new EmulateAnimationProvider(myBitmapManager);
				break;
			case slide:
				myAnimationProvider = new SlideAnimationProvider(myBitmapManager);
				break;
			case shift:
				myAnimationProvider = new ShiftAnimationProvider(myBitmapManager);
				break;
			case left2right:
				myAnimationProvider = new Left2RightAnimationProvider(myBitmapManager);
				break;
			}
			// myAnimationProvider = new
			// Left2RightAnimationProvider(myBitmapManager);
		}
		return myAnimationProvider;
	}

	private void onDrawInScrolling(Canvas canvas)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();

		// final int w = getWidth();
		// final int h = getMainAreaHeight();

		final AnimationProvider animator = getAnimationProvider();
		final AnimationProvider.Mode oldMode = animator.getMode();
		animator.doStep();
		if (animator.inProgress()) {
			animator.draw(canvas);
			if (animator.getMode().Auto) {
				postInvalidate();
			}
			drawFooter(canvas);
		} else {
			switch (oldMode) {
			case AnimatedScrollingForward: {
				final ZLView.PageIndex index = animator.getPageToScrollTo();
				myBitmapManager.shift(index == ZLView.PageIndex.next);
				view.onScrollingFinished(index);
				ZLApplication.Instance().onRepaintFinished();
				break;
			}
			case AnimatedScrollingBackward:
				view.onScrollingFinished(ZLView.PageIndex.current);
				break;
			}
			onDrawStatic(canvas);
		}
	}

	@Override
	public void reset()
	{
		myBitmapManager.reset();
	}

	@Override
	public void repaint()
	{
		postInvalidate();
	}

	@Override
	public void startManualScrolling(int x, int y, ZLView.Direction direction)
	{
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight());
		animator.startManualScrolling(x, y);
	}

	@Override
	public void scrollManuallyTo(int x, int y)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final AnimationProvider animator = getAnimationProvider();
		PageIndex index = animator.getPageToScrollTo(x, y);
		if (view.canScroll(index)) {
			animator.scrollTo(x, y);
			postInvalidate();
		}
	}

	@Override
	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, int x, int y, ZLView.Direction direction, int speed)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
			if (mListener != null) {
				if (pageIndex == ZLView.PageIndex.next) {
					mListener.onFirstOrLastPage(false);
				} else if (pageIndex == ZLView.PageIndex.previous) {
					mListener.onFirstOrLastPage(true);
				}
			}
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight());
		animator.startAnimatedScrolling(pageIndex, x, y, speed);
		if (animator.getMode().Auto) {
			postInvalidate();
		}
	}

	@Override
	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, ZLView.Direction direction, int speed)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
			if (mListener != null) {
				if (pageIndex == ZLView.PageIndex.next) {
					mListener.onFirstOrLastPage(false);
				} else if (pageIndex == ZLView.PageIndex.previous) {
					mListener.onFirstOrLastPage(true);
				}
			}
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight());
		animator.startAnimatedScrolling(pageIndex, null, null, speed);
		if (animator.getMode().Auto) {
			postInvalidate();
		}
	}

	@Override
	public void startAnimatedScrolling(int x, int y, float velocityX, float velocityY, int speed)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final AnimationProvider animator = getAnimationProvider();
		ZLView.PageIndex pageIndex = animator.getPageToScrollTo(x, y);
		if (!view.canScroll(pageIndex)) {
			animator.terminate();
			if (mListener != null) {
				if (pageIndex == ZLView.PageIndex.next) {
					mListener.onFirstOrLastPage(false);
				} else if (pageIndex == ZLView.PageIndex.previous) {
					mListener.onFirstOrLastPage(true);
				}
			}
			return;
		}
		animator.startAnimatedScrolling(x, y, velocityX, velocityY, speed);
		postInvalidate();
	}

	void drawOnBitmap(Bitmap bitmap, ZLView.PageIndex index)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (view == null) {
			return;
		}

		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(new Canvas(bitmap), new ZLAndroidPaintContext.Geometry(getWidth(), getHeight(), getWidth(), getMainAreaHeight(), 0, 0),
				view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0);
		view.paint(context, index);
	}

	private void drawFooter(Canvas canvas)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final ZLView.FooterArea footer = view.getFooterArea();

		if (footer == null) {
			myFooterBitmap = null;
			return;
		}

		if (myFooterBitmap != null && (myFooterBitmap.getWidth() != getWidth() || myFooterBitmap.getHeight() != footer.getHeight())) {
			myFooterBitmap = null;
		}
		if (myFooterBitmap == null) {
			myFooterBitmap = Bitmap.createBitmap(getWidth(), footer.getHeight(), Bitmap.Config.RGB_565);
		}
		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(new Canvas(myFooterBitmap), new ZLAndroidPaintContext.Geometry(getWidth(), getHeight(), getWidth(), footer.getHeight(), 0,
				getMainAreaHeight()), view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0);
		footer.paint(context);
		canvas.drawBitmap(myFooterBitmap, 0, getHeight() - footer.getHeight(), myPaint);
	}

	private void onDrawStatic(final Canvas canvas)
	{
		// ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ onDrawStatic > start");
		myBitmapManager.setSize(getWidth(), getMainAreaHeight());
		Bitmap bmap = myBitmapManager.getBitmap(ZLView.PageIndex.current);
		if(bmap != null){
			canvas.drawBitmap(bmap, 0, 0, myPaint);
		}
		drawFooter(canvas);
		// ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ onDrawStatic > end");

		// ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ 是否有书签 > "
		// + getCurrentZLTextPage().hasBookmark());

		((FBReader) (ZLApplication.Instance().getActivity())).updateBookmarkFlag();

		new Thread()
		{
			@Override
			public void run()
			{
				final ZLView view = ZLApplication.Instance().getCurrentView();
				final ZLAndroidPaintContext context = new ZLAndroidPaintContext(canvas, new ZLAndroidPaintContext.Geometry(getWidth(), getHeight(), getWidth(), getMainAreaHeight(), 0, 0),
						view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0);
				view.preparePage(context, ZLView.PageIndex.next);
			}
		}.start();
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);
		} else {
			ZLApplication.Instance().getCurrentView().onTrackballRotated((int) (10 * event.getX()), (int) (10 * event.getY()));
		}
		return true;
	}

	private class LongClickRunnable implements Runnable
	{
		@Override
		public void run()
		{
			if (performLongClick()) {
				myLongClickPerformed = true;
			}
		}
	}

	private volatile LongClickRunnable	myPendingLongClickRunnable;
	private volatile boolean			myLongClickPerformed;

	private void postLongClickRunnable()
	{
		myLongClickPerformed = false;
		myPendingPress = false;
		if (myPendingLongClickRunnable == null) {
			myPendingLongClickRunnable = new LongClickRunnable();
		}
		postDelayed(myPendingLongClickRunnable, 2 * ViewConfiguration.getLongPressTimeout());
	}

	private class ShortClickRunnable implements Runnable
	{
		@Override
		public void run()
		{
			final ZLView view = ZLApplication.Instance().getCurrentView();
			view.onFingerSingleTap(myPressedX, myPressedY);
			myPendingPress = false;
			myPendingShortClickRunnable = null;
		}
	}

	private volatile ShortClickRunnable	myPendingShortClickRunnable;

	private volatile boolean			myPendingPress;
	private volatile boolean			myPendingDoubleTap;
	private int							myPressedX, myPressedY;
	private boolean						myScreenIsTouched;

	private PageWidgetListener			mListener;

	// add by cjl
	private VelocityTracker				myVelocityTracker;
	private int							myPointerId;

	private void acquireVelocityTracker(MotionEvent event)
	{
		if (myVelocityTracker == null) {
			myVelocityTracker = VelocityTracker.obtain();
		}
		myVelocityTracker.addMovement(event);
		myPointerId = event.getPointerId(0);
	}

	private void releaseVelocityTracker()
	{
		if (null != myVelocityTracker) {
			myVelocityTracker.clear();
			myVelocityTracker.recycle();
			myVelocityTracker = null;
		}
	}

	// end by cjl

	public void setPageWidgetListener(PageWidgetListener listener)
	{
		mListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (!mTouchEnable) {
			return true;
		}

		acquireVelocityTracker(event);

		int x = (int) event.getX();
		int y = (int) event.getY();

		final ZLView view = ZLApplication.Instance().getCurrentView();
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (myPendingDoubleTap) {
				view.onFingerDoubleTap(x, y);
			} else if (myLongClickPerformed) {
				view.onFingerReleaseAfterLongPress(x, y);
			} else {
				if (myPendingLongClickRunnable != null) {
					removeCallbacks(myPendingLongClickRunnable);
					myPendingLongClickRunnable = null;
				}
				if (myPendingPress) {
					if (view.isDoubleTapSupported()) {
						if (myPendingShortClickRunnable == null) {
							myPendingShortClickRunnable = new ShortClickRunnable();
						}
						postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
					} else {
						view.onFingerSingleTap(x, y);
					}
				} else {
					if (pullType == 1) {
					} else {
						final VelocityTracker velocityTracker = myVelocityTracker;
						if (velocityTracker != null) {
							velocityTracker.computeCurrentVelocity(1000);
						}
						final float velocityX = velocityTracker.getXVelocity(myPointerId);
						final float velocityY = velocityTracker.getYVelocity(myPointerId);
						view.onFingerRelease(x, y, velocityX, velocityY);
					}
				}
			}
			myPendingDoubleTap = false;
			myPendingPress = false;
			myScreenIsTouched = false;

			releaseVelocityTracker();
			break;

		case MotionEvent.ACTION_CANCEL:
			releaseVelocityTracker();
			break;

		case MotionEvent.ACTION_DOWN:
			if (myPendingShortClickRunnable != null) {
				removeCallbacks(myPendingShortClickRunnable);
				myPendingShortClickRunnable = null;
				myPendingDoubleTap = true;
			} else {
				// TODO: ouyang 屏蔽长按
				// postLongClickRunnable();
				myPendingPress = true;
			}

			pullType = 0;
			mMarkY = 0;
			mLastMotionX = 0;
			mLastMotionY = 0;

			myScreenIsTouched = true;
			myPressedX = x;
			myPressedY = y;
			break;

		case MotionEvent.ACTION_MOVE: {
			final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
			final boolean isAMove = Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
			if (isAMove) {
				myPendingDoubleTap = false;
			}

			if (myLongClickPerformed) {
				// 长按
				view.onFingerMoveAfterLongPress(x, y);
			} else {
				if (myPendingPress) {
					if (isAMove) {
						if (myPendingShortClickRunnable != null) {
							removeCallbacks(myPendingShortClickRunnable);
							myPendingShortClickRunnable = null;
						}
						if (myPendingLongClickRunnable != null) {
							removeCallbacks(myPendingLongClickRunnable);
						}

						ZLog.d(ZLog.EpubReadPullDownAction, "ACTION_MOVE >> befor judge >> pullType{" + pullType + "}");
						if (pullType == 0) {
							pullType = judgeTouchType(event, myPressedX, myPressedY);
							ZLog.d(ZLog.EpubReadPullDownAction, "ACTION_MOVE >> after judge >> pullType{" + pullType + "}");
						}

						if (pullType != 1) {
							view.onFingerPress(myPressedX, myPressedY);
						}

						myPendingPress = false;
					}
				}

				ZLog.d(ZLog.EpubReadPullDownAction, "ACTION_MOVE >> myPendingPress{" + myPendingPress + "}");
				if (!myPendingPress) {
					// if (pullType == 0) {
					// pullType = judgeTouchType(event, myPressedX, myPressedY);
					// }

					if (pullType == 1) {
						// 下拉书签
						if (mListener != null) {
							mListener.onPullStart();
						}
					}

					if (pullType == 2) {
						view.onFingerMove(x, y);
					}
				}
			}
			break;
		}
		}

		ZLog.d(ZLog.EpubReadPullDownAction, "onTouchEvent >> total action >> pullType{" + pullType + "}");
		if (pullType == 1) {
			ZLog.d(ZLog.EpubReadPullDownAction, "onTouchEvent >> doMarkTouchEvent >> myPressedX{" + myPressedX + "}" + ", myPressedY{" + myPressedY + "}");
			return doMarkTouchEvent(event, myPressedX, myPressedY);
		} else {
			return true;
		}
	}

	private int				pullType			= 0;
	private float			mMarkY				= 0;
	private float			mLastMotionY;
	private float			mLastMotionX;
	private float			MARK_CHANGE_HEIGHT;
	private float			MARK_MAX_HEIGHT;
	public static final int	VAILD_MOVE_DISTANCE	= 5;

	private boolean doMarkTouchEvent(MotionEvent event, int x, int y)
	{
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// 第一次进入，初始化上次触摸的点
			if (mLastMotionY == 0) {
				mLastMotionY = y;
			}
			final float scrollY = event.getY() - mLastMotionY;

			if ((Math.abs(scrollY) >= 1)) {
				float lastMarkY = mMarkY;
				mMarkY = mMarkY + scrollY / 2;
				if (mMarkY > MARK_MAX_HEIGHT) {
					mMarkY = MARK_MAX_HEIGHT;
				} else if (mMarkY < 0) {
					mMarkY = 0;
				}

				if (mMarkY != lastMarkY) {
					translateView(0, 0, lastMarkY, mMarkY, 0, null);
				}

				ZLog.i(ZLog.EpubReadPullDownAction, "doMarkTouchEvent >> mMarkY{" + mMarkY + "}" + ", lastMarkY{" + lastMarkY + "}" + ", MARK{" + MARK_CHANGE_HEIGHT + "}");

				if (mMarkY >= MARK_CHANGE_HEIGHT && lastMarkY < MARK_CHANGE_HEIGHT && mMarkY > lastMarkY) {
					ZLog.e(ZLog.EpubReadPullDownAction, "doMarkTouchEvent >> onPullStateDown{}");
					if (mListener != null) {
						mListener.onPullStateDown();
					}
				} else if (mMarkY <= MARK_CHANGE_HEIGHT && lastMarkY > MARK_CHANGE_HEIGHT && mMarkY < lastMarkY) {
					ZLog.e(ZLog.EpubReadPullDownAction, "doMarkTouchEvent >> onPullStateUp{}");
					if (mListener != null) {
						mListener.onPullStateUp();
					}
				}

				mLastMotionY = event.getY();
			}
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			final boolean isMarkStatusChanged;
			if (mMarkY >= MARK_CHANGE_HEIGHT) {
				isMarkStatusChanged = true;
			} else {
				isMarkStatusChanged = false;
			}

			long duration = (long) (600.0F * Math.abs(mMarkY / MARK_MAX_HEIGHT));
			translateView(0, 0, mMarkY, 0, duration, new AnimationListener()
			{
				public void onAnimationStart(Animation animation)
				{

				}

				public void onAnimationRepeat(Animation animation)
				{

				}

				public void onAnimationEnd(Animation animation)
				{
					if (isMarkStatusChanged) {
						if (mListener != null) {
							mListener.onPullDown();
						}
					}
					if (mListener != null) {
						mListener.onPullEnd();
					}
				}
			});
		}
		return true;
	}

	private void translateView(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, long duration, AnimationListener listener)
	{
		TranslateAnimation transAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
		transAnimation.setDuration(duration);
		transAnimation.setFillAfter(true);
		transAnimation.setFillEnabled(true);
		transAnimation.setAnimationListener(listener);
		this.startAnimation(transAnimation);
	}

	public int judgeTouchType(MotionEvent e, int x, int y)
	{
		// if (isaVaildMove(e, x, y, VAILD_MOVE_DISTANCE)) {
		if (judgeMarkTouchType(e, x, y)) {
			return 1;
		}

		if (e.getX() > x) {
			return 2;
		} else if (e.getX() < x) {
			return 2;
		}
		// }
		return 0;
	}

	public boolean isaVaildMove(MotionEvent e, int x, int y, int vaildOffset)
	{
		float xOff = Math.abs(x - e.getX());
		float yOff = Math.abs(y - e.getY());
		float offsetToMove = 0f;
		if (vaildOffset > 0) {
			offsetToMove = PixelUtil.sp2px(vaildOffset, SinaBookApplication.gContext);
			if (offsetToMove < vaildOffset) {
				offsetToMove = vaildOffset;
			}
		}
		if ((xOff > offsetToMove) || (yOff > offsetToMove)) {
			return true;
		}
		return false;
	}

	// 垂直向下的手势
	private boolean judgeMarkTouchType(MotionEvent e, int x, int y)
	{
		if (e.getY() < y) {
			return false;
		}
		float distanceX = Math.abs(e.getX() - x);
		float distanceY = Math.abs(e.getY() - y);

		if (distanceX < VAILD_MOVE_DISTANCE && distanceY > 2 * VAILD_MOVE_DISTANCE) {
			return true;
		}

		// y轴移动距离是x轴移动的2倍及以上
		if (distanceY / distanceX >= 2) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onLongClick(View v)
	{
		final ZLView view = ZLApplication.Instance().getCurrentView();
		return view.onFingerLongPress(myPressedX, myPressedY);
	}

	// private int myKeyUnderTracking = -1;
	// private long myTrackingStartTime;
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// final ZLApplication application = ZLApplication.Instance();
	// final ZLKeyBindings bindings = application.keyBindings();
	//
	// if (bindings.hasBinding(keyCode, true)
	// || bindings.hasBinding(keyCode, false)) {
	// if (myKeyUnderTracking != -1) {
	// if (myKeyUnderTracking == keyCode) {
	// return true;
	// } else {
	// myKeyUnderTracking = -1;
	// }
	// }
	// if (bindings.hasBinding(keyCode, true)) {
	// myKeyUnderTracking = keyCode;
	// myTrackingStartTime = System.currentTimeMillis();
	// return true;
	// } else {
	// return application.runActionByKey(keyCode, false);
	// }
	// } else {
	// return false;
	// }
	// }
	//
	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// if (myKeyUnderTracking != -1) {
	// if (myKeyUnderTracking == keyCode) {
	// final boolean longPress = System.currentTimeMillis() >
	// myTrackingStartTime
	// + ViewConfiguration.getLongPressTimeout();
	// ZLApplication.Instance().runActionByKey(keyCode, longPress);
	// }
	// myKeyUnderTracking = -1;
	// return true;
	// } else {
	// final ZLKeyBindings bindings = ZLApplication.Instance()
	// .keyBindings();
	// return bindings.hasBinding(keyCode, false)
	// || bindings.hasBinding(keyCode, true);
	// }
	// }

	@Override
	protected int computeVerticalScrollExtent()
	{
		if(ZLApplication.Instance() == null){
			return 0;
		}
		
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		final AnimationProvider animator = getAnimationProvider();
		if (animator.inProgress()) {
			final int from = view.getScrollbarThumbLength(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbLength(animator.getPageToScrollTo());
			final int percent = animator.getScrolledPercent();
			return (from * (100 - percent) + to * percent) / 100;
		} else {
			return view.getScrollbarThumbLength(ZLView.PageIndex.current);
		}
	}

	@Override
	protected int computeVerticalScrollOffset()
	{
		if(ZLApplication.Instance() == null){
			return 0;
		}
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		final AnimationProvider animator = getAnimationProvider();
		if (animator.inProgress()) {
			final int from = view.getScrollbarThumbPosition(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbPosition(animator.getPageToScrollTo());
			final int percent = animator.getScrolledPercent();
			return (from * (100 - percent) + to * percent) / 100;
		} else {
			return view.getScrollbarThumbPosition(ZLView.PageIndex.current);
		}
	}

	@Override
	protected int computeVerticalScrollRange()
	{
		if(ZLApplication.Instance() == null){
			return 0;
		}
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		return view.getScrollbarFullSize();
	}

	private int getMainAreaHeight()
	{
		if(ZLApplication.Instance() == null){
			return 0;
		}
		final ZLView.FooterArea footer = ZLApplication.Instance().getCurrentView().getFooterArea();
		return footer != null ? getHeight() - footer.getHeight() : getHeight();
	}
}
