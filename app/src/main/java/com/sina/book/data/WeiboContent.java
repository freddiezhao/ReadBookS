package com.sina.book.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.VDiskSyncManager;
import com.sina.book.parser.BaseParser;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.ResourceUtil;
import com.vdisk.net.exception.VDiskException;

/**
 * 分享微博内容
 * 
 * @author MarkMjw
 * @date 2013-3-21
 */
public class WeiboContent implements Serializable {
	private static final long serialVersionUID = 1238180227489923279L;

	/**
	 * 书籍详情.
	 */
	public static final int TYPE_BOOK_DETAIL = 0;
	/**
	 * 阅读中.
	 */
	public static final int TYPE_READING = 1;
	/**
	 * 赞并发微博.
	 */
	public static final int TYPE_PRAISE = 2;
	/**
	 * 阅读笔记.
	 */
	public static final int TYPE_READ_NOTE = 3;
	/**
	 * 书籍评论.
	 */
	public static final int TYPE_COMMENT = 4;

	/**
	 * 微博最大字数.
	 */
	public static final int WEIBO_TEXT_LENGTH = 120;
	/**
	 * 书籍内容整理正则表达式.
	 */
	public static final String BOOK_CONTENT_PATTERN = "[\\s|　]";

	private Book mBook;

	private int mChapterId = Chapter.DEFAULT_GLOBAL_ID;
	private int mChapterOffset = 0;

	private String mMsg = "";
	private String mImagePath = "";

	private int mType;

	public WeiboContent(Book book, int type) {
		this.mBook = book;
		this.mType = type;
	}

	public Book getBook() {
		return mBook;
	}

	public int getType() {
		return mType;
	}

	public int getChapterId() {
		return mChapterId;
	}

	public void setChapterId(int chapterId) {
		this.mChapterId = chapterId;
	}

	public int getChapterOffset() {
		return mChapterOffset;
	}

	public void setChapterOffset(int chapterOffset) {
		this.mChapterOffset = chapterOffset;
	}

	public void setImagePath(String path) {
		mImagePath = path;
	}

	public String getImagePath() {
		return mImagePath;
	}

	public String getMsg() {
		return mMsg;
	}

	public void setMsg(String msg) {
		mMsg = msg;

		formatMsg();
	}

	/**
	 * 格式化消息内容
	 */
	private void formatMsg() {
		if (!TextUtils.isEmpty(mMsg) && null != mBook) {

			String title = mBook.getTitle();

			if (mBook.isOurServerBook()) {

				switch (mType) {
				case TYPE_BOOK_DETAIL:
					int n = getMsgLength(R.string.share_book, 2) - title.length();
					mMsg = String.format(ResourceUtil.getString(R.string.share_book), getWeiboString(mMsg, n), title);
					break;

				case TYPE_PRAISE:
				case TYPE_READING:
					int n1 = getMsgLength(R.string.share_book, 2) - title.length();
					mMsg = String.format(ResourceUtil.getString(R.string.share_book), getWeiboString(mMsg, n1), title);
					break;

				case TYPE_READ_NOTE:
					int n2 = getMsgLength(R.string.share_summary, 2) - title.length();
					mMsg = String.format(ResourceUtil.getString(R.string.share_summary), getWeiboString(mMsg, n2),
							title);
					break;

				case TYPE_COMMENT:
					int n3 = getMsgLength(R.string.share_comment, 2) - title.length();
					mMsg = String.format(ResourceUtil.getString(R.string.share_comment), getWeiboString(mMsg, n3),
							title);
					break;

				default:
					break;
				}

			} else if (mBook.isVDiskBook()) {
				String url = "";
				try {
					url = VDiskSyncManager.getInstance(SinaBookApplication.gContext).getShareUrl(
							mBook.getDownloadInfo().getVDiskFilePath());
				} catch (VDiskException e) {
					// 无需处理
				}

				if (!TextUtils.isEmpty(url)) {
					url = getShortUrl(url);
				} else {
					url = "";
				}

				switch (mType) {
				case TYPE_BOOK_DETAIL:
					mMsg = String.format(ResourceUtil.getString(R.string.share_book_vdisk), title, url);
					break;

				case TYPE_READ_NOTE:
					int n = getMsgLength(R.string.share_summary_vdisk, 3) - title.length() - url.length();
					mMsg = String.format(ResourceUtil.getString(R.string.share_summary_vdisk), getWeiboString(mMsg, n),
							title, url);
					break;

				default:
					break;
				}
			}
		}
	}

	/**
	 * 获取微博短链
	 * 
	 * @param url
	 * @return
	 */
	private String getShortUrl(String url) {
		if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			final ArrayList<String> urlList = new ArrayList<String>();
			urlList.add(url);

			String api = "https://api.weibo.com/2/short_url/shorten" + "" + ".json?source=%s&url_long=%s";
			api = String.format(Locale.CHINA, api, ConstantData.AppKey, url);
			api = ConstantData.addLoginInfoToUrl(api);

			TaskParams params = new TaskParams();
			params.put(RequestTask.PARAM_URL, api);
			params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

			RequestTask task = new RequestTask(new ShortUrlParser());
			task.setTaskFinishListener(new ITaskFinishListener() {
				@Override
				public void onTaskFinished(TaskResult taskResult) {
					Object result = taskResult.retObj;

					if (taskResult.stateCode == HttpStatus.SC_OK && result != null) {
						if (result instanceof String) {
							String shortUrl = String.valueOf(result);
							if (!TextUtils.isEmpty(shortUrl)) {
								urlList.set(0, shortUrl);
							}
						}
					}
				}
			});
			task.syncExecute(params);

			url = urlList.get(0);
		}

		return url;
	}

	/**
	 * 获取内容的字数
	 * 
	 * @param resId
	 * @param formatNum
	 * @return
	 */
	private int getMsgLength(int resId, int formatNum) {
		String format = ResourceUtil.getString(resId);
		int len = format.length() - formatNum * 4;
		return WEIBO_TEXT_LENGTH - len;
	}

	/**
	 * 得到分享到微博的字符串
	 * 
	 * @param msg
	 * @param textNum
	 * @return
	 */
	private String getWeiboString(String msg, int textNum) {
		if (null == msg || "".equals(msg)) {
			return msg;
		}

		String msgString = msg.replaceAll(BOOK_CONTENT_PATTERN, "");
		int len = msgString.length();
		int tnum = 6;
		if (mType == TYPE_PRAISE) {
			tnum = 7;
		}

		if (msgString != null && len > textNum && textNum > tnum) {
			msgString = msgString.substring(0, textNum - tnum);
			msgString += "......";
		}
		return msgString;
	}

	private class ShortUrlParser extends BaseParser {

		@Override
		protected Object parse(String jsonString) throws JSONException {
			JSONObject json = new JSONObject(jsonString);
			JSONArray jsonArray = json.optJSONArray("urls");

			if (jsonArray != null && jsonArray.length() > 0) {
				JSONObject object = jsonArray.optJSONObject(0);
				boolean result = object.optBoolean("result");
				String urlShort = object.optString("url_short");
				if (result) {
					return urlShort;
				}
			}

			return "";
		}
	}
}
