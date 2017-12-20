package com.sina.book.reader.page;

import com.sina.book.data.BookSummary;

/**
 * 每页的书摘数据结构
 * 
 * @author MarkMjw
 */
public class PageSummary {
    public int start;
    public int end;
    public String content;
    // 指向书籍中的书摘对象
    public BookSummary bookSummary;

    public PageSummary(int start, int end, String content, BookSummary bookSummary) {
        this.start = start;
        this.end = end;
        this.content = content;
        this.bookSummary = bookSummary;
    }

    public BookSummary getBookSummary() {
        return bookSummary;
    }

    public void setBookSummary(BookSummary bookSummary) {
        this.bookSummary = bookSummary;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof PageSummary) {
            PageSummary pageSummary = (PageSummary) o;
            if (this == pageSummary) {
                return true;
            }

            if (this.start == pageSummary.start && this.end == pageSummary.end) {
                return true;
            }
        }
        return false;
    }
}