package com.sina.book.control.download;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.db.DBService;
import com.sina.book.parser.UpdateBooksResultParser;
import com.sina.book.ui.notification.PushUpdatedNotification;
import com.sina.book.ui.notification.UpdateFinishedNotification;
import com.sina.book.util.CalendarUtil;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;

/**
 * 章节更新的管理器.
 * 
 * @author MarkMjw
 */
public class UpdateChapterManager
{
	private static final String				TAG				= "UpdateChapterManager";

	/**
	 * 自动更新类型.
	 */
	public static final String				REQ_AUTO		= "req_auto";
	/**
	 * 手动更新类型.
	 */
	public static final String				REQ_BY_USER		= "req_by_user";
	/**
	 * 单本书籍更新类型.
	 */
	public static final String				REQ_SINGLE_BOOK	= "req_single_book";
	/**
	 * 推送更新类型.
	 */
	public static final String				REQ_PUSH		= "req_push";

	/**
	 * 更新章节完成的监听器.
	 */
	private ArrayList<IUpdateBookListener>	mListeners		= new ArrayList<IUpdateBookListener>();

	private static UpdateChapterManager		mInstance		= new UpdateChapterManager();

	/**
	 * 检查更新的Task
	 */
	private UpdateCheckTask					mUpdateTask;

	private UpdateChapterManager()
	{
	}

	public static UpdateChapterManager getInstance()
	{
		return mInstance;
	}

	/**
	 * 删除更新章节事件监听器
	 * 
	 * @param listener
	 */
	public void removeUpdateBookListener(IUpdateBookListener listener)
	{
		mListeners.remove(listener);
	}

	/**
	 * 添加更新章节事件监听器
	 * 
	 * @param mListener
	 */
	public void addUpdateBookListener(IUpdateBookListener mListener)
	{
		if (!mListeners.contains(mListener)) {
			mListeners.add(mListener);
		}
	}

	/**
	 * 更新启动时间，假如上一次启动时间不是今天，<br>
	 * 则检查章节更新，否则不检查，更新启动时间.
	 */
	public void autoCheckNewChapter()
	{
		if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
			return;
		}

		String nowDate = CalendarUtil.getCurrentTImeWithFormat("yyyy-MM-dd");
		String oldDate = StorageUtil.getString(StorageUtil.KEY_LANUCH_DATE, "1970-01-01");

