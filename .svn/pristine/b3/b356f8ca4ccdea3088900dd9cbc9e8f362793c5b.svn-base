package com.sina.book.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PartitionLikedItem;
import com.sina.book.data.PartitionLikedResult;
import com.sina.book.parser.PartitionLikedParser;
import com.sina.book.parser.SimpleParser;
import com.sina.book.ui.adapter.PartitionLikedAdapter;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.useraction.UserActionUtil;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;

/**
 * 选您喜欢页面
 * 
 * @author YangZhanDong
 * 
 */
public class PartitionLikedActivity extends CustomTitleActivity implements OnClickListener, ITaskFinishListener {

	public static final String KEY_FROM_SPLASH_ACTIVITY = "from_splash_activity";
	public static final String KEY_FROM_PERSONAL_CENTER_ACTIVITY = "from_personal_center_activity";
	public static final String ACTION_UPDATE_LIKED_PARTITION = "com.sina.book.action.UPDATELIKEDPARTITION";

	private PartitionLikedAdapter mAdapter;
	private ListView mListView;

	private View mProgressView;
	private View mErrorView;
	private Button mRetryBtn;

	private View mBeginReadView;
	private TextView mBeginReadBtn;

	private ArrayList<String> mCateId = new ArrayList<String>();
	private ArrayList<String> mOriginalCateId = new ArrayList<String>();

	private String mFromType;

