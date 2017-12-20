package com.sina.book.reader.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.htmlcleaner.Utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.data.BookSummary;
import com.sina.book.reader.PageBitmap.ScrollReadPosInfo;
import com.sina.book.reader.PageFactory;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.reader.page.PageSummary;
import com.sina.book.reader.page.TextLine;
import com.sina.book.reader.selector.Selection;
import com.sina.book.util.LogUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.ThemeUtil;

/**
 * 一页内容
 * 
 * @author Tsimle
 * 
 */
public class PageContent {
	// private static final String TAG = "PageContent";

	public static int SUMMARY_LINE_TYPE_NO = 0;
	public static int SUMMARY_LINE_TYPE_PART = 1;
	public static int SUMMARY_LINE_TYPE_ALL = 2;

	private ReadStyleManager mReadStyleManager;

	private int mPageBegin = 0;
	private int mPageEnd = 0;

	private RectF mContentRectF;

	/** 段间距（保留属性）. */
	private float mParaSpace = 0.0f;

	private String mCharset;

	private List<Paragraph> paragraphs = new ArrayList<Paragraph>();

	private List<PageSummary> mPageSummaries;

	private List<BookSummaryPostion> mBookSummariePostions;

	/** 开始选择位置 */
	private Selection mStartSelection = new Selection();
	/** 结束选择位置 */
	private Selection mEndSelection = new Selection();
	/** 选择位置列表，按照段落分割 */
	private List<ParaSelection> mSelectionList;
	/** 选中的文本 */
	private SelectText mSelectText = new SelectText();

	public int getParagraphsSize() {
		if (paragraphs != null) {
			return paragraphs.size();
		}
		return 0;
	}

	/**
	 * 对所有书摘排序
	 */
	public static Comparator<PageSummary> mComparatorSummary = new Comparator<PageSummary>() {
		@Override
		public int compare(PageSummary summary1, PageSummary summary2) {
			if (summary1.start == summary2.start && summary1.end == summary2.end) {
				return 0;
			} else if (summary1.start > summary2.start) {
				return 1;
			} else {
				return -1;
			}
		}
	};

	/**
	 * 对所有选择位置排序
	 */
	public static Comparator<ParaSelection> sComparatorSelection = new Comparator<ParaSelection>() {
		@Override
		public int compare(ParaSelection s1, ParaSelection s2) {
			if (s1.paraIndex == s2.paraIndex) {
				return 0;
			} else if (s1.paraIndex > s2.paraIndex) {
				return 1;
			} else {
				return -1;
			}
		}
	};

