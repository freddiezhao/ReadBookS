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

package org.geometerplus.zlibrary.text.model;

import java.util.List;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.fonts.FontManager;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.util.ZLColor;

public abstract class ZLTextStyleEntry
{
	public interface Feature
	{
		int	LENGTH_PADDING_LEFT			= 0;
		int	LENGTH_PADDING_RIGHT		= 1;
		int	LENGTH_MARGIN_LEFT			= 2;
		int	LENGTH_MARGIN_RIGHT			= 3;
		int	LENGTH_FIRST_LINE_INDENT	= 4;
		int	LENGTH_SPACE_BEFORE			= 5;
		int	LENGTH_SPACE_AFTER			= 6;
		int	LENGTH_FONT_SIZE			= 7;
		int	LENGTH_VERTICAL_ALIGN		= 8;
		// add by yq
		int LENGTH_PADDING_TOP 			= 9;
		int LENGTH_PADDING_BOTTOM 		= 10;
		int LENGTH_MARGIN_TOP 			= 11;
		int LENGTH_MARGIN_BOTTOM 		= 12;
		int LENGTH_BORDER_TOP 			= 13;
		int LENGTH_BORDER_RIGHT 		= 14;
		int LENGTH_BORDER_BOTTOM 		= 15;
		int LENGTH_BORDER_LEFT 			= 16;
		// end by yq
		int	NUMBER_OF_LENGTHS			= 17;
		int	ALIGNMENT_TYPE				= NUMBER_OF_LENGTHS;
		int	FONT_FAMILY					= NUMBER_OF_LENGTHS + 1;
		int	FONT_STYLE_MODIFIER			= NUMBER_OF_LENGTHS + 2;
		int	NON_LENGTH_VERTICAL_ALIGN	= NUMBER_OF_LENGTHS + 3;
		// add by yq
		int	TEXT_COLOR					= NUMBER_OF_LENGTHS + 4;
		int	BG_COLOR					= NUMBER_OF_LENGTHS + 5;
		// end by yq
	}

	public interface FontModifier
	{
		byte	FONT_MODIFIER_BOLD				= 1 << 0;			// 粗体
		byte	FONT_MODIFIER_ITALIC			= 1 << 1;			// 斜体
		byte	FONT_MODIFIER_UNDERLINED		= 1 << 2;			// 下划线
		byte	FONT_MODIFIER_STRIKEDTHROUGH	= 1 << 3;			// 中间线
		byte	FONT_MODIFIER_SMALLCAPS			= 1 << 4;			// 小帽子?🎩
		byte	FONT_MODIFIER_INHERIT			= 1 << 5;			// 继承?
		byte	FONT_MODIFIER_SMALLER			= 1 << 6;			// 小字体?
		byte	FONT_MODIFIER_LARGER			= (byte) (1 << 7);	// 大字体?
	}

	public interface SizeUnit
	{
		byte	PIXEL	= 0;
		byte	POINT	= 1;
		byte	EM_100	= 2;
		byte	REM_100	= 3;
		byte	EX_100	= 4;
		byte	PERCENT	= 5;
		// TODO: add IN, CM, MM, PICA ("pc", = 12 POINT)
	}
	
	// add by yq
	public interface BorderFeature {
		int BORDER_TOP 		= 0;
		int BORDER_RIGHT		= 1;
		int BORDER_BOTTOM		= 2;
		int BORDER_LEFT		= 3;
		int NUMBER_OF_BORDERS	= 4;
		int BORDER_ALL			= NUMBER_OF_BORDERS;
	}
	
	public interface BorderStyle {
		byte BORDER_STYLE_NONE		= 0;
		byte BORDER_STYLE_HIDDEN	= 1;
		byte BORDER_STYLE_DOTTED	= 2;
		byte BORDER_STYLE_DASHED	= 3;
		byte BORDER_STYLE_SOLID		= 4;
		byte BORDER_STYLE_DOUBLE	= 5;
		byte BORDER_STYLE_GROOVE	= 6;
		byte BORDER_STYLE_RIDGE		= 7;
		byte BORDER_STYLE_INSET		= 8;
		byte BORDER_STYLE_OUTSET	= 9;
		byte BORDER_STYLE_INHERIT 	= 10;
	}
	
