package com.sina.book.ui;

import static com.sina.book.control.download.DownBookJob.STATE_FAILED;
import static com.sina.book.control.download.DownBookJob.STATE_FINISHED;
import static com.sina.book.control.download.DownBookJob.STATE_PAUSED;
import static com.sina.book.control.download.DownBookJob.STATE_PREPARING;
import static com.sina.book.control.download.DownBookJob.STATE_RECHARGE;
import static com.sina.book.control.download.DownBookJob.STATE_RUNNING;
import static com.sina.book.control.download.DownBookJob.STATE_UNKNOWN;
import static com.sina.book.control.download.DownBookJob.STATE_WAITING;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.geometerplus.android.fbreader.FBReader;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.UpdateAppManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookDetailData;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.data.EpubCheckInfo;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.db.DBService;
import com.sina.book.exception.UEHandler;
import com.sina.book.image.ImageUtil;
import com.sina.book.parser.BookDetailParser;
import com.sina.book.parser.EpubCheckParser;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.useraction.BasicFuncUtil;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.URLUtil;
import com.sina.book.util.UninstallObserverUtil;
import com.sina.book.util.Util;

/**
 * 微博呼起中转页
 * 
 * @author MarkMjw
 * @date 2013-11-22
 */
public class TransitActivity extends BaseActivity implements Callback {
	private final String TAG = "TransitActivity";

	public static final int MSG_INIT = 0x01;
	public static final int MSG_START = 0x02;
	public static final int MSG_CHECKUPDATE = 0x04;

	/** 去主界面(下载书) */
	private final String TYPE_MAIN_DOWN = "1";
	/** 去主界面 */
	private final String TYPE_MAIN = "2";
	/** 去阅读页 */
	private final String TYPE_READ = "3";

	public Handler mHandler;

	private boolean isFinish = false;
	
	// View
	// private View mWeiboPageLogoView;
	// private View mSinaAppView;

	static {
		System.loadLibrary("uninstall");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 退出软件时，软件没有完全关闭，第二次进入软件时，变量参数值没有重置，在此重置一下
		CloudSyncUtil.getInstance().setIsQuitAppOrLogoutAcount(false);

		// 判断是不是第一次登录
		// 为了处理这种情况：用户卸载客户端时书架内已经添加了许多书籍，此后再次安装客户端并
		// 启动客户端时，书架内的书籍依然存在。
		// 为了解决这个问题，在首次启动客户端时进行一次判断，
		// 如果是第一次启动，做一次类似退出登录的操作。
		// 将书架内的书籍，绑定过UID并且非在线的书籍进行删除并缓存到BookCacheTable中
		// 另外增加一个条件：第一次启动时处于未登录状态
		boolean isFirstStartApp = StorageUtil.getBoolean("isFirstStartApp", true);
		boolean isNoLoginState = LoginUtil.isValidAccessToken(SinaBookApplication.gContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS;
//		LogUtil.d("ReadInfoLeft", "TransitActivity >> isFirstStartApp >> " + isFirstStartApp);
//		LogUtil.d("ReadInfoLeft", "TransitActivity >> isNoLoginState >> " + isNoLoginState);
		
		if (isFirstStartApp && isNoLoginState) {
			StorageUtil.saveBoolean("isFirstStartApp", false);
			CloudSyncUtil.getInstance().logout("TransitActivity");
		}

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 不显示系统的标题栏
		setContentView(R.layout.act_transit);
		// mSinaAppView = findViewById(R.id.sinaapp_img);
		// mWeiboPageLogoView = findViewById(R.id.weibo_page_logo);

		// 微博版(新浪下载中心和新浪应用中心)的渠道包
		// if (ConstantData.isSinaAppChannel(TransitActivity.this)) {
		// mWeiboPageLogoView.setVisibility(View.VISIBLE);
		// mSinaAppView.setVisibility(View.GONE);
		// }
		
		mHandler = new Handler(this);
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_CHECKUPDATE;
//		msg.what = MSG_INIT;
		mHandler.sendMessage(msg);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// 解析Intent数据
		parseIntent();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		isFinish = hasFocus;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		
		case MSG_CHECKUPDATE:
			UpdateAppManager.getInstance(this).autoCheckVersion();
			break;
		
		case MSG_INIT:
			init();
			// 解析Intent数据
			parseIntent();
			return true;

		case MSG_START:
			WeiboBook book = (WeiboBook) msg.obj;
			if (null == book) {
				// 去主界面
				goMainPage();

			} else {
				String type = book.type;
				if (TYPE_MAIN_DOWN.equals(type)) {
					if (!HttpUtil.isConnected(this)) {
						shortToast(R.string.network_error);
						goMainPage();
					} else {
						// 去主界面并下载书
						goMainPageAndDown(book.book);
					}

				} else if (TYPE_READ.equals(type)) {
					if (HttpUtil.isConnected(this)) {
						// 去阅读页面
						judgeBookState(book);
					} else {
						// 如果网络异常则去主界面
						shortToast(R.string.network_error);
						goMainPage();
					}

				} else {
					// 其余情况默认去主界面
					goMainPage();
				}
			}

			return true;

		default:
			break;
		}

		return false;
	}

