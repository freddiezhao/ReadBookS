package com.sina.book.data;

import java.util.List;

/**
 * 用户购买的书.
 *
 * @author Li Wen
 * @date 2012-11-15
 */
public class PurchasedBook {

    /** 未导入. */
    public static int STATUS_NOT_IMPORT = 1;

    /** 正在导入. */
    public static int STATUS_IMPORTTING = 2;

    /** 已经导入. */
    public static int STATUS_IMPORTED = 3;
    
    /** 书籍id. */
    private String bookId = "";
    
    /** 书籍sid. */
    private String sId = "";
    
    /** 书籍src. */
    private String bookSrc = Book.BOOK_SRC_WEB;
    
    /** 书名称. */
    private String title = "";
    
    /** 作者. */
    private String author = "";
    
    /** 分类. */
    private String bookCate = "";
    
    /** 分类Id */
    private String bookCateId = "";
    
    /** 购买时间 */
    private String buyTime = "";

    /** 书籍封面下载地址. */
    private String imageUrl;
    
    /** 简介. */
    private String intro;

    /** 购买的章节信息. */
    private List<PurchasedChapter> purchasedChapterList;

    /** 导入状态，默认为未导入. */
    private int status = STATUS_NOT_IMPORT;

    /**
     * Gets the book id.
     *
     * @return the book id
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * Sets the book id.
     *
     * @param bookId the new book id
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the author.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author the new author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the book cate.
     *
     * @return the book cate
     */
    public String getBookCate() {
        return bookCate;
    }

    /**
     * Sets the book cate.
     *
     * @param bookCate the new book cate
     */
    public void setBookCate(String bookCate) {
        this.bookCate = bookCate;
    }

    /**
     * Gets the image url.
     *
     * @return the image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image url.
     *
     * @param imageUrl the new image url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the purchased chapter list.
     *
     * @return the purchased chapter list
     */
    public List<PurchasedChapter> getPurchasedChapterList() {
        return purchasedChapterList;
    }

    /**
     * Sets the purchased chapter list.
     *
     * @param purchasedChapterList the new purchased chapter list
     */
    public void setPurchasedChapterList(List<PurchasedChapter> purchasedChapterList) {
        this.purchasedChapterList = purchasedChapterList;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Gets the intro.
     *
     * @return the intro
     */
    public String getIntro() {
        return intro;
    }

    /**
     * Sets the intro.
     *
     * @param intro the new intro
     */
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bookId == null) ? 0 : bookId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PurchasedBook) {
            PurchasedBook other = (PurchasedBook) o;
            String otherId = other.getBookId();
            if (bookId != null && otherId != null && bookId.equalsIgnoreCase(otherId)) {
                return true;
            }
        }
        return false;
    }

    public String getSid() {
        return sId;
    }

    public void setSid(String sId) {
        this.sId = sId;
    }

    public String getBookSrc() {
        return bookSrc;
    }

    public void setBookSrc(String bookSrc) {
        this.bookSrc = bookSrc;
    }

    public String getBookCateId() {
        return bookCateId;
    }

    public void setBookCateId(String bookCateId) {
        this.bookCateId = bookCateId;
    }

    public String getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(String time) {
        this.buyTime = time;
    }

    @Override
    public String toString() {
        return "PurchasedBook [bookId=" + bookId + ", sId=" + sId + ", bookSrc=" + bookSrc
                + ", title=" + title + ", author=" + author + ", bookCate=" + bookCate
                + ", bookCateId=" + bookCateId + ", buyTime=" + buyTime + ", imageUrl=" + imageUrl
                + ", intro=" + intro + ", purchasedChapterList=" + purchasedChapterList
                + ", status=" + status + "]";
    }
}