	public PageContent() {
		mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.gContext);
		mContentRectF = mReadStyleManager.getContentRectF();
	}

	public boolean isEmpty() {
		return paragraphs.size() == 0;
	}

	public List<TextLine> getPageStringLines() {
		List<TextLine> lines = new ArrayList<TextLine>();
		for (Paragraph p : paragraphs) {
			if (p instanceof TextParagraph) {
				lines.addAll(((TextParagraph) p).getLines());
			}
		}
		return lines;
	}

	public void addParagraph(Paragraph paragraph) {
		if (paragraph != null) {
			paragraph.setPageContent(this);
			paragraphs.add(paragraph);
		}
	}

	public int getPageBegin() {
		return mPageBegin;
	}

	public void setPageBegin(int pageBegin) {
		this.mPageBegin = pageBegin;
	}

	public int getPageEnd() {
		return mPageEnd;
	}

	public void setPageEnd(int pageEnd) {
		this.mPageEnd = pageEnd;
	}

	public void getReadPosScroll(int scrollHeight, ScrollReadPosInfo nowScrollInfo) {
		float startH = 0f;
		float endH = 0f;
		for (Paragraph p : paragraphs) {
			startH = endH;
			endH = endH + p.getHeight();
			if (startH <= scrollHeight && endH >= scrollHeight) {
				int oldPos = nowScrollInfo.pos;
				nowScrollInfo.pos = p.mParaBegin;
				if (nowScrollInfo.pos != oldPos && p instanceof TextParagraph) {
					StringBuilder content = new StringBuilder();
					for (TextLine line : ((TextParagraph) p).getLines()) {
						content.append(line.getContent());
					}
					nowScrollInfo.content = content.toString();
				}
			}
		}
	}

	public void setCharset(String charset) {
		this.mCharset = charset;
	}

	public String getCharset() {
		return mCharset;
	}

	public BookSummaryPostion onTouchBookSummary(MotionEvent e) {
		if (!mReadStyleManager.getContentRectF().contains(e.getX(), e.getY())) {
			return null;
		}
		if (mBookSummariePostions != null) {
			for (BookSummaryPostion bookSummaryPostion : mBookSummariePostions) {
				if (bookSummaryPostion.contain(e)) {
					return bookSummaryPostion;
				}
			}
		}
		return null;
	}

	public void addBookSummaryPostion(BookSummaryPostion bookSummaryPostion) {
		if (mBookSummariePostions == null) {
			mBookSummariePostions = new ArrayList<BookSummaryPostion>();
		}
		BookSummaryPostion sameSummaryPostion = null;
		for (BookSummaryPostion oldSummaryPostion : mBookSummariePostions) {
			if (oldSummaryPostion.relateBookSummary.equals(bookSummaryPostion.relateBookSummary)) {
				sameSummaryPostion = oldSummaryPostion;
				break;
			}
		}
		if (sameSummaryPostion != null) {
			if (bookSummaryPostion.endBottom > sameSummaryPostion.endBottom) {
				sameSummaryPostion.endBottom = bookSummaryPostion.endBottom;
				sameSummaryPostion.endRight = bookSummaryPostion.endRight;
			} else if (bookSummaryPostion.startTop < sameSummaryPostion.startTop) {
				sameSummaryPostion.startTop = bookSummaryPostion.startTop;
				sameSummaryPostion.startLeft = bookSummaryPostion.startLeft;
			}
		} else {
			mBookSummariePostions.add(bookSummaryPostion);
		}
	}

	/**
	 * 增加一项书摘
	 * 
	 * @param summary
	 */
	public void addPageSummary(PageSummary summary) {
		if (mPageSummaries == null) {
			mPageSummaries = new ArrayList<PageSummary>();
		}

		mPageSummaries.add(summary);
	}

	public List<PageSummary> getPageSummarys() {
		return mPageSummaries;
	}

	public List<ParaSelection> getSelectionList() {
		return mSelectionList;
	}

	/**
	 * 拿到本页中与新创建书签相关的书签<br>
	 * 删除它们
	 * 
	 * @return
	 */
	public ArrayList<BookSummary> findRelatePageSummaries(BookSummary newBookSummary) {
		ArrayList<BookSummary> relates = new ArrayList<BookSummary>();
		if (mPageSummaries == null) {
			return relates;
		}

		int newSummaryStart = (int) newBookSummary.getOffset();
		int newSummaryEnd = (int) (newSummaryStart + newBookSummary.getLength());
		for (PageSummary pageSummary : mPageSummaries) {
			if (newSummaryStart < pageSummary.end && newSummaryEnd > pageSummary.start) {
				relates.add(pageSummary.getBookSummary());
			}
		}
		return relates;
	}

	/**
	 * 清除书摘信息
	 */
	public void clearPageSummary() {
		mPageSummaries = null;
		mBookSummariePostions = null;
	}

	/**
	 * 增加一个选择信息
	 * 
	 * @param selection
	 */
	public void addParaSelection(ParaSelection selection) {
		if (mSelectionList == null) {
			mSelectionList = new ArrayList<ParaSelection>();
		}

		mSelectionList.add(selection);
	}

	/**
	 * 清除选择信息
	 */
	public void clearParaSelections() {
		mSelectionList = null;
	}

	/**
	 * 获取开始选择位置
	 * 
	 * @return
	 */
	public Selection getStartSelection() {
		return mStartSelection;
	}

	/**
	 * 获取结束选择位置
	 * 
	 * @return
	 */
	public Selection getEndSelection() {
		return mEndSelection;
	}

	/**
	 * 获取选中的文字对象
	 * 
	 * @return
	 */
	public SelectText getSelectText() {
		return mSelectText;
	}

	/**
	 * 清理选择位置信息
	 */
	public void clearSelection() {
		mStartSelection.clearSelection();
		mEndSelection.clearSelection();

		clearParaSelections();

		mSelectText = new SelectText();
	}

	/**
	 * 传入x,y坐标，查找该位置的字符位置
	 * 
	 * @param x
	 * @param y
	 * @param index
	 *            位置索引,取值范围 -1 <= index <= 1的整数，为-1表示长按选择；0,1表示拖动选择
	 */
	public void findSelection(float x, float y, int index) {
		if (paragraphs == null || index < PageFactory.INDEX_DEFAULT || index > PageFactory.INDEX_END) {
			return;
		}

		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph para = paragraphs.get(i);

			RectF paraRect = getParaRect(i);

			if (null != paraRect && paraRect.contains(x, y)) {
				Selection selection = para.findSelection(paraRect, x, y);
				if (selection == null) {
					continue;
				}
				selection.setParaIndex(i);

				// 计算出指定位置
				switch (index) {
				case PageFactory.INDEX_DEFAULT:
					if (Selection.DEFAULT_VALUE != selection.getSelection()) {
						mStartSelection = selection;
						mEndSelection = mStartSelection;
					}
					break;

				case PageFactory.INDEX_START:
					if (Selection.DEFAULT_VALUE != selection.getSelection()
							&& Selection.compare(mEndSelection, selection) >= 0) {
						mStartSelection = selection;
					}
					break;

				case PageFactory.INDEX_END:
					if (Selection.DEFAULT_VALUE != selection.getSelection()
							&& Selection.compare(selection, mStartSelection) >= 0) {
						mEndSelection = selection;
					}
					break;

				default:
					break;
				}

				// 分割选择位置
				breakSelection();
				return;
			}
		}
	}

	/**
	 * 获取指定位置段落所在矩形
	 * 
	 * @param index
	 *            段落索引
	 * @return The RectF of paragraph, or null
	 */
	public RectF getParaRect(int index) {
		if (index < 0 || index >= paragraphs.size()) {
			return null;
		}

		float paraStartX = mContentRectF.left;
		float paraStartY = mContentRectF.top;

		RectF paraRect = new RectF();
		paraRect.left = paraStartX;
		paraRect.top = paraStartY;
		paraRect.right = mContentRectF.right;
		paraRect.bottom = paraStartY;

		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph para = paragraphs.get(i);

			paraRect.top = paraStartY;
			paraRect.bottom += para.getHeight();

			if (index == i) {
				break;
			} else {
				paraStartY += para.getHeight() + mParaSpace;
			}
		}

		return paraRect;
	}

	/**
	 * 检查选择位置信息是否都合法
	 * 
	 * @return 是否合法
	 */
	public boolean isSelectionsLegal() {
		boolean result = false;

		int parasSize = paragraphs.size();

		int startIndex = mStartSelection.getParaIndex();
		int endIndex = mEndSelection.getParaIndex();

		if (startIndex >= 0 && startIndex < parasSize && endIndex >= 0 && endIndex < parasSize) {
			if (Selection.DEFAULT_VALUE != mStartSelection.getSelection()
					&& Selection.DEFAULT_VALUE != mEndSelection.getSelection()) {
				result = true;
			}
		}

		return result;
	}

	public void draw(Canvas canvas) {
		drawContentBg(canvas);

		if (null != mSelectionList && !mSelectionList.isEmpty()) {
			drawContentSelector(canvas);
		}

		if (null != mPageSummaries && !mPageSummaries.isEmpty()) {
			drawUnderLine(canvas);
		}

		float contentStartX = mReadStyleManager.getLeftX();
		float contentStartY = mReadStyleManager.getContentStartY();
		for (Paragraph p : paragraphs) {
			p.draw(contentStartX, contentStartY, canvas);
			contentStartY = contentStartY + p.getHeight();
		}
	}

	public float getHeight() {
		float totalHeight = 0;
		for (Paragraph p : paragraphs) {
			totalHeight = totalHeight + p.getHeight();
		}
		return totalHeight;
	}

	/**
	 * 画阅读背景
	 * 
	 * @param canvas
	 */
	private void drawContentBg(Canvas canvas) {
		if (ReadStyleManager.READ_MODE_NIGHT == mReadStyleManager.getReadMode()) {
			// 如果是夜间模式，则直接绘制夜间模式背景
			canvas.drawColor(ResourceUtil.getColor(R.color.reading_bg_night));
		} else {
			int resId = mReadStyleManager.getReadBgResId();

			if (ThemeUtil.isDrawable(resId)) {
				if (null != mReadStyleManager.getReadBackground()) {
					canvas.drawBitmap(mReadStyleManager.getReadBackground(), 0, 0, null);
				} else {
					canvas.drawColor(Color.WHITE);
				}
			} else if (ThemeUtil.isColor(resId)) {
				canvas.drawColor(ResourceUtil.getColor(resId));
			} else {
				canvas.drawColor(ResourceUtil.getColor(R.color.reading_bg));
			}
		}
	}

	/**
	 * 画出阅读内容的选择器
	 * 
	 * @param canvas
	 */
	private void drawContentSelector(Canvas canvas) {
		// 重新生成一个对象，防止原来的值影响
		if (mSelectText != null) {
			mSelectText = null;
		}
		mSelectText = new SelectText();

		int minSelectParaBegin = Integer.MAX_VALUE;
		int maxSelectParaBegin = 0;
		int maxSelectParaLength = 0;

		for (ParaSelection pSelection : mSelectionList) {
			Paragraph para = paragraphs.get(pSelection.paraIndex);
			RectF paraRect = getParaRect(pSelection.paraIndex);

			if (para instanceof TextParagraph) {
				TextParagraph txtPara = (TextParagraph) para;
				SelectText selectText = txtPara.drawContentSelector(pSelection.start, pSelection.end, canvas, paraRect);
				// TODO 下面的逻辑出错了，这里做下特殊处理先
				// if (selectText != null && mSelectionList.size() == 1 &&
				// selectText.begin == 0) {
				// mSelectText = selectText;
				// return;
				// }

				// TODO 下面的逻辑在整本书只有一个段落的时候并且选择的文本起始位置在首字时会出现错误
				if (selectText.length > 0) {
					if (selectText.begin < minSelectParaBegin) {
						minSelectParaBegin = selectText.begin;
					}
					if (selectText.begin > maxSelectParaBegin) {
						maxSelectParaBegin = selectText.begin;
						// maxSelectParaLength = selectText.length;
					}

					// TODO 最终解决方案
					if (selectText.begin >= maxSelectParaBegin) {
						maxSelectParaLength = selectText.length;
					}

					mSelectText.content += selectText.content;
				}

			}
		}

		// 赋予选择的开始位置即长度
		if (!Utils.isEmptyString(mSelectText.content)) {
			mSelectText.begin = minSelectParaBegin;
			mSelectText.length = maxSelectParaBegin - minSelectParaBegin + maxSelectParaLength;
		}
	}

	/**
	 * 画出书摘下划线
	 * 
	 * @param canvas
	 */
	private void drawUnderLine(Canvas canvas) {
		for (int i = 0; i < paragraphs.size(); i++) {
			Paragraph para = paragraphs.get(i);

			if (para instanceof TextParagraph) {
				TextParagraph txtPara = (TextParagraph) para;

				int paraBegin = txtPara.mParaBegin;
				int paraEnd = txtPara.mParaEnd;

				for (PageSummary summary : mPageSummaries) {

					if (paraBegin > summary.end || paraEnd < summary.start) {
						// 如果跟这个书摘完全没有交集，则进行下次循环
						continue;
					}

					BookSummaryPostion bookSummaryPostion = null;
					RectF paraRect = getParaRect(i);
					if (paraBegin <= summary.start) {

						if (paraEnd >= summary.end) {
							// 正好在一个段落中 (summary.start, summary.end)
							bookSummaryPostion = txtPara.drawUnderLine(summary.start, summary.end, canvas, this,
									paraRect);
						} else {
							// 前半部分在一个段落中 (summary.start, paraEnd)
							bookSummaryPostion = txtPara.drawUnderLine(summary.start, paraEnd, canvas, this, paraRect);
						}

					} else {

						if (paraEnd >= summary.end) {
							// 后半部分在一个段落中 (paraBegin, summary.end)
							bookSummaryPostion = txtPara.drawUnderLine(paraBegin, summary.end, canvas, this, paraRect);
						} else {
							// 中间部分在一个段落中 (paraBegin, paraEnd)
							bookSummaryPostion = txtPara.drawUnderLine(paraBegin, paraEnd, canvas, this, paraRect);
						}

					}
					if (bookSummaryPostion != null && bookSummaryPostion.isLegal()) {
						bookSummaryPostion.relateBookSummary = summary.getBookSummary();
						addBookSummaryPostion(bookSummaryPostion);
					}
				}

			} else {
				// 图片段落，不处理
			}
		}
	}

	/**
	 * 按照段落分割选择位置
	 */
	private void breakSelection() {
		if (!isSelectionsLegal()) {
			return;
		}

		// 首先清理掉上次的信息
		clearParaSelections();

		int startIndex = mStartSelection.getParaIndex();
		int endIndex = mEndSelection.getParaIndex();

		if (startIndex == endIndex) {
			// 一个段落内
			Paragraph para = paragraphs.get(startIndex);

			if (para instanceof TextParagraph) {
				addParaSelection(new ParaSelection(startIndex, mStartSelection, mEndSelection));
			}

		} else {
			// 分布在不同的段落间
			for (int i = startIndex; i <= endIndex; i++) {
				Paragraph para = paragraphs.get(i);
				RectF paraRect = getParaRect(startIndex);

				if (para instanceof TextParagraph) {
					TextParagraph txtPara = (TextParagraph) para;

					if (i == startIndex) {
						Selection end = txtPara.getCharSelection(paraRect, txtPara.getCharsCount(), false);

						addParaSelection(new ParaSelection(i, mStartSelection, end));
					} else if (i == endIndex) {
						Selection start = txtPara.getCharSelection(paraRect, 2, true);

						addParaSelection(new ParaSelection(i, start, mEndSelection));
					} else {
						Selection start = txtPara.getCharSelection(paraRect, 2, true);
						Selection end = txtPara.getCharSelection(paraRect, txtPara.getCharsCount(), false);

						addParaSelection(new ParaSelection(i, start, end));
					}
				} else {
					// 图片段落，不处理
				}
			}
		}

		// 为了算出选中的文字排序
		if (null != mSelectionList) {
			Collections.sort(mSelectionList, sComparatorSelection);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof PageContent) {
			PageContent pageContent = (PageContent) o;
			if (this == pageContent) {
				return true;
			}

			boolean posEquals = false;
			if (mPageBegin == pageContent.getPageBegin() && mPageEnd == pageContent.getPageEnd()) {
				posEquals = true;
			}

			boolean selectionEquals = false;
			if (this.getSelectionList() == null || this.getSelectionList().isEmpty()) {
				if (pageContent.getSelectionList() == null || pageContent.getSelectionList().isEmpty()) {
					selectionEquals = true;
				}
			} else {
				if (this.getSelectionList().equals(pageContent.getSelectionList())) {
					selectionEquals = true;
				}
			}

			boolean summaryEquals = false;
			if (this.getPageSummarys() == null || this.getPageSummarys().isEmpty()) {
				if (pageContent.getPageSummarys() == null || pageContent.getPageSummarys().isEmpty()) {
					summaryEquals = true;
				}
			} else {
				if (this.getPageSummarys().equals(pageContent.getPageSummarys())) {
					summaryEquals = true;
				}
			}

			LogUtil.d("cx", "posEquals:" + posEquals + " selectionEquals:" + selectionEquals + " summaryEquals:"
					+ summaryEquals);
			if (posEquals && selectionEquals && summaryEquals) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (mPageSummaries != null) {
			sb.append("[PageSummarys]:{");
			for (PageSummary pageSummary : mPageSummaries) {
				sb.append("\n");
				sb.append("start:").append(pageSummary.start);
				sb.append("end:").append(pageSummary.end);
			}
			sb.append("}");
		}
		return sb.toString();
	}

	/**
	 * 一页有多个书摘的情况<br>
	 * 可能会有重叠的情况，把start、end间隔开来<br>
	 * 
	 * @author Tsimle
	 * 
	 */
	public static class SummaryPos {

		public SummaryPos(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof SummaryPos) {
				SummaryPos summaryPos = (SummaryPos) o;
				if (this == summaryPos) {
					return true;
				}

				if (this.start == summaryPos.start && this.end == summaryPos.end) {
					return true;
				}
			}
			return false;
		}

		public int start;
		public int end;
	}

	/**
	 * 段落选择位置信息
	 * 
	 * @author MarkMjw
	 */
	public static class ParaSelection {
		public int paraIndex = 0;
		public Selection start;
		public Selection end;

		public ParaSelection(int index, Selection start, Selection end) {
			this.paraIndex = index;
			this.start = start;
			this.end = end;

			start.setParaIndex(index);
			end.setParaIndex(index);
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof ParaSelection) {
				ParaSelection paraSelection = (ParaSelection) o;
				if (this == paraSelection) {
					return true;
				}

				if (this.paraIndex == paraSelection.paraIndex && this.start.equals(paraSelection.start)
						&& this.end.equals(paraSelection.end)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return "ParaSelection [paraIndex=" + paraIndex + ", start=" + start + ", end=" + end + "]";
		}
	}

	/**
	 * 选择文字结构
	 * 
	 * @author MarkMjw
	 */
	public static class SelectText {
		public String content = "";
		public int begin = 0;
		public int length = 0;

		public SelectText() {

		}

		// public SelectText(String content, int begin, int length) {
		// this.content = content;
		// this.begin = begin;
		// this.length = length;
		// }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + begin;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + length;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SelectText other = (SelectText) obj;
			if (begin != other.begin)
				return false;
			if (content == null) {
				if (other.content != null)
					return false;
			} else if (!content.equals(other.content))
				return false;
			if (length != other.length)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SelectText [content=" + content + ", begin=" + begin + ", length=" + length + "]";
		}
	}
}
