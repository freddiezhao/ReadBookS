package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 名人推荐书单详情
 * 
 * @author Tsimle
 * 
 */
public class FamousRecommendDetail {
    private String uid;
    private String headUrl;
    private String screenName;
    private String intro;
    private int listCount;
    private ArrayList<Book> recoBooks;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public int getListCount() {
        return listCount;
    }

    public void setListCount(int listCount) {
        this.listCount = listCount;
    }
    
    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public List<Book> getBooks() {
        return recoBooks;
    }

    public void addBooks(List<Book> books) {
        if (books == null) {
            return;
        }
        if (recoBooks == null) {
            recoBooks = new ArrayList<Book>();
        }
        recoBooks.addAll(books);
    }
}
