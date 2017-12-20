package com.sina.book.control.download;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.os.Handler;
import android.util.Log;

import com.sina.book.control.GenericTask;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ChapterList;
import com.sina.book.data.ConstantData;
import com.sina.book.db.DBService;
import com.sina.book.parser.ChapterListParser;
import com.sina.book.util.FileUtils;
import com.sina.book.util.HttpUtil;
import com.sina.weibo.sdk.utils.MD5;

/**
 * 书籍下载管理类.
 */
public class HtmlDownBookManager implements IResourceTaskUpdateListener
{

	/** 最大下载线程 */
	private static final int		RUNNING_MAX			= 5;

	/** 正在运行的任务个数. */
	private int						mRunningCount		= 0;

	// private static HtmlDownBookManager mInstance;

	public ResourceDownManager		mResourceInstance;

	/** The m update listeners. */
	// private ArrayList<ITaskUpdateListener> mUpdateListeners = new
	// ArrayList<ITaskUpdateListener>();

	private ArrayList<HtmlDownJob>	mBookJobs			= new ArrayList<HtmlDownJob>();

	public Book						mBook;

	/** 下载完成listener. */
	private IDownTaskFinishListener	mDownFinishListener	= new IDownTaskFinishListener()
														{
															@Override
															public void onTaskFinished(DownResult taskResult)
															{
																HtmlDownJob job = null;
																mRunningCount--;
																if (taskResult != null) {
																	if (taskResult.task instanceof HtmlDownBookTask) {
																		HtmlDownBookTask task = (HtmlDownBookTask) taskResult.task;
																		job = task.getJob();
																		
																		if(taskResult.stateCode == 0){
																			// html下载成功
																			doFinish(job);
																			notifyJobFinished();
																		}else{
																			// html下载失败
//																			Log.e("ouyang", "--HtmlDownBookManager--onTaskFinished--失败--： " + job.getUrl());
																			doError(job);
																			onState(job.chapterId, false, -1);
																		}

//																		if (taskResult.stateCode != -1) {
//																			if (ConstantData.CODE_SUCCESS.equals(""
//																					+ taskResult.stateCode)) {
//																				// html下载成功成功
////																				Log.i("ouyang", "--HtmlDownBookManager--成功--： " + job.getUrl());
//
//																				doFinish(job);
//																				// onTaskUpdate(job.getUrl(),
//																				// false,
//																				// true,
//																				// DownBookJob.STATE_FINISHED);
//
//																				notifyJobFinished();
//																			} else {
//																				// 失败
////																				Log.w("ouyang", "--HtmlDownBookManager--失败--： " + job.getUrl());
//																				doError(job);
//																				// if
//																				// (taskResult.retObj
//																				// !=
//																				// null
//																				// &&
//																				// !TextUtils.isEmpty(taskResult.retObj
//																				// .toString()))
//																				// {
//																				// Toast.makeText(SinaBookApplication.gContext,
//																				// taskResult.retObj.toString(),
//																				// Toast.LENGTH_SHORT).show();
//																				// }
//
//																				onState(job.chapterId, false, -1);
//																				// onTaskUpdate(mBook,
//																				// false,
//																				// true,
//																				// DownBookJob.STATE_FAILED);
//																			}
//																		} else {
//																			// 处理下载失败的
////																			Log.w("ouyang", "--HtmlDownBookManager--失败2--： " + job.getUrl());
//																			doError(job);
//																			onState(job.chapterId, false, -1);
//																			// Toast.makeText(SinaBookApplication.gContext,
//																			// R.string.downloading_failed,
//																			// Toast.LENGTH_SHORT).show();
//																			// onTaskUpdate(mBook,
//																			// false,
//																			// true,
//																			// DownBookJob.STATE_FAILED);
//																		}
																	}
																} else {
																	// html下载被取消
//																	stopAllJob();
																}
															}
														};

	// public static HtmlDownBookManager getInstance()
	// {
	// if (mInstance == null) {
	// mInstance = new HtmlDownBookManager();
	// mResourceInstance = ResourceDownManager.getInstance();
	// mResourceInstance.setListener(mInstance);
	// }
	// return mInstance;
	// }

	// public HtmlDownBookManager(){
	//
	// }

	// 下载状态（正在下载、暂停、错误等）
	private int						mState;

