package com.sina.book.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.Util;

/**
 * 礼包活动页
 * 
 */
public class GiftActivity extends CustomTitleActivity implements ITaskFinishListener, OnClickListener {
	private static final String TAG = "GiftActivity";

	/** 礼包活动 **/
	private WebView mWebView;

	/** 进度及错误显示 **/
	private View mProgress;

	/** 网络错误 **/
	private View mError;

	/** 网络错误，重试按钮 **/
	private Button mRetryBtn;

	/** web页之间的返回 **/
	private boolean isBack;

	/** 活动地址 **/
	// private String reqUrl =
	// "http://221.179.190.191/prog/wapsite/newbook/lottery/client_index.php?vt=4";//测试地址

	private String reqUrl = "";
	/** 活动标题 **/
	private String title;

	private CustomProDialog mProgressDialog;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_gift);
		initIntent();
		initView();
		initTitle();
		reqRedirectUrl();
	}

	private void initIntent() {
		if (getIntent() != null) {
			reqUrl = getIntent().getStringExtra("url");
			title = getIntent().getStringExtra("title");
		}
	}

	private void initView() {
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(false);
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GiftActivity.this).setTitle("提示")
						.setMessage(message).setPositiveButton("确认", new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});

				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}
		});
		mProgress = findViewById(R.id.waitingLayout);
		mError = findViewById(R.id.error_layout);
		mRetryBtn = (Button) mError.findViewById(R.id.retry_btn);
		mRetryBtn.setOnClickListener(this);
	}

	private void initTitle() {
		View leftView = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(leftView);

		TextView middleView = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middleView.setText(title);
		setTitleMiddle(middleView);
	}

	private void reqRedirectUrl() {

		if (reqUrl.contains("gsid")) {
			mWebView.loadUrl(reqUrl);
		} else {
			if (LoginUtil.isValidAccessToken(mContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
					&& Util.isNullOrEmpty(LoginUtil.getLoginInfo().getUserInfo().getGsid())) {
				GenericTask getGsidTask = new GenericTask() {
					protected void onPreExecute() {
						showProgressDialog(R.string.get_gsid);
					};

					@Override
					protected TaskResult doInBackground(TaskParams... params) {

						String gsid = LoginUtil.i.syncRequestGsidHttpClient().getGsid();
						LoginUtil.getLoginInfo().getUserInfo().setGsid(gsid);
						LoginUtil.saveLoginGsid(gsid);

						return null;
					}

					protected void onPostExecute(TaskResult result) {
						dismissProgressDialog();

						String gsid = LoginUtil.getLoginInfo().getUserInfo().getGsid();
						reqUrl = HttpUtil.setURLParams(reqUrl, "gsid", gsid);
						mWebView.loadUrl(reqUrl);
					};
				};

				getGsidTask.setTaskFinishListener(null);
				getGsidTask.execute();

			} else {
				String gsid = LoginUtil.getLoginInfo().getUserInfo().getGsid();
				reqUrl = HttpUtil.setURLParams(reqUrl, "gsid", gsid);
				mWebView.loadUrl(reqUrl);
			}
		}

	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains(reqUrl)) {
				isBack = true;
			} else {
				isBack = false;
			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			mProgress.setVisibility(View.VISIBLE);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			mProgress.setVisibility(View.GONE);
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

			Toast.makeText(GiftActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
			finish();
			// mError.setVisibility(View.VISIBLE);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}
	}

	@Override
	protected void retry() {
		reqRedirectUrl();
	}

	@Override
	public void onBackPressed() {
		// if (mWebView.canGoBack() && !isBack) {
		// mWebView.goBack();
		// String url = mWebView.getOriginalUrl();
		// if (url != null && url.contains(reqUrl)) {
		// isBack = true;
		// }
		// } else {
		finish();
		// }
	}

	@Override
	public void onClickLeft() {
		this.finish();
	}

	@Override
	protected void onDestroy() {
		LoginUtil.reqBalance(mContext);
		super.onDestroy();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.retry_btn:
			mError.setVisibility(View.GONE);
			reqRedirectUrl();
			break;

		default:
			break;
		}
	}

	private void showProgressDialog(int resId) {
		if (null == mProgressDialog) {
			mProgressDialog = new CustomProDialog(GiftActivity.this);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}

		mProgressDialog.show(resId);
	}

	private void dismissProgressDialog() {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
}
