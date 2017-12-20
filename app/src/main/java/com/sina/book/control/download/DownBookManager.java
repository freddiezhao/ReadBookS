package com.sina.book.control.download;

import static com.sina.book.data.Book.BOOK_LOCAL;
import static com.sina.book.data.Book.HAS_READ;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.BookDownloadInfo;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.db.DBService;
import com.sina.book.reader.SinaBookPageFactory;
import com.sina.book.ui.SinaAppLoginActivity;
import com.sina.book.ui.notification.DownloadBookNotification;
import com.sina.book.util.CalendarUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;

/**
 * 书籍下载管理类.
 * 
 * @author fzx
 */
public class DownBookManager {
	private static final String TAG = "DownBookManager";

	/** The Constant RUNNING_MAX. */
	private static final int RUNNING_MAX = 2;

	/** 创建下载任务OK. */
	public static final int FLAG_START_CACHE = 0;

	/** 该书籍不支持下载. */
	public static final int FLAG_UNSUPPORT = 1;

	/** 已经下载完成. */
	public static final int FLAG_CACHED = 2;

	/** 已经在下载列表. */
	public static final int FLAG_CACHING = 3;

	/** 错误. */
	public static final int FLAG_ERROR = 4;

	/** 正在运行的任务个数. */
	private int mRunningCount = 0;

	/** The m instance. */
	private static DownBookManager mInstance;

	/** The m update listeners. */
	private ArrayList<ITaskUpdateListener> mUpdateListeners = new ArrayList<ITaskUpdateListener>();

	/** The m book jobs. */
	private BookArrayList mBookJobs = new BookArrayList();

	/** 广播 */
	public static final String ACTION_INTENT_DOWNSTATE = "com.sina.book.downloadstate";
	public static final String ACTION_INTENT_RECOMMANDSTATE = "com.sina.book.recommandstate";

	/** 下载完成listener. */
	private IDownTaskFinishListener mDownFinishListener = new IDownTaskFinishListener() {

		@Override
		public void onTaskFinished(DownResult taskResult) {
			DownBookJob job = null;
			mRunningCount--;
			if (taskResult != null) {
				if (taskResult.task instanceof DownBookTask) {
					DownBookTask task = (DownBookTask) taskResult.task;
					job = task.getJob();
					Book book = job.getBook();

					if (taskResult.stateCode != -1) {
						if (ConstantData.CODE_RECHARGE.equals(""
								+ taskResult.stateCode)) {
							doError(job);

							// LogUtil.d(TAG, "DownBookManager code : " +
							// taskResult.stateCode);
							// LogUtil.d(TAG, "DownBookManager message : 余额不足");

							onTaskUpdate(job.getBook(), false, true,
									DownBookJob.STATE_RECHARGE);
						} else if (ConstantData.CODE_SUCCESS.equals(""
								+ taskResult.stateCode)) {
							doFinish(job, "DownBookManager(1)");

							// LogUtil.d(TAG, "DownBookManager code : " +
							// taskResult.stateCode);
							// LogUtil.d(TAG, "DownBookManager message : 下载成功");

							onTaskUpdate(job.getBook(), false, true,
									DownBookJob.STATE_FINISHED);
						} else {
							doError(job);

							// LogUtil.d(TAG, "DownBookManager code : " +
							// taskResult.stateCode);
							// LogUtil.d(TAG, "DownBookManager message : 下载失败");

							if (taskResult.retObj != null
									&& !TextUtils.isEmpty(taskResult.retObj
											.toString())) {
								Toast.makeText(SinaBookApplication.gContext,
										taskResult.retObj.toString(),
										Toast.LENGTH_SHORT).show();
							}
							onTaskUpdate(job.getBook(), false, true,
									DownBookJob.STATE_FAILED);
						}
					} else {
						Chapter chapter = DBService.getLastChapter(book);
						// 这里加入对下载的书如果有内容就成功，否则才失败
						if (chapter.getChapterId() >= 1) {
							// 处理下载成功的
							doFinish(job, "DownBookManager(2)");

							// LogUtil.d(TAG, "DownBookManager code : " +
							// taskResult.stateCode);
							// LogUtil.d(TAG, "DownBookManager message : 部分成功");

							onTaskUpdate(job.getBook(), false, true,
									DownBookJob.STATE_FINISHED);
						} else {
							// 处理下载失败的
							doError(job);

							// LogUtil.d(TAG, "DownBookManager code : " +
							// taskResult.stateCode);
							// LogUtil.d(TAG, "DownBookManager message : 下载失败");

							Toast.makeText(SinaBookApplication.gContext,
									R.string.downloading_failed,
									Toast.LENGTH_SHORT).show();
							onTaskUpdate(job.getBook(), false, true,
									DownBookJob.STATE_FAILED);
						}
					}
				} else if (taskResult.task instanceof DownVDiskFileTask
						|| taskResult.task instanceof DownEpubFileTask) {
					// LogUtil.d("FileMissingLog",
					// "DownBookManager >> 微盘文件下载返回 >>");
					if (taskResult.task instanceof DownVDiskFileTask) {
						DownVDiskFileTask task = (DownVDiskFileTask) taskResult.task;
						job = task.getJob();
					} else if (taskResult.task instanceof DownEpubFileTask) {
						DownEpubFileTask task = (DownEpubFileTask) taskResult.task;
						job = task.getJob();
					}

					if (ConstantData.CODE_SUCCESS.equals(""
							+ taskResult.stateCode)) {
						// LogUtil.d("FileMissingLog",
						// "DownBookManager >> 微盘文件下载返回 >> 成功");
						// 处理下载成功的
						doFinish(job, "DownBookManager(3)");

						// LogUtil.d(TAG, "DownBookManager code : " +
						// taskResult.stateCode);
						// LogUtil.d(TAG, "DownBookManager message : 微盘书籍下载成功");

						onTaskUpdate(job.getBook(), false, true,
								DownBookJob.STATE_FINISHED);
					} else {
						// LogUtil.d("FileMissingLog",
						// "DownBookManager >> 微盘文件下载返回 >> 失败");
						// 处理下载失败的
						doError(job);

						// LogUtil.d(TAG, "DownBookManager code : " +
						// taskResult.stateCode);
						// LogUtil.d(TAG, "DownBookManager message : 微盘书籍下载失败");

						Toast.makeText(SinaBookApplication.gContext,
								R.string.downloading_failed, Toast.LENGTH_SHORT)
								.show();
						onTaskUpdate(job.getBook(), false, true,
								DownBookJob.STATE_FAILED);
					}
				}
			}
			Intent intent = new Intent(ACTION_INTENT_DOWNSTATE);
			SinaBookApplication.gContext.sendBroadcast(intent);
			notifyJobFinished();
		}
	};

