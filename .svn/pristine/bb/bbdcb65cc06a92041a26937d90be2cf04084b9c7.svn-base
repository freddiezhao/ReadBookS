package com.sina.book.reader;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.sina.book.SinaBookApplication;
import com.sina.book.reader.model.PageContent;
import com.sina.book.reader.model.PageContent.ParaSelection;
import com.sina.book.reader.page.PageSummary;

/**
 * 对应书本的一页纸<br>
 * 除了bitmap外，带上内容信息<br>
 * 
 * @author Tsimle
 * 
 */
public class PageBitmap {
    private Bitmap realBitmap;
    private Canvas realCanvas;

    private int readMode;
    private int readBgRes;
    private float readFontSize;

    private List<ParaSelection> originSelectionList;
    private List<PageSummary> originPageSummaries;

    private PageContent relatePage;
    private String title;

    private String bottomTime;
    private String bottomPage;
    private String bottomParsing;

    private Paint mHeaderPaint;
    private Paint mFooterPaint;

    /**
     * 阅读到的位置[滑动翻页的时候存取]
     */
    private int mReadPosScroll = -1;
    private String mReadPosContent;

    public PageBitmap(int width, int height) {
        try {
            realBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError error) {
            // gc, try again
            System.gc();
            realBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }
        realCanvas = new Canvas(realBitmap);
    }

    public boolean canUseForCache(PageContent page) {
        ReadStyleManager readStyleManager = ReadStyleManager.getInstance(SinaBookApplication.gContext);
        if (relatePage != null && page != null && readMode == readStyleManager.getReadMode()
                && readBgRes == readStyleManager.getReadBgResId()
                && readFontSize == readStyleManager.getCurReadFontSizeInSp()) {
            boolean posEquals = false;
            if (relatePage.getPageBegin() == page.getPageBegin() && relatePage.getPageEnd() == page.getPageEnd()
                    && relatePage.getPageBegin() != relatePage.getPageEnd()) {
                posEquals = true;
            }

            boolean selectionEquals = false;
            if (originSelectionList == null || originSelectionList.isEmpty()) {
                if (page.getSelectionList() == null || page.getSelectionList().isEmpty()) {
                    selectionEquals = true;
                }
            } else {
                if (originSelectionList.equals(page.getSelectionList())) {
                    selectionEquals = true;
                }
            }

            boolean summaryEquals = false;
            if (originPageSummaries == null || originPageSummaries.isEmpty()) {
                if (page.getPageSummarys() == null || page.getPageSummarys().isEmpty()) {
                    summaryEquals = true;
                }
            } else {
                if (originPageSummaries.equals(page.getPageSummarys())) {
                    summaryEquals = true;
                }
            }

            if (posEquals && selectionEquals && summaryEquals) {
                return true;
            }
        }
        return false;
    }

    public Canvas getCanvas() {
        return realCanvas;
    }

    public Bitmap getBitmap() {
        return realBitmap;
    }

    public void setRelatePage(PageContent relatePage) {
        this.relatePage = relatePage;
        originSelectionList = null;
        originPageSummaries = null;

        if (relatePage != null) {
            if (relatePage.getSelectionList() != null) {
                originSelectionList = new ArrayList<PageContent.ParaSelection>();
                originSelectionList.addAll(relatePage.getSelectionList());
            }

            if (relatePage.getPageSummarys() != null) {
                originPageSummaries = new ArrayList<PageSummary>();
                originPageSummaries.addAll(relatePage.getPageSummarys());
            }
        }
    }

    public PageContent getRelatePage() {
        return relatePage;
    }

    public void setReadBg(int readMode, int readBgRes) {
        this.readBgRes = readBgRes;
        this.readMode = readMode;
        this.readFontSize = ReadStyleManager.getInstance(SinaBookApplication.gContext).getCurReadFontSizeInSp();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBottomTime(String bottomTime) {
        this.bottomTime = bottomTime;
    }

    public void setBottomPage(String bottomPage) {
        this.bottomPage = bottomPage;
    }

    public void setBottomParsing(String bottomParsing) {
        this.bottomParsing = bottomParsing;
    }

    public int getReadPosScroll() {
        return mReadPosScroll;
    }

    public void setReadPosScroll(int readPosScroll) {
        this.mReadPosScroll = readPosScroll;
    }

    public String getReadPosContent() {
        return mReadPosContent;
    }

    public void calculateReadPosScroll(int scrollYReal) {
        if (getRelatePage() != null) {
            ScrollReadPosInfo nowPosInfo = new ScrollReadPosInfo(mReadPosScroll, mReadPosContent);
            getRelatePage().getReadPosScroll(scrollYReal, nowPosInfo);
            this.mReadPosScroll = nowPosInfo.pos;
            this.mReadPosContent = nowPosInfo.content;
        }
    }

    public void reDraw() {
        if (relatePage == null || relatePage.isEmpty()) {
            return;
        }
        ReadStyleManager readStyleManager = ReadStyleManager.getInstance(SinaBookApplication.gContext);
        mHeaderPaint = readStyleManager.getHeaderPaint();
        mFooterPaint = readStyleManager.getFooterPaint();
        relatePage.draw(getCanvas());

        float titleStartX = readStyleManager.getLeftX();
        float titleStartY = readStyleManager.getTitleStartY();
        if (title != null) {
            getCanvas().drawText(title, titleStartX, titleStartY, mHeaderPaint);
        }

        float footerStartX = readStyleManager.getLeftX();
        float footerStartY = readStyleManager.getFooterStartY();
        if (bottomTime != null) {
            getCanvas().drawText(bottomTime, footerStartX, footerStartY, mFooterPaint);
        }

        if (bottomPage != null) {
            float pageTipWidth = mFooterPaint.measureText(bottomPage);
            float pageTipStart = readStyleManager.getRightX() - pageTipWidth;
            getCanvas().drawText(bottomPage, pageTipStart, footerStartY, mFooterPaint);
        } else if (bottomParsing != null) {
            float parseTipWidth = mFooterPaint.measureText(bottomParsing);
            float parseTipStart = ((readStyleManager.getRightX() - parseTipWidth) / 2);
            getCanvas().drawText(bottomParsing, parseTipStart, footerStartY, mFooterPaint);
        }
    }

    public void release() {
        if (realBitmap != null) {
            realBitmap.recycle();
        }
        relatePage = null;
    }

    public static class ScrollReadPosInfo {
        public int pos;
        public String content;

        public ScrollReadPosInfo(int pos, String content) {
            this.pos = pos;
            this.content = content;
        }
    }
}
