package com.sina.book.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.db.DBService;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.widget.SwitchButton;
import com.sina.book.ui.widget.XListView;

/**
 * 订阅更新书籍列表界面
 * 
 * @author MarkMjw
 * @date 2013-7-26
 */
public class RemindListActivity extends CustomTitleActivity {
	// private static final String TAG = "RemindListActivity";

	private XListView mListView;
	private RemindListAdapter mAdapter;
	private static List<Book> mBooks = new ArrayList<Book>();

	private View mProgressView;
	private View mEmptyView;

	/**
	 * 启动
	 * 
	 * @param c
	 *            上下文引用
	 */
	public static void launch(Context c) {
		Intent intent = new Intent();
		intent.setClass(c, RemindListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

		c.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_remind_list);

		initTitle();
		initViews();
		initData();
	}

	@Override
	public void onClickLeft() {
		finish();
		super.onClickLeft();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mBooks.isEmpty())
			return;

		for (Book book : mBooks) {
			DownBookManager.getInstance().getBook(book).setRemind(book.isRemind());
		}

		// PushHelper.getInstance().updateRemindBooks();

		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				DBService.updateBookRemind(mBooks);
				mBooks.clear();
				return null;
			}
		}.execute();
	}

	private void initTitle() {
		TextView middleTv = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (null != middleTv) {
			middleTv.setText(R.string.set_remind);
			setTitleMiddle(middleTv);
		}

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);
	}

	private void initViews() {
		mProgressView = findViewById(R.id.rl_progress);
		mEmptyView = findViewById(R.id.empty_layout);
		((TextView) mEmptyView.findViewById(R.id.empty_text)).setText(R.string.no_book_need_remind);

		mListView = (XListView) findViewById(R.id.lv_books);
		mListView.setPullRefreshEnable(false);
		mListView.setPullLoadEnable(false);
		mListView.setDivider(null);

		mAdapter = new RemindListAdapter();
		mListView.setAdapter(mAdapter);
	}

	private void initData() {
		mProgressView.setVisibility(View.VISIBLE);
		mEmptyView.setVisibility(View.GONE);
		mListView.setVisibility(View.GONE);

		new GenericTask() {
			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				List<DownBookJob> jobs = DownBookManager.getInstance().getAllJobs();
				if (!jobs.isEmpty()) {
					mBooks.clear();
					for (DownBookJob job : jobs) {
						Book book = job.getBook();
						if (book.isSeriesBook() && book.hasChapters()) {
							mBooks.add(book);
						}
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(TaskResult result) {
				super.onPostExecute(result);

				mProgressView.setVisibility(View.GONE);
				mEmptyView.setVisibility(mBooks.isEmpty() ? View.VISIBLE : View.GONE);
				mListView.setVisibility(mBooks.isEmpty() ? View.GONE : View.VISIBLE);
			}
		}.execute();
	}

	private class RemindListAdapter extends BaseAdapter {
		private ViewHolder mHolder;
		private BitmapDrawable mDotHDrawable;

		public RemindListAdapter() {
			Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.divider_dot_virtual);
			mDotHDrawable = new BitmapDrawable(getResources(), dotHBitmap);
			mDotHDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			mDotHDrawable.setDither(true);
		}

		@Override
		public int getCount() {
			return mBooks.size();
		}

		@Override
		public Object getItem(int position) {
			if (position < 0 || position >= mBooks.size()) {
				return null;
			} else {
				return mBooks.get(position);
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

			mHolder = (ViewHolder) convertView.getTag();
			updateData(position);

			return convertView;
		}

		private void updateData(final int position) {
			final Book book = mBooks.get(position);

			String imgUrl = book.getDownloadInfo().getImageUrl();
			if (imgUrl != null && !imgUrl.contains("http://")) {
				book.getDownloadInfo().setImageUrl(null);
			}
			ImageLoader.getInstance().load(imgUrl, mHolder.headerImg, ImageLoader.TYPE_COMMON_BOOK_COVER,
					ImageLoader.getDefaultPic());

			mHolder.title.setText(book.getTitle());
			String author = book.getAuthor();
			if (!TextUtils.isEmpty(author)) {
				mHolder.author.setVisibility(View.VISIBLE);
				mHolder.author.setText(mContext.getString(R.string.author) + author);
			} else {
				mHolder.author.setVisibility(View.GONE);
			}

			mHolder.updateInfo.setText(book.getIntro());

			mHolder.switchBtn.setCheckedWithOutListener(book.isRemind());
			mHolder.switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					book.setRemind(isChecked);

					notifyDataSetChanged();
				}
			});
		}

		protected View createView() {
			View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_remind_list_item, null);

			ViewHolder holder = new ViewHolder();
			holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
			holder.title = (TextView) itemView.findViewById(R.id.title);
			holder.author = (TextView) itemView.findViewById(R.id.author);
			holder.updateInfo = (TextView) itemView.findViewById(R.id.update_info);
			holder.switchBtn = (SwitchButton) itemView.findViewById(R.id.remind_switch);
			holder.listDivide = itemView.findViewById(R.id.list_divide);
			holder.listDivide.setBackgroundDrawable(mDotHDrawable);

			itemView.setTag(holder);
			return itemView;
		}

		protected class ViewHolder {
			public ImageView headerImg;
			public TextView title;
			public TextView author;
			public TextView updateInfo;
			public View listDivide;
			public SwitchButton switchBtn;
		}
	}
}
