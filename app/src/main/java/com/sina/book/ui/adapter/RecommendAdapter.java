package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.RecommendCateItem;
import com.sina.book.data.RecommendFamousItem;
import com.sina.book.data.RecommendMonthItem;
import com.sina.book.data.RecommendResult;
import com.sina.book.ui.CommonListActivity;
import com.sina.book.ui.FamousRecommendActivity;
import com.sina.book.ui.MainTabActivity;
import com.sina.book.ui.PaymentMonthDetailActivity;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

/**
 * 推荐页适配器
 * 
 * @author MarkMjw
 * @date 2013-8-28
 */
public class RecommendAdapter extends BaseAdapter {
	private static final int TYPE_HOT = 0;
	private static final int TYPE_FAMOUS = 1;
	private static final int TYPE_CLASSIC = 2;
	private static final int TYPE_NEW = 3;
	private static final int TYPE_FREE = 4;
	private static final int TYPE_MONTH = 5;

	private static final int TOTAL = 6;

	private LayoutInflater mInflater;

	private Activity mContext;

	private RecommendResult mResult;

	private BookImageAdapter mBookAdapter;

	private BitmapDrawable mDrawable;

	public RecommendAdapter(Activity context) {
		this.mContext = context;

		mInflater = LayoutInflater.from(mContext);
		mBookAdapter = new BookImageAdapter(mContext, null);

		Bitmap dotReal = BitmapFactory.decodeResource(context.getResources(), R.drawable.divider_dot_real);
		mDrawable = new BitmapDrawable(context.getResources(), dotReal);
		mDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mDrawable.setDither(true);
	}

	public void setData(RecommendResult result) {
		mResult = result;
	}

	@Override
	public int getCount() {
		return TOTAL;
	}

	@Override
	public Object getItem(int position) {
		switch (position) {
		case 0: // 热门推荐
			return mResult.getHotBook();
		case 1: // 名人推荐位置
			return mResult.getFamous();
		case 2: // 热门分类
			return mResult.getCates();
		case 3: // 精品新书
			return mResult.getNewBook();
		case 4: // 免费专区
			return mResult.getFreeBook();
		case 5: // 包月畅读
			return mResult.getMonth();
		default:
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.vw_recommend_layout, null);
		}

		if (null == mResult)
			return convertView;

		Holder holder = getHolder(convertView);

