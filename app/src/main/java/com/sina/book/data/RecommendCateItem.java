package com.sina.book.data;

import java.util.ArrayList;

public class RecommendCateItem {
    private String id;
    private String name;

    private ArrayList<Book> books = new ArrayList<Book>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void setBooks(ArrayList<Book> books) {
        this.books = books;
    }
    
    public void addBook(Book book) {
        this.books.add(book);
    }
}