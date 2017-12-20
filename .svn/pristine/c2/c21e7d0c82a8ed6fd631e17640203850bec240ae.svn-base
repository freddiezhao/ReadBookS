/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.fbreader;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.options.CancelMenuHelper;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.fbreader.fbreader.options.ImageOptions;
import org.geometerplus.fbreader.fbreader.options.MiscOptions;
import org.geometerplus.fbreader.fbreader.options.PageTurningOptions;
import org.geometerplus.fbreader.fbreader.options.ViewOptions;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;
import org.geometerplus.fbreader.network.sync.SyncData;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.drm.EncryptionMethod;
import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImageManager;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.db.DBService;
import com.sina.book.ui.view.BatteryView;

public final class FBReaderApp extends ZLApplication
{
	public interface ExternalFileOpener
	{
		public void openFile(ExternalFormatPlugin plugin, Book book,
				Bookmark bookmark);
	}

	private ExternalFileOpener	myExternalFileOpener;

	public void setExternalFileOpener(ExternalFileOpener o)
	{
		myExternalFileOpener = o;
	}

	public final MiscOptions		MiscOptions			= new MiscOptions();
	public final ImageOptions		ImageOptions		= new ImageOptions();
	public final ViewOptions		ViewOptions			= new ViewOptions();
	public final PageTurningOptions	PageTurningOptions	= new PageTurningOptions();

	private final ZLKeyBindings		myBindings			= new ZLKeyBindings();

	public final FBView				BookTextView;
	public final FBView				FootnoteView;
	private String					myFootnoteModelId;

	public volatile BookModel		Model;
	public volatile Book			ExternalBook;

	private ZLTextPosition			myJumpEndPosition;
	private Date					myJumpTimeStamp;

	public final IBookCollection	Collection;

	private SyncData				mySyncData			= new SyncData();

	public BatteryView				mBatteryView;

	public boolean					hasPosition			= true;
	public String					bookId;

	public void clear()
	{
		if (mBatteryView != null) {
			mBatteryView.clear();
		}
		getTextView().clear();
		run = false;
		clearInstance();
	}
	
