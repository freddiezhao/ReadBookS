package com.sina.book.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.Util;

import java.util.ArrayList;

/**
 * 针对ViewPager使用<br>
 * 将标题栏滑动切换效果做成控件，方便使用
 * 
 * @author Tsmile
 * 
 */
public class TitlePageIndicator extends LinearLayout implements
        ViewPager.OnPageChangeListener {

    private ViewPager mPager;
    private FrameLayout mBottomLine;
    private LinearLayout mPageIndicatorContainer;
    private LinearLayout mCursorLayout;

    private ArrayList<TextView> mPageIndicator;

    private View mDivider;
    private View mTotalLayout;

    private int mCount;
    private int mCurrIndex = 0;
    private ViewPager.OnPageChangeListener mListener;

    private int mMoveOffset;
    private int mInitOffset;

    /**
     * 选择栏标题默认的颜色
     */
    private int mIndicatorDefColor;
    /**
     * 选择栏标题选中的颜色
     */
    private int mIndicatorLightColor;
    /**
     * 标题栏字体大小
     */
    private float mIndicatorTextSize;
    /**
     * 下方线条左右的padding比例
     */
    private int mLinePercent;
    /**
     * 背景颜色
     */
    private int mDefBgColor;
    /**
     * 是否支持夜间模式
     */
    private boolean mSupportNight;

    /**
     * 整个view的left和right margin
     */
    private int mTotalLeftMargin;
    private int mTotalRightMargin;

    public TitlePageIndicator(Context context) {
        super(context);
        initViews();
    }

    public TitlePageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TitleIndicator);
        mLinePercent = a.getInt(R.styleable.TitleIndicator_linePaddingPercent,
                0);
        mDefBgColor = a.getColor(R.styleable.TitleIndicator_bg_color,
                getResources().getColor(R.color.public_bg));
        mSupportNight = a.getBoolean(
                R.styleable.TitleIndicator_supportNightMode, false);
        a.recycle();
        initViews();
    }

    public void hideDivider() {
        if (mDivider.getVisibility() == View.VISIBLE) {
            mDivider.setVisibility(View.GONE);
        }
    }

    public void showDivider() {
        if (mDivider.getVisibility() == View.GONE) {
            mDivider.setVisibility(View.VISIBLE);
        }
    }

