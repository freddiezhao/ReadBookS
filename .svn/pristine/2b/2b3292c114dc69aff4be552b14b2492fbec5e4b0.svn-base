package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.util.Util;

public class FamousBookAdapter extends ListAdapter<Book> {

	private Context mContext;
	private ViewHolder mHolder;

	public FamousBookAdapter(Context context) {
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
			Book book = (Book) getItem(position);
			if (book.getDownloadInfo().getImageUrl() != null
					&& !book.getDownloadInfo().getImageUrl().contains("http://")) {
				book.getDownloadInfo().setImageUrl(null);
			}
			ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), mHolder.headerImg,
					ImageLoader.TYPE_COMMON_BOOK_COVER, ImageLoader.getDefaultPic());

			mHolder.title.setText(book.getTitle());
			if (book.getAuthor() != null && !book.getAuthor().equalsIgnoreCase("")) {
				mHolder.author.setVisibility(View.VISIBLE);
				mHolder.author.setText(mContext.getString(R.string.author) + book.getAuthor());
			} else {
				mHolder.author.setVisibility(View.GONE);
			}
			if (!Util.isNullOrEmpty(book.getComment())) {
				mHolder.bookInfo.setText(book.getComment());
			} else {
				if (book.getIntro() != null) {
					mHolder.bookInfo.setText(Html.fromHtml(book.getIntro().trim()));
				} else {
					mHolder.bookInfo.setText("'");
				}
			}
			if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
				mHolder.cost.setVisibility(View.VISIBLE);
			} else {
				mHolder.cost.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	public void clearList() {
		if (mDataList != null) {
			mDataList.clear();
		}
	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_famous_book_list_item, null);
		ViewHolder holder = new ViewHolder();
		holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
		holder.title = (TextView) itemView.findViewById(R.id.title);
		holder.author = (TextView) itemView.findViewById(R.id.author);
		holder.bookInfo = (EllipsizeTextView) itemView.findViewById(R.id.book_info);
		holder.cost = itemView.findViewById(R.id.cost_free);
		itemView.setTag(holder);
		return itemView;
	}

	protected class ViewHolder {
		public ImageView headerImg;
		public TextView title;
		public TextView author;
		public EllipsizeTextView bookInfo;
		public View cost;
	}

	@Override
	protected List<Book> createList() {
		return new ArrayList<Book>();
	}

}
