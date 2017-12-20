package com.sina.book.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.sina.book.SinaBookApplication;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.view.ListDialog;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.PayDialog;
import com.sina.book.ui.view.ShareDialog;
import com.sina.book.useraction.UserActionManager;

/**
 * FragmentActivity，应用中所有FragmentActivity均应继承它，方便进行行为统计
 * 
 * @Author: MarkMjw
 * @Date: 13-5-16 下午5:11
 */
public abstract class BaseFragmentActivity extends FragmentActivity {
	private ILifecycleListener mLifeListener;
	private boolean mIsBeDestroyed;

	public void setLifecycleListener(ILifecycleListener lifeListener) {
		mLifeListener = lifeListener;
	}

	public boolean isBeDestroyed() {
		return mIsBeDestroyed;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SinaBookApplication.push(this);
		mIsBeDestroyed = false;
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
	protected void onDestroy() {
		SinaBookApplication.remove(this);
		// 这里统一隐藏显示的对话框
		LoginDialog.release(this);
		PayDialog.release(this);
		ShareDialog.dismiss(this);
		CommonDialog.dismiss(this);
		ListDialog.dismiss(this);
		ImageLoader.getInstance().releaseContext(this);

		mIsBeDestroyed = true;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mLifeListener != null) {
			mLifeListener.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
