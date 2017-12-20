package com.sina.book.ui;

import org.apache.http.HttpStatus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.CommentsResult;
import com.sina.book.data.ConstantData;
import com.sina.book.parser.CommentsParser;
import com.sina.book.ui.adapter.CommentListAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;

public class CommentListActivity extends CustomTitleActivity implements OnItemClickListener, ITaskFinishListener,
		IXListViewListener {
	private static final String EXTRA_PAGE = "page";

	// private int mPage = 1;

	private XListView mListView;
	private View mProgressView;
	private View mErrorView;
	private CommentListAdapter mAdapter;
	private View mNoComments;
	private EditText mAddCommentEdt;
	private Book mBook;
	private View mAddCommentLayout;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_comments_list);
		initIntent();
		initTitle();
		initViews();
		reqComments(mAdapter.getCurrentPage());

		UserActionManager.getInstance().recordEvent(Constants.PAGE_COMMENT);
	}

	@Override
	public void onClickLeft() {
		finish();
		super.onClickLeft();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			RequestTask task = (RequestTask) taskResult.task;
			int page = (Integer) task.getParams().get(EXTRA_PAGE);
			if (taskResult.retObj instanceof CommentsResult) {
				CommentsResult result = (CommentsResult) taskResult.retObj;
				// mPage = page;
				mAdapter.setCurrentPage(page);
				mAdapter.setTotal(result.getTotal());
				mAdapter.addList(result.getItems());
				if (!mAdapter.hasMore()) {
					mListView.setPullLoadEnable(false);
				} else {
					mListView.setPullLoadEnable(true);
				}
				if (mAdapter.getDataSize() <= 0) {
					mNoComments.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				} else {
					mListView.setVisibility(View.VISIBLE);
					mNoComments.setVisibility(View.GONE);
					mAdapter.notifyDataSetChanged();
				}
			} else {// 网络错误
				if (mAdapter.getDataSize() == 0) {
					mListView.setVisibility(View.GONE);
					mErrorView.setVisibility(View.VISIBLE);
				} else {
					shortToast(R.string.network_unconnected);
				}
			}
		} else {// 网络错误
			if (mAdapter.getDataSize() == 0) {
				mListView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
			} else {
				shortToast(R.string.network_unconnected);
			}
		}

		if (mAdapter.IsAdding()) {
			mAdapter.setAdding(false);
		}
		mListView.stopLoadMore();
		mProgressView.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		arg2 -= mListView.getHeaderViewsCount();
		if (arg2 >= mAdapter.getDataSize()) {
			if (mAdapter.IsAdding()) {
				return;
			}
			mAdapter.setAdding(true);
			mAdapter.notifyDataSetChanged();
			reqComments(mAdapter.getCurrentPage() + 1);
		}

	}

	@Override
	protected void retry() {
		mErrorView.setVisibility(View.GONE);
		reqComments(mAdapter.getCurrentPage());
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
			reqComments(mAdapter.getCurrentPage() + 1);
		}
	}

	/**
	 * 初始化标题
	 */
	private void initTitle() {
		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middle.setText(R.string.comments);
		setTitleMiddle(middle);

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);
	}

	private void initViews() {
		mListView = (XListView) findViewById(R.id.lv_comments);
		mErrorView = findViewById(R.id.error_layout);
		mProgressView = findViewById(R.id.waitingLayout);
		mAdapter = new CommentListAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setXListViewListener(this);
		mListView.setPullRefreshEnable(false);
		mListView.setPullLoadEnable(true);
		mListView.setOnItemClickListener(this);
		mNoComments = findViewById(R.id.no_comments);

		mAddCommentLayout = (LinearLayout) findViewById(R.id.add_my_comment);

		mAddCommentEdt = (EditText) mAddCommentLayout.findViewById(R.id.edt_my_comments_content);
		mAddCommentEdt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(CommentListActivity.this, SendCommentsPostActivity.class);
				intent.putExtra("book", mBook);
				startActivity(intent);
			}
		});
	}

	/**
	 * 发送获取评论请求
	 */
	private void reqComments(int page) {
		String reqUrl = String.format(ConstantData.URL_COMMENTS, mBook.getBookId(), mBook.getSid(), mBook.getBookSrc(),
				page, ConstantData.PAGE_SIZE);
		RequestTask reqTask = new RequestTask(new CommentsParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		params.put(EXTRA_PAGE, page);
		reqTask.execute(params);
		if (page == 1) {
			mProgressView.setVisibility(View.VISIBLE);
		}
	}

	private void initIntent() {
		Intent intent = getIntent();
		mBook = (Book) intent.getSerializableExtra("book");
	}
}
