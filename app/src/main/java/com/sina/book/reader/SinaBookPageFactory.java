package com.sina.book.reader;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.BookSummary;
import com.sina.book.data.Chapter;
import com.sina.book.data.MarkItem;
import com.sina.book.db.DBService;
import com.sina.book.reader.model.BookSummaryPostion;
import com.sina.book.reader.model.PageContent;
import com.sina.book.reader.model.SinaBookModel;
import com.sina.book.reader.page.PageSummary;
import com.sina.book.reader.page.TextLine;
import com.sina.book.reader.selector.Selection;
import com.sina.book.ui.view.BatteryView;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.LogUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.ThemeUtil;
import com.sina.book.util.Util;

/**
 * 阅读小说页面生成
 * 
 * @author Tsimle
 * 
 */
public class SinaBookPageFactory implements PageFactory {
	// private static final String TAG = "SinaBookPageFactory";

	private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
	private final String BOOK_CONTENT_PATTERN = "[\\s|　]";
	/** 分享微博最大字数 */
	private final int SHARE_WEIBO_MAX_NUM = 60;

	private Paint mHeaderPaint;
	private Paint mFooterPaint;

	/**
	 * 小说阅读页的画布和bitmap
	 */
	private PageBitmap mCurPage;
	private PageBitmap mNextPage;

	private PageContent mBookPage = new PageContent();

	private ReadStyleManager mReadStyleManager;
	private Book mBook;
	private SinaBookModel mBookModel;
	/**
	 * 当解析总页数等操作完成时，会通过listener通知
	 */
	private ITaskFinishListener mRefreshListener;

	private static SinaBookPageFactory mInstance;

	private BatteryView mBatteryView;

	private SinaBookPageFactory(int displayWidth, int displayHeight) {
		mReadStyleManager = ReadStyleManager.getInstance(displayWidth, displayHeight);
		mBatteryView = new BatteryView(SinaBookApplication.gContext);
	}

	/**
	 * 获取BookPageFactory实例
	 * 
	 * @param displayWidth
	 *            显示宽度
	 * @param displayHeight
	 *            显示高度
	 * @return
	 */
	public static SinaBookPageFactory getInstance(int displayWidth, int displayHeight) {
		if (mInstance != null) {
			mInstance.release(true);
			mInstance = null;
		}
		// if (mInstance == null) {
		mInstance = new SinaBookPageFactory(displayWidth, displayHeight);
		// }
		mInstance.initPageBitmapAndCanvas(displayWidth, displayHeight);
		return mInstance;
	}

	/**
	 * 获取BookPageFactory实例
	 * 
	 * @param context
	 * 
	 * @return
	 */
	public static SinaBookPageFactory getInstance(Context context) {
		int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
		int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
		return getInstance(displayWidth, displayHeight);
	}

	public void setRefreshListener(ITaskFinishListener refreshListener) {
		this.mRefreshListener = refreshListener;
	}

	public void setBatteryValue(int batteryValue) {
		mBatteryView.setValue(batteryValue);
	}

	/*-----------------------依赖bookmodel start-------------------------*/
	/**
	 * 映射书本文件
	 * 
	 * @param book
	 * @throws java.io.IOException
	 */
	public void openBook(Book book) throws IOException {
		mBook = book;
		mBookModel = new SinaBookModel(mBook, mReadStyleManager);
		mReadStyleManager.changeReadFontSize(StorageUtil.getFloat(StorageUtil.KEY_FONT_SIZE,
				ReadStyleManager.DEF_FONT_SIZE_SP));

		// 设置阅读的开始位置
		int lastPos = 0;
		if (mBook.getReadInfo().getLastPos() > 0) {
			lastPos = mBook.getReadInfo().getLastPos();
		}
		//

		LogUtil.d("SQLiteUpdateErrorLog", "SinaBookPageFactory >> openBook >> lastPos=" + lastPos);
		LogUtil.d("FileMissingLog", "SinaBookPageFactory >> openBook >> lastPos=" + lastPos);

		createBookPage(mBookModel.seek(lastPos));

		// 更新页数
		float fontSize = StorageUtil.getFloat(StorageUtil.KEY_FONT_SIZE, ReadStyleManager.DEF_FONT_SIZE_SP);
		if (mBook.getBookPage().getFontSize() == fontSize) {
			mBookModel.updateTotalPage(mRefreshListener, false, true);
		} else {
			mBook.getBookPage().setFontSize(fontSize);
			mBookModel.updateTotalPage(mRefreshListener, true, true);
		}
	}

