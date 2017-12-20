package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookDetailData;
import com.sina.book.data.ConstantData;
import com.sina.book.data.util.CollectedBookList;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.BookDetailParser;
import com.sina.book.parser.SimpleParser;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.ResourceUtil;

/**
 * 收藏书籍列表适配器
 * 
 * @author MarkMjw
 * @date 2012-12-25
 */
public class CollectedBookAdapter extends ListAdapter<Book> implements ITaskFinishListener {
	// private static final String TAG = "CollectedBookAdapter";

	private static final String EXTRA_TYPE = "extra_type";
	private static final String REQUEST_KEY = "delete_collected";
	private static final String EXTRA_OBJECT = "extra_object";

	private ListView mListView;
	private Context mContext;

	private CustomProDialog mProgressDialog;
	private BitmapDrawable mDividerDrawable;

	private SparseBooleanArray mBooleanArray;

	public CollectedBookAdapter(Context context, ListView listView) {
		mContext = context;
		mListView = listView;
		mBooleanArray = new SparseBooleanArray();

		Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.list_divide_dot);
		mDividerDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
		mDividerDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mDividerDrawable.setDither(true);
	}

	public void clearList() {
		if (mDataList != null) {
			mDataList.clear();
		}
	}

	@Override
	public int getCount() {
		if (mDataList == null || mDataList.size() == 0) {
			return 1;
		} else {
			return mDataList.size() + 1;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_purchased_item, null);
			return convertView;
		}

		if (convertView == null || convertView.getTag() == null) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();
		Book book = mDataList.get(position - 1);
		ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), holder.mBookCover,
				ImageLoader.TYPE_COMMON_BOOK_COVER, ImageLoader.getDefaultPic());

		holder.mBookTitle.setText(book.getTitle());
		holder.mBookAuthor.setText(mContext.getString(R.string.author) + book.getAuthor());
		if (book.getIntro() != null) {
			holder.mBookInfo.setText(book.getIntro().trim());
		} else {
			holder.mBookInfo.setText("No introduction.");
		}
		holder.mBookFree.setVisibility(View.GONE);

		ClickListener listener = new ClickListener(holder, position, book, convertView);
		holder.mMenuBtn.setOnClickListener(listener);
		holder.mMenuDeleteBtn.setOnClickListener(listener);
		holder.mMenuDownBtn.setOnClickListener(listener);

		if (mBooleanArray.get(position, false)) {
			holder.mMenuBtn.setImageResource(R.drawable.menu_btn_up);
			holder.mMenuBtnLayout.setVisibility(View.VISIBLE);
		} else {
			holder.mMenuBtn.setImageResource(R.drawable.menu_btn_down);
			holder.mMenuBtnLayout.setVisibility(View.GONE);
		}

		if (DownBookManager.getInstance().hasBook(book)) {
			holder.mMenuDownBtn.setEnabled(false);
			holder.mMenuDownBtn.setText(R.string.has_down);
		} else {
			holder.mMenuDownBtn.setEnabled(true);
			holder.mMenuDownBtn.setText(R.string.bookhome_down);
		}

		holder.mDivider.setImageDrawable(mDividerDrawable);

		return convertView;
	}

	protected View createView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_collected_list_item, null);

		ViewHolder holder = new ViewHolder();

		holder.mBookLayout = itemView.findViewById(R.id.item_content_layout);

		holder.mBookCover = (ImageView) holder.mBookLayout.findViewById(R.id.header_img);
		holder.mBookTitle = (TextView) holder.mBookLayout.findViewById(R.id.title);
		holder.mBookAuthor = (TextView) holder.mBookLayout.findViewById(R.id.author);
		holder.mBookInfo = (TextView) holder.mBookLayout.findViewById(R.id.book_info);
		holder.mBookFree = (ImageView) holder.mBookLayout.findViewById(R.id.cost_free);

		holder.mMenuBtn = (ImageView) itemView.findViewById(R.id.item_menu_btn);
		holder.mMenuBtnLayout = (RelativeLayout) itemView.findViewById(R.id.item_menu_layout);
		holder.mMenuDeleteBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_delete);
		holder.mMenuDownBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_down);

		holder.mDivider = (ImageView) itemView.findViewById(R.id.item_divider);

		itemView.setTag(holder);
		return itemView;
	}

	protected class ViewHolder {
		public View mBookLayout;
		public ImageView mBookCover;
		public TextView mBookTitle;
		public TextView mBookAuthor;
		public TextView mBookInfo;
		public ImageView mBookFree;

		public ImageView mMenuBtn;
		public RelativeLayout mMenuBtnLayout;
		public TextView mMenuDeleteBtn;
		public TextView mMenuDownBtn;

		public ImageView mDivider;
	}

	@Override
	protected List<Book> createList() {
		return new ArrayList<Book>();
	}

	private class ClickListener implements View.OnClickListener {
		private ViewHolder mHolder;
		private int mPosition;
		private Book mBook;
		private View mView;

		public ClickListener(ViewHolder holder, int position, Book book, View view) {
			mHolder = holder;
			mPosition = position;
			mBook = book;
			mView = view;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.item_menu_btn:
				mBooleanArray.clear();
				if (!mHolder.mMenuBtnLayout.isShown()) {
					mBooleanArray.put(mPosition, true);
					notifyDataSetChanged();

					// 列表菜单栏的高度
					int menuHeight = (int) ResourceUtil.getDimens(R.dimen.bookhome_item_menu_height);
					if (mListView.getHeight() - mView.getBottom() < menuHeight) {
						int itemMinHeight = v.getHeight();
						int height = mListView.getHeight() - itemMinHeight - menuHeight;

						int curPosition = mPosition + mListView.getHeaderViewsCount() - height / itemMinHeight;

						if (curPosition < mDataList.size() && curPosition >= 0) {
							mListView.setSelectionFromTop(curPosition, height % itemMinHeight);
						}
					}
				} else {
					notifyDataSetChanged();
				}

				break;

			case R.id.item_menu_btn_delete:
				deleteCollected();
				break;

			case R.id.item_menu_btn_down:
				mBooleanArray.clear();
				notifyDataSetChanged();

				showProgressDialog(R.string.downloading_text);
				reqBookInfo(mBook);
				break;

			default:
				break;
			}
		}

		private void deleteCollected() {
			String msg = String.format(mContext.getString(R.string.note_info), mBook.getTitle());
			CommonDialog.show(mContext, msg, new CommonDialog.DefaultListener() {
				@Override
				public void onRightClick(DialogInterface dialog) {
					deleteCollectedBook(mBook);
					mBooleanArray.clear();
					notifyDataSetChanged();
				}

				@Override
				public void onLeftClick(DialogInterface dialog) {
					mBooleanArray.clear();
					notifyDataSetChanged();
				}
			});
		}
	}

	private void deleteCollectedBook(Book book) {
		if (LoginUtil.isValidAccessToken(mContext) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS)
			return;

		String reqUrl = String.format(ConstantData.URL_DELETE_COLLECTE_BOOK, LoginUtil.getLoginInfo().getAccessToken(),
				book.getBookId(), book.getSid(), book.getBookSrc(), book.getBagId());

		RequestTask task = new RequestTask(new SimpleParser());
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		params.put(EXTRA_TYPE, REQUEST_KEY);
		params.put(EXTRA_OBJECT, book);

		task.setTaskFinishListener(this);
		task.execute(params);
	}

	/**
	 * 显示进度条.
	 * 
	 * @param resId
	 *            the res id
	 */
	private void showProgressDialog(int resId) {
		if (null == mProgressDialog) {
			mProgressDialog = new CustomProDialog(mContext);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
		}

		mProgressDialog.show(resId);
	}

	/**
	 * 隐藏进度条.
	 */
	private void dismissProgressDialog() {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	/**
	 * Req book info.
	 * 
	 * @param book
	 *            the book
	 */
	private void reqBookInfo(Book book) {
		showProgressDialog(R.string.downloading_text);
		String reqUrl = String.format(ConstantData.URL_BOOK_INFO, book.getBookId(), book.getSid(), book.getBookSrc());
		reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
		RequestTask reqTask = new RequestTask(new BookDetailParser());
		reqTask.setTaskFinishListener(this);
		TaskParams params = new TaskParams();
		params.put(RequestTask.PARAM_URL, reqUrl);
		params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
		reqTask.execute(params);
	}

	@Override
	public void onTaskFinished(TaskResult taskResult) {
		if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
			if (taskResult.retObj instanceof BookDetailData) {
				BookDetailData data = (BookDetailData) taskResult.retObj;
				DownBookManager.getInstance().downBook(data.getBook(), true);
				notifyDataSetChanged();
				dismissProgressDialog();
				return;
			} else if (taskResult.retObj instanceof String) {
				RequestTask reqTask = (RequestTask) taskResult.task;

				if (ConstantData.CODE_SUCCESS.equals(String.valueOf(taskResult.retObj))) {
					if (null != reqTask) {
						String extraType = reqTask.getParams().getString(EXTRA_TYPE);
						Book book = (Book) reqTask.getParams().get(EXTRA_OBJECT);

						if (REQUEST_KEY.equals(extraType) && null != book) {
							Toast.makeText(mContext, R.string.bookhome_delete_successed, Toast.LENGTH_SHORT).show();

							CollectedBookList.getInstance().deleteBook(book);
							return;
						}
					}
				}
				dismissProgressDialog();
				Toast.makeText(mContext, R.string.bookhome_delete_failed, Toast.LENGTH_SHORT).show();
				return;
			}
		}
		dismissProgressDialog();
		Toast.makeText(mContext, R.string.bookhome_net_error, Toast.LENGTH_SHORT).show();
	}
}