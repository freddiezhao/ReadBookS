package com.sina.book.ui;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.WeiboContent;
import com.sina.book.image.ImageUtil;
import com.sina.book.parser.SimpleParser;
import com.sina.book.parser.UserIdParser;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;

/**
 * 分享微博界面
 * 
 * @author MarkMjw
 */
public class ShareWeiboActivity extends CustomTitleActivity implements ITaskFinishListener, Callback {
	// RequestListener, Callback {
	private static final String TAG = "ShareWeiboActivity";
	/**
	 * 微博最大字数
	 */
	private int WEIBO_TEXT_LENGTH = 130;

	private static final String REQUEST_SEND_WEIBO = "sendWeibo";
	private static final String REQUEST_SEND_VDISK_WEIBO = "sendVdiskWeibo";

	private static final int TYPE_SUCCESS = 0;
	private static final int TYPE_ERROR = 1;

	private EditText mEditText;
	private ImageView mImageView;

	private RelativeLayout mDeleteBtnLayout;
	private TextView mWeiboTextNum;

	private CustomProDialog mProgressDialog;

	private WeiboContent mContent;
	private Bitmap mImage;
	private Bitmap mSrcImage;

	// private Handler mHandler;

	/**
	 * 点击事件监听器.
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.share_weibo_del_btn:
				clean();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 分享微博入口
	 * 
	 * @param context
	 *            上下文引用
	 * @param content
	 *            微博内容对象
	 */
	public static void launch(Context context, WeiboContent content) {
		if (null == content)
			return;

		Bundle bundle = new Bundle();
		Book book = content.getBook();

		// 自己服务器的书通过服务器发微博取消所有图片
		if (null != book && book.isOurServerBook()) {
			content.setImagePath(null);
		}

		bundle.putSerializable("WeiboContent", content);

		Intent intent = new Intent();
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.setClass(context, ShareWeiboActivity.class);

		context.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_share_weibo);

