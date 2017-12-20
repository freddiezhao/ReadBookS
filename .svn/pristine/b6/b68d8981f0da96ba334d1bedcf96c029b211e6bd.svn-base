package com.sina.book.data;

import com.sina.book.reader.ReadStyleManager;

import java.io.Serializable;

/**
 * 书籍页数信息
 * 
 * @author MarkMjw
 * @date 2013-2-21
 */
public class BookPage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int mTotalPage = -1;
    private int mCurPage = -1;
    private float mFontSize = ReadStyleManager.DEF_FONT_SIZE_SP;

    public int getTotalPage() {
        return mTotalPage;
    }

    public void setTotalPage(int totalPage) {
        this.mTotalPage = totalPage;
    }

    public int getCurPage() {
        return mCurPage;
    }

    public void setCurPage(int curPage) {
        this.mCurPage = curPage;
    }

    public float getFontSize() {
        return mFontSize;
    }

    public void setFontSize(float fontSize) {
        this.mFontSize = fontSize;
    }

    @Override
    public String toString() {
        return "BookPage{" +
                "mTotalPage=" + mTotalPage +
                ", mCurPage=" + mCurPage +
                ", mFontSize=" + mFontSize +
                '}';
    }
}
