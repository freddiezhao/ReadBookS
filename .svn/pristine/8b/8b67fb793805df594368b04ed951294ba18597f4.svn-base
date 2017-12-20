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

/**
 * “行”信息类
 * 
 * @author chenjl
 * 
 */
final class ZLTextLineInfo
{
	/**
	 * 该行所在段落游标信息
	 */
	final ZLTextParagraphCursor	ParagraphCursor;
	final int					ParagraphCursorLength;	// 段落的长度

	final int					StartElementIndex;		// 起始元素索引
	final int					StartCharIndex;			// 结束元素索引
	int							RealStartElementIndex;	// 真正开始绘制的起始元素索引(行前面可能会有ZLTextStyleElement、ZLTextControlElement)
	int							RealStartCharIndex;		// 真正开始绘制的起始字符索引(即从元素(如某个单词)中哪个索引开始)
	int							EndElementIndex;		// 结束元素索引
	int							EndCharIndex;			// 结束字符索引

	boolean						IsVisible;				// 是否可见
	int							LeftIndent;				// 左缩进值
	int							Width;					// 该行占据的宽度
	int							Height;					// 该行占据的高度
	int							Descent;
	int							VSpaceBefore;			// 行顶部垂直向的空白区域高度？
	int							VSpaceAfter;			// 行底部垂直向的空白区域高度？
	// add by cjl
	int							VSpaceBorderAfter;
	// end by cjl
	boolean						PreviousInfoUsed;		// 是否使用了上一行？
	int							SpaceCounter;			// 该行的空格数
	ZLTextStyle					StartStyle;

	ZLTextLineInfo(ZLTextParagraphCursor paragraphCursor, int elementIndex, int charIndex, ZLTextStyle style)
	{
		ParagraphCursor = paragraphCursor;// 行所在的段落
		ParagraphCursorLength = paragraphCursor.getParagraphLength();// 段落元素数量=段落长度

		StartElementIndex = elementIndex;
		StartCharIndex = charIndex;
		RealStartElementIndex = elementIndex;
		RealStartCharIndex = charIndex;
		EndElementIndex = elementIndex;
		EndCharIndex = charIndex;

		StartStyle = style;
	}

	boolean isEndOfParagraph()
	{
		return EndElementIndex == ParagraphCursorLength;
	}

	void adjust(ZLTextLineInfo previous)
	{
		if (!PreviousInfoUsed && previous != null) {
			Height -= Math.min(previous.VSpaceAfter, VSpaceBefore);
			PreviousInfoUsed = true;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		ZLTextLineInfo info = (ZLTextLineInfo) o;
		return (ParagraphCursor == info.ParagraphCursor) && (StartElementIndex == info.StartElementIndex) && (StartCharIndex == info.StartCharIndex);
	}

	@Override
	public int hashCode()
	{
		return ParagraphCursor.hashCode() + StartElementIndex + 239 * StartCharIndex;
	}
}
