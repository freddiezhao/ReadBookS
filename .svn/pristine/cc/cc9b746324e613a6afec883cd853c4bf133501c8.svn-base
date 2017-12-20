package com.sina.book.control.download;

import java.util.ArrayList;
import java.util.Date;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.db.DBService;
import com.sina.book.util.FileUtils;
import com.sina.book.util.StorageUtil;

/**
 * 书籍下载管理类.
 */
public class HtmlDownManager
{

	/** 最大下载线程 */
	private static final int				RUNNING_MAX						= 1;

	/** 正在运行的任务个数. */
	private int								mRunningCount					= 0;

	private ArrayList<ITaskUpdateListener>	mUpdateListeners				= new ArrayList<ITaskUpdateListener>();
	
	/** The m instance. */
	private static HtmlDownManager			mInstance;

	/** html书籍管理器 */
	public ArrayList<HtmlDownBookManager>	mHtmlBookList					= new ArrayList<HtmlDownBookManager>();
	
	
	/** 广播 */
	public static final String				ACTION_INTENT_DOWNSTATE			= "com.sina.book.downloadstate";

	public static HtmlDownManager getInstance()
	{
		if (mInstance == null) {
			mInstance = new HtmlDownManager();
		}
		return mInstance;
	}

	private HtmlDownManager()
	{
	}
	

	/**
	 * 设置更新listener
	 */
	public void addProgressListener(ITaskUpdateListener listener)
	{
		if (!mUpdateListeners.contains(listener)) {
			mUpdateListeners.add(listener);
		}
	}
	
	/**
	 * 删除更新listener.
	 */
	public void removeProgressListener(ITaskUpdateListener listener)
	{
		mUpdateListeners.remove(listener);
	}

	/**
	 * 通知listener更新，不强制更新.
	 */
	public void onTaskUpdate(Book book, boolean mustRefresh,
			boolean notifyDataSet, int progress, int stateCode)
	{
		for (ITaskUpdateListener listener : mUpdateListeners) {
			listener.onUpdate(book, mustRefresh, notifyDataSet, progress, stateCode);
		}
	}

	/**
	 * 根据下载或者导入、阅读、更新时间进行排序，包含全部书籍
	 * 
	 * @return the all jobs
	 */
	public ArrayList<HtmlDownBookManager> getAllJobs()
	{
		// ArrayList<HtmlDownBookManager> jobs = new ArrayList<HtmlDownBookManager>();
		// jobs.addAll(mBookJobs);
		// 对DownBookJob进行排序
		// Collections.sort(jobs, mReadOrDownTimeComparator);
		return mHtmlBookList;
	}


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
	 * 下载书籍
	 * forceThread: true-强制开线程下载，不受队列下载
	 */
	public void downBook(Book book, boolean isRead, boolean forceThread)
	{
		
		// 先判断书籍是否添加过下载列表
		HtmlDownBookManager manager = getJob(book);
		if(manager != null){
			manager.stopAllJob();
			mHtmlBookList.remove(manager);
		}
		
		String baseDir = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK);
		String bookDirName = "/bd" + book.getBookId() + ".epub";
		String path = baseDir + bookDirName;
		book.getDownloadInfo().setDownloadTime(new Date().getTime());
		book.getDownloadInfo().setOriginalFilePath(path);
		book.getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
		book.getDownloadInfo().setProgress(0.0);
		book.setOnlineBook(false);
		