	public static class BorderType {
		public byte		Style;
		public ZLColor 	Color;
		
		public BorderType(byte style, ZLColor color)
		{
			Style = style;
			Color = color;
		}

		@Override
		public String toString()
		{
			return Style + "_" + Color.toString();
		}
	};
	// end by yq

	public static class Length
	{
		/** 代表长度值 */
		public final short	Size;
		/** 代表长度单位，如em、px等 */
		public final byte	Unit;

		public Length(short size, byte unit)
		{
			Size = size;
			Unit = unit;
		}

		@Override
		public String toString()
		{
			return Size + "." + Unit;
		}
	}

	public final short		Depth;
//	private short			myFeatureMask;
	// add by yq
	private int				myFeatureMask;
	// end by yq
	
	private Length[]		myLengths	= new Length[Feature.NUMBER_OF_LENGTHS];
	private byte			myAlignmentType;
	private List<FontEntry>	myFontEntries;
	private byte			mySupportedFontModifiers;
	private byte			myFontModifiers;
	private byte			myVerticalAlignCode;

	// add by yq
	private ZLColor			myTextColor;
	private ZLColor			myBackgroundColor;
	private BorderType[]	myBorders	= {
		new BorderType(BorderStyle.BORDER_STYLE_NONE, new ZLColor(0)),
		new BorderType(BorderStyle.BORDER_STYLE_NONE, new ZLColor(0)),
		new BorderType(BorderStyle.BORDER_STYLE_NONE, new ZLColor(0)),
		new BorderType(BorderStyle.BORDER_STYLE_NONE, new ZLColor(0)),
	};
	// end by yq

//	static boolean isFeatureSupported(short mask, int featureId)
	// add by yq
	static boolean isFeatureSupported(int mask, int featureId)
	// end by yq
	{
		return (mask & (1 << featureId)) != 0;
	}

	protected ZLTextStyleEntry(short depth)
	{
		Depth = depth;
	}

	public final boolean isFeatureSupported(int featureId)
	{
		return isFeatureSupported(myFeatureMask, featureId);
	}

	final void setLength(int featureId, short size, byte unit)
	{
		myFeatureMask |= 1 << featureId;
		myLengths[featureId] = new Length(size, unit);
	}

	private static int fullSize(ZLTextMetrics metrics, int fontSize, int featureId)
	{
		switch (featureId) {
		default:
		case Feature.LENGTH_MARGIN_LEFT:
		case Feature.LENGTH_MARGIN_RIGHT:
		case Feature.LENGTH_PADDING_LEFT:
		case Feature.LENGTH_PADDING_RIGHT:
		case Feature.LENGTH_FIRST_LINE_INDENT:
			// add by yq
		case Feature.LENGTH_BORDER_LEFT:
		case Feature.LENGTH_BORDER_RIGHT:
			// end by yq
			return metrics.FullWidth;
		case Feature.LENGTH_SPACE_BEFORE:
		case Feature.LENGTH_SPACE_AFTER:
			// add by yq
		case Feature.LENGTH_MARGIN_TOP:
		case Feature.LENGTH_MARGIN_BOTTOM:
		case Feature.LENGTH_PADDING_TOP:
		case Feature.LENGTH_PADDING_BOTTOM:
		case Feature.LENGTH_BORDER_TOP:
		case Feature.LENGTH_BORDER_BOTTOM:
			// end by yq
			return metrics.FullHeight;
		case Feature.LENGTH_VERTICAL_ALIGN:
		case Feature.LENGTH_FONT_SIZE:
			return fontSize;
		}
	}

	public final int getLength(int featureId, ZLTextMetrics metrics, int fontSize)
	{
		return compute(myLengths[featureId], metrics, fontSize, featureId);
	}