//    public void setWholeDivider() {
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) divider
//                .getLayoutParams();
//        params.leftMargin = 0;
//        params.rightMargin = 0;
//        params.width = getResources().getDisplayMetrics().widthPixels;
//        divider.setBackgroundResource(R.drawable.divider_line);
//    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.vw_title_indicator,
                this);
        mDivider = findViewById(R.id.layout_divider);
        mTotalLayout = findViewById(R.id.totalLayout);
        mTotalLayout.setBackgroundColor(mDefBgColor);
        mPageIndicatorContainer = (LinearLayout) findViewById(R.id.titleIndicator);
        mCursorLayout = (LinearLayout) findViewById(R.id.cursor_layout);
        mIndicatorTextSize = 16;
        if (mSupportNight) {
            ReadStyleManager readStyleManager = ReadStyleManager
                    .getInstance(getContext());
            mIndicatorDefColor = readStyleManager.getColorFromIdentifier(
                    getContext(), R.color.title_indicator_def);
            mIndicatorLightColor = readStyleManager.getColorFromIdentifier(
                    getContext(), R.color.title_indicator_light);

            mTotalLayout.setBackgroundColor(readStyleManager
                    .getColorFromIdentifier(getContext(),
                            R.color.book_tag_mark_bg));
        } else {
            mIndicatorDefColor = getResources().getColor(
                    R.color.title_indicator_def);
            mIndicatorLightColor = getResources().getColor(
                    R.color.title_indicator_light);
        }

        LinearLayout.LayoutParams totalLayoutParams = (LinearLayout.LayoutParams) mTotalLayout
                .getLayoutParams();
        if (totalLayoutParams != null && totalLayoutParams.leftMargin > 0) {
            mTotalLeftMargin = totalLayoutParams.leftMargin;
            mTotalRightMargin = totalLayoutParams.rightMargin;
        }
    }

    public void init(ViewPager view, int initialPosition) {
        init(view, initialPosition, 0, Util.getDisplayMetrics(getContext()).widthPixels);
    }

    public void init(ViewPager view, int initialPosition, int linePaddingPercent) {
        init(view, initialPosition, linePaddingPercent, Util.getDisplayMetrics(getContext())
                .widthPixels);
    }

    /**
     * 确保init时，viewPager已设置了adapter
     *
     * @param view
     * @param initialPosition
     * @param linePaddingPercent
     * @param width
     */
    public void init(ViewPager view, int initialPosition, int linePaddingPercent, int width) {
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        mLinePercent = linePaddingPercent;

        setViewPager(view, initialPosition);
        mCurrIndex = initialPosition;

        // init title line
        float totalWidth = width - mTotalLeftMargin - mTotalRightMargin;
        int lineWidth = (int) (totalWidth / mCount);
        int indicatorPadding = 0;

        int textWidth = mPager.getAdapter().getPageTitle(0).length() * PixelUtil.sp2px
                (mIndicatorTextSize, getContext()) + 2;
        int maxPadding = (lineWidth - textWidth) / 2;
        if (maxPadding < 0) {
            maxPadding = 0;
        }
        if (mLinePercent > 0) {
            indicatorPadding = lineWidth / mLinePercent;
            if (indicatorPadding > maxPadding) {
                indicatorPadding = maxPadding;
            }
        } else {
            indicatorPadding = maxPadding;
        }

        mBottomLine = new FrameLayout(getContext());
        // mBottomLine.setPadding(indicatorPadding, 0, indicatorPadding, 0);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.title_indicator_img);
        imageView.setScaleType(ScaleType.FIT_XY);
        mBottomLine.addView(imageView);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(lineWidth,
                LayoutParams.MATCH_PARENT);
        mMoveOffset = lineWidth;
        mInitOffset = mMoveOffset * initialPosition;
        layoutParams.leftMargin = mInitOffset;
        mCursorLayout.addView(mBottomLine, layoutParams);
        mDivider.getLayoutParams().width = lineWidth * mCount;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    public void setBackground(int resId) {
        mTotalLayout.setBackgroundResource(resId);
    }

    private void setViewPager(ViewPager view, int initialPosition) {
        if (mPager == view) {
            return;
        }
        if (mPager != null) {
            mPager.setOnPageChangeListener(null);
        }
        mPager = view;
        initFromViewPager(initialPosition);
    }

    private void initFromViewPager(int initialPosition) {
        final int count = mPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }
        mCount = count;
        mPager.setOffscreenPageLimit(mCount);
        mPager.setCurrentItem(initialPosition);

        // init title text
        mPageIndicator = new ArrayList<TextView>(mCount);
        for (int i = 0; i < mCount; i++) {
            TextView tv = new TextView(getContext());
            tv.setText(mPager.getAdapter().getPageTitle(i));
            tv.setTextSize(mIndicatorTextSize);
            if (i == initialPosition) {
                tv.setTextColor(mIndicatorLightColor);
            } else {
                tv.setTextColor(mIndicatorDefColor);
            }
            tv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);
            tv.setOnClickListener(new IndicatorClickListener(i));
            mPageIndicatorContainer.addView(tv, lp);
            mPageIndicator.add(tv);
        }

        mPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (mListener != null) {
            mListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        onPageChanged(arg0);
        if (mListener != null) {
            mListener.onPageSelected(arg0);
        }
    }

    private void onPageChanged(int to) {
        int from = mCurrIndex;
        Animation animation = new TranslateAnimation(mMoveOffset * from
                - mInitOffset, mMoveOffset * to - mInitOffset, 0, 0);
        mPageIndicator.get(from).setTextColor(mIndicatorDefColor);
        mPageIndicator.get(to).setTextColor(mIndicatorLightColor);
        animation.setFillAfter(true);
        animation.setDuration(100);
        mBottomLine.startAnimation(animation);
        mCurrIndex = to;
    }

    public class IndicatorClickListener implements View.OnClickListener {
        private int index = 0;

        public IndicatorClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }

}
