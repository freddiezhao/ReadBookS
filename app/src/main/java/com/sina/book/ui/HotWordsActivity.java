package com.sina.book.ui;

import java.util.Locale;

import org.apache.http.HttpStatus;

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
import com.sina.book.data.ConstantData;
import com.sina.book.data.HotWord;
import com.sina.book.data.HotWordsResult;
import com.sina.book.parser.HotWordsParser;
import com.sina.book.ui.adapter.HotWordsAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;

/**
 * 热词列表页
 * 
 * @author liujie
 * @date 2013-7-17
 */

public class HotWordsActivity extends CustomTitleActivity implements OnItemClickListener, ITaskFinishListener,
		IXListViewListener {

	private XListView mListView;
	private HotWordsAdapter mAdapter;
	private View mProgressView;
	private View mErrorView;

	// private int mCurPage = 1;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_hot_words_list);

		initTitle();
		initViews();
		initData();
	}

	@Override
	protected void retry() {
		initData();
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	/**
	 * 初始化标题
	 */
	private void initTitle() {
		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middle.setText(R.string.hot_words);
		setTitleMiddle(middle);

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

	}

	private void initViews() {
		mListView = (XListView) findViewById(R.id.lv_hotwords);
		mAdapter = new HotWordsAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setXListViewListener(this);
		mListView.setPullRefreshEnable(false);
		mListView.setPullLoadEnable(false);

		mProgressView = findViewById(R.id.rl_progress);
		mErrorView = findViewById(R.id.error_layout);
	}

	private void initData() {
		if (!HttpUtil.isConnected(this)) {
			mErrorView.setVisibility(View.VISIBLE);
		} else {
			mErrorView.setVisibility(View.GONE);
			mProgressView.setVisibility(View.VISIBLE);
			requsetData(1);
		}
	}

	private void requsetData(int page) {
		String url = String.format(Locale.CHINA, ConstantData.URL_HOT_WORD, page, ConstantData.PAGE_SIZE);
		RequestTask reqTask = new RequestTask(new HotWordsParser());
		reqTask.setTaskFinishListener(this);
		reqTask.setExtra(page);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {

		RequestTask task = (RequestTask) taskResult.task;
		int page = (Integer) task.getExtra();

		if (page == 1) {
			mProgressView.setVisibility(View.GONE);
		} else {
			mListView.stopLoadMore();
		}

		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			HotWordsResult result = (HotWordsResult) taskResult.retObj;

			if (result != null && result.getTotal() > 0) {
				// mCurPage = page;
				mAdapter.setCurrentPage(page);

				mAdapter.addList(result.getItems());
				mAdapter.setTotal(result.getTotal());

				if (mAdapter.hasMore()) {
					mListView.setPullLoadEnable(true);
				} else {
					mListView.setPullLoadEnable(false);
				}

				mAdapter.notifyDataSetChanged();
				return;
			}
		} else {
			if (page == 1) {
				mErrorView.setVisibility(View.VISIBLE);
			} else {
				shortToast(R.string.network_unconnected);
			}
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position -= mListView.getHeaderViewsCount();

		if (position >= 0 && position < mAdapter.getCount()) {
			HotWord hotWord = (HotWord) mAdapter.getItem(position);
			SearchResultActivity.launch(HotWordsActivity.this, hotWord.getName());
		}
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

		if (mAdapter.hasMore()) {
			requsetData(mAdapter.getCurrentPage() + 1);
		}
	}

}
