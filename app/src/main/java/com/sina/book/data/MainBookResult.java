package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面返回的信息
 * 
 * @author YangZhanDong
 * 
 */
public class MainBookResult {
    private List<MainBookItem> bookItems;
    private List<Book> editorRecommends;
    private int indexOfEditor = 0;

    public List<MainBookItem> getMainBooks() {
        return bookItems;
    }

    public void addData(MainBookItem item) {
        if (null == item) {
            return;
        }

        if (null == bookItems) {
            bookItems = new ArrayList<MainBookItem>();
        }

        bookItems.add(item);
    }

    public Book getEditorRecommend(boolean change) {
        if (editorRecommends != null && editorRecommends.size() > 0) {
            if (change) {
                indexOfEditor++;
                indexOfEditor = indexOfEditor % editorRecommends.size();
            }
            return editorRecommends.get(indexOfEditor);
        }
        return null;
    }

    public void setEditorRecommend(List<Book> editorRecommends) {
        this.editorRecommends = editorRecommends;
    }

    public Book getPeopleRecommendBook() {
        if (null != bookItems) {
            MainBookItem item;
            for (int i = 0; i < bookItems.size(); i++) {
                item = bookItems.get(i);
                if (MainBookItem.TYPE_PEOPLE.equals(item.getType())) {
                    return item.getBook();
                }
            }
        }
        return null;
    }

    public UserInfoRec getPeopleRecommendUser() {
        if (null != bookItems) {
            MainBookItem item;
            for (int i = 0; i < bookItems.size(); i++) {
                item = bookItems.get(i);
                if (MainBookItem.TYPE_PEOPLE.equals(item.getType())) {
                    return item.getPeopleRecommend();
                }
            }
        }
        return null;
    }

    public String getPeopleRecommendTime() {
        if (null != bookItems) {
            MainBookItem item;
            for (int i = 0; i < bookItems.size(); i++) {
                item = bookItems.get(i);
                if (MainBookItem.TYPE_PEOPLE.equals(item.getType())) {
                    return item.getPeopleRecommendTime();
                }
            }
        }
        return null;
    }

    public void setPeopleRecommend(MainBookItem bookItem) {
        if (null != bookItems) {
            MainBookItem item;
            for (int i = 0; i < bookItems.size(); i++) {
                item = bookItems.get(i);
                if (MainBookItem.TYPE_PEOPLE.equals(item.getType())) {
                    item.setBook(bookItem.getBook());
                    item.setPeopleRecommend(bookItem.getPeopleRecommend());
                    item.setPeopleRecommendTime(bookItem.getPeopleRecommendTime());
                }
            }
        }
    }

}