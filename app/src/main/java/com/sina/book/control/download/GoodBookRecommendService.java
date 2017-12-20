package com.sina.book.control.download;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;

import com.sina.book.SinaBookApplication;
import com.sina.book.util.CalendarUtil;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

public class GoodBookRecommendService extends Service {
	private static final String TAG = "GoodBookRecommendService";

	private NetCheckReceiver mNetCheckReceiver = new NetCheckReceiver();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		fileLog(TAG, "onCreate()");
		if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
			fileLog(TAG, "registerReceiver");
			IntentFilter myIntentFilter = new IntentFilter();
			myIntentFilter.addAction(NetCheckReceiver.NET_CHANGE_ACTION);
			SinaBookApplication.gContext.registerReceiver(mNetCheckReceiver, myIntentFilter);
			return;
		}

		initService();
	}

	private void initService() {
		fileLog(TAG, "initService()");
		if (StorageUtil.getBoolean(StorageUtil.KEY_FIRST_CHANGE_ALARM, true)) {
			long nowTime = System.currentTimeMillis();
			long oldTime = StorageUtil.getLong(StorageUtil.KEY_GOOD_BOOK_PUSH_DATE, System.currentTimeMillis());
			long spaceTime = Math.abs(nowTime - oldTime);
			final long dayTime = 24 * 60 * 60 * 1000;
			final int dayCount = 7;
			if (spaceTime / dayTime > dayCount) {
				fileLog(TAG, "setup week Alarm");
				setupAlarm();
				StorageUtil.saveBoolean(StorageUtil.KEY_FIRST_CHANGE_ALARM, false);
				stopSelf();
				return;
			}
		}

		if (!isNeedReqData()) {
			fileLog(TAG, "not need ReqData()");
			stopSelf();
			return;
		}

		reqData();
	}

	public class NetCheckReceiver extends BroadcastReceiver {

		// android 中网络变化时所发的Intent的名字
		public static final String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (NET_CHANGE_ACTION.equals(intent.getAction())) {
				// Intent中ConnectivityManager.EXTRA_NO_CONNECTIVITY这个关键字表示着当前是否连接上了网络
				// true 代表网络断开 false 代表网络没有断开
				boolean isBreak = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
				if (!isBreak) {
					fileLog(TAG, "network is connected");
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							initService();
						}
					}, 5000);
					SinaBookApplication.gContext.unregisterReceiver(mNetCheckReceiver);
				}
			}
		}
	}

	private void reqData() {
		fileLog(TAG, "reqData()");
		StorageUtil.saveLong(StorageUtil.KEY_LAST_GOOD_BOOK_PUSH_DATE, System.currentTimeMillis());
	}

	private static void setupAlarm() {
		AlarmManager am = (AlarmManager) SinaBookApplication.gContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(SinaBookApplication.gContext, GoodBookRecommendService.class);
		PendingIntent pendingIntent = PendingIntent.getService(SinaBookApplication.gContext, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		long intervalWeek = 7 * AlarmManager.INTERVAL_DAY;
		long firstInterval = System.currentTimeMillis() + 6 * AlarmManager.INTERVAL_DAY;
		am.cancel(pendingIntent);
		am.setRepeating(AlarmManager.RTC_WAKEUP, firstInterval, intervalWeek, pendingIntent);
	}

	private boolean isNeedReqData() {
		long nowTime = System.currentTimeMillis();
		long oldTime = StorageUtil.getLong(StorageUtil.KEY_LAST_GOOD_BOOK_PUSH_DATE, 0);
		long spaceTime = Math.abs(nowTime - oldTime);
		final long dayTime = 18 * 60 * 60 * 1000;
		final int dayCount = 1;
		if (spaceTime / dayTime >= dayCount) {
			return true;
		}

		return false;
	}

	private void fileLog(String title, String content) {
		StringBuilder log = new StringBuilder();
		log.append("<--------------------------------------------");
		log.append("\n");
		log.append(title);
		String time = CalendarUtil.getCurrentTImeWithFormat("yyyy-MM-dd HH:mm:ss");
		log.append(time);
		log.append("\n");
		log.append(content);
		log.append("\n");
		log.append("-------------------------------------------->");
		log.append("\n");

		String logStr = log.toString();
		LogUtil.d(TAG, logStr);
		LogUtil.fileLogI(logStr);
	}
}