package com.sina.book.ui.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.FBReader;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.HtmlDownManager;
import com.sina.book.control.download.UpdateChapterManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookBuyInfo;
import com.sina.book.data.BookDownloadInfo;
import com.sina.book.data.Chapter;
import com.sina.book.data.GiftInfo;
import com.sina.book.data.LoginInfo;
import com.sina.book.data.UserInfoUb.Activitys;
import com.sina.book.data.WeiboContent;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.MainTabActivity;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.RecommendWebUrlActivity;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.view.ShareDialog;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.DialogUtils;
import com.sina.book.util.EpubHelper;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/*
 * 我的书架adapter
 */
public class MainShelvesGridAdapter extends BaseAdapter
{

	private static final String				TAG			= "ShelvesGridAdapter";
	private final int						COLUMNS_NUM	= 3;

	private Activity						mActivity;
	private MenuDialog						mMenuDialog;
	private CustomProDialog					mProgressDialog;

	private ArrayList<ArrayList<Object>>	mDatas;

	// 书架数据o
	private List<DownBookJob>				mShelfdata;

	// 是否添加"+"号项
	private boolean							mAddLastPos;

	// 是否显示赠书卡
	private boolean							isShowCard;
	private String							cardUrl;
	private String							cardName;
	private String							cardTip;

	// private boolean isShowCard = true;
	// private String cardUrl =
	// "http://book1.sina.cn/prog/wapsite/newbook/card/mycard.php";
	// private String cardName = "我的赠书卡";
	// private String cardTip = "赠书卡";

	// private GiftInfo info;

	public MainShelvesGridAdapter(Activity context)
	{
		mActivity = context;

		mMenuDialog = new MenuDialog();
		mDatas = new ArrayList<ArrayList<Object>>();
		mAddLastPos = true;
	}

	// public void setCardInfo(String name, String tip, String url, boolean
	// isShowCard) {
	// this.cardName = name;
	// if (tip == null || tip.length() == 0) {
	// tip = "赠书卡";
	// }
	// this.cardTip = tip;
	// this.cardUrl = url;
	// this.isShowCard = isShowCard;
	// setData(mShelfdata);
	// }

	public void setShowCard(boolean isShowCard)
	{
		this.isShowCard = isShowCard;
		if (isShowCard) {
			fetchCardInfo();
		}
		setData(mShelfdata);
	}

	public boolean isShowCard()
	{
		return isShowCard;
	}

