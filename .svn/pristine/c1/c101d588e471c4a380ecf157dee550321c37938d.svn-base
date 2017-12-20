package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;
import com.sina.book.util.ResourceUtil;

public class BookAdapter extends ExtraListAdapter
{

	private Context			mContext;
	private ViewHolder		mHolder;
	private BitmapDrawable	mDotHDrawable;

	public BookAdapter(Context context)
	{
		mContext = context;
		Bitmap dotHBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.list_divide_dot);
		mDotHDrawable = new BitmapDrawable(context.getResources(), dotHBitmap);
		mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mDotHDrawable.setDither(true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (position == mDataList.size()) {// 获取更多信息
			if (!IsAdding()) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.vw_generic_more, null);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.vw_generic_loading, null);
			}
			return convertView;
		} else {
			if (convertView == null || convertView.getTag() == null) {
				convertView = createView();
			}
			mHolder = (ViewHolder) convertView.getTag();
			Book book = (Book) getItem(position);
			if (book.getDownloadInfo().getImageUrl() != null
					&& !book.getDownloadInfo().getImageUrl()
							.contains("http://")) {
				book.getDownloadInfo().setImageUrl(null);
			}
			ImageLoader.getInstance().load(
					book.getDownloadInfo().getImageUrl(), mHolder.headerImg,
					ImageLoader.TYPE_COMMON_BOOK_COVER,
					ImageLoader.getDefaultPic());

			mHolder.title.setText(book.getTitle());
			if (book.getAuthor() != null
					&& !book.getAuthor().equalsIgnoreCase("")) {
				mHolder.author.setVisibility(View.VISIBLE);
				mHolder.author.setText(mContext.getString(R.string.author)
						+ book.getAuthor());
			} else {
				mHolder.author.setVisibility(View.GONE);
			}
			if (book.getIntro() != null) {
				mHolder.bookInfo.setText(Html.fromHtml(book.getIntro().trim()));
			} else {
				mHolder.bookInfo.setText("'");
			}
			if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
				mHolder.costLayout.setVisibility(View.VISIBLE);
				mHolder.cost.setText("免费");
				mHolder.cost.setTextColor(ResourceUtil
						.getColor(R.color.book_free_color));
			} else if (book.isSuite()) {
				mHolder.costLayout.setVisibility(View.VISIBLE);
				mHolder.cost.setText("包月");
				mHolder.cost.setTextColor(ResourceUtil
						.getColor(R.color.book_free_color));
			} else if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {
				mHolder.costLayout.setVisibility(View.VISIBLE);
				mHolder.cost.setTextColor(ResourceUtil
						.getColor(R.color.book_seria_color));
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
					mHolder.costLayout.setVisibility(View.GONE);
					break;
				}
			} else if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP
					&& book.getBuyInfo().getPrice() > 0) {
				mHolder.costLayout.setVisibility(View.VISIBLE);
				mHolder.cost.setText(book.getBuyInfo().getPrice() + mContext.getString(R.string.u_bi_name));
				mHolder.cost.setTextColor(ResourceUtil
						.getColor(R.color.book_charge_color));

			} else {
				mHolder.costLayout.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	public void clearList()
	{
		if (mDataList != null) {
			mDataList.clear();
		}
	}

	protected View createView()
	{
		View itemView = LayoutInflater.from(mContext).inflate(
				R.layout.vw_book_list_item, null);
		ViewHolder holder = new ViewHolder();
		holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
		holder.title = (TextView) itemView.findViewById(R.id.title);
		holder.author = (TextView) itemView.findViewById(R.id.author);
		holder.bookInfo = (TextView) itemView.findViewById(R.id.book_info);
		holder.costLayout = itemView.findViewById(R.id.cost_flag);
		holder.cost = (TextView) itemView.findViewById(R.id.cost_tv);
		ImageView listDivide = (ImageView) itemView
				.findViewById(R.id.list_divide);
		listDivide.setBackgroundDrawable(mDotHDrawable);
		itemView.setTag(holder);
		return itemView;
	}

	protected class ViewHolder
	{
		public ImageView	headerImg;
		public TextView		title;
		public TextView		author;
		public TextView		bookInfo;
		public View			costLayout;
		public TextView		cost;
	}

	@Override
	protected List<Book> createList()
	{
		return new ArrayList<Book>();
	}

}
