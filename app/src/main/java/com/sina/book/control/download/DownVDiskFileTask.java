package com.sina.book.control.download;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;

import org.apache.http.HttpStatus;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.TaskParams;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.reader.VDiskFileHandler;
import com.sina.book.ui.notification.DownloadBookNotification;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

public class DownVDiskFileTask extends DownFileTask {
	private static final String TAG = "DownVDiskFileTask";

	private static final int MAX_REFRESH_TIME = 1000;
	private DownBookJob mJob;
	private long mLastPos = 0;

	public DownVDiskFileTask(DownBookJob job) {
		super(job.getBook().getDownloadInfo().getVDiskDownUrl(), job.getBook().getDownloadInfo().getFilePath(), job
				.getBook().getDownloadInfo().getFileSize());
		// LogUtil.d("FileMissingLog",
		// "DownVDiskFileTask >> 构造器 >> mFileSize : " + mFileSize +
		// ", mTempFileSize:"
		// + mTempFileSize);
		mJob = job;
	}

	public DownBookJob getJob() {
		return mJob;
	}

	public void setJob(DownBookJob job) {
		this.mJob = job;
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
		// 500ms才会去回调一次，防止书架界面更新太多次，导致闪烁
		if (timePos - mLastPos > MAX_REFRESH_TIME) {
			DownBookManager.getInstance().onTaskUpdate(mJob.getBook(), false, true, DownBookJob.STATE_RUNNING);
			mLastPos = timePos;
		}
		super.onProgressUpdate(values);
	}

	@Override
	protected DownResult doInBackground(TaskParams... params) {
		LogUtil.d(TAG, "doInBackground");
		if (!isCancelled()) {
			Book book = mJob.getBook();
//			mTempFileSize = Math.abs((float) (mFileSize * book.getDownloadInfo().getProgress()));
			// LogUtil.d("FileMissingLog", "DownVDiskFileTask >> mFileSize : " +
			// mFileSize + ", mTempFileSize:"
			// + mTempFileSize + ", book.getDownloadInfo().getProgress()=" +
			// book.getDownloadInfo().getProgress());

			// 下载是否已经成功
			int downloadState = ConstantData.CODE_FAIL_KEY;

			if (mTempFileSize == mFileSize && mFileSize != 0) {
				downloadState = ConstantData.CODE_SUCCESS_KEY;
			} else {
				mUrl = book.getDownloadInfo().getVDiskDownUrl();
				// LogUtil.d("FileMissingLog",
				// "DownVDiskFileTask >> 微盘文件下载 >> mUrl=" + mUrl);
				super.doInBackground(params);
				if (mTempFileSize < mFileSize || mFileSize == 0) {
					downloadState = ConstantData.CODE_FAIL_KEY;
					// LogUtil.d("FileMissingLog",
					// "DownVDiskFileTask >> 微盘文件下载 >> 失败1");
				} else {
					downloadState = ConstantData.CODE_SUCCESS_KEY;
				}
			}

			if (!isCancelled() && !mIsWriting && downloadState != ConstantData.CODE_FAIL_KEY) {
				mIsWriting = true;
				book.getDownloadInfo().setParsering(mIsWriting);
				publishProgress(mTempFileSize, mFileSize);
				if (VDiskFileHandler.convertTmp2Dat(book, this)) {
					// 唯一下载解析成功出去的标识
					mResult.stateCode = ConstantData.CODE_SUCCESS_KEY;
					// LogUtil.d("FileMissingLog",
					// "DownVDiskFileTask >> 微盘文件下载 >> 成功1");
				} else {
					// LogUtil.d("FileMissingLog",
					// "DownVDiskFileTask >> 微盘文件下载 >> 失败2");
				}
				mIsWriting = false;
				book.getDownloadInfo().setParsering(mIsWriting);
				publishProgress(mTempFileSize, mFileSize);
			}
		}
		return mResult;
	}

