package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sina.book.R;
import com.sina.book.data.RecommendBannerItem;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.CommonRecommendActivity;
import com.sina.book.ui.PaymentMonthDetailActivity;
import com.sina.book.ui.RecommendDetailListAativity;
import com.sina.book.ui.RecommendWebUrlActivity;
import com.sina.book.ui.TopicActivity;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.Util;

public class ImageAdapter extends BaseAdapter {
	private List<RecommendBannerItem> mDatas;
	private Context mContext;

	public ImageAdapter(Context context) {
		mDatas = new ArrayList<RecommendBannerItem>();
		mContext = context;
	}

	public void addData(List<RecommendBannerItem> data) {
		mDatas.addAll(data);
	}

	public void add2First(RecommendBannerItem item) {
		mDatas.add(0, item);
	}

	public void add2End(RecommendBannerItem item) {
		mDatas.add(item);
	}

	public void clearData() {
		mDatas.clear();
	}

	@Override
	public int getCount() {
		if (mDatas == null || mDatas.size() == 0) {
			return 0;
		} else {
			return mDatas.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mDatas.size()) {
			return mDatas.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (null == convertView || null == convertView.getTag()) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();
		if (null != holder) {
			RecommendBannerItem item = (RecommendBannerItem) getItem(position);

			String type = item.getType();
			if (RecommendBannerItem.BANNER_MONTH.equals(type) || RecommendBannerItem.BANNER_STAFF.equals(type)) {
				ImageLoader.getInstance().cancelLoad(holder.imageView);
				holder.imageView.setImageResource(item.getImageResId());
			} else {
				ImageLoader.getInstance().load3(item.getImageUrl(), holder.imageView, ImageLoader.TYPE_BIG_PIC,
						ImageLoader.getDefaultHorizontalBannerPic());
			}

			holder.imageView.setOnClickListener(new ImageOnClickListener(item, position));
		}
		return convertView;
	}

	private View createView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_image_item, null);

		if (null != view) {
			ViewHolder holder = new ViewHolder();
			holder.imageView = ((ImageView) view.findViewById(R.id.imgView));

			view.setTag(holder);
		}

		return view;
	}

	private class ViewHolder {
		private ImageView imageView;
	}

	private class ImageOnClickListener implements OnClickListener {
		private RecommendBannerItem mItem;
		private int mPosition;

		public ImageOnClickListener(RecommendBannerItem item, int position) {
			mItem = item;
			mPosition = position;
		}

		@Override
		public void onClick(View v) {
			String type = mItem.getType();
			if (RecommendBannerItem.BANNER_MONTH.equals(type)) {
				Intent intent = new Intent(mContext, PaymentMonthDetailActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				mContext.startActivity(intent);

				UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AD_MONTH);

			} else if (RecommendBannerItem.BANNER_STAFF.equals(type)) {
				CommonRecommendActivity.launch(mContext, CommonRecommendActivity.EMPLOYEE_TYPE);

				UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AD_STAFF);

			} else if (RecommendBannerItem.BANNER_LIST.equals(type)) {
				RecommendDetailListAativity.launch(mContext, mItem.getBooks(), null);

				UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AD_LIST);

			} else if (RecommendBannerItem.BANNER_ONE.equals(type)) {
				if (mItem.getBooks().size() > 0) {
					String eventKey = "精选推荐_TopBanner_" + Util.formatNumber(mPosition + 1);
					BookDetailActivity.launchNew(mContext, mItem.getBooks().get(0), eventKey);

					UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AD_BOOK);
				}

			} else if (RecommendBannerItem.BANNER_TWO.equals(type)) {
				TopicActivity.launch(mContext, mItem.getTags(), mItem.getTopicId(), TopicActivity.TYPE_LATEST);

				UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AD_TOPIC);

			} else if (RecommendBannerItem.BANNER_THREE.equals(type)) {
				RecommendWebUrlActivity.launch(mContext, mItem.getUrl(), mItem.getTags());

				UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AD_MOVEMENT);
			}

		}
	}
}
