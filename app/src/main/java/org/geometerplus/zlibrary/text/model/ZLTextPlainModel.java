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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.geometerplus.android.util.ZLog;
import org.geometerplus.zlibrary.core.fonts.FontManager;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.util.ZLSearchPattern;
import org.geometerplus.zlibrary.core.util.ZLSearchUtil;

public class ZLTextPlainModel implements ZLTextModel, ZLTextStyleEntry.Feature, ZLTextStyleEntry.BorderFeature
{
	private final String									myId;
	private final String									myLanguage;

	protected int[]											myStartEntryIndices;
	protected int[]											myStartEntryOffsets;
	protected int[]											myParagraphLengths;							// 每个段落的长度信息
	protected int[]											myTextSizes;
	protected byte[]										myParagraphKinds;								// 每个段落的Kind信息
	/** 总段落数 */
	protected int											myParagraphsNumber;

	protected final CharStorage								myStorage;
	protected final Map<String, ZLImage>					myImageMap;									// TODO
																											// CJL
																											// 存储书籍中的图片，存储结构可能与BookModelImpl中的myImageMap是一样的！待验证！

	private ArrayList<ZLTextMark>							myMarks;

	private final FontManager								myFontManager;

	// add by zdc
	/**
	 * Key：某个章节的起始段落索引(相对于整本书的段落索引)，Value：该章节对应的背景色信息
	 */
	private final Map<Integer, Vector<BackgroundItemInfo>>	myBackgroundEntries;
	private boolean											isProcessingBackgroundEntry;
	/**
	 * 每一个章节的起始段落索引<br>
	 * <br>
	 * 比如说某一个epub一共有3个章节(三个html)，<br>
	 * 第一个html一共有段落12段，第二个有11段，第三个有20段，<br>
	 * 则这个myStartParagraphIndex存储的值分别是[0，12，23]
	 */
	private final int[]										myStartParagraphIndex;
	private final Stack<Integer>							myBackgroundEntryStack	= new Stack<Integer>();

	public static class IntRange
	{
		public int	paragraphIndex; // 段落索引paragraphIndex
		public int	elementIndex;	// 段落中的elementIndex

		@Override
		public boolean equals(Object o)
		{
			if (o == this) {
				return true;
			}

			if (!(o instanceof IntRange)) {
				return false;
			}

			IntRange range = (IntRange) o;
			if (paragraphIndex == range.paragraphIndex && elementIndex == range.elementIndex) {
				return true;
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			String toString = toString();
			int h = 0, offset = 0;
			for (int i = 0; i < toString.length(); i++) {
				h = 31 * h + toString.charAt(offset++);
			}
			return h;
		}

		@Override
		public String toString()
		{
			return "[" + paragraphIndex + " : " + elementIndex + "]";
		}
	}

	public static class BackgroundItemInfo
	{
		/**
		 * 存储着背景数据的起始的段落索引paragraphIndex和elementIndex信息
		 */
		public IntRange	startRange			= new IntRange();
		/**
		 * 存储着背景数据的结束的段落索引paragraphIndex和elementIndex信息
		 */
		public IntRange	endRange			= new IntRange();
		/**
		 * 背景色
		 */
		public ZLColor	bgColor				= new ZLColor(0);

		public byte		kind				= ZLTextBackgroundEntry.BG_KIND_BACKGROUND;

		public ZLColor	leftBorderColor		= new ZLColor(0);
		public ZLColor	topBorderColor		= new ZLColor(0);
		public ZLColor	rightBorderColor	= new ZLColor(0);
		public ZLColor	bottomBorderColor	= new ZLColor(0);

		/**
		 * block块的意思。<br>
		 * isBlock表示是否是DIV，P标签，如果是则绘制一整行
		 */
		public boolean	isBlock;

		// public bool hasSet;

		@Override
		public boolean equals(Object o)
		{
			if (o == this) {
				return true;
			}

			if (!(o instanceof BackgroundItemInfo)) {
				return false;
			}

			BackgroundItemInfo c = (BackgroundItemInfo) o;
			if (c.startRange.equals(startRange) && c.endRange.equals(endRange)) {
				return true;
			}
			return super.equals(o);
		}

