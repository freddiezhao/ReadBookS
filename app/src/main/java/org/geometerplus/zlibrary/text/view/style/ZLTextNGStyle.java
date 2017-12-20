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

package org.geometerplus.zlibrary.text.view.style;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

/**
 * 这个类只在{@link #ZLTextViewBase ZLTextViewBase}的applyControl 方法中被构造出<br>
 * 负责读取assets/default/style.css中的样式数据，该数据存储在ZLTextNGStyleDescription
 * myDescription中。
 * 
 * @author chenjl
 * 
 */
public class ZLTextNGStyle extends ZLTextDecoratedStyle
{
	/**
	 * 解析assets/default/style.css时构造该类，存储解析出来style.css的值
	 */
	private final ZLTextNGStyleDescription	myDescription;

	public ZLTextNGStyle(ZLTextStyle parent, ZLTextNGStyleDescription description, ZLTextHyperlink hyperlink)
	{
		super(parent, hyperlink);
		myDescription = description;
	}

	@Override
	protected List<FontEntry> getFontEntriesInternal()
	{
		final List<FontEntry> parentEntries = Parent.getFontEntries();
		final String decoratedValue = myDescription.FontFamilyOption.getValue();
		if ("".equals(decoratedValue)) {// 读取style.css文件的配置值
			return parentEntries;
		}
		final FontEntry e = FontEntry.systemEntry(decoratedValue);
		if (parentEntries.size() > 0 && e.equals(parentEntries.get(0))) {
			return parentEntries;// parentEntries首个支持的字体便是decoratedValue
		}
		final List<FontEntry> entries = new ArrayList<FontEntry>(parentEntries.size() + 1);
		entries.add(e);
		entries.addAll(parentEntries);
		return entries;// 合并返回
	}

	@Override
	protected int getFontSizeInternal(ZLTextMetrics metrics)
	{
		return myDescription.getFontSize(metrics, Parent.getFontSize(metrics));
	}

	@Override
	protected boolean isBoldInternal()
	{
		switch (myDescription.isBold()) {
		case B3_TRUE:
			return true;
		case B3_FALSE:
			return false;
		default:
			return Parent.isBold();
		}
	}

	@Override
	protected boolean isItalicInternal()
	{
		switch (myDescription.isItalic()) {
		case B3_TRUE:
			return true;
		case B3_FALSE:
			return false;
		default:
			return Parent.isItalic();
		}
	}

	@Override
	protected boolean isUnderlineInternal()
	{
		switch (myDescription.isUnderlined()) {
		case B3_TRUE:
			return true;
		case B3_FALSE:
			return false;
		default:
			return Parent.isUnderline();
		}
	}

	@Override
	protected boolean isStrikeThroughInternal()
	{
		switch (myDescription.isStrikedThrough()) {
		case B3_TRUE:
			return true;
		case B3_FALSE:
			return false;
		default:
			return Parent.isStrikeThrough();
		}
	}

	@Override
	protected int getLeftMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		return myDescription.getLeftMargin(metrics, Parent.getLeftMargin(metrics), fontSize);
	}

	@Override
	protected int getRightMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		return myDescription.getRightMargin(metrics, Parent.getRightMargin(metrics), fontSize);
	}

	@Override
	protected int getLeftPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return myDescription.getLeftPadding(metrics, Parent.getLeftPadding(metrics), fontSize);
	}

	@Override
	protected int getRightPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return myDescription.getRightPadding(metrics, Parent.getRightPadding(metrics), fontSize);
	}

	@Override
	protected int getFirstLineIndentInternal(ZLTextMetrics metrics, int fontSize)
	{
		return myDescription.getFirstLineIndent(metrics, Parent.getFirstLineIndent(metrics), fontSize);
	}

	@Override
	protected int getLineSpacePercentInternal()
	{
		final String lineHeight = myDescription.LineHeightOption.getValue();
		if (!lineHeight.matches("[1-9][0-9]*%")) {
			return Parent.getLineSpacePercent();
		}
		return Integer.valueOf(lineHeight.substring(0, lineHeight.length() - 1));
	}

	@Override
	protected int getVerticalAlignInternal(ZLTextMetrics metrics, int fontSize)
	{
		return myDescription.getVerticalAlign(metrics, Parent.getVerticalAlign(metrics), fontSize);
	}

//	@Override
//	protected int getSpaceBeforeInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
//	{
//		return myDescription.getSpaceBefore(metrics, Parent.getSpaceBefore(metrics), fontSize);
//	}
//
//	@Override
//	protected int getSpaceAfterInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
//	{
//		return myDescription.getSpaceAfter(metrics, Parent.getSpaceAfter(metrics), fontSize);
//	}

	@Override
	public byte getAlignment()
	{
		final byte defined = myDescription.getAlignment();
		if (defined != ZLTextAlignmentType.ALIGN_UNDEFINED) {
			return defined;
		}
		return Parent.getAlignment();
	}

	@Override
	public boolean allowHyphenations()
	{
		switch (myDescription.allowHyphenations()) {
		case B3_TRUE:
			return true;
		case B3_FALSE:
			return false;
		default:
			return Parent.allowHyphenations();
		}
	}

	// add by yq
	@Override
	protected int getTopMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		return myDescription.getTopMargin(metrics, Parent.getTopMargin(metrics), fontSize);
	}

	@Override
	protected int getBottomMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		return myDescription.getBottomMargin(metrics, Parent.getBottomMargin(metrics), fontSize);
	}

	@Override
	protected int getTopPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return myDescription.getTopPadding(metrics, Parent.getTopPadding(metrics), fontSize);
	}

	@Override
	protected int getBottomPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return myDescription.getBottomPadding(metrics, Parent.getBottomPadding(metrics), fontSize);
	}
	
	@Override
	public boolean isTextColorSupported()
	{
		return false;
	}

	@Override
	public ZLColor getTextColor()
	{
		return new ZLColor(0);
	}

	@Override
	public boolean isBgColorSupported()
	{
		return false;
	}

	@Override
	public ZLColor getBackgroundColor()
	{
		return new ZLColor(0);
	}
	
	@Override
	protected int getLeftBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return 0;
	}

	@Override
	protected int getRightBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return 0;
	}

	@Override
	protected int getTopBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return 0;
	}

	@Override
	protected int getBottomBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		return 0;
	}
	
	@Override
	public ZLColor getTopBorderColor()
	{
		return new ZLColor(0);
	}

	@Override
	public ZLColor getRightBorderColor()
	{
		return new ZLColor(0);
	}

	@Override
	public ZLColor getBottomBorderColor()
	{
		return new ZLColor(0);
	}

	@Override
	public ZLColor getLeftBorderColor()
	{
		return new ZLColor(0);
	}

	// end by yq

	@Override
	public String toString()
	{
		return "ZLTextNGStyle[" + myDescription.Name + "]";
	}
}