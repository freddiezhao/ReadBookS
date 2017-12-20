package com.sina.book.data;

import java.util.ArrayList;

public class CommonRecommendResult {

	private int total;
	private ArrayList<CommonRecommendItem> mLists;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public ArrayList<CommonRecommendItem> getItem() {
		if (null == mLists) {
			mLists = new ArrayList<CommonRecommendItem>();
		}
		return mLists;
	}

	public void addItem(CommonRecommendItem item) {
		if (null == item) {
			return;
		}

		if (null == mLists) {
			mLists = new ArrayList<CommonRecommendItem>();
		}

		mLists.add(item);
	}

}