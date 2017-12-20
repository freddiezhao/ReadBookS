package com.sina.book.ui.view;

import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.BookSummary;
import com.sina.book.db.DBService;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.BookTagActivity;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.adapter.BookSummaryAdapter;
import com.sina.book.util.ResourceUtil;

/**
 * 书摘页面
 * 
 * @author MarkMjw
 * @date 2013-3-7
 */
public class SummaryFragment extends BaseFragment implements
		OnItemClickListener, OnItemLongClickListener {

	/** 菜单-清除所有书摘. */
	public final static int MENU_CLEAR = Menu.FIRST;

	/** 书摘列表. */
	private ListView mSummaryList;

	/** 该Fragment是否初始化 */
	private boolean mIsInit;

	private BookSummaryAdapter mSummaryAdapter;
	private Book mBook = ReadActivity.gBook;
	private BookSummary mCurSummaryItem;

	private View mHasNotSummaryView;
	private TextView mHasNotSummaryText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_mark, container, false);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initViews();
		initSummaryData();
		super.onViewCreated(view, savedInstanceState);
	}

	private void initViews() {

		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}

		mSummaryList = (ListView) root.findViewById(R.id.lv_mark);
		mHasNotSummaryView = root.findViewById(R.id.has_not_summary_view);
		mHasNotSummaryText = (TextView) mHasNotSummaryView
				.findViewById(R.id.has_not_summary_text);

		updateViews();
	}

	private void updateViews() {
		ReadStyleManager readStyleManager = ReadStyleManager
				.getInstance(getActivity());

		int readBgColor = readStyleManager.getColorFromIdentifier(
				getActivity(), R.color.book_tag_mark_bg);

		mSummaryList.setBackgroundColor(readBgColor);
		mSummaryList.setDivider(readStyleManager.getDrawableFromIdentifier(
				getActivity(), R.drawable.divider_line));
		mHasNotSummaryView.setBackgroundColor(readBgColor);

		int hasNotSummaryColor = readStyleManager.getColorFromIdentifier(
				getActivity(), R.color.has_not_mark_font_color);
		mHasNotSummaryText.setTextColor(hasNotSummaryColor);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(0, MENU_CLEAR, 0,
				getString(R.string.clear_all_summary));
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CLEAR:
			if (mSummaryAdapter != null && mSummaryAdapter.getCount() > 0) {
				showClearAllMarkDialog();
			} else {
				Toast.makeText(getActivity(), getString(R.string.no_summary),
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// 震动70毫秒
		Vibrator vib = (Vibrator) getActivity().getSystemService(
				Service.VIBRATOR_SERVICE);
		vib.vibrate(70);

		mCurSummaryItem = (BookSummary) mSummaryAdapter.getItem(position);
		showClearCurrentSummaryDialog();
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		long begin = -1;

		if (position >= 0 && position < mSummaryAdapter.getCount()) {
			BookSummary mark = (BookSummary) mSummaryAdapter.getItem(position);
			begin = mark.getBegin();
			if (getActivity() instanceof BookTagActivity) {
				ReadActivity.setChapterReadEntrance("阅读页-书摘");
				((BookTagActivity) getActivity()).finishActivity(begin, mark);
			}
		}
	}

	@Override
	public void onSelected() {
		if (isDestroyed()) {
			return;
		}

		if (!mIsInit) {
			initSummaryData();
			mIsInit = true;
		}

	}

	private void initSummaryData() {
		mSummaryAdapter = new BookSummaryAdapter(getActivity());
		mSummaryList.setAdapter(mSummaryAdapter);
		mSummaryList.setOnItemClickListener(this);
		mSummaryList.setOnItemLongClickListener(this);

		initSummaryListData();

	}

	private void initSummaryListData() {
		if (mBook == null) {
			return;
		}
		if (mBook.getBookSummaries().isEmpty()) {
			List<BookSummary> list = DBService.getAllBookSummary(mBook);
			if (null != list && !list.isEmpty()) {
				mBook.setBookSummaries(list);
			} else {
				mHasNotSummaryView.setVisibility(View.VISIBLE);
			}
		} else {
			mHasNotSummaryView.setVisibility(View.GONE);
		}
		mSummaryAdapter.setDataList(mBook.getBookSummaries());
		mSummaryAdapter.notifyDataSetChanged();
	}

	/**
	 * 显示清除所有书摘对话框
	 */
	private void showClearAllMarkDialog() {
		CommonDialog.show(getActivity(), R.string.clear_all_summary,
				R.string.clear_all_summary_tip,
				new CommonDialog.DefaultListener() {
					@Override
					public void onRightClick(DialogInterface dialog) {
						mSummaryAdapter.clearDataList();
						mSummaryAdapter.notifyDataSetChanged();
						mHasNotSummaryView.setVisibility(View.VISIBLE);
						// 清除了所有书摘，通知阅读页面
						((BookTagActivity) SummaryFragment.this.getActivity())
								.setClearSummary(true);
						new GenericTask() {
							@Override
							protected TaskResult doInBackground(
									TaskParams... params) {
								DBService.deleteAllBookSummary(mBook);
								return null;
							}
						}.execute();
					}
				});
	}

	/**
	 * 显示清除选中书摘的对话框
	 */
	private void showClearCurrentSummaryDialog() {
		String title = ResourceUtil.getString(R.string.book_tag_function);
		List<String> items = Arrays.asList(ResourceUtil
				.getStringArrays(R.array.book_tag_functions));
		ListDialog.show(getActivity(), title, items,
				new ListDialog.ItemClickListener() {
					@Override
					public void onItemClick(DialogInterface dialog, int position) {
						switch (position) {
						case 0:
							mBook.getBookSummaries().remove(mCurSummaryItem);
							mSummaryAdapter.getDataList().remove(
									mCurSummaryItem);
							mSummaryAdapter.notifyDataSetChanged();
							if (mSummaryAdapter.getCount() == 0) {
								mHasNotSummaryView.setVisibility(View.VISIBLE);
							}
							new GenericTask() {
								@Override
								protected TaskResult doInBackground(
										TaskParams... params) {
									DBService
											.deleteBookSummary(mCurSummaryItem);
									return null;
								}
							}.execute();
							break;
						default:
							break;
						}
					}
				});
	}
}