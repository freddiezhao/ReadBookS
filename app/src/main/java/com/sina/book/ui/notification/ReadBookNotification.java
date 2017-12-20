package com.sina.book.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.ui.ReadActivity;
import com.sina.book.util.StorageUtil;

/**
 * 阅读时后台运行时通知
 */
public class ReadBookNotification {
	public static final String TAG = "ReadBookNotification";

	private static ReadBookNotification mInstance = new ReadBookNotification();

	private Context mContext;
	private NotificationManager mNotificationManager;

	private NotificationCompat.Builder mBuilder;

	public static ReadBookNotification getInstance() {
		return mInstance;
	}

	private ReadBookNotification() {
		mContext = SinaBookApplication.gContext;

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		mBuilder = new NotificationCompat.Builder(mContext);

		String ticker = mContext.getString(R.string.notification_go_read_book);
		mBuilder.setTicker(ticker);
		mBuilder.setContentInfo(mContext.getString(R.string.sina_reader));
		mBuilder.setAutoCancel(true);
		mBuilder.setSmallIcon(R.drawable.notification_icon);
	}

	private String getString(int resId, Object... args) {
		return String.format(mContext.getString(resId), args);
	}

	/**
	 * 显示最近阅读的书的通知栏
	 * 
	 * @param book
	 * @param chapter
	 */
	public void showNotification(Book book, Chapter chapter) {
		if (book == null) {
			return;
		}

		// 关闭通知时
		if (!StorageUtil.getBoolean(StorageUtil.KEY_OPEN_NOTIFICATION, true)) {
			return;
		}
		// 点击通知栏，进入阅读界面
		Intent intent = new Intent(mContext, ReadActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		Bundle bundle = new Bundle();
		bundle.putBoolean(TAG, true);
		bundle.putSerializable("book", book);
		intent.putExtras(bundle);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentTitle(book.getTitle());
		mBuilder.setContentIntent(pendingIntent);

		if (null != chapter) {
			String content = getString(R.string.notification_reading_chapter, chapter.getTitle());
			mBuilder.setContentText(content);
		} else {
			mBuilder.setContentText(mContext.getString(R.string.notification_go_read_book));
		}

		Notification notification = mBuilder.build();

		mNotificationManager.notify(NotificationConfig.READBOOK_NOTIFY_TAG, NotificationConfig.READBOOK_NOTIFY_ID,
				notification);
	}

	/**
	 * 清除所有最近阅读的书的通知
	 */
	public void clearNotification() {
		mNotificationManager.cancel(NotificationConfig.READBOOK_NOTIFY_TAG, NotificationConfig.READBOOK_NOTIFY_ID);
	}
}