	/**
	 * 释放之前打开书本的资源
	 */
	public void release(boolean forceRelease) {
		setRefreshListener(null);
		clearSelection();
		clearBookInfo();

		if (forceRelease) {
			if (mCurPage != null) {
				mCurPage.release();
				mCurPage = null;
			}
			if (mNextPage != null) {
				mNextPage.release();
				mNextPage = null;
			}
		} else {
			if (mCurPage != null) {
				int color = mReadStyleManager.getColorFromIdentifier(SinaBookApplication.gContext, R.color.reading_bg);
				mCurPage.getCanvas().drawColor(color);
				mCurPage.setRelatePage(null);
				mNextPage.getCanvas().drawColor(color);
				mNextPage.setRelatePage(null);
			}
		}
	}

	private void clearBookInfo() {
		mBook = null;
		if (mBookModel != null) {
			mBookModel.release();
			mBookModel = null;
		}
	}

	/**
	 * 需要在异步任务完成后刷新文件，并加载这里记录的位置<br>
	 * 
	 * @throws java.io.IOException
	 */
	public void prepareRefreshBookFile() {
		if (mBookModel == null) {
			return;
		}
		mBookModel.prepareRefreshBookFile();
	}

	/**
	 * 刷新文件
	 * 
	 * @throws java.io.IOException
	 */
	public void refreshBookFile() throws IOException {
		if (mBookModel == null) {
			return;
		}
		mBookModel.refreshBookFile();
		mBookModel.updateTotalPage(mRefreshListener, true, false);
	}

	/**
	 * 刷新文件
	 * 
	 * @throws java.io.IOException
	 */
	public void refreshBookFile(Book book) throws IOException {
		if (mBookModel == null) {
			return;
		}
		mBookModel.refreshOrReOpenBookFile(book);
		mBookModel.updateTotalPage(mRefreshListener, true, false);
	}

	@Override
	public BookSummaryPostion onTouchBookSummary(MotionEvent e) {
		if (!mBookPage.isEmpty()) {
			return mBookPage.onTouchBookSummary(e);
		}
		return null;
	}

	@Override
	public PageBitmap drawCurPage() {
		if (mBookPage.isEmpty() && mBookModel != null) {

			createBookPage(mBookModel.curPage());
		} else if (mBookModel != null) {
			// 强制刷新的时候更新下
			updateBookPage();
		}

		if (mNextPage != null && mNextPage.canUseForCache(mBookPage)) {
			// 设置必要信息
			if (!mBookPage.isEmpty()) {
				mCurPage.setRelatePage(mBookPage);
			}
			Chapter c = mBookModel.getCurrentChapter();
			if (c != null) {
				mCurPage.setTitle(c.getTitle());
				String curTime = mFormat.format(new Date());
				mCurPage.setBottomTime(curTime);

				if (mBook.isComplete() && mBookModel.getTotalPage() != 0) {
					mCurPage.setBottomPage(String.format(ResourceUtil.getString(R.string.reading_page_local),
							mBookModel.getCurrentPage(), mBookModel.getTotalPage()));
				} else if (mBookModel.getTotalPage(c) != 0) {
					int currPageIndex = mBookModel.getCurrentPage(c);
					int totalPageIndex = mBookModel.getTotalPage(c);
					mCurPage.setBottomPage(String.format(ResourceUtil.getString(R.string.reading_page_online),
							currPageIndex, totalPageIndex));
				} else {
					mCurPage.setBottomParsing(ResourceUtil.getString(R.string.reading_page_parsing));
				}
			}

			mCurPage.getCanvas().drawBitmap(mNextPage.getBitmap(), 0, 0, null);
		} else {
			draw(mCurPage);
		}

		return mCurPage;
	}

