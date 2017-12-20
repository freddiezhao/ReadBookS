package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class RecommendEpubResult {
	private int total;
	private String name;

	private ArrayList<Book> item;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<Book> getItems() {
		return item;
	}

	public void addItems(List<Book> items) {
		if (items == null) {
			return;
		}
		if (item == null) {
			item = new ArrayList<Book>();
		}
		item.addAll(items);
	}
}
