package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.BuyDetail;
import com.sina.book.data.ConstantData;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.view.BuyDetailDialog;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.Util;
import com.sina.weibo.sdk.utils.MD5;

public class BuyDetailAdapter extends ListAdapter<BuyDetail> {
	private Context mContext;
	private ViewHolder mHolder;
	private BuyDetail fixHead = new BuyDetail("购买时间", "作品", "详情");;
	private BuyDetailDialog mDetailDialog;

	public BuyDetailAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		int count = super.getCount();
		if (count == 0) {
			return count;
		} else {
			return count + 1;
		}
	}

	@Override
	public Object getItem(int position) {
		if (position == 0) {
			return fixHead;
		} else {
			return super.getItem(position - 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		mHolder = (ViewHolder) convertView.getTag();
		final BuyDetail rdetail = (BuyDetail) getItem(position);
		int color1;
		int color2;
		if (position == 0) {
			mHolder.book.setOnClickListener(null);
			mHolder.detailTitle.setVisibility(View.VISIBLE);
			mHolder.detail.setVisibility(View.GONE);
			mHolder.detailTotal.setOnClickListener(null);
			color1 = mContext.getResources().getColor(R.color.consume_item_title);
			color2 = color1;
		} else {
			mHolder.book.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					// TODO: 点击消费书籍，进入书籍详情页
					String uid = LoginUtil.getLoginInfo().getUID();
					String bid = rdetail.getBookId();

					StringBuffer buffer = new StringBuffer();
					buffer.append(uid);
					buffer.append("|");
					buffer.append(bid);
					buffer.append("|");
					buffer.append(ConstantData.BUYDETETAIL_KEY);
					String prikey = MD5.hexdigest(buffer.toString());

					Book b = new Book();
					b.setBookId(bid);

					//
					String eventKey = "消费记录_已购买_" + Util.formatNumber(position + 1);
					String eventExtra = "个人中心Book";
					BookDetailActivity.launch(mContext, b, prikey, eventKey, eventExtra);
				}
			});
			mHolder.detailTitle.setVisibility(View.GONE);
			mHolder.detail.setVisibility(View.VISIBLE);
			mHolder.detailTotal.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					popDialog(rdetail, rdetail.getBookId(), rdetail.getTitle(), rdetail.getPayType());
				}
			});
			color1 = mContext.getResources().getColor(R.color.consume_item_normal);
			color2 = mContext.getResources().getColor(R.color.consume_item_light);
		}

		if (position == 0) {
			if (mHolder.divide1.getLayoutParams() != null && mHolder.divide2.getLayoutParams() != null) {
				MarginLayoutParams ll1 = (MarginLayoutParams) mHolder.divide1.getLayoutParams();
				ll1.topMargin = PixelUtil.dp2px(12);
				MarginLayoutParams ll2 = (MarginLayoutParams) mHolder.divide2.getLayoutParams();
				ll2.topMargin = PixelUtil.dp2px(12);
			}
		} else if (position == getCount() - 1) {
			if (mHolder.divide1.getLayoutParams() != null && mHolder.divide2.getLayoutParams() != null) {
				MarginLayoutParams ll1 = (MarginLayoutParams) mHolder.divide1.getLayoutParams();
				ll1.bottomMargin = PixelUtil.dp2px(12);
				MarginLayoutParams ll2 = (MarginLayoutParams) mHolder.divide2.getLayoutParams();
				ll2.bottomMargin = PixelUtil.dp2px(12);
			}
		} else {
			if (mHolder.divide1.getLayoutParams() != null && mHolder.divide2.getLayoutParams() != null) {
				MarginLayoutParams ll1 = (MarginLayoutParams) mHolder.divide1.getLayoutParams();
				ll1.topMargin = 0;
				ll1.bottomMargin = 0;
				MarginLayoutParams ll2 = (MarginLayoutParams) mHolder.divide2.getLayoutParams();
				ll2.topMargin = 0;
				ll2.bottomMargin = 0;
			}
		}
		mHolder.time.setTextColor(color1);
		mHolder.book.setTextColor(color2);
		mHolder.time.setText(rdetail.getTime());
		mHolder.detailTitle.setText(rdetail.getDetail());
		mHolder.book.setText(rdetail.getTitle());
		return convertView;

	}

	@Override
	protected List<BuyDetail> createList() {
		return new ArrayList<BuyDetail>();
	}

	protected void popDialog(final BuyDetail buyDetail, String bookId, String bookName, int payType) {
		// if (mDetailDialog == null) {
		mDetailDialog = new BuyDetailDialog(mContext, bookId, bookName, payType);
		// }
		mDetailDialog.show(buyDetail);
	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_buy_detail_item, null);
		ViewHolder holder = new ViewHolder();
		holder.time = (TextView) itemView.findViewById(R.id.time);
		holder.book = (TextView) itemView.findViewById(R.id.book);
		holder.detailTotal = itemView.findViewById(R.id.detail_total);
		holder.detailTitle = (TextView) itemView.findViewById(R.id.detail_title);
		holder.detail = (ImageView) itemView.findViewById(R.id.detail);
		holder.divide1 = itemView.findViewById(R.id.divide1);
		holder.divide2 = itemView.findViewById(R.id.divide2);
		itemView.setTag(holder);
		return itemView;
	}

	protected class ViewHolder {
		public TextView time;
		public TextView book;
		public View detailTotal;
		public TextView detailTitle;
		public ImageView detail;
		public View divide1;
		public View divide2;
	}
}
