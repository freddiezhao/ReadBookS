package com.sina.book.data;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import android.text.TextUtils;

import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * 书籍下载状态
 * 
 * @author MarkMjw
 * @date 2013-2-4
 */
public class BookDownloadInfo implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	/** 下载地址. */
	private String				mImageUrl;

	/** 原始书籍路径. */
	private String				mOriginalFilePath;

	/** 小说内容的文件. */
	private String				mFilePath;

	/** 文件大小. */
	private float				mFileSize;

	/** 下载进度. */
	private double				mProgress;

	/** 下载状态. */
	private int					mDownLoadState;

	/** 正在解析. */
	private boolean				mIsParsering		= false;

	/** 微盘文件的下载地址. */
	private String				mVdiskDownUrl;

	/** 微盘文件的路径. */
	private String				mVdiskFilePath;

	// 添加Epub的下载信息
	// private String mEpubDownUrl;
	// private String mEpubFilePath;
	// public String getEpubDownUrl() {
	// return mEpubDownUrl;
	// }
	//
	// public void setEpubDownUrl(String epubDownUrl) {
	// this.mEpubDownUrl = epubDownUrl;
	// }
	//
	// public String getEpubFilePath() {
	// return mEpubFilePath;
	// }
	//
	// public void setEpubFilePath(String epubFilePath) {
	// this.mEpubFilePath = epubFilePath;
	// }

	/** 加入书架的时间. */
	private long				mDownloadTime;

	/** 文件创建时间. */
	private Date				mCreateFileDate		= new Date();

	/** 位置类型. */
	private int					mLocationType		= Book.BOOK_LOCAL;

	private Book				myBook;

	public BookDownloadInfo(Book myBook)
	{
		this.myBook = myBook;
	}

	public Book getMyBook()
	{
		return myBook;
	}

	public String getVDiskFilePath()
	{
		return mVdiskFilePath;
	}

	public void setVDiskFilePath(String vdiskFilePath)
	{
		this.mVdiskFilePath = vdiskFilePath;
	}

	public long getDownloadTime()
	{
		return mDownloadTime;
	}

	public void setDownloadTime(long downloadTime)
	{
		this.mDownloadTime = downloadTime;
	}

	public void setVDiskDownUrl(String vDiskDownUrl)
	{
		this.mVdiskDownUrl = vDiskDownUrl;

		// 初始化时需要设置
		if (!TextUtils.isEmpty(vDiskDownUrl) && vDiskDownUrl.startsWith(Book.EPUB_PATH_PROTOCOL)) {
			myBook.setIsEpubOnly(true);
		}
	}

	public String getVDiskDownUrl()
	{
		return mVdiskDownUrl;
	}

	public void setOriginalFilePath(String originalFilePath)
	{
		this.mOriginalFilePath = originalFilePath;
	}

	public int getDownLoadState()
	{
		return mDownLoadState;
	}

	public void setDownLoadState(int downLoadState)
	{
		this.mDownLoadState = downLoadState;
	}

	public boolean isParsering()
	{
		return mIsParsering;
	}

	public void setParsering(boolean isParsering)
	{
		this.mIsParsering = isParsering;
	}

	public double getProgress()
	{
		return mProgress;
	}

	public void setProgress(double d)
	{
		this.mProgress = d;
	}

	public String getImageUrl()
	{
		return mImageUrl;
	}

	/**
	 * 新浪书城的书一定要设置imageUrl<br>
	 * 即使设置成""也行，是否加密的逻辑依赖这里不为null
	 * 
	 * @param imageUrl
	 */
	public void setImageUrl(String imageUrl)
	{
		this.mImageUrl = imageUrl;
	}

	public String getOriginalFilePath()
	{
		if (TextUtils.isEmpty(mOriginalFilePath)) {
			if (myBook.isHtmlRead()) {
				String path = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK)
						+ "/bd" + myBook.getBookId() + Book.EPUB_SUFFIX;
				return path;
			} else {
				return getFilePath();
			}
		}
		return mOriginalFilePath;
	}

	/**
	 * 获取文件路径，若没有则生成一个唯一的字符串作为文件名
	 * 
	 * @return
	 */
	public String getFilePath()
	{
		if (TextUtils.isEmpty(mFilePath)) {
			mFilePath = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK)
					+ "/" + Util.getUniqueString() + Book.TMP_SUFFIX;
		}
		return mFilePath;
	}

	public String getFilePathOnly()
	{
		return mFilePath;
	}

	/**
	 * 书籍文件是否存在
	 * 
	 * @return
	 */
	public boolean bookFileExist()
	{
		// LogUtil.d("CheckBookExist",
		// "BookDownloadInfo >> bookFileExist >> mFilePath=" + mFilePath);
		if (!TextUtils.isEmpty(mFilePath)) {
			File file = new File(mFilePath);
			boolean exists = file.exists();
			long length = file.length();
			// LogUtil.d("CheckBookExist",
			// "BookDownloadInfo >> bookFileExist >> exists=" + exists);
			// LogUtil.d("CheckBookExist",
			// "BookDownloadInfo >> bookFileExist >> length=" + length);
			if (exists && length > 0) {
				// LogUtil.i("CheckBookExist",
				// "BookDownloadInfo >> bookFileExist >1> 文件存在并且文件长度不为零");
				return true;
			}
		}

		// LogUtil.e("CheckBookExist",
		// "BookDownloadInfo >> bookFileExist >2> 文件不存在或者文件长度为零");
		return false;
	}

	public void setFilePath(String filePath)
	{
		this.mFilePath = filePath;
		// LogUtil.d("CheckBookExist",
		// "BookDownloadInfo >> setFilePath >> mFilePath (1) =" + mFilePath);
	}

	public String getImageFolder()
	{
		// TODO
		String path = getFilePath();
		int dotIndex = path.lastIndexOf(".");
		StringBuilder sb = new StringBuilder(mFilePath.substring(0, dotIndex));
		sb.append("/");
		return sb.toString();
	}

	public float getFileSize()
	{
		return mFileSize;
	}

	public void setFileSize(float fileSize)
	{
		this.mFileSize = fileSize;
	}

	public Date getCreateFileDate()
	{
		return mCreateFileDate;
	}

	public int getLocationType()
	{
		return mLocationType;
	}

	public void setLocationType(int locationType)
	{
		this.mLocationType = locationType;
	}

	public void reset()
	{
		setFilePath(null);
		setOriginalFilePath(null);
		setFileSize(0.0f);
	}

	@Override
	public String toString()
	{
		// return "BookDownloadInfo{" + "mImageUrl='" + mImageUrl + '\'' +
		// ", mOriginalFilePath='" + mOriginalFilePath
		// + '\'' + ", mFilePath='" + mFilePath + '\'' + ", mFileSize=" +
		// mFileSize + ", mProgress=" + mProgress
		// + ", mDownLoadState=" + mDownLoadState + ", mIsParsering=" +
		// mIsParsering + ", mVdiskDownUrl='"
		// + mVdiskDownUrl + '\'' + ", mVdiskFilePath='" + mVdiskFilePath + '\''
		// + ", mDownloadTime="
		// + mDownloadTime + ", mCreateFileDate=" + mCreateFileDate +
		// ", mLocationType=" + mLocationType + '}';
		return "BookDownloadInfo{mFilePath='" + mFilePath + '\''
				+ ", mOriginalFilePath='" + mOriginalFilePath + '\''
				+ ", mFileSize=" + mFileSize + ", mDownLoadState="
				+ mDownLoadState + ", mVdiskDownUrl='" + mVdiskDownUrl + '\''
				+ ", mVdiskFilePath='" + mVdiskFilePath + '\''
				+ ", mDownloadTime=" + mDownloadTime + ", mCreateFileDate="
				+ mCreateFileDate + ", mLocationType=" + mLocationType + '}';
	}
}
