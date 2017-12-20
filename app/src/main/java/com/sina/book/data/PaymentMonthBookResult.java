package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class PaymentMonthBookResult {

	private int total;
	private ArrayList<Book> item;
	private String mSuiteName;

	public void setSuiteName(String suiteName) {
		mSuiteName = suiteName;
	}

	public String getSuiteName() {
		return mSuiteName;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<Book> getItem() {
		return item;
	}

	public void addLists(List<Book> lists) {
		if (lists == null) {
			return;
		}

		if (item == null) {
			item = new ArrayList<Book>();
		}

		item.addAll(lists);
	}
}