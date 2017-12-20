package com.sina.book.ui;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.AuthorPageResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.RecommendAuthorResult;
import com.sina.book.parser.AuthorRecommendParser;
import com.sina.book.ui.adapter.AuthorRecommendAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;

/**
 * 作家推荐
 * 
 * @author Tsimle
 * 
 */
public class AuthorRecommendActivity extends CustomTitleActivity implements ITaskFinishListener, OnItemClickListener,
		IXListViewListener {

	private XListView mListView;
	private View mProgressView;
	private View mErrorView;
	private AuthorRecommendAdapter mAdapter;

	// private int mCurPage = 1;

	public static void launch(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, AuthorRecommendActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_author_recommend);
		initView();
		initTitle();
		reqData(1);
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (mProgressView != null) {
			mProgressView.setVisibility(View.GONE);
		}

		RequestTask task = (RequestTask) taskResult.task;
		int page = (Integer) task.getExtra();
		if (taskResult.retObj != null && taskResult.retObj instanceof RecommendAuthorResult) {
			// mCurPage = page;
			mAdapter.setCurrentPage(page);
			RecommendAuthorResult result = (RecommendAuthorResult) taskResult.retObj;

			int total = result.getCount();
			List<AuthorPageResult> data = result.getDatas();

			if (mAdapter.IsAdding()) {
				mAdapter.setAdding(false);
				mAdapter.addList(data);
				mAdapter.setTotalAndPerpage(total, PAGE_SIZE);
			} else {
				mAdapter.setList(data);
				mAdapter.setTotalAndPerpage(total, PAGE_SIZE);
			}

			if (!mAdapter.hasMore()) {
				mListView.setPullLoadEnable(false);
			} else {
				mListView.setPullLoadEnable(true);
			}

			mAdapter.notifyDataSetChanged();
			return;

		} else {
			if (mAdapter.IsAdding()) {
				mAdapter.setAdding(false);
			}

			if (mAdapter.getDataSize() == 0) {
				mErrorView.setVisibility(View.VISIBLE);
			} else {
				shortToast(R.string.network_unconnected);
			}
		}
		mListView.stopLoadMore();
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mListView.stopLoadMore();
			return;
		}

		if (mAdapter.IsAdding()) {
			return;
		}

		if (mAdapter.hasMore()) {
			reqData(mAdapter.getCurrentPage() + 1);
		}
	}

	@Override
	protected void retry() {
		reqData(mAdapter.getCurrentPage());
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position -= mListView.getHeaderViewsCount();

		// 点击获取更多
		if (position >= mAdapter.getDataSize()) {
			if (mAdapter.IsAdding()) {
				return;
			}

			if (mAdapter.hasMore()) {
				reqData(mAdapter.getCurrentPage() + 1);
			}
		}
	}

	private void initView() {
		mListView = (XListView) findViewById(R.id.lv_authors);
		mListView.setOnItemClickListener(this);
		mListView.setPullRefreshEnable(false);
		mListView.setPullLoadEnable(false);
		mListView.setXListViewListener(this);
		mListView.setDivider(null);

		mErrorView = findViewById(R.id.error_layout);
		mProgressView = findViewById(R.id.rl_progress);
		mAdapter = new AuthorRecommendAdapter(mContext);
		mListView.setAdapter(mAdapter);
	}

	private void initTitle() {
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (null != middle) {
			middle.setText("热门作家");
			setTitleMiddle(middle);
		}
	}

	private final int PAGE_SIZE = 10;

	private void reqData(int page) {
		if (page == 1) {
			if (HttpUtil.isConnected(this)) {
				mErrorView.setVisibility(View.GONE);
				mProgressView.setVisibility(View.VISIBLE);
			} else {
				mErrorView.setVisibility(View.VISIBLE);
				mProgressView.setVisibility(View.GONE);
				return;
			}
		} else {
			mAdapter.setAdding(true);
			mAdapter.notifyDataSetChanged();
		}

		String url = String.format(Locale.CHINA, ConstantData.URL_AUTHOR_HOME, page, PAGE_SIZE);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

		RequestTask task = new RequestTask(new AuthorRecommendParser());
		task.setTaskFinishListener(this);
		task.setExtra(page);
		task.execute(params);
	}
}