	// 是否需要阅读(true：下载完成后进入阅读 false：只下载)
	private boolean					mIsRead;

	// 是否已经加入了书架(如果已加入书架，下载完成后需要更新到数据库；未加入书架表示只是点击了阅读)
	private boolean					mHasAddShelf;

	public String					mBookDir;

	public HtmlDownBookManager(Book book, String bookDir)
	{
		mBook = book;
		mBookDir = bookDir;

		mResourceInstance = new ResourceDownManager(bookDir);
		mResourceInstance.setListener(this);
	}

	public void setState(int state)
	{
		mState = state;
		mBook.getDownloadInfo().setDownLoadState(state);

		DownBookJob job = DownBookManager.getInstance().getJob(mBook);
		if (job != null) {
			job.setState(state);

			Book book = job.getBook();
			book.setOnlineBook(false);
			book.getDownloadInfo().setDownLoadState(state);
			book.getDownloadInfo().setProgress(mBook.getDownloadInfo().getProgress());
		}
	}

	public int getState()
	{
		return mState;
	}

	public void setIsRead(boolean isRead)
	{
		mIsRead = isRead;
	}

	public boolean isRead()
	{
		return mIsRead;
	}

	public void setHasAddShelf(boolean hasAddShelf)
	{
		mHasAddShelf = hasAddShelf;
	}

	public boolean hasAddshelf()
	{
		return mHasAddShelf;
	}

	// public ArrayList<String> mChapterUrlList;

	// 目录列表
	public ArrayList<OPFData>		chapterNcxList;

	// 资源列表
	public HashMap<String, OPFData>	opfResList;
	
	private Handler handler = new Handler();

	// 注册下载
	public void registerDownload(Book book)
	{
		mBook = book;
		downloadCount = 0;
		
		// 先检测书籍章节列表是否存在
		ArrayList<Chapter> cpList = mBook.getChapters();
		if(cpList == null || cpList.size() <= 0){
			// 没有章节列表，先去下载章节列表
			reqChapterList();
		}else{
			reqDownload();
		}
		
		
//		mBook = null;
//		if (mBook == null) {
//			mBook = new Book();
//			mBook.setBookId(book.getBookId());
//			mBook.setAuthor(book.getAuthor());
//			mBook.setTitle(book.getTitle());
//			mBook.getDownloadInfo().setDownloadTime(book.getDownloadInfo().getDownloadTime());
//			mBook.getDownloadInfo().setDownLoadState(book.getDownloadInfo().getDownLoadState());
//			mBook.getDownloadInfo().setOriginalFilePath(book.getDownloadInfo().getOriginalFilePath());
//			mBook.getDownloadInfo().setVDiskDownUrl(book.getDownloadInfo().getVDiskDownUrl());
//			mBook.getDownloadInfo().setImageUrl(book.getDownloadInfo().getImageUrl());
//
//			ArrayList<Chapter> cpList = new ArrayList<Chapter>();
//			for (int i = 0; i < 50; ++i) {
//				Chapter chapter = new Chapter();
//				chapter.setTitle("章节" + (i + 1));
//				chapter.setChapterId(i + 1);
//				cpList.add(chapter);
//			}
//			mBook.setChapters(cpList);
//		}
	}
	
