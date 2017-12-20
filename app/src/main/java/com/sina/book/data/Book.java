package com.sina.book.data;

import android.text.TextUtils;

import com.sina.book.SinaBookApplication;
import com.sina.book.data.util.PurchasedBookList;
import com.sina.book.db.DBService;
import com.sina.book.useraction.DeviceInfo;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.URLUtil;
import com.sina.book.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * 书籍
 */
public class Book implements Serializable {
    private static final String TAG = "Book";

    private static final long serialVersionUID = 1L;

    public static final String TMP_SUFFIX = ".tmp";
    public static final String EPUB_SUFFIX = ".epub";
    public static final String EPUB_CACHE_SUFFIX = ".ecache";
    public static final String EPUB_TMP_SUFFIX = ".etmp";
    public static final String BOOK_SUFFIX = ".dat";
    public static final String ONLINE_TMP_SUFFIX = ".oltmp";
    public static final String ONLINE_DAT_SUFFIX = ".oldat";
    public static final String TXT_SUFFIX = ".txt";
    public final static String LOCAL_PATH_IMG = "local_img/";
    public final static String SDCARD_PATH_IMG = "sdcard_img/";
    public final static String EPUB_PATH_PROTOCOL = "epub/";
    public final static String HTML_READ_PROTOCOL = "html/";

    /**
     * 书籍内容带有样式的<br>
     */
    public static final int CONTENT_TYPE_STYLED = 0x001;
    /**
     * 书籍退出时是否弹出下载提示框<br>
     */
    public static final int CONTENT_TYPE_DIALOG = 0x010;
    /**
     * 自动下载的书籍<br>
     */
    public static final int CONTENT_TYPE_AUTODOWN = 0x002;

    /**
     * 未读的新书-标签.
     */
    public static final int IS_NEW = 0;
    /**
     * 已经读过的书-标签.
     */
    public static final int HAS_READ = 1;

    /**
     * 书架上的书.
     */
    public static final int BOOK_LOCAL = 0;
    /**
     * sd卡上的书，或者微盘下载的书.
     */
    public static final int BOOK_SDCARD = 1;
    /**
     * 在线的书.
     */
    public static final int BOOK_ONLINE = 2;

    /**
     * 免费下载的书.
     */
    public static final int BOOK_TYPE_FREE = 1;
    /**
     * 全本收费的书.
     */
    public static final int BOOK_TYPE_VIP = 2;
    /**
     * 章节收费的书.
     */
    public static final int BOOK_TYPE_CHAPTER_VIP = 3;

    /**
     * Book source-新浪书城.
     */
    public static final String BOOK_SRC_READ = "read";
    /**
     * Book source-移动基地.
     */
    public static final String BOOK_SRC_CMREAD = "cmread";
    /**
     * Book source-新浪读书.
     */
    public static final String BOOK_SRC_WEB = "websina";

    /**
     * 连载完状态.
     */
    public static final String STATUS_FINISH = "连载完";
    /**
     * 连载完状态.
     */
    public static final String STATUS_FINISH_NEW = "完结";
    /**
     * 连载完状态（en）.
     */
    public static final String STATUS_FINISH_EN = "FINISH";
    /**
     * 连载中状态.
     */
    public static final String STATUS_SERIAL = "连载中";
    /**
     * 连载中状态.
     */
    public static final String STATUS_SERIAL_NEW = "连载";
    /**
     * 连载中状态.
     */
    public static final String STATUS_SERIAL_EN = "SERIES";
    /**
     * 选载状态.
     */
    public static final String STATUS_PAUSE = "选载";
    /**
     * 选载状态.
     */
    public static final String STATUS_PAUSE_EN = "PAUSE";

    /**
     * 未知状态.
     */
    public static final int STATUS_TYPE_UNKNOWN = 0x00;
    /**
     * 连载完状态.
     */
    public static final int STATUS_TYPE_FINISH = 0x01;
    /**
     * 连载中状态.
     */
    public static final int STATUS_TYPE_SERIAL = 0x02;
    /**
     * 选载状态.
     */
    public static final int STATUS_TYPE_PAUSE = 0x03;

    /**
     * 上升.
     */
    public static final String TREND_UP = "1";
    /**
     * 持平.
     */
    public static final String TREND_AVERAGE = "2";
    /**
     * 下降.
     */
    public static final String TREND_DOWN = "3";

    /**
     * 标签.
     */
    private int tag = IS_NEW;

    /**
     * 书籍内容标签.
     */
    private String contentTag = "";

    /**
     * 标题.
     */
    private String title = "";

    /**
     * 作者.
     */
    private String author = "";

    /**
     * 总章数，即连载至章数.
     */
    private int num = 1;

    /**
     * 分类.
     */
    private String bookCate = "";

    /**
     * 分类Id
     */
    private String bookCateId = "";

    /**
     * 简介.
     */
    private String intro = "";

    /**
     * 唯一key.
     */
    private String type;

    /**
     * 排行变化.
     */
    private String flag;

    /**
     * 包含的章节.
     */
    private ArrayList<Chapter> chapters;

    /**
     * 包含的书签.
     */
    private List<MarkItem> bookmarks;

    /**
     * 包含的书摘.
     */
    private List<BookSummary> bookSummaries;

    /**
     * 更新的章节数.
     */
    private int updateChaptersNum = 0;

    /**
     * 上次更新时间.
     */
    private long lastUpdateTime;

    /**
     * (index 1)数据库中的唯一标识.
     */
    private int id = -1;

    /**
     * (index 2)书籍id 【第三方数据库和新浪读书对应的图书ID】
     */
    private String bookId = "";
    /**
     * (index 2)sinaId 【新浪书城的图书ID】
     */
    private String sid = "";
    /**
     * (index 2)src 【来源,默认为 {@link Book#BOOK_SRC_WEB}】
     */
    private String bookSrc = BOOK_SRC_WEB;

    private String status;

    private String recommend_sub2;//原售价

    private String recommend_sub3;//现售价

    private String recommend_name;//

    /**
     * bag_id在bookSrc为websina时，<br>
     * 收藏列表接口会返回
     */
    private String bagId = "";

    /**
     * 赞的次数
     */
    private long praiseNum;

    /**
     * 是否被赞过
     */
    private String praiseType = "";

    /**
     * 评论次数
     */
    private long commentNum;

