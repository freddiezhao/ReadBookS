package com.sina.book.ui.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.util.StorageUtil;

/**
 * 更新完成的通知
 * 
 * @author MarkMjw
 * @date 2013-2-26
 */
public class UpdateFinishedNotification {
	private static UpdateFinishedNotification mInstance = new UpdateFinishedNotification();

	private Context mContext;
	private NotificationManager mNotificationManager;

	private PendingIntent mPendingIntent;

	public static UpdateFinishedNotification getInstance() {
		return mInstance;
	}

	private UpdateFinishedNotification() {

		mContext = SinaBookApplication.gContext;

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// 指定内容intent
		Intent intent = new Intent();
		mPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private String getString(int resId, Object... args) {
		return String.format(mContext.getString(resId), args);
	}

	/**
	 * 显示通知栏
	 */
	public void showNotification(int updateBooks) {
		// 关闭通知时
		if (!StorageUtil.getBoolean(StorageUtil.KEY_OPEN_NOTIFICATION, true)) {
			return;
		}

		String ticker = getString(R.string.notification_update_finish, updateBooks);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setTicker(ticker);
		builder.setContentText(ticker);
		builder.setContentTitle(mContext.getString(R.string.notification_update_title));
		builder.setContentInfo(mContext.getString(R.string.sina_reader));
		builder.setContentIntent(mPendingIntent);

		mNotificationManager.notify(NotificationConfig.UPDATE_NOTIFY_TAG, NotificationConfig.UPDATE_NOTIFY_ID,
				builder.build());
	}

	/**
	 * 清除所有通知
	 */
	public void clearNotification() {
		mNotificationManager.cancel(NotificationConfig.UPDATE_NOTIFY_TAG, NotificationConfig.UPDATE_NOTIFY_ID);
	}
}
