package com.sina.book.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.book.SinaBookApplication;

public class GlobalToast implements Callback
{

	// 正常的弹出Toast
	public final int		MSG_NORMAL	= 0x1;
	// 将来有可能需要自定义Toast
	public final int		MSG_CUSTOM	= 0x2;

	private static Handler	handler;

	private GlobalToast()
	{
		handler = new Handler(Looper.getMainLooper(), this);
	}

	public static GlobalToast	i;

	static {
		i = new GlobalToast();
	}

	public void toast(String text)
	{
		Message msg = handler.obtainMessage();
		msg.obj = text;
		msg.what = MSG_NORMAL;
		handler.sendMessage(msg);
	}

	public void toast(Activity context, final String text)
	{
		context.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(SinaBookApplication.gContext, text, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
		case MSG_NORMAL:
			if (msg.obj != null && msg.obj instanceof String) {
				String toast = (String) msg.obj;
				if (!TextUtils.isEmpty(toast)) {
					Toast.makeText(SinaBookApplication.gContext, toast, Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		}
		return false;
	}

}