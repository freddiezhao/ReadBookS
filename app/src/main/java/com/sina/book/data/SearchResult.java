package com.sina.book.data;

import java.util.List;

/**
 * 书本搜索结果
 * 
 * @author Tsimle
 * 
 */
public class SearchResult {
    /**
     * 检索关键字
     */
    private String key;
    /**
     * 搜索结果总数
     */
    private int total;
    /**
     * 本页显示开始位置
     */
    private int start;
    /**
     * 本页显示结束位置
     */
    private int end;
    /**
     * 本页显示个数
     */
    private int realNum;

    /**
     * 检索附加字段
     */
    private String pf;
    private String ps;

    private List<Book> items;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getRealNum() {
        return realNum;
    }

    public void setRealNum(int realNum) {
        this.realNum = realNum;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public String getPs() {
        return ps;
    }

    public void setPs(String ps) {
        this.ps = ps;
    }

    public List<Book> getItems() {
        return items;
    }

    public void setItems(List<Book> items) {
        this.items = items;
    }
}