		@Override
		public int hashCode()
		{
			return startRange.hashCode() + endRange.hashCode();
		}

		@Override
		public String toString()
		{
			return startRange + ", " + endRange + ", Color:" + bgColor + ", isBlock:" + isBlock;
		}
	}

	// end by zdc

	final class EntryIteratorImpl implements ZLTextParagraph.EntryIterator
	{
		private int						myCounter;
		private int						myLength;				// 段落的长度
		private byte					myType;
		/**
		 * 文件名索引，如0，则对应的数据源路径为/storage/emulated/0/Android/data/com.sina.book/
		 * cache/0.ncache
		 */
		int								myDataIndex;
		int								myDataOffset;

		// TextEntry data
		private char[]					myTextData;
		private int						myTextOffset;
		private int						myTextLength;

		// ControlEntry data
		private byte					myControlKind;
		private boolean					myControlIsStart;
		// HyperlinkControlEntry data
		private byte					myHyperlinkType;
		private String					myHyperlinkId;

		// ImageEntry
		private ZLImageEntry			myImageEntry;

		// VideoEntry
		private ZLVideoEntry			myVideoEntry;

		// add by zdc
		// BackgroundEntry
		private ZLTextBackgroundEntry	myTextBackgroundEntry;
		// end by adc

		// StyleEntry
		private ZLTextStyleEntry		myStyleEntry;

		// FixedHSpaceEntry data
		private short					myFixedHSpaceLength;

		EntryIteratorImpl(int index)
		{
			reset(index);
		}

		void reset(int index)
		{
			myCounter = 0;
			myLength = myParagraphLengths[index];
			myDataIndex = myStartEntryIndices[index];
			myDataOffset = myStartEntryOffsets[index];
		}

		public byte getType()
		{
			return myType;
		}

		public char[] getTextData()
		{
			return myTextData;
		}

		public int getTextOffset()
		{
			return myTextOffset;
		}

		public int getTextLength()
		{
			return myTextLength;
		}

		public byte getControlKind()
		{
			return myControlKind;
		}

		public boolean getControlIsStart()
		{
			return myControlIsStart;
		}

		public byte getHyperlinkType()
		{
			return myHyperlinkType;
		}

		public String getHyperlinkId()
		{
			return myHyperlinkId;
		}

		public ZLImageEntry getImageEntry()
		{
			return myImageEntry;
		}

		public ZLVideoEntry getVideoEntry()
		{
			return myVideoEntry;
		}

		// add by zdc
		public ZLTextBackgroundEntry getBackgroundEntry()
		{
			return myTextBackgroundEntry;
		}

		// end by zdc

		public ZLTextStyleEntry getStyleEntry()
		{
			return myStyleEntry;
		}

		public short getFixedHSpaceLength()
		{
			return myFixedHSpaceLength;
		}

