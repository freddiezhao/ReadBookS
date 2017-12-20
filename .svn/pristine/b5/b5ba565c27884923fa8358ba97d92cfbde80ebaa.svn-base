package com.sina.book.ui.view;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.ListResult;
import com.sina.book.data.RechargeDetail;
import com.sina.book.parser.RechargeDetailParser;
import com.sina.book.ui.adapter.RechargeDetailAdapter;
import com.sina.book.ui.widget.XScrollView;
import com.sina.book.ui.widget.XScrollView.IXScrollViewListener;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.Util;

public class RechargeDetailFragment extends BaseFragment implements IXScrollViewListener, ITaskFinishListener
{

	private XScrollView					mScrollView;
	private View						mProgressView;
	private View						mErrorView;
	private View						mEmptyView;
	private ListView					mContentList;
	private RechargeDetailAdapter		mAdapter;
	private ListResult<RechargeDetail>	mListResult;
	private boolean						mIsInit;

	// private int mCurPage = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_recharge, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		initView();
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onSelected()
	{
		if (isDestroyed()) {
			return;
		}
		if (!mIsInit) {
			requestData(mAdapter.getCurrentPage());
			mIsInit = true;
		}
	}

	@Override
	public void onRefresh()
	{

	}

	@Override
	public void onLoadMore()
	{
		if (!HttpUtil.isConnected(getActivity())) {
			shortToast(R.string.network_unconnected);
			mScrollView.stopLoadMore();
			return;
		}

		if (mAdapter.IsAdding()) {
			return;
		}

		// if (mAdapter.hasMore()) {
		if (mListResult != null && mListResult.getHasNext() == 1) {
			requestData(mAdapter.getCurrentPage() + 1);
		}
	}

	private void requestData(int page)
	{
		if (page == 1) {
			if (HttpUtil.isConnected(getActivity())) {
				mScrollView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.GONE);
				mProgressView.setVisibility(View.VISIBLE);
			} else {
				mScrollView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
				mProgressView.setVisibility(View.GONE);
				return;
			}
		} else {
			mAdapter.setAdding(true);
		}

		String url = String.format(Locale.CHINA, ConstantData.URL_CONSUME, page, "vpay_list");
		url = ConstantData.addLoginInfoToUrl(url);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		RequestTask task = new RequestTask(new RechargeDetailParser());
		task.setTaskFinishListener(this);
		task.setExtra(page);
		task.execute(params);
	}

	private void initView()
	{
		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}

		mProgressView = root.findViewById(R.id.progress_view);
		mEmptyView = root.findViewById(R.id.empty_view);
		mErrorView = root.findViewById(R.id.error_view);
		mErrorView.findViewById(R.id.retry_btn).setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				requestData(mAdapter.getCurrentPage());
			}
		});
		mErrorView.findViewById(R.id.net_set_btn).setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				getActivity().startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
			}
		});
		mScrollView = (XScrollView) root.findViewById(R.id.scroll_view);
		mScrollView.setPullRefreshEnable(false);
		mScrollView.setPullLoadEnable(true);
		mScrollView.setIXScrollViewListener(this);

		View content = LayoutInflater.from(getActivity()).inflate(R.layout.vw_consume_layout, null);
		if (null != content) {
			// TODO 有毛病啊 这里直接VISIBLE的控件 在xml里隐藏干啥 没有任何逻辑
			// content.findViewById(R.id.recharge_tip).setVisibility(View.VISIBLE);
			mContentList = (ListView) content.findViewById(R.id.consume_content);
			mAdapter = new RechargeDetailAdapter(getActivity());
			mContentList.setEmptyView(mEmptyView);
			mContentList.setAdapter(mAdapter);
		}
		mScrollView.setView(content);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTaskFinished(TaskResult taskResult)
	{
		RequestTask task = (RequestTask) taskResult.task;
		int page = (Integer) task.getExtra();
		Object result = taskResult.retObj;
		// 停止加载更多
		mScrollView.stopLoadMore();
		mProgressView.setVisibility(View.GONE);
		if (mAdapter.IsAdding()) {
			mAdapter.setAdding(false);
		}

		if (result instanceof ListResult<?>) {
			// mCurPage = page;
			mAdapter.setCurrentPage(page);
			ListResult<RechargeDetail> listResult = (ListResult<RechargeDetail>) result;
			mListResult = listResult;
			mAdapter.setTotalAndPerpage(listResult.getTotalNum(), 10);
			if (mAdapter.getCurrentPage() == 1) {
				mAdapter.setList(listResult.getList());
			} else {
				mAdapter.addList(listResult.getList());
			}
			mAdapter.notifyDataSetChanged();
			Util.measureListViewHeight(mContentList);

			// if (!mAdapter.hasMore()) {
			if (listResult.getHasNext() == 0) {
				mScrollView.setPullLoadEnable(false);
			} else {
				mScrollView.setPullLoadEnable(true);
			}
			mErrorView.setVisibility(View.GONE);
			mScrollView.setVisibility(View.VISIBLE);
			return;
		}

		if (page == 1) {
			mErrorView.setVisibility(View.VISIBLE);
			mScrollView.setVisibility(View.GONE);
		}
	}
}
