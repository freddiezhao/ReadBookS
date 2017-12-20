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

package org.geometerplus.zlibrary.core.view;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.util.ZLColor;

import android.graphics.Bitmap;
import android.graphics.Paint;

/**
 * 画笔上下文类<br>关于文本的测量的相关方法查看：http://blog.csdn.net/tianjf0514/article/details/7642656 <br>
 * 
 * @author chenjl
 * 
 */
abstract public class ZLPaintContext {
	private final ArrayList<String> myFamilies = new ArrayList<String>();

	protected ZLPaintContext() {
	}

	/**
	 * 填充模式
	 * 
	 * @author chenjl
	 * 
	 */
	public enum FillMode {
		/**
		 * 类似装修房子时铺瓷砖那样平铺下去
		 */
		tile,
		/**
		 * 间隔反向铺瓷砖
		 */
		tileMirror,
		/**
		 * 全屏
		 */
		fullscreen,
		/**
		 * 拉伸
		 */
		stretch,
		/**
		 * 水平方向上铺瓷砖
		 */
		tileVertically,
		/**
		 * 垂直方向上铺瓷砖
		 */
		tileHorizontally
	}

	/**
	 * 绘制背景图片
	 * 
	 * @param wallpaperFile
	 * @param mode
	 */
	abstract public void clear(ZLFile wallpaperFile, FillMode mode);

	/**
	 * 绘制纯色背景
	 * 
	 * @param color
	 */
	abstract public void clear(ZLColor color);

	abstract public void clear(Bitmap bitmap);

	abstract public ZLColor getBackgroundColor();

	/**
	 * 字体重置
	 */
	private boolean myResetFont = true;
	/**
	 * 字体集合
	 */
	private List<FontEntry> myFontEntries;

	private int myFontSize; // 字体：大小
	private boolean myFontIsBold; // 字体：是否为粗体
	private boolean myFontIsItalic; // 字体：是否为斜体
	private boolean myFontIsUnderlined; // 字体：是否带下划线
	private boolean myFontIsStrikedThrough; // 字体：是否周边矩形环绕

	public final void setFont(List<FontEntry> entries, int size, boolean bold, boolean italic, boolean underline, boolean strikeThrough) {
		// 判断值是否发生了变化
		if (entries != null && !entries.equals(myFontEntries)) {
			myFontEntries = entries;
			myResetFont = true;
		}
		if (myFontSize != size) {
			myFontSize = size;
			myResetFont = true;
		}
		if (myFontIsBold != bold) {
			myFontIsBold = bold;
			myResetFont = true;
		}
		if (myFontIsItalic != italic) {
			myFontIsItalic = italic;
			myResetFont = true;
		}
		if (myFontIsUnderlined != underline) {
			myFontIsUnderlined = underline;
			myResetFont = true;
		}
		if (myFontIsStrikedThrough != strikeThrough) {
			myFontIsStrikedThrough = strikeThrough;
			myResetFont = true;
		}
		if (myResetFont) {
			myResetFont = false;
			// 发生变化了则重置
			setFontInternal(myFontEntries, size, bold, italic, underline, strikeThrough);
			mySpaceWidth = -1;
			myStringHeight = -1;
			myDescent = -1;
		}
	}

	/**
	 * 设置字体效果
	 * 
	 * @param entries
	 * @param size
	 * @param bold
	 * @param italic
	 * @param underline
	 * @param strikeThrough
	 */
	abstract protected void setFontInternal(List<FontEntry> entries, int size, boolean bold, boolean italic, boolean underline, boolean strikeThrough);

	/**
	 * 设置文本颜色
	 * 
	 * @param color
	 */
	abstract public void setTextColor(ZLColor color);

	/**
	 * 设置下划线颜色
	 * 
	 * @param color
	 */
	abstract public void setLineColor(ZLColor color);

	/**
	 * 设置下划线宽度
	 * 
	 * @param width
	 */
	abstract public void setLineWidth(int width);

	/**
	 * 设置填充的颜色
	 * 
	 * @param color
	 */
	final public void setFillColor(ZLColor color) {
		setFillColor(color, 0xFF);
	}

	/**
	 * 设置填充的颜色
	 * 
	 * @param color
	 * @param alpha
	 */
	abstract public void setFillColor(ZLColor color, int alpha);

	/**
	 * 获取宽度
	 * 
	 * @return
	 */
	abstract public int getWidth();

	/**
	 * 获取高度
	 * 
	 * @return
	 */
	abstract public int getHeight();

	/**
	 * 传入字符，测量其宽度
	 * 
	 * @param string
	 * @return
	 */
	public final int getStringWidth(String string) {
		return getStringWidth(string.toCharArray(), 0, string.length());
	}

