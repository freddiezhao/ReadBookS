package com.sina.book.ui.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.download.UpdateAppManager;
import com.sina.book.ui.widget.BaseDialog;

/**
 * 公用对话框
 * 
 * @author MarkMjw
 * @date 2013-8-6
 */
public class CommonDialog extends BaseDialog implements OnClickListener {
	private static CommonDialog mDialog;

	private CharSequence mTitle;
	private CharSequence mText;

	private String mLeftBtnText;
	private String mRightBtnText;

	private ClickListener mListener;

	/**
	 * Show Dialog
	 * 
	 * @param context
	 *            上下文引用
	 * @param text
	 *            消息内容
	 * @param listener
	 *            按钮事件监听器
	 */
	public static void show(Context context, CharSequence text, ClickListener listener) {
		show(context, null, text, listener);
	}

	/**
	 * Show Dialog
	 * 
	 * @param context
	 *            上下文引用
	 * @param textRes
	 *            消息内容资源ID
	 * @param listener
	 *            按钮事件监听器
	 */
	public static void show(Context context, int textRes, ClickListener listener) {
		show(context, context.getString(textRes), listener);
	}

	/**
	 * Show Dialog
	 * 
	 * @param context
	 *            上下文引用
	 * @param titleRes
	 *            提示title资源ID
	 * @param textRes
	 *            消息内容资源ID
	 * @param listener
	 *            按钮事件监听器
	 */
	public static void show(Context context, int titleRes, int textRes, ClickListener listener) {
		show(context, context.getString(titleRes), context.getString(textRes), listener);
	}

	/**
	 * Show Dialog
	 * 
	 * @param context
	 *            上下文引用
	 * @param title
	 *            提示title,可以为空默认为“提示”
	 * @param text
	 *            消息内容
	 * @param listener
	 *            按钮事件监听器
	 */
	public static void show(Context context, String title, CharSequence text, ClickListener listener) {
		show(context, title, text, null, null, listener);
	}

	/**
	 * Show Dialog
	 * 
	 * @param context
	 *            上下文引用
	 * @param title
	 *            提示title
	 * @param text
	 *            消息内容
	 * @param leftBtn
	 *            左侧按钮文字，可以为空默认为“取消”
	 * @param rightBtn
	 *            右侧按钮文字，可以为空默认为“确定”
	 * @param listener
	 *            按钮事件监听器
	 */
	public static void show(Context context, CharSequence title, CharSequence text, String leftBtn, String rightBtn,
			ClickListener listener) {
		show(context, title, text, leftBtn, rightBtn, listener, null, true, true);
	}
	
	public static void show(Context context, int text, int leftBtn, int rightBtn,
			ClickListener listener) {
		show(context, null, context.getString(text), context.getString(leftBtn), context.getString(rightBtn), listener, null, true, true);
	}
	
	public static void show(Context context, CharSequence text, String leftBtn, String rightBtn,
			ClickListener listener) {
		show(context, null, text, leftBtn, rightBtn, listener, null, true, true);
	}
	
	public static void show(Context context, CharSequence text, String leftBtn, String rightBtn,
			ClickListener listener, boolean cancelable) {
		show(context, null, text, leftBtn, rightBtn, listener, null, cancelable, true);
	}
	
	public static void show(Context context, CharSequence text, String leftBtn, String rightBtn,
			ClickListener listener, boolean cancelable, boolean canceledOnTouchOutside) {
		show(context, null, text, leftBtn, rightBtn, listener, null, cancelable, canceledOnTouchOutside);
	}
	
	public static void show(Context context, CharSequence title,  CharSequence text, String leftBtn, String rightBtn,
			ClickListener listener, OnDismissListener dismissListener) {
		show(context, null, text, leftBtn, rightBtn, listener, dismissListener, true, true);
	}

