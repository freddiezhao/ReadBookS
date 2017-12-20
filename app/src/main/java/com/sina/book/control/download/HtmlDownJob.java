package com.sina.book.control.download;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 一个下载 书籍的job，包含负责下载的task。
 */
public class HtmlDownJob implements Parcelable {

	private DownFileTask mTask;

	private int mState = DownBookJob.STATE_PREPARING;
	
	public String mUrl;
	
	public String bookId;
	
	public String chapterId;
	
	public HtmlDownBookManager mananger;

	public HtmlDownJob(HtmlDownBookManager manager, String url, String bookId, String chapterId) {
		this.mananger = manager;
		mUrl = url;
		this.bookId = bookId;
		this.chapterId = chapterId;
	}
	
	public HtmlDownJob(Parcel source) {
		mUrl =  source.readString();
		mState = source.readInt();
	}

	/**
	 * Gets the book.
	 * 
	 * @return the book
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * Sets the book.
	 * 
	 * @param book
	 *            the new book
	 */
	public void setUrl(String url) {
		mUrl = url;
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
//	public void start() {
//		//TODO:
//		HtmlDownBookManager.getInstance().resumeJob(this);
//	}
	
	public void pause() {
		pause(true);
	}
	
	public void pause(boolean notify) {
		mananger.pauseJob(this, notify);
	}
	
	public void reset() {
//		getBook().reset();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mUrl == null) ? 0 : mUrl.hashCode());
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
		
		HtmlDownJob other = (HtmlDownJob) obj;
		if (mUrl == null) {
			if (other.mUrl != null)
				return false;
		} else if (!mUrl.equals(other.mUrl))
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
//		dest.writeSerializable(mBook);
		dest.writeString(mUrl);
		dest.writeInt(mState);
	}

	/** The Constant CREATOR. */
	public static final Parcelable.Creator<HtmlDownJob> CREATOR = new Parcelable.Creator<HtmlDownJob>() {

		public HtmlDownJob createFromParcel(Parcel source) {
			return new HtmlDownJob(source);
		}

		public HtmlDownJob[] newArray(int size) {
			return new HtmlDownJob[size];
		}

	};

}
