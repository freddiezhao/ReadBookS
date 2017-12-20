package com.sina.book.reader;

import com.sina.book.reader.model.BookSummaryPostion;

/**
 * 翻页状态变化监听器
 * 
 * @author Tsimle
 * 
 */
public interface PageWidgetListener {

    public void onPrePage();

    public void onNextPage();

    public boolean isToolBarVisible();

    public void onToolBar();

    public void onPageTurned();
    
    public void onFirstOrLastPage(boolean isFirstPage);
    
    public void onPullStart();

    public void onPullStateDown();

    public void onPullStateUp();

    public void onPullDown();
    
    public void onPullEnd();
    
    public boolean isBookSummaryPopVisible();
    
    public void onBookSummaryPop(BookSummaryPostion bookSummaryPostion);
}
