package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.MainBookItem;
import com.sina.book.image.IImageLoadListener;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.MainTabActivity;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

public class MainTabAdapter extends BaseAdapter {

	public static final String TOP_COMMENT = "热门评论榜";

	private Context mContext;
	private List<MainBookItem> mDatas;
	private ViewHolder mHolder;

	private IStateChangedListener mListener;

	public MainTabAdapter(Context context) {
		this.mContext = context;
	}

	public void setData(List<MainBookItem> items) {
		if (null == items) {
			return;
		}

		if (null == mDatas) {
			mDatas = new ArrayList<MainBookItem>();
		}
		mDatas.clear();
		mDatas.addAll(items);
	}

	@Override
	public int getCount() {
		if (mDatas == null || mDatas.size() == 0) {
			return 0;
		} else {
			return mDatas.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mDatas.size()) {
			return mDatas.get(position);
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
		if (null == convertView || null == convertView.getTag()) {
			convertView = createView();
		}

		final int index = position;
		mHolder = (ViewHolder) convertView.getTag();

		MainBookItem item = (MainBookItem) getItem(position);
		final Book book = item.getBook();
		if (null != book) {
			ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), mHolder.bookHeadImage,
					ImageLoader.TYPE_BIG_PIC, ImageLoader.getDefaultPic(), new IImageLoadListener() {

						@Override
						public void onImageLoaded(Bitmap bm, ImageView imageView, boolean loadSuccess) {
							if (loadSuccess) {
								mHolder.bookHeadTitle.setText("");
							} else {
								// 如果imageurl加载失败，默认书皮上会显示书籍标题
								if (book != null) {
									mHolder.bookHeadTitle.setText(book.getTitle());
								}
							}
						}
					});
			mHolder.bookTitle.setText(book.getTitle());

			String type = "";
			String head = "";
			String body = "";
			if (MainBookItem.TYPE_RECOMMEND.equals(item.getType())) {
				type = "精选推荐" + "-" + book.getComment();
				mHolder.bookType.setText(type);

				if (!TextUtils.isEmpty(book.getContentTag())) {
					head = "关联书签：";
					body = book.getContentTag();
				} else {
					head = book.getPraiseNum() + "";
					body = "人推荐";
				}
			} else if (MainBookItem.TYPE_TOPLIST.equals(item.getType())) {
				type = "畅销榜单" + "-" + book.getComment();
				mHolder.bookType.setText(type);

				if (TOP_COMMENT.equals(book.getComment())) {
					head = book.getCommentNum() + "";
					body = "人评论";
				} else {
					head = book.getPraiseNum() + "";
					body = "人推荐";
				}
			} else if (MainBookItem.TYPE_CATE.equals(item.getType())) {
				type = "分类浏览" + "-" + book.getComment();
				mHolder.bookType.setText(type);

				if (!TextUtils.isEmpty(book.getContentTag())) {
					head = "关联标签：";
					body = book.getContentTag();
				} else {
					head = book.getPraiseNum() + "";
					body = "人推荐";
				}
			} else if (MainBookItem.TYPE_PEOPLE.equals(item.getType())) {
				type = "大家推荐";
				mHolder.bookType.setText(type);
				if (item.getPeopleRecommend() != null) {
					head = item.getPeopleRecommend().getuName();
					if (head == null) {
						head = "";
					}
				} else {
					head = "";
				}
				body = "推荐";
			}

			if (!TextUtils.isEmpty(book.getRecommendIntro())) {
				head = "编辑推荐：";
				body = book.getRecommendIntro();
			}

			mHolder.bookContent.setText(Util.highLight(head, ResourceUtil.getColor(R.color.main_bottom_special_color),
					0, head.length()));
			mHolder.bookContent.append(body);

		}

		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MainTabActivity.launch((Activity) mContext, index);
				if (null != mListener) {
					mListener.stateChanged();
				}
				switch (index) {
				case 0:
					UserActionManager.getInstance().recordEvent(Constants.CLICK_MAIN_RECOMMEND);
					break;
				case 1:
					UserActionManager.getInstance().recordEvent(Constants.CLICK_MAIN_RANKING);
					break;
				case 2:
					UserActionManager.getInstance().recordEvent(Constants.CLICK_MAIN_CLASSIFIED);
					break;
				case 3:
					UserActionManager.getInstance().recordEvent(Constants.CLICK_MAIN_ALL_READ);
					break;
				}
			}
		});

		return convertView;
	}

	public void setStateChangedListener(IStateChangedListener listener) {
		this.mListener = listener;
	}

	private View createView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_main_item, null);

		ViewHolder holder = new ViewHolder();
		holder.bookHeadImage = (ImageView) view.findViewById(R.id.book_head_img);
		holder.bookHeadTitle = (TextView) view.findViewById(R.id.book_head_title);
		holder.bookTitle = (TextView) view.findViewById(R.id.book_title);
		holder.bookType = (TextView) view.findViewById(R.id.book_type);
		holder.bookContent = (TextView) view.findViewById(R.id.book_content);

		view.setTag(holder);

		return view;
	}

	private class ViewHolder {
		private ImageView bookHeadImage;
		private TextView bookHeadTitle;
		private TextView bookTitle;
		private TextView bookType;
		private TextView bookContent;

	}

	/**
	 * 对话框状态改变事件监听
	 * 
	 * @author YangZhanDong
	 * 
	 */
	public interface IStateChangedListener {

		/**
		 * 状态改变
		 */
		public void stateChanged();
	}

}