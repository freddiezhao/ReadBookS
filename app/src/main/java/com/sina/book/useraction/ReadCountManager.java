package com.sina.book.useraction;

import android.content.Context;

import com.sina.book.util.LogUtil;

public class ReadCountManager {

	// 阅读页新增统计章节阅读行为的统计
	// class ChapterReadCountInfo {
	//
	// private String bookId;
	// private String chapterId;
	// private String eventType;
	// private String eventExtra;
	//
	// public ChapterReadCountInfo(String bookId, String chapterId, String
	// eventType, String eventExtra) {
	// this.bookId = bookId;
	// this.chapterId = chapterId;
	// this.eventType = eventType;
	// this.eventExtra = eventExtra;
	// }
	//
	// }

	// private HashMap<String, ChapterReadCountInfo> mChapterReadCountList = new
	// HashMap<String, ReadCountManager.ChapterReadCountInfo>();

	private static ReadCountManager sInstance;
	private static Context sContext;

	// private final String PREF_NAME = "read_count_file";
	// private SharedPreferences mSharePref;
	// private Editor mEditor;

	private ReadCountManager() {
		//
		// mSharePref =
		// SinaBookApplication.gContext.getSharedPreferences(PREF_NAME,
		// Context.MODE_PRIVATE);
		// mEditor = mSharePref.edit();
		/*
		 * // 初始化存储在SharedPreferences中的未发送完的数据 Map<String, ?> maps =
		 * mSharePref.getAll(); Set<String> keySet = maps.keySet();
		 * Iterator<String> keys = keySet.iterator(); while (keys.hasNext()) {
		 * // （key=书籍ID_章节ID，value=事件类型_附带信息） String key = keys.next(); Object
		 * value = maps.get(key); LogUtil.d("ReadChapterCount",
		 * "ReadCountManager >> 初始化 >> 读取信息 {key=" + key + ", value=" + value +
		 * "}"); String unionStr = key + "_" + value; String[] strs =
		 * unionStr.split("_"); if (strs != null && strs.length == 4) { String
		 * bookId = strs[0]; String chapterId = strs[1]; String eventType =
		 * strs[2]; String eventExtra = strs[3]; ChapterReadCountInfo info = new
		 * ChapterReadCountInfo(bookId, chapterId, eventType, eventExtra);
		 * String lKey = bookId + "_" + chapterId;
		 * mChapterReadCountList.put(lKey, info); // 发送 String rEventKey =
		 * "阅读器_" + bookId + "_" + chapterId + "_" + eventType; String
		 * rEventExtra = eventExtra; LogUtil.d("ReadChapterCount",
		 * "ReadCountManager >> 初始化 >> 添加统计 {rEventKey=" + rEventKey +
		 * ", rEventExtra=" + rEventExtra + "}");
		 * UserActionManager.getInstance().recordEventNew(rEventKey,
		 * rEventExtra); } }
		 */
	}

	public static ReadCountManager getInstance(Context context) {
		sContext = context;
		if (sInstance == null) {
			synchronized (ReadCountManager.class) {
				if (sInstance == null) {
					sInstance = new ReadCountManager();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 添加新的统计事件
	 * 
	 * @param bookId
	 * @param chapterId
	 * @param eventType
	 * @param eventExtra
	 */
	public void addNewCount(String bookId, String chapterId, String eventType, String eventExtra) {
		// 检查
		// String sEventType = checkChapterEventType(bookId, chapterId,
		// eventType);
		// String sStoreKey = bookId + "_" + chapterId;
		// String sStoreValue = sEventType + "_" + eventExtra;
		/*
		 * // 存储（key=书籍ID_章节ID，value=事件类型_附带信息） // 文件 saveString(sStoreKey,
		 * sStoreValue); // 内存 ChapterReadCountInfo info = new
		 * ChapterReadCountInfo(bookId, chapterId, sEventType, eventExtra);
		 * String lKey = bookId + "_" + chapterId;
		 * mChapterReadCountList.put(lKey, info);
		 */
		// 发送
		String rEventKey = "阅读器_" + bookId + "_" + chapterId + "_" + eventType;
		String rEventExtra = eventExtra;
		LogUtil.d("ReadChapterCount", "ReadCountManager >> addNewCount >> 添加统计 {rEventKey=" + rEventKey
				+ ", rEventExtra=" + rEventExtra + "}");
		UserActionManager.getInstance().recordEventNew(rEventKey, "book_read", rEventExtra);
	}

	// private boolean checkChapterExsits(String bookId, String chapterId) {
	// if (bookId != null && chapterId != null) {
	// String storeKey = bookId + "_" + chapterId;
	// if (mChapterReadCountList.containsKey(storeKey)) {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// private String checkChapterEventType(String bookId, String chapterId,
	// String eventType) {
	// if (checkChapterExsits(bookId, chapterId)) {
	// return "r" + eventType;
	// }
	// return eventType;
	// }

	/**
	 * 保存指定Key对应的String
	 * 
	 * @param key
	 *            关键字
	 * @param value
	 *            值
	 */
	// public void saveString(String key, String value) {
	// SharedPreferences preferences =
	// SinaBookApplication.gContext.getSharedPreferences(PREF_NAME,
	// Context.MODE_PRIVATE);
	// Editor editor = preferences.edit();
	// editor.putString(key, value);
	// editor.commit();
	// }

	/**
	 * 删除指定Key对应的String
	 * 
	 * @param key
	 *            关键字
	 * @param value
	 *            值
	 */
	// public void removeString(String key) {
	// // 移除本地
	// SharedPreferences preferences =
	// SinaBookApplication.gContext.getSharedPreferences(PREF_NAME,
	// Context.MODE_PRIVATE);
	// Editor editor = preferences.edit();
	// editor.remove(key);
	// editor.commit();
	// // 移除内存
	// if (mChapterReadCountList != null) {
	// if (mChapterReadCountList.containsKey(key)) {
	// mChapterReadCountList.remove(key);
	// }
	// }
	// }

	/**
	 * 获取指定Key对应的字符串值,有默认值
	 * 
	 * @param key
	 *            关键字
	 * @param defaultValue
	 *            默认值
	 * @return key对应的value
	 */
	// public String getString(String key, String defaultValue) {
	// SharedPreferences preferences =
	// SinaBookApplication.gContext.getSharedPreferences(PREF_NAME,
	// Context.MODE_PRIVATE);
	// return preferences.getString(key, defaultValue);
	// }
}
