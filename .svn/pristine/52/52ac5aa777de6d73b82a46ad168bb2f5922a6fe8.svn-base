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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.geometerplus.android.util.ZLog;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.fbreader.BookmarkHighlighting;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenationInfo;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;
import org.geometerplus.zlibrary.text.model.ZLTextBackgroundEntry;
import org.geometerplus.zlibrary.text.model.ZLTextCSSStyleEntry;
import org.geometerplus.zlibrary.text.model.ZLTextMark;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;
import org.geometerplus.zlibrary.text.model.ZLTextPlainModel.BackgroundItemInfo;
import org.geometerplus.zlibrary.text.model.ZLTextStyleEntry;
import org.geometerplus.zlibrary.text.model.ZLTextStyleEntry.BorderFeature;
import org.geometerplus.zlibrary.text.model.ZLTextStyleEntry.Feature;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.SparseArray;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.ThemeUtil;

public abstract class ZLTextView extends ZLTextViewBase
{
	public static final int	MAX_SELECTION_DISTANCE	= 10;

	public interface ScrollingMode
	{
		int	NO_OVERLAPPING		= 0;
		int	KEEP_LINES			= 1;
		int	SCROLL_LINES		= 2;
		int	SCROLL_PERCENTAGE	= 3;
	};

	private ZLTextModel	myModel;

	private interface SizeUnit
	{
		int	PIXEL_UNIT	= 0;
		int	LINE_UNIT	= 1;
	};

	private int												myScrollingMode;
	private int												myOverlappingValue;

	private ZLTextPage										myPreviousPage				= new ZLTextPage();
	private ZLTextPage										myCurrentPage				= new ZLTextPage();
	private ZLTextPage										myNextPage					= new ZLTextPage();

	private final HashMap<ZLTextLineInfo, ZLTextLineInfo>	myLineInfoCache				= new HashMap<ZLTextLineInfo, ZLTextLineInfo>();

	private ZLTextRegion.Soul								mySelectedRegionSoul;
	private boolean											myHighlightSelectedRegion	= true;

	private final ZLTextSelection							mySelection					= new ZLTextSelection(this);
	public final Set<ZLTextHighlighting>					myHighlightings				= Collections.synchronizedSet(new TreeSet<ZLTextHighlighting>());

	// add by zdc
	private Set<Integer>									backgroundSet				= Collections.synchronizedSet(new HashSet<Integer>());

	// end by zdc
	
	
	public void clear(){
		myPreviousPage.clear();
		myCurrentPage.clear();
		myNextPage.clear();
		
		myLineInfoCache.clear();
		myHighlightings.clear();
		backgroundSet.clear();
	}

	// public ReadStyleManager mReadStyleManager;

	public ZLTextView(ZLApplication application)
	{
		super(application);
		// mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication
		// .getTopActivity());
	}

	public synchronized void setModel(ZLTextModel model)
	{// 以epub资源为例，传入的model为ZLTextNativeModel，由底层代码负责解析出来的书籍信息都在这个对象中了
		ZLTextParagraphCursorCache.clear();

		mySelection.clear();
		myHighlightings.clear();

		myModel = model;
		myCurrentPage.reset();
		myPreviousPage.reset();
		myNextPage.reset();
		backgroundSet.clear();
		if (myModel != null) {
			final int paragraphsNumber = myModel.getParagraphsNumber();
			if (paragraphsNumber > 0) {// 传入新的数据源myModel后，定位到该Model的首段，由ZLTextParagraphCursor负责加载数据
				// add by zdc
				int startIndex = 0;
				int endIndex = myModel.getParagraphEndIndex(startIndex);
				iterateBackgroundEntryInfo(startIndex, endIndex);
				// end by zdc
				myCurrentPage.moveStartCursor(ZLTextParagraphCursor.cursor(myModel, 0));
			}
		}
		Application.getViewWidget().reset();
	}

	public ZLTextModel getModel()
	{
		return myModel;
	}

