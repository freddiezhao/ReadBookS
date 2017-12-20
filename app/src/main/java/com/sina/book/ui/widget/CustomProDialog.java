package com.sina.book.ui.widget;

import com.sina.book.R;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 自定义的ProgressDialog
 * 
 * @author MarkMjw
 * @date 2013-4-16
 */
public class CustomProDialog extends Dialog {
    private Context mContext;

    private ProgressBar mProgressBar;
    private TextView mTextView;

    public CustomProDialog(Context context) {
        super(context, R.style.ProgressDialog);
        init(context);
    }

    public CustomProDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setContentView(R.layout.vw_generic_progress_dlg);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mTextView = (TextView) findViewById(R.id.progress_msg);
        
        mProgressBar.setInterpolator(new LinearInterpolator());
    }

    public void show(int msgResId) {
        show(mContext.getString(msgResId));
    }

    public void show(String msg) {
        mTextView.setText(msg);
        show();
    }

    public void setMessage(int msgResId) {
        mTextView.setText(msgResId);
    }

    public void setMessage(String msg) {
        mTextView.setText(msg);
    }
}
