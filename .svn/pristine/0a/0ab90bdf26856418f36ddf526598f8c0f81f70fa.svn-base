package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class PartitionDataResult {
    private int total;
    private ArrayList<PartitionItem> item;

    private List<RecommendCate> recommendCates;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<PartitionItem> getItems() {
        return item;
    }

    public void addItems(List<PartitionItem> items) {
    	if (items == null) {
			return;
		}
        if (item == null) {
            item = new ArrayList<PartitionItem>();
        }
        item.addAll(items);
    }

    public void setRecommendCates(List<RecommendCate> recommendCates) {
        this.recommendCates = recommendCates;
    }

    public List<RecommendCate> getRecommendCates() {
        return recommendCates;
    }

    public void addRecommendCate(RecommendCate recommendCate) {
        if (null == recommendCates) {
            recommendCates = new ArrayList<RecommendCate>();
        }

        recommendCates.add(recommendCate);
    }

    public RecommendCate newRcommendCateInstance() {
        return new RecommendCate();
    }

    public class RecommendCate {
        public String type;
        public String name;

        public int total;

        public List<Book> books;
    }
}
