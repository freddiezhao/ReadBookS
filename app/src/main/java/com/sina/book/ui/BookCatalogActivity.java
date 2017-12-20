package com.sina.book.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
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
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.db.DBService;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.adapter.ChapterListAdapter;
import com.sina.book.util.ResourceUtil;

/**
 * 目录页面
 * 
 * @author YangZhanDong
 */
public class BookCatalogActivity extends BaseActivity implements
		OnChildClickListener, OnScrollListener, OnClickListener {

	protected Context mContext;

	private ImageView mGoBtn;
	private TextView mBookName;
	private TextView mBookAuthor;
	private ImageView mDivider;

	private Book mBook;
	// private Book mBook = ReadActivity.gBook;

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

	private String mBookKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			Serializable s = savedInstanceState.getSerializable("book");
			if (s != null && mBook == null) {
				mBook = (Book) s;
				// ReadActivity.gBook = mBook;
			}
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = this;
		setContentView(R.layout.act_book_catalog);

		initIntent();
		initViews();
		initTitle();
		initChapterData();
		initListener();
	}

	@Override
	protected void onDestroy() {
		ImageLoader.getInstance().releaseContext(this);
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mBook != null) {
			outState.putSerializable("book", mBook);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initIntent() {
		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			mBookKey = bundle.getString("prikey");

			mBook = (Book) bundle.getSerializable("book");
			Book book = DownBookManager.getInstance().getBook(mBook);
			if (book != null) {
				// 判断本地（加入书架）书籍是否有章节信息
				if (book.getChapters() != null && book.getChapters().size() > 0) {
					// ReadActivity.gBook = book;
					mBook = book;
				} else {
					ArrayList<Chapter> chapters = DBService.getAllChapter(book);
					if (chapters != null && chapters.size() > 0) {
						book.setChapters(chapters);
						// ReadActivity.gBook = book;
						mBook = book;
					} else {
						// ReadActivity.gBook = mBook;
					}
				}
			} else {
				// ReadActivity.gBook = mBook;
			}
		}
	}

	private void initViews() {
		Bitmap dotHBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.list_divide_dot);
		BitmapDrawable dotHDrawable = new BitmapDrawable(getResources(),
				dotHBitmap);
		dotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		dotHDrawable.setDither(true);

		mGoBtn = (ImageView) findViewById(R.id.go_btn);
		mBookName = (TextView) findViewById(R.id.book_name);
		mBookAuthor = (TextView) findViewById(R.id.book_author);

		mDivider = (ImageView) findViewById(R.id.book_tag_divider);
		mDivider.setBackgroundDrawable(dotHDrawable);

		mGroupTag = getString(R.string.book_dir_group_tag);

		mExpandableList = (ExpandableListView) findViewById(R.id.lv_chapter);

		mGroupFloating = (LinearLayout) findViewById(R.id.lv_chapter_top);
		mGroupFloatingTitle = (TextView) mGroupFloating
				.findViewById(R.id.chapter_group);
		mGroupFloatingNew = (ImageView) mGroupFloating
				.findViewById(R.id.chapter_group_new);
		mGroupFloatingIcon = (ImageView) mGroupFloating
				.findViewById(R.id.chapter_group_icon);
		mGroupFloatingDivider = (ImageView) mGroupFloating
				.findViewById(R.id.chapter_group_divider);

		updateViews();
	}

	private void updateViews() {

		mExpandableList.setBackgroundColor(getResources().getColor(
				R.color.public_bg));
		mGroupFloating.setBackgroundColor(getResources().getColor(
				R.color.public_bg));

		Drawable divider = getResources().getDrawable(R.drawable.divider_line);
		mExpandableList.setDivider(divider);
		mExpandableList.setChildDivider(divider);

		// 设置浮动分组的分割线
		mGroupFloatingDivider.setImageResource(R.drawable.divider_line);

		mGroupFloatingIcon.setImageDrawable(getResources().getDrawable(
				R.drawable.expand_y_normal));

		mTitleColor = getResources().getColor(
				R.color.book_chapter_title_font_color);
	}

	private void initTitle() {
		if (mBook != null) {
			mBookName.setText(mBook.getTitle());
			mBookAuthor.setText(mBook.getAuthor());
		}

		mGoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				finish();
			}
		});
	}

	/**
	 * 结束目录
	 * 
	 * @param begin
	 *            开始位置
	 * @param selectChapter
	 *            选择的章节
	 */
	public void finishActivity(long begin, Chapter selectChapter) {
		// Intent intent = new Intent();
		// if (begin >= 0) {
		// intent.putExtra("begin", begin);
		// }
		// if (null != selectChapter) {
		// intent.putExtra("selectedChapter", selectChapter);
		// }

		/*
		 * TODO: 如果该书籍是已下载&&已阅读，书架记录中有存储总页数和当前页数； 再次从详情页的目录或者
		 * 最新章节点击进入，当前页数残留了上次的页数，造成页数显示bug； 在此清楚下当前页码。
		 */
		mBook.getBookPage().setCurPage(-1);

		ReadActivity.setChapterReadEntrance("书籍详情页-目录");
		if (mBook != null
				&& Math.abs(mBook.getDownloadInfo().getProgress() - 1.0) < 0.0001
				&& mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {
			ReadActivity.launch(mContext, mBook, false, selectChapter, false,
					mBookKey);
		} else {
			// 否则认为是在线试读
			ReadActivity.launch(mContext, mBook, true, selectChapter, false,
					mBookKey);
		}
		finish();
	}

	private void initListener() {
		// 设置滚动事件
		mExpandableList.setOnScrollListener(this);

		mExpandableList.setOnChildClickListener(this);
		mExpandableList.setSmoothScrollbarEnabled(true);

		mGroupFloating.setOnClickListener(this);
	}

	private void initChapterData() {
		mAdapter = new ChapterListAdapter(this,
				ChapterListAdapter.KEY_BOOK_CATALOG_ACTIVITY);
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

		mCurPos = mBook
				.getCurrentChapterIndex(mBook.getReadInfo().getLastPos());
		if (mCurPos < 0) {
			mCurPos = 0;
		}
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
				mChapterGroupList.add(String.format(mGroupTag, 1,
						chapterList.size()));
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
						mChapterGroupList.add(String.format(mGroupTag, index,
								100 * (i + 1)));

					} else {
						tempList = chapterList.subList(temp - 1,
								chapterList.size());
						int end = chapterList.size() > temp ? chapterList
								.size() : temp;
						mChapterGroupList.add(String.format(mGroupTag, temp,
								end));
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
			mGroupFloatingTitle.setTextColor(ResourceUtil
					.getColor(R.color.book_tag_radio_font_color_checked));
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
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		long begin = 0;
		Chapter chapter;

		chapter = (Chapter) mAdapter.getChild(groupPosition, childPosition);
		begin = chapter.getStartPos();

		// 清理new标签
		removeNewTag(chapter);

		finishActivity(begin, chapter);

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
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
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
			View groupView = mExpandableList.getChildAt(npos
					- mExpandableList.getFirstVisiblePosition());
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
			mAdapter.getGroupView(groupId,
					mExpandableList.isGroupExpanded(groupId),
					mGroupFloating.getChildAt(0), null);
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
			View viewNext = mExpandableList.getChildAt(endPos
					- mExpandableList.getFirstVisiblePosition());
			showHeight = viewNext.getTop();
		}

		// 更新Group位置
		MarginLayoutParams layoutParams = (MarginLayoutParams) mGroupFloating
				.getLayoutParams();
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