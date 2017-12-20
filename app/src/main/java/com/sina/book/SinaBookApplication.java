package com.sina.book;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.geometerplus.android.fbreader.config.ConfigShadow;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.HtmlDownBookManager;
import com.sina.book.control.download.HtmlDownManager;
import com.sina.book.data.Book;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.InstalledDeviceCountUtil;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.db.DBService;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.notification.DownloadBookNotification;
import com.sina.book.ui.notification.ReadBookNotification;
import com.sina.book.ui.notification.RestartNotification;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

//import com.sina.book.control.download.GoodBookRecommendService;

public class SinaBookApplication extends Application {
//	private static final String TAG = "SinaBookApplication";

	/**
	 * Application的context
	 */
	public static Context gContext;
	
	/**
	 * 监听网络变化
	 */
	private NetCheckReceiver mNetCheckReceiver = new NetCheckReceiver();

	// 用来统一管理所有Activity的堆栈
	private static Stack<Activity> sActivitysStack = new Stack<Activity>();

	public static void push(Activity activity) {
		if (sActivitysStack == null) {
			sActivitysStack = new Stack<Activity>();
		}
		sActivitysStack.push(activity);
	}

	public static void remove(Activity activity) {
		if (sActivitysStack != null) {
			sActivitysStack.remove(activity);
		}
	}

	public static Activity getTopActivity() {
		if (sActivitysStack != null) {
			if (!sActivitysStack.empty()) {
				return sActivitysStack.peek();
			}
		}
		return null;
	}

	private static void finishAllActivitys() {
		if (sActivitysStack != null) {
			int size = sActivitysStack.size();
			for (int i = 0; i < size; i++) {
				Activity activity = sActivitysStack.get(i);
				activity.finish();
				LogUtil.i("sActivitysStack", "activity:{" + activity + "}");
			}
			sActivitysStack.removeAllElements();
		}
	}

	public static int getAllActivitySize() {
		if (sActivitysStack == null) {
			sActivitysStack = new Stack<Activity>();
		}
		return sActivitysStack.size();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CloudSyncUtil.getInstance().setIsQuitAppOrLogoutAcount(false);
//		LogUtil.i(TAG, "Application onCreate");
		gContext = this;

		init();

		final ConfigShadow config = new ConfigShadow(this);
		new ZLAndroidImageManager();
		new ZLAndroidLibrary(this);
		
		config.runOnConnect(new Runnable() {
			public void run() {
				if ("".equals(Paths.TempDirectoryOption.getValue())) {
					String dir = null;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
						final File d = getExternalCacheDir();
						if (d != null) {
							d.mkdirs();
							if (d.exists() && d.isDirectory()) {
								dir = d.getPath();
							}
						}
					}
					if (dir == null) {
						dir = Paths.mainBookDirectory() + "/epreader";
					}
					Paths.TempDirectoryOption.setValue(dir);
				}
			}
		});

		// 打印手机信息
		// Log.i(TAG, ">>>>>>>>> 设备信息 >>>>>>>>>>>>\n" +
		// PhoneInfo.getPhoneInfo(gContext).toString()
		// + "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

	/**
	 * 是否Debug
	 * 
	 * @return
	 */
	public static boolean isDebug() {
		// return BuildConfig.DEBUG;
		return true;
	}

	private void init() {
		// 如果进程名包含:remote，则忽略
		String processName = getCurProcessName(this);
		if (processName.contains(":remote")) {
			return;
		}

		// 初始化数据库，需要在Application里面初始化，否则push来后数据库已经被释放导致所有操作失败
		DBService.init(this);

		// SQLiteDatabase db = DBService.sDbOpenHelper.getWritableDatabase();
		// DBService.sDbOpenHelper.addNewAsset(db);

		// 初始化InstalledDeviceCountUtil
		InstalledDeviceCountUtil.getInstance(this);

		if (!HttpUtil.isConnected(this)) {
			// 监听网络变化
			IntentFilter myIntentFilter = new IntentFilter();
			myIntentFilter.addAction(NetCheckReceiver.NET_CHANGE_ACTION);
			registerReceiver(mNetCheckReceiver, myIntentFilter);
		}

	}

	/**
	 * 程序退出时统一调用该方法<br>
	 */
	public static void quit() {
		// 程序退出时，清掉所有的通知信息
		DownloadBookNotification.getInstance().clearAllNotification();
		ReadBookNotification.getInstance().clearNotification();
		RestartNotification.getInstance().clearNotification();
		CloudSyncUtil.getInstance().setIsQuitAppOrLogoutAcount(true);

		// StorageUtil.saveString("etag", "");
		CloudSyncUtil.getInstance().setEtag("");
		CloudSyncUtil.getInstance().setFirstLoginAndSendOneSyncCompleteBroadcast(true);
		CloudSyncUtil.getInstance().clearH5GetCardBookIdLists();
		ReadStyleManager.clear();

		new Thread() {
			public void run() {
				// 包月信息保存
				PaymentMonthMineUtil.getInstance().savePaymentMonthToFile();

				// 设置好书推荐提醒的闹钟
				// setupAlarm();
				if (StorageUtil.getBoolean(
						StorageUtil.KEY_GOOD_BOOK_FIRST_DATE, true)) {
					// 保存首次退出客户端的时间
					StorageUtil.saveLong(StorageUtil.KEY_GOOD_BOOK_PUSH_DATE,
							System.currentTimeMillis());
					StorageUtil.saveBoolean(
							StorageUtil.KEY_GOOD_BOOK_FIRST_DATE, false);
				}

				DownBookManager.getInstance().stopAllJob();
				HtmlDownManager.getInstance().stopAllJob();
				DBService.close();
				clearHtmlCache();

				finishAllActivitys();

				// 完全退出程序时，如果有统计数据则先提交
				// UserActionManager.getInstance().onStopOnKillProcess();

				// 针对4.3以上系统做特殊处理
				// 1.关闭常驻服务(通知服务等)
				// 2.直接杀死进程
				// int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				// LogUtil.d("Exit", "currentapiVersion=" + currentapiVersion);
				// if (currentapiVersion >= 18) {
				// 停止服务
				// Intent pushService = new Intent(SinaBookApplication.gContext,
				// PushService.class);
				// SinaBookApplication.gContext.stopService(pushService);
				// Intent sinaPushService = new
				// Intent(SinaBookApplication.gContext, SinaPushService.class);
				// SinaBookApplication.gContext.stopService(sinaPushService);

				// Intent udidService = new
				// Intent(SinaBookApplication.gContext,
				// OpenUDIDService.class);
				// SinaBookApplication.gContext.stopService(udidService);
				// Intent bookRecomService = new
				// Intent(SinaBookApplication.gContext,
				// GoodBookRecommendService.class);
				// SinaBookApplication.gContext.stopService(bookRecomService);

				// Intent syncService = new Intent(SinaBookApplication.gContext,
				// SyncService.class);
				// SinaBookApplication.gContext.stopService(syncService);
				// CloudSyncUtil.getInstance().stopSyncService();

				// 杀死进程
				// android.os.Process.killProcess(android.os.Process.myPid());
				// System.exit(0);
				// }
			}
		}.start();
	}
	
