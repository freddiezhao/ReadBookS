package com.sina.book.ui.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sina.book.data.util.InstalledDeviceCountUtil;

public class BootCompletedAndNetChangeReceiver extends BroadcastReceiver {

	// private final String BOOT_COMPLETE_ACTION =
	// "android.intent.action.BOOT_COMPLETED";
	// 网络变化
	private final String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	// 解锁手机
	private final String USER_PRESENT_ACTION = "android.intent.action.USER_PRESENT";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent != null) {
			String action = intent.getAction();

			// LogUtil.d("InstalledDeviceCountLog",
			// "BootCompletedAndNetChangeReceiver >> onReceive >> action=" +
			// action);

			// 其实不用监听BOOT_COMPLETE_ACTION事件。
			// 因为在手机开机的时候，如果之前关机时网络连接是打开的状态(如移动网络开启或者Wifi是开启的)
			// 则开机时会先调用BOOT_COMPLETE_ACTION广播，之后再调用NET_CHANGE_ACTION广播
			// 后续的网络开关变化都会被NET_CHANGE_ACTION广播出来。
			// 因此这里只需要监听NET_CHANGE_ACTION即可。

			// 强大的监听：监听解锁
			// 每个手机基本上都有解锁的行为，所以无论用户在什么时候使用手机
			// 只要发生解锁行为，就发送统计，具体的判断由统计方法来判断。

			// if (BOOT_COMPLETE_ACTION.equals(action)) {
			// InstalledDeviceCountUtil.getInstance(context).check();
			// } else

			if (NET_CHANGE_ACTION.equals(action) || USER_PRESENT_ACTION.equals(action)) {
				InstalledDeviceCountUtil.getInstance(context).check();
			}
		}
	}

}
