package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;


public class PaymentMonthMineResult {
    
    private int count;
    private ArrayList<PaymentMonthMine> item;
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public List<PaymentMonthMine> getItem() {
        return item;
    }
    
    public void addLists(List<PaymentMonthMine> lists) {
        if (lists == null || lists.size() == 0) {
            return;
        }
        
        if(item == null) {
            item = new ArrayList<PaymentMonthMine>();
        }
        
        item.addAll(lists);
    }
}