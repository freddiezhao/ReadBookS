package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class SellFastResult {
    private ArrayList<Book> mOperationBooks;
    private ArrayList<SellFastItem> mLists;

    public ArrayList<Book> getOperationBooks() {
        return mOperationBooks;
    }

    public void setOperationBooks(ArrayList<Book> operationBooks) {
        if (operationBooks == null) {
            return;
        }
        if (mOperationBooks == null) {
            mOperationBooks = new ArrayList<Book>();
        }
        this.mOperationBooks = operationBooks;
    }

    public List<SellFastItem> getItems() {
        return mLists;
    }

    public void setItems(ArrayList<SellFastItem> items) {
        if (items == null) {
            return;
        }

        if (mLists == null) {
            mLists = new ArrayList<SellFastItem>();
        }

        mLists.addAll(items);
    }

}