package com.sina.book.ui.view;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.BookmarkEditActivity;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.Bookmark.DateType;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.BookTagActivity;
import com.sina.book.util.ResourceUtil;

/**
 * 展示Epub的书签Fragment(由阅读页点击工具栏->目录进入)
 * 
 * @author chenjl
 * 
 */
public class EpubMarkFragment extends BaseFragment {

	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	/** 该Fragment是否初始化 */
	private boolean mIsInit;

	/** 书签列表. */
	private ListView mMarkList;

	private View mHasNoMarkView;
	private TextView mHasNoMarkText;

	private volatile Book myBook;
	private final ZLResource myResource = ZLResource.resource("bookmarksView");
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();
	private volatile BookmarksAdapter myThisBookAdapter;
	// 日期格式化
	private SimpleDateFormat mFormat = new SimpleDateFormat("MM月dd日 HH:mm",
			Locale.getDefault());

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mark, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initView();
		initMarkData();
		super.onViewCreated(view, savedInstanceState);
	}

	private void initView() {
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
		BookTagActivity activity = (BookTagActivity) getActivity();
		if(activity.mBook.isHtmlRead()){
			mMarkList.setBackgroundColor(getResources().getColor(R.color.book_tag_mark_bg));
			mMarkList.setDivider(getResources().getDrawable(R.drawable.divider_line));
			mHasNoMarkText.setTextColor(getResources().getColor( R.color.has_not_mark_font_color));
		}else{
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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		myCollection.unbind();
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

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo()).position;
		final ListView view = mMarkList;
		final Bookmark bookmark = ((BookmarksAdapter) view.getAdapter())
				.getItem(position);
		switch (item.getItemId()) {
		case OPEN_ITEM_ID:
			gotoBookmark(bookmark);
			return true;
		case EDIT_ITEM_ID:
			final Intent intent = new Intent(getActivity(),
					BookmarkEditActivity.class);
			OrientationUtil.startActivityForResult(getActivity(), intent, 1);
			// TODO: implement
			return true;
		case DELETE_ITEM_ID:
			myCollection.deleteBookmark(bookmark);
			if (myThisBookAdapter != null) {
				myThisBookAdapter.remove(bookmark);
			}
			if (myThisBookAdapter.getCount() == 0) {
				mHasNoMarkView.setVisibility(View.VISIBLE);
			}
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private class Initializer implements Runnable {
		public void run() {
			if (myBook != null) {
				for (BookmarkQuery query = new BookmarkQuery(myBook, 20);; query = query
						.next()) {
					final List<Bookmark> thisBookBookmarks = myCollection
							.bookmarks(query);
					if (thisBookBookmarks.isEmpty()) {
						break;
					}
					myThisBookAdapter.addAll(thisBookBookmarks);
				}
			}

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (myThisBookAdapter.getCount() == 0) {
						mHasNoMarkView.setVisibility(View.VISIBLE);
					}
				}
			});
			// for (BookmarkQuery query = new BookmarkQuery(20);; query = query
			// .next()) {
			// final List<Bookmark> allBookmarks = myCollection
			// .bookmarks(query);
			// if (allBookmarks.isEmpty()) {
			// break;
			// }
			// myAllBooksAdapter.addAll(allBookmarks);
			// }
			// runOnUiThread(new Runnable() {
			// public void run() {
			// setProgressBarIndeterminateVisibility(false);
			// }
			// });
		}
	}

	private void initMarkData() {
		FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		myBook = fbreader.getCurrentBook();

		myCollection.bindToService(getActivity(), new Runnable() {
			public void run() {
				if (myBook != null) {
					myThisBookAdapter = new BookmarksAdapter(mMarkList, false);
				} else {
					// findViewById(R.id.this_book).setVisibility(View.GONE);
					mHasNoMarkView.setVisibility(View.VISIBLE);
				}
				new Thread(new Initializer()).start();
			}
		});
	}

	private void addBookmark() {
		// final Bookmark bookmark =
		// FBReaderIntents.getBookmarkExtra(getIntent());
		// if (bookmark != null) {
		// myCollection.saveBookmark(bookmark);
		// myThisBookAdapter.add(bookmark);
		// myAllBooksAdapter.add(bookmark);
		// }
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.getBookId());
		if (book != null) {
			//TODO:
			BookTagActivity activity = (BookTagActivity) getActivity();
			FBReader.openBookActivity(activity, book, activity.mBook, bookmark);
		} else {
			UIUtil.showErrorMessage(getActivity(), "cannotOpenBook");
		}
	}

	private final class BookmarksAdapter extends BaseAdapter implements
			AdapterView.OnItemClickListener,
			AdapterView.OnItemLongClickListener,
			View.OnCreateContextMenuListener {
		
		private final List<Bookmark> myBookmarks = Collections
				.synchronizedList(new LinkedList<Bookmark>());
		
		private final boolean myShowAddBookmarkItem;
		private Bookmark mCurMarkItem;

		BookmarksAdapter(ListView listView, boolean showAddBookmarkItem) {
			myShowAddBookmarkItem = showAddBookmarkItem;
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
			// 上下文菜单先毙掉
			// listView.setOnCreateContextMenuListener(this);
		}

		public List<Bookmark> bookmarks() {
			return Collections.unmodifiableList(myBookmarks);
		}

		public void addAll(final List<Bookmark> bookmarks) {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarks) {
						for (Bookmark b : bookmarks) {
							final int position = Collections.binarySearch(
									myBookmarks, b, myComparator);
							if (position < 0) {
								myBookmarks.add(-position - 1, b);
							}
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void add(final Bookmark b) {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarks) {
						final int position = Collections.binarySearch(
								myBookmarks, b, myComparator);
						if (position < 0) {
							myBookmarks.add(-position - 1, b);
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void remove(final Bookmark b) {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					myBookmarks.remove(b);
					notifyDataSetChanged();
				}
			});
		}

		public void clear() {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					myBookmarks.clear();
					notifyDataSetChanged();
				}
			});
		}

		public void onCreateContextMenu(ContextMenu menu, View view,
				ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
			if (getItem(position) != null) {
				menu.setHeaderTitle(getItem(position).getText());
				menu.add(0, OPEN_ITEM_ID, 0, myResource.getResource("open")
						.getValue());
				// menu.add(0, EDIT_ITEM_ID, 0,
				// myResource.getResource("edit").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, myResource.getResource("delete")
						.getValue());
			}
		}

		// @Override
		// public View getView(int position, View convertView, ViewGroup parent)
		// {
		// final View view = (convertView != null) ? convertView
		// : LayoutInflater.from(parent.getContext()).inflate(
		// R.layout.bookmark_item, parent, false);
		// final ImageView imageView = ViewUtil.findImageView(view,
		// R.id.bookmark_item_icon);
		// final TextView textView = ViewUtil.findTextView(view,
		// R.id.bookmark_item_text);
		// final TextView bookTitleView = ViewUtil.findTextView(view,
		// R.id.bookmark_item_booktitle);
		//
		// final Bookmark bookmark = getItem(position);
		// if (bookmark == null) {
		// imageView.setVisibility(View.VISIBLE);
		// imageView.setImageResource(R.drawable.ic_list_plus);
		// textView.setText(myResource.getResource("new").getValue());
		// bookTitleView.setVisibility(View.GONE);
		// } else {
		// imageView.setVisibility(View.GONE);
		// textView.setText(bookmark.getText());
		// if (myShowAddBookmarkItem) {
		// bookTitleView.setVisibility(View.GONE);
		// } else {
		// bookTitleView.setVisibility(View.VISIBLE);
		// bookTitleView.setText(bookmark.getBookTitle());
		// }
		// }
		// return view;
		// }
		
		private TOCTree getTOCElement(int paraIndex)
		{
			FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
			
			TOCTree treeToSelect = null;
			for (TOCTree tree : fbreader.Model.TOCTree) {
				final TOCTree.Reference reference = tree.getReference();
				if (reference == null) {
					continue;
				}
				if (reference.ParagraphIndex > paraIndex) {
					break;
				}
				treeToSelect = tree;
			}
			return treeToSelect;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView.getTag() == null) {
				convertView = createView();
			}
			
			ViewHolder holder = (ViewHolder) convertView.getTag();
			// MarkItem item = mDataList.get(position);
			final Bookmark bookmark = getItem(position);
			
			int paragraphIndex = bookmark.getParagraphIndex();
			
