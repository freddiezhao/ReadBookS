package com.sina.book.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.geometerplus.android.util.ZLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.sina.book.SinaBookApplication;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.util.FileUtils;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;

/**
 * 数据库创建
 * 
 * @author fzx
 * 
 */
public class DBOpenHelper extends SdCardSQLiteOpenHelper
{
	private static final String	TAG								= "DBOpenHelper";
	private static final String	DB_NAME							= "sinareader.db";
	public static String		DB_PATH							= Environment.getExternalStorageDirectory().toString() + "/sina/reader/db/";
	// TODO 数据库的版本号不能随意改动，只有在数据库中的某个表需要增加或删除某个字段时升级版本号
	// private static final int VERSION = 15;
	// private static final int VERSION = 16;
	private static final int	VERSION							= 17;																			// 首次支持线上的epub书籍阅读的版本
	/**
	 * 书本表目前版本有43列<br>
	 * 书签表目前版本11列<br>
	 * 章节表目前版本11列<br>
	 * 书摘表目前版本13列<br>
	 * 更新数据库完成后，会对列数做比对，只要列数不对，<br>
	 * 认为数据库更新有问题，删表重新创建<br>
	 * WARN 更新表后，请同步跟新这里
	 */
	private static final int	NOW_VERSION_BOOK_COLUMNS		= 43;
	// 1.9.0版本对书签和书摘表分别增加了book_id字段，所以需要
	// 将NOW_VERSION_BOOKMARK_COLUMNS和NOW_VERSION_BOOKSUMMARY_COLUMNS值分别+1
	// 各自由原来的11，13增加为12，14
	private static final int	NOW_VERSION_BOOKMARK_COLUMNS	= 12;
	private static final int	NOW_VERSION_BOOKSUMMARY_COLUMNS	= 14;
	private static final int	NOW_VERSION_CHAPTER_COLUMNS		= 11;
	// 缓存表加了6个列(TITLE,AUTHOR,INTRO,IMAGE_URL,UID,TAG)，由原来的16增加到22
	private static final int	NOW_VERSION_BOOKCACHE_COLUMNS	= 22;

	public Context				context;

	public DBOpenHelper(Context context)
	{
		super(DB_PATH, DB_NAME, null, VERSION);
		this.context = context;
	}

