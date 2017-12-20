package com.sina.book.ui.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpStatus;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.HtmlDownBookManager;
import com.sina.book.control.download.HtmlDownManager;
import com.sina.book.control.download.ITaskUpdateListener;
import com.sina.book.control.download.UpdateChapterManager;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.GiftInfo;
import com.sina.book.data.GiftType;
import com.sina.book.data.ListResult;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.image.ImageLoader;
import com.sina.book.image.PauseOnScrollListener;
import com.sina.book.ui.MainActivity;
import com.sina.book.ui.MaskGuideActivity;
import com.sina.book.ui.adapter.MainShelvesGridAdapter;
import com.sina.book.ui.notification.DownloadBookNotification;
import com.sina.book.ui.notification.PushUpdatedNotification;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.ui.widget.XListViewHeader;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;

/*
 * 书架activity->我的书架fragment
 */
public class MainShelvesFragment extends BaseFragment implements ITaskUpdateListener, IXListViewListener,
		UpdateChapterManager.IUpdateBookListener, ITaskFinishListener {

	/**
	 * 书架列表.
	 */
	private XListView mShelvesListView;
	/**
	 * 书架Grid书籍Adapter
	 */
	private MainShelvesGridAdapter mGridAdapter;

	/**
	 * 同步进度view
	 */
	// private View mSyncLoadingWeak;
	/**
	 * 该Fragment是否初始化
	 */
	private boolean mIsInit;

	// 设置开关：是否允许显示赠书卡
	public static boolean ENABLE_CARD_SHOW = false;
	
	// 赠书卡是否有值
	private boolean isShowCard;

	private BroadcastReceiver mSyncReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			String action = intent.getAction();

			if (CloudSyncUtil.ACTION_LOGIN_SYNC_SUCCESS.equals(action)) {
				// 登陆成功后，请求完书架信息，通知刷新书架
				// dismissSyncLoadView();
				showShelvesView();
				// } else if
				// (CloudSyncUtil.ACTION_SHELF_SYNC_SUCCESS.equals(action)) {
				// // 以前 5分钟轮询请求同步监听
				// refreshBookShelf();
			} else if (CloudSyncUtil.ACTION_SHELF_SYNC.equals(action)) {
				// 主页登陆成功后的回调(这里不需要请求同步接口，因为LoginDialog登陆成功会调用syncfrom())
				// syncBookShelf();
				refreshBookShelf();
			}
		};
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(CloudSyncUtil.ACTION_LOGIN_SYNC_SUCCESS);
		// myIntentFilter.addAction(CloudSyncUtil.ACTION_SHELF_SYNC_SUCCESS);
		myIntentFilter.addAction(CloudSyncUtil.ACTION_SHELF_SYNC);
		getActivity().registerReceiver(mSyncReceiver, myIntentFilter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_shelves, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initView();
		super.onViewCreated(view, savedInstanceState);
	}

	// 检测是否显示赠书卡
	public void checkCardIsShow() {
		if (ENABLE_CARD_SHOW) {
			boolean showcard = StorageUtil.getBoolean("showbookcard", true);
			mGridAdapter.setShowCard(showcard);
			mGridAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mShelvesListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

		// 从数据库中读取书架信息展示
		showShelvesView();

		// 是否显示赠书卡
		checkCardIsShow();

		// // 大礼包
		// if (!mIsInit) {
		// reqGiftInfo();
		// mIsInit = true;
		// }

		if (LoginUtil.isValidAccessToken(getActivity()) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			if (!mIsInit) {
				// 从服务器同步数据
				syncBookShelf();
				mIsInit = true;
			}
			// CloudSyncUtil.getInstance().stopSyncService();
			// CloudSyncUtil.getInstance().startSyncService();
		} else {
			// dismissSyncLoadView();
		}

		// TODO CJL 回到书架时要清除所有的通知？
		// 有正在下载的书籍的通知也要清除吗？
		// 并且最重要的问题是，为何没清除掉啊？
		// TM，好像只有我的GallaxyNexus3手机有问题，跟ROM有关系？
		DownloadBookNotification.getInstance().clearAllNotification();
		PushUpdatedNotification.getInstance().clearAllNotification();

		DownBookManager.getInstance().addProgressListener(this);
		HtmlDownManager.getInstance().addProgressListener(this);
		
		UpdateChapterManager.getInstance().addUpdateBookListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		
		UpdateChapterManager.getInstance().removeUpdateBookListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		DownBookManager.getInstance().removeProgressListener(this);
		HtmlDownManager.getInstance().removeProgressListener(this);
		
		mShelvesListView.stopRefresh();
	}

	@Override
	public void onDestroy() {
		CloudSyncUtil.getInstance().cancelSyncFrom();
		getActivity().unregisterReceiver(mSyncReceiver);
		super.onDestroy();
	}

	@Override
	// 书架frgment被选中
	public void onSelected() {
		if (isDestroyed()) {
			return;
		}

		// //TODO: 赠书卡是否可以显示

		// 请求大礼包接口
		// reqGiftInfo();
		// if (!mIsInit) {
		// boolean isSync = syncBookShelf();
		// // 书架上无书时，显示同步进度框
		// if (isSync && LoginUtil.isValidAccessToken(getActivity()) ==
		// LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
		// showSyncLoadView();
		// }
		// mIsInit = true;
		// }
	}

	private boolean syncBookShelf() {
		// 解决：BugID=21604
		CloudSyncUtil.getInstance().syncTo("MainShllvesFragment-syncBookShelf");
		boolean isSync = CloudSyncUtil.getInstance().syncFrom(getActivity(), new ITaskFinishListener() {

			@Override
			public void onTaskFinished(TaskResult taskResult) {
				refreshBookShelf();
			}

		}, "MainShllvesFragment-syncBookShelf");
		return isSync;
	}

	private void refreshBookShelf() {
		// 同步完成再刷新下载书籍
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).parseIntent();
		}

		// dismissSyncLoadView();
		// 刷新数据
		showShelvesView();
	}

	@Override
	public void updateAllBookFinished() {
		// 刷新数据
		showShelvesView();

		StorageUtil.saveLong(StorageUtil.KEY_UPDATE_TIME, System.currentTimeMillis());
		mShelvesListView.setRefreshTime(getUpdateTime());
		mShelvesListView.stopRefresh();
	}

	@Override
	public void updateBookFinished(Book book) {
		mGridAdapter.notifyDataSetChanged();
	}

	@Override
	public void onRefresh() {
		// 书架下拉刷新
		UpdateChapterManager.getInstance().checkNewChapter(UpdateChapterManager.REQ_BY_USER);
		syncBookShelf();
	}

	@Override
	public void onLoadMore() {

	}

	@Override
	public void onUpdate(Book book, boolean mustUpdate, boolean mustRefresh, int progress, int stateCode) {

		if (mustUpdate) {
			showShelvesView();
		} else if (mustRefresh) {
			mGridAdapter.notifyDataSetChanged();
		}

		if (stateCode == DownBookJob.STATE_RECHARGE) {
			PayDialog.showBalanceDlg(getActivity());
		}

	}

	private void initView() {
		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}

		mShelvesListView = (XListView) root.findViewById(R.id.book_home_shelves_listview);

		mShelvesListView.setPullRefreshEnable(true, XListViewHeader.TYPE_PULL_UPDATE);
		mShelvesListView.setPullLoadEnable(false);
		mShelvesListView.setXListViewListener(this);
		mShelvesListView.setRefreshTime(getUpdateTime());

		// 默认设置书架Adapter
		mGridAdapter = new MainShelvesGridAdapter(getActivity());
		mShelvesListView.setAdapter(mGridAdapter);
		mShelvesListView.setOnItemClickListener(null);

		// mSyncLoadingWeak = root.findViewById(R.id.sync_loading_weak);

		// 判断是不是第一次登录
		// 为了处理这种情况：用户卸载客户端时书架内已经添加了许多书籍，此后再次安装客户端并
		// 启动客户端时，书架内的书籍依然存在。
		// 为了解决这个问题，在首次启动客户端时进行一次判断，
		// 如果是第一次启动，做一次类似退出登录的操作。
		// 将书架内的书籍，绑定过UID并且非在线的书籍进行删除并缓存到BookCacheTable中
		// 另外增加一个条件：第一次启动时处于未登录状态
		boolean isFirstStartApp = StorageUtil.getBoolean("isFirstStartApp", true);
		boolean isNoLoginState = LoginUtil.isValidAccessToken(SinaBookApplication.gContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS;
		LogUtil.d("ReadInfoLeft", "MainShelvesFragment >> isFirstStartApp >> " + isFirstStartApp);
		LogUtil.d("ReadInfoLeft", "MainShelvesFragment >> isNoLoginState >> " + isNoLoginState);
		if (isFirstStartApp && isNoLoginState) {
			StorageUtil.saveBoolean("isFirstStartApp", false);
			CloudSyncUtil.getInstance().logout("MainShelvesFragment");
		}
	}

	private String getUpdateTime() {
		String timeStr = getString(R.string.do_not_update);
		long time = StorageUtil.getLong(StorageUtil.KEY_UPDATE_TIME);
		if (-1 != time) {
			timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(time));
		}
		return timeStr;
	}

	private void showShelvesView() {
		// 新浪应用中心的书不要遮蔽
		if (ConstantData.isSinaAppChannel(getActivity())) {
			StorageUtil.saveBoolean(StorageUtil.KEY_MAIN_GUIDE_SHOW, false);
		}
		MaskGuideActivity.launchForResult(getActivity(), StorageUtil.KEY_MAIN_GUIDE_SHOW,
				R.layout.guide_shelves_grid_layout, MainActivity.MASK_GUIDE_ACTIVITY_CODE);
		setList();
		mGridAdapter.notifyDataSetChanged();
	}

	// private void showSyncLoadView() {
	// // mSyncLoadingWeak.setVisibility(View.VISIBLE);
	// }
	//
	// private void dismissSyncLoadView() {
	// // mSyncLoadingWeak.setVisibility(View.GONE);
	// }

	// 请求礼包接口
	// private void reqGiftInfo() {
	// String url = ConstantData.URL_GIFT;
	// TaskParams params = new TaskParams();
	// params.put(RequestTask.PARAM_URL, url);
	// params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
	// RequestTask task = new RequestTask(new GiftParser());
	// task.setTaskFinishListener(this);
	// task.execute(params);
	// }

	@Override
	// 响应礼包数据请求
	public void onTaskFinished(TaskResult taskResult) {
		if (null != taskResult && taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof ListResult<?>) {
				ListResult<GiftInfo> listResult = (ListResult<GiftInfo>) taskResult.retObj;
				ArrayList<GiftInfo> giftInfos = listResult.getList();
				GiftInfo shelfGift = null;
				for (GiftInfo info : giftInfos) {
					if (info.getPlace().equals(GiftType.TYPE_BOOK_SHELF)) {
						shelfGift = info;
						mGridAdapter.setGiftInfo(shelfGift);
						setList();
						mGridAdapter.notifyDataSetChanged();
						break;
					}
				}

				if (shelfGift == null) {
					mGridAdapter.setGiftInfo(null);
					mGridAdapter.setGiftInfo(shelfGift);
					setList();
					mGridAdapter.notifyDataSetChanged();
				}

			}

		}
	}

	// 从数据库中获取
	private void setList() {
		ArrayList<DownBookJob> jobs = DownBookManager.getInstance().getAllJobs();

		if (jobs.isEmpty()) {
			DownBookManager.getInstance().init();
			jobs = DownBookManager.getInstance().getAllJobs();
		}
		mGridAdapter.setData(jobs);
	}

}
