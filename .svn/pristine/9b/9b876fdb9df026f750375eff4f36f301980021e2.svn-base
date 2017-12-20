package com.sina.book.data.util;

import com.sina.book.data.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏书籍列表
 * 
 * @author MarkMjw
 * @date 2013-1-14
 */
public class CollectedBookList {
    private static CollectedBookList mInstance;

    private List<Book> mCollectedList = new ArrayList<Book>();
    private IListDataChangeListener mDataChangeListener;
    
    private int mTotal = 0;

    private CollectedBookList() {

    }

    public static CollectedBookList getInstance() {
        if (mInstance == null) {
            synchronized (CollectedBookList.class) {
                if (mInstance == null) {
                    mInstance = new CollectedBookList();
                }
            }
        }
        return mInstance;
    }

    /**
     * 设置数据变化监听器
     * 
     * @param listener
     */
    public void setDataChangeListener(IListDataChangeListener listener) {
        mDataChangeListener = listener;
    }

    /**
     * 获取收藏列表
     * 
     * @return
     */
    public List<Book> getCollectedList() {
        return mCollectedList;
    }

    /**
     * 添加收藏
     * 
     * @param book
     * @return
     */
    public boolean addBook(Book book) {
        boolean result = false;
        if (null != book) {
            result = mCollectedList.add(book);

            if (result) {
                notifyDataChanged();
            }
        }
        return result;
    }

    /**
     * 删除收藏
     * 
     * @param book
     * @return
     */
    public boolean deleteBook(Book book) {
        boolean result;
        result = mCollectedList.remove(book);
        if (mTotal > 0) {
            mTotal = mTotal - 1;
        }        
        
        if (result) {
            notifyDataChanged();
        }
        
        return result;
    }

    /**
     * 清空
     */
    public void cleanAndNotify() {
        mCollectedList.clear();
        mTotal = 0;
        
        notifyDataChanged();
    }

    /**
     * 清空购买记录
     */
    public void clean() {
        mCollectedList.clear();
        mTotal = 0;
    }

    /**
     * 通知数据已经更新
     */
    public void notifyDataChanged() {
        if (null != mDataChangeListener) {
            mDataChangeListener.dataChange();
        }
    }

    /**
     * 添加收藏列表
     * 
     * @param bookList
     */
    public boolean addList(List<Book> bookList) {
        boolean result = false;
        
        if (bookList != null && bookList.size() > 0) {
            result = mCollectedList.addAll(bookList);

            if (result) {
                notifyDataChanged();
            }
        }
        
        return result;
    }

    /**
     * 是否已经收藏该书
     * 
     * @param book
     * @return boolean 返回类型
     */
    public boolean hasBook(Book book) {
        return mCollectedList.contains(book);
    }

    /**
     * 是否为空
     * 
     * @return
     */
    public boolean isEmpty() {
        return mCollectedList.isEmpty();
    }

    /**
     * 列表数量
     * 
     * @return
     */
    public int size() {
        return mCollectedList.size();
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        this.mTotal = total;
    }
}
