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

public interface ZLTextParagraph
{
	interface Entry
	{// 书籍实体内容类型
		byte	TEXT				= 1;	// 文本
		byte	IMAGE				= 2;	// 图像
		byte	CONTROL				= 3;	// ?
		byte	HYPERLINK_CONTROL	= 4;	// 超链接 ?
		byte	STYLE_CSS			= 5;	// CSS样式
		byte	STYLE_OTHER			= 6;	// 非CSS样式
		byte	STYLE_CLOSE			= 7;	// ?
		byte	FIXED_HSPACE		= 8;	// ?
		byte	RESET_BIDI			= 9;	// ?
		byte	AUDIO				= 10;	// 音频
		byte	VIDEO				= 11;	// 视频
		byte	BACKGROUND			= 12;
		// add by yq
		byte	SPECIAL_TAG			= 13;
		// end by yq
	}

	/** 书籍实体内容迭代器 */
	interface EntryIterator
	{
		byte getType();

		char[] getTextData();

		int getTextOffset();

		int getTextLength();

		byte getControlKind();

		boolean getControlIsStart();

		byte getHyperlinkType();

		String getHyperlinkId();

		ZLImageEntry getImageEntry();

		ZLVideoEntry getVideoEntry();

		ZLTextStyleEntry getStyleEntry();

		// add by zdc
		ZLTextBackgroundEntry getBackgroundEntry();

		// end by zdc

		short getFixedHSpaceLength();

		boolean next();
	}

	public EntryIterator iterator();

	/** 段落的类型 */
	interface Kind
	{
		byte	TEXT_PARAGRAPH				= 0;	// 带内容的段落
		// byte TREE_PARAGRAPH = 1;
		byte	EMPTY_LINE_PARAGRAPH		= 2;	// 该段落无数据，但在界面上要显示一空行，单独作为一个段落
		byte	BEFORE_SKIP_PARAGRAPH		= 3;
		byte	AFTER_SKIP_PARAGRAPH		= 4;
		byte	END_OF_SECTION_PARAGRAPH	= 5;
		byte	END_OF_TEXT_PARAGRAPH		= 6;
		byte	ENCRYPTED_SECTION_PARAGRAPH	= 7;	// 加密的段落?
	};

	byte getKind();
}
