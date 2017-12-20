package com.sina.book.ui.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.data.ConstantData;
import com.sina.book.ui.BaseActivity;
import com.sina.book.ui.BaseFragmentActivity;
import com.sina.book.ui.view.LoginDialog.AuthDialogListener;
import com.sina.book.util.Util;

/**
 * 新浪读书账号登陆流程对话框
 * 
 * @author sprite
 * @date 2014-1-9
 */
public class WebviewLoginDialog extends Dialog {

	public static final int TYPE_LOGIN = 1;

	public static final int TYPE_REGISTER = 2;

	private static WebviewLoginDialog mDialog;

	// 页面类型 1-登陆页面 2-注册页面
	private int type;

	private Activity activity;
	private String pageUrl;

	private RelativeLayout mContainer;
	private RelativeLayout webViewContainer;
	private WebView mWebView;
	private View mProgress;

	private AuthDialogListener listener;

	private String gsid;

	/**
	 * Show分享Dialog
	 * 
	 * @param activity
	 *            传入的Activity必须是BaseActivity或BaseFragmentActivity的子类
	 */
	public static void show(Activity activity, String url, AuthDialogListener listener, int type) {
		if (!(activity instanceof BaseActivity) && !(activity instanceof BaseFragmentActivity)) {
			throw new IllegalArgumentException("must be BaseActivity or BaseFragment Activity");
		}

		dismiss(activity);

		mDialog = new WebviewLoginDialog(activity, url, listener, type);
		mDialog.show();
	}

	public static void dismiss(Activity activity) {
		if (mDialog != null) {
			// if (mDialog != null && mDialog.getOwnerActivity() != null &&
			// !mDialog.getOwnerActivity().isFinishing()) {
			try {
				mDialog.dismiss();
			} catch (Exception e) {
			}
			mDialog = null;
		}
	}

