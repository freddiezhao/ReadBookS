package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.RecommendFamousItem;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.FamousDetailActivity;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;

/**
 * 【推荐页】名人推荐Adapter
 * 
 * @author MarkMjw
 * @date 2013-4-10
 */
public class RecommendFamousAdapter extends ListAdapter<RecommendFamousItem> {
	private Context mContext;
	private ViewHolder mHolder;

	private Drawable mDividerDrawable;

	public RecommendFamousAdapter(Context context, Drawable divider) {
		this.mContext = context;
		this.mDividerDrawable = divider;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		mHolder = (ViewHolder) convertView.getTag();

		RecommendFamousItem item = (RecommendFamousItem) getItem(position);
		if (item.getHeadUrl() != null && !item.getHeadUrl().contains("http://")) {
			item.setHeadUrl(null);
		}
		ImageLoader.getInstance().load(item.getHeadUrl(), mHolder.headerImg, ImageLoader.getDefaultAvatar(),
				ImageLoader.getDefaultMainAvatar());

		mHolder.divider.setImageDrawable(mDividerDrawable);
		mHolder.name.setText(item.getScreenName());

		String title = "";
		for (int i = 0; i < item.getBooks().size(); i++) {
			title += item.getBooks().get(i).getTitle().trim();

			if (i + 1 < item.getBooks().size()) {
				title += " | ";
			}
		}

		mHolder.books.setText(title);
		convertView.setOnClickListener(new ClickListener(item));

		return convertView;
	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_recommend_famous_item, null);
		ViewHolder holder = new ViewHolder();

		holder.divider = (ImageView) itemView.findViewById(R.id.famous_list_divider);
		holder.headerImg = (ImageView) itemView.findViewById(R.id.famous_head_img);
		holder.name = (TextView) itemView.findViewById(R.id.famous_name);
		holder.books = (EllipsizeTextView) itemView.findViewById(R.id.famous_books);

		itemView.setTag(holder);
		return itemView;
	}

	@Override
	protected List<RecommendFamousItem> createList() {
		return new ArrayList<RecommendFamousItem>();
	}

	protected class ViewHolder {
		public ImageView divider;
		public ImageView headerImg;
		public TextView name;
		public EllipsizeTextView books;
	}

	private class ClickListener implements OnClickListener {
		private RecommendFamousItem mItem;

		public ClickListener(RecommendFamousItem item) {
			mItem = item;
		}

		@Override
		public void onClick(View v) {
			// 进入详情
			FamousDetailActivity.launch(mContext, mItem.getUid(), mItem.getScreenName());

			UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_FAMOUS);
		}
	}
}
