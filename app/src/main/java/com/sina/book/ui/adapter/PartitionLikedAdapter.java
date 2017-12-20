package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.PartitionLikedItem;
import com.sina.book.image.ImageLoader;

/**
 * 选择喜欢的分类的Adapter
 * 
 * @author YangZhanDong
 * 
 */
public class PartitionLikedAdapter extends ListAdapter<PartitionLikedItem> {

	private final int ITEM_COUNT = 2;

	private Context mContext;

	private BitmapDrawable mDividerV;
	private BitmapDrawable mDividerH;

	private ArrayList<String> mCateId = new ArrayList<String>();

	public PartitionLikedAdapter(Context context) {
		this.mContext = context;

		decodeDivider();
	}

	private void decodeDivider() {
		Bitmap dividerV = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.partition_divider_line_v);
		mDividerV = new BitmapDrawable(mContext.getResources(), dividerV);
		mDividerV.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mDividerV.setDither(true);

		Bitmap dividerH = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.partition_divider_line_h);
		mDividerH = new BitmapDrawable(mContext.getResources(), dividerH);
		mDividerH.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mDividerH.setDither(true);
	}

	@Override
	public int getCount() {
		int count = 0;
		if (mDataList != null) {
			if (mDataList.size() > 0) {
				count = mDataList.size() / ITEM_COUNT;
			}
			if (mDataList.size() % ITEM_COUNT > 0) {
				count++;
			}
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		ArrayList<PartitionLikedItem> item = new ArrayList<PartitionLikedItem>();
		int length = mDataList.size();

		if (position * ITEM_COUNT < length) {
			item.add(mDataList.get(position * ITEM_COUNT));
		}
		if ((position * ITEM_COUNT + 1) < length) {
			item.add(mDataList.get(position * ITEM_COUNT + 1));
		}

		return item;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		final ViewHolder holder = (ViewHolder) convertView.getTag();

		@SuppressWarnings("unchecked")
		final ArrayList<PartitionLikedItem> item = (ArrayList<PartitionLikedItem>) getItem(position);

		// 换背景
		if ((position % 2) != 0) {
			holder.mItemLayout.get(0).setBackgroundResource(R.drawable.partition_dark_color);
			holder.mItemLayout.get(1).setBackgroundResource(R.drawable.partition_dark_color);
		} else {
			holder.mItemLayout.get(0).setBackgroundResource(R.drawable.partition_light_color);
			holder.mItemLayout.get(1).setBackgroundResource(R.drawable.partition_light_color);
		}

		holder.mDividerV.setImageDrawable(mDividerV);
		holder.mDividerH.setImageDrawable(mDividerH);

		for (int i = 0; i < item.size(); i++) {

			final int j = i;

			String imgUrl = item.get(i).getIconUrl();
			ImageLoader.getInstance().load2(imgUrl, holder.mIcon.get(i), ImageLoader.TYPE_COMMON_BIGGER_BOOK_COVER,
					ImageLoader.getPartitionLikeDefault());

			holder.mType.get(i).setText(item.get(i).getTitle());

			if (item.get(i).getIsFavorite()) {
				holder.mChoosedImg.get(i).setImageResource(R.drawable.icon_choosed);
			} else {
				holder.mChoosedImg.get(i).setImageResource(R.drawable.icon_unchoosed);
			}

			if (item.size() == 1) {
				holder.mIcon.get(item.size()).setVisibility(View.INVISIBLE);
				holder.mChoosedImg.get(item.size()).setVisibility(View.INVISIBLE);
				holder.mType.get(item.size()).setVisibility(View.INVISIBLE);
			} else if (item.size() == 2) {
				holder.mIcon.get(item.size() - 1).setVisibility(View.VISIBLE);
				holder.mChoosedImg.get(item.size() - 1).setVisibility(View.VISIBLE);
				holder.mType.get(item.size() - 1).setVisibility(View.VISIBLE);
			}

			holder.mItemLayout.get(i).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					if (item.get(j).getIsFavorite()) {
						item.get(j).setIsFavorite(false);
						holder.mChoosedImg.get(j).setImageResource(R.drawable.icon_unchoosed);
					} else {
						item.get(j).setIsFavorite(true);
						holder.mChoosedImg.get(j).setImageResource(R.drawable.icon_choosed);
					}
				}
			});
		}

		return convertView;
	}

	@Override
	protected List<PartitionLikedItem> createList() {
		return new ArrayList<PartitionLikedItem>();
	}

	private View createView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_partition_liked_item, null);

		ViewHolder holder = new ViewHolder();

		holder.mIcon.add((ImageView) view.findViewById(R.id.partition_liked_img1));
		holder.mIcon.add((ImageView) view.findViewById(R.id.partition_liked_img2));

		holder.mChoosedImg.add((ImageView) view.findViewById(R.id.partition_liked_choosed_icon1));
		holder.mChoosedImg.add((ImageView) view.findViewById(R.id.partition_liked_choosed_icon2));

		holder.mType.add((TextView) view.findViewById(R.id.partition_liked_type1));
		holder.mType.add((TextView) view.findViewById(R.id.partition_liked_type2));

		holder.mItemLayout.add((RelativeLayout) view.findViewById(R.id.partition_liked_item_layout1));
		holder.mItemLayout.add((RelativeLayout) view.findViewById(R.id.partition_liked_item_layout2));

		holder.mDividerV = (ImageView) view.findViewById(R.id.partition_liked_divider_v);
		holder.mDividerH = (ImageView) view.findViewById(R.id.partition_liked_divider_h);

		view.setTag(holder);

		return view;
	}

	public ArrayList<String> getCateId() {
		if (mDataList == null) {
			return null;
		}
		for (int i = 0; i < mDataList.size(); i++) {
			if (mDataList.get(i).getIsFavorite()) {
				mCateId.add(mDataList.get(i).getId());
			}
		}
		return mCateId;
	}

	public ArrayList<String> getSelectedCateNames() {
		if (mDataList == null) {
			return null;
		}

		ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < mDataList.size(); i++) {
			if (mDataList.get(i).getIsFavorite()) {
				names.add(mDataList.get(i).getTitle());
			}
		}
		return names;
	}

	private class ViewHolder {
		public ArrayList<ImageView> mIcon = new ArrayList<ImageView>(ITEM_COUNT);
		public ArrayList<ImageView> mChoosedImg = new ArrayList<ImageView>(ITEM_COUNT);
		public ArrayList<TextView> mType = new ArrayList<TextView>(ITEM_COUNT);
		public ArrayList<RelativeLayout> mItemLayout = new ArrayList<RelativeLayout>(ITEM_COUNT);
		public ImageView mDividerV;
		public ImageView mDividerH;
	}

}