		public boolean next()
		{
			if (myCounter >= myLength) {
				return false;
			}

			int dataOffset = myDataOffset;
			char[] data = myStorage.block(myDataIndex);// 一开始myDataIndex为0，myStorage中分两部分缓存了书籍数据源数据
			if (data == null) {
				return false;
			}
			if (dataOffset >= data.length) {// 偏移量大于数据本身，说明要提取的数据在myStorage中的第二部分
				data = myStorage.block(++myDataIndex);
				if (data == null) {
					return false;
				}
				dataOffset = 0;
			}
			short first = (short) data[dataOffset];
			byte type = (byte) first;
			if (type == 0) {
				data = myStorage.block(++myDataIndex);
				if (data == null) {
					return false;
				}
				dataOffset = 0;
				first = (short) data[0];
				type = (byte) first;
			}
			myType = type;
			++dataOffset;
			switch (type) {
			case ZLTextParagraph.Entry.TEXT: {
				int textLength = (int) data[dataOffset++];
				textLength += (((int) data[dataOffset++]) << 16);
				textLength = Math.min(textLength, data.length - dataOffset);
				myTextLength = textLength;
				myTextData = data;
				myTextOffset = dataOffset;
				dataOffset += textLength;
				break;
			}
			case ZLTextParagraph.Entry.CONTROL: {
				short kind = (short) data[dataOffset++];
				myControlKind = (byte) kind;
				myControlIsStart = (kind & 0x0100) == 0x0100;
				myHyperlinkType = 0;
				break;
			}
			case ZLTextParagraph.Entry.HYPERLINK_CONTROL: {
				final short kind = (short) data[dataOffset++];
				myControlKind = (byte) kind;
				myControlIsStart = true;
				myHyperlinkType = (byte) (kind >> 8);
				final short labelLength = (short) data[dataOffset++];
				myHyperlinkId = new String(data, dataOffset, labelLength);
				dataOffset += labelLength;
				break;
			}
			case ZLTextParagraph.Entry.IMAGE: {
				final short vOffset = (short) data[dataOffset++];
				final short len = (short) data[dataOffset++];
				final String id = new String(data, dataOffset, len);
				dataOffset += len;
				final boolean isCover = data[dataOffset++] != 0;
				myImageEntry = new ZLImageEntry(myImageMap, id, vOffset, isCover);
				break;
			}
			case ZLTextParagraph.Entry.FIXED_HSPACE:
				myFixedHSpaceLength = (short) data[dataOffset++];
				break;
			case ZLTextParagraph.Entry.STYLE_CSS:
			case ZLTextParagraph.Entry.STYLE_OTHER: {
				final short depth = (short) ((first >> 8) & 0xFF);
				final ZLTextStyleEntry entry =
						type == ZLTextParagraph.Entry.STYLE_CSS
								? new ZLTextCSSStyleEntry(depth)
								: new ZLTextOtherStyleEntry();
				// final short mask = (short) data[dataOffset++];
				// add by yq
				int mask = (int) data[dataOffset++];
				mask += (((int) data[dataOffset++]) << 16);
				// end by yq
				for (int i = 0; i < NUMBER_OF_LENGTHS; ++i) {
					if (ZLTextStyleEntry.isFeatureSupported(mask, i)) {
						final short size = (short) data[dataOffset++];
						final byte unit = (byte) data[dataOffset++];
						entry.setLength(i, size, unit);
					}
				}
				if (ZLTextStyleEntry.isFeatureSupported(mask, ALIGNMENT_TYPE) ||
						ZLTextStyleEntry.isFeatureSupported(mask, NON_LENGTH_VERTICAL_ALIGN)) {
					final short value = (short) data[dataOffset++];
					if (ZLTextStyleEntry.isFeatureSupported(mask, ALIGNMENT_TYPE)) {
						entry.setAlignmentType((byte) (value & 0xFF));
					}
					if (ZLTextStyleEntry.isFeatureSupported(mask, NON_LENGTH_VERTICAL_ALIGN)) {
						entry.setVerticalAlignCode((byte) ((value >> 8) & 0xFF));
					}
				}
				if (ZLTextStyleEntry.isFeatureSupported(mask, FONT_FAMILY)) {
					entry.setFontFamilies(myFontManager, (short) data[dataOffset++]);
				}
				if (ZLTextStyleEntry.isFeatureSupported(mask, FONT_STYLE_MODIFIER)) {
					final short value = (short) data[dataOffset++];
					entry.setFontModifiers((byte) (value & 0xFF), (byte) ((value >> 8) & 0xFF));
				}

				// add by yq
				if (ZLTextStyleEntry.isFeatureSupported(mask, TEXT_COLOR)) {
					// 使用int，避免数据溢出
					// final int value = (int) data[dataOffset + 1] << 16 |
					// (int) data[dataOffset];
					// dataOffset += 2;
					int value = (int) data[dataOffset++];
					value += (((int) data[dataOffset++]) << 16);
					entry.setTextColor(new ZLColor(value));
				}

				if (ZLTextStyleEntry.isFeatureSupported(mask, BG_COLOR)) {
					int value = (int) data[dataOffset++];
					value += (((int) data[dataOffset++]) << 16);
					entry.setBackgroundColor(new ZLColor((int) value));
				}
				for (int i = 0; i < NUMBER_OF_BORDERS; ++i) {
					final byte style = (byte) data[dataOffset++];
					int color = (int) data[dataOffset++];
					color += (((int) data[dataOffset++]) << 16);
					entry.setBorderType(style, color, i);
				}

				// end by yq

				myStyleEntry = entry;
			}
			case ZLTextParagraph.Entry.STYLE_CLOSE:
				// No data
				break;
			case ZLTextParagraph.Entry.RESET_BIDI:
				// No data
				break;
			case ZLTextParagraph.Entry.AUDIO:
				// No data
				break;
			case ZLTextParagraph.Entry.VIDEO: {
				myVideoEntry = new ZLVideoEntry();
				final short mapSize = (short) data[dataOffset++];
				for (short i = 0; i < mapSize; ++i) {
					short len = (short) data[dataOffset++];
					final String mime = new String(data, dataOffset, len);
					dataOffset += len;
					len = (short) data[dataOffset++];
					final String src = new String(data, dataOffset, len);
					dataOffset += len;
					myVideoEntry.addSource(mime, src);
				}
				break;
			}
			// add by zdc
			case ZLTextParagraph.Entry.BACKGROUND: {
				short kind = (short) data[dataOffset++];
				final byte bgKind = (byte) kind;
				final boolean isStart = (kind & 0x0100) == 0x0100;
				final boolean isBlockType = ((short) data[dataOffset++] == 1);
				if (!isBlockType) {
					ZLog.d(ZLog.FBReader, "inline");
				}
				int color = (int) data[dataOffset++];
				color += (((int) data[dataOffset++]) << 16);

				int lcolor = (int) data[dataOffset++];
				lcolor += (((int) data[dataOffset++]) << 16);

				int tcolor = (int) data[dataOffset++];
				tcolor += (((int) data[dataOffset++]) << 16);

				int rcolor = (int) data[dataOffset++];
				rcolor += (((int) data[dataOffset++]) << 16);

				int bcolor = (int) data[dataOffset++];
				bcolor += (((int) data[dataOffset++]) << 16);

				myTextBackgroundEntry = new ZLTextBackgroundEntry(bgKind, isStart, isBlockType, new ZLColor(color),
						new ZLColor(lcolor), new ZLColor(tcolor), new ZLColor(rcolor), new ZLColor(bcolor));
				break;
			}
			// end by zdc

			// add by yq
			case ZLTextParagraph.Entry.SPECIAL_TAG: {
				// No data
				break;
			}
			// end by yq

			}
			++myCounter;
			myDataOffset = dataOffset;
			return true;
		}
	}

