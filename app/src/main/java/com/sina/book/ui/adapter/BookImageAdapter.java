package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.R.id;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.Util;

/**
 * 推荐页书籍封面适配器
 * 
 * @author MarkMjw
 * @date 2013-4-8
 */
public class BookImageAdapter extends BaseAdapter
{
	private static final int		COLUMNS_NUM	= 3;

	private String					mActionType	= "";

	private ArrayList<List<Book>>	mDatas;
	private Context					mContext;

	private String					mChildRectName;

	public BookImageAdapter(Context context, String childRectName)
	{
		mDatas = new ArrayList<List<Book>>();

		this.mContext = context;
		this.mChildRectName = childRectName;
	}

	public void setChildRectName(String mChildRectName)
	{
		this.mChildRectName = mChildRectName;
	}

	/**
	 * 每行最多3项数据
	 * 
	 * @param data
	 */
	public void setData(List<Book> data)
	{
		if (null == data || data.isEmpty()) {
			return;
		}

		mDatas.clear();
		List<Book> itemList;

		int size = (int) Math.ceil(data.size() * 1.00 / COLUMNS_NUM);
		for (int i = 0; i < size; i++) {
			int index = i * COLUMNS_NUM + COLUMNS_NUM;
			if (index >= data.size()) {
				itemList = data.subList(i * COLUMNS_NUM, data.size());
			} else {
				itemList = data.subList(i * COLUMNS_NUM, index);
			}

			mDatas.add(itemList);
		}

	}

	public void setActionType(String actionType)
	{
		mActionType = actionType;
	}

	@Override
	public int getCount()
	{
		if (mDatas == null || mDatas.size() == 0) {
			return 0;
		} else {
			return mDatas.size();
		}
	}

	@Override
	public Object getItem(int position)
	{
		if (position < mDatas.size() && position >= 0) {
			return mDatas.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null || null == convertView.getTag()) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		List<Book> books = mDatas.get(position);
		// 先隐藏
		for (int i = 0; i < holder.items.size(); i++) {
			ViewItem item = holder.items.get(i);
			item.layout.setVisibility(View.GONE);
		}

		for (int i = 0; i < books.size(); i++) {
			Book book = books.get(i);
			if (null == book) {
				continue;
			}

			ViewItem item = holder.items.get(i);
			item.layout.setVisibility(View.VISIBLE);
			item.title.setText(book.getTitle().trim());
			item.author.setText(book.getAuthor().trim());

			ImageView cover = item.cover;
			ImageView coverClick = item.coverClick;

			cover.setVisibility(View.VISIBLE);
			coverClick.setVisibility(View.VISIBLE);
			// 因为封面框架已经覆盖封面，所以点击事件由封面框架来监听
			coverClick.setOnClickListener(new OnClickListener(books.get(i), i + (position * COLUMNS_NUM)));

			String imgUrl = book.getDownloadInfo().getImageUrl();
			ImageLoader.getInstance().load3(imgUrl, cover, ImageLoader.TYPE_COMMON_BIGGER_BOOK_COVER,
					ImageLoader.getNoImgPic());

			// StringBuilder builder = new StringBuilder("" +
			// book.getPraiseNum());
			// int end = builder.length();
			// builder.append(ResourceUtil.getString(R.string.sell_fast_recommend));
			// int color = ResourceUtil.getColor(R.color.praise_num_color);
			// Spanned spanned = Util.highLight(builder, color, 0, end);
			// item.praiseNum.setText(spanned);
			item.praiseNum.setText(book.getType());
		}

		return convertView;
	}

	private View createView()
	{
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_recommend_list_item, null);
		ViewHolder holder = new ViewHolder();

		if (view == null) {
			return null;
		}

		for (int i = 1; i <= COLUMNS_NUM; i++) {
			ViewItem item = new ViewItem();

			switch (i) {
			case 1:
				item.layout = view.findViewById(R.id.recommend_book1);
				break;
			case 2:
				item.layout = view.findViewById(R.id.recommend_book2);
				break;
			case 3:
				item.layout = view.findViewById(R.id.recommend_book3);
				break;
			}

			item.cover = (ImageView) item.layout.findViewById(R.id.book_cover);
			item.coverClick = (ImageView) item.layout.findViewById(R.id.book_cover_click);
			item.title = (EllipsizeTextView) item.layout.findViewById(R.id.book_title);
			item.author = (EllipsizeTextView) item.layout.findViewById(R.id.book_author);
			item.praiseNum = (TextView) item.layout.findViewById(id.book_praise_num);

			holder.items.add(item);
		}

		view.setTag(holder);

		return view;
	}

	private class ViewHolder
	{
		/**
		 * 每一项的item列表
		 */
		private List<ViewItem>	items	= new ArrayList<ViewItem>(COLUMNS_NUM);
	}

	private class ViewItem
	{
		private View				layout;
		private ImageView			cover;
		private ImageView			coverClick;
		private EllipsizeTextView	title;
		private EllipsizeTextView	author;
		private TextView			praiseNum;
	}

	private class OnClickListener implements View.OnClickListener
	{
		private Book	mBook;
		private int		mPosition;

		public OnClickListener(Book book, int position)
		{
			this.mBook = book;
			this.mPosition = position;
		}

		@Override
		public void onClick(View v)
		{
			if (null != mBook) {
				String eventKey = "精选推荐_" + mChildRectName + "_" + Util.formatNumber(mPosition + 1);
				BookDetailActivity.launchNew(mContext, mBook, eventKey);

				if (!TextUtils.isEmpty(mActionType)) {
					UserActionManager.getInstance().recordEvent(mActionType);
				}
			}
		}
	}
}
