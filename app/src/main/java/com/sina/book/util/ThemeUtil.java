package com.sina.book.util;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.SparseIntArray;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;

/**
 * 主题工具类
 * 
 * @author MarkMjw
 * @date 2013-2-19
 */
public class ThemeUtil {
	// private static final String TAG = "ThemeUtil";

	public static final int DEFAULT_VALUE = -1;

	/** 平铺. */
	public static final int DRAW_REPEAT = 0;
	/** 拉伸. */
	public static final int DRAW_ADJUST = 1;

	private static final int ITEMS = 6;

	private static Context sContext = SinaBookApplication.gContext;

	private static ArrayList<Integer> mThemeBg = new ArrayList<Integer>();
	private static SparseIntArray mThemeBgThumbnail = new SparseIntArray();
	private static SparseIntArray mTextColor = new SparseIntArray();
	private static SparseIntArray mTitleTextColor = new SparseIntArray();
	private static SparseIntArray mBackAreaColor = new SparseIntArray();
	private static SparseIntArray mDrawMethod = new SparseIntArray();

	private static final int[] DRAWABLE_IDS = { R.drawable.readbg_00, R.drawable.readbg_01, R.drawable.readbg_02,
			R.drawable.readbg_03, R.drawable.readbg_04, R.drawable.readbg_05, R.drawable.readbg_06 };

	private static final int[] COLOR_IDS = { R.color.reading_bg };

	static {
		TypedArray array = sContext.getResources().obtainTypedArray(R.array.reading_themes);

		int size = array.length() / ITEMS;
		for (int i = 0; i < size; i++) {
			int bgResId = array.getResourceId(i * ITEMS, DEFAULT_VALUE);
			if (DEFAULT_VALUE == bgResId) {
				continue;
			}

			mThemeBg.add(bgResId);
			mThemeBgThumbnail.put(bgResId, array.getResourceId(i * ITEMS + 1, DEFAULT_VALUE));
			mTextColor.put(bgResId, Color.parseColor(array.getString(i * ITEMS + 2)));
			mTitleTextColor.put(bgResId, Color.parseColor(array.getString(i * ITEMS + 3)));
			mBackAreaColor.put(bgResId, Color.parseColor(array.getString(i * ITEMS + 4)));
			mDrawMethod.put(bgResId, array.getInt(i * ITEMS + 5, DRAW_REPEAT));
		}

		array.recycle();
	}

	/**
	 * 获取背景图片资源ID列表
	 * 
	 * @return
	 */
	public static ArrayList<Integer> getBgResIdList() {
		return mThemeBg;
	}

	/**
	 * 获取背景图片资源ID列表
	 * 
	 * @return
	 */
	public static SparseIntArray getBgThumbnailResIdList() {
		return mThemeBgThumbnail;
	}

	/**
	 * 获取背景缩略图图片ID
	 * 
	 * @param bgResId
	 *            背景缩略图图片资源ID
	 * @return
	 */
	public static int getThumbnail(int bgResId) {
		return mThemeBgThumbnail.get(bgResId, DEFAULT_VALUE);
	}

	/**
	 * 获取文字颜色
	 * 
	 * @param bgResId
	 *            背景图片资源ID
	 * @return
	 */
	public static int getTextColor(int bgResId) {
		return mTextColor.get(bgResId, DEFAULT_VALUE);
	}

	/**
	 * 获取标题文字颜色
	 * 
	 * @param bgResId
	 *            背景图片资源ID
	 * @return
	 */
	public static int getTitleTextColor(int bgResId) {
		return mTitleTextColor.get(bgResId, DEFAULT_VALUE);
	}

	/**
	 * 获取背面的颜色
	 * 
	 * @param bgResId
	 *            背景图片资源ID
	 * @return
	 */
	public static int getBackAreaColor(int bgResId) {
		return mBackAreaColor.get(bgResId, DEFAULT_VALUE);
	}

	/**
	 * 获取背景图片绘制方式
	 * 
	 * @param bgResId
	 *            背景图片资源ID
	 * @return
	 */
	public static int getDrawMethod(int bgResId) {
		return mDrawMethod.get(bgResId, DRAW_REPEAT);
	}

	/**
	 * 指定资源ID是否是Drawable类型
	 * 
	 * @param bgResId
	 * @return
	 */
	public static boolean isDrawable(int bgResId) {
		boolean isDrawable = false;

		for (int id : DRAWABLE_IDS) {
			if (id == bgResId) {
				isDrawable = true;
				break;
			}
		}

		return isDrawable;
	}

	/**
	 * 指定资源ID是否是Color类型
	 * 
	 * @param bgResId
	 * @return
	 */
	public static boolean isColor(int bgResId) {
		boolean isColor = false;

		for (int id : COLOR_IDS) {
			if (id == bgResId) {
				isColor = true;
				break;
			}
		}

		return isColor;
	}
}
