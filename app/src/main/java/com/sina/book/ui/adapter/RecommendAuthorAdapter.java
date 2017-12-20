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
import com.sina.book.data.AuthorPageResult;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.AuthorActivity;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.ResourceUtil;

/**
 * 【推荐页】热门作家Adapter
 * 
 * @author MarkMjw
 * @date 2013-9-24
 */
public class RecommendAuthorAdapter extends ListAdapter<AuthorPageResult> {
	private Context mContext;

	private Drawable mDividerDrawable;

	public RecommendAuthorAdapter(Context context, Drawable divider) {
		this.mContext = context;
		this.mDividerDrawable = divider;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		AuthorPageResult item = (AuthorPageResult) getItem(position);
		if (item.getImgUrl() != null && !item.getImgUrl().contains("http://")) {
			item.setImgUrl(null);
		}
		ImageLoader.getInstance().load(item.getImgUrl(), holder.headerImg, ImageLoader.getDefaultAvatar(),
				ImageLoader.getDefaultMainAvatar());

		holder.divider.setImageDrawable(mDividerDrawable);
		holder.name.setText(item.getName());
		holder.des.setText(item.getIntro());

		String tag = item.getTag();
		if ("hot".equals(tag)) {
			holder.headerImgIcon.setImageResource(R.drawable.icon_hot);
		} else if ("new".equals(tag)) {
			holder.headerImgIcon.setImageResource(R.drawable.icon_new);
		} else {
			holder.headerImgIcon.setImageDrawable(null);
		}

		String title = "";
		for (int i = 0; i < item.getBooks().size(); i++) {
			title += item.getBooks().get(i).getTitle().trim();

			if (i + 1 < item.getBooks().size()) {
				title += " | ";
			}
		}

		holder.books.setText(title);
		convertView.setOnClickListener(new ClickListener(item));

		return convertView;
	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_recommend_author_item, null);
		ViewHolder holder = new ViewHolder();

		holder.divider = (ImageView) itemView.findViewById(R.id.author_list_divider);
		holder.headerImg = (ImageView) itemView.findViewById(R.id.author_img);
		holder.headerImgIcon = (ImageView) itemView.findViewById(R.id.author_img_icon);
		holder.name = (TextView) itemView.findViewById(R.id.author_name);
		holder.des = (TextView) itemView.findViewById(R.id.author_des);
		holder.books = (EllipsizeTextView) itemView.findViewById(R.id.author_books);

		itemView.setTag(holder);
		return itemView;
	}

	@Override
	protected List<AuthorPageResult> createList() {
		return new ArrayList<AuthorPageResult>();
	}

	protected class ViewHolder {
		private ImageView divider;
		private ImageView headerImg;
		private ImageView headerImgIcon;
		private TextView name;
		private TextView des;
		private EllipsizeTextView books;
	}

	private class ClickListener implements OnClickListener {
		private AuthorPageResult mItem;

		public ClickListener(AuthorPageResult item) {
			mItem = item;
		}

		@Override
		public void onClick(View v) {
			AuthorActivity.launch(mContext, ResourceUtil.getString(R.string.author_recommend_title), mItem.getUid(),
					AuthorActivity.TYPE_AUTHOR);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AUTHOR);
		}
	}
}