		manager = new HtmlDownBookManager(book, path);
		manager.setIsRead(isRead);
		addJob2DownQueue(manager, forceThread);
		
//		if(manager == null){
//			String baseDir = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK);
//			String bookDirName = "/bd" + book.getBookId() + ".epub";
//			String path = baseDir + bookDirName;
//			book.getDownloadInfo().setOriginalFilePath(path);
//			book.getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
//			book.getDownloadInfo().setProgress(0.0);
//			book.setOnlineBook(false);
//			
//			manager = new HtmlDownBookManager(book, path);
//			manager.setIsRead(isRead);
//			addJob2DownQueue(manager);
//		}else{
//			// 已经下载过，
//			int state = manager.getState();
//			if(state == DownBookJob.STATE_FINISHED){
//				// 已完成
//				if(isRead){
//					//TODO: 已下载完成，直接阅读
//					onTaskUpdate(manager.getBook(), false, true, 1, DownBookJob.STATE_FINISHED);
//				}
//			}else if(state == DownBookJob.STATE_RUNNING){
//				// 正在下载
//				manager.setIsRead(isRead);
//			}else{
//				// 其他状态，重新下载。
//				manager.setIsRead(isRead);
//				addJob2DownQueue(manager);
//			}
//		}
	}
	
	// 根据book对象获取该book的下载book对象
	public Book getBook(Book book) {
		for (HtmlDownBookManager manager : mHtmlBookList) {
			if (manager.getBook().equals(book)) {
				return manager.getBook();
			}
		}
		return null;
	}
	
	public HtmlDownBookManager getJob(Book book)
	{
		for (HtmlDownBookManager manager : mHtmlBookList) {
			if (manager.getBook().equals(book)) {
				return manager;
			}
		}
		return null;
	}

	/**
	 * 删除任务.
	 */
	public void removeJob(final HtmlDownBookManager manager)
	{
		new GenericTask()
		{
			protected TaskResult doInBackground(TaskParams... params)
			{
				// 删除数据库资源
				DBService.deleteBook(manager.getBook());
				
				// 删除书籍任务
				synchronized (mHtmlBookList) {
					manager.stopAllJob();
					mHtmlBookList.remove(manager);
				}
				return null;
			}
		}.execute();
	}

	public void removeJobMemory(final HtmlDownBookManager manager)
	{
		synchronized (mHtmlBookList) {
			int state = manager.getState();
			manager.stopAllJob();
			if(state == DownBookJob.STATE_RUNNING){
				mRunningCount--;
				if(mRunningCount < 0){
					mRunningCount = 0;
				}
				
//				Log.d("ouyang1", "--removeJobMemory--mRunningCount: "+ mRunningCount);
			}
			mHtmlBookList.remove(manager);
		}
	}

	/**
	 * 停止所有任务<br>
	 * 需要在异步线程中调用
	 */
	public void stopAllJob()
	{
		if (mHtmlBookList == null) {
			return;
		}
		
		// 保存所有job到数据库
		synchronized (mHtmlBookList) {
			while (mHtmlBookList.size() > 0) {
				HtmlDownBookManager manager = mHtmlBookList.remove(0);
				manager.stopAllJob();
			}
		}
		mRunningCount = 0;
//		Log.d("ouyang1", "--stopAllJob--mRunningCount: "+ mRunningCount);
	}

	 /**
	 * 暂停任务.
	 */
	public void pauseJob(HtmlDownBookManager manager, boolean notify)
	{
		// 暂停当前任务
		int state = manager.getState();
		manager.stopAllJob();
		if(state == DownBookJob.STATE_RUNNING){
			mRunningCount--;
			if(mRunningCount < 0){
				mRunningCount = 0;
			}
//			Log.d("ouyang1", "--pauseJob--mRunningCount: "+ mRunningCount);
		}
		manager.setState(DownBookJob.STATE_PAUSED);
//		manager.pauseAllJob();
		
		// 通知其他任务开始下载
		if (notify){
			notifyJobFinished();
		}
		
		// 通知UI暂停
		onTaskUpdate(manager.getBook(), false, true, 0, DownBookJob.STATE_PAUSED);
	}
	
	/**
	 * 在退出程序时暂停所有正在下载中的任务
	 */
	public void pauseAllJob()
	{
		if (mHtmlBookList != null) {
			for (HtmlDownBookManager manager : mHtmlBookList) {
				manager.pauseAllJob();
			}
		}
		mRunningCount = 0;
//		Log.d("ouyang1", "--pauseAllJob--mRunningCount: "+ mRunningCount);
	}

	/**
	 * 添加任务到下载队列.
	 */
	private void addJob2DownQueue(HtmlDownBookManager manager, boolean forceThread)
	{
		// 更新队列中已经存在的job，放在顶部
//		manager.stopAllJob();
		mHtmlBookList.remove(manager);
		mHtmlBookList.add(0, manager);

		// 下载线程不超过2个，否则等待
		if (mRunningCount < RUNNING_MAX || forceThread) {
			mRunningCount++;
//			Log.d("ouyang1", "--addJob2DownQueue-111-mRunningCount: "+ mRunningCount);
			manager.setState(DownBookJob.STATE_RUNNING);
			startDownTask(manager);
		} else {
//			Log.d("ouyang1", "--addJob2DownQueue-2222-mRunningCount: "+ mRunningCount);
			onTaskUpdate(manager.getBook(), false, true, 0, DownBookJob.STATE_RUNNING);
			manager.setState(DownBookJob.STATE_WAITING);
		}
	}

	/**
	 * 获取第一个等待任务下载
	 */
	private HtmlDownBookManager getFirstWaitingJob()
	{
		if (mHtmlBookList == null || mHtmlBookList.size() == 0) {
			return null;
		}
		
		synchronized (mHtmlBookList) {
			for (int i = mHtmlBookList.size() - 1; i >= 0; i--) {
				HtmlDownBookManager job = mHtmlBookList.get(i);
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
		
		HtmlDownBookManager manager = getFirstWaitingJob();
		if (manager != null) {
			manager.setState(DownBookJob.STATE_RUNNING);
			
			mRunningCount++;
//			Log.e("ouyang1", "--notifyJobFinished----startDownTask---mRunningCount: "+mRunningCount);
			startDownTask(manager);
		}
	}
	
	/**
	 * 启动下载书籍任务.
	 */
	private void startDownTask(HtmlDownBookManager manager)
	{
		manager.setState(DownBookJob.STATE_RUNNING);
		onTaskUpdate(manager.getBook(), false, true, 0, DownBookJob.STATE_RUNNING);
		manager.registerDownload(manager.getBook());
	}
	
	/**
	 * 一本Html书籍的下载回调
	 * stateCode： 0、成功  	-1、失败  	1、cancel
	 */
	public void onUpdate(HtmlDownBookManager manager, int stateCode){
		mRunningCount--;
		if(mRunningCount < 0){
			mRunningCount = 0;
		}
		
		if(stateCode == 0){
			// 单本书设置状态
			manager.setState(DownBookJob.STATE_FINISHED);
			
			// 通知其他html书籍继续下载
			notifyJobFinished();
			// 下载成功，UI提醒
			onTaskUpdate(manager.getBook(), false, true, 0, DownBookJob.STATE_FINISHED);
		}else if(stateCode == -1){
			int state = manager.getState();
			if(state == DownBookJob.STATE_FAILED){
				return;
			}
//			Log.w("ouyang1", "-DownManager--onUpdate--html 下载失败-- ");
			
			manager.setState(DownBookJob.STATE_FAILED);
			// 通知其他html书籍继续下载
			notifyJobFinished();
			
			onTaskUpdate(manager.getBook(), false, true, 0, DownBookJob.STATE_FAILED);
		}
		
		DownBookJob job = DownBookManager.getInstance().getJob(manager.getBook());
		if(job != null){
			job.getBook().setOnlineBook(false);
			DBService.saveBook(job.getBook());
		}
		
		// 单本书籍下载停止(成功或失败)，发送广播通知UI改变
		Intent intent = new Intent(DownBookManager.ACTION_INTENT_DOWNSTATE);
		SinaBookApplication.gContext.sendBroadcast(intent);
	}
	
	public void deleteBook(Activity activity, final Book book){
		DownBookJob job = DownBookManager.getInstance().getJob(book);
		if (job != null) {
			DownBookManager.getInstance().removeJob(job);
		}
		
		HtmlDownBookManager manager =  HtmlDownManager.getInstance().getJob(book);
		if(manager != null){
			HtmlDownManager.getInstance().removeJobMemory(manager);
		}
		
//		// SD卡上的书籍不能被删除
//		if (book.getDownloadInfo().getImageUrl() != null
//				&& !book.getDownloadInfo().getImageUrl()
//						.startsWith(Book.SDCARD_PATH_IMG)) {
//			StorageUtil.deleteFolder(book.getDownloadInfo().getFilePath());
//			StorageUtil.deleteFolder(book.getDownloadInfo().getImageFolder());
//		}
		
		boolean hasDBCache = false;
		ArrayList<Book> cacheList = DBService.getAllBookCache();
		for(int a = 0; a < cacheList.size(); ++a){
			Book tmpBook = cacheList.get(a);
			if(tmpBook.equals(book)){
				hasDBCache = true;
				break;
			}
		}
		
		if(!hasDBCache){
			// 开线程删除文件夹
			final String path = book.getDownloadInfo().getOriginalFilePath();
			new GenericTask() {
				protected TaskResult doInBackground(TaskParams... params) {
					FileUtils.deleteFile(path);
					return null;
				}
			}.execute();
			
			// 删除fbreader记录
			final BookCollectionShadow collection = new BookCollectionShadow();
			collection.bindToService(activity, new Runnable() {
				public void run() {
					collection.removeBook(
							collection.getBookByFile(ZLFile.createFileByPath(path)),
							false);
					collection.unbind();
				}
			});
		}
		
		// 更新书架
		CloudSyncUtil.getInstance().delCloud(activity, book);
	}
	
	

	
}