	@Override
	public SQLiteDatabase onCreateDatabase(String dbPath, String dbName, CursorFactory factory)
	{
//		LogUtil.d(TAG, "db onCreateDatabase");
		if (!StorageUtil.isSDCardExist()) {
			return null;
		}

		SQLiteDatabase db = null;
		// 创建数据库
		File dir = new File(dbPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File dbf = new File(dbPath + dbName);
		if (dbf.exists()) {
			dbf.delete();
		}

		// 拷贝本地书籍
		copyDataBase();

		db = SQLiteDatabase.openOrCreateDatabase(dbf, null);
		return db;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		LogUtil.d(TAG, "db onCreate");
		// warn:数据库的字段添加往表后添加，防止影响以前版本的使用
		// 尽量的兼容前面版本

		try {
			// 创建书本表
			db.execSQL(BookTable.getCreateSQL());

			// 创建章节信息表
			db.execSQL(ChapterTable.getCreateSQL());

			// 创建书签表
			db.execSQL(BookMarkTable.getCreateSQL());

			// 创建书摘表
			db.execSQL(BookSummaryTable.getCreateSQL());

			// 创建缓存表
			createCacheTable(db);

			// 创建书本缓存表
			createBookCacheTable(db);
		} catch (Exception e) {
			// TODO: handle exception
			LogUtil.d(TAG, "db onCreate, Exception >> e:" + e);
		}
	}

	/**
	 * 创建缓存表
	 * 
	 * @param db
	 */
	private void createCacheTable(SQLiteDatabase db)
	{
		// 缓存表数据不重要，每次创建都直接先删除掉
		db.execSQL(DataCacheTable.getDeleteSQL());
		db.execSQL(DataCacheTable.getCreateSQL());
	}

	/**
	 * 创建书本缓存表
	 * 
	 * @param db
	 */
	private void createBookCacheTable(SQLiteDatabase db)
	{
		// 缓存表数据不重要，每次创建都直接先删除掉
		db.execSQL(BookCacheTable.getDeleteSQL());
		db.execSQL(BookCacheTable.getCreatSQL());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int dbVersion, int newVersion)
	{
		LogUtil.d(TAG, "db onUpgrade oldVersion=" + dbVersion + ", newVersion=" + newVersion);
		LogUtil.d("ReadInfoLeft", "DBOpenHelper >> onUpgrade oldVersion=" + dbVersion + ", newVersion=" + newVersion);
		// TODO 这里没有必要onCreate吧!?
		onCreate(db);

		LogUtil.d(TAG, "onUpgrade oldVersion : " + dbVersion);
		LogUtil.d(TAG, "onUpgrade newVersion : " + newVersion);
		LogUtil.d(TAG, "onUpgrade getVersion : " + db.getVersion());
		// 当新版本小于当前数据库版本时，不更新
		// 这样的话，我们的数据库需要向前兼容
		// int dbVersion = db.getVersion();
		if (newVersion <= dbVersion) {
			return;
		}
		try {
			if (dbVersion < 2) {
				v1to2(db);
			}
			if (dbVersion < 3) {
				v2to3(db);
			}
			if (dbVersion < 4) {
				v3to4(db);
			}
			if (dbVersion < 5) {
				v4to5(db);
			}
			if (dbVersion < 6) {
				v5to6(db);
			}
			if (dbVersion < 7) {
				v6to7(db);
			}
			if (dbVersion < 8) {
				v7to8(db);
			}
			if (dbVersion < 9) {
				v8to9(db);
			}
			if (dbVersion < 10) {
				v9to10(db);
			}
			if (dbVersion < 11) {
				v10to11(db);
			}
			if (dbVersion < 12) {
				v11to12(db);
			}
			if (dbVersion < 13) {
				v12to13(db);
			}
			if (dbVersion < 14) {
				v13to14(db);
			}
			if (dbVersion < 15) {
				v14to15(db);
			}

			// if(dbVersion <= 16) {
			// v16to17(db);
			// }

			// 此判断多余了
			// if (newVersion > dbVersion) {
			changeAssetDB(db);
			// }

			updateTableCheck(db);
		} catch (Exception e) {
			// 如果实在更新不成功，重新创建
			if (dbVersion == 14) {
				// version 14->15 只更新了书签书摘数据库，删除也应该只删除这2个数据库
				db.execSQL(BookMarkTable.getDeleteSQL());
				db.execSQL(BookSummaryTable.getDeleteSQL());

				// 创建书签表
				db.execSQL(BookMarkTable.getCreateSQL());
				// 创建书摘表
				db.execSQL(BookSummaryTable.getCreateSQL());
			} else {
				deleteAllTable(db, "onUpgrade Exception");
				onCreate(db);
			}
		}
		db.setVersion(newVersion);
	}

	private void copyDataBase(String outFileName)
	{
		InputStream myInput = null;
		OutputStream myOutput = null;
		try {
			myInput = SinaBookApplication.gContext.getAssets().open(DB_NAME);
			// String outFileName = DB_PATH + DB_NAME;
			myOutput = new FileOutputStream(outFileName);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (myOutput != null) {
					myOutput.flush();
					myOutput.close();
				}
				if (myInput != null) {
					myInput.close();
				}
			} catch (IOException e) {
				LogUtil.d(TAG, "close db file failed");
				e.printStackTrace();
			}
		}
	}

	private void copyDataBase()
	{
		String path = DB_PATH + DB_NAME;
		copyDataBase(path);
	}

	/**
	 * 删除所有表
	 * 
	 * @param db
	 */
	private void deleteAllTable(SQLiteDatabase db, String tag)
	{
		LogUtil.d("DBOpenHelper", "deleteAllTable >> tag=" + tag);
		db.execSQL(BookTable.getDeleteSQL());
		db.execSQL(ChapterTable.getDeleteSQL());
		db.execSQL(BookMarkTable.getDeleteSQL());
		db.execSQL(BookSummaryTable.getDeleteSQL());
		db.execSQL(DataCacheTable.getDeleteSQL());
		db.execSQL(BookCacheTable.getDeleteSQL());
	}

