package com.sina.book.ui.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.UpdateChapterManager;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.WeiboContent;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.ShareDialog;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * 书架List Adapter
 * 
 * @author MarkMjw
 * @date 13-8-6.
 */
public class ShelvesListAdapter extends ListAdapter<DownBookJob> {
	private static final String TAG = "ShelvesListAdapter";

	private Activity mActivity;

	private ListView mListView;
	private ViewHolder mHolder;

	private SparseBooleanArray mBooleanArray;

	private CustomProDialog mProgressDialog;

	private BitmapDrawable mDividerDrawable;

	public ShelvesListAdapter(Activity context, ListView listView) {
		mActivity = context;
		mListView = listView;
		mBooleanArray = new SparseBooleanArray();

		Bitmap dotHBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.list_divide_dot);
		mDividerDrawable = new BitmapDrawable(mActivity.getResources(), dotHBitmap);
		mDividerDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mDividerDrawable.setDither(true);
	}

	@Override
	protected List<DownBookJob> createList() {
		return new ArrayList<DownBookJob>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView.getTag() == null) {
			convertView = createViewList();
		}

		mHolder = (ViewHolder) convertView.getTag();
		DownBookJob job = (DownBookJob) getItem(position);
		Book book = job.getBook();

		// 加载封面图片
		loadBookCover(book);

		// 加载进度条
		loadProgressView(job, book);

		// 加载其他控件
		loadOtherView(position, job, book, convertView);

		return convertView;
	}

	private void loadBookCover(final Book book) {
		if (book.getDownloadInfo().getImageUrl() != null
				&& book.getDownloadInfo().getImageUrl().startsWith(Book.SDCARD_PATH_IMG)) {
			// SD卡书籍
			mHolder.cover.setImageBitmap(ImageLoader.getNoImgPic());

		} else {
			// 其他书籍
			ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), mHolder.cover,
					ImageLoader.TYPE_BOOK_HOME_SHELVES_COVER, ImageLoader.getNoImgPic());
		}
	}

	private void loadProgressView(final DownBookJob job, final Book book) {
		if (Math.abs(job.getBook().getDownloadInfo().getProgress() - 1.0) > 0.0001
				|| job.getState() != DownBookJob.STATE_FINISHED) {
			// 给按钮设置监听器
			setDownBtnListener(job, book);

			mHolder.downBtn.setVisibility(View.VISIBLE);

			if (job.getState() == DownBookJob.STATE_PAUSED || job.getState() == DownBookJob.STATE_FAILED) {
				// 下载暂停或者下载失败的情况
				mHolder.downBtn.setImageResource(R.drawable.book_pause);
				mHolder.downPro.setVisibility(View.GONE);

			} else {
				// 正在下载或者解析的情况
				mHolder.downBtn.setImageResource(R.drawable.book_down);
				mHolder.downPro.setVisibility(View.VISIBLE);
				mHolder.downPro.setProgress((int) Math.round(100 * book.getDownloadInfo().getProgress()));
			}
			mHolder.newIcon.setVisibility(View.GONE);
			mHolder.newChapter.setVisibility(View.GONE);

		} else {
			// 下载成功
			mHolder.downBtn.setVisibility(View.GONE);
			mHolder.downPro.setVisibility(View.GONE);

			// 更新书籍文件长度
			updateBookLength(book);

			// 是否显示“new”标签
			if (book.getTag() == Book.IS_NEW) {
				mHolder.newIcon.setVisibility(View.VISIBLE);
				mHolder.newIcon.setBackgroundResource(R.drawable.new_icon);
			} else {
				mHolder.newIcon.setVisibility(View.GONE);
			}

			// 是否显示更新章节标签
			if (book.getUpdateChaptersNum() > 0) {
				mHolder.newChapter.setVisibility(View.VISIBLE);
				mHolder.newChapter.setText("" + book.getUpdateChaptersNum());

				mHolder.cloud.setVisibility(View.GONE);
			} else {
				mHolder.newChapter.setVisibility(View.GONE);
				mHolder.newChapter.setText("0");

				mHolder.cloud.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 更新书籍文件长度
	 * 
	 * @param book
	 */
	private void updateBookLength(Book book) {
		String filePath = book.getDownloadInfo().getFilePath();
		if (filePath.contains("file")) {
			try {
				filePath = StorageUtil.copyFile(filePath.substring(filePath.lastIndexOf('/')));
			} catch (IOException e) {
				LogUtil.e(TAG, e.getLocalizedMessage());
			}
		}

		File bookFile = new File(filePath);
		if (!bookFile.exists() || !bookFile.isFile() || !bookFile.canRead() || bookFile.length() <= 0) {
			book.getDownloadInfo().setFileSize(0);
		} else {
			book.getDownloadInfo().setFileSize(bookFile.length());
		}
	}

	/**
	 * 设置下载、暂停、重试按钮的监听器
	 * 
	 * @param job
	 * @param book
	 */
	private void setDownBtnListener(final DownBookJob job, final Book book) {
		mHolder.downBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (job.getState() == DownBookJob.STATE_PAUSED || job.getState() == DownBookJob.STATE_FAILED) {
					// 当书籍不是免费书，且没有登录时，弹出登陆对话框
					if (book.getBuyInfo().getPayType() != Book.BOOK_TYPE_FREE
							&& LoginUtil.isValidAccessToken(mActivity) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
						LoginDialog.launch(mActivity, null);
					} else {
						job.start();
					}

				} else {
					job.pause();
				}
			}
		});
	}

	/**
	 * 为其他控件加载数据
	 * 
	 * @param position
	 * @param job
	 * @param book
	 */
	private void loadOtherView(int position, DownBookJob job, Book book, View itemView) {
		mHolder.title.setText(book.getTitle());
		mHolder.author.setText(mActivity.getString(R.string.author) + book.getAuthor());
		float percent = book.getReadInfo().getLastReadPercent();
		if (percent <= 0 && book.getReadInfo().getLastPos() != 0) {
			if (book.getBuyInfo().getPayType() != Book.BOOK_TYPE_VIP
					&& book.getBuyInfo().getPayType() != Book.BOOK_TYPE_CHAPTER_VIP
					&& book.getDownloadInfo().getFileSize() > 0) {
				percent = (float) (book.getReadInfo().getLastPos() * 1.0 / book.getDownloadInfo().getFileSize()) * 100;
			}
		}

		percent = (Math.round(percent * 100f)) / 100;
		mHolder.readPro.setProgress((int) percent);
		String perStr = String.format(mActivity.getString(R.string.bookhome_read_percent), book.getReadInfo()
				.getLastReadPercent());
		mHolder.percent.setText(perStr);

		ClickListener listener = new ClickListener(mHolder, position, job, itemView);
		mHolder.menuBtn.setOnClickListener(listener);
		mHolder.menuDeleteBtn.setOnClickListener(listener);
		mHolder.menuShareBtn.setOnClickListener(listener);
		mHolder.menuUpdateBtn.setOnClickListener(listener);
		mHolder.menuDownBtn.setOnClickListener(listener);

		if (mBooleanArray.get(position, false)) {
			mHolder.menuBtn.setImageResource(R.drawable.menu_btn_up);
			mHolder.menuBtnLayout.setVisibility(View.VISIBLE);
		} else {
			if (book.getUpdateChaptersNum() > 0) {
				mHolder.menuBtn.setImageResource(R.drawable.menu_btn_update);
			} else {
				mHolder.menuBtn.setImageResource(R.drawable.menu_btn_down);
			}
			mHolder.menuBtnLayout.setVisibility(View.GONE);
		}

		if (book.isOurServerBook() && book.isOnlineBook()) {
			mHolder.menuDownBtn.setVisibility(View.VISIBLE);
			mHolder.menuUpdateBtn.setVisibility(View.GONE);
		} else {
			mHolder.menuDownBtn.setVisibility(View.GONE);
			mHolder.menuUpdateBtn.setVisibility(View.VISIBLE);

			// 不是自己服务器上以及微盘上下载的书籍分享更新按钮不可点
			if (!book.isOurServerBook()) {
				if (!book.isVDiskBook()) {
					mHolder.menuShareBtn.setEnabled(false);
				}
				mHolder.menuUpdateBtn.setEnabled(false);
			} else {
				mHolder.menuShareBtn.setEnabled(true);
				mHolder.menuUpdateBtn.setEnabled(true);
			}
		}

		if (book.isOnlineBook()) {
			mHolder.cloud.setVisibility(View.VISIBLE);
		} else {
			mHolder.cloud.setVisibility(View.GONE);
		}

		mHolder.divider.setImageDrawable(mDividerDrawable);
	}

	protected View createViewList() {
		View itemView = LayoutInflater.from(mActivity).inflate(R.layout.vw_shelf_list_item, null);
		ViewHolder holder = new ViewHolder();

		holder.bookLayout = itemView.findViewById(R.id.item_content_layout);

		View bookView = holder.bookLayout.findViewById(R.id.item_book_view);

		holder.cover = (ImageView) bookView.findViewById(R.id.book_img);
		holder.downPro = (ProgressBar) bookView.findViewById(R.id.book_download_progressBar);
		holder.downBtn = (ImageView) bookView.findViewById(R.id.book_down_pause);
		holder.newIcon = (ImageView) bookView.findViewById(R.id.book_tip);
		holder.newChapter = (TextView) bookView.findViewById(R.id.book_new_chapter);
		holder.cloud = (ImageView) bookView.findViewById(R.id.book_cloud);

		holder.title = (EllipsizeTextView) holder.bookLayout.findViewById(R.id.title);
		holder.author = (TextView) holder.bookLayout.findViewById(R.id.author);

		holder.readPro = (ProgressBar) holder.bookLayout.findViewById(R.id.read_progress_bar);
		holder.percent = (TextView) holder.bookLayout.findViewById(R.id.read_progress_percent);

		holder.menuBtn = (ImageView) itemView.findViewById(R.id.item_menu_btn);
		holder.menuBtnLayout = (RelativeLayout) itemView.findViewById(R.id.item_menu_layout);
		holder.menuDeleteBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_delete);
		holder.menuShareBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_share);
		holder.menuUpdateBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_update);
		holder.menuDownBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_down);

		holder.divider = (ImageView) itemView.findViewById(R.id.item_divider);

		itemView.setTag(holder);
		return itemView;
	}

	protected class ViewHolder {
		// 左边书籍封面
		private View bookLayout;
		private ImageView cover;
		private ProgressBar downPro;
		private ImageView downBtn;
		private ImageView newIcon;
		private TextView newChapter;
		private ImageView cloud;

		// 中间书籍信息和阅读进度条
		private EllipsizeTextView title;
		private TextView author;
		private ProgressBar readPro;
		private TextView percent;

		// 右侧按钮和菜单栏
		private ImageView menuBtn;
		private RelativeLayout menuBtnLayout;
		private TextView menuDeleteBtn;
		private TextView menuShareBtn;
		private TextView menuUpdateBtn;
		private TextView menuDownBtn;

		// 分割线
		private ImageView divider;
	}

	private class ClickListener implements View.OnClickListener {
		private ViewHolder holder;
		private int position;
		private DownBookJob job;
		private View view;

		public ClickListener(ViewHolder holder, int position, DownBookJob job, View view) {
			this.holder = holder;
			this.position = position;
			this.job = job;
			this.view = view;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.item_menu_btn:
				mBooleanArray.clear();

				if (!holder.menuBtnLayout.isShown()) {
					mBooleanArray.put(position, true);
					notifyDataSetChanged();

					// 列表菜单栏的高度
					int menuHeight = (int) ResourceUtil.getDimens(R.dimen.bookhome_item_menu_height);
					if (mListView.getHeight() - view.getBottom() < menuHeight) {
						int itemMinHeight = v.getHeight();
						int height = mListView.getHeight() - itemMinHeight - menuHeight;

						int curPosition = position + mListView.getHeaderViewsCount() - height / itemMinHeight;

						if (curPosition < mDataList.size() && curPosition >= 0) {
							mListView.setSelectionFromTop(curPosition, height % itemMinHeight);
						}
					}

				} else {
					notifyDataSetChanged();
				}
				break;

			case R.id.item_menu_btn_delete:
				deleteBook(job);
				break;

			case R.id.item_menu_btn_share:
				share(job.getBook());
				clearListExpand();
				break;

			case R.id.item_menu_btn_update:
				List<Book> needCheckBooks = new ArrayList<Book>();
				needCheckBooks.add(job.getBook());
				Toast.makeText(mActivity, R.string.xlistview_header_hint_loading, Toast.LENGTH_SHORT).show();
				UpdateChapterManager.getInstance()
						.checkNewChapter(needCheckBooks, UpdateChapterManager.REQ_SINGLE_BOOK);
				clearListExpand();
				break;
			case R.id.item_menu_btn_down:
				if (job == null) {
					return;
				}
				final Book book = job.getBook();
				DownBookManager.getInstance().downBook(book);
				clearListExpand();
				break;
			default:
				break;
			}
		}
	}

	private void deleteBook(final DownBookJob job) {
		String msg = String.format(mActivity.getString(R.string.note_info), job.getBook().getTitle());
		CommonDialog.show(mActivity, msg, new CommonDialog.DefaultListener() {
			@Override
			public void onRightClick(DialogInterface dialog) {
				final Book book = job.getBook();
				mDataList.remove(job);
				DownBookManager.getInstance().removeJob(job);

				// 如果没有数据则将Adapter设置为null
				if (DownBookManager.getInstance().getAllJobs().size() <= 0) {
					mListView.setAdapter(null);
				}

				// SD卡上的书籍不能被删除
				String imgUrl = book.getDownloadInfo().getImageUrl();
				if (!TextUtils.isEmpty(imgUrl) && !imgUrl.startsWith(Book.SDCARD_PATH_IMG)) {
					StorageUtil.deleteFolder(book.getDownloadInfo().getFilePath());
					StorageUtil.deleteFolder(book.getDownloadInfo().getImageFolder());
				}

				mBooleanArray.clear();
				notifyDataSetChanged();
				CloudSyncUtil.getInstance().delCloud(mActivity, book);
			}

			@Override
			public void onLeftClick(DialogInterface dialog) {
				clearListExpand();
			}
		});
	}

	private void share(final Book book) {
		new GenericTask() {
			WeiboContent mContent;

			@Override
			protected void onPreExecute() {
				showProgressDialog(R.string.create_share_content);
			}

			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				mContent = new WeiboContent(book, WeiboContent.TYPE_BOOK_DETAIL);

				Bitmap cover = ImageLoader.getInstance().syncLoadBitmap(book.getDownloadInfo().getImageUrl());
				if (null != cover && cover != ImageLoader.getDefaultPic()) {
					mContent.setImagePath(Util.saveBitmap2file(cover, book.getTitle(), 100));
				}

				mContent.setChapterId(Chapter.DEFAULT_GLOBAL_ID);
				mContent.setChapterOffset(0);

				if (book.isVDiskBook()) {
					// 微盘书籍生成分享链接
					mContent.setMsg(book.getDownloadInfo().getVDiskFilePath());
				} else {
					// 其他书籍分享简介
					mContent.setMsg(book.getIntro());
				}

				return null;
			}

			@Override
			protected void onPostExecute(TaskResult result) {
				dismissProgressDialog();

				if (null != mContent && !TextUtils.isEmpty(mContent.getMsg())) {
					ShareDialog.show(mActivity, mContent);
				} else {
					Toast.makeText(mActivity, R.string.create_share_content_failed, Toast.LENGTH_SHORT).show();
				}
			}
		}.execute();
	}

	/**
	 * 显示进度条.
	 * 
	 * @param resId
	 *            the res id
	 */
	private void showProgressDialog(int resId) {
		if (null == mProgressDialog) {
			mProgressDialog = new CustomProDialog(mActivity);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}

		mProgressDialog.show(resId);
	}

	/**
	 * 隐藏进度条
	 */
	private void dismissProgressDialog() {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	/**
	 * 清除列表选项菜单展开的情况
	 */
	public void clearListExpand() {
		mBooleanArray.clear();
		notifyDataSetChanged();
	}
}
