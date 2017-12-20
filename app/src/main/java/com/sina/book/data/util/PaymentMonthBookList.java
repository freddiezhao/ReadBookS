package com.sina.book.data.util;

import java.util.ArrayList;
import java.util.List;

import com.sina.book.data.Book;

public class PaymentMonthBookList {

	private static PaymentMonthBookList instance;

	private List<Book> mPaymentMonthBookList = new ArrayList<Book>();
	private IListDataChangeListener mDataChangeListener;

	private int mTotal = 0;

	private boolean mNetConnect = true;

	//
	private String mSuiteName;

	public void setSuiteName(String suiteName) {
		mSuiteName = suiteName;
	}

	public String getSuiteName() {
		return mSuiteName;
	}

	public PaymentMonthBookList() {

	}

	public static PaymentMonthBookList getInstance() {
		if (instance == null) {
			synchronized (PaymentMonthBookList.class) {
				if (instance == null) {
					instance = new PaymentMonthBookList();
				}
			}
		}

		return instance;
	}

	/**
	 * 设置数据变化监听器
	 * 
	 * @param listener
	 */
	public void setDataChangeListener(IListDataChangeListener listener) {
		mDataChangeListener = listener;
	}

	/**
	 * 获取包月书单列表
	 * 
	 * @return
	 */
	public List<Book> getPaymentMonthList() {
		return mPaymentMonthBookList;
	}

	/**
	 * 清空包月书单列表
	 */
	public void cleanList() {
		mPaymentMonthBookList.clear();
		mTotal = 0;
		notifyDataChanged();
	}

	/**
	 * 通知数据已经更新
	 */
	public void notifyDataChanged() {
		if (null != mDataChangeListener) {
			mDataChangeListener.dataChange();
		}
	}

	/**
	 * 添加包月书单列表
	 * 
	 * @param List
	 *            bookList
	 */
	public boolean addList(List<Book> bookLists) {
		boolean result = false;

		if (bookLists != null && bookLists.size() > 0) {
			result = mPaymentMonthBookList.addAll(bookLists);

			if (result) {
				notifyDataChanged();
			}
		}

		return result;
	}

	/**
	 * 列表数量
	 * 
	 * @return
	 */
	public int size() {
		return mPaymentMonthBookList.size();
	}

	public int getTotal() {
		return mTotal;
	}

	public void setTotal(int total) {
		this.mTotal = total;
	}

	public boolean getNetConnect() {
		return mNetConnect;
	}

	public void setNetConnect(boolean netConnect) {
		this.mNetConnect = netConnect;
	}
}