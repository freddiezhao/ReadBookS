package com.sina.book.data;

public class CommonRecommendItem {
    private UserInfoRec userInfoRec;
    private Book book;
    private String recommendTime;

    public UserInfoRec getUserInfoRec() {
        return userInfoRec;
    }

    public void setUserInfoRec(UserInfoRec userInfoRec) {
        this.userInfoRec = userInfoRec;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getRecommendTime() {
        return recommendTime;
    }

    public void setRecommendTime(String recommendTime) {
        this.recommendTime = recommendTime;
    }

    public CommonRecommendItem() {

    }

    public CommonRecommendItem(UserInfoRec userInfoRec, Book book, String recommendTime) {
        super();
        this.userInfoRec = userInfoRec;
        this.book = book;
        this.recommendTime = recommendTime;
    }

}