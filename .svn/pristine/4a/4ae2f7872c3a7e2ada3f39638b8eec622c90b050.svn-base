package com.sina.book.control.download;

import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ChapterList;
import com.sina.book.data.ConstantData;
import com.sina.book.db.DBService;
import com.sina.book.parser.ChapterListParser;
import com.sina.book.reader.SinaXmlHandler;
import com.sina.book.ui.notification.DownloadBookNotification;
import com.sina.book.util.LogUtil;

/**
 * 下载书籍的事务
 * 
 * @author fzx
 * 
 */
public class DownBookTask extends DownFileTask {
	private static final String TAG = "DownBookTask";

	private static final int MAX_REFRESH_TIME = 1000;
	private DownBookJob mJob;
	private long mLastPos = 0;
	private RequestTask mGetChaptersTask;

	public DownBookTask(DownBookJob job) {
		super(job.getBook().getDownloadInfo().getFilePath(), job.getBook().getDownloadInfo().getFileSize());
		mJob = job;
	}

	public DownBookJob getJob() {
		return mJob;
	}

	public void setJob(DownBookJob job) {
		this.mJob = job;
	}

	/**
	 * 同步执行<br>
	 * 请不要在UI线程使用该方法<br>
	 * 
	 * @return
	 */
	public final DownResult syncExecute(TaskParams... params) {
		return doInBackground(params);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (mGetChaptersTask != null) {
			mGetChaptersTask.abort();
		}
		return super.cancel(mayInterruptIfRunning);
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		if (mFileSize > 0) {
			DownloadBookNotification.getInstance().updateNotification(mJob.getBook());
			mJob.getBook().getDownloadInfo().setProgress(getProgress());
			mJob.getBook().getDownloadInfo().setFileSize(mFileSize);
		}
		if (mJob.getBook().getDownloadInfo().getProgress() > 1.0) {
			mJob.getBook().getDownloadInfo().setProgress(1.0);
		}
		mJob.getBook().getDownloadInfo().setParsering(mIsWriting);
		long timePos = System.currentTimeMillis();
		// 1000ms才会去回调一次，防止书架界面更新太多次，导致闪烁
		if (timePos - mLastPos > MAX_REFRESH_TIME) {
			DownBookManager.getInstance().onTaskUpdate(mJob.getBook(), false, true, DownBookJob.STATE_RUNNING);
			mLastPos = timePos;
		}
		super.onProgressUpdate(values);
	}

	@Override
	protected DownResult doInBackground(TaskParams... params) {
		LogUtil.d("SinaXmlHandler", "DownBookTask >> doInBackground");
		cancelable = true;
		setUrl(mJob.getBook().getDownUrl());

		if (!isCancelled()) {
			Book book = mJob.getBook();
			// 这里防止由于强制退出导致上次下载下来的xml没有解析的问题
			if (!mIsWriting) {
				cancelable = false;
				SinaXmlHandler.convertXml2Dat(this, "DownBookTask(1)");
			}
			cancelable = true;

			// 获取最后一个章节
			Chapter chapter = book.getLastChapter();
//			mTempFileSize = Math.abs((float) (mFileSize * book.getDownloadInfo().getProgress()));

			// 最后一个章节的章节ID
			int lastChapterId = chapter.getChapterId();
			// 总章节数
			int chaptersSize = book.getNum();

			// LogUtil.d(TAG, "TempFileSize : " + mTempFileSize);
			// LogUtil.d(TAG, "Last chapter id : " + lastChapterId);
			// LogUtil.d(TAG, "Total chapters size : " + chaptersSize);

			if (lastChapterId < chaptersSize) {
				mUrl = book.getDownUrl();
				super.doInBackground(params);
			} else {
				mResult.stateCode = 0;
			}

			if (!isCancelled() && !mIsWriting) {
				// LogUtil.i("SinaXmlHandler",
				// "DownBookTask >> doInBackground >> 1");
				mIsWriting = true;
				book.getDownloadInfo().setParsering(mIsWriting);
				cancelable = false;
				boolean flag = SinaXmlHandler.convertXml2Dat(this, "DownBookTask(2)");

				// LogUtil.i("SinaXmlHandler",
				// "DownBookTask >> doInBackground >> 2");

				if (flag || DBService.getLastChapter(book).getChapterId() >= chaptersSize) {
					mResult.stateCode = 0;
				}

				mIsWriting = false;
				book.getDownloadInfo().setParsering(mIsWriting);

				int payType = book.getBuyInfo().getPayType();
				// 全本购买的书和章节收费的书籍，补全章节列表
				if (payType == Book.BOOK_TYPE_CHAPTER_VIP) {
					// TODO: 按章节购买， 补全收费章节信息
					LogUtil.i("SinaXmlHandler", "DownBookTask >> doInBackground >> 3");
					reqChapters(book);
					LogUtil.i("SinaXmlHandler", "DownBookTask >> doInBackground >> 4");
				} else if (payType == Book.BOOK_TYPE_VIP) {
					// 包月的书籍已经完全下载不需要补全章节列表
					if (!book.isSuite()) {
						reqChapters(book);
					}
				}

				publishProgress(mTempFileSize, mFileSize);
			}
		}
		return mResult;
	}

	/**
	 * 下载并解析成功后，针对章节免费的图书书补全它的章节列表
	 */
	private void reqChapters(Book book) {
		String reqUrl = String
				.format(ConstantData.URL_GET_CHAPTERS, book.getBookId(), book.getSid(), book.getBookSrc());
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		ChapterListParser chaptersParser = new ChapterListParser();
		if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
			chaptersParser.setAllBookHasBuy(book.getBuyInfo().isHasBuy());
		}
		mGetChaptersTask = new RequestTask(chaptersParser);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		TaskResult taskResult = mGetChaptersTask.syncExecute(params);
		if (isCancelled()) {
			return;
		}
		if (taskResult != null && taskResult.retObj instanceof ChapterList) {
			// 获取章节列表接口数据
			ChapterList updateChaptersResult = (ChapterList) taskResult.retObj;
			book.setChapters(updateChaptersResult.getChapters(), "[DownBookTask-reqChapters]");
			DBService.updateChapterVipBookChapters(this, book, updateChaptersResult.getChapters());
		}
	}
}