	// 退出时，清除未加入书架书籍的下载文件缓存
	public static void clearHtmlCache(){
		ArrayList<HtmlDownBookManager> list = HtmlDownManager.getInstance().getAllJobs();
		if(list != null && list.size() > 0){
			for(int i=0; i < list.size(); ++i){
				HtmlDownBookManager manager = list.get(i);
				if(manager != null){
					Book book = manager.getBook();
					if(book != null){
						DownBookJob job = DownBookManager.getInstance().getJob(book);
						if(job == null){
							// 不在书架中, 删除缓存
							boolean hasDBCache = false;
							ArrayList<Book> cacheList = DBService.getAllBookCache();
							for(int a = 0; a < cacheList.size(); ++a){
								Book tmpBook = cacheList.get(a);
								if(tmpBook.equals(book)){
									hasDBCache = true;
									break;
								}
							}
							
							if(!hasDBCache){
								manager.deleteBookFileCache();
							}
							
//							String path = book.getDownloadInfo().getOriginalFilePath();
//							FileUtils.deleteFile(path);
						}
					}
				}
			}
		}
	}

	public class NetCheckReceiver extends BroadcastReceiver {

		// android 中网络变化时所发的Intent的名字
		public static final String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (NET_CHANGE_ACTION.equals(intent.getAction())) {
				// Intent中ConnectivityManager.EXTRA_NO_CONNECTIVITY这个关键字表示着当前是否连接上了网络
				// true 代表网络断开 false 代表网络没有断开
				boolean isBreak = intent.getBooleanExtra(
						ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
				if (!isBreak) {
					PaymentMonthMineUtil.getInstance().reqPaymentMonth();
					unregisterReceiver(this);
				}
			}
		}
	}

	/**
	 * 获取当前进程的名称
	 * 
	 * @param context
	 * @return
	 */
	private String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> processInfoList = am
				.getRunningAppProcesses();

		if (null != processInfoList && !processInfoList.isEmpty()) {
			for (ActivityManager.RunningAppProcessInfo appProcess : processInfoList) {
				if (appProcess.pid == pid) {
					return appProcess.processName;
				}
			}
		}

		return "";
	}

	/**
	 * 设置好书推荐提醒的闹钟
	 */
//	private static void setupAlarm() {
//		// LogUtil.fileLogI(TAG + " setupAlarm()");
//		// long nowTime = System.currentTimeMillis();
//		// long oldTime =
//		// StorageUtil.getLong(StorageUtil.KEY_GOOD_BOOK_PUSH_DATE,
//		// System.currentTimeMillis());
//		// long spaceTime = Math.abs(nowTime - oldTime);
//		// final long dayTime = 24 * 60 * 60 * 1000;
//		// final int dayCount = 7;
//		//
//		// AlarmManager am = (AlarmManager)
//		// gContext.getSystemService(Context.ALARM_SERVICE);
//		// Intent intent = new Intent(gContext, GoodBookRecommendService.class);
//		// PendingIntent pendingIntent = PendingIntent.getService(gContext, 0,
//		// intent, PendingIntent.FLAG_CANCEL_CURRENT);
//		// long intervalDay = AlarmManager.INTERVAL_DAY;
//		// long intervalWeek = 7 * AlarmManager.INTERVAL_DAY;
//		// long firstInterval = System.currentTimeMillis() +
//		// AlarmManager.INTERVAL_DAY;
//		// long firstIntervalW = System.currentTimeMillis() + 7 *
//		// AlarmManager.INTERVAL_DAY;
//		// am.cancel(pendingIntent);
//		// if (spaceTime / dayTime <= dayCount) {
//		// LogUtil.fileLogI(TAG + " setup day alarm");
//		// am.setRepeating(AlarmManager.RTC_WAKEUP, firstInterval, intervalDay,
//		// pendingIntent);
//		// } else {
//		// LogUtil.fileLogI(TAG + " setup week alarm");
//		// am.setRepeating(AlarmManager.RTC_WAKEUP, firstIntervalW,
//		// intervalWeek, pendingIntent);
//		// StorageUtil.saveBoolean(StorageUtil.KEY_FIRST_CHANGE_ALARM, false);
//		// }
//	}
	
	// 临时的静态变量
	public static int myCount = 0;
	public static boolean myBool_RewriteOldVersion = false;
	
}
