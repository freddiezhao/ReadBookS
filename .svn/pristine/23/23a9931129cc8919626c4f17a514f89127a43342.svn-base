package com.sina.book.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Scroller;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.ConstantData;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.image.ImageUtil;
import com.sina.book.parser.SimpleParser;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.NavigaterPageIndex;
import com.sina.book.useraction.BasicFuncUtil;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.UninstallObserverUtil;
import com.sina.book.util.Util;

public class SplashActivity extends BaseActivity implements Callback
{

	private final int				MSG_START		= 1;
	private final int				MSG_INIT		= 2;

	private final int				DELAY_TIME		= 2000;
	private View					mSplashIv;
	// private View mSplashIvSpringFestival;
	// private ImageView mSplashImSpecialLogo;
	private Handler					mHandler;
	private int						mSelectionIndex;

	private static final int		PAGE_COUNT		= 3;
	private static final int		PAGE_IMG_COUNT	= 2;
	private ViewPager				mGuidePageView;
	private NavigaterPageIndex		mNavigater;
	private NavigateViewPageAdapter	mAdapter;
	private FixedSpeedScroller		mScroller;


	private boolean needToWait = true;
	
	public static long beginTime;
	
//	public void test(){
//
////		int[] ints = new int[]{1,2,3,4};
////		byte[] data = new byte[8];
////		System.arraycopy(ints, 0, data, 0, data.length);
//
//		String text = "http://192.168.16.240:8082/epub/OEBPS/Styles/stylesheet.css";
//		String encoder = URLEncoder.encode(text);
//		String decoder = URLDecoder.decode(encoder);
//
//		System.out.println();
//	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	
//		test();
//		DesUtils.test();

		// test();
		// test2();

		// HtmlDownBookTask.parseImg();

		// // 退出软件时，软件没有完全关闭，第二次进入软件时，变量参数值没有重置，在此重置一下
		CloudSyncUtil.getInstance().setIsQuitAppOrLogoutAcount(false);

		SinaBookApplication.push(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 不显示系统的标题栏

		mHandler = new Handler(this);

		// splash的时间不浪费了，拿来初始化
		Message m = mHandler.obtainMessage();
		m.what = MSG_INIT;
		mHandler.sendMessage(m);

		setContentView(R.layout.act_splash);
		findViewById();

		// // 接着判断
		// if (StorageUtil.isShowGuide()) {
		// if (ConstantData.getVersion(this) < 1.6f) {
		// // 版本号小于1.6才处理
		// new GenericTask() {
		// @Override
		// protected TaskResult doInBackground(TaskParams... params) {
		// DBService.updateBookPath();
		// // 更新缓存
		// DownBookManager.getInstance().resetCacheBooks();
		// return null;
		// }
		// }.execute();
		// }
		// showGuidePage();
		// ConstantData.reqChannel();
		// CloudSyncUtil.getInstance().syncTo("SplashActivity-handleMessage");
		// } else {
		// delay_time后进入应用
		mHandler.sendEmptyMessageDelayed(MSG_START, DELAY_TIME);
		// }
	}

	private void findViewById()
	{
		mSplashIv = findViewById(R.id.splash_pic);
		// 新春版容器控件
		// mSplashIvSpringFestival =
		// findViewById(R.id.splash_pic_spring_festival);
		// mSplashImSpecialLogo = (ImageView) findViewById(R.id.special_logo);

		// 判断渠道，针对送券版(2014年新春活动版本)渠道显示不同的视图
		int channelCode = ConstantData.getChannelCode(SinaBookApplication.gContext);
		if (channelCode == ConstantData.CHANNEL_QUAN_GIFT) {
			mSplashIv.setVisibility(View.GONE);
			// mSplashIvSpringFestival.setVisibility(View.VISIBLE);
		} else if (channelCode == ConstantData.CHANNEL_BAIDU_LIMITFREE) {
			// mSplashImSpecialLogo.setVisibility(View.VISIBLE);
		} else if (channelCode == ConstantData.CHANNEL_BAIDU_FAMILY_91
				|| channelCode == ConstantData.CHANNEL_BAIDU_FAMILY_BAIDU
				|| channelCode == ConstantData.CHANNEL_BAIDU_FAMILY_ANZHUO) {
			// mSplashImSpecialLogo.setVisibility(View.VISIBLE);
		} else if (channelCode == ConstantData.CHANNEL_360) {
			// mSplashImSpecialLogo.setImageResource(R.drawable.special_logo);
			// mSplashImSpecialLogo.setVisibility(View.VISIBLE);
		}
	}

	private void enterMainWithBid(String bid)
	{
		// 记录当前的版本号
		StorageUtil.setShowGuide();
		MainActivity.launchWithBookId(SplashActivity.this, bid);
		finish();
	}

