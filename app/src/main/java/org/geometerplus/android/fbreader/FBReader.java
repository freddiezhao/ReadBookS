/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.GenericTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.db.DBService;
import com.sina.book.exception.UnparseEpubFileException;
import com.sina.book.reader.EpubFileHandler;
import com.sina.book.reader.PageWidgetListener;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.reader.model.BookSummaryPostion;
import com.sina.book.ui.BaseActivity;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.util.BrightnessUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

import org.geometerplus.android.fbreader.api.ApiListener;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.fbreader.httpd.DataService;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.sync.SyncOperations;
import org.geometerplus.android.util.DeviceType;
import org.geometerplus.android.util.SearchDialogUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.ZLog;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.CancelMenuHelper;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.view.ZLView.PageIndex;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.view.ZLTextPage;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextView.PagePosition;
import org.geometerplus.zlibrary.ui.android.error.ErrorKeys;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class FBReader extends BaseActivity implements ZLApplicationWindow {
	static final int ACTION_BAR_COLOR = Color.DKGRAY;

	public static final int REQUEST_PREFERENCES = 1;
	public static final int REQUEST_CANCEL_MENU = 2;

	public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
	public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;

	public static void openBookActivity(Context context, Book book, Bookmark bookmark) {
		openBookActivity(context, book, bookmark, null);
	}

	public static void openBookActivity(Context context, Book book, Bookmark bookmark, String bookId) {
		final Intent intent = new Intent(context, FBReader.class).setAction(FBReaderIntents.Action.VIEW).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		FBReaderIntents.putBookExtra(intent, book);
		FBReaderIntents.putBookmarkExtra(intent, bookmark);
		if (!TextUtils.isEmpty(bookId)) {
			intent.putExtra("bookId", bookId);
		}
		context.startActivity(intent);
	}

	public static void openBookActivity(Context context, Book book, com.sina.book.data.Book sinaBook, Bookmark bookmark) {
		final Intent intent = new Intent(context, FBReader.class).setAction(FBReaderIntents.Action.VIEW).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		FBReaderIntents.putBookExtra(intent, book);
		FBReaderIntents.putBookmarkExtra(intent, bookmark);
		if (sinaBook != null) {
			intent.putExtra("book", sinaBook);
		}
		context.startActivity(intent);
	}

	public static void openBookActivity(Context context, String url, boolean hasPosition) {
		openBookActivity(context, url, hasPosition, null);
	}

	public static void openBookActivity(Context context, String url, com.sina.book.data.Book book, boolean hasPosition) {
		final Intent intent = new Intent(context, FBReader.class).setAction(FBReaderIntents.Action.VIEW).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setData(Uri.parse(url));
		intent.putExtra("hasPosition", hasPosition);
		if (book != null) {
			intent.putExtra("book", book);
		}
		context.startActivity(intent);
	}

	public static void openBookActivity(Context context, String url, boolean hasPosition, String bookId) {
		final Intent intent = new Intent(context, FBReader.class).setAction(FBReaderIntents.Action.VIEW).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setData(Uri.parse(url));
		intent.putExtra("hasPosition", hasPosition);
		if (!TextUtils.isEmpty(bookId)) {
			intent.putExtra("bookId", bookId);
		}
		context.startActivity(intent);
	}

	private static ZLAndroidLibrary getZLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}

	public FBReaderApp myFBReaderApp;
	public volatile Book myBook;
	public com.sina.book.data.Book sinaBook;

	// 根root
	private RelativeLayout myRootView;
	// 阅读view
	public ZLAndroidWidget myMainView;
	// 翻页监听器
	private PageListener mPageWidgetListener;

	// 下拉添加书签相关View
	private View mPullDownLayout;
	private TextView mPullDownText;
	private ImageView mPullDownImage;
	private ImageView mBookmarkFlag;

	private volatile boolean myShowStatusBarFlag;
	// private String myMenuLanguage;

	final DataService.Connection DataConnection = new DataService.Connection();

	volatile boolean IsPaused = false;
	private volatile long myResumeTimestamp;
	volatile Runnable OnResumeAction = null;

	private Intent myCancelIntent = null;
	private Intent myOpenBookIntent = null;

	// public ReadStyleManager mReadStyleManager;

	// 是否有进度
	private boolean hasPosition = true;
	// 书籍BOOKID
	private String bookId;

	private static final String PLUGIN_ACTION_PREFIX = "___";
	private final List<PluginApi.ActionInfo> myPluginActions = new LinkedList<PluginApi.ActionInfo>();

	private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).<PluginApi.ActionInfo> getParcelableArrayList(PluginApi.PluginInfo.KEY);
			if (actions != null) {
				synchronized (myPluginActions) {
					int index = 0;
					while (index < myPluginActions.size()) {
						myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
					}
					myPluginActions.addAll(actions);
					index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						myFBReaderApp.addAction(PLUGIN_ACTION_PREFIX + index++, new RunPluginAction(FBReader.this, myFBReaderApp, info.getId()));
					}
				}
			}
		}
	};

	private synchronized void openBook(Intent intent, final Runnable action, boolean force) {
		if (!force && myBook != null) {
			return;
		}
		myBook = FBReaderIntents.getBookExtra(intent);
		final Bookmark bookmark = FBReaderIntents.getBookmarkExtra(intent);
		if (myBook == null) {
			// 从文件路径加载资源
			final Uri data = intent.getData();

			// String dir =
			// Environment.getExternalStorageDirectory().toString();
			// String path = dir + "/书籍/swft/OEBPS/"+chapterIndex +".xhtml";
			//
			if (data != null) {
				// myBook = createBookForFile(ZLFile.createFileByPath(path));
				myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
			}
		}

		if (myBook != null) {
			ZLFile file = myBook.File;
			if (!file.exists()) {
				if (file.getPhysicalFile() != null) {
					file = file.getPhysicalFile();
				}

				String text = "文件不存在:  " + file.getPath();
				Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
				// UIUtil.showErrorMessage(this, "fileNotFound",
				// file.getPath());
				myBook = null;
				myMainView.setTouchEnable(false);
			} else {
				Config.Instance().runOnConnect(new Runnable() {
					public void run() {
						myFBReaderApp.openBook(myBook, bookmark, action);
						AndroidFontUtil.clearFontCache();
					}
				});
			}
		} else {
			Toast.makeText(this, R.string.bookdetail_failed_text, Toast.LENGTH_SHORT).show();
			myMainView.setTouchEnable(false);
		}

		if (sinaBook == null) {
			Config.Instance().runOnConnect(new Runnable() {
				public void run() {
					if (myBook != null) {
						final String epubPath = myBook.File.getPath();
						sinaBook = createBookFromFile(new File(epubPath));
						sinaBook.setTitle(myBook.getTitle());
						sinaBook.setBookId(bookId);
						boolean hasBook = DownBookManager.getInstance().hasBook(sinaBook);
						if (!hasBook) {
							new Thread() {
								public void run() {
									try {
										EpubFileHandler.convertEpub2Dat(sinaBook);
									} catch (UnparseEpubFileException e) {
										e.printStackTrace();
									}
								};
							}.start();
						}
					}
					AndroidFontUtil.clearFontCache();
				}
			});
		}
	}

	public Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = myFBReaderApp.Collection.getBookByFile(file);
		if (book != null) {
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = myFBReaderApp.Collection.getBookByFile(child);
				if (book != null) {
					return book;
				}
			}
		}
		return null;
	}

	private Runnable getPostponedInitAction() {
		return new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						// new TipRunner().start();
						DictionaryUtil.init(FBReader.this, null);
						final Intent intent = getIntent();
						if (intent != null && FBReaderIntents.Action.PLUGIN.equals(intent.getAction())) {
							new RunPluginAction(FBReader.this, myFBReaderApp, intent.getData()).run();
						}
					}
				});
			}
		};
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// TODO CJL 暂时注释掉
		// Thread.setDefaultUncaughtExceptionHandler(new
		// UncaughtExceptionHandler(this));

		bindService(new Intent(this, DataService.class), DataConnection, DataService.BIND_AUTO_CREATE);

		final Config config = Config.Instance();
		config.runOnConnect(new Runnable() {
			public void run() {
				config.requestAllValuesForGroup("Options");
				config.requestAllValuesForGroup("Style");
				config.requestAllValuesForGroup("LookNFeel");
				config.requestAllValuesForGroup("Fonts");
				config.requestAllValuesForGroup("Colors");
				config.requestAllValuesForGroup("Files");
			}
		});

		final ZLAndroidLibrary zlibrary = getZLibrary();
		myShowStatusBarFlag = zlibrary.ShowStatusBarOption.getValue();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// View decorView = getWindow().getDecorView();
		// int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		// | View.SYSTEM_UI_FLAG_FULLSCREEN;
		// decorView.setSystemUiVisibility(uiOptions);

		// Window window = getWindow();
		// WindowManager.LayoutParams params = window.getAttributes();
		// params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
		// window.setAttributes(params);

		// getWindow().setFlags(
		// WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// myShowStatusBarFlag ? 0
		// : WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// DisplayMetrics metrics = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// int mDisplayWidth = metrics.widthPixels;
		// int mDisplayHeight = metrics.heightPixels;
		// mReadStyleManager = ReadStyleManager.getInstance(mDisplayWidth,
		// mDisplayHeight);

		// View _myRootView = View.inflate(this, R.layout.main, null);
		// setContentView(_myRootView);
		// ViewUtil.i.checkHardwareAcceleratedRecursive(myRootView);
		setContentView(R.layout.main);

		if (mPageWidgetListener == null) {
			mPageWidgetListener = new PageListener();
		}
		myRootView = (RelativeLayout) findViewById(R.id.root_view);
		myMainView = (ZLAndroidWidget) findViewById(R.id.reading_pagewidget);
		myMainView.setPageWidgetListener(mPageWidgetListener);
		myMainView.setTouchEnable(true);

		// 下拉增加书签View
		mPullDownLayout = findViewById(R.id.reading_add_mark_view);
		mBookmarkFlag = (ImageView) findViewById(R.id.reading_bookmark_flag);
		mPullDownText = (TextView) mPullDownLayout.findViewById(R.id.pulldown_text);
		mPullDownImage = (ImageView) mPullDownLayout.findViewById(R.id.pulldown_image);
		mBookmarkFlag = (ImageView) findViewById(R.id.reading_bookmark_flag);

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		zlibrary.setActivity(this);

		sinaBook = (com.sina.book.data.Book) getIntent().getSerializableExtra("book");
		if (sinaBook == null) {
			bookId = getIntent().getStringExtra("bookId");
		} else {
			bookId = sinaBook.getBookId();
		}

		hasPosition = getIntent().getBooleanExtra("hasPosition", true);

		initFBReadApp();

		// // 这里每次进入阅读页都重新构建FBReadApp，防止第二次阅读不刷新的问题。
		// // myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
		// // if (myFBReaderApp == null) {
		// myFBReaderApp = new FBReaderApp(new BookCollectionShadow());
		// // }
		// // FIXME: 微博读书数据库是否存在阅读记录
		// myFBReaderApp.hasPosition = hasPosition;
		// myFBReaderApp.bookId = bookId;

		getCollection().bindToService(this, null);
		myBook = null;

		// myFBReaderApp.setActivity(this);
		// myFBReaderApp.setWindow(this);
		// myFBReaderApp.initWindow();
		//
		// myFBReaderApp.setExternalFileOpener(new ExternalFileOpener(this));

		if (mNavigationPopupClickListener == null) {
			mNavigationPopupClickListener = new NavigationPopupClickListener();
		}

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, myShowStatusBarFlag ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// // 初始化字体，和微博读书保持一致
		// float size = StorageUtil.getFloat(StorageUtil.KEY_FONT_SIZE,
		// ReadStyleManager.DEF_FONT_SIZE_SP);
		// final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions
		// .getTextStyleCollection().getBaseStyle().FontSizeOption;
		// option.setValue((int) size);

		// TODO: 初始化文字颜色

		// if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
		// new TextSearchPopup(myFBReaderApp);
		// }
		// if (myFBReaderApp.getPopupById(NavigationPopup.ID) == null) {
		// new NavigationPopup(this,myFBReaderApp);
		// }
		// if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
		// new SelectionPopup(myFBReaderApp);
		// }

		// myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS,
		// new ShowBookmarksAction(this, myFBReaderApp));
		//
		// myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION,
		// new ShowNavigationAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this,
		// myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(
		// this, myFBReaderApp));
		//
		// myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL,
		// new SelectionShowPanelAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL,
		// new SelectionHidePanelAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD,
		// new SelectionCopyAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.SELECTION_SHARE,
		// new SelectionShareAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE,
		// new SelectionTranslateAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK,
		// new ProcessHyperlinkAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.OPEN_VIDEO, new OpenVideoAction(
		// this, myFBReaderApp));
		//
		// myFBReaderApp.addAction(ActionCode.SHOW_CANCEL_MENU,
		// new ShowCancelMenuAction(this, myFBReaderApp));
		//
		// myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM,
		// new SetScreenOrientationAction(this, myFBReaderApp,
		// ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		// myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR,
		// new SetScreenOrientationAction(this, myFBReaderApp,
		// ZLibrary.SCREEN_ORIENTATION_SENSOR));
		// myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT,
		// new SetScreenOrientationAction(this, myFBReaderApp,
		// ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		// myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE,
		// new SetScreenOrientationAction(this, myFBReaderApp,
		// ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		// if (ZLibrary.Instance().supportsAllOrientations()) {
		// myFBReaderApp.addAction(
		// ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT,
		// new SetScreenOrientationAction(this, myFBReaderApp,
		// ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
		// myFBReaderApp.addAction(
		// ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
		// new SetScreenOrientationAction(this, myFBReaderApp,
		// ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		// }
		// myFBReaderApp.addAction(ActionCode.OPEN_WEB_HELP,
		// new OpenWebHelpAction(this, myFBReaderApp));
		// myFBReaderApp.addAction(ActionCode.INSTALL_PLUGINS,
		// new InstallPluginsAction(this, myFBReaderApp));

		final Intent intent = getIntent();
		final String action = intent.getAction();

		myOpenBookIntent = intent;
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
			if (FBReaderIntents.Action.CLOSE.equals(action)) {
				myCancelIntent = intent;
				myOpenBookIntent = null;
			} else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(action)) {
				myFBReaderApp.ExternalBook = null;
				myOpenBookIntent = null;
				getCollection().bindToService(this, new Runnable() {
					public void run() {
						myFBReaderApp.openBook(null, null, null);
					}
				});
			}
		}

		mBookmarkFlag.setOnClickListener(mNavigationPopupClickListener);
	}

	private void initFBReadApp() {
		// 这里每次进入阅读页都重新构建FBReadApp，防止第二次阅读不刷新的问题。
		// myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
		// if (myFBReaderApp == null) {
		myFBReaderApp = new FBReaderApp(new BookCollectionShadow());
		// }
		// FIXME: 微博读书数据库是否存在阅读记录
		myFBReaderApp.hasPosition = hasPosition;
		myFBReaderApp.bookId = bookId;

		myFBReaderApp.setActivity(this);
		myFBReaderApp.setWindow(this);
		myFBReaderApp.initWindow();

		myFBReaderApp.setExternalFileOpener(new ExternalFileOpener(this));

		// 初始化字体，和微博读书保持一致
		float size = StorageUtil.getFloat(StorageUtil.KEY_FONT_SIZE, ReadStyleManager.DEF_FONT_SIZE_SP);
		final ZLIntegerRangeOption option = myFBReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
		option.setValue((int) size);

		if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(this, myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(myFBReaderApp);
		}

		myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.OPEN_VIDEO, new OpenVideoAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SENSOR));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}
		myFBReaderApp.addAction(ActionCode.OPEN_WEB_HELP, new OpenWebHelpAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.INSTALL_PLUGINS, new InstallPluginsAction(this, myFBReaderApp));

	}

	public NavigationPopupClickListener mNavigationPopupClickListener;

	public OnClickListener getNavigationPopupClickListener() {
		return mNavigationPopupClickListener;
	}

	public final class NavigationPopupClickListener implements OnClickListener {

		// private FBReader activity;

		public NavigationPopupClickListener() {
			// this.activity = activity;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.reading_toolbar_bookmark_btn:// 书签
				addOrDeleteMark();
				break;
			case R.id.reading_bookmark_flag:// 书签标志
				if (hasBookmark()) {
					deleteBookmark();
					hideBookmarkFlag();
					shortToast(R.string.delete_mark_succ);
				}
				break;
			}
		}

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		setStatusBarVisibility(true);
		// final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		// if (popup != null && popup.getId() == NavigationPopup.ID) {
		// myFBReaderApp.hideActivePopup();
		// }else{
		// navigate();
		// }
		// setupMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		setStatusBarVisibility(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		setStatusBarVisibility(false);
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		final String action = intent.getAction();
		final Uri data = intent.getData();

		hasPosition = intent.getBooleanExtra("hasPosition", true);
		sinaBook = (com.sina.book.data.Book) intent.getSerializableExtra("book");
		if (sinaBook == null) {
			bookId = intent.getStringExtra("bookId");
		} else {
			bookId = sinaBook.getBookId();
		}

		initFBReadApp();
		// // 这里每次进入阅读页都重新构建FBReadApp，防止第二次阅读不刷新的问题。
		// myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
		// if (myFBReaderApp == null) {
		// myFBReaderApp = new FBReaderApp(new BookCollectionShadow());
		// }
		// myFBReaderApp.hasPosition = hasPosition;
		// myFBReaderApp.bookId = bookId;

		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(action) && data != null && "fbreader-action".equals(data.getScheme())) {
			myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
		} else if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
			myOpenBookIntent = intent;
			if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
				final ExternalFormatPlugin plugin = (ExternalFormatPlugin) myFBReaderApp.ExternalBook.getPluginOrNull();
				try {
					startActivity(PluginUtil.createIntent(plugin, PluginUtil.ACTION_KILL));
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else if (FBReaderIntents.Action.PLUGIN.equals(action)) {
			new RunPluginAction(this, myFBReaderApp, data).run();
		} else if (Intent.ACTION_SEARCH.equals(action)) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final TextSearchPopup popup = (TextSearchPopup) myFBReaderApp.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					myFBReaderApp.MiscOptions.TextSearchPattern.setValue(pattern);
					if (myFBReaderApp.getTextView().search(pattern, true, false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								myFBReaderApp.showPopup(popup.getId());
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIUtil.showErrorMessage(FBReader.this, "textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else if (FBReaderIntents.Action.CLOSE.equals(intent.getAction())) {
			myCancelIntent = intent;
			myOpenBookIntent = null;
		} else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(intent.getAction())) {
			final Book book = FBReaderIntents.getBookExtra(intent);
			myFBReaderApp.ExternalBook = null;
			myOpenBookIntent = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					Book b = myFBReaderApp.Collection.getRecentBook(0);
					if (b.equals(book)) {
						b = myFBReaderApp.Collection.getRecentBook(1);
					}
					myFBReaderApp.openBook(b, null, null);
				}
			});
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		getCollection().bindToService(this, new Runnable() {
			public void run() {
				new Thread() {
					public void run() {
						getPostponedInitAction().run();
					}
				}.start();

				myFBReaderApp.getViewWidget().repaint();
			}
		});

		initPluginActions();

		final ZLAndroidLibrary zlibrary = getZLibrary();

		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				final boolean showStatusBar = zlibrary.ShowStatusBarOption.getValue();
				if (showStatusBar != myShowStatusBarFlag) {
					close();
					startActivity(new Intent(FBReader.this, FBReader.class));
				}
				zlibrary.ShowStatusBarOption.saveSpecialValue();
				myFBReaderApp.ViewOptions.ColorProfileName.saveSpecialValue();
				// 屏幕方向设定暂时关闭
				// SetScreenOrientationAction.setOrientation(FBReader.this,
				// zlibrary.getOrientationOption().getValue());
			}
		});

		((PopupPanel) myFBReaderApp.getPopupById(TextSearchPopup.ID)).setPanelInfo(this, myRootView);
		((PopupPanel) myFBReaderApp.getPopupById(NavigationPopup.ID)).setPanelInfo(this, myRootView);
		((PopupPanel) myFBReaderApp.getPopupById(SelectionPopup.ID)).setPanelInfo(this, myRootView);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		switchWakeLock(hasFocus && getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < myFBReaderApp.getBatteryLevel());
	}

	private void initPluginActions() {
		synchronized (myPluginActions) {
			int index = 0;
			while (index < myPluginActions.size()) {
				myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
			}
			myPluginActions.clear();
		}

		sendOrderedBroadcast(new Intent(PluginApi.ACTION_REGISTER), null, myPluginInfoReceiver, null, RESULT_OK, null, null);
	}

	// private class TipRunner extends Thread {
	// TipRunner() {
	// setPriority(MIN_PRIORITY);
	// }
	//
	// public void run() {
	// final TipsManager manager = TipsManager.Instance();
	// switch (manager.requiredAction()) {
	// case Initialize:
	// startActivity(new Intent(TipsActivity.INITIALIZE_ACTION, null,
	// FBReader.this, TipsActivity.class));
	// break;
	// case Show:
	// startActivity(new Intent(TipsActivity.SHOW_TIP_ACTION, null,
	// FBReader.this, TipsActivity.class));
	// break;
	// case Download:
	// manager.startDownloading();
	// break;
	// case None:
	// break;
	// }
	// }
	// }

	@Override
	protected void onResume() {
		super.onResume();

		SyncOperations.enableSync(this, true);

		myStartTimer = true;
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				if (StorageUtil.getBoolean(StorageUtil.KEY_AUTO_BRIGHTNESS, true)) {
					BrightnessUtil.setCurrentScreenDefault(FBReader.this);
				} else {
					int brightnessLevel = StorageUtil.getBrightness();
					BrightnessUtil.setCurrentScreenBrightness(FBReader.this, brightnessLevel);
				}

				// final int brightnessLevel =
				// getZLibrary().ScreenBrightnessLevelOption
				// .getValue();
				// if (brightnessLevel != 0) {
				// setScreenBrightness(brightnessLevel);
				// } else {
				// setScreenBrightnessAuto();
				// }
				// if (getZLibrary().DisableButtonLightsOption.getValue()) {
				// setButtonLight(false);
				// }

				getCollection().bindToService(FBReader.this, new Runnable() {
					public void run() {
						final BookModel model = myFBReaderApp.Model;
						if (model == null || model.Book == null) {
							return;
						}
						onPreferencesUpdate(myFBReaderApp.Collection.getBookById(model.Book.getId()));
					}
				});
			}
		});

		registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		IsPaused = false;
		myResumeTimestamp = System.currentTimeMillis();
		if (OnResumeAction != null) {
			final Runnable action = OnResumeAction;
			OnResumeAction = null;
			action.run();
		}

		registerReceiver(mySyncUpdateReceiver, new IntentFilter(SyncOperations.UPDATED));
		// 屏幕方向设定暂时关闭
		// SetScreenOrientationAction.setOrientation(this, ZLibrary.Instance()
		// .getOrientationOption().getValue());
		if (myCancelIntent != null) {
			final Intent intent = myCancelIntent;
			myCancelIntent = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					runCancelAction(intent);
				}
			});
			return;
		} else if (myOpenBookIntent != null) {
			final Intent intent = myOpenBookIntent;
			myOpenBookIntent = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					openBook(intent, null, true);
				}
			});
		} else if (myFBReaderApp.getCurrentServerBook() != null) {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.useSyncInfo(true);
				}
			});
		} else if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.openBook(myFBReaderApp.ExternalBook, null, null);
				}
			});
		} else {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.useSyncInfo(true);
				}
			});
		}

		PopupPanel.restoreVisibilities(myFBReaderApp);
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
	}

	@Override
	protected void onPause() {
		SyncOperations.quickSync(this);

		IsPaused = true;
		try {
			unregisterReceiver(mySyncUpdateReceiver);
		} catch (IllegalArgumentException e) {
		}
		try {
			unregisterReceiver(myBatteryInfoReceiver);
		} catch (IllegalArgumentException e) {
			// do nothing, this exception means myBatteryInfoReceiver was not
			// registered
		}
		myFBReaderApp.stopTimer();
		if (getZLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(true);
		}
		myFBReaderApp.onWindowClosing();

		// 关闭活动的PopupPanel
		myFBReaderApp.hideActivePopup();

		// 保持阅读记录
		saveReadPercent();
		super.onPause();
	}

	private void saveReadPercent() {
		if (isFinished) {
			return;
		}
		if (myBook != null) {
			final PagePosition pagePosition = myFBReaderApp.getTextView().pagePosition();
			float progress = (pagePosition.Current * 1.0f) / pagePosition.Total;
			progress *= 100;
			progress = Util.formatFloat(progress, 2);

			ZLFile file = myBook.File;
			if (file.getPhysicalFile() != null) {
				file = file.getPhysicalFile();
			}

			// 保存阅读记录到微博读书
			final com.sina.book.data.Book sinaBook = DownBookManager.getInstance().getBook(file.getPath());
			if (sinaBook != null) {
				sinaBook.getReadInfo().setLastReadPercent(progress);
				// 将书籍信息进行保存，同步到各处
				DownBookManager.getInstance().updateBook(sinaBook);
				StorageUtil.saveString(StorageUtil.KEY_READ_BOOK, sinaBook.getTitle());
				// 数据库的放入线程中，防止阻塞生命周期
				new GenericTask() {
					@Override
					protected TaskResult doInBackground(TaskParams... params) {
						DBService.updateBook(sinaBook);
						return null;
					}
				}.execute();
			}
		}
	}

	@Override
	protected void onStop() {
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
		PopupPanel.removeAllWindows(myFBReaderApp, this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		getCollection().unbind();
		unbindService(DataConnection);
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		myFBReaderApp.onWindowClosing();
		super.onLowMemory();
	}

	@Override
	public boolean onSearchRequested() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		myFBReaderApp.hideActivePopup();
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			final SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
			manager.setOnCancelListener(new SearchManager.OnCancelListener() {
				public void onCancel() {
					if (popup != null) {
						myFBReaderApp.showPopup(popup.getId());
					}
					manager.setOnCancelListener(null);
				}
			});
			startSearch(myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), true, null, false);
		} else {
			SearchDialogUtil.showDialog(this, FBReader.class, myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface di) {
					if (popup != null) {
						myFBReaderApp.showPopup(popup.getId());
					}
				}
			});
		}
		return true;
	}

	public void showSelectionPanel() {
		final ZLTextView view = myFBReaderApp.getTextView();
		((SelectionPopup) myFBReaderApp.getPopupById(SelectionPopup.ID)).move(view.getSelectionStartY(), view.getSelectionEndY());
		myFBReaderApp.showPopup(SelectionPopup.ID);
	}

	public void hideSelectionPanel() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		if (popup != null && popup.getId() == SelectionPopup.ID) {
			myFBReaderApp.hideActivePopup();
		}
	}

	private void onPreferencesUpdate(Book book) {
		AndroidFontUtil.clearFontCache();
		myFBReaderApp.onBookUpdated(book);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_PREFERENCES:
			if (resultCode != RESULT_DO_NOTHING && data != null) {
				final Book book = FBReaderIntents.getBookExtra(data);
				if (book != null) {
					getCollection().bindToService(this, new Runnable() {
						public void run() {
							onPreferencesUpdate(book);
						}
					});
				}
			}
			break;

		case REQUEST_CANCEL_MENU:
			runCancelAction(data);
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void runCancelAction(Intent intent) {
		final CancelMenuHelper.ActionType type;
		try {
			type = CancelMenuHelper.ActionType.valueOf(intent.getStringExtra(FBReaderIntents.Key.TYPE));
		} catch (Exception e) {
			// invalid (or null) type value
			return;
		}
		Bookmark bookmark = null;
		if (type == CancelMenuHelper.ActionType.returnTo) {
			bookmark = FBReaderIntents.getBookmarkExtra(intent);
			if (bookmark == null) {
				return;
			}
		}
		myFBReaderApp.runCancelAction(type, bookmark);
	}

	public void navigate() {
		((NavigationPopup) myFBReaderApp.getPopupById(NavigationPopup.ID)).runNavigation();
	}

	//
	// private Menu addSubmenu(Menu menu, String id) {
	// return menu.addSubMenu(ZLResource.resource("menu").getResource(id)
	// .getValue());
	// }
	//
	// private void addMenuItem(Menu menu, String actionId, Integer iconId,
	// String name) {
	// if (name == null) {
	// name = ZLResource.resource("menu").getResource(actionId).getValue();
	// }
	// final MenuItem menuItem = menu.add(name);
	// if (iconId != null) {
	// menuItem.setIcon(iconId);
	// }
	// menuItem.setOnMenuItemClickListener(myMenuListener);
	// myMenuItemMap.put(menuItem, actionId);
	// }
	//
	// private void addMenuItem(Menu menu, String actionId, String name) {
	// addMenuItem(menu, actionId, null, name);
	// }
	//
	// private void addMenuItem(Menu menu, String actionId, int iconId) {
	// addMenuItem(menu, actionId, iconId, null);
	// }
	//
	// private void addMenuItem(Menu menu, String actionId) {
	// addMenuItem(menu, actionId, null, null);
	// }
	//
	// private void fillMenu(Menu menu, List<MenuNode> nodes) {
	// for (MenuNode n : nodes) {
	// if (n instanceof MenuNode.Item) {
	// final Integer iconId = ((MenuNode.Item) n).IconId;
	// if (iconId != null) {
	// addMenuItem(menu, n.Code, iconId);
	// } else {
	// addMenuItem(menu, n.Code);
	// }
	// } else /* if (n instanceof MenuNode.Submenu) */{
	// final Menu submenu = addSubmenu(menu, n.Code);
	// fillMenu(submenu, ((MenuNode.Submenu) n).Children);
	// }
	// }
	// }

	// private void setupMenu(Menu menu) {
	// final String menuLanguage = ZLResource.getLanguageOption().getValue();
	// if (menuLanguage.equals(myMenuLanguage)) {
	// return;
	// }
	//
	// myMenuLanguage = menuLanguage;
	//
	// menu.clear();
	// fillMenu(menu, MenuData.topLevelNodes());
	// synchronized (myPluginActions) {
	// int index = 0;
	// for (PluginApi.ActionInfo info : myPluginActions) {
	// if (info instanceof PluginApi.MenuActionInfo) {
	// addMenuItem(menu, PLUGIN_ACTION_PREFIX + index++,
	// ((PluginApi.MenuActionInfo) info).MenuItemName);
	// }
	// }
	// }
	//
	// refresh();
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		// if (popup != null && popup.getId() == NavigationPopup.ID) {
		// myFBReaderApp.hideActivePopup();
		// }else{
		// navigate();
		// }
		// setupMenu(menu);
		return true;
	}

	protected void onPluginNotFound(final Book book) {
		getCollection().bindToService(this, new Runnable() {
			public void run() {
				final Book recent = getCollection().getRecentBook(0);
				if (recent != null && !recent.equals(book)) {
					myFBReaderApp.openBook(recent, null, null);
				} else {
					myFBReaderApp.openHelpBook();
				}
			}
		});
	}

	private void setStatusBarVisibility(boolean visible) {
		final ZLAndroidLibrary zlibrary = getZLibrary();
		if (DeviceType.Instance() != DeviceType.KINDLE_FIRE_1ST_GENERATION && !myShowStatusBarFlag) {
			if (visible) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			} else {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
		}
	}

	private boolean isFinished = false;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
			if (popup != null && popup.getId() == NavigationPopup.ID) {
				myFBReaderApp.hideActivePopup();
			} else if (sinaBook != null && !DownBookManager.getInstance().hasBook(sinaBook)) {// 弹框提示用户
				String tip = String.format(getString(R.string.import_tip), sinaBook.getTitle());
				CommonDialog.show(this, null, tip, getString(R.string.add_shelves_cancle), getString(R.string.add_shelves_ok), new Add2ShelfListener(sinaBook));
			} else {
				saveReadPercent();

				if (myMainView != null) {
					myMainView.clear();
				}
				myFBReaderApp.hideActivePopup();
				myFBReaderApp.closeWindow();
				myFBReaderApp.clear();
				isFinished = true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
			if (popup != null && popup.getId() == NavigationPopup.ID) {
				myFBReaderApp.hideActivePopup();
			} else {
				navigate();
			}
		}
		return true;
		// return super.onKeyDown(keyCode, event);
		// return (myMainView != null && myMainView.onKeyDown(keyCode, event))
		// || super.onKeyDown(keyCode, event);
	}

	public void onQuit() {
		if (sinaBook != null && !DownBookManager.getInstance().hasBook(sinaBook)) {// 弹框提示用户
			String tip = String.format(getString(R.string.import_tip), sinaBook.getTitle());
			CommonDialog.show(this, null, tip, getString(R.string.add_shelves_cancle), getString(R.string.add_shelves_ok), new Add2ShelfListener(sinaBook));
			myFBReaderApp.hideActivePopup();
		} else {
			saveReadPercent();

			if (myMainView != null) {
				myMainView.clear();
			}
			myFBReaderApp.hideActivePopup();
			myFBReaderApp.closeWindow();
			myFBReaderApp.clear();
			isFinished = true;
		}
	}

	public class Add2ShelfListener extends CommonDialog.DefaultListener {

		private com.sina.book.data.Book mBook;

		public Add2ShelfListener(com.sina.book.data.Book book) {
			mBook = book;
		}

		@Override
		public void onLeftClick(DialogInterface dialog) {// 取消
			super.onLeftClick(dialog);
			DBService.deleteAllBookMark(mBook);
			dialog.dismiss();
			deleteTempFile(mBook);
			close();
		}

		@Override
		public void onRightClick(DialogInterface dialog) {// 加入书架
			super.onRightClick(dialog);
			dialog.dismiss();
			addBook2Shelf(mBook);
			saveReadPercent();
			close();
		}

	}

	private void addBook2Shelf(com.sina.book.data.Book book) {
		if (Util.isNullOrEmpty(book.getAuthor())) {
			book.setAuthor(getString(R.string.unkonw_author));
		}
		book.getDownloadInfo().setDownloadTime(new Date().getTime());
		DownBookManager.getInstance().addBookToShelves(book);

		if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			String ownerUid = LoginUtil.getLoginInfo().getUID();
			book.setOwnerUid(ownerUid);
			CloudSyncUtil.getInstance().add2Cloud(this, book);
			// CloudSyncUtil.getInstance().add2CloudAndShelves(this, book,
			// null);
		}

		DBService.saveBook(book);
		DBService.updateAllChapter(book, book.getChapters());
	}

	private void deleteTempFile(com.sina.book.data.Book gBook) {
		String path = gBook.getDownloadInfo().getFilePath();
		if (null != path && path.contains(StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK))) {
			// 修复：BugID=21316
			// 微盘的书籍文件丢失时进入阅读，以下两个条件成立
			boolean isOnlineTempFile = path.endsWith(com.sina.book.data.Book.ONLINE_TMP_SUFFIX);
			if (gBook.isVDiskBook() && isOnlineTempFile) {
				return;
			}
			int dotIndex = path.lastIndexOf(".");
			if (dotIndex > 0) {
				String oltmpPath = path.substring(0, dotIndex) + com.sina.book.data.Book.ONLINE_TMP_SUFFIX;
				File bookOlFile = new File(oltmpPath);
				if (bookOlFile.exists()) {
					bookOlFile.delete();
				}
			}
		}
	}

	private com.sina.book.data.Book createBookFromFile(File file) {
		final com.sina.book.data.Book book = new com.sina.book.data.Book();
		// 注意，这里没有设置book的FilePath是因为epub文件需要转换为txt，所以只设置了文件的原始路径，
		// txt可以再创建txt文件book对象时进行设置，而epub则在转换epub文件时进行设置
		book.getDownloadInfo().setOriginalFilePath(file.getAbsolutePath());
		book.getDownloadInfo().setFileSize(file.length());
		book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FINISHED);
		book.getDownloadInfo().setImageUrl(com.sina.book.data.Book.SDCARD_PATH_IMG + file.getAbsolutePath());
		int dotIndex = file.getName().lastIndexOf(".");
		StringBuilder sb = new StringBuilder(file.getName().substring(0, dotIndex));
		book.setTitle(sb.toString());
		book.getDownloadInfo().setProgress(1.0);
		book.getBuyInfo().setPayType(com.sina.book.data.Book.BOOK_TYPE_FREE);
		book.getDownloadInfo().setLocationType(com.sina.book.data.Book.BOOK_SDCARD);
		book.setAuthor(getString(R.string.unkonw_author));
		return book;
	}

	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// return (myMainView != null && myMainView.onKeyUp(keyCode, event))
	// || super.onKeyUp(keyCode, event);
	// }

	private void setButtonLight(boolean enabled) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			setButtonLightInternal(enabled);
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private void setButtonLightInternal(boolean enabled) {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.buttonBrightness = enabled ? -1.0f : 0.0f;
		getWindow().setAttributes(attrs);
	}

	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;
	private boolean myStartTimer;

	public final void createWakeLock() {
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
					myWakeLock.acquire();
				}
			}
		}
		if (myStartTimer) {
			myFBReaderApp.startTimer();
			myStartTimer = false;
		}
	}

	private final void switchWakeLock(boolean on) {
		if (on) {
			if (myWakeLock == null) {
				myWakeLockToCreate = true;
			}
		} else {
			if (myWakeLock != null) {
				synchronized (this) {
					if (myWakeLock != null) {
						myWakeLock.release();
						myWakeLock = null;
					}
				}
			}
		}
	}

	private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final int level = intent.getIntExtra("level", 100);
			// final
			// ZLAndroidApplication
			// application =
			// (ZLAndroidApplication)getApplication();
			setBatteryLevel(level);
			switchWakeLock(hasWindowFocus() && getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < level);
		}
	};

	private void setScreenBrightnessAuto() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		getWindow().setAttributes(attrs);
	}

	public void setScreenBrightness(int percent) {
		if (percent < 1) {
			percent = 1;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
		getZLibrary().ScreenBrightnessLevelOption.setValue(percent);
	}

	public int getScreenBrightness() {
		final int level = (int) (100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private BookCollectionShadow getCollection() {
		return (BookCollectionShadow) myFBReaderApp.Collection;
	}

	// methods from ZLApplicationWindow interface
	@Override
	public void showErrorMessage(String key) {
		UIUtil.showErrorMessage(this, key);
	}

	@Override
	public void showErrorMessage(String key, String parameter) {
		UIUtil.showErrorMessage(this, key, parameter);
	}

	@Override
	public FBReaderApp.SynchronousExecutor createExecutor(String key) {
		return UIUtil.createExecutor(this, key);
	}

	private int myBatteryLevel;

	@Override
	public int getBatteryLevel() {
		return myBatteryLevel;
	}

	private void setBatteryLevel(int percent) {
		myBatteryLevel = percent;
	}

	@Override
	public void close() {
		if (myMainView != null) {
			myMainView.clear();
		}
		finish();
		isFinished = true;
	}

	@Override
	public ZLViewWidget getViewWidget() {
		return myMainView;
	}

	private final HashMap<MenuItem, String> myMenuItemMap = new HashMap<MenuItem, String>();

	private final MenuItem.OnMenuItemClickListener myMenuListener = new MenuItem.OnMenuItemClickListener() {
		public boolean onMenuItemClick(MenuItem item) {
			myFBReaderApp.runAction(myMenuItemMap.get(item));
			return true;
		}
	};

	@Override
	public void refresh() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (Map.Entry<MenuItem, String> entry : myMenuItemMap.entrySet()) {
					final String actionId = entry.getValue();
					final MenuItem menuItem = entry.getKey();
					menuItem.setVisible(myFBReaderApp.isActionVisible(actionId) && myFBReaderApp.isActionEnabled(actionId));
					switch (myFBReaderApp.isActionChecked(actionId)) {
					case B3_TRUE:
						menuItem.setCheckable(true);
						menuItem.setChecked(true);
						break;
					case B3_FALSE:
						menuItem.setCheckable(true);
						menuItem.setChecked(false);
						break;
					case B3_UNDEFINED:
						menuItem.setCheckable(false);
						break;
					}
				}
			}
		});
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();

		final Intent intent = new Intent(FBReaderIntents.Action.ERROR, new Uri.Builder().scheme(exception.getClass().getSimpleName()).build());
		intent.setPackage(FBReaderIntents.DEFAULT_PACKAGE);
		intent.putExtra(ErrorKeys.MESSAGE, exception.getMessage());
		final StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		intent.putExtra(ErrorKeys.STACKTRACE, stackTrace.toString());
		/*
		 * if (exception instanceof BookReadingException) { final ZLFile file =
		 * ((BookReadingException)exception).File; if (file != null) {
		 * intent.putExtra("file", file.getPath()); } }
		 */
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// ignore
			e.printStackTrace();
		}
	}

	@Override
	public void setWindowTitle(final String title) {
		runOnUiThread(new Runnable() {
			public void run() {
				setTitle(title);
			}
		});
	}

	private BroadcastReceiver mySyncUpdateReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			myFBReaderApp.useSyncInfo(myResumeTimestamp + 10 * 1000 > System.currentTimeMillis());
		}
	};

	// 更新书签标识
	// private void updateBookmarkFlag() {
	// if (myFBReaderApp.BookTextView.isFlag) {
	// mBookmarkFlag.setVisibility(View.VISIBLE);
	// } else {
	// mBookmarkFlag.setVisibility(View.GONE);
	// }
	// rotateArrowDown();
	// }

	private class PageListener implements PageWidgetListener {

		public void onPrePage() {
		}

		public void onNextPage() {
		}

		public boolean isToolBarVisible() {
			return false;
		}

		// 工具栏书签改变
		public void onToolBar() {
		}

		// 页面改变
		public void onPageTurned() {
			updateBookmarkFlag();
		}

		// 是否到了首页或末页
		public void onFirstOrLastPage(boolean isFirstPage) {
			if (myBook != null) {
				if (isFirstPage) {
					Toast.makeText(FBReader.this, "已经是第一页了", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(FBReader.this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
				}
			}
		}

		/**
		 * 开始下拉
		 */
		public void onPullStart() {
			ZLog.d(ZLog.EpubReadPullDownAction, "onPullStart(开始下拉) >>");
			mPullDownLayout.setVisibility(View.VISIBLE);
			if (isPullStartFlag) {
				isPullStartFlag = false;
				computePullDownLayoutState();
			}
		}

		/**
		 * 下拉已经达到临界值了
		 */
		public void onPullStateDown() {
			ZLog.d(ZLog.EpubReadPullDownAction, "onPullStateDown(下拉已经达到临界值了) >>");
			boolean hasMark = hasBookmark();
			if (hasMark) {
				mBookmarkFlag.setVisibility(View.VISIBLE);
				mPullDownText.setText(R.string.pulldown_tv_del_ready);
			} else {
				mBookmarkFlag.setVisibility(View.GONE);
				mPullDownText.setText(R.string.pulldown_tv_add_ready);
			}
			rotateArrowUp();
		}

		/**
		 * 页面已经开始下拉了
		 */
		public void onPullStateUp() {
			ZLog.d(ZLog.EpubReadPullDownAction, "onPullStateUp(页面已经开始下拉了) >>");
			computePullDownLayoutState();
		}

		/**
		 * 成功的下拉，需要判断是添加一个书签还是删除书签
		 */
		public void onPullDown() {
			ZLog.d(ZLog.EpubReadPullDownAction, "onPullDown(成功的下拉，需要判断是添加一个书签还是删除书签) >>");
			addOrDeleteMark();
		}

		/**
		 * 下拉操作结束
		 */
		public void onPullEnd() {
			ZLog.d(ZLog.EpubReadPullDownAction, "onPullEnd(下拉操作结束) >>");
			mPullDownLayout.setVisibility(View.GONE);
			isPullStartFlag = true;
		}

		public void onBookSummaryPop(BookSummaryPostion bookSummaryPostion) {

		}

		@Override
		public boolean isBookSummaryPopVisible() {
			return false;
		}
	}

	/**
	 * 每次下拉的时候都重新判断当前页的书签添加情况<br>
	 * 因为onPullStart方法几乎在整个下拉过程中都调用了，这里使用一个Boolean值来限制一下<br>
	 */
	private boolean isPullStartFlag = true;

	private void computePullDownLayoutState() {
		boolean hasMark = hasBookmark();
		if (hasMark) {
			mBookmarkFlag.setVisibility(View.VISIBLE);
			mPullDownText.setText(R.string.pulldown_tv_del_normal);
		} else {
			mBookmarkFlag.setVisibility(View.GONE);
			mPullDownText.setText(R.string.pulldown_tv_add_normal);
		}
		rotateArrowDown();
	}

	public void addOrDeleteMark() {
		// 隐藏PopupPanel
		// org.geometerplus.zlibrary.core.application.ZLApplication.PopupPanel
		// popup = myFBReaderApp.getPopupById(NavigationPopup.ID);
		myFBReaderApp.hideActivePopup();

		// // if (mCurMark != null) {
		// // mBookmarkFlag.setVisibility(View.VISIBLE);
		// mPullDownText.setText(R.string.pulldown_tv_del_normal);
		// // } else {
		// // mBookmarkFlag.setVisibility(View.GONE);
		// mPullDownText.setText(R.string.pulldown_tv_add_normal);
		// // }
		//
		// // DEBUG CJL 删除书签
		// myMainView.deleteCurrPageAllBookmark();

		// 判断是删除还是添加
		if (hasBookmark()) {
			// 删除
			deleteBookmark();
		} else {
			// 添加
			addBookmark();
		}
	}

	private void addBookmark() {
		Bookmark bookmark = myFBReaderApp.createBookmark(20, true);
		if (bookmark != null) {
			myFBReaderApp.Collection.saveBookmark(bookmark);
			shortToast(R.string.add_mark_succ);
			showBookmarkFlag();
		} else {
			// shortToast(R.string.add_mark_not);
		}
	}

	private void deleteBookmark() {
		// ZLAndroidWidget widget = (ZLAndroidWidget) myFBReaderApp
		// .getWindow().getViewWidget();
		myMainView.deleteCurrPageAllBookmark();
		shortToast(R.string.delete_mark_succ);
		hideBookmarkFlag();
	}

	private boolean hasBookmark() {
		// FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		boolean hasMark = false;
		ZLTextView text = myFBReaderApp.getTextView();
		ZLTextPage textPage = text.getPage(PageIndex.current);
		if (textPage != null) {
			hasMark = textPage.hasBookmark();
		}
		return hasMark;
	}

	/**
	 * 更新当前书签状态,同时更新章节信息.
	 */
	public void updateBookmarkFlag() {
		boolean hasMark = hasBookmark();

		// 滑动翻页不显示书签
		// DEBUG CJL 动画模式是否在两个阅读引擎间关联了？
		// if (mReadStyleManager.getReadAnim() ==
		// ReadStyleManager.ANIMATION_TYPE_SCROLL) {
		// mBookmarkFlag.setVisibility(View.GONE);
		// return;
		// }

		ZLog.e(ZLog.ZLAndroidWidget, "删除书签逻辑Log ~ 是否有书签 > RBReader->updateBookmarkFlag > " + hasMark);
		ZLog.d(ZLog.FBReader, "updateBookmarkFlag >> hasMark = " + hasMark);

		if (hasMark) {
			mBookmarkFlag.setVisibility(View.VISIBLE);
			// mPullDownText.setText(R.string.pulldown_tv_del_normal);
		} else {
			mBookmarkFlag.setVisibility(View.GONE);
			// mPullDownText.setText(R.string.pulldown_tv_add_normal);
		}

		// rotateArrowDown();
	}

	private void showBookmarkFlag() {
		// if (mReadStyleManager.getReadAnim() ==
		// ReadStyleManager.ANIMATION_TYPE_SCROLL) {
		// mBookmarkFlag.setVisibility(View.GONE);
		// return;
		// }

		mBookmarkFlag.setVisibility(View.VISIBLE);
		mPullDownText.setText(R.string.pulldown_tv_del_normal);
		rotateArrowDown();
	}

	private void hideBookmarkFlag() {
		// if (mReadStyleManager.getReadAnim() ==
		// ReadStyleManager.ANIMATION_TYPE_SCROLL) {
		// mBookmarkFlag.setVisibility(View.GONE);
		// return;
		// }

		mBookmarkFlag.setVisibility(View.GONE);
		mPullDownText.setText(R.string.pulldown_tv_add_normal);
		rotateArrowDown();
	}

	/**
	 * 旋转箭头向下
	 */
	private void rotateArrowDown() {
		mPullDownImage.clearAnimation();
		Animation rotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateDownAnim.setDuration(180);
		rotateDownAnim.setFillAfter(true);
		mPullDownImage.startAnimation(rotateDownAnim);
	}

	/**
	 * 旋转箭头向上
	 */
	private void rotateArrowUp() {
		mPullDownImage.clearAnimation();
		Animation rotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateUpAnim.setDuration(180);
		rotateUpAnim.setFillAfter(true);
		mPullDownImage.startAnimation(rotateUpAnim);
	}
}