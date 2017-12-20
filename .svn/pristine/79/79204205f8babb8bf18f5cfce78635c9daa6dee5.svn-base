package com.sina.book.ui;

import org.apache.http.HttpStatus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PaymentMonthBookResult;
import com.sina.book.data.util.IListDataChangeListener;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.parser.PaymentMonthBookParser;
import com.sina.book.ui.adapter.PaymentMonthBookListAdapter;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.Util;

/**
 * 包月书单
 * 
 * @author YangZhanDong
 * 
 */
public class PaymentMonthBookListActivity extends CustomTitleActivity implements OnItemClickListener,
		ITaskFinishListener, OnClickListener, IXListViewListener, IListDataChangeListener {

	public static final String KEY_PAYMENT_MONTH_TYPE = "payment_month_type";
	public static final String KEY_PAYMENT_MONTH_ID = "payment_month_id";
	public static final String KEY_PAYMENT_MONTH_OPEN = "payment_month_open";
	public static final int KEY_RESULT_OK = 1;

	/** 包月书单列表. */
	private XListView mPaymentMonthBookListView;

	/** 包月书单Adapter. */
	private PaymentMonthBookListAdapter mPaymentMonthBooksAdapter;

	/** 开通或者续订按钮. */
	private Button mPaymentMonthOpenBtn;

	/** 进度条. */
	private View mProgress;

	/** 网络错误. */
	private View mError;

	/** 网络错误，重试按钮. */
	private Button mRetryBtn;

	// private int mCurrentPage;
	private String mPaymentType;
	private String mPaymentId;
	private String mPaymentOpen;

	// 包月名称
	private String mSuiteName;

	@Override
	protected void init(Bundle savedInstanceState) {

		setContentView(R.layout.act_payment_month_booklist);

		initIntent();
		initTitle();
		initView();
		setListener();
		initData();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (PaymentMonthMineUtil.getInstance().isNeedRefreshPaymentBooklist(mPaymentId, mPaymentOpen)) {
			mPaymentMonthOpenBtn.setEnabled(false);
			mPaymentMonthOpenBtn.setText(getString(R.string.has_opened_payment_month));
			mPaymentMonthOpenBtn
					.setTextColor(this.getResources().getColor(R.color.payment_month_has_opened_font_color));
			mPaymentMonthOpenBtn.setBackgroundDrawable(this.getResources().getDrawable(
					R.drawable.payment_month_has_opened_btn_normal));
		}
	}

	@Override
	protected void onDestroy() {
		PaymentMonthMineUtil.getInstance().release(this);
		super.onDestroy();
	}

	private void initIntent() {
		Intent intent = getIntent();
		mPaymentType = intent.getStringExtra(KEY_PAYMENT_MONTH_TYPE);
		mPaymentId = intent.getStringExtra(KEY_PAYMENT_MONTH_ID);
		mPaymentOpen = intent.getStringExtra(KEY_PAYMENT_MONTH_OPEN);
	}

	private void initTitle() {

		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (mPaymentType == null) {
			middle.setText(getString(R.string.general_payment_month_booklist));
		} else {
			middle.setText(mPaymentType);
		}
		setTitleMiddle(middle);

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

	}

	private void initView() {

		mPaymentMonthOpenBtn = (Button) findViewById(R.id.payment_month_booklist_btn);
		mPaymentMonthBookListView = (XListView) findViewById(R.id.payment_month_books_listview);
		mProgress = (View) findViewById(R.id.payment_month_booklist_progress);
		mError = (View) findViewById(R.id.error_layout);
		mRetryBtn = (Button) mError.findViewById(R.id.retry_btn);

		if (mPaymentOpen.equals("Y")) {
			mPaymentMonthOpenBtn.setEnabled(false);
			mPaymentMonthOpenBtn.setText(getString(R.string.has_opened_payment_month));
			mPaymentMonthOpenBtn
					.setTextColor(this.getResources().getColor(R.color.payment_month_has_opened_font_color));
			mPaymentMonthOpenBtn.setBackgroundDrawable(this.getResources().getDrawable(
					R.drawable.payment_month_has_opened_btn_normal));
		}

		mRetryBtn.setOnClickListener(this);

		mPaymentMonthBookListView.setPullRefreshEnable(false);
		mPaymentMonthBookListView.setPullLoadEnable(false);
		mPaymentMonthBookListView.setOnItemClickListener(this);
		mPaymentMonthBookListView.setXListViewListener(this);

		PaymentMonthMineUtil.getInstance().setDataChangeListener(this);

		mPaymentMonthBooksAdapter = new PaymentMonthBookListAdapter(this);
		mPaymentMonthBookListView.setAdapter(mPaymentMonthBooksAdapter);
	}

	private void setListener() {

		mPaymentMonthOpenBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					LoginDialog.launch(PaymentMonthBookListActivity.this,

					new LoginDialog.LoginStatusListener() {

						@Override
						public void onSuccess() {
							PaymentMonthMineUtil.getInstance().openPaymentMonth(PaymentMonthBookListActivity.this,
									mPaymentId, mPaymentOpen, mPaymentType);
						}

						@Override
						public void onFail() {
						}
					});
					return;
				}
				PaymentMonthMineUtil.getInstance().openPaymentMonth(PaymentMonthBookListActivity.this, mPaymentId,
						mPaymentOpen, mPaymentType);
			}
		});
	}

	private void initData() {
		if (!HttpUtil.isConnected(this)) {
			showErrorView();
		} else {
			dismissErrorView();
			showProgressView();
			requestPaymentBooks(1);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position -= mPaymentMonthBookListView.getHeaderViewsCount();

		if (position >= 0 && position < mPaymentMonthBooksAdapter.getCount()) {
			//
			String eventKey = mSuiteName + "_01_" + Util.formatNumber(position + 1);
			String eventExtra = "包月厅";
			Book book = (Book) mPaymentMonthBooksAdapter.getItem(position);
			BookDetailActivity.launch(this, book, eventKey, eventExtra);
		}
	}

	@Override
	public void onClickLeft() {

		finish();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {

		RequestTask task = (RequestTask) taskResult.task;
		int page = (Integer) task.getExtra();

		if (page == 1) {
			dismissProgressView();
		} else {
			mPaymentMonthBookListView.stopLoadMore();
		}

		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			PaymentMonthBookResult result = (PaymentMonthBookResult) taskResult.retObj;
			if (result != null && result.getTotal() > 0) {
				// mCurrentPage = page;
				String suiteName = result.getSuiteName();
				if (suiteName != null) {
					mSuiteName = suiteName;
				}
				mPaymentMonthBooksAdapter.setCurrentPage(page);

				mPaymentMonthBooksAdapter.addList(result.getItem());
				mPaymentMonthBooksAdapter.setTotal(result.getTotal());

				if (mPaymentMonthBooksAdapter.hasMore()) {
					mPaymentMonthBookListView.setPullLoadEnable(true);
				} else {
					mPaymentMonthBookListView.setPullLoadEnable(false);
				}

				mPaymentMonthBooksAdapter.notifyDataSetChanged();
				return;
			}
		} else {
			if (page == 1) {
				showErrorView();
			} else {
				shortToast(R.string.network_unconnected);
			}
		}
	}

	private void requestPaymentBooks(int page) {
		// int perpage = 20;
		String reqUrl = String.format(ConstantData.URL_SUITE_BOOK_LIST, mPaymentId, page, ConstantData.PAGE_SIZE);

		RequestTask reqTask = new RequestTask(new PaymentMonthBookParser());
		reqTask.setTaskFinishListener(this);
		reqTask.setExtra(page);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);

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
		mProgress.setVisibility(View.VISIBLE);
	}

	public void dismissProgressView() {
		mProgress.setVisibility(View.GONE);
	}

	public void showErrorView() {
		mError.setVisibility(View.VISIBLE);
	}

	public void dismissErrorView() {
		mError.setVisibility(View.GONE);
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mPaymentMonthBookListView.stopLoadMore();
			return;
		}

		if (mPaymentMonthBooksAdapter.hasMore()) {
			requestPaymentBooks(mPaymentMonthBooksAdapter.getCurrentPage() + 1);
		}
	}

	@Override
	public void dataChange() {
		mPaymentMonthOpenBtn.setEnabled(false);
		mPaymentMonthOpenBtn.setText(getString(R.string.has_opened_payment_month));
		mPaymentMonthOpenBtn.setTextColor(this.getResources().getColor(R.color.payment_month_has_opened_font_color));
		mPaymentMonthOpenBtn.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.payment_month_has_opened_btn_normal));

		Intent intent = new Intent();
		setResult(KEY_RESULT_OK, intent);
	}

}