	private void enterMain()
	{
		// 记录当前的版本号
		StorageUtil.setShowGuide();
		MainActivity.launch(SplashActivity.this);
		finish();
	}

	@Override
	protected void onDestroy()
	{
		String unInstallObserverUrl = Util.getUnInstallObserverUrl(ConstantData.URL_UNISTALL_OBSERVER);
		if (!StorageUtil.getBoolean(StorageUtil.KEY_UNINSTALL_OBSERVER_INIT, false)) {
			try {
				UninstallObserverUtil.getInstance(SplashActivity.this).initUninstallObserver(Build.VERSION.SDK_INT,
						unInstallObserverUrl);
			} catch (Throwable e) {
			}
			StorageUtil.saveBoolean(StorageUtil.KEY_UNINSTALL_OBSERVER_INIT, true);
		}
		SinaBookApplication.remove(this);
		super.onDestroy();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mGuidePageView != null && mAdapter != null) {
				if (mSelectionIndex == mAdapter.getCount() - 1) {
					openMainTab(mAdapter.isShareWeiboChecked());
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		if (msg.what == MSG_START) {
			// 最多再等2秒，让登录信息顺利取回
			if (needToWait) {
				mHandler.sendEmptyMessageDelayed(MSG_START, DELAY_TIME);
				return true;
			}

			LoginUtil.cancelValidAccessToken2Refresh();
			// 确认初始化，PaymentMonthMineUtil会防重
			if (HttpUtil.isConnected(SplashActivity.this)) {
				PaymentMonthMineUtil.getInstance().reqPaymentMonth();
			}

			// 新浪应用中心，特殊渠道
			if (ConstantData.isSinaAppChannel(SplashActivity.this)) {
				// if (ConstantData.isSinaAppChannel(SplashActivity.this) &&
				// msg.arg1 == 1000) {

				if (StorageUtil.isShowGuide()) {
					if (LoginUtil.isValidAccessToken(SplashActivity.this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
						SinaAppLoginActivity.launch(SplashActivity.this);
						finish();
						return true;
					} else {
						if (LoginUtil.getLoginInfo().getUserInfo() != null
								&& !Util.isNullOrEmpty(LoginUtil.getLoginInfo().getUserInfo().getAutoDownBid())) {
							enterMainWithBid(LoginUtil.getLoginInfo().getUserInfo().getAutoDownBid());
							return true;
						} else {
							enterMain();
							return true;
						}
					}
				} else {
					// 不是第一次进入
					if (LoginUtil.isValidAccessToken(SplashActivity.this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
						SinaAppLoginActivity.launch(SplashActivity.this);
						finish();
						return true;
					}
				}

				// 不进入选你喜欢，不进入分类选择
				MainActivity.launch(this);
				finish();
				return true;
			}

			/*
			 * if (StorageUtil.isShowGuide()) { if
			 * (ConstantData.getVersion(this) < 1.6f) { // 版本号小于1.6才处理 new
			 * GenericTask() {
			 * 
			 * @Override protected TaskResult doInBackground(TaskParams...
			 * params) { DBService.updateBookPath(); // 更新缓存
			 * DownBookManager.getInstance().resetCacheBooks(); return null; }
			 * }.execute(); }
			 * 
			 * ConstantData.reqChannel(); showGuidePage();
			 * CloudSyncUtil.getInstance
			 * ().syncTo("SplashActivity-handleMessage"); } else {
			 */
			if (isNeedSelectPartition()) {
				// 选你喜欢
				PartitionLikedActivity.launch(this, PartitionLikedActivity.KEY_FROM_SPLASH_ACTIVITY);
				finish();
			} else {
				MainActivity.launch(this);
				finish();
			}
			// }
			return true;
		} else if (msg.what == MSG_INIT) {
			init();
			return true;
		}

		return false;
	}

	private void init()
	{
		// DownBookManager.getInstance().stopAllJob();
		// 初始化书籍缓存
		DownBookManager.getInstance().init();

		// int displayWidth = getWindowManager().getDefaultDisplay().getWidth();
		// int displayHeight =
		// getWindowManager().getDefaultDisplay().getHeight();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int displayWidth = metrics.widthPixels;
		int displayHeight = metrics.heightPixels;

		// String str = "";
		// DisplayMetrics dm = new DisplayMetrics();
		// // getWindowManager().getDefaultDisplay().getMetrics(dm);
		// dm = SinaBookApplication.gContext.getResources().getDisplayMetrics();
		// int screenWidth = dm.widthPixels;
		// int screenHeight = dm.heightPixels;
		// float density = dm.density;
		// float xdpi = dm.xdpi;
		// float ydpi = dm.ydpi;
		// str += "屏幕分辨率为:" + dm.widthPixels + " * " + dm.heightPixels + "\n";
		// str += "绝对宽度:" + String.valueOf(screenWidth) + "pixels\n";
		// str += "绝对高度:" + String.valueOf(screenHeight) + "pixels\n";
		// str += "逻辑密度:" + String.valueOf(density) + "\n";
		// str += "X 维 :" + String.valueOf(xdpi) + "像素每英尺\n";
		// str += "Y 维 :" + String.valueOf(ydpi) + "像素每英尺\n";
		// LogUtil.i("ScreenInfo", str);
		// LogUtil.i("ScreenInfo", ScreenInfo.getInstance().toString());

		ReadStyleManager.getInstance(displayWidth, displayHeight);

		// 如果登录信息已被清理，同步清理书架的书籍
		LoginUtil.isValidAccessToken2Refresh(new LoginStatusListener()
		{

			@Override
			public void onSuccess()
			{
				needToWait = false;
				// 解决：BugID=21465
				// 验证已经登录了，先同步一次
				// 暂时不改，如果需要改把下面的代码打开即可
				// CloudSyncUtil.getInstance().login();
			}

			@Override
			public void onFail()
			{
				needToWait = false;
			}
		});

		// 初始化推送服务
		// PushHelper.getInstance().start();

		// 启动定时更新
		// Intent intent = new Intent(SinaBookApplication.this,
		// UpdateChapterService.class);
		// startService(intent);

		// 另起线程进行一些初始化操作
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// 初始化行为统计
				UserActionManager.getInstance().init(SplashActivity.this, ConstantData.USER_ACTION_URL,
						ConstantData.USER_ACTION_APP_KEY);

				// 包月信息初始化
				PaymentMonthMineUtil.getInstance().readPaymentMonthFromFile();
				if (HttpUtil.isConnected(SplashActivity.this)) {
					PaymentMonthMineUtil.getInstance().reqPaymentMonth();
				}

				// 安装统计
				BasicFuncUtil.getInstance().recordInstall();

				// CJL 暂时注释掉(记录崩溃异常)
				// UEHandler ueHandler = new UEHandler(getApplicationContext());
				// Thread.setDefaultUncaughtExceptionHandler(ueHandler);

				// 调起本地图片缓存清理或无用文件的清理
				if (!ImageUtil.clearDiskCache(SinaBookApplication.gContext)) {
					DownBookManager.getInstance().clearUselessFile();
				}
			}
		}).start();

	}

