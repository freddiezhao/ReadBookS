package com.sina.book.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecommendAuthorListItem
{

	public HashMap<String, Object>	map	= new HashMap<String, Object>();
	private ArrayList<Book>			books;

	public List<Book> getItems()
	{
		return books;
	}

	public void addItem(Book book)
	{
		if (book == null) {
			return;
		}
		if (this.books == null) {
			this.books = new ArrayList<Book>();
		}
		this.books.add(book);
	}

	public void addItems(List<Book> books)
	{
		if (books == null) {
			return;
		}
		if (this.books == null) {
			this.books = new ArrayList<Book>();
		}
		this.books.addAll(books);
	}
}
