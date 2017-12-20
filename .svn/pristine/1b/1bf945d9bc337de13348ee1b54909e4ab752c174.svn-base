package com.sina.book.control.download;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 一个下载书籍的job，包含负责下载的task。
 * 
 * @author fzx
 */
public class ResourceDownJob implements Parcelable
{

	// 图片
	public static final int	TYPE_IMAGE		= 1;

	// CSS
	public static final int	TYPE_CSS		= 2;

	private DownFileTask	mTask;

	private int				mState			= DownBookJob.STATE_PREPARING;
	
	// 下载地址
	public String			mUrl;
	
	public String mBookId;
	
	// 资源标记
	public int mType;

	// 章节标记
	public String	chapterTag;
	
	public ResourceDownManager manager;

	public ResourceDownJob(ResourceDownManager manager, String url, int type, String bookId, String chapterTag)
	{
		this.manager = manager;
		mUrl = url;
		mBookId = bookId;
		this.mType = type;
		this.chapterTag = chapterTag;
	}

	public ResourceDownJob(Parcel source)
	{
		mUrl = source.readString();
		chapterTag = source.readString();
		mType = source.readInt();
		mState = source.readInt();
	}
	
	/**
	 * Gets the book.
	 * 
	 * @return the book
	 */
	public String getUrl()
	{
		return mUrl;
	}

	/**
	 * Sets the book.
	 * 
	 * @param book
	 *            the new book
	 */
	public void setUrl(String url)
	{
		mUrl = url;
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public int getState()
	{
		return mState;
	}

	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(int state)
	{
		mState = state;
	}

	/**
	 * Gets the task.
	 * 
	 * @return the task
	 */
	public DownFileTask getTask()
	{
		return mTask;
	}

	/**
	 * Sets the task.
	 * 
	 * @param task
	 *            the new task
	 */
	public void setTask(DownFileTask task)
	{
		mTask = task;
	}

	/**
	 * Start.
	 */
	public void start()
	{
		manager.resumeJob(this);
	}

	public void pause()
	{
		pause(true);
	}

	/**
	 * 
	 * @param notify
	 */
	public void pause(boolean notify)
	{
		manager.pauseJob(this, notify);
	}

	@Override
	public int hashCode()
	{
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
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ResourceDownJob other = (ResourceDownJob) obj;
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
	public int describeContents()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		// dest.writeSerializable(mBook);
		dest.writeString(mUrl);
		dest.writeString(chapterTag);
		dest.writeInt(mType);
		dest.writeInt(mState);
	}
	
	/** The Constant CREATOR. */
	public static final Parcelable.Creator<ResourceDownJob>	CREATOR	= new Parcelable.Creator<ResourceDownJob>()
																	{

																		public ResourceDownJob createFromParcel(Parcel source)
																		{
																			return new ResourceDownJob(source);
																		}

																		public ResourceDownJob[] newArray(int size)
																		{
																			return new ResourceDownJob[size];
																		}

																	};

}
