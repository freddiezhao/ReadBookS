package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.PartitionItem;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.widget.EllipsizeTextView;

/**
 * 分类页数据Adapter
 * 
 * @author MarkMjw
 * @date 2013-8-30
 */
public class PartitionAdapter1 extends ListAdapter<PartitionItem> {
	private Context mContext;

	private BitmapDrawable mDividerH;

	public PartitionAdapter1(Context context) {
		this.mContext = context;

		decodeDivider();
	}

	private void decodeDivider() {
		Bitmap dividerH = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.divider_dot_real);
		mDividerH = new BitmapDrawable(mContext.getResources(), dividerH);
		mDividerH.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mDividerH.setDither(true);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	protected List<PartitionItem> createList() {
		return new ArrayList<PartitionItem>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || null == convertView.getTag()) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		PartitionItem item = (PartitionItem) getItem(position);

		// TODO： 选你喜欢的图标，先去掉，因为选你喜欢界面被屏蔽了。
		// if (item.getIsFavorite()) {
		// holder.choosedImage.setVisibility(View.VISIBLE);
		// } else {
		// holder.choosedImage.setVisibility(View.GONE);
		// }
		holder.choosedImage.setVisibility(View.GONE);

		holder.dividerH.setImageDrawable(mDividerH);

		holder.typeName.setText(item.getName());

		List<Book> list = item.getBookLists();
		for (int i = 0; i < list.size(); i++) {
			String title = list.get(i).getTitle();
			switch (i) {
			case 0:
				ImageLoader.getInstance().load3(list.get(0).getDownloadInfo().getImageUrl(), holder.bookCover,
						ImageLoader.TYPE_SMALL_PIC, ImageLoader.getNoImgPic());
				break;

			case 1:
				holder.bookTitle1.setText(title);
				break;

			case 2:
				holder.bookTitle2.setText(title);
				break;
			}
		}

		return convertView;
	}

	private View createView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_partition_item1, null);

		if (view == null) {
			return null;
		}

		ViewHolder holder = new ViewHolder();
		holder.bookCover = (ImageView) view.findViewById(R.id.partition_img);
		holder.typeName = (TextView) view.findViewById(R.id.partition_type);
		holder.bookTitle1 = (EllipsizeTextView) view.findViewById(R.id.partition_book_1);
		holder.bookTitle2 = (EllipsizeTextView) view.findViewById(R.id.partition_book_2);
		holder.dividerH = (ImageView) view.findViewById(R.id.partition_divider_h);
		holder.choosedImage = (ImageView) view.findViewById(R.id.choosed_image);
		view.setTag(holder);

		return view;
	}

	private class ViewHolder {
		public ImageView bookCover;
		public TextView typeName;
		public EllipsizeTextView bookTitle1;
		public EllipsizeTextView bookTitle2;
		public ImageView dividerH;
		public ImageView choosedImage;
	}
}
