package com.sina.book.control.download;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;

import com.sina.book.data.ConstantData;
import com.sina.book.util.AsyncTaskUtils;
import com.sina.book.util.AsyncTaskUtils.AsyncTaskListener;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.vdisk.android.VDiskAuthSession;
import com.vdisk.android.VDiskDialogListener;
import com.vdisk.net.VDiskAPI;
import com.vdisk.net.VDiskAPI.Entry;
import com.vdisk.net.VDiskAPI.VDiskFileInfo;
import com.vdisk.net.exception.VDiskDialogError;
import com.vdisk.net.exception.VDiskException;
import com.vdisk.net.session.AccessToken;
import com.vdisk.net.session.AppKeyPair;
import com.vdisk.net.session.Session.AccessType;
import com.vdisk.net.session.WeiboAccessToken;

/**
 * The Class VDiskSyncManager.
 */
public class VDiskSyncManager implements AsyncTaskListener {
	private static final String TAG = "VDiskSyncManager";
	// 异步任务标志
	/** The Constant FLAG_TASK_GET_FILE_LIST. */
	public static final int FLAG_TASK_GET_FILE_LIST = 0;

	/** The Constant FLAG_TASK_DOWNLOAD_BOOK. */
	public static final int FLAG_TASK_DOWNLOAD_BOOK = 1;

	/** The m async task down book. */
	private AsyncTaskUtils mAsyncTaskDownBook;

	/** The m async task get file list. */
	private AsyncTaskUtils mAsyncTaskGetFileList;

	/** 上下文. */
	private Context mContext;

	/** V盘操作的对象定义. */
	private VDiskAPI<VDiskAuthSession> mVDiskApi;

	/** The instance. */
	private static VDiskSyncManager instance;

	/** 当前路径. */
	private String mDirPath = "/";

	/** 当前文件. */
	private String mFilePath = "";

	/** 缓存获取到的微盘文件对象 TODO 可以修改为软引用. */
	private static HashMap<String, Entry> mEntryMap = new HashMap<String, Entry>();

	/** 缓存获取到的微盘文件信息对象 TODO 可以修改为软引用. */
	private static HashMap<String, VDiskFileInfo> mFileInfoMap = new HashMap<String, VDiskFileInfo>();

	/** The m lisener. */
	private IGetEntryLisener mGetEntryLisener;

	/** The m get file info lisener. */
	private IGetFileInfoLisener mGetFileInfoLisener;

