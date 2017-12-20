package com.sina.book.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 程序相关工具类<br>
 * 如获取程序名称，程序版本名称，程序版本号等
 * 
 * @author chenjianli
 * 
 */
public class ApplicationUtils {

	/**
	 * 获取包信息类对象
	 * 
	 * @param context
	 *            上下文对象
	 * @return 返回包信息类对象，可以从中获取到版本号(versionName)，包名(packageName)等信息
	 */
	public static PackageInfo getPackageInfo(Context context) {
		PackageInfo info = null;
		if (context != null) {
			String packName = context.getPackageName();
			PackageManager pm = context.getPackageManager();
			try {
				info = pm.getPackageInfo(packName, PackageManager.GET_PERMISSIONS);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			} finally {

			}
		}
		return info;
	}

	public static String getVersionName(Context context) {
		if (context == null)
			return null;

		return getPackageInfo(context).versionName;

		// String version = getPackageInfo(context).versionName;
		//
		// // 这里需要将版本名称转换成浮点数然后给服务器
		// int dotIndex = version.indexOf(".");
		// if (dotIndex > 0 && dotIndex < version.length() - 1) {
		// String versionBegin = version.substring(0, dotIndex);
		// String versionEnd = version.substring(dotIndex);
		//
		// // 将主版本号加1(方便微博读书和新浪读书区分开)
		// int versionBeginInt = Integer.parseInt(versionBegin);
		// versionBeginInt++;
		// versionBegin = String.valueOf(versionBeginInt);
		//
		// version = versionBegin + versionEnd;
		// }
		// return version;
	}

	public static int getVersionCode(Context context) {
		if (context == null)
			return 1;
		return getPackageInfo(context).versionCode;
	}

	/**
	 * 获取程序的名称
	 * 
	 * @param context
	 *            上下文对象
	 * @return 返回程序名称
	 */
	public static String getApplicationName(Context context) {
		String appName = null;
		if (context != null) {
			String packName = context.getPackageName();
			PackageManager pm = context.getPackageManager();
			try {
				ApplicationInfo ai = pm.getApplicationInfo(packName, ApplicationInfo.FLAG_SYSTEM);
				appName = (String) pm.getApplicationLabel(ai);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return appName;
	}

}