	public static int compute(Length length, ZLTextMetrics metrics, int baseFontSize, int featureId)
	{
		switch (length.Unit) {
		default:
		case SizeUnit.PIXEL:
			// 像素不能直接应用，也要进行换算，以12px为基准单位，算出style样式中字体大小的倍率
			// 如，<span style="font-size: 20px;"><strong>自序</strong></span>
			// 则length.Size=20，baseFontSize按照density为2.0算的话，初始默认大小为20*2.0=40。
			return (int) ((length.Size / 12.0f) * baseFontSize + 0.5);
		case SizeUnit.POINT:
			// pt和px的换算公式: pt = px * 3/4
			// 安卓平台涉及多分辨率，因此要再次乘以(metrics.DPI/基准值160)
			// return length.Size * metrics.DPI / 72;
			return (int) (length.Size * 4.0f / 3.0f * (metrics.DPI / 160.0f));
		case SizeUnit.EM_100:// 比如样式为text-indent:6em;则这个length的Size为6 * 100
			return (length.Size * baseFontSize + 50) / 100;
		case SizeUnit.REM_100:
			return (length.Size * metrics.FontSize + 50) / 100;
		case SizeUnit.EX_100:
			// TODO 0.5 font size => height of x
			return (length.Size * baseFontSize / 2 + 50) / 100;
		case SizeUnit.PERCENT:
			return (length.Size * fullSize(metrics, baseFontSize, featureId) + 50) / 100;
		}
	}

	final void setAlignmentType(byte alignmentType)
	{
		myFeatureMask |= 1 << Feature.ALIGNMENT_TYPE;
		myAlignmentType = alignmentType;
	}

	public final byte getAlignmentType()
	{
		return myAlignmentType;
	}

	final void setFontFamilies(FontManager fontManager, int fontFamiliesIndex)
	{
		myFeatureMask |= 1 << Feature.FONT_FAMILY;
		myFontEntries = fontManager.getFamilyEntries(fontFamiliesIndex);
	}

	public final List<FontEntry> getFontEntries()
	{
		return myFontEntries;
	}

	final void setFontModifiers(byte supported, byte values)
	{
		myFeatureMask |= 1 << Feature.FONT_STYLE_MODIFIER;
		mySupportedFontModifiers = supported;
		myFontModifiers = values;
	}

	public final void setFontModifier(byte modifier, boolean on)
	{
		myFeatureMask |= 1 << Feature.FONT_STYLE_MODIFIER;
		mySupportedFontModifiers |= modifier;
		if (on) {
			myFontModifiers |= modifier;
		} else {
			myFontModifiers &= ~modifier;
		}
	}

	public final ZLBoolean3 getFontModifier(byte modifier)
	{
		if ((mySupportedFontModifiers & modifier) == 0) {
			return ZLBoolean3.B3_UNDEFINED;
		}
		return (myFontModifiers & modifier) == 0 ? ZLBoolean3.B3_FALSE : ZLBoolean3.B3_TRUE;
	}

	public final void setVerticalAlignCode(byte code)
	{
		myFeatureMask |= 1 << Feature.NON_LENGTH_VERTICAL_ALIGN;
		myVerticalAlignCode = code;
	}

	public final byte getVerticalAlignCode()
	{
		return myVerticalAlignCode;
	}

	// add by yq
	public final ZLColor getTextColor()
	{
		return myTextColor;
	}

	public final void setTextColor(ZLColor textColor)
	{
		myFeatureMask |= 1 << Feature.TEXT_COLOR;
		myTextColor = textColor;
	}

	public final ZLColor getBackgroundColor()
	{
		return myBackgroundColor;
	}

	public final void setBackgroundColor(ZLColor bgColor)
	{
		myFeatureMask |= 1 << Feature.BG_COLOR;
		myBackgroundColor = bgColor;
	}
	
