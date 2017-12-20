package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 明星作家模块数据类
 * <p/>
 * 明星作家==>人气作者
 *
 * @author chenjl
 */
public class RecommendAuthorListResult {

    public String title;
    public int code;
    public String msg;

    private int total;
    private String name;
    private ArrayList<RecommendAuthorListItem> authorLists;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<RecommendAuthorListItem> getLists() {
        return authorLists;
    }

    public void addItem(RecommendAuthorListItem listItem) {
        if (listItem == null) {
            return;
        }
        if (this.authorLists == null) {
            this.authorLists = new ArrayList<RecommendAuthorListItem>();
        }
        this.authorLists.add(listItem);
    }

    public void addItems(List<RecommendAuthorListItem> itemList) {
        if (itemList == null) {
            return;
        }
        if (this.authorLists == null) {
            this.authorLists = new ArrayList<RecommendAuthorListItem>();
        }
        this.authorLists.addAll(itemList);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
