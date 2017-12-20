package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class BookRelatedData {
	
	private ArrayList<Book> mAuthorItem;
	private ArrayList<Book> mCateItem;
	private ArrayList<String> mTag;

	public List<Book> getAuthorItem() {
		return mAuthorItem;
	}

	public void addAuthorItems(List<Book> authorItem) {
		if (authorItem == null) {
			return;
		}
		if (mAuthorItem == null) {
			mAuthorItem = new ArrayList<Book>();
		}
		mAuthorItem.addAll(authorItem);
	}

	public List<Book> getCateItems() {
		return mCateItem;
	}

	public void addCateItems(List<Book> cateItems) {
		if (cateItems == null) {
			return;
		}
		if (mCateItem == null) {
			mCateItem = new ArrayList<Book>();
		}
		mCateItem.addAll(cateItems);

	}

	public List<String> getTags() {
		return mTag;
	}

	public void addTags(List<String> tags) {
		if (tags == null) {
			return;
		}
		if (mTag == null) {
			mTag = new ArrayList<String>();
		}
		mTag.addAll(tags);
	}
	
}