	protected ZLTextPlainModel(
			String id,
			String language,
			int[] entryIndices,
			int[] entryOffsets,
			int[] paragraphLenghts,
			int[] paragraphStartIndexes,
			int[] textSizes,
			byte[] paragraphKinds,
			CharStorage storage,
			Map<String, ZLImage> imageMap,
			FontManager fontManager)
	{
		myId = id;
		myLanguage = language;
		myStartEntryIndices = entryIndices;
		myStartEntryOffsets = entryOffsets;
		myParagraphLengths = paragraphLenghts;
		myTextSizes = textSizes;
		myParagraphKinds = paragraphKinds;
		myStorage = storage;
		myImageMap = imageMap;
		myFontManager = fontManager;
		myBackgroundEntries = Collections.synchronizedMap(new HashMap<Integer, Vector<BackgroundItemInfo>>());
		myStartParagraphIndex = paragraphStartIndexes;
	}

	public final String getId()
	{
		return myId;
	}

	public final String getLanguage()
	{
		return myLanguage;
	}

	public final ZLTextMark getFirstMark()
	{
		return (myMarks == null || myMarks.isEmpty()) ? null : myMarks.get(0);
	}

	public final ZLTextMark getLastMark()
	{
		return (myMarks == null || myMarks.isEmpty()) ? null : myMarks.get(myMarks.size() - 1);
	}

	public final ZLTextMark getNextMark(ZLTextMark position)
	{
		if (position == null || myMarks == null) {
			return null;
		}

		ZLTextMark mark = null;
		for (ZLTextMark current : myMarks) {
			if (current.compareTo(position) >= 0) {
				if ((mark == null) || (mark.compareTo(current) > 0)) {
					mark = current;
				}
			}
		}
		return mark;
	}

