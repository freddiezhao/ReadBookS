package com.sina.book.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.db.DBService;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.BookTagActivity;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.adapter.ChapterListAdapter;
import com.sina.book.util.ResourceUtil;

/**
 * 目录页面
 * 
 * @author MarkMjw 修改： YangZhanDong
 */
public class ChapterFragment extends BaseFragment implements OnChildClickListener, OnScrollListener, OnClickListener {
	private View mTotalLayout;
	private ExpandableListView mExpandableList;

	private LinearLayout mGroupFloating;
	private TextView mGroupFloatingTitle;
	private ImageView mGroupFloatingNew;
	private ImageView mGroupFloatingIcon;
	private ImageView mGroupFloatingDivider;

	private int mGroupId = -1;
	private int mGroupHeight;
	private int mTitleColor;

	private List<String> mChapterGroupList = new ArrayList<String>();
	private List<List<Chapter>> mChapterChildList = new ArrayList<List<Chapter>>();

	private String mGroupTag = "";

	/**
	 * 当前的章节index.
	 */
	private int mCurPos = -1;

	private ChapterListAdapter mAdapter;
	private Book mBook = ReadActivity.gBook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_chapter, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initIntent();
		initViews();
		initChapterData();
		initListener();
		super.onViewCreated(view, savedInstanceState);
	}

	private void initIntent() {
		Intent intent = getActivity().getIntent();
		mCurPos = intent.getIntExtra("curpos", -1);
	}

	private void initViews() {
		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}

		mGroupTag = getString(R.string.book_dir_group_tag);

		mTotalLayout = root.findViewById(R.id.total_layout);
		mExpandableList = (ExpandableListView) root.findViewById(R.id.lv_chapter);

		mGroupFloating = (LinearLayout) root.findViewById(R.id.lv_chapter_top);
		mGroupFloatingTitle = (TextView) mGroupFloating.findViewById(R.id.chapter_group);
		mGroupFloatingNew = (ImageView) mGroupFloating.findViewById(R.id.chapter_group_new);
		mGroupFloatingIcon = (ImageView) mGroupFloating.findViewById(R.id.chapter_group_icon);
		mGroupFloatingDivider = (ImageView) mGroupFloating.findViewById(R.id.chapter_group_divider);

		updateViews();
	}

	private void updateViews() {
		ReadStyleManager styleManager = ReadStyleManager.getInstance(getActivity());

		int readBgColor = styleManager.getColorFromIdentifier(getActivity(), R.color.book_tag_mark_bg);
		mTotalLayout.setBackgroundColor(readBgColor);
		mExpandableList.setBackgroundColor(readBgColor);
		mGroupFloating.setBackgroundColor(readBgColor);

		Drawable divider = styleManager.getDrawableFromIdentifier(getActivity(), R.drawable.divider_line);
		mExpandableList.setDivider(divider);
		mExpandableList.setChildDivider(divider);

		// 设置浮动分组的分割线
		if (ReadStyleManager.READ_MODE_NIGHT == styleManager.getReadMode()) {
			mGroupFloatingDivider.setImageResource(R.drawable.divider_line_night);
		} else {
			mGroupFloatingDivider.setImageResource(R.drawable.divider_line);
		}

		mGroupFloatingIcon.setImageDrawable(styleManager.getDrawableFromIdentifier(getActivity(),
				R.drawable.expand_y_normal));

		mTitleColor = styleManager.getColorFromIdentifier(getActivity(), R.color.book_chapter_title_font_color);
	}

	private void initListener() {
		// 设置滚动事件
		mExpandableList.setOnScrollListener(this);

		mExpandableList.setOnChildClickListener(this);
		mExpandableList.setSmoothScrollbarEnabled(true);

		mGroupFloating.setOnClickListener(this);
	}

	private void initChapterData() {
		mAdapter = new ChapterListAdapter(getActivity());
		mExpandableList.setAdapter(mAdapter);
		initChapterListData();
	}

	private void initChapterListData() {
		if (mBook == null) {
			return;
		}
		if (null == mBook.getChapters() || mBook.getChapters().isEmpty()) {
			ArrayList<Chapter> chapters = DBService.getAllChapter(mBook);
			if (null != chapters && !chapters.isEmpty()) {
				mBook.setChapters(chapters);
			}
		}

		refreshChapterList(mBook.getChapters());
	}

	private void refreshChapterList(ArrayList<Chapter> chapterList) {
		initChapterGroup(chapterList);

		mAdapter.setCurPos(mCurPos);
		int groupPosition = mCurPos / 100;
		if (mCurPos % 100 != 0) {
			groupPosition += 1;
		}

		groupPosition = groupPosition - 1 < 0 ? 0 : groupPosition - 1;
		int childPosition = mCurPos - groupPosition * 100 - 3;
		childPosition = childPosition < 0 ? -1 : childPosition;
		mExpandableList.expandGroup(groupPosition);
		mExpandableList.setSelected(true);
		mExpandableList.setSelectedGroup(groupPosition);
		mExpandableList.setSelectedChild(groupPosition, childPosition, true);
		mAdapter.notifyDataSetChanged();

		// 初始化当前章节所在分组ID
		mGroupId = groupPosition;

		// 刷新浮动分组数据
		updateGroupFloatingData(groupPosition);
	}

	private void initChapterGroup(ArrayList<Chapter> chapterList) {
		if (null != chapterList && chapterList.size() > 0) {
			mChapterGroupList.clear();
			mChapterChildList.clear();

			if (chapterList.size() < 100) {
				mChapterGroupList.add(String.format(mGroupTag, 1, chapterList.size()));
				mChapterChildList.add(chapterList);

			} else {
				int chapters = chapterList.size();
				int groupNum = chapters / 100;
				if (chapters % 100 != 0) {
					groupNum += 1;
				}

				List<Chapter> tempList;
				for (int i = 0; i < groupNum; i++) {
					int temp = i * 100 + 1;
					if ((i * 100 + 99) < chapterList.size()) {
						tempList = chapterList.subList(i * 100, i * 100 + 100);
						String index = temp < 100 ? "00" + temp : temp + "";
						mChapterGroupList.add(String.format(mGroupTag, index, 100 * (i + 1)));

					} else {
						tempList = chapterList.subList(temp - 1, chapterList.size());
						int end = chapterList.size() > temp ? chapterList.size() : temp;
						mChapterGroupList.add(String.format(mGroupTag, temp, end));
					}

					mChapterChildList.add(tempList);
				}
			}

			mAdapter.setGroupList(mChapterGroupList);
			mAdapter.setDataList(mChapterChildList);
		}
	}

	/**
	 * 更新浮动分组的数据
	 * 
	 * @param groupId
	 */
	private void updateGroupFloatingData(int groupId) {
		mGroupFloatingTitle.setText(String.valueOf(mAdapter.getGroup(groupId)));
		mGroupFloatingDivider.setVisibility(View.VISIBLE);

		// 区别是否有更新的章节
		if (mAdapter.hasNewChapter(groupId)) {
			mGroupFloatingTitle.setTextColor(ResourceUtil.getColor(R.color.book_tag_radio_font_color_checked));
			mGroupFloatingNew.setVisibility(View.VISIBLE);
		} else {
			mGroupFloatingTitle.setTextColor(mTitleColor);
			mGroupFloatingNew.setVisibility(View.GONE);
		}

		if (mExpandableList.isGroupExpanded(groupId)) {
			mGroupFloatingIcon.setImageResource(R.drawable.expand_y_normal);
		} else {
			mGroupFloatingIcon.setImageResource(R.drawable.expand_n_normal);
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		long begin = 0;
		Chapter chapter;

		// 得到当前选中的章节
		chapter = (Chapter) mAdapter.getChild(groupPosition, childPosition);
		begin = chapter.getStartPos();

		// 清理new标签
		removeNewTag(chapter);

		if (getActivity() instanceof BookTagActivity) {
			ReadActivity.setChapterReadEntrance("阅读页-目录");
			((BookTagActivity) getActivity()).finishActivity(begin, chapter);
		}
		return true;
	}

	/**
	 * 去掉目录中章节的new标签
	 * 
	 * @param chapter
	 */
	private void removeNewTag(final Chapter chapter) {
		if (null != chapter && Chapter.NEW == chapter.getTag()) {
			chapter.setTag(Chapter.NORMAL);
			mAdapter.notifyDataSetChanged();

			new GenericTask() {
				@Override
				protected TaskResult doInBackground(TaskParams... params) {
					mBook.updateChapter(chapter);
					DBService.updateChapter(mBook, chapter);
					return null;
				}
			}.execute();
		}
	}

	@Override
	public void onSelected() {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// 计算point(0,0)其实就是firstVisibleItem
		int npos = view.pointToPosition(0, 0);
		if (npos == AdapterView.INVALID_POSITION) {
			return;
		}

		long pos = mExpandableList.getExpandableListPosition(npos);
		// 获取第一行Child的id
		int childId = ExpandableListView.getPackedPositionChild(pos);
		// 获取第一行Group的id
		int groupId = ExpandableListView.getPackedPositionGroup(pos);

		// 第一行不是显示child,得到对应的Group并计算出高度
		if (childId == AdapterView.INVALID_POSITION) {
			// 第一行的view
			View groupView = mExpandableList.getChildAt(npos - mExpandableList.getFirstVisiblePosition());
			// 获取group的高度
			mGroupHeight = groupView.getHeight();

			mGroupFloating.setVisibility(View.GONE);
		}

		if (groupId != AdapterView.INVALID_POSITION) {
			// 如果第一行显示的是Group，且该Group已展开则显示
			if (mExpandableList.isGroupExpanded(groupId)) {
				updateGroupFloatingData(groupId);

				mGroupFloating.setVisibility(View.VISIBLE);
			}
		} else {
			// 如果第一行显示的不是是Group，则隐藏
			mGroupFloating.setVisibility(View.GONE);
		}

		if (mGroupHeight == 0) {
			return;
		}

		// 如果指示器显示的不是当前Group
		if (groupId != mGroupId) {
			// 将指示器更新为当前Group
			mAdapter.getGroupView(groupId, mExpandableList.isGroupExpanded(groupId), mGroupFloating.getChildAt(0), null);
			mGroupId = groupId;
		}

		// 如果此时Group的id无效，则返回
		if (mGroupId == AdapterView.INVALID_POSITION) {
			return;
		}

		// 计算point (0, mGroupHeight) 下面是形成往上推出的效果
		int showHeight = mGroupHeight;
		// 第二个item的位置
		int endPos = mExpandableList.pointToPosition(0, mGroupHeight);
		// 如果无效直接返回
		if (endPos == AdapterView.INVALID_POSITION) {
			return;
		}

		long pos2 = mExpandableList.getExpandableListPosition(endPos);
		// 获取第二个group的id
		int groupId2 = ExpandableListView.getPackedPositionGroup(pos2);

		// 如果不等于指示器当前的group
		if (groupId2 != mGroupId) {
			View viewNext = mExpandableList.getChildAt(endPos - mExpandableList.getFirstVisiblePosition());
			showHeight = viewNext.getTop();
		}

		// 更新Group位置
		MarginLayoutParams layoutParams = (MarginLayoutParams) mGroupFloating.getLayoutParams();
		layoutParams.topMargin = -(mGroupHeight - showHeight);
		mGroupFloating.setLayoutParams(layoutParams);
	}

	@Override
	public void onClick(View v) {
		if (v == mGroupFloating) {
			if (mExpandableList.isGroupExpanded(mGroupId)) {
				mExpandableList.collapseGroup(mGroupId);
			} else {
				mExpandableList.expandGroup(mGroupId);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}
}