	/**
	 * 传入字符，测量其宽度
	 * 
	 * @param string
	 * @param offset
	 * @param length
	 * @return
	 */
	abstract public int getStringWidth(char[] string, int offset, int length);

	private int mySpaceWidth = -1;

	/**
	 * 获取每个元素间的间隙宽度
	 * 
	 * @return
	 */
	public final int getSpaceWidth() {
		int spaceWidth = mySpaceWidth;
		if (spaceWidth == -1) {
			spaceWidth = getSpaceWidthInternal();
			mySpaceWidth = spaceWidth;
		}
		return spaceWidth;
	}

	/**
	 * 获取每个元素间的间隙宽度(具体的内部实现由子类实现)
	 * 
	 * @return
	 */
	abstract protected int getSpaceWidthInternal();

	private int myStringHeight = -1;

	/**
	 * 获取当前字体大小配置下的字符的高度
	 * 
	 * @return
	 */
	public final int getStringHeight() {
		int stringHeight = myStringHeight;
		if (stringHeight == -1) {
			stringHeight = getStringHeightInternal();
			myStringHeight = stringHeight;
		}
		return stringHeight;
	}

	/**
	 * 获取当前字体大小配置下的字符的高度(具体的内部实现由子类实现)
	 * 
	 * @return
	 */
	abstract protected int getStringHeightInternal();

	/**
	 * 字符baseline基准线以下到字符最低处的距离
	 */
	private int myDescent = -1;

	public final int getDescent() {
		int descent = myDescent;
		if (descent == -1) {
			descent = getDescentInternal();
			myDescent = descent;
		}
		return descent;
	}

	abstract protected int getDescentInternal();

	/**
	 * 绘制字符
	 * 
	 * @param x
	 * @param y
	 * @param string
	 */
	public final void drawString(int x, int y, String string) {
		drawString(x, y, string.toCharArray(), 0, string.length());
	}

	/**
	 * 绘制字符(具体的内部实现由子类实现)
	 * 
	 * @param x
	 * @param y
	 * @param string
	 * @param offset
	 * @param length
	 */
	abstract public void drawString(int x, int y, char[] string, int offset, int length);

	/**
	 * Size静态内部类<br>
	 * TODO CJL 暂时不知道干啥用
	 * 
	 * @author chenjl
	 * 
	 */
	public static final class Size {
		public final int Width;
		public final int Height;

		public Size(int w, int h) {
			Width = w;
			Height = h;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (!(other instanceof Size)) {
				return false;
			}
			final Size s = (Size) other;
			return Width == s.Width && Height == s.Height;
		}
	}

	/**
	 * 缩放类型
	 * 
	 * @author chenjl
	 * 
	 */
	public static enum ScalingType {
		/**
		 * 原始大小
		 */
		OriginalSize,
		/**
		 * 整数倍
		 */
		IntegerCoefficient,
		/**
		 * 合适的最大值
		 */
		FitMaximum
	}

	/**
	 * 颜色适配模式
	 * 
	 * @author chenjl
	 * 
	 */
	public enum ColorAdjustingMode {
		NONE, DARKEN_TO_BACKGROUND, LIGHTEN_TO_BACKGROUND
	}

	/**
	 * 图片大小
	 * 
	 * @param image
	 * @param maxSize
	 * @param scaling
	 * @return
	 */
	abstract public Size imageSize(ZLImageData image, Size maxSize, ScalingType scaling);

	/**
	 * 绘制图片
	 * 
	 * @param x
	 * @param y
	 * @param image
	 * @param maxSize
	 * @param scaling
	 * @param adjustingMode
	 */
	abstract public void drawImage(int x, int y, ZLImageData image, Size maxSize, ScalingType scaling, ColorAdjustingMode adjustingMode);

	abstract public void drawImage(Bitmap bitmap, float x, float y, Paint paint);

	/**
	 * 绘制线
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	abstract public void drawLine(int x0, int y0, int x1, int y1);

	/**
	 * 填充矩形
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	abstract public void fillRectangle(int x0, int y0, int x1, int y1);

	/**
	 * 绘制多边形线
	 * 
	 * @param xs
	 * @param ys
	 */
	abstract public void drawPolygonalLine(int[] xs, int ys[]);

	/**
	 * 填充多边形
	 * 
	 * @param xs
	 * @param ys
	 */
	abstract public void fillPolygon(int[] xs, int[] ys);

	/**
	 * 绘制外边框
	 * 
	 * @param xs
	 * @param ys
	 */
	abstract public void drawOutline(int[] xs, int ys[]);
}
