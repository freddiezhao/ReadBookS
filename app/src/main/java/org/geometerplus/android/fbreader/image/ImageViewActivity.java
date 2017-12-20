/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.net.Uri;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;

import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import org.geometerplus.android.fbreader.OrientationUtil;

import com.sina.book.R;

public class ImageViewActivity extends Activity {
	public static final String BACKGROUND_COLOR_KEY = "bgColor";

	private Bitmap myBitmap;
	private ZLColor myBgColor;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final ZLAndroidLibrary library = (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
		final boolean showStatusBar = library.ShowStatusBarOption.getValue();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, showStatusBar ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		setContentView(new ImageView());

		final Intent intent = getIntent();

		myBgColor = new ZLColor(intent.getIntExtra(BACKGROUND_COLOR_KEY, new ZLColor(127, 127, 127).intValue()));
		myBgColor = new ZLColor(new ZLColor(0, 0, 0).intValue());

		final Uri uri = intent.getData();
		if (ZLFileImage.SCHEME.equals(uri.getScheme())) {
			final ZLFileImage image = ZLFileImage.byUrlPath(uri.getPath());
			if (image == null) {
				// TODO: error message (?)
				finish();
			}
			try {
				final ZLImageData imageData = ZLImageManager.Instance().getImageData(image);
				myBitmap = ((ZLAndroidImageData) imageData).getFullSizeBitmap();
			} catch (Exception e) {
				// TODO: error message (?)
				e.printStackTrace();
				finish();
			}
		} else {
			// TODO: error message (?)
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		OrientationUtil.setOrientation(this, getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myBitmap != null) {
			myBitmap.recycle();
		}
		myBitmap = null;
	}

	private class ImageView extends View {
		private final Paint myPaint = new Paint();

		private volatile int myDx = 0;
		private volatile int myDy = 0;
		private volatile float myZoomFactor = 1.0f;

		ImageView() {
			super(ImageViewActivity.this);
		}

		@Override
		protected void onDraw(final Canvas canvas) {
			myPaint.setColor(ZLAndroidColorUtil.rgb(myBgColor));
			final int w = getWidth();
			final int h = getHeight();
			canvas.drawRect(0, 0, w, h, myPaint);
			if (myBitmap == null || myBitmap.isRecycled()) {
				return;
			}

			// Bitmap缩放后的大小
			final int bw = (int) (myBitmap.getWidth() * myZoomFactor);
			final int bh = (int) (myBitmap.getHeight() * myZoomFactor);

			final Rect src = new Rect(0, 0, (int) (w / myZoomFactor), (int) (h / myZoomFactor));// bitmap不经过缩放的rect
			final Rect dst = new Rect(0, 0, w, h);// 真正bitmap绘制到得区域
			if (bw <= w) {// 如果缩放bitmap宽度小于控件宽度，则水平居中dst
				src.left = 0;
				src.right = myBitmap.getWidth();
				dst.left = (w - bw) / 2;
				dst.right = dst.left + bw;
			} else {// 缩放bitmap宽度大于控件宽度，
				final int bWidth = myBitmap.getWidth();
				final int pWidth = (int) (w / myZoomFactor);
				src.left = Math.min(bWidth - pWidth, Math.max((bWidth - pWidth) / 2 - myDx, 0));
				src.right += src.left;
			}
			if (bh <= h) {// 如果缩放bitmap高度小于控件高度，则竖直居中dst
				src.top = 0;
				src.bottom = myBitmap.getHeight();
				dst.top = (h - bh) / 2;
				dst.bottom = dst.top + bh;
			} else {
				final int bHeight = myBitmap.getHeight();
				final int pHeight = (int) (h / myZoomFactor);
				src.top = Math.min(bHeight - pHeight, Math.max((bHeight - pHeight) / 2 - myDy, 0));
				src.bottom += src.top;
			}
			canvas.drawBitmap(myBitmap, src, dst, myPaint);// 把bitmap和src的交集画到dst内
			// myPaint.setStyle(Style.STROKE);
			// myPaint.setColor(Color.GREEN);
			// canvas.drawRect(src, myPaint);
			// myPaint.setColor(Color.RED);
			// canvas.drawRect(dst, myPaint);
		}

		private void shift(int dx, int dy) {// 单指拖动
			if (myBitmap == null || myBitmap.isRecycled()) {
				return;
			}

			final int w = (int) (getWidth() / myZoomFactor);
			final int h = (int) (getHeight() / myZoomFactor);
			final int bw = myBitmap.getWidth();
			final int bh = myBitmap.getHeight();

			final int newDx, newDy;

			if (w < bw) {
				final int delta = (bw - w) / 2;
				newDx = Math.max(-delta, Math.min(delta, myDx + dx));
			} else {// bitmap宽度小于控件宽度，不响应水平拖动
				newDx = myDx;
			}
			if (h < bh) {
				final int delta = (bh - h) / 2;
				newDy = Math.max(-delta, Math.min(delta, myDy + dy));
			} else {// bitmap高度小于控件高度，不响应竖直拖动
				newDy = myDy;
			}

			if (newDx != myDx || newDy != myDy) {
				myDx = newDx;
				myDy = newDy;
				postInvalidate();
			}
		}

		private boolean myMotionControl;
		private int mySavedX;
		private int mySavedY;

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getPointerCount()) {
			case 1:// 单指
				return onSingleTouchEvent(event);
			case 2:// 双指
				return onDoubleTouchEvent(event);
			default:
				return false;
			}
		}

		private boolean onSingleTouchEvent(MotionEvent event) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				if (!myMotionControl) {
					Log.d("finish", "finish");
					finish();
				}
				myMotionControl = false;
				break;
			case MotionEvent.ACTION_DOWN:
				// myMotionControl = true;
				mySavedX = x;
				mySavedY = y;
				break;
			case MotionEvent.ACTION_MOVE:
				int dx = Math.abs(mySavedX - x);
				int dy = Math.abs(mySavedY - y);
				if ((dx > 10 || dy > 10))
					myMotionControl = true;
				if (myMotionControl) {
					shift((int) ((x - mySavedX) / myZoomFactor), (int) ((y - mySavedY) / myZoomFactor));
				}
				// myMotionControl = true;
				mySavedX = x;
				mySavedY = y;
				break;
			}
			return true;
		}

		private float myStartPinchDistance2 = -1;
		private float myStartZoomFactor;

		private boolean onDoubleTouchEvent(MotionEvent event) {// 双指缩放
			myMotionControl = true;
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_POINTER_UP:
				myStartPinchDistance2 = -1;
				break;
			case MotionEvent.ACTION_POINTER_DOWN: {
				final float diffX = event.getX(0) - event.getX(1);
				final float diffY = event.getY(0) - event.getY(1);
				myStartPinchDistance2 = Math.max(diffX * diffX + diffY * diffY, 10f);
				myStartZoomFactor = myZoomFactor;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				final float diffX = event.getX(0) - event.getX(1);
				final float diffY = event.getY(0) - event.getY(1);
				final float distance2 = Math.max(diffX * diffX + diffY * diffY, 10f);
				if (myStartPinchDistance2 < 0) {
					myStartPinchDistance2 = distance2;
					myStartZoomFactor = myZoomFactor;
				} else {
					myZoomFactor = (float) (myStartZoomFactor * Math.sqrt(distance2 / myStartPinchDistance2));
					postInvalidate();
				}
			}
				break;
			}
			return true;
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.zoomout);
	}
}
