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

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.image.ZLImageManager;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.model.ZLImageEntry;
import org.geometerplus.zlibrary.text.model.ZLTextBackgroundEntry;
import org.geometerplus.zlibrary.text.model.ZLTextMark;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextOtherStyleEntry;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;
import org.geometerplus.zlibrary.text.model.ZLTextStyleEntry;
import org.vimgadgets.linebreak.LineBreaker;

/**
 * FBReader中,是以段落为基准,这个是在生成Model的时候,每一次添加数据addData()所添加的都<br>
 * 是一个段落,即一段一段得放到Model中。整个文章就是一个段落一个段落来连接。假设一个文<br>
 * 章有10段,每一页显示一段,一打开书,第1段从Model里取出,index=1;然后点击下一页,那么index=2<br>
 * ,从Model获取第二段 , 其实就是一个索引值和一个段落内容。<br>
 * <br>
 * ZLTextParagraphCursor就是这样的一个东西,ZLTextWordCursor就是利用ZLTextParagraphCursor来<br>
 * 定位到某个字所在的段，所在段的第几个Element，第几字母。( 注: FBReader主要是写给英文或者<br>
 * 其他字母型语言，所以就有所谓的Element，英文"world"算一个字，但是字里还有字母。)。
 * 
 * @author chenjl
 * 
 */
public final class ZLTextParagraphCursor
{
	/**
	 * 负责填充段落内容的处理类
	 * 
	 * @author chenjl
	 * 
	 */
	private static final class Processor
	{
		private final ZLTextModel				myModel;
		private final ZLTextParagraph			myParagraph;
		private final LineBreaker				myLineBreaker;
		/**
		 * 段落所有的元素
		 */
		private final ArrayList<ZLTextElement>	myElements;
		private int								myOffset;
		private int								myFirstMark;
		private int								myLastMark;
		private final List<ZLTextMark>			myMarks;
		private final int						myParagraphIndex;

		private Processor(ZLTextModel textModel, ZLTextParagraph paragraph, LineBreaker lineBreaker, List<ZLTextMark> marks, int paragraphIndex, ArrayList<ZLTextElement> elements)
		{
			myModel = textModel;
			myParagraph = paragraph;
			myLineBreaker = lineBreaker;
			myElements = elements;
			myMarks = marks;
			myParagraphIndex = paragraphIndex;
			final ZLTextMark mark = new ZLTextMark(paragraphIndex, 0, 0);
			int i;
			for (i = 0; i < myMarks.size(); i++) {
				if (((ZLTextMark) myMarks.get(i)).compareTo(mark) >= 0) {
					break;
				}
			}
			myFirstMark = i;
			myLastMark = myFirstMark;
			for (; (myLastMark != myMarks.size()) && (((ZLTextMark) myMarks.get(myLastMark)).ParagraphIndex == paragraphIndex); myLastMark++)
				;
			myOffset = 0;
		}

