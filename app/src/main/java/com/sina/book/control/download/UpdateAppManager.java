package com.sina.book.control.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.UpdateVersionInfo;
import com.sina.book.parser.UpdateInfoParser;
import com.sina.book.ui.TransitActivity;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.util.CalendarUtil;
import com.sina.book.util.FileUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

/**
 * 更新App管理器
 * 
 * @author MarkMjw
 */
public class UpdateAppManager implements ITaskFinishListener, DialogInterface.OnCancelListener, Callback {
	private static final String TAG = "UpdateAppManager";

	public static final String TEMP_SUFFIX = ".tmp";
	public static final String APK_SUFFIX = ".apk";

	public static final int KEY_CHECK = 1;
	public static final int KEY_DOWN = 2;

	private static final int DOWNLOAD_SUCC = 1;
	private static final int DOWNLOAD_FAILED = 0;
	private static final int SERVER_TIMEOUT = 2;
	private static final int VERSION_COMPARE = 3;
	private static final int UPDATE_PROGRESS = 4;

	private static UpdateAppManager sInstance;
	private static Context sContext;
	private CustomProDialog mProgressDialog;

	private UpdateVersionInfo mUpdateInfo;
	private UpdateInfoParser mUpdateInfoParser;

	private File mTempFile;
	private String mApkTmpPath;
	private String mApkPath;

	private int mCurType = 0;

	private RequestTask mCheckTask;
	private GenericTask mDownTask;
	private HttpClient mHttpClient;

	private boolean isCancelDown = false;

	private boolean isBackgroundCheck = false;

	private Handler mHandler;
	private double mLastProgress = 0.00;

	public static boolean isNewAppUpdateDialogShown = false;

	private UpdateAppManager() {
		mHandler = new Handler(this);
	}

	public static UpdateAppManager getInstance(Context context) {
		sContext = context;
		if (sInstance == null) {
			synchronized (UpdateAppManager.class) {
				if (sInstance == null) {
					sInstance = new UpdateAppManager();
				}
			}
		}
		return sInstance;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// 更新进度条
		if (UPDATE_PROGRESS == msg.what) {
			if (null != mProgressDialog) {
				double progress = (Double) msg.obj;

				if (Math.abs(progress - mLastProgress) >= 0.05) {
					mLastProgress = progress;

					String str = new DecimalFormat("00.00%").format(progress);

					String text = String.format(sContext.getString(R.string.download_version), str);
					mProgressDialog.setMessage(text);
				}
			}
			return true;
		}

		dismissProgressDialog();

		switch (msg.what) {
		case DOWNLOAD_SUCC:
			if (mTempFile != null) {
				// 安装apk
				installApk();
				
				// 退出当前客户端
				SinaBookApplication.quit();
				break;
			}

		case DOWNLOAD_FAILED:{
			// 下载apk失败
			if(mUpdateInfo != null && mUpdateInfo.isForce()){
				showUpdataDialog();
			}
			if (!isCancelDown) {
				showToast(R.string.version_error);
			}
			break;
		}

		case SERVER_TIMEOUT:{
			// 下载超时
			if(mUpdateInfo != null && mUpdateInfo.isForce()){
				showUpdataDialog();
			}
			showToast(R.string.outtime_error);
			break;
		}

		case VERSION_COMPARE:
			if (!mUpdateInfo.isUpdate()) {
				// 版本号相同无需升级
				showToast(R.string.dialog_current_veriosn);
				if(sContext instanceof TransitActivity){
					TransitActivity activity = (TransitActivity)sContext;
					activity.mHandler.sendEmptyMessage(TransitActivity.MSG_INIT);
				}
			} else {
				// 版本号不同 ,提示用户升级
				showUpdataDialog();
			}
			break;
		}
		return true;
	}

