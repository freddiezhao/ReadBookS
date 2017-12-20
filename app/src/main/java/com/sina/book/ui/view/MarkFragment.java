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
import com.sina.book.data.MarkItem;
import com.sina.book.db.DBService;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.BookTagActivity;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.adapter.BookMarkAdapter;
import com.sina.book.util.ResourceUtil;

/**
 * 书签页面
 * 
 * @author MarkMjw 修改： YangZhanDong
 */
public class MarkFragment extends BaseFragment implements OnItemClickListener,
		OnItemLongClickListener {

	/** 菜单-清除所有书签. */
	public final static int MENU_CLEAR_MARK = Menu.FIRST;

	/** 书签列表. */
	private ListView mMarkList;

	/** 该Fragment是否初始化 */
	private boolean mIsInit;

	private BookMarkAdapter mMarkAdapter;
	private Book mBook = ReadActivity.gBook;
	private MarkItem mCurMarkItem;

	private View mHasNoMarkView;
	private TextView mHasNoMarkText;

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
		initMarkData();
		super.onViewCreated(view, savedInstanceState);
	}

	private void initViews() {

		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}

		mMarkList = (ListView) root.findViewById(R.id.lv_mark);
		mHasNoMarkView = root.findViewById(R.id.has_not_mark_view);
		mHasNoMarkText = (TextView) mHasNoMarkView
				.findViewById(R.id.has_not_mark_text);

		updateViews();
	}

	private void updateViews() {
		ReadStyleManager readStyleManager = ReadStyleManager
				.getInstance(getActivity());

		int readBgColor = readStyleManager.getColorFromIdentifier(
				getActivity(), R.color.book_tag_mark_bg);

		mMarkList.setBackgroundColor(readBgColor);
		mMarkList.setDivider(readStyleManager.getDrawableFromIdentifier(
				getActivity(), R.drawable.divider_line));
		mHasNoMarkView.setBackgroundColor(readBgColor);

		int hasNotMarkColor = readStyleManager.getColorFromIdentifier(
				getActivity(), R.color.has_not_mark_font_color);
		mHasNoMarkText.setTextColor(hasNotMarkColor);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(0, MENU_CLEAR_MARK, 0,
				getString(R.string.clear_all_mark));
		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CLEAR_MARK:
			if (mMarkAdapter != null && mMarkAdapter.getCount() > 0) {
				showClearAllMarkDialog();
			} else {
				Toast.makeText(getActivity(), getString(R.string.no_mark),
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

		mCurMarkItem = (MarkItem) mMarkAdapter.getItem(position);
		showClearCurrentMarkDialog();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		long begin = -1;

		if (position >= 0 && position < mMarkAdapter.getCount()) {
			MarkItem mark = (MarkItem) mMarkAdapter.getItem(position);
			begin = mark.getBegin();
			if (getActivity() instanceof BookTagActivity) {
				ReadActivity.setChapterReadEntrance("阅读页-书签");
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
			initMarkData();
			mIsInit = true;
		}

	}

	private void initMarkData() {

		mMarkAdapter = new BookMarkAdapter(getActivity());
		mMarkList.setAdapter(mMarkAdapter);
		mMarkList.setOnItemClickListener(this);
		mMarkList.setOnItemLongClickListener(this);

		initMarkListData();

	}

	private void initMarkListData() {
		if (mBook == null) {
			return;
		}
		if (mBook.getBookmarks().isEmpty()) {
			List<MarkItem> list = DBService.getAllBookMark(mBook);
			if (null != list && !list.isEmpty()) {
				mBook.setBookmarks(list);
			} else {
				mHasNoMarkView.setVisibility(View.VISIBLE);
			}
		} else {
			mHasNoMarkView.setVisibility(View.GONE);
		}
		mMarkAdapter.setDataList(mBook.getBookmarks());
		mMarkAdapter.notifyDataSetChanged();
	}

	/**
	 * 显示清除所有书签对话框
	 */
	private void showClearAllMarkDialog() {
		CommonDialog.show(getActivity(), R.string.clear_all_mark,
				R.string.clear_all_mark_tip,
				new CommonDialog.DefaultListener() {

					@Override
					public void onRightClick(DialogInterface dialog) {
						mMarkAdapter.clearDataList();
						mMarkAdapter.notifyDataSetChanged();
						mHasNoMarkView.setVisibility(View.VISIBLE);
						// 清除了所有书签，通知阅读页面
						((BookTagActivity) MarkFragment.this.getActivity())
								.setClearMark(true);
						new GenericTask() {
							@Override
							protected TaskResult doInBackground(
									TaskParams... params) {
								DBService.deleteAllBookMark(mBook);
								return null;
							}
						}.execute();
					}
				});
	}

	/**
	 * 显示清除选中书签的对话框
	 */
	private void showClearCurrentMarkDialog() {
		String title = ResourceUtil.getString(R.string.book_tag_function);
		List<String> items = Arrays.asList(ResourceUtil
				.getStringArrays(R.array.book_tag_functions));
		ListDialog.show(getActivity(), title, items,
				new ListDialog.ItemClickListener() {
					@Override
					public void onItemClick(DialogInterface dialog, int position) {
						switch (position) {
						case 0:
							mBook.getBookmarks().remove(mCurMarkItem);
							mMarkAdapter.getDataList().remove(mCurMarkItem);
							mMarkAdapter.notifyDataSetChanged();
							if (mMarkAdapter.getCount() == 0) {
								mHasNoMarkView.setVisibility(View.VISIBLE);
							}
							new GenericTask() {
								@Override
								protected TaskResult doInBackground(
										TaskParams... params) {
									DBService.deleteBookMark(mCurMarkItem);
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