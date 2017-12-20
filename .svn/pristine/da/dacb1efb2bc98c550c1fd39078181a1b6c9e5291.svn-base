package com.sina.book.data.util;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import org.apache.http.HttpStatus;

import android.content.Context;

import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ConstantData;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

/**
 * 已安装的设备统计工具类
 * 
 * @author chenjianli
 * 
 */
public class InstalledDeviceCountUtil {

	public static final String RECORD_KEY = "InstalledDevice";
	private final String PREF_KEY = "lastCountTime";

	private static InstalledDeviceCountUtil sInstance;
	private static Context sContext;

	private InstalledDeviceCountUtil(Context context) {
		sContext = context;
		// 初始化行为统计
		UserActionManager.getInstance().init(context, ConstantData.USER_ACTION_URL, ConstantData.USER_ACTION_APP_KEY);
	}

	public static InstalledDeviceCountUtil getInstance(Context context) {

		if (sInstance == null) {
			synchronized (InstalledDeviceCountUtil.class) {
				if (sInstance == null) {
					sInstance = new InstalledDeviceCountUtil(context);
				}
			}
		}
		return sInstance;
	}

	public void check() {

		// 获取最后一次成功上传统计数据的时间
		long lastCountTime = StorageUtil.getLong(PREF_KEY, 0);
		LogUtil.d("InstalledDeviceCountLog", "InstalledDeviceCountUtil >> check >> lastCountTime=" + lastCountTime);
		// 首次安装或者程序文件被用户清除掉的情况
		if (lastCountTime <= 0) {
			// 发送统计请求
			count(System.currentTimeMillis());
		} else {
			// 比较时间
			long nowTime = System.currentTimeMillis();

			LogUtil.d("InstalledDeviceCountLog", "InstalledDeviceCountUtil >> check >> nowTime=" + nowTime);

			// 不同的一天
			if (!getFormatTime(lastCountTime).equals(getFormatTime(nowTime))) {
				// 发送统计请求
				count(nowTime);
			}
		}
	}

	private String getFormatTime(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		Formatter ft = new Formatter(Locale.CHINA);
		String formatStr = ft.format("%1$tY%1$tm%1$td", cal).toString();
		LogUtil.d("InstalledDeviceCountLog", "InstalledDeviceCountUtil >> count >> millis=" + millis + ", formatStr="
				+ formatStr);
		return formatStr;
	}

	/** 是否正在请求中 */
	private boolean isRequesting = false;

	private void count(final long countTime) {
		boolean isNetWorking = HttpUtil.isConnected(sContext);
		LogUtil.d("InstalledDeviceCountLog", "InstalledDeviceCountUtil >> count >> countTime=" + countTime);
		LogUtil.d("InstalledDeviceCountLog", "InstalledDeviceCountUtil >> count >> isNetWorking=" + isNetWorking);
		LogUtil.d("InstalledDeviceCountLog", "InstalledDeviceCountUtil >> count >> isRequesting=" + isRequesting);

		// 判断网络连接情况
		if (isNetWorking && !isRequesting) {
			isRequesting = true;
			UserActionManager.getInstance().recordInstalledDevice(new ITaskFinishListener() {

				@Override
				public void onTaskFinished(TaskResult taskResult) {
					// TODO Auto-generated method stub

					LogUtil.d("InstalledDeviceCountLog",
							"InstalledDeviceCountUtil >> count >> onTaskFinished >> taskResult=" + taskResult);

					isRequesting = false;
					if (taskResult != null
							&& (taskResult.stateCode == HttpStatus.SC_OK || taskResult.stateCode == HttpStatus.SC_PARTIAL_CONTENT)) {

						LogUtil.i("InstalledDeviceCountLog",
								"InstalledDeviceCountUtil >> count >> onTaskFinished >> saveLong >> countTime="
										+ countTime + " and listener set null.");

						StorageUtil.saveLong(PREF_KEY, countTime);
						// 当天成功上传记录后，将监听器置空，从而阻止遍历检查每次统计请求的结果
						UserActionManager.getInstance().setConnectionQueueListener(null);
					}
				}
			});
		}
	}

}
