package com.sina.book.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import com.sina.book.R;
import com.sina.book.ui.adapter.ViewPagerAdapter;
import com.sina.book.util.ResourceUtil;

import java.util.List;

/**
 * 使用ViewPager装View
 *
 * @author MarkMjw
 */
public class CommonViewPager extends LinearLayout {
    protected Context mContext;

    private ViewPager mPager;

    private TitlePageIndicator mPageIndicator;

    public CommonViewPager(Context context) {
        super(context);

        init(context);
    }

    public CommonViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        View root = LayoutInflater.from(context).inflate(R.layout.vw_viewpager_layout, this);
        if (null == root) {
            throw new RuntimeException("Inflater the layout xml failed : " + ResourceUtil
                    .getNameById(R.layout.act_base_fragment));
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPageIndicator = (TitlePageIndicator) findViewById(R.id.pageIndicator);
    }

    /**
     * 初始化viewPager和对应的indicator<br>
     * 子类必须调用
     *
     * @param views
     * @param titles
     */
    public void initPager(List<View> views, List<String> titles) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(views, titles);
        mPager.setAdapter(adapter);

        final ViewTreeObserver observer = mPager.getViewTreeObserver();

        if (null != observer) {
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int width = mPager.getMeasuredWidth();

                    // 获取到宽度后，用于设置title indicator的宽度
                    mPageIndicator.init(mPager, 0, 0, width);
                    mPageIndicator.setOnPageChangeListener(new PageViewChangeListener());

                    // 防止重复调用，完成之后便remove监听器
                    observer.removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    /**
     * 设置Title的背景
     *
     * @param resId
     */
    public void setTitleBackground(int resId) {
        mPageIndicator.setBackground(resId);
    }

    /**
     * 滑动改变页面监听
     *
     * @author MarkMjw
     */
    public class PageViewChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