	public static void launch(Context c, String type) {
		Intent intent = new Intent();
		intent.setClass(c, PartitionLikedActivity.class);
		intent.putExtra("fromType", type);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		c.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_like_partition);
		initIntent();
		initTitle();
		initView();
		initData();
		initListener();
	}

	private void initTitle() {
		TextView middleView = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middleView.setText(R.string.select_your_liked);
		setTitleMiddle(middleView);

		if (KEY_FROM_PERSONAL_CENTER_ACTIVITY.equals(mFromType)) {
			View leftView = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
			setTitleLeft(leftView);
		}
	}

	@Override
	public void onClickLeft() {

		finish();
	}

	private void initIntent() {
		Intent intent = getIntent();
		mFromType = intent.getStringExtra("fromType");
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.partition_liked_listview);
		mBeginReadView = findViewById(R.id.partition_liked_begin_read);
		mBeginReadBtn = (TextView) mBeginReadView.findViewById(R.id.begin_read_btn);

		if (KEY_FROM_SPLASH_ACTIVITY.equals(mFromType)) {
			mBeginReadBtn.setText(getResources().getString(R.string.begin_read_text));
		} else {
			mBeginReadBtn.setText(getResources().getString(R.string.ok));
		}

		mProgressView = findViewById(R.id.progress_layout);
		mErrorView = findViewById(R.id.error_layout);
		mRetryBtn = (Button) mErrorView.findViewById(R.id.retry_btn);
		mRetryBtn.setOnClickListener(this);

		mAdapter = new PartitionLikedAdapter(this);
		mListView.setAdapter(mAdapter);
	}

	private void initData() {

		if (!HttpUtil.isConnected(this) && KEY_FROM_SPLASH_ACTIVITY.equals(mFromType)) {
			enterMainActivity();
		}

		if (!HttpUtil.isConnected(this)) {
			showErrorView();
		} else {
			dismissErrorView();
			showProgressView();
			requestPartitionLikedData();
		}
	}

	private void initListener() {
		mBeginReadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (LoginUtil.isValidAccessToken(PartitionLikedActivity.this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					if (KEY_FROM_SPLASH_ACTIVITY.equals(mFromType)) {
						longToast(R.string.no_access_token);
						enterMainActivity();
					} else {
						longToast(R.string.no_access_token);
						finish();
					}

				} else {
					mCateId = mAdapter.getCateId();

					if (mCateId == null || mCateId.size() == 0) {
						showDialog();
					} else {
						requestPartitionUpdateData();
					}
				}

				recordEvents();
			}
		});
	}

	/**
	 * 统计选择的分类
	 */
	private void recordEvents() {
		ArrayList<String> selectedNames = mAdapter.getSelectedCateNames();
		if (selectedNames != null && !selectedNames.isEmpty()) {

			for (String name : selectedNames) {
				String tag = UserActionUtil.getActionCateTag(name);
				if (!TextUtils.isEmpty(tag)) {
					UserActionManager.getInstance().recordEvent(tag + "Like");
				}
			}
		}
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		dismissProgressView();

		if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof PartitionLikedResult) {
				PartitionLikedResult result = (PartitionLikedResult) taskResult.retObj;
				getOriginalCateId(result.getItems());
				mAdapter.setList(result.getItems());
				mAdapter.notifyDataSetChanged();

				String uid;
				if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
						&& !TextUtils.isEmpty(uid = LoginUtil.getLoginInfo().getUID())) {
					String key = uid + StorageUtil.KEY_SELECT_PARTITION;
					StorageUtil.saveBoolean(key, true);
				}
			}
		} else {
			if (KEY_FROM_SPLASH_ACTIVITY.equals(mFromType)) {
				enterMainActivity();
			}
			showErrorView();
		}
	}

	private void requestPartitionLikedData() {
		String reqUrl = ConstantData.addLoginInfoToUrl(ConstantData.URL_LIKED_PARTITION);

		RequestTask reqTask = new RequestTask(new PartitionLikedParser());
		reqTask.setTaskFinishListener(this);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	private void requestPartitionUpdateData() {
		if (!HttpUtil.isConnected(this)) {
			if (KEY_FROM_PERSONAL_CENTER_ACTIVITY.equals(mFromType)) {
				shortToast(R.string.no_select_sucess);
				return;
			}
		}
		// 防止空指针
		if (mCateId == null || mCateId.size() == 0) {
			if (KEY_FROM_SPLASH_ACTIVITY.equals(mFromType)) {
				enterMainActivity();
			} else {
				finish();
			}
		}
		String cateId;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mCateId.size(); i++) {
			sb.append(mCateId.get(i));
			if (i != (mCateId.size() - 1)) {
				sb.append(",");
			}
		}
		cateId = sb.toString();

		String reqUrl = String.format(ConstantData.URL_UPDATE_LIKED_PARTITION, cateId);
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);

		RequestTask reqTask = new RequestTask(new SimpleParser());

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);

		if (KEY_FROM_SPLASH_ACTIVITY.equals(mFromType)) {
			enterMainActivity();
		} else {
			if (isUpdateLikedPartition()) {
				// 发送更新喜欢分类的广播
				Intent intent = new Intent();
				intent.setAction(ACTION_UPDATE_LIKED_PARTITION);
				sendBroadcast(intent);
			}
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.retry_btn:
			initData();
			break;

		default:
			break;
		}
	}

	public void showProgressView() {
		mProgressView.setVisibility(View.VISIBLE);
	}

	public void dismissProgressView() {
		mProgressView.setVisibility(View.GONE);
	}

	public void showErrorView() {
		mErrorView.setVisibility(View.VISIBLE);
	}

	public void dismissErrorView() {
		mErrorView.setVisibility(View.GONE);
	}

	private void showDialog() {
		String msg = getResources().getString(R.string.select_partition_tips);
		CommonDialog.show(this, msg, new CommonDialog.DefaultListener() {
			@Override
			public void onRightClick(DialogInterface dialog) {
				dialog.dismiss();
				requestPartitionUpdateData();
			}
		});
	}

	private void enterMainActivity() {
		MainActivity.launch(this);
		finish();
	}

	private void longToast(int resId) {
		Toast.makeText(this, getResources().getString(resId), Toast.LENGTH_LONG).show();
	}

	private boolean isUpdateLikedPartition() {
		if (mOriginalCateId == null && (mCateId == null || mCateId.size() == 0)) {
			return false;
		}
		if ((mOriginalCateId != null && mOriginalCateId.size() > 0) && mCateId == null) {
			return true;
		}
		if (!mOriginalCateId.equals(mCateId)) {
			return true;
		}

		return false;
	}

	private void getOriginalCateId(List<PartitionLikedItem> list) {
		ArrayList<PartitionLikedItem> items = new ArrayList<PartitionLikedItem>();
		if (list == null) {
			return;
		}
		items.addAll(list);
		if (mOriginalCateId == null) {
			mOriginalCateId = new ArrayList<String>();
		}
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getIsFavorite()) {
				mOriginalCateId.add(items.get(i).getId());
			}
		}
	}

}