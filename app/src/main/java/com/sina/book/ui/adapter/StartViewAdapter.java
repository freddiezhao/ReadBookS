package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Gallery;
import android.widget.ImageView;

import com.sina.book.R;
import com.sina.book.image.ImageUtil;
import com.sina.book.ui.SplashActivity;

public class StartViewAdapter extends BaseAdapter implements OnCheckedChangeListener, OnClickListener {

	private int[] mLayoutIds;
	private Integer[] mImageIds = { R.drawable.guide1, R.drawable.guide2 };
	protected List<Bitmap> mBmpViews = new ArrayList<Bitmap>();
	protected SplashActivity mSplashActivity;
	protected boolean mChecked = false;

	public StartViewAdapter(SplashActivity activity) {
		mLayoutIds = new int[2];
		mLayoutIds[0] = R.layout.vw_guide_normal;
		mLayoutIds[1] = R.layout.vw_guide_last;
		mSplashActivity = activity;
		initBmps();
	}

	public void release() {
		for (Bitmap bitmap : mBmpViews) {
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
		mBmpViews.clear();
	}

	public int getCount() {
		if (mBmpViews != null) {
			return mBmpViews.size();
		} else {
			return 0;
		}
	}

	public Object getItem(int position) {
		return Integer.valueOf(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public final int getItemViewType(int position) {
		int i;
		// position为最后一项时，需要inflate的布局为vw_guide_last
		if (position == getCount() - 1) {
			i = 1;
		} else {
			i = 0;
		}
		return i;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null || (((Integer) convertView.getTag()).intValue() != getItemViewType(position))) {
			convertView = View.inflate(mSplashActivity, mLayoutIds[getItemViewType(position)], null);
			convertView.setTag(Integer.valueOf(getItemViewType(position)));
			convertView.setLayoutParams(new Gallery.LayoutParams(-1, -1));
		}
		if (getItemViewType(position) == 1) {
			final CheckBox checkbox = ((CheckBox) convertView.findViewById(R.id.weibo_check));
			checkbox.setOnCheckedChangeListener(this);
			final Button enter = (Button) convertView.findViewById(R.id.enter_weibo);
			enter.setOnClickListener(this);
			((ImageView) convertView.findViewById(R.id.what_new)).setImageBitmap(mBmpViews.get(position));
		} else if (getItemViewType(position) == 0) {
			((ImageView) convertView.findViewById(R.id.what_new)).setImageBitmap(mBmpViews.get(position));
		}
		return convertView;
	}

	private void initBmps() {
		for (int id : mImageIds) {
			Bitmap bitmap = ImageUtil.getBitmapFromResId(mSplashActivity, id, true);
			if (null != bitmap) {
				mBmpViews.add(bitmap);
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mChecked = isChecked;
	}

	@Override
	public void onClick(View v) {
		mSplashActivity.openMainTab(mChecked);
	}

	public boolean isShareWeiboChecked() {
		return mChecked;
	}
}
