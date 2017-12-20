package com.sina.book.ui.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.ui.widget.BaseDialog;

import java.util.List;

/**
 * 列表对话框
 *
 * @author MarkMjw
 * @date 2013-8-21
 */
public class ListDialog extends BaseDialog {
    private static ListDialog mDialog;

    private String mTitle;

    private ListAdapter mAdapter;
    private ItemClickListener mListener;

    /**
     * Show分享Dialog
     *
     * @param context  上下文引用
     * @param title    提示title
     * @param items    选项数据
     * @param listener 监听器
     */
    public static void show(Context context, String title, List<String> items,
                            ItemClickListener listener) {
        dismiss(context);

        mDialog = new ListDialog(context, title, items, listener);
        mDialog.show();
    }

    /**
     * 构造方法
     *
     * @param context  上下文引用
     * @param title    提示title
     * @param items    选项数据
     * @param listener 监听器
     */
    public ListDialog(Context context, String title, List<String> items,
                      ItemClickListener listener) {
        super(context);

        mTitle = title;
        mAdapter = new ListAdapter(items);
        mListener = listener;
    }

    /**
     * Dismiss
     *
     * @param context
     */
    public static void dismiss(Context context) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setTitle(TextUtils.isEmpty(mTitle) ? mContext.getString(R.string.note) : mTitle);

        View content = LayoutInflater.from(mContext).inflate(R.layout.vw_common_list_dialog,
                mContentLayout);
        if (null != content) {
            ListView listView = (ListView) content.findViewById(R.id.dialog_list);

            listView.setAdapter(mAdapter);
        }
    }

    private class ListAdapter extends BaseAdapter {
        private List<String> mItems;

        public ListAdapter(List<String> items) {
            mItems = items;
        }

        @Override
        public Object getItem(int position) {
            if (null != mItems && position >= 0 && position < mItems.size()) {
                return mItems.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            if (null != mItems) {
                return mItems.size();
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView || null == convertView.getTag()) {
                convertView = createView();
            }

            if (null != convertView) {
                ViewHolder holder = (ViewHolder) convertView.getTag();

                holder.textView.setText(mItems.get(position));
                final int pos = position;
                holder.textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 隐藏Dialog
                        dismiss(mContext);

                        mListener.onItemClick(mDialog, pos);
                    }
                });
            }

            return convertView;
        }

        private View createView() {
            ViewHolder holder = new ViewHolder();
            View view = LayoutInflater.from(mContext).inflate(R.layout
                    .vw_common_list_dialog_item, null);
            if (null != view) {
                holder.textView = (TextView) view.findViewById(R.id.common_list_dialog_item_tv);

                view.setTag(holder);
            }
            return view;
        }

        private class ViewHolder {
            private TextView textView;
        }
    }

    /**
     * 选项被点击事件监听器
     */
    public interface ItemClickListener {

        /**
         * 选项被点击
         *
         * @param position 位置
         */
        public void onItemClick(DialogInterface dialog, int position);
    }
}
