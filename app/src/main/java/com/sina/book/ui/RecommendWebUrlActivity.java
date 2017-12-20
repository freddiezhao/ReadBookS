package com.sina.book.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.ui.widget.BaseLayout;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * 当推荐内容为url时，使用该Activity<br>
 * 
 * @author Tsimle
 * 
 */
public class RecommendWebUrlActivity extends CustomTitleActivity implements OnClickListener {
	private WebView mWebView;
	private String mUrl;
	private String mTitle;

	private View mProgress;

	private ImageButton mBackView;
	private ImageButton mForwardView;
	private ImageButton mRefreshView;
	private ProgressBar mProgressBar;

	private View mErrorView;

	public static boolean launch(final Context context, String url, String title) {
		if (url == null || url.equals("")) {
			return false;
		}
		Intent i = new Intent(context, RecommendWebUrlActivity.class);
		i.putExtra("url", url);
		i.putExtra("title", title);
		i.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(i);
		return true;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_recommend_weburl);
		initIntent();
		initView();
		initTitle();

		if (savedInstanceState != null) {
			mWebView.restoreState(savedInstanceState);
		}

		checkViewEnabled();
		mWebView.loadUrl(mUrl);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mWebView != null) {
			mWebView.saveState(outState);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWebView != null) {
			mWebView.stopLoading();
			mWebView.removeAllViews();
			mWebView.destroy();
		}
	}

	private void initIntent() {
		if (getIntent() != null) {
			mUrl = getIntent().getStringExtra("url");
			mTitle = getIntent().getStringExtra("title");
			if (Util.isNullOrEmpty(mTitle)) {
				mTitle = "活动";
			}
			// TODO
			// mUrl = "http://sina.cn";
			// mUrl =
			// "http://book1.sina.cn/prog/wapsite/newbook/card/detail.php?pid=2&PHPSESSID=bc7d5ab0d91e8d3de7b2c9605b95f2db&gsid=4uIDe77716YJuHuqjUWY47t8Xd7&ftype=weibo&c=android";
			if (!TextUtils.isEmpty(mTitle)) {
				if (mTitle.contains("赠书卡")) {
					StorageUtil.saveBoolean(StorageUtil.KEY_NEW_FUNC_ZENGSHUKA, false);
				}
			}
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
				AlertDialog.Builder builder = new AlertDialog.Builder(RecommendWebUrlActivity.this).setTitle("提示")
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
		// 支持JS方法调用
		mWebView.addJavascriptInterface(new Object() {
			public void getcardsuccess() {
				LogUtil.d("RecommendWebUrlActivity", "getcardsuccess >> ");
				mWebView.post(new Runnable() {
					@Override
					public void run() {
						if (mWebView != null) {
							mWebView.reload();
						}
					}
				});
			}

			public void addshelf(String bookId) {
				LogUtil.d("RecommendWebUrlActivity", "addshelf >> bookId=" + bookId);
				if (!TextUtils.isEmpty(bookId)) {
					// 加入云端，但是不直接加入书架
					Book book = new Book();
					book.setBookId(bookId);
					CloudSyncUtil.getInstance().addH5GetCardBookId(bookId);
					CloudSyncUtil.getInstance().add2Cloud(RecommendWebUrlActivity.this, book, false);
				}
			}
		}, "sinacard");

		mProgress = findViewById(R.id.waitingLayout);
		mProgress.setVisibility(View.GONE);

		mBackView = (ImageButton) findViewById(R.id.layout_bottom_webview_goback);
		mForwardView = (ImageButton) findViewById(R.id.layout_bottom_webview_goforward);
		mRefreshView = (ImageButton) findViewById(R.id.layout_bottom_webview_refresh);
		mProgressBar = (ProgressBar) findViewById(R.id.layout_bottom_webview_progress);

		mErrorView = findViewById(R.id.webview_error_layout);

		mBackView.setOnClickListener(this);
		mForwardView.setOnClickListener(this);
		mRefreshView.setOnClickListener(this);
	}

	private void initTitle() {
		View leftView = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(leftView);

		TextView middleView = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middleView.setText(mTitle);
		setTitleMiddle(middleView);
	}

	// @Override
	// public void onBackPressed() {
	// if (mWebView.canGoBack()) {
	// mWebView.goBack();
	// } else {
	// finish();
	// }
	// }

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 显示错误界面时按返回键关闭当前界面
		if (keyCode == KeyEvent.KEYCODE_BACK && mErrorView.isShown()) {
			return super.onKeyDown(keyCode, event);
		}

		if (keyCode == KeyEvent.KEYCODE_BACK && checkBackViewEnabled()) {
			String currUrl = mWebView.getUrl();
			if (currUrl != null && currUrl.startsWith("https://")) {
				// 重定向的问题
				mWebView.goBack();
			}
			mWebView.goBack(); // goBack()表示返回webView的上一页面
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void retry() {
		mErrorView.setVisibility(View.GONE);
		mErrorView.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mWebView.reload();
			}
		}, 500);
	}

	@Override
	public void onClickLeft() {
		this.finish();
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (isFinishing()) {
				return false;
			}
			mWebView.requestFocus();
			if (url.startsWith("sms") && url.split(":").length > 1) {
				Intent intent = new Intent();
				// 系统默认的action，用来打开默认的短信界面
				intent.setAction(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse("smsto:" + url.split(":")[1]));
				RecommendWebUrlActivity.this.startActivity(intent);
				return true;
			} else if (url.startsWith("http://book1.sina.cn/prog/wapsite/newbook/card/toread.php?bid=")) {
				if (Util.isFastDoubleClick()) {
					return true;
				}

				// 拦截该地址，进入摘要页
				String bookId = HttpUtil.getUrlKeyValue(url, "bid");
				if (!TextUtils.isEmpty(bookId)) {
					Book book = new Book();
					book.setBookId(bookId);
					BookDetailActivity.launch(RecommendWebUrlActivity.this, book, "我的赠书卡_01_01", "个人中心_赠书卡");
					return true;
				}
			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			// LogUtil.d("RecommendWebUrlActivity", "onPageStarted >> ");
			View middleView = getMiddleView();
			if (middleView != null) {
				View titleView = middleView.findViewWithTag(BaseLayout.TAG_MIDDLE);
				if (titleView != null && titleView instanceof TextView) {
					TextView mTextview = (TextView) titleView;

					String text = mTitle + "(加载中...)";
					mTextview.setText(text);
					// preTitleText = mTextview.getText().toString();
					// preTitleText.replaceAll("(加载中...)", "");
					// mTextview.setText(preTitleText + "(加载中...)");
				}
			}

			onPageStart();
			if (url != null && url.endsWith("apk")) {
				Intent intent = new Intent();
				intent.setData(Uri.parse(url));
				intent.setAction(Intent.ACTION_VIEW);
				startActivity(intent);
				onPageFinish(view);
			}
		}

		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			mErrorView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			super.onReceivedSslError(view, handler, error);
			mErrorView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			onPageFinish(view);
			View middleView = getMiddleView();
			if (middleView != null) {
				View titleView = middleView.findViewWithTag(BaseLayout.TAG_MIDDLE);
				if (titleView != null && titleView instanceof TextView) {
					TextView mText = (TextView) titleView;
					mText.setText(mTitle);
				}
			}
		}
	}

	private void onPageStart() {
		// 加载网页过程中"返回"要设置为可用的
		mBackView.setEnabled(true);
		mLoadingUrl = true;
		mProgressBar.setVisibility(View.VISIBLE);
		mRefreshView.setVisibility(View.INVISIBLE);
		mBackView.setImageResource(R.drawable.weibobrowser_stop);
	}

	private void onPageFinish(WebView view) {
		mLoadingUrl = false;
		mWebView.setVisibility(View.VISIBLE);
		mRefreshView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mBackView.setImageResource(R.drawable.weibobrowser_goback);
		// mTitleBarTitleTextView.setText(view.getTitle());
		checkViewEnabled();
	}

	private void checkViewEnabled() {
		checkBackViewEnabled();
		checkForwardViewEnabled();
	}

	private boolean checkBackViewEnabled() {
		if (mWebView.canGoBack()) {
			mBackView.setEnabled(true);
			return true;
		} else {
			mBackView.setEnabled(false);
		}
		return false;
	}

	private boolean checkForwardViewEnabled() {
		if (mWebView.canGoForward()) {
			mForwardView.setEnabled(true);
			return true;
		} else {
			mForwardView.setEnabled(false);
		}
		return false;
	}

	private boolean mLoadingUrl = true;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_bottom_webview_goback:
			if (mLoadingUrl) {
				mLoadingUrl = false;
				mWebView.stopLoading();
				checkViewEnabled();
			} else {
				if (checkBackViewEnabled()) {
					String currUrl = mWebView.getUrl();
					if (currUrl != null && currUrl.startsWith("https://")) {
						// 重定向的问题
						mWebView.goBack();
					}
					mWebView.goBack();
				}
			}
			break;
		case R.id.layout_bottom_webview_goforward:
			if (checkForwardViewEnabled()) {
				mWebView.goForward();
			}
			break;
		case R.id.layout_bottom_webview_refresh:
			mWebView.reload();
			break;
		}
	}

}
