package com.sina.book.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.sina.book.R;
import com.sina.book.data.ConstantData;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * 新浪应用中心渠道包，进入后直接要求登录
 * 
 * @author Tsimle
 * 
 */
public class SinaAppLoginActivity extends BaseActivity implements OnClickListener {
	private Activity mActivity;

	public static boolean launch(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, SinaAppLoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(intent);
		return true;
	}

	public boolean isAuthComplete = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		setContentView(R.layout.act_sinaapp_login);
		findViewById(R.id.cancel_enter).setOnClickListener(this);
		findViewById(R.id.login).setOnClickListener(this);

		if (ConstantData.isSinaAppChannel(this)) {
			if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				isAuthComplete = true;
				LoginDialog.weiboLoginLaunch(this, new LoginStatusListener() {
					@Override
					public void onSuccess() {
						if (LoginUtil.isValidAccessToken(SinaAppLoginActivity.this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
								&& LoginUtil.getLoginInfo().getUserInfo() != null
								&& !Util.isNullOrEmpty(LoginUtil.getLoginInfo().getUserInfo().getAutoDownBid())) {
							enterMainWithBid(LoginUtil.getLoginInfo().getUserInfo().getAutoDownBid());
						} else {
							enterMain();
						}
					}

					public void onFail() {
						// enterMain();
					}
				});
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			if (isAuthComplete) {
				break;
			}

			if (LoginUtil.isValidAccessToken(mActivity) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				LoginDialog.weiboLoginLaunch(mActivity, new LoginStatusListener() {

					@Override
					public void onSuccess() {
						if (LoginUtil.isValidAccessToken(SinaAppLoginActivity.this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
								&& LoginUtil.getLoginInfo().getUserInfo() != null
								&& !Util.isNullOrEmpty(LoginUtil.getLoginInfo().getUserInfo().getAutoDownBid())) {
							enterMainWithBid(LoginUtil.getLoginInfo().getUserInfo().getAutoDownBid());
						} else {
							enterMain();
						}
					}

					@Override
					public void onFail() {
						// do nothing
					}
				});
			} else {
				if (!isAuthComplete) {
					enterMain();
				}
			}
			break;
		case R.id.cancel_enter:
			enterMain();
			break;
		}
	}

	private void enterMain() {
		// 记录当前的版本号
		StorageUtil.setShowGuide();
		MainActivity.launch(mActivity);
		finish();
	}

	private void enterMainWithBid(String bid) {
		// 记录当前的版本号
		StorageUtil.setShowGuide();
		MainActivity.launchWithBookId(mActivity, bid);
		finish();
	}
}
