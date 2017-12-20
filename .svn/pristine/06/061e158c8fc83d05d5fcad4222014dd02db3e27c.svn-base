package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class RecomondBannerResult {
    private int total;
    private ArrayList<RecommendBannerItem> item;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<RecommendBannerItem> getItems() {
        return item;
    }

    public void addItems(List<RecommendBannerItem> items) {
    	if (items == null) {
			return;
		}
        if (item == null) {
            item = new ArrayList<RecommendBannerItem>();
        }
        item.addAll(items);
    }
}
