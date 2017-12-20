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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.SellFastItem;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.CommonListActivity;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

/**
 * 畅销榜单适配器
 * 
 * @author YangZhanDong
 */
public class SellFastAdapter extends BaseAdapter {
	private final int ITEM_COUNT = 3;

	private Context mContext;
	private ArrayList<SellFastItem> mLists;

	public SellFastAdapter(Context context) {
		this.mContext = context;
	}

	public void setList(List<SellFastItem> list) {
		if (list == null) {
			return;
		}
		if (mLists == null) {
			mLists = new ArrayList<SellFastItem>();
		}
		mLists.clear();
		mLists.addAll(list);
	}

	@Override
	public int getCount() {
		if (mLists == null || mLists.size() == 0) {
			return 0;
		} else {
			return mLists.size();
		}

	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mLists.size()) {
			return mLists.get(position);
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

		final SellFastItem item = mLists.get(position);

		holder.title.setText(item.getBookType());

		String count = String.format(ResourceUtil.getString(R.string.sell_fast_count), item.getBookCount());
		holder.count.setText(count);

		holder.titleLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enterChildList(item);
			}
		});
		holder.allRecommend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enterChildList(item);
			}
		});

		int size = (item.getBooks().size() <= 3) ? item.getBooks().size() : 3;
		for (int i = 0; i < size; i++) {
			setBookData(holder, item, i);
		}

		return convertView;
	}

	private void setBookData(ViewHolder holder, final SellFastItem item, final int i) {
		final Book book = item.getBooks().get(i);
		ViewItem viewItem = holder.items.get(i);

		viewItem.title.setText(book.getTitle());
		viewItem.author.setText(book.getAuthor());

		// boolean isComment = "hot_comment".equals(item.getItemType());
		// String praiseNum = String.valueOf(isComment ? book.getCommentNum() :
		// book.getPraiseNum());
		// String recommend = ResourceUtil
		// .getString(isComment ? R.string.sell_fast_comment :
		// R.string.sell_fast_recommend);
		// String text = praiseNum + recommend;
		// int color = ResourceUtil.getColor(R.color.praise_num_color);
		// viewItem.praiseNum.setText(Util.highLight(text, color, 0,
		// praiseNum.length()));
		// 1.8.6 畅销榜单的各个栏目中的xxx人推荐改成显示书籍的分类类型(该需求由王金江提)
		viewItem.praiseNum.setText(book.getBookCate());

		String imgUrl = book.getDownloadInfo().getImageUrl();
		ImageLoader.getInstance().load3(imgUrl, viewItem.cover, ImageLoader.TYPE_COMMON_BIGGER_BOOK_COVER,
				ImageLoader.getNoImgPic());

		viewItem.coverClick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				String eventKey = "畅销榜单_" + item.getBookType() + "_" + Util.formatNumber(i + 1);
				BookDetailActivity.launchNew(mContext, book, eventKey);

				String bookTag = item.getBookTag();
				if (!TextUtils.isEmpty(bookTag)) {
					UserActionManager.getInstance().recordEvent(bookTag);
				}
			}
		});

		switch (i) {
		case 0:
			viewItem.icon.setImageResource(R.drawable.no1_icon);
			break;
		case 1:
			viewItem.icon.setImageResource(R.drawable.no2_icon);
			break;
		case 2:
			viewItem.icon.setImageResource(R.drawable.no3_icon);
			break;
		default:
			viewItem.icon.setImageDrawable(null);
			break;
		}
	}

	private void enterChildList(SellFastItem item) {
		String type = item.getItemType();

		// if ("free".equals(type) || "sina_top".equals(type)) {
		// String url = String.format(ConstantData.URL_CHILD_SELL_FAST_LIST,
		// type, "%s",
		// ConstantData.PAGE_SIZE);
		// CommonListActivity.launch(mContext, url, item.getBookType());
		// } else {
		String url = String.format(ConstantData.URL_CHILD_SELL_FAST_NEW, type, "%s", ConstantData.PAGE_SIZE);
		CommonListActivity.launch(mContext, url, item.getBookType(), type);
		// }

		String tag = item.getItemTag();
		if (!TextUtils.isEmpty(tag)) {
			UserActionManager.getInstance().recordEvent(tag);
		}
	}

	private View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_sell_fast_list_item, null);

		if (itemView == null) {
			return null;
		}

		ViewHolder holder = new ViewHolder();

		holder.titleLayout = itemView.findViewById(R.id.sell_fast_title_layout);
		holder.title = (TextView) itemView.findViewById(R.id.sell_fast_title);
		holder.count = (TextView) itemView.findViewById(R.id.sell_fast_count);
		holder.allRecommend = (RelativeLayout) itemView.findViewById(R.id.all_recommend_btn);

		View bookView = itemView.findViewById(R.id.sell_fast_book_layout);

		for (int i = 1; i <= ITEM_COUNT; i++) {
			ViewItem item = new ViewItem();

			switch (i) {
			case 1:
				item.layout = bookView.findViewById(R.id.sell_fast_book1);
				break;
			case 2:
				item.layout = bookView.findViewById(R.id.sell_fast_book2);
				break;
			case 3:
				item.layout = bookView.findViewById(R.id.sell_fast_book3);
				break;
			}

			item.cover = (ImageView) item.layout.findViewById(R.id.book_cover);
			item.coverClick = (ImageView) item.layout.findViewById(R.id.book_cover_click);
			item.icon = (ImageView) item.layout.findViewById(R.id.book_icon);
			item.title = (EllipsizeTextView) item.layout.findViewById(R.id.book_title);
			item.author = (EllipsizeTextView) item.layout.findViewById(R.id.book_author);
			item.praiseNum = (TextView) item.layout.findViewById(R.id.book_praise_num);

			holder.items.add(item);
		}

		itemView.setTag(holder);
		return itemView;
	}

	private class ViewHolder {
		private View titleLayout;
		private TextView title;
		private TextView count;
		private RelativeLayout allRecommend;

		private List<ViewItem> items = new ArrayList<ViewItem>(ITEM_COUNT);
	}

	private class ViewItem {
		private View layout;
		private ImageView cover;
		private ImageView coverClick;
		private ImageView icon;
		private EllipsizeTextView title;
		private EllipsizeTextView author;
		private TextView praiseNum;
	}

}