	/**
	 * 更新启动时间，假如上一次启动时间不是今天，<br>
	 * 则检查更新，否则不检查，更新启动时间.
	 */
	public void autoCheckVersion() {
		if (!HttpUtil.isConnected(sContext)) {
			return;
		}
		
		String nowDate = CalendarUtil.getCurrentTImeWithFormat("yyyy-MM-dd");
		String oldDate = StorageUtil.getString(StorageUtil.KEY_CHECK_APP_VERSION);
		
		checkVersion(true);
		// 更新启动时间
		StorageUtil.saveString(StorageUtil.KEY_CHECK_APP_VERSION, nowDate);

//		
//		// 当日期发生变化时才进行检查更新
//		if (!nowDate.equals(oldDate)) {
//			checkVersion(true);
//			// 更新启动时间
//			StorageUtil.saveString(StorageUtil.KEY_CHECK_APP_VERSION, nowDate);
//		}else{
//			if(sContext instanceof TransitActivity){
//				// 不需要更新时 & 由微博呼起， 继续执行呼起后续事情
//				TransitActivity activity = (TransitActivity)sContext;
//				activity.mHandler.sendEmptyMessage(TransitActivity.MSG_INIT);
//			}
//		}
	}

	/**
	 * 检查新版本
	 * 
	 * @param isBack
	 *            是否后台检查
	 */
	public void checkVersion(boolean isBack) {
		this.isBackgroundCheck = isBack;
		this.mCurType = KEY_CHECK;

		if (!isBackgroundCheck) {
			showProgressDialog();
		}

		clearTempFile();

		PackageManager pm = sContext.getPackageManager();

		if (pm != null) {
			String version = ConstantData.VERSION;

			try {
				version = pm.getPackageInfo(sContext.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				LogUtil.e(TAG, "Get version : " + e.getMessage());
			}

			// 这里需要将版本名称转换成浮点数然后给服务器
			int dotIndex = version.indexOf(".");
			if (dotIndex > 0 && dotIndex < version.length() - 1) {
				String versionBegin = version.substring(0, dotIndex + 1);
				String versionEnd = version.substring(dotIndex + 1);

				versionEnd = versionEnd.replaceAll("\\.", "");
				version = versionBegin + versionEnd;
			}

			String url = String.format(ConstantData.URL_UPDATE_VERSION, version);

			mUpdateInfoParser = new UpdateInfoParser();
			mCheckTask = new RequestTask(mUpdateInfoParser);

			TaskParams params = new TaskParams();
			params.put(RequestTask.PARAM_URL, url);
			params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
			mCheckTask.setTaskFinishListener(this);
			mCheckTask.execute(params);
		}
	}

	/**
	 * 清理上次下载的临时文件
	 */
	private void clearTempFile() {
		String path = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_APK);
		FileUtils.deleteFile(path);
		// File dir = new File(path);
		// if (dir.exists() && dir.isDirectory()) {
		// File[] files = dir.listFiles();
		// if (null != files) {
		// for (File f : files) {
		// if (f.exists()) {
		// f.delete();
		// }
		// }
		// }
		// }
	}

