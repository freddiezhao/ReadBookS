package com.sina.book.data.util;

import com.sina.book.data.Book;
import com.sina.book.util.StorageUtil;

import java.util.List;

/**
 * 搜索历史工具类
 * 
 * @author Tsimle
 * 
 */
public class SearchHistoryUtil {

    public static final int WORD_LIMIT = 3;

    private static SearchHistoryUtil mInstance;
    private List<String> mSearchWords;
    private List<Book> mHistoryBooks;

    private SearchHistoryUtil() {
        mSearchWords = StorageUtil.getSearchHistory();
        mHistoryBooks = StorageUtil.getBooks(StorageUtil.KEY_CACHE_BOOK_HISTORY);
    }

    public static SearchHistoryUtil getInstance() {
        if (mInstance == null) {
            synchronized (SearchHistoryUtil.class) {
                if (mInstance == null) {
                    mInstance = new SearchHistoryUtil();
                }
            }
        }
        return mInstance;
    }

    public void addSearchHistory(String searchWord) {
        if (mSearchWords.contains(searchWord)) {
            return;
        }
        if (mSearchWords.size() == WORD_LIMIT) {
            mSearchWords.remove(2);
        }
        mSearchWords.add(0, searchWord);
        StorageUtil.setSearchHistory(mSearchWords);
    }

    public List<String> getSearchWords() {
        return mSearchWords;
    }

    public void addHistoryBook(Book book) {
        if (mHistoryBooks.contains(book)) {
            return;
        }
        if (mHistoryBooks.size() == WORD_LIMIT) {
            mHistoryBooks.remove(2);
        }
        mHistoryBooks.add(0, book);
        StorageUtil.saveBooks(mHistoryBooks, StorageUtil.KEY_CACHE_BOOK_HISTORY);
    }

    public List<Book> getHistoryBooks() {
        return mHistoryBooks;
    }
}