//			FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
//			ZLTextModel model = fbreader.Model.getFootnoteModel(bookmark.ModelId);
//			if(model.getParagraph(paragraphIndex).getKind() == ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH){
//				paragraphIndex++;
//			}
			
			TOCTree treeToSelect = getTOCElement(paragraphIndex);
			String chapterText = treeToSelect.getText();
			if(TextUtils.isEmpty(chapterText)){
				chapterText = bookmark.getBookTitle();
			}
			holder.mChapterTitle.setText(chapterText);
			
//			int index = childAt(treeToSelect);
			
////			int bookId = (int) bookmark.getBookId();
//			com.sina.book.data.Book book = new com.sina.book.data.Book();
//			book.setBookId(fbreader.bookId);
//			List<Chapter> chapters = DBService.getAllChapter(book);
//			Chapter chapter = chapters.get(index);
//			String chapterText = chapter.getTitle();
//			holder.mChapterTitle.setText(chapterText);
			
//			holder.mChapterTitle.setText(bookmark.getBookTitle());
			String timeStr = mFormat.format(bookmark.getDate(DateType.Latest));
			if(timeStr.startsWith("0")){
				timeStr = timeStr.substring(1);
			}
			holder.mTime.setText(timeStr);
//			holder.mTime.setText(mFormat.format(bookmark
//					.getDate(DateType.Creation)));
			holder.mContent.setText(bookmark.getText());

			return convertView;
		}

		protected View createView() {
			Context context = getActivity();
			View itemView = LayoutInflater.from(context).inflate(
					R.layout.vw_bookmark_item, null);
			ViewHolder holder = new ViewHolder();
			holder.mChapterTitle = (TextView) itemView
					.findViewById(R.id.chapter_title);
			holder.mTime = (TextView) itemView.findViewById(R.id.time);
			holder.mContent = (TextView) itemView
					.findViewById(R.id.markcontent);
			itemView.setTag(holder);

			BookTagActivity activity = (BookTagActivity) getActivity();
			if(activity.mBook.isHtmlRead()){
				itemView.setBackgroundResource(R.drawable.mark_list_item_bg);
				int textColor = getResources().getColor(R.color.book_mark_chapter_color);
				holder.mChapterTitle.setTextColor(textColor);
				holder.mTime.setTextColor(textColor);
				holder.mContent.setTextColor(getResources().getColor(R.color.book_mark_font_color));
			}else{
				ReadStyleManager readStyleManager = ReadStyleManager
						.getInstance(context);
				
				if (ReadStyleManager.READ_MODE_NIGHT == readStyleManager
						.getReadMode()) {
					itemView.setBackgroundDrawable(context.getResources()
							.getDrawable(R.drawable.mark_list_item_bg_night));
				} else {
					itemView.setBackgroundDrawable(context.getResources()
							.getDrawable(R.drawable.mark_list_item_bg));
				}
				
				int textColor = readStyleManager.getColorFromIdentifier(context,
						R.color.book_mark_chapter_color);
				holder.mChapterTitle.setTextColor(textColor);
				holder.mTime.setTextColor(textColor);
				
				int textContentColor = readStyleManager.getColorFromIdentifier(
						context, R.color.book_mark_font_color);
				holder.mContent.setTextColor(textContentColor);
			}
			return itemView;
		}

		protected class ViewHolder {
			public TextView mChapterTitle;
			public TextView mTime;
			public TextView mContent;
		}

		@Override
		public final boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public final boolean isEnabled(int position) {
			return true;
		}

		@Override
		public final long getItemId(int position) {
			return position;
		}

		@Override
		public final Bookmark getItem(int position) {
			if (myShowAddBookmarkItem) {
				--position;
			}
			return (position >= 0) ? myBookmarks.get(position) : null;
		}

		@Override
		public final int getCount() {
			return myShowAddBookmarkItem ? myBookmarks.size() + 1 : myBookmarks
					.size();
		}

		public final void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			final Bookmark bookmark = getItem(position);
			if (bookmark != null) {
				gotoBookmark(bookmark);
			} else {
				addBookmark();
			}
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// 震动70毫秒
			Vibrator vib = (Vibrator) getActivity().getSystemService(
					Service.VIBRATOR_SERVICE);
			vib.vibrate(70);

			mCurMarkItem = (Bookmark) getItem(position);
			showClearCurrentMarkDialog();
			return true;
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
						public void onItemClick(DialogInterface dialog,
								int position) {
							switch (position) {
							case 0:
								myCollection.deleteBookmark(mCurMarkItem);
								if (myThisBookAdapter != null) {
									myThisBookAdapter.remove(mCurMarkItem);
								}
								if (myThisBookAdapter.getCount() == 0) {
									mHasNoMarkView.setVisibility(View.VISIBLE);
								}
								break;
							default:
								break;
							}
						}
					});
		}
	}

}
