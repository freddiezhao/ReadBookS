package com.sina.book.reader.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.sina.book.reader.model.PageContent.SelectText;
import com.sina.book.reader.page.TextLine;
import com.sina.book.reader.selector.Selection;
import com.sina.book.util.PixelUtil;

/**
 * 文字段落
 * 
 * @author Tsimle
 * 
 */
public class TextParagraph extends Paragraph {
	// private static final String TAG = "TextParagraph";

	public static int SUMMARY_LINE_TYPE_NO = 0;
	public static int SUMMARY_LINE_TYPE_PART = 1;
	public static int SUMMARY_LINE_TYPE_ALL = 2;

	/** 段落开始空格. */
	protected static Pattern mParaPattern = Pattern.compile("^([\\s|　]+)");

	private float fontScale = 1.0f;
	private boolean isBold;
	private ArrayList<TextLine> lines = new ArrayList<TextLine>();

	private Paint readPaint;

	public TextParagraph() {
		super();
	}

	public void init(String content, ParagraphCreateBean createBean, int realBytesLength, int styleBytesLength) {
		readPaint = mReadStyleManager.getScaledReadPaint(fontScale, isBold);

		createBean.byteUsed = realBytesLength + styleBytesLength;
		int paraBeginDiff = 0;
		int paraEndDiff = 0;

		// 段尾处理
		try {
			if (content.lastIndexOf(ROW_DIV1) != -1) {
				paraEndDiff = ROW_DIV1.getBytes(mCharset).length;
				content = content.replaceAll(ROW_DIV1, "");

			} else if (content.lastIndexOf(ROW_DIV2) != -1) {
				paraEndDiff = ROW_DIV2.getBytes(mCharset).length;
				content = content.replaceAll(ROW_DIV2, "");
			}
		} catch (UnsupportedEncodingException e) {
		}

		// 段首处理
		if (createBean.isParagrahBegin) {
			Matcher m = mParaPattern.matcher(content);
			if (m.find()) {
				String paraBeginSpaces = m.group(0);
				content = content.replaceFirst(paraBeginSpaces, PARA_BEGIN_SPACE);
				try {
					paraBeginDiff = paraBeginSpaces.getBytes(mCharset).length
							- PARA_BEGIN_SPACE.getBytes(mCharset).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

			} else {
				content = PARA_BEGIN_SPACE + content;
				try {
					paraBeginDiff = -PARA_BEGIN_SPACE.getBytes(mCharset).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		// LogUtil.d("cx", "bytesLength:" + realBytesLength);
		// LogUtil.d("cx", "paraBeginDiff:" + paraBeginDiff + " paraEndDiff：" +
		// paraEndDiff);

		int size = 0;
		char[] paraChars = content.toCharArray();
		int paraLength = paraChars.length;
		boolean allUsed = false;

		int lastLineLeight = (int) (fontScale * mReadStyleManager.getLastLineFontHeight());

		if (createBean.towardsFoward) {
			while (size < paraLength) {
				float overHeight = createBean.availableHeight - getHeight();
				if (overHeight < lastLineLeight) {
					allUsed = true;
					if (size == 0) {
						createBean.byteUsed = 0;
						break;
					}
					String remain = new String(paraChars, size, paraLength - size);
					// LogUtil.d("cx", "remain:" + remain);
					try {
						createBean.byteUsed = createBean.byteUsed - remain.getBytes(mCharset).length - paraEndDiff;
						// LogUtil.d("cx", "2 createBean.byteUsed:" +
						// createBean.byteUsed);
					} catch (UnsupportedEncodingException e) {
					}
					break;
				}

				int alinesize = readPaint.breakText(paraChars, size, paraLength - size,
						mReadStyleManager.getVisibleWidth(), null);
				TextLine line = new TextLine();
				line.setContent(new String(paraChars, size, alinesize));
				if (size == 0) {
					line.setOffset(-paraBeginDiff - styleBytesLength);
				}
				lines.add(line);
				size = size + alinesize;
			}

			// LogUtil.w("cx", "AvailableHeight : " +
			// createBean.availableHeight);
			// LogUtil.e("cx", "TextParagraph height : " + getHeight());
		} else {
			while (size < paraLength) {
				int alinesize = readPaint.breakText(paraChars, size, paraLength - size,
						mReadStyleManager.getVisibleWidth(), null);
				TextLine line = new TextLine();
				line.setContent(new String(paraChars, size, alinesize));
				lines.add(line);
				size = size + alinesize;
			}
			// 如果需要remove bytes，减去样式字节
			if (lastLineLeight + (lines.size() - 1) * fontScale * mReadStyleManager.getLineHeight() > createBean.availableHeight) {
				createBean.byteUsed -= styleBytesLength;
			}

			while (lastLineLeight + (lines.size() - 1) * fontScale * mReadStyleManager.getLineHeight() > createBean.availableHeight) {
				allUsed = true;
				try {
					createBean.byteUsed -= (lines.get(0).getContent().getBytes(mCharset).length + paraBeginDiff);
					paraBeginDiff = 0;
					lines.remove(0);
				} catch (UnsupportedEncodingException e) {
				}
				if (lines.size() == 0) {
					createBean.byteUsed = 0;
					break;
				}
			}
		}

		if (allUsed) {
			createBean.availableHeight = 0;
		} else {
			createBean.availableHeight -= getHeight();
		}

		// LogUtil.e("cx", "AvailableHeight : " + createBean.availableHeight);
	}

	public void setFontScale(float fontScale) {
		this.fontScale = fontScale;
	}

	public void setBold(boolean isBold) {
		this.isBold = isBold;
	}

	@Override
	public float getHeight() {
		return lines.size() * fontScale * mReadStyleManager.getLineHeight();
	}

	public ArrayList<TextLine> getLines() {
		return lines;
	}

	/**
	 * 获取一段总的字符数
	 * 
	 * @return
	 */
	public int getCharsCount() {
		int count = 0;
		for (TextLine line : lines) {
			count += line.getContent().length();
		}

		return count;
	}

	@Override
	public void draw(float startX, float startY, Canvas canvas) {
		float fontHeight = mReadStyleManager.getFontHeight();
		float fontSpaceHeight = mReadStyleManager.getLineSpaceHeight();

		mReadStyleManager.updateReadPaint(readPaint);
		int offset = mReadStyleManager.getTextOffsetVertical();
		float increase = (fontSpaceHeight + fontHeight) * fontScale;

		for (TextLine line : lines) {
			canvas.drawText(line.getContent(), startX, startY - offset, readPaint);
			startY += increase;
		}
	}

	/**
	 * 画出选择器背景
	 * 
	 * @param start
	 *            开始字符位置
	 * @param end
	 *            结束字符位置
	 * @param canvas
	 *            画布
	 * @param rectF
	 *            该段落矩形
	 * @return 选择的文字
	 */
	public SelectText drawContentSelector(Selection start, Selection end, Canvas canvas, RectF rectF) {
		SelectText select = new SelectText();

		float fontHeight = mReadStyleManager.getFontHeight() * fontScale;
		float fontSpaceHeight = mReadStyleManager.getLineSpaceHeight() * fontScale;

		float contentStartX = rectF.left;
		float contentStartY = rectF.top + fontHeight;

		float selectWordHeight = mReadStyleManager.getSelectionHeight();

		int min = start.getSelection();
		int max = end.getSelection();

		int drawCurrent = 0;

		StringBuilder selectText = new StringBuilder();
		StringBuilder beforeText = new StringBuilder();

		boolean isBeforeSelect = true;
		int startOffset = 0;

		boolean isSelecting = false;
		int endOffset = 0;

		Paint newPaint = new Paint(readPaint);
		newPaint.setColor(mReadStyleManager.getSelectionTextColor());

		for (TextLine line : lines) {
			String lineContent = line.getContent();
			char[] textCharArray = lineContent.toCharArray();
			float drawedWidth = 0;
			float charWidth = 0;

			if (isBeforeSelect) {
				startOffset += line.getOffset();
			}

			if (isSelecting) {
				endOffset += line.getOffset();
			}

			for (int i = 0; i < textCharArray.length; i++) {
				charWidth = readPaint.measureText(textCharArray, i, 1);

				String ch = String.valueOf(textCharArray[i]);

				// 画选中的文字的背景
				if (drawCurrent >= min && drawCurrent <= max) {
					isBeforeSelect = false;
					selectText.append(textCharArray[i]);

					if (lineContent.startsWith(PARA_BEGIN_SPACE) && PARA_SINGLE_SPACE.equals(ch) && i < 2) {
						// 如果是段首且选中字符为段首指定空格字符则忽略
						isSelecting = false;
					} else {
						isSelecting = true;

						float leftX = contentStartX + drawedWidth - 2;
						float rightX = contentStartX + drawedWidth + charWidth + 2;
						float topY = contentStartY - selectWordHeight;

						canvas.drawRect(leftX, topY, rightX, contentStartY, newPaint);
					}

				} else if (drawCurrent < min) {
					beforeText.append(textCharArray[i]);

					isBeforeSelect = true;
					isSelecting = false;
				} else {
					isSelecting = false;
				}

				drawedWidth += charWidth;
				drawCurrent++;
			}
			contentStartY += fontSpaceHeight + fontHeight;
		}

		try {
			// 取得选中文字在文件中的准确字节位置
			select.begin = mParaBegin;
			int beforeTxtLength = beforeText.toString().getBytes(mCharset).length;
			int selectTxtLength = selectText.toString().getBytes(mCharset).length;

			select.begin += beforeTxtLength - startOffset;
			select.length = selectTxtLength - endOffset;
			select.content = selectText.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return select;
	}

	/**
	 * 画下划线
	 * 
	 * @param start
	 *            开始字节位置
	 * @param end
	 *            结束字节位置
	 * @param canvas
	 *            画布
	 * @param page
	 *            所在页面对象
	 * @param rectF
	 *            该段落矩形
	 */
	public BookSummaryPostion drawUnderLine(int start, int end, Canvas canvas, PageContent page, RectF rectF) {
		BookSummaryPostion bookSummaryPostion = new BookSummaryPostion();

		float fontHeight = mReadStyleManager.getFontHeight() * fontScale;
		float fontSpaceHeight = mReadStyleManager.getLineSpaceHeight() * fontScale;

		float contentStartX = rectF.left;
		float contentStartY = rectF.top + fontHeight;

		int drawCursor = mParaBegin;
		int lineHeight = PixelUtil.dp2px(1.5f);

		Paint linePaint = new Paint(readPaint);
		linePaint.setColor(mReadStyleManager.getUnderLineColor());
		for (int j = 0; j < getLines().size(); j++) {
			TextLine line = getLines().get(j);
			String lineContent = line.getContent();
			drawCursor -= line.getOffset();

			int bytesAline = 0;
			try {
				bytesAline = lineContent.getBytes(mCharset).length;
			} catch (UnsupportedEncodingException e) {
				bytesAline = 2 * lineContent.length();
			}

			// 通过区分画每一行书摘可能的类型，提升效率
			int summaryLineType = getSummaryLineType(drawCursor, drawCursor + bytesAline, start, end);
			if (summaryLineType == PageContent.SUMMARY_LINE_TYPE_NO) {
				drawCursor = drawCursor + bytesAline;
			} else if (summaryLineType == PageContent.SUMMARY_LINE_TYPE_ALL) {
				float leftX = contentStartX;
				if (lineContent.startsWith(PARA_BEGIN_SPACE)) {
					lineContent = lineContent.substring(2);
					leftX += readPaint.measureText(PARA_BEGIN_SPACE);
				}

				float rightX = leftX + readPaint.measureText(lineContent);
				float topY = contentStartY - lineHeight;

				canvas.drawRect(leftX, topY, rightX, contentStartY, linePaint);
				drawCursor = drawCursor + bytesAline;

				bookSummaryPostion.setPostion(contentStartY - fontHeight, leftX, rightX, contentStartY);
			} else {
				char[] textCharArray = lineContent.toCharArray();
				float drawedWidth = 0;
				float charWidth = 0;

				for (int i = 0; i < textCharArray.length; i++) {
					charWidth = readPaint.measureText(textCharArray, i, 1);

					String ch = String.valueOf(textCharArray[i]);

					// 画出下划线
					if (drawCursor >= start && drawCursor < end) {

						if (lineContent.startsWith(PARA_BEGIN_SPACE) && PARA_SINGLE_SPACE.equals(ch) && i < 2) {
							// 如果是段首且选中字符为段首指定空格字符则忽略
						} else {
							float leftX = contentStartX + drawedWidth - 2;
							float rightX = contentStartX + drawedWidth + charWidth + 2;
							float topY = contentStartY - lineHeight;

							canvas.drawRect(leftX, topY, rightX, contentStartY, linePaint);
							bookSummaryPostion.setPostion(contentStartY - fontHeight, leftX, rightX, contentStartY);
						}
					}

					drawedWidth += charWidth;

					try {
						int length = String.valueOf(textCharArray[i]).getBytes(mCharset).length;
						drawCursor += length;
					} catch (UnsupportedEncodingException e) {
						drawCursor += 2;
						e.printStackTrace();
					}
				}
			}
			contentStartY += fontSpaceHeight + fontHeight;
		}
		bookSummaryPostion.oneLineHeight = fontHeight;

		return bookSummaryPostion;
	}

	@Override
	public Selection findSelection(RectF paraRect, float x, float y) {
		float fontHeight = mReadStyleManager.getFontHeight() * fontScale;
		float contentStartX = paraRect.left;
		float contentStartY = paraRect.top + fontHeight;
		float fontSpaceHeight = mReadStyleManager.getLineSpaceHeight();

		int drawCurrent = 0;
		Selection selection = new Selection();

		for (TextLine line : lines) {
			String lineContent = line.getContent();
			char[] textCharArray = lineContent.toCharArray();
			float drawedWidth = 0;
			float charWidth = 0;

			for (int i = 0; i < textCharArray.length; i++) {
				charWidth = readPaint.measureText(textCharArray, i, 1);

				float left = contentStartX + drawedWidth;
				float top = contentStartY - fontHeight;
				float right = contentStartX + drawedWidth + charWidth;
				float bottom = contentStartY + fontSpaceHeight;

				// 判断是否是段落开始空格
				String ch = String.valueOf(textCharArray[i]);

				RectF rect = new RectF(left, top, right, bottom);

				if (lineContent.startsWith(PARA_BEGIN_SPACE) && PARA_SINGLE_SPACE.equals(ch) && i < 2) {
					// 如果是段首且选中字符为段首指定空格字符则忽略
				} else if (rect.contains(x, y)) {
					selection.setSelection(drawCurrent);
					selection.setSelectionX(rect.left);
					selection.setSelectionY(rect.top);
					selection.setSelectionCharW(charWidth);
					selection.setSelectionCharH(fontSpaceHeight + fontHeight);
					return selection;
				}
				drawedWidth += charWidth;
				drawCurrent++;
			}

			contentStartY += fontSpaceHeight + fontHeight;
		}
		return selection;
	}

	/**
	 * 获取指定字符的Selection
	 * 
	 * @param paraRect
	 *            字符所在段的矩形
	 * @param charIndex
	 *            字符索引
	 * @param inFront
	 *            指定索引前一字符,用于区分开始位置和结束位置
	 * @return 该字符Selection
	 */
	public Selection getCharSelection(RectF paraRect, int charIndex, boolean inFront) {
		float fontHeight = mReadStyleManager.getFontHeight() * fontScale;
		float contentStartX = paraRect.left;
		float contentStartY = paraRect.top + fontHeight;
		float fontSpaceHeight = mReadStyleManager.getLineSpaceHeight();

		Selection selection = new Selection();

		int charCount = 0;

		for (TextLine line : lines) {
			String lineContent = line.getContent();
			charCount += lineContent.length();

			if (charCount < charIndex) {
				contentStartY += fontSpaceHeight + fontHeight;
				continue;

			} else {
				char[] textCharArray = lineContent.toCharArray();
				float drawedWidth = 0;
				float charWidth = 0;

				for (int i = 0; i < textCharArray.length; i++) {
					charWidth = readPaint.measureText(textCharArray, i, 1);

					float left = contentStartX + drawedWidth;
					float top = contentStartY - fontHeight;
					float right = contentStartX + drawedWidth + charWidth;
					float bottom = contentStartY + fontSpaceHeight;

					// 判断是否是段落开始空格
					String ch = String.valueOf(textCharArray[i]);

					RectF rect = new RectF(left, top, right, bottom);

					if (lineContent.startsWith(PARA_BEGIN_SPACE) && PARA_SINGLE_SPACE.equals(ch) && i < 2) {
						// 如果是段首且选中字符为段首指定空格字符则忽略
					} else {

						// 这里需要区分是开始位置还是结束位置，否则如果一行是一段就会出错
						if ((charCount >= charIndex && ((inFront ? i : i + 1) == charIndex))
								|| (charCount + i == charIndex)) {

							selection.setSelection(charIndex);
							selection.setSelectionX(rect.left);
							selection.setSelectionY(rect.top);
							selection.setSelectionCharW(charWidth);
							selection.setSelectionCharH(fontSpaceHeight + fontHeight);

							return selection;
						}
					}
					drawedWidth += charWidth;
				}
			}
		}

		return selection;
	}

	private static int getSummaryLineType(int lineBytesStart, int lineBytesEnd, int summaryBytesStart,
			int summaryBytesEnd) {
		if (lineBytesStart <= summaryBytesEnd && lineBytesEnd >= summaryBytesStart) {
			if (lineBytesStart >= summaryBytesStart && lineBytesEnd < summaryBytesEnd) {
				return SUMMARY_LINE_TYPE_ALL;
			} else {
				return SUMMARY_LINE_TYPE_PART;
			}
		}
		return SUMMARY_LINE_TYPE_NO;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (lines != null) {
			sb.append("[lines]:{");
			for (TextLine line : lines) {
				sb.append("\n");
				sb.append("content:").append(line.getContent());
			}
			sb.append("}");
		}

		return sb.toString();
	}

}
