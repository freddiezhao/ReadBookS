package com.sina.book.data;

import java.util.ArrayList;

public class PartitionDetailTopResult {
    private ArrayList<Book> newBooks;
    private ArrayList<Book> rankBooks;
    private ArrayList<Book> freeBooks;
    private int freeBooksTotal;
    private int rankBooksTotal;
    private int newBooksTotal;

    public ArrayList<Book> getNewBooks() {
        if (newBooks != null) {
            return newBooks;
        } else {
            return new ArrayList<Book>();
        }
    }

    public void setNewBooks(ArrayList<Book> newBooks) {
        this.newBooks = newBooks;
    }

    public ArrayList<Book> getRankBooks() {
        if (rankBooks != null) {
            return rankBooks;
        } else {
            return new ArrayList<Book>();
        }
    }

    public void setRankBooks(ArrayList<Book> rankBooks) {
        this.rankBooks = rankBooks;
    }

    public ArrayList<Book> getFreeBooks() {
        if (freeBooks != null) {
            return freeBooks;
        } else {
            return new ArrayList<Book>();
        }
    }

    public void setFreeBooks(ArrayList<Book> freeBooks) {
        this.freeBooks = freeBooks;
    }

    public int getFreeBooksTotal() {
        return freeBooksTotal;
    }

    public void setFreeBooksTotal(int freeBooksTotal) {
        this.freeBooksTotal = freeBooksTotal;
    }

    public int getRankBooksTotal() {
        return rankBooksTotal;
    }

    public void setRankBooksTotal(int rankBooksTotal) {
        this.rankBooksTotal = rankBooksTotal;
    }

    public int getNewBooksTotal() {
        return newBooksTotal;
    }

    public void setNewBooksTotal(int newBooksTotal) {
        this.newBooksTotal = newBooksTotal;
    }

}
