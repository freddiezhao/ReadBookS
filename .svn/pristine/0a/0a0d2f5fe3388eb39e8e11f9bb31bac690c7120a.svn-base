package com.sina.book.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.RecommendMonthItem;
import com.sina.book.ui.PaymentMonthBookListActivity;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 【推荐页】包月信息Adapter
 *
 * @author MarkMjw
 * @date 2013-4-10
 */
public class RecommendMonthAdapter extends ListAdapter<RecommendMonthItem> {
    private Context mContext;

    private Drawable mDividerDrawable;

    public RecommendMonthAdapter(Context context, Drawable divider) {
        this.mContext = context;
        this.mDividerDrawable = divider;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = createView();
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        RecommendMonthItem item = (RecommendMonthItem) getItem(position);

        String name = item.getName();
        if (!TextUtils.isEmpty(name)) {
            name = name.substring(name.indexOf("-") + 1, name.length());
            holder.mType.setText(name);
        }

        holder.mDivider.setImageDrawable(mDividerDrawable);
        holder.mTypeNum.setText(String.format(mContext.getString(
                R.string.recommend_month_num), item.getTotal()));

        String price = String.format(mContext.getString(R.string.recommend_month_price),
                item.getPrice() / 100);
        holder.mPrice.setText(price);

        String title = mContext.getString(R.string.recommend_month_new);
        for (int i = 0; i < item.getBooks().size(); i++) {
            title += item.getBooks().get(i).getTitle().trim();

            if (i + 1 < item.getBooks().size()) {
                title += " | ";
            }
        }
        holder.mBooks.setText(title);
        convertView.setOnClickListener(new ClickListener(item));

        return convertView;
    }

    @Override
    protected List<RecommendMonthItem> createList() {
        return new ArrayList<RecommendMonthItem>();
    }

    protected View createView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_recommend_mounth_item,
                null);
        ViewHolder holder = new ViewHolder();

        holder.mDivider = (ImageView) itemView.findViewById(R.id.mounth_list_divider);
        holder.mType = (TextView) itemView.findViewById(R.id.mounth_type);
        holder.mTypeNum = (TextView) itemView.findViewById(R.id.mounth_type_num);
        holder.mPrice = (TextView) itemView.findViewById(R.id.mounth_price);
        holder.mBooks = (EllipsizeTextView) itemView.findViewById(R.id.mounth_books);

        itemView.setTag(holder);
        return itemView;
    }

    private class ViewHolder {
        public ImageView mDivider;
        public TextView mType;
        public TextView mTypeNum;
        public TextView mPrice;
        public EllipsizeTextView mBooks;
    }

    private class ClickListener implements OnClickListener {
        private RecommendMonthItem mItem;

        public ClickListener(RecommendMonthItem item) {
            mItem = item;
        }

        @Override
        public void onClick(View v) {
            // 进入详情
            Intent intent = new Intent();
            intent.setClass(mContext, PaymentMonthBookListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            intent.putExtra(PaymentMonthBookListActivity.KEY_PAYMENT_MONTH_TYPE, mItem.getName());
            intent.putExtra(PaymentMonthBookListActivity.KEY_PAYMENT_MONTH_ID, mItem.getId());
            intent.putExtra(PaymentMonthBookListActivity.KEY_PAYMENT_MONTH_OPEN, mItem.getIsBuy());
            mContext.startActivity(intent);

            UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_MONTH);
        }
    }
}