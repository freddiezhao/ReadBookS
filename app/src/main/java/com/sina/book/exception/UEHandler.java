package com.sina.book.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.sina.book.util.CalendarUtil;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.HttpUtil.NetworkState;
import com.sina.book.util.LogUtil;

/**
 * 将崩溃异常记录在文件中，方便我们查看异常
 * 
 * @author Tsimle
 * 
 */
public class UEHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

    public UEHandler(Context context) {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        mContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ByteArrayOutputStream baos = null;
        PrintStream printStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            baos = new ByteArrayOutputStream();
            printStream = new PrintStream(baos);
            ex.printStackTrace(printStream);
            byte[] data = baos.toByteArray();
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printStream);
                cause = cause.getCause();
            }
            
            String curDate = CalendarUtil.getCurrentTImeWithFormat("yyyy-MM-dd HH:mm:ss");
            sb.append("Exception time:" + curDate
                    + " Thread Name:" + thread.getName() + " Thread id:"
                    + thread.getId() + "\n");
            sb.append(collectCrashDeviceInfo(mContext) + "\n");
            sb.append(new String(data) + "\n");
            LogUtil.fileLogE(sb.toString());
            data = null;
        } catch (Exception e) {
            LogUtil.e(e);
        } finally {
            try {
                if (printStream != null) {
                    printStream.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                LogUtil.e(e);
            }
        }
        // 弹出程序crash的对话框
        mDefaultHandler.uncaughtException(thread, ex);
    }

    /**
     * 收集程序崩溃的设备信息
     * 
     * @param context
     */
    public String collectCrashDeviceInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            final PackageManager pm = context.getPackageManager();
            final PackageInfo info = pm.getPackageInfo(
                    context.getPackageName(), 0);
            sb.append("版本").append(info.versionName).append(",")
                    .append(info.versionCode).append(",");
            sb.append("型号").append(Build.MODEL).append(",");
            sb.append("系统").append(Build.VERSION.RELEASE).append(",");
            sb.append(getNetworkType());
        } catch (final Exception e) {
            // 忽略异常
        }
        return sb.toString();
    }

    private String getNetworkType() {
        if (HttpUtil.getNetworkState(mContext) == NetworkState.WIFI) {
            return "wifi";
        } else {
            String apn = HttpUtil.getAPN(mContext).apn;
            return apn;
        }
    }
}
