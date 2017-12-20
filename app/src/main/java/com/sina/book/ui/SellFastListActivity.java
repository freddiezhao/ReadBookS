package com.sina.book.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.SellFastResult;
import com.sina.book.image.ImageLoader;
import com.sina.book.image.PauseOnScrollListener;
import com.sina.book.parser.SellFastBookParser;
import com.sina.book.ui.adapter.SellFastAdapter;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.Util;

/**
 * 畅销榜单页面
 * 
 * @author YangZhanDong
 */
public class SellFastListActivity extends CustomTitleActivity implements ITaskFinishListener {

	/** 畅销榜单适配器 */
	private SellFastAdapter mSellFastAdapter;

	/** 畅销榜单数据 */
	private SellFastResult mData;

	/** 进度条. */
	private View mProgress;

	/** 网络错误. */
	private View mError;

	private ViewHolder mViewHolder;

	// 推荐位视图
	private View mOperationView;
	private ListView mListView;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_sell_fast_list);
		initTitle();
		initView();
		initData();
	}

	private void initTitle() {
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_backmain_left, null);
		setTitleLeft(left);

		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		if (null != middle) {
			middle.setText(getString(R.string.sell_fast_list));
			setTitleMiddle(middle);
		}

		View right = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_search, null);
		setTitleRight(right);
	}

	@Override
	public void onClickLeft() {
		finish();
	}

	@Override
	public void onClickRight() {
		Intent intent = new Intent();
		intent.setClass(this, SearchActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
	}

	private void initView() {
		mProgress = findViewById(R.id.progress_layout);
		mError = findViewById(R.id.error_layout);
		mError.findViewById(R.id.retry_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				initData();
			}
		});

		mListView = (ListView) findViewById(R.id.sell_fast_listview);
		initHeadView(mListView);

		mSellFastAdapter = new SellFastAdapter(this);
		mListView.setAdapter(mSellFastAdapter);
	}

	private void initHeadView(ListView listView) {
		// 运营位视图
		mViewHolder = new ViewHolder();
		mOperationView = LayoutInflater.from(this).inflate(R.layout.vw_sell_fast_operation, null);
		if (null != mOperationView) {
			mViewHolder.mImages.add((ImageView) mOperationView.findViewById(R.id.sell_fast_image1));
			mViewHolder.mImages.add((ImageView) mOperationView.findViewById(R.id.sell_fast_image2));
			mViewHolder.mImages.add((ImageView) mOperationView.findViewById(R.id.sell_fast_image3));
			mViewHolder.mImages.add((ImageView) mOperationView.findViewById(R.id.sell_fast_image4));

			listView.addHeaderView(mOperationView);
		}
	}

	private void initData() {
		if (!HttpUtil.isConnected(this)) {
			showErrorView();
		} else {
			dismissErrorView();
			showProgressView();

			requestData();
		}
	}

	private void requestData() {
		RequestTask reqTask = new RequestTask(new SellFastBookParser());
		reqTask.setTaskFinishListener(this);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, ConstantData.URL_SELL_FAST_NEW);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		// 10分钟内请求，使用缓存
		reqTask.executeWitchCache(params, 600000L);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		dismissProgressView();

		if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
			mData = (SellFastResult) taskResult.retObj;
			if (mData.getItems().size() > 0) {
				mSellFastAdapter.setList(mData.getItems());
				mSellFastAdapter.notifyDataSetChanged();
			}

			if (mData != null && mData.getOperationBooks().size() > 0) {
				for (int i = 0; i < mData.getOperationBooks().size(); i++) {
					if (i >= mViewHolder.mImages.size())
						break;

					final Book book = mData.getOperationBooks().get(i);
					String imageUrl = book.getDownloadInfo().getImageUrl();

					if (imageUrl != null && !imageUrl.contains("http://")) {
						book.getDownloadInfo().setImageUrl(null);
					}

					ImageView imageView = mViewHolder.mImages.get(i);
					ImageLoader.getInstance().load2(imageUrl, imageView, ImageLoader.TYPE_BIG_PIC,
							ImageLoader.getDefaultPic());

					final int j = i;
					mViewHolder.mImages.get(i).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							//
							String eventKey = "畅销榜单_TopBanner_" + Util.formatNumber(j + 1);
							BookDetailActivity.launchNew(SellFastListActivity.this, book, eventKey);

							UserActionManager.getInstance().recordEvent(Constants.CLICK_SELLWELL_AD + j);
						}
					});
				}
				mOperationView.setVisibility(View.VISIBLE);
			} else {
				mOperationView.setVisibility(View.GONE);
			}
		} else {
			showErrorView();
		}

	}

	public void showProgressView() {
		mProgress.setVisibility(View.VISIBLE);
	}

	public void dismissProgressView() {
		mProgress.setVisibility(View.GONE);
	}

	public void showErrorView() {
		mError.setVisibility(View.VISIBLE);
	}

	public void dismissErrorView() {
		mError.setVisibility(View.GONE);
	}

	private class ViewHolder {
		/** 运营位相对应的Image */
		protected List<ImageView> mImages = new ArrayList<ImageView>(4);
	}
}