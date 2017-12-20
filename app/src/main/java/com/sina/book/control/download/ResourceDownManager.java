package com.sina.book.control.download;


import java.util.ArrayList;
import java.util.HashMap;


/**
 * Html书籍资源下载管理类.
 */
public class ResourceDownManager
{
	/** 最大下载线程 */
	private static final int				RUNNING_MAX						= 5;

	/** 创建下载任务OK. */
	public static final int					FLAG_START_CACHE				= 0;

	/** 该书籍不支持下载. */
	public static final int					FLAG_UNSUPPORT					= 1;

	/** 已经下载完成. */
	public static final int					FLAG_CACHED						= 2;

	/** 已经在下载列表. */
	public static final int					FLAG_CACHING					= 3;

	/** 错误. */
	public static final int					FLAG_ERROR						= 4;

	/** 正在运行的任务个数. */
	private int								mRunningCount					= 0;
	
	private ArrayList<ResourceDownJob>			mResJobs						= new ArrayList<ResourceDownJob>();
	
	
	// key：章节地址  value：章节内的资源下载列表
	private HashMap<String , ArrayList<ResourceDownJob>>  mResMap = new HashMap<String, ArrayList<ResourceDownJob>>();
	
	private IResourceTaskUpdateListener listener;
	

	/** 下载完成listener. */
	private IDownTaskFinishListener			mDownFinishListener				= new IDownTaskFinishListener()
																			{
																				@Override
																				public void onTaskFinished(DownResult taskResult)
																				{
																					ResourceDownJob job = null;
																					mRunningCount--;
																					if (taskResult != null) {
																						if (taskResult.task instanceof ResourceDownTask) {
																							ResourceDownTask task = (ResourceDownTask) taskResult.task;
																							job = task.getJob();
																							
																							if(taskResult.stateCode == 0){
																								// 资源下载成功
//																								Log.d("ouyang", "-onTaskFinished-资源下载--成功-: "+ job.getUrl());
																								doFinish(job);
																								onTaskUpdate(job, DownBookJob.STATE_FINISHED);
																								// 通知其他任务继续下载
																								notifyJobFinished();
																							}else{
																								// 资源下载失败
//																								Log.e("ouyang", "-onTaskFinished-资源下载--失败--: "+ job.getUrl());
																								stopAllJob();
//																								doError(job);
																								onTaskUpdate(job, DownBookJob.STATE_FAILED);
																							}
																							
//																							if (taskResult.stateCode != -1) {
//																								if (ConstantData.CODE_SUCCESS.equals(""
//																										+ taskResult.stateCode)) {
//																									// 成功
//																									doFinish(job);
//																									onTaskUpdate(job, DownBookJob.STATE_FINISHED);
//																									// 通知其他任务继续下载
//																									notifyJobFinished();
//																								} else {
//																									// 失败
//																									Log.e("ouyang", "--资源下载--失败--: "+ job.getUrl());
//																									stopAllJob();
////																									doError(job);
//																									onTaskUpdate(job, DownBookJob.STATE_FAILED);
//																								}
//																							} else {
//																								// 处理下载失败的
//																								stopAllJob();
////																								doError(job);
//																								
//																								Log.e("ouyang", "--资源下载--失败2--: "+ job.getUrl());
//																								onTaskUpdate(job, DownBookJob.STATE_FAILED);
//																							}
																						}
																					}else{
																						// 资源下载被取消
//																						stopAllJob();
																					}
																				}
																			};


	public void setListener(IResourceTaskUpdateListener listener){
		this.listener = listener;
	}
	
	// 通知htmlmanager状态
	public void onTaskUpdate(ResourceDownJob job, int stateCode)
	{
		if(stateCode == DownBookJob.STATE_FINISHED){
			// 下载完成， 先检测一章资源是否下载完整
			boolean result = checkDownloadComplete(job);
			if(result){
//				Log.e("ouyang", "--资源下载--检测完整--true: ");
				if(listener != null){
					listener.onState(job.chapterTag,true, 0);
				}
			}
		}else{
			// 下载失败，通知html管理器中断下载
//			Log.e("ouyang", "--资源下载--失败处理--false: ");
			if(listener != null){
				listener.onState(job.chapterTag, false, -1);
			}
		}
	}
	
