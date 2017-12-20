package com.sina.book.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.book.R;

/**
 * 通用活动提示对话框
 * 
 * @author chenjianli
 */
public class GeneralActivityDialog extends Dialog {

	private Context mContext;
	// private TextView mTextView;
	// private TextView mSubTextView;
	// private Button mButton;

	private ImageView mImageView;
	private TextView mSubTitleView;
	private LinearLayout mButtonsContainer;

	public GeneralActivityDialog(Context context) {
		super(context, R.style.CustomDialog);
		init(context);
	}

	public GeneralActivityDialog(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		setContentView(R.layout.vw_generic_general_activity_dlg);
		// mTextView = (TextView) findViewById(R.id.custom_tip_text);
		// mSubTextView = (TextView) findViewById(R.id.custom_tip_sub_text);
		// mButton = (Button) findViewById(R.id.custom_tip_button);
		// // 支持滚动(出现活动文字过长情况时可以让文本滚动)
		// mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		// mButton.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// dismiss();
		// }
		//
		// });

		mImageView = (ImageView) findViewById(R.id.general_activity_image);
		mSubTitleView = (TextView) findViewById(R.id.general_activity_subtitle);
		mButtonsContainer = (LinearLayout) findViewById(R.id.general_activity_buttons);
		setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH) {
					return true;
				}
				return false;
			}
		});
		windowDeploy(0, 0, 0);
	}

	public TextView getSubTitleView() {
		return mSubTitleView;
	}

	public ImageView getImageView() {
		return mImageView;
	}

	public LinearLayout getButtonsContainer() {
		return mButtonsContainer;
	}

	// 设置窗口显示
	public void windowDeploy(int x, int y, int width) {
		Window window = getWindow(); // 得到对话框
		if (width <= 0)
			width = (int) (280 * getDensity(mContext));
		int height = (int) (285 * getDensity(mContext));
		LayoutParams params = window.getAttributes();
		params.width = width;
		// params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		params.height = height;
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);
	}

	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

	// public void show(int msgResId) {
	// show(mContext.getString(msgResId));
	// }
	//
	// public void show(String msg) {
	// mTextView.setText(msg);
	// show();
	// }
	//
	// public void show(String msg, String subMsg) {
	// mTextView.setText(msg);
	// mSubTextView.setText(msg);
	// show();
	// }
	//
	// public void show(String msg, String subMsg, String buttonMsg) {
	// setMessage(msg);
	// setSubMessage(subMsg);
	// setButtonMsg(buttonMsg);
	// show();
	// }
	//
	// public void setMessage(int msgResId) {
	// mTextView.setText(msgResId);
	// }
	//
	// public void setMessage(String msg) {
	// if (!TextUtils.isEmpty(msg)) {
	// mTextView.setText(msg);
	// }
	// }
	//
	// public void setSubMessage(String msg) {
	// if (!TextUtils.isEmpty(msg)) {
	// mSubTextView.setText(msg);
	// }
	// }
	//
	// public void setButtonMsg(String msg) {
	// if (!TextUtils.isEmpty(msg)) {
	// mButton.setText(msg);
	// }
	// }

}
