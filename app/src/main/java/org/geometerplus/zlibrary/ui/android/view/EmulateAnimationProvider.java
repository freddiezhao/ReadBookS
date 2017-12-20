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

import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLView.PageIndex;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.ThemeUtil;

/**
 * 仿真翻页动画 从新浪阅读移植
 * 
 * @author Penggj
 * 
 */
class EmulateAnimationProvider extends SimpleAnimationProvider {

	/**
	 * 阅读内容显示的宽度和高度
	 */
	private int mDisplayWidth;
	private int mDisplayHeight;
	private float mDisplayMaxLength;
	/**
	 * 当前页翻起背面及下一页看到部分的path
	 */
	private Path mPath0;
	/**
	 * 当前页翻起页背面的path
	 */
	private Path mPath1;

	/**
	 * 拖拽点对应的页脚
	 */
	private int mCornerX = 0;
	private int mCornerY = 0;

	/**
	 * 是否属于右上左下
	 */
	private boolean mIsRTandLB;
	/**
	 * 拖拽点
	 */
	private PointF mTouch = new PointF();
	/**
	 * 拖拽点到页脚的距离
	 */
	private float mTouchToCornerDis;
	/**
	 * 拖拽点和页脚点的中点
	 */
	private float mMiddleX;
	private float mMiddleY;
	private float mDegrees;

	/**
	 * 两条贝塞尔曲线
	 */
	private PointF mBezierStart1 = new PointF(); // 起点
	private PointF mBezierControl1 = new PointF(); // 控制点
	private PointF mBeziervertex1 = new PointF(); // 顶点
	private PointF mBezierEnd1 = new PointF(); // 终点

	private PointF mBezierStart2 = new PointF();
	private PointF mBezierControl2 = new PointF();
	private PointF mBeziervertex2 = new PointF();
	private PointF mBezierEnd2 = new PointF();

	private int[] mBackShadowColors;
	private int[] mFrontShadowColors;
	private GradientDrawable mBackShadowDrawableLR;
	private GradientDrawable mBackShadowDrawableRL;
	private GradientDrawable mFolderShadowDrawableLR;
	private GradientDrawable mFolderShadowDrawableRL;

	private GradientDrawable mFrontShadowDrawableHBT;
	private GradientDrawable mFrontShadowDrawableHTB;
	private GradientDrawable mFrontShadowDrawableVLR;
	private GradientDrawable mFrontShadowDrawableVRL;

	private GradientDrawable mSilideShadowDrawable;

	private Paint mPaint;

	private Matrix mMatrix;
	private float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };

	private ZLView.PageIndex toIndex;

