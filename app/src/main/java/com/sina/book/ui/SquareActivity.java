package com.sina.book.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.SquareResult;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.SquareBookParser;
import com.sina.book.ui.adapter.ListAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.PixelUtil;

/**
 * @Description 广场页面
 * 
 * @author YangZhanDong
 * 
 */
public class SquareActivity extends CustomTitleActivity implements OnClickListener, ITaskFinishListener,
		IXListViewListener {

	/** 广场列表. */
	private XListView mSquareListView;

	/** 广场Adapter. */
	private ListAdapter<Book> mSquareAdapter;

	/** 广场数据. */
	private SquareResult result = new SquareResult();

	/** 进度条. */
	private View mProgress;

	/** 网络错误. */
	private View mError;

	/** 网络错误，重试按钮 */
	private Button mRetryBtn;

	// private int mCurrentPage;
	private ArrayList<Book> mBook = new ArrayList<Book>();

	private int mLastFirstPos;

	/** 价格图标的高度、宽度 */
	private final int PRICE_LABEL_BOUND = PixelUtil.dp2px(10.67f);

	public static void launch(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, SquareActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void init(Bundle savedInstanceState) {

		setContentView(R.layout.act_square);
		initIntent();
		initTitle();
		initView();
		initData();
	}

	@Override
	public void onRelease() {
		mLastFirstPos = mSquareListView.getFirstVisiblePosition();
		mSquareListView.setAdapter(null);
		super.onRelease();
	}

	@Override
	public void onLoad() {
		mSquareListView.setAdapter(mSquareAdapter);
		mSquareListView.setSelectionFromTop(mLastFirstPos, 0);
		super.onLoad();
	}

	private void initIntent() {
		Intent intent = getIntent();
		Book book = (Book) intent.getSerializableExtra("book");
		if (book != null) {
			mBook.add(book);
		}

	}

	private void initTitle() {
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(left);

		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middle.setText(getString(R.string.all_read));
		setTitleMiddle(middle);

		View right = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_search, null);
		setTitleRight(right);

	}

	private void initView() {
		mSquareListView = (XListView) findViewById(R.id.square_list);
		mProgress = findViewById(R.id.progress_layout);
		mError = findViewById(R.id.error_layout);
		mRetryBtn = (Button) mError.findViewById(R.id.retry_btn);

		mRetryBtn.setOnClickListener(this);

		mSquareListView.setPullRefreshEnable(false);
		mSquareListView.setPullLoadEnable(false);
		mSquareListView.setXListViewListener(this);

		mSquareAdapter = new SquareAdapter();
		mSquareListView.setAdapter(mSquareAdapter);

	}

	private void initData() {
		if (!HttpUtil.isConnected(this)) {
			showErrorView();
		} else {
			dismissErrorView();
			showProgressView();

			requestSquareData(1);
		}
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.retry_btn:
			initData();
			break;

		default:
			break;
		}
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		if (!HttpUtil.isConnected(this)) {
			shortToast(R.string.network_unconnected);
			mSquareListView.stopLoadMore();
			return;
		}

		if (mSquareAdapter.hasMore()) {
			requestSquareData(mSquareAdapter.getCurrentPage() + 1);
		}
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		RequestTask task = (RequestTask) taskResult.task;
		int page = (Integer) task.getExtra();

		if (page == 1) {
			dismissProgressView();
		} else {
			mSquareListView.stopLoadMore();
		}

		if (taskResult.retObj != null && taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof SquareResult) {
				if (page == 1) {
					result.addItems(mBook);
					SquareResult item = (SquareResult) taskResult.retObj;
					ArrayList<Book> books = item.getItem();
					if (mBook.size() > 0 && books.size() > 0) {
						books.remove(0);
					}
					result.addItems(books);
					result.setTotal(item.getTotal());
				} else {
					result = (SquareResult) taskResult.retObj;
				}
				if (result.getItem() != null && result.getTotal() > 0) {
					// mCurrentPage = page;
					mSquareAdapter.setCurrentPage(page);

					mSquareAdapter.addList(result.getItem());
					mSquareAdapter.setTotalAndPerpage(result.getTotal(), PAGE_SIZE);

					if (mSquareAdapter.hasMore()) {
						mSquareListView.setPullLoadEnable(true);
					} else {
						mSquareListView.setPullLoadEnable(false);
					}

					mSquareAdapter.notifyDataSetChanged();
				}
			}
		} else {
			if (page == 1) {
				showErrorView();
			} else {
				shortToast(R.string.network_unconnected);
			}
		}
	}

	private final int PAGE_SIZE = 12;

	private void requestSquareData(int page) {
		String reqUrl = String.format(Locale.CHINA, ConstantData.URL_ALL_READ, page, PAGE_SIZE);

		RequestTask reqTask = new RequestTask(new SquareBookParser());
		reqTask.setTaskFinishListener(this);
		reqTask.setExtra(page);

		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
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

	private class SquareAdapter extends ListAdapter<Book> {

		private final int ITEM_COUNT = 3;
		private Book[] mBooks = new Book[3];
		private ViewHolder mHolder;

		public SquareAdapter() {

		}

		@Override
		public int getCount() {
			int count = 0;
			if (mDataList != null) {
				if (mDataList.size() > 0) {
					count = mDataList.size() / ITEM_COUNT;
				}
				if (mDataList.size() % ITEM_COUNT > 0) {
					count++;
				}
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			ArrayList<Book> bookItem = new ArrayList<Book>();
			int length = mDataList.size();

			if (position * ITEM_COUNT < length) {
				bookItem.add(mDataList.get(position * ITEM_COUNT));
			}
			if ((position * ITEM_COUNT + 1) < length) {
				bookItem.add(mDataList.get(position * ITEM_COUNT + 1));
			}
			if ((position * ITEM_COUNT + 2) < length) {
				bookItem.add(mDataList.get(position * ITEM_COUNT + 2));
			}
			return bookItem;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView.getTag() == null) {
				convertView = createView();
			}
			mHolder = (ViewHolder) convertView.getTag();

			@SuppressWarnings("unchecked")
			ArrayList<Book> item = (ArrayList<Book>) getItem(position);
			for (int i = 0; i < mHolder.mSquareItemLayout.size(); i++) {
				mHolder.mSquareItemLayout.get(i).setVisibility(View.INVISIBLE);
			}

			for (int i = 0; i < item.size(); i++) {
				mHolder.mSquareItemLayout.get(i).setVisibility(View.VISIBLE);

				mBooks[i] = item.get(i);

				if (mBooks[i].getDownloadInfo().getImageUrl() != null
						&& !mBooks[i].getDownloadInfo().getImageUrl().contains("http://")) {
					mBooks[i].getDownloadInfo().setImageUrl(null);
				}
				ImageLoader.getInstance().load(mBooks[i].getDownloadInfo().getImageUrl(),
						mHolder.mSquareItemImage.get(i), ImageLoader.TYPE_COMMON_BIGGER_BOOK_COVER,
						ImageLoader.getDefaultPic());

				mHolder.mSquareItemPrice.get(i).setText(null);
				mHolder.mSquareItemPrice.get(i).setCompoundDrawables(null, null, null, null);
				mHolder.mSquareFreeBookImage.get(i).setVisibility(View.INVISIBLE);
				mHolder.mSquareSeriaLizeImage.get(i).setVisibility(View.INVISIBLE);

				if (mBooks[i].getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
					mHolder.mSquareFreeBookImage.get(i).setVisibility(View.VISIBLE);
				} else if (mBooks[i].getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {

					mHolder.mSquareItemPrice.get(i).setText(String.valueOf(mBooks[i].getBuyInfo().getPrice()));

					Drawable drawable = getResources().getDrawable(R.drawable.price_label_normal);
					drawable.setBounds(0, 0, PRICE_LABEL_BOUND, PRICE_LABEL_BOUND);
					mHolder.mSquareItemPrice.get(i).setCompoundDrawables(drawable, null, null, null);
				} else if (mBooks[i].getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {
					mHolder.mSquareSeriaLizeImage.get(i).setVisibility(View.VISIBLE);
				}

				if (mBooks[i].getPraiseNum() > 0) {
					mHolder.mSquareItemPraise.get(i).setText(String.valueOf(mBooks[i].getPraiseNum()));
				} else {
					mHolder.mSquareItemPraise.get(i).setText(String.valueOf(0));
				}

				if (mBooks[i].getTitle() != null) {
					mHolder.mSquareBookTitle.get(i).setText(mBooks[i].getTitle());
				}

				if (mBooks[i].getAuthor() != null) {
					mHolder.mSquareBookAuthor.get(i).setText(mBooks[i].getAuthor());
				}

				mHolder.mSquareItemImageShadow.get(i).setOnClickListener(new OnClickListener(mBooks[i]));

			}

			return convertView;
		}

		public View createView() {
			View itemView = LayoutInflater.from(SquareActivity.this).inflate(R.layout.vw_square_list_item, null);

			ViewHolder holder = new ViewHolder();

			holder.mSquareItemLayout.add(itemView.findViewById(R.id.square_item_layout1));
			holder.mSquareItemLayout.add(itemView.findViewById(R.id.square_item_layout2));
			holder.mSquareItemLayout.add(itemView.findViewById(R.id.square_item_layout3));

			holder.mSquareItemImage.add((ImageView) itemView.findViewById(R.id.square_item_book_image1));
			holder.mSquareItemImage.add((ImageView) itemView.findViewById(R.id.square_item_book_image2));
			holder.mSquareItemImage.add((ImageView) itemView.findViewById(R.id.square_item_book_image3));

			holder.mSquareItemImageShadow.add((ImageView) itemView.findViewById(R.id.square_item_book_image1_shadow));
			holder.mSquareItemImageShadow.add((ImageView) itemView.findViewById(R.id.square_item_book_image2_shadow));
			holder.mSquareItemImageShadow.add((ImageView) itemView.findViewById(R.id.square_item_book_image3_shadow));

			holder.mSquareItemPrice.add((TextView) itemView.findViewById(R.id.square_item_book_price_text1));
			holder.mSquareItemPrice.add((TextView) itemView.findViewById(R.id.square_item_book_price_text2));
			holder.mSquareItemPrice.add((TextView) itemView.findViewById(R.id.square_item_book_price_text3));

			holder.mSquareItemPraise.add((TextView) itemView.findViewById(R.id.square_item_book_praise_text1));
			holder.mSquareItemPraise.add((TextView) itemView.findViewById(R.id.square_item_book_praise_text2));
			holder.mSquareItemPraise.add((TextView) itemView.findViewById(R.id.square_item_book_praise_text3));

			holder.mSquareBookTitle.add((TextView) itemView.findViewById(R.id.square_book_title_text1));
			holder.mSquareBookTitle.add((TextView) itemView.findViewById(R.id.square_book_title_text2));
			holder.mSquareBookTitle.add((TextView) itemView.findViewById(R.id.square_book_title_text3));

			holder.mSquareBookAuthor.add((TextView) itemView.findViewById(R.id.square_book_author_text1));
			holder.mSquareBookAuthor.add((TextView) itemView.findViewById(R.id.square_book_author_text2));
			holder.mSquareBookAuthor.add((TextView) itemView.findViewById(R.id.square_book_author_text3));

			holder.mSquareFreeBookImage.add((ImageView) itemView.findViewById(R.id.square_item_book_free_image1));
			holder.mSquareFreeBookImage.add((ImageView) itemView.findViewById(R.id.square_item_book_free_image2));
			holder.mSquareFreeBookImage.add((ImageView) itemView.findViewById(R.id.square_item_book_free_image3));

			holder.mSquareSeriaLizeImage.add((ImageView) itemView.findViewById(R.id.square_item_book_serialize_image1));
			holder.mSquareSeriaLizeImage.add((ImageView) itemView.findViewById(R.id.square_item_book_serialize_image2));
			holder.mSquareSeriaLizeImage.add((ImageView) itemView.findViewById(R.id.square_item_book_serialize_image3));

			itemView.setTag(holder);
			return itemView;
		}

		public class ViewHolder {
			private ArrayList<View> mSquareItemLayout = new ArrayList<View>(ITEM_COUNT);
			private ArrayList<ImageView> mSquareItemImage = new ArrayList<ImageView>(ITEM_COUNT);
			private ArrayList<ImageView> mSquareItemImageShadow = new ArrayList<ImageView>(ITEM_COUNT);
			private ArrayList<ImageView> mSquareFreeBookImage = new ArrayList<ImageView>(ITEM_COUNT);
			private ArrayList<ImageView> mSquareSeriaLizeImage = new ArrayList<ImageView>(ITEM_COUNT);
			private ArrayList<TextView> mSquareItemPraise = new ArrayList<TextView>(ITEM_COUNT);
			private ArrayList<TextView> mSquareItemPrice = new ArrayList<TextView>(ITEM_COUNT);
			private ArrayList<TextView> mSquareBookTitle = new ArrayList<TextView>(ITEM_COUNT);
			private ArrayList<TextView> mSquareBookAuthor = new ArrayList<TextView>(ITEM_COUNT);
		}

		@Override
		protected List<Book> createList() {
			return new ArrayList<Book>();
		}

		private class OnClickListener implements View.OnClickListener {
			private Book mBook;

			public OnClickListener(Book book) {
				this.mBook = book;
			}

			@Override
			public void onClick(View v) {
				if (null != mBook) {
					BookDetailActivity.launch(SquareActivity.this, mBook);
				}
			}
		}

	}

}