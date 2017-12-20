package com.sina.book.control.download;

import android.content.Context;
import com.sina.book.ui.view.CommentDialog;
import com.sina.book.util.StorageUtil;

import java.util.Calendar;

/**
 * 提示去App Store评分管理器
 *
 * @author MarkMjw
 * @date 13-10-11.
 */
public class CommentAppManager {
    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
//    private static final long MINUTE_MILLIS = 60 * 1000;
    private static final int CYCLE = 7;

    private static CommentAppManager sInstance;
    private static Context sContext;

    public static CommentAppManager getInstance(Context context) {
        sContext = context;
        if (sInstance == null) {
            synchronized (CommentAppManager.class) {
                if (sInstance == null) {
                    sInstance = new CommentAppManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 检查是否需要弹出评分对话框
     */
    public void showCommentDialog() {
        boolean needShow = StorageUtil.getBoolean(StorageUtil.KEY_NEED_SHOW_COMMENT_APP, true);
        if (!needShow) {
            return;
        }

        long oldTime = StorageUtil.getLong(StorageUtil.KEY_SHOW_COMMENT_APP_DATE);

        // 刚安装或者清掉配置文件之后，首次启动不提示，只存储当前日期
        if (oldTime < 0) {
            saveCurrentDate();
            return;
        }

        long nowTime = System.currentTimeMillis();

        long spaceTime = Math.abs(nowTime - oldTime);
        // 当间隔天数≥7时才提示
        if (spaceTime / DAY_MILLIS >= CYCLE) {
            new CommentDialog(sContext).show();
        }
    }

    /**
     * 保存当前日期的开始时间
     */
    private void saveCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        StorageUtil.saveLong(StorageUtil.KEY_SHOW_COMMENT_APP_DATE, calendar.getTimeInMillis());
    }
}