	public WebviewLoginDialog(Activity activity, String loginUrl, AuthDialogListener listener, int type) {
		super(activity, android.R.style.Theme_Translucent_NoTitleBar);
		this.activity = activity;
		this.pageUrl = loginUrl;
		this.listener = listener;
		this.type = type;
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFeatureDrawableAlpha(Window.FEATURE_OPTIONS_PANEL, 0);

		mContainer = new RelativeLayout(getContext());
		// mContainer.setLayoutParams(new ViewGroup.LayoutParams(
		// android.view.ViewGroup.LayoutParams.MATCH_PARENT,
		// android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		mContainer.setBackgroundColor(0);
		setUpWebView();
		addContentView(mContainer, new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

		this.setCanceledOnTouchOutside(true);

		this.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH) {
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (Util.isSoftInputActive(mWebView)) {
						Util.hideSoftInput(activity, mWebView);
					} else {
						onBack();
					}
					return false;
				}
				return false;
			}
		});
	}

	protected void onBack() {
		if (mWebView.canGoBack()) {
			// if (mWebView.canGoBack() && !isBack) {
			mWebView.goBack();
			// String url = mWebView.getOriginalUrl();
			// if (url != null && url.contains(pageUrl)) {
			// isBack = true;
			// }
		} else {
			dismiss();
		}
	}

	// public static void synCookies(Context context, String url) {
	// CookieSyncManager.createInstance(context);
	// CookieManager cookieManager = CookieManager.getInstance();
	// cookieManager.setAcceptCookie(true);
	// cookieManager.removeSessionCookie();//移除
	// cookieManager.setCookie(url, cookies);//cookies是在HttpClient中获得的cookie
	// CookieSyncManager.getInstance().sync();
	// }

	protected void setUpWebView() {
		String sinbookLoginUrl = String.format(ConstantData.URL_SINABOOK_LOGIN, "androidclient");
		CookieSyncManager.createInstance(activity);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setCookie(sinbookLoginUrl, null);
		CookieSyncManager.getInstance().sync();

		// CookieSyncManager.createInstance(activity);
		// CookieSyncManager.getInstance().startSync();
		// CookieManager.getInstance().removeSessionCookie();
		// CookieManager cookieManager = CookieManager.getInstance();
		// cookieManager.removeAllCookie();
		// CookieSyncManager.getInstance().sync();

		LayoutInflater inflater = LayoutInflater.from(activity);
		webViewContainer = (RelativeLayout) inflater.inflate(R.layout.act_login_sinabook, null);
		webViewContainer.setBackgroundResource(R.drawable.login_dialog_bg2);
		webViewContainer.setGravity(Gravity.CENTER);

		mWebView = (WebView) webViewContainer.findViewById(R.id.login_webview);
		mProgress = webViewContainer.findViewById(R.id.login_waitingLayout);

		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSavePassword(true);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

		mWebView.setWebViewClient(new LoginWebViewClient());

		mWebView.loadUrl(pageUrl);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		Resources res = activity.getResources();
		lp.leftMargin = (int) res.getDimension(R.dimen.login_dialog_left_margin);
		lp.topMargin = (int) res.getDimension(R.dimen.login_dialog_top_margin);
		lp.rightMargin = (int) res.getDimension(R.dimen.login_dialog_right_margin);
		lp.bottomMargin = (int) res.getDimension(R.dimen.login_dialog_bottom_margin);
		mContainer.addView(webViewContainer, lp);

		// 添加左上叉号
		ImageView imgview = new ImageView(activity);
		imgview.setBackgroundResource(R.drawable.login_dialog_x);
		int w = (int) activity.getResources().getDimension(R.dimen.login_dialog_x_rect);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(w, w);
		Resources res2 = activity.getResources();
		lp2.leftMargin = (int) res2.getDimension(R.dimen.login_dialog_x_margin_left);
		lp2.topMargin = (int) res2.getDimension(R.dimen.login_dialog_x_margin_top);
		mContainer.addView(imgview, lp2);

		imgview.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Util.isSoftInputActive(mWebView)) {
					Util.hideSoftInput(activity, mWebView);
				}
				dismiss();
			}
		});
	}

	// boolean isBack = false;

	private class LoginWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// if (url.contains(pageUrl)) {
			// isBack = true;
			// } else {
			// isBack = false;
			// }

			if (url.startsWith("sms") && url.split(":").length > 1) {
				Intent intent = new Intent();
				// 系统默认的action，用来打开默认的短信界面
				intent.setAction(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse("smsto:" + url.split(":")[1]));
				activity.startActivity(intent);
				return true;
			} else {
				return super.shouldOverrideUrlLoading(view, url);
			}
		}

		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			// Log.d("comic", "=onReceivedError-description-: "+ description);
			// Log.d("comic", "=onReceivedError-failingUrl-: "+ failingUrl);
			super.onReceivedError(view, errorCode, description, failingUrl);

			// TODO: 提示错误
			Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
			dismiss();
		}

		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			super.onReceivedSslError(view, handler, error);
		}

		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			// Log.d("comic", "=onPageStarted-url-: "+ url);
			Bundle bundle = handleRedirectUrl(view, url);
			if (bundle != null) {
				// TODO: 登陆成功
				if (listener != null) {
					listener.onComplete(bundle);
				}
				view.stopLoading();
				dismiss();
			} else {
				mProgress.setVisibility(View.VISIBLE);
			}
		}

		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			// TODO:
			// CookieManager cookieManager = CookieManager.getInstance();
			// String CookieStr = cookieManager.getCookie(url);
			// Log.e("comic", "Cookies = " + CookieStr);

			mProgress.setVisibility(View.GONE);
		}
	}

	private Bundle handleRedirectUrl(WebView view, String url) {
		Bundle values = parseUrl(url);
		gsid = values.getString("gsid");

		if (gsid != null && gsid.length() > 0) {
			return values;
		} else {
			String paraUrl = values.getString("url");
			if (paraUrl != null && paraUrl.startsWith("http")) {
				Bundle tmp_values = parseUrl(paraUrl);
				gsid = tmp_values.getString("gsid");
				if (gsid != null && gsid.length() > 0) {
					return values;
				}
			}
		}
		return null;
	}

	public Bundle parseUrl(String url) {
		try {
			URL u = new URL(url);
			Bundle b = decodeUrl(u.getQuery());
			b.putAll(decodeUrl(u.getRef()));
			return b;
		} catch (MalformedURLException e) {
		}
		return new Bundle();
	}

	public static Bundle decodeUrl(String s) {
		Bundle params = new Bundle();
		if (s != null) {
			String[] array = s.split("&");
			for (String parameter : array) {
				String[] v = parameter.split("=");
				if (v != null && v.length > 1) {
					params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
				}
			}
		}
		return params;
	}

	public interface SinaBookLoginDialogListener {
		public void onComplete(Bundle values);
	}

}
