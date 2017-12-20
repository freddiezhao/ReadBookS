package com.sina.book.db;

/**
 * 数据缓存表
 * 
 * @author Tsimle
 * 
 */
public class DataCacheTable {

    /** 表名 */
    public static final String TABLE_NAME = "DataCache";

    public static final String ID = "_id";

    public static final String CACHE_KEY = "key";
    public static final String DATA = "data";
    public static final String TIME = "time";
    public static final String INVALID = "invalid";

    /**
     * 数据缓存表列名称数组<br>
     * 注意：顺序不能变
     */
    public static final String[] COLUMNS = { ID, CACHE_KEY, DATA, TIME, INVALID };

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
        builder.append(CACHE_KEY).append(" varchar(500), ");
        builder.append(DATA).append(" text, ");
        builder.append(TIME).append(" largeint(256) DEFAULT (0), ");
        builder.append(INVALID).append(" largeint(256) DEFAULT (0)");

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