	public final ZLTextMark getPreviousMark(ZLTextMark position)
	{
		if ((position == null) || (myMarks == null)) {
			return null;
		}

		ZLTextMark mark = null;
		for (ZLTextMark current : myMarks) {
			if (current.compareTo(position) < 0) {
				if ((mark == null) || (mark.compareTo(current) < 0)) {
					mark = current;
				}
			}
		}
		return mark;
	}

	public final int search(final String text, int startIndex, int endIndex, boolean ignoreCase)
	{
		int count = 0;
		ZLSearchPattern pattern = new ZLSearchPattern(text, ignoreCase);
		myMarks = new ArrayList<ZLTextMark>();
		if (startIndex > myParagraphsNumber) {
			startIndex = myParagraphsNumber;
		}
		if (endIndex > myParagraphsNumber) {
			endIndex = myParagraphsNumber;
		}
		int index = startIndex;
		final EntryIteratorImpl it = new EntryIteratorImpl(index);
		while (true) {
			int offset = 0;
			while (it.next()) {
				if (it.getType() == ZLTextParagraph.Entry.TEXT) {
					char[] textData = it.getTextData();
					int textOffset = it.getTextOffset();
					int textLength = it.getTextLength();
					for (int pos = ZLSearchUtil.find(textData, textOffset, textLength, pattern); pos != -1; pos = ZLSearchUtil.find(textData, textOffset, textLength, pattern, pos + 1)) {
						myMarks.add(new ZLTextMark(index, offset + pos, pattern.getLength()));
						++count;
					}
					offset += textLength;
				}
			}
			if (++index >= endIndex) {
				break;
			}
			it.reset(index);
		}
		return count;
	}

	public final List<ZLTextMark> getMarks()
	{
		return myMarks != null ? myMarks : Collections.<ZLTextMark> emptyList();
	}

	public final void removeAllMarks()
	{
		myMarks = null;
	}

	public final int getParagraphsNumber()
	{
		return myParagraphsNumber;
	}

	public final ZLTextParagraph getParagraph(int index)
	{
		final byte kind = myParagraphKinds[index];
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
				new ZLTextParagraphImpl(this, index) :
				new ZLTextSpecialParagraphImpl(kind, this, index);
	}

	public final int getTextLength(int index)
	{
		if (myTextSizes.length == 0) {
			return 0;
		}
		return myTextSizes[Math.max(Math.min(index, myParagraphsNumber - 1), 0)];
	}

	private static int binarySearch(int[] array, int length, int value)
	{
		int lowIndex = 0;
		int highIndex = length - 1;

		while (lowIndex <= highIndex) {
			int midIndex = (lowIndex + highIndex) >>> 1;
			int midValue = array[midIndex];
			if (midValue > value) {
				highIndex = midIndex - 1;
			} else if (midValue < value) {
				lowIndex = midIndex + 1;
			} else {
				return midIndex;
			}
		}
		return -lowIndex - 1;
	}

	public final int findParagraphByTextLength(int length)
	{
		int index = binarySearch(myTextSizes, myParagraphsNumber, length);
		if (index >= 0) {
			return index;
		}
		return Math.min(-index - 1, myParagraphsNumber - 1);
	}

	// add by zdc
	/**
	 * 根据传入的段落索引，获取该段落所在章节中的背景信息
	 * 
	 * @param paragraphIndex
	 *            段落索引
	 */
	public Vector<BackgroundItemInfo> getParagraphBackgroundItemInfo(int paragraphIndex)
	{
		int startIndex = getParagraphStartIndex(paragraphIndex);
		Object resultObject = myBackgroundEntries.get(startIndex);
		if (resultObject != null) {
			return (Vector<BackgroundItemInfo>) myBackgroundEntries.get(startIndex);
		}
		return null;
	}