		switch (position) {
		case TYPE_HOT:
			updateRecommendData(holder);
			break;

		case TYPE_FAMOUS:
			updateFamousData(holder);
			break;

		case TYPE_CLASSIC:
			updateClassicData(holder);
			break;

		case TYPE_NEW:
			updateNewData(holder);
			break;

		case TYPE_FREE:
			updateFreeData(holder);
			break;

		case TYPE_MONTH:
			updateMonthData(holder);
			break;

		default:
			break;
		}

		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		switch (position) {
		case 0: // 热门推荐
			return TYPE_HOT;
		case 1: // 名人推荐位置
			return TYPE_FAMOUS;
		case 2: // 热门分类
			return TYPE_CLASSIC;
		case 3: // 精品新书
			return TYPE_NEW;
		case 4: // 免费专区
			return TYPE_FREE;
		case 5: // 包月畅读
			return TYPE_MONTH;
		default:
			return TYPE_HOT;
		}
	}

	private Holder getHolder(final View view) {
		Holder holder = (Holder) view.getTag();
		if (holder == null) {
			holder = new Holder(view);
			view.setTag(holder);
		}
		return holder;
	}

	private class Holder {
		private View titleLayout;

		private TextView title;

		private TextView count;

		private RelativeLayout more;

		private ListView listView;

		public Holder(View view) {
			titleLayout = view.findViewById(R.id.recommend_list_title_layout);
			title = (TextView) view.findViewById(R.id.recommend_title);
			count = (TextView) view.findViewById(R.id.recommend_more);
			more = (RelativeLayout) view.findViewById(R.id.recommend_all_btn);
			listView = (ListView) view.findViewById(R.id.recommend_list);
		}
	}

	private void updateRecommendData(Holder holder) {
		List<Book> books = mResult.getHotBook().getItems();
		if (books != null && books.size() > 0) {
			mBookAdapter.setData(getPartBooks(books));
			mBookAdapter.setActionType(Constants.CLICK_RECOMMEND_HOT);

			holder.listView.setAdapter(mBookAdapter);
			Util.measureListViewHeight(holder.listView);
		}

		holder.title.setText(ResourceUtil.getString(R.string.recommend_hot));
		String count = String.format(ResourceUtil.getString(R.string.recommend_book_count), mResult.getHotBook()
				.getTotal());
		holder.count.setText(count);
		holder.count.setVisibility(View.VISIBLE);

		Listener listener = new Listener(TYPE_HOT);
		holder.titleLayout.setOnClickListener(listener);
		holder.more.setOnClickListener(listener);
	}

	private void updateFamousData(Holder holder) {
		List<RecommendFamousItem> famous = mResult.getFamous().getDatas();
		if (null != famous && !famous.isEmpty()) {
			RecommendFamousAdapter adapter = new RecommendFamousAdapter(mContext, mDrawable);
			adapter.setList(famous);

			holder.listView.setAdapter(adapter);
			Util.measureListViewHeight(holder.listView);
		}

		holder.title.setText(ResourceUtil.getString(R.string.recommend_famous));
		holder.count.setVisibility(View.GONE);

		Listener listener = new Listener(TYPE_FAMOUS);
		holder.titleLayout.setOnClickListener(listener);
		holder.more.setOnClickListener(listener);
	}

	private void updateClassicData(Holder holder) {
		List<RecommendCateItem> classic = mResult.getCates().getDatas();
		if (null != classic && !classic.isEmpty()) {
			RecommendClassicAdapter adapter = new RecommendClassicAdapter(mContext, mDrawable);
			adapter.setList(classic);

			holder.listView.setAdapter(adapter);
			Util.measureListViewHeight(holder.listView);
		}

		holder.title.setText(ResourceUtil.getString(R.string.recommend_classic));
		holder.count.setVisibility(View.GONE);

		Listener listener = new Listener(TYPE_CLASSIC);
		holder.titleLayout.setOnClickListener(listener);
		holder.more.setOnClickListener(listener);
	}

	private void updateNewData(Holder holder) {
		List<Book> books = mResult.getNewBook().getItems();
		if (books != null && books.size() > 0) {
			mBookAdapter.setData(getPartBooks(books));
			mBookAdapter.setActionType(Constants.CLICK_RECOMMEND_NEW);

			holder.listView.setAdapter(mBookAdapter);
			Util.measureListViewHeight(holder.listView);
		}

		holder.title.setText(ResourceUtil.getString(R.string.recommend_new));
		String count = String.format(ResourceUtil.getString(R.string.recommend_book_count), mResult.getNewBook()
				.getTotal());
		holder.count.setText(count);
		holder.count.setVisibility(View.VISIBLE);

		Listener listener = new Listener(TYPE_NEW);
		holder.titleLayout.setOnClickListener(listener);
		holder.more.setOnClickListener(listener);
	}

	private void updateFreeData(Holder holder) {
		List<Book> books = mResult.getFreeBook().getItems();
		if (books != null && books.size() > 0) {
			mBookAdapter.setData(getPartBooks(books));
			mBookAdapter.setActionType(Constants.CLICK_RECOMMEND_FREE);

			holder.listView.setAdapter(mBookAdapter);
			Util.measureListViewHeight(holder.listView);
		}

		holder.title.setText(ResourceUtil.getString(R.string.recommend_free));
		String count = String.format(ResourceUtil.getString(R.string.recommend_book_count), mResult.getFreeBook()
				.getTotal());
		holder.count.setText(count);
		holder.count.setVisibility(View.VISIBLE);

		Listener listener = new Listener(TYPE_FREE);
		holder.titleLayout.setOnClickListener(listener);
		holder.more.setOnClickListener(listener);
	}

	private void updateMonthData(Holder holder) {
		List<RecommendMonthItem> month = mResult.getMonth().getDatas();
		if (null != month && !month.isEmpty()) {
			RecommendMonthAdapter adapter = new RecommendMonthAdapter(mContext, mDrawable);
			adapter.setList(month);

			holder.listView.setAdapter(adapter);
			Util.measureListViewHeight(holder.listView);
		}

		holder.title.setText(ResourceUtil.getString(R.string.recommend_month));
		holder.count.setVisibility(View.GONE);

		Listener listener = new Listener(TYPE_MONTH);
		holder.titleLayout.setOnClickListener(listener);
		holder.more.setOnClickListener(listener);
	}

	private List<Book> getPartBooks(List<Book> books) {
		List<Book> partBooks = new ArrayList<Book>();
		if (books != null && books.size() > 0) {
			int size = books.size() <= 6 ? books.size() : 6;
			for (int i = 0; i < size; i++) {
				partBooks.add(books.get(i));
			}
		}
		return partBooks;
	}

	private class Listener implements View.OnClickListener {
		/** 热门推荐类型. */
		private final int HOT_TYPE = 1;
		/** 精品新书类型. */
		private final int NEW_TYPE = 2;
		/** 免费专区类型. */
		private final int FREE_TYPE = 3;

		private int mType;

		public Listener(int type) {
			mType = type;
		}

		@Override
		public void onClick(View v) {
			switch (mType) {
			case TYPE_HOT:
				// 去热门推荐
				enterChildRecommendPage(HOT_TYPE, R.string.recommend_hot, Constants.PAGE_RECOMMEND_HOT);
				break;

			case TYPE_FAMOUS:
				// 去名人推荐
				FamousRecommendActivity.launch(mContext);
				break;

			case TYPE_CLASSIC:
				// 去分类
				enterClassicPage();
				break;

			case TYPE_NEW:
				// 去精品新书
				enterChildRecommendPage(NEW_TYPE, R.string.recommend_new, Constants.PAGE_RECOMMEND_NEW);
				break;

			case TYPE_FREE:
				// 去免费专区
				enterChildRecommendPage(FREE_TYPE, R.string.recommend_free, Constants.PAGE_RECOMMEND_FREE);
				break;

			case TYPE_MONTH:
				// 去包月厅
				enterMonthPage();
				break;

			default:
				break;
			}
		}

		private void enterChildRecommendPage(int type, int titleRes, String actionKey) {
			String url = String.format(ConstantData.URL_INDEX, type, "%s", ConstantData.PAGE_SIZE);
			CommonListActivity
					.launch(mContext, url, ResourceUtil.getString(titleRes), CommonListAdapter.TYPE_RECOMMEND);

			UserActionManager.getInstance().recordEvent(actionKey);
		}

		private void enterMonthPage() {
			Intent intent = new Intent(mContext, PaymentMonthDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			mContext.startActivity(intent);

			UserActionManager.getInstance().recordEvent(Constants.PAGE_MONTH);
		}

		private void enterClassicPage() {
			MainTabActivity.launch(mContext, 2);

			UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_CLASSIC);
		}
	}
}
