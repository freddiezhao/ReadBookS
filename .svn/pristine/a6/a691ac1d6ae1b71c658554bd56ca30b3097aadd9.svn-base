package com.sina.book.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import com.sina.book.R;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.adapter.SimpleFragmentPagerAdapter;
import com.sina.book.ui.view.BaseFragment;
import com.sina.book.ui.widget.BaseLayout;
import com.sina.book.ui.widget.TitlePageIndicator;

import java.util.ArrayList;

/**
 * 使用FragmentActivity的共同基类<br>
 * FragmentActivity会帮助我们管理包含的Fragment的生命周期
 * 
 * @author Tsimle
 * 
 */
public abstract class CustomTitleFragmentActivity extends BaseFragmentActivity
        implements BaseLayout.BarClickListener {

    protected Context mContext;

    protected BaseLayout mBaseLayout;
    /**
     * ViewPager相关
     */
    protected ViewPager mPager;
    protected TitlePageIndicator mPageIndicator;
    protected SimpleFragmentPagerAdapter mAdapter;

    private boolean mReleaseOnlyStop = false;
    protected boolean mNeedStopActivity;
    private ArrayList<BaseFragment> mPageFragments;
    
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.act_base_fragment);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPageIndicator = (TitlePageIndicator) findViewById(R.id.pageIndicator);
        init(savedInstanceState);
    }
    
    @Override
    public void setContentView(int layoutResID) {
        mBaseLayout = new BaseLayout(this, layoutResID);
        super.setContentView(mBaseLayout);
        mBaseLayout.setBarClickListener(this);
    }
    
    protected void setReleaseOnlyStop(boolean releaseOnlyStop) {
        this.mReleaseOnlyStop = releaseOnlyStop;
    }

    protected abstract void init(Bundle savedInstanceState);

    @Override
    protected void onResume() {
        releaseOrLoadFragments(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 如果只在onstop中释放，直接派下去
        if (mReleaseOnlyStop) {
            mNeedStopActivity = true;
        }
        if (!mNeedStopActivity) {
            releaseOrLoadFragments(true);
        }
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        if (mNeedStopActivity) {
            releaseOrLoadFragments(true);
            mNeedStopActivity = false;
        }        
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        ImageLoader.getInstance().releaseContext(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {        
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return true;
        } else {
            BaseFragment curFragment = getCurFragment();
            if (curFragment != null) {
                // 如果容器里的当前Fragment要处理抛给其处理
                boolean handled = curFragment.onKeyDown(keyCode, event);
                if (handled) {
                    return handled;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClickRight() {
    }

    @Override
    public void onClickLeft() {
    }

    @Override
    public void onClickMiddle() {
    }
    
    @Override
    public void onClickNearRight() {
    }

    @Override
    public void startActivity(Intent intent) {
        mNeedStopActivity = true;
        if (intent != null) {
            if (intent.getBooleanExtra("willNotStop", false)) {
                mNeedStopActivity = false;
            }
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        mNeedStopActivity = true;
        if (intent != null) {
            if (intent.getBooleanExtra("willNotStop", false)) {
                mNeedStopActivity = false;
            }
        }
        super.startActivityForResult(intent, requestCode);
    }
    
    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent,
            int requestCode) {
        mNeedStopActivity = true;
        if (intent != null) {
            if (intent.getBooleanExtra("willNotStop", false)) {
                mNeedStopActivity = false;
            }
        }
        super.startActivityFromFragment(fragment, intent, requestCode);
    }
    
    @Override
    public void finish() {
        mNeedStopActivity = true;
        super.finish();
    }
    
    /**
     * 初始化viewPager和对应的indicator<br>
     * 子类必须调用
     * 
     * @param pageFragments
     * @param titles
     * @param initalPostion
     */
    public void initPager(ArrayList<BaseFragment> pageFragments,
            ArrayList<String> titles, int initalPostion) {
        initPager(pageFragments, titles, initalPostion, 0);
    }

    /**
     * 初始化viewPager和对应的indicator<br>
     * 子类必须调用
     * 
     * @param pageFragments
     * @param titles
     * @param initalPostion
     * @param linePaddingPercent
     *            滑动上方的样式参数
     */
    public void initPager(ArrayList<BaseFragment> pageFragments,
            ArrayList<String> titles, int initalPostion, int linePaddingPercent) {
        mAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(),
                pageFragments, titles);
        mAdapter.setDefaultFragment(initalPostion);

        mPager.setAdapter(mAdapter);
        if (linePaddingPercent > 0) {
            mPageIndicator.init(mPager, initalPostion, linePaddingPercent);
        } else {
            mPageIndicator.init(mPager, initalPostion);
        }
        mPageIndicator.setOnPageChangeListener(new PageViewChangeListener());
        
        mPageFragments = pageFragments;
    }
    
    public void allFragmentsResume() {
        if (mPageFragments != null) {
            for (BaseFragment f : mPageFragments) {
                f.onResume();
            }
        }
    }
    
    protected BaseFragment getCurFragment() {
        if (mAdapter != null) {
            return mAdapter.getCurFragment();
        }
        return null;
    }
    
    protected int getCurPos() {
        if (mAdapter != null) {
            return mAdapter.getCurPos();
        }
        return 0;
    }
    
    public void setTitleLeft(View view) {
        mBaseLayout.setTitleLeft(view);
    }

    public void setTitleRight(View view) {
        mBaseLayout.setTitleRight(view);
    }

    public void setTitleMiddle(View view) {
        mBaseLayout.setTitleMiddle(view);
    }

    protected void releaseOrLoadFragments(boolean release) {
        if (mPageFragments != null) {
            for (int i = 0; i < mPageFragments.size(); i++) {
                if (release) {
                    mPageFragments.get(i).onRelease();
                } else {
                    mPageFragments.get(i).onLoad();
                }
            }
        }
    }

    /**
     * 滑动改变页面监听
     * 
     * @author Tsimle
     * 
     */
    public class PageViewChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            mAdapter.onSelected(arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
