package com.sina.book.ui.notification;

import java.util.HashMap;

import org.geometerplus.android.util.ZLog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.data.Book;
import com.sina.book.db.DBService;
import com.sina.book.ui.MainActivity;
import com.sina.book.util.StorageUtil;

/**
 * 下载书籍通知栏.
 */
public class DownloadBookNotification {
	private static final String TAG = "DownloadBookNotification";

	private static DownloadBookNotification mInstance = new DownloadBookNotification();

	private HashMap<String, NotificationCompat.Builder> mBuilderMap = new HashMap<String, NotificationCompat.Builder>();

	private Context mContext;
	private NotificationManager mNotificationManager;

	private PendingIntent mPendingIntent;

	public static DownloadBookNotification getInstance() {
		return mInstance;
	}

	private DownloadBookNotification() {
		mContext = SinaBookApplication.gContext;

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

		// 点击通知栏，进入书架界面
		Intent intent = new Intent(mContext, MainActivity.class);
		mPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private String getString(int resId, Object... args) {
		return String.format(mContext.getString(resId), args);
	}

	/**
	 * 下载书籍时，调用此函数，显示通知栏
	 * 
	 * @param book
	 *            the book
	 */
	public void addNotification(Book book) {
		// 关闭通知时
		if (!StorageUtil.getBoolean(StorageUtil.KEY_OPEN_NOTIFICATION, true)) {
			return;
		}

		long progress = Math.round(100 * book.getDownloadInfo().getProgress());

		String ticker = getString(R.string.notification_info_start_download, book.getTitle());
		String title = getString(R.string.notification_book_title, book.getTitle());
		String info = getString(R.string.notification_downloading, progress);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.notification_icon);
		builder.setTicker(ticker);
		builder.setContentTitle(title);
		builder.setContentText(info);
		builder.setContentInfo(mContext.getString(R.string.sina_reader));
		builder.setContentIntent(mPendingIntent);
		Notification notification = builder.build();

		mBuilderMap.put(book.getUniqueIdentify(), builder);

		if (book.getId() > 0) {
			mNotificationManager.notify(book.getUniqueIdentify(), book.getId(), notification);
		} else {
			Book tbook = DBService.getBook(book);
			if (tbook != null && tbook.getId() > 0) {
				book.setId(tbook.getId());
				mNotificationManager.notify(book.getUniqueIdentify(), book.getId(), notification);
			}
		}
	}

	/**
	 * 下载书籍时，调用此函数，更新通知栏
	 * 
	 * @param book
	 *            the book
	 */
	public void updateNotification(Book book) {
		ZLog.d(ZLog.DownloadBookNotification, "updateNotification >> ");
		// 关闭通知时
		if (!StorageUtil.getBoolean(StorageUtil.KEY_OPEN_NOTIFICATION, true)) {
			ZLog.e(ZLog.DownloadBookNotification, "updateNotification >> return 1");
			return;
		}

		if (mBuilderMap.containsKey(book.getUniqueIdentify())) {
			NotificationCompat.Builder builder = mBuilderMap.get(book.getUniqueIdentify());

			long progress = Math.round(100 * book.getDownloadInfo().getProgress());
			String ticker = "";
			String info = "";

			if (book.getDownloadInfo().getDownLoadState() != DownBookJob.STATE_FAILED) {

				if (book.getDownloadInfo().isParsering()) {
					// 解析中
					info = getString(R.string.notification_parsering, progress);

				} else {
					if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) > 0.0001
							|| book.getDownloadInfo().getDownLoadState() != DownBookJob.STATE_FINISHED) {
						// 正在下载
						info = getString(R.string.notification_downloading, progress);

					} else {
						// 下载完成
						ticker = getString(R.string.notification_info_finish_download, book.getTitle());
						info = getString(R.string.notification_download_finish, progress);
					}
				}
			} else {
				// 下载失败
				ticker = getString(R.string.notification_info_interrupt_download, book.getTitle());
				info = getString(R.string.notification_download_failed, progress);
			}

			if (!TextUtils.isEmpty(ticker)) {
				builder.setTicker(ticker);
			}

			if (!TextUtils.isEmpty(info)) {
				builder.setContentText(info);
			}

			Notification notification = builder.build();

			if (book.getId() > 0) {
				mNotificationManager.notify(book.getUniqueIdentify(), book.getId(), notification);
				ZLog.d(ZLog.DownloadBookNotification, "updateNotification >> 1");
			} else {
				Book tbook = DBService.getBook(book);
				if (tbook != null && tbook.getId() > 0) {
					book.setId(tbook.getId());
					mNotificationManager.notify(book.getUniqueIdentify(), book.getId(), notification);
					ZLog.d(ZLog.DownloadBookNotification, "updateNotification >> 2");
				} else {
					ZLog.e(ZLog.DownloadBookNotification, "updateNotification >> return 2");
				}
			}
			// mNotificationManager.notify(book.getUniqueIdentify(),
			// book.getId(), notification);
		} else {
			ZLog.e(ZLog.DownloadBookNotification, "updateNotification >> return 3");
		}
	}

	/**
	 * 清除所有的通知信息
	 */
	public void clearAllNotification() {
		mNotificationManager.cancelAll();
		mBuilderMap.clear();
	}
}
