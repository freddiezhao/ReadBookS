package com.sina.book.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.sina.book.R;

/**
 * 针对CustomTitleActivity，直接将TitleBar包含<br>
 * 方便父类的灵活控制
 * 
 * @author Tsimle
 * 
 */
public class BaseLayout extends RelativeLayout implements OnClickListener {
	private View mTitlebar;
	private FrameLayout mLeft;
	private FrameLayout mRight;
	private FrameLayout mMiddle;
	private FrameLayout mNearRight;

	public static final int TAG_LEFT = 1;
	public static final int TAG_MIDDLE = 2;
	public static final int TAG_RIGHT = 3;
	public static final int TAG_NEAR_RIGHT = 4;

	private BarClickListener mBarClickListener = null;

	public BaseLayout(Context context, int resId) {
		super(context);
		this.setBackgroundColor(getResources().getColor(R.color.public_bg));
		// title
		LayoutInflater i = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTitlebar = i.inflate(R.layout.vw_generic_title, null);
		LayoutParams titlelp = null;
		titlelp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		titlelp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		this.addView(mTitlebar, titlelp);

		// content
		View contentView = i.inflate(resId, null);
		LayoutParams contentlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		contentlp.addRule(RelativeLayout.BELOW, R.id.lyTitleBar);
		this.addView(contentView, contentlp);

		// shadow
		ImageView shadow = new ImageView(context);
		shadow.setScaleType(ScaleType.FIT_XY);
		shadow.setBackgroundResource(R.drawable.titlebar_shadow);
		LayoutParams shadowlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
		shadowlp.addRule(RelativeLayout.BELOW, R.id.lyTitleBar);
		this.addView(shadow, shadowlp);

		// find
		mLeft = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_left);
		mRight = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_right);
		mMiddle = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_middle);
		mNearRight = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_near_right);
		mTitlebar.setVisibility(View.GONE);
	}

	public BaseLayout(Context context, View contentView) {
		super(context);
		this.setBackgroundColor(getResources().getColor(R.color.public_bg));
		// title
		LayoutInflater i = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTitlebar = i.inflate(R.layout.vw_generic_title, null);
		LayoutParams titlelp = null;
		titlelp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		titlelp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		this.addView(mTitlebar, titlelp);

		// content
		LayoutParams contentlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		contentlp.addRule(RelativeLayout.BELOW, R.id.lyTitleBar);
		this.addView(contentView, contentlp);

		// shadow
		ImageView shadow = new ImageView(context);
		shadow.setScaleType(ScaleType.FIT_XY);
		shadow.setBackgroundResource(R.drawable.titlebar_shadow);
		LayoutParams shadowlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
		shadowlp.addRule(RelativeLayout.BELOW, R.id.lyTitleBar);
		this.addView(shadow, shadowlp);

		// find
		mLeft = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_left);
		mRight = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_right);
		mMiddle = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_middle);
		mNearRight = (FrameLayout) mTitlebar.findViewById(R.id.fl_title_near_right);
		mTitlebar.setVisibility(View.GONE);
	}

	public BaseLayout(Context context) {
		super(context);
		// 这个构造方法仅仅为了在编辑器里面能够预览
	}

	public void setTitleLeft(View view) {
		if (mTitlebar.getVisibility() == View.GONE) {
			mTitlebar.setVisibility(View.VISIBLE);
		}
		if (view != null) {
			mLeft.removeAllViews();
			// view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT));
			view.setOnClickListener(this);
			view.setTag(TAG_LEFT);
			mLeft.addView(view);
			mLeft.setTag(true);
		}
	}

	public void setTitleRight(View view) {
		if (mTitlebar.getVisibility() == View.GONE) {
			mTitlebar.setVisibility(View.VISIBLE);
		}
		if (view != null) {
			mRight.removeAllViews();
			// FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
			// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
			// Gravity.CENTER_VERTICAL);
			// view.setLayoutParams(lp);
			view.setOnClickListener(this);
			view.setTag(TAG_RIGHT);
			mRight.addView(view);
			mRight.setTag(true);
		}
	}

	public void setTitleMiddle(View view) {
		if (mTitlebar.getVisibility() == View.GONE) {
			mTitlebar.setVisibility(View.VISIBLE);
		}
		if (view != null) {
			mMiddle.removeAllViews();
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.CENTER;
			view.setTag(TAG_MIDDLE);
			view.setLayoutParams(lp);
			view.setOnClickListener(this);
			mMiddle.addView(view);
			mMiddle.setTag(true);
		}
	}

	public void setTitleNearRight(View view) {
		if (mTitlebar.getVisibility() == View.GONE) {
			mTitlebar.setVisibility(View.VISIBLE);
		}
		if (view != null) {
			mNearRight.removeAllViews();
			// view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT));
			view.setOnClickListener(this);
			view.setTag(TAG_NEAR_RIGHT);
			mNearRight.addView(view);
			mNearRight.setTag(true);
		}
	}

	public View getMiddleView() {
		return mMiddle;
	}
	
	public View getNearRightView() {
		return mNearRight;
	}

	public void setBarClickListener(BarClickListener listener) {
		mBarClickListener = listener;
	}

	@Override
	public void onClick(View v) {
		int tag = (Integer) v.getTag();

		if (tag == TAG_LEFT) {
			if (!(Boolean) mLeft.getTag())
				return;

			mBarClickListener.onClickLeft();
		} else if (tag == TAG_MIDDLE) {
			if (!(Boolean) mMiddle.getTag())
				return;

			mBarClickListener.onClickMiddle();
		} else if (tag == TAG_RIGHT) {
			if (!(Boolean) mRight.getTag())
				return;

			mBarClickListener.onClickRight();
		} else if (tag == TAG_NEAR_RIGHT) {
			if (!(Boolean) mNearRight.getTag())
				return;

			mBarClickListener.onClickNearRight();
		}
	}

	public static interface BarClickListener {
		public void onClickRight();

		public void onClickLeft();

		public void onClickMiddle();

		public void onClickNearRight();
	}

}
