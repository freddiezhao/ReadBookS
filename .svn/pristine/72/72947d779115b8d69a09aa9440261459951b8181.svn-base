package com.sina.book.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.CommonRecommendItem;
import com.sina.book.data.CommonRecommendResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.MainBookItem;
import com.sina.book.data.MainBookResult;
import com.sina.book.data.util.StaticInfoKeeper;
import com.sina.book.image.ImageLoader;
import com.sina.book.image.PauseOnScrollListener;
import com.sina.book.parser.CommonRecommendParser;
import com.sina.book.ui.adapter.CommonRecommendAdapter;
import com.sina.book.ui.adapter.ListAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.StorageUtil;

/**
 * 员工推荐、大家推荐页面
 * 
 * @author YangZhanDong
 * 
 */
public class CommonRecommendActivity extends CustomTitleActivity implements IXListViewListener, ITaskFinishListener
{

	private static final String					EMPLOYEE_REC_TYPE	= "2";
	private static final String					PEOPLE_REC_TYPE		= "3";

	public static final String					EMPLOYEE_TYPE		= "employee_type";	// 员工推荐
	public static final String					PEOPLE_TYPE			= "people_type";	// 大家推荐

	private XListView							mListView;
	private ListAdapter<CommonRecommendItem>	mAdapter;

	private View								mErrorView;
	private View								mProgressView;

	// private int mCurrentPage;
	private String								mType;

	private int									mLastFirstPos;

	private boolean								mIsFirstTime		= true;

	public static void launch(Context context, String type)
	{
		Intent intent = new Intent();
		intent.setClass(context, CommonRecommendActivity.class);
		intent.putExtra("type", type);
		context.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState)
	{
		setContentView(R.layout.act_all_recommend);

		initIntent();
		initTitle();
		initView();
		initData();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
	}

	@Override
	public void onRelease()
	{
		mLastFirstPos = mListView.getFirstVisiblePosition();
		mListView.setAdapter(null);
		super.onRelease();
	}

	@Override
	public void onLoad()
	{
		mListView.setAdapter(mAdapter);
		mListView.setSelectionFromTop(mLastFirstPos, 0);
		super.onLoad();
	}

	private void initIntent()
	{
		Intent intent = getIntent();
		mType = intent.getStringExtra("type");
	}

