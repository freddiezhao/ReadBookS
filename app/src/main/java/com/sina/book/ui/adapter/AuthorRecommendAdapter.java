package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.AuthorPageResult;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.AuthorActivity;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

/**
 * 作家推荐adapter
 * 
 * @author Tsimle
 * 
 */
public class AuthorRecommendAdapter extends ListAdapter<AuthorPageResult> {
	private Context mContext;
	private ViewHolder mHolder;

	public AuthorRecommendAdapter(Context context) {
		mContext = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
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

			// 给第一个Card设置一个paddingTop值
			int left = convertView.getPaddingLeft();
			int right = convertView.getPaddingRight();
			int bottom = convertView.getPaddingBottom();
			if (position == 0) {
				convertView.setPadding(left, PixelUtil.dp2px(8), right, bottom);
			} else {
				convertView.setPadding(left, 0, right, bottom);
			}

			mHolder = (ViewHolder) convertView.getTag();
			final AuthorPageResult item = (AuthorPageResult) getItem(position);

			View.OnClickListener enterAuthorPageListener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					AuthorActivity.launch(mContext, ResourceUtil.getString(R.string.author_recommend_title),
							item.getUid(), AuthorActivity.TYPE_AUTHOR);
				}
			};
			mHolder.headLayout.setOnClickListener(enterAuthorPageListener);
			mHolder.more.setOnClickListener(enterAuthorPageListener);

			ImageLoader.getInstance().load(item.getImgUrl(), mHolder.head, ImageLoader.getDefaultAvatar(),
					ImageLoader.getDefaultMainAvatar());

			String tag = item.getTag();
			if ("hot".equals(tag)) {
				mHolder.headTip.setImageResource(R.drawable.icon_hot);
			} else if ("new".equals(tag)) {
				mHolder.headTip.setImageResource(R.drawable.icon_new);
			} else {
				mHolder.headTip.setImageDrawable(null);
			}

			mHolder.title.setText(item.getName());
			String intro = item.getIntro();
			if (TextUtils.isEmpty(intro)) {
				intro = mContext.getString(R.string.no_intro);
			}
			mHolder.intro.setText(intro);

			mHolder.book1.setVisibility(View.GONE);
			mHolder.book1.setOnClickListener(null);
			mHolder.book2.setVisibility(View.GONE);
			mHolder.book2.setOnClickListener(null);
			mHolder.book3.setVisibility(View.GONE);
			mHolder.book3.setOnClickListener(null);
			List<Book> books = item.getBooks();
			Book book = null;
			if (books != null) {
				if (books.size() > 0) {
					book = books.get(0);
					mHolder.book1.setVisibility(View.VISIBLE);
					final Book clickBook = book;
					mHolder.book1.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							BookDetailActivity.launchNew(mContext, clickBook, "热门作家_" + Util.formatNumber(position + 1)
									+ "_01");
						}
					});
					mHolder.bookTitle1.setText(book.getTitle());
					mHolder.bookNum1.setText(String.valueOf(book.getPraiseNum()));
					mHolder.bookTag1.setText(book.getContentTag());
				}
				if (books.size() > 1) {
					book = books.get(1);
					mHolder.book2.setVisibility(View.VISIBLE);
					final Book clickBook = book;
					mHolder.book2.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							BookDetailActivity.launchNew(mContext, clickBook, "热门作家_" + Util.formatNumber(position + 1)
									+ "_02");
						}
					});
					mHolder.bookTitle2.setText(book.getTitle());
					mHolder.bookNum2.setText(String.valueOf(book.getPraiseNum()));
					mHolder.bookTag2.setText(book.getContentTag());
				}
				if (books.size() > 2) {
					book = books.get(2);
					mHolder.book3.setVisibility(View.VISIBLE);
					final Book clickBook = book;
					mHolder.book3.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							//
							BookDetailActivity.launchNew(mContext, clickBook, "热门作家_" + Util.formatNumber(position + 1)
									+ "_03");
						}
					});
					mHolder.bookTitle3.setText(book.getTitle());
					mHolder.bookNum3.setText(String.valueOf(book.getPraiseNum()));
					mHolder.bookTag3.setText(book.getContentTag());
				}
			}

		}
		return convertView;
	}

	@Override
	protected List<AuthorPageResult> createList() {
		return new ArrayList<AuthorPageResult>();

	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_author_recommend_card, null);
		ViewHolder holder = new ViewHolder();
		holder.headLayout = itemView.findViewById(R.id.head_layout);
		holder.head = (ImageView) holder.headLayout.findViewById(R.id.head);
		holder.headTip = (ImageView) holder.headLayout.findViewById(R.id.head_tip);
		holder.title = (TextView) holder.headLayout.findViewById(R.id.title);
		holder.intro = (TextView) itemView.findViewById(R.id.intro);
		holder.more = itemView.findViewById(R.id.card_bottom);
		holder.book1 = itemView.findViewById(R.id.book_layout1);
		holder.bookTitle1 = (TextView) holder.book1.findViewById(R.id.book_title);
		holder.bookNum1 = (TextView) holder.book1.findViewById(R.id.recommend_book_nums);
		holder.bookTag1 = (TextView) holder.book1.findViewById(R.id.book_tag);

		holder.book2 = itemView.findViewById(R.id.book_layout2);
		holder.bookTitle2 = (TextView) holder.book2.findViewById(R.id.book_title);
		holder.bookNum2 = (TextView) holder.book2.findViewById(R.id.recommend_book_nums);
		holder.bookTag2 = (TextView) holder.book2.findViewById(R.id.book_tag);

		holder.book3 = itemView.findViewById(R.id.book_layout3);
		holder.bookTitle3 = (TextView) holder.book3.findViewById(R.id.book_title);
		holder.bookNum3 = (TextView) holder.book3.findViewById(R.id.recommend_book_nums);
		holder.bookTag3 = (TextView) holder.book3.findViewById(R.id.book_tag);
		itemView.setTag(holder);
		return itemView;
	}

	protected class ViewHolder {
		public View headLayout;
		public ImageView head;
		public ImageView headTip;
		public TextView title;
		public TextView intro;
		public View more;
		public View book1;
		public TextView bookTitle1;
		public TextView bookNum1;
		public TextView bookTag1;
		public View book2;
		public TextView bookTitle2;
		public TextView bookNum2;
		public TextView bookTag2;
		public View book3;
		public TextView bookTitle3;
		public TextView bookNum3;
		public TextView bookTag3;

	}
}
