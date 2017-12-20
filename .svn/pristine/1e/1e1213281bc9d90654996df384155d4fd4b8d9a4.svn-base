package com.sina.book.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 书架同步
 * 
 */
public class SyncService extends Service {

	// boolean isCancelSync = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	// @Override
	// public void onStart(Intent intent, int startId) {
	// super.onStart(intent, startId);
	// // new SyncThread().start();
	// }

	@Override
	public void onDestroy() {
		super.onDestroy();
		// isCancelSync = true;
	}

	// class SyncThread extends Thread {
	// @Override
	// public void run() {
	// isCancelSync = false;
	// CloudSyncUtil.getInstance().syncFrom(SinaBookApplication.gContext, new
	// ITaskFinishListener() {
	//
	// @Override
	// public void onTaskFinished(TaskResult taskResult) {
	// if (!isCancelSync) {
	// if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) !=
	// LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
	// return;
	// }
	//
	// Intent intent = new Intent();
	// intent.setAction(CloudSyncUtil.ACTION_SHELF_SYNC_SUCCESS);
	// SinaBookApplication.gContext.sendBroadcast(intent);
	// }
	// }
	// });
	// }
	// }
}
