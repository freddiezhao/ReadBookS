package com.sina.book.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.RecommendCmsResult;
import com.sina.book.parser.RecommendCmsParser;
import com.sina.book.ui.adapter.RecommendCmsListAdapter;
import com.sina.book.ui.widget.CommonViewPager;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.Util;

/**
 * 推荐-男生女生出版界面
 * 
 * @author MarkMjw
 * @date 2013-8-29
 */
public class RecommendCmsActivity extends CustomTitleActivity implements ITaskFinishListener {
	// private static final String TAG = "RecommendCmsActivity";

	private static final String PARAM_URL = "Url";
	private static final String PARAM_TITLE = "Title";
	private static final String PARAM_TYPE = "Type";

	private View mProgressView;
	private View mErrorView;

	private ScrollView mScrollView;

	private View mTodayView;
	private ListView mListToday;

	private CommonViewPager mCateView;

	private String mTitle;
	private String mReqUrl;
	private String mType;

	/**
	 * 启动
	 * 
	 * @param c
	 *            上下文引用
	 * @param url
	 *            请求Url
	 * @param title
	 *            界面title
	 * @param type
	 *            数据类型
	 */
	public static void launch(Context c, String url, String title, String type) {
		Intent intent = new Intent();
		intent.setClass(c, RecommendCmsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.putExtra(PARAM_URL, url);
		intent.putExtra(PARAM_TITLE, title);
		intent.putExtra(PARAM_TYPE, type);

		c.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_recommend_cms);

		getDataIntent();
		initTitle();
		initViews();
		reqData();
	}

	@Override
	protected void retry() {
		reqData();
	}

	@Override
	public void onClickLeft() {
		finish();
		super.onClickLeft();
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (null != taskResult.retObj) {
			RecommendCmsResult result = (RecommendCmsResult) taskResult.retObj;
			updateTodayData(result);
			updateCateData(result);

			mScrollView.setVisibility(View.VISIBLE);
			mProgressView.setVisibility(View.GONE);
		} else {
			mProgressView.setVisibility(View.GONE);
			mErrorView.setVisibility(View.VISIBLE);
		}
	}

	private void getDataIntent() {
		Intent intent = getIntent();
		mTitle = intent.getStringExtra(PARAM_TITLE);
		mType = intent.getStringExtra(PARAM_TYPE);
		mReqUrl = intent.getStringExtra(PARAM_URL);
	}

	private void initTitle() {
		TextView middleTv = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (null != middleTv) {
			middleTv.setText(mTitle);
			setTitleMiddle(middleTv);
		}

		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);
	}

	private void initViews() {
		mProgressView = findViewById(R.id.progress);
		mErrorView = findViewById(R.id.error_layout);

		mScrollView = (ScrollView) findViewById(R.id.scroll_view);

		mTodayView = findViewById(R.id.layout_cms_today);
		mListToday = (ListView) mTodayView.findViewById(R.id.book_list);

		mCateView = (CommonViewPager) findViewById(R.id.layout_cms_cate);
	}

	private void reqData() {
		if (TextUtils.isEmpty(mReqUrl)) {
			mProgressView.setVisibility(View.GONE);
			mErrorView.setVisibility(View.VISIBLE);
			mScrollView.setVisibility(View.GONE);

			shortToast(R.string.wrong_url);
			return;
		} else {
			mProgressView.setVisibility(View.VISIBLE);
			mErrorView.setVisibility(View.GONE);
			mScrollView.setVisibility(View.GONE);
		}

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, mReqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

		RequestTask requestTask = new RequestTask(new RecommendCmsParser());
		requestTask.setTaskFinishListener(this);
		requestTask.execute(params);
	}

	private void updateTodayData(RecommendCmsResult result) {
		if (null == result.getRecommendToday()) {
			mTodayView.setVisibility(View.GONE);
		} else {
			RecommendCmsListAdapter adapter = new RecommendCmsListAdapter(this, mTitle, result.getRecommendToday().name);
			adapter.setList(result.getRecommendToday().books);

			adapter.setActionType(mType);
			mListToday.setAdapter(adapter);

			Util.measureListViewHeight(mListToday);
			mTodayView.setVisibility(View.VISIBLE);
		}
	}

	private void updateCateData(RecommendCmsResult result) {
		List<RecommendCmsResult.RecommendCate> cates = result.getRecommendCates();

		if (null == cates) {
			mCateView.setVisibility(View.GONE);
		} else {
			List<String> names = new ArrayList<String>();
			List<View> views = new ArrayList<View>();

			int height = -1;

			LayoutInflater inflater = getLayoutInflater();
			for (RecommendCmsResult.RecommendCate cate : cates) {
				ListView listView = (ListView) inflater.inflate(R.layout.vw_cms_listview, null);
				if (null == listView) {
					continue;
				}

				RecommendCmsListAdapter adapter = new RecommendCmsListAdapter(this, mTitle, cate.name);
				adapter.setList(cate.books);
				adapter.setActionType(mType);

				listView.setAdapter(adapter);

				int h = Util.measureListViewHeight(listView);
				height = height < h ? h : height;

				views.add(listView);
				names.add(cate.name);
			}

			ViewGroup.LayoutParams params = mCateView.getLayoutParams();
			if (null == params) {
				params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
			}
			// title indicator 的高度是35.33dp
			params.height = height + PixelUtil.dp2px(35.33f);
			// 因为ViewPager在ScrollView中，所以需要设置固定高度才能显示
			mCateView.setLayoutParams(params);

			mCateView.setTitleBackground(R.color.transparent);

			mCateView.initPager(views, names);

			mCateView.setVisibility(View.VISIBLE);
		}
	}
}
