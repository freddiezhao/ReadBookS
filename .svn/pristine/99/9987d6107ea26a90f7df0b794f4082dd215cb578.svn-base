package com.sina.book.control.download;

import java.io.File;
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
import org.geometerplus.android.util.ZLog;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.TaskParams;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.ui.notification.DownloadBookNotification;
import com.sina.book.util.GlobalToast;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

/**
 * 目前来说，这个DownEpubFileTask和DownEpubFileTask是完全一样的代码
 * 
 * @author chenjl
 * 
 */
public class DownEpubFileTask extends DownFileTask {
	private static final String TAG = "DownEpubFileTask";

	private static final int MAX_REFRESH_TIME = 1000;
	private DownBookJob mJob;
	private long mLastPos = 0;

	public DownEpubFileTask(DownBookJob job) {
		super(job.getBook().getDownloadInfo().getVDiskDownUrl(), job.getBook()
				.getDownloadInfo().getFilePath(), job.getBook()
				.getDownloadInfo().getFileSize());
		ZLog.d(ZLog.FileMissingLog, "DownEpubFileTask >> 构造器 >> mFileSize : "
				+ mFileSize + ", mTempFileSize:" + mTempFileSize);
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
			DownloadBookNotification.getInstance().updateNotification(
					mJob.getBook());
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
			DownBookManager.getInstance().onTaskUpdate(mJob.getBook(), false,
					true, DownBookJob.STATE_RUNNING);
			mLastPos = timePos;
		}
		super.onProgressUpdate(values);
	}

	@Override
	protected DownResult doInBackground(TaskParams... params) {
		ZLog.d(ZLog.FileMissingLog, "doInBackground");
		if (!isCancelled()) {
			Book book = mJob.getBook();
			
//			mTempFileSize = Math.abs((float) (mFileSize * book
//					.getDownloadInfo().getProgress()));
//			ZLog.d(ZLog.FileMissingLog, "DownEpubFileTask >> mFileSize : "
//					+ mFileSize + ", mTempFileSize:" + mTempFileSize
//					+ ", book.getDownloadInfo().getProgress()="
//					+ book.getDownloadInfo().getProgress());

			// 下载是否已经成功
			int downloadState = ConstantData.CODE_FAIL_KEY;

			if (mTempFileSize == mFileSize && mFileSize != 0) {
				downloadState = ConstantData.CODE_SUCCESS_KEY;
			} else {
				// mUrl = book.getDownloadInfo().getVDiskDownUrl();
				// computeURL();
				mUrl = book.getEpubDownUrl();
				LogUtil.d("FileMissingLog",
						"DownEpubFileTask >> Epub文件下载 >> mUrl=" + mUrl);
				super.doInBackground(params);
				if (mTempFileSize < mFileSize || mFileSize == 0) {
					downloadState = ConstantData.CODE_FAIL_KEY;
					LogUtil.d("FileMissingLog",
							"DownEpubFileTask >> Epub文件下载 >> 失败1");
				} else {
					downloadState = ConstantData.CODE_SUCCESS_KEY;
					LogUtil.d("FileMissingLog",
							"DownEpubFileTask >> Epub文件下载 >> 成功");
				}
			}

			if (!isCancelled() && !mIsWriting
					&& downloadState != ConstantData.CODE_FAIL_KEY) {
				mIsWriting = true;
				book.getDownloadInfo().setParsering(mIsWriting);
				publishProgress(mTempFileSize, mFileSize);

				boolean flag = false;
				// 更改文件名
				File file = new File(book.getDownloadInfo().getFilePath());
				LogUtil.d(
						"FileMissingLog",
						"DownEpubFileTask >> Epub文件下载 >> 更名前 file="
								+ file.getAbsolutePath());
				if (file.exists() && file.isFile()) {
					try {
						// 更改文件名为.epub
						file.renameTo(new File(book.changeFileSuffix()));
						LogUtil.d("FileMissingLog",
								"DownEpubFileTask >> Epub文件下载 >> 更名后 file="
										+ file.getAbsolutePath());
						flag = true;
					} catch (Exception e) {
						flag = false;
					}
				}

				if (flag) {
					mResult.stateCode = ConstantData.CODE_SUCCESS_KEY;
				}

				// 不解析
				// if (VDiskFileHandler.convertTmp2Dat(book, this)) {
				// // 唯一下载解析成功出去的标识
				// mResult.stateCode = ConstantData.CODE_SUCCESS_KEY;
				// // LogUtil.d("FileMissingLog",
				// // "DownVDiskFileTask >> 微盘文件下载 >> 成功1");
				// } else {
				// // LogUtil.d("FileMissingLog",
				// // "DownVDiskFileTask >> 微盘文件下载 >> 失败2");
				// }

				mIsWriting = false;
				book.getDownloadInfo().setParsering(mIsWriting);
				publishProgress(mTempFileSize, mFileSize);
			}
		}
		return mResult;
	}

	private void computeURL() {
		// 需要将epub://裁剪掉
		if (mUrl != null && mUrl.startsWith(Book.EPUB_PATH_PROTOCOL)) {
			mUrl = mUrl.substring(Book.EPUB_PATH_PROTOCOL.length());
		}
	}

	protected HttpURLConnection initHttpConnection() throws IOException,
			MalformedURLException, ProtocolException {
		ZLog.d(ZLog.FileMissingLog, "url : " + mUrl);
		URL url = new URL(mUrl);
		HttpURLConnection con = HttpUtil.getHttpUrlConnection(url,
				SinaBookApplication.gContext, false);
		// Range头域 用来实现断点续传
		// 表示头500个字节：Range: bytes=0-499
		// 表示第二个500字节：Range: bytes=500-999
		// 表示最后500个字节：Range: bytes=-500
		// 表示500字节以后的范围：Range: bytes=500-
		// 第一个和最后一个字节：Range: bytes=0-0,-1
		// 同时指定几个范围：Range: bytes=500-600,601-999
		con.setRequestProperty("RANGE", "bytes=" + Math.round(mTempFileSize)
				+ "-");
		LogUtil.d("FileMissingLog",
				"RANGE " + "bytes=" + Math.round(mTempFileSize) + "-");
		con.connect();
		return con;
	}

	protected synchronized boolean downloadData() {
		LogUtil.d("FileMissingLog",
				"DownEpubFileTask >> downloadData >> 开始下载微盘文件");
		boolean flag = false;
		InputStream stream = null;
		try {
			mUrlConnection = initHttpConnection();
			int code = mUrlConnection.getResponseCode();
			if (code == HttpStatus.SC_OK
					|| code == HttpStatus.SC_PARTIAL_CONTENT) {
				stream = mUrlConnection.getInputStream();
				int length = mUrlConnection.getContentLength();
				ZLog.d(ZLog.FileMissingLog, "文件流长度 : " + length);
				ZLog.d(ZLog.FileMissingLog, "DownEpubFileTask >> 文件流长度 : "
						+ length + ", mFileSize:" + mFileSize);
				if (length >= 0 && mFileSize <= 0) {
					mFileSize += length;
				}
				flag = writeData2File(stream);
			}
		} catch (MalformedURLException e) {
			ZLog.d(ZLog.FileMissingLog, e.toString());
		} catch (ProtocolException e) {
			ZLog.d(ZLog.FileMissingLog, e.toString());
		} catch (IOException e) {
			ZLog.d(ZLog.FileMissingLog, e.toString());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					ZLog.d(ZLog.FileMissingLog, e.toString());
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
		boolean errorFlag = true;
		FileOutputStream fos = null;
		boolean isEncodingFixed = false;
		String encoding = "GBK";
		try {
			byte[] data = new byte[BYTE_SIZE];
			Arrays.fill(data, (byte) 0);
			int len = 0;
			boolean sdExist = true;
			//
			if (mTempFileSize > 0) {
				LogUtil.e("FileMissingLog",
						"FileMissingLog >> writeData2File >> new FileOutputStream append is true（追加）");
				fos = new FileOutputStream(mTempFilePath, true);
			} else {
				LogUtil.e("FileMissingLog",
						"FileMissingLog >> writeData2File >> new FileOutputStream append is false（覆盖）");
				fos = new FileOutputStream(mTempFilePath, false);
			}
			//

			while ((len = inputStream.read(data, 0, data.length)) != -1) {
				LogUtil.e("FileMissingLog",
						"DownEpubFileTask >> writeData2File >> len = " + len);

				// 返回的数据可能会是JSON格式，包含有错误信息
				// 如：{"status":{"code":15,"msg":"\u8bbf\u95ee\u672a\u6388\u6743"}}
				// 错误码：
				// 37 未购买
				// 10 书籍信息有误
				// 5 未登录
				if (errorFlag) {
					String msg = new String(data, encoding);

					if (!isEncodingFixed) {
						final String encodingKey = "encoding=\"";
						int encodingStartPos = msg.indexOf(encodingKey);

						if (encodingStartPos >= 0) {
							int encodingEndPos = msg
									.indexOf("\"", encodingStartPos
											+ encodingKey.length() + 1);

							if (encodingEndPos >= 0) {
								isEncodingFixed = true;

								String newEncoding = msg
										.substring(encodingStartPos
												+ encodingKey.length(),
												encodingEndPos);

								if (!encoding.equals(newEncoding)) {
									encoding = newEncoding;
									msg = new String(data, encoding);
								}
							}
						}
					}

					int idx = msg.lastIndexOf("}");
					if (idx >= 0) {
						msg = msg.substring(0, idx + 1);
						try {
							JSONObject obj = new JSONObject(msg);
							JSONObject childObj = obj.optJSONObject("status");
							if (childObj != null) {
								mResult.stateCode = Integer.parseInt(childObj
										.optString("code", "0"));
								mResult.retObj = childObj
										.optString("msg", "成功");

								if (mResult.stateCode != ConstantData.CODE_SUCCESS_KEY) {
									// TODO CJL 这里要不要弹出Toast提示
									String toast = null;
									switch (mResult.stateCode) {
									case 37:
										toast = "对不起，您尚未购买本书籍";
										break;
									case 5:
										toast = "对不起，您尚未登录，请先登录";
										break;
									// case 10:
									default:
										toast = "书籍信息有误";
										break;
									}
									GlobalToast.i.toast(toast);
									return flag;
								} else {
									errorFlag = false;
								}
							} else {
								errorFlag = false;
							}
						} catch (JSONException e) {
							errorFlag = false;
							// 不是json对象，有可能是访问chinaunicom网络(未输入账号密码状态)
							// return flag;
						}
					} else {
						errorFlag = false;
					}
				}

				if (!isCancelled()) {
					if (!StorageUtil.isSDCardExist()
							&& mTempFilePath.contains("sdcard")) {
						sdExist = false;
						break;
					} else {
						fos.write(data, 0, len);
						mTempFileSize += len;
						LogUtil.e("FileMissingLog",
								"DownEpubFileTask >> writeData2File >> mFileSize = "
										+ mFileSize + ", mTempFileSize = "
										+ mTempFileSize);
						// 预防临时文件比总文件长度大时的问题
						if (mFileSize < mTempFileSize) {
							mFileSize = mTempFileSize;
						}
						publishProgress(mTempFileSize, mFileSize);
						
						// DEBUG CJL 暂时休眠，网速太快了，无法调试啊。呜呜
//						try {
//							Thread.sleep(500);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
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
			ZLog.d(ZLog.FileMissingLog, e.toString());
		} catch (IOException e) {
			ZLog.d(ZLog.FileMissingLog, e.toString());
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
					fos = null;
					// fos.close();
				} catch (IOException e) {
					ZLog.d(ZLog.FileMissingLog, e.toString());
				}
			}
		}
		return flag;
	}
}