		// 当日期发生变化时才进行检查更新
		if (!nowDate.equals(oldDate)) {
			checkNewChapter(REQ_AUTO);
			// 更新启动时间
			StorageUtil.saveString(StorageUtil.KEY_LANUCH_DATE, nowDate);
			StorageUtil.saveLong(StorageUtil.KEY_UPDATE_TIME, System.currentTimeMillis());
		}
	}

	/**
	 * 定时检查章节更新
	 */
	public void checkNewChapterByTimer()
	{
		if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
			return;
		}

		// 首先初始化书籍缓存
		DownBookManager.getInstance().init();

		checkNewChapter(REQ_PUSH);
		// 更新启动时间
		StorageUtil.saveLong(StorageUtil.KEY_UPDATE_TIME, System.currentTimeMillis());
	}

	/**
	 * 检查章节更新
	 */
	public void checkNewChapter(String reqType)
	{
		ArrayList<DownBookJob> jobs = DownBookManager.getInstance().getAllJobs();
		List<Book> needCheckBooks = new ArrayList<Book>();

		if (jobs != null) {
			for (DownBookJob job : jobs) {
				Book book = job.getBook();

				// 只检查连载但不是在线的书籍
				if (null != book && book.isSeriesBook() && !book.isOnlineBook()) {
					// 只有检查状态为下载成功或准备的书籍
					if (job.getState() == DownBookJob.STATE_FINISHED) {
						if (!needCheckBooks.contains(book)) {
							needCheckBooks.add(book);
						}
					}
				}
			}
		}

		checkNewChapter(needCheckBooks, reqType);
	}

	/**
	 * 收到Push通知后检查更新
	 * 
	 * @param book
	 *            带更新的书Log
	 */
	public void checkNewChapterAfterPush(Book book)
	{
		if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
			// 无网络
			return;
		}

		if (null == book || TextUtils.isEmpty(book.getBookId())) {
			// 书籍信息为空
			return;
		}

		Book localBook = DownBookManager.getInstance().getBook(book);
		if (null == localBook) {
			// 本地没有此书
			return;
		}

		List<Book> books = new ArrayList<Book>();
		books.add(DownBookManager.getInstance().getBook(book));

		checkNewChapter(books, REQ_PUSH);
	}

	/**
	 * 检查章节更新，传入需要检查的书籍<br>
	 * 
	 * @param needCheckBooks
	 * @param reqType
	 */
	public void checkNewChapter(List<Book> needCheckBooks, String reqType)
	{
		if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
			callbackAllListener();
			showToast(R.string.network_unconnected);
			return;
		}

		if (needCheckBooks.size() > 0) {
			if (mUpdateTask == null || mUpdateTask.isDead()) {
				mUpdateTask = new UpdateCheckTask(needCheckBooks, reqType);
				mUpdateTask.start();

			} else {
				// 当用户手动更新或者单本书检查更新时，先关闭当前任务
				if (REQ_BY_USER.equals(reqType) || REQ_SINGLE_BOOK.equals(reqType)) {
					mUpdateTask.shutDown();
					mUpdateTask = new UpdateCheckTask(needCheckBooks, reqType);
					mUpdateTask.start();
				}
			}
		} else {
			// 不是Push更新则需要回调
			if (!REQ_PUSH.equals(reqType)) {
				callbackAllListener();
				showToast(R.string.nothing_update);
			} else {
				LogUtil.i(TAG, "PushCheckNewChapter: No books need check.");
			}
		}
	}

	/**
	 * 回调所有监听器
	 */
	private void callbackAllListener()
	{
		// 找不到可更新的书籍，直接返回，停止更新
		for (IUpdateBookListener listener : mListeners) {
			if (listener != null) {
				listener.updateAllBookFinished();
			}
		}
	}

	private void showToast(int resId)
	{
		showToast(ResourceUtil.getString(resId));
	}

	private void showToast(String text)
	{
		Toast.makeText(SinaBookApplication.gContext, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 检查章节更新的线程
	 * 
	 * @author Tsimle
	 */
	private class UpdateCheckTask extends Thread
	{
		private volatile boolean	mTerminated	= false;

		/**
		 * 使用handler与UI线程交互
		 */
		private Handler				mHandler;

		/**
		 * 需要检查的书籍
		 */
		private List<Book>			mNeedCheckBooks;

		/**
		 * 实际执行网络请求的task
		 */
		private RequestTask			mReqTask;

		/**
		 * 请求类型
		 */
		private String				mReqType;

		public UpdateCheckTask(List<Book> needCheckBooks, String reqType)
		{
			mNeedCheckBooks = needCheckBooks;
			mReqType = reqType;

			mHandler = new Handler(SinaBookApplication.gContext.getMainLooper());
		}

		public boolean isDead()
		{
			return mTerminated;
		}

		public void shutDown()
		{
			mTerminated = true;
			if (mReqTask != null) {
				mReqTask.abort();
			}
		}

		@Override
		public void run()
		{
			try {
				if (mTerminated) {
					return;
				}

				// 组织请求信息
				String reqUrl = getUrl(mNeedCheckBooks);

				if (TextUtils.isEmpty(reqUrl)) {
					return;
				}

				mReqTask = new RequestTask(Process.THREAD_PRIORITY_LOWEST, new UpdateBooksResultParser());
				TaskParams params = new TaskParams();
				params.put(RequestTask.PARAM_URL, reqUrl);
				params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
				mReqTask.setType(mReqType);
				TaskResult taskResult = mReqTask.syncExecute(params);

				taskFinished(taskResult);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				LogUtil.i(TAG, "UpdateCheckTask terminated.");
				mTerminated = true;
			}
		}

		private String getUrl(List<Book> needCheckBooks)
		{
			if (needCheckBooks == null || needCheckBooks.isEmpty()) {
				return null;
			}

			String value = "";
			for (int i = 0; i < needCheckBooks.size(); i++) {
				// 从缓存中读取待检查更新的Book对象
				Book book = DownBookManager.getInstance().getBook(needCheckBooks.get(i));

				// 构造bid_cid_sNum的字符串，没有s_num时只构造bid_cid
				value += book.getBookId();
				value += "_";

				Chapter lastChapter = getLastChapter(book);
				if (null != lastChapter && lastChapter.getSerialNumber() > 0) {
					value += lastChapter.getGlobalId();
					value += "_";
					value += lastChapter.getSerialNumber();
				} else {
					value += getMaxGlobalChapterId(book);
				}

				if (i < needCheckBooks.size() - 1) {
					value += "|";
				}
			}

			if (TextUtils.isEmpty(value)) {
				return value;
			} else {
				try {
					value = URLEncoder.encode(value, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					return null;
				}
			}

			return String.format(ConstantData.URL_UPDATE_BOOKS, value);
		}

		private int getMaxGlobalChapterId(Book book)
		{
			ArrayList<Chapter> chapters = book.getChapters();

			// 如果需要，数据库取章节
			if (chapters == null || chapters.isEmpty()) {
				book.parseChapters();
				chapters = book.getChapters();
			}

			int maxGlobalChapterId = 0;

			if (null != chapters && !chapters.isEmpty()) {
				int findFromLastLen = 30;
				int chaptersSize = chapters.size();
				if (chaptersSize < 30) {
					findFromLastLen = chaptersSize;
				}

				// 从章节最后开始遍历，最多30条已经能排除干扰的因素
				for (int i = chaptersSize - 1; i >= chaptersSize - findFromLastLen; i--) {
					Chapter chapter = chapters.get(i);
					if (chapter.getGlobalId() > maxGlobalChapterId) {
						maxGlobalChapterId = chapter.getGlobalId();
					}
				}
			}

			return maxGlobalChapterId;
		}

		private Chapter getLastChapter(Book book)
		{
			ArrayList<Chapter> chapters = book.getChapters();

			// 如果需要，数据库取章节
			if (chapters == null || chapters.isEmpty()) {
				book.parseChapters();
				chapters = book.getChapters();
			}

			// 这里获取最大的s_num对应的Chapter
			if (null != chapters && !chapters.isEmpty()) {
				int findFromLastLen = 30;
				int chaptersSize = chapters.size();
				if (chaptersSize < 30) {
					findFromLastLen = chaptersSize;
				}

				Chapter chapter = chapters.get(chapters.size() - 1);
				// 从章节最后开始遍历，最多30条已经能排除干扰的因素
				int sNumber = -1;
				for (int i = chaptersSize - 1; i >= chaptersSize - findFromLastLen; i--) {
					Chapter tempChapter = chapters.get(i);
					if (tempChapter.getSerialNumber() > sNumber) {
						chapter = tempChapter;
						sNumber = chapter.getSerialNumber();
					}
				}

				return chapter;
			}

			return null;
		}

		@SuppressWarnings("unchecked")
		private void taskFinished(TaskResult taskResult)
		{
			// 根据返回结果，判断是否需要更新
			if (taskResult.stateCode == HttpStatus.SC_OK && taskResult.retObj != null) {
				final String reqType = taskResult.task.getType();
				LogUtil.i(TAG, "Request Type -> " + reqType);

				// 修正返回的数据
				final List<Book> books = correctData((List<Book>) taskResult.retObj);

				if (!books.isEmpty()) {
					for (Book book : books) {
						updateBook(book);
					}

					saveUpdateBookTitle(books);
				}

				updateUI(reqType, books);
			}
		}

		private List<Book> correctData(List<Book> bookList)
		{
			List<Book> books = new ArrayList<Book>();
			books.addAll(bookList);

			if (!bookList.isEmpty()) {
				for (Book book : bookList) {
					Book localBook = DownBookManager.getInstance().getBook(book);
					ArrayList<Chapter> chapters = localBook.getChapters();

					// 如果需要，数据库取章节
					if (chapters == null || chapters.isEmpty()) {
						localBook.parseChapters();
						chapters = book.getChapters();
					}

					ArrayList<Chapter> newChapters = new ArrayList<Chapter>();
					newChapters.addAll(book.getChapters());

					// 过滤掉比本地已有的章节
					for (Chapter newChapter : newChapters) {
						// 比较章节global id
						if (chapters.contains(newChapter)) {
							book.getChapters().remove(newChapter);
						}
					}

					if (book.getChapters().size() <= 0) {
						books.remove(book);
					}
				}
			}

			return books;
		}

		private void updateBook(Book tempBook)
		{
			Book localBook = DownBookManager.getInstance().getBook(tempBook);
			// 首先清除Book对象中原有章节new标签
			localBook.clearAllChapterNewTag();
			ArrayList<Chapter> chapters = localBook.getChapters();

			// 如果需要，数据库取章节
			if (chapters == null || chapters.isEmpty()) {
				localBook.parseChapters();
				// 首先清除Book对象中原有章节new标签
				localBook.clearAllChapterNewTag();
				chapters = localBook.getChapters();
			}

			if (chapters != null && !chapters.isEmpty()) {
				ArrayList<Chapter> newChapters = tempBook.getChapters();

				// 1、先找到当前书籍章节列表的章节最大id
				int localMaxChapterId = chapters.get(chapters.size() - 1).getChapterId();

				// 2、给新的章节设置Id、length、tag等信息
				for (Chapter newChapter : newChapters) {
					newChapter.setChapterId(++localMaxChapterId);
					newChapter.setStartPos(0);
					// 由于没有内容，长度统统设为0
					newChapter.setLength(0);
					newChapter.setTag(Chapter.NEW);
				}
				// 将更新后的章节设置给Book对象
				tempBook.setChapters(newChapters);
				 
				// 3、更新新的章节列表到本地
				if(localBook.isHtmlRead() 
						&& !localBook.isOnlineBook()){
					//ouyang： 图文书籍，不是在线状态，不要更新 最新章节到数据库，防止下次刷新无法刷到新章节，而造成图文新章节不能下载，丢失掉。
					localBook.setUpdateChaptersNum(newChapters.size());
				}else{
					chapters.addAll(newChapters);
					localBook.setChapters(chapters);
					DBService.updateNewChapter(localBook, newChapters);
					
					// 更新章节数
					localBook.setUpdateChaptersNum(newChapters.size());
					localBook.setNum(chapters.size());
					localBook.setLastUpdateTime(System.currentTimeMillis());
					DBService.saveBook(localBook);
				}

				// 检查到某书更新，通知UI线程
				final Book updatedBook = localBook;
				mHandler.post(new Runnable()
				{

					@Override
					public void run()
					{
						for (IUpdateBookListener listener : mListeners) {
							if (listener != null) {
								listener.updateBookFinished(updatedBook);
							}
						}
					}
				});
			}
		}

		private void saveUpdateBookTitle(List<Book> books)
		{
			// 保存更新的两本书
			List<Book> saveBooks = new ArrayList<Book>();
			for (Book book : books) {
				saveBooks.add(DownBookManager.getInstance().getBook(book));

				if (saveBooks.size() >= 2)
					break;
			}
			StorageUtil.saveBooks(saveBooks, StorageUtil.KEY_CACHE_REMIND_BOOK);
		}

		/**
		 * 更新检查完毕，通知UI线程更新结果
		 * 
		 * @param reqType
		 * @param books
		 */
		private void updateUI(final String reqType, final List<Book> books)
		{
			final int updateCount = books.size();

			LogUtil.i(TAG, "Task finished, updated books' -> " + updateCount);

			mHandler.post(new Runnable()
			{

				@Override
				public void run()
				{
					callbackAllListener();

					if (updateCount == 0) {
						if (REQ_SINGLE_BOOK.equals(reqType)) {
							showToast(R.string.book_no_update);

						} else if (REQ_BY_USER.equals(reqType)) {
							showToast(R.string.nothing_update);
						}

					} else {
						if (REQ_AUTO.equals(reqType)) {
							UpdateFinishedNotification.getInstance().showNotification(updateCount);

						} else if (REQ_PUSH.equals(reqType)) {
							showPushNotify(books);

						} else if (REQ_BY_USER.equals(reqType)) {
							String format = ResourceUtil.getString(R.string.notification_update_finish);
							showToast(String.format(format, updateCount));

						} else if (REQ_SINGLE_BOOK.equals(reqType)) {
							String format = ResourceUtil.getString(R.string.book_update_finish);
							showToast(String.format(format, books.get(0).getChapters().size()));
						}
					}
				}
			});
		}

		private void showPushNotify(List<Book> books)
		{
			for (Book book : books) {
				Book jobBook = DownBookManager.getInstance().getBook(book);

				if (jobBook.isRemind()) {
					ArrayList<Chapter> chapters = book.getChapters();

					if (!chapters.isEmpty()) {
						Chapter chapter = chapters.get(chapters.size() - 1);
						PushUpdatedNotification.getInstance().addNotification(jobBook, chapter);
					}
				}
			}
		}
	}

	/**
	 * 章节更新完成监听器
	 * 
	 * @author MarkMjw
	 */
	public interface IUpdateBookListener
	{

		/**
		 * 书籍全部更新完成的回调
		 */
		public void updateAllBookFinished();

		/**
		 * 某本书更新完成的回调
		 */
		public void updateBookFinished(Book book);
	}
}
