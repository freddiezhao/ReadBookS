package com.sina.book.control.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.sina.book.data.Book;

/**
 * 一个下载书籍的job，包含负责下载的task。
 * 
 * @author fzx
 */
public class DownBookJob implements Parcelable {

	/** 未知状态. */
	public static final int STATE_UNKNOWN = -1;

	/** 准备状态. */
	public static final int STATE_PREPARING = 0;

	/** 等待状态. */
	public static final int STATE_WAITING = 1;

	/** 下载中状态. */
	public static final int STATE_RUNNING = 2;

	/** 暂停状态. */
	public static final int STATE_PAUSED = 3;

	/** 结束状态. */
	public static final int STATE_FINISHED = 4;

	/** 失败状态. */
	public static final int STATE_FAILED = 5;

	/** 余额不足状态. */
	public static final int STATE_RECHARGE = 6;

	/** 解析补全状态. */
	public static final int STATE_PARSER = 7;

	private Book mBook;

	private DownFileTask mTask;

	private int mState = STATE_PREPARING;
	
	
	
	/**
	 * Instantiates a new down book job.
	 * 
	 * @param book
	 *            the book
	 */
	public DownBookJob(Book book) {
		mBook = book;
	}
	
	/**
	 * Instantiates a new down book job.
	 * 
	 * @param book
	 *            the book
	 * @param task
	 *            the task
	 */
	public DownBookJob(Book book, DownBookTask task) {
		mTask = task;
	}

	/**
	 * Instantiates a new down book job.
	 * 
	 * @param source
	 *            the source
	 */
	public DownBookJob(Parcel source) {
		mBook = (Book) source.readSerializable();
		mState = source.readInt();
	}

	/**
	 * Gets the book.
	 * 
	 * @return the book
	 */
	public Book getBook() {
		return mBook;
	}

	/**
	 * Sets the book.
	 * 
	 * @param book
	 *            the new book
	 */
	public void setBook(Book book) {
		mBook = book;
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public int getState() {
		return mState;
	}

	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(int state) {
		mState = state;
	}

	/**
	 * Gets the task.
	 * 
	 * @return the task
	 */
	public DownFileTask getTask() {
		return mTask;
	}

	/**
	 * Sets the task.
	 * 
	 * @param task
	 *            the new task
	 */
	public void setTask(DownFileTask task) {
		mTask = task;
	}

	/**
	 * Start.
	 */
	public void start() {
		start(false);
	}
	
	public void start(boolean forceThread) {
		if(mBook.isHtmlRead()){
			HtmlDownManager.getInstance().downBook(mBook, false, forceThread);
		}else{
			DownBookManager.getInstance().resumeJob(this);
		}
	}
	
	
	/**
	 * Pause.
	 */
	public void pause() {
		pause(true);
	}
	
	/**
	 * 
	 * @param notify
	 */
	public void pause(boolean notify) {
		if(mBook.isHtmlRead()){
			// 暂停单本的html下载，同时更新job
			HtmlDownBookManager manager = HtmlDownManager.getInstance().getJob(mBook);
			if(manager != null){
				HtmlDownManager.getInstance().pauseJob(manager, notify);
			}
//			HtmlDownManager.getInstance().pauseAllJob();
		}else{
			DownBookManager.getInstance().pauseJob(this, notify);
		}
	}
	
	public void reset() {
		getBook().reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mBook == null) ? 0 : mBook.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownBookJob other = (DownBookJob) obj;
		if (mBook == null) {
			if (other.mBook != null)
				return false;
		} else if (!mBook.equals(other.mBook))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(mBook);
		dest.writeInt(mState);
	}

	/** The Constant CREATOR. */
	public static final Parcelable.Creator<DownBookJob> CREATOR = new Parcelable.Creator<DownBookJob>() {

		public DownBookJob createFromParcel(Parcel source) {
			return new DownBookJob(source);
		}

		public DownBookJob[] newArray(int size) {
			return new DownBookJob[size];
		}

	};

}