	private void init() {
		// int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
		// int displayHeight =
		// getWindowManager().getDefaultDisplay().getHeight();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int displayWidth = metrics.widthPixels;
		int displayHeight = metrics.heightPixels;
		ReadStyleManager.getInstance(displayWidth, displayHeight);

		// 初始化书籍缓存
		DownBookManager.getInstance().init();

		// 判断登录信息，触发相应的清理
		LoginUtil.isValidAccessToken(this);

		// 初始化推送服务
		// PushHelper.getInstance().start();

		// 另起线程进行一些初始化操作
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 包月信息初始化
				PaymentMonthMineUtil.getInstance().readPaymentMonthFromFile();
				if (HttpUtil.isConnected(TransitActivity.this)) {
					PaymentMonthMineUtil.getInstance().reqPaymentMonth();
				}

				// 初始化行为统计
				UserActionManager.getInstance().init(TransitActivity.this, ConstantData.USER_ACTION_URL,
						ConstantData.USER_ACTION_APP_KEY);

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

	private String mWeiboPageBookID;

	/**
	 * 从Intent中解析数据
	 */
	public void parseIntent() {
		Intent intent = getIntent();
		if (null != intent) {
			if (!TextUtils.isEmpty(intent.getScheme())) {
				// 微博呼起
				parseSchemeData(intent);
			} else {
				// 当数据为空时默认跳去主界面
				buildBook();
			}

		} else {
			// 当数据为空时默认跳去主界面
			buildBook();
		}

		String mEventKey = "微博Page_01_01_";
		if (!TextUtils.isEmpty(mWeiboPageBookID)) {
			mEventKey += mWeiboPageBookID;
		} else {
			mEventKey += "NoID";
		}
		
		// 统计微博呼起次数
		UserActionManager.getInstance().recordEventNew(Util.checkAndClip(mEventKey), null);
		UserActionManager.getInstance().recordEvent(Constants.ACTION_FROM_WEIBO);
	}

	/**
	 * 从Scheme中获取数据
	 * 
	 * @param intent
	 *            the intent
	 */
	private void parseSchemeData(Intent intent) {
		String data = intent.getDataString();
//		LogUtil.d(TAG, "The data come from weibo : " + data);
		
		if (!TextUtils.isEmpty(data)) {
			final HashMap<String, String> map = URLUtil.parseUrl(data);
			String uid = map.get("uid");

			// 如果没有登录且uid不为空，则需要登录后再继续
			if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS && !TextUtils.isEmpty(uid)) {
				// 不管登录成功失败都继续下一步操作
				LoginDialog.autoLoginLaunch(this, uid, new LoginDialog.LoginStatusListener() {
					@Override
					public void onSuccess() {
						LogUtil.d(TAG, "parseSchemeData--1---");
						buildBook(map);
					}
					
					@Override
					public void onFail() {
						LogUtil.d(TAG, "parseSchemeData--2---");
						buildBook(map);
					}
				});
			} else {
				LogUtil.d(TAG, "parseSchemeData--3---");
				buildBook(map);
			}
		} else {
			// 当数据为空时默认跳去主界面
			buildBook();
		}
	}

	/**
	 * 构造一个去主界面的WeiboBook对象并跳转
	 */
	private void buildBook() {
		WeiboBook weiboBook = new WeiboBook();
		weiboBook.type = TYPE_MAIN;
		sendMessage(weiboBook);
	}

	/**
	 * 根据参数Map构造WeiboBook对象并跳转
	 * 
	 * @param map
	 */
	private void buildBook(HashMap<String, String> map) {
		String type = map.get("type");
		String bookId = map.get("b_id");
		mWeiboPageBookID = bookId;
		
		//FIXME: 增加联网请求接口判断
		checkIsEpub(bookId, type, map);
	}
	
