package com.sina.book.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.sina.book.R;

@Deprecated
/**
 * use BaseLayout directly instead
 * @author Tsimle
 *
 */
public final class TitleBar extends FrameLayout implements OnClickListener {

    private FrameLayout mLeft;
    private FrameLayout mRight;
    private FrameLayout mMiddle;

    public static final int TAG_LEFT = 1;
    public static final int TAG_MIDDLE = 2;
    public static final int TAG_RIGHT = 3;

    private BarClickListener mBarClickListener = null;

    public TitleBar(Context context) {
        super(context);
        init();
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 设置标题栏阴影的显示
     * 
     * @param visible
     */
    public void setShadowVisible(int visible) {
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.vw_generic_title, this);
        mLeft = (FrameLayout) findViewById(R.id.fl_title_left);
        mRight = (FrameLayout) findViewById(R.id.fl_title_right);
        mMiddle = (FrameLayout) findViewById(R.id.fl_title_middle);
    }

    public void setLeft(View view) {
        if (view != null) {
            mLeft.removeAllViews();
            view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            view.setOnClickListener(this);
            mLeft.addView(view);
            mLeft.setTag(true);
        }
    }

    public void setRight(View view) {
        if (view != null) {
            mRight.removeAllViews();
            view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            view.setOnClickListener(this);
            mRight.addView(view);
            mRight.setTag(true);
        }
    }

    public void setMiddle(View view) {
        if (view != null) {
            mMiddle.removeAllViews();
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER;
            view.setLayoutParams(lp);
            view.setOnClickListener(this);
            mMiddle.addView(view);
            mMiddle.setTag(true);
        }
    }

    public void setClickTag(int index, boolean tag) {
        if (index == TAG_LEFT) {
            mLeft.setTag(tag);
        } else if (index == TAG_MIDDLE) {
            mMiddle.setTag(tag);
        } else if (index == TAG_RIGHT) {
            mRight.setTag(tag);
        }
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
        }
    }

    public static interface BarClickListener {
        public void onClickRight();

        public void onClickLeft();

        public void onClickMiddle();
    }

}
