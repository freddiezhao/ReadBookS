package com.sina.book.ui.notification;

/**
 * 通知栏信息配置类<br>
 * <p>TAG和ID共同标志一个唯一的通知栏信息</p>
 * 
 * @author MarkMjw
 */
public class NotificationConfig {
    /** 下载书籍通知TAG */
    public static final String DOWNLOAD_NOTIFY_TAG = "download_notify";
    
    /** 下载书籍通知ID */
    public static final int DOWNLOAD_NOTIFY_ID = -1000;
    
    /** 返回阅读通知TAG */
    public static final String READBOOK_NOTIFY_TAG = "readbook_notify";
    
    /** 返回阅读通知ID */
    public static final int READBOOK_NOTIFY_ID = -1001;
    
    /** 返回书城通知TAG */
    public static final String RESTART_NOTIFY_TAG = "restart_notify";
    
    /** 返回书城通知ID */
    public static final int RESTART_NOTIFY_ID = -1002;
    
    /** 更新完成TAG */
    public static final String UPDATE_NOTIFY_TAG = "update_notify";
    
    /** 更新完成ID */
    public static final int UPDATE_NOTIFY_ID = -1003;

    /** 推送更新完成TAG */
    public static final String PUSH_NOTIFY_TAG = "push_notify";

    /** 推送更新完成ID */
    public static final int PUSH_NOTIFY_ID = -1004;
}
