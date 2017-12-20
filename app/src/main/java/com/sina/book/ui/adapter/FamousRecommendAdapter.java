package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.FamousRecommend;
import com.sina.book.image.ImageLoader;

/**
 * 名人书单adapter
 * 
 * @author Tsimle
 * 
 */
public class FamousRecommendAdapter extends ListAdapter<FamousRecommend> {
	private Context mContext;
	private ViewHolder mHolder;

	public FamousRecommendAdapter(Context context) {
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == mDataList.size()) {// 获取更多信息
			if (!IsAdding()) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_generic_more, null);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_generic_loading, null);
			}
			return convertView;
		} else {
			if (convertView == null || convertView.getTag() == null) {
				convertView = createView();
			}
			mHolder = (ViewHolder) convertView.getTag();
			FamousRecommend famousRecommend = (FamousRecommend) getItem(position);
			if (famousRecommend.getHeadUrl() != null && !famousRecommend.getHeadUrl().contains("http://")) {
				famousRecommend.setHeadUrl(null);
			}
			ImageLoader.getInstance().load(famousRecommend.getHeadUrl(), mHolder.headerImg,
					ImageLoader.getDefaultAvatar(), ImageLoader.getDefaultMainAvatar());

			mHolder.name.setText(famousRecommend.getScreenName());
			mHolder.num.setText(mContext.getString(R.string.booklist_prefix) + famousRecommend.getListCount());
			mHolder.desc.setText(famousRecommend.getDesc());
		}
		return convertView;
	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_famous_item, null);
		ViewHolder holder = new ViewHolder();
		holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
		holder.name = (TextView) itemView.findViewById(R.id.name);
		holder.num = (TextView) itemView.findViewById(R.id.num);
		holder.desc = (TextView) itemView.findViewById(R.id.desc);
		itemView.setTag(holder);
		return itemView;
	}

	@Override
	protected List<FamousRecommend> createList() {
		return new ArrayList<FamousRecommend>();
	}

	protected class ViewHolder {
		public ImageView headerImg;
		public TextView name;
		public TextView num;
		public TextView desc;
	}
}
