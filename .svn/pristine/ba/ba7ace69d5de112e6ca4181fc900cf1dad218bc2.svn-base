package com.sina.book.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.sina.book.R;
import com.sina.book.ui.widget.BaseLayout;

/**
 * 所有页面的基类
 * 
 * @author Administrator
 * 
 */
public abstract class CustomTitleActivity extends BaseActivity implements BaseLayout.BarClickListener {

	/**
	 * 页面标题
	 */
	protected BaseLayout mBaseLayout;
	protected Button mNetSetButton;
	protected Button mRetryButton;
	/**
	 * Title刷新按钮的loading状态
	 */
	protected Handler mHandler;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mHandler = new Handler();
		init(savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResID) {
		mBaseLayout = new BaseLayout(this, layoutResID);
		super.setContentView(mBaseLayout);
		contentInit();
	}

	@Override
	public void setContentView(View view) {
		mBaseLayout = new BaseLayout(this, view);
		super.setContentView(mBaseLayout);
		contentInit();
	}

	private void contentInit() {
		mBaseLayout.setBarClickListener(this);
		mNetSetButton = (Button) findViewById(R.id.net_set_btn);
		mRetryButton = (Button) findViewById(R.id.retry_btn);
		if (mRetryButton != null) {
			mRetryButton.setOnClickListener(new OnBtnclickListener());
		}
		if (mNetSetButton != null) {
			mNetSetButton.setOnClickListener(new OnBtnclickListener());
		}
	}

	protected abstract void init(Bundle savedInstanceState);

	@Override
	public void onClickRight() {
	}

	@Override
	public void onClickLeft() {
	}

	@Override
	public void onClickMiddle() {
	}

	@Override
	public void onClickNearRight() {
	}

	@Override
	protected void onResume() {
		super.onResume();
		onLoad();
	}

	@Override
	protected void onStop() {
		onRelease();
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 当可以释放资源时由父类调用<br>
	 * 与onLoad成对使用
	 */
	public void onRelease() {

	}

	/**
	 * 当资源释放了，需要重新加载，由父类调用<br>
	 * 与onRelease成对使用
	 */
	public void onLoad() {

	}

	public void setTitleLeft(View view) {
		mBaseLayout.setTitleLeft(view);
	}

	public void setTitleRight(View view) {
		mBaseLayout.setTitleRight(view);
	}

	public void setTitleMiddle(View view) {
		mBaseLayout.setTitleMiddle(view);
	}

	public View getMiddleView() {
		return mBaseLayout.getMiddleView();
	}

	public void setTitleNearRight(View view) {
		mBaseLayout.setTitleNearRight(view);
	}
	
	public View getNearRightView() {
		return mBaseLayout.getNearRightView();
	}

	/**
	 * 如果dialog存在，隐藏它，否则donothing
	 * 
	 * @param id
	 */
	@SuppressWarnings("deprecation")
	protected void dismissDialogIfExists(int id) {
		try {
			dismissDialog(id);
		} catch (IllegalArgumentException e) {
			// the dialog not exists
		}
	}

	protected void retry() {

	}

	protected void netset() {
		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	}

	private class OnBtnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.retry_btn:
				retry();
				break;
			case R.id.net_set_btn:
				netset();
				break;
			}
		}

	}
}
