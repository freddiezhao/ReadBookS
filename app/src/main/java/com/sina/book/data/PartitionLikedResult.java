package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

public class PartitionLikedResult {
	
    private ArrayList<PartitionLikedItem> item;

    public List<PartitionLikedItem> getItems() {
        return item;
    }

    public void setItems(List<PartitionLikedItem> items) {
    	if (items == null) {
			return;
		}
        if (item == null) {
            item = new ArrayList<PartitionLikedItem>();
        }
        item.addAll(items);
    }
}
