package com.sina.book.ui;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.TopicItem;
import com.sina.book.data.TopicResult;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.TopicParser;
import com.sina.book.ui.adapter.PartitionDetailListAdapter;
import com.sina.book.ui.adapter.TopicListAdapter;
import com.sina.book.ui.widget.LinearLayoutListView;
import com.sina.book.ui.widget.XScrollView;
import com.sina.book.useraction.Constants;
import com.sina.book.util.HttpUtil;

/**
 * 专题界面
 * 
 * @author MarkMjw
 * @date 13-12-9.
 */
public class TopicActivity extends CustomTitleActivity implements ITaskFinishListener, XScrollView.IXScrollViewListener {
	/** 最新专题. */
	public static final int TYPE_LATEST = 0x00;
	/** 往期专题. */
	public static final int TYPE_OLD = 0x01;

	private View mProgressView;
	private View mErrorView;

	private TextView mTitleTextView;
	private XScrollView mScrollView;
	private LinearLayout mContentLayout;
	private ImageView mImageView;
	private TextView mTextView;

	private String mTitle;
	private int mTid = -1;
	private int mType = TYPE_LATEST;

	private TopicListAdapter mAdapter;
	private LinearLayoutListView mListView;

	private final int FIRST_PAGE = 1;

	// private int mCurPage = 1;

	public static void launch(Context context, String title, int topicId, int showType) {
		Intent intent = new Intent();
		intent.setClass(context, TopicActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

		Bundle bundle = new Bundle();
		bundle.putString("title", title);
		bundle.putInt("tid", topicId);
		bundle.putInt("type", showType);
		intent.putExtras(bundle);

		context.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_topic);

		parseIntent();
		initTitle();
		initViews();
		reqData(1);
	}

	@Override
	protected void retry() {
		reqData(1);
	}

