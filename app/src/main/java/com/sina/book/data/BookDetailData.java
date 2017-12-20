package com.sina.book.data;

public class BookDetailData {

	/**
	 * 书记状态：正常
	 */
	public static final int BOOK_STATE_NORMAL = 0;

	/**
	 * 书记状态：被下架
	 */
	public static final int BOOK_STATE_UNDERCARRIAGE = 34;

	private Book mBook;
	private int total;
	private int code;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public Book getBook() {
		return mBook;
	}

	public void setBook(Book mBook) {
		this.mBook = mBook;
	}
}
