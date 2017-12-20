package com.sina.book.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.data.WeiboContent;
import com.sina.book.parser.SimpleParser;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * 编辑评论界面
 * 
 * @author fzx
 * 
 */
public class SendCommentsPostActivity extends CustomTitleActivity implements ITaskFinishListener {

	private static final int CHAR_MAX_NUM = 300;

	private static final int REQ_COMMENT = 1;
	private static final int REQ_WEIBO = 2;

	/**
	 * 左侧按钮
	 */
	private View mLeftV;
	/**
	 * 中间标题部分
	 */
	private TextView mMiddleV;

	private EditText mContentV;
	private TextView mTxtNum;
	private CustomProDialog mProgressDialog;
	private CheckBox mShareWeiboCheck;

	private Bitmap mImage = null;
	private Book mBook;
	/**
	 * 是否都返回
	 */
	private int count = 0;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_comments_post);
		initIntent();
		initTitle();
		initViews();
		setListener();
	}

	@Override
	public void onClickLeft() {
		Util.hideSoftInput(this, mContentV);
		finish();
	}

	@Override
	// 发送评论
	public void onClickRight() {
		if (Util.isFastDoubleClick()) {
			return;
		}

		final String msg = mContentV.getText().toString().trim();
		if (msg.length() <= 0) {
			shortToast(R.string.comments_null);
			return;
		} else if (msg.length() > CHAR_MAX_NUM) {
			shortToast(R.string.coments_too_long);
			return;
		}

		if (mShareWeiboCheck.isChecked() && LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			// 分享至微博 && 微博未登陆
			Util.hideSoftInput(this, mContentV);
			LoginDialog.launch(SendCommentsPostActivity.this, new LoginStatusListener() {

				@Override
				public void onSuccess() {
					sendComments(msg);
				}

				@Override
				public void onFail() {

				}
			});
			return;
		}

		sendComments(msg);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		count++;
		if (taskResult != null && taskResult.task != null && taskResult.task instanceof RequestTask) {
			int extra = (Integer) ((RequestTask) taskResult.task).getExtra();
			if (ConstantData.CODE_SUCCESS.equals(String.valueOf(taskResult.retObj))) {
				if (extra == REQ_WEIBO) {
					shortToast(R.string.share_comments_success);
				} else {
					shortToast(R.string.send_comments_success);
				}

			} else {
				if (extra == REQ_WEIBO) {
					shortToast(R.string.share_comments_failed);
				} else {
					shortToast(R.string.send_comments_failed);
				}
			}

			if (REQ_WEIBO == extra) {
				if (mImage != null) {
					mImage.recycle();
					mImage = null;
				}
			}
		}

		if (count >= 2) {
			dismissProgressDialog();
			finish();
		}
	}

	/**
	 * 初始化view
	 */
	private void initViews() {
		mContentV = (EditText) findViewById(R.id.edt_comments_content);
		mTxtNum = (TextView) findViewById(R.id.txt_show_txtnum);
		mShareWeiboCheck = (CheckBox) findViewById(R.id.share_weibo);
		mShareWeiboCheck.setChecked(StorageUtil.getBoolean(StorageUtil.KEY_AUTO_WEIBO));
	}

	private void initIntent() {
		Intent intent = getIntent();
		mBook = (Book) intent.getSerializableExtra("book");
	}

	private void setListener() {
		mContentV.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mTxtNum.setText("" + (CHAR_MAX_NUM - s.length()));
			}
		});
	}

	/**
	 * 初始化标题
	 */
	private void initTitle() {
		mMiddleV = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		mMiddleV.setText(R.string.comments);
		setTitleMiddle(mMiddleV);

		mLeftV = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(mLeftV);

		View right = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_confirm, null);
		setTitleRight(right);
	}

	/**
	 * 执行评论请求
	 * 
	 * @param msg
	 *            输入框文字
	 */
	private void sendComments(String msg) {
		if (!HttpUtil.isConnected(mContext)) {
			Toast.makeText(SinaBookApplication.gContext, R.string.network_unconnected, Toast.LENGTH_SHORT).show();
			return;
		}

		Util.hideSoftInput(this, mContentV);

		if (mShareWeiboCheck.isChecked()) {
			count = 0;
			WeiboContent content = new WeiboContent(mBook, WeiboContent.TYPE_COMMENT);
			content.setMsg(msg);
			shareWeibo(content.getMsg());
		} else {
			count = 1;
		}

		// 发送评论接口
		String reqUrl = ConstantData.addLoginInfoToUrl(ConstantData.URL_COMMENTS_POST);
		String msgStr = msg;
		try {
			msgStr = URLEncoder.encode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore
		}

		RequestTask reqTask = new RequestTask(new SimpleParser());
		reqTask.setExtra(REQ_COMMENT);
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("bid", mBook.getBookId()));
		strNParams.add(new BasicNameValuePair("sid", mBook.getSid()));
		strNParams.add(new BasicNameValuePair("src", mBook.getBookSrc()));
		strNParams.add(new BasicNameValuePair("message", msgStr));
		reqTask.setPostParams(strNParams);
		reqTask.execute(params);

		showProgressDialog(R.string.upload_data);

		UserActionManager.getInstance().recordEvent(Constants.CLICK_COMMENT);
	}

	// 分享微博
	public void shareWeibo(String msg) {
		RequestTask task = null;

		// TODO: 评论分享微博不发送图片，默认接口传递图书card
		// mImage =
		// ImageLoader.getInstance().syncLoadBitmap(mBook.getDownloadInfo().getImageUrl());
		// if (null != mImage && !ImageLoader.getDefaultPic().equals(mImage)) {
		// task = new RequestTask(new SimpleParser(), mImage);
		// } else {
		task = new RequestTask(new SimpleParser());
		// }

		task.setExtra(REQ_WEIBO);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_SHARE_WEIBO);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("authcode", ConstantData.AUTH_CODE));
		strNParams.add(new BasicNameValuePair("u_id", LoginUtil.getLoginInfo().getUID()));
		strNParams.add(new BasicNameValuePair("access_token", LoginUtil.getLoginInfo().getAccessToken()));
		strNParams.add(new BasicNameValuePair("sid", mBook.getSid()));
		strNParams.add(new BasicNameValuePair("b_id", mBook.getBookId()));
		strNParams.add(new BasicNameValuePair("b_src", mBook.getBookSrc()));
		strNParams.add(new BasicNameValuePair("c_id", Chapter.DEFAULT_GLOBAL_ID + ""));
		strNParams.add(new BasicNameValuePair("c_offset", "0"));
		strNParams.add(new BasicNameValuePair("u_comment", msg));
		task.setPostParams(strNParams);
		task.setTaskFinishListener(this);
		task.execute(params);

		showProgressDialog(R.string.upload_data);
	}

	private void showProgressDialog(int resId) {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.setMessage(resId);
			return;
		}

		if (null == mProgressDialog) {
			mProgressDialog = new CustomProDialog(this);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(true);
		}

		mProgressDialog.show(resId);
	}

	private void dismissProgressDialog() {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
}
