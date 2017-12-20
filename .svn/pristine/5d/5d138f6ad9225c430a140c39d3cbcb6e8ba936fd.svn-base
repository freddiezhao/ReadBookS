package com.sina.book.ui;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.ui.adapter.SimpleFragmentPagerAdapter;
import com.sina.book.ui.view.BaseFragment;
import com.sina.book.ui.view.BuyDetailFragment;
import com.sina.book.ui.view.RechargeDetailFragment;
import com.sina.book.ui.widget.BaseLayout;

/**
 * 消费记录activity
 * 
 * @author Tsimle
 * 
 */
public class ConsumeActivity extends BaseFragmentActivity implements BaseLayout.BarClickListener, OnPageChangeListener,
		OnCheckedChangeListener {
	protected Context mContext;
	protected BaseLayout mBaseLayout;

	protected ViewPager mPager;
	protected RadioGroup mPageIndicator;
	protected SimpleFragmentPagerAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.act_consume);
		initTitle();
		initView();
	}

	@Override
	public void setContentView(int layoutResID) {
		mBaseLayout = new BaseLayout(this, layoutResID);
		super.setContentView(mBaseLayout);
		mBaseLayout.setBarClickListener(this);
	}

	private void initTitle() {
		TextView middleV = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middleV.setText("消费记录");
		mBaseLayout.setTitleMiddle(middleV);

		View leftV = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		mBaseLayout.setTitleLeft(leftV);
	}

	private void initView() {
		mPager = (ViewPager) findViewById(R.id.pager);
		mPageIndicator = (RadioGroup) findViewById(R.id.pageIndicator);

		ArrayList<BaseFragment> pageFragments = new ArrayList<BaseFragment>();
		pageFragments.add(new BuyDetailFragment());
		pageFragments.add(new RechargeDetailFragment());
		ArrayList<String> titles = new ArrayList<String>();
		titles.add(0, getString(R.string.my_buy));
		titles.add(1, getString(R.string.my_recharge));

		mPageIndicator.check(R.id.indicator0);
		mAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), pageFragments, titles);
		mAdapter.setDefaultFragment(0);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(mAdapter.getCount());
		mPager.setOnPageChangeListener(this);
		mPageIndicator.setOnCheckedChangeListener(this);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		mAdapter.onSelected(arg0);
		switch (arg0) {
		case 0:
			mPageIndicator.check(R.id.indicator0);
			break;
		case 1:
			mPageIndicator.check(R.id.indicator1);
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.indicator0:
			if (0 != mPager.getCurrentItem()) {
				mPager.setCurrentItem(0);
			}
			break;
		case R.id.indicator1:
			if (1 != mPager.getCurrentItem()) {
				mPager.setCurrentItem(1);
			}
			break;
		}
	}

	@Override
	public void onClickRight() {

	}

	@Override
	public void onClickLeft() {
		this.finish();
	}

	@Override
	public void onClickMiddle() {

	}

	@Override
	public void onClickNearRight() {

	}
}