	private void showUpdataDialog() {
		isNewAppUpdateDialogShown = true;
		
		String title = sContext.getString(R.string.dialog_new_version);
		final  boolean isForce = mUpdateInfo.isForce();
		
		String left = "";
		if(isForce){
			left = sContext.getString(R.string.exit_btn);
		}else{
			left = sContext.getString(R.string.next_btn);
		}
		String right = sContext.getString(R.string.update_btn);
		
		CommonDialog.show(sContext, title, mUpdateInfo.getIntro(), left, right, new CommonDialog.DefaultListener() {

			public void onLeftClick(DialogInterface dialog)
			{
				if(isForce){
					// 退出
					SinaBookApplication.quit();
				}else{
					if(sContext instanceof TransitActivity){
						TransitActivity activity = (TransitActivity)sContext;
						activity.mHandler.sendEmptyMessage(TransitActivity.MSG_INIT);
					}else{
						super.onLeftClick(dialog);
					}
				}
			}

			@Override
			public void onRightClick(DialogInterface dialog) {
				downloadApk();
			}
		}, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				isNewAppUpdateDialogShown = false;
				if(sContext instanceof TransitActivity && !isForce){
					TransitActivity activity = (TransitActivity)sContext;
					activity.mHandler.sendEmptyMessage(TransitActivity.MSG_INIT);
				}
			}
		}, !isForce, !isForce);
	}

	private void downloadApk() {
		// 这里需要将是否是后台检查设置为false否则没有下载进度条和后续Toast提示
		this.isBackgroundCheck = false;
		this.mCurType = KEY_DOWN;

		showProgressDialog();
		downloadFile();
	}

	/**
	 * 请求网络下载Apk
	 */
	private void downloadFile() {
		mDownTask = new GenericTask() {

			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				try {
					mHttpClient = new DefaultHttpClient();
					HttpGet get = new HttpGet(mUpdateInfo.getUrl());
					HttpResponse response = mHttpClient.execute(get);

					int code = response.getStatusLine().getStatusCode();
					if (code == HttpStatus.SC_OK || code == HttpStatus.SC_PARTIAL_CONTENT) {

						HttpEntity entity = response.getEntity();

						long totalSize = entity.getContentLength();

						InputStream is = entity.getContent();
						FileOutputStream fileOutputStream = null;

						if (is != null) {
							mTempFile = new File(mApkTmpPath);
							fileOutputStream = new FileOutputStream(mTempFile);

							byte[] b = new byte[1024];
							int len = -1;
							int total = 0;

							while ((len = is.read(b)) != -1) {
								fileOutputStream.write(b, 0, len);
								total += len;
								double progress = total * 1.00 / totalSize;

								Message msg = new Message();
								msg.what = UPDATE_PROGRESS;
								msg.obj = progress;
								mHandler.sendMessage(msg);
							}
						}

						if (fileOutputStream != null) {
							fileOutputStream.flush();
							fileOutputStream.close();
						}

						if (is != null) {
							is.close();
						}

						mHandler.sendEmptyMessage(DOWNLOAD_SUCC);

					} else if (code == HttpStatus.SC_REQUEST_TIMEOUT) {
						mHandler.sendEmptyMessage(SERVER_TIMEOUT);
					}

				} catch (Exception e) {
					LogUtil.e(TAG, e.getMessage());
					mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
				}

				return null;
			}
		};

		mDownTask.execute();
	}

	/**
	 * Intent安装APK
	 */
	private void installApk() {
		File file = new File(mApkPath + APK_SUFFIX);
		mTempFile.renameTo(file);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		sContext.startActivity(intent);
	}

	private void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProDialog(sContext);
			
			if(mUpdateInfo != null && mUpdateInfo.isForce()){
				mProgressDialog.setCancelable(false);
				mProgressDialog.setCanceledOnTouchOutside(false);
			}else{
				mProgressDialog.setCancelable(true);
				mProgressDialog.setOnCancelListener(this);
				mProgressDialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						mProgressDialog = null;
					}
				});
			}
		}

		switch (mCurType) {
		case KEY_CHECK:
			mProgressDialog.show(R.string.dialog_checking);
			break;

		case KEY_DOWN:
			mProgressDialog.show(String.format(sContext.getString(R.string.download_version), "00.00%"));
			break;

		default:
			break;
		}
	}

	private void dismissProgressDialog() {
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
		}
	}

	/**
	 * Toast提示
	 * 
	 * @param resId
	 */
	private void showToast(int resId) {
		if (!isBackgroundCheck) {
			Toast.makeText(sContext, resId, Toast.LENGTH_SHORT).show();
		}
	}

	private boolean checkUpdateInfo() {
		boolean isCheckSuccess = false;
		if (mUpdateInfo != null) {
			String name;
			String url = mUpdateInfo.getUrl();
			if (url.contains("/")) {
				name = url.substring(url.lastIndexOf("/"));
			} else {
				name = url;
			}

			mApkPath = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_APK) + name;
			mApkTmpPath = mApkPath + TEMP_SUFFIX;
			isCheckSuccess = true;
		}
		return isCheckSuccess;
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			if ("0".equals(mUpdateInfoParser.getCode())) {
				if (taskResult.retObj instanceof UpdateVersionInfo) {
					mUpdateInfo = (UpdateVersionInfo) taskResult.retObj;
					if (checkUpdateInfo()) {
						mHandler.sendEmptyMessage(VERSION_COMPARE);
						return;
					}
				}
			}
			mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
		} else {
			showToast(R.string.network_error);
		}

		dismissProgressDialog();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		switch (mCurType) {
		case KEY_CHECK:
			if (null != mCheckTask) {
				mCheckTask.abort();
			}
			break;

		case KEY_DOWN:
			if (null != mHttpClient) {
				mHttpClient.getConnectionManager().shutdown();
				isCancelDown = true;
			}

			if (null != mDownTask) {
				mDownTask.cancel(true);
			}

			if (null != mTempFile && mTempFile.exists()) {
				mTempFile.delete();
			}
			break;

		default:
			break;
		}
	}
}
