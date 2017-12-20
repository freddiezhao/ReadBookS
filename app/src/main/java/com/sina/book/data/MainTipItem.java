package com.sina.book.data;

import java.util.ArrayList;

import android.content.Context;

import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.RecommendDetailListAativity;
import com.sina.book.ui.RecommendWebUrlActivity;

/**
 * 首页信息的推广
 * 
 * @author Tsimle
 * 
 */
public class MainTipItem {

    public static final int TIP_TYPE_BOOK = 1;
    public static final int TIP_TYPE_URL = 2;
    public static final int TIP_TYPE_BOOKLIST = 3;

    private String type;
    private int recommendType;
    private String url;
    private String comment;
    private ArrayList<Book> books;

    public void launch(Context context) {
        switch (recommendType) {
        case TIP_TYPE_BOOK:
            if (books.size() > 0) {
                BookDetailActivity.launch(context, books.get(0));
            }
            break;
        case TIP_TYPE_URL:
            // 跳转
            RecommendWebUrlActivity.launch(context, url, comment);
            break;
        case TIP_TYPE_BOOKLIST:
            RecommendDetailListAativity.launch(context, books, comment);
            break;
        default:
            break;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRecommendType() {
        return recommendType;
    }

    public void setRecommendType(int recommendType) {
        this.recommendType = recommendType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void setBooks(ArrayList<Book> books) {
        this.books = books;
    }

}
