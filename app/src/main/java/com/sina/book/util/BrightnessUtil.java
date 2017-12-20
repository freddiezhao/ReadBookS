package com.sina.book.util;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.WindowManager;

/**
 * 系统设置工具类
 * 
 * @author MarkMjw
 *
 */
public class BrightnessUtil {
	private final static String TAG = "SystemUtil";
	
	/**
	 * 是否是自动调节亮度
	 * 
	 * @param context
	 * @return true if the system is auto adjust brightness. or false; 
	 * @see Settings.System#SCREEN_BRIGHTNESS_MODE_AUTOMATIC
	 */
	public static boolean isAutoBrightness(Context context) {
		boolean automicBrightness = false;
		try {
			automicBrightness = Settings.System.getInt(context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE) == 
					Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			LogUtil.e(TAG, e.getMessage());
		}
		return automicBrightness;
	}
	
	/**
     * 获取调节亮度模式
     * 
     * @param context
     * @return  the system adjust brightness mode; 
     * @see Settings.System#SCREEN_BRIGHTNESS_MODE_AUTOMATIC
     * @see Settings.System#SCREEN_BRIGHTNESS_MODE_MANUAL
     */
    public static int getBrightnessMode(Context context) {
        int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        
        try {
            mode = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException e) {
            LogUtil.e(TAG, e.getMessage());
        }
        
        return mode;
    }
	
	/**
	 * 设置系统亮度调节模式（自动or手动）
	 * 
	 * @param context
	 * @param mode SCREEN_BRIGHTNESS_MODE_AUTOMATIC or SCREEN_BRIGHTNESS_MODE_MANUAL
	 * @see Settings.System#SCREEN_BRIGHTNESS_MODE_AUTOMATIC
	 * @see Settings.System#SCREEN_BRIGHTNESS_MODE_MANUAL
	 */
	public static void setScreenBrightnessMode(Context context, int mode) {
		Settings.System.putInt(context.getContentResolver(), 
				Settings.System.SCREEN_BRIGHTNESS_MODE, 
				mode);
	}
	
	/**
	 * 设置系统亮度
	 * 
	 * @param context
	 * @param value 20~255
	 */
	public static void setScreenBrightnessValue(Context context, float value) {
		int brightness = (int) (value * 255.0f  / 100);
		if(brightness < 20) {
			brightness = 20;
		}
		if(brightness > 255) {
			brightness = 255;
		}
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, brightness);
	}
	
	/**
	 * 获取当前系统设置屏幕亮度值
	 * 
	 * @param context
	 * @return
	 */
	public static float getScreenBrightnewssValue(Context context) {
		float brightness = 0;
		try {
			brightness = Settings.System.getFloat(context.getContentResolver(), 
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			LogUtil.e(TAG, e.getMessage());
		}
		return brightness;
	}
	
	/**
	 * 方法并不会修改系统设置里的亮度设置，它仅仅是在调用这个方法的Activity显示时调整屏幕亮度，
	 * 当退出Activity时，屏幕亮度会恢复原来系统设置的值
	 * 
	 * @param activity 当前Activity
	 * @param value 亮度值
	 */
	public static void setCurrentScreenBrightness(Activity activity, int value) {
		int brightness = (int) (value * 255.0f  / 100);
		if(brightness < 20) {
			brightness = 20;
		}
		if(brightness > 255) {
			brightness = 255;
		}
		
		WindowManager.LayoutParams wl = activity.getWindow().getAttributes();
		float tmpFloat = (float) brightness / 255.0f;
		if (tmpFloat > 0 && tmpFloat <= 1) {
			wl.screenBrightness = tmpFloat;
		}
		activity.getWindow().setAttributes(wl);
	}
	
	/**
     * 设为默认亮度
     * 
     * @param activity 当前Activity
     * @param value 亮度值
     */
    public static void setCurrentScreenDefault(Activity activity) {
        WindowManager.LayoutParams wl = activity.getWindow().getAttributes();
        wl.screenBrightness = -1;
        activity.getWindow().setAttributes(wl);
    }
	
	/**
     * 获取当前系统设置屏幕亮度值
     * 
     * @param context
     * @return
     */
    public static float getCurrentScreenBrightness(Activity activity) {
        
        WindowManager.LayoutParams wl = activity.getWindow().getAttributes();
        return wl.screenBrightness * 255.0f;
    }
}
