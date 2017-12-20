package com.sina.book.ui;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.exception.UEHandler;
import com.sina.book.image.ImageUtil;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.useraction.BasicFuncUtil;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.URLUtil;

public class BrowserSchemeInterceptActivity extends BaseActivity {

	String mBookId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d("BrowserSchemeInterceptActivity", "--------- >> onCreate >> ");
		CloudSyncUtil.getInstance().setIsQuitAppOrLogoutAcount(false);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int displayWidth = metrics.widthPixels;
		int displayHeight = metrics.heightPixels;
		ReadStyleManager.getInstance(displayWidth, displayHeight);
		DownBookManager.getInstance().init();
		// 客户端未活动状态，直接调起的BrowserSchemeInterceptActivity
		if (SinaBookApplication.getAllActivitySize() == 0) {
			// 另起线程进行一些初始化操作
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化行为统计
					UserActionManager.getInstance().init(BrowserSchemeInterceptActivity.this,
							ConstantData.USER_ACTION_URL, ConstantData.USER_ACTION_APP_KEY);

					// 包月信息初始化
					PaymentMonthMineUtil.getInstance().readPaymentMonthFromFile();
					if (HttpUtil.isConnected(BrowserSchemeInterceptActivity.this)) {
						PaymentMonthMineUtil.getInstance().reqPaymentMonth();
					}

					// 安装统计
					BasicFuncUtil.getInstance().recordInstall();

					// 记录崩溃异常
					UEHandler ueHandler = new UEHandler(getApplicationContext());
					Thread.setDefaultUncaughtExceptionHandler(ueHandler);

					// 调起本地图片缓存清理或无用文件的清理
					if (!ImageUtil.clearDiskCache(SinaBookApplication.gContext)) {
						DownBookManager.getInstance().clearUselessFile();
					}
				}
			}).start();
		}

		// 判断是不是第一次登录
		// 为了处理这种情况：用户卸载客户端时书架内已经添加了许多书籍，此后再次安装客户端并
		// 启动客户端时，书架内的书籍依然存在。
		// 为了解决这个问题，在首次启动客户端时进行一次判断，
		// 如果是第一次启动，做一次类似退出登录的操作。
		// 将书架内的书籍，绑定过UID并且非在线的书籍进行删除并缓存到BookCacheTable中
		// 另外增加一个条件：第一次启动时处于未登录状态
		boolean isFirstStartApp = StorageUtil.getBoolean("isFirstStartApp", true);
		boolean isNoLoginState = LoginUtil.isValidAccessToken(SinaBookApplication.gContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS;
		LogUtil.d("ReadInfoLeft", "BrowserSchemeInterceptActivity >> isFirstStartApp >> " + isFirstStartApp);
		LogUtil.d("ReadInfoLeft", "BrowserSchemeInterceptActivity >> isNoLoginState >> " + isNoLoginState);
		if (isFirstStartApp && isNoLoginState) {
			StorageUtil.saveBoolean("isFirstStartApp", false);
			CloudSyncUtil.getInstance().logout("BrowserSchemeInterceptActivity");
		}

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		LogUtil.d("BrowserSchemeInterceptActivity", "--------- >> onNewIntent >> ");
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		boolean result = parseSchemeData(intent);
		if (result) {
			Book book = new Book();
			book.setBookId(mBookId);
			BookDetailActivity.launch(BrowserSchemeInterceptActivity.this, book, "Browser赠书卡_01_01", "去客户端阅读");
		}
		finish();
	}

	private boolean parseSchemeData(Intent intent) {
		if (intent != null) {
			String data = intent.getDataString();
			LogUtil.d("BrowserSchemeInterceptActivity", "--------- >> parseSchemeData : " + data);

			if (!TextUtils.isEmpty(data)) {
				final HashMap<String, String> map = URLUtil.parseUrl(data);
				mBookId = map.get("b_id");
				return "weibo".equals(map.get("ftype"));
			}
		}
		return false;
	}
}