	@Override
	public PageBitmap drawNextPage() {
		nextPage();
		if (mBookModel == null || mBookModel.islastPage()) {
			return null;
		}
		draw(mNextPage);
		return mNextPage;
	}

	@Override
	public PageBitmap drawPrePage() {
		prePage();
		if (mBookModel == null || mBookModel.isfirstPage()) {
			return null;
		}
		draw(mNextPage);
		return mNextPage;
	}

	/**
	 * 往回翻页
	 */
	@Override
	public void prePage() {
		if (mBookModel == null) {
			return;
		}

		createBookPage(mBookModel.prePage());
	}

	/**
	 * 往前翻页
	 */
	@Override
	public void nextPage() {
		if (mBookModel == null) {
			return;
		}

		createBookPage(mBookModel.nextPage());
	}

	public Chapter getCurrentChapter() {
		LogUtil.i("BugID=21356", "SinaBookPageFactory >> getCurrentChapter >> {mBookModel =" + mBookModel + "}");
		if (mBookModel == null) {
			return null;
		}
		return mBookModel.getCurrentChapter();
	}

	public int getCurrentChapterIndex() {
		if (mBookModel == null) {
			return 1;
		}
		return mBookModel.getCurrentChapterIndex();
	}

	/**
	 * 得到当前的阅读进度百分比显示字符串
	 * 
	 * @return
	 */
	public String getCurrentPercentString() {
		if (mBookModel == null) {
			return "";
		}
		float fPercent = mBookModel.getReadPercent();
		DecimalFormat df = new DecimalFormat("#0.0");
		return df.format(fPercent * 100) + "%";
	}

	/**
	 * 得到当前的阅读进度百分比
	 * 
	 * @return
	 */
	public float getCurrentPercent() {
		if (mBookModel == null) {
			return 0;
		}
		return mBookModel.getReadPercent() * 100;
	}

	/**
	 * 跳转百分比
	 * 
	 * @param percent
	 */
	public void seek(float percent) {
		if (mBookModel == null) {
			return;
		}

		createBookPage(mBookModel.seek(percent));
	}

	/**
	 * 获取整本书的页数
	 * 
	 * @return totalPage
	 */
	public int getTotalPage() {
		if (mBookModel == null) {
			return 0;
		}
		return mBookModel.getTotalPage();
	}

	/**
	 * 跳转到某个位置
	 * 
	 * @param bufferBegin
	 */
	public void seek(int bufferBegin) {
		if (mBookModel == null) {
			return;
		}

		createBookPage(mBookModel.seek(bufferBegin));

		mBookModel.updateTotalPage(mRefreshListener, false, false);
	}

	/**
	 * 更新字体
	 */
	public void updateFontSize() {
		if (mBookModel == null) {
			return;
		}

		int begin = getScrollReadPos();
		if (begin >= 0) {
			createBookPage(mBookModel.updateFontSize(begin));
		} else {
			createBookPage(mBookModel.updateFontSize());
		}

		mBookModel.updateTotalPage(mRefreshListener, true, false);
	}

