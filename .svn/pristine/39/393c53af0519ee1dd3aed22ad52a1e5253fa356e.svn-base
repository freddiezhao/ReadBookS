package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * 通过LinearLayout模仿ListView，适用场景：ScrollView嵌套ListView
 *
 * @author MarkMjw
 * @date 13-12-9.
 */
public class LinearLayoutListView extends LinearLayout {
//    private final String TAG = "LinearLayoutListView";

//    private Context mContext;

    private BaseAdapter mAdapter;
    private OnClickListener mListener = null;

    public LinearLayoutListView(Context context) {
        super(context);

        init(context);
    }

    public LinearLayoutListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) return;

//        mContext = context;
        setOrientation(LinearLayout.VERTICAL);
    }

    /**
     * 获取Adapter
     *
     * @return
     */
    public BaseAdapter getAdpater() {
        return mAdapter;
    }

    /**
     * 设置Adapter
     *
     * @param adapter
     */
    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        notifyDataSetChanged();
    }

    /**
     * 设置点击事件
     *
     * @param listener
     */
    public void setOnItemClickLinstener(OnClickListener listener) {
        mListener = listener;
    }

    public void notifyDataSetChanged() {
        if (null != mAdapter) {
            if (getChildCount() > 0) {
                // 如果有子View，则先remove掉
                removeAllViews();
            }

            // add content list view
            int count = mAdapter.getCount();
            for (int i = 0; i < count; i++) {
                View view = mAdapter.getView(i, null, null);

                if (null != view) {
                    if (null != mListener) {
                        view.setOnClickListener(mListener);
                    }
                    addView(view);
                }
            }
        }
    }

}
