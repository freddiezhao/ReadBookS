package com.sina.book.control;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.book.SinaBookApplication;
import com.sina.book.data.ConstantData;
import com.sina.book.parser.RechargeParser;
import com.sina.book.ui.RechargeCenterActivity;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.useraction.DeviceInfo;
import com.sina.book.util.ApplicationUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.Util;

public class RechargeManager implements Callback, DialogInterface.OnCancelListener {

	private static final String TAG = "RechargeManager";

	private static RechargeManager sInstance;
	private static Context sContext;
	private CustomProDialog mProgressDialog;

	private RequestTask mReqTask;
	private Handler mHandler;
	private RechargeParser mParser;

	private final int MSG_ERROR_TOAST = 1;

	private RechargeManager() {
		mHandler = new Handler(this);
	}

	public static RechargeManager getInstance(Context context) {
		sContext = context;
		if (sInstance == null) {
			synchronized (RechargeManager.class) {
				if (sInstance == null) {
					sInstance = new RechargeManager();
				}
			}
		}
		return sInstance;
	}

	public void reqRechargeURL(final ITaskFinishListener listener) {
		showProgressDialog();

		// 1 如果无gsid再去取一次gsid
		String gsid = null;
		if (LoginUtil.isValidAccessToken(sContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
				&& Util.isNullOrEmpty(LoginUtil.getLoginInfo().getUserInfo().getGsid())) {
			gsid = LoginUtil.i.syncRequestGsidHttpClient().getGsid();

			if (TextUtils.isEmpty(gsid)) {
				// 无法获取到GSID
				mHandler.sendEmptyMessage(MSG_ERROR_TOAST);
				return;
			} else {
				LoginUtil.getLoginInfo().getUserInfo().setGsid(gsid);
				LoginUtil.saveLoginGsid(gsid);
			}
		}

		gsid = LoginUtil.getLoginInfo().getUserInfo().getGsid();

		String url = ConstantData.RECHARGE_URL;
		String carrier = DeviceInfo.getCarrier(SinaBookApplication.gContext);
		String apn = HttpUtil.getNetworkType(SinaBookApplication.gContext);
		String imei = ConstantData.getDeviceId();
		String deviceId = DeviceInfo.getUDID();
		String appChannel = String.valueOf(ConstantData.getChannelCode(SinaBookApplication.gContext));
		String version = ApplicationUtils.getVersionName(SinaBookApplication.gContext);

		url = HttpUtil.setURLParams(url, "gsid", gsid);
		url = HttpUtil.setURLParams(url, ConstantData.OPERATORS_NAME_KEY, carrier);
		url = HttpUtil.setURLParams(url, ConstantData.APN_ACCESS_KEY, apn);
		url = HttpUtil.setURLParams(url, ConstantData.PHONE_IMEI_KEY, imei);
		url = HttpUtil.setURLParams(url, ConstantData.DEVICE_ID_KEY, deviceId);
		url = HttpUtil.setURLParams(url, ConstantData.APP_CHANNEL_KEY, appChannel);
		url = HttpUtil.setURLParams(url, ConstantData.APP_VERSION_KEY, version);
		url = HttpUtil.setURLParams(url, ConstantData.ACCESS_TOKEN_KEY, LoginUtil.getLoginInfo().getAccessToken());
		url = HttpUtil.addAuthCode2Url(url);

		mParser = new RechargeParser();
		mReqTask = new RequestTask(mParser);

		LogUtil.d("RechargeTest", "url=" + url);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		mReqTask.setTaskFinishListener(new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				// TODO Auto-generated method stub
				dismissProgressDialog();
				if (mParser != null && "0".equals(mParser.getCode())) {
					if (taskResult.retObj != null && taskResult.retObj instanceof String) {
						String webUrl = (String) taskResult.retObj;
						LogUtil.d("RechargeTest", "webUrl=" + webUrl);
						RechargeCenterActivity.launch(sContext, webUrl);
						// if (listener != null) {
						// listener.onTaskFinished(taskResult);
						// }
					}
				} else {
					//
					mHandler.sendEmptyMessage(MSG_ERROR_TOAST);
				}
			}
		});
		mReqTask.execute(params);
	}

	private void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new CustomProDialog(sContext);

			mProgressDialog.setCancelable(true);
			mProgressDialog.setOnCancelListener(this);

			mProgressDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					mProgressDialog = null;
				}
			});
		}

		mProgressDialog.show();
	}

	private void dismissProgressDialog() {
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_ERROR_TOAST:
			Toast.makeText(sContext, "数据获取失败，请检查网络", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		if (null != mReqTask) {
			mReqTask.abort();
			mReqTask = null;
		}
	}

}