		/**
		 * 负责填充段落元素myElements
		 */
		void fill()
		{
			int hyperlinkDepth = 0;
			ZLTextHyperlink hyperlink = null;

			final ArrayList<ZLTextElement> elements = myElements;
			for (ZLTextParagraph.EntryIterator it = myParagraph.iterator(); it.next();) {// it.next()负责解析
				switch (it.getType()) {// switch则将各个数据放入myElements中
				case ZLTextParagraph.Entry.TEXT:
					processTextEntry(it.getTextData(), it.getTextOffset(), it.getTextLength(), hyperlink);
					break;
				case ZLTextParagraph.Entry.CONTROL:
					if (hyperlink != null) {
						hyperlinkDepth += it.getControlIsStart() ? 1 : -1;
						if (hyperlinkDepth == 0) {
							hyperlink = null;
						}
					}
					elements.add(ZLTextControlElement.get(it.getControlKind(), it.getControlIsStart()));
					break;
				case ZLTextParagraph.Entry.HYPERLINK_CONTROL: {
					final byte hyperlinkType = it.getHyperlinkType();
					if (hyperlinkType != 0) {
						final ZLTextHyperlinkControlElement control = new ZLTextHyperlinkControlElement(it.getControlKind(), hyperlinkType, it.getHyperlinkId());
						elements.add(control);
						hyperlink = control.Hyperlink;
						hyperlinkDepth = 1;
					}
					break;
				}
				case ZLTextParagraph.Entry.IMAGE:
					final ZLImageEntry imageEntry = it.getImageEntry();
					final ZLImage image = imageEntry.getImage();
					if (image != null) {
						ZLImageData data = ZLImageManager.Instance().getImageData(image);
						if (data != null) {
							if (hyperlink != null) {
								hyperlink.addElementIndex(elements.size());
							}
							elements.add(new ZLTextImageElement(imageEntry.Id, data, image.getURI(), imageEntry.IsCover));
						}
					}
					break;
				case ZLTextParagraph.Entry.AUDIO:
					break;
				case ZLTextParagraph.Entry.VIDEO:
					elements.add(new ZLTextVideoElement(it.getVideoEntry().sources()));
					break;
				case ZLTextParagraph.Entry.STYLE_CSS:
				case ZLTextParagraph.Entry.STYLE_OTHER:
					elements.add(new ZLTextStyleElement(it.getStyleEntry()));
					break;
				case ZLTextParagraph.Entry.STYLE_CLOSE:
					elements.add(ZLTextElement.StyleClose);
					break;
				case ZLTextParagraph.Entry.FIXED_HSPACE:
					elements.add(ZLTextFixedHSpaceElement.getElement(it.getFixedHSpaceLength()));
					break;
				// add by zdc
				case ZLTextParagraph.Entry.BACKGROUND: {
					int elementIndex = elements.size();
					ZLTextBackgroundEntry entry = it.getBackgroundEntry();
					if (!entry.getStart()) {
						elementIndex = Math.max(0, elementIndex - 1);
						// while (elementIndex >= 0 &&
						// !(elements.get(elementIndex) instanceof ZLTextWord))
						// {
						// --elementIndex;
						// }
					}
					myModel.processBackgroundEntry(it.getBackgroundEntry(), myParagraphIndex, elementIndex);
					break;
				}
				// end by zdc
				// add by yq
				case ZLTextParagraph.Entry.SPECIAL_TAG: {
					elements.add(new ZLTextSpecialElement());
				}
				// end by yq
				}
			}
		}

		private static byte[]		ourBreaks	= new byte[1024];
		private static final int	NO_SPACE	= 0;
		private static final int	SPACE		= 1;

		// private static final int NON_BREAKABLE_SPACE = 2;
		private void processTextEntry(final char[] data, final int offset, final int length, ZLTextHyperlink hyperlink)
		{
			if (length != 0) {
				if (ourBreaks.length < length) {
					ourBreaks = new byte[length];
				}
				final byte[] breaks = ourBreaks;
				myLineBreaker.setLineBreaks(data, offset, length, breaks);

				final ZLTextElement hSpace = ZLTextElement.HSpace;
				final ArrayList<ZLTextElement> elements = myElements;
				char ch = 0;
				char previousChar = 0;
				int spaceState = NO_SPACE;
				int wordStart = 0;
				for (int index = 0; index < length; ++index) {// 循环遍历解析文本元素
					previousChar = ch;
					ch = data[offset + index];
					if (Character.isSpace(ch)) {
						if (index > 0 && spaceState == NO_SPACE) {
							addWord(data, offset + wordStart, index - wordStart, myOffset + wordStart, hyperlink);
						}
						spaceState = SPACE;
					} else {
						switch (spaceState) {
						case SPACE:
							// if (breaks[index - 1] == LineBreak.NOBREAK ||
							// previousChar == '-') {
							// }
							elements.add(hSpace);
							wordStart = index;
							break;
						// case NON_BREAKABLE_SPACE:
						// break;
						case NO_SPACE:
							if (index > 0 && breaks[index - 1] != LineBreaker.NOBREAK && previousChar != '-' && index != wordStart) {
								addWord(data, offset + wordStart, index - wordStart, myOffset + wordStart, hyperlink);
								wordStart = index;
							}
							break;
						}
						spaceState = NO_SPACE;
					}
				}
				switch (spaceState) {
				case SPACE:
					elements.add(hSpace);
					break;
				// case NON_BREAKABLE_SPACE:
				// break;
				case NO_SPACE:
					addWord(data, offset + wordStart, length - wordStart, myOffset + wordStart, hyperlink);
					break;
				}
				myOffset += length;
			}
		}

