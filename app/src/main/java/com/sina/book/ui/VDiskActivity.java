package com.sina.book.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.VDiskSyncManager;
import com.sina.book.control.download.VDiskSyncManager.IGetEntryLisener;
import com.sina.book.control.download.VDiskSyncManager.IGetFileInfoLisener;
import com.sina.book.control.download.VDiskSyncManager.IUpdateVDiskFinishedListener;
import com.sina.book.control.download.VDiskSyncManager.IUpdateVDiskListener;
import com.sina.book.data.Book;
import com.sina.book.data.WeiboContent;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.view.ShareDialog;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.ui.widget.XListViewHeader;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.vdisk.net.RESTUtility;
import com.vdisk.net.VDiskAPI.Entry;
import com.vdisk.net.VDiskAPI.VDiskFileInfo;
import com.vdisk.net.exception.VDiskException;

public class VDiskActivity extends CustomTitleActivity implements OnClickListener, OnItemClickListener,
		IXListViewListener {
	/**
	 * 微盘列表.
	 */
	private XListView mVDiskListView;

	/**
	 * 购买书籍Adapter
	 */
	private VDiskAdapter mVDiskAdapter;

	/**
	 * 登陆微博提示
	 */
	private TextView mLoginView;

	/**
	 * 微盘引导
	 */
	private View mVDiskGuide;

	/**
	 * 进度条.
	 */
	private View mProgressView;

	/**
	 * 网络错误.
	 */
	private View mErrorView;

	/**
	 * 网络错误，重试按钮
	 */
	private Button mRetryBtn;

	public static final String ROOT_PATH = "/";

	private String mCurrentPath = ROOT_PATH;
	private String mParentPath = null;

	/**
	 * 当前显示在列表中的Entry.
	 */
	private List<Entry> mEntryList = new ArrayList<Entry>();

	private VDiskSyncManager mVDiskManager;

	/**
	 * 列表项是否展开的稀松数组.
	 */
	private SparseBooleanArray mBooleanArray = new SparseBooleanArray();

	private SimpleDateFormat mVDiskDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

	private CustomProDialog mProgressDialog;

	private String mAccessToken;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_vdisk);
		initView();
		initTitle();
		showVDiskView();
	}

	private void initView() {

		mVDiskListView = (XListView) findViewById(R.id.book_home_listview);
		mLoginView = (TextView) findViewById(R.id.book_home_login_weibo_view);
		mVDiskGuide = findViewById(R.id.vdisk_guide);
		mProgressView = findViewById(R.id.book_home_progress);
		mErrorView = findViewById(R.id.error_layout);
		mRetryBtn = (Button) mErrorView.findViewById(R.id.retry_btn);

		mLoginView.setOnClickListener(this);
		mRetryBtn.setOnClickListener(this);

		mVDiskListView.setPullRefreshEnable(true, XListViewHeader.TYPE_PULL_REFRESH);
		mVDiskListView.setPullLoadEnable(false);
		mVDiskListView.setOnItemClickListener(this);
		mVDiskListView.setXListViewListener(this);
		mVDiskListView.setRefreshTime(getUpdateTime());

		// 默认设置收藏Adapter
		mVDiskAdapter = new VDiskAdapter(mContext, mEntryList);
		mVDiskListView.setAdapter(mVDiskAdapter);

		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
				&& LoginUtil.getLoginInfo().getAccessToken() != null) {
			mAccessToken = LoginUtil.getLoginInfo().getAccessToken();
		}
	}

	/**
	 * 初始化标题.
	 */
	private void initTitle() {
		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middle.setText(R.string.book_home_tab_vdisk);
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleMiddle(middle);
		setTitleLeft(left);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!HttpUtil.isConnected(mContext)) {
			dismissProgressView();
		}

		isNeedRefreshByAccessToken();

	}

	@Override
	public void onPause() {

		super.onPause();
		VDiskSyncManager.cancelGetFileListTask();
	}

	public void onClickLeft() {
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (null != mVDiskListView && keyCode == KeyEvent.KEYCODE_BACK) {
			if (backupParentDir()) {
				return true;
			} else {
				finish();
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private String getUpdateTime() {
		String timeStr = getString(R.string.do_not_update);
		long time = StorageUtil.getLong(StorageUtil.KEY_UPDATE_VDISK);
		if (-1 != time) {
			timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(time));
		}
		return timeStr;
	}

	private void showVDiskView() {
		if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			mLoginView.setText(R.string.login_tip_vdisk);
			mLoginView.setVisibility(View.VISIBLE);

			mVDiskListView.setVisibility(View.GONE);
			return;
		} else {
			mVDiskListView.setVisibility(View.VISIBLE);
			mLoginView.setVisibility(View.GONE);

			mVDiskManager = VDiskSyncManager.getInstance(mContext);
			initFileList(mCurrentPath);
		}
	}

	private void initFileList(String path) {
		mCurrentPath = path;

		showProgressView();

		mVDiskManager.getFileList(mCurrentPath, new IGetEntryLisener() {

			@Override
			public void fireEntryFinished(Entry entry) {

				if (mContext == null) {
					return;
				}
				dismissProgressView();

				mEntryList.clear();

				if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
					dismissVdiskGuideView();
					showErrorView();
				} else {
					if (entry != null) {
						dismissErrorView();
						initAddBackUp(entry);
						mEntryList.addAll(listEntrys(entry));
						mVDiskAdapter.notifyDataSetChanged();
						if (mVDiskAdapter.isEmpty()) {
							showVdiskGuideView();
						}
					} else {
						showErrorView();
						dismissVdiskGuideView();
					}
				}
			}
		});
	}

	private void updateFileList(String path) {
		mCurrentPath = path;

		mVDiskManager.getFileList(mCurrentPath, new IGetEntryLisener() {

			@Override
			public void fireEntryFinished(Entry entry) {

				if (mContext == null) {
					return;
				}
				if (!HttpUtil.isConnected(SinaBookApplication.gContext)) {
					Toast.makeText(mContext, R.string.network_unconnected, Toast.LENGTH_SHORT).show();
					mVDiskListView.stopRefresh();
				} else {
					if (entry != null) {
						mEntryList.clear();

						initAddBackUp(entry);
						mEntryList.addAll(listEntrys(entry));
						mVDiskAdapter.notifyDataSetChanged();
						if (mVDiskAdapter.isEmpty()) {
							showVdiskGuideView();
						}
					} else {
						Toast.makeText(mContext, R.string.network_unconnected, Toast.LENGTH_SHORT).show();
						mVDiskListView.stopRefresh();
					}
					VDiskSyncManager.getInstance(mContext).updateVDiskFinished(new IUpdateVDiskFinishedListener() {

						@Override
						public void updateVDiskFinished() {

							StorageUtil.saveLong(StorageUtil.KEY_UPDATE_VDISK, System.currentTimeMillis());
							mVDiskListView.setRefreshTime(getUpdateTime());
							mVDiskListView.stopRefresh();
						}
					});
				}
			}
		});
	}

	private void initAddBackUp(Entry entry) {
		if (!entry.path.equals(ROOT_PATH)) {
			Entry parentEntry = new Entry();
			parentEntry.path = entry.parentPath();
			parentEntry.isDir = true;
			// 这里设置一个不用的参数来区别是否是返回上一级菜单
			parentEntry.rev = "BackToParent";
			mEntryList.add(parentEntry);

			mParentPath = entry.parentPath();
		} else {
			mParentPath = null;
		}
	}

	private ArrayList<Entry> listEntrys(Entry entry) {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		if (entry.contents != null) {
			for (Entry e : entry.contents) {
				// 过滤掉不相干的文件
				if ((e.isDir || e.fileName().toLowerCase(Locale.CHINA).endsWith("txt") || e.fileName()
						.toLowerCase(Locale.CHINA).endsWith("epub"))) {
					entries.add(e);
				}
			}
		}
		return entries;
	}

	/**
	 * 返回上级目录
	 * 
	 * @return 返回成功或失败
	 */
	public boolean backupParentDir() {
		boolean backup = false;

		if (null != mParentPath) {
			initFileList(mParentPath);
			backup = true;
		}

		return backup;
	}

	/**
	 * 是否已经下载该书
	 * 
	 * @param entry
	 * @return book or null
	 */
	private Book getBook(Entry entry) {
		if (null == entry)
			return null;

		Book book = new Book();
		book.getDownloadInfo().setVDiskFilePath(entry.path);

		if (LoginUtil.isValidAccessToken(mContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			book.getBuyInfo().setUid(LoginUtil.getLoginInfo().getUID());
		}

		return DownBookManager.getInstance().getBook(book);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position -= mVDiskListView.getHeaderViewsCount();

		if (position < 0 || position >= mEntryList.size()) {
			return;
		} else {
			mVDiskListView.stopRefresh();
			VDiskSyncManager.cancelGetFileListTask();
		}

		Entry entry = mEntryList.get(position);
		if (entry.isDir) {
			initFileList(entry.path);
		} else {
			Book book = getBook(entry);
			if (null != book) {
				// 打开该书
				ReadActivity.setChapterReadEntrance("微盘");
				ReadActivity.launch(mContext, book, false, false);
			} else {
				// 展开或隐藏菜单栏
				if (mBooleanArray.get(position, false)) {
					mBooleanArray.clear();
					mVDiskAdapter.notifyDataSetChanged();
				} else {
					mBooleanArray.clear();
					mBooleanArray.put(position, true);
					mVDiskAdapter.notifyDataSetChanged();

					scrollListViewUp(view, position);
				}
			}
		}
	}

	/**
	 * 向上滚动ListView以显示菜单栏
	 * 
	 * @param view
	 * @param position
	 */
	private void scrollListViewUp(View view, int position) {
		// 列表菜单栏的高度
		int menuHeight = (int) ResourceUtil.getDimens(R.dimen.bookhome_item_menu_height);

		if (mVDiskListView.getHeight() - view.getBottom() < menuHeight) {
			int itemMinHeight = view.getHeight();
			int height = mVDiskListView.getHeight() - itemMinHeight - menuHeight;

			int curPosition = position + mVDiskListView.getHeaderViewsCount() - height / itemMinHeight;

			if (curPosition < mVDiskAdapter.getCount() && curPosition >= 0) {
				mVDiskListView.setSelectionFromTop(curPosition, height % itemMinHeight);
			}
		}
	}

	public void showProgressView() {
		mProgressView.setVisibility(View.VISIBLE);
		mErrorView.setVisibility(View.GONE);
	}

	public void dismissProgressView() {
		mProgressView.setVisibility(View.GONE);
	}

	public void showErrorView() {
		mErrorView.setVisibility(View.VISIBLE);
		mProgressView.setVisibility(View.GONE);
	}

	public void dismissErrorView() {
		mErrorView.setVisibility(View.GONE);
	}

	public void showVdiskGuideView() {
		mVDiskGuide.setVisibility(View.VISIBLE);
	}

	public void dismissVdiskGuideView() {
		mVDiskGuide.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.book_home_login_weibo_view:
			// 点击banner进入个人中心界面
			if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {

				LoginDialog.launch(this, new LoginStatusListener() {

					@Override
					public void onSuccess() {
						if (!HttpUtil.isConnected(mContext)) {
							dismissProgressView();
						}

						isNeedRefreshByAccessToken();
					}

					@Override
					public void onFail() {

					}
				});
			}
			break;

		case R.id.retry_btn:
			showVDiskView();
			break;

		default:
			break;
		}

	}

	/**
	 * 微盘文件适配器.
	 */
	class VDiskAdapter extends BaseAdapter {
		private Context mContext;

		private List<Entry> mEntryList;

		private BitmapDrawable mDividerDrawable;

		public VDiskAdapter(Context context, List<Entry> files) {
			mContext = context;
			mEntryList = files;

			Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.list_divide_dot);
			mDividerDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
			mDividerDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			mDividerDrawable.setDither(true);
		}

		@Override
		public int getCount() {
			return mEntryList.size();
		}

		@Override
		public Object getItem(int position) {
			return mEntryList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup viewgroup) {
			Entry entry = mEntryList.get(position);

			if (convertView == null || null == convertView.getTag(R.layout.vw_vdisk_dir_list_item)
					|| null == convertView.getTag(R.layout.vw_vdisk_file_list_item)) {
				if (entry.isDir) {
					convertView = createDirView();
				} else {
					convertView = createFileView();
				}
			}

			// TODO 这里应该有更好的办法，暂时先这样处理
			if (entry.rev.equals("BackToParent")) {
				DirViewHolder holder = (DirViewHolder) convertView.getTag(R.layout.vw_vdisk_dir_list_item);

				holder.mDirFolder.setImageResource(R.drawable.up_arrow_button);
				holder.mDirName.setText(R.string.back_to_parent_dir);
				holder.mDirFolderArrow.setVisibility(View.GONE);
			} else {
				if (entry.isDir) {
					DirViewHolder holder = (DirViewHolder) convertView.getTag(R.layout.vw_vdisk_dir_list_item);

					holder.mDirFolder.setImageResource(R.drawable.folder);
					holder.mDirName.setText(entry.fileName());
					holder.mDirFolderArrow.setVisibility(View.VISIBLE);

				} else {
					loadFileData(position, convertView, entry);
				}
			}
			return convertView;
		}

		private void loadFileData(int position, View convertView, Entry entry) {
			FileViewHolder holder = (FileViewHolder) convertView.getTag(R.layout.vw_vdisk_file_list_item);

			String fileName = entry.fileName();
			holder.mFileName.setText(fileName);

			String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase(
					Locale.CHINA);// 取出文件后缀名并转成小写
			if (fileEnds.equals("txt")) {
				holder.mFileInfo.setVisibility(View.VISIBLE);
				holder.mFileInfo.setText(mVDiskDateFormat.format(RESTUtility.parseDate(entry.modified)) + "  " + ""
						+ entry.size);
				holder.mFileIcon.setImageResource(R.drawable.txt);

			} else {
				holder.mFileInfo.setVisibility(View.VISIBLE);
				holder.mFileInfo.setText(mVDiskDateFormat.format(RESTUtility.parseDate(entry.modified)) + "  " + ""
						+ entry.size);
				holder.mFileIcon.setImageResource(R.drawable.epub);
			}

			ClickListener listener = new ClickListener(holder, position, entry, convertView);

			holder.mMenuBtn.setVisibility(View.VISIBLE);
			holder.mMenuBtn.setClickable(true);
			holder.mMenuBtn.setOnClickListener(listener);

			holder.mMenuDownBtn.setOnClickListener(listener);
			holder.mMenuShareBtn.setOnClickListener(listener);

			if (mBooleanArray.get(position, false)) {
				holder.mMenuBtnLayout.setVisibility(View.VISIBLE);
				holder.mMenuBtn.setImageResource(R.drawable.menu_btn_up);
			} else {
				holder.mMenuBtnLayout.setVisibility(View.GONE);
				holder.mMenuBtn.setImageResource(R.drawable.menu_btn_down);
			}

			if ((null != getBook(entry))) {
				holder.mMenuDownBtn.setEnabled(false);
				holder.mMenuDownBtn.setText(R.string.has_down);
			} else {
				holder.mMenuDownBtn.setEnabled(true);
				holder.mMenuDownBtn.setText(R.string.bookhome_down);
			}

			holder.mDivider.setImageDrawable(mDividerDrawable);
		}

		protected View createFileView() {
			View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_vdisk_file_list_item, null);

			FileViewHolder holder = new FileViewHolder();

			holder.mFileLayout = itemView.findViewById(R.id.item_content_layout);

			holder.mFileIcon = (ImageView) holder.mFileLayout.findViewById(R.id.file_icon);
			holder.mFileName = (EllipsizeTextView) holder.mFileLayout.findViewById(R.id.vdisk_file_name);
			holder.mFileInfo = (TextView) holder.mFileLayout.findViewById(R.id.vdisk_file_info);

			holder.mMenuBtn = (ImageView) itemView.findViewById(R.id.item_menu_btn);
			holder.mMenuBtnLayout = (RelativeLayout) itemView.findViewById(R.id.item_menu_layout);
			holder.mMenuDownBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_down);
			holder.mMenuShareBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_share);
			holder.mDivider = (ImageView) itemView.findViewById(R.id.vdisk_file_divider);

			itemView.setTag(R.layout.vw_vdisk_file_list_item, holder);
			return itemView;
		}

		protected View createDirView() {
			View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_vdisk_dir_list_item, null);

			DirViewHolder holder = new DirViewHolder();

			holder.mDirFolder = (ImageView) itemView.findViewById(R.id.vdisk_dir_folder);
			holder.mDirName = (EllipsizeTextView) itemView.findViewById(R.id.vdisk_dir_name);
			holder.mDirFolderArrow = (ImageView) itemView.findViewById(R.id.vdisk_dir_folder_arrow);

			itemView.setTag(R.layout.vw_vdisk_dir_list_item, holder);
			return itemView;
		}

		protected class FileViewHolder {
			public View mFileLayout;
			public ImageView mFileIcon;
			public EllipsizeTextView mFileName;
			public TextView mFileInfo;

			public ImageView mMenuBtn;
			public RelativeLayout mMenuBtnLayout;
			public TextView mMenuDownBtn;
			public TextView mMenuShareBtn;

			public ImageView mDivider;
		}

		protected class DirViewHolder {
			public ImageView mDirFolder;
			public EllipsizeTextView mDirName;
			public ImageView mDirFolderArrow;
		}

		private class ClickListener implements OnClickListener, IGetFileInfoLisener {
			private FileViewHolder mHolder;
			private int mPosition;
			private Entry mEntry;
			private View mView;

			public ClickListener(FileViewHolder holder, int position, Entry entry, View view) {
				mHolder = holder;
				mPosition = position;
				mEntry = entry;
				mView = view;
			}

			@Override
			public void onClick(View v) {
				switch (v.getId()) {

				case R.id.item_menu_btn:
					mBooleanArray.clear();

					if (!mHolder.mMenuBtnLayout.isShown()) {
						mBooleanArray.put(mPosition, true);
						notifyDataSetChanged();

						// 列表菜单栏的高度
						int menuHeight = (int) ResourceUtil.getDimens(R.dimen.bookhome_item_menu_height);
						if (mVDiskListView.getHeight() - mView.getBottom() < menuHeight) {
							int itemMinHeight = v.getHeight();
							int height = mVDiskListView.getHeight() - itemMinHeight - menuHeight;

							int curPosition = mPosition + mVDiskListView.getHeaderViewsCount() - height / itemMinHeight;

							if (curPosition < getCount() && curPosition >= 0) {
								mVDiskListView.setSelectionFromTop(curPosition,
										height % itemMinHeight - PixelUtil.dp2px(3));
							}
						}
					} else {
						notifyDataSetChanged();
					}
					break;

				case R.id.item_menu_btn_down:
					v.setEnabled(false);
					mBooleanArray.clear();
					notifyDataSetChanged();

					showProgressDialog(R.string.downloading_text);
					mVDiskManager.getFileInfo(mEntry.path, this);
					break;

				case R.id.item_menu_btn_share:
					mBooleanArray.clear();
					notifyDataSetChanged();

					share(mEntry);
					break;

				default:
					break;
				}
			}

			@Override
			public void fireFileInfoFinished(VDiskFileInfo info) {
				if (mContext == null) {
					return;
				}

				if (info != null) {
					Book book = new Book();
					book.getDownloadInfo().setImageUrl(Book.SDCARD_PATH_IMG + info.getMetadata().fileName());
					book.getDownloadInfo().setVDiskFilePath(info.getMetadata().path);
					book.getDownloadInfo().setDownloadTime(new Date().getTime());
					int dotIndex = info.getMetadata().fileName().lastIndexOf(".");
					StringBuilder sb = new StringBuilder(info.getMetadata().fileName().substring(0, dotIndex));
					book.setTitle(sb.toString());
					book.getDownloadInfo().setVDiskDownUrl(info.getDownloadURL());
					book.getBuyInfo().setPayType(Book.BOOK_TYPE_FREE);
					book.getDownloadInfo().setLocationType(Book.BOOK_SDCARD);
					book.setAuthor(getString(R.string.unkonw_author));
					if (LoginUtil.isValidAccessToken(mContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
						book.getBuyInfo().setUid(LoginUtil.getLoginInfo().getUID());
					}

					int flag = DownBookManager.getInstance().downBook(book);
					if (flag == DownBookManager.FLAG_START_CACHE) {
						notifyDataSetChanged();
						dismissProgressDialog();

						Toast.makeText(mContext,
								String.format(mContext.getString(R.string.add_shelves_down), book.getTitle()),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					dismissProgressDialog();
					Toast.makeText(SinaBookApplication.gContext, R.string.downloading_failed, Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}

	private void share(final Entry entry) {
		new GenericTask() {
			WeiboContent mContent;

			@Override
			protected void onPreExecute() {
				showProgressDialog(R.string.create_share_content);
			}

			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				mContent = new WeiboContent(null, WeiboContent.TYPE_BOOK_DETAIL);

				String url = "";
				try {
					url = VDiskSyncManager.getInstance(SinaBookApplication.gContext).getShareUrl(entry.path);
				} catch (VDiskException e) {
					// 无需处理
				}

				if (TextUtils.isEmpty(url)) {
					url = "";
				}

				// 当Book设置为null时，setMsg将不会格式化，所以需要在设置之前先格式化
				mContent.setMsg(String.format(ResourceUtil.getString(R.string.share_book_vdisk), entry.fileName(), url));

				return null;
			}

			@Override
			protected void onPostExecute(TaskResult result) {
				dismissProgressDialog();

				if (null != mContent) {
					ShareDialog.show(VDiskActivity.this, mContent);
				} else {
					Toast.makeText(mContext, R.string.create_share_content_failed, Toast.LENGTH_SHORT).show();
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
			mProgressDialog = new CustomProDialog(mContext);
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

	@Override
	public void onRefresh() {
		VDiskSyncManager.getInstance(mContext).updateVDisk(new IUpdateVDiskListener() {

			@Override
			public void updateVDiskData() {

				updateFileList(mCurrentPath);
			}

		});
	}

	@Override
	public void onLoadMore() {

	}

	private void isNeedRefreshByAccessToken() {
		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
				&& LoginUtil.getLoginInfo().getAccessToken() != null) {
			if (mAccessToken != null) {
				if (!mAccessToken.equals(LoginUtil.getLoginInfo().getAccessToken())) {
					showVDiskView();
				}
			} else {
				showVDiskView();
			}
			mAccessToken = LoginUtil.getLoginInfo().getAccessToken();
		} else {
			if (mAccessToken != null) {
				showVDiskView();
			}
			mAccessToken = null;
		}
	}

	/**
	 * 菜单栏的点击事件
	 * 
	 * @param view
	 */
	public void menuLayoutOnClick(View view) {
		// 暂不处理，此方法仅用于拦截其点击事件
	}
}
