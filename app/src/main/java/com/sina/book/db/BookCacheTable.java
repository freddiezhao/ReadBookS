package com.sina.book.db;

import com.sina.book.util.LogUtil;

public class BookCacheTable {
	/** 表名 */
	public static final String TABLE_NAME = "BookCache";

	public static final String ORIGIN_ID = "originId";
	public static final String BOOK_ID = "bookId";
	public static final String FILE_PATH = "filePath";
	public static final String FILE_SIZE = "fileSize";

	public static final String PROGRESS = "progress";
	public static final String DOWNLOAD_STATE = "downLoadState";
	public static final String TOTAL_PAGE = "totalPage";
	public static final String LAST_POS = "lastPos";
	public static final String LAST_FONTSIZE = "lastFontSize";
	public static final String LAST_PAGE = "lastPage";
	public static final String ORIGINAL_FILE_PATH = "originalFilePath";
	public static final String LAST_READ_PERCENT = "lastReadPercent";
	public static final String SINA_ID = "sid";
	public static final String BOOK_CONTENT_TYPE = "contentType";
	public static final String SUITE_ID = "suiteId";
	public static final String OWNER_UID = "ownerUid";
	
	// 17版本
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String INTRO = "intro";
    public static final String IMAGE_URL = "imageUrl";
    public static final String UID = "uid";
	public static final String TAG = "tag";

	public static final String[] COLUMNS = { ORIGIN_ID, BOOK_ID, FILE_PATH, FILE_SIZE, PROGRESS, DOWNLOAD_STATE,
			TOTAL_PAGE, LAST_POS, LAST_FONTSIZE, LAST_PAGE, ORIGINAL_FILE_PATH, LAST_READ_PERCENT, SINA_ID,
			BOOK_CONTENT_TYPE, SUITE_ID, OWNER_UID, TITLE, AUTHOR, INTRO, IMAGE_URL, UID, TAG};

	/**
	 * 获取建表SQL语句
	 * 
	 * @return
	 */
	public static String getCreatSQL() {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME);
		builder.append(" (");

		builder.append(ORIGIN_ID).append(" integer primary key,");
		builder.append(BOOK_ID).append(" varchar(20),");
		builder.append(FILE_PATH).append(" varchar(256),");
		builder.append(FILE_SIZE).append(" varchar(20),");
		builder.append(PROGRESS).append(" varchar(20),");
		builder.append(DOWNLOAD_STATE).append(" varchar(20),");
		builder.append(TOTAL_PAGE).append(" integer DEFAULT (0),");
		builder.append(LAST_POS).append(" integer DEFAULT (0),");
		builder.append(LAST_FONTSIZE).append(" integer DEFAULT (0),");
		builder.append(LAST_PAGE).append(" integer DEFAULT (0),");
		builder.append(ORIGINAL_FILE_PATH).append(" varchar(256) ,");
		builder.append(LAST_READ_PERCENT).append(" float(20) DEFAULT(0),");
		builder.append(SINA_ID).append(" varchar(50),");
		builder.append(BOOK_CONTENT_TYPE).append(" integer DEFAULT (0),");
		builder.append(SUITE_ID).append(" integer DEFAULT (0),");
		builder.append(OWNER_UID).append(" varchar(256),");
		// 新添加的字段放到后面来
		builder.append(TITLE).append(" varchar(256),");
		builder.append(AUTHOR).append(" varchar(20),");
		builder.append(INTRO).append(" text,");
		builder.append(IMAGE_URL).append(" varchar(256),");
        builder.append(UID).append(" varchar(256),");
        builder.append(TAG).append(" integer DEFAULT (0)");

		builder.append(")");
		return builder.toString();
	}

	/**
	 * 获取删表SQL语句
	 * 
	 * @return
	 */
	public static String getDeleteSQL() {
		LogUtil.e("ReadInfoLeft", "BookCacheTable >> getDeleteSQL >> DROP TABLE IF EXISTS");
		return "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
}
