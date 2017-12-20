package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import com.sina.book.data.Book;

public abstract class ExtraListAdapter extends ListAdapter<Book>
{

	List<Object>	mExtraObjList;

	public Object getExtraItem(int position)
	{
		if (mExtraObjList != null && position >= 0 && position < mExtraObjList.size()) {
			return mExtraObjList.get(position);
		}
		return null;
	}

	public void setExtraList(List<? extends Object> datalist)
	{
		if (mExtraObjList == null) {
			mExtraObjList = new ArrayList<Object>();
		}

		if (datalist == null) {
			return;
		}

		if (mExtraObjList.size() > 0) {
			mExtraObjList.clear();
		}
		mExtraObjList.addAll(datalist);
	}

	public void addExtraList(List<? extends Object> datalist)
	{
		if (mExtraObjList == null) {
			mExtraObjList = new ArrayList<Object>();
		}
		if (datalist != null) {
			mExtraObjList.addAll(datalist);
		}
	}

}