package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
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
import com.sina.book.util.Util;

/**
 * 推荐页男生女生出版列表适配器
 * 
 * @author MarkMjw
 * @date 2013-8-29
 */
public class RecommendCmsListAdapter extends ListAdapter<Book> {
	private static final int TYPE_FIRST = 0;
	private static final int TYPE_ITEM = 1;
	private static final int TYPE_MAX_COUNT = 2;

	private String mActionType = "";

	private Context mContext;

	private LayoutInflater mInflater;

	private BitmapDrawable mDivider;

	private String mParentRectName;
	private String mChildRectName;

	public RecommendCmsListAdapter(Context context, String mParentRectName, String mChildRectName) {
		this.mContext = context;
		this.mParentRectName = mParentRectName;
		this.mChildRectName = mChildRectName;

		mInflater = LayoutInflater.from(context);

		Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.divider_dot_real);
		mDivider = new BitmapDrawable(context.getResources(), dotHBitmap);
		mDivider.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mDivider.setDither(true);
	}

	public void setActionType(String actionType) {
		mActionType = actionType;
	}

	@Override
	protected List<Book> createList() {
		return new ArrayList<Book>();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return 0 == position ? TYPE_FIRST : TYPE_ITEM;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = getItemViewType(position);
		Book book = mDataList.get(position);

		switch (type) {
		case TYPE_FIRST: {
			if (null == convertView || null == convertView.getTag(R.layout.vw_recommend_cms_item0)) {
				convertView = createFirstView();
			}

			Object tag = convertView.getTag(R.layout.vw_recommend_cms_item0);
			ViewHolderFirst holder = (ViewHolderFirst) tag;

			if (null != holder) {
				holder.title.setText(book.getTitle());
				holder.author.setText("作者：" + book.getAuthor());
				holder.intro.setText("简介：" + book.getIntro());
				holder.divider.setImageDrawable(mDivider);

				ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), holder.cover,
						ImageLoader.getDefaultPic());
			}
			break;
		}

		case TYPE_ITEM: {
			if (null == convertView || null == convertView.getTag(R.layout.vw_recommend_cms_item)) {
				convertView = createView();
			}

			Object tag = convertView.getTag(R.layout.vw_recommend_cms_item);
			ViewHolder holder = (ViewHolder) tag;

			if (null != holder) {
				holder.title.setText(book.getTitle());
				holder.divider.setImageDrawable(mDivider);
			}
			break;
		}

		default:
			break;
		}

		convertView.setOnClickListener(new OnClickListener(book, position));

		return convertView;
	}

	private View createFirstView() {
		View view = mInflater.inflate(R.layout.vw_recommend_cms_item0, null);
		ViewHolderFirst holder = new ViewHolderFirst();

		if (null != view) {
			holder.cover = (ImageView) view.findViewById(R.id.cover);
			holder.title = (TextView) view.findViewById(R.id.title);
			holder.author = (TextView) view.findViewById(R.id.author);
			holder.intro = (EllipsizeTextView) view.findViewById(R.id.intro);
			holder.divider = (ImageView) view.findViewById(R.id.divider);

			view.setTag(R.layout.vw_recommend_cms_item0, holder);
		}
		return view;
	}

	private View createView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_recommend_cms_item, null);
		ViewHolder holder = new ViewHolder();

		if (view != null) {
			holder.title = (TextView) view.findViewById(R.id.title);
			holder.divider = (ImageView) view.findViewById(R.id.divider);
			view.setTag(R.layout.vw_recommend_cms_item, holder);
		}
		return view;
	}

	private class ViewHolderFirst {
		private ImageView cover;
		private TextView title;
		private TextView author;
		private EllipsizeTextView intro;
		private ImageView divider;
	}

	private class ViewHolder {
		private TextView title;
		private ImageView divider;
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
				//
				String eventKey = mParentRectName + "_" + mChildRectName + "_" + Util.formatNumber(mPosition + 1);
				BookDetailActivity.launchNew(mContext, mBook, eventKey);

				if (!TextUtils.isEmpty(mActionType)) {
					UserActionManager.getInstance().recordEvent(mActionType);
				}
			}
		}
	}
}
