package com.sina.book.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.data.CommentItem;

import java.util.ArrayList;
import java.util.List;

public class CommentListAdapter extends ListAdapter<CommentItem> {

	private Context mContext;
	private int mMoreItem = 0;
	private BitmapDrawable mDotHDrawable;

    public CommentListAdapter(Context context) {
        this.mContext = context;
        Bitmap dotHBitmap = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.list_divide_dot);
        mDotHDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
        mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        mDotHDrawable.setDither(true);
    }

    /**
     * 构造方法
     *
     * @param context
     * @param dividerRes 分割线资源ID
     */
    public CommentListAdapter(Context context, int dividerRes) {
        this.mContext = context;
        Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(), dividerRes);
        mDotHDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
        mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        mDotHDrawable.setDither(true);
    }

	@Override
	public int getCount() {
		int count = 0;
		if (mDataList != null) {
			count = mDataList.size();
			if (mDataList.size() < mTotal) {
				count = mDataList.size() + mMoreItem;
			}
		}
		return count;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (position == mDataList.size()) {// 获取更多信息
			if (!IsAdding()) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.vw_generic_more, null);
			} else {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.vw_generic_loading, null);
			}
			return convertView;
		} else {
			if (convertView == null || convertView.getTag() == null) {
				holder = new Holder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.vw_comments_list_item, null);
				holder.mUerTv = (TextView) convertView
						.findViewById(R.id.user_name);
				holder.mTimeTv = (TextView) convertView
						.findViewById(R.id.comments_time);
				holder.mMsgTv = (TextView) convertView
						.findViewById(R.id.comments_content);
				holder.mDivider = (ImageView) convertView
						.findViewById(R.id.comments_divider);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			holder.mUerTv.setText(mDataList.get(position).getUser());
			holder.mMsgTv.setText(mDataList.get(position).getMsg());
			holder.mTimeTv.setText(mDataList.get(position).getTime());
			holder.mDivider.setBackgroundDrawable(mDotHDrawable);
			return convertView;
		}
	}

	@Override
	protected List<CommentItem> createList() {
		return new ArrayList<CommentItem>();
	}

	private class Holder {
		TextView mUerTv;
		TextView mTimeTv;
		TextView mMsgTv;
		ImageView mDivider;
	}
}
