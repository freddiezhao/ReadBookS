package com.sina.book.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.data.PurchasedBook;
import com.sina.book.data.util.IListDataChangeListener;
import com.sina.book.data.util.ListCacheUtil;
import com.sina.book.data.util.PurchasedBookList;
import com.sina.book.ui.adapter.PurchasedBookAdapter;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.ui.widget.XListViewHeader;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

public class PurchasedActivity extends CustomTitleActivity implements OnClickListener, OnItemClickListener,
		IListDataChangeListener, IXListViewListener {
	/** 已购买列表. */
	private XListView mPurchasedListView;

	/** 已购买书籍Adapter */
	private PurchasedBookAdapter mPurchasedBooksAdapter;

	/** 登陆微博提示 */
	private TextView mLoginView;

	/** 去书城view */
	private View mGoBookLayout;

	/** 进度条. */
	private View mProgressView;

	/** 网络错误. */
	private View mErrorView;

	private String mAccessToken;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_purchased);
		initView();
		initTitle();
		showPurchasedView();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!HttpUtil.isConnected(this)) {
			dismissProgressView();
		}

		isNeedRefreshByAccessToken();

	}

	@Override
	public void onDestroy() {

		PurchasedBookList.getInstance().cleanAndNotify();
		super.onDestroy();
	}

	public void onClickLeft() {
		finish();
	}

	private void initView() {
		mPurchasedListView = (XListView) findViewById(R.id.book_home_listview);
		mLoginView = (TextView) findViewById(R.id.book_home_login_weibo_view);
		mGoBookLayout = findViewById(R.id.to_bookstore);
		// 去书城按钮
		Button goBookStoreBtn = (Button) mGoBookLayout.findViewById(R.id.book_home_btn);
		mProgressView = findViewById(R.id.book_home_progress);
		mErrorView = findViewById(R.id.error_layout);
		// 网络错误，重试按钮
		Button retryBtn = (Button) mErrorView.findViewById(R.id.retry_btn);

		mLoginView.setOnClickListener(this);
		goBookStoreBtn.setOnClickListener(this);
		retryBtn.setOnClickListener(this);

		mPurchasedListView.setPullRefreshEnable(true, XListViewHeader.TYPE_PULL_REFRESH);
		mPurchasedListView.setPullLoadEnable(false);
		mPurchasedListView.setXListViewListener(this);
		mPurchasedListView.setOnItemClickListener(this);
		mPurchasedListView.setRefreshTime(getUpdateTime());

		PurchasedBookList.getInstance().setDataChangeListener(this);

		// 默认设置已购买Adapter
		mPurchasedBooksAdapter = new PurchasedBookAdapter(this, mPurchasedListView);
		mPurchasedListView.setAdapter(mPurchasedBooksAdapter);

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
		middle.setText(R.string.book_home_tab_purchased);
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleMiddle(middle);
		setTitleLeft(left);
	}

	private String getUpdateTime() {
		String timeStr = getString(R.string.do_not_update);
		long time = StorageUtil.getLong(StorageUtil.KEY_UPDATE_PUURCHASED);
		if (-1 != time) {
			timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(time));
		}
		return timeStr;
	}

	private void showPurchasedView() {
		if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			mLoginView.setText(R.string.login_tip_purchased);
			mLoginView.setVisibility(View.VISIBLE);

			mPurchasedListView.setVisibility(View.GONE);
		} else {
			mPurchasedListView.setVisibility(View.VISIBLE);
			mLoginView.setVisibility(View.GONE);

			initData();
		}
	}

	private void initData() {
		if (PurchasedBookList.getInstance().size() > 0) {
			loadData();
		} else {
			if (!HttpUtil.isConnected(this)) {
				dismissProgressView();
				dismissEmptyView();
				showErrorView();
			} else {
				if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					dismissErrorView();
					dismissEmptyView();
					showProgressView();

					ListCacheUtil.requestPurchasedBooks();
					StorageUtil.saveLong(StorageUtil.KEY_UPDATE_PUURCHASED, System.currentTimeMillis());
					mPurchasedListView.setRefreshTime(getUpdateTime());
				}
			}
		}
	}

	private void updateData() {
		if (!HttpUtil.isConnected(this)) {
			Toast.makeText(this, R.string.network_unconnected, Toast.LENGTH_SHORT).show();
			mPurchasedListView.stopRefresh();
		} else {
			if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				ListCacheUtil.requestPurchasedBooks();
				StorageUtil.saveLong(StorageUtil.KEY_UPDATE_PUURCHASED, System.currentTimeMillis());
				mPurchasedListView.setRefreshTime(getUpdateTime());
				mPurchasedBooksAdapter.setAdding(true);
			}
		}
	}

	private void loadData() {
		mPurchasedBooksAdapter.clearList();

		mPurchasedBooksAdapter.setTotal(PurchasedBookList.getInstance().getTotal());
		mPurchasedBooksAdapter.addList(PurchasedBookList.getInstance().getList());
		mPurchasedBooksAdapter.notifyDataSetChanged();

		if (mPurchasedBooksAdapter.IsAdding()) {
			mPurchasedBooksAdapter.setAdding(false);
		}

		if (mPurchasedBooksAdapter.isEmpty() && PurchasedBookList.getInstance().getTotal() < 1) {
			dismissErrorView();
			dismissProgressView();

			if (!HttpUtil.isConnected(this)) {
				showErrorView();
				return;
			}

			if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				dismissEmptyView();
			} else {
				showEmptyView();
				mPurchasedListView.setPullRefreshEnable(false);
			}
		} else {
			mPurchasedListView.setPullRefreshEnable(true, XListViewHeader.TYPE_PULL_REFRESH);
			if (!HttpUtil.isConnected(this)) {
				shortToast(R.string.network_unconnected);
			}
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
		mGoBookLayout.setVisibility(View.VISIBLE);
	}

	public void dismissEmptyView() {
		mGoBookLayout.setVisibility(View.GONE);
	}

	@Override
	public void onRefresh() {
		if (!mPurchasedBooksAdapter.IsAdding()) {
			updateData();
		}
	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mPurchasedListView.stopLoadMore();
			return;
		}

		if (mPurchasedBooksAdapter.IsAdding()) {
			return;
		}

		// 当前页
		int lastPage = PurchasedBookList.getInstance().getPage();
		// 每页取10条数据
		int perPage = 10;

		int total = PurchasedBookList.getInstance().getTotal();
		int size = PurchasedBookList.getInstance().size();

		int curPage = lastPage + 1;
		if ((curPage * perPage) >= (total + perPage)) {
			PurchasedBookList.getInstance().setTotal(total = size);

			loadData();
			isHasMore();
		}

		if (size >= total) {
			Toast.makeText(this, R.string.bookhome_no_more_data, Toast.LENGTH_SHORT).show();
			mPurchasedListView.stopLoadMore();
			return;
		}

		ListCacheUtil.requestPurchasedBooks(curPage, perPage);
		mPurchasedBooksAdapter.setAdding(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.book_home_login_weibo_view:
			// 点击banner进入个人中心界面
			if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
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

		case R.id.book_home_btn:
			// 去书城
			MainTabActivity.launch(this);
			break;

		case R.id.retry_btn:
			showPurchasedView();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// 减去HeaderView的数量
		position -= mPurchasedListView.getHeaderViewsCount();

		if (position >= 0 && position < mPurchasedBooksAdapter.getCount()) {
			//
			String eventKey = "已购买_01_" + Util.formatNumber(position + 1);
			String eventExtra = "书架";
			BookDetailActivity.launch(
					this,
					PurchasedBookList.getInstance().purchasedBook2Book(
							(PurchasedBook) mPurchasedBooksAdapter.getItem(position)), eventKey, eventExtra);
		}

	}

	@Override
	public void dataChange() {
		dismissErrorView();
		dismissProgressView();
		dismissEmptyView();

		mPurchasedListView.stopRefresh();
		mPurchasedListView.stopLoadMore();
		loadData();

		isHasMore();
	}

	private void isHasMore() {
		if (mPurchasedBooksAdapter.hasMore2()) {
			mPurchasedListView.setPullLoadEnable(true);
		} else {
			mPurchasedListView.setPullLoadEnable(false);
		}
	}

	private void isNeedRefreshByAccessToken() {
		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
				&& LoginUtil.getLoginInfo().getAccessToken() != null) {
			if (mAccessToken != null) {
				if (!mAccessToken.equals(LoginUtil.getLoginInfo().getAccessToken())) {
					showPurchasedView();
				}
			} else {
				showPurchasedView();
			}
			mAccessToken = LoginUtil.getLoginInfo().getAccessToken();
		} else {
			if (mAccessToken != null) {
				showPurchasedView();
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