	public final boolean anyBorderSupported()
	{
		return (myBorders[0].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[0].Style != BorderStyle.BORDER_STYLE_HIDDEN)
						|| (myBorders[1].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[1].Style != BorderStyle.BORDER_STYLE_HIDDEN)
						|| (myBorders[2].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[2].Style != BorderStyle.BORDER_STYLE_HIDDEN)
						|| (myBorders[3].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[3].Style != BorderStyle.BORDER_STYLE_HIDDEN);
	}
	public final boolean isBorderSupported(int featureId)
	{
		if (featureId == BorderFeature.BORDER_ALL) {
			return (myBorders[0].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[0].Style != BorderStyle.BORDER_STYLE_HIDDEN)
					&& (myBorders[1].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[1].Style != BorderStyle.BORDER_STYLE_HIDDEN)
					&& (myBorders[2].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[2].Style != BorderStyle.BORDER_STYLE_HIDDEN)
					&& (myBorders[3].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[3].Style != BorderStyle.BORDER_STYLE_HIDDEN);
		} else if (featureId >= BorderFeature.BORDER_TOP && featureId < BorderFeature.NUMBER_OF_BORDERS) {
			return (myBorders[featureId].Style != BorderStyle.BORDER_STYLE_NONE && myBorders[featureId].Style != BorderStyle.BORDER_STYLE_HIDDEN);
		} else {
			return false;
		}
	}
	final void setBorderType(byte style, int color, int featureId) {
		if (featureId >= BorderFeature.BORDER_TOP && featureId < BorderFeature.NUMBER_OF_BORDERS) {
			myBorders[featureId].Style = style;
			myBorders[featureId].Color = new ZLColor(color);
		}
	}
	public final ZLColor getBorderColor(int featureId)
	{
		if (featureId >= BorderFeature.BORDER_TOP && featureId < BorderFeature.NUMBER_OF_BORDERS) {
			return myBorders[featureId].Color;
		}
		return new ZLColor(0);
	}
	// end by yq

	@Override
	public String toString()
	{
		final StringBuilder buffer = new StringBuilder("StyleEntry[");
		buffer.append("features: ").append(myFeatureMask).append(";");
		// add by yq
		if (isFeatureSupported(Feature.LENGTH_PADDING_TOP)) {
			buffer.append("padding-top: ").append(myLengths[Feature.LENGTH_PADDING_TOP]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_PADDING_RIGHT)) {
			buffer.append("padding-right: ").append(myLengths[Feature.LENGTH_PADDING_RIGHT]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_PADDING_BOTTOM)) {
			buffer.append("padding-bottom: ").append(myLengths[Feature.LENGTH_PADDING_BOTTOM]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_PADDING_LEFT)) {
			buffer.append("padding-left: ").append(myLengths[Feature.LENGTH_PADDING_LEFT]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_MARGIN_TOP)) {
			buffer.append("margin-top: ").append(myLengths[Feature.LENGTH_MARGIN_TOP]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_MARGIN_RIGHT)) {
			buffer.append("margin-right: ").append(myLengths[Feature.LENGTH_MARGIN_RIGHT]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_MARGIN_BOTTOM)) {
			buffer.append("margin-bottom: ").append(myLengths[Feature.LENGTH_MARGIN_BOTTOM]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_MARGIN_LEFT)) {
			buffer.append("margin-left: ").append(myLengths[Feature.LENGTH_MARGIN_LEFT]).append(";");
		}
		// end by yq
		if (isFeatureSupported(Feature.LENGTH_SPACE_BEFORE)) {
			buffer.append("space-before: ").append(myLengths[Feature.LENGTH_SPACE_BEFORE]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_SPACE_AFTER)) {
			buffer.append("space-after: ").append(myLengths[Feature.LENGTH_SPACE_AFTER]).append(";");
		}
		if (anyBorderSupported()) {
			if (isBorderSupported(BorderFeature.BORDER_TOP)) {
				buffer.append("border-top: ").append(myLengths[Feature.LENGTH_BORDER_TOP]).append(";");
			}
			if (isBorderSupported(BorderFeature.BORDER_RIGHT)) {
				buffer.append("border-right: ").append(myLengths[Feature.LENGTH_BORDER_RIGHT]).append(";");
			}
			if (isBorderSupported(BorderFeature.BORDER_BOTTOM)) {
				buffer.append("border-bottom: ").append(myLengths[Feature.LENGTH_BORDER_BOTTOM]).append(";");
			}
			if (isBorderSupported(BorderFeature.BORDER_LEFT)) {
				buffer.append("border-left: ").append(myLengths[Feature.LENGTH_BORDER_LEFT]).append(";");
			}
		}
		if (isFeatureSupported(Feature.BG_COLOR)) {
			buffer.append("background-color: ").append(getBackgroundColor()).append(";");
		}
		if (isFeatureSupported(Feature.TEXT_COLOR)) {
			buffer.append("text-color: ").append(getTextColor()).append(";");
		}
		buffer.append("]");
		return buffer.toString();
	}
}