	private void fetchCardInfo()
	{
		LoginInfo loginInfo = LoginUtil.getLoginInfo();
		if (loginInfo != null) {
			ArrayList<Activitys> list = loginInfo.getActivitys();
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); ++i) {
					Activitys activitys = list.get(i);
					int type = activitys.getActivityType();
					if (type == Activitys.TYPE_CARD) {
						this.cardName = activitys.getActivityName();
						this.cardTip = activitys.getActivityTip();
						this.cardUrl = activitys.getActivityUrl();
						break;
					}
				}
			}
		}
		if (cardTip == null || cardTip.length() == 0) {
			cardTip = "赠书卡";
		}
	}

	/**
	 * 每行最多3项数据
	 * 
	 * @param data
	 */
	public synchronized void setData(List<DownBookJob> data)
	{
		mShelfdata = data;
		// if (null == data) {
		// return;
		// }

		mDatas.clear();

		ArrayList<Object> itemList = new ArrayList<Object>();

		if (isShowCard) {
			// 显示赠书卡
			itemList.add(1);
		}

		if (data != null && data.size() > 0) {
			for (int i = 0; i < data.size(); ++i) {
				DownBookJob job = data.get(i);
				itemList.add(job);

				if (itemList.size() >= COLUMNS_NUM) {
					mDatas.add(itemList);
					itemList = new ArrayList<Object>();
				}
				// if (itemList.size() < COLUMNS_NUM) {
				// itemList.add(job);
				// } else if (itemList.size() >= COLUMNS_NUM) {
				// mDatas.add(itemList);
				// itemList = new ArrayList<Object>();
				// itemList.add(job);
				// }
			}
		}
		// if(itemList.size() >= COLUMNS_NUM){
		// itemList = new ArrayList<Object>();
		// }
		if (mAddLastPos) {
			itemList.add(2);
			mDatas.add(itemList);
		}
		// if (info != null) {
		//
		// if (data.size() > 2) {
		// itemList = (data.subList(0, 2));
		// mDatas.add(itemList);
		//
		// int size = (int) Math.ceil((data.size() - 2) * 1.00 / COLUMNS_NUM);
		// for (int i = 0; i < size; i++) {
		// int index = i * COLUMNS_NUM + COLUMNS_NUM + 2;
		// if (index >= data.size()) {
		// itemList = data.subList(i * COLUMNS_NUM + 2, data.size());
		// } else {
		// itemList = data.subList(i * COLUMNS_NUM + 2, index);
		// }
		//
		// mDatas.add(itemList);
		// }
		//
		// } else {
		// itemList = (data.subList(0, data.size()));
		// mDatas.add(itemList);
		//
		// }
		//
		// } else {
		// int size = (int) Math.ceil(data.size() * 1.00 / COLUMNS_NUM);
		// for (int i = 0; i < size; i++) {
		// int index = i * COLUMNS_NUM + COLUMNS_NUM;
		// if (index >= data.size()) {
		// itemList = data.subList(i * COLUMNS_NUM, data.size());
		// } else {
		// itemList = data.subList(i * COLUMNS_NUM, index);
		// }
		//
		// mDatas.add(itemList);
		// }

		// }
	}

	public void setGiftInfo(GiftInfo info)
	{
		// this.info = info;
		// if (info != null && !info.isShow()) {
		// info = null;
		// }
	}

	public int getCount()
	{
		if (mDatas == null || mDatas.size() == 0) {
			return 0;
		} else {
			return mDatas.size();
		}

		// if (mDatas == null || mDatas.size() == 0) {
		// if (mAddLastPos || isShowCard) {
		// return 1;
		// } else {
		// return 0;
		// }
		// } else {
		// if (mAddLastPos) {
		// // if (info != null && mDatas.size() == 1) {
		// // if (mDatas.get(mDatas.size() - 1).size() + 1 == COLUMNS_NUM)
		// // {
		// // return mDatas.size() + 1;
		// // } else {
		// // return mDatas.size();
		// // }
		// // }
		//
		// if (mDatas.get(mDatas.size() - 1).size() == COLUMNS_NUM) {
		// return mDatas.size() + 1;
		// } else {
		// return mDatas.size();
		// }
		// } else {
		// return mDatas.size();
		// }
		// }
	}

	public Object getItem(int position)
	{
		return null;
		// if (position < mDatas.size() && position >= 0) {
		// return mDatas.get(position);
		// } else {
		// return null;
		// }
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		// 先都隐藏
		holder.items.get(0).layout.setVisibility(View.GONE);
		holder.items.get(1).layout.setVisibility(View.GONE);
		holder.items.get(2).layout.setVisibility(View.GONE);

		// 增加头部高度
		if (position == 0) {
			MarginLayoutParams layoutparams = (MarginLayoutParams) holder.totalView
					.getLayoutParams();
			layoutparams.topMargin = PixelUtil.dp2px(10);
		} else {
			MarginLayoutParams layoutparams = (MarginLayoutParams) holder.totalView
					.getLayoutParams();
			layoutparams.topMargin = 0;
		}

		ArrayList<Object> list = mDatas.get(position);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); ++i) {
				Object obj = list.get(i);
				if (obj instanceof Integer) {
					int in = (Integer) obj;
					ViewItem item = holder.items.get(i);
					if (in == 1) {
						// 赠书卡
						loadCardItem(item);
					} else if (in == 2) {
						// "+"号
						loadAddItem(item);
					}
				} else if (obj instanceof DownBookJob) {
					DownBookJob job = (DownBookJob) obj;
					if (job != null) {
						ViewItem item = holder.items.get(i);
						loadItem(item, job);
					}
				}
			}
		}

		return convertView;
	}

	private void loadItem(final ViewItem item, final DownBookJob job)
	{
		item.layout.setVisibility(View.VISIBLE);

		// 加载封面图片
		loadBookCover(item, job);

		// 加载进度条
		loadProgressView(item, job);

		// 加载其他控件
		loadOtherView(item, job);
	}

	private void loadAddItem(final ViewItem item)
	{
		item.layout.setVisibility(View.VISIBLE);
		item.cover.setBackgroundDrawable(null);
		item.cover.setImageResource(R.drawable.new_main_add);
		item.cover.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				MainTabActivity.launch(mActivity);
				UserActionManager.getInstance().recordEvent(
						Constants.CLICK_MAIN_ADD);

				// TODO CJL 模拟下载epub资源
				// boolean isEpubBook = true;
				// if (isEpubBook) {
				// String downURL = Book.EPUB_PATH_PROTOCOL
				// +
				// "http://files.2epub.net/sites/default/files/eb_converted/2012_07/tou_ying_zi_de_ren_.epub";
				// String imgURL =
				// "http://www.2epub.net/sites/default/files/imagecache/cover_156x200/covers/s10339418.jpg";
				//
				// Book book = new Book();
				// book.getDownloadInfo().setImageUrl(imgURL);
				// book.getDownloadInfo().setVDiskFilePath(downURL);
				// book.getDownloadInfo()
				// .setDownloadTime(new Date().getTime());
				// book.setTitle("tou_ying_zi_de_ren_");
				// book.getDownloadInfo().setVDiskDownUrl(downURL);
				// book.getBuyInfo().setPayType(Book.BOOK_TYPE_FREE);
				// book.getDownloadInfo().setLocationType(Book.BOOK_SDCARD);
				// book.setAuthor(mActivity.getString(R.string.unkonw_author));
				// if (LoginUtil.isValidAccessToken(mActivity) ==
				// LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
				// book.getBuyInfo().setUid(
				// LoginUtil.getLoginInfo().getUID());
				// }
				// if (!DownBookManager.getInstance().hasBook(book)) {
				// DownBookManager.getInstance().downBook(book);
				// }
				// return;
				// }
			}
		});
		item.coverClick.setVisibility(View.GONE);

		item.downBtn.setVisibility(View.GONE);
		item.downPro.setVisibility(View.GONE);
		item.newIcon.setVisibility(View.GONE);
		item.cloud.setVisibility(View.GONE);
		item.newChapter.setVisibility(View.GONE);

		item.title.setVisibility(View.GONE);
		item.percent.setVisibility(View.GONE);
	}

	// 加载赠书卡
	private void loadCardItem(final ViewItem item)
	{
		item.layout.setVisibility(View.VISIBLE);
		loadCardCover(item, null, this.cardUrl, this.cardTip);
		loadOtherView(item, this.cardTip);
	}

	/**
	 * 加载礼包
	 * 
	 * @param item
	 * @param info
	 */
	// private void loadGiftItem(final ViewItem item, final GiftInfo info) {
	// item.layout.setVisibility(View.VISIBLE);
	// loadGiftCover(item, info);
	// loadOtherView(item, info);
	// }

	/**
	 * 加载赠书卡名称
	 */
	private void loadOtherView(ViewItem item, String name)
	{
		item.title.setVisibility(View.VISIBLE);
		item.title.setText(name);
		item.percent.setVisibility(View.GONE);
		item.downBtn.setVisibility(View.GONE);
		item.downPro.setVisibility(View.GONE);
		item.newIcon.setVisibility(View.GONE);
		item.cloud.setVisibility(View.GONE);
		item.newChapter.setVisibility(View.GONE);
	}

	/**
	 * 加载赠书卡封面
	 */
	private void loadCardCover(final ViewItem item, String imgUrl,
			final String actvityUrl, final String activityTip)
	{
		item.cover.setBackgroundResource(R.drawable.new_main_bookframe);
		item.cover.setImageResource(R.drawable.bookcard);

		item.coverClick.setVisibility(View.VISIBLE);
		item.coverClick.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!HttpUtil.isConnected(mActivity)) {
					Toast.makeText(mActivity, R.string.network_error,
							Toast.LENGTH_LONG).show();
					return;
				}

				if (LoginUtil.isValidAccessToken(mActivity) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					reqGsid(actvityUrl, activityTip);
					// String finalURL =
					// Util.getUnInstallObserverUrl(actvityUrl);
					// String gsid =
					// LoginUtil.getLoginInfo().getUserInfo().getGsid();
					// finalURL = HttpUtil.setURLParams(finalURL, "gsid", gsid);
					// RecommendWebUrlActivity.launch(mActivity, finalURL,
					// activityTip);

					// Intent intent = new Intent(mActivity, Reco.class);
					// // String actvity_url = info.getActvity_url();
					// intent.putExtra("url", actvity_url);
					// intent.putExtra("title", info.getGiftName());
					// intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

					// GenericTask getGsidTask = new GenericTask() {
					// protected void onPreExecute() {
					// showProgressDialog(R.string.get_gsid);
					// };
					//
					// @Override
					// protected TaskResult doInBackground(TaskParams... params)
					// {
					//
					// String gsid = LoginUtil.syncRequestGsidHttpConnection();
					// LoginUtil.getLoginInfo().getUserInfo().setGsid(gsid);
					// LoginUtil.saveLoginGsid(gsid);
					//
					// return null;
					// }
					//
					// protected void onPostExecute(TaskResult result) {
					// dismissProgressDialog();
					// };
					// };

					// if (!actvity_url.contains("gsid")) {
					// getGsidTask.setTaskFinishListener(null);
					// getGsidTask.execute();
					// }
					// mActivity.startActivity(intent);

				} else {
					LoginDialog.launch(mActivity, new LoginStatusListener()
					{
						public void onSuccess()
						{
							Intent intent = new Intent();
							intent.setAction(CloudSyncUtil.ACTION_SHELF_SYNC);
							SinaBookApplication.gContext.sendBroadcast(intent);

							//检查是否显示赠书卡
							isShowCard = StorageUtil.getBoolean("showbookcard",
									true);
							setShowCard(isShowCard);

							// 登陆成功后需要给adapter传递赠书卡数据
							reqGsid(actvityUrl, activityTip);
							// String finalURL =
							// Util.getUnInstallObserverUrl(actvityUrl);
							// String gsid =
							// LoginUtil.getLoginInfo().getUserInfo().getGsid();
							// finalURL = HttpUtil.setURLParams(finalURL,
							// "gsid", gsid);
							// RecommendWebUrlActivity.launch(mActivity,
							// finalURL, activityTip);
						}

						public void onFail()
						{

						}
					});
				}
			}
		});
		item.cover.setOnClickListener(null);
		item.coverClick.setOnLongClickListener(null);
	}

	private GenericTask	mReqGsidTask;

	private void reqGsid(String url, final String title)
	{
		final String finalURL = Util.getUnInstallObserverUrl(url);
		String gsid = LoginUtil.getLoginInfo().getUserInfo().getGsid();

		if (gsid != null && gsid.length() > 0 && finalURL != null
				&& finalURL.length() > 0) {
			String endUrl = HttpUtil.setURLParams(finalURL, "gsid", gsid);
			endUrl = HttpUtil.addAuthCode2Url(endUrl);
			RecommendWebUrlActivity.launch(mActivity, endUrl, title);
		} else {
			DialogUtils.showProgressDialog(mActivity, "正在加载...", true, false,
					new OnCancelListener()
					{
						public void onCancel(DialogInterface dialog)
						{
						}
					}, new OnDismissListener()
					{
						public void onDismiss(DialogInterface dialog)
						{
							if (mReqGsidTask != null) {
								mReqGsidTask.cancel(true);
								mReqGsidTask = null;
							}
						}
					});

			mReqGsidTask = new GenericTask()
			{
				protected TaskResult doInBackground(TaskParams... params)
				{
					TaskResult taskResult = new TaskResult(-1, this, null);

					// 1 如果无gsid再去取一次gsid
					if (LoginUtil.isValidAccessToken(mActivity) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
							&& Util.isNullOrEmpty(LoginUtil.getLoginInfo()
									.getUserInfo().getGsid())) {
						String gsid = LoginUtil.i.syncRequestGsidHttpClient().getGsid();
						LoginUtil.getLoginInfo().getUserInfo().setGsid(gsid);
						LoginUtil.saveLoginGsid(gsid);
					}

					// 如果赠书卡url为空，请求userinfo
					String tUrl = finalURL;
					if (finalURL == null || finalURL.length() == 0) {
						boolean result = LoginUtil.syncReqBalance(mActivity);
						if (result) {
							fetchCardInfo();
							tUrl = Util.getUnInstallObserverUrl(cardUrl);
						}
					}

					String endUrl = HttpUtil.setURLParams(tUrl, "gsid",
							LoginUtil.getLoginInfo().getUserInfo().getGsid());
					endUrl = HttpUtil.addAuthCode2Url(endUrl);
					RecommendWebUrlActivity.launch(mActivity, endUrl, title);
					return null;
				}
			};
			mReqGsidTask.setTaskFinishListener(new ITaskFinishListener()
			{
				public void onTaskFinished(TaskResult taskResult)
				{
					DialogUtils.dismissProgressDialog();
					mReqGsidTask = null;
				}
			});
			mReqGsidTask.execute();

		}
	}

	private void loadBookCover(final ViewItem item, final DownBookJob job)
	{
		final Book book = job.getBook();
		final BookDownloadInfo downInfo = book.getDownloadInfo();

		item.cover.setBackgroundResource(R.drawable.new_main_bookframe);
		if (downInfo.getImageUrl() != null
				&& downInfo.getImageUrl().startsWith(Book.SDCARD_PATH_IMG)) {
			// SD卡书籍
			item.cover.setImageBitmap(ImageLoader.getNoImgPic());
		} else {
			// 其他书籍
			ImageLoader.getInstance().load(downInfo.getImageUrl(), item.cover,
					ImageLoader.TYPE_BOOK_HOME_SHELVES_COVER,
					ImageLoader.getNoImgPic());
		}

		item.cover.setOnClickListener(null);
		item.coverClick.setVisibility(View.VISIBLE);
		item.coverClick.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (Math.abs(downInfo.getProgress() - 1.0) < 0.0001
						&& job.getState() == DownBookJob.STATE_FINISHED) {
					if (book.getDownloadInfo() != null) {
						if(book.isHtmlRead()){
							// html书籍
							if(book.isOnlineBook()){
								// 在线书籍
								book.setChapters(null);
								job.start();
							}else{
								// 已下载完成状态，判断更新条件
								int updateChapterNum = book.getUpdateChaptersNum();
								
								//TODO:ouyang
//								updateChapterNum = 2;
								
								if(updateChapterNum > 0){
									// 需要更新阅读
									book.setChapters(null);
									book.setUpdateChaptersNum(0);
									job.start(true);
								}else{
									// 直接阅读
									String path = book.getDownloadInfo().getOriginalFilePath();
									float precent = book.getReadInfo()
											.getLastReadPercent();
									boolean hasPrecent = precent > 0.0f ? true: false;
									FBReader.openBookActivity(mActivity, path, book, hasPrecent);
//									FBReader.openBookActivity(mActivity, path,
//											hasPrecent, book.getBookId());
								}
							}
						}else{
							String path = book.getDownloadInfo()
									.getOriginalFilePath();
							if (path.endsWith(".epub") || book.isEpub()) {
								// epub书籍走FBReader
								boolean handle = EpubHelper.i.checkFileExistsBeforeRead(mActivity, book,
										new EpubHelper.DefaultListener()
										{
											@Override
											public void removeBook()
											{
												setData(DownBookManager.getInstance().getAllJobs());
												notifyDataSetChanged();
											}

											@Override
											public void redownloadBook()
											{
												setData(DownBookManager.getInstance().getAllJobs());
												notifyDataSetChanged();
											}
										});
								if (!handle) {
									float precent = book.getReadInfo()
											.getLastReadPercent();
									boolean hasPrecent = precent > 0.0f ? true
											: false;
									FBReader.openBookActivity(mActivity, path,
											hasPrecent, book.getBookId());
								}
							} else {
								ReadActivity.setChapterReadEntrance("书架");
								ReadActivity.launch(mActivity, book, false, false);
							}
						}
					} else {
						ReadActivity.setChapterReadEntrance("书架");
						ReadActivity.launch(mActivity, book, false, false);
					}
				} else if (job.getState() == DownBookJob.STATE_RUNNING) {
					if(book.isHtmlRead()){
						job.pause();
					}else{
						if (job.getTask() != null) {
							if (job.getTask().isCancelable()) {
								// 正在下载时点击，进入下载暂停状态
								job.pause();
							}
						}
					}
				} else {
					// 下载失败或下载暂停状态，点击都是启动下载
					if(book.isHtmlRead()){
						book.setChapters(null);
						job.start();
					}else{
						// 当书籍是全本购买，且没有登录时，弹出登陆对话框
						if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP
								&& book.getBuyInfo().getBuyType() == 1
								&& LoginUtil.isValidAccessToken(mActivity) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
							LoginDialog.launch(mActivity, null);
						} else {
							job.start();
						}
					}
				}
			}
		});

		item.coverClick.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				// 震动70毫秒
				// Vibrator vib = (Vibrator)
				// mActivity.getSystemService(Service.VIBRATOR_SERVICE);
				// vib.vibrate(70);

				mMenuDialog.show(job);
				return false;
			}
		});
	}

	private void loadProgressView(ViewItem item, DownBookJob job)
	{
		Book book = job.getBook();
		if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) > 0.0001
				|| job.getState() != DownBookJob.STATE_FINISHED) {
			// 给按钮设置监听器
			setDownBtnListener(item, job);

			item.downBtn.setVisibility(View.VISIBLE);
			if (job.getState() == DownBookJob.STATE_PAUSED
					|| job.getState() == DownBookJob.STATE_FAILED) {
				// 下载暂停或者下载失败的情况
				item.downBtn.setImageResource(R.drawable.book_down);
				item.downPro.setVisibility(View.GONE);

			} else {
				// 正在下载或者解析的情况
				item.downBtn.setImageResource(R.drawable.book_pause);
				item.downPro.setVisibility(View.VISIBLE);
				item.downPro.setProgress((int) Math.round(100 * book
						.getDownloadInfo().getProgress()));
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
			// if (book.getTag() == Book.IS_NEW) {
			// item.newIcon.setVisibility(View.VISIBLE);
			// item.newIcon.setBackgroundResource(R.drawable.new_icon);
			// } else {
			// item.newIcon.setVisibility(View.GONE);
			// }

			float precent = book.getReadInfo().getLastReadPercent();
			boolean hasPrecent = precent > 0.0f ? true : false;
			// Log.d("IS_NEW",
			// "precent="+precent+", book={"+book.getTitle()+"}");
			// "新书"标识的逻辑：只要这本书当前处于下载完成状态了，并且用户没有阅读过，就可以显示新书标识
			// 如果没有阅读百分比并且不是在线书籍并且没有进入阅读过
			if (!hasPrecent && !book.isOnlineBook() && book.getTag() != Book.HAS_READ) {
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
	private void updateBookLength(Book book)
	{
		String filePath = book.getDownloadInfo().getFilePath();
		if (filePath.contains("file")) {
			try {
				filePath = StorageUtil.copyFile(filePath.substring(filePath
						.lastIndexOf('/')));
			} catch (IOException e) {
				LogUtil.e(TAG, e.getLocalizedMessage());
			}
		}

		File bookFile = new File(filePath);
		if (!bookFile.exists() || !bookFile.isFile() || !bookFile.canRead()
				|| bookFile.length() <= 0) {
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
	private void setDownBtnListener(ViewItem item, final DownBookJob job)
	{
		item.downBtn.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				if (job.getState() == DownBookJob.STATE_PAUSED
						|| job.getState() == DownBookJob.STATE_FAILED) {
					// 当书籍不是免费书，且没有登录时，提示登录
					BookBuyInfo info = job.getBook().getBuyInfo();
					if (info.getPayType() == Book.BOOK_TYPE_VIP
							&& info.getBuyType() == 1
							&& LoginUtil.isValidAccessToken(mActivity) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
						LoginDialog.launch(mActivity, null);
					} else {
						job.start();
					}
				} else {
					if(job.getBook().isHtmlRead()){
						job.pause();
					}else{
						if (job.getTask() != null) {
							if (job.getTask().isCancelable()) {
								// 正在下载时点击，进入下载暂停状态
								job.pause();
							}
						}
					}
					
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
	private void loadOtherView(ViewItem item, DownBookJob job)
	{
		Book book = job.getBook();
		item.title.setVisibility(View.VISIBLE);
		item.title.setText(book.getTitle());

		StringBuilder builder = new StringBuilder("已读");
		// int start = builder.length();
		builder.append(book.getReadInfo().getLastReadPercent()).append("%");
		// int end = builder.length();
		// int color = ResourceUtil.getColor(R.color.praise_num_color);
		// Spanned spanned = Util.highLight(builder, color, start, end);
		item.percent.setVisibility(View.VISIBLE);
		item.percent.setText(builder.toString());

		if (book.isOnlineBook()) {
			item.cloud.setVisibility(View.VISIBLE);
		} else {
			item.cloud.setVisibility(View.GONE);
		}
	}

	private View createView()
	{
		View itemView = LayoutInflater.from(mActivity).inflate(
				R.layout.vw_main_shelf_grid_item, null);
		ViewHolder holder = new ViewHolder();

		if (itemView == null) {
			return null;
		}
		holder.totalView = itemView.findViewById(R.id.total_view);

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
			item.coverClick = (ImageView) item.layout
					.findViewById(R.id.book_cover_click);
			item.cloud = (ImageView) item.layout.findViewById(R.id.book_cloud);
			item.newIcon = (ImageView) item.layout.findViewById(R.id.book_tip);
			item.newChapter = (TextView) item.layout
					.findViewById(R.id.book_new_chapter);
			item.downBtn = (ImageView) item.layout
					.findViewById(R.id.book_down_pause);
			item.downPro = (ProgressBar) item.layout
					.findViewById(R.id.book_download_progressBar);
			item.title = (EllipsizeTextView) item.layout
					.findViewById(R.id.book_title);
			item.percent = (TextView) item.layout.findViewById(R.id.book_state);

			holder.items.add(item);
		}

		itemView.setTag(holder);

		return itemView;
	}

	protected class ViewHolder
	{
		protected View				totalView;
		protected List<ViewItem>	items	= new ArrayList<ViewItem>(COLUMNS_NUM);
	}

	private class ViewItem
	{
		private View				layout;
		private ImageView			cover;
		private ImageView			coverClick;
		// 下载，暂停
		private ImageView			downBtn;
		// 新书
		private ImageView			newIcon;
		// 在线书籍
		private ImageView			cloud;
		// 新章节更新Tip
		private TextView			newChapter;
		// 下载进度
		private ProgressBar			downPro;
		// 书籍名称
		private EllipsizeTextView	title;
		// 阅读百分比
		private TextView			percent;
	}

	/**
	 * 菜单栏Dialog
	 */
	private class MenuDialog
	{
		private Dialog		mDialog;

		private View		mContentView;
		private ViewHolder	mHolder;

		public MenuDialog()
		{
			createView();

			// mDialog = new Dialog(mActivity,
			// android.R.style.Theme_DeviceDefault_Dialog);
			mDialog = new Dialog(mActivity, R.style.MenuDialog);
			// mDialog = new Dialog(mActivity,
			// android.R.style.Theme_Translucent_NoTitleBar);

			WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
			lp.gravity = Gravity.BOTTOM;
			lp.width = Util.getDisplayMetrics(mActivity).widthPixels;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

			mDialog.onWindowAttributesChanged(lp);
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.setContentView(mContentView);
		}

		public void show(DownBookJob job)
		{
			if (null != mDialog) {
				if (mDialog.isShowing()) {
					dismiss();
				}

				setViewValue(job);
				mDialog.show();
			}
		}

		public void dismiss()
		{
			if (null != mDialog) {
				mDialog.dismiss();
			}
		}

		private void setViewValue(DownBookJob job)
		{
			if (null != mContentView && null != mHolder && null != job) {
				Book book = job.getBook();

				// 封面
				final String imgUrl = book.getDownloadInfo().getImageUrl();
				if (!TextUtils.isEmpty(imgUrl)
						&& imgUrl.startsWith(Book.SDCARD_PATH_IMG)) {
					// SD卡书籍
					mHolder.cover.setImageBitmap(ImageLoader.getNoImgPic());

				} else {
					// 其他书籍
					ImageLoader.getInstance().load(imgUrl, mHolder.cover,
							ImageLoader.TYPE_BOOK_HOME_SHELVES_COVER,
							ImageLoader.getNoImgPic());
				}

				// 书名
				mHolder.title.setText(book.getTitle());
				// 作者
				mHolder.author.setText(mActivity.getString(R.string.author)
						+ book.getAuthor());

				// 阅读进度
				float percent = book.getReadInfo().getLastReadPercent();
				float fileSize = book.getDownloadInfo().getFileSize();
				// 无阅读进度，但是存在最后阅读记录
				if (percent <= 0 && book.getReadInfo().getLastPos() != 0) {
					// 免费书籍并且文件大小不为0
					if (book.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE
							&& fileSize > 0) {
						percent = (float) (book.getReadInfo().getLastPos() * 1.0 / fileSize) * 100;
					}
				}

				percent = Util.formatFloat(percent, 2);
				mHolder.readPro.setProgress((int) percent);
				// 阅读百分比
				String percentStr = String.format(
						mActivity.getString(R.string.bookhome_read_percent),
						book.getReadInfo().getLastReadPercent());
				mHolder.percent.setText(percentStr);

				ClickListener listener = new ClickListener(job);
				mHolder.menuDelete.setOnClickListener(listener);
				mHolder.menuShare.setOnClickListener(listener);
				mHolder.menuUpdate.setOnClickListener(listener);
				mHolder.menuDown.setOnClickListener(listener);

				if (book.isOurServerBook() && book.isOnlineBook()) {
					mHolder.menuDown.setVisibility(View.VISIBLE);
					mHolder.menuUpdate.setVisibility(View.GONE);
					// 长按本地书籍后,再长按在线书籍时，需要重置按钮状态
					mHolder.menuShare.setEnabled(true);
					mHolder.menuUpdate.setEnabled(true);
				} else {
					mHolder.menuDown.setVisibility(View.GONE);
					mHolder.menuUpdate.setVisibility(View.VISIBLE);

					// 不是自己服务器上以及微盘上下载的书籍分享更新按钮不可点
					if (!book.isOurServerBook()) {
						if (!book.isVDiskBook()) {
							mHolder.menuShare.setEnabled(false);
						} else {
							mHolder.menuShare.setEnabled(true);
						}
						mHolder.menuUpdate.setEnabled(false);
					} else {
						// epub的书籍不能分享/更新
						if (book.isEpub()) {
							mHolder.menuShare.setEnabled(false);
							mHolder.menuUpdate.setEnabled(false);
						} else {
							mHolder.menuShare.setEnabled(true);
							mHolder.menuUpdate.setEnabled(true);
						}
					}
				}
			}
		}

		private void createView()
		{
			mContentView = LayoutInflater.from(mActivity).inflate(
					R.layout.vw_shelf_grid_menu, null);
			mHolder = new ViewHolder();

			mHolder.cover = (ImageView) mContentView.findViewById(R.id.cover);

			mHolder.title = (EllipsizeTextView) mContentView
					.findViewById(R.id.title);
			mHolder.author = (TextView) mContentView.findViewById(R.id.author);

			mHolder.readPro = (ProgressBar) mContentView
					.findViewById(R.id.progress_bar);
			mHolder.percent = (TextView) mContentView
					.findViewById(R.id.progress_percent);

			mHolder.menuDelete = (TextView) mContentView
					.findViewById(R.id.menu_delete);
			mHolder.menuShare = (TextView) mContentView
					.findViewById(R.id.menu_share);
			mHolder.menuUpdate = (TextView) mContentView
					.findViewById(R.id.menu_update);
			mHolder.menuDown = (TextView) mContentView
					.findViewById(R.id.menu_down);

			Bitmap dotHBitmap = BitmapFactory.decodeResource(
					mActivity.getResources(), R.drawable.list_divide_dot);
			BitmapDrawable drawable = new BitmapDrawable(
					mActivity.getResources(), dotHBitmap);
			drawable.setTileModeXY(Shader.TileMode.REPEAT,
					Shader.TileMode.REPEAT);
			drawable.setDither(true);
			((ImageView) mContentView.findViewById(R.id.divider))
					.setImageDrawable(drawable);
		}

		protected class ViewHolder
		{
			private ImageView			cover;
			private EllipsizeTextView	title;
			private TextView			author;
			private ProgressBar			readPro;
			private TextView			percent;

			private TextView			menuDelete;
			private TextView			menuShare;
			private TextView			menuUpdate;
			private TextView			menuDown;
		}

		private class ClickListener implements View.OnClickListener
		{
			private DownBookJob	job;

			public ClickListener(DownBookJob job)
			{
				this.job = job;
			}

			@Override
			public void onClick(View v)
			{
				switch (v.getId()) {
				case R.id.menu_delete:
					// 删除
					mMenuDialog.dismiss();
					deleteBook(job);
					break;
					
				case R.id.menu_share:
					// 分享
					mMenuDialog.dismiss();
					share(job.getBook());
					break;

				case R.id.menu_update:
					// 更新
					mMenuDialog.dismiss();

					List<Book> needCheckBooks = new ArrayList<Book>();
					needCheckBooks.add(job.getBook());
					Toast.makeText(mActivity,
							R.string.xlistview_header_hint_loading,
							Toast.LENGTH_SHORT).show();
					
					UpdateChapterManager.getInstance().checkNewChapter(
							needCheckBooks,
							UpdateChapterManager.REQ_SINGLE_BOOK);
					break;
					
				case R.id.menu_down:
					// 下载
					mMenuDialog.dismiss();
					final Book book = job.getBook();
					// 修复：BugID=21228
					// 判断书籍的付费类型，配置相应的参数
					// int payType = book.getBuyInfo().getPayType();
					// if (payType == BOOK_TYPE_VIP) {
					// if (!book.getBuyInfo().isHasBuy() ||
					// !PurchasedBookList.getInstance().isBuy(book)) {
					// book.setAutoDownBook(true);
					// }
					// }
					book.checkAutoDownBook();
					
					if(book.isHtmlRead()){
						job.start();
					}else{
						DownBookManager.getInstance().downBook(book);
					}
					break;
				default:
					break;
				}
			}
		}
		
		// 删除一本书
		private void deleteBook(final DownBookJob job)
		{
			String msg = String.format(mActivity.getString(R.string.note_info),
					job.getBook().getTitle());
			CommonDialog.show(mActivity, msg,
					new CommonDialog.DefaultListener()
					{
						public void onRightClick(DialogInterface dialog)
						{
							final Book book = job.getBook();
							if(book.isHtmlRead()){
								// 删除html阅读
								HtmlDownManager.getInstance().deleteBook(mActivity, book);
							}else {
								EpubHelper.i.removeBook(mActivity, book);
							}
							
							// 重新设置数据
							setData(DownBookManager.getInstance().getAllJobs());
							notifyDataSetChanged();
						}
					});
		}
		
		private void share(final Book book)
		{
			new GenericTask()
			{
				WeiboContent	mContent;

				@Override
				protected void onPreExecute()
				{
					showProgressDialog(R.string.create_share_content);
				}

				@Override
				protected TaskResult doInBackground(TaskParams... params)
				{
					mContent = new WeiboContent(book,
							WeiboContent.TYPE_BOOK_DETAIL);

					Bitmap cover = ImageLoader.getInstance().syncLoadBitmap(
							book.getDownloadInfo().getImageUrl());
					if (null != cover && cover != ImageLoader.getDefaultPic()) {
						mContent.setImagePath(Util.saveBitmap2file(cover,
								book.getTitle(), 100));
					}

					mContent.setChapterId(Chapter.DEFAULT_GLOBAL_ID);
					mContent.setChapterOffset(0);

					if (book.isVDiskBook()) {
						// 微盘书籍生成分享链接
						mContent.setMsg(book.getDownloadInfo()
								.getVDiskFilePath());
					} else {
						// 其他书籍分享简介
						String introText = book.getIntro();
						if (TextUtils.isEmpty(introText)) {
							introText = " ";
						}
						mContent.setMsg(introText);
					}

					return null;
				}

				@Override
				protected void onPostExecute(TaskResult result)
				{
					dismissProgressDialog();

					if (null != mContent
							&& !TextUtils.isEmpty(mContent.getMsg())) {
						ShareDialog.show(mActivity, mContent);
					} else {
						Toast.makeText(mActivity,
								R.string.create_share_content_failed,
								Toast.LENGTH_SHORT).show();
					}
				}
			}.execute();
		}
	}

	private void showProgressDialog(int resId)
	{
		if (null == mProgressDialog) {
			mProgressDialog = new CustomProDialog(mActivity);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}

		mProgressDialog.show(resId);
	}

	private void dismissProgressDialog()
	{
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

}
