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
import com.sina.book.util.LogUtil;

/**
 * 更新完成的通知
 * 
 * @author MarkMjw
 * @date 2013-2-26
 */
public class PushUpdatedNotification {
	private static final String TAG = "PushUpdatedNotification";
	private static PushUpdatedNotification mInstance = new PushUpdatedNotification();

	private Context mContext;
	private NotificationManager mNotificationManager;

	public static PushUpdatedNotification getInstance() {
		return mInstance;
	}

	private PushUpdatedNotification() {
		mContext = SinaBookApplication.gContext;

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private String getString(int resId, Object... args) {
		return String.format(mContext.getString(resId), args);
	}

	/**
	 * 增加通知栏提示
	 */
	public void addNotification(Book book, Chapter chapter) {
		// 点击通知栏，进入阅读界面
		Intent intent = new Intent(mContext, ReadActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		Bundle bundle = new Bundle();
		bundle.putSerializable("book", book);
		intent.putExtras(bundle);
		// 这里第二个参数必须传不同的值，否则intent会覆盖之前的
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, book.getId(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		String title = getString(R.string.notification_update_push_title, book.getTitle());

		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setTicker(title);
		builder.setContentText(chapter.getTitle());
		builder.setContentTitle(title);
		builder.setContentInfo(mContext.getString(R.string.sina_reader));
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();

		mNotificationManager.notify(book.getUniqueIdentify(), book.getId(), notification);

		LogUtil.d(TAG, "Show notification : " + book.getTitle());
	}

	/**
	 * 清除通知
	 */
	public void clearNotification(Book book) {
		mNotificationManager.cancel(book.getUniqueIdentify(), book.getId());
	}

	/**
	 * 清除所有通知
	 */
	public void clearAllNotification() {
		mNotificationManager.cancelAll();
	}
}
