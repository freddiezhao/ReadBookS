package com.sina.book.data;

import java.io.Serializable;

import org.json.JSONObject;

import com.sina.book.util.LogUtil;
import com.sina.book.util.Util;

/**
 * 书籍阅读状态
 * 
 * @author MarkMjw
 * @date 2013-2-4
 */
public class BookReadInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 上次阅读到的位置. */
	private int mLastPos;

	/** 上次阅读的时间. */
	private long mLastReadTime;

	/** 上次阅读的百分比. */
	private float mLastReadPercent = 0.0f;

	/** JSON形式存取，通过章节id的方式存取上次阅读信息，用作下载等恢复 */
	private String mLastReadJsonString;

	public void setLastReadInfo(int lastPos, int lastChapterPos, int lastGlobalChapterId) {
		this.mLastPos = lastPos;
		if (lastGlobalChapterId >= 0) {
			try {

				JSONObject lastReadObj = new JSONObject();
				lastReadObj.put("lastPosInChapter", lastPos - lastChapterPos);
				lastReadObj.put("lastGlobalChapterId", lastGlobalChapterId);
				mLastReadJsonString = lastReadObj.toString();

				LogUtil.d("SQLiteUpdateErrorLog", "BookReadInfo >> BookReadInfo >> mLastReadJsonString="
						+ mLastReadJsonString);

			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public void parseLastChapterId(Book relateBook) {
		if (!Util.isNullOrEmpty(mLastReadJsonString) && relateBook != null && relateBook.getChapters() != null
				&& relateBook.getChapters().size() > 0) {
			try {
				JSONObject lastReadObj = new JSONObject(mLastReadJsonString);
				// int lastPosInChapter =
				// lastReadObj.optInt("lastPosInChapter");
				int lastGlobalChapterId = lastReadObj.optInt("lastGlobalChapterId");
				if (lastGlobalChapterId > 0) {
					if (relateBook.isOnlineBook()) {
						relateBook.setOnlineReadChapterId(lastGlobalChapterId, "ReadActivity-online");
					}
				}
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public boolean parsePosFromJson(Book relateBook) {
		if (!Util.isNullOrEmpty(mLastReadJsonString) && relateBook != null && relateBook.getChapters() != null
				&& relateBook.getChapters().size() > 0) {
			try {
				JSONObject lastReadObj = new JSONObject(mLastReadJsonString);
				int lastPosInChapter = lastReadObj.optInt("lastPosInChapter");
				int lastGlobalChapterId = lastReadObj.optInt("lastGlobalChapterId");
				if (lastGlobalChapterId > 0) {
					Chapter relateChapter = relateBook.getCurrentChapterById(lastGlobalChapterId);
					if (relateChapter != null && relateChapter.getLength() > 0) {
						mLastPos = lastPosInChapter + Long.valueOf(relateChapter.getStartPos()).intValue();
					}
				} else if (lastGlobalChapterId == 0) {
					mLastPos = lastPosInChapter;
				}

				if (relateBook.getDownloadInfo().getFileSize() < mLastPos) {
					mLastPos = 0;
				}
				return true;
			} catch (Exception e) {
				// do nothing
			}
		}

		mLastPos = 0;
		return false;
	}

	public void setLastReadJsonString(String lastReadJsonString, String log) {
		mLastReadJsonString = lastReadJsonString;
		LogUtil.d("SQLiteUpdateErrorLog", "BookReadInfo >> setLastReadJsonString >> mLastReadJsonString="
				+ mLastReadJsonString + ", log=" + log);

	}

	public String getLastReadJsonString() {
		return mLastReadJsonString;
	}

	public int getLastPos() {
		return mLastPos;
	}

	public void setLastPos(int lastPos) {
		this.mLastPos = lastPos;
	}

	public long getLastReadTime() {
		return mLastReadTime;
	}

	public void setLastReadTime(long lastReadTime) {
		this.mLastReadTime = lastReadTime;
	}

	public float getLastReadPercent() {
		return mLastReadPercent;
	}

	public void setLastReadPercent(float lastReadPercent) {
		this.mLastReadPercent = lastReadPercent;
	}

	@Override
	public String toString() {
		return "BookReadInfo{" + "(阅读位置)mLastPos=" + mLastPos + ", (阅读时间)mLastReadTime=" + mLastReadTime
				+ ", (阅读进度)mLastReadPercent=" + mLastReadPercent + '}';
	}
}
