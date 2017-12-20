package com.sina.book.ui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.SearchResult;
import com.sina.book.parser.SearchResultParser;
import com.sina.book.ui.adapter.CommonListAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.Util;

/**
 * 书本搜索结果页面
 * 
 * @author Tsimle
 * 
 */
public class SearchResultActivity extends CustomTitleActivity implements ITaskFinishListener, OnItemClickListener,
		IXListViewListener {

	public static final String KEY_SEARCH_TEXT = "searchTxt";

	private EditText mKeyEdt;
	private Button mSearchBtn;
	private XListView mListView;
	private View mProgress;
	private View mErrorView;
	private ImageButton mClearSearchBtn;
	private CommonListAdapter mAdapter;
	private String mCurSearchKey = "";

	// private int mCurPage = 1;

	public static void launch(Context context, String text) {
		Intent intent = new Intent();
		intent.setClass(context, SearchResultActivity.class);
		intent.putExtra(KEY_SEARCH_TEXT, text);
		context.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_search_result);

		initTitle();
		initViews();
		getIntentData();
		reqSearch(1);
	}

	@Override
	public void onClickLeft() {
		Intent intent = new Intent();
		intent.putExtra("key", mKeyEdt.getText().toString());
		setResult(0, intent);
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position -= mListView.getHeaderViewsCount();

		// 点击获取更多
		if (position >= mAdapter.getDataSize()) {
			if (mAdapter.IsAdding()) {
				return;
			}
			mAdapter.setAdding(true);
			mAdapter.notifyDataSetChanged();
			reqSearch(mAdapter.getCurrentPage() + 1);
		} else if (position >= 0) {
			// 进入详情
			String eventKey = "发现_搜索_" + Util.formatNumber(position + 1);
			String eventExtra = mCurSearchKey;
			BookDetailActivity.launch(this, ((Book) mAdapter.getItem(position)), eventKey, eventExtra);
		}
	}

	@Override
	protected void onPause() {
		Util.hideSoftInput(this, mKeyEdt);
		super.onPause();
	}

	@Override
	protected void retry() {
		reqSearch(mAdapter.getCurrentPage());
		mErrorView.setVisibility(View.GONE);
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
			mAdapter.setAdding(true);
			mAdapter.notifyDataSetChanged();
			reqSearch(mAdapter.getCurrentPage() + 1);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTaskFinished(TaskResult taskResult) {
		RequestTask reqTask = (RequestTask) (taskResult.task);
		List<Object> datas = (List<Object>) reqTask.getExtra();
		int page = (Integer) datas.get(0);

		if (taskResult.stateCode == HttpStatus.SC_OK) {
			String searchKey = (String) datas.get(1);
			if (searchKey.equals(mCurSearchKey)) {
				if (taskResult.retObj instanceof SearchResult) {
					// mCurPage = page;
					mAdapter.setCurrentPage(page);
					SearchResult result = (SearchResult) taskResult.retObj;
					List<Book> retList = result.getItems();
					int total = result.getTotal();
					if (mAdapter.IsAdding()) {
						mAdapter.setAdding(false);
						mAdapter.addList(retList);
						mAdapter.setTotal(total);
					} else {
						mAdapter.setList(retList);
						mAdapter.setTotal(total);
					}
					if (!mAdapter.hasMore()) {
						mListView.setPullLoadEnable(false);
					} else {
						mListView.setPullLoadEnable(true);
					}
					mAdapter.notifyDataSetChanged();
					if (total == 0) {
						shortToast(R.string.search_data_null);
					}
				} else {// 网络错误
					if (page == 1) {
						mErrorView.setVisibility(View.VISIBLE);
					} else {
						shortToast(R.string.network_unconnected);
					}
				}
			}
		} else {// 网络错误
			if (page == 1) {
				mErrorView.setVisibility(View.VISIBLE);
			} else {
				shortToast(R.string.network_unconnected);
			}
		}
		mProgress.setVisibility(View.GONE);
		mKeyEdt.setFocusableInTouchMode(true);
		if (mAdapter.IsAdding()) {
			mAdapter.setAdding(false);
			mListView.stopLoadMore();
		}
	}

	private void initTitle() {
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		TextView middleTv = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middleTv.setText(R.string.search);
		setTitleMiddle(middleTv);
	}

	private void initViews() {
		mListView = (XListView) findViewById(R.id.lv_result);
		mAdapter = new CommonListAdapter(this, "");
		mListView.setAdapter(mAdapter);
		mListView.setXListViewListener(this);
		mListView.setPullRefreshEnable(false);
		mListView.setPullLoadEnable(true);
		mListView.setOnItemClickListener(this);

		mProgress = findViewById(R.id.waitingLayout);
		mErrorView = findViewById(R.id.error_layout);
		mKeyEdt = (EditText) findViewById(R.id.et_key);
		mKeyEdt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s == null || s.length() <= 0) {
					mClearSearchBtn.setVisibility(View.INVISIBLE);
				} else {
					mClearSearchBtn.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mKeyEdt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mKeyEdt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				switch (actionId) {
				case EditorInfo.IME_ACTION_UNSPECIFIED:
				case EditorInfo.IME_ACTION_SEARCH:
					if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
						search();
					}
					return true;
				default:
					return false;
				}
			}
		});
		mKeyEdt.setFocusableInTouchMode(false);
		mSearchBtn = (Button) findViewById(R.id.btn_search);
		mSearchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListView.setSelectionFromTop(0, 0);
				search();
			}
		});
		mClearSearchBtn = (ImageButton) findViewById(R.id.btn_clear_search);
		mClearSearchBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mKeyEdt.setText("");
				if (reqTask != null) {
					reqTask.setTaskFinishListener(null);
					reqTask.abort();
					reqTask = null;
				}
				mProgress.setVisibility(View.GONE);
				mKeyEdt.setFocusableInTouchMode(true);
				mListView.setPullLoadEnable(false);
			}
		});
	}

	private void getIntentData() {
		Intent i = getIntent();
		if (i != null) {
			mCurSearchKey = i.getStringExtra(KEY_SEARCH_TEXT);
			mKeyEdt.setText(mCurSearchKey);
			mKeyEdt.setSelection(mCurSearchKey.length());
		}
	}

	/**
	 * 取搜索框中信息，并调起搜索
	 */
	private void search() {
		String key = mKeyEdt.getText().toString();
		if (key == null || key.trim().equals("")) {
			shortToast(R.string.search_key_null);
			return;
		}

		mCurSearchKey = key;
		reqSearch(1);
	}

	private RequestTask reqTask;

	/**
	 * 发送搜索书本请求
	 */
	@SuppressWarnings("deprecation")
	private void reqSearch(int page) {
		if (reqTask != null) {
			reqTask.setTaskFinishListener(null);
			reqTask.abort();
			reqTask = null;
		}
		reqTask = new RequestTask(new SearchResultParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		String reqUrl = String.format(ConstantData.URL_SEARCH_KEY, URLEncoder.encode(mCurSearchKey), page,
				ConstantData.PAGE_SIZE);

		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		List<Object> datas = new ArrayList<Object>();
		datas.add(page);
		datas.add(mCurSearchKey);
		reqTask.setExtra(datas);
		reqTask.execute(params);
		if (page == 1) {
			if (mAdapter != null) {
				mAdapter.clearList();
			}
			mProgress.setVisibility(View.VISIBLE);
		}
		Util.hideSoftInput(this, mKeyEdt);
	}
}
