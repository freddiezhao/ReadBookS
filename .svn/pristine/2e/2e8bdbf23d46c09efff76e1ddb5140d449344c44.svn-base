package com.sina.book.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.util.IListDataChangeListener;
import com.sina.book.data.util.ListCacheUtil;
import com.sina.book.data.util.PaymentMonthBookList;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.ui.adapter.PaymentMonthBookListAdapter;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.ui.widget.XListViewHeader;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

public class PaymentMonthActivity extends CustomTitleActivity implements OnClickListener, OnItemClickListener,
		IXListViewListener, IListDataChangeListener {
	/** 包月列表. */
	private XListView mPaymentMonthListView;

	/** 包月书籍Adapter. */
	private PaymentMonthBookListAdapter mPaymentMonthBookListAdapter;

	/** 登陆微博提示. */
	private TextView mLoginView;

	/** 查看包月详情view. */
	private View mSeeDetailLayout;

	/** 查看包月详情按钮. */
	private Button mSeeDetailBtn;

	/** 继续包月view. */
	private View mContinuePaymentLayout;

	/** 继续包月按钮. */
	private Button mContinuePaymentBtn;

	/** 进度条. */
	private View mProgressView;

	/** 网络错误. */
	private View mErrorView;

	/** 网络错误，重试按钮 */
	private Button mRetryBtn;

	private String mAccessToken;

	//
	private String mEventExtra;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_payment_month);

		Intent intent = getIntent();
		if (intent != null) {
			String eventExtra = intent.getStringExtra("eventExtra");
			if (!TextUtils.isEmpty(eventExtra)) {
				mEventExtra = eventExtra;
			}
		}

		initView();
		initTitle();
		showPaymentMonthView();
	}

	public void onClickLeft() {
		finish();
	}

	private void initView() {

		mPaymentMonthListView = (XListView) findViewById(R.id.payment_month_books_listview);
		mLoginView = (TextView) findViewById(R.id.book_home_login_weibo_view);
		mSeeDetailLayout = findViewById(R.id.payment_month_no_open);
		mSeeDetailBtn = (Button) mSeeDetailLayout.findViewById(R.id.payment_month_see_detail_btn);
		mContinuePaymentLayout = findViewById(R.id.payment_month_continue_open);
		mContinuePaymentBtn = (Button) mContinuePaymentLayout.findViewById(R.id.payment_month_continue_btn);
		mProgressView = findViewById(R.id.payment_month_progress);
		mErrorView = findViewById(R.id.error_layout);
		mRetryBtn = (Button) mErrorView.findViewById(R.id.retry_btn);

		mLoginView.setOnClickListener(this);
		mSeeDetailBtn.setOnClickListener(this);
		mContinuePaymentBtn.setOnClickListener(this);
		mRetryBtn.setOnClickListener(this);

		mPaymentMonthListView.setPullRefreshEnable(true, XListViewHeader.TYPE_PULL_REFRESH);
		mPaymentMonthListView.setPullLoadEnable(false);
		mPaymentMonthListView.setXListViewListener(this);
		mPaymentMonthListView.setOnItemClickListener(this);
		mPaymentMonthListView.setRefreshTime(getUpdateTime());

		PaymentMonthBookList.getInstance().setDataChangeListener(this);

		mPaymentMonthBookListAdapter = new PaymentMonthBookListAdapter(mContext);
		mPaymentMonthListView.setAdapter(mPaymentMonthBookListAdapter);

		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
				&& LoginUtil.getLoginInfo().getAccessToken() != null) {
			mAccessToken = LoginUtil.getLoginInfo().getAccessToken();
		}
	}

	/**
	 * 初始化标题.
	 */
	private void initTitle() {
		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middle.setText(R.string.month_pay);
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleMiddle(middle);
		setTitleLeft(left);
	}

	private String getUpdateTime() {
		String timeStr = getString(R.string.do_not_update);
		long time = StorageUtil.getLong(StorageUtil.KEY_UPDATE_PAYMENT);
		if (-1 != time) {
			timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(time));
		}
		return timeStr;
	}

	private void showPaymentMonthView() {

		if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			mLoginView.setText(R.string.login_tip_suite);
			mLoginView.setVisibility(View.VISIBLE);
			mPaymentMonthListView.setVisibility(View.GONE);
			dismissEmptyView();
			return;
		} else {
			mPaymentMonthListView.setVisibility(View.VISIBLE);
			mLoginView.setVisibility(View.GONE);

			initData();

		}
	}

	private void initData() {
		if (PaymentMonthBookList.getInstance().size() > 0) {
			loadData();
		} else {
			if (!HttpUtil.isConnected(mContext)) {
				dismissProgressView();
				dismissEmptyView();
				showErrorView();
			} else {
				if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					dismissErrorView();
					dismissEmptyView();
					showProgressView();

					ListCacheUtil.requestPaymentMonthMine();
					StorageUtil.saveLong(StorageUtil.KEY_UPDATE_PAYMENT, System.currentTimeMillis());
					mPaymentMonthListView.setRefreshTime(getUpdateTime());
				}
			}
		}
	}

	private void updateData() {
		if (!HttpUtil.isConnected(mContext)) {
			Toast.makeText(mContext, R.string.network_unconnected, Toast.LENGTH_SHORT).show();
			mPaymentMonthListView.stopRefresh();
			return;
		} else {
			if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				ListCacheUtil.requestPaymentMonthMine();
				StorageUtil.saveLong(StorageUtil.KEY_UPDATE_PAYMENT, System.currentTimeMillis());
				mPaymentMonthListView.setRefreshTime(getUpdateTime());
			}
		}
	}

	private void loadData() {
		mPaymentMonthBookListAdapter.clearList();

		mPaymentMonthBookListAdapter.setTotal(PaymentMonthBookList.getInstance().getTotal());
		mPaymentMonthBookListAdapter.addList(PaymentMonthBookList.getInstance().getPaymentMonthList());
		mPaymentMonthBookListAdapter.notifyDataSetChanged();

		if (mPaymentMonthBookListAdapter.isEmpty() && PaymentMonthBookList.getInstance().getTotal() < 1) {
			dismissErrorView();
			dismissProgressView();

			if (!HttpUtil.isConnected(mContext)) {
				showErrorView();
				return;
			}

			if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				dismissEmptyView();
			} else {
				showEmptyView();
			}
		} else {
			if (!HttpUtil.isConnected(mContext)) {
				shortToast(R.string.network_unconnected);
				return;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!HttpUtil.isConnected(mContext)) {
			dismissProgressView();
		}

		isNeedRefreshByAccessToken();
	}

	@Override
	public void onDestroy() {

		PaymentMonthBookList.getInstance().cleanList();
		super.onDestroy();
	}

	@Override
	public void onRefresh() {
		updateData();
	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(mContext)) {
			shortToast(R.string.network_unconnected);
			mPaymentMonthListView.stopLoadMore();
			return;
		}

		// 每页取20条数据
		int perPage = 20;

		int total = PaymentMonthBookList.getInstance().getTotal();
		int size = PaymentMonthBookList.getInstance().size();

		if (size >= total) {
			Toast.makeText(mContext, R.string.bookhome_no_more_data, Toast.LENGTH_SHORT).show();
			mPaymentMonthListView.stopLoadMore();
			return;
		}

		int page = (int) Math.ceil(size * 1.00 / perPage) + 1;
		ListCacheUtil.requestPaymentMonthBooks(page, perPage);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position -= mPaymentMonthListView.getHeaderViewsCount();

		if (position >= 0 && position < mPaymentMonthBookListAdapter.getCount()) {
			//
			String eventKey = PaymentMonthBookList.getInstance().getSuiteName() + "_01_"
					+ Util.formatNumber(position + 1);
			Book book = (Book) mPaymentMonthBookListAdapter.getItem(position);
			BookDetailActivity.launch(mContext, book, eventKey, "书架");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.book_home_login_weibo_view:
			// 点击banner进入个人中心界面
			if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {

				LoginDialog.launch(this, new LoginStatusListener() {

					@Override
					public void onSuccess() {

						if (!HttpUtil.isConnected(mContext)) {
							dismissProgressView();
						}

						isNeedRefreshByAccessToken();
					}

					@Override
					public void onFail() {

					}
				});
			}
			break;

		case R.id.payment_month_see_detail_btn:
			// 去包月厅

			enterPaymentMonthDetail();
			break;

		case R.id.payment_month_continue_btn:
			// 去包月厅

			enterPaymentMonthDetail();
			break;

		case R.id.retry_btn:
			showPaymentMonthView();
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

	public void showEmptyView() {

		if (!PaymentMonthMineUtil.getInstance().isContinuePaymentMonth()) {
			mSeeDetailLayout.setVisibility(View.VISIBLE);
		} else {
			mContinuePaymentLayout.setVisibility(View.VISIBLE);
		}

	}

	public void dismissEmptyView() {

		mSeeDetailLayout.setVisibility(View.GONE);
		mContinuePaymentLayout.setVisibility(View.GONE);
	}

	@Override
	public void dataChange() {

		if (mContext == null) {
			return;
		}

		if (!PaymentMonthBookList.getInstance().getNetConnect()) {
			showErrorView();
			return;
		}

		dismissErrorView();
		dismissProgressView();
		dismissEmptyView();

		mPaymentMonthListView.stopRefresh();
		mPaymentMonthListView.stopLoadMore();
		loadData();

		isHasMore();
	}

	/**
	 * 去包月厅页面
	 */
	private void enterPaymentMonthDetail() {
		Intent intent = new Intent(mContext, PaymentMonthDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(intent);
	}

	/**
	 * 判断是否有更多数据
	 */
	private void isHasMore() {
		if (mPaymentMonthBookListAdapter.hasMore2()) {
			mPaymentMonthListView.setPullLoadEnable(true);
		} else {
			mPaymentMonthListView.setPullLoadEnable(false);
		}
	}

	private void isNeedRefreshByAccessToken() {
		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
				&& LoginUtil.getLoginInfo().getAccessToken() != null) {
			if (mAccessToken != null) {
				if (!mAccessToken.equals(LoginUtil.getLoginInfo().getAccessToken())) {
					showPaymentMonthView();
				}
			} else {
				showPaymentMonthView();
			}
			mAccessToken = LoginUtil.getLoginInfo().getAccessToken();
		} else {
			if (mAccessToken != null) {
				showPaymentMonthView();
			}
			mAccessToken = null;
		}
	}

	/**
	 * 菜单栏的点击事件
	 * 
	 * @param view
	 */
	public void menuLayoutOnClick(View view) {
		// 暂不处理，此方法仅用于拦截其点击事件
	}
}
