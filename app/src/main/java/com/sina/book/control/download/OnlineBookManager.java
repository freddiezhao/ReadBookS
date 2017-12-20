package com.sina.book.control.download;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.parser.BookContentParser;
import com.sina.book.util.LogUtil;

/**
 * 在线书籍管理器
 * 
 * @author MarkMjw
 */
public class OnlineBookManager implements ITaskFinishListener {
	private static final String TAG = "OnlineBookManager";

	private static final String REQ_CONTENT_TYPE = "req_content_type";

	private static OnlineBookManager sInstance;

	private IOnlineBookListener mListener;

	private RequestTask mRequestTask;

	private int mChapterId;

	private Context mContext;

	private BookContentParser mParser;

	private OnlineBookManager() {

	}

	public static OnlineBookManager getInstance() {
		if (sInstance == null) {
			sInstance = new OnlineBookManager();
		}
		return sInstance;
	}

	/**
	 * 释放下载资源
	 */
	public void release() {
		mListener = null;
		if (mRequestTask != null) {
			mRequestTask.cancel(true);
		}
	}

	/**
	 * Read chapter.
	 * 
	 * @param context
	 *            the context
	 * @param book
	 *            the book
	 * @param chapterId
	 *            the chapter id
	 * @param buy
	 * @param reqType
	 */
	public void readChapter(Context context, Book book, int chapterId, boolean buy, int reqType, String offShelfKey) {
		String reqUrl;
		StringBuilder sb = new StringBuilder(ConstantData.URL_READ_ONLINE);
		sb.append("?bid=").append(book.getBookId());
		sb.append("&cid=").append(chapterId);
		sb.append("&src=").append(Book.BOOK_SRC_WEB);
		sb.append("&sid=").append(book.getSid());
		if (offShelfKey != null) {
			sb.append("&prikey=").append(offShelfKey);
		}

		reqUrl = sb.toString();
		reqUrl = ConstantData.addDeviceIdToUrl(reqUrl);
		if (buy) {
			reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		}

		if (null != mRequestTask) {
			mRequestTask.cancel(true);
		}

		mParser = new BookContentParser(book, chapterId);
		mRequestTask = new RequestTask(mParser);
		mParser.setRequestTask(mRequestTask);
		mContext = context;
		mChapterId = chapterId;
		mRequestTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		params.put(REQ_CONTENT_TYPE, reqType);
		mRequestTask.execute(params);
	}

	public void setListener(IOnlineBookListener listener) {
		this.mListener = listener;
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			RequestTask task = (RequestTask) taskResult.task;
			int typeCode = (Integer) task.getParams().get(REQ_CONTENT_TYPE);
			if (mListener != null) {
				LogUtil.d(TAG, "Response code : " + mParser.getCode());
				LogUtil.d(TAG, "Response message : " + mParser.getMsg());
				if (ConstantData.CODE_SUCCESS.equals(mParser.getCode())) {
					mListener.onSuccess(mChapterId, Chapter.CHAPTER_SUCCESS, typeCode);
				} else if (ConstantData.CODE_LOGIN.equals(mParser.getCode())) {
					mListener.onSuccess(mChapterId, Chapter.CHAPTER_NEED_BUY, typeCode);
				} else if (ConstantData.CODE_RECHARGE.equals(mParser.getCode())) {
					// PayDialog.showBalanceDlg(mContext);
					mListener.onSuccess(mChapterId, Chapter.CHAPTER_RECHARGE, typeCode);
				} else {
					if (!TextUtils.isEmpty(mParser.getMsg())) {
						Toast.makeText(mContext, mParser.getMsg(), Toast.LENGTH_SHORT).show();
					}
					mListener.onSuccess(mChapterId, Chapter.CHAPTER_PREPARE, typeCode);
				}
			}
		} else {
			if (mListener != null) {
				mListener.onError(mChapterId, Chapter.CHAPTER_FAILED);
			}
		}
	}
}
