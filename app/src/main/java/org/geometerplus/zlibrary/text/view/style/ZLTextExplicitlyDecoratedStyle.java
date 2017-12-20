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
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.model.ZLTextCSSStyleEntry;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.model.ZLTextStyleEntry;
import org.geometerplus.zlibrary.text.model.ZLTextStyleEntry.SizeUnit;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

/**
 * 详细的，具体的，明确的修饰样式类
 * 
 * @author chenjl
 * 
 */
public class ZLTextExplicitlyDecoratedStyle extends ZLTextDecoratedStyle implements ZLTextStyleEntry.Feature, ZLTextStyleEntry.FontModifier, ZLTextStyleEntry.BorderFeature
{
	/**
	 * 样式数据
	 */
	private final ZLTextStyleEntry	myEntry;

	public ZLTextExplicitlyDecoratedStyle(ZLTextStyle parent, ZLTextStyleEntry entry)
	{
		super(parent, parent.Hyperlink);
		myEntry = entry;
	}

	@Override
	protected List<FontEntry> getFontEntriesInternal()
	{
		final List<FontEntry> parentEntries = Parent.getFontEntries();
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSFontFamilyOption.getValue()) {
			return parentEntries;// 首先检查UseCSSFontFamilyOption的开关是否开启
		}

		if (!myEntry.isFeatureSupported(FONT_FAMILY)) {
			return parentEntries;// 再检查样式数据信息中是否支持字体
		}

		// 开关开启了，并且支持字体
		final List<FontEntry> entries = myEntry.getFontEntries();
		final int lSize = entries.size();
		if (lSize == 0) {
			return parentEntries;
		}

		final int pSize = parentEntries.size();
		if (pSize > lSize && entries.equals(parentEntries.subList(0, lSize))) {
			return parentEntries;// 在父样式支持的字体样式种类范围内
		}

		final List<FontEntry> allEntries = new ArrayList<FontEntry>(pSize + lSize);
		allEntries.addAll(entries);
		allEntries.addAll(parentEntries);
		return allEntries;// 子样式继承父样式后一并返回所有支持的样式
	}

	private ZLTextStyle	myTreeParent;

	private ZLTextStyle computeTreeParent()
	{
		if (myEntry.Depth == 0) {
			return Parent.Parent;
		}
		int count = 0;
		ZLTextStyle p = Parent;
		for (; p != p.Parent; p = p.Parent) {
			if (p instanceof ZLTextExplicitlyDecoratedStyle) {
				if (((ZLTextExplicitlyDecoratedStyle) p).myEntry.Depth != myEntry.Depth) {
					return p;
				}
			} else {
				if (++count > 1) {
					return p;
				}
			}
		}
		return p;
	}

	private ZLTextStyle getTreeParent()
	{
		if (myTreeParent == null) {
			myTreeParent = computeTreeParent();
		}
		return myTreeParent;
	}

	@Override
	protected int getFontSizeInternal(ZLTextMetrics metrics)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSFontSizeOption.getValue()) {
			return Parent.getFontSize(metrics);
		}

		final int baseFontSize = getTreeParent().getFontSize(metrics);
		if (myEntry.isFeatureSupported(FONT_STYLE_MODIFIER)) {
			if (myEntry.getFontModifier(FONT_MODIFIER_INHERIT) == ZLBoolean3.B3_TRUE) {
				return baseFontSize;// 直接继承父字体大小
			}
			if (myEntry.getFontModifier(FONT_MODIFIER_LARGER) == ZLBoolean3.B3_TRUE) {
				return baseFontSize * 120 / 100;// 基准字体大小->放大1.2倍
			}
			if (myEntry.getFontModifier(FONT_MODIFIER_SMALLER) == ZLBoolean3.B3_TRUE) {
				return baseFontSize * 100 / 120;// 基准字体大小->缩小1.2倍
			}
		}
		if (myEntry.isFeatureSupported(LENGTH_FONT_SIZE)) {
			return myEntry.getLength(LENGTH_FONT_SIZE, metrics, baseFontSize);
		}
		return Parent.getFontSize(metrics);
	}

	@Override
	protected boolean isBoldInternal()
	{
		switch (myEntry.getFontModifier(FONT_MODIFIER_BOLD)) {
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
		switch (myEntry.getFontModifier(FONT_MODIFIER_ITALIC)) {
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
		switch (myEntry.getFontModifier(FONT_MODIFIER_UNDERLINED)) {
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
		switch (myEntry.getFontModifier(FONT_MODIFIER_STRIKEDTHROUGH)) {
		case B3_TRUE:
			return true;
		case B3_FALSE:
			return false;
		default:
			return Parent.isStrikeThrough();
		}
	}

	@Override
	public int getLeftMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getLeftMargin(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_MARGIN_LEFT)) {
			return Parent.getLeftMargin(metrics);
		}
		return getTreeParent().getLeftMargin(metrics) + myEntry.getLength(LENGTH_MARGIN_LEFT, metrics, fontSize);
	}

	@Override
	public int getRightMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getRightMargin(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_MARGIN_RIGHT)) {
			return Parent.getRightMargin(metrics);
		}
		return getTreeParent().getRightMargin(metrics) + myEntry.getLength(LENGTH_MARGIN_RIGHT, metrics, fontSize);
	}

	@Override
	public int getLeftPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getLeftPadding(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_PADDING_LEFT)) {
			return Parent.getLeftPadding(metrics);
		}
		
		int value = myEntry.getLength(LENGTH_PADDING_LEFT, metrics, fontSize);
		if (containParent) {
			value += getTreeParent().getLeftPadding(metrics);
		}
		return value;
	}

	@Override
	public int getRightPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getRightPadding(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_PADDING_RIGHT)) {
			return Parent.getRightPadding(metrics);
		}
		
		int value = myEntry.getLength(LENGTH_PADDING_RIGHT, metrics, fontSize);
		if (containParent) {
			value += getTreeParent().getRightPadding(metrics);
		}
		return value;
	}

	@Override
	protected int getFirstLineIndentInternal(ZLTextMetrics metrics, int fontSize)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getFirstLineIndent(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_FIRST_LINE_INDENT)) {
			return Parent.getFirstLineIndent(metrics);
		}
		return myEntry.getLength(LENGTH_FIRST_LINE_INDENT, metrics, fontSize);
	}

	@Override
	protected int getLineSpacePercentInternal()
	{
		// TODO: implement
		return Parent.getLineSpacePercent();
	}

	@Override
	protected int getVerticalAlignInternal(ZLTextMetrics metrics, int fontSize)
	{
		// TODO: implement
		if (myEntry.isFeatureSupported(LENGTH_VERTICAL_ALIGN)) {
			return myEntry.getLength(LENGTH_VERTICAL_ALIGN, metrics, fontSize);
		} else if (myEntry.isFeatureSupported(NON_LENGTH_VERTICAL_ALIGN)) {
			switch (myEntry.getVerticalAlignCode()) {
			default:
				return Parent.getVerticalAlign(metrics);
			case 0: // sub
				return ZLTextStyleEntry.compute(
						new ZLTextStyleEntry.Length((short) -50, ZLTextStyleEntry.SizeUnit.EM_100),
						metrics, fontSize, LENGTH_VERTICAL_ALIGN
						);
			case 1: // super
				return ZLTextStyleEntry.compute(
						new ZLTextStyleEntry.Length((short) 50, ZLTextStyleEntry.SizeUnit.EM_100),
						metrics, fontSize, LENGTH_VERTICAL_ALIGN
						);
				/*
				 * case 2: // top return 0; case 3: // text-top return 0; case
				 * 4: // middle return 0; case 5: // bottom return 0; case 6: //
				 * text-bottom return 0; case 7: // initial return 0; case 8: //
				 * inherit return 0;
				 */
			}
		} else {
			return Parent.getVerticalAlign(metrics);
		}
	}

