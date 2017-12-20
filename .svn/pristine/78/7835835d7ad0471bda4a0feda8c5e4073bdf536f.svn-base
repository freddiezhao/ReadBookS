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

import org.geometerplus.zlibrary.text.model.ZLTextMark;
import org.geometerplus.zlibrary.text.model.ZLTextModel;

/**
 * “字”有效信息
 * 
 * @author chenjl
 * 
 */
public final class ZLTextWordCursor extends ZLTextPosition
{
	/**
	 * “段”
	 */
	private ZLTextParagraphCursor	myParagraphCursor;
	/**
	 * 元素索引(某个单词)
	 */
	private int						myElementIndex;
	/**
	 * 字符索引(某个字母)<br>
	 * (如果此元素是单字符，那么元素索引和字符索引值一致，如果元素是<br>
	 * 一个如单词等由多字符组成的元素，则字符索引为该字符在元素内的索引值)
	 */
	private int						myCharIndex;

	// private int myModelIndex;
	
	public void clear(){
		myParagraphCursor = null;
	}

	public ZLTextWordCursor()
	{
	}

	public ZLTextWordCursor(ZLTextWordCursor cursor)
	{
		setCursor(cursor);
	}

	public void setCursor(ZLTextWordCursor cursor)
	{
		myParagraphCursor = cursor.myParagraphCursor;
		myElementIndex = cursor.myElementIndex;
		myCharIndex = cursor.myCharIndex;
	}

	public ZLTextWordCursor(ZLTextParagraphCursor paragraphCursor)
	{
		setCursor(paragraphCursor);
	}

	public void setCursor(ZLTextParagraphCursor paragraphCursor)
	{
		myParagraphCursor = paragraphCursor;
		myElementIndex = 0;
		myCharIndex = 0;
	}

	/**
	 * 如果依附的段落光标信息对象为空，则返回true
	 * 
	 * @return
	 */
	public boolean isNull()
	{
		return myParagraphCursor == null;
	}

	/**
	 * 是否是段落的首"字符"
	 * 
	 * @return
	 */
	public boolean isStartOfParagraph()
	{
		return myElementIndex == 0 && myCharIndex == 0;
	}

	/**
	 * 是否是第一段的首"字符"
	 * 
	 * @return
	 */
	public boolean isStartOfText()
	{
		return isStartOfParagraph() && myParagraphCursor.isFirst();
	}

	/**
	 * 是否是段落的尾"字(元素)"
	 * 
	 * @return
	 */
	public boolean isEndOfParagraph()
	{
		return myParagraphCursor != null && myElementIndex == myParagraphCursor.getParagraphLength();
	}

	/**
	 * 是否是最后一段的尾"字(元素)"
	 * 
	 * @return
	 */
	public boolean isEndOfText()
	{
		return isEndOfParagraph() && myParagraphCursor.isLast();
	}

	/**
	 * 获取段落索引
	 */
	@Override
	public int getParagraphIndex()
	{
		return myParagraphCursor != null ? myParagraphCursor.Index : 0;
	}

	/**
	 * 获取元素索引
	 */
	@Override
	public int getElementIndex()
	{
		return myElementIndex;
	}

	/**
	 * 获取字符索引
	 */
	@Override
	public int getCharIndex()
	{
		return myCharIndex;
	}

	/**
	 * 获取元素本身
	 * 
	 * @return
	 */
	public ZLTextElement getElement()
	{
		return myParagraphCursor.getElement(myElementIndex);
	}

	/**
	 * 获取“段”
	 * 
	 * @return
	 */
	public ZLTextParagraphCursor getParagraphCursor()
	{
		return myParagraphCursor;
	}

	public ZLTextMark getMark()
	{
		if (myParagraphCursor == null) {
			return null;
		}
		final ZLTextParagraphCursor paragraph = myParagraphCursor;
		int paragraphLength = paragraph.getParagraphLength();
		int wordIndex = myElementIndex;
		while ((wordIndex < paragraphLength) && (!(paragraph.getElement(wordIndex) instanceof ZLTextWord))) {
			wordIndex++;
		}
		if (wordIndex < paragraphLength) {
			return new ZLTextMark(paragraph.Index, ((ZLTextWord) paragraph.getElement(wordIndex)).getParagraphOffset(), 0);
		}
		return new ZLTextMark(paragraph.Index + 1, 0, 0);
	}

