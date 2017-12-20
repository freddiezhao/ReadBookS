package com.sina.book.db;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.geometerplus.android.util.ZLog;
import org.htmlcleaner.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.sina.book.control.DataCacheBean;
import com.sina.book.control.ICancelable;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookSummary;
import com.sina.book.data.Chapter;
import com.sina.book.data.MarkItem;
import com.sina.book.util.EpubHelper;
import com.sina.book.util.LogUtil;

/**
 * 封装所有数据库操作.
 * 
 * @author jsh
 */
public class DBService
{
	private static final String	TAG	= "DBService";

	/**
	 * 确保它的单例存在<br>
	 * 只在程序启动时做一次创建
	 */
	public static DBOpenHelper	sDbOpenHelper;

	/**
	 * 初始化DBService<br>
	 * 初始化单例的sDbOpenHelper<br>
	 * SQLiteDatabase会在sDbOpenHelper中缓存<br>
	 * 确认开始时调用<br>
	 * 
	 * @param context
	 */
	public synchronized static void init(Context context)
	{
		sDbOpenHelper = new DBOpenHelper(context);
		try {
			sDbOpenHelper.getWritableDatabase();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		}

	}

	/**
	 * 释放DBService<br>
	 * 确认程序退出时释放<br>
	 */
	public synchronized static void close()
	{
		LogUtil.d(TAG, "db close");
		try {
			if (sDbOpenHelper != null) {
				sDbOpenHelper.close();
				// sDbOpenHelper = null;
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * 清除所有表的数据
	 */
	public synchronized static void clear()
	{
		LogUtil.d(TAG, "db clear");
		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.beginTransaction();

			db.execSQL("delete from " + BookTable.TABLE_NAME);
			db.execSQL("delete from " + ChapterTable.TABLE_NAME);
			db.execSQL("delete from " + BookMarkTable.TABLE_NAME);
			db.execSQL("delete from " + BookSummaryTable.TABLE_NAME);
			db.execSQL("delete from " + DataCacheTable.TABLE_NAME);
			db.execSQL("delete from " + BookCacheTable.TABLE_NAME);
			LogUtil.e("ReadInfoLeft",
					"DBService >> clear >> delete from BookCacheTable");

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	/*********************************** Book相关Method开始 ************************************/
	/**
	 * Gets the book.
	 * 
	 * @param book
	 *            the book
	 * @return the book
	 */
	public synchronized static Book getBook(Book book)
	{
		LogUtil.d(TAG, "db getBook");
		if (book == null) {
			return null;
		}
		if (TextUtils.isEmpty(book.getBookId())
				&& TextUtils.isEmpty(book.getSid())
				&& TextUtils.isEmpty(book.getDownloadInfo()
						.getOriginalFilePath())) {
			return null;
		}

		String sql = null;
		Book newBook = null;

		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();

			if (db == null) {
				return null;
			}

			if (!TextUtils.isEmpty(book.getBookId())) {
				sql = BookTable.BOOK_ID + " = ?";
				cursor = db.query(BookTable.TABLE_NAME, BookTable.COLUMNS, sql,
						new String[] { book.getBookId() }, null, null, null);
			} else if (!TextUtils.isEmpty(book.getSid())) {
				sql = BookTable.SINA_ID + " = ?";
				cursor = db.query(BookTable.TABLE_NAME, BookTable.COLUMNS, sql,
						new String[] { book.getSid() }, null, null, null);
			} else if (!TextUtils.isEmpty(book.getDownloadInfo()
					.getOriginalFilePath())) {
				sql = BookTable.ORIGINAL_FILE_PATH + " = ?";
				cursor = db.query(BookTable.TABLE_NAME, BookTable.COLUMNS, sql,
						new String[] { book.getDownloadInfo()
								.getOriginalFilePath() }, null, null, null);
			}

			if (cursor != null && cursor.moveToFirst()) {
				newBook = new Book();
				readBookFromCursor(cursor, newBook);
			}
		} catch (Exception e) {
			LogUtil.d("comic", e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return newBook;
	}

	/**
	 * Exist the book.
	 * 
	 * @param book
	 *            the book
	 * @return the book is exist.
	 */
	public synchronized static boolean exists(Book book)
	{
		LogUtil.d(TAG, "db exists");
		boolean result = false;
		if (null != book) {

			if (!TextUtils.isEmpty(book.getBookId())
					|| !TextUtils.isEmpty(book.getSid())
					|| !TextUtils.isEmpty(book.getDownloadInfo()
							.getOriginalFilePath())) {

				Cursor cursor = null;
				try {
					SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
					if (null != db) {

						String sql = BookTable.BOOK_ID + " = ? or "
								+ BookTable.SINA_ID + " = ? or "
								+ BookTable.ORIGINAL_FILE_PATH + " = ?";
						cursor = db.query(BookTable.TABLE_NAME,
								BookTable.COLUMNS, sql,
								new String[] {
										book.getBookId(),
										book.getSid(),
										book.getDownloadInfo()
												.getOriginalFilePath() }, null,
								null, null);

						if (cursor != null) {
							if (cursor.getColumnCount() > 0) {
								result = true;
							}
							cursor.close();
							cursor = null;
						}
					}
				} catch (Exception e) {
					LogUtil.d(TAG, e.toString());
					e.printStackTrace();
				} finally {
					if (cursor != null) {
						cursor.close();
						cursor = null;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Gets the all book.
	 * 
	 * @return the all book
	 */
	public synchronized static ArrayList<Book> getAllBook()
	{
		LogUtil.d(TAG, "db getAllBook");
//		LogUtil.d("ReadInfoLeft", "db getAllBook");
		ArrayList<Book> books = null;
		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			String sql = "select * from " + BookTable.TABLE_NAME;
			if (db == null) {
				return null;
			}
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				books = new ArrayList<Book>();
				do {
					Book book = new Book();
					readBookFromCursor(cursor, book);
//					LogUtil.d("ReadInfoLeft", "db getAllBook book={" + book
//							+ "}");
//					LogUtil.i("CursorError", "db getAllBook book={" + book
//							+ "}");
					books.add(book);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return books;
	}

	/**
	 * 对书籍路径做一定的处理。 这里是为了解决1.6以前书籍文件路径重复的问题
	 */
	public synchronized static void updateBookPath()
	{
		LogUtil.d(TAG, "db updateBookPath");
		Cursor cursor = null;

		SQLiteDatabase db = null;

		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}
			db.beginTransaction();

			String sql = "select * from " + BookTable.TABLE_NAME;
			cursor = db.rawQuery(sql, null);

			ArrayList<Book> books = new ArrayList<Book>();
			// 先找出需要修改的书籍列表
			if (cursor != null && cursor.moveToFirst()) {
				ArrayList<String> paths = new ArrayList<String>();

				do {
					String originalPath = cursor.getString(cursor
							.getColumnIndex(BookTable.ORIGINAL_FILE_PATH));

					if (paths.contains(originalPath)) {
						Book book = new Book();
						readBookFromCursor(cursor, book);

						book.getDownloadInfo().setOriginalFilePath(null);
						book.getDownloadInfo().setFilePath(null);

						books.add(book);

					} else {
						paths.add(originalPath);
					}
				} while (cursor.moveToNext());
			}

			// 更新需要修改的书籍
			if (!books.isEmpty()) {
				for (Book book : books) {
					db.update(BookTable.TABLE_NAME, setBookValues(book),
							BookTable.ID + " = ?", new String[] { book.getId()
									+ "" });
				}
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();

		} finally {
			if (db != null) {
				db.endTransaction();
			}
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	public synchronized static void saveBooksChange(List<Book> updateBooks)
	{
		LogUtil.d(TAG, "db saveBooksChange update");
		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.beginTransaction();
			for (Book book : updateBooks) {
				LogUtil.i("Log", "db saveBooksChange update book={" + book
						+ "}");
				db.update(BookTable.TABLE_NAME, setBookValues(book),
						BookTable.ID + " = ?",
						new String[] { book.getId() + "" });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	public synchronized static void saveBooksChange(List<Book> delBooks,
			List<Book> insertBooks)
	{
		LogUtil.d(TAG, "db saveBooksChange insert and delete");
		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			// 实现查询一下Book表
			// Log.i("CursorError", "====================================");
			// getAllBook();
			// ArrayList<Book> allCacheBooks = getAllBookCache();
			// Log.i("CursorError", "====================================");
			// for (Book book : allCacheBooks) {
			// Log.i("CursorError", "allCacheBooks >>>>>>>>>>> book {" + book +
			// "}");
			// }
			// Log.i("CursorError", "====================================");

			db.beginTransaction();
			for (Book delBook : delBooks) {
				db.delete(BookTable.TABLE_NAME, BookTable.ID + " = ?",
						new String[] { delBook.getId() + "" });

				db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_SID
						+ " = ?", new String[] { delBook.getBookId() });
				db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID
						+ " = ?", new String[] { delBook.getId() + "" });

				db.delete(ChapterTable.TABLE_NAME, ChapterTable.BOOK_ID
						+ " = ?", new String[] { delBook.getId() + "" });

				db.delete(BookSummaryTable.TABLE_NAME,
						BookSummaryTable.SUMMARY_BID + " = ?",
						new String[] { delBook.getBookId() });
				db.delete(BookSummaryTable.TABLE_NAME, BookSummaryTable.BOOK_ID
						+ " = ?", new String[] { delBook.getId() + "" });

				delBook.setId(-1);
			}

			for (Book insBook : insertBooks) {
				if (insBook.getId() >= 0) {
					ContentValues values = setBookValues(insBook);
					values.put(BookTable.ID, insBook.getId());

					int rowId = checkBookExist(db, insBook);
					if (rowId == -1) {
						db.insert(BookTable.TABLE_NAME, null, values);

						Cursor cursor = db.rawQuery(
								"select LAST_INSERT_ROWID()", null);
						if (cursor != null && cursor.moveToFirst()) {
							insBook.setId(cursor.getInt(0));
						}
						if (cursor != null) {
							cursor.close();
						}
					} else {
						db.update(BookTable.TABLE_NAME, setBookValues(insBook),
								BookTable.ID + " = ?",
								new String[] { insBook.getId() + "" });
					}

					// CJL BugID=21492
					db.delete(BookCacheTable.TABLE_NAME,
							BookCacheTable.ORIGIN_ID + " = ?",
							new String[] { insBook.getId() + "" });
				} else {
					int rowId = checkBookExist(db, insBook);
					if (rowId == -1) {
						db.insert(BookTable.TABLE_NAME, null,
								setBookValues(insBook));
						Cursor cursor = db.rawQuery(
								"select LAST_INSERT_ROWID()", null);
						if (cursor != null && cursor.moveToFirst()) {
							insBook.setId(cursor.getInt(0));
						}
						if (cursor != null) {
							cursor.close();
						}
					} else {
						insBook.setId(rowId);
						db.update(BookTable.TABLE_NAME, setBookValues(insBook),
								BookTable.ID + " = ?",
								new String[] { insBook.getId() + "" });
					}
				}

				LogUtil.i("ReadInfoLeft",
						"DBService saveBooksChange 插入数据insertBooks >> insBook{"
								+ insBook + "}");
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			ZLog.d(ZLog.FBReader, ">>>>>>>>>>> e {" + e + "}");
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 检查书籍是否存在
	 * 
	 * @param db
	 * @param book
	 * @return
	 */
	public synchronized static int checkBookExist(SQLiteDatabase db, Book book)
	{
		Cursor findCursor;
		if (book.isVDiskBook() || book.isLocalImportBook()) {
			String sql = "select 1 from " + BookTable.TABLE_NAME + " where "
					+ BookTable.BOOK_ID + " = ? and " + BookTable.TITLE
					+ " = ?";
			findCursor = db.rawQuery(sql,
					new String[] { book.getBookId(), book.getTitle() });
		} else {
			String sql = "select 1 from " + BookTable.TABLE_NAME + " where "
					+ BookTable.BOOK_ID + " = ?";
			findCursor = db.rawQuery(sql, new String[] { book.getBookId() });
		}

		int cursorRowId = -1;

		if ((/* book.getId() <= 0 && */findCursor != null && findCursor.moveToFirst())) {//findCursor.getCount() > 0
//			StringBuffer cursorResult = new StringBuffer();
//			int columnCount = findCursor.getColumnCount();
//			String[] columnNames = findCursor.getColumnNames();
//			Log.d("CursorError", ">>>>>>>>>>> columnCount = "+columnCount+", columnNames = "+columnNames);
//			for (String name : columnNames) {
//				int columnIndex = findCursor.getColumnIndexOrThrow(name);
//				int type = findCursor.getType(columnIndex);
//				switch (type) {
//				case Cursor.FIELD_TYPE_STRING:
//					cursorResult.append(findCursor.getString(columnIndex));
//					break;
//				case Cursor.FIELD_TYPE_FLOAT:
//					cursorResult.append(findCursor.getFloat(columnIndex));
//					break;
//				case Cursor.FIELD_TYPE_INTEGER:
//					cursorResult.append(findCursor.getInt(columnIndex));
//					break;
//				}
//			}
//			Log.d("CursorError", ">>>>>>>>>>> cursorResult = "+cursorResult.toString());
			

			int columnIndex = findCursor.getColumnIndex(BookTable.ID);
			if (columnIndex >= 0) {
				cursorRowId = findCursor.getInt(columnIndex);
			}
			// if (findCursor != null) {
			// findCursor.close();
			// }
			// return cursorRowId;
		}

		if (findCursor != null) {
			findCursor.close();
		}
		return cursorRowId;
	}

	/**
	 * 删除在线书籍，并缓存下来已下到本地的书籍
	 * 
	 * @param delBooks
	 */
	public synchronized static void delBooksOrToCache(List<Book> delBooks)
	{
		LogUtil.d(TAG, "delBooksOrToCache");
		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.beginTransaction();
			for (Book delBook : delBooks) {
				int rowId = db.delete(BookTable.TABLE_NAME, BookTable.ID
						+ " = ?", new String[] { delBook.getId() + "" });

//				LogUtil.e("BugID=21665", "delBooksOrToCache >> rowId:::"
//						+ rowId);

				if (rowId <= 0) {
					db.delete(BookTable.TABLE_NAME, BookTable.BOOK_ID + " = ?",
							new String[] { delBook.getBookId() + "" });
				}
//				LogUtil.d("ReadInfoLeft", "delBooksOrToCache >> delBook{"
//						+ delBook + "}");
				// 非在线书籍缓存下来
				if (!delBook.isOnlineBook()) {// || delBook.getTag() ==
												// Book.IS_NEW
					// epub书籍就算没下载完成也要缓存
					if (!delBook.isEpub()
							&& delBook.getDownloadInfo().getDownLoadState() != DownBookJob.STATE_FINISHED) {
						// db.delete(BookMarkTable.TABLE_NAME,
						// BookMarkTable.BOOK_ID + " = ?",
						// new String[] { delBook.getId() + "" });
						// db.delete(BookMarkTable.TABLE_NAME,
						// BookMarkTable.BOOK_SID + " = ?",
						// new String[] { delBook.getBookId() });

						db.delete(ChapterTable.TABLE_NAME, ChapterTable.BOOK_ID
								+ " = ?", new String[] { delBook.getId() + "" });
					} else {
						LogUtil.d("ReadInfoLeft",
								"delBooksOrToCache >> 插入 delBook{" + delBook
										+ "}");
						// 缓存到BookCache表
						/**
						 * 当前对epub书籍的处理操作逻辑是：epub书籍的不予同步，无论是删除还是加入书架，都将被终止掉，<br>
						 * 但是UID还是依然绑定给Book，这样当用户注销登录时也能将Book移除。<br>
						 * 但是移除Book时没有同步移除本地sina/reader/book下的epub文件，因此还需要做进一步的
						 * 处理，备用方案：<br>
						 * (1)在用户注销时，移除该Book的同时，将其对应的本地epub文件后缀名改成etemp;<br>
						 * 保留epub文件不删除的原因：用户下载下来的epub书籍是出于完全下载完成的状态，<br>
						 * 在注销账户的时候，程序会将绑定了UID的书籍并且出于完
						 * 全下载完成状态下的书籍缓存到BookCache表中，<br>
						 * 当下次重新登录该账户的时候，大部分的数据从该缓存表中读取，包括阅读进度等，
						 * 因此这本epub书籍也将被缓存起来。<br>
						 * (2)当用户再次登录相同账户时，需要还原对应的etemp文件为epub文件，
						 * 然后将存储在BookCache中的数据载入书架<br>
						 * 中(即使登录时从服务器down下来的数据中没有这本书籍，也要还原，因为epub书籍不予服务器同步)。<br>
						 */
						EpubHelper.i.beforCacheEpubBook2BookCache(delBook);
						db.insert(BookCacheTable.TABLE_NAME, null,
								setBookCacheValues(delBook));
					}
				}
				delBook.setId(-1);
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Save book.
	 * 
	 * @param book
	 *            the book
	 * @return true, if successful
	 */
	public synchronized static boolean saveBook(Book book)
	{
		LogUtil.d(TAG, "db saveBook");
		if (book == null || book.getTitle() == null) {
			// if (book == null || book.getTitle() == null ||
			// book.getDownloadInfo().getImageUrl() == null) {
			return false;
		}

		Cursor findCursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			LogUtil.i("CursorError", "DBService >> saveBook book={" + book + "}");

			if (book.getId() == -1) {
				LogUtil.i("CursorError", "DBService >> saveBook book={" + book + "}");
			}

			String sql = "select 1 from " + BookTable.TABLE_NAME + " where "
					+ BookTable.BOOK_ID + " = ?";
			findCursor = db.rawQuery(sql, new String[] { book.getBookId() });

			if (book.isVDiskBook() || book.isLocalImportBook()) {
				sql = "select 1 from " + BookTable.TABLE_NAME + " where "
						+ BookTable.BOOK_ID + " = ? and " + BookTable.TITLE
						+ " = ?";

				findCursor = db.rawQuery(sql, new String[] { book.getBookId(),
						book.getTitle() });
			}

			// 未找到相关数据，执行插入操作
			if ((book.getId() <= 0 && findCursor.getCount() <= 0)) {
				LogUtil.i("CursorError", "DBService >> insert 插入");
				db.insert(BookTable.TABLE_NAME, null, setBookValues(book));
				// 查询最后插入的一条数据的自增长ID值
				Cursor cursor = db.rawQuery("select LAST_INSERT_ROWID()", null);
				if (cursor != null && cursor.moveToFirst()) {
					book.setId(cursor.getInt(0));
				}
				if (cursor != null) {
					cursor.close();
				}
			} else {
				LogUtil.i("CursorError", "DBService >> update 更新");
				// 更新操作
				db.update(BookTable.TABLE_NAME, setBookValues(book),
						BookTable.ID + " = ?",
						new String[] { book.getId() + "" });
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			LogUtil.i("CursorError", "DBService >> saveBook >> e {" + e.toString() + "}");
			e.printStackTrace();
			return false;
		} finally {
			if (findCursor != null) {
				findCursor.close();
				findCursor = null;
			}
		}
		return true;
	}

	public synchronized static boolean saveBook(Book book, SQLiteDatabase db)
	{
		LogUtil.d(TAG, "db saveBook 2");
		if (book == null || book.getTitle() == null) {
			return false;
		}

		if (db == null) {
			return false;
		}

		Cursor findCursor = null;
		try {
			String sql = "select 1 from " + BookTable.TABLE_NAME + " where "
					+ BookTable.BOOK_ID + " = ?";
			findCursor = db.rawQuery(sql, new String[] { book.getBookId() });

			if (book.isVDiskBook() || book.isLocalImportBook()) {
				sql = "select 1 from " + BookTable.TABLE_NAME + " where "
						+ BookTable.BOOK_ID + " = ? and " + BookTable.TITLE
						+ " = ?";

				findCursor = db.rawQuery(sql, new String[] { book.getBookId(),
						book.getTitle() });
			}

			db.insert(BookTable.TABLE_NAME, null, setBookValues(book));
			// db.update(BookTable.TABLE_NAME, setBookValues(book), BookTable.ID
			// + " = ?", new String[] { book.getId()
			// + "" });
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (findCursor != null) {
				findCursor.close();
				findCursor = null;
			}
		}
		return true;
	}

	/**
	 * Update book info
	 * 
	 * @param book
	 *            the book
	 * @return true, if successful
	 */
	public synchronized static boolean updateBook(Book book)
	{
		LogUtil.d(TAG, "db updateBook");

		if (book == null) {
			return false;
		}
		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			db.update(BookTable.TABLE_NAME, setBookValues(book), BookTable.ID
					+ " = ?", new String[] { book.getId() + "" });
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Update book info about read
	 * 
	 * @param book
	 *            the book
	 * @return true, if successful
	 */
	public synchronized static boolean updateBookReadInfo(Book book)
	{
		LogUtil.d(TAG, "db updateBookReadInfo");

		if (book == null) {
			return false;
		}
		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			ContentValues values = new ContentValues();

			values.put(BookTable.NUM, book.getNum());
			values.put(BookTable.LAST_POS, book.getReadInfo().getLastPos());
			values.put(BookTable.UPDATE_CHAPTER_NUM,
					book.getUpdateChaptersNum());
			values.put(BookTable.LAST_READ_TIME, book.getReadInfo()
					.getLastReadTime());
			values.put(BookTable.LAST_READ_PERCENT, book.getReadInfo()
					.getLastReadPercent());
			values.put(BookTable.LAST_FONTSIZE, book.getBookPage()
					.getFontSize());
			values.put(BookTable.TOTAL_PAGE, book.getBookPage().getTotalPage());
			values.put(BookTable.LAST_PAGE, book.getBookPage().getCurPage());
			values.put(BookTable.AUTO_BUY, book.getBuyInfo().isAutoBuy() ? 1
					: 0);
			values.put(BookTable.ONLINE_READ_CHAPTER_ID,
					book.getOnlineReadChapterId("DBService-updateBookReadInfo"));
			values.put(BookTable.LAST_READ_JSON, book.getReadInfo()
					.getLastReadJsonString());
			db.update(BookTable.TABLE_NAME, values, BookTable.ID + " = ?",
					new String[] { book.getId() + "" });
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Update book remind info
	 * 
	 * @param books
	 *            the book list
	 * @return true, if successful
	 */
	public static boolean updateBookRemind(List<Book> books)
	{
		LogUtil.d(TAG, "db updateBookReadInfo");

		if (books == null || books.size() <= 0) {
			return false;
		}
		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			for (Book book : books) {
				ContentValues values = new ContentValues();

				values.put(BookTable.IS_REMIND, book.isRemind() ? 1 : 0);

				db.update(BookTable.TABLE_NAME, values, BookTable.ID + " = ?",
						new String[] { book.getId() + "" });
			}

		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 删除书籍的所有章节信息
	 * 
	 * @param book
	 * @return
	 */
	public synchronized static boolean deleteChapter(Book book)
	{
		LogUtil.d(TAG, "db deleteChapter");
		if (book == null) {
			return false;
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			db.beginTransaction();
			if (book.getId() > 0) {
				int result = db.delete(ChapterTable.TABLE_NAME,
						ChapterTable.BOOK_ID + " = ?",
						new String[] { book.getId() + "" });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}

		return true;
	}

	/**
	 * Delete book.
	 * 
	 * @param book
	 *            the book
	 * @return true, if successful
	 */
	public synchronized static boolean deleteBook(Book book)
	{
		LogUtil.d(TAG, "db deleteBook--1--bookid: "+ book.getBookId());
		LogUtil.d(TAG, "db deleteBook--2----id: "+ book.getId());
		
		if (book == null || book.getTitle() == null
				|| book.getDownloadInfo().getImageUrl() == null) {
			return false;
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			db.beginTransaction();
			
			if (book.getId() <= 0) {
				// 此处删除存在重大隐患，如果id值<=0, 下面sql语句有可能将书架的数据全给清空掉，缩小修改
				String sql = BookTable.BOOK_ID + " = ?";
//				String sql = BookTable.BOOK_ID + " = ? or " + BookTable.SINA_ID
//						+ " = ? or " + BookTable.IMAGE_URL + " = ? or "
//						+ BookTable.TITLE + " = ?";
				db.delete(
						BookTable.TABLE_NAME,
						sql,
						new String[] { book.getBookId(), book.getSid(),
								book.getDownloadInfo().getImageUrl(),
								book.getTitle() });
			} else {
				db.delete(BookTable.TABLE_NAME, BookTable.ID + " = ?",
						new String[] { book.getId() + "" });

				db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_SID
						+ " = ?", new String[] { book.getBookId() });
				db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID
						+ " = ?", new String[] { book.getId() + "" });

				db.delete(ChapterTable.TABLE_NAME, ChapterTable.BOOK_ID
						+ " = ?", new String[] { book.getId() + "" });

				db.delete(BookSummaryTable.TABLE_NAME,
						BookSummaryTable.SUMMARY_BID + " = ?",
						new String[] { book.getBookId() });
				db.delete(BookSummaryTable.TABLE_NAME, BookSummaryTable.BOOK_ID
						+ " = ?", new String[] { book.getId() + "" });
				book.setId(-1);
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
		return true;
	}

	public synchronized static boolean clearBookRelateInfo(Book book)
	{
		LogUtil.d(TAG, "db clearBookRelateInfo");
		
		// if (book.getId() < 0) {
		// return true;
		// }

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			db.beginTransaction();

			db.delete(ChapterTable.TABLE_NAME, ChapterTable.BOOK_ID + " = ?",
					new String[] { book.getId() + "" });

			db.delete(BookMarkTable.TABLE_NAME,
					BookMarkTable.BOOK_SID + " = ?",
					new String[] { book.getBookId() });
			db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID + " = ?",
					new String[] { book.getId() + "" });

			db.delete(BookSummaryTable.TABLE_NAME, BookSummaryTable.SUMMARY_BID
					+ " = ?", new String[] { book.getBookId() });
			db.delete(BookSummaryTable.TABLE_NAME, BookSummaryTable.BOOK_ID
					+ " = ?", new String[] { book.getId() + "" });
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
		return true;
	}

	public synchronized static void saveBookRelateInfo(Book book)
	{
		LogUtil.d(TAG, "db saveBookRelateInfo");
		if (null != book && book.getId() > 0) {
			SQLiteDatabase db = null;
			try {
				db = sDbOpenHelper.getWritableDatabase();
				if (db == null || !db.isOpen()) {
					return;
				}

				db.beginTransaction();
				List<MarkItem> marks = book.getBookmarks();
				if (!marks.isEmpty() && marks.size() > 0) {
					db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_SID
							+ " = ?", new String[] { book.getBookId() });
					db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID
							+ " = ?", new String[] { book.getId() + "" });

					for (MarkItem mark : marks) {
						db.insert(BookMarkTable.TABLE_NAME, null,
								setBookMarkValues(mark));

						Cursor cursor = db.rawQuery(
								"select LAST_INSERT_ROWID()", null);
						if (cursor != null && cursor.moveToFirst()) {
							mark.setId(cursor.getInt(0));
						}

						if (cursor != null) {
							cursor.close();
						}
					}
				}

				List<BookSummary> summaries = book.getBookSummaries();
				if (!summaries.isEmpty() && summaries.size() > 0) {
					db.delete(BookSummaryTable.TABLE_NAME,
							BookSummaryTable.SUMMARY_BID + " = ?",
							new String[] { book.getBookId() });
					db.delete(BookSummaryTable.TABLE_NAME,
							BookSummaryTable.BOOK_ID + " = ?",
							new String[] { book.getId() + "" });

					for (BookSummary summary : summaries) {
						db.insert(BookSummaryTable.TABLE_NAME, null,
								setBookSummaryValues(summary));

						Cursor cursor = db.rawQuery(
								"select LAST_INSERT_ROWID()", null);
						if (cursor != null && cursor.moveToFirst()) {
							summary.setId(cursor.getInt(0));
						}

						if (cursor != null) {
							cursor.close();
						}
					}
				}
				db.setTransactionSuccessful();
			} catch (Exception e) {
				LogUtil.d(TAG, e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					if (db != null) {
						db.endTransaction();
					}
				} catch (Exception e) {
				}
			}
		}
		LogUtil.d(TAG, "db saveBookRelateInfo end");
	}

	/*********************************** Book相关Method结束 ************************************/

	/*********************************** BookMark相关Method开始 ************************************/
	/**
	 * Gets the all book mark.
	 * 
	 * @param book
	 *            the book
	 * @return the all book mark
	 */
	public synchronized static List<MarkItem> getAllBookMark(Book book)
	{
		LogUtil.d(TAG, "db getAllBookMark");
		
		if (book == null) {
			return new ArrayList<MarkItem>();
		}
		List<MarkItem> marklist = new LinkedList<MarkItem>();

		Cursor cursor = null;
		Cursor cursor2 = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			if (db == null) {
				return marklist;
			}

			// 优先使用书架上的ID
			boolean shelfHasThisBook = false;
			Book shelfBook = DownBookManager.getInstance().getBook(book);
			if (shelfBook == null) {
				shelfBook = book;
			} else {
				shelfHasThisBook = true;
			}

			String sql2 = "select * from " + BookMarkTable.TABLE_NAME
					+ " where " + BookMarkTable.BOOK_SID + " = ? order by "
					+ BookMarkTable.ID + " desc";
			// String sql2 = "select * from " + BookMarkTable.TABLE_NAME +
			// " where " + BookMarkTable.BOOK_SID
			// + " = ? order by " + BookMarkTable.DATE + ", " +
			// BookMarkTable.TIME + ", " + BookMarkTable.ID
			// + " desc";
			String bid = shelfBook.getBookId();
			if (bid == null || bid.length() == 0) {
				bid = shelfBook.getSid();
			}

			if (TextUtils.isEmpty(bid)) {
				// 本地导入书籍
				bid = "local_" + String.valueOf(shelfBook.getId());
			}

			cursor2 = db.rawQuery(sql2, new String[] { bid });
			if (cursor2 != null && cursor2.moveToFirst()) {
				do {
					MarkItem item = new MarkItem();
					readBookMarkFromCursor(cursor2, item);
					//  CJL BugID=21492
					if (shelfHasThisBook) {
						// item.parsePosFromJson(shelfBook);
						item.parsePosFromJson(shelfBook, false);
					}
					marklist.add(item);
				} while (cursor2.moveToNext());
			}

			int dbid = shelfBook.getId();
			if (dbid > 0) {
				String sql = "select * from " + BookMarkTable.TABLE_NAME
						+ " where " + BookMarkTable.BOOK_ID + " = ? order by "
						+ BookMarkTable.DATE + ", " + BookMarkTable.TIME + ", "
						+ BookMarkTable.ID + " desc";
				cursor = db
						.rawQuery(sql, new String[] { String.valueOf(dbid) });
				if (cursor != null && cursor.moveToFirst()) {
					// ArrayList<Chapter> list =
					// DBService.getAllChapter(shelfBook);
					do {
						MarkItem item = new MarkItem();
						readBookMarkFromCursor(cursor, item);
						// CJL BugID=21492
						if (shelfHasThisBook) {
							// item.parsePosFromJson(shelfBook);
							item.parsePosFromJson(shelfBook, false);
						}

						marklist.add(item);
					} while (cursor.moveToNext());
				}
			}

		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			if (cursor2 != null) {
				cursor2.close();
				cursor2 = null;
			}
		}

		return marklist;
	}

	/**
	 * Adds the book mark.
	 * 
	 * @param mark
	 *            the mark
	 */
	public synchronized static void addBookMark(MarkItem mark)
	{
		LogUtil.d(TAG, "db addBookMark");
		if (mark == null) {
			return;
		}

		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.insert(BookMarkTable.TABLE_NAME, null, setBookMarkValues(mark));

			Cursor cursor = db.rawQuery("select LAST_INSERT_ROWID()", null);
			if (cursor != null && cursor.moveToFirst()) {
				mark.setId(cursor.getInt(0));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * 保存Book的所有书签
	 * 
	 * @param book
	 */
	public synchronized static void addAllBookMark(Book book)
	{
		LogUtil.d("db addAllBookMark");
		if (null != book) {
			List<MarkItem> marks = book.getBookmarks();
			if (!marks.isEmpty() && marks.size() > 0) {

				SQLiteDatabase db = null;
				try {
					db = sDbOpenHelper.getWritableDatabase();
					if (db == null) {
						return;
					}

					db.beginTransaction();
					for (MarkItem mark : marks) {
						db.insert(BookMarkTable.TABLE_NAME, null,
								setBookMarkValues(mark));

						Cursor cursor = db.rawQuery(
								"select LAST_INSERT_ROWID()", null);
						if (cursor != null && cursor.moveToFirst()) {
							mark.setId(cursor.getInt(0));
						}

						if (cursor != null) {
							cursor.close();
						}
					}

					db.setTransactionSuccessful();
				} catch (Exception e) {
					LogUtil.d(TAG, e.getMessage());
					e.printStackTrace();
				} finally {
					try {
						if (db != null) {
							db.endTransaction();
						}
					} catch (Exception e) {
					}
				}
			}
		}
	}

	/**
	 * Delete book mark.
	 * 
	 * @param mark
	 *            the mark
	 * @return true, if successful
	 */
	public synchronized static boolean deleteBookMark(MarkItem mark)
	{
		LogUtil.d("db deleteBookMark");
		if (mark == null) {
			return false;
		}

		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			String sql = BookMarkTable.BOOK_ID + " = ? and "
					+ BookMarkTable.BEGIN + " = ?";
			db.delete(
					BookMarkTable.TABLE_NAME,
					sql,
					new String[] { mark.getBookId() + "", mark.getBegin() + "" });

			sql = BookMarkTable.BOOK_SID + " = ? and " + BookMarkTable.BEGIN
					+ " = ?";
			db.delete(BookMarkTable.TABLE_NAME, sql,
					new String[] { mark.getBookIdStr() + "",
							mark.getBegin() + "" });
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Delete all book mark.
	 * 
	 * @param book
	 *            the book
	 * @return true, if successful
	 */
	public synchronized static boolean deleteAllBookMark(Book book)
	{
		LogUtil.d("db deleteAllBookMark");
		if (book == null) {
			return false;
		}

		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}
			String sql = BookMarkTable.BOOK_ID + " = ?";
			db.delete(BookMarkTable.TABLE_NAME, sql,
					new String[] { book.getId() + "" });

			sql = BookMarkTable.BOOK_SID + " = ?";
			db.delete(BookMarkTable.TABLE_NAME, sql,
					new String[] { book.getBookId() });
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*********************************** BookMark相关Method开始 ************************************/

	/*********************************** Chapter相关Method开始 ************************************/
	/**
	 * Update the chapter.
	 * 
	 * @param book
	 *            the book
	 * @param chapter
	 *            the chapter
	 */
	public static void updateChapter(Book book, Chapter chapter)
	{
		LogUtil.d(TAG, "db updateChapter");
		if (book == null || chapter == null) {
			return;
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.beginTransaction();

			String sql = ChapterTable.BOOK_ID + " = ? and "
					+ ChapterTable.CHAPTER_ID + " = ?";
			db.update(ChapterTable.TABLE_NAME, setChapterValues(book, chapter),
					sql,
					new String[] { book.getId() + "",
							chapter.getChapterId() + "" });

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	public static void insertChapter(Book book, Chapter chapter)
	{
		LogUtil.d(TAG, "db updateChapter");
		if (book == null || chapter == null) {
			return;
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.beginTransaction();
			// String sql = ChapterTable.BOOK_ID + " = ? and " +
			// ChapterTable.CHAPTER_ID + " = ?";
			db.insert(ChapterTable.TABLE_NAME, null,
					setChapterValues(book, chapter));

			// db.update(ChapterTable.TABLE_NAME, setChapterValues(book,
			// chapter), sql, new String[] { book.getId() + "",
			// chapter.getChapterId() + "" });

			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	public synchronized static void updateAllChapter(Book book,
			List<Chapter> chapters)
	{
		LogUtil.d(TAG, "db updateAllChapter 1");
		updateAllChapter(book, chapters, null, "8888");
	}

	/**
	 * Update all chapter.
	 * 
	 * @param book
	 *            the book
	 * @param chapters
	 *            the chapters
	 */
	public synchronized static void updateAllChapter(Book book,
			List<Chapter> chapters, ICancelable task, String debugTag)
	{
		LogUtil.d(TAG, "db updateAllChapter 2");
		if (book == null || chapters == null) {
			return;
		}

		if (book.getId() < 0) {
			Book dbook = DBService.getBook(book);
			if (dbook != null && dbook.getId() > 0) {
				book.setId(dbook.getId());
			}
		}

		if (book.getId() < 0) {
			return;
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			// 1 计算出需要删除章节的最小id，减少数据库删除操作
			int minChapterId = 9999999;
			for (Chapter chapter : chapters) {
				if (chapter.getChapterId() < minChapterId) {
					minChapterId = chapter.getChapterId();
				}
			}

			// 2 执行数据库更新
			db.beginTransaction();
			// 删除与即将插入章节重复的章节
			String sql = ChapterTable.BOOK_ID + " = ? and "
					+ ChapterTable.CHAPTER_ID + " >= ?";
			db.delete(
					ChapterTable.TABLE_NAME,
					sql,
					new String[] { String.valueOf(book.getId()),
							String.valueOf(minChapterId) });

			for (Chapter chapter : chapters) {
				if (task != null && task.isCancelled()) {
					// LogUtil.i("SinaXmlHandler",
					// "DBService >> updateAllChapter >> task is cancelled.");
					// LogUtil.e("BugID=21413",
					// "DBService >> updateAllChapter >> task is cancelled.");
					return;
				}
				// LogUtil.e("BugID=21413",
				// "DBService >> updateAllChapter >> chapter=" + chapter);
				db.insert(ChapterTable.TABLE_NAME, null,
						setChapterValues(book, chapter));

				Cursor cursor = db.rawQuery("select LAST_INSERT_ROWID()", null);
				if (cursor != null && cursor.moveToFirst()) {
					chapter.setId(cursor.getInt(0));
				}
				if (cursor != null) {
					cursor.close();
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
		LogUtil.d(TAG, "db updateAllChapter end");
		// DebugHelper.end();
		return;
	}

	/**
	 * Update new chapter. 数据库是阻塞的，更新时应该做尽量少的更新操作
	 * 
	 * @param book
	 *            the book
	 * @param newChapters
	 *            the chapters
	 */
	public synchronized static void updateNewChapter(Book book,
			List<Chapter> newChapters)
	{
		LogUtil.d(TAG, "db updateNewChapter");
		if (book == null || newChapters == null) {
			return;
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			String sql = ChapterTable.BOOK_ID + " = ? and "
					+ ChapterTable.CHAPTER_ID + " = ?";

			db.beginTransaction();

			// 首先将该书籍章节tag设置为默认值
			ContentValues values = new ContentValues();
			values.put(ChapterTable.TAG, Chapter.NORMAL);
			db.update(ChapterTable.TABLE_NAME, values, ChapterTable.BOOK_ID
					+ " = ?", new String[] { book.getId() + "" });

			for (Chapter chapter : newChapters) {
				// 如果以前有存有该章节，则先删除该章节，再插入，防止重复插入
				db.delete(
						ChapterTable.TABLE_NAME,
						sql,
						new String[] { book.getId() + "",
								chapter.getChapterId() + "" });

				db.insert(ChapterTable.TABLE_NAME, null,
						setChapterValues(book, chapter));

				Cursor cursor = db.rawQuery("select LAST_INSERT_ROWID()", null);
				if (cursor != null && cursor.moveToFirst()) {
					chapter.setId(cursor.getInt(0));
				}
				if (cursor != null) {
					cursor.close();
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 更新VIP书籍的章节列表<br>
	 * 只是在章节免费的书籍时会使用<br>
	 * 所以传入下载的task，当task取消时，取消本次更新操作<br>
	 * 
	 * @param book
	 * @param chapters
	 */
	public synchronized static void updateChapterVipBookChapters(
			ICancelable task, Book book, List<Chapter> chapters)
	{
		LogUtil.d(TAG, "db updateChapterVipBookChapters");
		// DebugHelper.start("updateChapterVipBookChapters");
		if (book == null || chapters == null) {
			return;
		}
		// 1 取出该书已拥有的章节列表
		ArrayList<Chapter> dbChapters = getAllChapter(book);
		if (dbChapters == null) {
			dbChapters = new ArrayList<Chapter>();
		}

		// 2 通过新下载的章节做补全
		for (Chapter netChapter : chapters) {

			// 数据库chapter中找到了更新startPos和length
			for (Chapter dbChapter : dbChapters) {
				if (dbChapter.getGlobalId() == netChapter.getGlobalId()) {
					netChapter.setStartPos(dbChapter.getStartPos());
					netChapter.setLength(dbChapter.getLength());
					break;
				}
			}
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}
			db.beginTransaction();

			// 删除当前书本所有的chapter
			String sql = ChapterTable.BOOK_ID + " = ?";
			db.delete(ChapterTable.TABLE_NAME, sql, new String[] { book.getId()
					+ "" });

			// 重新插入
			for (Chapter chapter : chapters) {
				if (task != null && task.isCancelled()) {
					// 直接返回，回滚该次数据库操作事务
					return;
				}
				db.insert(ChapterTable.TABLE_NAME, null,
						setChapterValues(book, chapter));
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
		// DebugHelper.end();
	}

	/**
	 * Gets the all chapter.
	 * 
	 * @param book
	 *            the book
	 * @return the all chapter
	 */
	public synchronized static ArrayList<Chapter> getAllChapter(Book book)
	{
		LogUtil.d(TAG, "db getAllChapter");
		if (book == null) {
			return new ArrayList<Chapter>();
		}
		ArrayList<Chapter> chapters = new ArrayList<Chapter>();

		// 优先使用书架上的ID
		Book shelfBook = DownBookManager.getInstance().getBook(book);
		if (shelfBook == null) {
			shelfBook = book;
		}

		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			String sql = "select * from " + ChapterTable.TABLE_NAME + " where "
					+ ChapterTable.BOOK_ID + " = ? order by "
					+ ChapterTable.CHAPTER_ID;
			if (db == null) {
				return chapters;
			}

			cursor = db.rawQuery(sql,
					new String[] { String.valueOf(shelfBook.getId()) });
			// TODO:
			if (cursor != null && cursor.moveToFirst()) {
				do {
					Chapter chapter = new Chapter();
					readChapterFromCursor(cursor, chapter);
					chapters.add(chapter);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return chapters;
	}

	/**
	 * Gets the chapter .
	 * 
	 * @param bookId
	 *            书在数据库的Id，不是书城给的Id
	 * 
	 * @param globalChapterId
	 *            the global chapter id
	 * @return the chapter by global chapter id
	 */
	public synchronized static Chapter getChapter(int bookId,
			int globalChapterId)
	{
		LogUtil.d(TAG, "db getChapterByGlobalChapterId");
		Chapter chapter = new Chapter();

		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			String sql = "select * from " + ChapterTable.TABLE_NAME + " where "
					+ ChapterTable.BOOK_ID + " = ? and "
					+ ChapterTable.GLOBAL_CHAPTER_ID + " = ?";
			if (db == null) {
				return chapter;
			}

			cursor = db.rawQuery(sql, new String[] { String.valueOf(bookId),
					String.valueOf(globalChapterId) });
			if (cursor != null && cursor.moveToLast()) {
				readChapterFromCursor(cursor, chapter);
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return chapter;
	}

	/**
	 * Gets the last chapter.
	 * 
	 * @param book
	 *            the book
	 * @return the last chapter
	 */
	public synchronized static Chapter getLastChapter(Book book)
	{
		LogUtil.d(TAG, "db getLastChapter");
		Chapter lastChapter = new Chapter();
		if (book == null || book.getId() < 0) {
			return lastChapter;
		}
		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			String sql = "select * from " + ChapterTable.TABLE_NAME + " where "
					+ ChapterTable.BOOK_ID + " = ? and " + ChapterTable.LENGTH
					+ " > 0 order by " + ChapterTable.CHAPTER_ID
					+ " desc limit 1";
			if (db == null) {
				return lastChapter;
			}

			cursor = db.rawQuery(sql,
					new String[] { String.valueOf(book.getId()) });

			if (cursor != null && cursor.moveToFirst()) {
				// 循环查找最后一个有内容的章节
				Chapter chapter = new Chapter();
				readChapterFromCursor(cursor, chapter);
				lastChapter = chapter;
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		LogUtil.d(TAG, "db getLastChapter end");
		LogUtil.d(
				"FileMissingLog",
				"db getLastChapter end and lastChapter Id is "
						+ lastChapter.getChapterId());
		return lastChapter;
	}

	/*********************************** Chapter相关Method结束 ************************************/

	/*********************************** BookSummary相关Method开始 ************************************/
	/**
	 * Gets the all book summary.
	 * 
	 * @param book
	 *            the book
	 * @return the all book summary
	 */
	public synchronized static List<BookSummary> getAllBookSummary(Book book)
	{
		LogUtil.d(TAG, "db getAllBookSummary");
		if (book == null) {
			return new ArrayList<BookSummary>();
		}
		List<BookSummary> summaries = new LinkedList<BookSummary>();

		Cursor cursor = null;
		Cursor cursor2 = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			if (db == null) {
				return summaries;
			}

			int dbid = book.getId();
			if (dbid > 0) {
				String sql = "select * from " + BookSummaryTable.TABLE_NAME
						+ " where " + BookSummaryTable.BOOK_ID
						+ " = ? order by " + BookSummaryTable.DATE + ", "
						+ BookSummaryTable.TIME + ", " + BookSummaryTable.ID
						+ " desc";
				cursor = db.rawQuery(sql,
						new String[] { String.valueOf(book.getId()) });
				if (cursor != null && cursor.moveToFirst()) {
					do {
						BookSummary item = new BookSummary();
						readBookSummaryFromCursor(cursor, item);
						summaries.add(item);
					} while (cursor.moveToNext());
				}
			}

			String sql2 = "select * from " + BookSummaryTable.TABLE_NAME
					+ " where " + BookSummaryTable.SUMMARY_BID
					+ " = ? order by " + BookSummaryTable.ID + " desc";
			// String sql2 = "select * from " + BookSummaryTable.TABLE_NAME +
			// " where " + BookSummaryTable.SUMMARY_BID
			// + " = ? order by " + BookSummaryTable.DATE + ", " +
			// BookSummaryTable.TIME + ", "
			// + BookSummaryTable.ID + " desc";

			String bid = book.getBookId();
			if (bid == null || bid.length() == 0) {
				bid = book.getSid();
			}

			if (TextUtils.isEmpty(bid)) {
				bid = "local_" + book.getId();
			}

			cursor2 = db.rawQuery(sql2, new String[] { bid });
			if (cursor2 != null && cursor2.moveToFirst()) {
				do {
					BookSummary item = new BookSummary();
					readBookSummaryFromCursor(cursor2, item);

					summaries.add(item);
				} while (cursor2.moveToNext());
			}

		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			if (cursor2 != null) {
				cursor2.close();
				cursor2 = null;
			}
		}
		return summaries;
	}

	/**
	 * Adds the book summary.
	 * 
	 * @param summary
	 *            the summary
	 */
	public synchronized static void addBookSummary(BookSummary summary)
	{
		LogUtil.d(TAG, "db addBookSummary");
		if (summary == null) {
			return;
		}

		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.insert(BookSummaryTable.TABLE_NAME, null,
					setBookSummaryValues(summary));

			Cursor cursor = db.rawQuery("select LAST_INSERT_ROWID()", null);
			if (cursor != null && cursor.moveToFirst()) {
				summary.setId(cursor.getInt(0));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		}
	}

	public synchronized static void addBookSummary(BookSummary summaryAdd,
			List<BookSummary> relatesDel)
	{
		LogUtil.d(TAG, "db addBookSummary2");
		if (summaryAdd == null) {
			return;
		}

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.beginTransaction();
			// 删除相关书摘
			if (relatesDel != null && relatesDel.size() > 0) {
				String sql = BookSummaryTable.BOOK_ID + " = ? and "
						+ BookSummaryTable.OFFSET + " = ?";
				for (BookSummary summaryDel : relatesDel) {
					db.delete(BookSummaryTable.TABLE_NAME, sql, new String[] {
							summaryDel.getBookId() + "",
							summaryDel.getOffset() + "" });
				}

				String sql2 = BookSummaryTable.SUMMARY_BID + " = ? and "
						+ BookSummaryTable.OFFSET + " = ?";
				for (BookSummary summaryDel : relatesDel) {
					db.delete(BookSummaryTable.TABLE_NAME, sql2, new String[] {
							summaryDel.getBookIdStr(),
							summaryDel.getOffset() + "" });
				}
			}

			// 插入新的书摘
			db.insert(BookSummaryTable.TABLE_NAME, null,
					setBookSummaryValues(summaryAdd));

			Cursor cursor = db.rawQuery("select LAST_INSERT_ROWID()", null);
			if (cursor != null && cursor.moveToFirst()) {
				summaryAdd.setId(cursor.getInt(0));
			}
			if (cursor != null) {
				cursor.close();
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 保存Book的所有书摘
	 * 
	 * @param book
	 */
	public synchronized static void addAllBookSummary(Book book)
	{
		LogUtil.d("db addAllBookSummary");
		if (null != book) {
			List<BookSummary> summaries = book.getBookSummaries();
			if (!summaries.isEmpty() && summaries.size() > 0) {
				SQLiteDatabase db = null;
				try {
					db = sDbOpenHelper.getWritableDatabase();
					if (db == null) {
						return;
					}

					db.beginTransaction();
					for (BookSummary summary : summaries) {
						db.insert(BookSummaryTable.TABLE_NAME, null,
								setBookSummaryValues(summary));

						Cursor cursor = db.rawQuery(
								"select LAST_INSERT_ROWID()", null);
						if (cursor != null && cursor.moveToFirst()) {
							summary.setId(cursor.getInt(0));
						}

						if (cursor != null) {
							cursor.close();
						}
					}

					db.setTransactionSuccessful();
				} catch (Exception e) {
					LogUtil.d(TAG, e.getMessage());
					e.printStackTrace();
				} finally {
					try {
						if (db != null) {
							db.endTransaction();
						}
					} catch (Exception e) {
					}
				}
			}
		}
	}

	/**
	 * Delete book summary.
	 * 
	 * @param summary
	 *            the summary
	 * @return true, if successful
	 */
	public synchronized static boolean deleteBookSummary(BookSummary summary)
	{
		LogUtil.d("db deleteBookSummary");
		if (summary == null) {
			return false;
		}

		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			String sql = BookSummaryTable.SUMMARY_BID + " = ? and "
					+ BookSummaryTable.OFFSET + " = ?";
			db.delete(BookSummaryTable.TABLE_NAME, sql,
					new String[] { summary.getBookIdStr(),
							summary.getOffset() + "" });

			sql = BookSummaryTable.BOOK_ID + " = ? and "
					+ BookSummaryTable.OFFSET + " = ?";
			db.delete(
					BookSummaryTable.TABLE_NAME,
					sql,
					new String[] { summary.getBookId() + "",
							summary.getOffset() + "" });

		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Delete all book summary.
	 * 
	 * @param book
	 *            the book
	 * @return true, if successful
	 */
	public synchronized static boolean deleteAllBookSummary(Book book)
	{
		LogUtil.d("db deleteAllBookSummary");
		if (book == null) {
			return false;
		}

		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}

			String sql = BookSummaryTable.SUMMARY_BID + " = ?";
			db.delete(BookSummaryTable.TABLE_NAME, sql,
					new String[] { book.getBookId() });

			sql = BookSummaryTable.BOOK_ID + " = ?";
			db.delete(BookSummaryTable.TABLE_NAME, sql,
					new String[] { book.getId() + "" });
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public synchronized static DataCacheBean getDataCache(String url)
	{
		LogUtil.d(TAG, "db getDataCache");
		if (Utils.isEmptyString(url)) {
			return null;
		}

		DataCacheBean bean = null;
		String sql = null;

		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			if (db == null) {
				return null;
			}

			String key = DataCacheBean.generateKey(url);
			sql = DataCacheTable.CACHE_KEY + " = ?";
			cursor = db.query(DataCacheTable.TABLE_NAME,
					DataCacheTable.COLUMNS, sql, new String[] { key }, null,
					null, null);

			if (cursor != null && cursor.moveToFirst()) {
				bean = new DataCacheBean();
				readDataCacheFromCursor(cursor, bean);
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bean;
	}

	public synchronized static boolean setDataCache(String url, String data,
			long invalidTime)
	{
		LogUtil.d(TAG, "db setDataCache");
		if (url == null || data == null) {
			return false;
		}
		DataCacheBean bean = new DataCacheBean(url, data, invalidTime);

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}
			String sql = DataCacheTable.CACHE_KEY + " = ?";

			db.beginTransaction();
			db.delete(DataCacheTable.TABLE_NAME, sql,
					new String[] { bean.getKey() });
			db.insert(DataCacheTable.TABLE_NAME, null, setDataCacheValues(bean));
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
		return true;
	}

	public synchronized static boolean clearAllDataCache()
	{
		LogUtil.d(TAG, "db clearAllDataCache");

		try {
			SQLiteDatabase db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return false;
			}
			String sql = DataCacheTable.TIME + " > 0";

			db.delete(DataCacheTable.TABLE_NAME, sql, null);
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public synchronized static ArrayList<Book> getAllBookCache(String ownerUid)
	{
		LogUtil.d(TAG, "db getAllBookCache ownerUid=" + ownerUid);
		ArrayList<Book> books = new ArrayList<Book>();

		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			String sql = "select * from " + BookCacheTable.TABLE_NAME
					+ " where " + BookCacheTable.OWNER_UID + " = ?";
			if (db == null) {
				return books;
			}
			cursor = db.rawQuery(sql, new String[] { ownerUid });
			if (cursor != null && cursor.moveToFirst()) {
				LogUtil.d("ReadInfoLeft", "db getAllBookCache ownerUid="
						+ ownerUid + ", BookCacheTable存在缓存数据");
				do {
					Book book = new Book();
					readBookCacheFromCursor(cursor, book);
					books.add(book);
				} while (cursor.moveToNext());
			} else {
				LogUtil.w("ReadInfoLeft", "db getAllBookCache ownerUid="
						+ ownerUid + ", BookCacheTable 不存在缓存数据");
			}
		} catch (Exception e) {
			LogUtil.e("ReadInfoLeft", "db getAllBookCache ownerUid=" + ownerUid
					+ ", 异常了");
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return books;
	}

	public synchronized static ArrayList<Book> getAllBookCache()
	{
		LogUtil.d(TAG, "db getAllBookCache");
		ArrayList<Book> books = new ArrayList<Book>();

		Cursor cursor = null;
		try {
			SQLiteDatabase db = sDbOpenHelper.getReadableDatabase();
			String sql = "select * from " + BookCacheTable.TABLE_NAME;
			if (db == null) {
				return books;
			}
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					Book book = new Book();
					readBookCacheFromCursor(cursor, book);
					books.add(book);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return books;
	}

	public synchronized static void delBookCaches(List<Book> delBooks)
	{
		LogUtil.d(TAG, "db delBookCaches");

		SQLiteDatabase db = null;
		try {
			db = sDbOpenHelper.getWritableDatabase();
			if (db == null) {
				return;
			}

			db.beginTransaction();
			for (Book delBook : delBooks) {
				LogUtil.e("ReadInfoLeft",
						"BookCacheTable delete 删除 BookCacheTable(2) >> delBook{"
								+ delBook + "}");
				db.delete(BookCacheTable.TABLE_NAME, BookCacheTable.ORIGIN_ID
						+ " = ?", new String[] { delBook.getId() + "" });

				db.delete(BookMarkTable.TABLE_NAME, BookMarkTable.BOOK_ID
						+ " = ?", new String[] { delBook.getId() + "" });

				db.delete(ChapterTable.TABLE_NAME, ChapterTable.BOOK_ID
						+ " = ?", new String[] { delBook.getId() + "" });

				db.delete(BookSummaryTable.TABLE_NAME, BookSummaryTable.BOOK_ID
						+ " = ?", new String[] { delBook.getId() + "" });
				delBook.setId(-1);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			LogUtil.d(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (db != null) {
					db.endTransaction();
				}
			} catch (Exception e) {
			}
		}
	}

	/*********************************** 以下为private method ************************************/
	/**
	 * 将Book对象设置到ContentValues中
	 * 
	 * @param book
	 * @return
	 */
	private synchronized static ContentValues setBookValues(Book book)
	{
		ContentValues values = new ContentValues();

		values.put(BookTable.TITLE, book.getTitle());
		values.put(BookTable.AUTHOR, book.getAuthor());
		values.put(BookTable.NUM, book.getNum());
		values.put(BookTable.INTRO, book.getIntro());
		values.put(BookTable.IMAGE_URL, book.getDownloadInfo().getImageUrl());
		values.put(BookTable.FILE_PATH, book.getDownloadInfo().getFilePath());
		values.put(BookTable.FILE_SIZE, book.getDownloadInfo().getFileSize());
		values.put(BookTable.FLAG, book.getFlag());
		values.put(BookTable.PROGRESS, book.getDownloadInfo().getProgress());
		values.put(BookTable.DOWNLOAD_STATE, book.getDownloadInfo()
				.getDownLoadState());
		values.put(BookTable.LAST_POS, book.getReadInfo().getLastPos());
		values.put(BookTable.BOOK_ID, book.getBookId());
		values.put(BookTable.UPDATE_CHAPTER_NUM, book.getUpdateChaptersNum());
		values.put(BookTable.TAG, book.getTag());
		values.put(BookTable.PAY_TYPE, book.getBuyInfo().getPayType());
		values.put(BookTable.PRICE, book.getBuyInfo().getPrice());
		values.put(BookTable.LAST_READ_TIME, book.getReadInfo()
				.getLastReadTime());
		values.put(BookTable.ORIGINAL_FILE_PATH, book.getDownloadInfo()
				.getOriginalFilePath());
		values.put(BookTable.STATUS_INFO, book.getBuyInfo().getStatusInfo());
		values.put(BookTable.LAST_UPDATE_TIME, book.getLastUpdateTime());
		values.put(BookTable.LAST_READ_PERCENT, book.getReadInfo()
				.getLastReadPercent());
		values.put(BookTable.DOWNLOAD_TIME, book.getDownloadInfo()
				.getDownloadTime());
		values.put(BookTable.VDISK_DOWNLOAD_URL, book.getDownloadInfo()
				.getVDiskDownUrl());
		values.put(BookTable.VDISK_FILE_PATH, book.getDownloadInfo()
				.getVDiskFilePath());
		values.put(BookTable.UID, book.getBuyInfo().getUid());
		values.put(BookTable.SINA_ID, book.getSid());
		values.put(BookTable.BOOK_CONTENT_TYPE, book.getBookContentType());
		values.put(BookTable.SUITE_ID, book.getSuiteId());
		values.put(BookTable.ORIGIN_SUITE_ID, book.getOriginSuiteId());
		values.put(BookTable.SUITE_NAME, book.getSuiteName());
		values.put(BookTable.LAST_FONTSIZE, book.getBookPage().getFontSize());
		values.put(BookTable.TOTAL_PAGE, book.getBookPage().getTotalPage());
		values.put(BookTable.LAST_PAGE, book.getBookPage().getCurPage());
		values.put(BookTable.AUTO_BUY, book.getBuyInfo().isAutoBuy() ? 1 : 0);
		values.put(BookTable.IS_ONLINE_BOOK, book.isOnlineBook() ? 1 : 0);
		values.put(BookTable.OWNER_UID, book.getOwnerUid());
		values.put(BookTable.ONLINE_READ_CHAPTER_ID,
				book.getOnlineReadChapterId("DBService-setBookValues"));
		values.put(BookTable.IS_REMIND, book.isRemind() ? 1 : 0);
		values.put(BookTable.LAST_READ_JSON, book.getReadInfo()
				.getLastReadJsonString());

		return values;
	}

	/**
	 * 从Cursor中读取Book
	 * 
	 * @param cursor
	 * @param book
	 */
	public synchronized static void readBookFromCursor(Cursor cursor, Book book)
	{
		book.setId(cursor.getInt(cursor.getColumnIndex(BookTable.ID)));
		book.setTitle(cursor.getString(cursor.getColumnIndex(BookTable.TITLE)));
		book.setAuthor(cursor.getString(cursor.getColumnIndex(BookTable.AUTHOR)));
		book.setNum(cursor.getInt(cursor.getColumnIndex(BookTable.NUM)));
		book.setIntroRealNeed(cursor.getString(cursor
				.getColumnIndex(BookTable.INTRO)));
		book.getDownloadInfo().setImageUrl(
				cursor.getString(cursor.getColumnIndex(BookTable.IMAGE_URL)));
		book.getDownloadInfo().setFilePath(
				cursor.getString(cursor.getColumnIndex(BookTable.FILE_PATH)));
		book.getDownloadInfo().setFileSize(
				cursor.getFloat(cursor.getColumnIndex(BookTable.FILE_SIZE)));
		book.setFlag(cursor.getString(cursor.getColumnIndex(BookTable.FLAG)));
		book.getDownloadInfo().setProgress(
				cursor.getDouble(cursor.getColumnIndex(BookTable.PROGRESS)));
		book.getDownloadInfo().setDownLoadState(
				cursor.getInt(cursor.getColumnIndex(BookTable.DOWNLOAD_STATE)));
		book.getReadInfo().setLastPos(
				cursor.getInt(cursor.getColumnIndex(BookTable.LAST_POS)));
		book.setBookId(cursor.getString(cursor
				.getColumnIndex(BookTable.BOOK_ID)));
		
		// 更新章节数
		book.setUpdateChaptersNum(cursor.getInt(cursor
				.getColumnIndex(BookTable.UPDATE_CHAPTER_NUM)));
		
		book.setTag(cursor.getInt(cursor.getColumnIndex(BookTable.TAG)));
		book.getBuyInfo().setPayType(
				cursor.getInt(cursor.getColumnIndex(BookTable.PAY_TYPE)));
		book.getBuyInfo().setPrice(
				cursor.getDouble(cursor.getColumnIndex(BookTable.PRICE)));
		book.getReadInfo()
				.setLastReadTime(
						cursor.getLong(cursor
								.getColumnIndex(BookTable.LAST_READ_TIME)));
		book.getDownloadInfo().setOriginalFilePath(
				cursor.getString(cursor
						.getColumnIndex(BookTable.ORIGINAL_FILE_PATH)));
		book.getBuyInfo().setStatusInfo(
				cursor.getString(cursor.getColumnIndex(BookTable.STATUS_INFO)));
		book.setLastUpdateTime(cursor.getLong(cursor
				.getColumnIndex(BookTable.LAST_UPDATE_TIME)));
		book.getReadInfo().setLastReadPercent(
				cursor.getFloat(cursor
						.getColumnIndex(BookTable.LAST_READ_PERCENT)));
		book.getDownloadInfo().setDownloadTime(
				cursor.getLong(cursor.getColumnIndex(BookTable.DOWNLOAD_TIME)));
		
		// 借助diskurl参数判断是epub阅读还是html阅读
		String vDislUrl = cursor.getString(cursor.getColumnIndex(BookTable.VDISK_DOWNLOAD_URL));
		book.getDownloadInfo().setVDiskDownUrl(vDislUrl);
		if(vDislUrl != null){
			if(vDislUrl.equals(Book.HTML_READ_PROTOCOL)){
				book.setIsHtmlRead(true);
			}else if(vDislUrl.startsWith(Book.EPUB_PATH_PROTOCOL)){
				book.setIsEpubOnly(true);
			}
		}
		//TODO:ouyang 测试，注意还原
//		book.setIsHtmlRead(true);
//		book.getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
		
		book.getDownloadInfo().setVDiskFilePath(
				cursor.getString(cursor
						.getColumnIndex(BookTable.VDISK_FILE_PATH)));
		
		book.getBuyInfo().setUid(
				cursor.getString(cursor.getColumnIndex(BookTable.UID)));
		book.setSid(cursor.getString(cursor.getColumnIndex(BookTable.SINA_ID)));
		book.setBookContentType(cursor.getInt(cursor
				.getColumnIndex(BookTable.BOOK_CONTENT_TYPE)));
		book.setSuiteId(cursor.getInt(cursor.getColumnIndex(BookTable.SUITE_ID)));
		book.setOriginSuiteId(cursor.getInt(cursor
				.getColumnIndex(BookTable.ORIGIN_SUITE_ID)));
		book.setSuiteName(cursor.getString(cursor
				.getColumnIndex(BookTable.SUITE_NAME)));
		book.getBookPage().setFontSize(
				cursor.getInt(cursor.getColumnIndex(BookTable.LAST_FONTSIZE)));
		book.getBookPage().setTotalPage(
				cursor.getInt(cursor.getColumnIndex(BookTable.TOTAL_PAGE)));
		book.getBookPage().setCurPage(
				cursor.getInt(cursor.getColumnIndex(BookTable.LAST_PAGE)));
		book.getBuyInfo().setAutoBuy(
				1 == cursor.getInt(cursor.getColumnIndex(BookTable.AUTO_BUY)));
		book.setOnlineBook(1 == cursor.getInt(cursor
				.getColumnIndex(BookTable.IS_ONLINE_BOOK)));
		book.setOwnerUid(cursor.getString(cursor
				.getColumnIndex(BookTable.OWNER_UID)));
		book.setOnlineReadChapterId(cursor.getInt(cursor
				.getColumnIndex(BookTable.ONLINE_READ_CHAPTER_ID)),
				"DBService-readBookFromCursor");
		book.setRemind(1 == cursor.getInt(cursor
				.getColumnIndex(BookTable.IS_REMIND)));
		book.getReadInfo().setLastReadJsonString(
				cursor.getString(cursor
						.getColumnIndex(BookTable.LAST_READ_JSON)),
				"[DBService >> readBookFromCursor]");
	}

	/**
	 * 将Chapter对象设置到ContentValues中
	 * 
	 * @param book
	 * @param chapter
	 * @return
	 */
	private synchronized static ContentValues setChapterValues(Book book,
			Chapter chapter)
	{
		ContentValues values = new ContentValues();

		values.put(ChapterTable.BOOK_ID, book.getId());
		values.put(ChapterTable.CHAPTER_ID, chapter.getChapterId());
		values.put(ChapterTable.TITLE, chapter.getTitle());
		values.put(ChapterTable.START_POS, chapter.getStartPos());
		values.put(ChapterTable.LENGTH, chapter.getLength());
		values.put(ChapterTable.IS_VIP, chapter.getVip());
		values.put(ChapterTable.GLOBAL_CHAPTER_ID, chapter.getGlobalId());
		values.put(ChapterTable.TAG, chapter.getTag());
		values.put(ChapterTable.CHAPTER_FLAGS, chapter.getChapterFlags());
		values.put(ChapterTable.SERIAL_NUMBER, chapter.getSerialNumber());
		return values;
	}

	/**
	 * 从Cursor中读取Chapter
	 * 
	 * @param cursor
	 * @param chapter
	 */
	public synchronized static void readChapterFromCursor(Cursor cursor,
			Chapter chapter)
	{
		chapter.setId(cursor.getInt(cursor.getColumnIndex(ChapterTable.ID)));
		chapter.setChapterId(cursor.getInt(cursor
				.getColumnIndex(ChapterTable.CHAPTER_ID)));
		chapter.setTitle(cursor.getString(cursor
				.getColumnIndex(ChapterTable.TITLE)));
		chapter.setStartPos(cursor.getInt(cursor
				.getColumnIndex(ChapterTable.START_POS)));
		chapter.setLength(cursor.getInt(cursor
				.getColumnIndex(ChapterTable.LENGTH)));
		chapter.setVip(cursor.getString(cursor
				.getColumnIndex(ChapterTable.IS_VIP)));
		chapter.setGlobalId(cursor.getInt(cursor
				.getColumnIndex(ChapterTable.GLOBAL_CHAPTER_ID)));
		chapter.setTag(cursor.getInt(cursor.getColumnIndex(ChapterTable.TAG)));
		chapter.setChapterFlags(cursor.getInt(cursor
				.getColumnIndex(ChapterTable.CHAPTER_FLAGS)));
		chapter.setSerialNumber(cursor.getInt(cursor
				.getColumnIndex(ChapterTable.SERIAL_NUMBER)));
	}

	/**
	 * 将MarkItem对象设置到ContentValues中
	 * 
	 * @param mark
	 * @return
	 */
	private synchronized static ContentValues setBookMarkValues(MarkItem mark)
	{
		ContentValues values = new ContentValues();

		values.put(BookMarkTable.BOOK_ID, mark.getBookId());
		values.put(BookMarkTable.BEGIN, mark.getBegin());
		values.put(BookMarkTable.END, mark.getEnd());
		values.put(BookMarkTable.CONTENT, mark.getContent());
		values.put(BookMarkTable.CHAPTER_TITLE, mark.getContent());
		values.put(BookMarkTable.PERCENT, mark.getPercent());
		values.put(BookMarkTable.TIME, mark.getTime());
		values.put(BookMarkTable.CHAPTER_ID, mark.getChapterId());
		values.put(BookMarkTable.CHAPTER_TITLE, mark.getChapterTitle());
		values.put(BookMarkTable.DATE, mark.getDate());
		values.put(BookMarkTable.MARK_JSON, mark.getMarkJsonString());
		values.put(BookMarkTable.BOOK_SID, mark.getBookIdStr());

		return values;
	}

	/**
	 * 从Cursor中读取BookMark
	 * 
	 * @param cursor
	 * @param item
	 */
	private synchronized static void readBookMarkFromCursor(Cursor cursor,
			MarkItem item)
	{
		item.setId(cursor.getInt(cursor.getColumnIndex(BookMarkTable.ID)));
		item.setBookId(cursor.getInt(cursor
				.getColumnIndex(BookMarkTable.BOOK_ID)));
		item.setBegin(cursor.getInt(cursor.getColumnIndex(BookMarkTable.BEGIN)));
		item.setEnd(cursor.getInt(cursor.getColumnIndex(BookMarkTable.END)));
		item.setContent(cursor.getString(cursor
				.getColumnIndex(BookMarkTable.CONTENT)));
		item.setPercent(cursor.getString(cursor
				.getColumnIndex(BookMarkTable.PERCENT)));
		item.setTime(cursor.getString(cursor.getColumnIndex(BookMarkTable.TIME)));
		item.setChapterId(cursor.getString(cursor
				.getColumnIndex(BookMarkTable.CHAPTER_ID)));
		item.setChapterTitle(cursor.getString(cursor
				.getColumnIndex(BookMarkTable.CHAPTER_TITLE)));
		item.setDate(cursor.getString(cursor.getColumnIndex(BookMarkTable.DATE)));
		item.setMarkJsonString(cursor.getString(cursor
				.getColumnIndex(BookMarkTable.MARK_JSON)));
		item.setBookIdStr(cursor.getString(cursor
				.getColumnIndex(BookMarkTable.BOOK_SID)));
	}

	/**
	 * 将BookSummary对象设置到ContentValues中
	 * 
	 * @param summary
	 * @return
	 */
	private synchronized static ContentValues setBookSummaryValues(
			BookSummary summary)
	{
		ContentValues values = new ContentValues();

		values.put(BookSummaryTable.BOOK_ID, summary.getBookId());
		values.put(BookSummaryTable.BEGIN, summary.getBegin());
		values.put(BookSummaryTable.END, summary.getEnd());
		values.put(BookSummaryTable.OFFSET, summary.getOffset());
		values.put(BookSummaryTable.LENGTH, summary.getLength());
		values.put(BookSummaryTable.CONTENT, summary.getContent());
		values.put(BookSummaryTable.CHAPTER_TITLE, summary.getContent());
		values.put(BookSummaryTable.PERCENT, summary.getPercent());
		values.put(BookSummaryTable.TIME, summary.getTime());
		values.put(BookSummaryTable.CHAPTER_ID, summary.getChapterId());
		values.put(BookSummaryTable.CHAPTER_TITLE, summary.getChapterTitle());
		values.put(BookSummaryTable.DATE, summary.getDate());
		values.put(BookSummaryTable.SUMMARY_JSON,
				summary.getSummaryJsonString());
		values.put(BookSummaryTable.SUMMARY_BID, summary.getBookIdStr());
		return values;
	}

	/**
	 * 从Cursor中读取BookSummary
	 * 
	 * @param cursor
	 * @param item
	 */
	private synchronized static void readBookSummaryFromCursor(Cursor cursor,
			BookSummary item)
	{
		item.setId(cursor.getInt(cursor.getColumnIndex(BookSummaryTable.ID)));
		item.setBookId(cursor.getInt(cursor
				.getColumnIndex(BookSummaryTable.BOOK_ID)));
		item.setBegin(cursor.getInt(cursor
				.getColumnIndex(BookSummaryTable.BEGIN)));
		item.setEnd(cursor.getInt(cursor.getColumnIndex(BookSummaryTable.END)));
		item.setOffset(cursor.getInt(cursor
				.getColumnIndex(BookSummaryTable.OFFSET)));
		item.setLength(cursor.getInt(cursor
				.getColumnIndex(BookSummaryTable.LENGTH)));
		item.setContent(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.CONTENT)));
		item.setPercent(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.PERCENT)));
		item.setTime(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.TIME)));
		item.setChapterId(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.CHAPTER_ID)));
		item.setChapterTitle(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.CHAPTER_TITLE)));
		item.setDate(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.DATE)));
		item.setSummaryJsonString(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.SUMMARY_JSON)));
		item.setBookIdStr(cursor.getString(cursor
				.getColumnIndex(BookSummaryTable.SUMMARY_BID)));
	}

	private synchronized static ContentValues setDataCacheValues(
			DataCacheBean bean)
	{
		ContentValues values = new ContentValues();
		values.put(DataCacheTable.CACHE_KEY, bean.getKey());
		values.put(DataCacheTable.DATA, bean.getData());
		values.put(DataCacheTable.TIME, bean.getTime());
		values.put(DataCacheTable.INVALID, bean.getInvalid());
		return values;
	}

	private synchronized static void readDataCacheFromCursor(Cursor cursor,
			DataCacheBean bean)
	{
		bean.setId(cursor.getInt(cursor.getColumnIndex(DataCacheTable.ID)));
		bean.setKey(cursor.getString(cursor
				.getColumnIndex(DataCacheTable.CACHE_KEY)));
		bean.setData(cursor.getString(cursor
				.getColumnIndex(DataCacheTable.DATA)));
		bean.setTime(cursor.getLong(cursor.getColumnIndex(DataCacheTable.TIME)));
		bean.setInvalid(cursor.getLong(cursor
				.getColumnIndex(DataCacheTable.INVALID)));
	}

	/**
	 * 将Book对象设置到ContentValues中
	 * 
	 * @param book
	 * @return
	 */
	private synchronized static ContentValues setBookCacheValues(Book book)
	{
		ContentValues values = new ContentValues();

		values.put(BookCacheTable.ORIGIN_ID, book.getId());
		values.put(BookCacheTable.BOOK_ID, book.getBookId());

		values.put(BookCacheTable.FILE_PATH, book.getDownloadInfo()
				.getFilePath());
		values.put(BookCacheTable.FILE_SIZE, book.getDownloadInfo()
				.getFileSize());
		values.put(BookCacheTable.PROGRESS, book.getDownloadInfo()
				.getProgress());
		values.put(BookCacheTable.DOWNLOAD_STATE, book.getDownloadInfo()
				.getDownLoadState());
		values.put(BookCacheTable.LAST_POS, book.getReadInfo().getLastPos());
		values.put(BookCacheTable.ORIGINAL_FILE_PATH, book.getDownloadInfo()
				.getOriginalFilePath());
		values.put(BookCacheTable.LAST_READ_PERCENT, book.getReadInfo()
				.getLastReadPercent());
		values.put(BookCacheTable.SINA_ID, book.getSid());
		values.put(BookCacheTable.BOOK_CONTENT_TYPE, book.getBookContentType());
		values.put(BookCacheTable.SUITE_ID, book.getSuiteId());
		values.put(BookCacheTable.LAST_FONTSIZE, book.getBookPage()
				.getFontSize());
		values.put(BookCacheTable.TOTAL_PAGE, book.getBookPage().getTotalPage());
		values.put(BookCacheTable.LAST_PAGE, book.getBookPage().getCurPage());
		values.put(BookCacheTable.OWNER_UID, book.getOwnerUid());
		// 新添加的五个列
		values.put(BookCacheTable.TITLE, book.getTitle());
		values.put(BookCacheTable.AUTHOR, book.getAuthor());
		values.put(BookCacheTable.INTRO, book.getIntro());
		values.put(BookCacheTable.IMAGE_URL, book.getDownloadInfo()
				.getImageUrl());
		values.put(BookCacheTable.UID, book.getBuyInfo().getUid());
		values.put(BookCacheTable.TAG, book.getTag());
		LogUtil.i("ReadInfoLeft", "setBookCacheValues >> book{" + book + "}");
		return values;
	}

	/**
	 * 从Cursor中读取Book
	 * 
	 * @param cursor
	 * @param book
	 */
	private synchronized static void readBookCacheFromCursor(Cursor cursor,
			Book book)
	{
		book.setId(cursor.getInt(cursor
				.getColumnIndex(BookCacheTable.ORIGIN_ID)));
		book.setBookId(cursor.getString(cursor
				.getColumnIndex(BookCacheTable.BOOK_ID)));
		book.getDownloadInfo().setFilePath(
				cursor.getString(cursor
						.getColumnIndex(BookCacheTable.FILE_PATH)));
		book.getDownloadInfo()
				.setFileSize(
						cursor.getFloat(cursor
								.getColumnIndex(BookCacheTable.FILE_SIZE)));
		book.getDownloadInfo()
				.setProgress(
						cursor.getDouble(cursor
								.getColumnIndex(BookCacheTable.PROGRESS)));
		book.getDownloadInfo().setDownLoadState(
				cursor.getInt(cursor
						.getColumnIndex(BookCacheTable.DOWNLOAD_STATE)));
		book.getReadInfo().setLastPos(
				cursor.getInt(cursor.getColumnIndex(BookCacheTable.LAST_POS)));
		book.getDownloadInfo().setOriginalFilePath(
				cursor.getString(cursor
						.getColumnIndex(BookCacheTable.ORIGINAL_FILE_PATH)));
		book.getReadInfo().setLastReadPercent(
				cursor.getFloat(cursor
						.getColumnIndex(BookCacheTable.LAST_READ_PERCENT)));
		book.setSid(cursor.getString(cursor
				.getColumnIndex(BookCacheTable.SINA_ID)));
		book.setBookContentType(cursor.getInt(cursor
				.getColumnIndex(BookCacheTable.BOOK_CONTENT_TYPE)));
		book.setSuiteId(cursor.getInt(cursor
				.getColumnIndex(BookCacheTable.SUITE_ID)));
		book.getBookPage().setFontSize(
				cursor.getInt(cursor
						.getColumnIndex(BookCacheTable.LAST_FONTSIZE)));
		book.getBookPage()
				.setTotalPage(
						cursor.getInt(cursor
								.getColumnIndex(BookCacheTable.TOTAL_PAGE)));
		book.getBookPage().setCurPage(
				cursor.getInt(cursor.getColumnIndex(BookCacheTable.LAST_PAGE)));
		book.setOwnerUid(cursor.getString(cursor
				.getColumnIndex(BookCacheTable.OWNER_UID)));
		// 读取新添加的五个列
		book.setTitle(cursor.getString(cursor
				.getColumnIndex(BookCacheTable.TITLE)));
		book.setAuthor(cursor.getString(cursor
				.getColumnIndex(BookCacheTable.AUTHOR)));
		book.setIntroRealNeed(cursor.getString(cursor
				.getColumnIndex(BookCacheTable.INTRO)));
		book.getDownloadInfo().setImageUrl(
				cursor.getString(cursor
						.getColumnIndex(BookCacheTable.IMAGE_URL)));
		book.getBuyInfo().setUid(
				cursor.getString(cursor.getColumnIndex(BookCacheTable.UID)));
		book.setTag(cursor.getInt(cursor.getColumnIndex(BookCacheTable.TAG)));

		LogUtil.e("ReadInfoLeft", "readBookCacheFromCursor >> book{" + book
				+ "}");
	}
}