//	private ReadStyleManager	styleManager;

	/**
	 * 阴影的默认宽度
	 */
	private static final int SHADOW_WIDTH_IN_DP = 30;
	private final String TAG = getClass().getSimpleName();
	private final Paint myPaint = new Paint();

	EmulateAnimationProvider(BitmapManager bitmapManager) {
		super(bitmapManager);
		init();
	}

	private void init() {

		mTouch.x = 0.01f;
		mTouch.y = 0.01f;

		mPath0 = new Path();
		mPath1 = new Path();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAlpha(180);

		mMatrix = new Matrix();

		createDrawable();

//		styleManager = ReadStyleManager
//				.getInstance(SinaBookApplication.gContext);

	}

	@Override
	void startManualScrolling(int x, int y) {// 手动
		super.startManualScrolling(x, y);
		// myEndY = myStartY = (y > (myHeight / 2) ? myHeight : 0);
		// myEndY = myStartY = 0;

		resetCornerAndTouch();
	}

	boolean isIndexInited = false;
	boolean isCenterArea = false;

	@Override
	void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		if (!isIndexInited) {
			toIndex = getPageToScrollTo(x, y);
			isIndexInited = true;
			isCenterArea = isCenterArea(myStartX, myStartY);
		}
		if (ZLView.PageIndex.next == toIndex && x == myStartX) {
			// Log.d("terminate", "terminate");
			// terminate();
		}
		if (ZLView.PageIndex.previous == toIndex || isCenterArea) {
			myEndY = myStartY = (y > (myHeight / 2) ? myHeight : 0);
		}
	}

	@Override
	void terminate() {
		super.terminate();
		isIndexInited = false;
		fromBitmap = null;
		toBitmap = null;
	}

	@Override
	void startAnimatedScrolling(int x, int y, float velocityX, float velocityY, int speed) {// 惯性
		super.startAnimatedScrolling(x, y, velocityX, velocityY, speed);
		toIndex = getPageToScrollTo(myStartX, myStartY);
		resetCornerAndTouch();
	}

	@Override
	public void startAnimatedScrolling(PageIndex pageIndex, Integer x, Integer y, int speed) {// 点触和音量键
		super.startAnimatedScrolling(pageIndex, x, y, speed);
		toIndex = getPageToScrollTo();
		resetCornerAndTouch();
	}

	@Override
	protected void setupAnimatedScrollingStart(Integer x, Integer y) {// 点触和音量键
		super.setupAnimatedScrollingStart(x, y);
		if (ZLView.PageIndex.previous == toIndex || isCenterArea(x, y)) {
			myEndY = myStartY = 0;
		}
	}

	private Bitmap fromBitmap, toBitmap;

	@Override
	protected void drawInternal(Canvas canvas) {
		caculatePoints();
		PageIndex index = getPageToScrollTo();
		boolean next = index == ZLView.PageIndex.next;
		// Log.d("index", index + "");
		if (fromBitmap == null || toBitmap == null) {
			fromBitmap = next ? getBitmapFrom() : getBitmapTo();
			toBitmap = next ? getBitmapTo() : getBitmapFrom();
		}
		drawContentBg(canvas);
		drawCurrentPageArea(canvas, fromBitmap);
		drawNextPageAreaAndShadow(canvas, toBitmap);
		drawCurrentPageShadow(canvas);
		drawCurrentBackArea(canvas, fromBitmap);
	}

	private void drawContentBg(Canvas canvas) {
		// if (ReadStyleManager.READ_MODE_NIGHT == styleManager.getReadMode()) {
		// // 如果是夜间模式，则直接绘制夜间模式背景
		// canvas.drawColor(ResourceUtil.getColor(R.color.reading_bg_night));
		// } else {
		// int resId = styleManager.getReadBgResId();
		// canvas.drawColor(ThemeUtil.getBackAreaColor(resId));
		// }
//		canvas.drawColor(Color.RED);
		canvas.drawColor(0xFFF9F9F9);//图文混排没有夜间模式，全部采用纯色，此处采用白色主题的颜色
	}

	/**
	 * 判断触发区域是否是在屏幕的中央区域
	 * 
	 * @param e
	 * @return
	 */
	public boolean isCenterArea(Integer x, Integer y) {
		if (x == null || y == null)
			return true;
		if (y >= mDisplayHeight / 3 && y <= mDisplayHeight * 2 / 3) {
			y = 0;
			return true;
		}
		return false;
	}

	/**
	 * 计算贝塞尔曲线的各点的值
	 */
	private void caculatePoints() {
		caculateCorner();
		mTouch.x = myEndX;
		mTouch.y = myEndY;
		// 尽量防止触摸点在x轴和y轴上
		if (mTouch.x == 0) {
			mTouch.x = 0.01f;
		} else if (mTouch.x == mDisplayWidth) {
			mTouch.x = mDisplayWidth - 0.01f;
		}
		if (mTouch.y == 0) {
			mTouch.y = 0.01f;
		} else if (mTouch.y == mDisplayHeight) {
			mTouch.y = mDisplayHeight - 0.01f;
		}

		mMiddleX = (mTouch.x + mCornerX) / 2;
		mMiddleY = (mTouch.y + mCornerY) / 2;
		mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
		mBezierControl1.y = mCornerY;
		mBezierControl2.x = mCornerX;
		mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

		mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
		mBezierStart1.y = mCornerY;

		if (mTouch.x > 0 && mTouch.x < mDisplayWidth) {
			if (mBezierStart1.x < 0 || mBezierStart1.x > mDisplayWidth) {
				if (mBezierStart1.x < 0)
					mBezierStart1.x = mDisplayWidth - mBezierStart1.x;

				float f1 = Math.abs(mCornerX - mTouch.x);
				float f2 = mDisplayWidth * f1 / mBezierStart1.x;
				mTouch.x = Math.abs(mCornerX - f2);

				float f3 = Math.abs(mCornerX - mTouch.x) * Math.abs(mCornerY - mTouch.y) / f1;
				mTouch.y = Math.abs(mCornerY - f3);

				mMiddleX = (mTouch.x + mCornerX) / 2;
				mMiddleY = (mTouch.y + mCornerY) / 2;

				mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
				mBezierControl1.y = mCornerY;

				mBezierControl2.x = mCornerX;
				mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
				mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
			}
		}
		// Log.d("middle", "mMiddleX=" + mMiddleX + ",mMiddleY=" + mMiddleY);
		mBezierStart2.x = mCornerX;
		mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2;

		mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX), (mTouch.y - mCornerY));

		mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
		mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1, mBezierStart2);
		mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
		mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
		mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
		mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
	}

	private void resetCornerAndTouch() {
		mTouch.x = 0.01f;
		mTouch.y = 0.01f;
		mCornerX = 0;
		mCornerY = 0;
	}

	private void caculateCorner() {
		mDisplayWidth = myWidth;
		mDisplayHeight = myHeight;
		mDisplayMaxLength = (float) Math.hypot(mDisplayWidth, mDisplayHeight - 5);

		mCornerX = mDisplayWidth;
		if (myStartY <= mDisplayHeight / 2) {
			mCornerY = 0;
		} else {
			mCornerY = mDisplayHeight;
		}

		if ((mCornerX == 0 && mCornerY == mDisplayHeight) || (mCornerX == mDisplayWidth && mCornerY == 0)) {
			mIsRTandLB = true;
		} else {
			mIsRTandLB = false;
		}
	}

	/**
	 * P1 P2构成一条直线，P3 构成一条直线，求两条直线的交点
	 * 
	 * @param P1
	 * @param P2
	 * @param P3
	 * @param P4
	 * @return
	 */
	private PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
		PointF CrossP = new PointF();
		float a1 = (P2.y - P1.y) / (P2.x - P1.x);// 斜率
		float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);// 常量

		float a2 = (P4.y - P3.y) / (P4.x - P3.x);
		float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
		CrossP.x = (b2 - b1) / (a1 - a2);
		CrossP.y = a1 * CrossP.x + b1;
		return CrossP;
	}

	/**
	 * 仿真动画--画出当前页
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap) {
		mPath0.reset();
		mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y);
		mPath0.lineTo(mTouch.x, mTouch.y);
		mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y);
		mPath0.lineTo(mCornerX, mCornerY);
		mPath0.close();

		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}

	/**
	 * 仿真动画--画出下一页阴影
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
		mPath1.reset();
		mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.lineTo(mCornerX, mCornerY);
		mPath1.close();

		mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x - mCornerX, mBezierControl2.y - mCornerY));
		int leftx;
		int rightx;
		GradientDrawable mBackShadowDrawable;
		if (mIsRTandLB) {
			leftx = (int) (mBezierStart1.x);
			rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
			mBackShadowDrawable = mBackShadowDrawableLR;
		} else {
			leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
			rightx = (int) mBezierStart1.x;
			mBackShadowDrawable = mBackShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx, (int) (mDisplayMaxLength + mBezierStart1.y));
		mBackShadowDrawable.draw(canvas);
		canvas.restore();
	}

	/**
	 * 仿真动画--画出当前页阴影
	 * 
	 * @param canvas
	 */
	private void drawCurrentPageShadow(Canvas canvas) {
		if (mTouch.x <= 0.01f && mTouch.y <= 0.01f) {
			return;
		}
		double degree;
		if (mIsRTandLB) {
			degree = Math.PI / 4 - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
		} else {
			degree = Math.PI / 4 - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x - mBezierControl1.x);
		}
		int shadow = (int) (SHADOW_WIDTH_IN_DP * (Math.abs(mTouch.x - mCornerX) / mCornerX));
		double d1 = (float) shadow * 1.414 * Math.cos(degree);
		double d2 = (float) shadow * 1.414 * Math.sin(degree);
		float x = (float) (mTouch.x + d1);
		float y;
		if (mIsRTandLB) {
			y = (float) (mTouch.y + d2);
		} else {
			y = (float) (mTouch.y - d2);
		}
		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
		mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.close();
		float rotateDegrees;
		canvas.save();

		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		int leftx;
		int rightx;
		GradientDrawable mCurrentPageShadow;
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl1.x);
			rightx = (int) mBezierControl1.x + shadow;
			mCurrentPageShadow = mFrontShadowDrawableVLR;
		} else {
			leftx = (int) (mBezierControl1.x - shadow);
			rightx = (int) mBezierControl1.x + 1;
			mCurrentPageShadow = mFrontShadowDrawableVRL;
		}

		rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y));
		canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
		mCurrentPageShadow.setBounds(leftx, (int) (mBezierControl1.y - mDisplayMaxLength), rightx, (int) (mBezierControl1.y));
		mCurrentPageShadow.draw(canvas);
		canvas.restore();

		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.close();
		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl2.y);
			rightx = (int) (mBezierControl2.y + shadow);
			mCurrentPageShadow = mFrontShadowDrawableHTB;
		} else {
			leftx = (int) (mBezierControl2.y - shadow);
			rightx = (int) (mBezierControl2.y + 1);
			mCurrentPageShadow = mFrontShadowDrawableHBT;
		}
		rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x));
		canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
		float temp;
		if (mBezierControl2.y < 0)
			temp = mBezierControl2.y - mDisplayHeight;
		else
			temp = mBezierControl2.y;

		int hmg = (int) Math.hypot(mBezierControl2.x, temp);
		if (hmg > mDisplayMaxLength)
			mCurrentPageShadow.setBounds((int) (mBezierControl2.x - shadow) - hmg, leftx, (int) (mBezierControl2.x + mDisplayMaxLength) - hmg, rightx);
		else
			mCurrentPageShadow.setBounds((int) (mBezierControl2.x - mDisplayMaxLength), leftx, (int) (mBezierControl2.x), rightx);
		mCurrentPageShadow.draw(canvas);
		canvas.restore();
	}

	/**
	 * 仿真动画--画出当前页后面部分
	 * 
	 * @param canvas
	 * @param bitmap
	 */
	private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
		int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
		float f1 = Math.abs(i - mBezierControl1.x);
		int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);
		float f3 = Math.min(f1, f2);
		mPath1.reset();
		mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath1.close();
		GradientDrawable mFolderShadowDrawable;
		int left;
		int right;
		if (mIsRTandLB) {
			left = (int) (mBezierStart1.x - 1);
			right = (int) (mBezierStart1.x + f3 + 1);
			mFolderShadowDrawable = mFolderShadowDrawableLR;
		} else {
			left = (int) (mBezierStart1.x - f3 - 1);
			right = (int) (mBezierStart1.x + 1);
			mFolderShadowDrawable = mFolderShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);

		// mPaint.setColorFilter(mColorMatrixFilter);

		float dis = (float) Math.hypot(mCornerX - mBezierControl1.x, mBezierControl2.y - mCornerY);
		float f8 = (mCornerX - mBezierControl1.x) / dis;
		float f9 = (mBezierControl2.y - mCornerY) / dis;
		mMatrixArray[0] = 1 - 2 * f9 * f9;
		mMatrixArray[1] = 2 * f8 * f9;
		mMatrixArray[3] = mMatrixArray[1];
		mMatrixArray[4] = 1 - 2 * f8 * f8;
		mMatrix.reset();
		mMatrix.setValues(mMatrixArray);
		mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
		mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		canvas.drawBitmap(bitmap, mMatrix, mPaint);
		// mPaint.setColorFilter(null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right, (int) (mBezierStart1.y + mDisplayMaxLength));
		mFolderShadowDrawable.draw(canvas);
		canvas.restore();
	}

	private void createDrawable() {
		int[] color = { 0x222222, 0x60222222 };
		mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
		mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
		mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowColors = new int[] { 0x80111111, 0x111111 };
		mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
		mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
		mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowColors = new int[] { 0x60222222, 0x222222 };
		mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
		mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
		mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
		mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
		mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		int[] silideColors = new int[] { 0x60222222, 0x222222 };
		mSilideShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, silideColors);
		mSilideShadowDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}
}
