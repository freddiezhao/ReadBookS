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
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.ui.widget.XListViewHeader;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;

/**
 * 我的书房的收藏页面
 * 
 * @author MarkMjw
 * @date 2012-12-24 修改： YangZhanDong
 * @date 2013-03-08
 */
public class MyPurchasedActivity extends CustomTitleActivity implements OnClickListener, OnItemClickListener,
		IListDataChangeListener, IXListViewListener {

	public static final String TAG = "MyPurchasedActivity";

	/** 购买列表. */
	private XListView mPurchasedListView;

	/** 购买书籍Adapter */
	private PurchasedBookAdapter mPurchasedBooksAdapter;

	/** 去书城view */
	private View mGoBookLayout;

	/** 去书城按钮. */
	private Button mGoBookStoreBtn;

	/** 进度条. */
	private View mProgressView;

	/** 网络错误. */
	private View mErrorView;

	/** 网络错误，重试按钮 */
	private Button mRetryBtn;

	/** 是否从新浪书城返回 */
	private boolean mIsSinaBookStoreBack;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_my_purchased);

		initTitle();
		initView();
		initData();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mIsSinaBookStoreBack) {
			initData();
			mIsSinaBookStoreBack = false;
		}

	}

	@Override
	public void onDestroy() {

		PurchasedBookList.getInstance().cleanAndNotify();
		super.onDestroy();
	}

	private void initTitle() {

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middle.setText("我的购买");
		setTitleMiddle(middle);
	}

	@Override
	public void onClickLeft() {

		finish();
	}

	private void initView() {

		mPurchasedListView = (XListView) findViewById(R.id.book_home_listview);
		mGoBookLayout = findViewById(R.id.to_bookstore);
		mGoBookStoreBtn = (Button) mGoBookLayout.findViewById(R.id.book_home_btn);
		mProgressView = findViewById(R.id.book_home_progress);
		mErrorView = findViewById(R.id.error_layout);
		mRetryBtn = (Button) mErrorView.findViewById(R.id.retry_btn);

		mGoBookStoreBtn.setOnClickListener(this);
		mRetryBtn.setOnClickListener(this);

		mPurchasedListView.setPullRefreshEnable(true, XListViewHeader.TYPE_PULL_REFRESH);
		mPurchasedListView.setPullLoadEnable(false);
		mPurchasedListView.setXListViewListener(this);
		mPurchasedListView.setOnItemClickListener(this);
		mPurchasedListView.setRefreshTime(getUpdateTime());

		PurchasedBookList.getInstance().setDataChangeListener(this);

		mPurchasedBooksAdapter = new PurchasedBookAdapter(this, mPurchasedListView);
		mPurchasedListView.setAdapter(mPurchasedBooksAdapter);
	}

	private String getUpdateTime() {
		String timeStr = getString(R.string.do_not_update);
		long time = StorageUtil.getLong(StorageUtil.KEY_UPDATE_PUURCHASED);
		if (-1 != time) {
			timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(time));
		}
		return timeStr;
	}

	private void initData() {

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

	private void updateData() {
		if (!HttpUtil.isConnected(this)) {
			Toast.makeText(this, R.string.network_unconnected, Toast.LENGTH_SHORT).show();
			mPurchasedListView.stopRefresh();
			return;
		} else {
			if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				ListCacheUtil.requestPurchasedBooks();
				StorageUtil.saveLong(StorageUtil.KEY_UPDATE_PUURCHASED, System.currentTimeMillis());
				mPurchasedListView.setRefreshTime(getUpdateTime());
			}
		}
	}

	private void loadData() {
		mPurchasedBooksAdapter.clearList();

		mPurchasedBooksAdapter.setTotal(PurchasedBookList.getInstance().getTotal());
		mPurchasedBooksAdapter.addList(PurchasedBookList.getInstance().getList());
		mPurchasedBooksAdapter.notifyDataSetChanged();

		if (mPurchasedBooksAdapter.isEmpty()) {
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
		updateData();
	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mPurchasedListView.stopLoadMore();
			return;
		}

		// 每页取10条数据
		int perPage = 10;

		int total = PurchasedBookList.getInstance().getTotal();
		int size = PurchasedBookList.getInstance().size();

		if (size >= total) {
			Toast.makeText(this, R.string.bookhome_no_more_data, Toast.LENGTH_SHORT).show();
			mPurchasedListView.stopLoadMore();
			return;
		}

		int page = (int) Math.ceil(size * 1.00 / perPage) + 1;
		ListCacheUtil.requestPurchasedBooks(page, perPage);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.book_home_login_weibo_view:
			// 点击banner进入个人中心界面
			if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				LoginDialog.launch(this, null);
			}
			break;

		case R.id.book_home_btn:
			// 去书城
			mIsSinaBookStoreBack = true;

			MainTabActivity.launch(this);
			break;

		case R.id.retry_btn:
			initData();
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
			BookDetailActivity.launch(
					this,
					PurchasedBookList.getInstance().purchasedBook2Book(
							(PurchasedBook) mPurchasedBooksAdapter.getItem(position)));
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

		if (mPurchasedBooksAdapter.hasMore2()) {
			mPurchasedListView.setPullLoadEnable(true);
		} else {
			mPurchasedListView.setPullLoadEnable(false);
		}
	}

	/**
	 * 菜单栏的点击事件
	 * 
	 * @param view
	 */
	public void menuLayoutOnClick(View view) {
		// 暂不处理，此方法仅用于拦截其点击事件
		LogUtil.d(TAG, "menuLayoutOnClick");
	}

}