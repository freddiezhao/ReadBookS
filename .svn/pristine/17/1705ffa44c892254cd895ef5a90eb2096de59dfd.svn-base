package com.sina.book.data;

import java.util.ArrayList;

import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.sina.book.SinaBookApplication;
import com.sina.book.util.LogUtil;

public class ScreenInfo {
	private int width;
	private int height;
	private float density;
	private float xdpi;
	private float ydpi;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public float getDensity() {
		return density;
	}

	public void setDensity(float density) {
		this.density = density;
	}

	public float getXdpi() {
		return xdpi;
	}

	public void setXdpi(float xdpi) {
		this.xdpi = xdpi;
	}

	public float getYdpi() {
		return ydpi;
	}

	public void setYdpi(float ydpi) {
		this.ydpi = ydpi;
	}

	@Override
	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append(width);
		message.append("x");
		message.append(height);
		message.append("/");
		message.append(density);
		message.append("/");
		message.append(xdpi);
		return message.toString();
	}

	private static ScreenInfo instance;

	private ScreenInfo() {
		DisplayMetrics dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);
		dm = SinaBookApplication.gContext.getResources().getDisplayMetrics();
		width = dm.widthPixels;
		height = dm.heightPixels;
		density = dm.density;
		xdpi = dm.xdpi;
		ydpi = dm.ydpi;

		levelArray.add(LEVEL_1000);
		levelArray.add(LEVEL_1500);
		levelArray.add(LEVEL_2000);
		levelArray.add(LEVEL_3000);
		levelArray.add(LEVEL_5000);
		levelArray.add(LEVEL_10000);
	}

	public static ScreenInfo getInstance() {
		if (instance == null) {
			synchronized (ScreenInfo.class) {
				if (instance == null) {
					instance = new ScreenInfo();
				}
			}
		}
		return instance;
	}

	private final String[] LEVEL_1000 = { "240x320/1.5/120.0", "480x640/1.5/240.0" };

	private final String[] LEVEL_1500 = { "240x400/0.75/120.0", "240x432/0.75/120.0", "320x480/1.0/160.0",
			"640x960/2.0/320.0", "480x800/1.5/240.0", "480x854/1.5/240.0" };

	private final String[] LEVEL_2000 = { "600x1024/1.5/240.0", "720x1280/2.0/320.0", "768x1280/2.0/320.0",
			"800x1280/2.0/320.0", "720x1184/2.0/315.31033" };

	private final String[] LEVEL_3000 = { "480x800/1/160.0", "480x854/1.0/160.0", "720x1280/1.5/240.0", };

	private final String[] LEVEL_5000 = { "480x800/0.75/120.0", "600x1024/1.0/160.0", "800x1280/1.33/213.0", };

	private final String[] LEVEL_10000 = { "480x854/0.75/120.0", "768x1024/1.0/160.0", "768x1280/1.0/160.0",
			"800x1280/1.0/160.0" };

	private ArrayList<String[]> levelArray = new ArrayList<String[]>();
	private int[] levelLength = new int[] { 1000, 1500, 2000, 3000, 5000, 10000 };

	public static final int DEFAULT_LIMIT_SIZE = 10000;

	private int sinaBookModelLimitSize;

	public int computeSinaBookModelLimitSize() {
		if (sinaBookModelLimitSize <= 0) {
			String screenMsg = toString();
			for (int i = 0; i < levelArray.size(); i++) {
				boolean fetch = false;
				String[] levels = levelArray.get(i);
				for (int j = 0; j < levels.length; j++) {
					String level = levels[j];
					if (!TextUtils.isEmpty(level) && !TextUtils.isEmpty(screenMsg)) {
						if (level.equals(screenMsg)) {
							fetch = true;
							break;
						}
					}
				}
				if (fetch) {
					sinaBookModelLimitSize = levelLength[i];
					break;
				}
			}
		}

		if (sinaBookModelLimitSize <= 0) {
			sinaBookModelLimitSize = DEFAULT_LIMIT_SIZE;
		}
		LogUtil.d("ScreenInfo", "ScreenInfo >> computeSinaBookModelLimitSize >> sinaBookModelLimitSize="
				+ sinaBookModelLimitSize);
		return sinaBookModelLimitSize;
	}

}