	protected HttpURLConnection initHttpConnection() throws IOException, MalformedURLException, ProtocolException {
		LogUtil.d(TAG, "url : " + mUrl);
		URL url = new URL(mUrl);
		HttpURLConnection con = HttpUtil.getHttpUrlConnection(url, SinaBookApplication.gContext, false);
		// Range头域 用来实现断点续传
		// 表示头500个字节：Range: bytes=0-499
		// 表示第二个500字节：Range: bytes=500-999
		// 表示最后500个字节：Range: bytes=-500
		// 表示500字节以后的范围：Range: bytes=500-
		// 第一个和最后一个字节：Range: bytes=0-0,-1
		// 同时指定几个范围：Range: bytes=500-600,601-999
		con.setRequestProperty("RANGE", "bytes=" + Math.round(mTempFileSize) + "-");
		LogUtil.d(TAG, "RANGE " + "bytes=" + Math.round(mTempFileSize) + "-");
		con.connect();
		return con;
	}

	protected synchronized boolean downloadData() {
		// LogUtil.d("FileMissingLog",
		// "DownVDiskFileTask >> downloadData >> 开始下载微盘文件");
		boolean flag = false;
		InputStream stream = null;
		try {
			mUrlConnection = initHttpConnection();
			int code = mUrlConnection.getResponseCode();
			if (code == HttpStatus.SC_OK || code == HttpStatus.SC_PARTIAL_CONTENT) {
				stream = mUrlConnection.getInputStream();
				int length = mUrlConnection.getContentLength();
				LogUtil.d(TAG, "文件流长度 : " + length);
				// LogUtil.d("FileMissingLog", "DownVDiskFileTask >> 文件流长度 : " +
				// length + ", mFileSize:" + mFileSize);
				if (length >= 0 && mFileSize <= 0) {
					mFileSize += length;
				}
				flag = writeData2File(stream);
			}
		} catch (MalformedURLException e) {
			LogUtil.d(TAG, e.toString());
		} catch (ProtocolException e) {
			LogUtil.d(TAG, e.toString());
		} catch (IOException e) {
			LogUtil.d(TAG, e.toString());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					LogUtil.d(TAG, e.toString());
				}
				if (mUrlConnection != null) {
					mUrlConnection.disconnect();
					mUrlConnection = null;
				}
			}
		}
		return flag;
	}

	protected synchronized boolean writeData2File(InputStream inputStream) {
		boolean flag = false;
		FileOutputStream fos = null;
		try {
			byte[] data = new byte[BYTE_SIZE];
			Arrays.fill(data, (byte) 0);
			int len = 0;
			boolean sdExist = true;
			//
			if (mTempFileSize > 0) {
				// LogUtil.e("FileMissingLog",
				// "DownVDiskFileTask >> writeData2File >> new FileOutputStream append is true");
				fos = new FileOutputStream(mTempFilePath, true);
			} else {
				// LogUtil.e("FileMissingLog",
				// "DownVDiskFileTask >> writeData2File >> new FileOutputStream append is false");
				fos = new FileOutputStream(mTempFilePath, false);
			}
			//

			while ((len = inputStream.read(data, 0, data.length)) != -1) {
				// LogUtil.e("FileMissingLog",
				// "DownVDiskFileTask >> writeData2File >> len = " + len);
				if (!isCancelled()) {
					if (!StorageUtil.isSDCardExist() && mTempFilePath.contains("sdcard")) {
						sdExist = false;
						break;
					} else {
						fos.write(data, 0, len);
						mTempFileSize += len;
						// LogUtil.e("FileMissingLog",
						// "DownVDiskFileTask >> writeData2File >> mFileSize = "
						// + mFileSize
						// + ", mTempFileSize = " + mTempFileSize);
						// 预防临时文件比总文件长度大时的问题
						if (mFileSize < mTempFileSize) {
							mFileSize = mTempFileSize;
						}
						publishProgress(mTempFileSize, mFileSize);
					}
				} else {
					break;
				}
			}

			if (sdExist) {
				flag = true;
			} else {
				flag = false;
				mResult.stateCode = ConstantData.CODE_FAIL_KEY;
			}

		} catch (FileNotFoundException e) {
			LogUtil.d(TAG, e.toString());
		} catch (IOException e) {
			LogUtil.d(TAG, e.toString());
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
					fos = null;
					// fos.close();
				} catch (IOException e) {
					LogUtil.d(TAG, e.toString());
				}
			}
		}
		return flag;
	}
}
