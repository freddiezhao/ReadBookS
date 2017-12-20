package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

/**
 * Epub专区列表adapter
 */
public class BookEpubAdapter extends BaseAdapter {

	// 显示几个选项
	public static final int COLUMNS_NUM = 2;

	private int maxNum = COLUMNS_NUM;

	private Context mContext;

	// 统计点击事件
	private String mActionType = "";
	private ArrayList<Book> mDatas;

	private String mChildRectName;

	private BitmapDrawable mDivider;

	public BookEpubAdapter(Context context, String childRectName) {
		mDatas = new ArrayList<Book>();
		this.mContext = context;
		this.mChildRectName = childRectName;
		decodeDivider();
	}

	private void decodeDivider() {
		int resId = R.drawable.list_divide_dot;
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				resId);
		mDivider = new BitmapDrawable(mContext.getResources(), bitmap);
		mDivider.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mDivider.setDither(true);
	}

	public void setChildRectName(String mChildRectName) {
		this.mChildRectName = mChildRectName;
	}

	public void setData(List<Book> data) {
		if (null == data || data.isEmpty()) {
			return;
		}

		int size = data.size();
		if (size < COLUMNS_NUM) {
			maxNum = size;
		} else {
			maxNum = COLUMNS_NUM;
		}

		for (int i = 0; i < maxNum; i++) {
			mDatas.add(data.get(i));
		}
	}

	public void setActionType(String actionType) {
		mActionType = actionType;
	}

	public int getCount() {
		if (mDatas == null || mDatas.size() == 0) {
			return 0;
		} else {
			return mDatas.size();
		}
	}

	public Object getItem(int position) {
		if (position < mDatas.size() && position >= 0) {
			return mDatas.get(position);
		} else {
			return null;
		}
	}

	public long getItemId(int position) {
		return position;
	}

	private ViewHolder mHolder;

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || null == convertView.getTag()) {
			convertView = createView();
		}

		mHolder = (ViewHolder) convertView.getTag();
		if (position == mDatas.size() - 1) {
			mHolder.listDivide.setVisibility(View.GONE);
		}

		Book book = (Book) getItem(position);
		updateCommonView(book);
		updateCostType(book);
		updateDifferentData(book);

		OnClickListener listener = new OnClickListener(book, position);
		convertView.setOnClickListener(listener);
		return convertView;
	}

	private void updateCommonView(Book book) {
		if (book.getDownloadInfo().getImageUrl() != null
				&& !book.getDownloadInfo().getImageUrl().contains("http://")) {
			book.getDownloadInfo().setImageUrl(null);
		}
		ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(),
				mHolder.headerImg, ImageLoader.TYPE_COMMON_BOOK_COVER,
				ImageLoader.getDefaultPic());
		mHolder.title.setText(book.getTitle());

		if (book.getAuthor() != null && !book.getAuthor().equalsIgnoreCase("")) {
			mHolder.author.setVisibility(View.VISIBLE);
			mHolder.author.setText(mContext.getString(R.string.author)
					+ book.getAuthor());
		} else {
			mHolder.author.setVisibility(View.GONE);
		}

	}

	private void updateCostType(Book book) {
		mHolder.cost.setVisibility(View.VISIBLE);

		int payType = book.getBuyInfo().getPayType();
		if (payType == Book.BOOK_TYPE_FREE) {
			mHolder.cost.setVisibility(View.VISIBLE);
			mHolder.cost.setText("免费");
		} else if (book.isSuite()) {
			mHolder.cost.setVisibility(View.VISIBLE);
			mHolder.cost.setText("包月");
		} else if (payType == Book.BOOK_TYPE_CHAPTER_VIP) {
			mHolder.cost.setVisibility(View.VISIBLE);
			switch (book.getStatusType()) {
			case Book.STATUS_TYPE_FINISH:
				mHolder.cost.setText("完结");
				break;

			case Book.STATUS_TYPE_SERIAL:
				mHolder.cost.setText("连载");
				break;

			case Book.STATUS_TYPE_PAUSE:
				mHolder.cost.setText("选载");
				break;

			default:
				mHolder.cost.setVisibility(View.GONE);
				break;
			}
		} else if (payType == Book.BOOK_TYPE_VIP
				&& book.getBuyInfo().getPrice() > 0) {
			mHolder.cost.setVisibility(View.VISIBLE);
			mHolder.cost.setText(book.getBuyInfo().getPrice()
					+ mContext.getString(R.string.u_bi_name));
		} else {
			mHolder.cost.setVisibility(View.GONE);
		}

		// 如果是免费单独设置颜色
		if (payType == Book.BOOK_TYPE_FREE) {
			mHolder.cost.setTextColor(ResourceUtil
					.getColor(R.color.book_free_color));
		} else {
			mHolder.cost.setTextColor(ResourceUtil
					.getColor(R.color.book_cost_color));
		}
	}

	private void updateDifferentData(Book book) {
		// int color = ResourceUtil.getColor(R.color.praise_num_color);
		// StringBuilder builder = new StringBuilder("推荐：");
		// int start = builder.length();
		// builder.append(book.getPraiseNum());
		// int end = builder.length();
		// builder.append("人");
		// Spanned spanned = Util.highLight(builder, color, start, end);
		// mHolder.chapterInfo.setText(spanned);
		String text = book.getType();
		mHolder.chapterInfo.setText(text);
		showTags(book);
	}

	private void showTags(Book book) {
		// 获取书籍内容标签tag
		String tags = book.getContentTag();
		if (!TextUtils.isEmpty(tags)) {
			mHolder.updateInfo.setText("标签：" + tags);
			mHolder.updateInfo.setVisibility(View.VISIBLE);

		} else {
			mHolder.updateInfo.setVisibility(View.GONE);
		}
	}

	private View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(
				R.layout.vw_new_book_item, null);

		ViewHolder holder = new ViewHolder();
		holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
		holder.title = (TextView) itemView.findViewById(R.id.title);
		holder.author = (TextView) itemView.findViewById(R.id.author);
		holder.chapterInfo = (TextView) itemView
				.findViewById(R.id.chapter_info);
		holder.updateInfo = (TextView) itemView.findViewById(R.id.update_info);
		holder.cost = (TextView) itemView.findViewById(R.id.cost_tv);
		holder.listDivide = itemView.findViewById(R.id.list_divide);
		holder.listDivide.setBackgroundDrawable(mDivider);
		holder.trend = (ImageView) itemView.findViewById(R.id.trend);

		itemView.setBackgroundResource(R.drawable.list_item_bg1);
		itemView.setTag(holder);

		return itemView;
	}

	protected class ViewHolder {
		public ImageView headerImg;
		public TextView title;
		public TextView author;
		public TextView chapterInfo;
		public TextView updateInfo;
		public TextView cost;
		public View listDivide;
		public ImageView trend;
	}

	private class OnClickListener implements View.OnClickListener {
		private Book mBook;
		private int mPosition;

		public OnClickListener(Book book, int position) {
			this.mBook = book;
			this.mPosition = position;
		}

		public void onClick(View v) {
			if (null != mBook) {
				String eventKey = "精选推荐_" + mChildRectName + "_"
						+ Util.formatNumber(mPosition + 1);

				BookDetailActivity.launchNew(mContext, mBook, eventKey);

				if (!TextUtils.isEmpty(mActionType)) {
					UserActionManager.getInstance().recordEvent(mActionType);
				}
			}
		}
	}
}
