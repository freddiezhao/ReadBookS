package com.sina.book.db;

/**
 * 书摘表
 * 
 * @author MarkMjw
 * @date 2013-3-7
 */
public class BookSummaryTable {
	/** 表名 */
	public static final String TABLE_NAME = "BookSummary";

	public static final String ID = "_id";

	public static final String BOOK_ID = "bookId";
	public static final String BEGIN = "begin";
	public static final String END = "end";
	public static final String OFFSET = "offset";
	public static final String LENGTH = "length";
	public static final String CONTENT = "content";
	public static final String PERCENT = "percent";
	public static final String TIME = "time";
	public static final String CHAPTER_ID = "chapterId";
	public static final String CHAPTER_TITLE = "chapterTitle";
	public static final String DATE = "date";

	public static final String SUMMARY_JSON = "summaryJsonString";
	public static final String SUMMARY_BID = "book_id";

	/**
	 * 书摘表列名称数组<br>
	 * 注意：顺序不能变
	 */
	public static final String[] COLUMNS = { ID, BOOK_ID, BEGIN, END, CONTENT, PERCENT, TIME, CHAPTER_ID,
			CHAPTER_TITLE, DATE, SUMMARY_JSON };

	/**
	 * 获取建表SQL语句
	 * 
	 * @return
	 */
	public static String getCreateSQL() {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME);
		builder.append(" (");

		builder.append(ID).append(" integer primary key autoincrement, ");
		builder.append(BOOK_ID).append(" integer not null, ");
		builder.append(BEGIN).append(" integer, ");
		builder.append(END).append(" integer, ");
		builder.append(OFFSET).append(" integer, ");
		builder.append(LENGTH).append(" integer, ");
		builder.append(CONTENT).append(" text, ");
		builder.append(PERCENT).append(" varchar(50), ");
		builder.append(TIME).append(" varchar(50), ");
		builder.append(CHAPTER_ID).append(" integer, ");
		builder.append(CHAPTER_TITLE).append(" varchar(256), ");
		builder.append(DATE).append(" varchar(20), ");
		builder.append(SUMMARY_JSON).append(" text, ");
		builder.append(SUMMARY_BID).append(" text");

		builder.append(")");
		return builder.toString();
	}

	/**
	 * 获取删表SQL语句
	 * 
	 * @return
	 */
	public static String getDeleteSQL() {
		return "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
}
