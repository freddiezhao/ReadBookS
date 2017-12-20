package com.sina.book.control.download;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.parser.SimpleParser;
import com.sina.book.util.LogUtil;

/**
 * 购买书籍管理器
 * 
 * @author MarkMjw
 */
public class BuyBookManager implements ITaskFinishListener {
	private static final String TAG = "BuyBookManager";

	/** 购买成功. */
	public static final int STATE_SUCCESS = 0x01;

	/** 购买失败. */
	public static final int STATE_RECHARGE = 0x02;

	/** 余额不足. */
	public static final int STATE_FAILURE = 0x03;

	/** 成功. */
	private static final String CODE_SUCCESS = "0";
	/** 账户余额不足. */
	private static final String CODE_RECHARGE = "4";
	/** 失败. */
	private static final String CODE_FAILURE = "6";

	private static BuyBookManager sInstance;

	private Context mContext;

	private RequestTask mTask;
	private SimpleParser mParser;
	private IBuyBookListener mListener;

	private BuyBookManager() {
		mContext = SinaBookApplication.gContext;
	}

	public static BuyBookManager getInstance() {
		if (sInstance == null) {
			sInstance = new BuyBookManager();
		}
		return sInstance;
	}

	/**
	 * 释放下载资源
	 */
	public void release() {
		mListener = null;
		if (mTask != null) {
			mTask.cancel(true);
		}
	}

	/**
	 * Buy book. 购买书籍
	 * 
	 * @param book
	 *            所购买书籍
	 * @param chapter
	 *            为空则购买全本
	 * @param listener
	 */
	public void buyBook(Book book, Chapter chapter, IBuyBookListener listener) {
		if (null == book)
			return;

		if (null != mTask) {
			mTask.cancel(true);
		}

		// // 首先查找是否数据库中已经有该书了，如果有，则使用数据库中的book对象
		// boolean hasBook = DownBookManager.getInstance().hasBook(book);
		// if (hasBook) {
		// Book shelfBook = DownBookManager.getInstance().getBook(book);
		// shelfBook.exchangeBookContentType(book);
		// book = shelfBook;
		// }

		mListener = listener;
		mParser = new SimpleParser();

		mTask = new RequestTask(mParser);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, book.getBuyUrl(chapter));
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		mTask.setTaskFinishListener(this);
		mTask.execute(params);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		LogUtil.d(TAG, "Response code : " + mParser.getCode());
		LogUtil.d(TAG, "Response message : " + mParser.getMsg());

		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {

			if (mListener != null) {
				if (CODE_SUCCESS.equals(mParser.getCode())) {
					mListener.onFinish(STATE_SUCCESS);

				} else if (CODE_RECHARGE.equals(mParser.getCode())) {
					mListener.onFinish(STATE_RECHARGE);

				} else if (CODE_FAILURE.equals(mParser.getCode())) {
					mListener.onFinish(STATE_FAILURE);

				} else {
					if (!TextUtils.isEmpty(mParser.getMsg())) {
						Toast.makeText(mContext, mParser.getMsg(), Toast.LENGTH_SHORT).show();
					}
					mListener.onFinish(STATE_FAILURE);
				}
			}
		} else {
			if (mListener != null) {
				mListener.onFinish(STATE_FAILURE);
			}
		}
	}
}