	private void initTitle()
	{
		if (mType.equals(PEOPLE_TYPE)) {
			View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_backmain_left, null);
			setTitleLeft(left);

			TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
			if (null != middle) {
				middle.setText(R.string.all_recommend_title);
			}
			setTitleMiddle(middle);

			View right = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_search, null);
			setTitleRight(right);

		} else {
			View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
			setTitleLeft(left);

			TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
			if (null != middle) {
				middle.setText(R.string.employee_recommend_title);
			}
			setTitleMiddle(middle);
		}
	}

	private void initView()
	{
		mListView = (XListView) findViewById(R.id.all_recommend_listview);
		mAdapter = new CommonRecommendAdapter(this, mType);
		mListView.setAdapter(mAdapter);
		mListView.setPullRefreshEnable(true);
		mListView.setPullLoadEnable(false);
		mListView.setXListViewListener(this);
		mListView.setRefreshTime(getUpdateTime());

		mErrorView = findViewById(R.id.error_layout);
		mProgressView = findViewById(R.id.progress_layout);
	}

	private void initData()
	{
		if (!HttpUtil.isConnected(this)) {
			mErrorView.setVisibility(View.VISIBLE);
		} else {
			mErrorView.setVisibility(View.GONE);
			mProgressView.setVisibility(View.VISIBLE);

			reqData(1);
			if (mType.equals(EMPLOYEE_TYPE)) {
				mListView.setRefreshTime(getUpdateTime());
			} else {
				mListView.setRefreshTime(getUpdateTime());
			}
		}
	}

	@Override
	public void onClickLeft()
	{
		finish();
	}

	@Override
	public void onClickRight()
	{
		Intent intent = new Intent();
		intent.setClass(this, SearchActivity.class);
		startActivity(intent);
	}

	@Override
	public void onRefresh()
	{
		updateData();
	}

	@Override
	public void onLoadMore()
	{
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mListView.stopLoadMore();
			return;
		}
		if (mType.equals(EMPLOYEE_TYPE)) {
			if (mAdapter.hasMore()) {
				reqData(mAdapter.getCurrentPage() + 1);
			}
		} else {
			reqData(mAdapter.getCurrentPage() + 1);
		}
	}

	@Override
	protected void retry()
	{
		mErrorView.setVisibility(View.GONE);
		mProgressView.setVisibility(View.VISIBLE);
		reqData(1);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult)
	{

		RequestTask task = (RequestTask) taskResult.task;
		int page = (Integer) task.getExtra();

		if (page == 1) {
			mProgressView.setVisibility(View.GONE);
			((CommonRecommendAdapter) mAdapter).clearList();
			((CommonRecommendAdapter) mAdapter).updateDate();
		}
		mListView.stopRefresh();
		mListView.stopLoadMore();

		if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
			CommonRecommendResult result = (CommonRecommendResult) taskResult.retObj;
			if (page == 1 && mType.equals(PEOPLE_TYPE)) {
				// 为了与主界面推荐的书一致
				if (mIsFirstTime) {
					if (result.getItem() != null && result.getItem().size() > 0
							&& StaticInfoKeeper.getMainBookInfo() != null
							&& StaticInfoKeeper.getMainBookInfo().getPeopleRecommendBook() != null
							&& StaticInfoKeeper.getMainBookInfo().getPeopleRecommendUser() != null
							&& StaticInfoKeeper.getMainBookInfo().getPeopleRecommendTime() != null) {

						for (int i = 1; i < result.getItem().size(); i++) {
							CommonRecommendItem item = result.getItem().get(i);
							MainBookResult mainBookInfo = StaticInfoKeeper.getMainBookInfo();
							if (item.getBook().getBookId().equals(mainBookInfo.getPeopleRecommendBook().getBookId())
									&& item.getUserInfoRec().getUid()
											.equals(mainBookInfo.getPeopleRecommendUser().getUid())) {

								result.getItem().remove(i);
							}
						}
						CommonRecommendItem cpItem = result.getItem().get(0);
						result.getItem().set(
								0,
								new CommonRecommendItem(StaticInfoKeeper.getMainBookInfo().getPeopleRecommendUser(),
										StaticInfoKeeper.getMainBookInfo().getPeopleRecommendBook(), StaticInfoKeeper
												.getMainBookInfo().getPeopleRecommendTime()));

						cpItem.getBook().setComment(
								StaticInfoKeeper.getMainBookInfo().getPeopleRecommendBook().getComment());
						updateMainInfo(cpItem);
					}
					mIsFirstTime = false;
				} else {
					if (result.getItem() != null && result.getItem().size() > 0
							&& StaticInfoKeeper.getMainBookInfo() != null
							&& StaticInfoKeeper.getMainBookInfo().getPeopleRecommendBook() != null) {
						CommonRecommendItem cpItem = result.getItem().get(0);
						cpItem.getBook().setComment(
								StaticInfoKeeper.getMainBookInfo().getPeopleRecommendBook().getComment());
						updateMainInfo(cpItem);
					}
				}
			}

			if (mType.equals(EMPLOYEE_TYPE)) {
				if (result != null && result.getTotal() > 0) {
					// mCurrentPage = page;
					// TODO
					mAdapter.setCurrentPage(page);
					mAdapter.addList(result.getItem());
					mAdapter.setTotal(result.getTotal());

					if (mAdapter.hasMore()) {
						mListView.setPullLoadEnable(true);
					} else {
						mListView.setPullLoadEnable(false);
					}

				}
			} else {
				if (result != null) {
					// mCurrentPage = page;
					// TODO
					mAdapter.setCurrentPage(page);
					mAdapter.addList(result.getItem());
					mListView.setPullLoadEnable(true);
				}
			}

			mAdapter.notifyDataSetChanged();
			if (mType.equals(EMPLOYEE_TYPE)) {
				StorageUtil.saveLong(StorageUtil.KEY_UPDATE_EMPLOYEE, System.currentTimeMillis());
				mListView.setRefreshTime(getUpdateTime());
			} else {
				StorageUtil.saveLong(StorageUtil.KEY_UPDATE_PEOPLE, System.currentTimeMillis());
				mListView.setRefreshTime(getUpdateTime());
			}
			return;

		} else {
			if (page == 1) {
				if (taskResult.stateCode != HttpStatus.SC_OK) {
					mErrorView.setVisibility(View.VISIBLE);
				} else {
					// 数据异常
					mListView.setPullLoadEnable(false);
					// shortToast(R.string.data_error);
				}
			} else {
				shortToast(R.string.network_unconnected);
			}
		}

	}

	private void reqData(int page)
	{
		String reqUrl;
		if (mType.equals(EMPLOYEE_TYPE)) {
			reqUrl = String.format(ConstantData.URL_REC_COMMENDLIST, EMPLOYEE_REC_TYPE, page, ConstantData.PAGE_SIZE);
		} else {
			reqUrl = String.format(ConstantData.URL_REC_COMMENDLIST, PEOPLE_REC_TYPE, page, ConstantData.PAGE_SIZE);
		}

		RequestTask reqTask = new RequestTask(new CommonRecommendParser());
		reqTask.setTaskFinishListener(this);
		reqTask.setExtra(page);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	private void updateData()
	{
		if (!HttpUtil.isConnected(this)) {
			Toast.makeText(this, R.string.network_unconnected, Toast.LENGTH_SHORT).show();

			mListView.stopRefresh();
			return;
		} else {
			reqData(1);
			mListView.setRefreshTime(getUpdateTime());
		}
	}

	private String getUpdateTime()
	{
		String key = "";
		if (mType.equals(EMPLOYEE_TYPE)) {
			key = StorageUtil.KEY_UPDATE_EMPLOYEE;
		} else {
			key = StorageUtil.KEY_UPDATE_PEOPLE;
		}
		String timeStr = getString(R.string.do_not_update);
		long time = StorageUtil.getLong(key);
		if (-1 != time) {
			timeStr = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(time));
		}
		return timeStr;
	}

	private void updateMainInfo(CommonRecommendItem item)
	{
		MainBookItem bookItem = new MainBookItem();
		bookItem.setBook(item.getBook());
		bookItem.setPeopleRecommend(item.getUserInfoRec());
		bookItem.setPeopleRecommendTime(item.getRecommendTime());
		StaticInfoKeeper.getMainBookInfo().setPeopleRecommend(bookItem);
	}
}