	public FBReaderApp(IBookCollection collection)
	{
		Collection = collection;
		mBatteryView = new BatteryView(SinaBookApplication.gContext, true);

		collection.addListener(new IBookCollection.Listener()
		{
			public void onBookEvent(BookEvent event, Book book)
			{
				switch (event) {
				case BookmarkStyleChanged:
				case BookmarksUpdated:
					if (Model != null
							&& (book == null || book.equals(Model.Book))) {
						if (BookTextView.getModel() != null) {
							setBookmarkHighlightings(BookTextView, null);
						}
						if (FootnoteView.getModel() != null
								&& myFootnoteModelId != null) {
							setBookmarkHighlightings(FootnoteView,
									myFootnoteModelId);
						}
					}
					break;
				case Updated:
					onBookUpdated(book);
					break;
				}
			}

			public void onBuildEvent(IBookCollection.Status status)
			{
			}
		});

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		addAction(ActionCode.CLEAR_FIND_RESULTS, new ClearFindResultsAction(
				this));

		addAction(ActionCode.SELECTION_CLEAR, new SelectionClearAction(this));

		addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(this, true));
		addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(this, false));

		addAction(ActionCode.MOVE_CURSOR_UP, new MoveCursorAction(this,
				FBView.Direction.up));
		addAction(ActionCode.MOVE_CURSOR_DOWN, new MoveCursorAction(this,
				FBView.Direction.down));
		addAction(ActionCode.MOVE_CURSOR_LEFT, new MoveCursorAction(this,
				FBView.Direction.rightToLeft));
		addAction(ActionCode.MOVE_CURSOR_RIGHT, new MoveCursorAction(this,
				FBView.Direction.leftToRight));

		addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD,
				new VolumeKeyTurnPageAction(this, true));
		addAction(ActionCode.VOLUME_KEY_SCROLL_BACK,
				new VolumeKeyTurnPageAction(this, false));

		addAction(ActionCode.SWITCH_TO_DAY_PROFILE, new SwitchProfileAction(
				this, ColorProfile.DAY));
		addAction(ActionCode.SWITCH_TO_NIGHT_PROFILE, new SwitchProfileAction(
				this, ColorProfile.NIGHT));

		addAction(ActionCode.EXIT, new ExitAction(this));

		BookTextView = new FBView(this);
		FootnoteView = new FBView(this);

		setView(BookTextView);
	}

	public Book getCurrentBook()
	{
		final BookModel m = Model;
		return m != null ? m.Book : ExternalBook;
	}

	public void openHelpBook()
	{
		openBook(Collection.getBookByFile(BookUtil.getHelpFile()), null, null);
	}

	private void showBookNotFoundMessage()
	{
		if (mySyncData.getServerBookHashes().size() > 0) {
			showErrorMessage("bookIsMissing", mySyncData.getServerBookTitle());
		}
	}

	public Book getCurrentServerBook()
	{
		for (String hash : mySyncData.getServerBookHashes()) {
			final Book book = Collection.getBookByHash(hash);
			if (book != null) {
				return book;
			}
		}
		return null;
	}

	public void openBook(Book book, final Bookmark bookmark, Runnable postAction)
	{
		// if (Model != null) {
		// // 同一本书籍，又没有跳转目的(bookmark)时直接return
		// if (book == null || bookmark == null
		// && book.File.equals(Model.Book.File)) {
		// return;
		// }
		// }

		if (book == null) {
			// 服务器上的同步书籍
			// book = getCurrentServerBook();
			// 最近打开的书
			// if (book == null) {
			// showBookNotFoundMessage();
			// book = Collection.getRecentBook(0);
			// }
			// 帮助文档
			// if (book == null || !book.File.exists()) {
			// book = Collection.getBookByFile(BookUtil.getHelpFile());
			// }
			// if (book == null) {
			return;
			// }
		}

		// 标记书籍为已读
		ZLFile file = book.File;
		if (file.getPhysicalFile() != null) {
			file = file.getPhysicalFile();
		}
		final com.sina.book.data.Book sinaBook = DownBookManager.getInstance()
				.getBook(file.getPath());
		if (sinaBook != null) {
			// sinaBook.setTag(com.sina.book.data.Book.HAS_READ);
			// 更新最后阅读时间
			sinaBook.getReadInfo().setLastReadTime(new Date().getTime());
			// 数据库的放入线程中，防止阻塞生命周期
			new GenericTask()
			{
				@Override
				protected TaskResult doInBackground(TaskParams... params)
				{
					DBService.updateBook(sinaBook);
					return null;
				}
			}.execute();
		}

		final Book bookToOpen = book;
		bookToOpen.addLabel(Book.READ_LABEL);

		Collection.saveBook(bookToOpen);

		final SynchronousExecutor executor = createExecutor("loadingBook");
		executor.execute(new Runnable()
		{
			public void run()
			{
				openBookInternal(bookToOpen, bookmark, false);
			}
		}, postAction);
	}

	public void reloadBook()
	{
		final Book book = getCurrentBook();
		if (book != null) {
			final SynchronousExecutor executor = createExecutor("loadingBook");
			executor.execute(new Runnable()
			{
				public void run()
				{
					openBookInternal(book, null, true);
				}
			}, null);
		}
	}

	public ZLKeyBindings keyBindings()
	{
		return myBindings;
	}

	public FBView getTextView()
	{
		return (FBView) getCurrentView();
	}

	public void tryOpenFootnote(String id)
	{
		if (Model != null) {
			myJumpEndPosition = null;
			myJumpTimeStamp = null;
			BookModel.Label label = Model.getLabel(id);
			if (label != null) {
				if (label.ModelId == null) {
					if (getTextView() == BookTextView) {
						addInvisibleBookmark();
						myJumpEndPosition = new ZLTextFixedPosition(
								label.ParagraphIndex, 0, 0);
						myJumpTimeStamp = new Date();
					}
					BookTextView.gotoPosition(label.ParagraphIndex, 0, 0);
					setView(BookTextView);
				} else {
					setFootnoteModel(label.ModelId);
					setView(FootnoteView);
					FootnoteView.gotoPosition(label.ParagraphIndex, 0, 0);
				}
				getViewWidget().repaint();
				storePosition();
			}
		}
	}

	public void clearTextCaches()
	{
		BookTextView.clearCaches();
		FootnoteView.clearCaches();
	}

	public Bookmark addSelectionBookmark()
	{
		final FBView fbView = getTextView();
		final String text = fbView.getSelectedText();

		final Bookmark bookmark = new Bookmark(Model.Book, fbView.getModel()
				.getId(), fbView.getSelectionStartPosition(),
				fbView.getSelectionEndPosition(), text, true);
		Collection.saveBookmark(bookmark);
		fbView.clearSelection();

		return bookmark;
	}

	private void setBookmarkHighlightings(ZLTextView view, String modelId)
	{
		view.removeHighlightings(BookmarkHighlighting.class);
		// 每次加载20条数据，一直到全部加载完
		for (BookmarkQuery query = new BookmarkQuery(Model.Book, 20);; query = query
				.next()) {
			final List<Bookmark> bookmarks = Collection.bookmarks(query);
			if (bookmarks.isEmpty()) {
				break;
			}

			for (Bookmark b : bookmarks) {
				if (b.getEnd() == null) {
					b.findEnd(view);
				}
				if (MiscUtil.equals(modelId, b.ModelId)) {
					view.addHighlighting(new BookmarkHighlighting(view,
							Collection, b));
				}
			}
		}
	}

	private void setFootnoteModel(String modelId)
	{
		final ZLTextModel model = Model.getFootnoteModel(modelId);
		FootnoteView.setModel(model);
		if (model != null) {
			myFootnoteModelId = modelId;
			setBookmarkHighlightings(FootnoteView, modelId);
		}
	}

	// TODO:ouyang
	void openBookText(TOCTree tree, boolean notifyViewChanged)
	{
		final TOCTree.Reference reference = tree.getReference();
		if (reference != null) {
			final FBReaderApp fbreader = (FBReaderApp) ZLApplication
					.Instance();
			fbreader.addInvisibleBookmark();
			fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0,
					0);
			fbreader.showBookTextView(notifyViewChanged);
			fbreader.storePosition();
		}
	}

	private final int indexByPosition(int position, ZLTree<?> tree)
	{
		if (position == 0) {
			return 0;
		}
		--position;
		int index = 1;
		for (ZLTree<?> subtree : tree.subtrees()) {
			int count = getCount(subtree);
			if (count <= position) {
				position -= count;
				index += subtree.getSize();
			} else {
				return index + indexByPosition(position, subtree);
			}
		}
		throw new RuntimeException("That's impossible!!!");
	}

	private int getCount(ZLTree<?> tree)
	{
		int count = 1;
		// if (isOpen(tree)) {
		for (ZLTree<?> subtree : tree.subtrees()) {
			count += getCount(subtree);
		}
		// }
		return count;
	}

	ZLTree<?>	myTree;
	ZLTree<?>[]	myItems;

	public final ZLTree<?> getItem(int position)
	{
		final int index = indexByPosition(position + 1, myTree) - 1;
		ZLTree<?> item = myItems[index];
		if (item == null) {
			item = myTree.getTreeByParagraphNumber(index + 1);
			myItems[index] = item;
		}
		return item;
	}

	private synchronized void openBookInternal(Book book, Bookmark bookmark,
			boolean force)
	{
		// 跳转到同一本书的某个书签位置
		if (hasPosition) {
			if (!force && Model != null && book.equals(Model.Book)) {
				if (bookmark != null) {
					gotoBookmark(bookmark, false);
				}
				return;
			}
		}

		onViewChanged();

		// storePosition();
		BookTextView.setModel(null);
		FootnoteView.setModel(null);
		clearTextCaches();
		Model = null;
		ExternalBook = null;
		System.gc();
		System.gc();

		// 插件(如阅读PDF的插件的Open逻辑)
		final FormatPlugin plugin = book.getPluginOrNull();
		if (plugin instanceof ExternalFormatPlugin) {
			ExternalBook = book;
			final Bookmark bm;
			if (bookmark != null) {
				bm = bookmark;
			} else {
				ZLTextPosition pos = getStoredPosition(book);
				if (pos == null) {
					pos = new ZLTextFixedPosition(0, 0, 0);
				}
				bm = new Bookmark(book, "", pos, pos, "", false);
			}
			myExternalFileOpener.openFile((ExternalFormatPlugin) plugin, book,
					bm);
			return;
		}

		try {
			Model = BookModel.createModel(book);
			// Collection.saveBook(book);

			// TODO CJL 资源中没有中文
			ZLTextHyphenator.Instance().load("en");// 读取连字符模式数据
			// ZLTextHyphenator.Instance().load(book.getLanguage());// 读取连字符模式数据
			BookTextView.setModel(Model.getTextModel());// 设置BookTextView的数据源
			setBookmarkHighlightings(BookTextView, null);// 装载书签信息

			// TODO:ouyang
			// myTree = Model.TOCTree;
			// myItems = new ZLTree[myTree.getSize() - 1];
			// openBookText((TOCTree) getItem(20), false);

			gotoStoredPosition();// 从首字开始阅读或者跳转到上次阅读记录

			if (bookmark == null) {
				setView(BookTextView);// 绘制
			} else {
				gotoBookmark(bookmark, true);// 跳转到某书签
			}
			Collection.addBookToRecentList(book);// 添加到最近阅读记录
			final StringBuilder title = new StringBuilder(book.getTitle());// 书名+作者信息，设置到Activity标题处
			if (!book.authors().isEmpty()) {
				boolean first = true;
				for (Author a : book.authors()) {
					title.append(first ? " (" : ", ");
					title.append(a.DisplayName);
					first = false;
				}
				title.append(")");
			}
			setTitle(title.toString());
		} catch (BookReadingException e) {
			processException(e);
		}

		// 为毛又要重绘啊!?
		getViewWidget().reset();
		getViewWidget().repaint();

		try {
			for (FileEncryptionInfo info : book.getPlugin()
					.readEncryptionInfos(book)) {
				if (info != null && !EncryptionMethod.isSupported(info.Method)) {
					showErrorMessage("unsupportedEncryptionMethod",
							book.File.getPath());
					break;
				}
			}
		} catch (BookReadingException e) {
			// ignore
		}
	}

	private List<Bookmark> invisibleBookmarks()
	{
		final List<Bookmark> bookmarks = Collection
				.bookmarks(new BookmarkQuery(Model.Book, false, 10));
		Collections.sort(bookmarks, new Bookmark.ByTimeComparator());
		return bookmarks;
	}

	public boolean jumpBack()
	{
		try {
			if (getTextView() != BookTextView) {
				showBookTextView();
				return true;
			}

			if (myJumpEndPosition == null || myJumpTimeStamp == null) {
				return false;
			}
			// more than 2 minutes ago
			if (myJumpTimeStamp.getTime() + 2 * 60 * 1000 < new Date()
					.getTime()) {
				return false;
			}
			if (!myJumpEndPosition.equals(BookTextView.getStartCursor())) {
				return false;
			}

			final List<Bookmark> bookmarks = invisibleBookmarks();
			if (bookmarks.isEmpty()) {
				return false;
			}
			final Bookmark b = bookmarks.get(0);
			Collection.deleteBookmark(b);
			gotoBookmark(b, true);
			return true;
		} finally {
			myJumpEndPosition = null;
			myJumpTimeStamp = null;
		}
	}

	private void gotoBookmark(Bookmark bookmark, boolean exactly)
	{
		final String modelId = bookmark.ModelId;
		if (modelId == null) {
			addInvisibleBookmark();
			if (exactly) {
				BookTextView.gotoPosition(bookmark);
			} else {
				BookTextView.gotoHighlighting(new BookmarkHighlighting(
						BookTextView, Collection, bookmark));
			}
			setView(BookTextView);
		} else {
			setFootnoteModel(modelId);
			if (exactly) {
				FootnoteView.gotoPosition(bookmark);
			} else {
				FootnoteView.gotoHighlighting(new BookmarkHighlighting(
						FootnoteView, Collection, bookmark));
			}
			setView(FootnoteView);
		}
		getViewWidget().repaint();
		storePosition();
	}

	public void showBookTextView()
	{
		setView(BookTextView);
	}

	public void showBookTextView(boolean notifyViewChanged)
	{
		setView(BookTextView, notifyViewChanged);
	}

	public void onWindowClosing()
	{
		storePosition();
	}

	private class PositionSaver implements Runnable
	{
		private final Book				myBook;
		private final ZLTextPosition	myPosition;
		private final RationalNumber	myProgress;

		PositionSaver(Book book, ZLTextPosition position,
				RationalNumber progress)
		{
			myBook = book;
			myPosition = position;
			myProgress = progress;
		}

		public void run()
		{
			Collection.storePosition(myBook.getId(), myPosition);
			myBook.setProgress(myProgress);
			Collection.saveBook(myBook);
		}
	}
	
	private boolean run = true;

	private class SaverThread extends Thread
	{
		private final List<Runnable>	myTasks	= Collections
														.synchronizedList(new LinkedList<Runnable>());

		SaverThread()
		{
			setPriority(MIN_PRIORITY);
		}

		void add(Runnable task)
		{
			myTasks.add(task);
		}

		public void run()
		{
			while (run) {
				synchronized (myTasks) {
					while (!myTasks.isEmpty()) {
						myTasks.remove(0).run();
					}
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void useSyncInfo(boolean openOtherBook)
	{
		if (openOtherBook) {
			final Book fromServer = getCurrentServerBook();
			if (fromServer == null) {
				showBookNotFoundMessage();
			}
			if (fromServer != null
					&& !fromServer.equals(Collection.getRecentBook(0))) {
				openBook(fromServer, null, null);
				return;
			}
		}

		if (myStoredPositionBook != null
				&& mySyncData.hasPosition(Collection.getHash(
						myStoredPositionBook, true))) {
			gotoStoredPosition();
			storePosition();
		}
	}

	private volatile SaverThread	mySaverThread;
	private volatile ZLTextPosition	myStoredPosition;
	private volatile Book			myStoredPositionBook;

	private ZLTextFixedPosition getStoredPosition(Book book)
	{
		// 服务器存储的位置信息
		final ZLTextFixedPosition.WithTimestamp fromServer = mySyncData
				.getAndCleanPosition(Collection.getHash(book, true));
		// 本地数据库存储的位置信息
		final ZLTextFixedPosition.WithTimestamp local = Collection
				.getStoredPosition(book.getId());
		// 1.如果本地为空，则使用服务器端
		// 如果服务器端也为空，则位置指向书籍开头处
		// 2.如果本地不为空，判断服务器端是否为空，为空则返回本地
		// 3.本地和服务器都不为空，则判断时间戳，哪个时间戳大使用哪个
		if (local == null) {
			return fromServer != null ? fromServer : new ZLTextFixedPosition(0,
					0, 0);
		} else if (fromServer == null) {
			return local;
		} else {
			return fromServer.Timestamp >= local.Timestamp ? fromServer : local;
		}
	}

	private void gotoStoredPosition()
	{
		// if(!hasPosition){
		// return;
		// }

		myStoredPositionBook = Model != null ? Model.Book : null;
		if (myStoredPositionBook == null) {
			return;
		}

		if (!hasPosition) {
			myStoredPosition = new ZLTextFixedPosition(0, 0, 0);
		} else {
			myStoredPosition = getStoredPosition(myStoredPositionBook);
		}

		BookTextView.gotoPosition(myStoredPosition);
		savePosition();
	}

	public void storePosition()
	{
		final Book bk = Model != null ? Model.Book : null;
		if (bk != null && bk == myStoredPositionBook
				&& myStoredPosition != null && BookTextView != null) {
			final ZLTextPosition position = new ZLTextFixedPosition(
					BookTextView.getStartCursor());
			if (!myStoredPosition.equals(position)) {
				myStoredPosition = position;
				savePosition();
			}
		}
	}

	private void savePosition()
	{
		final RationalNumber progress = BookTextView.getProgress();
		if (mySaverThread == null) {
			mySaverThread = new SaverThread();
			mySaverThread.start();
		}
		mySaverThread.add(new PositionSaver(myStoredPositionBook,
				myStoredPosition, progress));
	}

	public boolean hasCancelActions()
	{
		return new CancelMenuHelper().getActionsList(Collection).size() > 1;
	}

	public void runCancelAction(CancelMenuHelper.ActionType type,
			Bookmark bookmark)
	{
		switch (type) {
		case library:
			runAction(ActionCode.SHOW_LIBRARY);
			break;
		case networkLibrary:
			runAction(ActionCode.SHOW_NETWORK_LIBRARY);
			break;
		case previousBook:
			openBook(Collection.getRecentBook(1), null, null);
			break;
		case returnTo:
			Collection.deleteBookmark(bookmark);
			gotoBookmark(bookmark, true);
			break;
		case close:
			closeWindow();
			break;
		}
	}

	private synchronized void updateInvisibleBookmarksList(Bookmark b)
	{
		if (Model != null && Model.Book != null && b != null) {
			for (Bookmark bm : invisibleBookmarks()) {
				if (b.equals(bm)) {
					Collection.deleteBookmark(bm);
				}
			}
			Collection.saveBookmark(b);
			final List<Bookmark> bookmarks = invisibleBookmarks();
			for (int i = 3; i < bookmarks.size(); ++i) {
				Collection.deleteBookmark(bookmarks.get(i));
			}
		}
	}

	public void addInvisibleBookmark(ZLTextWordCursor cursor)
	{
		if (cursor != null && Model != null && Model.Book != null
				&& getTextView() == BookTextView) {
			cursor = new ZLTextWordCursor(cursor);
			if (cursor.isNull()) {
				return;
			}

			updateInvisibleBookmarksList(Bookmark.createBookmark(Model.Book,
					getTextView().getModel().getId(), cursor, 6, false));
		}
	}

	public void addInvisibleBookmark()
	{
		if (Model.Book != null && getTextView() == BookTextView) {
			updateInvisibleBookmarksList(createBookmark(6, false));
		}
	}

	public Bookmark createBookmark(int maxLength, boolean visible)
	{
		final FBView view = getTextView();
		final ZLTextWordCursor cursor = view.getStartCursor();

		if (cursor.isNull()) {
			return null;
		}

		return Bookmark.createBookmark(Model.Book, view.getModel().getId(),
				cursor, maxLength, visible);
	}

	public TOCTree getCurrentTOCElement()
	{
		final ZLTextWordCursor cursor = BookTextView.getStartCursor();
		if (Model == null || cursor == null) {
			return null;
		}

		int index = cursor.getParagraphIndex();
		if (cursor.isEndOfParagraph()) {
			++index;
		}
		TOCTree treeToSelect = null;
		for (TOCTree tree : Model.TOCTree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference == null) {
				continue;
			}
			if (reference.ParagraphIndex > index) {
				break;
			}
			treeToSelect = tree;
		}
		return treeToSelect;
	}

	public void onBookUpdated(Book book)
	{
		if (Model == null || Model.Book == null || !Model.Book.equals(book)) {
			return;
		}

		final String newEncoding = book.getEncodingNoDetection();
		final String oldEncoding = Model.Book.getEncodingNoDetection();

		Model.Book.updateFrom(book);

		if (newEncoding != null && !newEncoding.equals(oldEncoding)) {
			reloadBook();
		} else {
			ZLTextHyphenator.Instance().load(Model.Book.getLanguage());
			clearTextCaches();
			getViewWidget().repaint();
		}
	}
}
