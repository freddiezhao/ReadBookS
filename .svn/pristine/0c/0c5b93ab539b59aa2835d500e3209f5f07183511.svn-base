package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class HotWordsResult {

    private int total;
    private ArrayList<HotWord> hotWordList;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<HotWord> getItems() {
        return hotWordList;
    }

    public void addItems(List<HotWord> items) {
        if (items == null) {
            return;
        }
        if (hotWordList == null) {
            hotWordList = new ArrayList<HotWord>();
        }
        hotWordList.addAll(items);
    }
}