	private void reqChapterList() {
		new GenericTask()
		{
			protected TaskResult doInBackground(TaskParams... taskParams)
			{
				String reqUrl = String
						.format(ConstantData.URL_GET_CHAPTERS, mBook.getBookId(), mBook.getSid(), mBook.getBookSrc());
				reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
				reqUrl = HttpUtil.addAuthCode2Url(reqUrl);

				RequestTask task = new RequestTask(new ChapterListParser());
				TaskParams params = new TaskParams();
				params.put(RequestTask.PARAM_URL, reqUrl);
				params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
				TaskResult taskResult = task.syncExecute(params);
				
//				if(taskResult.stateCode != 200 && taskResult.stateCode != 206){
//					// 网络异常
//					
//				}else{
					if (taskResult != null && taskResult.retObj instanceof ChapterList) {
						ChapterList updateChaptersResult = (ChapterList) taskResult.retObj;
						mBook.setChapters(updateChaptersResult.getChapters());
						
						// 保存章节目录
						DBService.updateAllChapter(mBook, updateChaptersResult.getChapters());
						
						// 开始下载
						handler.post(new Runnable()
						{
							public void run()
							{
								reqDownload();
							}
						});
					}else{
						// 网络错误，没得到章节列表，停止下载
						mBook.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FAILED);
//						Log.e("ouyang", "---HtmlDownBookManager--下载失败---");
						handler.post(new Runnable()
						{
							public void run()
							{
								HtmlDownManager.getInstance().onUpdate(HtmlDownBookManager.this, -1);
							}
						});
					}
//				}

				
				return null;
			}
		}.execute();
	}
	
	private void reqDownload(){
		String bookId = mBook.getBookId();
		
//		//以前走消费记录进入阅读时，传递一个prikey，让付费用户还能阅读下架书籍的已付费内容。
//		String uid = LoginUtil.getLoginInfo().getUID();
//		StringBuffer buffer = new StringBuffer();
//		buffer.append(uid);
//		buffer.append("|");
//		buffer.append(bookId);
//		buffer.append("|");
//		buffer.append(ConstantData.BUYDETETAIL_KEY);
//		String prikey = MD5.hexdigest(buffer.toString());
//		if (buy) {
//			reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
//		}
		
//		String url = "http://192.168.16.240:8082/epub/OEBPS/Text/chapter";
		
		chapterNcxList = new ArrayList<OPFData>();
		opfResList = new HashMap<String, OPFData>();
		ArrayList<Chapter> chapterList = mBook.getChapters();
		
		// 下载时，将已加入书架的章节列表记录存储数据库(每次开始下载前都存储，也可起到更新最新章节列表到数据库的效果)
		DBService.updateAllChapter(mBook, chapterList);
		
		for (int i = 0; i < chapterList.size(); ++i) {
			Chapter cp = chapterList.get(i);
			int cid = cp.getGlobalId();
//			int cid = cp.getChapterId();

			OPFData opfData = new OPFData();
			opfData.href = "text/" + cid + ".xhtml";
			opfData.id = cid + ".xhtml";
			opfData.text = cp.getTitle();
			opfData.media_type = OPFData.MEDIA_TYPE_XML;
			chapterNcxList.add(opfData);

//			String cpurl = url + (i + 1) + ".xhtml";
			
			
			// 单张下载接口，增加一个key值，碰到收费章节也能直接返回书籍内容，绕开收费逻辑
			StringBuffer html_Buffer = new StringBuffer();
			html_Buffer.append(bookId);
			html_Buffer.append("|");
			html_Buffer.append(cid);
			html_Buffer.append("|");
			html_Buffer.append(ConstantData.HTMLREAD_KEY);
			String html_key = MD5.hexdigest(html_Buffer.toString());
			
			String reqUrl = null;
			StringBuilder sb = new StringBuilder(ConstantData.URL_READ_ONLINE);
			sb.append("?").append(ConstantData.AUTH_CODE_GET);
			sb.append("&bid=").append(bookId);
			sb.append("&cid=").append(cid);
			sb.append("&src=").append(Book.BOOK_SRC_WEB);
			sb.append("&sid=").append(mBook.getSid());
			sb.append("&kkey=").append(html_key);
			
//			if (offShelfKey != null) {
//				sb.append("&prikey=").append(offShelfKey);
//			}
			reqUrl = sb.toString();
			reqUrl = ConstantData.addDeviceIdToUrl(reqUrl);
			downBook(reqUrl, mBook.getBookId(), String.valueOf(cid));
//			downBook(reqUrl, mBook.getBookId(), String.valueOf(cp.getChapterId()));
		}
		
	}
	
	public void setBook(Book book)
	{
		mBook = book;
	}

	private void doError(HtmlDownJob job)
	{
		job.setState(DownBookJob.STATE_FAILED);
		stopAllJob();
	}

	// 章节下载完成
	private void doFinish(HtmlDownJob job)
	{
		job.setState(DownBookJob.STATE_FINISHED);
	}

	/**
	 * 设置更新listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	// public void addProgressListener(ITaskUpdateListener listener)
	// {
	// if (!mUpdateListeners.contains(listener)) {
	// mUpdateListeners.add(listener);
	// }
	// }

	/**
	 * 删除更新listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	// public void removeProgressListener(ITaskUpdateListener listener)
	// {
	// mUpdateListeners.remove(listener);
	// }

	// /**
	// * 通知listener更新，不强制更新.
	// */
	// public void onTaskUpdate(Book book, boolean mustRefresh,
	// boolean notifyDataSet, int progress, int stateCode)
	// {
	// for (ITaskUpdateListener listener : mUpdateListeners) {
	// listener.onUpdate(mBook, mustRefresh, notifyDataSet, progress,
	// stateCode);
	// }
	// }

	/**
	 * 根据下载或者导入、阅读、更新时间进行排序，包含全部书籍
	 * 
	 * @return the all jobs
	 */
	public ArrayList<HtmlDownJob> getAllJobs()
	{
		// ArrayList<HtmlDownJob> jobs = new ArrayList<HtmlDownJob>();
		// jobs.addAll(mBookJobs);
		// 对DownBookJob进行排序
		// Collections.sort(jobs, mReadOrDownTimeComparator);
		return mBookJobs;
	}

	// /**
	// * 获取所有订阅更新的书籍
	// *
	// * @return
	// */
	// public List<Book> getRemindBooks()
	// {
	// List<Book> books = new ArrayList<Book>();
	//
	// if (!mBookJobs.isEmpty()) {
	// for (DownBookJob job : mBookJobs) {
	// Book book = job.getBook();
	// if (book.isSeriesBook() && book.hasChapters()
	// && book.isRemind()) {
	// books.add(book);
	// }
	// }
	// }
	// return books;
	// }

	// /**
	// * 获取所有未订阅更新的书籍
	// *
	// * @return
	// */
	// public List<Book> getUnRemindBooks()
	// {
	// List<Book> books = new ArrayList<Book>();
	//
	// if (!mBookJobs.isEmpty()) {
	// for (DownBookJob job : mBookJobs) {
	// Book book = job.getBook();
	// if (book.isSeriesBook() && book.hasChapters()
	// && !book.isRemind()) {
	// books.add(book);
	// }
	// }
	// }
	//
	// return books;
	// }

	// /**
	// * 根据最后阅读下载时间进行排序，不包含没有下载完成的书籍
	// * @return
	// */
	// public ArrayList<DownBookJob> getLastOperateJobs()
	// {
	// ArrayList<DownBookJob> downBookJobs = new ArrayList<DownBookJob>(6);
	// for (DownBookJob job : mBookJobs) {
	// Book book = job.getBook();
	// // 下载已经成功的
	// if (book != null
	// && Math.abs(book.getDownloadInfo().getProgress() - 1.0) < 0.0001
	// && job.getState() == DownBookJob.STATE_FINISHED) {
	// downBookJobs.add(job);
	// }
	// }
	// Collections.sort(downBookJobs, mReadOrDownTimeComparator);
	// return downBookJobs;
	// }

	/**
	 * 判断是否需要下载书籍，如果需要，创建任务.
	 */

	public void downBook(String url, String bookId, String chapterId)
	{
		mBook.getDownloadInfo().setDownloadTime(new Date().getTime());
		mBook.getDownloadInfo().setDownLoadState(DownBookJob.STATE_RUNNING);
		mBook.getDownloadInfo().setProgress(0.0);
		
		addJob(url, bookId, chapterId);
	}

	/**
	 * Gets the book job.
	 * 
	 * @param book
	 *            the book
	 * @return the book job
	 */
	public HtmlDownJob getJob(String url)
	{
		for (HtmlDownJob job : mBookJobs) {
			if (job.getUrl().equals(url)) {
				return job;
			}
		}
		return null;
	}

	/**
	 * 通过传入文件的原始路径来获取Book对象
	 * 
	 * @param originalFilePath
	 * @return
	 */
	public Book getBook()
	{
		return mBook;
	}

	/**
	 * 创建下载任务.
	 * 
	 * @param book
	 *            the book
	 */
	private void addJob(String url, String bookId, String chapterId)
	{
		HtmlDownJob job = new HtmlDownJob(this, url, bookId, chapterId);
		addJob2DownQueue(job);
	}

	/**
	 * 删除任务.
	 * 
	 * @param job
	 *            the job
	 */
	// public void removeJob(final DownBookJob job)
	// {
	// LogUtil.e("ReadInfoLeft",
	// "DownBookManager >> removeJob >> job`s book={" + job.getBook()
	// + "}");
	// // 删除数据库资源
	// new GenericTask()
	// {
	// @Override
	// protected TaskResult doInBackground(TaskParams... params)
	// {
	// DBService.deleteBook(job.getBook());
	// return null;
	// }
	// }.execute();
	// // 删除书籍任务
	// synchronized (mBookJobs) {
	// mBookJobs.remove(job);
	// if (job.getTask() != null) {
	// job.getTask().cancel(true);
	// }
	// job.setTask(null);
	// }
	// }

	public void removeJobMemory(String log, final DownBookJob job)
	{
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
	public void stopAllJob()
	{
		if (mBookJobs == null) {
			return;
		}

		// 保存所有job到数据库
		synchronized (mBookJobs) {
//			LogUtil.e("ReadInfoLeft", "DownBookManager >> stopAllJob(停止所有任务)");
			while (mBookJobs.size() > 0) {
				HtmlDownJob job = mBookJobs.remove(0);
				if (job.getTask() != null) {
					job.getTask().cancel(true);
					job.setTask(null);
				}
				if (job.getState() != DownBookJob.STATE_FINISHED) {
					job.setState(DownBookJob.STATE_FAILED);
				}
			}
		}

		// 停止资源下载
		mResourceInstance.stopAllJob();

	}

	// /**
	// * 暂停任务.
	// *
	// * @param job
	// * the job
	// */
	public void pauseJob(HtmlDownJob job, boolean notify)
	{
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

		// onTaskUpdate(mBook, false, true, DownBookJob.STATE_PAUSED);
	}

	/**
	 * Job pause.
	 * 
	 * @param job
	 *            the job
	 */
	private void jobPause(HtmlDownJob job)
	{
		job.setState(DownBookJob.STATE_PAUSED);
	}

	/**
	 * 在退出程序时暂停所有正在下载中的任务
	 */
	public void pauseAllJob()
	{
		if (mBookJobs != null) {
			for (HtmlDownJob job : mBookJobs) {
				if (job.getState() == DownBookJob.STATE_RUNNING) {
					if (job.getTask() != null) {
						if (job.getTask().isCancelable()) {
							// 暂停下载(但是不通知其他的下载，只是全部暂停，挨个的)
							job.pause(false);
						}
					}
				}
			}
		}
		// onTaskUpdate(mBook, false, true, downloadCount,
		// DownBookJob.STATE_PAUSED);
	}

	// /**
	// * 继续任务.
	// *
	// * @param job
	// * the job
	// */
	// public void resumeJob(HtmlDownJob job)
	// {
	// addJob2DownQueue(job);
	// onTaskUpdate(job.getUrl(), false, true, DownBookJob.STATE_RUNNING);
	// }

	/**
	 * 添加任务到下载队列.
	 * 
	 * @param job
	 *            the job
	 */
	private void addJob2DownQueue(HtmlDownJob job)
	{
		// 更新队列中已经存在的job，放在顶部
		mBookJobs.remove(job);
		mBookJobs.add(job);

		// 下载线程不超过2个，否则等待
		if (mRunningCount < RUNNING_MAX) {
			mRunningCount++;
			job.setState(DownBookJob.STATE_RUNNING);
			startDownTask(job);
		} else {
			job.setState(DownBookJob.STATE_WAITING);
		}
	}

	/**
	 * 获取第一个等待任务下载
	 */
	private HtmlDownJob getFirstWaitingJob()
	{
		if (mBookJobs == null || mBookJobs.size() == 0) {
			return null;
		}

		synchronized (mBookJobs) {
			for (int i = mBookJobs.size() - 1; i >= 0; i--) {
				HtmlDownJob job = mBookJobs.get(i);
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
	private void notifyJobFinished()
	{
		if (mRunningCount >= RUNNING_MAX) {
			return;
		}

		HtmlDownJob job = getFirstWaitingJob();
		if (job != null) {
			job.setState(DownBookJob.STATE_RUNNING);
			startDownTask(job);
		}
	}

	/**
	 * 启动下载书籍任务.
	 */
	private void startDownTask(HtmlDownJob job)
	{
		job.setState(DownBookJob.STATE_RUNNING);
		HtmlDownBookTask task = new HtmlDownBookTask(job);
		task.setTaskFinishListener(mDownFinishListener);
		job.setTask(task);
		task.execute();
	}

	// 统计下载章节数目
	public int	downloadCount	= 0;

	// 章节资源下载的回调
	public void onState(String chapterId, boolean isReplace, int stateCode)
	{
		if (stateCode == 0) {
			// 单张的全部资源下载成功, 表示一章下载完成
			if (isReplace) {
				// 替换html
				HtmlDownBookTask.replaceHtml(mBook.getBookId(), chapterId);
			}
			downloadCount++;
			
			mBook.getDownloadInfo().setProgress(getProgress());
			if (mBook.getDownloadInfo().getProgress() > 1.0) {
				mBook.getDownloadInfo().setProgress(1.0);
			}

			// 将html下载进度复制一遍给书架的书籍
			DownBookJob job = DownBookManager.getInstance().getJob(mBook);
			if (job != null) {
				Book book = job.getBook();
				book.getDownloadInfo().setProgress(mBook.getDownloadInfo().getProgress());
				book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_RUNNING);
			}
			HtmlDownManager.getInstance().onTaskUpdate(mBook, false, true, downloadCount, DownBookJob.STATE_RUNNING);
			
			int size = mBook.getChapters().size();
			if (downloadCount >= size) {
				// 全部下载完成
				// 生成ncx和opf
				createNcx();
				createOPF();
				createMetaIEF();

				mBook.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FINISHED);
				mBook.getDownloadInfo().setProgress(1.0);
				
				handler.post(new Runnable()
				{
					
					@Override
					public void run()
					{
						HtmlDownManager.getInstance().onUpdate(HtmlDownBookManager.this, 0);
					}
				});
//				HtmlDownManager.getInstance().onUpdate(this, 0);
				
				// onTaskUpdate(mBook, false, true, downloadCount,
				// DownBookJob.STATE_FINISHED);
				downloadCount = 0;
//			} else {
////				float progress = getProgress();
////				Log.d("ouyang", "-进度：" + progress);
//
//				mBook.getDownloadInfo().setProgress(getProgress());
//				if (mBook.getDownloadInfo().getProgress() > 1.0) {
//					mBook.getDownloadInfo().setProgress(1.0);
//				}
//
//				// 将html下载进度复制一遍给书架的书籍
//				DownBookJob job = DownBookManager.getInstance().getJob(mBook);
//				if (job != null) {
//					Book book = job.getBook();
//					book.getDownloadInfo().setProgress(mBook.getDownloadInfo().getProgress());
//					book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_RUNNING);
//				}
//
//				HtmlDownManager.getInstance().onTaskUpdate(mBook, false, true, downloadCount, DownBookJob.STATE_RUNNING);
			}
		} else {
			// 下载失败
			mBook.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FAILED);
//			Log.e("ouyang", "---HtmlDownBookManager--下载失败---");
			
			HtmlDownManager.getInstance().onUpdate(this, -1);
			// onTaskUpdate(mBook, false, true, downloadCount,
			// DownBookJob.STATE_FAILED);
		}
	}

	// 获取书籍下载进度
	public float getProgress()
	{
		int total = mBook.getChapters().size();
		if (total > 0) {
			float progress = downloadCount * 1.0F / total;
			return progress;
		}
		return 0;
	}

	private void createNcx()
	{
		NCXWriter writer = new NCXWriter();
		writer.setTitle(mBook.getTitle());
		writer.setOpfList(chapterNcxList);
		String xml = writer.writeXml();

		byte[] data = null;
		try {
			data = xml.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			data = xml.getBytes();
		}

		String path = mBookDir + "/OEBPS/toc.ncx";
		FileUtils.writeData(path, data);
	}

	private void createOPF()
	{
		OPFWriter writer = new OPFWriter();
		writer.setTitle(mBook.getTitle());
		writer.setAuthor(mBook.getAuthor());
		writer.mCpList = chapterNcxList;
		writer.mOPFResList = opfResList;

		String xml = writer.writeXml();
		byte[] data = null;
		try {
			data = xml.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			data = xml.getBytes();
		}

		String path = mBookDir + "/OEBPS/content.opf";
		FileUtils.writeData(path, data);
	}

	private void createMetaIEF()
	{
		ContainerWriter writer = new ContainerWriter();
		String xml = writer.writeXml();
		byte[] data = null;
		try {
			data = xml.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			data = xml.getBytes();
		}

		String path = mBookDir + "/META-INF/container.xml";
		FileUtils.writeData(path, data);
	}

	// 删除书籍对应的文件夹
	public void deleteBookFileCache(){
		FileUtils.deleteFile(mBookDir);
	}
	
}
