package com.sina.book.ui.view;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.WeiboContent;
import com.sina.book.ui.BaseActivity;
import com.sina.book.ui.BaseFragmentActivity;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.ShareWeiboActivity;
import com.sina.book.ui.widget.BaseDialog;

/**
 * 分享对话框
 * 
 * @author MarkMjw
 * @date 2013-8-6
 */
public class ShareDialog extends BaseDialog implements OnClickListener {
	private static ShareDialog mDialog;

	private WeiboContent mContent;

	/**
	 * Show分享Dialog
	 * 
	 * @param activity
	 *            传入的Activity必须是BaseActivity或BaseFragmentActivity的子类
	 */
	public static void show(Activity activity, WeiboContent content) {
		if (!(activity instanceof BaseActivity) && !(activity instanceof BaseFragmentActivity)) {
			throw new IllegalArgumentException("must be BaseActivity or BaseFragment Activity");
		}

		dismiss(activity);

		mDialog = new ShareDialog(activity, content);
		mDialog.show();
	}

	/**
	 * Dismiss
	 */
	public static void dismiss(Activity activity) {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public ShareDialog(Activity activity, WeiboContent content) {
		super(activity);

		mContent = content;
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setTitle(R.string.share_to);

		View content = LayoutInflater.from(mContext).inflate(R.layout.vw_share_dialog_content, mContentLayout);
		if (null != content) {
			TextView shareWeibo = (TextView) content.findViewById(R.id.share_to_weibo);
			TextView shareOthers = (TextView) content.findViewById(R.id.share_to_others);
			shareWeibo.setOnClickListener(this);
			shareOthers.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.share_to_weibo:
			if(mContext instanceof BookDetailActivity){
				((BookDetailActivity)mContext).cancelDownload();
			}
			
			ShareWeiboActivity.launch(mContext, mContent);
			break;

		case R.id.share_to_others:
			if(mContext instanceof BookDetailActivity){
				((BookDetailActivity)mContext).cancelDownload();
			}
			share2others();
			break;

		default:
			break;
		}

		dismiss();
	}

	private void share2others() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.setType("text/*");
		String text = mContent.getMsg();
		// String text = mContent.getMsg() +
		// mContext.getString(R.string.url_down_app);
		intent.putExtra(Intent.EXTRA_TEXT, text);

		String imgPath = mContent.getImagePath();
		if (!TextUtils.isEmpty(imgPath)) {
			File img = new File(imgPath);
			if (img.exists()) {
				Uri uri = Uri.fromFile(img);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_STREAM, uri);
			}
		}

		mContext.startActivity(Intent.createChooser(intent, "分享到"));
	}
}
