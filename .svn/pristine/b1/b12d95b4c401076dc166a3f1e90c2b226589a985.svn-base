package com.sina.book.ui;

import java.util.ArrayList;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.CardPostion;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PartitionDetailTopResult;
import com.sina.book.parser.PartitionDetailTopParser;
import com.sina.book.ui.adapter.CommonListAdapter;
import com.sina.book.ui.adapter.PartitionDetailListAdapter;
import com.sina.book.ui.widget.ListenableScrollView;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.Util;

/**
 * 分类详情
 * 
 * @author Tsimle
 * 
 */
public class PartitionDetailActivity extends CustomTitleActivity implements ITaskFinishListener {
	public static final String TYPE_FREE = "free";
	public static final String TYPE_NEW = "new";
	public static final String TYPE_RANK = "top";

	private final int INDEX_RANK = 0;
	private final int INDEX_NEW = 1;
	private final int INDEX_FREE = 2;

	/**
	 * 分类id
	 */
	private String mCate = "";
	/**
	 * 分类名称
	 */
	private String mCateName = "";

	private View mProgressView;
	private View mErrorView;

	private ListenableScrollView mTotalScroll;

	/** 以下为快速tab相关变量. */
	private View mQuickTab;
	private TextView mQuickNew;
	private TextView mQuickRank;
	private TextView mQuickFree;

	/** 最新. */
	private View mNew;
	private TextView mNewTotal;
	private View mNewMoreBtn;
	private ListView mListViewNew;
	private PartitionDetailListAdapter mNewAdapter;

	/** 排行. */
	private View mRank;
	private TextView mRankTotal;
	private View mRankMoreBtn;
	private ListView mListViewRank;
	private PartitionDetailListAdapter mRankAdapter;

	/** 免费. */
	private View mFree;
	private TextView mFreeTotal;
	private ListView mListFree;
	private View mFreeMoreBtn;
	private PartitionDetailListAdapter mFreeAdapter;

	private View mCardTip;
	private TextView mCardTitle;

	private RequestTask mReqTask;

	private int mSelectTab = INDEX_NEW;
	private Runnable mScrollRunnable = new Runnable() {
		public void run() {
			int y = 0;

			switch (mSelectTab) {
			case INDEX_NEW:
				y = mNew.getTop() - PixelUtil.dp2px(105);
				break;

			case INDEX_RANK:
				y = mRank.getTop();
				break;

			case INDEX_FREE:
				y = mFree.getTop() - PixelUtil.dp2px(105);
				break;
			default:
				break;
			}

			mTotalScroll.smoothScrollTo(0, y);
		};
	};

	/**
	 * 每一个card的头部需要
	 */
	private ArrayList<CardPostion> cardPostions = new ArrayList<CardPostion>();

	public static void launch(Context context, String cate, String cateName, int initIndex) {
//		Intent intent = new Intent();
//		intent.setClass(context, PartitionDetailActivity.class);
//		intent.putExtra("cate", cate);
//		intent.putExtra("cate_name", cateName);
//		context.startActivity(intent);
		launchMore(context, TYPE_RANK, cate, cateName);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_partition_detail);
		initIntent();
		initTitle();
		initViews();
		reqData();
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		mProgressView.setVisibility(View.GONE);

		if (taskResult.stateCode == HttpStatus.SC_OK && taskResult.retObj instanceof PartitionDetailTopResult) {
			PartitionDetailTopResult result = (PartitionDetailTopResult) taskResult.retObj;
			ArrayList<Book> freeBooks = result.getFreeBooks();
			int freeBooksTotal = result.getFreeBooksTotal();
			mFreeAdapter.setList(freeBooks);
			mFreeAdapter.setTotal(freeBooks.size());
			mFreeTotal.setText(String.format(getString(R.string.recommend_book_count), freeBooksTotal));
			mFreeAdapter.notifyDataSetChanged();
			Util.measureListViewHeight(mListFree);

			ArrayList<Book> newBooks = result.getNewBooks();
			int newBooksTotal = result.getNewBooksTotal();
			mNewAdapter.setList(newBooks);
			mNewAdapter.setTotal(newBooks.size());
			mNewTotal.setText(String.format(getString(R.string.recommend_book_count), newBooksTotal));
			mNewAdapter.notifyDataSetChanged();
			Util.measureListViewHeight(mListViewNew);

			ArrayList<Book> rankBooks = result.getRankBooks();
			int rankBooksTotal = result.getRankBooksTotal();
			mRankAdapter.setList(rankBooks);
			mRankAdapter.setTotal(rankBooks.size());
			mRankTotal.setText(String.format(getString(R.string.recommend_book_count), rankBooksTotal));
			mRankAdapter.notifyDataSetChanged();
			Util.measureListViewHeight(mListViewRank);

			mTotalScroll.setVisibility(View.VISIBLE);
			return;
		}

