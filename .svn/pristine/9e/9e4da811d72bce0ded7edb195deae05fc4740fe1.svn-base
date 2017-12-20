package com.sina.book.util;

import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtil {

	public static ViewUtil i;

	private ViewUtil() {

	}

	static {
		i = new ViewUtil();
	}

	public void checkHardwareAcceleratedRecursive(View view) {
		checkHardwareAcceleratedRecursive(view, false);
	}

	public void checkHardwareAcceleratedRecursive(View view,
			boolean ignoreCompareVersion) {
		if (view != null || ignoreCompareVersion && Build.VERSION.SDK_INT >= 11) {
			if (view instanceof ViewGroup) {
				int childCount = ((ViewGroup) view).getChildCount();
				for (int i = 0; i < childCount; i++) {
					checkHardwareAcceleratedRecursive(
							((ViewGroup) view).getChildAt(i), true);
				}
			} else {
				checkHardwareAccelerated(view, true);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void checkHardwareAccelerated(View view, boolean ignoreCompareVersion) {
		// 3.0后关闭硬件加速
		if (ignoreCompareVersion || Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			try {
				Method layerMethod = View.class.getMethod("setLayerType",
						int.class, Paint.class);
				int type = Integer.parseInt(View.class
						.getField("LAYER_TYPE_SOFTWARE").get(null).toString());
				if (layerMethod != null) {
					layerMethod.invoke(view, type, null);
				}
			} catch (Exception e) {
				// no problem
				view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
	}

}