	/**
	 * 处理某个章节的DIV背景数据
	 */
	public synchronized final void processBackgroundEntry(ZLTextBackgroundEntry bgEntry, int paragraphIndex, int elementIndex)
	{
		if (!isProcessingBackgroundEntry) {
			return;
		}

		int paragraphStartIndex = getParagraphStartIndex(paragraphIndex);
		elementIndex = Math.max(0, elementIndex);
		Vector<BackgroundItemInfo> myParagraphBgItemInfoVector = (Vector<BackgroundItemInfo>) myBackgroundEntries.get(paragraphStartIndex);
		if (myParagraphBgItemInfoVector == null) {
			myParagraphBgItemInfoVector = new Vector<BackgroundItemInfo>();
			myBackgroundEntries.put(paragraphStartIndex, myParagraphBgItemInfoVector);
		}
		// 例如，遇到<div start>xxx...<span start>xxx...<span end>xxx...<div end>
		// 则进入逻辑是：
		// 1.遇到<div start>，进入A，myBackgroundEntryStack存储了{顶<-[0]->底}
		// 2.遇到<span start>，进入A，myBackgroundEntryStack存储了{顶<-[1，0]->底}
		// 3.自此，myParagraphBgItemInfoVector中存储了myParagraphBgItemInfoVector[0]->div
		// 和myParagraphBgItemInfoVector[1]->span的起始信息
		// go on
		if (bgEntry.getStart()) {
			// A
			BackgroundItemInfo myBgItemInfo = new BackgroundItemInfo();
			myBgItemInfo.bgColor = bgEntry.getBgColor();
			myBgItemInfo.startRange.paragraphIndex = paragraphIndex;
			myBgItemInfo.startRange.elementIndex = elementIndex;
			myBgItemInfo.isBlock = bgEntry.getBlockType();

			myBgItemInfo.kind = bgEntry.getKind();
			myBgItemInfo.leftBorderColor = bgEntry.getLeftBorderColor();
			myBgItemInfo.topBorderColor = bgEntry.getTopBorderColor();
			myBgItemInfo.rightBorderColor = bgEntry.getRightBorderColor();
			myBgItemInfo.bottomBorderColor = bgEntry.getBottomBorderColor();

			myParagraphBgItemInfoVector.add(myBgItemInfo);
			myBackgroundEntryStack.push(myParagraphBgItemInfoVector.size() - 1);
		} else {
			// 4.遇到<span end>，进入B，取出的p为1，从myParagraphBgItemInfoVector中取出
			// myParagraphBgItemInfoVector[1]->span，存储该span的结束信息
			// okay，span结束了
			// 5.遇到<div end>，进入B，取出的p为0，从myParagraphBgItemInfoVector中取出
			// myParagraphBgItemInfoVector[0]->div，存储该div的结束信息
			// div，span结束了
			// all end
			// B
			if (myBackgroundEntryStack.size() > 0) {
				int p = myBackgroundEntryStack.pop();
				if (p > myParagraphBgItemInfoVector.size() - 1) {
					ZLog.d(ZLog.FBReader, "Error encountered");
					return;
				}
				BackgroundItemInfo itemInfo = myParagraphBgItemInfoVector.get(p);
				itemInfo.endRange.paragraphIndex = paragraphIndex;
				itemInfo.endRange.elementIndex = elementIndex;
			}
		}
	}

	/**
	 * 返回该段落所在章节的起始段落索引
	 * 
	 * @param paragraphIndex
	 *            段落索引
	 */
	public int getParagraphStartIndex(int paragraphIndex)
	{
		for (int i = 0; i < myStartParagraphIndex.length; ++i) {
			if (i == myStartParagraphIndex.length - 1) {
				if (myStartParagraphIndex[i] <= paragraphIndex) {
					return myStartParagraphIndex[i];
				}
			} else {
				if (myStartParagraphIndex[i] <= paragraphIndex
						&& myStartParagraphIndex[i + 1] > paragraphIndex) {
					return myStartParagraphIndex[i];
				}
			}
		}
		return 0;
	}

	/**
	 * 返回该段落所在章节的结束段落索引
	 * 
	 * @param paragraphIndex
	 *            段落索引
	 */
	public int getParagraphEndIndex(int paragraphIndex)
	{
		for (int i = 0; i < myStartParagraphIndex.length - 1; ++i) {
			if (myStartParagraphIndex[i] <= paragraphIndex
					&& myStartParagraphIndex[i + 1] > paragraphIndex) {
				return myStartParagraphIndex[i + 1] - 1;
			}
		}
		return myParagraphsNumber - 1;
	}

	public void setProcessingBackgroundEntry(boolean isProcessing)
	{
		isProcessingBackgroundEntry = isProcessing;
	}
	// end by zdc
}
