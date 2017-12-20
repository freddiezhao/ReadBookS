package com.sina.book.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.ui.MainActivity;
import com.sina.book.util.StorageUtil;

/**
 * 退出程序的通知
 */
public class RestartNotification {
	private static RestartNotification mInstance = new RestartNotification();

	private Context mContext;
	private NotificationManager mNotificationManager;

	private Notification mNotification;
	private PendingIntent mPendingIntent;

	public static RestartNotification getInstance() {
		return mInstance;
	}

	private RestartNotification() {
		mContext = SinaBookApplication.gContext;

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// 点击通知栏，进入阅读界面
		Intent intent = new Intent(mContext, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		mPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		String ticker = mContext.getString(R.string.notification_go_read_book);
		String title = mContext.getString(R.string.sina_reader);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setTicker(ticker);
		builder.setContentTitle(title);
		builder.setContentInfo(title);
		builder.setContentText(ticker);
		builder.setContentIntent(mPendingIntent);
		mNotification = builder.build();
	}

	/**
	 * 显示通知栏
	 */
	public void showNotification() {
		// 关闭通知时
		if (StorageUtil.getBoolean(StorageUtil.KEY_OPEN_NOTIFICATION, true)) {
			mNotificationManager.notify(NotificationConfig.RESTART_NOTIFY_TAG, NotificationConfig.RESTART_NOTIFY_ID,
					mNotification);
		}
	}

	/**
	 * 清除所有通知
	 */
	public void clearNotification() {
		mNotificationManager.cancel(NotificationConfig.RESTART_NOTIFY_TAG, NotificationConfig.RESTART_NOTIFY_ID);
	}
}