		mErrorView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void retry() {
		mErrorView.setVisibility(View.GONE);
		reqData();
	}

	@Override
	protected void onDestroy() {
		cancelTasks();
		super.onDestroy();
	}

	private void initIntent() {
		Intent intent = getIntent();
		mCate = intent.getStringExtra("cate");
		mCateName = intent.getStringExtra("cate_name");
	}

	/**
	 * 初始化标题
	 */
	private void initTitle() {
		TextView middleV = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (mCateName != null) {
			middleV.setText(mCateName);
		} else {
			middleV.setText(R.string.partition);
		}
		setTitleMiddle(middleV);

		View leftV = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(leftV);
	}

	private void initViews() {
		mTotalScroll = (ListenableScrollView) findViewById(R.id.total_scroll);
		mTotalScroll.setOnScrollListener(new ListenableScrollView.OnScrollListener() {

			@Override
			public void onScroll(int postion) {
				ensureCardPostions();
				CardPostion finded = null;
				for (CardPostion cp : cardPostions) {
					if (cp.inRange(postion)) {
						finded = cp;
						break;
					}
				}

				if (finded != null) {
					final String origntitle = mCardTitle.getText().toString();
					final String findtitle = finded.getTitle();
					if (!origntitle.equals(findtitle)) {
						Animation cardVisible = AnimationUtils.loadAnimation(mContext, R.anim.card_title_fade_in);
						cardVisible.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {

							}

							@Override
							public void onAnimationRepeat(Animation animation) {

							}

							@Override
							public void onAnimationEnd(Animation animation) {
								mCardTip.setVisibility(View.VISIBLE);
								mCardTitle.setText(findtitle);
							}
						});
						mCardTip.clearAnimation();
						mCardTip.setAnimation(cardVisible);
					}
				} else {
					mCardTip.clearAnimation();
					mCardTitle.setText("");
					mCardTip.setVisibility(View.GONE);
				}
			}
		});

		// quicktab
		mQuickTab = findViewById(R.id.quick_tab);
		mQuickNew = (TextView) mQuickTab.findViewById(R.id.partition_quick_new);
		mQuickRank = (TextView) mQuickTab.findViewById(R.id.partition_quick_rank);
		mQuickFree = (TextView) mQuickTab.findViewById(R.id.partition_quick_free);
		ScrollClickListener scrollClickListener = new ScrollClickListener();
		mQuickNew.setOnClickListener(scrollClickListener);
		mQuickRank.setOnClickListener(scrollClickListener);
		mQuickFree.setOnClickListener(scrollClickListener);

		// 排行
		mRank = findViewById(R.id.layout_rank);
		mRankTotal = (TextView) mRank.findViewById(R.id.cate_more);
		((TextView) mRank.findViewById(R.id.cate_title)).setText(R.string.rank);
		mListViewRank = (ListView) mRank.findViewById(R.id.cate_list);
		mRankMoreBtn = mRank.findViewById(R.id.recommend_all_btn);
		mRankAdapter = new PartitionDetailListAdapter(mContext, "排行", mCateName);
		mListViewRank.setAdapter(mRankAdapter);
		mRankAdapter.setActionType(Constants.CLICK_CLASSIFIED_RANK);
		OnClickListener rankMoreListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				launchMore(TYPE_RANK);
			}
		};
		mRank.findViewById(R.id.recommend_list_title_layout).setOnClickListener(rankMoreListener);
		mRankMoreBtn.setOnClickListener(rankMoreListener);

		// 最新
		mNew = findViewById(R.id.layout_new);
		mNewTotal = (TextView) mNew.findViewById(R.id.cate_more);
		((TextView) mNew.findViewById(R.id.cate_title)).setText(R.string.latest);
		mListViewNew = (ListView) mNew.findViewById(R.id.cate_list);
		mNewMoreBtn = mNew.findViewById(R.id.recommend_all_btn);
		mNewAdapter = new PartitionDetailListAdapter(mContext, "最新", mCateName);
		mListViewNew.setAdapter(mNewAdapter);
		mNewAdapter.setActionType(Constants.CLICK_CLASSIFIED_NEW);
		OnClickListener newMoreListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				launchMore(TYPE_NEW);
			}
		};
		mNew.findViewById(R.id.recommend_list_title_layout).setOnClickListener(newMoreListener);
		mNewMoreBtn.setOnClickListener(newMoreListener);

		// 免费
		mFree = findViewById(R.id.layout_free);
		mFreeTotal = (TextView) mFree.findViewById(R.id.cate_more);
		((TextView) mFree.findViewById(R.id.cate_title)).setText(R.string.free);
		mListFree = (ListView) mFree.findViewById(R.id.cate_list);
		mFreeMoreBtn = mFree.findViewById(R.id.recommend_all_btn);
		mFreeAdapter = new PartitionDetailListAdapter(mContext, "免费", mCateName);
		mListFree.setAdapter(mFreeAdapter);
		mFreeAdapter.setActionType(Constants.CLICK_CLASSIFIED_FREE);
		OnClickListener freeMoreListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				launchMore(TYPE_FREE);
			}
		};
		mFree.findViewById(R.id.recommend_list_title_layout).setOnClickListener(freeMoreListener);
		mFreeMoreBtn.setOnClickListener(freeMoreListener);

		mProgressView = findViewById(R.id.rl_progress);
		mErrorView = findViewById(R.id.error_layout);

		mCardTip = findViewById(R.id.card_tip);
		mCardTitle = (TextView) mCardTip.findViewById(R.id.card_title);
	}

	private void reqData() {
		String reqUrl = String.format(ConstantData.URL_PARTITION_TOP, mCate);

		cancelTasks();

		mReqTask = new RequestTask(new PartitionDetailTopParser());
		mReqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		mReqTask.execute(params);

		mProgressView.setVisibility(View.VISIBLE);
		mErrorView.setVisibility(View.GONE);
		mTotalScroll.setVisibility(View.GONE);
	}

	private void launchMore(String type) {
		String name = "";
		if (TYPE_FREE.equals(type)) {
			name = mContext.getString(R.string.free);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_FREE_MORE);
		} else if (TYPE_NEW.equals(type)) {
			name = mContext.getString(R.string.latest);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_NEW_MORE);
		} else if (TYPE_RANK.equals(type)) {
			name = mContext.getString(R.string.rank);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_RANK_MORE);
		}

		String url = String.format(ConstantData.URL_PARTITION_DATA, mCate, "%s", ConstantData.PAGE_SIZE, type);
		CommonListActivity.launch(mContext, url, mCateName + "-" + name, "分类" + name, CommonListAdapter.TYPE_PARTITION);
	}

	private static void launchMore(Context context, String type, String cate, String cateName) {
		String name = "";
		if (TYPE_FREE.equals(type)) {
			name = context.getString(R.string.free);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_FREE_MORE);
		} else if (TYPE_NEW.equals(type)) {
			name = context.getString(R.string.latest);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_NEW_MORE);
		} else if (TYPE_RANK.equals(type)) {
			name = context.getString(R.string.rank);

			UserActionManager.getInstance().recordEvent(Constants.CLICK_RANK_MORE);
		}

		String url = String.format(ConstantData.URL_PARTITION_DATA, cate, "%s", ConstantData.PAGE_SIZE, type);
		CommonListActivity.launch(context, url, cateName, "分类" + name, CommonListAdapter.TYPE_PARTITION);
	}

	private void cancelTasks() {
		if (mReqTask != null) {
			mReqTask.abort();
		}
	}

	private class ScrollClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.partition_quick_new:
				mSelectTab = INDEX_NEW;
				break;

			case R.id.partition_quick_rank:
				mSelectTab = INDEX_RANK;
				break;

			case R.id.partition_quick_free:
				mSelectTab = INDEX_FREE;
				break;
			default:
				break;
			}

			mHandler.removeCallbacks(mScrollRunnable);
			mHandler.post(mScrollRunnable);
		}

	}

	/**
	 * 确保cardPostions位置被初始化
	 */
	private void ensureCardPostions() {
		if (cardPostions.size() > 0) {
			return;
		}
		// card里的layout修改对应修改这里
		int topInterval = PixelUtil.dp2px(23);
		int bottomInterval = PixelUtil.dp2px(43);

		CardPostion cpNew = new CardPostion(mNew.getTop() + topInterval, mNew.getBottom() - bottomInterval, getString(R.string.latest));
		cardPostions.add(cpNew);

		CardPostion cpRank = new CardPostion(mRank.getTop() + topInterval, mRank.getBottom() - bottomInterval, getString(R.string.rank));
		cardPostions.add(cpRank);

		CardPostion cpFree = new CardPostion(mFree.getTop() + topInterval, mFree.getBottom() - bottomInterval, getString(R.string.free));
		cardPostions.add(cpFree);
	}
}
