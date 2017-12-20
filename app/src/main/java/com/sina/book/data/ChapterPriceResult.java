package com.sina.book.data;

public class ChapterPriceResult {
    private Chapter chapter;
    private Book book;
    private UserInfoRole userInfoRole;

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public UserInfoRole getUserInfoRole() {
        return userInfoRole;
    }

    public void setUserInfoRole(UserInfoRole userInfoRole) {
        this.userInfoRole = userInfoRole;
    }

}