		Intent intent = getIntent();
		if (null != intent) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				mContent = (WeiboContent) bundle.get("WeiboContent");
			}
		}
		// 生成Handler实例
		mHandler = new Handler(this);

		initTitle();
		initViews();
		setViewListener();
		initViewsValue();
	}

	@Override
	protected void onDestroy() {
		if (null != mImage) {
			mImage.recycle();
			mImage = null;
		}

		if (null != mSrcImage) {
			mSrcImage.recycle();
			mSrcImage = null;
		}

		super.onDestroy();
	}

	@Override
	public void onClickRight() {
		if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			LoginDialog.launch(ShareWeiboActivity.this, new LoginStatusListener() {

				@Override
				public void onSuccess() {
					toShareWeibo();
				}

				@Override
				public void onFail() {
				}
			});
		} else {
			toShareWeibo();
		}
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	@Override
	public boolean handleMessage(android.os.Message msg) {
		if (null != msg) {
			dismissProgressDialog();

			switch (msg.what) {
			case TYPE_ERROR:
				shortToast((String) msg.obj);
				break;

			case TYPE_SUCCESS:
				shortToast((String) msg.obj);
				finish();
				break;
			}

			if (null != mSrcImage) {
				mSrcImage.recycle();
				mSrcImage = null;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		String reqType = taskResult.task.getType();

		LogUtil.d(TAG, "RequestType:" + reqType + "，StateCode:" + taskResult.stateCode);

		if (REQUEST_SEND_WEIBO.equals(reqType)) {
			if (taskResult.retObj instanceof String) {
				LogUtil.d(TAG, "RetObj:" + taskResult.retObj);
				if (ConstantData.CODE_SUCCESS.equals(String.valueOf(taskResult.retObj))) {
					shortToast(R.string.share_weibo_success);
					finish();
				} else {
					reqUId();
					return;
				}
			} else if (taskResult.retObj instanceof Integer) {
				int errorCode = (Integer) taskResult.retObj;
				LogUtil.d(TAG, "ExpireCode:" + errorCode);
				if (errorCode == ConstantData.WEIBO_SDK_ERROR_CODE_20019) {
					shortToast(R.string.share_weibo_failed_repeat_content);
				} else {
					toLogin();
				}
			}
		} else if (REQUEST_SEND_VDISK_WEIBO.equals(reqType)) {
			if (taskResult.stateCode == HttpStatus.SC_OK) {
				shortToast(R.string.share_weibo_success);
				finish();
			} else {
				reqUId();
				return;
			}
		} else {
			if (taskResult.retObj instanceof Integer) {
				LogUtil.d(TAG, "ExpireCode:" + taskResult.retObj);
				toLogin();
			} else {
				LogUtil.d(TAG, "Uid:" + taskResult.retObj);
				shortToast(R.string.share_weibo_failed);
			}
		}
		dismissProgressDialog();
	}

	private void toLogin() {
		/**
		 * 清除登录信息
		 */
		LoginUtil.i.clearLoginInfo(this, "ShareWeiboActivity->toLogin");

		LoginDialog.launch(ShareWeiboActivity.this, null);
	}

	private void initTitle() {
		TextView middleText = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (null != middleText) {
			middleText.setText(R.string.share_weibo);
			setTitleMiddle(middleText);
		}

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		View right = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_confirm, null);
		setTitleRight(right);
	}

	private void initViews() {
		mEditText = (EditText) findViewById(R.id.share_weibo_edit);
		mImageView = (ImageView) findViewById(R.id.share_weibo_img);
		mDeleteBtnLayout = (RelativeLayout) findViewById(R.id.share_weibo_del_btn);
		mWeiboTextNum = (TextView) findViewById(R.id.share_weibo_text_num);
	}

	private void setViewListener() {
		// 监听编辑字数，实时显示可输入的字数，超过140则无法输入
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (null != s) {
					// 设置光标位置
					mEditText.requestFocus();

					Editable editable = mEditText.getText();
					if (null != editable) {
						int len = editable.length();
						mWeiboTextNum.setText("" + (WEIBO_TEXT_LENGTH - len));
					}
				}
			}
		});

		mDeleteBtnLayout.setOnClickListener(mClickListener);
	}

	private void initViewsValue() {
		if (null != mContent) {
			mEditText.setText(mContent.getMsg());
			mEditText.setSelection(mContent.getMsg().length());

			final String imgPath = mContent.getImagePath();
			if (!TextUtils.isEmpty(imgPath)) {
				new GenericTask() {

					@Override
					protected TaskResult doInBackground(TaskParams... taskParamses) {
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
						if (null != params) {
							// 根据需要显示图片的高宽去读取图片，尽量少使用内存
							mImage = ImageUtil.getBitmapFromFile(imgPath, params.width, params.height);
						}
						return null;
					}

					@Override
					protected void onPostExecute(TaskResult result) {
						if (null != mImage) {
							mImageView.setImageBitmap(mImage);

							mImageView.setVisibility(View.VISIBLE);
						} else {
							mImageView.setVisibility(View.GONE);
						}

					}
				}.execute();

			} else {
				mImageView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 清除文字.
	 */
	private void clean() {
		if (null != mEditText.getText()) {
			CommonDialog.show(this, R.string.reading_shareweibo_clean, new CommonDialog.DefaultListener() {
				@Override
				public void onRightClick(DialogInterface dialog) {
					mEditText.setText("");
				}
			});
		}
	}

	private void reqUId() {
		String url = ConstantData.addLoginInfoToUrl(ConstantData.WEIBO_GET_UID);
		RequestTask task = new RequestTask(new UserIdParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		task.setTaskFinishListener(this);
		task.execute(params);
	}

	private void toShareWeibo() {
		if (!HttpUtil.isConnected(mContext)) {
			Toast.makeText(SinaBookApplication.gContext, R.string.network_unconnected, Toast.LENGTH_SHORT).show();
		} else {
			Editable editable = mEditText.getText();

			if (null != editable) {
				String msg = editable.toString().trim();
				if (!TextUtils.isEmpty(msg)) {

					int num = msg.length();
					if (num > WEIBO_TEXT_LENGTH) {
						shortToast(R.string.max_num_tips);
						return;
					}

					Book book = mContent.getBook();

					if (null == book || book.isVDiskBook()) {
						shareVDiskWeibo(msg);
					} else {
						shareWeibo(msg);
					}

					UserActionManager.getInstance().recordEvent(Constants.ACTION_SHARE_WEIBO);
				} else {
					showDialog();
				}
			}
		}
	}

	public void shareWeibo(String text) {
		RequestTask task;
		String imgPath = mContent.getImagePath();
		if (!TextUtils.isEmpty(imgPath)) {
			mSrcImage = BitmapFactory.decodeFile(imgPath);
		}

		if (null != mSrcImage) {
			task = new RequestTask(new SimpleParser(), mSrcImage);
		} else {
			task = new RequestTask(new SimpleParser());
		}

		task.setType(REQUEST_SEND_WEIBO);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_SHARE_WEIBO);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("authcode", ConstantData.AUTH_CODE));
		strNParams.add(new BasicNameValuePair("u_id", LoginUtil.getLoginInfo().getUID()));
		strNParams.add(new BasicNameValuePair("access_token", LoginUtil.getLoginInfo().getAccessToken()));
		strNParams.add(new BasicNameValuePair("sid", mContent.getBook().getSid()));
		strNParams.add(new BasicNameValuePair("b_id", mContent.getBook().getBookId()));
		strNParams.add(new BasicNameValuePair("b_src", mContent.getBook().getBookSrc()));
		strNParams.add(new BasicNameValuePair("c_id", mContent.getChapterId() + ""));
		strNParams.add(new BasicNameValuePair("c_offset", mContent.getChapterOffset() + ""));
		strNParams.add(new BasicNameValuePair("u_comment", text));
		task.setPostParams(strNParams);
		task.setTaskFinishListener(this);
		task.execute(params);

		showProgressDialog(R.string.upload_data);
	}

	private void shareVDiskWeibo(String text) {
		if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			return;
		}
		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			// LoginInfo loginInfo = LoginUtil.getLoginInfo();
			showProgressDialog(R.string.upload_data);

			// Oauth2AccessToken accessToken = new
			// Oauth2AccessToken(loginInfo.getAccessToken(),
			// loginInfo.getExpires());

			String imgPath = mContent.getImagePath();

			postText(text, imgPath);
		}
	}

	/**
	 * 发送文字信息
	 * 
	 * @param msg
	 */
	private void postText(final String msg, final String imgPath) {
		RequestTask task = null;
		if (!TextUtils.isEmpty(imgPath)) {
			// mSrcImage = BitmapFactory.decodeFile(imgPath);
			task = new RequestTask(new SimpleParser(), null);
		} else {
			task = new RequestTask(new SimpleParser());
		}
		task.setType(REQUEST_SEND_VDISK_WEIBO);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_SEND_WEIBO);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

		ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
		strNParams.add(new BasicNameValuePair("source", ConstantData.AppKey));
		strNParams.add(new BasicNameValuePair("status", msg));
		strNParams.add(new BasicNameValuePair("access_token", LoginUtil.getLoginInfo().getAccessToken()));

		task.setPostParams(strNParams);
		task.setTaskFinishListener(this);
		task.execute(params);
	}

	// @Override
	// public void onIOException(IOException arg0) {
	// Message msg = mHandler.obtainMessage();
	// msg.what = TYPE_ERROR;
	// msg.obj = getString(R.string.share_weibo_failed);
	// mHandler.sendMessage(msg);
	// }

	// @Override
	// public void onError(WeiboException arg0) {
	// Message msg = mHandler.obtainMessage();
	// msg.what = TYPE_ERROR;
	// msg.obj = getString(R.string.share_weibo_failed);
	// mHandler.sendMessage(msg);
	// }

	// @Override
	// public void onComplete(String arg0) {
	// Message msg = mHandler.obtainMessage();
	// msg.what = TYPE_SUCCESS;
	// msg.obj = getString(R.string.share_weibo_success);
	// mHandler.sendMessage(msg);
	// }

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

	/**
	 * 提示分享内容为空的对话框
	 */
	private void showDialog() {
		CommonDialog.show(this, R.string.share_weibo_text_note, new CommonDialog.DefaultListener() {
			@Override
			public void onRightClick(DialogInterface dialog) {
				Book book = mContent.getBook();
				mContent.setMsg("");

				if (null == book || book.isVDiskBook()) {
					// String text = mContent.getMsg();
					// // text += "http://t.cn/zHDkKCt";
					// shareVDiskWeibo(text);
					shareVDiskWeibo(mContent.getMsg());
				} else {
					shareWeibo(mContent.getMsg());
				}

				UserActionManager.getInstance().recordEvent(Constants.ACTION_SHARE_WEIBO);
			}
		});
	}
}