	public ZLTextWordCursor getStartCursor()
	{
		if (myCurrentPage.StartCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		return myCurrentPage.StartCursor;
	}

	public ZLTextWordCursor getEndCursor()
	{
		if (myCurrentPage.EndCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		return myCurrentPage.EndCursor;
	}

	private synchronized void gotoMark(ZLTextMark mark)
	{
		if (mark == null) {
			return;
		}

		myPreviousPage.reset();
		myNextPage.reset();
		boolean doRepaint = false;
		if (myCurrentPage.StartCursor.isNull()) {
			doRepaint = true;
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.StartCursor.isNull()) {
			return;
		}
		if (myCurrentPage.StartCursor.getParagraphIndex() != mark.ParagraphIndex || myCurrentPage.StartCursor.getMark().compareTo(mark) > 0) {
			doRepaint = true;
			gotoPosition(mark.ParagraphIndex, 0, 0);
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.EndCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		while (mark.compareTo(myCurrentPage.EndCursor.getMark()) > 0) {
			doRepaint = true;
			turnPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			preparePaintInfo(myCurrentPage);
		}
		if (doRepaint) {
			if (myCurrentPage.StartCursor.isNull()) {
				preparePaintInfo(myCurrentPage);
			}
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	public synchronized void gotoHighlighting(ZLTextHighlighting highlighting)
	{
		myPreviousPage.reset();
		myNextPage.reset();
		boolean doRepaint = false;
		if (myCurrentPage.StartCursor.isNull()) {
			doRepaint = true;
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.StartCursor.isNull()) {
			return;
		}
		if (!highlighting.intersects(myCurrentPage)) {
			gotoPosition(highlighting.getStartPosition().getParagraphIndex(), 0, 0);
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.EndCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		while (!highlighting.intersects(myCurrentPage)) {
			doRepaint = true;
			turnPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			preparePaintInfo(myCurrentPage);
		}
		if (doRepaint) {
			if (myCurrentPage.StartCursor.isNull()) {
				preparePaintInfo(myCurrentPage);
			}
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	public synchronized int search(final String text, boolean ignoreCase, boolean wholeText, boolean backward, boolean thisSectionOnly)
	{
		if (text.length() == 0) {
			return 0;
		}
		int startIndex = 0;
		int endIndex = myModel.getParagraphsNumber();
		if (thisSectionOnly) {
			// TODO: implement
		}
		int count = myModel.search(text, startIndex, endIndex, ignoreCase);
		myPreviousPage.reset();
		myNextPage.reset();
		if (!myCurrentPage.StartCursor.isNull()) {
			rebuildPaintInfo();
			if (count > 0) {
				ZLTextMark mark = myCurrentPage.StartCursor.getMark();
				gotoMark(wholeText ? (backward ? myModel.getLastMark() : myModel.getFirstMark()) : (backward ? myModel.getPreviousMark(mark) : myModel.getNextMark(mark)));
			}
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
		return count;
	}

	public boolean canFindNext()
	{
		final ZLTextWordCursor end = myCurrentPage.EndCursor;
		return !end.isNull() && (myModel != null) && (myModel.getNextMark(end.getMark()) != null);
	}

	public synchronized void findNext()
	{
		final ZLTextWordCursor end = myCurrentPage.EndCursor;
		if (!end.isNull()) {
			gotoMark(myModel.getNextMark(end.getMark()));
		}
	}

	public boolean canFindPrevious()
	{
		final ZLTextWordCursor start = myCurrentPage.StartCursor;
		return !start.isNull() && (myModel != null) && (myModel.getPreviousMark(start.getMark()) != null);
	}

	public synchronized void findPrevious()
	{
		final ZLTextWordCursor start = myCurrentPage.StartCursor;
		if (!start.isNull()) {
			gotoMark(myModel.getPreviousMark(start.getMark()));
		}
	}

	public void clearFindResults()
	{
		if (!findResultsAreEmpty()) {
			myModel.removeAllMarks();
			rebuildPaintInfo();
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	public boolean findResultsAreEmpty()
	{
		return myModel == null || myModel.getMarks().isEmpty();
	}

	@Override
	public synchronized void onScrollingFinished(PageIndex pageIndex)
	{

		ZLog.i(ZLog.ZLTextView, "onScrollingFinished >> pageIndex = " + pageIndex);

		switch (pageIndex) {
		case current:
			break;
		case previous: {
			final ZLTextPage swap = myNextPage;
			myNextPage = myCurrentPage;
			myCurrentPage = myPreviousPage;
			myPreviousPage = swap;
			myPreviousPage.reset();
			if (myCurrentPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
				preparePaintInfo(myNextPage);
				myCurrentPage.EndCursor.setCursor(myNextPage.StartCursor);
				myCurrentPage.PaintState = PaintStateEnum.END_IS_KNOWN;
			} else if (!myCurrentPage.EndCursor.isNull() && !myNextPage.StartCursor.isNull() && !myCurrentPage.EndCursor.samePositionAs(myNextPage.StartCursor)) {
				myNextPage.reset();
				myNextPage.StartCursor.setCursor(myCurrentPage.EndCursor);
				myNextPage.PaintState = PaintStateEnum.START_IS_KNOWN;
				Application.getViewWidget().reset();
			}
			break;
		}
		case next: {
			final ZLTextPage swap = myPreviousPage;
			myPreviousPage = myCurrentPage;
			myCurrentPage = myNextPage;
			myNextPage = swap;
			myNextPage.reset();
			switch (myCurrentPage.PaintState) {
			case PaintStateEnum.NOTHING_TO_PAINT:
				preparePaintInfo(myPreviousPage);
				myCurrentPage.StartCursor.setCursor(myPreviousPage.EndCursor);
				myCurrentPage.PaintState = PaintStateEnum.START_IS_KNOWN;
				break;
			case PaintStateEnum.READY:
				myNextPage.StartCursor.setCursor(myCurrentPage.EndCursor);
				myNextPage.PaintState = PaintStateEnum.START_IS_KNOWN;
				break;
			}
			break;
		}
		}
	}

	public boolean removeHighlightings(Class<? extends ZLTextHighlighting> type)
	{
		boolean result = false;
		synchronized (myHighlightings) {
			for (Iterator<ZLTextHighlighting> it = myHighlightings.iterator(); it.hasNext();) {
				final ZLTextHighlighting h = it.next();
				if (type.isInstance(h)) {
					it.remove();
					result = true;
				}
			}
		}
		return result;
	}

	public void highlight(ZLTextPosition start, ZLTextPosition end)
	{
		removeHighlightings(ZLTextManualHighlighting.class);
		addHighlighting(new ZLTextManualHighlighting(this, start, end));
	}

	public final void addHighlighting(ZLTextHighlighting h)
	{
		myHighlightings.add(h);
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
	}

	public final void addHighlightings(Collection<ZLTextHighlighting> hilites)
	{
		myHighlightings.addAll(hilites);
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
	}

	public void clearHighlighting()
	{
		if (removeHighlightings(ZLTextManualHighlighting.class)) {
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	protected void moveSelectionCursorTo(ZLTextSelectionCursor cursor, int x, int y)
	{
		y -= ZLTextSelectionCursor.getHeight() / 2 + ZLTextSelectionCursor.getAccent() / 2;
		mySelection.setCursorInMovement(cursor, x, y);
		mySelection.expandTo(myCurrentPage, x, y);
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
	}

	protected void releaseSelectionCursor()
	{
		mySelection.stop();
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
	}

	protected ZLTextSelectionCursor getSelectionCursorInMovement()
	{
		return mySelection.getCursorInMovement();
	}

	private ZLTextSelection.Point getSelectionCursorPoint(ZLTextPage page, ZLTextSelectionCursor cursor)
	{
		if (cursor == ZLTextSelectionCursor.None) {
			return null;
		}

		if (cursor == mySelection.getCursorInMovement()) {
			return mySelection.getCursorInMovementPoint();
		}

		if (cursor == ZLTextSelectionCursor.Left) {
			if (mySelection.hasPartBeforePage(page)) {
				return null;
			}
			final ZLTextElementArea selectionStartArea = mySelection.getStartArea(page);
			if (selectionStartArea != null) {
				return new ZLTextSelection.Point(selectionStartArea.XStart, selectionStartArea.YEnd);
			}
		} else {
			if (mySelection.hasPartAfterPage(page)) {
				return null;
			}
			final ZLTextElementArea selectionEndArea = mySelection.getEndArea(page);
			if (selectionEndArea != null) {
				return new ZLTextSelection.Point(selectionEndArea.XEnd, selectionEndArea.YEnd);
			}
		}
		return null;
	}

	private int distanceToCursor(int x, int y, ZLTextSelection.Point cursorPoint)
	{
		if (cursorPoint == null) {
			return Integer.MAX_VALUE;
		}

		final int dX, dY;

		final int w = ZLTextSelectionCursor.getWidth() / 2;
		if (x < cursorPoint.X - w) {
			dX = cursorPoint.X - w - x;
		} else if (x > cursorPoint.X + w) {
			dX = x - cursorPoint.X - w;
		} else {
			dX = 0;
		}

		final int h = ZLTextSelectionCursor.getHeight();
		if (y < cursorPoint.Y) {
			dY = cursorPoint.Y - y;
		} else if (y > cursorPoint.Y + h) {
			dY = y - cursorPoint.Y - h;
		} else {
			dY = 0;
		}

		return Math.max(dX, dY);
	}

	protected ZLTextSelectionCursor findSelectionCursor(int x, int y)
	{
		return findSelectionCursor(x, y, Integer.MAX_VALUE);
	}

	protected ZLTextSelectionCursor findSelectionCursor(int x, int y, int maxDistance)
	{
		if (mySelection.isEmpty()) {
			return ZLTextSelectionCursor.None;
		}

		final int leftDistance = distanceToCursor(x, y, getSelectionCursorPoint(myCurrentPage, ZLTextSelectionCursor.Left));
		final int rightDistance = distanceToCursor(x, y, getSelectionCursorPoint(myCurrentPage, ZLTextSelectionCursor.Right));

		if (rightDistance < leftDistance) {
			return rightDistance <= maxDistance ? ZLTextSelectionCursor.Right : ZLTextSelectionCursor.None;
		} else {
			return leftDistance <= maxDistance ? ZLTextSelectionCursor.Left : ZLTextSelectionCursor.None;
		}
	}

	private void drawSelectionCursor(ZLPaintContext context, ZLTextSelection.Point pt)
	{
		if (pt == null) {
			return;
		}

		final int w = ZLTextSelectionCursor.getWidth() / 2;
		final int h = ZLTextSelectionCursor.getHeight();
		final int a = ZLTextSelectionCursor.getAccent();
		final int[] xs = { pt.X, pt.X + w, pt.X + w, pt.X - w, pt.X - w };
		final int[] ys = { pt.Y - a, pt.Y, pt.Y + h, pt.Y + h, pt.Y };
		context.setFillColor(context.getBackgroundColor(), 192);
		context.fillPolygon(xs, ys);
		// FIXME:ouyang
		// int color = mReadStyleManager.mReadTextColor;
		// ZLColor fgColor = new ZLColor(color);
		// context.setLineColor(fgColor);
		context.setLineColor(getTextColor(ZLTextHyperlink.NO_LINK));

		context.drawPolygonalLine(xs, ys);
	}

	@Override
	public synchronized void preparePage(ZLPaintContext context, PageIndex pageIndex)
	{
		setContext(context);
		preparePaintInfo(getPage(pageIndex));
	}

	/**
	 * 真正的绘制方法
	 */
	@Override
	public synchronized void paint(ZLPaintContext context, PageIndex pageIndex)
	{
		setContext(context);
		// 设置背景颜色

//		ReadStyleManager mReadStyleManager = ReadStyleManager.getInstance(SinaBookApplication.getTopActivity());
//		// Log.i("ouyang",
//		// "ZLTextView--paint------mReadStyleManager:"+mReadStyleManager);
//
//		if (ReadStyleManager.READ_MODE_NIGHT == mReadStyleManager.getReadMode()) {
//			// 夜间
//			ZLColor myValue = new ZLColor(ResourceUtil.getColor(R.color.reading_bg_night));
//			context.clear(myValue);
//			// context.clear(getBackgroundColor());
//		} else {
//			int resId = mReadStyleManager.getReadBgResId();
//			if (ThemeUtil.isDrawable(resId)) {
//				if (null != mReadStyleManager.getReadBackground()) {
//					context.clear(mReadStyleManager.getReadBackground());
//				} else {
//					ZLColor myValue = new ZLColor(Color.WHITE);
//					context.clear(myValue);
//				}
//			} else if (ThemeUtil.isColor(resId)) {
//				ZLColor myValue = new ZLColor(ResourceUtil.getColor(resId));
//				context.clear(myValue);
//			} else {
//				ZLColor myValue = new ZLColor(ResourceUtil.getColor(R.color.reading_bg));
//				context.clear(myValue);
//			}
//		}
		
		ZLColor myValue = new ZLColor(ResourceUtil.getColor(R.color.reading_bg_fb));//暂时屏蔽背景切换以及夜间模式，统一使用纯色绘制
		context.clear(myValue);
		// final ZLFile wallpaper = getWallpaperFile();
		// if (wallpaper != null) {
		// context.clear(wallpaper, getFillMode());
		// } else {
		// context.clear(getBackgroundColor());
		// }

		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return;
		}

		// TODO:ouyang
		ZLTextPage page;
		switch (pageIndex) {
		default:
		case current:// 当前页
			page = myCurrentPage;
			break;
		case previous:// 上一页
			// myBgRectLinkedMap.clear();
			// myBgSpanLinkedList.clear();
			// myElementIndexsWithParagraph.clear();

			page = myPreviousPage;
			// 如果上一页的绘制状态为NOTHING_TO_PAINT，则需要先准备myCurrentPage的数据
			if (myPreviousPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
				preparePaintInfo(myCurrentPage);
				myPreviousPage.EndCursor.setCursor(myCurrentPage.StartCursor);
				myPreviousPage.PaintState = PaintStateEnum.END_IS_KNOWN;
			}
			break;
		case next:// 下一页
			// myBgRectLinkedMap.clear();
			// myBgSpanLinkedList.clear();
			// myElementIndexsWithParagraph.clear();

			page = myNextPage;
			// 如果下一页的绘制状态为NOTHING_TO_PAINT，则需要先准备myCurrentPage的数据
			if (myNextPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
				preparePaintInfo(myCurrentPage);
				myNextPage.StartCursor.setCursor(myCurrentPage.EndCursor);
				myNextPage.PaintState = PaintStateEnum.START_IS_KNOWN;
			}
		}
		// 清除一些变量，如是否有书签存在，这一页的所有文本元素集的数据
		page.setHasBookmark(false);
		page.TextElementMap.clear();

		// 准备页数据
		// 上面的page已经声明，接下来便是初始化了
		// IMPORTANT(此方法内的代码相当重要，是构建整页信息的关键所在)
		preparePaintInfo(page);

		// 如果页首字或者页尾字都没有，那就不用绘制了
		if (page.StartCursor.isNull() || page.EndCursor.isNull()) {
			return;
		}

		// 生成各个Element的坐标，然后paint
		final ArrayList<ZLTextLineInfo> lineInfos = page.LineInfos;
		final int[] labels = new int[lineInfos.size() + 1];
		int x = getLeftMargin();
		int y = getTopMargin();
		int index = 0;
		ZLTextLineInfo previousInfo = null;
		for (ZLTextLineInfo info : lineInfos) {
			info.adjust(previousInfo);
			prepareTextLine(page, info, x, y);// 算page.TextElementMap元素
			y += info.Height + info.Descent + info.VSpaceAfter;
			labels[++index] = page.TextElementMap.size();
			if (index == page.Column0Height) {
				y = getTopMargin();
				x += page.getTextWidth() + getSpaceBetweenColumns();
			}
			previousInfo = info;
		}

		// 绘制高亮
		x = getLeftMargin();
		y = getTopMargin();
		index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			drawHighlightings(page, info, labels[index], labels[index + 1], x, y, pageIndex);
			y += info.Height + info.Descent + info.VSpaceAfter;
			++index;
			if (index == page.Column0Height) {
				y = getTopMargin();
				x += page.getTextWidth() + getSpaceBetweenColumns();
			}
		}

		// 绘制背景
		myBgRectLinkedMap.clear();
		myBgSpanLinkedList.clear();
		myElementIndexsWithParagraph.clear();

		int firstLineParagraphIndex = -1;
		int lastLineParagraphIndex = -1;
		if (lineInfos.size() > 0) {
			firstLineParagraphIndex = lineInfos.get(0).ParagraphCursor.Index;
			lastLineParagraphIndex = lineInfos.get(lineInfos.size() - 1).ParagraphCursor.Index;
		}
		index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			computeBackgroundInfo(page, info, labels[index], labels[index + 1], firstLineParagraphIndex, lastLineParagraphIndex);
			++index;
		}
		for (Entry<BackgroundItemInfo, BackgroundDivInfo> entry : myBgRectLinkedMap.entrySet()) {
			BackgroundItemInfo myItemInfo = entry.getKey();
			BackgroundDivInfo myRectInfo = entry.getValue();
			if (myItemInfo.isBlock) {
				if (myRectInfo.isAvaliable()) {
					context.setFillColor(myRectInfo.myColor);
					Rect myRect = myRectInfo.drawRect();
					context.fillRectangle(myRect.left, myRect.top, myRect.right, myRect.bottom);
					ZLog.e(ZLog.ZLCSS_DIV_BG, "DIV (left, top, right, bottom) -> (" + myRect.left + ", " + myRect.top + ", " + myRect.right + ", " + myRect.bottom + ")");

					BorderValue myBorderValue = myBordersLinkedMap.get(myItemInfo);
					if (myRectInfo.isLeftBorderAvaliable() && myBorderValue.left != 0) {
						context.setFillColor(myRectInfo.myLeftBorderColor);
						int[][] leftBorderPoints = myRectInfo.leftBorderPoints(myBorderValue);
						context.fillPolygon(leftBorderPoints[0], leftBorderPoints[1]);
					}

					if (myRectInfo.isTopBorderAvaliable() && myBorderValue.top != 0) {
						context.setFillColor(myRectInfo.myTopBorderColor);
						int[][] topBorderPoints = myRectInfo.topBorderPoints(myBorderValue);
						context.fillPolygon(topBorderPoints[0], topBorderPoints[1]);
					}

					if (myRectInfo.isRightBorderAvaliable() && myBorderValue.right != 0) {
						context.setFillColor(myRectInfo.myRightBorderColor);
						int[][] rightBorderPoints = myRectInfo.rightBorderPoints(myBorderValue);
						context.fillPolygon(rightBorderPoints[0], rightBorderPoints[1]);
					}

					if (myRectInfo.isBottomBorderAvaliable() && myBorderValue.bottom != 0) {
						context.setFillColor(myRectInfo.myBottomBorderColor);
						int[][] bottomBorderPoints = myRectInfo.bottomBorderPoints(myBorderValue);
						context.fillPolygon(bottomBorderPoints[0], bottomBorderPoints[1]);
					}
				}
			}
		}
		for (BackgroundSpanInfo mySpanItem : myBgSpanLinkedList) {
			if (mySpanItem.isBgAvaliable()) {
				context.setFillColor(mySpanItem.myColor);
				Rect myRect = mySpanItem.drawRect();
				context.fillRectangle(myRect.left, myRect.top, myRect.right, myRect.bottom);
				ZLog.e(ZLog.ZLCSS_DIV_BG, "Span (left, top, right, bottom) -> (" + myRect.left + ", " + myRect.top + ", " + myRect.right + ", " + myRect.bottom + ")");
			}

			if (mySpanItem.isLeftBorderAvaliable()) {
				context.setFillColor(mySpanItem.myLeftBorderColor);
				Rect myRect = mySpanItem.leftBorderRect();
				context.fillRectangle(myRect.left, myRect.top, myRect.right, myRect.bottom);
			}

			if (mySpanItem.isTopBorderAvaliable()) {
				context.setFillColor(mySpanItem.myTopBorderColor);
				Rect myRect = mySpanItem.topBorderRect();
				context.fillRectangle(myRect.left, myRect.top, myRect.right, myRect.bottom);
			}

			if (mySpanItem.isRightBorderAvaliable()) {
				context.setFillColor(mySpanItem.myRightBorderColor);
				Rect myRect = mySpanItem.rightBorderRect();
				context.fillRectangle(myRect.left, myRect.top, myRect.right, myRect.bottom);
			}

			if (mySpanItem.isBottomBorderAvaliable()) {
				context.setFillColor(mySpanItem.myBottomBorderColor);
				Rect myRect = mySpanItem.bottomBorderRect();
				context.fillRectangle(myRect.left, myRect.top, myRect.right, myRect.bottom);
			}
		}

		// 绘制文字
		x = getLeftMargin();
		y = getTopMargin();
		index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			drawTextLine(page, info, labels[index], labels[index + 1]);
			y += info.Height + info.Descent + info.VSpaceAfter;
			++index;
			if (index == page.Column0Height) {
				y = getTopMargin();
				x += page.getTextWidth() + getSpaceBetweenColumns();
			}
		}

		// // 绘制被选择的文字的区域
		// final ZLTextRegion selectedElementRegion = getSelectedRegion(page);
		// if (selectedElementRegion != null && myHighlightSelectedRegion) {
		// selectedElementRegion.draw(context);
		// }

		// // 绘制选择文字时左右两个拖动柄
		// drawSelectionCursor(context,
		// getSelectionCursorPoint(page, ZLTextSelectionCursor.Left));
		// drawSelectionCursor(context,
		// getSelectionCursorPoint(page, ZLTextSelectionCursor.Right));
	}

	/**
	 * 存储DIV等区块的背景绘制区域数据
	 */
	private LinkedHashMap<BackgroundItemInfo, BackgroundDivInfo>	myBgRectLinkedMap				= new LinkedHashMap<BackgroundItemInfo, ZLTextView.BackgroundDivInfo>();
	private LinkedHashMap<BackgroundItemInfo, BorderValue>			myBordersLinkedMap				= new LinkedHashMap<BackgroundItemInfo, ZLTextView.BorderValue>();
	/**
	 * 存储SPAN等段内区块的背景绘制区域数据
	 */
	private LinkedList<BackgroundSpanInfo>							myBgSpanLinkedList				= new LinkedList<ZLTextView.BackgroundSpanInfo>();
	/**
	 * Key：段落索引，Value：所有背景区块中起始段落相同的所有起始元素索引信息
	 */
	SparseArray<ElementIndexs>										myElementIndexsWithParagraph	= new SparseArray<ZLTextView.ElementIndexs>();

	private static class BorderValue
	{
		int	top;
		int	right;
		int	bottom;
		int	left;
	}

	/**
	 * DIV等区块的背景绘制区域数据
	 * 
	 * @author chenjl
	 * 
	 */
	private class BackgroundDivInfo
	{
		Point	myStartPoint;
		Point	myEndPoint;
		ZLColor	myColor;
		int		myRectWidth;

		byte	myKind;

		ZLColor	myLeftBorderColor;
		ZLColor	myTopBorderColor;
		ZLColor	myRightBorderColor;
		ZLColor	myBottomBorderColor;

		int		myLeftBorderWidth;
		int		myTopBorderHeight;
		int		myRightBorderWidth;
		int		myBottomBorderHeight;

		Rect	myDrawRect;

		public boolean isAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BORDER && (myStartPoint != null && myEndPoint != null);
		}

		public boolean isLeftBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND;// &&
																		// myLeftBorderWidth
																		// != 0;
		}

		public boolean isTopBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND;// &&
																		// myTopBorderHeight
																		// != 0;
		}

		public boolean isRightBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND;// &&
																		// myRightBorderWidth
																		// != 0;
		}

		public boolean isBottomBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND;// &&
																		// myBottomBorderHeight
																		// != 0;
		}

		public Rect drawRect()
		{
			if (myDrawRect == null && myStartPoint != null && myEndPoint != null) {
				myDrawRect = new Rect(myStartPoint.x, myStartPoint.y, myRectWidth, myEndPoint.y);
			}
			return myDrawRect;
		}

		/**
		 * 纠正下可能超出绘制边界的坐标值
		 * 
		 * @param points
		 */
		private void correctCorrdinate(int[][] points)
		{
			int minXLeft = getLeftMargin();
			int maxXRight = minXLeft + getTextColumnWidth();
			for (int j = 0; j < points[0].length; j++) {
				if (j == 0 || j == 3) {
					if (points[0][j] < minXLeft) {
						points[0][j] = minXLeft;
					}
				}
				if (j == 1 || j == 2) {
					if (points[0][j] > maxXRight) {
						points[0][j] = maxXRight;
					}
				}
			}
			int minYTop = getTopMargin();
			int maxYBottom = minYTop + getTextAreaHeight();
			for (int j = 0; j < points[1].length; j++) {
				if (j == 0 || j == 1) {
					if (points[1][j] < minYTop) {
						points[1][j] = minYTop;
					}
				}
				if (j == 2 || j == 3) {
					if (points[1][j] > maxYBottom) {
						points[1][j] = maxYBottom;
					}
				}
			}
		}

		public int[][] leftBorderPoints(BorderValue myBorderValue)
		{
			drawRect();
			int[][] points = new int[2][4];
			int left = myDrawRect.left;
			int top = myDrawRect.top;
			int bottom = myDrawRect.bottom;
			points[0] = new int[] { left - myBorderValue.left, left, left, left - myBorderValue.left };
			points[1] = new int[] { top - myBorderValue.top, top, bottom, bottom + myBorderValue.bottom };
			correctCorrdinate(points);
			return points;
		}

		public int[][] topBorderPoints(BorderValue myBorderValue)
		{
			drawRect();
			int[][] points = new int[2][4];
			int left = myDrawRect.left;
			int right = myDrawRect.right;
			int top = myDrawRect.top;
			points[0] = new int[] { left - myBorderValue.left, right + myBorderValue.right, right, left };
			points[1] = new int[] { top - myBorderValue.top, top - myBorderValue.top, top, top };
			correctCorrdinate(points);
			return points;
		}

		public int[][] rightBorderPoints(BorderValue myBorderValue)
		{
			drawRect();
			int[][] points = new int[2][4];
			int right = myDrawRect.right;
			int top = myDrawRect.top;
			int bottom = myDrawRect.bottom;
			points[0] = new int[] { right, right + myBorderValue.right, right + myBorderValue.right, right };
			points[1] = new int[] { top, top - myBorderValue.top, bottom + myBorderValue.bottom, bottom };
			correctCorrdinate(points);
			return points;
		}

		public int[][] bottomBorderPoints(BorderValue myBorderValue)
		{
			drawRect();
			int[][] points = new int[2][4];
			int left = myDrawRect.left;
			int right = myDrawRect.right;
			int bottom = myDrawRect.bottom;
			points[0] = new int[] { left, right, right + myBorderValue.right, left - myBorderValue.left };
			points[1] = new int[] { bottom, bottom, bottom + myBorderValue.bottom, bottom + myBorderValue.bottom };
			correctCorrdinate(points);
			return points;
		}

	}

	/**
	 * SPAN等段内区块的背景绘制区域数据
	 * 
	 * @author chenjl
	 * 
	 */
	private class BackgroundSpanInfo
	{

		public BackgroundSpanInfo(ZLTextElementArea myStartArea, ZLTextElementArea myEndArea, ZLColor myColor)
		{
			this.myStartArea = myStartArea;
			this.myEndArea = myEndArea;
			this.myColor = myColor;
		}

		ZLTextElementArea	myStartArea;
		ZLTextElementArea	myEndArea;
		ZLColor				myColor;

		byte				myKind;

		ZLColor				myLeftBorderColor;
		int					myLeftBorderWidth;

		ZLColor				myTopBorderColor;
		int					myTopBorderHeight;

		ZLColor				myRightBorderColor;
		int					myRightBorderWidth;

		ZLColor				myBottomBorderColor;
		int					myBottomBorderHeight;

		public boolean isBgAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BORDER;
		}

		public Rect drawRect()
		{
			int left = 0, top = 0, right = 0, bottom = 0;
			left = myStartArea.XStart;
			top = Math.min(myStartArea.YStart, myEndArea.YStart);
			right = myEndArea.XEnd;
			bottom = Math.max(myStartArea.YEnd, myEndArea.YEnd);
			return new Rect(left, top, right, bottom);
		}

		public boolean isLeftBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND && myLeftBorderWidth > 0;
		}

		public boolean isTopBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND && myTopBorderHeight > 0;
		}

		public boolean isRightBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND && myRightBorderWidth > 0;
		}

		public boolean isBottomBorderAvaliable()
		{
			return myKind != ZLTextBackgroundEntry.BG_KIND_BACKGROUND && myBottomBorderHeight > 0;
		}

		public Rect leftBorderRect()
		{
			Rect rect = drawRect();
			rect.left -= myLeftBorderWidth;
			return rect;
		}

		public Rect topBorderRect()
		{
			Rect rect = drawRect();
			rect.top -= myTopBorderHeight;
			if (myLeftBorderWidth > 0) {
				rect.left -= myLeftBorderWidth;
			}
			if (myRightBorderWidth > 0) {
				rect.right += myRightBorderWidth;
			}
			return rect;
		}

		public Rect rightBorderRect()
		{
			Rect rect = drawRect();
			rect.right += myRightBorderWidth;
			return rect;
		}

		public Rect bottomBorderRect()
		{
			Rect rect = drawRect();
			rect.bottom += myBottomBorderHeight;
			if (myLeftBorderWidth > 0) {
				rect.left -= myLeftBorderWidth;
			}
			if (myRightBorderWidth > 0) {
				rect.right += myRightBorderWidth;
			}
			return rect;
		}
	}

