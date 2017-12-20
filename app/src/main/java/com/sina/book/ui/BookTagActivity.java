package com.sina.book.ui;

import java.io.Serializable;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.BookSummary;
import com.sina.book.data.Chapter;
import com.sina.book.data.MarkItem;
import com.sina.book.image.ImageLoader;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.view.BaseFragment;
import com.sina.book.ui.view.ChapterFragment;
import com.sina.book.ui.view.EpubChapterFragment;
import com.sina.book.ui.view.EpubMarkFragment;
import com.sina.book.ui.view.MarkFragment;
import com.sina.book.ui.view.SummaryFragment;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;

/**
 * 书签、目录界面
 * 
 * @author MarkMjw 修改： YangZhanDong
 */
public class BookTagActivity extends BaseFragmentActivity implements
		OnCheckedChangeListener {

	private View mLayout;
	private ImageView mGoBtn;
	private TextView mBookName;
	private TextView mBookAuthor;

	protected Context mContext;

	public Book mBook = ReadActivity.gBook;

	protected LinearLayout mFragmentView;

	private boolean mNeedClearMark;
	private boolean mNeedClearSummary = false;

	public static FragmentManager mFragmentManager;
	private RadioGroup mRadioGroup;
	private RadioButton[] mRadioButtons;

	private final int RADIO_COUNT = 3;

	BaseFragment mChapterFragment, mMarkFragment, mSummaryFragment;

	// Epub Support新增
	public static final String BOOKTYPE_SINA = "sina"; // 新浪书城
	public static final String BOOKTYPE_ZXIN_EPUB = "zhongxin_epub"; // 中信Epub
	public static final String EXTRA_BOOK_TYPE = "bookType";
	private String mBookType = BOOKTYPE_SINA;
	
	public String bookId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			Serializable s = savedInstanceState.getSerializable("book");
			if (s != null && mBook == null) {
				mBook = (Book) s;
				if(!mBook.isHtmlRead()){
					ReadActivity.gBook = mBook;
				}
			}
		}

		Intent intent = getIntent();
		if (intent!= null) {
			if (intent.hasExtra(EXTRA_BOOK_TYPE)) {
				String bookType = getIntent().getStringExtra(EXTRA_BOOK_TYPE);
				if (!TextUtils.isEmpty(bookType)) {
					mBookType = bookType;
				}
			}
			bookId = intent.getStringExtra("bookId");
			Serializable ser = intent.getSerializableExtra("book");
			if(ser != null){
				mBook = (Book) ser;
			}
		}
		bookId = getIntent().getStringExtra("bookId");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = this;
		setContentView(R.layout.act_book_tag);
		mFragmentManager = getSupportFragmentManager();

		initViews();
		initTitle();
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

			finishActivity(-1, null);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initViews() {
		mFragmentView = (LinearLayout) findViewById(R.id.pager);
		mLayout = findViewById(R.id.catalog_tag_layout);
		mGoBtn = (ImageView) findViewById(R.id.go_btn);
		mBookName = (TextView) findViewById(R.id.book_name);
		mBookAuthor = (TextView) findViewById(R.id.book_author);

		mRadioGroup = (RadioGroup) findViewById(R.id.book_tag_radio);

		mRadioButtons = new RadioButton[RADIO_COUNT];
		for (int i = 0; i < RADIO_COUNT; i++) {
			mRadioButtons[i] = (RadioButton) mRadioGroup
					.findViewWithTag("radio_button" + i);
			if (isEpubBook() && i == 2) {
				mRadioButtons[i].setVisibility(View.GONE);
			} else {
				mRadioButtons[i].setVisibility(View.VISIBLE);
				mRadioButtons[i].setOnCheckedChangeListener(this);
			}
		}
		mRadioButtons[0].setChecked(true);

		updateViews();
	}

	public static void initFragment(BaseFragment bf) {
		changeFragment(bf, true);
	}

	public static void changeFragment(BaseFragment bf) {
		changeFragment(bf, false);
	}

	private static void changeFragment(BaseFragment bf, boolean flag) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.replace(R.id.pager, bf);
		if (!flag) {
			ft.addToBackStack(null);
		}
		ft.commit();
	}

	private void initTitle() {
		if (mBookType.equals(BOOKTYPE_ZXIN_EPUB)) {
			// 获取全局的变量
			FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
			org.geometerplus.fbreader.book.Book book = fbreader.Model.Book;
			if (book != null) {
				// 书名
				mBookName.setText(book.getTitle());
				// 作者
				if (!book.authors().isEmpty()) {
					StringBuilder author = new StringBuilder();
					boolean first = true;
					for (Author a : book.authors()) {
						author.append(first ? "" : ", ");
						author.append(a.DisplayName);
						first = false;
					}
					mBookAuthor.setText(author.toString());
				} else {
					mBookAuthor.setText("未知");
				}
			}
		} else {
			if (mBook != null) {
				mBookName.setText(mBook.getTitle());
				mBookAuthor.setText(mBook.getAuthor());
			}
		}

		mGoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				finishActivity(-1, null);
			}
		});
	}

	private void updateViews() {
		if(mBook != null && mBook.isHtmlRead()){
			mLayout.setBackgroundColor(getResources().getColor(R.color.book_tag_mark_bg));
			mBookName.setTextColor(getResources().getColor(R.color.book_tag_title_color));
			mBookName.setTextColor(getResources().getColor(R.color.book_tag_author_color));
			mRadioButtons[0].setBackgroundResource(R.drawable.selector_radio_book_tag_left_tab);
			mRadioButtons[1].setBackgroundResource(R.drawable.selector_radio_book_tag_right_tab);
			mRadioButtons[2].setBackgroundResource(R.drawable.selector_radio_book_tag_right_tab);

			for (int i = 0; i < RADIO_COUNT; i++) {
				mRadioButtons[i].setTextColor(getResources().getColor(R.drawable.selector_book_tag_radio_font_color));
			}
		}else{
			ReadStyleManager readStyleManager = ReadStyleManager.getInstance(this);

			int readBgColor = readStyleManager.getColorFromIdentifier(this,
					R.color.book_tag_mark_bg);
			mLayout.setBackgroundColor(readBgColor);

			mBookName.setTextColor(readStyleManager.getColorFromIdentifier(this,
					R.color.book_tag_title_color));
			
			mBookAuthor.setTextColor(readStyleManager.getColorFromIdentifier(this,
					R.color.book_tag_author_color));

			mRadioButtons[0].setBackgroundDrawable(readStyleManager
					.getDrawableFromIdentifier(this,
							R.drawable.selector_radio_book_tag_left_tab));
			
			mRadioButtons[1]
					.setBackgroundDrawable(readStyleManager
							.getDrawableFromIdentifier(
									this,
									!isEpubBook() ? R.drawable.selector_radio_book_tag_middle_tab
											: R.drawable.selector_radio_book_tag_right_tab));
			mRadioButtons[2].setBackgroundDrawable(readStyleManager
					.getDrawableFromIdentifier(this,
							R.drawable.selector_radio_book_tag_right_tab));

			for (int i = 0; i < RADIO_COUNT; i++) {
				mRadioButtons[i].setTextColor(readStyleManager
						.getColorStateListFromIdentifier(this,
								R.drawable.selector_book_tag_radio_font_color));
			}
		}
	}

	public void setClearMark(boolean needClearMark) {
		mNeedClearMark = needClearMark;
	}

	public void setClearSummary(boolean needClearSummary) {
		mNeedClearSummary = needClearSummary;
	}

	/**
	 * 结束目录、书签、书摘页
	 * 
	 * @param begin
	 *            开始位置
	 * @param selectChapter
	 *            选择的章节
	 */
	public void finishActivity(long begin, Object obj) {
		Bundle bundle = new Bundle();
		// 中信Epub
		if (mBookType.equals(BOOKTYPE_ZXIN_EPUB)) {
			// do nothing
		} else {
			// 新浪书城
			if (begin >= 0) {
				bundle.putLong("begin", begin);
			}

			if (obj instanceof Chapter) {
				bundle.putSerializable("selectedChapter", (Chapter) obj);
			} else if (obj instanceof MarkItem) {
				bundle.putSerializable("bookmark", (MarkItem) obj);
			} else if (obj instanceof BookSummary) {
				bundle.putSerializable("booksummary", (BookSummary) obj);
			}

			// if (null != selectChapter) {
			// bundle.putSerializable("selectedChapter", selectChapter);
			// }

			if (mNeedClearMark) {
				mBook.clearAllBookMarks();
				ReadActivity.gBook = mBook;
			}

			if (mNeedClearSummary) {
				mBook.clearAllBookSummaries();
				ReadActivity.gBook = mBook;
			}

			// TODO:ouyang 在书签书摘页面可能存在删除行为，返回阅读页时需要重新加载一遍书签书摘
			ReadActivity.gBook.setBookmarks(mBook.getBookmarks());
			ReadActivity.gBook.setBookSummaries(mBook.getBookSummaries());
		}

		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(ReadActivity.MARK_CHAPTER_CODE, intent);
		finish();
		overridePendingTransition(0, R.anim.push_left_out);
	}

	private boolean isEpubBook() {
		return mBookType.equals(BOOKTYPE_ZXIN_EPUB);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {

			if (buttonView == mRadioButtons[0]) {
				if (mChapterFragment == null) {
					if (isEpubBook()) {
						// 中信Epub格式电子书的目录Fragment
						mChapterFragment = new EpubChapterFragment();
					} else {
						mChapterFragment = new ChapterFragment();
					}
				}
				changeFragment(mChapterFragment);

				UserActionManager.getInstance().recordEvent(
						Constants.CLICK_CATALOG_CHAPTER);
			} else if (buttonView == mRadioButtons[1]) {
				if (mMarkFragment == null) {
					if (isEpubBook()) {
						// 中信Epub格式电子书的书签Fragment
						mMarkFragment = new EpubMarkFragment();
					} else {
						mMarkFragment = new MarkFragment();
					}
				}
				changeFragment(mMarkFragment);

				UserActionManager.getInstance().recordEvent(
						Constants.CLICK_CATALOG_MARK);
			} else if (buttonView == mRadioButtons[2]) {
				if (mSummaryFragment == null) {
					mSummaryFragment = new SummaryFragment();
				}
				changeFragment(mSummaryFragment);

				UserActionManager.getInstance().recordEvent(
						Constants.CLICK_CATALOG_SUMMARY);
			}
		}
	}
}
