package com.sina.book.ui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.BuyDetail;
import com.sina.book.data.BuyItem;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.data.ListResult;
import com.sina.book.parser.BuyDetailItemParser;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.adapter.ListAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.Util;
import com.sina.weibo.sdk.utils.MD5;

public class BuyDetailDialog extends Dialog implements IXListViewListener, ITaskFinishListener
{
	protected Context		mContext;

	private View			mProgressView;
	private View			mErrorView;
	private View			mEmptyView;
	private TextView		mTitle;
	private XListView		mContentList;
	private BuyItemAdapter	mItemAdapter;
	// private int mCurPage = 1;

	private BuyDetail		mBuyDetail;
	private RequestTask		mReqTask;
	private String			bookId;
	private String			bookName;
	private int				paytype;

	public BuyDetailDialog(Context context, String bookId, String bookName, int payType)
	{
		super(context, R.style.BuyDetailDialog);
		mContext = context;
		this.bookId = bookId;
		this.bookName = bookName;
		this.paytype = payType;
		setCanceledOnTouchOutside(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vw_buy_detail_dialog);
		initView();
	}

	@Override
	public void show()
	{
		getWindow().setWindowAnimations(R.style.PopWindowAnimation);
		super.show();
	}

	public void show(BuyDetail buyDetail)
	{
		show();
		if (mReqTask != null) {
			mReqTask.cancel(true);
		}
		if (buyDetail != null) {
			// mCurPage = 1;
			mItemAdapter.setCurrentPage(1);
			mItemAdapter.setList(new ArrayList<BuyItem>());
			mItemAdapter.notifyDataSetChanged();
			mBuyDetail = buyDetail;
			mTitle.setText(mBuyDetail.getTitle());
			requestData(mItemAdapter.getCurrentPage());
		}
	}

	@Override
	public void onRefresh()
	{

	}

	@Override
	public void onLoadMore()
	{
		if (!HttpUtil.isConnected(mContext)) {
			mContentList.stopLoadMore();
			return;
		}

		if (mItemAdapter.IsAdding()) {
			return;
		}

		if (mItemAdapter.hasMore()) {
			requestData(mItemAdapter.getCurrentPage() + 1);
		}
	}