	/**
	 * 管理各数据库版本变化，检查数据库的更新是否有效
	 */
	private void updateTableCheck(SQLiteDatabase db)
	{
		Cursor cursor = null;

		try {
			String sql = null;
			int bookColumnsCount = -1;
			int markColumnsCount = -1;
			int chapterColumnsCount = -1;
			int summaryColumnsCount = -1;
			int bookCacheColumnsCount = -1;

			// 比对书本表
			sql = "SELECT * from Book LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				bookColumnsCount = cursor.getColumnCount();
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			LogUtil.d(TAG, "book table: now:" + bookColumnsCount + " real:" + NOW_VERSION_BOOK_COLUMNS);
			if (bookColumnsCount != -1 && bookColumnsCount != NOW_VERSION_BOOK_COLUMNS) {
				deleteAllTable(db, "book");
				onCreate(db);
				return;
			}

			// 比对书签表
			sql = "SELECT * from BookMark LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				markColumnsCount = cursor.getColumnCount();
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			LogUtil.d(TAG, "bookmark table: now:" + markColumnsCount + " real:" + NOW_VERSION_BOOKMARK_COLUMNS);
			if (markColumnsCount != -1 && markColumnsCount != NOW_VERSION_BOOKMARK_COLUMNS) {
				deleteAllTable(db, "bookmark");
				onCreate(db);
				return;
			}

			// 比对章节表
			sql = "SELECT * from Chapter LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				chapterColumnsCount = cursor.getColumnCount();
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			LogUtil.d(TAG, "chapter table: now:" + chapterColumnsCount + " real:" + NOW_VERSION_CHAPTER_COLUMNS);
			if (chapterColumnsCount != -1 && chapterColumnsCount != NOW_VERSION_CHAPTER_COLUMNS) {
				deleteAllTable(db, "chapter");
				onCreate(db);
				return;
			}

			// 比对书摘表
			sql = "SELECT * from BookSummary LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				summaryColumnsCount = cursor.getColumnCount();
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			LogUtil.d(TAG, "booksummary table: now:" + summaryColumnsCount + " real:" + NOW_VERSION_BOOKSUMMARY_COLUMNS);
			if (summaryColumnsCount != -1 && summaryColumnsCount != NOW_VERSION_BOOKSUMMARY_COLUMNS) {
				deleteAllTable(db, "booksummary");
				onCreate(db);
				return;
			}

			// 比对书本缓存表
			sql = "SELECT * from BookCache LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				bookCacheColumnsCount = cursor.getColumnCount();
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			LogUtil.d(TAG, "BookCache table: now:" + bookCacheColumnsCount + " real:" + NOW_VERSION_BOOKCACHE_COLUMNS);
			if (bookCacheColumnsCount != -1 && bookCacheColumnsCount != NOW_VERSION_BOOKCACHE_COLUMNS) {
				deleteAllTable(db, "BookCache");
				onCreate(db);
				return;
			}
		} catch (Exception e) {
			// doNothing
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	// 版本1到版本2
	private void v1to2(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD lastReadPercent float(20) DEFAULT(0)");
		db.execSQL("ALTER TABLE Book ADD downloadTime largeint(256)");
		db.execSQL("ALTER TABLE Book ADD vdiskDownloadUrl varchar(256)");
		db.execSQL("ALTER TABLE Book ADD vdiskFilePath varchar(256)");
		db.execSQL("ALTER TABLE Book ADD uid varchar(256)");
	}

	// 版本2到版本3
	private void v2to3(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD sid varchar(50)");
	}

	// 版本3到版本4
	private void v3to4(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD isParise integer DEFAULT (0)");
	}

	// 版本4到版本5
	private void v4to5(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD contentType integer DEFAULT (0)");
		db.execSQL("ALTER TABLE Book ADD suiteId integer DEFAULT (0)");
		// 创建书摘表
		db.execSQL(BookSummaryTable.getCreateSQL());
	}

	// 版本5到版本6
	private void v5to6(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD suiteName varchar(80)");
	}

	// 版本6到版本7
	private void v6to7(SQLiteDatabase db)
	{
		createCacheTable(db);
	}

	// 版本7到版本8
	private void v7to8(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD autoBuy integer DEFAULT (0)");
		db.execSQL("ALTER TABLE Book ADD isOnlineBook integer DEFAULT (0)");
		db.execSQL("ALTER TABLE Book ADD ownerUid varchar(256)");
		db.execSQL("ALTER TABLE Book ADD onlineReadChapterId integer DEFAULT (0)");
	}

	// 版本8到版本9
	private void v8to9(SQLiteDatabase db)
	{
		createBookCacheTable(db);
	}

	// 版本9到版本10
	private void v9to10(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD isRemind integer DEFAULT (1)");
	}

	// 版本10到版本11
	private void v10to11(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Chapter ADD chapterFlags integer DEFAULT (0)");
	}

	// 版本11到版本12
	private void v11to12(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD originSuiteId integer DEFAULT (0)");
	}

	// 版本12到版本13
	private void v12to13(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Chapter ADD serialNumber integer DEFAULT (-1)");
	}

	// 版本13到版本14
	private void v13to14(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE Book ADD lastReadJsonString text");
		db.execSQL("ALTER TABLE BookSummary ADD summaryJsonString text");
		db.execSQL("ALTER TABLE BookMark ADD markJsonString text");
	}

	// 版本14到15
	private void v14to15(SQLiteDatabase db)
	{
		db.execSQL("ALTER TABLE BookSummary ADD book_id text");
		db.execSQL("ALTER TABLE BookMark ADD book_id text");
	}

	// 版本15到16
	private void changeAssetDB(SQLiteDatabase db)
	{
		deleteOldDB(db);
		// addNewAsset(db);
	}

	// 版本16到17
	// private void v16to17(SQLiteDatabase db) {
	// db.execSQL("ALTER TABLE BookCache ADD "+BookCacheTable.TITLE+" varchar(256)");
	// db.execSQL("ALTER TABLE BookCache ADD "+BookCacheTable.AUTHOR+" varchar(20)");
	// db.execSQL("ALTER TABLE BookCache ADD "+BookCacheTable.INTRO+" text");
	// db.execSQL("ALTER TABLE BookCache ADD "+BookCacheTable.IMAGE_URL+" varchar(256)");
	// db.execSQL("ALTER TABLE BookCache ADD "+BookCacheTable.UID+" varchar(256)");
	// }

	private void deleteOldDB(SQLiteDatabase db)
	{
		String sql = "select * from " + BookTable.TABLE_NAME;
		Cursor cursor = db.rawQuery(sql, null);
		cursor = db.rawQuery(sql, null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				String path = cursor.getString(cursor.getColumnIndex(BookTable.FILE_PATH));
				if (path.contains("file:///android_asset/")) {
					int id = cursor.getInt(cursor.getColumnIndex(BookTable.ID));
					db.delete(BookTable.TABLE_NAME, BookTable.ID + " = ?", new String[] { id + "" });
				}
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

	public void addNewAsset(SQLiteDatabase db)
	{
		SharedPreferences preferences = context.getSharedPreferences(LoginUtil.LOGIN_INFO_NAME, Context.MODE_PRIVATE);
		String accessToken = preferences.getString(LoginUtil.KEY_ACCESS_TOKEN, "");
		if (TextUtils.isEmpty(accessToken)) {
			// 新版用户为未登录状态
			String path = DB_PATH + "tmp.db";
			copyDataBase(path);

			File file = new File(path);
			SQLiteDatabase tmpDb = SQLiteDatabase.openOrCreateDatabase(file, null);

			String sql = "select * from " + BookTable.TABLE_NAME;
			Cursor cursor = tmpDb.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					Book book = new Book();
					DBService.readBookFromCursor(cursor, book);

					ZLog.d(ZLog.FBReader, "--1-------: " + book.getId());

					sql = "select * from " + ChapterTable.TABLE_NAME + " where " + ChapterTable.BOOK_ID
							+ " = ? order by " + ChapterTable.CHAPTER_ID;
					Cursor chapter_cursor = tmpDb.rawQuery(sql, new String[] { String.valueOf(book.getId()) });

					ArrayList<Chapter> chapters = new ArrayList<Chapter>();
					if (chapter_cursor != null && chapter_cursor.moveToFirst()) {
						do {
							Chapter chapter = new Chapter();
							DBService.readChapterFromCursor(chapter_cursor, chapter);
							chapters.add(chapter);
						} while (chapter_cursor.moveToNext());
					}
					ZLog.d(ZLog.FBReader, "--2-------: " + chapters.size());

					if (chapter_cursor != null) {
						chapter_cursor.close();
						chapter_cursor = null;
					}

					DBService.saveBook(book, db);

					sql = "select * from " + BookTable.TABLE_NAME + " where " + BookTable.BOOK_ID + " = ?";
					Cursor cursor2 = db.rawQuery(sql, new String[] { book.getBookId() });
					if (cursor2 != null && cursor2.moveToFirst()) {
						int id = cursor2.getInt(cursor2.getColumnIndex(BookTable.ID));
						ZLog.d(ZLog.FBReader, "--3-------: " + id);

						book.setId(id);
						DBService.updateAllChapter(book, chapters);
					}
					if (cursor2 != null) {
						cursor2.close();
						cursor2 = null;
					}
				} while (cursor.moveToNext());
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			FileUtils.deleteFile(path);
		}
	}
}