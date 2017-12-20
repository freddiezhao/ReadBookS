package com.sina.book.ui;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.ui.widget.CustomTabHost;
import com.sina.book.useraction.UserActionManager;

@SuppressWarnings("deprecation")
public class MainTabActivity extends TabActivity implements OnCheckedChangeListener {

	public static final String ACT_CHANGE_TAB = "com.sina.changetab";
	public static final String KEY_TAB = "tab";
	public static final String KEY_TAB_CONTENT_INDEX = "tab_content_index";

	public static final String TAG_TAB1 = "tag1";
	public static final String TAG_TAB2 = "tag2";
	public static final String TAG_TAB3 = "tag3";
	public static final String TAG_TAB4 = "tag4";

	private final int RADIO_COUNT = 4;

	private TabHost mHost;
	private RadioGroup mRadioGroup;
	/**
	 * 上一次切换到的tab
	 */
	private RadioButton[] mRadioButtons;

	/**
	 * 被干掉之前的tabIndex
	 */
	private int mLastTabIndex;
	/**
	 * 初始显示第几个tab
	 */
	private int mTabIndex;
	/**
	 * 初始显示该tab的第几项
	 */
	private int mTabContentIndex;

	public static boolean launch(Activity context) {
		Intent intent = new Intent();
		intent.setClass(context, MainTabActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(intent);
		// context.overridePendingTransition(R.anim.push_up_in,
		// R.anim.push_fade_out);
		return true;
	}

	public static boolean launch(Activity context, int index) {
		Intent intent = new Intent();
		intent.setClass(context, MainTabActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.putExtra(MainTabActivity.KEY_TAB, index);
		context.startActivity(intent);
		// context.overridePendingTransition(R.anim.push_up_in,
		// R.anim.push_fade_out);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		SinaBookApplication.push(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (savedInstanceState != null) {
			mLastTabIndex = savedInstanceState.getInt("curtab", 0);
		}
		setContentView(R.layout.act_maintabs);
		getIntentData();
		initTabContents();
		initRadios();
		// checkNetworkInfo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SinaBookApplication.remove(this);
	}

	@Override
	public void finishFromChild(Activity child) {
		super.finishFromChild(child);
		// overridePendingTransition(R.anim.push_fade_in, R.anim.push_up_out);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		getIntentData();
		if (mRadioButtons != null && mRadioButtons.length > 0) {
			mRadioButtons[mTabIndex].setChecked(true);
		}
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// 开始行为统计
		UserActionManager.getInstance().onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// 结束行为统计，并提交服务器
		UserActionManager.getInstance().onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mHost != null) {
			int curTabIndex = mHost.getCurrentTab();
			outState.putInt("curtab", curTabIndex);
		}
		super.onSaveInstanceState(outState);
	}

	private void getIntentData() {
		Intent intent = getIntent();
		if (intent != null) {
			mTabIndex = intent.getIntExtra(KEY_TAB, mLastTabIndex);
			if (mTabIndex >= RADIO_COUNT) {
				mTabIndex = RADIO_COUNT - 1;
			}
			mTabContentIndex = intent.getIntExtra(KEY_TAB_CONTENT_INDEX, 0);
		}
	}

	private void initTabContents() {
		mHost = getTabHost();
		mHost.clearAllTabs();
		if (mHost instanceof CustomTabHost) {
			((CustomTabHost) mHost).setInInitStep(true);
		}

		Intent nowIntent = new Intent();
		nowIntent.setClass(this, RecommendActivity.class);
		if (mTabIndex == 0) {
			nowIntent.putExtra(KEY_TAB_CONTENT_INDEX, mTabContentIndex);
		}
		mHost.addTab(mHost.newTabSpec(TAG_TAB1)
				.setIndicator(TAG_TAB1, getResources().getDrawable(R.drawable.ic_launcher)).setContent(nowIntent));

		Intent dayIntent = new Intent();
		dayIntent.setClass(this, SellFastListActivity.class);
		if (mTabIndex == 1) {
			dayIntent.putExtra(KEY_TAB_CONTENT_INDEX, mTabContentIndex);
		}
		mHost.addTab(mHost.newTabSpec(TAG_TAB4)
				.setIndicator(TAG_TAB4, getResources().getDrawable(R.drawable.ic_launcher)).setContent(dayIntent));

		Intent weekIntent = new Intent();
		weekIntent.setClass(this, PartitionActivity.class);

		if (mTabIndex == 2) {
			weekIntent.putExtra(KEY_TAB_CONTENT_INDEX, mTabContentIndex);
		}
		mHost.addTab(mHost.newTabSpec(TAG_TAB2)
				.setIndicator(TAG_TAB2, getResources().getDrawable(R.drawable.ic_launcher)).setContent(weekIntent));

		Intent mapIntent = new Intent();
		mapIntent.setClass(this, CommonRecommendActivity.class);
		mapIntent.putExtra("type", CommonRecommendActivity.PEOPLE_TYPE);
		if (mTabIndex == 3) {
			mapIntent.putExtra(KEY_TAB_CONTENT_INDEX, mTabContentIndex);
		}
		mHost.addTab(mHost.newTabSpec(TAG_TAB3)
				.setIndicator(TAG_TAB3, getResources().getDrawable(R.drawable.ic_launcher)).setContent(mapIntent));
		if (mHost instanceof CustomTabHost) {
			((CustomTabHost) mHost).setInInitStep(false);
		}
	}

	private void initRadios() {
		mRadioGroup = (RadioGroup) findViewById(R.id.main_radio);
		mRadioButtons = new RadioButton[RADIO_COUNT];
		for (int i = 0; i < RADIO_COUNT; i++) {
			mRadioButtons[i] = (RadioButton) mRadioGroup.findViewWithTag("radio_button" + i);
			mRadioButtons[i].setOnCheckedChangeListener(this);
		}
		mRadioButtons[mTabIndex].setChecked(true);
	}

	/**
	 * 检测网络连接状态
	 */
	// private void checkNetworkInfo() {
	// if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
	// startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	// }
	// }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked && mHost != null) {
			// 一段有风险的逻辑，可以不完成
			try {
				if (mHost.getCurrentTabTag() != null) {
					Activity curActivity = getCurrentActivity();
					if (curActivity instanceof CustomTitleActivity) {
						((CustomTitleActivity) curActivity).onRelease();
					}
				}
			} catch (Exception e) {

			}

			if (buttonView == mRadioButtons[0]) {
				mHost.setCurrentTabByTag(TAG_TAB1);

				BaseActivity.setClassTop(RecommendActivity.class);

				UserActionManager.getInstance().recordEvent("精选推荐");
			} else if (buttonView == mRadioButtons[1]) {
				mHost.setCurrentTabByTag(TAG_TAB4);

				BaseActivity.setClassTop(SellFastListActivity.class);
				UserActionManager.getInstance().recordEvent("畅销榜单");
			} else if (buttonView == mRadioButtons[2]) {
				mHost.setCurrentTabByTag(TAG_TAB2);

				BaseActivity.setClassTop(PartitionActivity.class);
				UserActionManager.getInstance().recordEvent("分类浏览");
			} else if (buttonView == mRadioButtons[3]) {
				mHost.setCurrentTabByTag(TAG_TAB3);

				BaseActivity.setClassTop(CommonRecommendActivity.class);
				UserActionManager.getInstance().recordEvent("大家推荐");
			}
		}
	}
}
