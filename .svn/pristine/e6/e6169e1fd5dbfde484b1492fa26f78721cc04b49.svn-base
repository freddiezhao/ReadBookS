package com.sina.book.ui;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.FamousRecommendDetail;
import com.sina.book.data.ObjResult;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.FamousDetailParser;
import com.sina.book.ui.adapter.FamousBookAdapter;
import com.sina.book.util.Util;

/**
 * 名人书单
 * 
 * @author Tsimle
 * 
 */
public class FamousDetailActivity extends CustomTitleActivity implements OnItemClickListener, ITaskFinishListener {
	private static final String USER_ID = "uid";
	private static final String USER_NAME = "uname";

	private String mUid;
	private String mUname;

	private FamousRecommendDetail mDetail;
	/**
	 * 书籍adapter
	 */
	private FamousBookAdapter mAdapter;

	private ImageView mUserHeadImg;
	private TextView mDesc;
	private TextView mNum;
	private ListView mBookList;
	private View mProgressView;
	private View mErrorView;

	public static void launch(Context c, String uid, String uname) {
		Intent intent = new Intent();
		intent.setClass(c, FamousDetailActivity.class);
		intent.putExtra(USER_ID, uid);
		intent.putExtra(USER_NAME, uname);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		c.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_famous_detail);
		initIntent();
		initTitle();
		initViews();
		reqDetailInfo();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position -= mBookList.getHeaderViewsCount();
		if (position >= 0 && position < mAdapter.getCount()) {
			//
			String eventKey = "书单_01_" + Util.formatNumber(position + 1);
			String eventExtra = mUname;
			BookDetailActivity.launch(this, ((Book) mAdapter.getItem(position)), eventKey, eventExtra);
		}
	}

	@Override
	public void onClickLeft() {
		finish();
		super.onClickLeft();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		// whatever
		mProgressView.setVisibility(View.GONE);

		if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof ObjResult<?>) {
				@SuppressWarnings("unchecked")
				ObjResult<FamousRecommendDetail> result = (ObjResult<FamousRecommendDetail>) taskResult.retObj;
				if (result.isSucc()) {
					mDetail = result.getObj();
					setDetailInfo();
					return;
				}
			}
		}

		// not success
		mErrorView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void retry() {
		mErrorView.setVisibility(View.GONE);
		reqDetailInfo();
	}

	/**
	 * 初始化Intent
	 */
	private void initIntent() {
		Intent intent = getIntent();
		mUid = intent.getStringExtra(USER_ID);
		mUname = intent.getStringExtra(USER_NAME);
	}

	/**
	 * 初始化标题
	 */
	private void initTitle() {
		TextView middleTv = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (mUname != null && !mUname.equalsIgnoreCase("")) {
			middleTv.setText(mUname + "的书单");
		} else {
			middleTv.setText("名人书单");
		}
		setTitleMiddle(middleTv);

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);
	}

	/**
	 * 初始化UI控件
	 */
	private void initViews() {
		RelativeLayout header = (RelativeLayout) LayoutInflater.from(mContext).inflate(
				R.layout.vw_famous_detail_header, null);

		mUserHeadImg = (ImageView) header.findViewById(R.id.user_head_img);
		mDesc = (TextView) header.findViewById(R.id.desc);
		mNum = (TextView) header.findViewById(R.id.num);

		mBookList = (ListView) findViewById(R.id.book_list);
		mBookList.setOnItemClickListener(this);
		mProgressView = findViewById(R.id.rl_progress);
		mErrorView = findViewById(R.id.error_layout);

		mBookList.addHeaderView(header);
		mAdapter = new FamousBookAdapter(this);
		mBookList.setAdapter(mAdapter);
	}

	private void setDetailInfo() {
		ImageLoader.getInstance().load(mDetail.getHeadUrl(), mUserHeadImg, ImageLoader.TYPE_ROUND_PIC,
				ImageLoader.getDefaultPic());
		mDesc.setText(mDetail.getIntro());
		mNum.setText("书单：" + mDetail.getListCount() + "本");

		if (mDetail.getBooks() != null) {
			mAdapter.setList(mDetail.getBooks());
			mAdapter.setTotal(mDetail.getBooks().size());
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 发送搜索书本请求
	 */
	private void reqDetailInfo() {
		String reqUrl = String.format(ConstantData.URL_USER_DETAIL, mUid, 1, 1, 20);
		RequestTask reqTask = new RequestTask(new FamousDetailParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
		mProgressView.setVisibility(View.VISIBLE);
	}

}