	private boolean checkDownloadComplete(ResourceDownJob job){
		ArrayList<ResourceDownJob> list =  mResMap.get(job.chapterTag);
		
		boolean isComplete = true;
		for(int i = 0; i < list.size(); ++i){
			ResourceDownJob tJob = list.get(i);
			if(tJob.getState() != DownBookJob.STATE_FINISHED){
				isComplete = false;
			}
		}
		return isComplete;
	}
	
	public String mBookDir;
	
	public ResourceDownManager(String bookDir)
	{
		mBookDir = bookDir;
	}

	// 注册资源下载
	public void registerResDownload(String chapterUrl, ArrayList<ResourceDownJob> resList){
		if(mResMap.get(chapterUrl) == null){
			mResMap.put(chapterUrl, resList);
		}
		
		for(int i = 0; i < resList.size(); ++i){
			downRes(resList.get(i));
		}
	}
	
//	private void doError(ResourceDownJob job)
//	{
//		job.setState(DownBookJob.STATE_FAILED);
//	}

	private void doFinish(ResourceDownJob job)
	{
		job.setState(DownBookJob.STATE_FINISHED);
	}

//	/**
//	 * 初始化下载管理器，读取数据库数据.
//	 */
//	public void init()
//	{
////		initCacheBooks();
//	}

//	/**
//	 * 从数据库初始化缓存和正在缓存的数据.
//	 */
//	public void initCacheBooks()
//	{
//		if (!mBookJobs.isEmpty())
//			return;
//
//		ArrayList<Book> books = DBService.getAllBook();
//
//		mBookJobs.clear("initCacheBooks");
//		if (books != null && books.size() > 0) {
//			for (Book book : books) {
//				DownBookJob job = new DownBookJob(book);
//				if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) > 0.0001
//						|| book.getDownloadInfo().getDownLoadState() != DownBookJob.STATE_FINISHED) {
//					job.setState(DownBookJob.STATE_FAILED);
//				} else {
//					job.setState(book.getDownloadInfo().getDownLoadState());
//				}
//				mBookJobs.add(job);
//			}
//		}
//	}

//	/**
//	 * 重置缓存的书籍（从数据库中读取）
//	 * <ul>
//	 * <li>慎用
//	 * </ul>
//	 */
//	public void resetCacheBooks()
//	{
//		mBookJobs.clear("resetCacheBooks");
//		initCacheBooks();
//	}

//	/**
//	 * 书的最后阅读时间和下载时间比较器
//	 */
//	private Comparator<DownBookJob>	mReadOrDownTimeComparator	= new Comparator<DownBookJob>()
//																{
//																	@Override
//																	public int compare(DownBookJob job1, DownBookJob job2)
//																	{
//																		Book book1 = job1.getBook();
//																		Book book2 = job2.getBook();
//																		if (book1 != null && book2 != null) {
//																			long book1LastReadTime = Math.max(book1.getReadInfo()
//																					.getLastReadTime(), book1.getDownloadInfo()
//																					.getDownloadTime());
//
//																			long book2LatsReadTime = Math.max(book2.getReadInfo()
//																					.getLastReadTime(), book2.getDownloadInfo()
//																					.getDownloadTime());
//
//																			if (book1LastReadTime > book2LatsReadTime) {
//																				return -1;
//																			} else if (book1LastReadTime < book2LatsReadTime) {
//																				return 1;
//																			}
//																		}
//																		return 0;
//																	}
//																};

	/**
	 * 根据下载或者导入、阅读、更新时间进行排序，包含全部书籍
	 * 
	 * @return the all jobs
	 */
	public ArrayList<ResourceDownJob> getAllJobs()
	{
		return mResJobs;
	}
	
