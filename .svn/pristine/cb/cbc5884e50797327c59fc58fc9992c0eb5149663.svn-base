package org.geometerplus.android.util;

import android.util.Log;

/**
 * 可单独控制每个Tag的打印与否，也可通过全局变量来统一控制
 * 
 * @author chenjl
 * 
 */
public class ZLog
{
	/**
	 * 全局控制是否打印的变量
	 */
	public static final boolean	PRINT_OR_NOT				= true;

	public static final String	PACKAGE_NAME				= "org.geometerplus.zlibrary.ui.android";

	public static final LogInfo	ZLTextPage					= get("ZLTextPage", true);
	public static final LogInfo	ZLTextView					= get("ZLTextView", true);
	public static final LogInfo	NavigationPopup				= get("NavigationPopup", true);
	public static final LogInfo	ZLAndroidWidget				= get("ZLAndroidWidget", true);
	public static final LogInfo	FBReader					= get("FBReader", true);
	public static final LogInfo	FBReaderSync				= get("FBReader.Sync", true);
	public static final LogInfo	DeleteBook					= get("DeleteBook", true);
	public static final LogInfo	DownloadBookNotification	= get("DownloadBookNotification", false);
	public static final LogInfo	EpubReadPullDownAction		= get("EpubReadPullDownAction", true);
	public static final LogInfo	DownProgressUpdate			= get("DownProgressUpdate", true);
	public static final LogInfo	Cookies						= get("Cookies", true);
	public static final LogInfo	FileMissingLog				= get("FileMissingLog", true);
	public static final LogInfo	ZLDebug						= get("ZLDebug", true);
	public static final LogInfo	ZLTextIndent				= get("ZLTextIndent", true);
	public static final LogInfo	ZLFont						= get("ZLFont", true);
	public static final LogInfo	ZLCSS_DIV_BG				= get("ZLCSS_DIV_BG", true);

	public static class LogInfo
	{
		/**
		 * 打印Log的Tag
		 */
		public String	tag;
		/**
		 * 是否打印Log
		 */
		public boolean	print;

		public LogInfo(String tag, boolean print)
		{
			this.tag = tag;
			this.print = print;
		}
	}

	public static LogInfo get(String tag, boolean print)
	{
		return new LogInfo(tag, print);
	}

	public static void d(LogInfo logInfo, String log)
	{
		// 全局是关的话直接pass
		if (PRINT_OR_NOT) {
			if (logInfo != null) {
				if (logInfo.print) {
					Log.d(logInfo.tag, log);
				}
			} else {
				Log.d(PACKAGE_NAME, log);
			}
		}
	}

	public static void e(LogInfo logInfo, String log)
	{
		if (PRINT_OR_NOT) {
			if (logInfo != null) {
				if (logInfo.print) {
					Log.e(logInfo.tag, log);
				}
			} else {
				Log.e(PACKAGE_NAME, log);
			}
		}
	}

	public static void v(LogInfo logInfo, String log)
	{
		if (PRINT_OR_NOT) {
			if (logInfo != null) {
				if (logInfo.print) {
					Log.v(logInfo.tag, log);
				}
			} else {
				Log.v(PACKAGE_NAME, log);
			}
		}
	}

	public static void i(LogInfo logInfo, String log)
	{
		if (PRINT_OR_NOT) {
			if (logInfo != null) {
				if (logInfo.print) {
					Log.i(logInfo.tag, log);
				}
			} else {
				Log.i(PACKAGE_NAME, log);
			}
		}
	}

	public static void w(LogInfo logInfo, String log)
	{
		if (PRINT_OR_NOT) {
			if (logInfo != null) {
				if (logInfo.print) {
					Log.w(logInfo.tag, log);
				}
			} else {
				Log.w(PACKAGE_NAME, log);
			}
		}
	}

	public static void d(String log)
	{
		d(null, log);
	}

	public static void e(String log)
	{
		e(null, log);
	}

	public static void v(String log)
	{
		v(null, log);
	}

	public static void i(String log)
	{
		i(null, log);
	}

	public static void w(String log)
	{
		w(null, log);
	}
}