    /**
     * 书籍类型
     */
    private String comment;

    /**
     * 标识书籍内容的类型<br>
     */
    private int bookContentType = 0x000;

    /**
     * 书籍的包月套餐状态
     */
    private int suiteId;
    /**
     * 书籍属于的包月套餐类型
     */
    private int originSuiteId;
    /**
     * 用户是否购买了该书的包月套餐
     */
    private boolean hasBuySuite;
    /**
     * 书籍的包月套餐名字
     */
    private String suiteName;

    /**
     * 包月书单封面的url
     */
    private String suiteImageUrl;

    /**
     * 云端收藏相关
     */
    private boolean isOnlineBook;
    private String ownerUid;
    private int onlineReadChapterId;
    /**
     * 临时变量，顺序
     **/
    private int index;
    /**
     * 服务器端更新该书的时间
     */
    private String updateTimeServer = "";
    /**
     * 服务器端更新该书的最新章节
     */
    private String updateChapterNameServer = "";

    /**
     * 书籍被推荐的时间
     */
    private String recommendTime = "";

    /**
     * 编辑推荐理由
     */
    private String recommendIntro = "";

    /**
     * 书籍是否下架(true：下架， false：未下架)
     */
    private boolean is_forbidden = false;

    /**
     * 是否epub资源
     */
    private boolean isEpub = false;

    /**
     * 是否html阅读
     */
    private boolean isHtmlRead = false;

    /**
     * 和阅读字段
     */
    private boolean isCmreadBook = false;
    private boolean isCmreadBookAndNeedShow = false;

    public boolean isCmreadBook() {
        return isCmreadBook;
    }

    public void setCmreadBook(boolean isCmreadBook) {
        this.isCmreadBook = isCmreadBook;
    }

    public boolean isCmreadBookAndNeedShow() {
        return isCmreadBookAndNeedShow;
    }

    public void setCmreadBookAndNeedShow(boolean isCmreadBookAndNeedShow) {
        this.isCmreadBookAndNeedShow = isCmreadBookAndNeedShow;
    }

    private BookReadInfo mReadInfo = new BookReadInfo();
    private BookDownloadInfo mDownloadInfo = new BookDownloadInfo(this);
    private BookBuyInfo mBuyInfo = new BookBuyInfo();
    private BookPage mBookPage = new BookPage();
    private Chapter mUpdateChapterInfo = new Chapter();
    private BookPicType mPicType = new BookPicType();

    public void parsePosFromJson() {
        mReadInfo.parsePosFromJson(this);

        if (bookmarks == null || bookmarks.isEmpty()) {
            // 书持久化到了db，也许数据库中有书签
            // if (getId() > 0) {
            bookmarks = DBService.getAllBookMark(this);
            // }
        }

        if (bookmarks != null) {
            ArrayList<MarkItem> needDelMarks = new ArrayList<MarkItem>();
            for (MarkItem bookmark : bookmarks) {
                boolean successMark = bookmark.parsePosFromJson(this, false);
                if (!successMark) {
                    needDelMarks.add(bookmark);
                }
            }

            bookmarks.removeAll(needDelMarks);
        }

        if (bookSummaries == null || bookSummaries.isEmpty()) {
            // if (getId() > 0) {
            bookSummaries = DBService.getAllBookSummary(this);
            // }
        }

        if (bookSummaries != null) {
            ArrayList<BookSummary> needDelSummarys = new ArrayList<BookSummary>();
            for (BookSummary booksummary : bookSummaries) {
                boolean successSummary = booksummary.parsePosFromJson(this);
                if (!successSummary) {
                    needDelSummarys.add(booksummary);
                }
            }

            bookSummaries.removeAll(needDelSummarys);
        }

    }

    private boolean isRemind = true;

    public boolean isRemind() {
        return isRemind;
    }

    public void setRemind(boolean remind) {
        isRemind = remind;
    }

    public boolean isAutoDownBook() {
        return (bookContentType & CONTENT_TYPE_AUTODOWN) == CONTENT_TYPE_AUTODOWN;
    }

    public void setAutoDownBook(boolean isAutoDownBook) {
        setContentTypeFlags(isAutoDownBook ? CONTENT_TYPE_AUTODOWN : 0, CONTENT_TYPE_AUTODOWN);
    }

    public void exchangeBookContentType(Book book) {
        if (null != book) {
            setBookContentType(book.getBookContentType());
        }
    }