	// 显示导航页
	private void showGuidePage()
	{
		mGuidePageView = (ViewPager) findViewById(R.id.welcome_guide);
		View guideView = findViewById(R.id.welcome_guide_navigater);
		mNavigater = (NavigaterPageIndex) guideView.findViewById(R.id.mycontrolview);

		mAdapter = new NavigateViewPageAdapter();
		mGuidePageView.setAdapter(mAdapter);
		mNavigater.initPageIndex(mAdapter.getCount() - 1);
		mSplashIv.setVisibility(View.GONE);
		guideView.setVisibility(View.VISIBLE);
		mGuidePageView.setVisibility(View.VISIBLE);
		mGuidePageView.setOnPageChangeListener(new NavigatePageChangeListener());
		try {
			Field scroller;
			scroller = ViewPager.class.getDeclaredField("mScroller");
			scroller.setAccessible(true);
			mScroller = new FixedSpeedScroller(this);
			scroller.set(mGuidePageView, mScroller);

			Field minimumVelocity = ViewPager.class.getDeclaredField("mMinimumVelocity");
			minimumVelocity.setAccessible(true);
			minimumVelocity.setInt(mGuidePageView, PixelUtil.dp2px(70));

			Field flingFiled = ViewPager.class.getDeclaredField("mFlingDistance");
			flingFiled.setAccessible(true);
			flingFiled.setInt(mGuidePageView, PixelUtil.dp2px(10));
		} catch (Exception e) {
			// do nothing
		}
	}

	public void openMainTab(boolean shareWeibo)
	{
		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			if (shareWeibo) {
				shareWeibo();
			}

			// 默认关注官方微博
			addAttention();
		}