	/**
	 * Instantiates a new v disk sync manager.
	 * 
	 * @param context
	 *            the context
	 */
	private VDiskSyncManager(Context context) {
		mContext = context;
		AppKeyPair appKeyPair = new AppKeyPair(ConstantData.APP_KEY_VDISK, ConstantData.APP_SECRET_VDISK);

		final VDiskAuthSession vDiskAuthSession = VDiskAuthSession.getInstance(context, appKeyPair, AccessType.VDISK);

		mVDiskApi = new VDiskAPI<VDiskAuthSession>(vDiskAuthSession);

		// 使用微博Token认证
		WeiboAccessToken weiboToken = new WeiboAccessToken();

		if (LoginUtil.isValidAccessToken(context) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			// 1.通过微博token登录和认证
			String token = LoginUtil.getLoginInfo().getAccessToken();
			weiboToken.mAccessToken = token;

			// weiboToken.mAccessToken = "2.000BtNI3MHPhmCda52ca59ba0UQTm2";
			// 开启使用微博Token的开关,如果要使用微博Token的话，必须执行此方法
			vDiskAuthSession.enabledAndSetWeiboAccessToken(weiboToken);

			// 2.通过回调地址认证和登录
			// vDiskAuthSession.setRedirectUrl(ConstantData.APP_REDIRECT_URL_VDISK);
			// 验证权限
			vDiskAuthSession.authorize(context, new VDiskDialogListener() {

				@Override
				public void onVDiskException(VDiskException e) {
					LogUtil.d(TAG, "onVDiskException: " + e.getMessage());
				}

				@Override
				public void onError(VDiskDialogError e) {
					LogUtil.d(TAG, "onError: " + e.getMessage());
				}

				@Override
				public void onComplete(Bundle values) {
					LogUtil.d(TAG, "onComplete: values=" + values);
					if (values != null) {
						AccessToken mToken = (AccessToken) values.getSerializable(VDiskAuthSession.OAUTH2_TOKEN);
						vDiskAuthSession.finishAuthorize(mToken);
					}
				}

				@Override
				public void onCancel() {
					LogUtil.d(TAG, "onCancel: ");
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sina.book.util.AsyncTaskUtils.AsyncTaskListener#doInBackground(com
	 * .sina.book.util.AsyncTaskUtils, int)
	 */
	@Override
	public int doInBackground(AsyncTaskUtils task, int flag) {
		switch (flag) {
		case FLAG_TASK_GET_FILE_LIST:
			LogUtil.d(TAG, "获取微盘文件列表开始.");
			try {
				Entry metaData = mVDiskApi.metadata(mDirPath, null, true, false);
				mEntryMap.put(mDirPath, metaData);
			} catch (VDiskException e) {
				LogUtil.e(TAG, e.getMessage());
			}
			break;
		case FLAG_TASK_DOWNLOAD_BOOK:
			LogUtil.d(TAG, "获取微盘文件信息开始.");
			try {
				VDiskFileInfo fileInfo = mVDiskApi.getFileLink(mFilePath, null);
				mFileInfoMap.put(mFilePath, fileInfo);
			} catch (VDiskException e) {
				e.printStackTrace();
			}
			break;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sina.book.util.AsyncTaskUtils.AsyncTaskListener#onPostExecute(com
	 * .sina.book.util.AsyncTaskUtils, int, int)
	 */
	@Override
	public void onPostExecute(AsyncTaskUtils task, int result, int flag) {
		switch (flag) {
		case FLAG_TASK_GET_FILE_LIST:
			LogUtil.d(TAG, "获取微盘文件列表：：获取微盘文件列表完成");
			if (mGetEntryLisener != null) {
				mGetEntryLisener.fireEntryFinished(mEntryMap.get(mDirPath));
			}
			break;
		case FLAG_TASK_DOWNLOAD_BOOK:
			LogUtil.d(TAG, "获取微盘文件信息：：获取微盘文件信息完成");
			if (mGetFileInfoLisener != null) {
				mGetFileInfoLisener.fireFileInfoFinished(mFileInfoMap.get(mFilePath));
			}
			break;
		}
	}

	/**
	 * Gets the single instance of VDiskSyncManager.
	 * 
	 * @param context
	 *            the context
	 * @return single instance of VDiskSyncManager
	 */
	public static VDiskSyncManager getInstance(Context context) {
		if (instance == null) {
			instance = new VDiskSyncManager(context);
		}
		return instance;
	}

	/**
	 * Gets the file list.
	 * 
	 * @param path
	 *            the path
	 * @param lisener
	 *            the lisener
	 * @return the file list
	 */
	public void getFileList(String path, IGetEntryLisener lisener) {
		mGetEntryLisener = lisener;
		mDirPath = path;
		if (mEntryMap.containsKey(path)) {
			if (mGetEntryLisener != null) {
				mGetEntryLisener.fireEntryFinished(mEntryMap.get(path));
			}
		} else {
			// 如果任务不为空，则首先取消掉它
			if (null != mAsyncTaskGetFileList) {
				mAsyncTaskGetFileList.setCancel(true);
				mAsyncTaskGetFileList.cancel(true);
			}

			mAsyncTaskGetFileList = AsyncTaskUtils.create(mContext, this, false);
			mAsyncTaskGetFileList.execute(FLAG_TASK_GET_FILE_LIST);
		}
	}

	/**
	 * Gets the file info.
	 * 
	 * @param path
	 *            the path
	 * @param lisener
	 *            the lisener
	 * @return the file info
	 */
	public void getFileInfo(String path, IGetFileInfoLisener lisener) {
		mGetFileInfoLisener = lisener;
		mFilePath = path;
		if (mFileInfoMap.containsKey(path)) {
			if (mGetFileInfoLisener != null) {
				mGetFileInfoLisener.fireFileInfoFinished(mFileInfoMap.get(path));
			}
		} else {
			mAsyncTaskDownBook = AsyncTaskUtils.create(mContext, this, false);
			mAsyncTaskDownBook.execute(FLAG_TASK_DOWNLOAD_BOOK);
		}
	}

	public String getShareUrl(String path) throws VDiskException {
		if (LoginUtil.isValidAccessToken(mContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			return mVDiskApi.share(path);
		} else {
			return "";
		}
	}

	/**
	 * 获取entry完成的回调接口.
	 * 
	 * @author like
	 */
	public interface IGetEntryLisener {

		/**
		 * Fire entry finished.
		 * 
		 * @param entry
		 *            the entry
		 */
		public void fireEntryFinished(Entry entry);
	}

	/**
	 * 获取文件信息完成的回调接口.
	 */
	public interface IGetFileInfoLisener {

		/**
		 * Fire file info finished.
		 * 
		 * @param info
		 *            the info
		 */
		public void fireFileInfoFinished(VDiskFileInfo info);
	}

	/**
	 * 回收，将单例对象设置为null.
	 */
	public static void recycle() {
		// 取消异步任务:获取文件列表
		try {
			instance.mAsyncTaskGetFileList.setCancel(true);
			instance.mAsyncTaskGetFileList.cancel(true);
		} catch (Throwable e) {
			// ignore...
		}
		// 取消异步任务:下载书籍
		try {
			instance.mAsyncTaskDownBook.setCancel(true);
			instance.mAsyncTaskDownBook.cancel(true);
		} catch (Throwable e) {
			// ignore...
		}
		mEntryMap.clear();
		mFileInfoMap.clear();
		instance = null;
	}

	/**
	 * 取消获取文件列表异步任务
	 */
	public static void cancelGetFileListTask() {
		// 取消异步任务:获取文件列表
		try {
			if (null != instance.mAsyncTaskGetFileList) {
				instance.mAsyncTaskGetFileList.setCancel(true);
				instance.mAsyncTaskGetFileList.cancel(true);
			}
		} catch (Throwable e) {
			// ignore...
		}
	}

	/**
	 * 取消下载文件异步任务
	 */
	public static void cancelDownFileTask() {
		// 取消异步任务:下载书籍
		try {
			if (null != instance.mAsyncTaskDownBook) {
				instance.mAsyncTaskDownBook.setCancel(true);
				instance.mAsyncTaskDownBook.cancel(true);
			}
		} catch (Throwable e) {
			// ignore...
		}
	}

	/**
	 * 更新微盘数据
	 */
	public void updateVDisk(IUpdateVDiskListener listener) {

		recycle();

		if (listener != null) {

			listener.updateVDiskData();
		}

	}

	/**
	 * 完成微盘数据更新
	 */
	public void updateVDiskFinished(IUpdateVDiskFinishedListener listener) {

		if (listener != null) {
			listener.updateVDiskFinished();
		}
	}

	/**
	 * 微盘数据更新的监听
	 */
	public interface IUpdateVDiskListener {

		/**
		 * 更新微盘数据回调
		 */
		public void updateVDiskData();

	}

	/**
	 * 完成微盘数据更新的监听
	 */
	public interface IUpdateVDiskFinishedListener {

		/**
		 * 完成微盘数据更新的回调
		 */
		public void updateVDiskFinished();

	}
}
