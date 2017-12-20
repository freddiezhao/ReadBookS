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
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

public class BookImageAdapter2 extends ListAdapter<Book> {
	/** 每个Item的宽度 */
	public static final int ITEM_WIDTH = PixelUtil.dp2px(80);
	/** 每个Item的高度 */
	public static final int ITEM_HEIGHT = PixelUtil.dp2px(188.0f);

	public Context mContext;

	private String mActionType = "";

	// 统计相关：大区块和子区块名称
	private String mParentRectName;
	private String mChildRectName;
	private String mBookId;

	public BookImageAdapter2(Context context, String mParentRectName, String mChildRectName, String mBookId) {
		mContext = context;
		this.mParentRectName = mParentRectName;
		this.mChildRectName = mChildRectName;
		this.mBookId = mBookId;
	}

	public void setBookId(String bookId) {
		mBookId = bookId;
	}

	public String getBookId() {
		return mBookId;
	}

	@Override
	public int getCount() {
		if (mDataList == null || mDataList.size() == 0) {
			return 0;
		} else {
			return mDataList.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mDataList.size()) {
			return mDataList.get(position);
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
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		Book book = mDataList.get(position);

		holder.title.setText(book.getTitle());
		holder.author.setText(book.getAuthor());

		holder.cover.setVisibility(View.VISIBLE);
		holder.coverClick.setVisibility(View.VISIBLE);
		// 因为封面框架已经覆盖封面，所以点击事件由封面框架来监听
		holder.coverClick.setOnClickListener(new OnClickListener(book, position));

		String imgUrl = book.getDownloadInfo().getImageUrl();
		ImageLoader.getInstance().load(imgUrl, holder.cover, ImageLoader.TYPE_COMMON_BIGGER_BOOK_COVER,
				ImageLoader.getDefaultPic());

		TextView state = holder.state;
		if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
			state.setTextColor(ResourceUtil.getColor(R.color.book_free_color));
			state.setText("免费");

		} else if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {
			state.setTextColor(ResourceUtil.getColor(R.color.book_seria_color));

			switch (book.getStatusType()) {
			case Book.STATUS_TYPE_FINISH:
				state.setText("完结");
				break;

			case Book.STATUS_TYPE_SERIAL:
				state.setText("连载");
				break;

			case Book.STATUS_TYPE_PAUSE:
				state.setText("选载");
				break;

			default:
				state.setText("");
				break;
			}

		} else if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP && book.getBuyInfo().getPrice() > 0) {
			state.setTextColor(ResourceUtil.getColor(R.color.book_charge_color));
			state.setText(book.getBuyInfo().getPrice() + mContext.getString(R.string.u_bi_name));

		} else if (book.isSuite()) {
			state.setTextColor(ResourceUtil.getColor(R.color.book_free_color));
			state.setText("包月");

		} else {
			state.setText("");
		}

		return convertView;
	}

	private View createView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_related_book_item, null);
		ViewHolder holder = new ViewHolder();

		holder.cover = (ImageView) view.findViewById(R.id.book_cover);
		holder.coverClick = (ImageView) view.findViewById(R.id.book_cover_click);
		holder.title = (EllipsizeTextView) view.findViewById(R.id.book_title);
		holder.author = (EllipsizeTextView) view.findViewById(R.id.book_author);
		holder.state = (EllipsizeTextView) view.findViewById(R.id.book_state);

		view.setTag(holder);

		return view;
	}

	@Override
	protected List<Book> createList() {
		return new ArrayList<Book>();
	}

	public void setActionType(String actionType) {
		mActionType = actionType;
	}

	private class ViewHolder {
		public ImageView cover;
		public ImageView coverClick;
		public EllipsizeTextView title;
		public EllipsizeTextView author;
		public EllipsizeTextView state;
	}

	private class OnClickListener implements View.OnClickListener {
		private Book mBook;
		private int mPosition;

		public OnClickListener(Book book, int position) {
			this.mBook = book;
			this.mPosition = position;
		}

		@Override
		public void onClick(View v) {
			if (null != mBook) {
				String eventKey = mParentRectName + "_" + mChildRectName + "_" + Util.formatNumber(mPosition + 1);
				// String eventExtra = mBook.getBookId();
				
				if(mContext instanceof BookDetailActivity){
					BookDetailActivity activity = (BookDetailActivity)mContext;
					activity.cancelDownload();
				}
				BookDetailActivity.launch(mContext, mBook, eventKey, mBookId);

				
				if (!TextUtils.isEmpty(mActionType)) {
					UserActionManager.getInstance().recordEvent(mActionType);
				}
			}
		}
	}

}