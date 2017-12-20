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

import org.geometerplus.zlibrary.core.application.ZLApplication;

abstract public class ZLView
{

	public final ZLApplication	Application;
	private ZLPaintContext		myViewContext	= new DummyPaintContext();

	protected ZLView(ZLApplication application)
	{
		Application = application;
	}

	protected final void setContext(ZLPaintContext context)
	{
		myViewContext = context;
	}

	public final ZLPaintContext getContext()
	{
		return myViewContext;
	}

	public final int getContextWidth()
	{
		return myViewContext.getWidth();
	}

	public final int getContextHeight()
	{
		return myViewContext.getHeight();
	}

	/**
	 * 定义底部区域的接口
	 * 
	 * @author chenjl
	 * 
	 */
	abstract public interface FooterArea
	{
		/**
		 * 获取底部区域的高度
		 * 
		 * @return
		 */
		int getHeight();

		/**
		 * 底部区域的绘制
		 * 
		 * @param context
		 */
		void paint(ZLPaintContext context);
	}

	abstract public FooterArea getFooterArea();

	/**
	 * 定义当前页索引的枚举类
	 * 
	 * @author chenjl
	 * 
	 */
	public static enum PageIndex
	{
		previous, current, next;

		public PageIndex getNext()
		{
			switch (this) {
			case previous:
				return current;
			case current:
				return next;
			default:
				return null;
			}
		}

		public PageIndex getPrevious()
		{
			switch (this) {
			case next:
				return current;
			case current:
				return previous;
			default:
				return null;
			}
		}
	};

	/**
	 * 定义绘制方向的枚举类
	 * 
	 * @author chenjl
	 * 
	 */
	public static enum Direction
	{
		leftToRight(true), rightToLeft(true), up(false), down(false);

		public final boolean	IsHorizontal;

		Direction(boolean isHorizontal)
		{
			IsHorizontal = isHorizontal;
		}
	};

	/**
	 * 定义翻页的动作的动画类型
	 * 
	 * @author chenjl
	 * 
	 */
	public static enum Animation
	{
		none, curl, slide, shift, left2right
	}

	/**
	 * 获取当前配置的动画类型
	 * 
	 * @return
	 */
	public abstract Animation getAnimationType();

	/**
	 * 准备某一页(PageIndex pageIndex)的数据
	 * 
	 * @param context
	 * @param pageIndex
	 */
	abstract public void preparePage(ZLPaintContext context, PageIndex pageIndex);

	/**
	 * 绘制
	 * 
	 * @param context
	 * @param pageIndex
	 */
	abstract public void paint(ZLPaintContext context, PageIndex pageIndex);

	/**
	 * 滑动结束回调
	 * 
	 * @param pageIndex
	 */
	abstract public void onScrollingFinished(PageIndex pageIndex);

	/**
	 * 手指按下
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerPress(int x, int y)
	{
		return false;
	}

	/**
	 * 手指释放
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerRelease(int x, int y, float velocityX, float velocityY)
	{
		return false;
	}

	/**
	 * 手指移动
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerMove(int x, int y)
	{
		return false;
	}

	/**
	 * 手指长按
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerLongPress(int x, int y)
	{
		return false;
	}

	/**
	 * 手指长按之后释放
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerReleaseAfterLongPress(int x, int y)
	{
		return false;
	}

	/**
	 * 手指长按之后移动
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerMoveAfterLongPress(int x, int y)
	{
		return false;
	}

	/**
	 * 快速的点按
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerSingleTap(int x, int y)
	{
		return false;
	}

	/**
	 * 双击
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onFingerDoubleTap(int x, int y)
	{
		return false;
	}

	/**
	 * 是否支持双击
	 * 
	 * @return
	 */
	public boolean isDoubleTapSupported()
	{
		return false;
	}

	/**
	 * 滚动球滚动
	 * 
	 * @param diffX
	 * @param diffY
	 * @return
	 */
	public boolean onTrackballRotated(int diffX, int diffY)
	{
		return false;
	}

	/**
	 * 是否显示滚动条
	 * 
	 * @return
	 */
	public abstract boolean isScrollbarShown();

	/**
	 * 获取滚动条全长
	 * 
	 * @return
	 */
	public abstract int getScrollbarFullSize();

	/**
	 * 滚动条起始绘制位置
	 * 
	 * @param pageIndex
	 * @return
	 */
	public abstract int getScrollbarThumbPosition(PageIndex pageIndex);

	/**
	 * 滚动条绘制长度
	 * 
	 * @param pageIndex
	 * @return
	 */
	public abstract int getScrollbarThumbLength(PageIndex pageIndex);

	/**
	 * 是否可滚动
	 * 
	 * @param index
	 * @return
	 */
	public abstract boolean canScroll(PageIndex index);
}
