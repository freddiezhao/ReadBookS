package com.sina.book.data.util;

import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.PurchasedBook;

import java.util.ArrayList;
import java.util.List;

/**
 * 购买记录列表
 * 
 * @author Li Wen
 * @date 2012-11-15
 */
public class PurchasedBookList {
    private static PurchasedBookList mInstance;
    /**
     * 记录购买记录
     */
    private List<PurchasedBook> mPurchasedList = new ArrayList<PurchasedBook>();
    private IListDataChangeListener mDataChangeListener;
    
    private int mTotal = 0;
    
    private int mPage;

    private PurchasedBookList() {

    }

    public static PurchasedBookList getInstance() {
        if (mInstance == null) {
            synchronized (PurchasedBookList.class) {
                if (mInstance == null) {
                    mInstance = new PurchasedBookList();
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
     * 通知数据已经更新
     */
    public void notifyDataChanged() {
        if (null != mDataChangeListener) {
            mDataChangeListener.dataChange();
        }
    }

    /**
     * 购买记录列表
     * 
     * @return
     */
    public List<PurchasedBook> getList() {
        return mPurchasedList;
    }

    /**
     * 添加购买记录
     * 
     * @param book
     */
    public boolean addBook(PurchasedBook book) {
        boolean result = false;

        if (book != null) {
            result = mPurchasedList.add(book);

            if (result) {
                notifyDataChanged();
            }
        }

        return result;
    }

    /**
     * 添加购买记录
     * 
     * @param book
     */

    public void addBook(Book book) {
        if (book != null) {
            addBook(book2PurchasedBook(book));
        }
    }

    /**
     * book转PurchasedBook
     * 
     * @param book
     * @return
     */
    public PurchasedBook book2PurchasedBook(Book book) {
        if (book != null) {
            PurchasedBook pBook = new PurchasedBook();
            pBook.setAuthor(book.getAuthor());
            pBook.setBookId(book.getBookId());
            pBook.setSid(book.getSid());
            pBook.setBookCate(book.getBookCate());
            pBook.setImageUrl(book.getDownloadInfo().getImageUrl());
            pBook.setTitle(book.getTitle());
            pBook.setIntro(book.getIntro());
            pBook.setBookCateId(book.getBookCateId());
            pBook.setBookSrc(book.getBookSrc());
            pBook.setBuyTime(book.getBuyInfo().getBuyTime());
            return pBook;
        }
        return null;
    }
    
    /**
     * purchasedBook转Book
     * 
     * @param pBook
     * @return
     */
    public Book purchasedBook2Book(PurchasedBook pBook) {
        if (pBook != null) {
            Book book = new Book();
            book.setAuthor(pBook.getAuthor());
            book.setBookId(pBook.getBookId());
            book.setSid(pBook.getSid());
            book.setBookCate(pBook.getBookCate());
            book.getDownloadInfo().setImageUrl(pBook.getImageUrl());
            book.setTitle(pBook.getTitle());
            book.setIntro(pBook.getIntro());
            book.setBookCateId(pBook.getBookCateId());
            book.setBookSrc(pBook.getBookSrc());
            book.getBuyInfo().setBuyTime(pBook.getBuyTime());
            return book;
        }
        return null;
    }

    /**
     * 清空购买记录
     */
    public void cleanAndNotify() {
        mPurchasedList.clear();
        mTotal = 0;
        
        notifyDataChanged();
    }

    /**
     * 清空购买记录
     */
    public void clean() {
        mPurchasedList.clear();
        mTotal = 0;
    }

    /**
     * 添加购买记录
     * 
     * @param bList
     *            <PurchasedBook>
     */
    public boolean addList(List<PurchasedBook> bList) {
        boolean result = false;

        if (bList != null && bList.size() > 0) {
            result = mPurchasedList.addAll(bList);

            if (result) {
                notifyDataChanged();
            }
        }

        return result;
    }

    /**
     * 是否已经买了该本书
     * 
     * @param book
     * @return boolean 返回类型
     */

    public boolean isBuy(Book book) {
        if (book != null) {
            return mPurchasedList.contains(book2PurchasedBook(book));
        }
        return false;
    }

    /**
     * 列表数量
     * 
     * @return
     */
    public int size() {
        return mPurchasedList.size();
    }
    
    /**
     * 是否为空
     * 
     * @return
     */
    public boolean isEmpty() {
        return mPurchasedList.isEmpty();
    }

    /**
     * 更新导入状态
     */
    public void updateImportStatus() {
        if (mPurchasedList.size() != 0) {
            ArrayList<DownBookJob> downBooksList = DownBookManager.getInstance().getAllJobs();
            for (DownBookJob downBook : downBooksList) {
                compareStatus(mPurchasedList, downBook);
            }
        }
    }

    /**
     * 通过与本地书架上的书进行比较，得到导入状态
     * 
     * @param pList
     */
    public static void initImportStatus(List<PurchasedBook> pList) {
        if (pList != null && pList.size() != 0) {
            ArrayList<DownBookJob> downBooksList = DownBookManager.getInstance().getAllJobs();
            for (DownBookJob downBook : downBooksList) {
                compareStatus(pList, downBook);
            }
        }
    }

    /**
     * 进行比较，得到导入状态
     * 
     * @param pList
     * @param downBook
     */
    public static void compareStatus(List<PurchasedBook> pList, DownBookJob downBook) {
        if (downBook != null && downBook.getBook() != null && pList != null && pList.size() != 0) {
            Book book = downBook.getBook();
            for (PurchasedBook pBook : pList) {
                Book tempBook = mInstance.purchasedBook2Book(pBook);
                if (book.equals(tempBook)) {
                    if (downBook.getState() == DownBookJob.STATE_FINISHED) {
                        pBook.setStatus(PurchasedBook.STATUS_IMPORTED);
                    } else {
                        pBook.setStatus(PurchasedBook.STATUS_IMPORTTING);
                    }
                    return;
                }
            }
        }
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int mTotal) {
        this.mTotal = mTotal;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        this.mPage = page;
    }
   
}
