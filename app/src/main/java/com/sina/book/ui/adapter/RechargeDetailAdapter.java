package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.RechargeDetail;
import com.sina.book.util.PixelUtil;

public class RechargeDetailAdapter extends ListAdapter<RechargeDetail> {
    private Context mContext;
    private ViewHolder mHolder;
    private RechargeDetail fixHead = new RechargeDetail("充值时间", "订单详情", "订单号");;

    public RechargeDetailAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        if (count == 0) {
            return count;
        } else {
            return count + 1;
        }
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return fixHead;
        } else {
            return super.getItem(position - 1);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = createView();
        }

        mHolder = (ViewHolder) convertView.getTag();
        RechargeDetail rdetail = (RechargeDetail) getItem(position);
        int color;
        if (position == 0) {
            color = mContext.getResources().getColor(R.color.consume_item_title);
            mHolder.no.setTextSize(12);
        } else {
            color = mContext.getResources().getColor(R.color.consume_item_normal);
            mHolder.no.setTextSize(10);
        }
        
        if (position == 0) {
            if (mHolder.divide1.getLayoutParams() != null && mHolder.divide2.getLayoutParams() != null) {
                MarginLayoutParams ll1 = (MarginLayoutParams) mHolder.divide1.getLayoutParams();
                ll1.topMargin = PixelUtil.dp2px(12);
                MarginLayoutParams ll2 = (MarginLayoutParams) mHolder.divide2.getLayoutParams();
                ll2.topMargin = PixelUtil.dp2px(12);
            }
        } else if (position == getCount() - 1) { 
            if (mHolder.divide1.getLayoutParams() != null && mHolder.divide2.getLayoutParams() != null) {
                MarginLayoutParams ll1 = (MarginLayoutParams) mHolder.divide1.getLayoutParams();
                ll1.bottomMargin = PixelUtil.dp2px(12);
                MarginLayoutParams ll2 = (MarginLayoutParams) mHolder.divide2.getLayoutParams();
                ll2.bottomMargin = PixelUtil.dp2px(12);
            }
        } else {
            if (mHolder.divide1.getLayoutParams() != null && mHolder.divide2.getLayoutParams() != null) {
                MarginLayoutParams ll1 = (MarginLayoutParams) mHolder.divide1.getLayoutParams();
                ll1.topMargin = 0;
                ll1.bottomMargin = 0;
                MarginLayoutParams ll2 = (MarginLayoutParams) mHolder.divide2.getLayoutParams();
                ll2.topMargin = 0;
                ll2.bottomMargin = 0;
            }
        }
        mHolder.time.setTextColor(color);
        mHolder.detail.setTextColor(color);
        mHolder.no.setTextColor(color);

        mHolder.time.setText(rdetail.getTime());
        mHolder.detail.setText(rdetail.getDetail());
        mHolder.no.setText(rdetail.getNo());
        return convertView;

    }

    @Override
    protected List<RechargeDetail> createList() {
        return new ArrayList<RechargeDetail>();
    }

    protected View createView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_recharge_detail_item, null);
        ViewHolder holder = new ViewHolder();
        holder.time = (TextView) itemView.findViewById(R.id.time);
        holder.detail = (TextView) itemView.findViewById(R.id.detail);
        holder.no = (TextView) itemView.findViewById(R.id.no);
        holder.divide1 = itemView.findViewById(R.id.divide1);
        holder.divide2 = itemView.findViewById(R.id.divide2);
        itemView.setTag(holder);
        return itemView;
    }

    protected class ViewHolder {
        public TextView time;
        public TextView detail;
        public TextView no;
        public View divide1;
        public View divide2;
    }
}