//	@Override
//	protected int getSpaceBeforeInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
//	{
//		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
//			return Parent.getSpaceBefore(metrics);
//		}
//
//		if (!myEntry.isFeatureSupported(LENGTH_SPACE_BEFORE)) {
//			return Parent.getSpaceBefore(metrics);
//		}
//		
//		int value = myEntry.getLength(LENGTH_SPACE_BEFORE, metrics, fontSize);
//		if (containParent) {
//			value += getTreeParent().getSpaceBefore(metrics);
//		}
//		
//		return value;
//	}
//
//	@Override
//	protected int getSpaceAfterInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
//	{
//		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
//			return Parent.getSpaceAfter(metrics);
//		}
//
//		if (!myEntry.isFeatureSupported(LENGTH_SPACE_AFTER)) {
//			return Parent.getSpaceAfter(metrics);
//		}
//		
//		int value = myEntry.getLength(LENGTH_SPACE_AFTER, metrics, fontSize);
//		if (containParent) {
//			value += getTreeParent().getSpaceAfter(metrics);
//		}
//		return value;
//	}

	public byte getAlignment()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSTextAlignmentOption.getValue()) {
			return Parent.getAlignment();
		}
		return myEntry.isFeatureSupported(ALIGNMENT_TYPE)
				? myEntry.getAlignmentType()
				: Parent.getAlignment();
	}

	public boolean allowHyphenations()
	{
		// TODO: implement
		return Parent.allowHyphenations();
	}

	// add by yq
	@Override
	protected int getTopMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getTopMargin(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_MARGIN_TOP)) {
			return Parent.getTopMargin(metrics);
		}
		return getTreeParent().getTopMargin(metrics) + myEntry.getLength(LENGTH_MARGIN_TOP, metrics, fontSize);
	}

	@Override
	protected int getBottomMarginInternal(ZLTextMetrics metrics, int fontSize)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getBottomMargin(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_MARGIN_BOTTOM)) {
			return Parent.getBottomMargin(metrics);
		}
		return getTreeParent().getBottomMargin(metrics) + myEntry.getLength(LENGTH_MARGIN_BOTTOM, metrics, fontSize);
	}

	@Override
	protected int getTopPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getTopPadding(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_PADDING_TOP)) {
			return Parent.getTopPadding(metrics);
		}
		
		int value = myEntry.getLength(LENGTH_PADDING_TOP, metrics, fontSize);
		if (containParent) {
			value += getTreeParent().getTopPadding(metrics);
		}
		return value;
	}

	@Override
	protected int getBottomPaddingInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getBottomPadding(metrics);
		}

		if (!myEntry.isFeatureSupported(LENGTH_PADDING_BOTTOM)) {
			return Parent.getBottomPadding(metrics);
		}
		
		int value = myEntry.getLength(LENGTH_PADDING_BOTTOM, metrics, fontSize);
		if (containParent) {
			value += getTreeParent().getBottomPadding(metrics);
		}
		return value;
	}
	
	@Override
	public boolean isTextColorSupported()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return false;
		}

		return myEntry.isFeatureSupported(TEXT_COLOR);
	}

	@Override
	public ZLColor getTextColor()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return new ZLColor(0);
		}

		return myEntry.getTextColor();
	}

	@Override
	public boolean isBgColorSupported()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return false;
		}

		return myEntry.isFeatureSupported(BG_COLOR);
	}

	@Override
	public ZLColor getBackgroundColor()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return new ZLColor(0);
		}

		return myEntry.getBackgroundColor();
	}

	@Override
	protected int getLeftBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getLeftBorder(metrics);
		}

		if (!myEntry.isBorderSupported(BORDER_LEFT)) {
			return Parent.getLeftBorder(metrics);
		}
		
		int value = 0;
		if (!myEntry.isFeatureSupported(LENGTH_BORDER_LEFT)) {
			value = ZLTextStyleEntry.compute(new ZLTextStyleEntry.Length((short)1, SizeUnit.PIXEL), metrics, fontSize, LENGTH_BORDER_LEFT);
		} else {
			value = myEntry.getLength(LENGTH_BORDER_LEFT, metrics, fontSize);
		}
		
		if (containParent) {
			value += getTreeParent().getLeftBorder(metrics);
		}
		
		return value;
	}

	@Override
	protected int getRightBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getRightBorder(metrics);
		}

		if (!myEntry.isBorderSupported(BORDER_RIGHT)) {
			return Parent.getRightBorder(metrics);
		}
		
		int value = 0;
		if (!myEntry.isFeatureSupported(LENGTH_BORDER_RIGHT)) {
			value = ZLTextStyleEntry.compute(new ZLTextStyleEntry.Length((short)1, SizeUnit.PIXEL), metrics, fontSize, LENGTH_BORDER_RIGHT);
		} else {
			value = myEntry.getLength(LENGTH_BORDER_RIGHT, metrics, fontSize);
		}
		
		if (containParent) {
			value += getTreeParent().getRightBorder(metrics);
		}
		
		return value;
	}

	@Override
	protected int getTopBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getTopBorder(metrics);
		}

		if (!myEntry.isBorderSupported(BORDER_TOP)) {
			return Parent.getTopBorder(metrics);
		}
		
		int value = 0;
		if (!myEntry.isFeatureSupported(LENGTH_BORDER_TOP)) {
			value = ZLTextStyleEntry.compute(new ZLTextStyleEntry.Length((short)1, SizeUnit.PIXEL), metrics, fontSize, LENGTH_BORDER_TOP);
		} else {
			value = myEntry.getLength(LENGTH_BORDER_TOP, metrics, fontSize);
		}
		
		if (containParent) {
			value += getTreeParent().getTopBorder(metrics);
		}
		
		return value;
	}

	@Override
	protected int getBottomBorderInternal(ZLTextMetrics metrics, int fontSize, boolean containParent)
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return Parent.getBottomBorder(metrics);
		}

		if (!myEntry.isBorderSupported(BORDER_BOTTOM)) {
			return Parent.getBottomBorder(metrics);
		}
		
		int value = 0;
		if (!myEntry.isFeatureSupported(LENGTH_BORDER_BOTTOM)) {
			value = ZLTextStyleEntry.compute(new ZLTextStyleEntry.Length((short)1, SizeUnit.PIXEL), metrics, fontSize, LENGTH_BORDER_BOTTOM);
		} else {
			value = myEntry.getLength(LENGTH_BORDER_BOTTOM, metrics, fontSize);
		}
		
		if (containParent) {
			value += getTreeParent().getBottomBorder(metrics);
		}
		
		return value;
	}

	@Override
	public ZLColor getTopBorderColor()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return new ZLColor(0);
		}

		return myEntry.getBorderColor(BORDER_TOP);
	}

	@Override
	public ZLColor getRightBorderColor()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return new ZLColor(0);
		}

		return myEntry.getBorderColor(BORDER_RIGHT);
	}

	@Override
	public ZLColor getBottomBorderColor()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return new ZLColor(0);
		}

		return myEntry.getBorderColor(BORDER_BOTTOM);
	}

	@Override
	public ZLColor getLeftBorderColor()
	{
		if (myEntry instanceof ZLTextCSSStyleEntry && !BaseStyle.UseCSSMarginsOption.getValue()) {
			return new ZLColor(0);
		}

		return myEntry.getBorderColor(BORDER_LEFT);
	}

	// end by yq
}