	public ZLTextPage getPage(PageIndex pageIndex)
	{
		switch (pageIndex) {
		default:
		case current:
			return myCurrentPage;
		case previous:
			return myPreviousPage;
		case next:
			return myNextPage;
		}
	}

	public static final int	SCROLLBAR_HIDE				= 0;
	public static final int	SCROLLBAR_SHOW				= 1;
	public static final int	SCROLLBAR_SHOW_AS_PROGRESS	= 2;

	public abstract int scrollbarType();

	@Override
	public final boolean isScrollbarShown()
	{
		return scrollbarType() == SCROLLBAR_SHOW || scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS;
	}

	protected final synchronized int sizeOfTextBeforeParagraph(int paragraphIndex)
	{
		return myModel != null ? myModel.getTextLength(paragraphIndex - 1) : 0;
	}

	protected final synchronized int sizeOfFullText()
	{
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 1;
		}
		return myModel.getTextLength(myModel.getParagraphsNumber() - 1);
	}

	private final synchronized int getCurrentCharNumber(PageIndex pageIndex, boolean startNotEndOfPage)
	{
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 0;
		}
		final ZLTextPage page = getPage(pageIndex);
		preparePaintInfo(page);
		if (startNotEndOfPage) {
			return Math.max(0, sizeOfTextBeforeCursor(page.StartCursor));
		} else {
			int end = sizeOfTextBeforeCursor(page.EndCursor);
			if (end == -1) {
				end = myModel.getTextLength(myModel.getParagraphsNumber() - 1) - 1;
			}
			return Math.max(1, end);
		}
	}

	@Override
	public final synchronized int getScrollbarFullSize()
	{
		return sizeOfFullText();
	}

	@Override
	public final synchronized int getScrollbarThumbPosition(PageIndex pageIndex)
	{
		return scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS ? 0 : getCurrentCharNumber(pageIndex, true);
	}

	@Override
	public final synchronized int getScrollbarThumbLength(PageIndex pageIndex)
	{
		int start = scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS ? 0 : getCurrentCharNumber(pageIndex, true);
		int end = getCurrentCharNumber(pageIndex, false);
		return Math.max(1, end - start);
	}

	private int sizeOfTextBeforeCursor(ZLTextWordCursor wordCursor)
	{
		final ZLTextParagraphCursor paragraphCursor = wordCursor.getParagraphCursor();
		if (paragraphCursor == null) {
			return -1;
		}
		final int paragraphIndex = paragraphCursor.Index;
		int sizeOfText = myModel.getTextLength(paragraphIndex - 1);
		final int paragraphLength = paragraphCursor.getParagraphLength();
		if (paragraphLength > 0) {
			sizeOfText += (myModel.getTextLength(paragraphIndex) - sizeOfText) * wordCursor.getElementIndex() / paragraphLength;
		}
		return sizeOfText;
	}

	// Can be called only when (myModel.getParagraphsNumber() != 0)
	private synchronized float computeCharsPerPage()
	{
		setTextStyle(getTextStyleCollection().getBaseStyle());

		final int textWidth = getTextColumnWidth();
		final int textHeight = getTextAreaHeight();

		final int num = myModel.getParagraphsNumber();
		final int totalTextSize = myModel.getTextLength(num - 1);
		final float charsPerParagraph = ((float) totalTextSize) / num;

		final float charWidth = computeCharWidth();

		final int indentWidth = getElementWidth(ZLTextElement.Indent, 0);
		final float effectiveWidth = textWidth - (indentWidth + 0.5f * textWidth) / charsPerParagraph;
		float charsPerLine = Math.min(effectiveWidth / charWidth, charsPerParagraph * 1.2f);

		final int strHeight = getWordHeight() + getContext().getDescent();
		final int effectiveHeight = (int) (textHeight - (getTextStyle().getSpaceBefore(metrics()) + getTextStyle().getSpaceAfter(metrics()) / 2) / charsPerParagraph);
		final int linesPerPage = effectiveHeight / strHeight;

		return charsPerLine * linesPerPage;
	}

	private synchronized int computeTextPageNumber(int textSize)
	{
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 1;
		}

		final float factor = 1.0f / computeCharsPerPage();
		final float pages = textSize * factor;
		return Math.max((int) (pages + 1.0f - 0.5f * factor), 1);
	}

	private static final char[]	ourDefaultLetters		= "System developers have used modeling languages for decades to specify, visualize, construct, and document systems. The Unified Modeling Language (UML) is one of those languages. UML makes it possible for team members to collaborate by providing a common language that applies to a multitude of different systems. Essentially, it enables you to communicate solutions in a consistent, tool-supported language."
																.toCharArray();

	private final char[]		myLettersBuffer			= new char[512];
	private int					myLettersBufferLength	= 0;
	private ZLTextModel			myLettersModel			= null;
	private float				myCharWidth				= -1f;

	private final float computeCharWidth()
	{
		if (myLettersModel != myModel) {
			myLettersModel = myModel;
			myLettersBufferLength = 0;
			myCharWidth = -1f;

			int paragraph = 0;
			final int textSize = myModel.getTextLength(myModel.getParagraphsNumber() - 1);
			if (textSize > myLettersBuffer.length) {
				paragraph = myModel.findParagraphByTextLength((textSize - myLettersBuffer.length) / 2);
			}
			while (paragraph < myModel.getParagraphsNumber() && myLettersBufferLength < myLettersBuffer.length) {
				final ZLTextParagraph.EntryIterator it = myModel.getParagraph(paragraph++).iterator();
				while (myLettersBufferLength < myLettersBuffer.length && it.next()) {
					if (it.getType() == ZLTextParagraph.Entry.TEXT) {
						final int len = Math.min(it.getTextLength(), myLettersBuffer.length - myLettersBufferLength);
						System.arraycopy(it.getTextData(), it.getTextOffset(), myLettersBuffer, myLettersBufferLength, len);
						myLettersBufferLength += len;
					}
				}
			}

			if (myLettersBufferLength == 0) {
				myLettersBufferLength = Math.min(myLettersBuffer.length, ourDefaultLetters.length);
				System.arraycopy(ourDefaultLetters, 0, myLettersBuffer, 0, myLettersBufferLength);
			}
		}

		if (myCharWidth < 0f) {
			myCharWidth = computeCharWidth(myLettersBuffer, myLettersBufferLength);
		}
		return myCharWidth;
	}

	private final float computeCharWidth(char[] pattern, int length)
	{
		return getContext().getStringWidth(pattern, 0, length) / ((float) length);
	}

	public static class PagePosition
	{
		public final int	Current;
		public final int	Total;

		PagePosition(int current, int total)
		{
			Current = current;
			Total = total;
		}
	}

	public final synchronized PagePosition pagePosition()
	{
		int current = computeTextPageNumber(getCurrentCharNumber(PageIndex.current, false));
		int total = computeTextPageNumber(sizeOfFullText());

		if (total > 3) {
			return new PagePosition(current, total);
		}

		preparePaintInfo(myCurrentPage);
		ZLTextWordCursor cursor = myCurrentPage.StartCursor;
		if (cursor == null || cursor.isNull()) {
			return new PagePosition(current, total);
		}

		if (cursor.isStartOfText()) {
			current = 1;
		} else {
			ZLTextWordCursor prevCursor = myPreviousPage.StartCursor;
			if (prevCursor == null || prevCursor.isNull()) {
				preparePaintInfo(myPreviousPage);
				prevCursor = myPreviousPage.StartCursor;
			}
			if (prevCursor != null && !prevCursor.isNull()) {
				current = prevCursor.isStartOfText() ? 2 : 3;
			}
		}

		total = current;
		cursor = myCurrentPage.EndCursor;
		if (cursor == null || cursor.isNull()) {
			return new PagePosition(current, total);
		}
		if (!cursor.isEndOfText()) {
			ZLTextWordCursor nextCursor = myNextPage.EndCursor;
			if (nextCursor == null || nextCursor.isNull()) {
				preparePaintInfo(myNextPage);
				nextCursor = myNextPage.EndCursor;
			}
			if (nextCursor != null) {
				total += nextCursor.isEndOfText() ? 1 : 2;
			}
		}

		return new PagePosition(current, total);
	}

	public final RationalNumber getProgress()
	{
		final PagePosition position = pagePosition();
		return RationalNumber.create(position.Current, position.Total);
	}

	public final synchronized void gotoPage(int page)
	{
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return;
		}

		final float factor = computeCharsPerPage();
		final float textSize = page * factor;

		int intTextSize = (int) textSize;
		int paragraphIndex = myModel.findParagraphByTextLength(intTextSize);

		if (paragraphIndex > 0 && myModel.getTextLength(paragraphIndex) > intTextSize) {
			--paragraphIndex;
		}
		intTextSize = myModel.getTextLength(paragraphIndex);

		int sizeOfTextBefore = myModel.getTextLength(paragraphIndex - 1);
		while (paragraphIndex > 0 && intTextSize == sizeOfTextBefore) {
			--paragraphIndex;
			intTextSize = sizeOfTextBefore;
			sizeOfTextBefore = myModel.getTextLength(paragraphIndex - 1);
		}

		final int paragraphLength = intTextSize - sizeOfTextBefore;

		final int wordIndex;
		if (paragraphLength == 0) {
			wordIndex = 0;
		} else {
			preparePaintInfo(myCurrentPage);
			final ZLTextWordCursor cursor = new ZLTextWordCursor(myCurrentPage.EndCursor);
			cursor.moveToParagraph(paragraphIndex);
			wordIndex = cursor.getParagraphCursor().getParagraphLength();
		}

		gotoPositionByEnd(paragraphIndex, wordIndex, 0);
	}

	public void gotoHome()
	{
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphIndex() == 0) {
			return;
		}
		gotoPosition(0, 0, 0);
		preparePaintInfo();
	}

	public boolean	isFlag					= false;
	private boolean	deleteAllBookmarkFlag	= false;

	public void setDeleteAllBookmarkFlag(boolean deleteAllBookmarkFlag)
	{
		this.deleteAllBookmarkFlag = deleteAllBookmarkFlag;
		// ZLog.d(ZLog.ZLTextView,
		// "setDeleteAllBookmarkFlag >> deleteAllBookmarkFlag = "
		// + deleteAllBookmarkFlag);
	}

	public void deleteAllBookmark()
	{
		setDeleteAllBookmarkFlag(true);
	}

	/**
	 * 绘制高亮(书签的背景)
	 * 
	 * @param page
	 * @param info
	 * @param from
	 * @param to
	 * @param x
	 * @param y
	 */
	private void drawHighlightings(ZLTextPage page, ZLTextLineInfo info, int from, int to, int x, int y, PageIndex pageIndex)
	{
		if (from == to) {
			return;
		}

		final LinkedList<ZLTextHighlighting> hilites = new LinkedList<ZLTextHighlighting>();
		if (mySelection.intersects(page)) {
			hilites.add(mySelection);
		}

		isFlag = false;
		synchronized (myHighlightings) {
			for (ZLTextHighlighting h : myHighlightings) {
				if (h.intersects(page)) {
					hilites.add(h);
					// 排除选择文字高亮区
					if (h != mySelection) {// && !page.hasBookmark()
						isFlag = true;
						// 当deleteAllBookmarkFlag为true，说明此时的绘制工作其
						// 实是为了删除书签，因此不可以设置setHasBookmark为true。
						if (!deleteAllBookmarkFlag) {
							// ouyang: 判断高亮书签的起始索引是否在页面内，是则显示书签标志。
							/*
							 *  因为存储书签时，截取文字的需要，连续存储了不超过20字，这20字有可能跨越章节，造成结束索引在下一章内，如果显示书签标示时，
							 *  连书签结束索引一起判断，就造成存储一个书签，结果有2个章节，显示了书签标志。
							 */
							if(page.StartCursor.compareTo(h.getStartPosition()) <= 0
									&& page.EndCursor.compareTo(h.getStartPosition()) >= 0){
								page.setHasBookmark(true);
							}
						}
					}
				}
			}
		}
		if (hilites.isEmpty()) {
			return;
		}

		final ZLTextElementArea fromArea = page.TextElementMap.get(from);
		final ZLTextElementArea toArea = page.TextElementMap.get(to - 1);
		for (ZLTextHighlighting h : hilites) {
			final ZLColor bgColor = h.getBackgroundColor();
			if (bgColor == null) {
				continue;
			}
			final ZLTextElementArea selectionStartArea = h.getStartArea(page);
			if (selectionStartArea == null || selectionStartArea.compareTo(toArea) > 0) {
				continue;
			}
			final ZLTextElementArea selectionEndArea = h.getEndArea(page);
			if (selectionEndArea == null || selectionEndArea.compareTo(fromArea) < 0) {
				continue;
			}

			final int top = y + 1;
			int left, right, bottom = y + info.Height + info.Descent;
			if (selectionStartArea.compareTo(fromArea) < 0) {
				left = x;
			} else {
				left = selectionStartArea.XStart;
			}
			if (selectionEndArea.compareTo(toArea) > 0) {
				right = x + page.getTextWidth() - 1;
				bottom += info.VSpaceAfter;
			} else {
				right = selectionEndArea.XEnd;
			}

			// ZLog.d(ZLog.ZLTextView, "删除书签Flag > deleteAllBookmarkFlag = " +
			// deleteAllBookmarkFlag);

			// 删除书签(当前页)
			if (pageIndex == PageIndex.current && deleteAllBookmarkFlag) {
				if (h instanceof BookmarkHighlighting) {
					// 删除书签时，只删除起始索引在当前页面内的书签，不删除那种有交集的书签
					if(page.StartCursor.compareTo(h.getStartPosition()) <= 0
							&& page.EndCursor.compareTo(h.getStartPosition()) >= 0){
						FBReaderApp app = (FBReaderApp) ZLApplication.Instance();
						app.Collection.deleteBookmark(((BookmarkHighlighting) h).Bookmark);
					}
//					FBReaderApp app = (FBReaderApp) ZLApplication.Instance();
//					app.Collection.deleteBookmark(((BookmarkHighlighting) h).Bookmark);
				}
			} else {
				// 绘制高亮区
				// TODO:ouyang 屏蔽绘制高亮区域
				// getContext().setFillColor(bgColor);
				// getContext().fillRectangle(left, top, right, bottom);
			}
		}
	}

	protected abstract ZLPaintContext.ColorAdjustingMode getAdjustingModeForImages();

	private static final char[]	SPACE	= new char[] { ' ' };

	private static class ElementIndexs
	{
		ArrayList<Integer>			elements;

		private Comparator<Integer>	myComparator	= new Comparator<Integer>()
													{
														@Override
														public int compare(Integer v1, Integer v2)
														{
															if (v1 > v2) {
																return 1;
															} else if (v1 < v2) {
																return -1;
															}
															return 0;
														}
													};

		public ElementIndexs()
		{
			elements = new ArrayList<Integer>();
			elements.add(0);
		}

		void add(int elementIndex)
		{
			if (!elements.contains(elementIndex)) {
				elements.add(elementIndex);
				Collections.sort(elements, myComparator);
			}
		}

		int previou(int elementIndex)
		{
			int index = 0;
			for (Integer v : elements) {
				if (v == elementIndex) {
					break;
				}
				++index;
			}
			if (index > 0) {
				return elements.get(--index);
			}
			return 0;
		}
	}

	// TODO 计算每一个<div>、<span>对应的四个边的border数据
	private void computeBorders(ZLTextParagraphCursor paragraphCursor, ZLTextLineInfo myInfo, boolean isFirstLine
			, boolean isEndOfParagraph)
	{
		int paragraghIndex = paragraphCursor.Index;
		Vector<BackgroundItemInfo> myBgItemInfoVector = myModel.getParagraphBackgroundItemInfo(paragraghIndex);
		if (myBgItemInfoVector != null) {
			for (int i = 0; i < myBgItemInfoVector.size(); ++i) {
				BackgroundItemInfo myBgItemInfo = myBgItemInfoVector.get(i);
				int startParagraphIndex = myBgItemInfo.startRange.paragraphIndex;
				int endParagraphIndex = myBgItemInfo.endRange.paragraphIndex;
				int startElementIndex = myBgItemInfo.startRange.elementIndex;
				// int endElementIndex = myBgItemInfo.endRange.elementIndex;

				if (paragraghIndex == 5 || paragraghIndex == 6) {
					int debug = 0;
					debug = debug + 1;
				}

				boolean myContinue = startParagraphIndex <= paragraghIndex;
				if (myBgItemInfo.isBlock) {
					myContinue = myContinue && endParagraphIndex > paragraghIndex;
				} else {
					myContinue = myContinue && endParagraphIndex >= paragraghIndex;
				}

				if (myContinue) {

					ElementIndexs myElementIndexs = myElementIndexsWithParagraph.get(startParagraphIndex);
					if (myElementIndexs == null) {
						myElementIndexs = new ElementIndexs();
						myElementIndexsWithParagraph.put(startParagraphIndex, myElementIndexs);
					}
					myElementIndexs.add(startElementIndex);

					BorderValue myBorderValue = myBordersLinkedMap.get(myBgItemInfo);
					if (myBorderValue == null) {
						myBorderValue = new BorderValue();
						myBordersLinkedMap.put(myBgItemInfo, myBorderValue);
					}

					if (paragraghIndex == startParagraphIndex || paragraghIndex == endParagraphIndex - 1
							|| !isFirstLine || isEndOfParagraph) {

						ZLTextStyle StartStyle = null;
						if (isFirstLine || isEndOfParagraph) {
							StartStyle = getTextStyle();
						} else {
							// isFirstLine=false是为了走这里，因为可能打开某本书籍时，直接跳转到的是段中的某处
							// 示例图可以查看./SinaReader2.0.2/debug-screenshot/其他/示例1.png
							ZLBgUtil.i.applyStyleChanges(ZLTextParagraphCursor.cursor(myModel, startParagraphIndex), this);
							StartStyle = ZLBgUtil.i.getTextStyle();
						}

						ZLTextStyle preStyle = StartStyle;

						if (isFirstLine && !isEndOfParagraph) {
							ZLBgUtil.i.applyStyleChanges(ZLTextParagraphCursor.cursor(myModel, startParagraphIndex), 0, startElementIndex, this);
							StartStyle = ZLBgUtil.i.getTextStyle();
						}

						if (StartStyle != null) {
							if (myBorderValue.top == 0) {
								myBorderValue.top = StartStyle.getTopBorderOnly(metrics());
							}
							if (myBorderValue.right == 0) {
								myBorderValue.right = StartStyle.getRightBorderOnly(metrics());
							}
							if (myBorderValue.bottom == 0) {
								myBorderValue.bottom = StartStyle.getBottomBorderOnly(metrics());
							}
							if (myBorderValue.left == 0) {
								myBorderValue.left = StartStyle.getLeftBorderOnly(metrics());
							}
						}

						if (endParagraphIndex - startParagraphIndex == 1 || (myBgItemInfo.isBlock && paragraghIndex == endParagraphIndex - 1)) {
							StartStyle = preStyle;
							if (StartStyle != null) {
								if (myBorderValue.bottom == 0) {
									myBorderValue.bottom = StartStyle.getBottomBorderOnly(metrics());
								}
							}
						}
					}
				}

			}
		}
	}

	// add by zdc
	private void computeBackgroundInfo(ZLTextPage page, ZLTextLineInfo info, int from,
			int to, int firstLineParagraphIndex, int lastLineParagraphIndex)
	{
		// 当前绘制行所在的段落
		final ZLTextParagraphCursor paragraphCursor = info.ParagraphCursor;
		// 当前绘制行的段落索引
		final int drawLineParagraphIndex = paragraphCursor.Index;
		// 当前行的尾字的索引(相对段落)
		final int lineEndElementIndex = info.EndElementIndex;
		// 本章节所有的<div>、<span>等的背景信息
		Vector<BackgroundItemInfo> myBgItemInfoVector = myModel.getParagraphBackgroundItemInfo(drawLineParagraphIndex);

		if (myBgItemInfoVector != null) {
			for (int i = 0; i < myBgItemInfoVector.size(); ++i) {
				// 以./epub_book/测试/single-word.epub或者div-empty-div.epub为例，myBgItemInfo的值：
				// (1) [1:3],[6:0],ColorZLColor(255, 255, 0),isBlock:true
				// (2) [1:5],[2:0],ColorZLColor(0, 255, 0),isBlock:true
				// (3) [4:5],[5:0],ColorZLColor(255, 0, 0),isBlock:true
				BackgroundItemInfo myBgItemInfo = myBgItemInfoVector.get(i);
				int startParagraphIndex = myBgItemInfo.startRange.paragraphIndex;
				int endParagraphIndex = myBgItemInfo.endRange.paragraphIndex;
				int startElementIndex = myBgItemInfo.startRange.elementIndex;
				int endElementIndex = myBgItemInfo.endRange.elementIndex;

				// 绘制该行时，背景区域所在的起始段落索引肯定要小于等于当前行所在段落索引
				// 背景区域所在的结束段落索引大于等于(div等为大于)当前行所在段落索引
				boolean myContinue = startParagraphIndex <= drawLineParagraphIndex;
				// isBlock代表的是div属性区域，
				if (myBgItemInfo.isBlock) {
					myContinue = myContinue && endParagraphIndex > drawLineParagraphIndex;
				} else {
					myContinue = myContinue && endParagraphIndex >= drawLineParagraphIndex;
				}

				// 在范围内的段落方可继续
				if (myContinue) {

					ElementIndexs myElementIndexs = myElementIndexsWithParagraph.get(startParagraphIndex);
					if (myElementIndexs == null) {
						myElementIndexs = new ElementIndexs();
						myElementIndexsWithParagraph.put(startParagraphIndex, myElementIndexs);
					}
					myElementIndexs.add(startElementIndex);

					// myRectInfo中的myStartPoint和myEndPoint才是接下来需要去测量并得到其值的关键
					BackgroundDivInfo myRectInfo = myBgRectLinkedMap.get(myBgItemInfo);
					if (myRectInfo == null) {
						myRectInfo = new BackgroundDivInfo();
						myRectInfo.myColor = myBgItemInfo.bgColor;
						myRectInfo.myKind = myBgItemInfo.kind;
						myRectInfo.myLeftBorderColor = myBgItemInfo.leftBorderColor;
						myRectInfo.myTopBorderColor = myBgItemInfo.topBorderColor;
						myRectInfo.myRightBorderColor = myBgItemInfo.rightBorderColor;
						myRectInfo.myBottomBorderColor = myBgItemInfo.bottomBorderColor;
						myBgRectLinkedMap.put(myBgItemInfo, myRectInfo);
					}

					int index = from;
					ZLTextElementArea startArea = null;
					ZLTextElementArea endArea = null;
					int wordIndex = info.RealStartElementIndex;
					final boolean paragraphHaveNoWord = wordIndex == lineEndElementIndex;
					for (; wordIndex != lineEndElementIndex && index < to; ++wordIndex) {
						ZLTextElement element = paragraphCursor.getElement(wordIndex);
						ZLTextElementArea area = page.TextElementMap.get(index);
						if (element == area.Element) {
							++index;
							// <span>
							if (!myBgItemInfo.isBlock) {
								// 确定startArea
								if (startArea == null) {
									// 背景起始段落
									if (drawLineParagraphIndex == startParagraphIndex) {
										if (startElementIndex <= wordIndex) {
											// START
											startArea = area;
										}
									} else {
										// 背景起始段落之下的段落，直接赋值，GO ON
										startArea = area;
									}
								}

								// 确定endArea
								if (drawLineParagraphIndex == endParagraphIndex) {
									// 一个元素一个元素往后遍历时，不断的给endArea赋值
									if (endElementIndex >= wordIndex) {
										// 直到wordIndex超出endElementIndex，END
										endArea = area;
									}
								} else {
									// 背景结束段落之上段落，直接赋值，GO ON
									endArea = area;
								}

							} else {
								// <div>
								if (drawLineParagraphIndex == startParagraphIndex) {
									if (startElementIndex <= wordIndex) {
										if (myRectInfo.myStartPoint == null) {
											computeDivStartRect(myRectInfo, page, startParagraphIndex, startElementIndex, area, myElementIndexs);
										}
									}
								} else {
									if (myRectInfo.myStartPoint == null) {
										computeDivStartRect(myRectInfo, page, startParagraphIndex, startElementIndex, area, myElementIndexs);
									}
								}

								int x = area.XEnd;
								int y = area.YEnd + info.VSpaceAfter - info.VSpaceBorderAfter;
								// 临界点判断
								if (lastLineParagraphIndex < endParagraphIndex) {
									y = getTopMargin() + getTextAreaHeight();
								}
								myRectInfo.myEndPoint = new Point(x, y);
							}
						}
					}

					// 如<div><div></div></div>这种空div的情况
					if (paragraphHaveNoWord) {
						if (myRectInfo.myStartPoint == null) {
							// 如果能走到这里，说明在prepareTextLine方法中已经计算过
							// 对应空div的坐标了，这时候只需要取到各个div对应的坐标即可
							int previou = myElementIndexs.previou(startElementIndex);
							ZLTextElementArea area = null;
							ZLTextElement element = paragraphCursor.getElement(previou);
							boolean isAttach = false;
							while (!isAttach && element != null) {
								index = from;
								for (; index < to; ++index) {
									area = page.TextElementMap.get(index);
									if (element == area.Element) {
										isAttach = true;
										break;
									}
								}
								if (!isAttach) {
									++previou;
									element = paragraphCursor.getElement(previou);
								}
							}

							if (isAttach && area != null) {
								myRectInfo.myRectWidth = area.XEnd;
								myRectInfo.myStartPoint = new Point(area.XStart, area.YStart);
								// 临界点判断
								int y = area.YEnd;
								if (lastLineParagraphIndex < endParagraphIndex) {
									y = getTopMargin() + getTextAreaHeight();
								}
								myRectInfo.myEndPoint = new Point(area.XEnd, y);
							}
						}

						index = from;
						wordIndex = 0;
						for (; wordIndex != lineEndElementIndex && index < to; ++wordIndex) {
							ZLTextElement element = paragraphCursor.getElement(wordIndex);
							ZLTextElementArea area = page.TextElementMap.get(index);
							if (element == area.Element) {
								++index;
								// 临界点判断
								int y = area.YEnd;
								if (lastLineParagraphIndex < endParagraphIndex) {
									y = getTopMargin() + getTextAreaHeight();
								}
								myRectInfo.myEndPoint = new Point(area.XEnd, y);
							}
						}
					}

					// SPAN
					if (!myBgItemInfo.isBlock) {
						if (startArea != null && endArea != null) {
							BackgroundSpanInfo mySpanItem = new BackgroundSpanInfo(startArea, endArea, myBgItemInfo.bgColor);
							mySpanItem.myKind = myBgItemInfo.kind;
							mySpanItem.myLeftBorderColor = myBgItemInfo.leftBorderColor;
							mySpanItem.myTopBorderColor = myBgItemInfo.topBorderColor;
							mySpanItem.myRightBorderColor = myBgItemInfo.rightBorderColor;
							mySpanItem.myBottomBorderColor = myBgItemInfo.bottomBorderColor;
							myBgSpanLinkedList.add(mySpanItem);
						}
					}
				}
			}
		}
	}

	/**
	 * 计算<div>标签所对应的背景区域的起始点位置<br>
	 * 计算方式：无论当前绘制的段落是不是div背景区域的起始所在段落，<br>
	 * 我们只需要
	 * 
	 * @param myRectInfo
	 * @param page
	 * @param startParagraphIndex
	 * @param startElementIndex
	 * @param area
	 * @param myElementIndexs
	 */
	private void computeDivStartRect(BackgroundDivInfo myRectInfo, ZLTextPage page, int startParagraphIndex, int startElementIndex,
			ZLTextElementArea area, ElementIndexs myElementIndexs)
	{
		// 处理字前面包含有效{@link ZLTextStyleElement}的情况
		// 确定paragraphCursor的第一个有效“字”，获取其索引值

		// 1.可能达到的最top的y轴坐标值，该值在{#link ViewOptions -> TopMargin}中设定
		int yTop = getTopMargin();
		int left = 0, top = 0, right = 0, bottom = 0;
		// 将当前“如”字的坐标记录下来
		left = area.XStart;
		top = area.YStart;

		ZLTextParagraphCursor startParagraphCursor = ZLTextParagraphCursor.cursor(myModel, startParagraphIndex);
		int paragraphRealStartElementIndex = 0;
		final int paragraphLength = startParagraphCursor.getParagraphLength();
		ZLTextElement element = startParagraphCursor.getElement(paragraphRealStartElementIndex);
		while (isStyleChangeElement(element)) {
			++paragraphRealStartElementIndex;
			if (paragraphRealStartElementIndex == paragraphLength) {
				break;
			}
			element = startParagraphCursor.getElement(paragraphRealStartElementIndex);
		}
		// 当前startElementIndex=3，wordIndex=5->“如”字
		// 根据“如”字所在的Area来获取“如”字以上的Area，最终确定Rect
		// previou=0
		int previou = myElementIndexs.previou(startElementIndex);
		// 从5遍历到0
		int elementIndex = paragraphRealStartElementIndex;
		// 不包含本身，因此自减一次
		--elementIndex;
		element = startParagraphCursor.getElement(elementIndex);
		// 存储<border>四边的宽度值
		boolean borderOccurred = false;
		int[] borderWidths = new int[] { 0, 0, 0, 0 };
		int divCount = 0;
		int topBorderHeight = 0;
		while (isStyleChangeElement(element)) {
			// 遍历找到有效ZLTextStyleElement
			if (element instanceof ZLTextStyleElement) {
				ZLTextStyleElement styleElement = (ZLTextStyleElement) element;
				ZLTextStyleEntry entry = styleElement.Entry;
				if (entry instanceof ZLTextCSSStyleEntry) {
					int endIndex = elementIndex;
					int leftBorderWidth = 0;
					int rightBorderWidth = 0;
					int Height = 0;
					if (borderOccurred) {
						rightBorderWidth = borderWidths[1];
						leftBorderWidth = borderWidths[3];
						topBorderHeight += borderWidths[0];
						// 重置
						borderOccurred = false;
						for (int i = 0; i < borderWidths.length; i++) {
							borderWidths[i] = 0;
						}
					}
					ZLBgUtil.i.applyStyleChanges(startParagraphCursor, 0, ++endIndex, this);
					ZLTextStyle Style = ZLBgUtil.i.getTextStyle();
					int BottomPadding = Style.getBottomPaddingOnly(metrics());
					int TopPadding = Style.getTopPaddingOnly(metrics());
					int LeftIndent = Style.getLeftIndent(metrics()) + leftBorderWidth;
					int MaxWidth = page.getTextWidth() + getLeftMargin() - (Style.getRightIndent(metrics()) + rightBorderWidth) + Style.getRightPaddingOnly(metrics());
					if ((entry.isFeatureSupported(Feature.LENGTH_PADDING_TOP))
							|| (entry.isFeatureSupported(Feature.LENGTH_PADDING_BOTTOM))) {
						++divCount;
						left = getLeftMargin() + LeftIndent - Style.getLeftPaddingOnly(metrics());
						// VSpaceBefore或VSpaceAfter二选一
						if (BottomPadding == 0) {
							Height = TopPadding;
						} else {
							Height = BottomPadding;
						}
						right = MaxWidth;
						bottom = top;
						// y坐标递减
						top -= Height;
					} else {
						// 没有padding-top或者padding-bottom
						// right = MaxWidth;
					}

					if (entry.anyBorderSupported()) {
						borderOccurred = true;
						if (entry.isBorderSupported(BorderFeature.BORDER_TOP)) {
							borderWidths[0] = Style.getTopBorderOnly(metrics());
						}
						if (entry.isBorderSupported(BorderFeature.BORDER_RIGHT)) {
							borderWidths[1] = Style.getRightBorderOnly(metrics());
						}
						if (entry.isBorderSupported(BorderFeature.BORDER_BOTTOM)) {
							borderWidths[2] = Style.getBottomBorderOnly(metrics());
						}
						if (entry.isBorderSupported(BorderFeature.BORDER_LEFT)) {
							borderWidths[3] = Style.getLeftBorderOnly(metrics());
						}
					}
				}
			}
			// elementIndex=previou之后，停止
			if (elementIndex == previou) {
				break;
			}
			--elementIndex;
			element = startParagraphCursor.getElement(elementIndex);
		}

		if (divCount >= 2) {
			top -= topBorderHeight;
		}

		// 测量的数据有效
		if (right == 0) {
			right = page.getTextWidth() + getLeftMargin();
		}
		// 临界判断
		if (top <= yTop) {
			top = yTop;
		}

		// 当前页的起始字所在段落索引大于要测量的
		// 这个<div>标签所在起始段落索引，则将该<div>
		// 标签所在区域的顶部设置为yTop
		if (area.ParagraphIndex > startParagraphIndex) {
			top = yTop;
		}
		myRectInfo.myRectWidth = right;
		myRectInfo.myStartPoint = new Point(left, top);
	}

	// end by zdc

	private void drawTextLine(ZLTextPage page, ZLTextLineInfo info, int from, int to)
	{
		final ZLPaintContext context = getContext();
		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;
		int index = from;
		final int endElementIndex = info.EndElementIndex;
		int charIndex = info.RealStartCharIndex;

		for (int wordIndex = info.RealStartElementIndex; wordIndex != endElementIndex && index < to; ++wordIndex, charIndex = 0) {
			final ZLTextElement element = paragraph.getElement(wordIndex);
			final ZLTextElementArea area = page.TextElementMap.get(index);
			if (element == area.Element) {
				++index;
				if (area.ChangeStyle) {
					setTextStyle(area.Style);// 设置字体，加粗，倾斜等样式
				}
				final int areaX = area.XStart;
				final int areaY = area.YEnd - getElementDescent(element) - getTextStyle().getVerticalAlign(metrics());
				if (element instanceof ZLTextWord) {
					// drawWord(areaX, areaY, (ZLTextWord) element, charIndex,
					// -1, false,
					// mySelection.isAreaSelected(area) ?
					// getSelectionForegroundColor() :
					// getTextColor(getTextStyle().Hyperlink));
					// add by yq
					if (((ZLTextWord)element).getWidth(getContext()) == 0) {//部分手机会出现绘制特殊符号
						continue;
					}
					ZLColor color = getTextColor(getTextStyle().Hyperlink);
					if (mySelection.isAreaSelected(area)) {
						color = getSelectionForegroundColor();
					} else if (area.Style.isTextColorSupported()) {
						color = area.Style.getTextColor();
					}
					// ZLog.e(ZLog.ZLCSS_DIV_BG, "element -> " + element +
					// ", areaX -> " + areaX + ", areaY -> " + areaY);
					drawWord(areaX, areaY, (ZLTextWord) element, charIndex, -1, false, color);
					// end by yq
				} else if (element instanceof ZLTextImageElement) {
					final ZLTextImageElement imageElement = (ZLTextImageElement) element;
					context.drawImage(areaX, areaY, imageElement.ImageData, getImageRectSize(), getScalingType(imageElement), getAdjustingModeForImages());
				} else if (element instanceof ZLTextVideoElement) {
					// TODO: draw
					// FIXME:ouyang
					// int color = mReadStyleManager.mReadTextColor;
					// ZLColor fgColor = new ZLColor(color);
					// context.setLineColor(fgColor);
					context.setLineColor(getTextColor(ZLTextHyperlink.NO_LINK));
					context.setFillColor(new ZLColor(127, 127, 127));
					final int xStart = area.XStart + 10;
					final int xEnd = area.XEnd - 10;
					final int yStart = area.YStart + 10;
					final int yEnd = area.YEnd - 10;
					context.fillRectangle(xStart, yStart, xEnd, yEnd);
					context.drawLine(xStart, yStart, xStart, yEnd);
					context.drawLine(xStart, yEnd, xEnd, yEnd);
					context.drawLine(xEnd, yEnd, xEnd, yStart);
					context.drawLine(xEnd, yStart, xStart, yStart);
					final int l = xStart + (xEnd - xStart) * 7 / 16;
					final int r = xStart + (xEnd - xStart) * 10 / 16;
					final int t = yStart + (yEnd - yStart) * 2 / 6;
					final int b = yStart + (yEnd - yStart) * 4 / 6;
					final int c = yStart + (yEnd - yStart) / 2;
					context.setFillColor(new ZLColor(196, 196, 196));
					context.fillPolygon(new int[] { l, l, r }, new int[] { t, b, c });
				} else if (element == ZLTextElement.HSpace) {
					final int cw = context.getSpaceWidth();
					/*
					 * context.setFillColor(getHighlightingColor());
					 * context.fillRectangle( area.XStart, areaY -
					 * context.getStringHeight(), area.XEnd - 1, areaY +
					 * context.getDescent() );
					 */
					for (int len = 0; len < area.XEnd - area.XStart; len += cw) {
						context.drawString(areaX + len, areaY, SPACE, 0, 1);
					}
				}
				// add by yq
				else if (element instanceof ZLTextSpecialElement) {
					final int xStart = area.XStart;
					final int xEnd = area.XEnd + 1;
					final int yStart = area.YStart;
					final int yEnd = area.YEnd + 1;
					// todo:border的color需支持
					ZLTextStyle beforeStyle = getTextStyle();
					final int borderLeft = beforeStyle.getLeftBorderOnly(metrics());
					final int borderTop = beforeStyle.getTopBorderOnly(metrics());
					final int borderRight = beforeStyle.getRightBorderOnly(metrics());
					int bottom = beforeStyle.getBottomBorderOnly(metrics());
					int tmpIndex = wordIndex + 1;
					for (; tmpIndex != endElementIndex; ++tmpIndex) {
						final ZLTextElement tmpElement = paragraph.getElement(tmpIndex);
						if (isStyleChangeElement(tmpElement)) {
							applyStyleChangeElement(tmpElement);
						} else {
							break;
						}
					}
					ZLColor bottomColor = getTextStyle().getBottomBorderColor();
					bottom += getTextStyle().getBottomBorderOnly(metrics());
					setTextStyle(beforeStyle);

					final int borderBottom = bottom;
					if (borderLeft > 0 || borderTop > 0 || borderRight > 0 || borderBottom > 0) {
						if (borderLeft > 0) {
							context.setFillColor(beforeStyle.getLeftBorderColor());
							context.fillPolygon(new int[] { xStart, xStart + borderLeft, xStart + borderLeft, xStart }, new int[] { yStart, yStart + borderTop, yEnd - borderBottom, yEnd });
						}

						if (borderTop > 0) {
							context.setFillColor(beforeStyle.getTopBorderColor());
							context.fillPolygon(new int[] { xStart, xEnd, xEnd - borderRight, xStart + borderLeft }, new int[] { yStart, yStart, yStart + borderTop, yStart + borderTop });
						}

						if (borderRight > 0) {
							context.setFillColor(beforeStyle.getRightBorderColor());
							context.fillPolygon(new int[] { xEnd, xEnd, xEnd - borderRight, xEnd - borderRight }, new int[] { yStart, yEnd, yEnd - borderBottom, yStart + borderTop });
						}

						if (borderBottom > 0) {
							context.setFillColor(bottomColor);
							context.fillPolygon(new int[] { xStart, xEnd, xEnd - borderRight, xStart + borderLeft }, new int[] { yEnd, yEnd, yEnd - borderBottom, yEnd - borderBottom });
						}
					} else {
						context.setLineColor(new ZLColor(0));
						context.drawLine(xStart, yStart, xStart, yEnd);
						context.drawLine(xStart, yEnd, xEnd, yEnd);
						context.drawLine(xEnd, yEnd, xEnd, yStart);
						context.drawLine(xEnd, yStart, xStart, yStart);
					}
				}
				// end by yq
			}
		}
		if (index != to) {// 会有这种情况出现吗!?
			ZLTextElementArea area = page.TextElementMap.get(index++);
			if (area.ChangeStyle) {
				setTextStyle(area.Style);
			}
			final int start = info.StartElementIndex == info.EndElementIndex ? info.StartCharIndex : 0;
			final int len = info.EndCharIndex - start;
			final ZLTextWord word = (ZLTextWord) paragraph.getElement(info.EndElementIndex);
			if (word != null) {
				drawWord(area.XStart, area.YEnd - context.getDescent() - getTextStyle().getVerticalAlign(metrics()), word, start, len, area.AddHyphenationSign,
						mySelection.isAreaSelected(area) ? getSelectionForegroundColor() : getTextColor(getTextStyle().Hyperlink));
			}
		}
	}

	/** 构建一页的数据 */
	private void buildInfos(ZLTextPage page, ZLTextWordCursor start, ZLTextWordCursor result)
	{// 这个start便是构建page的起始坐标
		result.setCursor(start);// 拷贝start给result，下面操作的都是result，但是一开始赋的是start的值，也就是说从start开始检索，此时还不知道endCursor
		int textAreaHeight = page.getTextHeight();// 绘制文本区域的高度
		page.LineInfos.clear();// 清除所有行
		page.Column0Height = 0;
		boolean nextParagraph;
		ZLTextLineInfo info = null;
		do {//解析一个段落
			final ZLTextLineInfo previousInfo = info;
			resetTextStyle();// 绘制每一行的时候清除掉之前所有的绘制样式(因为有可能每一行的字体大小，是否倾斜等样式都不一样)
			final ZLTextParagraphCursor paragraphCursor = result.getParagraphCursor();// 元素所在的段落
			final int wordIndex = result.getElementIndex();// 元素索引
			applyStyleChanges(paragraphCursor, 0, wordIndex);// 应用该元素对应的样式，便于测量对应样式下的一些属性值
			info = new ZLTextLineInfo(paragraphCursor, wordIndex, result.getCharIndex(), getTextStyle());// 构建新行，传入行的起始元素信息
			final int endIndex = info.ParagraphCursorLength;// 整个段落的长度
			while (info.EndElementIndex != endIndex) {// 将段落拆分成各个行，直到段落最后一个字
				info = processTextLine(page, paragraphCursor, info.EndElementIndex, info.EndCharIndex, endIndex, previousInfo);
				textAreaHeight -= info.Height + info.Descent;// 减去刚出算出来的一行高度
				if (textAreaHeight < 0 && page.LineInfos.size() > page.Column0Height) {
					if (page.Column0Height == 0 && page.twoColumnView()) {
						textAreaHeight = page.getTextHeight();
						textAreaHeight -= info.Height + info.Descent;
						page.Column0Height = page.LineInfos.size();
					} else {
						break;
					}
				}
				textAreaHeight -= info.VSpaceAfter;
				result.moveTo(info.EndElementIndex, info.EndCharIndex);// 将result光标移动到info(行)尾字
				page.LineInfos.add(info);// 一行信息检索完毕，加入
				if (textAreaHeight < 0) {
					if (page.Column0Height == 0 && page.twoColumnView()) {
						textAreaHeight = page.getTextHeight();
						page.Column0Height = page.LineInfos.size();
					} else {
						break;
					}
				}
			}

			// add by zdc
			if (result.isEndOfParagraph()) {
				if (!result.getParagraphCursor().isLast()) {
					int paraStartIndex = myModel.getParagraphStartIndex(result.getParagraphIndex() + 1);
					int paraEndIndex = myModel.getParagraphEndIndex(paraStartIndex);
					iterateBackgroundEntryInfo(paraStartIndex, paraEndIndex);
				}
			}
			// end by zdc

			nextParagraph = result.isEndOfParagraph() && result.nextParagraph();
			if (nextParagraph && result.getParagraphCursor().isEndOfSection()) {
				if (page.Column0Height == 0 && page.twoColumnView() && !page.LineInfos.isEmpty()) {
					textAreaHeight = page.getTextHeight();
					page.Column0Height = page.LineInfos.size();
				}
			}
		} while (nextParagraph && textAreaHeight >= 0 && (!result.getParagraphCursor().isEndOfSection() || page.LineInfos.size() == page.Column0Height));
		resetTextStyle();
	}

	private boolean isHyphenationPossible()
	{
		return getTextStyleCollection().getBaseStyle().AutoHyphenationOption.getValue() && getTextStyle().allowHyphenations();
	}

	private ZLTextLineInfo processTextLine(ZLTextPage page, ZLTextParagraphCursor paragraphCursor, final int startIndex, final int startCharIndex, final int endIndex, ZLTextLineInfo previousInfo)
	{
		final ZLPaintContext context = getContext();
		final ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, startIndex, startCharIndex, getTextStyle());
		final ZLTextLineInfo cachedInfo = myLineInfoCache.get(info);// 取出某个ZLTextLineInfo的关键在其ParagraphCursor、StartElementIndex、StartCharIndex的值是否全部一致
		if (cachedInfo != null) {
			cachedInfo.adjust(previousInfo);
			applyStyleChanges(paragraphCursor, startIndex, cachedInfo.EndElementIndex);
			return cachedInfo;
		}
		// 从传入的startIndex开始计算
		int currentElementIndex = startIndex;
		int currentCharIndex = startCharIndex;
		final boolean isFirstLine = startIndex == 0 && startCharIndex == 0;// 是不是段落的第一行

		if (isFirstLine) {// 这里负责解析出实际的起始元素索引RealStartElementIndex、RealStartCharIndex
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			while (isStyleChangeElement(element)) {
				applyStyleChangeElement(element);
				++currentElementIndex;
				currentCharIndex = 0;
				if (currentElementIndex == endIndex) {
					break;
				}
				element = paragraphCursor.getElement(currentElementIndex);
			}
			info.StartStyle = getTextStyle();
			info.RealStartElementIndex = currentElementIndex;
			info.RealStartCharIndex = currentCharIndex;
		}// End

		ZLTextStyle storedStyle = getTextStyle();
		// 左缩进值
		info.LeftIndent = storedStyle.getLeftIndent(metrics());
		if (isFirstLine) {
			// 首行缩进值
			info.LeftIndent += storedStyle.getFirstLineIndent(metrics());
		}

		info.Width = info.LeftIndent;
		if (info.RealStartElementIndex == endIndex) {
			/**
			 * 实际起始元素索引==段落尾字 (说明该paragraphCursor中没有任何有效的可绘制内容，如文字(ZLTextWord)，<br>
			 * 图像(ZLTextImageElement)等 ，遇到诸如 <br>
			 * <br>
			 * <div style="background-color:#ffff00; padding:44px;"> <br>
			 * <div style="background-color:#ff00ff; padding:44px;"></div> <br>
			 * 对酒当歌，人生几何。...<br>
			 * </div> <br>
			 * <br>
			 * 这种div嵌套div，而且子级div没有任何内容的情况时，底层会返回这种结构的paragraphCursor，<br>
			 * 该paragraphCursor中将大多只包含{#link ZLTextControlElement}、<br>
			 * {#link ZLTextStyleElemnt} 等CSS样式属性信息)<br>
			 */
			info.EndElementIndex = info.RealStartElementIndex;
			info.EndCharIndex = info.RealStartCharIndex;

			// add by cjl

			// 判断有没有有效的CSS数据
			boolean hasCSSStyleEntry = false;
			int elementIndex = startIndex;
			ZLTextElement element = paragraphCursor.getElement(elementIndex);
			while (isStyleChangeElement(element)) {
				if (element instanceof ZLTextStyleElement) {
					ZLTextStyleElement styleElement = (ZLTextStyleElement) element;
					ZLTextStyleEntry entry = styleElement.Entry;
					if (entry instanceof ZLTextCSSStyleEntry) {
						hasCSSStyleEntry = true;
						break;
					}
				}
				++elementIndex;
				if (elementIndex == endIndex) {
					break;
				}
				element = paragraphCursor.getElement(elementIndex);
			}
			if (hasCSSStyleEntry) {
				// 有的话，要算其对应的padding，margin等值
				info.VSpaceBefore = getTextStyle().getSpaceBefore(metrics());
				info.VSpaceAfter = getTextStyle().getSpaceAfter(metrics());
				info.VSpaceBorderAfter = getTextStyle().getBottomBorder(metrics());
				info.Height += info.VSpaceBefore;
				info.IsVisible = true;

				// 这里负责测量top、left、right或bottom的border值
				computeBorders(paragraphCursor, info, isFirstLine, false);
			}

			// end by cjl
			return info;
		}// End

		// 一般说来，这里负责测量top、left、right的border值
		computeBorders(paragraphCursor, info, isFirstLine, false);

		int newWidth = info.Width;
		int newHeight = info.Height;
		int newDescent = info.Descent;
		int maxWidth = page.getTextWidth() - storedStyle.getRightIndent(metrics());// 最大宽度=可绘制的宽度-右缩进长度
		boolean wordOccurred = false;// ZLTextWord、ZLTextImageElement、ZLTextVideoElement都属wordOccurred(true)，如果遇到空格，会重新置为false
		boolean isVisible = false;
		int lastSpaceWidth = 0;
		int internalSpaceCounter = 0;// 有多少个空格
		boolean removeLastSpace = false;// 是否移除最后的空格

		// 临界处理，防止死循环导致内存溢出
		if (maxWidth - newWidth < 0) {
			newWidth -= info.LeftIndent;
		}

		if (maxWidth - newWidth < 0) {
			maxWidth += storedStyle.getRightIndent(metrics());
		}

		do {
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			newWidth += getElementWidth(element, currentCharIndex);// 累加该字的宽度
			newHeight = Math.max(newHeight, getElementHeight(element));// 取一行中高度最高的那个字的高度作为高
			newDescent = Math.max(newDescent, getElementDescent(element));// 基线baseline以下的height，同样也是取最大的
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred) {
					wordOccurred = false;
					internalSpaceCounter++;
					lastSpaceWidth = context.getSpaceWidth();
					newWidth += lastSpaceWidth;
				}
			} else if (element instanceof ZLTextWord) {
				wordOccurred = true;
				isVisible = true;
			} else if (element instanceof ZLTextImageElement) {
				wordOccurred = true;
				isVisible = true;
			} else if (element instanceof ZLTextVideoElement) {
				wordOccurred = true;
				isVisible = true;
			} else if (isStyleChangeElement(element)) {
				applyStyleChangeElement(element);
			}
			// add by yq
			else if (element instanceof ZLTextSpecialElement) {
				newWidth = maxWidth - storedStyle.getRightIndent(metrics());
				wordOccurred = true;
				isVisible = true;
			}
			// end by yq
			if (newWidth > maxWidth) {
				if (info.EndElementIndex != startIndex || element instanceof ZLTextWord) {
					break;
					// 唯一条件：
					// 1）当前检索的宽度大于最大宽度了 && 2）行结束元素索引非传入的起始索引 || 当前元素是字
				}
			}
			ZLTextElement previousElement = element;// 上一个元素
			++currentElementIndex;
			currentCharIndex = 0;
			boolean allowBreak = currentElementIndex == endIndex;// allowBreak=true的条件：1）最后一个元素了
			if (!allowBreak) {// 非最后一个元素
				element = paragraphCursor.getElement(currentElementIndex);// 下一个元素
				allowBreak = ((!(element instanceof ZLTextWord) || previousElement instanceof ZLTextWord) && !(element instanceof ZLTextImageElement) && !(element instanceof ZLTextControlElement)
						&& !(element instanceof ZLTextSpecialElement));
			}
			// allowBreak=true的条件：
			// 2）下一个元素不是字或者上一个元素是字 并且 下一个元素非图片元素 并且
			// 下一个元素非ZLTextControlElement
			if (allowBreak) {
				info.IsVisible = isVisible;
				info.Width = newWidth;
				if (info.Height < newHeight) {
					info.Height = newHeight;
				}
				if (info.Descent < newDescent) {
					info.Descent = newDescent;
				}
				info.EndElementIndex = currentElementIndex;
				info.EndCharIndex = currentCharIndex;
				info.SpaceCounter = internalSpaceCounter;
				storedStyle = getTextStyle();
				removeLastSpace = !wordOccurred && (internalSpaceCounter > 0);
			}
		} while (currentElementIndex != endIndex);

		if (currentElementIndex != endIndex && (isHyphenationPossible() || info.EndElementIndex == startIndex)) {
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			if (element instanceof ZLTextWord) {
				final ZLTextWord word = (ZLTextWord) element;
				newWidth -= getWordWidth(word, currentCharIndex);
				int spaceLeft = maxWidth - newWidth;
				if ((word.Length > 3 && spaceLeft > 2 * context.getSpaceWidth()) || info.EndElementIndex == startIndex) {
					ZLTextHyphenationInfo hyphenationInfo = ZLTextHyphenator.Instance().getInfo(word);
					int hyphenationPosition = word.Length - 1;
					int subwordWidth = 0;
					for (; hyphenationPosition > currentCharIndex; hyphenationPosition--) {
						if (hyphenationInfo.isHyphenationPossible(hyphenationPosition)) {
							subwordWidth = getWordWidth(word, currentCharIndex, hyphenationPosition - currentCharIndex, word.Data[word.Offset + hyphenationPosition - 1] != '-');
							if (subwordWidth <= spaceLeft) {
								break;
							}
						}
					}
					if (hyphenationPosition == currentCharIndex && info.EndElementIndex == startIndex) {
						hyphenationPosition = word.Length == currentCharIndex + 1 ? word.Length : word.Length - 1;
						subwordWidth = getWordWidth(word, currentCharIndex, word.Length - currentCharIndex, false);
						for (; hyphenationPosition > currentCharIndex + 1; hyphenationPosition--) {
							subwordWidth = getWordWidth(word, currentCharIndex, hyphenationPosition - currentCharIndex, word.Data[word.Offset + hyphenationPosition - 1] != '-');
							if (subwordWidth <= spaceLeft) {
								break;
							}
						}
					}
					if (hyphenationPosition > currentCharIndex) {
						info.IsVisible = true;
						info.Width = newWidth + subwordWidth;
						if (info.Height < newHeight) {
							info.Height = newHeight;
						}
						if (info.Descent < newDescent) {
							info.Descent = newDescent;
						}
						info.EndElementIndex = currentElementIndex;
						info.EndCharIndex = hyphenationPosition;
						info.SpaceCounter = internalSpaceCounter;
						storedStyle = getTextStyle();
						removeLastSpace = false;
					}
				}
			}
		}

		if (removeLastSpace) {// 移除最后的空格，行宽度减去一个空格宽度，行的空格数减一
			info.Width -= lastSpaceWidth;
			info.SpaceCounter--;
		}

		setTextStyle(storedStyle);

		if (isFirstLine) {
			info.VSpaceBefore = info.StartStyle.getSpaceBefore(metrics());
			if (previousInfo != null) {
				info.PreviousInfoUsed = true;
				info.Height += Math.max(0, info.VSpaceBefore - previousInfo.VSpaceAfter);
			} else {
				info.PreviousInfoUsed = false;
				info.Height += info.VSpaceBefore;
			}
		}
		if (info.isEndOfParagraph()) {
			info.VSpaceAfter = getTextStyle().getSpaceAfter(metrics());
			info.VSpaceBorderAfter = getTextStyle().getBottomBorder(metrics());
			// 一般说来，这里负责测量bottom的border值
			computeBorders(paragraphCursor, info, isFirstLine, true);
		}

		if (info.EndElementIndex != endIndex || endIndex == info.ParagraphCursorLength) {
			myLineInfoCache.put(info, info);
		}

		return info;
	}

	private void prepareTextLine(ZLTextPage page, ZLTextLineInfo info, int x, int y)
	{
		y = Math.min(y + info.Height, getTopMargin() + page.getTextHeight() - 1);

		final ZLPaintContext context = getContext();
		final ZLTextParagraphCursor paragraphCursor = info.ParagraphCursor;

		setTextStyle(info.StartStyle);
		int spaceCounter = info.SpaceCounter;
		int fullCorrection = 0;
		final boolean endOfParagraph = info.isEndOfParagraph();
		boolean wordOccurred = false;
		boolean changeStyle = true;
		x += info.LeftIndent;

		final int maxWidth = page.getTextWidth();
		switch (getTextStyle().getAlignment()) {
		case ZLTextAlignmentType.ALIGN_RIGHT:
			x += maxWidth - getTextStyle().getRightIndent(metrics()) - info.Width;
			break;
		case ZLTextAlignmentType.ALIGN_CENTER:
			x += (maxWidth - getTextStyle().getRightIndent(metrics()) - info.Width) / 2;
			break;
		case ZLTextAlignmentType.ALIGN_JUSTIFY:
			if (!endOfParagraph && (paragraphCursor.getElement(info.EndElementIndex) != ZLTextElement.AfterParagraph)) {
				// 可绘制宽度-右缩进-行宽度=剩余多少长度? -> 将fullCorrection平均分给该行的所有空格
				fullCorrection = maxWidth - getTextStyle().getRightIndent(metrics()) - info.Width;
			}
			break;
		case ZLTextAlignmentType.ALIGN_LEFT:
		case ZLTextAlignmentType.ALIGN_UNDEFINED:
			break;
		}

		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;
		final int paragraphIndex = paragraph.Index;
		final int endElementIndex = info.EndElementIndex;
		int charIndex = info.RealStartCharIndex;
		ZLTextElementArea spaceElement = null;
		// add by cjl
		int wordIndex = info.RealStartElementIndex;
		boolean paragraphHaveNoWord = wordIndex == endElementIndex;
		for (; wordIndex != endElementIndex; ++wordIndex, charIndex = 0) {// 循环遍历测量各个元素的坐标数据
			final ZLTextElement element = paragraph.getElement(wordIndex);
			final int width = getElementWidth(element, charIndex);// 该元素的宽度(可能是某个单词的半截，charIndex!=0时)
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred && (spaceCounter > 0)) {
					final int correction = fullCorrection / spaceCounter;
					final int spaceLength = context.getSpaceWidth() + correction;
					if (getTextStyle().isUnderline()) {
						spaceElement = new ZLTextElementArea(paragraphIndex, wordIndex, 0, 0, // length
								true, // is last in element
								false, // add hyphenation sign
								false, // changed style
								getTextStyle(), element, x, x + spaceLength, y, y);
					} else {
						spaceElement = null;
					}
					x += spaceLength;
					fullCorrection -= correction;
					wordOccurred = false;
					--spaceCounter;
				}
			} else if (element instanceof ZLTextWord || element instanceof ZLTextImageElement || element instanceof ZLTextVideoElement) {
				final int height = getElementHeight(element);
				final int descent = getElementDescent(element);
				final int length = element instanceof ZLTextWord ? ((ZLTextWord) element).Length : 0;
				if (spaceElement != null) {
					page.TextElementMap.add(spaceElement);
					spaceElement = null;
				}

				page.TextElementMap.add(new ZLTextElementArea(paragraphIndex, wordIndex, charIndex, length - charIndex, true, // is
																																// last
																																// in
																																// element
						false, // add hyphenation sign
						changeStyle, getTextStyle(), element, x, x + width - 1, y - height + 1, y + descent));
				changeStyle = false;
				wordOccurred = true;
			} else if (isStyleChangeElement(element)) {
				applyStyleChangeElement(element);
				changeStyle = true;
			}
			// add by yq
			else if (element instanceof ZLTextSpecialElement) {
				int height = getElementHeight(element);
				final int space = (height - 2) / 2;

				final int length = 0;
				if (spaceElement != null) {
					page.TextElementMap.add(spaceElement);
					spaceElement = null;
				}

				ZLTextStyle style = getTextStyle();
				resetTextStyle();
				int tmpIndex = 0;
				for (; tmpIndex < wordIndex; ++tmpIndex) {
					final ZLTextElement tmpElement = paragraph.getElement(tmpIndex);
					if (isStyleChangeElement(tmpElement)) {
						if (tmpElement instanceof ZLTextControlElement
								&& ((ZLTextControlElement) tmpElement).Kind == FBTextKind.HR) {
							resetTextStyle();
						} else {
							applyStyleChangeElement(tmpElement);
						}
					}
				}

				final int top = getTextStyle().getTopPaddingOnly(metrics()) + getTextStyle().getTopBorderOnly(metrics());
				final int left = getTextStyle().getLeftPaddingOnly(metrics()) + getTextStyle().getLeftBorderOnly(metrics());
				final int right = getTextStyle().getRightPaddingOnly(metrics()) + getTextStyle().getRightBorderOnly(metrics());
				height += (top > 0 ? top - 1 : top);
				setTextStyle(style);
				tmpIndex = wordIndex + 1;
				for (; tmpIndex != endElementIndex; ++tmpIndex) {
					final ZLTextElement tmpElement = paragraph.getElement(tmpIndex);
					if (isStyleChangeElement(tmpElement)) {
						if (tmpElement instanceof ZLTextControlElement
								&& ((ZLTextControlElement) tmpElement).Kind == FBTextKind.HR) {
							break;
						}
						applyStyleChangeElement(tmpElement);
					} else {
						break;
					}
				}

				int bottom = getTextStyle().getBottomPaddingOnly(metrics()) + getTextStyle().getBottomBorderOnly(metrics());
				bottom = bottom > 0 ? bottom - 1 : bottom;
				setTextStyle(style);

				final int specialW = info.Width;
				page.TextElementMap.add(new ZLTextElementArea(paragraphIndex, wordIndex, charIndex, length - charIndex, true, false,
						changeStyle, getTextStyle(), element, x - left, x + specialW + right - 1, y - height + 1 + space, y - space + bottom));
				changeStyle = false;
				wordOccurred = true;
			}
			// end by yq
			x += width;
		}

		// 1.div可能没有padding属性
		// 2.如<div><div></div></div>这种空div的情况，则此时段落中没有任何有效的可绘制内容，如文字，图像等
		// 3.2的情况中，还可能带有border属性。如div-border-empty-div-border.epub
		if (paragraphHaveNoWord) {
			// || !wordOccurred
			// ZLTextElement element = null;
			// if (!wordOccurred) {
			// wordIndex = info.RealStartElementIndex;
			// for (; wordIndex != endElementIndex; ++wordIndex) {
			// element = paragraph.getElement(wordIndex);
			// if (isStyleChangeElement(element)) {
			// break;
			// }
			// }
			// } else {
			wordIndex = 0;
			// }

			x -= info.LeftIndent;
			y -= info.Height;
			ZLTextElement element = paragraph.getElement(wordIndex);
			while (isStyleChangeElement(element)) {
				if (element instanceof ZLTextStyleElement) {
					ZLTextStyleElement styleElement = (ZLTextStyleElement) element;
					ZLTextStyleEntry entry = styleElement.Entry;
					if (entry instanceof ZLTextCSSStyleEntry) {
						// 1.topPadding或bottomPadding二者只能用其一
						int endIndex = wordIndex;
						if ((entry.isFeatureSupported(Feature.LENGTH_PADDING_TOP)) || (entry.isFeatureSupported(Feature.LENGTH_PADDING_BOTTOM))) {
							ZLBgUtil.i.applyStyleChanges(paragraphCursor, 0, ++endIndex, this);
							ZLTextStyle Style = ZLBgUtil.i.getTextStyle();
							int bottomPadding = Style.getBottomPaddingOnly(metrics());
							int topPadding = Style.getTopPaddingOnly(metrics());
							int leftPadding = Style.getLeftPaddingOnly(metrics());
							int rightPadding = Style.getRightPaddingOnly(metrics());
							boolean isBorderSupport = false;
							if (isNextElementHasBorderSupport(paragraph, wordIndex)) {
								isBorderSupport = true;
								ZLBgUtil.i.applyStyleChanges(paragraphCursor, 0, ++endIndex, this);
								Style = ZLBgUtil.i.getTextStyle();
							}
							int LeftIndent = Style.getLeftIndent(metrics());
							int MaxWidth = page.getTextWidth() + getLeftMargin() - Style.getRightIndent(metrics()) + rightPadding;
							int Height = 0;
							x = getLeftMargin() + LeftIndent - leftPadding;
							if (bottomPadding == 0) {
								if (isBorderSupport) {
									y += Style.getTopBorderOnly(metrics());
								}
								Height = topPadding;
							} else {
								Height = bottomPadding;
							}
							y += Height;
							page.TextElementMap.add(new ZLTextElementArea(paragraphIndex, wordIndex, 0, 1, true,
									false,
									changeStyle, getTextStyle(), element, x, MaxWidth, y - Height + 1, y));
							// 跳过下面的那个border元素
							if (isBorderSupport)
								++wordIndex;
						} else if (entry.isFeatureSupported(Feature.BG_COLOR)) {

							ZLBgUtil.i.applyStyleChanges(paragraphCursor, 0, ++endIndex, this);
							ZLTextStyle Style = ZLBgUtil.i.getTextStyle();
							// int bottomPadding =
							// Style.getBottomPaddingOnly(metrics());
							// int topPadding =
							// Style.getTopPaddingOnly(metrics());
							// int LeftIndent = Style.getLeftIndent(metrics());
							int MaxWidth = page.getTextWidth() + getLeftMargin() - Style.getRightIndent(metrics()) + Style.getRightPaddingOnly(metrics());
							// int Height = 0;

							// 没有padding值但是依然支持背景
							page.TextElementMap.add(new ZLTextElementArea(paragraphIndex, wordIndex, 0, 1, true,
									false,
									changeStyle, getTextStyle(), element, x, MaxWidth, y, y));
						}
					}
				}
				++wordIndex;
				if (wordIndex == endElementIndex) {
					break;
				}
				element = paragraph.getElement(wordIndex);
			}
			y += info.Height;
		}
		// end by cjl

		if (!endOfParagraph) {
			final int len = info.EndCharIndex;
			if (len > 0) {
				final int _wordIndex = info.EndElementIndex;
				final ZLTextWord word = (ZLTextWord) paragraph.getElement(wordIndex);
				final boolean addHyphenationSign = word.Data[word.Offset + len - 1] != '-';
				final int width = getWordWidth(word, 0, len, addHyphenationSign);
				final int height = getElementHeight(word);
				final int descent = context.getDescent();
				page.TextElementMap.add(new ZLTextElementArea(paragraphIndex, _wordIndex, 0, len, false, // is
																											// last
																											// in
																											// element
						addHyphenationSign, changeStyle, getTextStyle(), word, x, x + width - 1, y - height + 1, y + descent));
			}
		}
	}

	private boolean isNextElementHasBorderSupport(ZLTextParagraphCursor paragraph, int wordIndex)
	{
		int paragraphLength = paragraph.getParagraphLength();
		if (++wordIndex < paragraphLength) {
			ZLTextElement element = paragraph.getElement(wordIndex);
			if (element instanceof ZLTextStyleElement) {
				ZLTextStyleElement styleElement = (ZLTextStyleElement) element;
				ZLTextStyleEntry entry = styleElement.Entry;
				if (entry instanceof ZLTextCSSStyleEntry) {
					if (entry.anyBorderSupported()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public synchronized final void turnPage(boolean forward, int scrollingMode, int value)
	{
		preparePaintInfo(myCurrentPage);
		myPreviousPage.reset();
		myNextPage.reset();
		if (myCurrentPage.PaintState == PaintStateEnum.READY) {
			myCurrentPage.PaintState = forward ? PaintStateEnum.TO_SCROLL_FORWARD : PaintStateEnum.TO_SCROLL_BACKWARD;
			myScrollingMode = scrollingMode;
			myOverlappingValue = value;
		}
	}

	public final synchronized void gotoPosition(ZLTextPosition position)
	{
		if (position != null) {
			gotoPosition(position.getParagraphIndex(), position.getElementIndex(), position.getCharIndex());
		}
	}

	public final synchronized void gotoPosition(int paragraphIndex, int wordIndex, int charIndex)
	{
		if (myModel != null && myModel.getParagraphsNumber() > 0) {

			// add by zdc
			int paraIndex = myModel.getParagraphStartIndex(paragraphIndex);
			int paraEndIndex = myModel.getParagraphEndIndex(paragraphIndex);
			iterateBackgroundEntryInfo(paraIndex, paraEndIndex);
			// end by zdc

			Application.getViewWidget().reset();
			myCurrentPage.moveStartCursor(paragraphIndex, wordIndex, charIndex);
			myPreviousPage.reset();
			myNextPage.reset();
			preparePaintInfo(myCurrentPage);
			if (myCurrentPage.isEmptyPage()) {
				turnPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			}
		}
	}

	private final synchronized void gotoPositionByEnd(int paragraphIndex, int wordIndex, int charIndex)
	{
		if (myModel != null && myModel.getParagraphsNumber() > 0) {

			// add by zdc
			int paraIndex = myModel.getParagraphStartIndex(paragraphIndex);
			int paraEndIndex = myModel.getParagraphEndIndex(paragraphIndex);
			iterateBackgroundEntryInfo(paraIndex, paraEndIndex);
			// end by zdc

			myCurrentPage.moveEndCursor(paragraphIndex, wordIndex, charIndex);
			myPreviousPage.reset();
			myNextPage.reset();
			preparePaintInfo(myCurrentPage);
			if (myCurrentPage.isEmptyPage()) {
				turnPage(false, ScrollingMode.NO_OVERLAPPING, 0);
			}
		}
	}

	protected synchronized void preparePaintInfo()
	{
		myPreviousPage.reset();
		myNextPage.reset();
		preparePaintInfo(myCurrentPage);
	}

	private synchronized void preparePaintInfo(ZLTextPage page)
	{
		// 参数分别是：绘制页宽度(如果是两页宽度缩半)，绘制页区域高度，是不是两页(由页宽高和用户的配置有关系)，是不是上一页
		page.setSize(getTextColumnWidth(), getTextAreaHeight(), twoColumnView(), page == myPreviousPage);

		if (page.PaintState == PaintStateEnum.NOTHING_TO_PAINT || page.PaintState == PaintStateEnum.READY) {
			return;
		}
		// 将原来的信息缓存下来
		final int oldState = page.PaintState;
		// 缓存所有的行信息
		final HashMap<ZLTextLineInfo, ZLTextLineInfo> cache = myLineInfoCache;
		for (ZLTextLineInfo info : page.LineInfos) {
			cache.put(info, info);
		}

		switch (page.PaintState) {
		default:
			break;
		case PaintStateEnum.TO_SCROLL_FORWARD:
			// 判断page(页)的EndCursor(结束字/元素)是不是整篇文章的段尾最后一字
			if (!page.EndCursor.isEndOfText()) {

				// add by zdc
				int paraIndex = page.EndCursor.getParagraphCursor().Index - 1;
				int paraEndIndex = myModel.getParagraphEndIndex(paraIndex);
				if (paraIndex + 1 > paraEndIndex) {
					int nextParaStartIndex = paraEndIndex + 1;
					int nextParaEndIndex = myModel.getParagraphEndIndex(nextParaStartIndex);
					iterateBackgroundEntryInfo(nextParaStartIndex, nextParaEndIndex);
				}
				// end by zdc

				final ZLTextWordCursor startCursor = new ZLTextWordCursor();
				switch (myScrollingMode) {
				case ScrollingMode.NO_OVERLAPPING:
					break;
				case ScrollingMode.KEEP_LINES:
					page.findLineFromEnd(startCursor, myOverlappingValue);
					break;
				case ScrollingMode.SCROLL_LINES:
					page.findLineFromStart(startCursor, myOverlappingValue);
					if (startCursor.isEndOfParagraph()) {
						startCursor.nextParagraph();
					}
					break;
				case ScrollingMode.SCROLL_PERCENTAGE:
					page.findPercentFromStart(startCursor, myOverlappingValue);
					break;
				}

				if (!startCursor.isNull() && startCursor.samePositionAs(page.StartCursor)) {
					page.findLineFromStart(startCursor, 1);
				}

				if (!startCursor.isNull()) {
					final ZLTextWordCursor endCursor = new ZLTextWordCursor();
					buildInfos(page, startCursor, endCursor);
					if (!page.isEmptyPage() && (myScrollingMode != ScrollingMode.KEEP_LINES || !endCursor.samePositionAs(page.EndCursor))) {
						page.StartCursor.setCursor(startCursor);
						page.EndCursor.setCursor(endCursor);
						break;
					}
				}

				page.StartCursor.setCursor(page.EndCursor);
				buildInfos(page, page.StartCursor, page.EndCursor);
			}
			break;
		case PaintStateEnum.TO_SCROLL_BACKWARD:
			if (!page.StartCursor.isStartOfText()) {

				// add by zdc
				int paraIndex = page.StartCursor.getParagraphCursor().Index;
				int paraStartIndex = myModel.getParagraphStartIndex(paraIndex);
				if (paraIndex == paraStartIndex) {
					int prevParaEndIndex = paraIndex - 1;
					int prevParaStartIndex = myModel.getParagraphStartIndex(prevParaEndIndex);
					iterateBackgroundEntryInfo(prevParaStartIndex, prevParaEndIndex);
				}
				// end by zdc

				switch (myScrollingMode) {
				case ScrollingMode.NO_OVERLAPPING:
					page.StartCursor.setCursor(findStartOfPrevousPage(page, page.StartCursor));
					break;
				case ScrollingMode.KEEP_LINES: {
					ZLTextWordCursor endCursor = new ZLTextWordCursor();
					page.findLineFromStart(endCursor, myOverlappingValue);
					if (!endCursor.isNull() && endCursor.samePositionAs(page.EndCursor)) {
						page.findLineFromEnd(endCursor, 1);
					}
					if (!endCursor.isNull()) {
						ZLTextWordCursor startCursor = findStartOfPrevousPage(page, endCursor);
						if (startCursor.samePositionAs(page.StartCursor)) {
							page.StartCursor.setCursor(findStartOfPrevousPage(page, page.StartCursor));
						} else {
							page.StartCursor.setCursor(startCursor);
						}
					} else {
						page.StartCursor.setCursor(findStartOfPrevousPage(page, page.StartCursor));
					}
					break;
				}
				case ScrollingMode.SCROLL_LINES:
					page.StartCursor.setCursor(findStart(page, page.StartCursor, SizeUnit.LINE_UNIT, myOverlappingValue));
					break;
				case ScrollingMode.SCROLL_PERCENTAGE:
					page.StartCursor.setCursor(findStart(page, page.StartCursor, SizeUnit.PIXEL_UNIT, page.getTextHeight() * myOverlappingValue / 100));
					break;
				}
				buildInfos(page, page.StartCursor, page.EndCursor);
				if (page.isEmptyPage()) {
					page.StartCursor.setCursor(findStart(page, page.StartCursor, SizeUnit.LINE_UNIT, 1));
					buildInfos(page, page.StartCursor, page.EndCursor);
				}
			}
			break;
		case PaintStateEnum.START_IS_KNOWN:
			if (!page.StartCursor.isNull()) {

				// add by zdc
				int paraIndex = page.StartCursor.getParagraphCursor().Index + 1;
				int paraStartIndex = myModel.getParagraphStartIndex(paraIndex);
				int paraEndIndex = myModel.getParagraphEndIndex(paraIndex);
				iterateBackgroundEntryInfo(paraStartIndex, paraEndIndex);
				// end by zdc

				buildInfos(page, page.StartCursor, page.EndCursor);
			}
			break;
		case PaintStateEnum.END_IS_KNOWN:
			if (!page.EndCursor.isNull()) {

				// add by zdc
				int paraIndex = page.EndCursor.getParagraphCursor().Index;
				int paraStartIndex = myModel.getParagraphStartIndex(paraIndex);
				int paraEndIndex = myModel.getParagraphEndIndex(paraIndex);
				iterateBackgroundEntryInfo(paraStartIndex, paraEndIndex);
				// end by zdc

				page.StartCursor.setCursor(findStartOfPrevousPage(page, page.EndCursor));
				buildInfos(page, page.StartCursor, page.EndCursor);
			}
			break;
		}
		page.PaintState = PaintStateEnum.READY;
		// TODO: cache?
		myLineInfoCache.clear();

		if (page == myCurrentPage) {
			if (oldState != PaintStateEnum.START_IS_KNOWN) {
				myPreviousPage.reset();
			}
			if (oldState != PaintStateEnum.END_IS_KNOWN) {
				myNextPage.reset();
			}
		}
	}

	public void clearCaches()
	{
		myBordersLinkedMap.clear();
		resetMetrics();
		rebuildPaintInfo();
		Application.getViewWidget().reset();
		myCharWidth = -1;
	}

	protected synchronized void rebuildPaintInfo()
	{
		myPreviousPage.reset();
		myNextPage.reset();
		ZLTextParagraphCursorCache.clear();

		if (myCurrentPage.PaintState != PaintStateEnum.NOTHING_TO_PAINT) {
			myCurrentPage.LineInfos.clear();
			if (!myCurrentPage.StartCursor.isNull()) {
				myCurrentPage.StartCursor.rebuild();
				myCurrentPage.EndCursor.reset();
				myCurrentPage.PaintState = PaintStateEnum.START_IS_KNOWN;
			} else if (!myCurrentPage.EndCursor.isNull()) {
				myCurrentPage.EndCursor.rebuild();
				myCurrentPage.StartCursor.reset();
				myCurrentPage.PaintState = PaintStateEnum.END_IS_KNOWN;
			}
		}

		myLineInfoCache.clear();
	}

	private int infoSize(ZLTextLineInfo info, int unit)
	{
		return (unit == SizeUnit.PIXEL_UNIT) ? (info.Height + info.Descent + info.VSpaceAfter) : (info.IsVisible ? 1 : 0);
	}

	private static class ParagraphSize
	{
		public int	Height;
		public int	TopMargin;
		public int	BottomMargin;
	}

	private ParagraphSize paragraphSize(ZLTextPage page, ZLTextWordCursor cursor, boolean beforeCurrentPosition, int unit)
	{
		final ParagraphSize size = new ParagraphSize();

		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		if (paragraphCursor == null) {
			return size;
		}
		final int endElementIndex = beforeCurrentPosition ? cursor.getElementIndex() : paragraphCursor.getParagraphLength();

		resetTextStyle();

		int wordIndex = 0;
		int charIndex = 0;
		ZLTextLineInfo info = null;
		while (wordIndex != endElementIndex) {
			final ZLTextLineInfo prev = info;
			info = processTextLine(page, paragraphCursor, wordIndex, charIndex, endElementIndex, prev);
			wordIndex = info.EndElementIndex;
			charIndex = info.EndCharIndex;
			size.Height += infoSize(info, unit);
			if (prev == null) {
				size.TopMargin = info.VSpaceBefore;
			}
			size.BottomMargin = info.VSpaceAfter;
		}

		return size;
	}

	private void skip(ZLTextPage page, ZLTextWordCursor cursor, int unit, int size)
	{
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		if (paragraphCursor == null) {
			return;
		}
		final int endElementIndex = paragraphCursor.getParagraphLength();

		resetTextStyle();
		applyStyleChanges(paragraphCursor, 0, cursor.getElementIndex());

		ZLTextLineInfo info = null;
		while (!cursor.isEndOfParagraph() && size > 0) {
			info = processTextLine(page, paragraphCursor, cursor.getElementIndex(), cursor.getCharIndex(), endElementIndex, info);
			cursor.moveTo(info.EndElementIndex, info.EndCharIndex);
			size -= infoSize(info, unit);
		}
	}

	private ZLTextWordCursor findStartOfPrevousPage(ZLTextPage page, ZLTextWordCursor end)
	{
		if (twoColumnView()) {
			end = findStart(page, end, SizeUnit.PIXEL_UNIT, page.getTextHeight());
		}
		end = findStart(page, end, SizeUnit.PIXEL_UNIT, page.getTextHeight());
		return end;
	}

	private ZLTextWordCursor findStart(ZLTextPage page, ZLTextWordCursor end, int unit, int height)
	{
		final ZLTextWordCursor start = new ZLTextWordCursor(end);
		ParagraphSize size = paragraphSize(page, start, true, unit);
		height -= size.Height;
		boolean positionChanged = !start.isStartOfParagraph();
		start.moveToParagraphStart();
		while (height > 0) {
			final ParagraphSize previousSize = size;
			if (positionChanged && start.getParagraphCursor().isEndOfSection()) {
				break;
			}
			// add by zdc
			int prevParaIndex = start.getParagraphIndex() - 1;
			if (prevParaIndex >= 0) {
				int prevParaStartIndex = myModel.getParagraphStartIndex(prevParaIndex);
				int prevParaEndIndex = myModel.getParagraphEndIndex(prevParaIndex);
				iterateBackgroundEntryInfo(prevParaStartIndex, prevParaEndIndex);
			}
			// end by zdc
			
			if (!start.previousParagraph()) {
				break;
			}
			if (!start.getParagraphCursor().isEndOfSection()) {
				positionChanged = true;
			}
			size = paragraphSize(page, start, false, unit);
			height -= size.Height;
			if (previousSize != null) {
				height += Math.min(size.BottomMargin, previousSize.TopMargin);
			}
			
		}
		skip(page, start, unit, -height);

		if (unit == SizeUnit.PIXEL_UNIT) {
			boolean sameStart = start.samePositionAs(end);
			if (!sameStart && start.isEndOfParagraph() && end.isStartOfParagraph()) {
				ZLTextWordCursor startCopy = new ZLTextWordCursor(start);
				startCopy.nextParagraph();
				sameStart = startCopy.samePositionAs(end);
			}
			if (sameStart) {
				start.setCursor(findStart(page, end, SizeUnit.LINE_UNIT, 1));
			}
		}

		return start;
	}

	protected ZLTextElementArea getElementByCoordinates(int x, int y)
	{
		return myCurrentPage.TextElementMap.binarySearch(x, y);
	}

	public void hideSelectedRegionBorder()
	{
		myHighlightSelectedRegion = false;
		Application.getViewWidget().reset();
	}

	private ZLTextRegion getSelectedRegion(ZLTextPage page)
	{
		return page.TextElementMap.getRegion(mySelectedRegionSoul);
	}

	public ZLTextRegion getSelectedRegion()
	{
		return getSelectedRegion(myCurrentPage);
	}

	protected ZLTextHighlighting findHighlighting(int x, int y, int maxDistance)
	{
		final ZLTextRegion region = findRegion(x, y, maxDistance, ZLTextRegion.AnyRegionFilter);
		if (region == null) {
			return null;
		}
		synchronized (myHighlightings) {
			for (ZLTextHighlighting h : myHighlightings) {
				if (h.getBackgroundColor() != null && h.intersects(region)) {
					return h;
				}
			}
		}
		return null;
	}

	protected ZLTextRegion findRegion(int x, int y, ZLTextRegion.Filter filter)
	{
		return findRegion(x, y, Integer.MAX_VALUE - 1, filter);
	}

	protected ZLTextRegion findRegion(int x, int y, int maxDistance, ZLTextRegion.Filter filter)
	{
		return myCurrentPage.TextElementMap.findRegion(x, y, maxDistance, filter);
	}

	public void selectRegion(ZLTextRegion region)
	{
		final ZLTextRegion.Soul soul = region != null ? region.getSoul() : null;
		if (soul == null || !soul.equals(mySelectedRegionSoul)) {
			myHighlightSelectedRegion = true;
		}
		mySelectedRegionSoul = soul;
	}

	protected boolean initSelection(int x, int y)
	{
		y -= ZLTextSelectionCursor.getHeight() / 2 + ZLTextSelectionCursor.getAccent() / 2;
		if (!mySelection.start(x, y)) {
			return false;
		}
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
		return true;
	}

	public void clearSelection()
	{
		if (mySelection.clear()) {
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	public int getSelectionStartY()
	{
		if (mySelection.isEmpty()) {
			return 0;
		}
		final ZLTextElementArea selectionStartArea = mySelection.getStartArea(myCurrentPage);
		if (selectionStartArea != null) {
			return selectionStartArea.YStart;
		}
		if (mySelection.hasPartBeforePage(myCurrentPage)) {
			final ZLTextElementArea firstArea = myCurrentPage.TextElementMap.getFirstArea();
			return firstArea != null ? firstArea.YStart : 0;
		} else {
			final ZLTextElementArea lastArea = myCurrentPage.TextElementMap.getLastArea();
			return lastArea != null ? lastArea.YEnd : 0;
		}
	}

	public int getSelectionEndY()
	{
		if (mySelection.isEmpty()) {
			return 0;
		}
		final ZLTextElementArea selectionEndArea = mySelection.getEndArea(myCurrentPage);
		if (selectionEndArea != null) {
			return selectionEndArea.YEnd;
		}
		if (mySelection.hasPartAfterPage(myCurrentPage)) {
			final ZLTextElementArea lastArea = myCurrentPage.TextElementMap.getLastArea();
			return lastArea != null ? lastArea.YEnd : 0;
		} else {
			final ZLTextElementArea firstArea = myCurrentPage.TextElementMap.getFirstArea();
			return firstArea != null ? firstArea.YStart : 0;
		}
	}

	public ZLTextPosition getSelectionStartPosition()
	{
		return mySelection.getStartPosition();
	}

	public ZLTextPosition getSelectionEndPosition()
	{
		return mySelection.getEndPosition();
	}

	public boolean isSelectionEmpty()
	{
		return mySelection.isEmpty();
	}

	public void resetRegionPointer()
	{
		mySelectedRegionSoul = null;
		myHighlightSelectedRegion = true;
	}

	public ZLTextRegion nextRegion(Direction direction, ZLTextRegion.Filter filter)
	{
		return myCurrentPage.TextElementMap.nextRegion(getSelectedRegion(), direction, filter);
	}

	@Override
	public boolean canScroll(PageIndex index)
	{
		switch (index) {
		default:
			return true;
		case next: {
			final ZLTextWordCursor cursor = getEndCursor();
			boolean result = cursor != null && !cursor.isNull() && !cursor.isEndOfText();
			return result;
		}
		case previous: {
			final ZLTextWordCursor cursor = getStartCursor();
			return cursor != null && !cursor.isNull() && !cursor.isStartOfText();
		}
		case current:
			return false;
		}
	}

	// add by zdc
	void iterateBackgroundEntryInfo(int startParaIndex, int endParaIndex)
	{
		if (backgroundSet.contains(startParaIndex)) {
			return;
		}

		if (startParaIndex >= 0 && startParaIndex <= endParaIndex) {
			myModel.setProcessingBackgroundEntry(true);
			ZLTextParagraphCursorCache.remove(myModel, startParaIndex);
			ZLTextWordCursor cursor = new ZLTextWordCursor(ZLTextParagraphCursor.cursor(myModel, startParaIndex));
			while (cursor.getParagraphIndex() < endParaIndex) {
				ZLTextParagraphCursorCache.remove(myModel, cursor.getParagraphIndex() + 1);
				if (!cursor.nextParagraph()) {
					break;
				}
			}
			myModel.setProcessingBackgroundEntry(false);
		}

		backgroundSet.add(startParaIndex);
	}
	// end by zdc
}
