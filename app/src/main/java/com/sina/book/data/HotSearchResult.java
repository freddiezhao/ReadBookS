package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class HotSearchResult {
	private int total;
	private ArrayList<String> item;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<String> getItems() {
		return item;
	}

	public void addItems(List<String> items) {
		if (items == null) {
			return;
		}
		if (item == null) {
			item = new ArrayList<String>();
		}
		item.addAll(items);
	}
}
