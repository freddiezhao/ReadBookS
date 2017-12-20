package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑推荐
 */
public class RecommendHotResult {
    private String title;
    private int total;
    private ArrayList<Book> item;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