	/**
	 * 判断是否需要下载书籍，如果需要，创建任务.
	 */
	public int downRes(ResourceDownJob job)
	{
		mResJobs.remove(job);
		mResJobs.add(job);
		addJob2DownQueue(job);
		return FLAG_START_CACHE;
	}

	public ResourceDownJob getJob(String url)
	{
		for (ResourceDownJob job : mResJobs) {
			if (job.getUrl().equals(url)) {
				return job;
			}
		}
		return null;
	}


	/**
	 * 删除任务.
	 */
	public void removeJob(final ResourceDownJob job)
	{
		// 删除书籍任务
		synchronized (mResJobs) {
			mResJobs.remove(job);
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
		if (mResJobs == null) {
			return;
		}
		
		// 保存所有job到数据库
		synchronized (mResJobs) {
			while (mResJobs.size() > 0) {
				ResourceDownJob job = mResJobs.remove(0);
				if (job.getTask() != null) {
					job.getTask().cancel(true);
					job.setTask(null);
				}
				if (job.getState() != DownBookJob.STATE_FINISHED) {
					job.setState(DownBookJob.STATE_FAILED);
				}
			}
		}
		
		// 删除hashmap
		mResMap.clear();
	}
	
	/**
	 * 暂停单个任务.
	 * 
	 * @param job
	 *            the job
	 */
	public void pauseJob(ResourceDownJob job, boolean notify)
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

//		onTaskUpdate(job.getUrl(), false, true, DownBookJob.STATE_PAUSED);
	}

	/**
	 * Job pause.
	 * 
	 * @param job
	 *            the job
	 */
	private void jobPause(ResourceDownJob job)
	{
		job.setState(DownBookJob.STATE_PAUSED);
	}
	
	/**
	 * 在退出程序时暂停所有正在下载中的任务
	 */
	public void pauseAllJob()
	{
		if (mResJobs != null) {
			for (ResourceDownJob job : mResJobs) {
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
	}

	/**
	 * 继续任务.
	 * 
	 * @param job
	 *            the job
	 */
	public void resumeJob(ResourceDownJob job)
	{
		mResJobs.remove(job);
		mResJobs.add(0, job);
		addJob2DownQueue(job);
//		onTaskUpdate(job.getUrl(), false, true, DownBookJob.STATE_RUNNING);
	}
	
	/**
	 * 添加任务到下载队列.
	 * 
	 * @param job
	 *            the job
	 */
	private void addJob2DownQueue(ResourceDownJob job)
	{
		// 更新队列中已经存在的job，放在顶部
//		mResJobs.remove(job);
//		mResJobs.add(job);
		
		// 检查存储空间足够
		// if (StorageUtil.checkExternalSpace(job.getBook().getDownloadInfo()
		// .getFileSize())) {
		// 下载线程不超过5个，否则等待
		if (mRunningCount < RUNNING_MAX) {
			mRunningCount++;
			job.setState(DownBookJob.STATE_RUNNING);
			startDownTask(job);
		} else {
			job.setState(DownBookJob.STATE_WAITING);
		}
		// } else {
		// LogUtil.e(TAG, "存储卡无可用的下载空间");
		// job.setState(DownBookJob.STATE_PAUSED);
		// notifyJobFinished();
		// }
	}

	/**
	 * 获取第一个等待任务下载
	 */
	private ResourceDownJob getFirstWaitingJob()
	{
		if (mResJobs == null || mResJobs.size() == 0) {
			return null;
		}

		synchronized (mResJobs) {
			for (int i = mResJobs.size() - 1; i >= 0; i--) {
				ResourceDownJob job = mResJobs.get(i);
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
		ResourceDownJob job = getFirstWaitingJob();
		if (job != null) {
			addJob2DownQueue(job);
		}
	}

	/**
	 * 启动下载书籍任务.
	 */
	private void startDownTask(ResourceDownJob job)
	{
		job.setState(DownBookJob.STATE_RUNNING);
		ResourceDownTask task = new ResourceDownTask(job);
		task.setTaskFinishListener(mDownFinishListener);
		job.setTask(task);
		task.execute();
	}

}
