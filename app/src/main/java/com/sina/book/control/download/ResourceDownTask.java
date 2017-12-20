package com.sina.book.control.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

import com.sina.book.DesUtils;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.TaskParams;
import com.sina.book.data.ConstantData;
import com.sina.book.util.FileUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.StorageUtil;

/**
 * image、css资源下载
 */
public class ResourceDownTask extends DownFileTask
{
	public static final int		MAX_LENGTH			= 10 * 1024;
	
	public ResourceDownJob			mJob;
	
	private String mFileName;
	
	public ResourceDownTask(ResourceDownJob job)
	{
		super("",0);
		mJob = job;
		mUrl = job.getUrl();

		//TODO:ouyang
		if(job.mType == ResourceDownJob.TYPE_IMAGE){
			mFileName = DesUtils.encryptString(mUrl);
//			mFileName = Util.md5(mUrl) + ".jpg";
			mTempFilePath = mJob.manager.mBookDir + "/OEBPS/images/" + mFileName;
		}else if(job.mType == ResourceDownJob.TYPE_CSS){
			mFileName = DesUtils.encryptString(mUrl);
//			mFileName = Util.md5(mUrl) + ".css";
			mTempFilePath = mJob.manager.mBookDir + "/OEBPS/styles/" + mFileName;
		}
	}

	public ResourceDownJob getJob()
	{
		return mJob;
	}

	public void setJob(ResourceDownJob job)
	{
		this.mJob = job;
	}

	public boolean cancel(boolean mayInterruptIfRunning)
	{
		return super.cancel(mayInterruptIfRunning);
	}

	// 回调展示进度
	protected void onProgressUpdate(Object... values)
	{
		//		DownBookManager.getInstance().onTaskUpdate(mJob.getBook(), false, true, DownBookJob.STATE_RUNNING);
		super.onProgressUpdate(values);
	}

	// 下载任务
	protected DownResult doInBackground(TaskParams... params)
	{
		cancelable = true;
		if (!isCancelled()) {
			// 1、检查文件是否存在
			File file = FileUtils.checkOrCreateFile(mTempFilePath, false);
			if (file == null || !file.exists()) {
				// 开始下载
				boolean flag = downloadData(true);
				if (!flag) {
//					Log.e("ouyang", "ResourceDownTask---doInBackground--资源下载失败");
					
					mResult.stateCode = -1;
					mResult.retObj = "下载失败";
					return mResult;
				}else{
					if(!isCancelled()){
//						Log.d("ouyang", "ResourceDownTask---doInBackground--资源下载成功");
						mResult.stateCode = 0;
						mResult.retObj = "成功";
					}else{
//						Log.i("ouyang", "ResourceDownTask---doInBackground--资源下载取消");
						return null;
					}
				}
				
//				DownResult result = super.doInBackground(params);
//				if (result == null) {
//					// 下载失败
//				}else{
//					mResult.stateCode = 0;
//					mResult.retObj = "成功";
//				}
			}else{
//				Log.d("ouyang", "ResourceDownTask---doInBackground--资源本地存在，成功");
				mResult.stateCode = 0;
				mResult.retObj = "成功";
			}
		}
		return mResult;
	}
	
	protected HttpURLConnection initHttpConnection(boolean disableAttr) throws IOException
	{
		URL url = new URL(mUrl);
		HttpURLConnection con = HttpUtil.getHttpUrlConnection(url, SinaBookApplication.gContext, disableAttr);
		con.setRequestProperty("Accept-Encoding", "gzip,deflate");
		con.connect();
		return con;
	}
	
//	protected synchronized boolean downloadData()
//	{
//		boolean flag = false;
//		InputStream stream = null;
//		try {
//			mUrlConnection = initHttpConnection(true);
//			int code = mUrlConnection.getResponseCode();
//			if (code == HttpStatus.SC_OK || code == HttpStatus.SC_PARTIAL_CONTENT) {
//				stream = mUrlConnection.getInputStream();
//				flag = writeData2File(stream);
//				if(!flag){
//					// 下载或存储失败,删除掉可能下载一半的文件
//					FileUtils.deleteFile(mTempFilePath);
//				}
//			}
//		} catch (MalformedURLException e) {
//			LogUtil.e(TAG, e.toString());
//		} catch (ProtocolException e) {
//			LogUtil.e(TAG, e.toString());
//		} catch (IOException e) {
//			LogUtil.e(TAG, e.toString());
//		} finally {
//			if (stream != null) {
//				try {
//					stream.close();
//				} catch (IOException e) {
//					LogUtil.d(TAG, e.toString());
//				}
//				if (mUrlConnection != null) {
//					mUrlConnection.disconnect();
//					mUrlConnection = null;
//				}
//			}
//		}
//		return flag;
//	}

	// 存储文件
	protected boolean writeData2File(InputStream inputStream)
	{
		boolean flag = false;
		FileOutputStream fos = null;
		try {
			byte[] data = new byte[BYTE_SIZE];

			int len = 0;
			boolean sdExist = true;
			
			File file = new File(mTempFilePath);
			if (file == null || !file.exists()) {
				FileUtils.checkAndCreateFile(mTempFilePath);
			}
			fos = new FileOutputStream(mTempFilePath);
			
			while ((len = inputStream.read(data, 0, data.length)) != -1) {
				if (!isCancelled()) {
					if (!StorageUtil.isSDCardExist()) {
						// SD卡不存在
						sdExist = false;
						break;
					} else {
						fos.write(data, 0, len);
						mTempFileSize += len;
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
		} catch (IOException e) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		return flag;
	}

}
