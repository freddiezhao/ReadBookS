package com.sina.book.reader;

import android.view.MotionEvent;

import com.sina.book.reader.model.BookSummaryPostion;
import com.sina.book.reader.selector.Selection;

/**
 * 翻页的内容控制类，翻页控件通过它拿到下一页、上一页的内容
 * 
 * @author Tsimle
 * 
 */
public interface PageFactory {
	/** 选择位置索引默认值. */
	public static int INDEX_DEFAULT = -1;
	/** 选择位置索引开始. */
	public static int INDEX_START = 0;
	/** 选择位置索引结束. */
	public static int INDEX_END = 1;

	public PageBitmap drawCurPage();

	public void nextPage();

	public PageBitmap drawNextPage();

	public void prePage();

	public PageBitmap drawPrePage();

	/**
	 * 获取开始选择位置
	 * 
	 * @return
	 */
	public Selection getStartSelection();

	/**
	 * 获取结束选择位置
	 * 
	 * @return
	 */
	public Selection getEndSelection();

	/**
	 * 传入x,y坐标,查找选择结束位置
	 * 
	 * @param x
	 *            指定点X坐标
	 * @param y
	 *            指定点Y坐标
	 * @param index
	 *            位置索引,取值范围 -1 <= index <= 1的整数，为-1表示长按选择；0,1表示拖动选择
	 *            {@link #INDEX_DEFAULT} {@link #INDEX_START} {@link #INDEX_END}
	 */
	public void findSelection(float x, float y, int index);

	/**
	 * 清理选择位置信息
	 */
	public void clearSelection();

	/**
	 * 选择位置信息是否合法
	 * 
	 * @return
	 */
	public boolean isSelectionsLegal();

	/**
	 * 当前章节是否为VIP章节
	 * 
	 * @return
	 */
	public boolean isVipChapter();

	/** 触摸书摘区域 */
	public BookSummaryPostion onTouchBookSummary(MotionEvent e);

	/** 重绘电池区域 */
	public void reDrawBattery();
}
