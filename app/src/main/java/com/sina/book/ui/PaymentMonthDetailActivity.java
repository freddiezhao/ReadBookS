package com.sina.book.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PaymentMonthDetail;
import com.sina.book.data.util.IListDataChangeListener;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.parser.PaymentMonthDetailParser;
import com.sina.book.ui.adapter.PaymentMonthDetailListAdapter;
import com.sina.book.util.HttpUtil;
import org.apache.http.HttpStatus;

import java.util.ArrayList;

/**
 * @Description 包月厅
 * @author YangZhanDong
 * @Date 2013-02-28
 * 
 */
public class PaymentMonthDetailActivity extends CustomTitleActivity implements
		ITaskFinishListener, OnClickListener, IListDataChangeListener {

	/** 书城端的包月厅. */
	public static final String KEY_SINA_STORE_PAYMENT = "sina_store_payment";

	/** 包月选项列表. */
	private ListView mPaymentDetailListView;

	/** 包月选项Adapter. */
	private PaymentMonthDetailListAdapter mPaymentDetailAdapter;

	/** 进度条. */
	private View mProgress;

	/** 网络错误. */
	private View mError;

	/** 网络错误，重试按钮 */
	private Button mRetryBtn;

	/** 包月厅的数据 */
	private ArrayList<PaymentMonthDetail> mLists = new ArrayList<PaymentMonthDetail>();

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_payment_month_detail);

		initTitle();
		initView();
		initData();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		PaymentMonthMineUtil.getInstance().setDataChangeListener(this);

		if (resultCode == PaymentMonthBookListActivity.KEY_RESULT_OK) {
			initData();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (PaymentMonthMineUtil.getInstance().isNeedRefreshPaymentDetail(
				mLists)) {
			initData();
		}
	}

	@Override
	protected void onDestroy() {
		PaymentMonthMineUtil.getInstance().release(this);
		super.onDestroy();
	}

	private void initTitle() {
	    View left = LayoutInflater.from(this).inflate(
				R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		TextView middle = (TextView) LayoutInflater.from(this).inflate(
				R.layout.vw_title_textview, null);
		middle.setText(getString(R.string.payment_month_detail));
		setTitleMiddle(middle);
	}

	private void initView() {

		View paymentMonthNoteView = LayoutInflater.from(this).inflate(
				R.layout.vw_payment_month_note, null);
		mPaymentDetailListView = (ListView) findViewById(R.id.payment_month_detail_listview);
		mProgress = findViewById(R.id.payment_month_detail_progress);
		mError = findViewById(R.id.error_layout);
		mRetryBtn = (Button) mError.findViewById(R.id.retry_btn);

		mPaymentDetailListView.addHeaderView(paymentMonthNoteView);
		mRetryBtn.setOnClickListener(this);

		PaymentMonthMineUtil.getInstance().setDataChangeListener(this);

		mPaymentDetailAdapter = new PaymentMonthDetailListAdapter(this);
		mPaymentDetailListView.setAdapter(mPaymentDetailAdapter);
	}

	private void initData() {
		if (!HttpUtil.isConnected(this)) {
			showErrorView();
		} else {
			dismissErrorView();
			showProgressView();

			reqPaymentMonthDetail();
		}
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	@Override
	public void onClickRight() {

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

	@SuppressWarnings("unchecked")
	@Override
	public void onTaskFinished(TaskResult taskResult) {
		dismissProgressView();

		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			mLists = (ArrayList<PaymentMonthDetail>) taskResult.retObj;
			if (mLists != null && mLists.size() > 0) {
				mPaymentDetailAdapter.setList(mLists);
				mPaymentDetailAdapter.notifyDataSetChanged();
			}
		} else {
			showErrorView();
		}

	}

	private void reqPaymentMonthDetail() {
		String reqUrl = ConstantData.URL_SUITE_LIST;
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new PaymentMonthDetailParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
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
	public void dataChange() {
		initData();
	}

}