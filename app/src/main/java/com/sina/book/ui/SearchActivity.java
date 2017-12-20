package com.sina.book.ui;

import java.util.ArrayList;

import org.apache.http.HttpStatus;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.sina.book.R;
import com.sina.book.control.ITaskCacheLoadedListener;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.SearchData;
import com.sina.book.parser.SearchParser;
import com.sina.book.ui.adapter.CommonListAdapter;
import com.sina.book.ui.adapter.SearchAdapter;
import com.sina.book.ui.widget.FreeTextListView;
import com.sina.book.util.Util;

/**
 * 搜索页面
 * 
 * @author Tsimle
 * 
 */
public class SearchActivity extends CustomTitleActivity implements ITaskFinishListener, OnItemClickListener,
		com.sina.book.ui.widget.FreeTextListView.OnItemClickListener, ITaskCacheLoadedListener, OnClickListener {

	/**
	 * 搜索框
	 */
	private EditText mKeyEdt;
	private Button mSearchBtn;
	private ImageButton mClearSearchBtn;

	/**
	 * 搜索推荐
	 */
	private RelativeLayout mHotBookLayout;
	private TextView mHotBookName;
	private Book mHotBook;

	/**
	 * 热词
	 */
	private View mHotWordsRoot;
	private FreeTextListView mHotWordsListView;
	private RelativeLayout mMoreHotWordsBtn;
	private ArrayList<String> mHotWords;

	/**
	 * 热门搜索
	 */
	private LinearLayout mHotSearchBooksLayout;
	private ListView mHotBooksListView;
	private SearchAdapter mHotBooksAdapter;
	private ArrayList<Book> mHotBooks;
	private RelativeLayout mMoreHotBooksBtn;

	private SearchData mData;

	private View mProgressView;
	private View mErrorView;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_search);
		initTitle();
		initViews();
		setListener();
		reqSearch();
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	@Override
	protected void retry() {
		mErrorView.setVisibility(View.GONE);
		reqSearch();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == 0 && data != null) {
			String key = data.getStringExtra("key");
			mKeyEdt.setText(key);
			mKeyEdt.setSelection(key.length());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		if (position >= 0 && position < mHotBooks.size()) {
			String eventKey = "发现_热门搜索_" + Util.formatNumber(position + 1);
			BookDetailActivity.launchNew(mContext, mHotBooks.get(position), eventKey);
		}
	}

	@Override
	public void onItemClick(int posiont) {
		if (posiont >= 0 && posiont < mHotWords.size()) {
			SearchResultActivity.launch(SearchActivity.this, mHotWords.get(posiont));
		}
	}

	@Override
	public void onTaskCacheLoaded(Object result) {
		mData = (SearchData) result;

		mHotBook = mData.getBook();
		mHotWords = mData.getHotWords();
		mHotBooks = mData.getHotBooks();

		updateHotBook(mHotBook);
		updateHotWords(mHotWords);
		updateHotBooks(mHotBooks);
		mProgressView.setVisibility(View.GONE);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof SearchData) {
				mData = (SearchData) taskResult.retObj;

				mHotBook = mData.getBook();
				mHotWords = mData.getHotWords();
				mHotBooks = mData.getHotBooks();

				updateHotBook(mHotBook);
				updateHotWords(mHotWords);
				updateHotBooks(mHotBooks);
			} else {// 网络错误
				if (!taskResult.cacheAlreadySuccess) {
					mErrorView.setVisibility(View.VISIBLE);
				}
			}
		} else {// 网络错误
			if (!taskResult.cacheAlreadySuccess) {
				mErrorView.setVisibility(View.VISIBLE);
			}
		}
		mProgressView.setVisibility(View.GONE);
	}

	private void updateHotBook(Book book) {
		if (null == book) {
			mHotBookLayout.setVisibility(View.GONE);
			return;
		}
		mHotBookLayout.setVisibility(View.VISIBLE);
		mHotBookName.setText(book.getTitle());
	}

	private void updateHotWords(ArrayList<String> hotWords) {
		if (null == hotWords || hotWords.size() == 0) {
			mHotWordsRoot.setVisibility(View.GONE);
			return;
		}
		mHotWordsRoot.setVisibility(View.VISIBLE);
		mHotWordsListView.setString(hotWords);
		mHotWordsListView.notifyChanged();
	}

	private void updateHotBooks(ArrayList<Book> hotBooks) {
		if (null == hotBooks) {
			mHotSearchBooksLayout.setVisibility(View.GONE);
			mMoreHotBooksBtn.setVisibility(View.GONE);
			return;
		}

		mHotSearchBooksLayout.setVisibility(View.VISIBLE);
		mMoreHotBooksBtn.setVisibility(View.VISIBLE);
		mHotBooksAdapter.setList(hotBooks);
		mHotBooksAdapter.notifyDataSetChanged();
	}

	private void initTitle() {
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		TextView middleTv = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middleTv.setText(R.string.search);
		setTitleMiddle(middleTv);
	}

	private void initViews() {

		mHotBookLayout = (RelativeLayout) findViewById(R.id.search_hot_book_layout);
		mHotBookName = (TextView) findViewById(R.id.search_hot_book);

		mHotWordsRoot = findViewById(R.id.search_hot_words_root);
		mHotWordsListView = (FreeTextListView) findViewById(R.id.search_hot_words_listview);
		mMoreHotWordsBtn = (RelativeLayout) findViewById(R.id.search_hot_words_all_btn);

		mHotSearchBooksLayout=(LinearLayout) findViewById(R.id.search_hot_search_books);
		mMoreHotBooksBtn = (RelativeLayout) findViewById(R.id.search_hot_books_all_btn);
		mHotBooksListView = (ListView) findViewById(R.id.search_hot_book_listview);
		mHotBooksAdapter = new SearchAdapter(this);
		mHotBooksListView.setAdapter(mHotBooksAdapter);
		mHotBooksListView.setOnItemClickListener(this);

		mKeyEdt = (EditText) findViewById(R.id.et_key);
		mSearchBtn = (Button) findViewById(R.id.btn_search);
		mClearSearchBtn = (ImageButton) findViewById(R.id.btn_clear_search);

		mProgressView = findViewById(R.id.progress_layout);
		mErrorView = findViewById(R.id.error_layout);
	}

	private void setListener() {
		mHotBookLayout.setOnClickListener(this);
		mMoreHotWordsBtn.setOnClickListener(this);
		mMoreHotBooksBtn.setOnClickListener(this);
		mSearchBtn.setOnClickListener(this);
		mClearSearchBtn.setOnClickListener(this);

		mHotWordsListView.setOnItemClickListener(this);

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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_hot_book_layout:
			// 获取事件Key
			String eventKey = "发现_搜索推荐_01";
			if (mData != null && TextUtils.isEmpty(mData.getRecommandName())) {
				eventKey = "发现_" + mData.getRecommandName() + "_01";
			}
			BookDetailActivity.launchNew(mContext, mHotBook, eventKey);
			break;

		case R.id.search_hot_words_all_btn:
			Intent intent = new Intent();
			intent.setClass(SearchActivity.this, HotWordsActivity.class);
			startActivity(intent);
			break;

		case R.id.search_hot_books_all_btn:
			if (null == mData) {
				return;
			}
			String type = mData.getBookType();
			String url = String.format(ConstantData.URL_CHILD_SELL_FAST_NEW, type, "%s", ConstantData.PAGE_SIZE);
			CommonListActivity.launch(mContext, url, mData.getBookTypeName(), CommonListAdapter.TYPE_SEARCH);
			break;

		case R.id.btn_search:
			search();
			break;

		case R.id.btn_clear_search:
			mKeyEdt.setText("");
			break;

		default:
			break;
		}
	}

	/**
	 * 搜索
	 */
	private void search() {
		String key = mKeyEdt.getText().toString().trim();
		if (TextUtils.isEmpty(key)) {
			shortToast(R.string.search_key_null);
			return;
		}

		Intent intent = new Intent(mContext, SearchResultActivity.class);
		intent.putExtra(SearchResultActivity.KEY_SEARCH_TEXT, key);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivityForResult(intent, 0);

		Util.hideSoftInput(this, mKeyEdt);
	}

	private void reqSearch() {
		RequestTask reqTask = new RequestTask(new SearchParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_SEARCH_INDEX);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.executeWitchCache(params, this);

		mProgressView.setVisibility(View.VISIBLE);
	}
}
