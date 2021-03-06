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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

public final class ZLAndroidPaintContext extends ZLPaintContext
{

	// 一些与Paint的字体样式属性相关的属性
	// 可以查看这里的API：http://api.apkbus.com/reference/android/graphics/Paint.html
	public static ZLBooleanOption	AntiAliasOption		= new ZLBooleanOption("Fonts", "AntiAlias", true);		// 启用抗锯齿
	public static ZLBooleanOption	DeviceKerningOption	= new ZLBooleanOption("Fonts", "DeviceKerning", false); // 启用设备的文本字距
	public static ZLBooleanOption	DitheringOption		= new ZLBooleanOption("Fonts", "Dithering", false);	// 启用防抖动
	public static ZLBooleanOption	SubpixelOption		= new ZLBooleanOption("Fonts", "Subpixel", false);		// true时，有助于文本在LCD屏幕上的显示效果

	// 画布
	private final Canvas			myCanvas;

	// 画笔
	private final Paint				myTextPaint			= new Paint();
	private final Paint				myLinePaint			= new Paint();
	private final Paint				myFillPaint			= new Paint();
	private final Paint				myOutlinePaint		= new Paint();

	/**
	 * Geometry：几何<br>
	 * 包括了屏幕大小，绘制区域大小和左边距，上边距大小
	 * 
	 * @author chenjl
	 * 
	 */
	static class Geometry
	{
		/**
		 * 屏大小
		 */
		final Size	ScreenSize;
		/**
		 * 绘制区域大小
		 */
		final Size	AreaSize;
		final int	LeftMargin;
		final int	TopMargin;

		Geometry(int screenWidth, int screenHeight, int width, int height, int leftMargin, int topMargin)
		{
			ScreenSize = new Size(screenWidth, screenHeight);
			AreaSize = new Size(width, height);
			LeftMargin = leftMargin;
			TopMargin = topMargin;
		}
	}

	private final Geometry	myGeometry;
	private final int		myScrollbarWidth;

	private ZLColor			myBackgroundColor	= new ZLColor(0, 0, 0);

	ZLAndroidPaintContext(Canvas canvas, Geometry geometry, int scrollbarWidth)
	{
		myCanvas = canvas;
		myGeometry = geometry;
		myScrollbarWidth = scrollbarWidth;

		myTextPaint.setLinearText(false);
		myTextPaint.setAntiAlias(AntiAliasOption.getValue());
		if (DeviceKerningOption.getValue()) {
			myTextPaint.setFlags(myTextPaint.getFlags() | Paint.DEV_KERN_TEXT_FLAG);
		} else {
			myTextPaint.setFlags(myTextPaint.getFlags() & ~Paint.DEV_KERN_TEXT_FLAG);
		}
		myTextPaint.setDither(DitheringOption.getValue());
		myTextPaint.setSubpixelText(SubpixelOption.getValue());

		myLinePaint.setStyle(Paint.Style.STROKE);

		myOutlinePaint.setColor(Color.rgb(255, 127, 0));
		myOutlinePaint.setAntiAlias(true);
		myOutlinePaint.setDither(true);
		myOutlinePaint.setStrokeWidth(4);
		myOutlinePaint.setStyle(Paint.Style.STROKE);
		myOutlinePaint.setPathEffect(new CornerPathEffect(5));
		myOutlinePaint.setMaskFilter(new EmbossMaskFilter(new float[] { 1, 1, 1 }, .4f, 6f, 3.5f));
	}

	// 背景相关
	private static ZLFile	ourWallpaperFile;
	private static Bitmap	ourWallpaper;
	private static FillMode	ourFillMode;

