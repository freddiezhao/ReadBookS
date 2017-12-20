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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.data.HotWord;

import java.util.ArrayList;
import java.util.List;

public class HotWordsAdapter extends ListAdapter<HotWord> {
	private Context mContext;
	private BitmapDrawable mDotHDrawable;
	public HotWordsAdapter(Context context) {
		this.mContext = context;
		Bitmap dotHBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.list_divide_dot);
		mDotHDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
		mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mDotHDrawable.setDither(true);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null) {
            Holder holder = new Holder();
            LinearLayout layout = new LinearLayout(mContext);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_hotwords_list_item,
                    layout);
            holder.mHotrankTv = (TextView) convertView.findViewById(R.id.hotwords_rank);
            holder.mHotnameTv = (TextView) convertView.findViewById(R.id.hotwords_name);
            holder.mHotstaTIm = (ImageView) convertView.findViewById(R.id.hotwords_state);
            holder.mDivider = (ImageView) convertView.findViewById(R.id.hotwords_divider);
            convertView.setTag(holder);
        }

        Holder holder = (Holder) convertView.getTag();

        HotWord hotWord = (HotWord) getItem(position);
        holder.mHotrankTv.setText(Integer.toString(position + 1));
        holder.mHotnameTv.setText(mDataList.get(position).getName());
        holder.mDivider.setBackgroundDrawable(mDotHDrawable);

        if (position == 0) {
            holder.mHotrankTv.setBackgroundResource(R.drawable.number_1);
        } else if (position == 1) {
            holder.mHotrankTv.setBackgroundResource(R.drawable.number_2);
        } else if (position == 2) {
            holder.mHotrankTv.setBackgroundResource(R.drawable.number_3);
        } else {
            holder.mHotrankTv.setBackgroundResource(R.drawable.number_more);
        }

        if ((HotWord.STATE_UP).equals(hotWord.getState())) {
            holder.mHotstaTIm.setBackgroundResource(R.drawable.up);
        } else if ((HotWord.STATE_BALANCE).equals(hotWord.getState())) {
            holder.mHotstaTIm.setBackgroundResource(R.drawable.right);
        } else if ((HotWord.STATE_DOWN).equals(hotWord.getState())) {
            holder.mHotstaTIm.setBackgroundResource(R.drawable.down);
        } else {
            holder.mHotstaTIm.setBackgroundResource(R.drawable.right);
        }
        return convertView;
    }

	@Override
	protected List<HotWord> createList() {
		return new ArrayList<HotWord>();
	}

	private class Holder {
		TextView mHotnameTv;
		TextView mHotrankTv;
		ImageView mHotstaTIm;
		ImageView mDivider;
	}

}
