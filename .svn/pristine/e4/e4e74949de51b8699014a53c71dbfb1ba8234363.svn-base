package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.htmlcleaner.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spanned;
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
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

public class PartitionDetailListAdapter extends ListAdapter<Book> {

	private Context mContext;
	private ViewHolder mHolder;
	private BitmapDrawable mDotHDrawable;
	private String mActionType = "";
	private Date mNow;

	// private String mParentRectName;// 父区块名称
	private String mChildRectName;// 子区块名称
	private String mExtraName;// Extra

	public PartitionDetailListAdapter(Context context, String mChildRectName, String mExtraName) {
		mContext = context;
		this.mChildRectName = mChildRectName;
		this.mExtraName = mExtraName;
		Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.divider_dot_real);
		mDotHDrawable = new BitmapDrawable(context.getResources(), dotHBitmap);
		mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mDotHDrawable.setDither(true);
		mNow = new Date();
	}

	public void setChildRectName(String childRectName) {
		mChildRectName = childRectName;
	}

	public void setExtraName(String extraName) {
		mExtraName = extraName;
	}

	public void setActionType(String actionType) {
		mActionType = actionType;
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
			ImageLoader.getInstance().load3(book.getDownloadInfo().getImageUrl(), mHolder.headerImg,
					ImageLoader.TYPE_COMMON_BOOK_COVER, ImageLoader.getNoImgPic());

			mHolder.title.setText(book.getTitle());

			if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
				mHolder.cost.setVisibility(View.VISIBLE);
				mHolder.cost.setText("免费");
				mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_free_color));
			} else if (book.isSuite()) {
				mHolder.cost.setVisibility(View.VISIBLE);
				mHolder.cost.setText("包月");
				mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_free_color));
			} else if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {
				mHolder.cost.setVisibility(View.VISIBLE);
				mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_seria_color));
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
			} else if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP && book.getBuyInfo().getPrice() > 0) {
				mHolder.cost.setVisibility(View.VISIBLE);
				mHolder.cost.setText(book.getBuyInfo().getPrice() + mContext.getString(R.string.u_bi_name));
				mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_charge_color));
			} else {
				mHolder.cost.setVisibility(View.GONE);
			}
			if (book.getAuthor() != null && !book.getAuthor().equalsIgnoreCase("")) {
				mHolder.author.setVisibility(View.VISIBLE);
				mHolder.author.setText(mContext.getString(R.string.author) + book.getAuthor());
			} else {
				mHolder.author.setVisibility(View.GONE);
			}

			int color = ResourceUtil.getColor(R.color.praise_num_color);
			if ((book.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP || book.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE)
					&& !Utils.isEmptyString(book.getUpdateChapterNameServer())) {
				StringBuilder builder = new StringBuilder("最新：");
				int start = builder.length();
				builder.append(book.getUpdateChapterNameServer());
				int end = builder.length();
				Spanned spanned = Util.highLight(builder, color, start, end);
				mHolder.chapterInfo.setText(spanned);

				String time = book.getUpdateTimeServer();
				if (Utils.isEmptyString(time) || "null".equalsIgnoreCase(time)) {
					mHolder.updateInfo.setVisibility(View.GONE);
				} else {
					mHolder.updateInfo.setVisibility(View.VISIBLE);
					mHolder.updateInfo.setText("更新：" + Util.getTimeToDisplay(time, mNow));
				}

			} else {
				StringBuilder builder = new StringBuilder("总共：");
				int start = builder.length();
				builder.append(book.getNum()).append("章");
				int end = builder.length();
				Spanned spanned = Util.highLight(builder, color, start, end);
				mHolder.chapterInfo.setText(spanned);

				showTags(book);
			}

			if (position == 0) {
				mHolder.listDivide.setVisibility(View.GONE);
			} else {
				mHolder.listDivide.setVisibility(View.VISIBLE);
			}
			mHolder.wholeLayout.setOnClickListener(new OnClickListener(book, position));
		}

		return convertView;
	}

	/**
	 * 显示书籍标签
	 * 
	 * @param book
	 */
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

	public void clearList() {
		if (mDataList != null) {
			mDataList.clear();
		}
	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_new_book_card_item, null);
		ViewHolder holder = new ViewHolder();
		holder.wholeLayout = itemView;
		holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
		holder.title = (TextView) itemView.findViewById(R.id.title);
		holder.author = (TextView) itemView.findViewById(R.id.author);
		holder.chapterInfo = (TextView) itemView.findViewById(R.id.chapter_info);
		holder.updateInfo = (TextView) itemView.findViewById(R.id.update_info);
		holder.cost = (TextView) itemView.findViewById(R.id.cost_tv);
		holder.listDivide = itemView.findViewById(R.id.list_divide);
		holder.listDivide.setBackgroundDrawable(mDotHDrawable);
		itemView.setTag(holder);
		return itemView;
	}

	protected class ViewHolder {
		public View wholeLayout;
		public ImageView headerImg;
		public TextView title;
		public TextView author;
		public TextView chapterInfo;
		public TextView updateInfo;
		public TextView cost;
		public View listDivide;
	}

	@Override
	protected List<Book> createList() {
		return new ArrayList<Book>();
	}

	private class OnClickListener implements View.OnClickListener {
		private Book mBook;
		private int position;

		public OnClickListener(Book book, int position) {
			this.mBook = book;
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			if (null != mBook) {
				// 专题不统计基本事件，分类需要统计
				boolean recordNormalEvent = false;
				if (!TextUtils.isEmpty(mActionType)) {
					String eventKey = "分类_";
					if (Constants.CLICK_CLASSIFIED_RANK.equals(mActionType)
							|| Constants.CLICK_CLASSIFIED_NEW.equals(mActionType)
							|| Constants.CLICK_CLASSIFIED_FREE.equals(mActionType)) {
						recordNormalEvent = true;
						eventKey = "分类_";
					} else if (Constants.CLICK_RECOMMAND_TOPIC.equals(mActionType)) {
						eventKey = "专题_";
					}
					eventKey += mChildRectName + "_" + Util.formatNumber(position + 1);
					String eventExtra = mExtraName;
					BookDetailActivity.launch(mContext, mBook, eventKey, eventExtra);
				}

				if (recordNormalEvent) {
					UserActionManager.getInstance().recordEvent(mActionType);
				}
			}
		}
	}

}
