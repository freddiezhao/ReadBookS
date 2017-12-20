package com.sina.book.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.book.SinaBookApplication;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.view.ListDialog;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.PayDialog;
import com.sina.book.ui.view.ShareDialog;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.DialogUtils;

/**
 * Activity基类，应用中所有Activity均应继承它，方便进行行为统计
 * 
 * @Author: MarkMjw
 * @Date: 13-5-16 下午5:11
 */
@SuppressWarnings("rawtypes")
public abstract class BaseActivity extends Activity {
	// private static final String TAG = "BaseActivity";

	private static Stack<Class> sActivityStack = new Stack<Class>();
	private static List<Class> sKeyActivities = new ArrayList<Class>();

	private CustomProDialog mProgressDialog;
	private Toast mToast;
	private ILifecycleListener mLifeListener;
	private boolean mIsBeDestroyed;
	protected Context mContext;

	static {
		sKeyActivities.add(RecommendActivity.class);
		sKeyActivities.add(SellFastListActivity.class);
		sKeyActivities.add(PartitionActivity.class);
		sKeyActivities.add(CommonRecommendActivity.class);
		sKeyActivities.add(SearchResultActivity.class);
		sKeyActivities.add(ReadActivity.class);
		sKeyActivities.add(BookDetailActivity.class);
	}

	/**
	 * 实现BookDetailActivity打开三层的控制
	 */
	private static final int BOOK_DETAIL_ACTIVITY_NUM = 3;
	private static LinkedList<Activity> sBookDetailActivitys = new LinkedList<Activity>();

	public void setLifecycleListener(ILifecycleListener lifeListener) {
		mLifeListener = lifeListener;
	}

	public boolean isBeDestroyed() {
		return mIsBeDestroyed;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 硬件加速
		// https://github.com/umeng/umeng-muti-channel-build-tool/wiki/%E7%9B%AE%E5%89%8D%E6%B2%A1%E6%9C%89%E6%94%AF%E6%8C%81%E7%9A%84%E7%89%B9%E6%80%A7
		// http://stackoverflow.com/questions/13850259/is-there-a-way-to-disable-hardware-acceleration-only-for-android-4-0-3
		// Log.d("Build", "Build.VERSION.SDK_INT is "+Build.VERSION.SDK_INT);
		// if (Build.VERSION.SDK_INT !=
		// Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
		// getWindow().setFlags(
		// WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
		// WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		// }

		SinaBookApplication.push(this);

		mIsBeDestroyed = false;
		mContext = this;

		Class cls = getClass();
		if (cls.equals(BookDetailActivity.class)) {

			if (sBookDetailActivitys != null) {
				// 判断书籍ID
				for (int i = 0; i < sBookDetailActivitys.size(); i++) {
					BookDetailActivity activity = (BookDetailActivity) sBookDetailActivitys
							.get(i);
					String bookId = activity.getBookId();
					Intent intent = getIntent();
					if (!TextUtils.isEmpty(bookId) && intent != null) {
						Bundle bundle = intent.getExtras();
						if (null != bundle) {
							String tempBookId = bundle
									.getString(BookDetailActivity.BID);
							if (!TextUtils.isEmpty(tempBookId)
									&& tempBookId.equals(bookId)) {
								sBookDetailActivitys.remove(i).finish();
								break;
							}
						}
					}
				}

				sBookDetailActivitys.add(this);
				// 如果连续的bookDetailActivity超过阀值，finish掉
				if (sBookDetailActivitys.size() > BOOK_DETAIL_ACTIVITY_NUM) {
					sBookDetailActivitys.remove(0).finish();
				}
			}

			if (!sActivityStack.isEmpty()) {
				Class c = sActivityStack.peek();
				if (sKeyActivities.contains(c)) {
					String key = c.getSimpleName() + "_" + cls.getSimpleName();
					UserActionManager.getInstance().recordEvent(key);
				}
			}
			sActivityStack.push(cls);
		} else {
			// sBookDetailActivitys.clear();
			if (sKeyActivities.contains(cls)) {
				sActivityStack.push(cls);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// 开始行为统计
		UserActionManager.getInstance().onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();

		// 结束行为统计，并提交服务器
		UserActionManager.getInstance().onStop();
	}

	@Override
	protected void onDestroy() {
		SinaBookApplication.remove(this);

		// 这里统一隐藏显示的对话框
		LoginDialog.release(this);
		PayDialog.release(this);
		ShareDialog.dismiss(this);
		CommonDialog.dismiss(this);
		ListDialog.dismiss(this);
		DialogUtils.dismissProgressDialog();
		ImageLoader.getInstance().releaseContext(this);

		Class cls = getClass();
		if (cls.equals(BookDetailActivity.class)) {
			sBookDetailActivitys.remove(this);
		}

		if (sActivityStack.contains(cls)) {
			sActivityStack.remove(cls);
		}
		mIsBeDestroyed = true;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mLifeListener != null) {
			mLifeListener.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 设置为栈顶
	 * 
	 * @param cls
	 */
	public static void setClassTop(Class cls) {
		if (sActivityStack.contains(cls)) {
			sActivityStack.remove(cls);

			sActivityStack.push(cls);
		}
	}

	/**
	 * Short toast.
	 * 
	 * @param resId
	 */
	protected void shortToast(int resId) {
		shortToast(getString(resId));
	}

	/**
	 * Short toast.
	 * 
	 * @param content
	 */
	protected void shortToast(String content) {
		if (mToast != null) {
			mToast.setText(content);
			mToast.setDuration(Toast.LENGTH_SHORT);
		} else {
			mToast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	/**
	 * 显示进度框
	 * 
	 * @param resId
	 *            信息资源ID
	 * @param cancelAble
	 *            是否可取消
	 */
	protected void showProgress(int resId, boolean cancelAble) {
		showProgress(getString(resId), cancelAble, null);
	}

	/**
	 * 显示进度框
	 * 
	 * @param message
	 *            信息
	 * @param cancelAble
	 *            是否可取消
	 */
	protected void showProgress(String message, boolean cancelAble) {
		showProgress(message, cancelAble, null);
	}

	/**
	 * 显示进度框
	 * 
	 * @param resId
	 *            信息资源ID
	 * @param cancelAble
	 *            是否可取消
	 * @param listener
	 *            DialogInterface.OnKeyListener
	 */
	protected void showProgress(int resId, boolean cancelAble,
			DialogInterface.OnKeyListener listener) {
		showProgress(getString(resId), cancelAble, listener);
	}

	/**
	 * 显示进度框
	 * 
	 * @param message
	 *            信息
	 * @param cancelAble
	 *            是否可取消
	 * @param listener
	 *            DialogInterface.OnKeyListener
	 */
	protected void showProgress(String message, boolean cancelAble,
			DialogInterface.OnKeyListener listener) {
		if (TextUtils.isEmpty(message)) {
			return;
		}

		if (null == mProgressDialog) {
			mProgressDialog = new CustomProDialog(this);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}

		mProgressDialog.setOnKeyListener(listener);
		mProgressDialog.setCancelable(cancelAble);
		mProgressDialog.show(message);
	}

	/**
	 * 进度框是否正显示
	 * 
	 * @return
	 */
	protected boolean progressShowing() {
		return null != mProgressDialog && mProgressDialog.isShowing();
	}

	/**
	 * 隐藏进度框
	 */
	protected void dismissProgress() {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
}
