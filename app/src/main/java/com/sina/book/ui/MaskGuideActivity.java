package com.sina.book.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import com.sina.book.util.StorageUtil;

public class MaskGuideActivity extends BaseActivity {

	private int layoutResId;

	private boolean isFullScreen = false;

	private String key;

	public static boolean launch(Context context, String key, int layoutResId) {
		if (!StorageUtil.getBoolean(key, true)) {
			return false;
		}
		StorageUtil.saveBoolean(key, false);
		Intent i = new Intent(context, MaskGuideActivity.class);
		i.putExtra("layoutId", layoutResId);
		i.putExtra("key", key);
		i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(i);
		return true;
	}

	public static boolean launchForResult(Activity context, String key, int layoutResId, int requestCode) {
		if (!StorageUtil.getBoolean(key, true)) {
			return false;
		}
		StorageUtil.saveBoolean(key, false);
		Intent i = new Intent(context, MaskGuideActivity.class);
		i.putExtra("layoutId", layoutResId);
		i.putExtra("key", key);
		i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivityForResult(i, requestCode);
		return true;
	}

	public static boolean launch(Context context, String key, int layoutResId, boolean isFullScreen) {
		if (!StorageUtil.getBoolean(key, true)) {
			return false;
		}
		StorageUtil.saveBoolean(key, false);

		Intent i = new Intent(context, MaskGuideActivity.class);
		i.putExtra("layoutId", layoutResId);
		i.putExtra("isFullScreen", isFullScreen);
		i.putExtra("key", key);
		i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(i);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initIntent();

		// 取消标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (isFullScreen) {
			// 不显示系统的标题栏
			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(layoutResId);

		View v = this.getWindow().getDecorView();
		v.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MaskGuideActivity.this.finish();
				return false;
			}
		});
	}

	@Override
	public void finish() {
		if (key != null && StorageUtil.KEY_MAIN_GUIDE_SHOW.equals(key)) {
			setResult(RESULT_OK);
		}
		super.finish();
	}

	/**
	 * 初始化Intent
	 */
	private void initIntent() {
		Intent intent = getIntent();
		layoutResId = intent.getIntExtra("layoutId", 0);
		key = intent.getStringExtra("key");
		isFullScreen = intent.getBooleanExtra("isFullScreen", false);
	}
}