	public static void show(Context context, CharSequence title, CharSequence text, String leftBtn, String rightBtn,
			ClickListener listener, OnDismissListener dismissListener, boolean cancelable, boolean canceledOnTouchOutside) {
		dismiss(context);

		mDialog = new CommonDialog(context, title, text, leftBtn, rightBtn, listener);
		mDialog.setCancelable(cancelable);
		mDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
		mDialog.setOnDismissListener(dismissListener);
		mDialog.show();
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	@Override
	public void cancel() {
		super.cancel();
	}

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            上下文引用
	 * @param title
	 *            提示title
	 * @param text
	 *            消息内容
	 * @param listener
	 *            按钮事件监听器
	 */
	public CommonDialog(Context context, CharSequence title, CharSequence text, ClickListener listener) {
		this(context, title, text, null, null, listener);
	}

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            上下文引用
	 * @param title
	 *            提示title
	 * @param text
	 *            消息内容
	 * @param leftBtn
	 *            左侧按钮文字
	 * @param rightBtn
	 *            右侧按钮文字
	 * @param listener
	 *            按钮事件监听器
	 */
	public CommonDialog(Context context, CharSequence title, CharSequence text, String leftBtn, String rightBtn,
			ClickListener listener) {
		super(context);

		mTitle = title;
		mText = text;

		mLeftBtnText = leftBtn;
		mRightBtnText = rightBtn;

		if (null == listener) {
			mListener = new DefaultListener();
		} else {
			mListener = listener;
		}
	}

	/**
	 * Dismiss
	 * 
	 * @param context
	 */
	public static void dismiss(Context context) {
		if (mDialog != null && !UpdateAppManager.isNewAppUpdateDialogShown) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		String titleStr = "";
		if(TextUtils.isEmpty(mTitle)){
			titleStr = mContext.getString(R.string.note);
		}else{
			titleStr = mTitle.toString();
		}
		setTitle(titleStr);

		View content = LayoutInflater.from(mContext).inflate(R.layout.vw_common_dialog, mContentLayout);
		if (null != content) {
			// ((TextView)
			// content.findViewById(R.id.dialog_text)).setText(mText);
			TextView contentText = (TextView) content.findViewById(R.id.dialog_text);

			ViewGroup singleGroup = (ViewGroup) content.findViewById(R.id.dialog_container1);
			ViewGroup mixGroup = (ViewGroup) content.findViewById(R.id.dialog_container2);

			TextView centerBtn = (TextView) content.findViewById(R.id.dialog_center_btn);
			TextView rightBtn = (TextView) content.findViewById(R.id.dialog_right_btn);
			TextView leftBtn = (TextView) content.findViewById(R.id.dialog_left_btn);

			contentText.setText(mText);

			// 解决：BugID=21309
			// 设置一下contentText的最大高度
			contentText.setMovementMethod(ScrollingMovementMethod.getInstance());
			contentText.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
			DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
			contentText.setMaxHeight(dm.widthPixels - (int) (10 * dm.density));

			if (!TextUtils.isEmpty(mLeftBtnText) && TextUtils.isEmpty(mRightBtnText)) {
				singleGroup.setVisibility(ViewGroup.VISIBLE);
				mixGroup.setVisibility(ViewGroup.GONE);

				// String text = null;
				// if (!TextUtils.isEmpty(mLeftBtnText)) {
				// text = mLeftBtnText;
				// } else {
				// text = mLeftBtnText;
				// }

				centerBtn.setText(mLeftBtnText);
				centerBtn.setOnClickListener(this);
			} else {
				singleGroup.setVisibility(ViewGroup.GONE);
				mixGroup.setVisibility(ViewGroup.VISIBLE);

				if (!TextUtils.isEmpty(mLeftBtnText)) {
					leftBtn.setText(mLeftBtnText);
				}

				if (!TextUtils.isEmpty(mRightBtnText)) {
					rightBtn.setText(mRightBtnText);
				}

				rightBtn.setOnClickListener(this);
				leftBtn.setOnClickListener(this);
			}

		}
	}

	@Override
	public void onClick(View v) {
		// 先隐藏避免回调方法执行时间过长
		dismiss();

		int id = v.getId();
		switch (id) {
		case R.id.dialog_right_btn:
			mListener.onRightClick(this);
			break;

		case R.id.dialog_left_btn:
			mListener.onLeftClick(this);
			break;

		case R.id.dialog_center_btn:

			break;

		default:
			break;
		}
	}

	/**
	 * 按钮被按下事件监听器
	 */
	public interface ClickListener {
		/**
		 * 右边按钮被按下
		 * 
		 * @param dialog
		 */
		public void onRightClick(DialogInterface dialog);

		/**
		 * 左边按钮被按下
		 * 
		 * @param dialog
		 */
		public void onLeftClick(DialogInterface dialog);
	}

	/**
	 * 默认的监听器
	 */
	public static class DefaultListener implements ClickListener {
		@Override
		public void onRightClick(DialogInterface dialog) {

		}

		@Override
		public void onLeftClick(DialogInterface dialog) {

		}
	}
}
