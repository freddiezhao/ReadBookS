package com.sina.book.ui.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.PaymentMonthDetail;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.ui.BaseActivity;
import com.sina.book.ui.PaymentMonthBookListActivity;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.util.LoginUtil;

public class PaymentMonthDetailListAdapter extends ListAdapter<PaymentMonthDetail> {

	// private PaymentMonthDetailActivity mContext;
	private BaseActivity mContext;

	public PaymentMonthDetailListAdapter(BaseActivity context) {
		this.mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null || convertView.getTag() == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_payment_month_detail_list_item, null);
			holder.mType = (TextView) convertView.findViewById(R.id.payment_month_type);
			holder.mOpen = (Button) convertView.findViewById(R.id.payment_month_btn);
			holder.mDetail = (TextView) convertView.findViewById(R.id.payment_month_text);
			holder.mSeeList = (Button) convertView.findViewById(R.id.payment_month_see_list_btn);
			holder.mTimeRemain = (TextView) convertView.findViewById(R.id.payment_month_time_remain);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PaymentMonthDetail item = mDataList.get(position);
		holder.mType.setText(item.getPayType());
		holder.mDetail.setText(item.getPayDetail());
		if (item.getPayOpen().equals("N")) {
			holder.mOpen.setEnabled(true);
			holder.mOpen.setText(mContext.getString(R.string.open_payment_month));
			holder.mOpen.setTextColor(mContext.getResources().getColor(R.color.payment_month_open_btn_color));
			holder.mTimeRemain.setText(null);
		} else if (item.getPayOpen().equals("Y")) {
			holder.mOpen.setEnabled(false);
			holder.mOpen.setText(mContext.getString(R.string.has_opened_payment_month));
			holder.mOpen.setTextColor(mContext.getResources().getColor(R.color.payment_month_opened_btn_color));
			holder.mTimeRemain.setText(mContext.getString(R.string.to) + getTimeRemain(item.getEndTime()));
		}

		ClickListener listener = new ClickListener(item);
		holder.mOpen.setOnClickListener(listener);
		holder.mSeeList.setOnClickListener(listener);

		return convertView;
	}

	@Override
	protected List<PaymentMonthDetail> createList() {
		return new ArrayList<PaymentMonthDetail>();
	}

	private class ViewHolder {
		public TextView mType;
		public Button mOpen;
		public Button mSeeList;
		public TextView mDetail;
		public TextView mTimeRemain;

	}

	private class ClickListener implements View.OnClickListener {

		private PaymentMonthDetail mDetail;

		public ClickListener(PaymentMonthDetail detail) {

			this.mDetail = detail;
		}

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.payment_month_btn:
				if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					LoginDialog.launch(mContext,

					new LoginDialog.LoginStatusListener() {

						@Override
						public void onSuccess() {
							PaymentMonthMineUtil.getInstance().openPaymentMonth(mContext, mDetail.getPayId(),
									mDetail.getPayOpen(), mDetail.getPayType());
						}

						@Override
						public void onFail() {
						}
					});
				} else {
					PaymentMonthMineUtil.getInstance().openPaymentMonth(mContext, mDetail.getPayId(),
							mDetail.getPayOpen(), mDetail.getPayType());
				}
				break;

			case R.id.payment_month_see_list_btn:
				Intent intent = new Intent();
				intent.setClass(mContext, PaymentMonthBookListActivity.class);
				intent.putExtra(PaymentMonthBookListActivity.KEY_PAYMENT_MONTH_TYPE, mDetail.getPayType());
				intent.putExtra(PaymentMonthBookListActivity.KEY_PAYMENT_MONTH_ID, mDetail.getPayId());
				intent.putExtra(PaymentMonthBookListActivity.KEY_PAYMENT_MONTH_OPEN, mDetail.getPayOpen());
				if (mContext instanceof Activity) {
					((Activity) mContext).startActivityForResult(intent, 0);
				}
				break;

			default:
				break;
			}

		}

	}

	private String getTimeRemain(long timeRemain) {
		String timeRemainStr = "";
		if (timeRemain != 0) {
			timeRemainStr = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(timeRemain * 1000));
		}
		return timeRemainStr;
	}

}