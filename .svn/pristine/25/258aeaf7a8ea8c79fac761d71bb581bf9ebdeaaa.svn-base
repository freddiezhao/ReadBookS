package com.sina.book.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.sina.book.control.AbsNormalAsyncTask;

/**
 * 封装AsyncTask类，用于执行异步耗时操作。
 * 
 * @author 张代松 2012-7-24
 */
public final class AsyncTaskUtils extends AbsNormalAsyncTask<Integer, Integer, Integer> implements
        OnCancelListener {

    public static final int FLAG_DEFAULT = 0;

    private static final String DEFAULT_PROGRESS_MESSAGE = "Please waiting...";

    /** Progress dialog. */
    private ProgressDialog progressDialog;

    private Context context;

    private boolean showProgress;

    private boolean progressCancelable;

    private String progressTitle;

    private String progressMessage;

    private AsyncTaskCancelListener onCancelListener;

    private boolean cancel;

    private AsyncTaskListener listener;

    private int flag;

    /**
     * Private constructor.
     * 
     * @param context
     * @param listener
     * @param showProgress
     */
    private AsyncTaskUtils(Context context, AsyncTaskListener listener, boolean showProgress) {
        this.context = context;
        this.listener = listener;
        this.showProgress = showProgress;
    }

    /**
     * @param context
     * @param listener
     * @param showProgress
     * @return
     */
    public static AsyncTaskUtils create(Context context, AsyncTaskListener listener,
            boolean showProgress) {
        AsyncTaskUtils task = new AsyncTaskUtils(context, listener, showProgress);

        return task;
    }

    /**
     * @param context
     * @param listener
     * @param progressMsgId
     * @return
     */
    public static AsyncTaskUtils create(Context context, AsyncTaskListener listener,
            int progressMsgId) {
        AsyncTaskUtils task = new AsyncTaskUtils(context, listener, true);
        task.setProgressDialog(null, progressMsgId, false);

        return task;
    }

    /**
     * @param context
     * @param listener
     * @param progressMsg
     * @return
     */
    public static AsyncTaskUtils create(Context context, AsyncTaskListener listener,
            String progressMsg) {
        AsyncTaskUtils task = new AsyncTaskUtils(context, listener, true);
        task.setProgressDialog(null, progressMsg, false);

        return task;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        return listener.doInBackground(this, params[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (!cancel) {
            listener.onPostExecute(this, result, flag);
        } else if (onCancelListener != null) {
            onCancelListener.onCancel(this);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (showProgress) {
            progressDialog = ProgressDialog.show(context, progressTitle,
                    progressMessage == null ? DEFAULT_PROGRESS_MESSAGE : progressMessage, true,
                    progressCancelable, this);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel = true;
    }

    /**
     * Executes the task with the no parameters, flag is default 0.
     */
    public void execute() {
        this.execute(0);
    }

    /**
     * Executes the task with the specify flag.
     */
    public void execute(int flag) {
        this.flag = flag;
        this.cancel = false;

        super.execute(flag);
    }

    /**
     * @param title
     * @param msg
     * @param cancelable
     * @return
     */
    public AsyncTaskUtils setProgressDialog(String title, String msg, boolean cancelable) {
        this.progressTitle = title;
        this.progressMessage = msg;
        this.progressCancelable = cancelable;

        return this;
    }

    /**
     * @param title
     * @param msg
     * @param cancelable
     * @param onCancelListener
     * @return
     */
    public AsyncTaskUtils setProgressDialog(String title, String msg, boolean cancelable,
            AsyncTaskCancelListener onCancelListener) {
        this.progressTitle = title;
        this.progressMessage = msg;
        this.progressCancelable = cancelable;
        this.onCancelListener = onCancelListener;

        return this;
    }

    /**
     * @param title
     * @param msgId
     * @param cancelable
     * @return
     */
    public AsyncTaskUtils setProgressDialog(String title, int msgId, boolean cancelable) {
        this.progressTitle = title;
        this.progressMessage = context.getText(msgId).toString();
        this.progressCancelable = cancelable;

        return this;
    }

    /**
     * @param title
     * @param msgId
     * @param cancelable
     * @param onCancelListener
     * @return
     */
    public AsyncTaskUtils setProgressDialog(String title, int msgId, boolean cancelable,
            AsyncTaskCancelListener onCancelListener) {
        this.progressTitle = title;
        this.progressMessage = context.getText(msgId).toString();
        this.progressCancelable = cancelable;
        this.onCancelListener = onCancelListener;

        return this;
    }

    /**
     * Get the field cancel.
     * 
     * @return the cancel
     */
    public boolean isCancel() {
        return cancel;
    }

    /**
     * Set the field cancel.
     * 
     * @param cancel
     *            the cancel to set
     */
    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * @author 张代松 2012-7-24
     */
    public interface AsyncTaskListener {
        int doInBackground(AsyncTaskUtils task, int flag);

        void onPostExecute(AsyncTaskUtils task, int result, int flag);
    }

    /**
     * @author 张代松 2012-7-24
     */
    public interface AsyncTaskCancelListener {
        void onCancel(AsyncTaskUtils task);
    }

    /**
     * 定义异步任务结果code， 用于判断成功或者失败类型
     * 
     * @author 张代松 2012-7-25
     */
    public static interface TaskResultCode {
        // 操作成功
        int CODE_OK = 0;
    }
}