	@Override
	public void clear(ZLFile wallpaperFile, FillMode mode)
	{
		// 背景图片不一样了或者填充的方式不一样了，都要重新绘制背景
		if (!wallpaperFile.equals(ourWallpaperFile) || mode != ourFillMode) {
			ourWallpaperFile = wallpaperFile;
			ourFillMode = mode;
			ourWallpaper = null;
			// 获取ourWallpaper位图资源
			try {
				final Bitmap fileBitmap = BitmapFactory.decodeStream(wallpaperFile.getInputStream());
				switch (mode) {
				default:
					ourWallpaper = fileBitmap;
					break;
				case tileMirror: {
					// 创建两倍宽高的位图wallpaper，然后反向镜像操作生成一个新的ourWallpaper
					final int w = fileBitmap.getWidth();
					final int h = fileBitmap.getHeight();
					final Bitmap wallpaper = Bitmap.createBitmap(2 * w, 2 * h, fileBitmap.getConfig());
					final Canvas wallpaperCanvas = new Canvas(wallpaper);
					final Paint wallpaperPaint = new Paint();

					Matrix m = new Matrix();
					wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
					m.preScale(-1, 1);
					m.postTranslate(2 * w, 0);
					wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
					m.preScale(1, -1);
					m.postTranslate(0, 2 * h);
					wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
					m.preScale(-1, 1);
					m.postTranslate(-2 * w, 0);
					wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
					ourWallpaper = wallpaper;
					break;
				}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		// 背景图像不为空，绘制吧 :-D
		if (ourWallpaper != null) {
			myBackgroundColor = ZLAndroidColorUtil.getAverageColor(ourWallpaper);
			final int w = ourWallpaper.getWidth();
			final int h = ourWallpaper.getHeight();
			final Geometry g = myGeometry;
			switch (mode) {
			case fullscreen: {
				// 全屏
				final Matrix m = new Matrix();
				m.preScale(1f * g.ScreenSize.Width / w, 1f * g.ScreenSize.Height / h);
				m.postTranslate(-g.LeftMargin, -g.TopMargin);
				myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
				break;
			}
			case stretch: {
				// 拉伸
				final Matrix m = new Matrix();
				final float sw = 1f * g.ScreenSize.Width / w;
				final float sh = 1f * g.ScreenSize.Height / h;
				final float scale;
				float dx = g.LeftMargin;
				float dy = g.TopMargin;
				if (sw < sh) {
					scale = sh;
					dx += (scale * w - g.ScreenSize.Width) / 2;
				} else {
					scale = sw;
					dy += (scale * h - g.ScreenSize.Height) / 2;
				}
				m.preScale(scale, scale);
				m.postTranslate(-dx, -dy);
				myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
				break;
			}
			case tileVertically: {
				final Matrix m = new Matrix();
				final int dx = g.LeftMargin;
				final int dy = g.TopMargin - g.TopMargin / h * h;
				m.preScale(1f * g.ScreenSize.Width / w, 1);
				m.postTranslate(-dx, -dy);
				for (int ch = g.AreaSize.Height + dy; ch > 0; ch -= h) {
					myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
					m.postTranslate(0, h);
				}
				break;
			}
			case tileHorizontally: {
				final Matrix m = new Matrix();
				final int dx = g.LeftMargin - g.LeftMargin / w * w;
				final int dy = g.TopMargin;
				m.preScale(1, 1f * g.ScreenSize.Height / h);
				m.postTranslate(-dx, -dy);
				for (int cw = g.AreaSize.Width + dx; cw > 0; cw -= w) {
					myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
					m.postTranslate(w, 0);
				}
				break;
			}
			case tile:
			case tileMirror: {
				final int dx = g.LeftMargin - g.LeftMargin / w * w;
				final int dy = g.TopMargin - g.TopMargin / h * h;
				final int fullw = g.AreaSize.Width + dx;
				final int fullh = g.AreaSize.Height + dy;
				for (int cw = 0; cw < fullw; cw += w) {
					for (int ch = 0; ch < fullh; ch += h) {
						myCanvas.drawBitmap(ourWallpaper, cw - dx, ch - dy, myFillPaint);
					}
				}
				break;
			}
			}
		} else {
			// 如果出现了异常，则用灰色(#808080)来绘制背景
			clear(new ZLColor(128, 128, 128));
		}
	}

	@Override
	public void clear(Bitmap bitmap)
	{
		myCanvas.drawBitmap(bitmap, 0, 0, null);
	}

	@Override
	public void clear(ZLColor color)
	{
		myBackgroundColor = color;
		myFillPaint.setColor(ZLAndroidColorUtil.rgb(color));
		myCanvas.drawRect(0, 0, myGeometry.AreaSize.Width, myGeometry.AreaSize.Height, myFillPaint);
	}

	@Override
	public ZLColor getBackgroundColor()
	{
		return myBackgroundColor;
	}

	public void fillPolygon(int[] xs, int ys[])
	{
		final Path path = new Path();
		final int last = xs.length - 1;
		path.moveTo(xs[last], ys[last]);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		myCanvas.drawPath(path, myFillPaint);
	}

	public void drawPolygonalLine(int[] xs, int ys[])
	{
		final Path path = new Path();
		final int last = xs.length - 1;
		path.moveTo(xs[last], ys[last]);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		myCanvas.drawPath(path, myLinePaint);
	}

	public void drawOutline(int[] xs, int ys[])
	{
		final int last = xs.length - 1;
		int xStart = (xs[0] + xs[last]) / 2;
		int yStart = (ys[0] + ys[last]) / 2;
		int xEnd = xStart;
		int yEnd = yStart;
		if (xs[0] != xs[last]) {
			if (xs[0] > xs[last]) {
				xStart -= 5;
				xEnd += 5;
			} else {
				xStart += 5;
				xEnd -= 5;
			}
		} else {
			if (ys[0] > ys[last]) {
				yStart -= 5;
				yEnd += 5;
			} else {
				yStart += 5;
				yEnd -= 5;
			}
		}

		final Path path = new Path();
		path.moveTo(xStart, yStart);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		path.lineTo(xEnd, yEnd);
		myCanvas.drawPath(path, myOutlinePaint);
	}

	@Override
	protected void setFontInternal(List<FontEntry> entries, int size, boolean bold, boolean italic, boolean underline, boolean strikeThrought)
	{
		Typeface typeface = null;
		// FontEntry fontEntry = null;
		for (FontEntry e : entries) {
			typeface = AndroidFontUtil.typeface(e, bold, italic);
			if (typeface != null) {
				// fontEntry = e;
				break;
			}
		}
		// ZLog.i(ZLog.ZLFont, fontEntry.Family + "," + italic);
		myTextPaint.setTypeface(typeface);
		myTextPaint.setTextSize(size);
		myTextPaint.setUnderlineText(underline);
		if (bold && !typeface.isBold()) {
			myTextPaint.setFakeBoldText(bold);
		} else {
			myTextPaint.setFakeBoldText(false);
		}
		if (italic && !typeface.isItalic()) {
			myTextPaint.setTextSkewX(-0.25f);
		} else {
			myTextPaint.setTextSkewX(0.0f);
		}
		myTextPaint.setStrikeThruText(strikeThrought);
	}

	@Override
	public void setTextColor(ZLColor color)
	{
		myTextPaint.setColor(ZLAndroidColorUtil.rgb(color));
	}

	@Override
	public void setLineColor(ZLColor color)
	{
		myLinePaint.setColor(ZLAndroidColorUtil.rgb(color));
	}

	@Override
	public void setLineWidth(int width)
	{
		myLinePaint.setStrokeWidth(width);
	}

	@Override
	public void setFillColor(ZLColor color, int alpha)
	{
		myFillPaint.setColor(ZLAndroidColorUtil.rgba(color, alpha));
	}

	public int getWidth()
	{
		return myGeometry.AreaSize.Width - myScrollbarWidth;
	}

	public int getHeight()
	{
		return myGeometry.AreaSize.Height;
	}

	@Override
	public int getStringWidth(char[] string, int offset, int length)
	{
		// 是否包括连字符
		boolean containsSoftHyphen = false;
		for (int i = offset; i < offset + length; ++i) {
			if (string[i] == (char) 0xAD) {
				containsSoftHyphen = true;
				break;
			}
		}
		if (!containsSoftHyphen) {
			return (int) (myTextPaint.measureText(new String(string, offset, length)) + 0.5f);
		} else {
			final char[] corrected = new char[length];
			int len = 0;
			for (int o = offset; o < offset + length; ++o) {
				final char chr = string[o];
				if (chr != (char) 0xAD) {
					corrected[len++] = chr;
				}
			}
			return (int) (myTextPaint.measureText(corrected, 0, len) + 0.5f);
		}
	}

	@Override
	protected int getSpaceWidthInternal()
	{
		return (int) (myTextPaint.measureText(" ", 0, 1) + 0.5f);// 测量一个空格的宽度
	}

	@Override
	protected int getStringHeightInternal()
	{
		return (int) (myTextPaint.getTextSize() + 0.5f);// 获取文字的高度竟然是Paint.getTextSize()
														// ？
	}

	@Override
	protected int getDescentInternal()
	{
		return (int) (myTextPaint.descent() + 0.5f);// descent代表baseline以下的height
	}

	@Override
	public void drawString(int x, int y, char[] string, int offset, int length)
	{
		boolean containsSoftHyphen = false;
		for (int i = offset; i < offset + length; ++i) {
			if (string[i] == (char) 0xAD) {
				containsSoftHyphen = true;
				break;
			}
		}
		if (!containsSoftHyphen) {
			myCanvas.drawText(string, offset, length, x, y, myTextPaint);
		} else {
			final char[] corrected = new char[length];
			int len = 0;
			for (int o = offset; o < offset + length; ++o) {
				final char chr = string[o];
				if (chr != (char) 0xAD) {
					corrected[len++] = chr;
				}
			}
			myCanvas.drawText(corrected, 0, len, x, y, myTextPaint);
		}
	}
	
	@Override
	public Size imageSize(ZLImageData imageData, Size maxSize, ScalingType scaling)
	{
		final Bitmap bitmap = ((ZLAndroidImageData) imageData).getBitmap(maxSize, scaling);
		return (bitmap != null && !bitmap.isRecycled()) ? new Size(bitmap.getWidth(), bitmap.getHeight()) : null;
	}

	@Override
	public void drawImage(int x, int y, ZLImageData imageData, Size maxSize, ScalingType scaling, ColorAdjustingMode adjustingMode)
	{
		final Bitmap bitmap = ((ZLAndroidImageData) imageData).getBitmap(maxSize, scaling);//maxSize:textAreaSize
		if (bitmap != null && !bitmap.isRecycled()) {
			switch (adjustingMode) {
			case LIGHTEN_TO_BACKGROUND:
				// myFillPaint.setXfermode(new PorterDuffXfermode(
				// PorterDuff.Mode.LIGHTEN));
				break;
			case DARKEN_TO_BACKGROUND:
				// TODO:
				// myFillPaint.setXfermode(new PorterDuffXfermode(
				// PorterDuff.Mode.DARKEN));
				break;
			case NONE:
				break;
			}

			// FIXME:
			x = (myCanvas.getWidth() - bitmap.getWidth()) / 2;// fix the bug
																// image not
																// center in
																// horizontal;

			// Log.d("position","x=" + x + ",y=" + y + ",width=" +
			// bitmap.getWidth()+ ",height=" + bitmap.getHeight() + ",cw="+
			// myCanvas.getWidth() + ",ch="+ myCanvas.getHeight());
			myCanvas.drawBitmap(bitmap, x, y - bitmap.getHeight(), myFillPaint);
			myFillPaint.setXfermode(null);
		}
	}

	public void drawImage(Bitmap bitmap, float x, float y, Paint paint)
	{
		myCanvas.drawBitmap(bitmap, x, y, null);
	}

	@Override
	public void drawLine(int x0, int y0, int x1, int y1)
	{
		final Canvas canvas = myCanvas;
		final Paint paint = myLinePaint;
		paint.setAntiAlias(false);
		canvas.drawLine(x0, y0, x1, y1, paint);
		canvas.drawPoint(x0, y0, paint);
		canvas.drawPoint(x1, y1, paint);
		paint.setAntiAlias(true);
	}

	@Override
	public void fillRectangle(int x0, int y0, int x1, int y1)
	{
		if (x1 < x0) {
			int swap = x1;
			x1 = x0;
			x0 = swap;
		}
		if (y1 < y0) {
			int swap = y1;
			y1 = y0;
			y0 = swap;
		}
		myCanvas.drawRect(x0, y0, x1 + 1, y1 + 1, myFillPaint);
	}
}