		// 记录当前的版本号
		StorageUtil.setShowGuide();
		if (isNeedSelectPartition()) {
			PartitionLikedActivity.launch(this, PartitionLikedActivity.KEY_FROM_SPLASH_ACTIVITY);
			finish();
		} else {
			MainActivity.launch(this);
			finish();
		}
	}

	private void shareWeibo()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("伙伴们，微博读书全新发版啦！~~ ");
		// 更改未动态获取版本号，避免版本更改时漏改
		sb.append("我刚更新了#微博读书 Android V" + ConstantData.getVersion(SinaBookApplication.gContext) + "版#");
		sb.append(" http://t.cn/zHDkKCt");
		String msg = sb.toString();

		// Bitmap bitmap =
		// ImageUtil.getBitmapFromAssetsFile(SplashActivity.this,
		// "weibo_share_pic.jpg");
		// RequestTask task = new RequestTask(new SimpleParser(), bitmap,
		// "pic");
		RequestTask task = new RequestTask(new SimpleParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_SEND_WEIBO);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("source", ConstantData.AppKey));
		strNParams.add(new BasicNameValuePair("status", msg));
		strNParams.add(new BasicNameValuePair("access_token", LoginUtil.getLoginInfo().getAccessToken()));
		task.setPostParams(strNParams);
		task.execute(params);
	}

	private boolean isNeedSelectPartition()
	{
		// String uid;
		// if (LoginUtil.isValidAccessToken(this) ==
		// LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
		// && !TextUtils.isEmpty(uid = LoginUtil.getLoginInfo().getUID())) {
		// String key = uid + StorageUtil.KEY_SELECT_PARTITION;
		// if (StorageUtil.getBoolean(key)) {
		// return false;
		// } else {
		// return true;
		// }
		// }

		return false;
	}

	private void addAttention()
	{
		RequestTask task = new RequestTask(new SimpleParser());

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_ATTENTION_WEIBO);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("source", ConstantData.AppKey));
		strNParams.add(new BasicNameValuePair("access_token", LoginUtil.getLoginInfo().getAccessToken()));
		strNParams.add(new BasicNameValuePair("uid", "2005723670"));
		task.setPostParams(strNParams);
		task.execute(params);
	}

	private class NavigatePageChangeListener implements OnPageChangeListener
	{

		@Override
		public void onPageScrollStateChanged(int state)
		{
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{
		}

		@Override
		public void onPageSelected(int position)
		{
			mSelectionIndex = position;
			if (position < PAGE_COUNT - 1) {
				mNavigater.setVisibility(View.VISIBLE);
				mNavigater.changePageIndex(position);
			} else {
				mNavigater.setVisibility(View.GONE);
			}
		}

	}

	/*
	 * 导航页适配器
	 */
	private class NavigateViewPageAdapter extends PagerAdapter implements View.OnClickListener,
			CompoundButton.OnCheckedChangeListener
	{
		private LayoutInflater	mInflater;
		private List<Integer>	mListResourceIds;
		private List<View>		mListHoldViews;
		protected boolean		mChecked	= true;

		public NavigateViewPageAdapter()
		{
			mInflater = getLayoutInflater();
			initAdapterViews();
			mListResourceIds = new ArrayList<Integer>(PAGE_IMG_COUNT);
			mListResourceIds.add(R.drawable.guide1);
			mListResourceIds.add(R.drawable.guide2);
			// mListResourceIds.add(R.drawable.guide3);
		}

		// 销毁position位置的界面
		@Override
		public void destroyItem(View v, int position, Object arg2)
		{
			((ViewPager) v).removeView(mListHoldViews.get(position));
		}

		// 初始化position位置的界面
		@Override
		public Object instantiateItem(View v, int position)
		{
			final View pageview = mListHoldViews.get(position);
			if (position < PAGE_IMG_COUNT) {
				ImageView ivShowBg = (ImageView) pageview.findViewById(R.id.what_new);
				ivShowBg.setImageResource(mListResourceIds.get(position));
			}
			((ViewPager) v).addView(pageview);
			return pageview;
		}

		@Override
		public int getCount()
		{
			return PAGE_COUNT;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0 == arg1;
		}

		@Override
		public void onClick(View v)
		{
			openMainTab(mChecked);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			mChecked = isChecked;
		}

		public boolean isShareWeiboChecked()
		{
			return mChecked;
		}

		private void initAdapterViews()
		{
			mListHoldViews = new ArrayList<View>(PAGE_COUNT);
			mListHoldViews.add(mInflater.inflate(R.layout.vw_guide_normal, null));
			mListHoldViews.add(mInflater.inflate(R.layout.vw_guide_normal, null));
			// mListHoldViews.add(mInflater.inflate(R.layout.vw_guide_normal,
			// null));

			View last = mInflater.inflate(R.layout.vw_guide_last, null);
			final CheckBox checkbox = ((CheckBox) last.findViewById(R.id.weibo_check));
			checkbox.setOnCheckedChangeListener(this);
			final Button enter = (Button) last.findViewById(R.id.enter_weibo);
			enter.setOnClickListener(this);
			mListHoldViews.add(last);
		}
	}

	public class FixedSpeedScroller extends Scroller
	{
		private int	mDuration	= 800;

		public FixedSpeedScroller(Context context)
		{
			super(context);
		}

		public FixedSpeedScroller(Context context, Interpolator interpolator)
		{
			super(context, interpolator);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy, int duration)
		{
			// Ignore received duration, use fixed one instead
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy)
		{
			// Ignore received duration, use fixed one instead
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

	}

}
