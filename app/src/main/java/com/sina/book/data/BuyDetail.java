package com.sina.book.data;

import java.util.List;

public class BuyDetail {
    private String time;
    private String title;
    private String bookId;
    private String productType;
    private int payType;
    private String detail;
    private List<BuyItem> buyItems;

    public BuyDetail(String time, String title, String detail) {
        super();
        this.time = time;
        this.title = title;
        this.detail = detail;
    }

    public BuyDetail() {
        super();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public List<BuyItem> getBuyItems() {
        return buyItems;
    }

    public void setBuyItems(List<BuyItem> buyItems) {
        this.buyItems = buyItems;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

}