	/**
	 * 添加最后阅读mark
	 * 
	 * @return
	 */
	public void addLastReadMark() {
		if (mBookModel == null) {
			return;
		}
		int begin = mBookModel.getMbBufferBegin();
		int end = mBookModel.getMbBufferEnd();

		int lastPos = getScrollReadPos();
		if (lastPos < 0 && begin >= 0) {
			lastPos = begin;
		}
		mBook.getReadInfo().setLastPos(lastPos);

		// 更新当前页数
		mBook.getBookPage().setCurPage(mBookModel.getCurrentPage());

		// updateReadProgress
		float progress = mBook.getReadInfo().getLastReadPercent();
		if (mBook.isSingleChapterBook() || (mBook.isComplete() && mBookModel.getTotalPage() != 0)) {
			progress = getCurrentPercent();
			mBook.getReadInfo().setLastReadInfo(lastPos, 0, 0);
		} else {
			Chapter curChapter = getCurrentChapter();
			ArrayList<Chapter> chapters = mBook.getChapters();
			int total = chapters.size();

			if (null != curChapter && total > 0) {
				long inChapterPos = begin - curChapter.getStartPos();

				float cell = 1.0f / total;
				int preChapterId = chapters.indexOf(curChapter);

				progress = preChapterId * cell;
				if (curChapter.getLength() > 0 && inChapterPos > 0) {
					float pos = inChapterPos * 1.0f / (curChapter.getLength());
					progress += pos * cell;
				}
				progress *= 100;

				// 修正阅读至最后一页时的阅读进度
				if (mBook.isLastChapter(curChapter) && end == curChapter.getLastPos()) {
					progress = 100.00f;
				}
				mBook.getReadInfo().setLastReadInfo(lastPos, Long.valueOf(curChapter.getStartPos()).intValue(),
						curChapter.getGlobalId());
			}

		}

		progress = Util.formatFloat(progress, 2);
		mBook.getReadInfo().setLastReadPercent(progress);
	}

	/**
	 * 得到当前的书签
	 * 
	 * @return
	 */
	public MarkItem getCurBookMark() {
		if (mBookModel == null) {
			return null;
		}
		int begin = getScrollReadPos();
		if (begin < 0) {
			begin = mBookModel.getMbBufferBegin();
		}
		return mBook.getCurrentMark(begin);
	}

	/**
	 * 添加书签
	 * 
	 * @return
	 */
	public MarkItem addBookMark() {
		if (mBookModel == null) {
			return null;
		}

		// 获取3个值
		int begin = mBookModel.getMbBufferBegin();
		int end = mBookModel.getMbBufferEnd();

		String percent = getCurrentPercentString();
		String currentTime = mFormat.format(new Date());
		final Chapter curChapter = mBook.getCurrentChapter(begin);

		if (null == curChapter) {
			return null;
		}

		// TODO:
		// String chapterId =
		// String.format(SinaBookApplication.gContext.getString(R.string.chapter_format),
		// curChapter.getChapterId());

		String chapterTitle = curChapter.getTitle();

		Calendar calendar = Calendar.getInstance();
		String date = String.format(SinaBookApplication.gContext.getString(R.string.date_format),
				calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));

		String content;
		int scrollBegin = getScrollReadPos();
		String scrollContent = getScrollReadContent();
		if (scrollBegin >= 0) {
			begin = scrollBegin;
			content = scrollContent;
		} else {
			content = getShareWeiboString(true);
		}

		// TODO:
		String chapterId = null;
		if (curChapter.getLength() > 0) {
			chapterId = String.valueOf(curChapter.getChapterId());
		} else {
			chapterId = String.format(SinaBookApplication.gContext.getString(R.string.chapter_format),
					curChapter.getChapterId());
		}

		// 创建一个书签
		String bid = mBook.getBookId();
		if (bid == null || bid.length() == 0) {
			bid = mBook.getSid();
		}

		if (TextUtils.isEmpty(bid)) {
			// 本地导入书籍
			bid = "local_" + String.valueOf(mBook.getId());
		}
		final MarkItem item = new MarkItem(bid, 0, begin, end, content, percent, currentTime, chapterId, chapterTitle,
				date);

		if (mBook.isSingleChapterBook() || mBook.isComplete()) {
			// if (mBook.isSingleChapterBook() || (mBook.isComplete() &&
			// mBookModel.getTotalPage() != 0)) {
			item.setMarkPosInfo(begin, end, 0, 0);
		} else {
			item.setMarkPosInfo(begin, end, Long.valueOf(curChapter.getStartPos()).intValue(), curChapter.getGlobalId());
		}
		mBook.addBookmark(item);

