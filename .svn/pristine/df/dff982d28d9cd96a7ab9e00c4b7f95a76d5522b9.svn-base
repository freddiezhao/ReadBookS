package com.sina.book.ui.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.sina.book.data.BookDownloadInfo;
import com.sina.book.data.Chapter;
import com.sina.book.data.WeiboContent;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.ReadActivity;
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
 * 书架Grid Adapter
 * 
 * @author MarkMjw
 * @date 13-8-6.
 */
public class ShelvesGridAdapter extends BaseAdapter {
	private static final String TAG = "ShelvesGridAdapter";
	private final int COLUMNS_NUM = 3;

	private Activity mActivity;
	private MenuDialog mMenuDialog;
	private CustomProDialog mProgressDialog;

	private ListView mListView;

	private ArrayList<List<DownBookJob>> mDatas;

	public ShelvesGridAdapter(Activity context, ListView listView) {
		mActivity = context;
		mListView = listView;

		mMenuDialog = new MenuDialog();
		mDatas = new ArrayList<List<DownBookJob>>();
	}

	/**
	 * 每行最多3项数据
	 * 
	 * @param data
	 */
	public void setData(List<DownBookJob> data) {
		if (null == data || data.isEmpty()) {
			return;
		}

		mDatas.clear();
		List<DownBookJob> itemList;

		int size = (int) Math.ceil(data.size() * 1.00 / COLUMNS_NUM);
		for (int i = 0; i < size; i++) {
			int index = i * COLUMNS_NUM + COLUMNS_NUM;
			if (index >= data.size()) {
				itemList = data.subList(i * COLUMNS_NUM, data.size());
			} else {
				itemList = data.subList(i * COLUMNS_NUM, index);
			}

			mDatas.add(itemList);
		}
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
		if (position < mDatas.size() && position >= 0) {
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
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		List<DownBookJob> jobs = mDatas.get(position);

		int size = jobs.size();

		// 没有数据的则先隐藏
		if (size > 0 && size < COLUMNS_NUM) {
			switch (size) {
			case 1:
				holder.items.get(1).layout.setVisibility(View.GONE);
				holder.items.get(2).layout.setVisibility(View.GONE);
				break;
			case 2:
				holder.items.get(2).layout.setVisibility(View.GONE);
				break;
			}
		}

		for (int i = 0; i < size; i++) {
			DownBookJob job = jobs.get(i);

			if (null == job) {
				continue;
			}

			ViewItem item = holder.items.get(i);
			item.layout.setVisibility(View.VISIBLE);

			// 加载封面图片
			loadBookCover(item, job);

			// 加载进度条
			loadProgressView(item, job);

			// 加载其他控件
			loadOtherView(item, job);
		}

		return convertView;
	}

	private void loadBookCover(final ViewItem item, final DownBookJob job) {
		final Book book = job.getBook();
		final BookDownloadInfo downInfo = book.getDownloadInfo();

		if (downInfo.getImageUrl() != null && downInfo.getImageUrl().startsWith(Book.SDCARD_PATH_IMG)) {
			// SD卡书籍
			item.cover.setImageBitmap(ImageLoader.getNoImgPic());

		} else {
			// 其他书籍
			ImageLoader.getInstance().load(downInfo.getImageUrl(), item.cover,
					ImageLoader.TYPE_BOOK_HOME_SHELVES_COVER, ImageLoader.getNoImgPic());
		}

		item.coverClick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Math.abs(downInfo.getProgress() - 1.0) < 0.0001 && job.getState() == DownBookJob.STATE_FINISHED) {
					ReadActivity.setChapterReadEntrance("书架");
					ReadActivity.launch(mActivity, book, false, false);

				} else if (job.getState() == DownBookJob.STATE_RUNNING) {
					// 正在下载时点击，进入下载暂停状态
					job.pause();

				} else {
					// 下载失败或下载暂停状态，点击都是启动下载
					// 当书籍是全本购买，且没有登录时，弹出登陆对话框
					if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP
							&& LoginUtil.isValidAccessToken(mActivity) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
						LoginDialog.launch(mActivity, null);
					} else {
						job.start();
					}
				}
			}
		});

		item.coverClick.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 震动70毫秒
				// Vibrator vib = (Vibrator)
				// mActivity.getSystemService(Service.VIBRATOR_SERVICE);
				// vib.vibrate(70);

				mMenuDialog.show(job);
				return false;
			}
		});
	}

	private void loadProgressView(ViewItem item, DownBookJob job) {
		Book book = job.getBook();
		if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) > 0.0001
				|| job.getState() != DownBookJob.STATE_FINISHED) {
			// 给按钮设置监听器
			setDownBtnListener(item, job);

			item.downBtn.setVisibility(View.VISIBLE);

			if (job.getState() == DownBookJob.STATE_PAUSED || job.getState() == DownBookJob.STATE_FAILED) {
				// 下载暂停或者下载失败的情况
				item.downBtn.setImageResource(R.drawable.book_down);
				item.downPro.setVisibility(View.GONE);

			} else {
				// 正在下载或者解析的情况
				item.downBtn.setImageResource(R.drawable.book_pause);
				item.downPro.setVisibility(View.VISIBLE);
				item.downPro.setProgress((int) Math.round(100 * book.getDownloadInfo().getProgress()));
			}
			item.newIcon.setVisibility(View.GONE);
			item.newChapter.setVisibility(View.GONE);

		} else {
			// 下载成功
			item.downBtn.setVisibility(View.GONE);
			item.downPro.setVisibility(View.GONE);

			// 更新书籍文件长度
			updateBookLength(job.getBook());

			// 是否显示“new”标签
			if (book.getTag() == Book.IS_NEW) {
				item.newIcon.setVisibility(View.VISIBLE);
				item.newIcon.setBackgroundResource(R.drawable.new_icon);
			} else {
				item.newIcon.setVisibility(View.GONE);
			}

			// 是否显示更新章节标签
			if (book.getUpdateChaptersNum() > 0) {
				item.newChapter.setVisibility(View.VISIBLE);
				item.newChapter.setText("" + book.getUpdateChaptersNum());

				item.cloud.setVisibility(View.GONE);
			} else {
				item.newChapter.setVisibility(View.GONE);
				item.newChapter.setText("0");

				item.cloud.setVisibility(View.VISIBLE);
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
	 * @param item
	 * @param job
	 */
	private void setDownBtnListener(ViewItem item, final DownBookJob job) {
		item.downBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (job.getState() == DownBookJob.STATE_PAUSED || job.getState() == DownBookJob.STATE_FAILED) {
					// 当书籍不是免费书，且没有登录时，进入个人中心界面
					if (job.getBook().getBuyInfo().getPayType() != Book.BOOK_TYPE_FREE
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
	 * @param item
	 * @param job
	 */
	private void loadOtherView(ViewItem item, DownBookJob job) {
		Book book = job.getBook();
		item.title.setText(book.getTitle());
		item.author.setText(book.getAuthor());

		StringBuilder builder = new StringBuilder("已读");
		int start = builder.length();
		builder.append(book.getReadInfo().getLastReadPercent()).append("%");
		int end = builder.length();
		int color = ResourceUtil.getColor(R.color.praise_num_color);
		Spanned spanned = Util.highLight(builder, color, start, end);
		item.percent.setText(spanned);

		if (book.isOnlineBook()) {
			item.cloud.setVisibility(View.VISIBLE);
		} else {
			item.cloud.setVisibility(View.GONE);
		}
	}

	private View createView() {
		View itemView = LayoutInflater.from(mActivity).inflate(R.layout.vw_shelf_grid_item, null);
		ViewHolder holder = new ViewHolder();

		if (itemView == null) {
			return null;
		}

		for (int i = 1; i <= COLUMNS_NUM; i++) {
			ViewItem item = new ViewItem();

			switch (i) {
			case 1:
				item.layout = itemView.findViewById(R.id.shelf_book1);
				break;
			case 2:
				item.layout = itemView.findViewById(R.id.shelf_book2);
				break;
			case 3:
				item.layout = itemView.findViewById(R.id.shelf_book3);
				break;
			}

			item.cover = (ImageView) item.layout.findViewById(R.id.book_cover);
			item.coverClick = (ImageView) item.layout.findViewById(R.id.book_cover_click);
			item.cloud = (ImageView) item.layout.findViewById(R.id.book_cloud);
			item.newIcon = (ImageView) item.layout.findViewById(R.id.book_tip);
			item.newChapter = (TextView) item.layout.findViewById(R.id.book_new_chapter);
			item.downBtn = (ImageView) item.layout.findViewById(R.id.book_down_pause);
			item.downPro = (ProgressBar) item.layout.findViewById(R.id.book_download_progressBar);
			item.title = (EllipsizeTextView) item.layout.findViewById(R.id.book_title);
			item.author = (EllipsizeTextView) item.layout.findViewById(R.id.book_author);
			item.percent = (TextView) item.layout.findViewById(R.id.book_state);

			holder.items.add(item);
		}

		itemView.setTag(holder);

		return itemView;
	}

	protected class ViewHolder {
		private List<ViewItem> items = new ArrayList<ViewItem>(COLUMNS_NUM);
	}

	private class ViewItem {
		private View layout;
		private ImageView cover;
		private ImageView coverClick;
		private ImageView downBtn;
		private ImageView newIcon;
		private ImageView cloud;
		private TextView newChapter;
		private ProgressBar downPro;

		private EllipsizeTextView title;
		private EllipsizeTextView author;
		private TextView percent;
	}

	/**
	 * 菜单栏Dialog
	 */
	private class MenuDialog {
		private Dialog mDialog;

		private View mContentView;
		private ViewHolder mHolder;

		public MenuDialog() {
			createView();

			mDialog = new Dialog(mActivity, R.style.MenuDialog);

			WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
			lp.gravity = Gravity.BOTTOM;
			lp.width = Util.getDisplayMetrics(mActivity).widthPixels;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

			mDialog.onWindowAttributesChanged(lp);
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.setContentView(mContentView);
		}

		public void show(DownBookJob job) {
			if (null != mDialog) {
				if (mDialog.isShowing())
					dismiss();

				setViewValue(job);
				mDialog.show();
			}
		}

		public void dismiss() {
			if (null != mDialog) {
				mDialog.dismiss();
			}
		}

		private void setViewValue(DownBookJob job) {
			if (null != mContentView && null != mHolder && null != job) {
				Book book = job.getBook();

				// 封面
				final String imgUrl = book.getDownloadInfo().getImageUrl();
				if (!TextUtils.isEmpty(imgUrl) && imgUrl.startsWith(Book.SDCARD_PATH_IMG)) {
					// SD卡书籍
					mHolder.cover.setImageBitmap(ImageLoader.getNoImgPic());

				} else {
					// 其他书籍
					ImageLoader.getInstance().load(imgUrl, mHolder.cover, ImageLoader.TYPE_BOOK_HOME_SHELVES_COVER,
							ImageLoader.getNoImgPic());
				}

				// 书名
				mHolder.title.setText(book.getTitle());
				// 作者
				mHolder.author.setText(mActivity.getString(R.string.author) + book.getAuthor());

				// 阅读进度
				float percent = book.getReadInfo().getLastReadPercent();
				float fileSize = book.getDownloadInfo().getFileSize();
				if (percent <= 0 && book.getReadInfo().getLastPos() != 0) {
					if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE && fileSize > 0) {
						percent = (float) (book.getReadInfo().getLastPos() * 1.0 / fileSize) * 100;
					}
				}

				percent = Util.formatFloat(percent, 2);
				mHolder.readPro.setProgress((int) percent);
				// 阅读百分比
				String percentStr = String.format(mActivity.getString(R.string.bookhome_read_percent), book
						.getReadInfo().getLastReadPercent());
				mHolder.percent.setText(percentStr);

				ClickListener listener = new ClickListener(job);
				mHolder.menuDelete.setOnClickListener(listener);
				mHolder.menuShare.setOnClickListener(listener);
				mHolder.menuUpdate.setOnClickListener(listener);
				mHolder.menuDown.setOnClickListener(listener);

				if (book.isOurServerBook() && book.isOnlineBook()) {
					mHolder.menuDown.setVisibility(View.VISIBLE);
					mHolder.menuUpdate.setVisibility(View.GONE);
				} else {
					mHolder.menuDown.setVisibility(View.GONE);
					mHolder.menuUpdate.setVisibility(View.VISIBLE);

					// 不是自己服务器上以及微盘上下载的书籍分享更新按钮不可点
					if (!book.isOurServerBook()) {
						if (!book.isVDiskBook()) {
							mHolder.menuShare.setEnabled(false);
						}
						mHolder.menuUpdate.setEnabled(false);
					} else {
						mHolder.menuShare.setEnabled(true);
						mHolder.menuUpdate.setEnabled(true);
					}
				}
			}
		}

		private void createView() {
			mContentView = LayoutInflater.from(mActivity).inflate(R.layout.vw_shelf_grid_menu, null);
			mHolder = new ViewHolder();

			mHolder.cover = (ImageView) mContentView.findViewById(R.id.cover);

			mHolder.title = (EllipsizeTextView) mContentView.findViewById(R.id.title);
			mHolder.author = (TextView) mContentView.findViewById(R.id.author);

			mHolder.readPro = (ProgressBar) mContentView.findViewById(R.id.progress_bar);
			mHolder.percent = (TextView) mContentView.findViewById(R.id.progress_percent);

			mHolder.menuDelete = (TextView) mContentView.findViewById(R.id.menu_delete);
			mHolder.menuShare = (TextView) mContentView.findViewById(R.id.menu_share);
			mHolder.menuUpdate = (TextView) mContentView.findViewById(R.id.menu_update);
			mHolder.menuDown = (TextView) mContentView.findViewById(R.id.menu_down);

			Bitmap dotHBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.list_divide_dot);
			BitmapDrawable drawable = new BitmapDrawable(mActivity.getResources(), dotHBitmap);
			drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			drawable.setDither(true);
			((ImageView) mContentView.findViewById(R.id.divider)).setImageDrawable(drawable);
		}

		protected class ViewHolder {
			private ImageView cover;
			private EllipsizeTextView title;
			private TextView author;
			private ProgressBar readPro;
			private TextView percent;

			private TextView menuDelete;
			private TextView menuShare;
			private TextView menuUpdate;
			private TextView menuDown;
		}

		private class ClickListener implements View.OnClickListener {
			private DownBookJob job;

			public ClickListener(DownBookJob job) {
				this.job = job;
			}

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.menu_delete:
					mMenuDialog.dismiss();

					deleteBook(job);
					break;

				case R.id.menu_share:
					mMenuDialog.dismiss();

					share(job.getBook());
					break;

				case R.id.menu_update:
					mMenuDialog.dismiss();

					List<Book> needCheckBooks = new ArrayList<Book>();
					needCheckBooks.add(job.getBook());
					Toast.makeText(mActivity, R.string.xlistview_header_hint_loading, Toast.LENGTH_SHORT).show();
					UpdateChapterManager.getInstance().checkNewChapter(needCheckBooks,
							UpdateChapterManager.REQ_SINGLE_BOOK);
					break;

				case R.id.menu_down:
					mMenuDialog.dismiss();
					final Book book = job.getBook();

					int chaptersSize = book.getNum();
					if (chaptersSize <= 0) {
						// 有的书籍0章节
						Toast.makeText(mActivity, R.string.bookdetail_failed_text, Toast.LENGTH_SHORT).show();
						return;
					} else {
						DownBookManager.getInstance().downBook(book);
					}
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
					DownBookManager.getInstance().removeJob(job);

					// 如果没有数据则将Adapter设置为null
					if (DownBookManager.getInstance().getAllJobs().size() <= 0) {
						mListView.setAdapter(null);
					}

					// 重新设置数据
					setData(DownBookManager.getInstance().getAllJobs());

					// SD卡上的书籍不能被删除
					if (!book.getDownloadInfo().getImageUrl().startsWith(Book.SDCARD_PATH_IMG)) {
						StorageUtil.deleteFolder(book.getDownloadInfo().getFilePath());
						StorageUtil.deleteFolder(book.getDownloadInfo().getImageFolder());
					}

					notifyDataSetChanged();
					CloudSyncUtil.getInstance().delCloud(mActivity, book);
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
	}

	private void showProgressDialog(int resId) {
		if (null == mProgressDialog) {
			mProgressDialog = new CustomProDialog(mActivity);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}

		mProgressDialog.show(resId);
	}

	private void dismissProgressDialog() {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
}