	/**
	 * 下一个“词”，将myElementIndex值+1
	 */
	public void nextWord()
	{
		myElementIndex++;
		myCharIndex = 0;
	}

	/**
	 * 上一个“词”，将myElementIndex值-1
	 */
	public void previousWord()
	{
		myElementIndex--;
		myCharIndex = 0;
	}

	/**
	 * 跳转到下一个“段”段首
	 * 
	 * @return
	 */
	public boolean nextParagraph()
	{
		if (!isNull()) {
			if (!myParagraphCursor.isLast()) {
				myParagraphCursor = myParagraphCursor.next();
				moveToParagraphStart();
				return true;
			}
		}
		return false;
	}

	/**
	 * 跳转到上一个“段”段首
	 * 
	 * @return
	 */
	public boolean previousParagraph()
	{
		if (!isNull()) {
			if (!myParagraphCursor.isFirst()) {
				myParagraphCursor = myParagraphCursor.previous();
				moveToParagraphStart();
				return true;
			}
		}
		return false;
	}

	/**
	 * 移动到“段”首
	 */
	public void moveToParagraphStart()
	{
		if (!isNull()) {
			myElementIndex = 0;
			myCharIndex = 0;
		}
	}

	/**
	 * 移动到“段”尾
	 */
	public void moveToParagraphEnd()
	{
		if (!isNull()) {
			myElementIndex = myParagraphCursor.getParagraphLength();
			myCharIndex = 0;
		}
	}

	/**
	 * 移动到指定“段”
	 * 
	 * @param paragraphIndex
	 *            段落索引
	 */
	public void moveToParagraph(int paragraphIndex)
	{
		if (!isNull() && (paragraphIndex != myParagraphCursor.Index)) {
			final ZLTextModel model = myParagraphCursor.Model;
			paragraphIndex = Math.max(0, Math.min(paragraphIndex, model.getParagraphsNumber() - 1));
			myParagraphCursor = ZLTextParagraphCursor.cursor(model, paragraphIndex);
			moveToParagraphStart();
		}
	}

	/**
	 * 移动到指定位置
	 * 
	 * @param position
	 */
	public void moveTo(ZLTextPosition position)
	{
		moveToParagraph(position.getParagraphIndex());
		moveTo(position.getElementIndex(), position.getCharIndex());
	}

	/**
	 * 移动到指定位置
	 * 
	 * @param wordIndex
	 * @param charIndex
	 */
	public void moveTo(int wordIndex, int charIndex)
	{
		if (!isNull()) {
			if (wordIndex == 0 && charIndex == 0) {
				myElementIndex = 0;
				myCharIndex = 0;
			} else {
				wordIndex = Math.max(0, wordIndex);
				int size = myParagraphCursor.getParagraphLength();
				// 设置的这个值比段落的总元素数还大
				if (wordIndex > size) {
					// 设置为段尾最后一个元素的索引值
					myElementIndex = size;// 为啥不减1？
					myCharIndex = 0;
				} else {
					myElementIndex = wordIndex;
					setCharIndex(charIndex);
				}
			}
		}
	}

	/**
	 * 设置字符索引
	 * 
	 * @param charIndex
	 */
	public void setCharIndex(int charIndex)
	{
		charIndex = Math.max(0, charIndex);
		myCharIndex = 0;
		if (charIndex > 0) {
			// 根据当前的元素索引，获取该元素
			ZLTextElement element = myParagraphCursor.getElement(myElementIndex);
			// 如果该元素是一个“Word”(字符)
			if (element instanceof ZLTextWord) {
				// 当charIndex小于字符总长度时
				if (charIndex <= ((ZLTextWord) element).Length) {
					myCharIndex = charIndex;
				}
			}
		}
	}

	/**
	 * 重置
	 */
	public void reset()
	{
		myParagraphCursor = null;
		myElementIndex = 0;
		myCharIndex = 0;
	}

	/**
	 * 重新填充“段”落
	 */
	public void rebuild()
	{
		if (!isNull()) {
			myParagraphCursor.clear();
			myParagraphCursor.fill();
			moveTo(myElementIndex, myCharIndex);
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + " (" + myParagraphCursor + "," + myElementIndex + "," + myCharIndex + ")";
	}
}