	private void requestData(int page)
	{
		if (page == 1) {
			if (HttpUtil.isConnected(mContext)) {
				mContentList.setVisibility(View.GONE);
				mErrorView.setVisibility(View.GONE);
				mProgressView.setVisibility(View.VISIBLE);
			} else {
				mContentList.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
				mProgressView.setVisibility(View.GONE);
				return;
			}
		} else {
			mItemAdapter.setAdding(true);
		}

		String url = String.format(Locale.CHINA, ConstantData.URL_CONSUME_DETAIL, page, mBuyDetail.getBookId());
		url = ConstantData.addLoginInfoToUrl(url);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, url);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		mReqTask = new RequestTask(new BuyDetailItemParser());
		mReqTask.setTaskFinishListener(this);
		mReqTask.setExtra(page);
		mReqTask.execute(params);
	}

	private void initView()
	{
		mTitle = (TextView) findViewById(R.id.book_title);
		mContentList = (XListView) findViewById(R.id.content);
		mItemAdapter = new BuyItemAdapter(mContext);
		mContentList.setAdapter(mItemAdapter);
		mContentList.setPullRefreshEnable(false);
		mContentList.setPullLoadEnable(true);
		mContentList.setXListViewListener(this);
		mContentList.setEmptyView(mEmptyView);

		mProgressView = findViewById(R.id.progress_view);
		mEmptyView = findViewById(R.id.empty_view);
		mErrorView = findViewById(R.id.error_view);
		mErrorView.findViewById(R.id.retry_btn).setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				requestData(mItemAdapter.getCurrentPage());
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTaskFinished(TaskResult taskResult)
	{
		RequestTask task = (RequestTask) taskResult.task;
		int page = (Integer) task.getExtra();
		Object result = taskResult.retObj;
		// 停止加载更多
		mContentList.stopLoadMore();
		mProgressView.setVisibility(View.GONE);
		if (mItemAdapter.IsAdding()) {
			mItemAdapter.setAdding(false);
		}

		if (result instanceof ListResult<?>) {
			// mCurPage = page;
			mItemAdapter.setCurrentPage(page);
			ListResult<BuyItem> listResult = (ListResult<BuyItem>) result;
			mItemAdapter.setTotalAndPerpage(listResult.getTotalNum(), 10);

			if (mItemAdapter.getCurrentPage() == 1) {
				mItemAdapter.setList(listResult.getList());
			} else {
				mItemAdapter.addList(listResult.getList());
			}

			mItemAdapter.notifyDataSetChanged();
			Util.measureListViewHeight(mContentList);

			if (!mItemAdapter.hasMore()) {
				mContentList.setPullLoadEnable(false);
			} else {
				mContentList.setPullLoadEnable(true);
			}
			mErrorView.setVisibility(View.GONE);
			mContentList.setVisibility(View.VISIBLE);
			return;
		}

		if (page == 1) {
			mErrorView.setVisibility(View.VISIBLE);
			mContentList.setVisibility(View.GONE);
		}
	}

	private class BuyItemAdapter extends ListAdapter<BuyItem>
	{
		private Context		mContext;
		private ViewHolder	mHolder;

		public BuyItemAdapter(Context context)
		{
			mContext = context;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			if (convertView == null || convertView.getTag() == null) {
				convertView = createView();
			}

			mHolder = (ViewHolder) convertView.getTag();
			final BuyItem rdetail = (BuyItem) getItem(position);

			mHolder.time.setText(rdetail.getTime());
			mHolder.title.setText(rdetail.getTitle());

			// 如果书籍已经下架了，则置灰并不可点击
			if (rdetail.isOutOfService()) {
				mHolder.title.setTextColor(Color.GRAY);
				mHolder.title.setOnClickListener(null);
			} else {
				int color = mContext.getResources().getColor(R.color.consume_item_light);
				mHolder.title.setTextColor(color);
				mHolder.title.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						String uid = LoginUtil.getLoginInfo().getUID();

						String tbookId = rdetail.getBookId();
						if (tbookId == null || "".equals(tbookId)) {
							tbookId = bookId;
						}

						StringBuffer buffer = new StringBuffer();
						buffer.append(uid);
						buffer.append("|");
						buffer.append(tbookId);
						buffer.append("|");
						buffer.append(ConstantData.BUYDETETAIL_KEY);
						String prikey = MD5.hexdigest(buffer.toString());

						if (rdetail.getPayType() == 2) {
							// 按本- 进入书籍详情页
							Book b = new Book();
							b.setBookId(tbookId);

							String eventKey = "消费记录_已购买_" + Util.formatNumber(position + 1);
							String eventExtra = "个人中心Chapter";
							BookDetailActivity.launch(mContext, b, prikey, eventKey, eventExtra);
						} else {
							// 按章 - 进入阅读页对应章节
							Book b = new Book();
							b.setBookId(tbookId);

							int index = bookName.indexOf('：');
							if (index < 0) {
								index = bookName.indexOf(':');
							}
							if (index >= 0) {
								bookName = bookName.substring(index + 1);
							}
							b.setTitle(bookName);
							b.getBuyInfo().setPayType(paytype);

							Book book = DownBookManager.getInstance().getBook(b);
							if (book == null) {
								book = b;
							}

							Chapter cp = null;
							String chapterId = rdetail.getChapterId();
							if (chapterId != null && chapterId.length() > 0) {
								cp = new Chapter();
								cp.setGlobalId(Integer.parseInt(chapterId));
								cp.setTitle(rdetail.getTitle());
								cp.setPrice(Double.parseDouble(rdetail.getPrice()));
								cp.setUpdateTime(rdetail.getTime());
								cp.setVip("Y");
								cp.setHasBuy(true);
							}

							ReadActivity.setChapterReadEntrance("消费记录-已购买");
							if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) < 0.0001
									&& book.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {
								// 已下载完成
								ReadActivity.launch(mContext, book, false, cp, false, prikey);
							} else {
								ReadActivity.launch(mContext, book, true, cp, false, prikey);
							}

						}
					}
				});
			}

			// TODO:
			String units = rdetail.getUnit();
			if (units != null && units.contains("赠书卡")) {
				mHolder.money.setText(rdetail.getUnit());
			} else {
				mHolder.money.setText(rdetail.getPrice() + rdetail.getUnit());
			}
			return convertView;
		}

		@Override
		protected List<BuyItem> createList()
		{
			return new ArrayList<BuyItem>();
		}

		protected View createView()
		{
			View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_buy_item, null);
			ViewHolder holder = new ViewHolder();
			holder.time = (TextView) itemView.findViewById(R.id.time);
			holder.title = (TextView) itemView.findViewById(R.id.chapter_title);
			holder.title.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 下划线

			holder.money = (TextView) itemView.findViewById(R.id.money);
			itemView.setTag(holder);
			return itemView;
		}

		protected class ViewHolder
		{
			public TextView	time;
			public TextView	title;
			public TextView	money;
		}
	}
}
