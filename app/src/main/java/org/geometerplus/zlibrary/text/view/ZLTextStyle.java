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

package org.geometerplus.zlibrary.text.view;

import java.util.List;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;

/**
 * 定义文本样式的抽象类
 * 
 * @author chenjl
 * 
 */
public abstract class ZLTextStyle
{
	public final ZLTextStyle		Parent;
	public final ZLTextHyperlink	Hyperlink;

	protected ZLTextStyle(ZLTextStyle parent, ZLTextHyperlink hyperlink)
	{
		Parent = parent != null ? parent : this;
		Hyperlink = hyperlink;
	}

	/**
	 * 字体组
	 * 
	 * @return
	 */
	public abstract List<FontEntry> getFontEntries();

	/**
	 * 字体大小
	 * 
	 * @param metrics
	 * @return
	 */
	public abstract int getFontSize(ZLTextMetrics metrics);

	/**
	 * 粗体
	 * 
	 * @return
	 */
	public abstract boolean isBold();

	/**
	 * 斜体
	 * 
	 * @return
	 */
	public abstract boolean isItalic();

	/**
	 * 下划线
	 * 
	 * @return
	 */
	public abstract boolean isUnderline();

	/**
	 * 删除线
	 * 
	 * @return
	 */
	public abstract boolean isStrikeThrough();

	public final int getLeftIndent(ZLTextMetrics metrics)
	{
//		return getLeftMargin(metrics) + getLeftPadding(metrics);
		// add by yq
		return getLeftMargin(metrics) + getLeftPadding(metrics) + getLeftBorder(metrics);
		// end by yq
	}

	public final int getRightIndent(ZLTextMetrics metrics)
	{
//		return getRightMargin(metrics) + getRightPadding(metrics);
		// add by yq
		return getRightMargin(metrics) + getRightPadding(metrics) + getRightBorder(metrics);
		// end by yq
	}

	public abstract int getLeftMargin(ZLTextMetrics metrics);

	public abstract int getRightMargin(ZLTextMetrics metrics);

	public abstract int getLeftPadding(ZLTextMetrics metrics);

	public abstract int getRightPadding(ZLTextMetrics metrics);

	/**
	 * 首行缩进
	 * 
	 * @param metrics
	 * @return
	 */
	public abstract int getFirstLineIndent(ZLTextMetrics metrics);

	/**
	 * 行间距百分比
	 * 
	 * @return
	 */
	public abstract int getLineSpacePercent();

	/**
	 * 垂直对齐方向
	 * 
	 * @param metrics
	 * @return
	 */
	public abstract int getVerticalAlign(ZLTextMetrics metrics);

	/**
	 * 对应padding-top、margin-top等属性
	 * 
	 * @param metrics
	 * @return
	 */
//	public abstract int getSpaceBefore(ZLTextMetrics metrics);
	public final int getSpaceBefore(ZLTextMetrics metrics) {
		return getTopMargin(metrics) + getTopPadding(metrics) + getTopBorder(metrics);
	}

	/**
	 * 对应padding-bottom、margin-bottom等属性
	 * 
	 * @param metrics
	 * @return
	 */
//	public abstract int getSpaceAfter(ZLTextMetrics metrics);
	public final int getSpaceAfter(ZLTextMetrics metrics) {
		return getBottomMargin(metrics) + getBottomPadding(metrics) + getBottomBorder(metrics);
	}

	/**
	 * 对齐方向
	 * 
	 * @return
	 */
	public abstract byte getAlignment();

	/**
	 * 是否允许连字符(-)，在单词无法在一行内显示时的跨行显示时是否使用-
	 * 
	 * @return
	 */
	public abstract boolean allowHyphenations();

	// add by yq
//	public final int getTopSpace(ZLTextMetrics metrics)
//	{
//		return getSpaceBefore(metrics) + getTopBorder(metrics);
//	}
//
//	public final int getBottomSpace(ZLTextMetrics metrics)
//	{
//		return getSpaceAfter(metrics) + getBottomBorder(metrics);
//	}
//	
	/**
	 * 是否支持文本颜色
	 * 
	 * @return
	 */
	public abstract boolean isTextColorSupported();

	/**
	 * 文本颜色
	 * 
	 * @return
	 */
	public abstract ZLColor getTextColor();

	/**
	 * 是否支持文本背景颜色
	 * 
	 * @return
	 */
	public abstract boolean isBgColorSupported();

	/**
	 * 背景颜色
	 * 
	 * @return
	 */
	public abstract ZLColor getBackgroundColor();
	
	public abstract int getLeftBorder(ZLTextMetrics metrics);
	public abstract int getLeftBorderOnly(ZLTextMetrics metrics);

	public abstract int getRightBorder(ZLTextMetrics metrics);
	public abstract int getRightBorderOnly(ZLTextMetrics metrics);
	
	public abstract int getTopBorder(ZLTextMetrics metrics);
	public abstract int getTopBorderOnly(ZLTextMetrics metrics);

	public abstract int getBottomBorder(ZLTextMetrics metrics);
	public abstract int getBottomBorderOnly(ZLTextMetrics metrics);
	
	public abstract int getTopMargin(ZLTextMetrics metrics);
	public abstract int getBottomMargin(ZLTextMetrics metrics);
	public abstract int getTopPadding(ZLTextMetrics metrics);
	public abstract int getBottomPadding(ZLTextMetrics metrics);
	
	public abstract ZLColor getTopBorderColor();
	public abstract ZLColor getRightBorderColor();
	public abstract ZLColor getBottomBorderColor();
	public abstract ZLColor getLeftBorderColor();

	// end by yq

	// add by cjl
	public abstract int getLeftPaddingOnly(ZLTextMetrics metrics);

	public abstract int getRightPaddingOnly(ZLTextMetrics metrics);
	
	public abstract int getTopPaddingOnly(ZLTextMetrics metrics);

	public abstract int getBottomPaddingOnly(ZLTextMetrics metrics);

//	public abstract int getSpaceBeforeOnly(ZLTextMetrics metrics);
//
//	public abstract int getSpaceAfterOnly(ZLTextMetrics metrics);
	// end by cjl
}
