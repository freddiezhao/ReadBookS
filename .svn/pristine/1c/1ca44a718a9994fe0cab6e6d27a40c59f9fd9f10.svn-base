package com.sina.book.control.download;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpStatus;
import org.geometerplus.android.util.ZLog;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.TaskParams;
import com.sina.book.data.ConstantData;
import com.sina.book.util.FileUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.StreamHelper;

public class DownFileTask extends
		AbsDownloadAsyncTask<TaskParams, Object, DownResult>
{

//	private static final String			TAG				= "DownFileTask";

	protected static final int			RANGE			= 100 * 1024;
	protected static final int			BYTE_SIZE		= 10 * 1024;

	protected String					mUrl;
	protected float						mTempFileSize	= 0;
	protected float						mFileSize		= 0;
	protected String					mTempFilePath	= null;
	protected boolean					mIsWriting		= false;
	protected IDownTaskFinishListener	mTaskFinishListener;
	protected DownResult				mResult			= new DownResult(-1, this, null);

	/**
	 * 通过HttpURLConnection关闭io异常的方式强制中断下载请求<br>
	 */
	protected HttpURLConnection			mUrlConnection;

	/**
	 * 是否可取消任务
	 */
	protected boolean					cancelable		= true;

	public boolean isCancelable()
	{
		LogUtil.d("SinaXmlHandler", "DownFileTask >> isCancelable="
				+ cancelable);
		return cancelable;
	}

	public void setCancelable(boolean cancelable)
	{
		this.cancelable = cancelable;
	}

	public DownFileTask(String url, String filePath, float fileSize)
	{
		mUrl = url;
		mTempFilePath = filePath;
		mFileSize = fileSize;
		File file = new File(mTempFilePath);
		LogUtil.d("FileMissingLog", "DownFileTask >> 构造器 >> mTempFilePath : "
				+ mTempFilePath + ", mFileSize:" + mFileSize);
		if (file.exists()) {
			mTempFileSize = file.length();
			LogUtil.d("FileMissingLog",
					"DownFileTask >> 构造器 >> 文件存在，获取文件长度 mTempFileSize:"
							+ mTempFileSize);
		} else {
			// try {
			// TODO
			// file.createNewFile();
			// LogUtil.d("FileMissingLog", "DownFileTask >> 构造器 >> 文件不存在，创建文件");
			FileUtils.checkAndCreateFile(mTempFilePath);
			// } catch (IOException e) {
			// LogUtil.w(TAG, "DownFileTask() create file:" + mTempFilePath, e);
			// }
		}
	}

	/**
	 * 当使用这个构造方法时，需要手动调用setUrl()<br>
	 * 主要针对url构造很耗时的操作<br>
	 * 
	 * @param filePath
	 * @param fileSize
	 */
	public DownFileTask(String filePath, float fileSize)
	{
		mTempFilePath = filePath;
		mFileSize = fileSize;
		File file = new File(mTempFilePath);
		LogUtil.d("BugID=21413", "DownFileTask >> {filePath=" + filePath
				+ ", fileSize=" + fileSize + "}");
		if (file.exists()) {
			mTempFileSize = file.length();
			LogUtil.d("BugID=21413",
					"DownFileTask >> {file exists and mTempFileSize="
							+ mTempFileSize + "}");
		} else {
			LogUtil.d("BugID=21413",
					"DownFileTask >> {file not exists and checkAndCreateFile}");
			// try {
			// TODO
			// file.createNewFile();
			FileUtils.checkAndCreateFile(mTempFilePath);
			// } catch (IOException e) {
			// LogUtil.w(TAG, "DownFileTask() create file:" + mTempFilePath, e);
			// }
		}
	}

	public void setUrl(String url)
	{
		mUrl = url;
	}

	public String getUrl()
	{
		return mUrl;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		LogUtil.i("SinaXmlHandler", "DownFileTask >> cancel");
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mUrlConnection != null) {
					mUrlConnection.disconnect();
				}
			}
		}).start();
		return super.cancel(mayInterruptIfRunning);
	}

	public float getTempFileSize()
	{
		return mTempFileSize;
	}

	public float getFileSize()
	{
		return mFileSize;
	}

	public float getProgress()
	{
		if (mFileSize > 0) {
			return (mTempFileSize / mFileSize);
		}
		return 0;
	}

	@Override
	protected DownResult doInBackground(TaskParams... params)
	{
		if (!isCancelled()) {
//			LogUtil.d("FileMissingLog", "Before download temp file size："
//					+ mTempFileSize + " -- " + mFileSize + ", mTempFilePath="
//					+ mTempFilePath);
			boolean flag = downloadData();
			if (!flag) {
				return null;
			}
//			LogUtil.d("FileMissingLog", "After download temp tile size："
//					+ mTempFileSize + " -- " + mFileSize + ", mTempFilePath="
//					+ mTempFilePath);
		}
		return mResult;
	}

	@Override
	protected void onPostExecute(DownResult result)
	{
		super.onPostExecute(result);
		if (mTaskFinishListener != null) {
			mTaskFinishListener.onTaskFinished(result);
		}
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		if (mTaskFinishListener != null) {
			mTaskFinishListener.onTaskFinished(null);
		}
	}

	public IDownTaskFinishListener getTaskFinishListener()
	{
		return mTaskFinishListener;
	}

	public void setTaskFinishListener(IDownTaskFinishListener taskFinishListener)
	{
		this.mTaskFinishListener = taskFinishListener;
	}
	
	protected boolean downloadData()
	{
		return downloadData(false);
	}
	
	protected boolean downloadData(boolean disableAttr) {
		boolean flag = false;
		InputStream stream = null;
		try {
			mUrlConnection = initHttpConnection();

			int code = mUrlConnection.getResponseCode();
			if (code == HttpStatus.SC_OK
					|| code == HttpStatus.SC_PARTIAL_CONTENT) {

				stream = mUrlConnection.getInputStream();
				int length = mUrlConnection.getContentLength();
//				ZLog.d(ZLog.FileMissingLog, "文件流长度 : " + length);
				if (length >= 0) {
					mFileSize += length;
				}

				String contentEnc = mUrlConnection.getHeaderField("Content-Encoding");

				//
				if (contentEnc != null) {
					boolean isDeflate = contentEnc.toLowerCase().indexOf("deflate") >= 0;
					boolean isGzip = contentEnc.toLowerCase().indexOf("gzip") >= 0;

					if (isGzip || isDeflate) {
						byte[] streamContent = StreamHelper.readAll(stream);

						try {
							stream.close();
						} catch (Exception e) {
						}

						ByteArrayInputStream bais = new ByteArrayInputStream(streamContent);
						stream = new GZIPInputStream(bais);
					}

					// Log.d("processNetworkRequest a conn.openInputStream 1");
				}

				flag = writeData2File(stream);
				if (!flag) {
					mFileSize -= length;
				}
			}
		}
		// catch (MalformedURLException e) {
		// ZLog.d(ZLog.FileMissingLog, e.toString());
		// } catch (ProtocolException e) {
		// ZLog.d(ZLog.FileMissingLog, e.toString());
		// }
		catch (Exception e) {
			ZLog.e(ZLog.FileMissingLog, e.toString());
			flag = false;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					ZLog.e(ZLog.FileMissingLog, "downloadData() close stream, e {" + e + "}");
				}

				if (mUrlConnection != null) {
					mUrlConnection.disconnect();
					mUrlConnection = null;
				}
			}
		}

		return flag;
	}

	protected HttpURLConnection initHttpConnection() throws IOException
	{
		return initHttpConnection(false);
	}

	protected HttpURLConnection initHttpConnection(boolean disableAttr) throws IOException
	{
//		LogUtil.i("RequestTask-DownFileTask", mUrl);
		URL url = new URL(mUrl);
		HttpURLConnection con = HttpUtil.getHttpUrlConnection(url, SinaBookApplication.gContext, disableAttr);

		con.connect();
		return con;
	}

	protected boolean writeData2File(InputStream inputStream)
	{
		boolean flag = false;
		boolean errorFlag = true;
		FileOutputStream fos = null;
		try {
			byte[] data = new byte[BYTE_SIZE];
			Arrays.fill(data, (byte) 0);
			int len = 0;
			boolean sdExist = true;
			boolean isEncodingFixed = false;
			String encoding = "GBK";

			while ((len = inputStream.read(data, 0, data.length)) != -1) {
				if (!isCancelled()) {
					if (errorFlag) {
						String msg = new String(data, encoding);

						if (!isEncodingFixed) {
							final String encodingKey = "encoding=\"";
							int encodingStartPos = msg.indexOf(encodingKey);

							if (encodingStartPos >= 0) {
								int encodingEndPos = msg.indexOf("\"",
										encodingStartPos + encodingKey.length()
												+ 1);

								if (encodingEndPos >= 0) {
									isEncodingFixed = true;

									String newEncoding = msg.substring(
											encodingStartPos
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
								JSONObject childObj = obj
										.optJSONObject("status");
								if (childObj != null) {
									mResult.stateCode = Integer
											.parseInt(childObj.optString(
													"code", "0"));
									mResult.retObj = childObj.optString("msg",
											"成功");

									if (mResult.stateCode != ConstantData.CODE_SUCCESS_KEY) {
										return flag;
									} else {
										errorFlag = false;
									}
								} else {
									errorFlag = false;
								}
							} catch (JSONException e) {
								errorFlag = false;
								// TODO:不是json对象，有可能是访问chinaunicom网络(未输入账号密码状态)
								// return flag;
							}
						} else {
							errorFlag = false;
						}
					}
					if (!StorageUtil.isSDCardExist()
							&& mTempFilePath.contains("sdcard")) {
						sdExist = false;
						break;
					} else {
						File file = new File(mTempFilePath);
						if (file == null || !file.exists()) {
							FileUtils.checkAndCreateFile(mTempFilePath);
						}

						if (mTempFileSize > 0) {
							fos = new FileOutputStream(mTempFilePath, true);
						} else {
							fos = new FileOutputStream(mTempFilePath, false);
						}
						fos.write(data, 0, len);
						fos.flush();
						fos.close();
						fos = null;
						mTempFileSize += len;
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
			ZLog.w(ZLog.FileMissingLog, "writeData2File() filenotfound, e {" + e + "}");
		} catch (IOException e) {
			ZLog.w(ZLog.FileMissingLog, "writeData2File() io, e {" + e + "}");
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					ZLog.w(ZLog.FileMissingLog, "writeData2File() fos.close, e {" + e + "}");
				}
			}
		}
		return flag;
	}
}
