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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;

import com.sina.book.util.PixelUtil;

/**
 * 左右翻页动画 从新浪阅读移植
 * 
 * @author Penggj
 * 
 */
class Left2RightAnimationProvider extends SimpleAnimationProvider
{
	/**
	 * 阴影的默认宽度
	 */
	private static final int	SHADOW_WIDTH_IN_DP	= 20;
	private final String		TAG					= getClass().getSimpleName();
	private final Paint			myPaint				= new Paint();
	private GradientDrawable	mSilideShadowL2RDrawable,
								mSilideShadowT2BDrawable;

	Left2RightAnimationProvider(BitmapManager bitmapManager)
	{
		super(bitmapManager);
		int[] silideColors = new int[] { 0x60222222, 0x222222 };
		mSilideShadowL2RDrawable = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, silideColors);
		mSilideShadowT2BDrawable = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, silideColors);
		mSilideShadowL2RDrawable
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mSilideShadowT2BDrawable
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}

	@Override
	protected void drawInternal(Canvas canvas)
	{
		myPaint.setColor(Color.rgb(127, 127, 127));
		int distance = 0;
		if (myDirection.IsHorizontal) {
			distance = myEndX - myStartX;
		} else {
			distance = myEndY - myStartY;
		}
		// Log.d(TAG, "drawInternal,dx = " + distance + ",myWidth = " + myWidth
		// + ",myHeight = " + myHeight);
		drawPageBitmaps(canvas, distance);
	}

	/**
	 * 左右动画--画出当前页阴影
	 * 
	 * @param canvas
	 */
	private void drawPageBitmaps(Canvas canvas, int distance)
	{
		// if (mTouch.x <= 0.01f && mTouch.y <= 0.01f) {
		// return;
		// }
		boolean isHorizontal = myDirection.IsHorizontal;
		GradientDrawable mCurrentPageShadow = isHorizontal ? mSilideShadowL2RDrawable
				: mSilideShadowT2BDrawable;
		int bound = isHorizontal ? myWidth : myHeight;
		int shadowWidth = PixelUtil.dp2px(SHADOW_WIDTH_IN_DP);
		if (distance >= 0 && distance < bound) {// 上一页
			canvas.drawBitmap(getBitmapFrom(), 0, 0, myPaint);
			if (isHorizontal) {
				canvas.drawBitmap(getBitmapTo(), distance - myWidth, 0, myPaint);
				mCurrentPageShadow.setBounds(distance, 0, distance
						+ shadowWidth, myHeight);
			} else {
				canvas.drawBitmap(getBitmapTo(), 0, distance - myHeight,
						myPaint);
				mCurrentPageShadow.setBounds(0, distance, myWidth, distance
						+ shadowWidth);

			}
		} else if (distance < 0 && distance > -bound) {// 下一页
			canvas.drawBitmap(getBitmapTo(), 0, 0, myPaint);
			if (isHorizontal) {
				canvas.drawBitmap(getBitmapFrom(), distance, 0, myPaint);
				mCurrentPageShadow.setBounds(distance + myWidth, 0, distance
						+ myWidth + shadowWidth, myHeight);
			} else {
				canvas.drawBitmap(getBitmapFrom(), 0, distance, myPaint);
				mCurrentPageShadow.setBounds(0, distance + myHeight, myWidth,
						distance + myHeight + shadowWidth);
			}

		}

		canvas.save();
		mCurrentPageShadow.draw(canvas);
		canvas.restore();
	}
}
