package com.sina.book.useraction;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.sina.book.data.ConstantData;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;

/**
 * 设备信息
 * 
 * @author MarkMjw
 * @date 2013-4-25
 */
public class DeviceInfo {
	// private static final String TAG = "DeviceInfo";

	public static final String REPLACE_UDID = "REPLACE_UDID";

	public synchronized static String getUDID() {
		return OpenUDIDManager.isInitialized() ? OpenUDIDManager.getOpenUDID() : REPLACE_UDID;
	}

	public static String getOS() {
		return "Android";
	}

	public static String getOSVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public static String getDevice() {
		return android.os.Build.MODEL;
	}

	public static String getResolution(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		Display display = wm.getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		return metrics.heightPixels + "x" + metrics.widthPixels;
	}

	public static String getCarrier(Context context) {
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String subscriber = "N/A";
		try {
			subscriber = manager.getSubscriberId();
			String operatorName = manager.getSimOperatorName();
			if (subscriber != null) {
				if (subscriber.startsWith("46000") || subscriber.startsWith("46002") || subscriber.startsWith("46007")) {
					subscriber = "中国移动";
				} else if (subscriber.startsWith("46001")) {
					subscriber = "中国联通";
				} else if (subscriber.startsWith("46003")) {
					subscriber = "中国电信";
				} else if (operatorName != null) {
					if ("CMCC".equalsIgnoreCase(operatorName)) {
						subscriber = "中国移动";
					} else if ("China Unicom".equalsIgnoreCase(operatorName)) {
						subscriber = "中国联通";
					} else if ("China Telecom".equalsIgnoreCase(operatorName)) {
						subscriber = "中国电信";
					} else {
						subscriber = "N/A";
					}
				} else {
					subscriber = "N/A";
				}
			}else{
				subscriber = "N/A";
			}
			subscriber = URLEncoder.encode(subscriber, "UTF-8");
//			if (subscriber == null || subscriber.equals("N/A")) {
//				return URLEncoder.encode("N/A", "UTF-8");
//			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return subscriber;
	}

	public static String getLocale() {
		Locale locale = Locale.getDefault();
		return locale.getLanguage() + "_" + locale.getCountry();
	}

	public static String appVersion(Context context) {
		String result = "1.0";
		try {
			result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException ignored) {

		}

		return result;
	}

	public static int appChannel(Context context) {
		return ConstantData.getChannelCode(context);
	}

	public static String getMetrics(Context context) {
		String result = "{";

		result += "\"" + "device" + "\"" + ":" + "\"" + getDevice() + "\"";

		result += "," + "\"" + "os" + "\"" + ":" + "\"" + getOS() + "\"";

		result += "," + "\"" + "os_version" + "\"" + ":" + "\"" + getOSVersion() + "\"";

		result += "," + "\"" + "carrier" + "\"" + ":" + "\"" + getCarrier(context) + "\"";

		result += "," + "\"" + "resolution" + "\"" + ":" + "\"" + getResolution(context) + "\"";

		result += "," + "\"" + "locale" + "\"" + ":" + "\"" + getLocale() + "\"";

		result += "," + "\"" + "app_version" + "\"" + ":" + "\"" + appVersion(context) + "\"";

		result += "," + "\"" + "app_channel" + "\"" + ":" + "\"" + appChannel(context) + "\"";

		String apn_access = HttpUtil.getNetworkType(context);
		LogUtil.d("UserActionEventCount", "DeviceInfo -> apn_access=" + apn_access);

		result += "," + "\"" + "apn_access" + "\"" + ":" + "\"" + apn_access + "\"";

		result += "," + "\"" + "phone_imei" + "\"" + ":" + "\"" + ConstantData.getDeviceId() + "\"";

		// result += "," + "\"" + "device_id" + "\"" + ":" + "\"" + getUDID() +
		// "\"";

		result += "}";

		// LogUtil.d(TAG, "DeviceInfo -> " + result);

		// try {
		// result = java.net.URLEncoder.encode(result, HTTP.UTF_8);
		// } catch (UnsupportedEncodingException e) {
		//
		// }

		return result;
	}
}