    public void checkAutoDownBook() {
        int payType = getBuyInfo().getPayType();
        if (payType == BOOK_TYPE_VIP) {
            if (!getBuyInfo().isHasBuy() || !PurchasedBookList.getInstance().isBuy(this)) {
                setAutoDownBook(true);
            }
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 该本书籍是否属于某套餐
     *
     * @return
     */
    public boolean isSuite() {
        return suiteId > 0;
    }

    public int getOnlineReadChapterId(String log) {
        // LogUtil.d("OnlineChapterIdRecordError",
        // "Book >> getOnlineReadChapterId >> " + log + " {onlineReadChapterId="
        // + onlineReadChapterId + "}");
        // if (onlineReadChapterId <= 100) {
        // LogUtil.e("OnlineChapterIdRecordError",
        // "Book >> getOnlineReadChapterId >> " + log
        // + " {onlineReadChapterId=" + onlineReadChapterId + "}");
        // }
        return onlineReadChapterId;
    }

    public void setOnlineReadChapterId(int onlineReadChapterId, String log) {
        this.onlineReadChapterId = onlineReadChapterId;
        // LogUtil.d("OnlineChapterIdRecordError",
        // "Book >> setOnlineReadChapterId >> " + log + " {onlineReadChapterId="
        // + onlineReadChapterId + "}");
        // if (onlineReadChapterId <= 100) {
        // LogUtil.e("OnlineChapterIdRecordError",
        // "Book >> setOnlineReadChapterId >> " + log
        // + " {onlineReadChapterId=" + onlineReadChapterId + "}");
        // }
    }

    public boolean isOnlineBook() {
        return isOnlineBook;
    }

    public void setOnlineBook(boolean isOnlineBook) {
        this.isOnlineBook = isOnlineBook;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    /**
     * 用户是否购买了该书的包月套餐
     */
    public boolean hasBuySuite() {
        return hasBuySuite;
    }

    public void setHasBuySuite(boolean hasBuySuite) {
        this.hasBuySuite = hasBuySuite;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public int getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(int suiteId) {
        this.suiteId = suiteId;
    }

    public int getOriginSuiteId() {
        return originSuiteId;
    }

    public void setOriginSuiteId(int originSuiteId) {
        this.originSuiteId = originSuiteId;
    }

    public String getSuiteImageUrl() {
        return suiteImageUrl;
    }

    public void setSuiteImageUrl(String suiteImageUrl) {
        this.suiteImageUrl = suiteImageUrl;
    }

    // 书籍是否已下架
    public boolean isForbidden() {
        return is_forbidden;
    }

    public void setIsForbidden(boolean is_forbidden) {
        this.is_forbidden = is_forbidden;
    }

    // 书籍是否Epub资源
    public boolean isEpub() {
        return isEpub;
    }

    public void setIsEpub(boolean isEpub) {
        this.isEpub = isEpub;
        if (isEpub) {
            // 直接设置BookDownloadInfo的mVdiskDownUrl
            getDownloadInfo().setVDiskDownUrl(Book.EPUB_PATH_PROTOCOL);
        }
    }

    public void setIsEpubOnly(boolean isEpub) {
        this.isEpub = isEpub;
    }

    // 书籍是否Epub资源
    public boolean isHtmlRead() {
        return isHtmlRead;
    }

    public void setIsHtmlRead(boolean isHtmlRead) {
        this.isHtmlRead = isHtmlRead;
        if (isHtmlRead) {
            getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
        }
    }

    public int getBookContentType() {
        return bookContentType;
    }

    public void setBookContentType(int bookContentType) {
        if (bookContentType != 0) {
            this.bookContentType = bookContentType;
        }
    }

    /**
     * 是否是含有样式标签的书籍
     *
     * @return
     */
    public boolean isStyledBook() {
        return (bookContentType & CONTENT_TYPE_STYLED) == CONTENT_TYPE_STYLED;
    }

    public void setStyledBook(boolean styled) {
        setContentTypeFlags(styled ? CONTENT_TYPE_STYLED : 0, CONTENT_TYPE_STYLED);
    }

    /**
     * 退出时是否需要弹下载框
     *
     * @return
     */
    public boolean isDownloadDialogNotPop() {
        return (bookContentType & CONTENT_TYPE_DIALOG) == CONTENT_TYPE_DIALOG;
    }

    public void setDownloadDialogNotPop(boolean isPop) {
        setContentTypeFlags(isPop ? CONTENT_TYPE_DIALOG : 0, CONTENT_TYPE_DIALOG);
    }

    public long getPraiseNum() {
        return praiseNum;
    }

    public void setPraiseNum(long praiseNum) {
        this.praiseNum = praiseNum;
    }

    public long getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(long commentNum) {
        this.commentNum = commentNum;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;

        if (HAS_READ == tag) {
            System.out.print("去除新书标识");
        }
    }

    public String getContentTag() {
        return contentTag;
    }

    public void setContentTag(String contentTag) {
        this.contentTag = contentTag;
    }

    public String getBookCate() {
        return bookCate;
    }

    public void setBookCate(String bookCate) {
        this.bookCate = bookCate;
    }

    public int getUpdateChaptersNum() {
        return updateChaptersNum;
    }

    public void setUpdateChaptersNum(int updateChaptersNum) {
        this.updateChaptersNum = updateChaptersNum;
    }

    public Chapter getLastChapter() {
        return DBService.getLastChapter(this);
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBagId() {
        return bagId;
    }

    public void setBagId(String bagId) {
        this.bagId = bagId;
    }

    public String getBookCateId() {
        return bookCateId;
    }

    public void setBookCateId(String bookCateId) {
        this.bookCateId = bookCateId;
    }

    public ArrayList<Chapter> getChapters() {
        return null == chapters ? new ArrayList<Chapter>() : chapters;
    }

    public Chapter getChapterByPosition(int index) {
        ArrayList<Chapter> lists = getChapters();
        if (index >= 0 && index < lists.size()) {
            return lists.get(index);
        }
        return null;
    }

    public void setChapters(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
    }

    public void setChapters(ArrayList<Chapter> chapters, String log) {
        // LogUtil.d("BugBugFuckU_BOOK",
        // "===========================================================================");
        // if (chapters != null && chapters.size() > 0) {
        // LogUtil.d("BugBugFuckU_BOOK", "Book >> setChapters >> " + log +
        // " >> {chapter size=" + chapters.size()
        // + "}");
        // boolean isAllOkay = true;
        // for (int i = 0; i < chapters.size(); i++) {
        // Chapter chapter = chapters.get(i);
        // if (chapter != null) {
        // if (chapter.getStartPos() == 0 && chapter.getLength() == 0) {
        // LogUtil.d("BugBugFuckU_BOOK", "Book >> setChapters >> " + log +
        // " >> {chapter=" + chapter + "}");
        // isAllOkay = false;
        // break;
        // } else {
        // LogUtil.i("BugBugFuckU_BOOK", "Book >> setChapters >> " + log +
        // " >> {chapter=" + chapter + "}");
        // }
        // }
        // }
        // LogUtil.d("BugBugFuckU_BOOK", "Book >> setChapters >> " + log +
        // " >> {isAllOkay=" + isAllOkay + "}");
        // } else {
        // LogUtil.d("BugBugFuckU_BOOK", "Book >> setChapters >> " + log +
        // " >> {chapter size is null or size = 0}");
        // }
        // LogUtil.d("BugBugFuckU_BOOK",
        // "===========================================================================");
        this.chapters = chapters;
    }

    public void setBookmarks(List<MarkItem> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public void fetchBookmarks(List<MarkItem> bookmarks) {
        if (bookmarks == null || bookmarks.size() <= 0) {
            return;
        }

        if (this.bookmarks != null && this.bookmarks.size() > 0) {
            for (int i = 0; i < bookmarks.size(); ++i) {
                MarkItem item = bookmarks.get(i);
                for (int j = 0; j < this.bookmarks.size(); ++j) {
                    MarkItem tmp_item = this.bookmarks.get(j);
                    if (item != null && tmp_item != null) {
                        if (tmp_item.getMarkJsonString() != null && tmp_item.getMarkJsonString().equals(item.getMarkJsonString())) {
                            continue;
                        } else {
                            this.bookmarks.add(item);
                        }
                    }
                }
            }
        } else {
            this.bookmarks = bookmarks;
        }
    }

    public void fetchBook(Book book) {
        setId(book.getId());
        getDownloadInfo().setFilePath(book.getDownloadInfo().getFilePath());
        getDownloadInfo().setFileSize(book.getDownloadInfo().getFileSize());
        getDownloadInfo().setProgress(book.getDownloadInfo().getProgress());
        getDownloadInfo().setDownLoadState(book.getDownloadInfo().getDownLoadState());
        getReadInfo().setLastPos(book.getReadInfo().getLastPos());
        getDownloadInfo().setOriginalFilePath(book.getDownloadInfo().getOriginalFilePath());
        getReadInfo().setLastReadPercent(book.getReadInfo().getLastReadPercent());
        setBookContentType(book.getBookContentType());
        setSuiteId(book.getSuiteId());
        getBookPage().setFontSize(book.getBookPage().getFontSize());
        getBookPage().setTotalPage(book.getBookPage().getTotalPage());
        // getBookPage().setCurPage(book.getBookPage().getCurPage());
    }

    public List<MarkItem> getBookmarks() {
        return null == bookmarks ? bookmarks = new LinkedList<MarkItem>() : bookmarks;
    }

    public void addBookmark(MarkItem item) {
        if (null != item) {
            LinkedList<MarkItem> list = (LinkedList<MarkItem>) getBookmarks();
            list.addFirst(item);
        }
    }

    public void setBookSummaries(List<BookSummary> bookSummaries) {
        this.bookSummaries = bookSummaries;
    }

    public List<BookSummary> getBookSummaries() {
        return null == bookSummaries ? bookSummaries = new LinkedList<BookSummary>() : bookSummaries;
    }

    public void addBookSummary(BookSummary summary) {
        if (null != summary) {
            LinkedList<BookSummary> list = (LinkedList<BookSummary>) getBookSummaries();
            list.addFirst(summary);
        }
    }

    public void clearAllBookMarks() {
        bookmarks.clear();
    }

    public void clearAllBookSummaries() {
        bookSummaries.clear();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public void setIntroRealNeed(String intro) {
        this.intro = intro;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getBookSrc() {
        return bookSrc;
    }

    public void setBookSrc(String bookSrc) {
        this.bookSrc = bookSrc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int describeContents() {
        return 0;
    }

    public String getPraiseType() {
        return praiseType;
    }

    public boolean hasPraised() {
        return "Y".equalsIgnoreCase(praiseType);
    }

    public void setPraiseType(String praiseType) {
        this.praiseType = praiseType;
    }

    public BookReadInfo getReadInfo() {
        return mReadInfo;
    }

    public void setReadInfo(BookReadInfo readInfo) {
        this.mReadInfo = readInfo;
    }

    public BookDownloadInfo getDownloadInfo() {
        return mDownloadInfo;
    }

    public BookBuyInfo getBuyInfo() {
        return mBuyInfo;
    }

    public BookPage getBookPage() {
        return mBookPage;
    }

    public Chapter getBookUpdateChapterInfo() {
        if (mUpdateChapterInfo != null && chapters != null && chapters.size() > 0) {
            int size = chapters.size();
            for (int i = size - 1; i >= 0; --i) {
                Chapter cp = chapters.get(i);
                if (cp.getGlobalId() == mUpdateChapterInfo.getGlobalId()) {
                    mUpdateChapterInfo.setChapterId(cp.getChapterId());
                }
            }
        }
        return mUpdateChapterInfo;
    }

    public void setBookUpdateChapterInfo(Chapter updateChapterInfo) {
        this.mUpdateChapterInfo = updateChapterInfo;
    }

    public BookPicType getPicType() {
        return mPicType;
    }

    public void setPicType(BookPicType picType) {
        this.mPicType = picType;
    }

    /**
     * 默认构造函数.
     */
    public Book() {

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bookId == null) ? 0 : bookId.hashCode());
        result = prime * result + ((sid == null) ? 0 : sid.hashCode());
        result = prime * result + ((mDownloadInfo.getOriginalFilePath() == null) ? 0 : mDownloadInfo.getOriginalFilePath().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Book) {
            Book book = (Book) o;
            if (this == book) {
                return true;
            }
            // 新浪书城的书判断
            if (this.isOurServerBook() && book.isOurServerBook()) {
                // bookid不为空，使用bookid判断
                if (!TextUtils.isEmpty(this.bookId)) {
                    return this.bookId.equals(book.getBookId());
                    // sid不为空，使用sid判断
                } else {
                    return this.sid.equals(book.getSid());
                }
                // 微盘的书判断
            } else if (!TextUtils.isEmpty(book.getDownloadInfo().getVDiskFilePath()) && !TextUtils.isEmpty(mDownloadInfo.getVDiskFilePath())) {
                // 同时需要判断book保存的uid是否相同，以处理多用户的情况
                if (book.getDownloadInfo().getVDiskFilePath().equals(mDownloadInfo.getVDiskFilePath())) {
                    if (!TextUtils.isEmpty(book.getBuyInfo().getUid()) && !TextUtils.isEmpty(mBuyInfo.getUid()) && book.getBuyInfo().getUid().equals(mBuyInfo.getUid())) {
                        return true;
                    }
                }
                return false;
            } else {
                return (book.getDownloadInfo().getOriginalFilePath().equals(mDownloadInfo.getOriginalFilePath()));
            }
        }
        return false;
    }

    /**
     * 得到服务器的书籍的唯一标识，通过bookId和sid组装
     *
     * @return
     */
    public String getUniqueIdentify() {
        return bookId + sid + bookSrc;
    }

    /**
     * 判断是否是一本我们服务器的书<br>
     * 通过bookId和sinaBookId判断
     *
     * @return
     */
    public boolean isOurServerBook() {
        return !TextUtils.isEmpty(bookId) || !TextUtils.isEmpty(sid);
    }

    /**
     * 判断是否是一本我们服务器上的epub资源的书<br>
     * 通过VDiskDownUrl不为空并且是以Book.EPUB_PATH_PROTOCOL开头来判定
     *
     * @return
     */
    public boolean isOurServerEpubBook() {
        if (mDownloadInfo != null) {
            String vdiskURL = mDownloadInfo.getVDiskDownUrl();
            return !TextUtils.isEmpty(vdiskURL) && vdiskURL.startsWith(Book.EPUB_PATH_PROTOCOL);
        }
        return false;
    }

    /**
     * 是否是连载中
     *
     * @return
     */
    public boolean isSeriesBook() {
        String statusInfo = getBuyInfo().getStatusInfo();
        String statusFlag = getBuyInfo().getStatusFlag();

        return statusInfo.contains(STATUS_SERIAL) || STATUS_SERIAL_NEW.equals(statusInfo) || statusInfo.contains(STATUS_SERIAL_EN) || statusFlag.equalsIgnoreCase(STATUS_SERIAL_EN);
    }

    /**
     * 连载状态
     *
     * @return
     */
    public int getStatusType() {
        String statusInfo = getBuyInfo().getStatusInfo();
        String statusFlag = getBuyInfo().getStatusFlag();

        if (statusInfo.contains(STATUS_SERIAL) || STATUS_SERIAL_NEW.equals(statusInfo) || statusInfo.contains(STATUS_SERIAL_EN) || statusFlag.equalsIgnoreCase(STATUS_SERIAL_EN)) {
            return STATUS_TYPE_SERIAL;

        } else if (statusInfo.contains(STATUS_FINISH) || STATUS_FINISH_NEW.equals(statusInfo) || statusInfo.contains(STATUS_FINISH_EN) || statusFlag.equalsIgnoreCase(STATUS_FINISH_EN)) {
            return STATUS_TYPE_FINISH;

        } else if (statusInfo.contains(STATUS_PAUSE) || statusInfo.contains(STATUS_PAUSE_EN) || statusFlag.equalsIgnoreCase(STATUS_PAUSE_EN)) {
            return STATUS_TYPE_PAUSE;

        } else {
            return STATUS_TYPE_UNKNOWN;
        }
    }

    /**
     * 是否是通过微盘下载的书籍
     *
     * @return
     */
    public boolean isVDiskBook() {
        String url = mDownloadInfo.getVDiskDownUrl();
        return !TextUtils.isEmpty(url) && !Book.EPUB_PATH_PROTOCOL.equals(url) && !Book.HTML_READ_PROTOCOL.equals(url);
    }

    /**
     * 判断是否在线书籍（即加了密的书籍）.
     *
     * @return true, if is online book
     */
    public boolean isEncryptedBook() {
        boolean flag;

        // TODO:sb才根据图片地址判断是否下载的加密书籍
        String imageUrl = mDownloadInfo.getImageUrl();
        String originalFilePath = mDownloadInfo.getOriginalFilePath().toLowerCase(Locale.CHINA);
        String vdiskUrl = mDownloadInfo.getVDiskDownUrl();

        if ((bookId != null && bookId.length() > 0) || (sid != null && sid.length() > 0)) {
            flag = true;
        } else {
            if (TextUtils.isEmpty(imageUrl)) {
                flag = false;
            } else {
                // Epub的书，由于有封面，且image url不为local和sd card path，所以也做判断
                // 微盘下载的书由于不需要加密，这里也要判断
                flag = (!imageUrl.startsWith(LOCAL_PATH_IMG) && !imageUrl.startsWith(SDCARD_PATH_IMG) && !originalFilePath.endsWith(EPUB_SUFFIX) && TextUtils.isEmpty(vdiskUrl));
            }
        }
        return flag;
    }

    /**
     * 是否是本地导入
     *
     * @return
     */
    public boolean isLocalImportBook() {
        String originalFilePath = getDownloadInfo().getOriginalFilePath();

        if (!TextUtils.isEmpty(originalFilePath)) {
            return originalFilePath.endsWith(TXT_SUFFIX) || (originalFilePath.endsWith(EPUB_SUFFIX) && !isEpub());
        }

        return false;
    }

    /**
     * 是否是章节只有一章的书籍.
     *
     * @return true, if the book has single chapter.
     */
    public boolean isSingleChapterBook() {
        boolean result = false;
        if (chapters != null && chapters.size() == 1) {
            result = true;
        }
        return result;
    }

    /**
     * 是否有章节
     *
     * @return
     */
    public boolean hasChapters() {
        parseChapters();
        return null != chapters && !chapters.isEmpty();
    }

    /**
     * 从数据库查出章节信息.
     *
     * @return true, if successful
     */
    public boolean parseChapters() {
        if (chapters == null || chapters.isEmpty()) {
            chapters = DBService.getAllChapter(this);
            // LogUtil.e("BugID=21356", "Book >> parseChapters");
            if (chapters != null && !chapters.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the current chapter.
     *
     * @param postion the postion
     * @return the current chapter
     */
    public Chapter getCurrentChapter(long postion) {
        if (chapters != null && chapters.size() > 0) {
            // LogUtil.i("BugID=21356",
            // "Book >> getCurrentChapter >> {chapter size =" + chapters.size()
            // + "}， postion="
            // + postion);
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                // LogUtil.i("BugID=21356",
                // "Book >> getCurrentChapter >> {chapter=" + chapter + "}");

                if (chapter.getStartPos() <= postion && chapter.getStartPos() + chapter.getLength() > postion) {
                    // LogUtil.e("BugID=21356",
                    // "Book >> getCurrentChapter >> 匹配上 {chapter=" + chapter +
                    // "}");
                    return chapter;
                }
            }
            // LogUtil.i("BugID=21356",
            // "Don't find the chapter by the given position : " + postion +
            // ".");
            LogUtil.e(TAG, "Don't find the chapter by the given position : " + postion + ".");
            return null;
        } else {
            // LogUtil.i("BugID=21356",
            // "Book >> getCurrentChapter >> {chapter == null or size = 0}");
            LogUtil.e(TAG, "The book's chapters is null or the chapters' size is less 0.");
            return null;
        }
    }

    /**
     * Gets the current chapter.
     *
     * @param gloabalId the gloabal id
     * @return the chapter.
     */
    public Chapter getCurrentChapterById(long gloabalId) {
        if (chapters != null && chapters.size() > 0) {
            for (Chapter chapter : chapters) {
                if (gloabalId == chapter.getGlobalId()) {
                    return chapter;
                }
            }
        }
        return null;
    }

    /**
     * Get chapter from the chapter list.
     *
     * @param chapter
     * @return
     */
    public Chapter getChapter(Chapter chapter) {
        if (chapter != null && chapters != null && chapters.size() > 0) {
            for (int i = 0; i < chapters.size(); i++) {
                if (chapter.equals(chapters.get(i))) {
                    chapter = chapters.get(i);
                    break;
                }
            }
        }
        return chapter;
    }

    /**
     * Gets the next chapter.
     *
     * @param chapter the chapter
     * @return the next chapter.
     */
    public Chapter getNextChapter(Chapter chapter) {
        if (chapter != null && chapters != null && chapters.size() > 0) {
            int chapterSize = chapters.size();
            if (chapterSize > chapter.getChapterId()) {
                return chapters.get(chapter.getChapterId());
            }
        }
        return null;
    }

    /**
     * Gets the next chapter.
     *
     * @param chapter the current chapter.
     * @return the pre chapter.
     */
    public Chapter getPreChapter(Chapter chapter) {
        if (chapter != null && chapters != null && chapters.size() > 0) {
            int chapterSize = chapters.size();
            if (chapterSize >= chapter.getChapterId() && chapter.getChapterId() > 1) {
                return chapters.get(chapter.getChapterId() - 2);
            }
        }
        return null;
    }

    /**
     * Gets the current chapter index.
     *
     * @param postion the postion
     * @return the current chapter index
     */
    public int getCurrentChapterIndex(int postion) {
        if (chapters != null && chapters.size() > 0) {
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                if (chapter.getStartPos() <= postion && chapter.getStartPos() + chapter.getLength() > postion) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 清除所有章节的新标签，一般在更新章节的时候调用，此方法不会去更新数据库.
     */
    public void clearAllChapterNewTag() {
        if (chapters != null && chapters.size() > 0) {
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                chapter.setTag(Chapter.NORMAL);
            }
        }
    }

    /**
     * 查找当前位置是否有书签.
     *
     * @param postion the postion
     * @return the current mark
     */
    public MarkItem getCurrentMark(int postion) {
        if (bookmarks != null) {
            for (MarkItem mark : bookmarks) {
                // TODO:
                // boolean successMark = mark.parsePosFromJson(this);
                // if (successMark) {
                //
                // }

                // String strCpid = mark.getChapterId();
                // long begin = 0l;
                // try {
                // int chapterid = Integer.parseInt(strCpid);
                // if (chapters.size() > 0) {
                // for (int i = 0; i < chapters.size(); ++i) {
                // Chapter cp = chapters.get(i);
                // if (cp.getChapterId() == chapterid) {
                // begin = cp.getStartPos() + mark.getBegin();
                // }
                // }
                // }
                // } catch (Exception e) {
                // Log.e("ouyang", "e: " + e);
                // }
                // if (begin == 0) {
                // begin = mark.getBegin();
                // } else {
                // mark.setBegin(begin);
                // }
                //
                // if (begin == postion) {
                // return mark;
                // }
                if (mark.getBegin() == postion) {
                    return mark;
                }
            }
        }
        return null;
    }

    /**
     * 下载使用的url.
     *
     * @return the down url
     */
    public String getDownUrl() {
        StringBuilder sb = new StringBuilder(ConstantData.URL_COMMON_DOWN);

        Chapter chapter = DBService.getLastChapter(this);
        int totalNum = num - chapter.getChapterId() + 1;
        if ((num < 2) && (getChapters().isEmpty())) {
            totalNum = Integer.MAX_VALUE;
        }
        // TODO
        // totalNum = num;
        // int totalNum = Integer.MAX_VALUE;

        sb.append("?").append(ConstantData.AUTH_CODE_GET);
        sb.append("&format=xml");
        sb.append("&bid=").append(bookId);
        sb.append("&src=").append(bookSrc);
        sb.append("&sid=").append(sid);
        sb.append("&num=").append(totalNum);

        // 这里没有判断是否已经登陆，统统加入access_token
        if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS && !isAutoDownBook()) {
            int payType = mBuyInfo.getPayType();

            // 1.type为suite为包月下载,当s_num不为空则为断章包月下载;
            // 2.type为single进行单本下载流程；
            // 3.其他的走免费章节下载
            if (isSuite()) {
                sb.append("&type=suite");
                sb.append("&s_num=").append("");
                sb.append("&access_token=").append(LoginUtil.getLoginInfo().getAccessToken());

                StringBuilder signFormat = new StringBuilder();
                signFormat.append(LoginUtil.getLoginInfo().getAccessToken());
                signFormat.append("|").append(bookId);
                signFormat.append("|").append(ConstantData.AUTO_KEY);

                String sign = URLUtil.md5Signature(signFormat.toString());
                sb.append("&sign=").append(sign);

            } else if (payType != BOOK_TYPE_CHAPTER_VIP) {
                // 章节收费的书籍加入书架不能传登录信息，只下载免费的部分
                sb.append("&type=single");
                sb.append("&s_num=").append(chapter.getChapterId() + 1);

                // 若无余额，不拼接access_token,以游客身份下载免费部分
                // TODO CJL getBuyInfo().getBuyType() == 1
                // 这个判断不大准确，进入到这里的时候没有被准确的赋值
                // if (getBuyInfo().isHasBuy() && getBuyInfo().getBuyType() ==
                // 1) {
                boolean isAppendedAccessToken = false;
                if (getBuyInfo().isHasBuy() && (getBuyInfo().getBuyType() == 1 || getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP || getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP)) {
                    isAppendedAccessToken = true;
                    sb.append("&access_token=").append(LoginUtil.getLoginInfo().getAccessToken());
                } else {
                    String balance = LoginUtil.getLoginInfo().getBalance();
                    if (!Util.isNullOrEmpty(balance) && !balance.equals("0")) {
                        // 余额不为0，拼接
                        isAppendedAccessToken = true;
                        sb.append("&access_token=").append(LoginUtil.getLoginInfo().getAccessToken());
                    }
                }

                // CJL 增加对使用赠书卡逻辑的判断
                PriceTip priceTip = getBuyInfo().getPriceTip();
                if (priceTip != null && priceTip.getBuyType() == PriceTip.TYPE_DISCOUNT_EIGHT && getBuyInfo().isHasBuy() && !isAppendedAccessToken) {
                    sb.append("&access_token=").append(LoginUtil.getLoginInfo().getAccessToken());
                }
            }
        } else {
            sb.append("&s_num=").append(chapter.getChapterId() + 1);
        }

        LogUtil.d(TAG, "The book 《" + title + "》 Download url -> " + sb);
        // LogUtil.d("FileMissingLog", "The book 《" + title +
        // "》 Download url -> " + sb);

        return ConstantData.addDeviceIdToUrl(sb.toString());
    }

    /**
     * epub书籍的下载
     *
     * @return
     */
    public String getEpubDownUrl() {
        String carrier = DeviceInfo.getCarrier(SinaBookApplication.gContext);
        String apn = HttpUtil.getNetworkType(SinaBookApplication.gContext);
        String imei = ConstantData.getDeviceId();
        String deviceId = DeviceInfo.getUDID();
        String appChannel = String.valueOf(ConstantData.getChannelCode(SinaBookApplication.gContext));

        StringBuilder sb = new StringBuilder(ConstantData.EPUB_DOWNLOAD);
        // TODO CJL 配置成错误的AUTH_CODE，来查看下载接口返回的JSON错误信息
        // 在DownEpubFileTask的writeData2File方法中对其进行判断
        sb.append("?").append(ConstantData.AUTH_CODE_GET);
        // sb.append("?").append("authcode=d6712b498d9815f23cf1d5df43afd243");
        sb.append("&" + ConstantData.APP_VERSION_KEY + "=").append(ConstantData.APP_VERSION_VALUE);
        sb.append("&" + ConstantData.OPERATORS_NAME_KEY + "=").append(carrier);
        sb.append("&" + ConstantData.APN_ACCESS_KEY + "=").append(apn);
        sb.append("&" + ConstantData.PHONE_IMEI_KEY + "=").append(imei);
        sb.append("&" + ConstantData.DEVICE_ID_KEY + "=").append(deviceId);
        sb.append("&" + ConstantData.APP_CHANNEL_KEY + "=").append(appChannel);
        // TODO CJL MONI临时替换
        sb.append("&bid=").append(bookId);
        // sb.append("&bid=").append(243473);

        if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
            sb.append("&" + ConstantData.ACCESS_TOKEN_KEY + "=").append(LoginUtil.getLoginInfo().getAccessToken());
        }

        return ConstantData.addDeviceIdToUrl(sb.toString());
    }

    /**
     * 购买使用的url.
     *
     * @param chapter 指定章节, 若为null默认全本购买
     * @return the buy url
     */
    public String getBuyUrl(Chapter chapter) {
        StringBuilder sb = new StringBuilder(ConstantData.URL_BOOK_BUY);

        int chapterId = null == chapter ? 0 : chapter.getGlobalId();
        sb.append("?");
        sb.append("bid=").append(bookId);
        sb.append("&cid=").append(chapterId);

        // 这里没有判断是否已经登陆，统统加入access_token
        if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
            // if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) ==
            // LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS
            // && !isAutoDownBook()) {
            String token = LoginUtil.getLoginInfo().getAccessToken();
            sb.append("&access_token=").append(token);

            StringBuilder signFormat = new StringBuilder();
            signFormat.append(token);
            signFormat.append("|").append(bookId);
            signFormat.append("|").append(chapterId);
            signFormat.append("|").append(ConstantData.AUTO_KEY);

            String sign = URLUtil.md5Signature(signFormat.toString());
            sb.append("&sign=").append(sign);
        }

        if (null == chapter) {
            LogUtil.d(TAG, "The book 《" + title + "》 Buy url -> " + sb);
        } else {
            LogUtil.d(TAG, "The Chapter 《" + chapter.getTitle() + "》 Buy url -> " + sb);
        }

        return ConstantData.addDeviceIdToUrl(sb.toString());
    }

    /**
     * Change file suffix to dat
     *
     * @return the string
     */
    public String changeFileSuffix() {
        // dat格式
        String suffix = Book.BOOK_SUFFIX;
        if (isEpub()) {
            suffix = Book.EPUB_SUFFIX;
            // Bug描述：下载epub过程中退出APP，然后重新启动，继续下载，下载完成后点击阅读提示文件丢失
            // Bug原因：重启后继续下载完成时没有将mOriginalFilePath也设置成最终的路径名，实际点击读取时读取的却是mOriginalFilePath信息
            // Bug解决：下载完成也要把mOriginalFilePath更改一下
            changeOriginalFile2FilePath();
        }
        return changeFileSuffix(suffix);
    }

    /**
     * Change file suffix to tmp.
     *
     * @return the string
     */
    public String changeFileSuffixToTmp() {
        return changeFileSuffix(Book.TMP_SUFFIX);
    }

    /**
     * Change file suffix
     *
     * @param suffix
     * @return the string
     */
    public String changeFileSuffix(String suffix) {
        if (mDownloadInfo.getFilePath() != null) {
            int dotIndex = mDownloadInfo.getFilePath().lastIndexOf(".");
            StringBuilder sb = new StringBuilder(mDownloadInfo.getFilePath().substring(0, dotIndex));
            sb.append(suffix);
            mDownloadInfo.setFilePath(sb.toString());
        } else {
            mDownloadInfo.setFilePath(StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK) + "/" + mDownloadInfo.getCreateFileDate().getTime() + suffix);
        }
        return mDownloadInfo.getFilePath();
    }

    public void changeOriginalFile2FilePath() {
        mDownloadInfo.setOriginalFilePath(mDownloadInfo.getFilePath());
    }

    /**
     * 当前书籍内容是否完整，即章节列表中的每一章的Length是否都大于0.
     *
     * @return true if the book is complete or the book's chapters are null or
     * size <= 0, false otherwise.
     */
    public boolean isComplete() {
        boolean result = true;
        if (null != chapters && chapters.size() > 0) {
            for (Chapter chapter : chapters) {
                if (chapter.getLength() <= 0) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 是否是最后一章，目前只判断是否为章节列表的最后一项
     *
     * @param chapter 指定章节
     * @return
     */
    public boolean isLastChapter(Chapter chapter) {
        if (null != chapter && null != chapters && chapters.size() > 0) {
            Chapter lastChapter = chapters.get(chapters.size() - 1);
            return chapter.equals(lastChapter) || chapter.getChapterId() == lastChapter.getChapterId();
        } else {
            return false;
        }
    }

    /**
     * 是否是第一章，目前只判断是否为章节列表的最后一项
     *
     * @param chapter 指定章节
     * @return
     */
    public boolean isFirstChapter(Chapter chapter) {
        if (null != chapter && null != chapters && chapters.size() > 0) {
            Chapter lastChapter = chapters.get(0);
            return chapter.equals(lastChapter) || chapter.getChapterId() == lastChapter.getChapterId();
        } else {
            return false;
        }
    }

    /**
     * 更新指定章节
     *
     * @param chapter
     */
    public void updateChapter(Chapter chapter) {
        if (null == chapter)
            return;

        if (chapters != null && chapters.size() > 0) {
            for (int i = 0; i < chapters.size(); i++) {
                if (chapter.equals(chapters.get(i))) {
                    // LogUtil.e("BugID=21356",
                    // "Book >> updateChapter >> {chapter=" + chapter + "}");
                    chapters.set(i, chapter);
                    return;
                }
            }
        }
    }

    /**
     * 更新自己,只更新部分数据
     *
     * @param book
     */
    public void update(Book book) {
        if (null == book)
            return;

        praiseType = book.getPraiseType();
        num = book.getNum();
        praiseNum = book.getPraiseNum();
        // 包月信息不能更新，因为包月信息里包含了下载该书的状态

        mBuyInfo.setVip(book.getBuyInfo().isVip());
        mBuyInfo.setPayType(book.getBuyInfo().getPayType());
        mBuyInfo.setHasBuy(book.getBuyInfo().isHasBuy());
        mBuyInfo.setBuyType(book.getBuyInfo().getBuyType());
        mBuyInfo.setStatusInfo(book.getBuyInfo().getStatusInfo());
        mBuyInfo.setStatusFlag(book.getBuyInfo().getStatusFlag());

        // ----------- 解决：BugID=21446 -----------
        // 存储简介
        setIntroRealNeed(book.getIntro());
        // ----------- ----------- ----------- ----
    }

    private void setContentTypeFlags(int flags, int mask) {
        bookContentType = (bookContentType & ~mask) | (flags & mask);
    }

    /**
     * 判断是否有最新章节的更新
     */
    public boolean isUpdateChapter() {
        if (0 != mUpdateChapterInfo.getGlobalId() && null != mUpdateChapterInfo.getUpdateTime() && null != mUpdateChapterInfo.getTitle()) {
            return true;
        }
        return false;
    }

    public String getUpdateTimeServer() {
        return updateTimeServer;
    }

    public void setUpdateTimeServer(String updateTimeServer) {
        this.updateTimeServer = updateTimeServer;
    }

    public String getUpdateChapterNameServer() {
        return updateChapterNameServer;
    }

    public void setUpdateChapterNameServer(String updateChapterNameServer) {
        this.updateChapterNameServer = updateChapterNameServer;
    }

    public String getRecommendTime() {
        return recommendTime;
    }

    public void setRecommendTime(String recommendTime) {
        this.recommendTime = recommendTime;
    }

    public String getRecommendIntro() {
        return recommendIntro;
    }

    public void setRecommendIntro(String recommendIntro) {
        this.recommendIntro = recommendIntro;
    }

    public void reset() {
        getDownloadInfo().reset();
        getReadInfo().setLastReadPercent(0.0f);
        getReadInfo().setLastPos(0);

        // 重新生成
        getDownloadInfo().getFilePath();
        getDownloadInfo().getOriginalFilePath();
    }

    @Override
    public String toString() {
        // return "Book{" + "tag=" + tag + ", contentTag='" + contentTag + '\''
        // + ", title='" + title + '\''
        // + ", author='" + author + '\'' + ", num=" + num + ", bookCate='" +
        // bookCate + '\'' + ", bookCateId='"
        // + bookCateId + '\'' + ", intro='" + intro + '\'' + ", type='" + type
        // + '\'' + ", flag='" + flag + '\''
        // + ", chapters=" + chapters + ", bookmarks=" + bookmarks +
        // ", bookSummaries=" + bookSummaries
        // + ", updateChaptersNum=" + updateChaptersNum + ", lastUpdateTime=" +
        // lastUpdateTime + ", id=" + id
        // + ", bookId='" + bookId + '\'' + ", sid='" + sid + '\'' +
        // ", bookSrc='" + bookSrc + '\'' + ", bagId='"
        // + bagId + '\'' + ", praiseNum=" + praiseNum + ", praiseType='" +
        // praiseType + '\'' + ", commentNum="
        // + commentNum + ", comment='" + comment + '\'' + ", bookContentType="
        // + bookContentType + ", suiteId="
        // + suiteId + ", hasBuySuite=" + hasBuySuite + ", suiteName='" +
        // suiteName + '\'' + ", suiteImageUrl='"
        // + suiteImageUrl + '\'' + ", isOnlineBook=" + isOnlineBook +
        // ", ownerUid='" + ownerUid + '\''
        // + ", onlineReadChapterId=" + onlineReadChapterId + ", index=" + index
        // + ", updateTimeServer='"
        // + updateTimeServer + '\'' + ", updateChapterNameServer='" +
        // updateChapterNameServer + '\''
        // + ", mReadInfo=" + mReadInfo + ", mDownloadInfo=" + mDownloadInfo +
        // ", mBuyInfo=" + mBuyInfo
        // + ", mBookPage=" + mBookPage + ", mUpdateChapterInfo=" +
        // mUpdateChapterInfo + ", isRemind=" + isRemind
        // + '}';

        // + ", file path=" + getDownloadInfo().getFilePath() +
        // ", original path="
        // + getDownloadInfo().getOriginalFilePath()
        return "epub=" + isEpub + ", title=" + title + ", author=" + author + ", ownerUid=" + ownerUid + ", id=" + id + ", bookId=" + bookId + ", isOnlineBook=" + isOnlineBook + ", LastPos="
                + getReadInfo().getLastPos() + ", LastReadPercent=" + getReadInfo().getLastReadPercent() + ", DownLoadInfo=" + getDownloadInfo();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecommend_sub2() {
        return recommend_sub2;
    }

    public void setRecommend_sub2(String recommend_sub2) {
        this.recommend_sub2 = recommend_sub2;
    }

    public String getRecommend_sub3() {
        return recommend_sub3;
    }

    public void setRecommend_sub3(String recommend_sub3) {
        this.recommend_sub3 = recommend_sub3;
    }

    public String getRecommend_name() {
        return recommend_name;
    }

    public void setRecommend_name(String recommend_name) {
        this.recommend_name = recommend_name;
    }
}
