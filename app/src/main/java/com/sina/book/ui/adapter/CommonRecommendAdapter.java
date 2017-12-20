package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.CommonRecommendItem;
import com.sina.book.data.UserInfoRec;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.AuthorActivity;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.CommonRecommendActivity;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

public class CommonRecommendAdapter extends ListAdapter<CommonRecommendItem> {

	private Context mContext;
	private Date mNow;
	private String mCommonRecommandType;

	public CommonRecommendAdapter(Context context, String commonRecommandType) {
		this.mContext = context;
		this.mCommonRecommandType = commonRecommandType;

		mNow = new Date();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();

		CommonRecommendItem item = mDataList.get(position);

		UserInfoRec userInfoRec = item.getUserInfoRec();
		if (userInfoRec != null) {
			ImageLoader.getInstance().load2(userInfoRec.getUserProfileUrl(), holder.userImage,
					ImageLoader.getDefaultMainAvatar());
			holder.userName.setText(userInfoRec.getuName());
		}

		if (!TextUtils.isEmpty(item.getRecommendTime())) {
			String time = Util.getTimeToDisplay1(item.getRecommendTime(), mNow);
			int color = ResourceUtil.getColor(R.color.all_recommend_time_color);
			Spanned spanned = Util.highLight(time, color, 0, time.length());
			holder.userRecommendTime.setText(spanned);

			String recommend = ResourceUtil.getString(R.string.has_recommend);
			int color1 = ResourceUtil.getColor(R.color.all_recommend_font_color);
			Spanned spanned1 = Util.highLight(recommend, color1, 0, recommend.length());
			holder.userRecommendTime.append(spanned1);
		}

		Book book = item.getBook();
		if (book != null) {
			if (book.getDownloadInfo().getImageUrl() != null
					&& !book.getDownloadInfo().getImageUrl().contains("http://")) {
				book.getDownloadInfo().setImageUrl(null);
			}
			ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), holder.bookImage,
					ImageLoader.TYPE_COMMON_BOOK_COVER, ImageLoader.getDefaultPic());

			holder.bookTitle.setText(book.getTitle());
			holder.bookRecommendNums.setText(String.valueOf(book.getPraiseNum()));

			if (!TextUtils.isEmpty(book.getAuthor())) {
				holder.bookInfo.setText(book.getAuthor());
			}

			if (!TextUtils.isEmpty(book.getBookCate())) {
				holder.bookInfo.append("/" + book.getBookCate());
			}

			if (!TextUtils.isEmpty(book.getUpdateChapterNameServer()) && !TextUtils.isEmpty(book.getUpdateTimeServer())) {
				String updateTime = Util.getTimeToDisplay(book.getUpdateTimeServer());
				holder.bookInfo.append("/" + updateTime);

				String newChapter = book.getUpdateChapterNameServer();
				holder.bookInfo.append("/" + newChapter);
			} else {
				String allChapter = String.format(ResourceUtil.getString(R.string.book_all), book.getNum());
				holder.bookInfo.append("/" + allChapter);
			}
		}

		ClickListener listener = new ClickListener(item, position);
		holder.bookImageClick.setOnClickListener(listener);
		holder.bookLayout.setOnClickListener(listener);
		holder.userLayout.setOnClickListener(listener);

		return convertView;
	}

	private View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_common_recommend_list_item,
				new LinearLayout(mContext));
		ViewHolder holder = new ViewHolder();
		holder.userImage = (ImageView) itemView.findViewById(R.id.recommend_user_image);
		holder.userName = (TextView) itemView.findViewById(R.id.recommend_user_name);
		holder.userRecommendTime = (TextView) itemView.findViewById(R.id.recommend_time);
		holder.bookImage = (ImageView) itemView.findViewById(R.id.recommend_book_image);
		holder.bookImageClick = (ImageView) itemView.findViewById(R.id.recommend_book_image_click);
		holder.bookTitle = (TextView) itemView.findViewById(R.id.recommend_book_title);
		holder.bookInfo = (TextView) itemView.findViewById(R.id.recommend_book_info);
		holder.bookRecommendNums = (TextView) itemView.findViewById(R.id.recommend_book_nums);
		holder.userLayout = (LinearLayout) itemView.findViewById(R.id.recommend_user_layout);
		holder.bookLayout = (LinearLayout) itemView.findViewById(R.id.recommend_book_layout);

		itemView.setTag(holder);
		return itemView;
	}

	@Override
	protected List<CommonRecommendItem> createList() {
		return new ArrayList<CommonRecommendItem>();
	}

	public void clearList() {
		if (mDataList != null) {
			mDataList.clear();
		}
	}

	public void updateDate() {
		mNow = new Date();
	}

	private class ViewHolder {
		private ImageView userImage;
		private TextView userName;
		private TextView userRecommendTime;
		private ImageView bookImage;
		private ImageView bookImageClick;
		private TextView bookTitle;
		private TextView bookInfo;
		private TextView bookRecommendNums;
		private LinearLayout userLayout;
		private LinearLayout bookLayout;

	}

	private class ClickListener implements View.OnClickListener {

		private CommonRecommendItem mItem;
		private int mPosition;

		public ClickListener(CommonRecommendItem item, int position) {
			this.mItem = item;
			this.mPosition = position;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.recommend_book_image_click:
			case R.id.recommend_book_layout: {
				// 大家推荐
				String eventKey = "";
				if (CommonRecommendActivity.PEOPLE_TYPE.equals(mCommonRecommandType)) {
					eventKey = "大家推荐";
				} else if (CommonRecommendActivity.EMPLOYEE_TYPE.equals(mCommonRecommandType)) {
					eventKey = "员工推荐";
				}
				eventKey += "_01_" + Util.formatNumber(mPosition + 1);
				BookDetailActivity.launchNew(mContext, mItem.getBook(), eventKey);
			}
				break;

			case R.id.recommend_user_layout: {
				// 进入用户
				String title = String.format(Locale.CHINA, ResourceUtil.getString(R.string.staff_recommend_title),
						mItem.getUserInfoRec().getuName());
				int type = 0;
				if (CommonRecommendActivity.PEOPLE_TYPE.equals(mCommonRecommandType)) {
					type = AuthorActivity.TYPE_USER;
				} else if (CommonRecommendActivity.EMPLOYEE_TYPE.equals(mCommonRecommandType)) {
					type = AuthorActivity.TYPE_STAFF;
				}
				AuthorActivity.launch(mContext, title, mItem.getUserInfoRec().getUid(), type);
			}
				break;

			// case R.id.recommend_book_layout: {
			// // 大家推荐
			// String eventKey = "大家推荐_01_" + Util.formatNumber(mPosition + 1);
			// BookDetailActivity.launchNew(mContext, mItem.getBook(),
			// eventKey);
			// }
			// break;
			}

		}

	}

}