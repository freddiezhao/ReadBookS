package com.sina.book.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 简单的ViewPager的adapter，支持加入标题栏
 *
 * @author MakrMjw
 */
public class ViewPagerAdapter extends PagerAdapter {
    private List<View> mViews;
    private List<String> mTitles;
    private int mCurPos;

    public ViewPagerAdapter(List<View> views, List<String> titles) {
        this.mTitles = titles;
        this.mViews = views;
    }

    public int getCurPos() {
        return mCurPos;
    }

    public View getCurrentView() {
        if (mViews != null && mCurPos < mViews.size() && mCurPos >= 0) {
            return mViews.get(mCurPos);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int index, Object arg2) {
        container.removeView(mViews.get(index));

        if (mTitles.size() > index && index >= 0) {
            mTitles.remove(index);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViews.get(position), 0);
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
