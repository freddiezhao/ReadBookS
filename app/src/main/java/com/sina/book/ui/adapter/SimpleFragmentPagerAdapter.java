package com.sina.book.ui.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.sina.book.ui.view.BaseFragment;

/**
 * 简单的ViewPager的adapter，支持加入标题栏
 * 
 * @author Tsmile
 * 
 */
public class SimpleFragmentPagerAdapter extends CustomFragmentPagerAdapter {
    private ArrayList<BaseFragment> mFragmentsList;
    private ArrayList<String> mTitles;
    private int mCurPos;

    public SimpleFragmentPagerAdapter(FragmentManager fm,
            ArrayList<BaseFragment> fragments, ArrayList<String> titles) {
        this(fm, fragments);
        this.mTitles = titles;
    }

    public SimpleFragmentPagerAdapter(FragmentManager fm,
            ArrayList<BaseFragment> fragments) {
        super(fm);
        this.mFragmentsList = restore(fm, fragments);
    }
    
    public int getCurPos() {
        return mCurPos;
    }
    
    public BaseFragment getCurFragment() {
        if (mFragmentsList != null && mCurPos < mFragmentsList.size()
                && mCurPos >= 0) {
            return mFragmentsList.get(mCurPos);
        }
        return null;
    }
    
    /**
     * 如果能从FragmentManager中找到，直接取用
     * 
     * @param fm
     * @param fragments
     * @return
     */
    private ArrayList<BaseFragment> restore(FragmentManager fm,
            ArrayList<BaseFragment> fragments) {
        int count = fragments.size();
        ArrayList<BaseFragment> restoreFragments = new ArrayList<BaseFragment>(
                fragments);
        for (int i = 0; i < count; i++) {
            Fragment f = fm.findFragmentByTag(makeFragmentName(i));
            if (f != null) {
                restoreFragments.set(i, (BaseFragment) f);
            }
        }
        return restoreFragments;
    }

    public void setDefaultFragment(int position) {
        if (mFragmentsList != null) {
            mCurPos = position;
            mFragmentsList.get(position).setDefaultFragment();
        }
    }

    public void onSelected(int position) {
        if (mFragmentsList != null) {
            mCurPos = position;
            mFragmentsList.get(position).onSelected();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentsList.size();
    }

    @Override
    public Fragment getItem(int postion) {
        return mFragmentsList.get(postion);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }
}
