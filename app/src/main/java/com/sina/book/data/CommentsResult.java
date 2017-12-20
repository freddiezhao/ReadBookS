package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class CommentsResult {
	private int total;
	private ArrayList<CommentItem> item;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<CommentItem> getItems() {
		return item;
	}

	public void addItems(List<CommentItem> items) {
		if (items == null) {
			return;
		}
		if (item == null) {
			item = new ArrayList<CommentItem>();
		}
		item.addAll(items);
	}
}