	/**
	 * Do error.
	 * 
	 * @param job
	 *            the job
	 */
	private void doError(DownBookJob job) {
		Book book = job.getBook();
		book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FAILED);
		saveBookToDb(book, false);
		job.setState(DownBookJob.STATE_FAILED);
		DownloadBookNotification.getInstance().updateNotification(book);
	}

	/**
	 * Do finish.
	 * 
	 * @param job
	 *            the job
	 */
	private void doFinish(DownBookJob job, String debugTagInfo) {
		Book book = job.getBook();
		book.changeFileSuffix();
		book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FINISHED);
		book.getDownloadInfo().setProgress(1.0);
		// 下载成功之后，更新章节信息
		ArrayList<Chapter> chapters = DBService.getAllChapter(book);
		book.setChapters(chapters, "[DownBookManager-doFinish-debugTagInfo="
				+ debugTagInfo + "]");
		book.setNum(chapters.size());

		book.setBookmarks(DBService.getAllBookMark(book));
		book.setBookSummaries(DBService.getAllBookSummary(book));
		book.parsePosFromJson();
		saveBookToDb(book, true);
		job.setState(DownBookJob.STATE_FINISHED);
		DownloadBookNotification.getInstance().updateNotification(book);
	}

	/**
	 * Instantiates a new down book manager.
	 */
	private DownBookManager() {
	}

	/**
	 * Gets the single instance of DownBookManager.
	 * 
	 * @return single instance of DownBookManager
	 */
	public static DownBookManager getInstance() {
		if (mInstance == null) {
			mInstance = new DownBookManager();
		}
		return mInstance;
	}

	/**
	 * 初始化下载管理器，读取数据库数据.
	 * 
	 */
	public void init() {
		initCacheBooks();
	}

	/**
	 * 从数据库初始化缓存和正在缓存的数据.
	 */
	public void initCacheBooks() {
		if (!mBookJobs.isEmpty())
			return;

		ArrayList<Book> books = DBService.getAllBook();
		mBookJobs.clear("initCacheBooks");
		
		if (books != null && books.size() > 0) {
			for (Book book : books) {
//				if(book.isHtmlRead()){
//					// Html阅读
//					String baseDir = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK);
//					String bookDirName = "/bd" + book.getBookId() + ".epub";
//					String path = baseDir + bookDirName;
//					book.getDownloadInfo().setOriginalFilePath(path);
//					book.getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
//					
//					HtmlDownBookManager manager = new HtmlDownBookManager(book, path);
//					if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) > 0.0001
//							|| book.getDownloadInfo().getDownLoadState() != DownBookJob.STATE_FINISHED) {
//						manager.setState(DownBookJob.STATE_FAILED);
//					} else {
//						manager.setState(book.getDownloadInfo().getDownLoadState());
//					}
//					HtmlDownManager.getInstance().mHtmlBookList.add(manager);
//				}else{
					DownBookJob job = new DownBookJob(book);
					if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) > 0.0001
							|| book.getDownloadInfo().getDownLoadState() != DownBookJob.STATE_FINISHED) {
						job.setState(DownBookJob.STATE_FAILED);
					} else {
						job.setState(book.getDownloadInfo().getDownLoadState());
					}
					mBookJobs.add(job);
//				}
			}
		}
	}
	
	/**
	 * 重置缓存的书籍（从数据库中读取）
	 * <ul>
	 * <li>慎用
	 * </ul>
	 */
	public void resetCacheBooks() {
		mBookJobs.clear("resetCacheBooks");
		initCacheBooks();
	}

	/**
	 * 书的最后阅读时间和下载时间比较器
	 */
	private Comparator<DownBookJob> mReadOrDownTimeComparator = new Comparator<DownBookJob>() {
		@Override
		public int compare(DownBookJob job1, DownBookJob job2) {
			Book book1 = job1.getBook();
			Book book2 = job2.getBook();
			if (book1 != null && book2 != null) {
				long book1LastReadTime = Math.max(book1.getReadInfo()
						.getLastReadTime(), book1.getDownloadInfo()
						.getDownloadTime());

				long book2LatsReadTime = Math.max(book2.getReadInfo()
						.getLastReadTime(), book2.getDownloadInfo()
						.getDownloadTime());

				if (book1LastReadTime > book2LatsReadTime) {
					return -1;
				} else if (book1LastReadTime < book2LatsReadTime) {
					return 1;
				}
			}
			return 0;
		}
	};
	

	/**
	 * 书的下载时间比较器
	 */
	// private Comparator<DownBookJob> mDownTimeComparator = new
	// Comparator<DownBookJob>() {
	// @Override
	// public int compare(DownBookJob job1, DownBookJob job2) {
	// Book book1 = job1.getBook();
	// Book book2 = job2.getBook();
	//
	// if (book1 != null && book2 != null) {
	// long book1DownTime = book1.getDownloadInfo().getDownloadTime();
	//
	// long book2DownTime = book2.getDownloadInfo().getDownloadTime();
	//
	// if (book1DownTime > book2DownTime) {
	// return -1;
	// } else if (book1DownTime < book2DownTime) {
	// return 1;
	// }
	// }
	// return 0;
	// }
	// };

	/**
	 * 设置更新listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addProgressListener(ITaskUpdateListener listener) {
		if (!mUpdateListeners.contains(listener)) {
			mUpdateListeners.add(listener);
		}
	}

	/**
	 * 删除更新listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeProgressListener(ITaskUpdateListener listener) {
		mUpdateListeners.remove(listener);
	}

	/**
	 * 通知listener更新，不强制更新.
	 * 
	 * @param mustRefresh
	 *            the must refresh
	 * @param notifyDataSet
	 *            the notify data set
	 * @param stateCode
	 *            the state code
	 */
	public void onTaskUpdate(Book book, boolean mustRefresh,
			boolean notifyDataSet, int stateCode) {
		for (ITaskUpdateListener listener : mUpdateListeners) {
			listener.onUpdate(book, mustRefresh, notifyDataSet, 0, stateCode);
		}
	}

	/**
	 * 根据下载或者导入、阅读、更新时间进行排序，包含全部书籍
	 * 
	 * @return the all jobs
	 */
	public ArrayList<DownBookJob> getAllJobs() {
		ArrayList<DownBookJob> jobs = new ArrayList<DownBookJob>();
		jobs.addAll(mBookJobs);
		// 对DownBookJob进行排序
		Collections.sort(jobs, mReadOrDownTimeComparator);
		return jobs;
	}

	/**
	 * 获取所有订阅更新的书籍
	 * 
	 * @return
	 */
	public List<Book> getRemindBooks() {
		List<Book> books = new ArrayList<Book>();

		if (!mBookJobs.isEmpty()) {
			for (DownBookJob job : mBookJobs) {
				Book book = job.getBook();
				if (book.isSeriesBook() && book.hasChapters()
						&& book.isRemind()) {
					books.add(book);
				}
			}
		}

		return books;
	}
	
	/**
	 * 获取所有未订阅更新的书籍
	 * 
	 * @return
	 */
	public List<Book> getUnRemindBooks() {
		List<Book> books = new ArrayList<Book>();

		if (!mBookJobs.isEmpty()) {
			for (DownBookJob job : mBookJobs) {
				Book book = job.getBook();
				if (book.isSeriesBook() && book.hasChapters()
						&& !book.isRemind()) {
					books.add(book);
				}
			}
		}

		return books;
	}

	/**
	 * 根据最后阅读下载时间进行排序，不包含没有下载完成的书籍
	 * 
	 * @return
	 */
	public ArrayList<DownBookJob> getLastOperateJobs() {
		ArrayList<DownBookJob> downBookJobs = new ArrayList<DownBookJob>(6);
		for (DownBookJob job : mBookJobs) {
			Book book = job.getBook();
			// 下载已经成功的
			if (book != null
					&& Math.abs(book.getDownloadInfo().getProgress() - 1.0) < 0.0001
					&& job.getState() == DownBookJob.STATE_FINISHED) {
				downBookJobs.add(job);
			}
		}
		Collections.sort(downBookJobs, mReadOrDownTimeComparator);
		return downBookJobs;
	}

	/**
	 * 缓存云端书籍
	 */
	public void cacheBookCloud(Book book, SinaBookPageFactory pageFactory) {
		String oldFilePath = book.getDownloadInfo().getFilePath();

		if (oldFilePath != null
				&& (oldFilePath.endsWith(Book.TMP_SUFFIX) || oldFilePath
						.endsWith(Book.ONLINE_TMP_SUFFIX))) {
			File oldFile = new File(oldFilePath);
			if (oldFile.exists()) {
				Chapter curChapter = pageFactory.getCurrentChapter();
				if (curChapter != null && curChapter.getLength() > 0) {
					book.changeFileSuffix(Book.ONLINE_DAT_SUFFIX);
					File newFile = new File(book.getDownloadInfo()
							.getFilePath());
					oldFile.renameTo(newFile);

					pageFactory.addLastReadMark();

					final Book needSaveBook = book;
					new GenericTask() {
						@Override
						protected TaskResult doInBackground(
								TaskParams... params) {
							DBService.saveBook(needSaveBook);
							// DBService.updateAllChapter(needSaveBook,
							// needSaveBook.getChapters());
							DBService.saveBookRelateInfo(needSaveBook);
							return null;
						}

						protected void onPostExecute(TaskResult result) {
						}
					}.execute();
				}
			}
		}
	}

	/**
	 * 无需下载书籍，直接存储书籍的文件.
	 * 
	 * @param book
	 *            the book
	 * @return the int
	 */
	public void downBookLocal(Book book,
			final ITaskFinishListener saveFinishListener) {
		final boolean isCloudBook = hasBook(book) && book.isOnlineBook();

		String oldFilePath = book.getDownloadInfo().getFilePath();
		// 仅仅是临时文件需要改名字
		if (oldFilePath != null
				&& (oldFilePath.endsWith(Book.TMP_SUFFIX)
						|| oldFilePath.endsWith(Book.ONLINE_TMP_SUFFIX) || oldFilePath
							.endsWith(Book.ONLINE_DAT_SUFFIX))) {
			book.changeFileSuffix();
			File oldFile = new File(oldFilePath);
			File newFile = new File(book.getDownloadInfo().getFilePath());
			oldFile.renameTo(newFile);
		}

		book.setOnlineBook(false);
		book.getDownloadInfo().setProgress(1.00);
		book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FINISHED);
		// book.setTag(HAS_READ);
		book.getDownloadInfo().setLocationType(BOOK_LOCAL);
		book.getReadInfo().setLastReadTime(new Date().getTime());
		book.getDownloadInfo().setDownloadTime(new Date().getTime());
		PaymentMonthMineUtil.getInstance().downBecausePaymentMonth(book);

		// 存入数据库
		final Book saveBook = book;
		final boolean needSaveRelateInfo;
		if (isCloudBook) {
			needSaveRelateInfo = !(oldFilePath != null && oldFilePath
					.endsWith(Book.ONLINE_DAT_SUFFIX));
		} else {
			needSaveRelateInfo = true;
		}

		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.saveBook(saveBook);
				if (needSaveRelateInfo) {
					DBService
							.updateAllChapter(saveBook, saveBook.getChapters());
					DBService.saveBookRelateInfo(saveBook);
				}
				return null;
			}

			protected void onPostExecute(TaskResult result) {
				if (!isCloudBook) {
					DownBookManager.getInstance().addBookToShelves(saveBook);
				}
				if (saveFinishListener != null) {
					saveFinishListener.onTaskFinished(null);
				}
			}
		}.execute();
	}

	/**
	 * 判断是否需要下载书籍，如果需要，创建任务.
	 * 
	 * @param book
	 *            the book
	 * @return the int
	 */
	public int downBook(Book book) {
		return downBook(book, false);
	}

	public int downBook(Book book, boolean toast) {
		return downBook(book, toast, false);
	}

	/**
	 * 判断是否需要下载书籍，如果需要，创建任务.
	 * 
	 * @param book
	 *            the book
	 * @param toast
	 *            是否需要提升加入书架的Toast
	 * @return the int
	 */
	public int downBook(Book book, boolean toast, boolean exchange) {
		// 如果书架上有该书，使用书架上的该书实例
		Book jobBook = getBook(book);

		// 需要exchange的情景：
		// 1）在PC端加入了某本既有txt又有epub资源的书籍，此时去书架下拉刷新同步下来，书籍处于在线状态
		// 此时去摘要页，摘要页会显示“立即下载(免费)”或者“购买并立即下载(收费)”，因为同步下来在书架上
		// 的Book对象不具备isEpub方法的正确判定，默认为false，但是此时的摘要页的Book对象的isEpub显然
		// 是true的，此时点击下载执行到本方法并进入startDownTask方法后，无法正常下载epub。因此这里需
		// 要判定一下，如果是在摘要页调用的downBook方法，都直接调用本方法，并传入exchange=true。
		if (exchange) {
			if (jobBook != null) {
				boolean isEpub = book.isEpub();
				boolean shelfIsEpub = jobBook.isEpub();
				// 书架上的这本书籍的isEpub为false，说明这本书籍还没有下载过epub资源
				// 而此时传入的book对象是epub，则说明需要将书架这本书籍原先对应的文件删除
				// 并重置下载，阅读参数。
				// 如果书架上这本书籍已经是epub资源了，则不要删除临时文件，避免重头下载浪费流量
				if (isEpub && !shelfIsEpub) {
					// TODO CJL 干删除tmp文件的操作
					// TODO CJL 删除旧文件后重置job的所有参数
					// (具体解释看BookDetailActivity的initDownLoadBtnText方法)
					// TODO CJL 这里的oldFilePath不正确，因为book不是书架内的那个Book对象
					// 而是摘要页的，因此这里需要处理下，可以通过查看本方法是在哪里调用的来推算
					// TODO CJL 推算出来的结果是如果这本书已经在书架上了，那么这里的Book对象也
					// 有可能不是书架上的Book对象，因此这里需要额外的判断(在downBook方法)
					String oldFilePath = jobBook.getDownloadInfo()
							.getFilePath();
					File oldFile = new File(oldFilePath);
					if (oldFile != null && oldFile.exists()) {
						oldFile.delete();
					}
					jobBook.reset();
					// saveBookToDb(book, false);
					// 设置为新书标识(解决BugID=22766)
					// jobBook.setTag(Book.IS_NEW);
				}
				jobBook.setIsEpub(isEpub);
			}
		}

		if (jobBook != null && jobBook.isOnlineBook()) {
			jobBook.getReadInfo().setLastReadJsonString(
					book.getReadInfo().getLastReadJsonString(),
					"[DownBookManager >> downBook]");
			jobBook.setBookmarks(book.getBookmarks());
			jobBook.setBookSummaries(book.getBookSummaries());
			// 解决：BugID=21468
			// 转移Book的bookContentType属性值
			jobBook.exchangeBookContentType(book);
			book = jobBook;
			// 云端书籍的话转为本地书
			book.getReadInfo().setLastPos(0);
			book.getBookPage().setCurPage(-1);
			book.getBookPage().setTotalPage(-1);
			// book.getReadInfo().setLastReadPercent(0);
		}
		book.setOnlineBook(false);

		int ret = FLAG_START_CACHE;
		book.getDownloadInfo().setDownloadTime(new Date().getTime());
		book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_RUNNING);
		book.getDownloadInfo().setProgress(0.0);
		// 判断下载时是否有包月信息，来排除因为包月的下载
		PaymentMonthMineUtil.getInstance().downBecausePaymentMonth(book);

		final Book saveBook = book;

		// 绑定epub书籍的uid
		if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
				&& book.isEpub()) {
			String uid = LoginUtil.getLoginInfo().getUID();
			book.getBuyInfo().setUid(uid);
			// 同时还要绑定OwnerUID
			// 这样用户在注销的时候就可以把这本书籍也移除了
			// 避免因没有绑定UID导致程序认为这本书是本地导入或微盘下载下来的
			// 本地书籍，造成收费书籍免费供多个账户阅读的bug
			book.setOwnerUid(uid);
		}

		// 下载书籍在通知栏发送通知
		// saveBookToDb(saveBook, true);

		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.saveBook(saveBook);
				DBService.saveBookRelateInfo(saveBook);
				// 下载书籍在通知栏发送通知
				DownloadBookNotification.getInstance()
						.addNotification(saveBook);
				return null;
			}
		}.execute();

		addJob(book);
		LogUtil.d(TAG, "start down book");
		onTaskUpdate(book, true, true, DownBookJob.STATE_RUNNING);

		// Toast提示加入书架并下载
		if (toast) {
			Toast.makeText(SinaBookApplication.gContext,
					R.string.add_shelves_down, Toast.LENGTH_SHORT).show();
		}
		return ret;
	}

	/**
	 * Update book.
	 * 
	 * @param book
	 *            the book
	 */
	public void updateBook(Book book) {
		for (DownBookJob job : mBookJobs) {
			Book jobBook = job.getBook();
			if (jobBook.equals(book)) {
				jobBook.getReadInfo().setLastPos(
						book.getReadInfo().getLastPos());
				jobBook.getReadInfo().setLastReadPercent(
						book.getReadInfo().getLastReadPercent());
				break;
			}
		}
	}

	/**
	 * Gets the book.
	 * 
	 * @return the book
	 */
	public Book getBook(Book book) {
		for (DownBookJob job : mBookJobs) {
			if (job.getBook().equals(book)) {
				return job.getBook();
			}
		}
		return null;
	}

	/**
	 * Gets the book job.
	 * 
	 * @param book
	 *            the book
	 * @return the book job
	 */
	public DownBookJob getJob(Book book) {
		for (DownBookJob job : mBookJobs) {
			if (job.getBook().equals(book)) {
				return job;
			}
		}
		return null;
	}

	/**
	 * Checks for book.
	 * 
	 * @param book
	 *            the book
	 * @return true, if successful
	 */
	public boolean hasBook(Book book) {
		// TODO:临时打印查看消息
		// printAllBookJob();
		for (DownBookJob job : mBookJobs) {
			if (job.getBook().equals(book)) {
				return true;
			}
		}
		return false;
	}

	// private void printAllBookJob() {
	// int index = 0;
	// for (DownBookJob job : mBookJobs) {
	// index++;
	// }
	// }

	/**
	 * 通过传入文件的原始路径来获取Book对象
	 * 
	 * @param originalFilePath
	 * @return
	 */
	public Book getBook(String originalFilePath) {
		for (DownBookJob job : mBookJobs) {
			if (job.getBook().getDownloadInfo().getOriginalFilePath()
					.equals(originalFilePath)) {
				return job.getBook();
			}
		}
		return null;
	}

	/**
	 * 是否已经购买
	 * 
	 * @param book
	 * @return
	 */
	public boolean hasBuy(Book book) {
		for (DownBookJob job : mBookJobs) {
			if (job.getBook().equals(book)) {
				return job.getBook().getBuyInfo().isHasBuy();
			}
		}
		return false;
	}

	/**
	 * Adds the online book to shelves.
	 * 
	 * @param book
	 *            the book
	 */
	public void addBookToShelves(Book book) {
		DownBookJob job = new DownBookJob(book);
		job.setState(book.getDownloadInfo().getDownLoadState());
		mBookJobs.add(0, job);
	}

	/**
	 * 创建下载任务.
	 * 
	 * @param book
	 *            the book
	 */
	private void addJob(Book book) {
		DownBookJob job = new DownBookJob(book);
		addJob2DownQueue(job);
	}

	/**
	 * 删除任务.
	 * 
	 * @param job
	 *            the job
	 */
	public void removeJob(final DownBookJob job) {
		LogUtil.e("ReadInfoLeft",
				"DownBookManager >> removeJob >> job`s book={" + job.getBook()
						+ "}");
		// 删除数据库资源
		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.deleteBook(job.getBook());
				return null;
			}
		}.execute();
		// 删除书籍任务
		synchronized (mBookJobs) {
			mBookJobs.remove(job);
			if (job.getTask() != null) {
				job.getTask().cancel(true);
			}
			job.setTask(null);
		}
	}

	public void removeJobMemory(String log, final DownBookJob job) {
		synchronized (mBookJobs) {
			mBookJobs.remove(job);
			if (job.getTask() != null) {
				job.getTask().cancel(true);
			}
			job.setTask(null);
		}
	}

	/**
	 * 停止所有任务<br>
	 * 需要在异步线程中调用
	 */
	public void stopAllJob() {
		if (mBookJobs == null) {
			return;
		}
		// 保存所有job到数据库
		synchronized (mBookJobs) {
			LogUtil.e("ReadInfoLeft", "DownBookManager >> stopAllJob(停止所有任务)");
			while (mBookJobs.size() > 0) {
				DownBookJob job = mBookJobs.remove(0);
				if (job.getTask() != null) {
					job.getTask().cancel(true);
					job.setTask(null);
				}
				if (job.getState() != DownBookJob.STATE_FINISHED) {
					job.setState(DownBookJob.STATE_FAILED);
					job.getBook().getDownloadInfo()
							.setDownLoadState(job.getState());
					DBService.saveBook(job.getBook());
				}
			}
		}
	}

	/**
	 * 暂停任务.
	 * 
	 * @param job
	 *            the job
	 */
	public void pauseJob(DownBookJob job, boolean notify) {
		switch (job.getState()) {
		case DownBookJob.STATE_RUNNING: {
			if (job.getTask() != null) {
				if (job.getTask().cancel(true)) {
					job.setTask(null);
				}
			}
			jobPause(job);
			break;
		}
		case DownBookJob.STATE_PREPARING: {
			jobPause(job);
			break;
		}
		case DownBookJob.STATE_WAITING: {
			jobPause(job);
			break;
		}
		case DownBookJob.STATE_FAILED: {
			jobPause(job);
			break;
		}
		default:
			break;
		}

		if (notify)
			notifyJobFinished();
		
		onTaskUpdate(job.getBook(), false, true, DownBookJob.STATE_PAUSED);
	}

	/**
	 * Job pause.
	 * 
	 * @param job
	 *            the job
	 */
	private void jobPause(DownBookJob job) {
		job.setState(DownBookJob.STATE_PAUSED);
		job.getBook().getDownloadInfo()
				.setDownLoadState(DownBookJob.STATE_PAUSED);
		LogUtil.d(TAG, "job Pause");
	}

	/**
	 * 在退出程序时暂停所有正在下载中的任务
	 */
	public void pauseAllJob() {
		if (mBookJobs != null) {
			for (DownBookJob job : mBookJobs) {
				Book book = job.getBook();
				if (book != null && book.getDownloadInfo() != null) {
					if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) < 0.0001
							&& job.getState() == DownBookJob.STATE_FINISHED) {
						// 下载完成的状态，啥事不做
					} else if (job.getState() == DownBookJob.STATE_RUNNING) {
						if (job.getTask() != null) {
							if (job.getTask().isCancelable()) {
								// 暂停下载(但是不通知其他的下载，只是全部暂停，挨个的)
								job.pause(false);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 继续任务.
	 * 
	 * @param job
	 *            the job
	 */
	public void resumeJob(DownBookJob job) {
		Book book = job.getBook();
		LogUtil.d("SinaXmlHandler", "DownBookManager >> resumeJob >> {book="
				+ book + "}");
		if (book == null) {
			return;
		}

		BookDownloadInfo info = book.getDownloadInfo();
		if (info == null) {
			return;
		}

		if (Math.abs(info.getProgress() - 1.0) < 0.0001
				&& info.getDownLoadState() == DownBookJob.STATE_FINISHED) {
			return;
		}
		book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_PREPARING);
		addJob2DownQueue(job);
		onTaskUpdate(job.getBook(), false, true, DownBookJob.STATE_RUNNING);
	}

	/**
	 * 添加任务到下载队列.
	 * 
	 * @param job
	 *            the job
	 */
	private void addJob2DownQueue(DownBookJob job) {
		// 更新队列中已经存在的job，放在顶部
		mBookJobs.remove(job);
		mBookJobs.add(0, job);
		// 检查存储空间足够
		if (StorageUtil.checkExternalSpace(job.getBook().getDownloadInfo()
				.getFileSize())) {

			// 下载线程不超过2个，否则等待
			if (mRunningCount < RUNNING_MAX) {
				mRunningCount++;
				job.setState(DownBookJob.STATE_RUNNING);
				startDownTask(job);
			} else {
				job.setState(DownBookJob.STATE_WAITING);
			}
		} else {
			LogUtil.e(TAG, "存储卡无可用的下载空间");
			job.setState(DownBookJob.STATE_PAUSED);
			notifyJobFinished();
		}
	}

	/**
	 * Gets the first waiting job.
	 * 
	 * @return the first waiting job
	 */
	private DownBookJob getFirstWaitingJob() {
		if (mBookJobs == null || mBookJobs.size() == 0) {
			return null;
		}
		synchronized (mBookJobs) {
			for (int i = mBookJobs.size() - 1; i >= 0; i--) {
				DownBookJob job = mBookJobs.get(i);
				if (job.getState() == DownBookJob.STATE_WAITING) {
					return job;
				}
			}
		}
		return null;
	}

	/**
	 * 通知任务下载完成，启动新任务.
	 */
	private void notifyJobFinished() {
		if (mRunningCount >= RUNNING_MAX) {
			return;
		}
		DownBookJob job = getFirstWaitingJob();
		if (job != null) {
			addJob2DownQueue(job);
		}
	}

	/**
	 * 启动下载书籍任务.
	 */
	private void startDownTask(DownBookJob job) {
		Book book = job.getBook();
		job.setState(DownBookJob.STATE_RUNNING);
		// epub书籍和vpan的书籍走的逻辑一样，只是2.0.2版本中的epub书籍会在
		// VDiskDownUrl前加一个前缀：Book.EPUB_PATH_PROTOCOL
		String vDiskDownUrl = book.getDownloadInfo().getVDiskDownUrl();
		if (vDiskDownUrl != null) {
			if (vDiskDownUrl.startsWith(Book.EPUB_PATH_PROTOCOL)) {
				// epub书籍的下载
				onTaskUpdate(book, false, true, DownBookJob.STATE_RUNNING);
				DownEpubFileTask task = new DownEpubFileTask(job);
				task.setTaskFinishListener(mDownFinishListener);
				job.setTask(task);
				task.execute();
			} else {
				// v盘的书籍下载使用DownVDiskFileTask
				onTaskUpdate(book, false, true, DownBookJob.STATE_RUNNING);
				DownVDiskFileTask task = new DownVDiskFileTask(job);
				task.setTaskFinishListener(mDownFinishListener);
				job.setTask(task);
				task.execute();
			}
		} else {
			// 每次启动新任务时都会去切换一下书籍的临时文件目录
			book.changeFileSuffixToTmp();

			// 通知UI界面状态
			onTaskUpdate(book, false, true, DownBookJob.STATE_RUNNING);

			DownBookTask task = new DownBookTask(job);
			task.setTaskFinishListener(mDownFinishListener);
			job.setTask(task);
			task.execute();
		}
	}

	/**
	 * Save book to db.
	 * 
	 * @param book
	 *            the book
	 */
	private void saveBookToDb(final Book book, final boolean needSaveRelateInfo) {
		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.saveBook(book);

				if (needSaveRelateInfo) {
					// save relateInfo
					DBService.saveBookRelateInfo(book);
				}
				return null;
			}
		}.execute();
	}

	/**
	 * 异步清理无用的缓存书籍文件
	 */
	public void clearUselessFile() {
		final String curDate = CalendarUtil
				.getCurrentTImeWithFormat("yyyy-MM-dd");
		// final String curDate = new SimpleDateFormat("yyyy-MM-dd",
		// Locale.getDefault()).format(new Date());

		if (null == mBookJobs
				|| mBookJobs.size() <= 0
				|| curDate.equals(StorageUtil.getString(
						StorageUtil.KEY_CLEAR_DATE, "1970-01-01"))) {
			return;
		}

		// 找到不被清理的路径,这里放在UI线程
		final ArrayList<String> noClearPaths = new ArrayList<String>();
		for (DownBookJob job : mBookJobs) {
			String filePath = job.getBook().getDownloadInfo().getFilePath();
			String originalPath = job.getBook().getDownloadInfo().getOriginalFilePath();
			if(originalPath != null && originalPath.endsWith(".epub")){
				noClearPaths.add(originalPath);
			}else{
				noClearPaths.add(filePath);
			}
		}
		
		List<Book> cacheBooks = DBService.getAllBookCache();
		for (Book cache : cacheBooks) {
			String cacheFilePath = cache.getDownloadInfo().getFilePath();
			String cacheOriginalPath = cache.getDownloadInfo().getOriginalFilePath();
			if(cacheOriginalPath != null && cacheOriginalPath.endsWith(".epub")){
				noClearPaths.add(cacheOriginalPath);
			}else{
				noClearPaths.add(cacheFilePath);
			}
		}
		
		// 开始一个任务清理无用的文件
		new GenericTask() {
			protected TaskResult doInBackground(TaskParams... params) {
				try {
					File bookDir = new File(
							StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK));

					if (bookDir.exists()) {
						if (bookDir.isDirectory()) {
							File[] files = bookDir.listFiles();

							if (null != files) {
								ArrayList<File> fileList = new ArrayList<File>();
								for (File file : files) {
									fileList.add(file);
								}

								// 找出无用的文件
								for (File file : files) {
									String fileName = file.getName();
									if (fileName.endsWith(Book.BOOK_SUFFIX)) {
										// 慎重删除，对于dat文件，宁可多存放也不要轻易删除。
										fileList.remove(file);
									} else {
										for (String noClearPath : noClearPaths) {
											if (noClearPath.contains(getNoSuffixFileName(file.getName()))) {
												fileList.remove(file);
											}
										}
									}
								}

								// 循环删除无用的文件
								if (fileList.size() > 0) {
									for (File file : fileList) {
										StorageUtil.deleteFolder(file);
									}

									StorageUtil
											.saveString(
													StorageUtil.KEY_CLEAR_DATE,
													curDate);
								}
							}
						}
					}

					// 清理temp目录下临时生成的图片
					StorageUtil.deleteFolder(StorageUtil
							.getDirByType(StorageUtil.DIR_TYPE_TEMP));
				} catch (Exception e) {
					// 捕捉所有异常，不做处理
				}

				return null;
			}
		}.execute();
	}

	/**
	 * 得到不包含后缀的文件名
	 * 
	 * @param fileName
	 * @return
	 */
	private String getNoSuffixFileName(String fileName) {
		if (fileName == null) {
			return fileName;
		}
		try {
			int index = fileName.lastIndexOf(".");
			if (index > 0) {
				return fileName.substring(0, index);
			}
		} catch (Exception e) {
		}
		return fileName;
	}
}