		// TODO CJL 解决BugID=21165
		// if (mBook.getId() > -1 && mBook.getDownloadInfo().getLocationType()
		// == Book.BOOK_LOCAL) {
		// if (mBook.getId() > -1 &&
		// DownBookManager.getInstance().hasBook(mBook)) {
		// TODO:ouyang
		new GenericTask() {
			protected TaskResult doInBackground(TaskParams... params) {
				// MarkItem tmpItem = new MarkItem(item);
				// if (curChapter.getLength() > 0) {
				// int tmpBegin = (int) (item.getBegin() - curChapter
				// .getStartPos());
				// int tmpEnd = (int) (item.getEnd() - curChapter
				// .getStartPos());
				// tmpItem.setBegin(tmpBegin);
				// tmpItem.setBegin(tmpEnd);
				// }
				//
				// Log.d("ouyang", "tmpItem---begin: " +
				// tmpItem.getBegin());
				// Log.d("ouyang", "tmpItem---end: " + tmpItem.getEnd());
				// DBService.addBookMark(tmpItem);
				DBService.addBookMark(item);
				return null;
			}
		}.execute();
		// }

		UserActionManager.getInstance().recordEvent(Constants.ACTION_ADD_MARK);

		return item;
	}

	/**
	 * 删除书签
	 * 
	 * @param markItem
	 */
	public void deleteBookMark(final MarkItem markItem) {
		mBook.getBookmarks().remove(markItem);
		// if (mBook.getDownloadInfo().getLocationType() == Book.BOOK_LOCAL
		// || DownBookManager.getInstance().hasBook(mBook)) {
		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.deleteBookMark(markItem);
				return null;
			}
		}.execute();
		// }
	}

	/**
	 * 删除书摘
	 * 
	 */
	public void deleteBookSummary(final BookSummary bookSummary) {
		mBook.getBookSummaries().remove(bookSummary);
		// if (mBook.getDownloadInfo().getLocationType() == Book.BOOK_LOCAL
		// || DownBookManager.getInstance().hasBook(mBook)) {
		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.deleteBookSummary(bookSummary);
				return null;
			}
		}.execute();
		// }
	}

	/**
	 * 添加书摘
	 * 
	 * @return
	 */
	public BookSummary addBookSummary() {
		if (mBookModel == null || mBookPage.getSelectText().length <= 0) {
			return null;
		}

		// 获取3个值
		int begin = mBookModel.getMbBufferBegin();
		int end = mBookModel.getMbBufferEnd();

		String percent = getCurrentPercentString();
		String currentTime = mFormat.format(new Date());
		Chapter curChapter = mBook.getCurrentChapter(begin);

		if (null == curChapter) {
			return null;
		}

		// TODO:
		// String chapterId = String.format(
		// ResourceUtil.getString(R.string.chapter_format),
		// curChapter.getChapterId());
		String chapterId = String.valueOf(curChapter.getChapterId());
		String chapterTitle = curChapter.getTitle();

		Calendar calendar = Calendar.getInstance();
		String date = String.format(ResourceUtil.getString(R.string.date_format), calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DATE));

		// 取得选中文字相关信息
		int offset = mBookPage.getSelectText().begin;
		int length = mBookPage.getSelectText().length;
		String content = mBookPage.getSelectText().content;

		// 创建一个书摘，删除相关书摘
		String bid = mBook.getBookId();
		if (bid == null || bid.length() == 0) {
			bid = mBook.getSid();
		}

		if (TextUtils.isEmpty(bid)) {
			// 本地导入书籍
			bid = "local_" + String.valueOf(mBook.getId());
		}

		final BookSummary item = new BookSummary(bid, 0, begin, end, offset, length, content, percent, currentTime,
				chapterId, chapterTitle, date);
		if (mBook.isSingleChapterBook() || (mBook.isComplete() && mBookModel.getTotalPage() != 0)) {
			item.setSummaryPosInfo(begin, end, offset, length, 0, 0);
		} else {
			item.setSummaryPosInfo(begin, end, offset, length, Long.valueOf(curChapter.getStartPos()).intValue(),
					curChapter.getGlobalId());
		}

		final ArrayList<BookSummary> relateSummarys = mBookPage.findRelatePageSummaries(item);

		mBook.addBookSummary(item);
		mBook.getBookSummaries().removeAll(relateSummarys);

		// if (mBook.getId() > -1 && mBook.getDownloadInfo().getLocationType()
		// == Book.BOOK_LOCAL) {
		// if (mBook.getId() > -1 &&
		// DownBookManager.getInstance().hasBook(mBook)) {
		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.addBookSummary(item, relateSummarys);
				return null;
			}
		}.execute();
		// }

		UserActionManager.getInstance().recordEvent(Constants.ACTION_ADD_SUMMARY);

		return item;
	}

	/**
	 * 得到分享到微博的字符串
	 * 
	 * @param isReading
	 *            是否分享阅读内容
	 * 
	 * @return
	 */
	public String getShareWeiboString(boolean isReading) {
		StringBuilder msgBuilder = new StringBuilder();

		if (isReading) {
			for (TextLine line : getLines()) {
				msgBuilder.append(line.getContent());
			}
		} else {
			msgBuilder.append(mBook.getIntro());
		}

		String msgString = msgBuilder.toString().replaceAll(BOOK_CONTENT_PATTERN, "");
		if (msgString != null && msgString.length() > SHARE_WEIBO_MAX_NUM) {
			msgString = msgString.substring(0, SHARE_WEIBO_MAX_NUM);
		}
		return msgString;
	}

	/**
	 * 计算指定章节页数
	 * 
	 * @param chapter
	 */
	public void calculateChapterPages(final Chapter chapter) {
		if (mBookModel == null) {
			return;
		}

		mBookModel.calculateChapterPages(chapter, mRefreshListener);
	}

	/**
	 * 同步执行计算指定章节页数
	 * 
	 * @param chapter
	 */
	public void calculateChapterPagesSync(final Chapter chapter) {
		if (mBookModel == null) {
			return;
		}
		mBookModel.calculateChapterPages(chapter);
	}

	/*-----------------------依赖bookmodel end-------------------------*/

	/*-----------------------self private start-------------------------*/
	/**
	 * 构建一页的数据
	 * 
	 * @param pageContent
	 */
	private void createBookPage(PageContent pageContent) {
		mBookPage = pageContent;
		if (!mBookPage.isEmpty()) {
			updateBookPage();
		}
	}

	/**
	 * 刷新一页的数据
	 */
	private void updateBookPage() {
		int begin = mBookModel.getMbBufferBegin();
		int end = mBookModel.getMbBufferEnd();

		List<BookSummary> summaries = mBook.getBookSummaries();
		if (!summaries.isEmpty()) {

			mBookPage.clearPageSummary();
			for (BookSummary summary : summaries) {
				int summaryStart = (int) summary.getOffset();
				int summaryEnd = (int) (summaryStart + summary.getLength());
				String content = summary.getContent();

				if (summaryStart >= begin && summaryEnd <= end) {
					mBookPage.addPageSummary(new PageSummary(summaryStart, summaryEnd, content, summary));

				} else if (summaryStart < begin && summaryEnd > begin && summaryEnd <= end) {
					mBookPage.addPageSummary(new PageSummary(begin, summaryEnd, content, summary));

				} else if (summaryStart >= begin && summaryStart < end && summaryEnd > end) {
					mBookPage.addPageSummary(new PageSummary(summaryStart, summaryEnd, content, summary));

				} else if (summaryStart < begin && summaryEnd > end) {
					mBookPage.addPageSummary(new PageSummary(begin, summaryEnd, content, summary));
				}
			}
		} else {
			mBookPage.clearPageSummary();
		}

		// 针对本地导入或者微盘的书籍(整本书只有一个全本章节)不进行全章节计算
		if ((mBook.isLocalImportBook() || mBook.isVDiskBook()) && mBook.isSingleChapterBook())
			return;

		Chapter chapter = getCurrentChapter();
		if (chapter != null && chapter.getTotalPage() <= 1) {
			calculateChapterPagesSync(chapter);
		}
	}

	private void draw(PageBitmap pageBitmap) {
		if (mBookPage.isEmpty() || mBookModel == null) {
			// 至少画个背景
			if (pageBitmap != null) {
				drawContentBg(pageBitmap.getCanvas());
			}
			return;
		}

		// 封装
		constructPaints();
		drawContent(pageBitmap);
		Chapter c = mBookModel.getCurrentChapter();
		drawTitle(pageBitmap, c);

		// 绘制底部文字栏(带电量)
		drawBottomText(pageBitmap, c);
	}

	@Override
	public Selection getStartSelection() {
		return mBookPage.getStartSelection();
	}

	@Override
	public Selection getEndSelection() {
		return mBookPage.getEndSelection();
	}

	@Override
	public void findSelection(float x, float y, int index) {
		mBookPage.findSelection(x, y, index);
	}

	@Override
	public void clearSelection() {
		mBookPage.clearSelection();
	}

	@Override
	public boolean isSelectionsLegal() {
		return mBookPage.isSelectionsLegal() && !isVipChapter();
	}

	@Override
	public boolean isVipChapter() {
		Chapter curChapter = getCurrentChapter();
		if (curChapter != null && curChapter.isVip()) {
			return true;
		}

		return false;
	}

	public String getSelectText() {
		return mBookPage.getSelectText().content;
	}

	public void changeReadAnim() {
		int begin = getScrollReadPos();

		if (begin >= 0) {
			seek(begin);
		}
	}

	/**
	 * 得到滑动翻页的阅读位置
	 * 
	 * @return
	 */
	private int getScrollReadPos() {
		int begin = -1;
		if (mCurPage != null && mCurPage.getReadPosScroll() >= 0) {
			begin = mCurPage.getReadPosScroll();
		} else if (mNextPage != null && mNextPage.getReadPosScroll() >= 0) {
			begin = mNextPage.getReadPosScroll();
		}
		return begin;
	}

	/**
	 * 得到滑动翻页当前内容
	 * 
	 * @return
	 */
	private String getScrollReadContent() {
		String content = null;
		// if (mCurPage != null) {
		if (mCurPage != null && mCurPage.getReadPosScroll() >= 0) {
			content = mCurPage.getReadPosContent();
		} else if (mNextPage != null && mNextPage.getReadPosScroll() > 0) {
			content = mNextPage.getReadPosContent();
		}
		return content;
	}

	/**
	 * 画阅读内容
	 * 
	 * @param pageBitmap
	 */
	private void drawContent(PageBitmap pageBitmap) {
		if (!mBookPage.isEmpty()) {
			// 正常画出内容
			if (pageBitmap != null) {
				pageBitmap.setRelatePage(mBookPage);
				pageBitmap.setReadBg(mReadStyleManager.getReadMode(), mReadStyleManager.getReadBgResId());
				mBookPage.draw(pageBitmap.getCanvas());
			}
		} else {
			pageBitmap.setRelatePage(null);
		}
	}

	/**
	 * 画阅读头部信息
	 * 
	 * @param pageBitmap
	 * @param c
	 */
	private void drawTitle(PageBitmap pageBitmap, Chapter c) {
		if (c != null) {
			String title = c.getTitle();
			float totalWidth = mReadStyleManager.getVisibleWidth();
			float titleWidth = mHeaderPaint.measureText(title);

			float titleStartX = mReadStyleManager.getLeftX();
			float titleStartY = mReadStyleManager.getTitleStartY();

			if (titleWidth > totalWidth) {
				int index = mHeaderPaint.breakText(title, true, totalWidth, null);
				title = title.substring(0, index - 3) + "...";
			}

			pageBitmap.setTitle(title);
			pageBitmap.getCanvas().drawText(title, titleStartX, titleStartY, mHeaderPaint);
		}
	}

	/**
	 * 画阅读页底部信息
	 * 
	 * @param pageBitmap
	 * @param chapter
	 */
	private void drawBottomText(PageBitmap pageBitmap, Chapter chapter) {
		float footerStartX = mReadStyleManager.getLeftX();
		float footerStartY = mReadStyleManager.getFooterStartY();

		if (chapter != null) {
			// 画电量图片
			Bitmap bitmap = mBatteryView.getBitmap();
			// float top = footerStartY - (bitmap.getHeight() +
			// mFooterPaint.getTextSize()) / 2 + 3;
			float top = footerStartY - bitmap.getHeight() + 3;
			pageBitmap.getCanvas().drawBitmap(bitmap, footerStartX, top, null);

			// 画时间
			String curTime = mFormat.format(new Date());
			pageBitmap.setBottomTime(curTime);
			pageBitmap.getCanvas().drawText(curTime, footerStartX + bitmap.getWidth() + PixelUtil.dp2px(5),
					footerStartY, mFooterPaint);

			// 画页码
			String pageTagText = "";
			if (mBook.isComplete() && mBookModel.getTotalPage() != 0) {
				// 按整本书籍显示
				pageTagText = String.format(ResourceUtil.getString(R.string.reading_page_local),
						mBookModel.getCurrentPage(), mBookModel.getTotalPage());

				float pageTipWidth = mFooterPaint.measureText(pageTagText);
				float pageTipStart = mReadStyleManager.getRightX() - pageTipWidth;
				pageBitmap.setBottomPage(pageTagText);
				pageBitmap.getCanvas().drawText(pageTagText, pageTipStart, footerStartY, mFooterPaint);
				return;
			} else if (mBookModel.getTotalPage(chapter) != 0) {

				int currPageIndex = mBookModel.getCurrentPage(chapter);
				int totalPageIndex = mBookModel.getTotalPage(chapter);
				// 按章节显示
				pageTagText = String.format(ResourceUtil.getString(R.string.reading_page_online), currPageIndex,
						totalPageIndex);
				float pageTipWidth = mFooterPaint.measureText(pageTagText);
				float pageTipStart = mReadStyleManager.getRightX() - pageTipWidth;
				pageBitmap.setBottomPage(pageTagText);
				pageBitmap.getCanvas().drawText(pageTagText, pageTipStart, footerStartY, mFooterPaint);
				return;
			}
		}

		String parseTip = ResourceUtil.getString(R.string.reading_page_parsing);
		float parseTipWidth = mFooterPaint.measureText(parseTip);
		float parseTipStart = ((mReadStyleManager.getRightX() - parseTipWidth) / 2);
		pageBitmap.setBottomParsing(parseTip);
		pageBitmap.getCanvas().drawText(parseTip, parseTipStart, footerStartY, mFooterPaint);
	}

	private void initPageBitmapAndCanvas(int displayWidth, int displayHeight) {
		if (mCurPage != null && mNextPage != null) {
			return;
		}
		mCurPage = new PageBitmap(displayWidth, displayHeight);
		mNextPage = new PageBitmap(displayWidth, displayHeight);
	}

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

	private List<TextLine> getLines() {
		List<TextLine> lines;
		if (mBookPage.isEmpty()) {
			if (mBookModel != null) {
				lines = mBookModel.curPage().getPageStringLines();
			} else {
				lines = new ArrayList<TextLine>();
			}
		} else {
			lines = mBookPage.getPageStringLines();
		}
		return lines;
	}

	/**
	 * 通过styleManager装配所有新的画笔
	 */
	private void constructPaints() {
		mHeaderPaint = mReadStyleManager.getHeaderPaint();
		mFooterPaint = mReadStyleManager.getFooterPaint();

		if (mBatteryView.getHeight() != mFooterPaint.getTextSize()) {
			mBatteryView.updateSize(-1, mFooterPaint.getTextSize());
			mBatteryView.draw();
		}
	}

	@Override
	public void reDrawBattery() {
		if (mBatteryView != null) {
			mBatteryView.draw();
		}
	}

	/*-----------------------self private end-------------------------*/
}