	private void checkIsEpub(final String bookId, final String type, final HashMap<String, String> map){
		String reqUrl = String.format(ConstantData.PAGE_EPUB_CEHCK, bookId);
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		
		RequestTask reqTask = new RequestTask(new EpubCheckParser());
		reqTask.setTaskFinishListener(new ITaskFinishListener() {
			public void onTaskFinished(TaskResult taskResult) {
				boolean isEpub = false;
				boolean isHtml = false;
				if (taskResult.retObj instanceof EpubCheckInfo) {
					EpubCheckInfo info = (EpubCheckInfo)taskResult.retObj;
					
					if(info.code > 0){
						if(TextUtils.isEmpty(info.msg)){
							Toast.makeText(TransitActivity.this, R.string.bookdetail_failed_text, Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(TransitActivity.this, info.msg, Toast.LENGTH_SHORT).show();
						}
						return;
					}
					
					if("2".equals(info.getIsEpub())){
						isEpub = true;
					}
					isHtml = info.isHtml();
				}
				
				if(isHtml){
					// html图文混排
					Book book = new Book();
					book.setBookId(bookId);
					Book dBook = DownBookManager.getInstance().getBook(book);
					
					if(dBook != null 
							&& !dBook.isOnlineBook()
							&& dBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED 
							&& Math.abs(dBook.getDownloadInfo().getProgress() - 1.0) < 0.0001){
						// 下载完成的书籍，进入Html阅读页
						float precent = 0.0f;
						if(dBook.getReadInfo() != null){
							precent = dBook.getReadInfo().getLastReadPercent();
						}
						boolean hasPrecent = precent > 0.0f ? true : false;

						String path = dBook.getDownloadInfo().getOriginalFilePath();
						FBReader.openBookActivity(
								TransitActivity.this, path, dBook,
								hasPrecent);
						finish();
					}else{
						// 进入摘要页
						BookDetailActivity.launch(TransitActivity.this, book);
						finish();
					}
				}else if(isEpub){
					// epub书籍，进入摘要页
					Book book = new Book();
					book.setBookId(bookId);
					BookDetailActivity.launch(TransitActivity.this, book);
					finish();
				}else{
					WeiboBook weiboBook = new WeiboBook();
					if (TYPE_MAIN_DOWN.equals(type)) {
						// 去主界面并下载书
						weiboBook.type = TYPE_MAIN_DOWN;

					} else if (TYPE_READ.equals(type) || (null == type && !TextUtils.isEmpty(bookId))) {
						// 1.如果传了type值，则直接判断是否去阅读页
						// 2.如果没传type值，则判断bookId是否为空，若不为空也去阅读页，
						// 这样是为了避免老版微博客户端呼起咱们新版客户端逻辑不受影响
						weiboBook.type = TYPE_READ;
					} else {
						// 其余情况默认去主界面
						weiboBook.type = TYPE_MAIN;
					}

					weiboBook.book.setBookId(bookId);

					String chapterId = map.get("c_id");
					int cId = Chapter.DEFAULT_GLOBAL_ID;
					if (!TextUtils.isEmpty(chapterId)) {
						cId = Integer.parseInt(chapterId);
						cId = cId > 0 ? cId : Chapter.DEFAULT_GLOBAL_ID;
					}
					weiboBook.chapter.setGlobalId(cId);

					String offset = map.get("c_offset");
					if (!TextUtils.isEmpty(offset)) {
						weiboBook.chapterPos = Integer.valueOf(offset);
						weiboBook.chapterPos = weiboBook.chapterPos > 0 ? weiboBook.chapterPos : 0;
					}

					sendMessage(weiboBook);
				}
			}
		});
		reqTask.setType("");
		reqTask.bindActivity(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	private void sendMessage(final WeiboBook book) {
		Runnable checkRunnable = new Runnable() {
			@Override
			public void run() {
				if (isFinish) {
					// 停止检测
					mHandler.removeCallbacks(this);

					Message msg = mHandler.obtainMessage();
					msg.what = MSG_START;
					msg.obj = book;
					mHandler.sendMessageDelayed(msg, 500);

				} else {
					// 如果Activity没有初始化完毕则等待500毫秒再次检测
					mHandler.postDelayed(this, 500);
				}
			}
		};

		// 开始检测
		mHandler.post(checkRunnable);
	}

	/**
	 * 判断微博呼起的书籍下载状态并进行下一步操作
	 * 
	 * @param book
	 */
	private void judgeBookState(WeiboBook book) {
		String bid = book.book.getBookId();
		if (TextUtils.isEmpty(bid)) {
			// 无bookId时默认去主界面
			goMainPage();
		} else {
			Book localBook = DownBookManager.getInstance().getBook(book.book);
			if (null != localBook) {
				// 书架（本地存在）的书籍
				Chapter chapter = DBService.getChapter(localBook.getId(), book.chapter.getGlobalId());
				if (null != chapter && chapter.getGlobalId() > 0) {
					book.chapterPos = (int) book.chapter.getStartPos();
				}

				DownBookJob bookJob = DownBookManager.getInstance().getJob(book.book);
				int state = bookJob.getState();

				switch (state) {
				case STATE_PAUSED:
				case STATE_FAILED:
				case STATE_WAITING:
				case STATE_RUNNING:
					// 这几种状态直接去书架
					LogUtil.w(TAG, "书架的书 -> 暂停or失败or等待or下载中");

					MainActivity.launch(this);
					break;

				case STATE_RECHARGE:
					// 去书架并弹出购买对话框
					LogUtil.w(TAG, "书架的书 -> 余额不足");

					MainActivity.launch(this);
					break;

				case STATE_FINISHED:
					// 下载完成直接去阅读页进行本地阅读
					ReadActivity.setChapterReadEntrance("微博Page");
					ReadActivity.launch(this, localBook, false, chapter, false);
					break;

				case STATE_PREPARING:
				case STATE_UNKNOWN:
				default:
					// 应该不能到这里，到了这里表示出错了
					MainActivity.launch(this);
					break;
				}

				finish();
			} else {
				// 下载书籍详情并去阅读页在线阅读
				downBookInfo(book.book, book.chapter);
			}
		}
	}

	/**
	 * 下载书籍详情并去阅读页
	 * 
	 * @param book
	 * @param chapter
	 */
	private void downBookInfo(Book book, final Chapter chapter) {
		String reqUrl = String.format(ConstantData.URL_BOOK_INFO, book.getBookId(), book.getSid(), book.getBookSrc());
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new BookDetailParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

		reqTask.setTaskFinishListener(new ITaskFinishListener() {
			@Override
			public void onTaskFinished(TaskResult taskResult) {
				if (null != taskResult && taskResult.stateCode == HttpStatus.SC_OK) {
					if (taskResult.retObj instanceof BookDetailData) {
						BookDetailData data = (BookDetailData) taskResult.retObj;
						Book book = data.getBook();
						// if (null != book) {
						// book.setOnlineBook(true);
						// }
						goReadPage(book, chapter);
					}
				}
			}
		});
		reqTask.execute(params);
	}

	private void goReadPage(Book book, Chapter chapter) {
		ReadActivity.setChapterReadEntrance("微博Page");
		ReadActivity.launch(TransitActivity.this, book, true, false);
		finish();
	}

	private void goMainPage() {
		MainActivity.launch(this);
		finish();
	}

	private void goMainPageAndDown(Book book) {
		String bid = book.getBookId();
		if (!TextUtils.isEmpty(bid)) {
			MainActivity.launchWithBookId(this, book.getBookId());
		} else {
			MainActivity.launch(this);
		}
		finish();
	}

	private class WeiboBook implements Serializable {
		private static final long serialVersionUID = 5223947062781049980L;
		private Book book;
		private Chapter chapter;
		private int chapterPos = 0;
		private String type = TYPE_MAIN;

		public WeiboBook() {
			book = new Book();
			chapter = new Chapter();
		}
	}

	@Override
	protected void onDestroy() {
		String unInstallObserverUrl = Util.getUnInstallObserverUrl(ConstantData.URL_UNISTALL_OBSERVER);
		if (!StorageUtil.getBoolean(StorageUtil.KEY_UNINSTALL_OBSERVER_INIT, false)) {
			try {
				UninstallObserverUtil.getInstance(TransitActivity.this).initUninstallObserver(Build.VERSION.SDK_INT,
						unInstallObserverUrl);
			} catch (Throwable e) {
			}

			StorageUtil.saveBoolean(StorageUtil.KEY_UNINSTALL_OBSERVER_INIT, true);
		}
		super.onDestroy();
	}

}