	@Override
	public void onClickLeft() {
		finish();
		super.onClickLeft();
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mScrollView.stopLoadMore();
			mAdapter.setAdding(false);
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
	public void onTaskFinished(TaskResult taskResult) {
		RequestTask task = (RequestTask) taskResult.task;
		int mCurPage = (Integer) task.getExtra();
		mAdapter.setCurrentPage(mCurPage);
		if (FIRST_PAGE != mCurPage) {
			// 停止加载更多
			mScrollView.stopLoadMore();

			if (null != taskResult.retObj) {
				TopicResult result = (TopicResult) taskResult.retObj;
				loadMoreTopic(result);
			} else {
				if (mAdapter.IsAdding()) {
					mAdapter.setAdding(false);
				}
				shortToast(R.string.bookhome_no_more_data);
			}

		} else {
			if (null != taskResult.retObj) {
				TopicResult result = (TopicResult) taskResult.retObj;
				updateData(result);

				mScrollView.setVisibility(View.VISIBLE);
				mProgressView.setVisibility(View.GONE);
			} else {
				mProgressView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
			}
		}
	}

	private void parseIntent() {
		Intent intent = getIntent();
		if (null != intent) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				mTitle = bundle.getString("title");
				mTid = bundle.getInt("tid", 0);
				mType = bundle.getInt("type", TYPE_LATEST);
			}
		}
	}

	private void initTitle() {
		mTitleTextView = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (null != mTitleTextView) {
			mTitleTextView.setText(TextUtils.isEmpty(mTitle) ? getString(R.string.topic_title) : mTitle);
			setTitleMiddle(mTitleTextView);
		}

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);
	}

	private void initViews() {
		mProgressView = findViewById(R.id.topic_progress);
		mErrorView = findViewById(R.id.topic_error);

		mScrollView = (XScrollView) findViewById(R.id.topic_scroll_view);
		mScrollView.setIXScrollViewListener(this);
		mScrollView.setPullRefreshEnable(false);
		mScrollView.setPullLoadEnable(false);

		View content = LayoutInflater.from(this).inflate(R.layout.vw_topic_detail_layout, null);
		if (null != content) {
			mImageView = (ImageView) content.findViewById(R.id.topic_image);
			mTextView = (TextView) content.findViewById(R.id.topic_text);
			mContentLayout = (LinearLayout) content.findViewById(R.id.topic_books);

			mListView = (LinearLayoutListView) content.findViewById(R.id.topic_list);
			mAdapter = new TopicListAdapter(this);
			mListView.setAdapter(mAdapter);
		}
		mScrollView.setView(content);
	}

	private final int PAGE_SIZE = 10;

	private void reqData(int page) {
		if (FIRST_PAGE == page) {
			if (!HttpUtil.isConnected(this) || mTid < 0) {
				mProgressView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
				mScrollView.setVisibility(View.GONE);
			} else {
				mProgressView.setVisibility(View.VISIBLE);
				mErrorView.setVisibility(View.GONE);
				mScrollView.setVisibility(View.GONE);
			}
		} else {
			mAdapter.setAdding(true);
		}

		String reqUrl;
		if (TYPE_LATEST == mType) {
			// 最新一期专题url
			reqUrl = String.format(Locale.CHINA, ConstantData.URL_TOPIC_LIST, mTid, page, PAGE_SIZE);
		} else {
			// 往期专题url
			reqUrl = String.format(Locale.CHINA, ConstantData.URL_TOPIC, mTid);
		}

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

		RequestTask task = new RequestTask(new TopicParser());
		task.setTaskFinishListener(this);
		task.setExtra(page);
		task.execute(params);
	}

	private void updateData(TopicResult result) {
		// 专题详情
		TopicItem topic = result.getTopic();
		if (null != topic) {
			mTitleTextView.setText(topic.getTitle());
			mTextView.setText(topic.getIntro());
			ImageLoader.getInstance().load3(topic.getImgUrl(), mImageView, ImageLoader.TYPE_BIG_PIC,
					ImageLoader.getDefaultHorizontalBannerPic());
			loadTopicBooks(topic);
		}

		loadTopics(result);
	}

	/**
	 * 推荐书籍
	 * 
	 * @param topic
	 */
	private void loadTopicBooks(TopicItem topic) {
		List<TopicItem.TopicBook> books = topic.getTopicBooks();
		if (null != books && !books.isEmpty()) {
			for (TopicItem.TopicBook book : books) {
				LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.vw_topic_books_card,
						new LinearLayout(this));
				if (null != layout) {
					List<Book> data = book.getBooks();
					// 如果没有书籍则直接返回
					if (null == data || data.isEmpty())
						return;

					// 书籍分组名
					String name = book.getName();
					TextView title = (TextView) layout.findViewById(R.id.list_title);
					if (!TextUtils.isEmpty(name)) {
						title.setText(name);
						title.setVisibility(View.VISIBLE);
					} else {
						title.setVisibility(View.GONE);
					}

					// 书籍列表
					PartitionDetailListAdapter adapter = new PartitionDetailListAdapter(this, name, mTitle);
					adapter.setList(data);
					adapter.setTotal(data.size());
					adapter.setActionType(Constants.CLICK_RECOMMAND_TOPIC);
					LinearLayoutListView listView = (LinearLayoutListView) layout.findViewById(R.id.list_books);
					listView.setAdapter(adapter);

					mContentLayout.addView(layout);
					mContentLayout.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	/**
	 * 加载往期专题列表
	 * 
	 * @param result
	 */
	private void loadTopics(TopicResult result) {
		List<TopicItem> topics = result.getTopics();
		if (null != topics && !topics.isEmpty()) {
			mAdapter.setList(topics);
			mAdapter.setTotalAndPerpage(result.getTotal() - 1, PAGE_SIZE);
			mAdapter.notifyDataSetChanged();
			mListView.setVisibility(View.VISIBLE);
			mListView.notifyDataSetChanged();

			mScrollView.setPullLoadEnable(mAdapter.hasMore());
		} else {
			mListView.setVisibility(View.GONE);
		}
	}

	/**
	 * 加载更多往期专题列表
	 * 
	 * @param result
	 */
	private void loadMoreTopic(TopicResult result) {
		List<TopicItem> topics = result.getTopics();
		if (null != topics && !topics.isEmpty()) {
			if (mAdapter.IsAdding()) {
				mAdapter.setAdding(false);
				mAdapter.addList(topics);
				mAdapter.setTotal(result.getTotal() - 1);
			} else {
				mAdapter.setList(topics);
				mAdapter.setTotal(result.getTotal());
			}
			mListView.notifyDataSetChanged();
		}

		mScrollView.setPullLoadEnable(mAdapter.hasMore());
	}
}
