package com.sina.book.ui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sina.book.R;

/**
 * 对话框父类
 * <p>应用内所有Dialog继承该类，统一Dialog风格</p>
 *
 * @author MarkMjw
 * @date 2013-8-7
 */
public abstract class BaseDialog extends Dialog {

    protected Context mContext;

    private TextView mTitle;
    protected LinearLayout mContentLayout;

    /**
     * 传入的Activity必须是BaseActivity或BaseFragmentActivity的子类
     *
     * @param activity
     */
    public BaseDialog(Activity activity) {
        super(activity, R.style.BaseDialog);

        mContext = activity;
    }

    public BaseDialog(Context context) {
        super(context, R.style.BaseDialog);

        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vw_base_dialog);
        initView();

        init(savedInstanceState);
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.dialog_title);
        mContentLayout = (LinearLayout) findViewById(R.id.dialog_content_layout);
    }

    public void setTitle(int resId) {
        setTitle(mContext.getString(resId));
    }

    public void setTitle(String title) {
        if (null != mTitle) mTitle.setText(title);
    }

    /**
     * 设置Content view，不建议使用
     *
     * @param contentView 注意该View不能有parent
     */
    @Deprecated
    public void setContent(View contentView) {
        if (null != contentView.getParent()) {
            throw new RuntimeException("The content view has parent.");
        }

        if (null != mContentLayout) {
            mContentLayout.removeAllViews();

            mContentLayout.addView(contentView);
        }
    }

    /**
     * 设置Content view
     *
     * @param contentView 注意该View不能有parent
     * @param params      LayoutParams
     */
    public void setContent(View contentView, ViewGroup.LayoutParams params) {
        if (null != contentView.getParent()) {
            throw new RuntimeException("The content view has parent.");
        }

        if (null != mContentLayout) {
            mContentLayout.removeAllViews();

            mContentLayout.addView(contentView, params);
        }
    }

    @Override
    public void show() {
        getWindow().setWindowAnimations(R.style.PopWindowAnimation);
        super.show();
    }

    /**
     * 初始化Dialog，类似onCreate()
     *
     * @param savedInstanceState
     */
    protected abstract void init(Bundle savedInstanceState);
}