		private final void addWord(char[] data, int offset, int len, int paragraphOffset, ZLTextHyperlink hyperlink)
		{
			ZLTextWord word = new ZLTextWord(data, offset, len, paragraphOffset);
			for (int i = myFirstMark; i < myLastMark; ++i) {
				final ZLTextMark mark = (ZLTextMark) myMarks.get(i);
				if ((mark.Offset < paragraphOffset + len) && (mark.Offset + mark.Length > paragraphOffset)) {
					word.addMark(mark.Offset - paragraphOffset, mark.Length);
				}
			}
			if (hyperlink != null) {
				hyperlink.addElementIndex(myElements.size());
			}
			myElements.add(word);
		}
	}

	public final int						Index;											// 段落索引，从0开始
	public final ZLTextModel				Model;											// 书籍的数据源
	/**
	 * 该段落的所有元素集合，包含的元素不单单只有实体元素本身，还包含样式信息。？<br>
	 * 待验证：某个实体元素的样式发生变化了，就会插入一个ZLTextStyleElement或ZLT<br>
	 * extControlElement来表示?
	 */
	private final ArrayList<ZLTextElement>	myElements	= new ArrayList<ZLTextElement>();

	private ZLTextParagraphCursor(ZLTextModel model, int index)
	{
		Model = model;
		Index = Math.min(index, Model.getParagraphsNumber() - 1);
		fill();// 构建段落时便要开始填充段落数据
	}

	static ZLTextParagraphCursor cursor(ZLTextModel model, int index)
	{
		ZLTextParagraphCursor result = ZLTextParagraphCursorCache.get(model, index);
		if (result == null) {// 存储缓存中读取不到，创建，然后放入缓存中，缓存由ZLTextParagraphCursorCache负责
			result = new ZLTextParagraphCursor(model, index);
			ZLTextParagraphCursorCache.put(model, index, result);
		}
		return result;
	}

	private static final char[]	SPACE_ARRAY	= { ' ' };

	void fill()
	{
		ZLTextParagraph paragraph = Model.getParagraph(Index);
		switch (paragraph.getKind()) {
		case ZLTextParagraph.Kind.TEXT_PARAGRAPH:// 带内容的段落
			new Processor(Model, paragraph, new LineBreaker(Model.getLanguage()), Model.getMarks(), Index, myElements).fill();
			break;
		case ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH:// 空行
			myElements.add(new ZLTextWord(SPACE_ARRAY, 0, 1, 0));
			break;
		case ZLTextParagraph.Kind.ENCRYPTED_SECTION_PARAGRAPH: {// 加密的段落?
			final ZLTextStyleEntry entry = new ZLTextOtherStyleEntry();
			entry.setFontModifier(ZLTextStyleEntry.FontModifier.FONT_MODIFIER_BOLD, true);
			myElements.add(new ZLTextStyleElement(entry));
			myElements.add(new ZLTextWord(ZLResource.resource("drm").getResource("encryptedSection").getValue(), 0));
			break;
		}
		default:
			break;
		}
	}

	void clear()
	{
		myElements.clear();
	}

	public boolean isFirst()
	{
		return Index == 0;
	}

	public boolean isLast()
	{
		return (Index + 1 >= Model.getParagraphsNumber());
	}

	public boolean isEndOfSection()
	{
		return (Model.getParagraph(Index).getKind() == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
	}

	/**
	 * 段落的长度(所有元素的总数)
	 * 
	 * @return
	 */
	int getParagraphLength()
	{
		return myElements.size();
	}

	/**
	 * 上一段
	 * 
	 * @return
	 */
	public ZLTextParagraphCursor previous()
	{
		return isFirst() ? null : cursor(Model, Index - 1);
	}

	/**
	 * 下一段
	 * 
	 * @return
	 */
	public ZLTextParagraphCursor next()
	{
		return isLast() ? null : cursor(Model, Index + 1);
	}

	/**
	 * 根据段索引获取元素
	 * 
	 * @param index
	 * @return
	 */
	ZLTextElement getElement(int index)
	{
		try {
			return myElements.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	ZLTextParagraph getParagraph()
	{
		return Model.getParagraph(Index);
	}

	@Override
	public String toString()
	{
		return "ZLTextParagraphCursor [" + Index + " (0.." + myElements.size() + ")]";
	}
}
