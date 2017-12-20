package com.sina.book.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.geometerplus.android.fbreader.FBReader;
import org.htmlcleaner.Utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.HtmlDownBookManager;
import com.sina.book.control.download.HtmlDownManager;
import com.sina.book.control.download.ITaskUpdateListener;
import com.sina.book.data.AuthorInfo;
import com.sina.book.data.Book;
import com.sina.book.data.BookBuyInfo;
import com.sina.book.data.BookDetailData;
import com.sina.book.data.BookDownloadInfo;
import com.sina.book.data.BookPicType;
import com.sina.book.data.BookPriceResult;
import com.sina.book.data.BookReadInfo;
import com.sina.book.data.BookRelatedData;
import com.sina.book.data.Chapter;
import com.sina.book.data.ChapterList;
import com.sina.book.data.CommentItem;
import com.sina.book.data.CommentsResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PriceTip;
import com.sina.book.data.WeiboContent;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.data.util.PurchasedBookList;
import com.sina.book.db.DBService;
import com.sina.book.image.IImageLoadListener;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.AuthorInfoParser;
import com.sina.book.parser.BookDetailParser;
import com.sina.book.parser.BookPriceParser;
import com.sina.book.parser.BookRelatedParser;
import com.sina.book.parser.ChapterListParser;
import com.sina.book.parser.CommentsParser;
import com.sina.book.parser.SimpleParser;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.adapter.BookImageAdapter2;
import com.sina.book.ui.adapter.CommentListAdapter;
import com.sina.book.ui.notification.DownloadBookNotification;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.PayDialog;
import com.sina.book.ui.view.ShareDialog;
import com.sina.book.ui.widget.EllipsizeTextView;
import com.sina.book.ui.widget.FreeTextListView;
import com.sina.book.ui.widget.FreeTextListView.OnItemClickListener;
import com.sina.book.ui.widget.HorizontalListView;
import com.sina.book.ui.widget.LinearLayoutListView;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.DialogUtils;
import com.sina.book.util.EpubHelper;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * 书籍详情界面
 *
 * @author fzx
 */
public class BookDetailActivity extends CustomTitleActivity implements
        ITaskFinishListener, ITaskUpdateListener, OnItemClickListener {
    // private static final String TAG = "BookDetailActivity";

    /**
     * 书籍简介最多显示行数.
     */
    private static final int MAX_LINE_INFO = 3;

    /**
     * 详情显示评论最多的情况.
     */
    private static final int MAX_COMMENTS_ITEMS = 10;

    private static final String REQ_CHAPTERS = "req_chapters";
    private static final String REQ_AUTO_READ = "auto_read";
    private static final String REQ_SHARE_WEIBO = "req_share_weibo";
    private static final String REQ_PRAISE = "req_praise";
    private static final String KEY_SHOW_PRAISE = "show_praise";
    private static final String KEY_SHOW_DIALOG = "show_dialog";

    public static final String BID = "bid";
    private static final String SID = "sid";
    private static final String SRC = "src";
    private static final String KEY = "prikey";

    // 增加Intent传递的数据属性
    private static final String FROM = "from";
    private static final String BOOK = "book";
    private String mFromWay;
    private Book mReadActivityBook;

    // 书籍
    private Book mBook;
    private Book mUpdateBook;

    private String mBookId;
    private String mBookSrc;
    private String mBookSid;

    // 从消费记录 进入详情页， 传递key值标记
    private String mBookKey;

    private int mHttpReturnCount = 0;

    // UI控件部分

    // 封皮
    private ImageView mBookHeadImg;
    private ImageView mBookTicketImage;
    private TextView mBookHeadTitle;

    private TextView mBookTitle;
    private TextView mBookAuthorTitle;
    private TextView mBookAuthor;
    private TextView mBookState;
    private TextView mBookPrice;
    private TextView mBookCate;
    private TextView mBookChapterNum;
    private EllipsizeTextView mBookInfo;
    private Button mReadBtn;
    private LinearLayoutListView mListView;
    private View mProgressView;
    private View mErrorView;
    private TextView mShowAllTv;
    private ImageView mShowAllIv;

    private ViewGroup mBookDownContainer;
    private ViewGroup mBookProgressContainer;
    private ProgressBar mBookProgressBar;

    private Button mBuyButton;
    private Button mCollectButton;
    private Button mEpubButton;

    private View mMonthPayView;
    private TextView mMonthPayTv;

    // 和阅读书籍标识
    private View mCmreadBookTagView;

    private TextView mMoreCommentTv;
    private CommentListAdapter mAdapter;

    // 书籍简介与我要评论部分
    private TextView mBookIntroText;
    private RelativeLayout mPraiseCommentBtn;
    private LinearLayout mShowAllLayout;
    private LinearLayout mShowPartLayout;
    private LinearLayout mPraiseCommentLayout;
    private TextView mPraiseBtn;
//    private TextView mCommentBtn;
    private TextView mPraiseNum;
    // 评论
//    private TextView mCommentNum;

    // 作者相关的书籍部分
    private View mRelatedAuthorBook;
    private TextView mRelatedAuthorText;
    private ImageView mRelatedAuthorDivider;
    private HorizontalListView mRelatedAuthorListView;
    private BookImageAdapter2 mRelatedAuthorAdapter;

    // 喜欢本书的人喜欢的书籍部分
    private View mRelatedPersonBook;
    private TextView mRelatedPersonText;
    private ImageView mRelatedPersonDivider;
    private HorizontalListView mRelatedPersonListView;
    private BookImageAdapter2 mRelatedPersonAdapter;

    // 与本书相关联的关键词
    private View mRelatedKeyWord;
    private View mRelateKeyWordTitle;                                    // 关联标签
    private ImageView mRelatedKeyWordDivider;
    private FreeTextListView mRelatedKeyWordListView;
    private ArrayList<String> mKeyWords = new ArrayList<String>();

    // 目录按钮、章节更新、更新时间相关部分
    private View mCatalog;
    private ImageView mCatalogDivider;
    private RelativeLayout mCatalogLayout1;
    private RelativeLayout mCatalogLayout2;
    private RelativeLayout mCatalogBtn;
    private RelativeLayout mCatalogChapterBtn;
    private TextView mCatalogUpdateChapter;
    private TextView mCatalogUpdateTime;

    /**
     * 赞、评论数目图标的高度、宽度
     */
    private final int PRAISE_COMMENT_NUM_BOUND = PixelUtil.dp2px(10.67f);
    /**
     * 赞、评论图标的高度、宽度
     */
    private final int PRAISE_COMMENT_BOUND = PixelUtil.dp2px(21.33f);

    /**
     * 分类标签的paddingLeft值
     */
    private final int CATE_TAG_PADDING_LEFT = PixelUtil.dp2px(5.33f);
    /**
     * 分类标签的paddingRight值
     */
    private final int CATE_TAG_PADDING_RIGHT = PixelUtil.dp2px(16f);

    private BookRelatedData mData;
    private ChapterList mChapterList;

    private AuthorInfo mAuthorInfo;

    private BitmapDrawable mDotHDrawable;

    private BookReadInfo mReadInfo = new BookReadInfo();

    public String getBookId() {
        return mBookId;
    }

    /*
     * 下载书籍广播监听-修改UI变化
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            initDownLoadBtnText();
        }
    };

    /*
     * 阅读页发送推荐监听-修改UI变化
     */
    private BroadcastReceiver mRmdReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // 阅读页返回摘要页，刷新推荐位显示
            String tBookid = intent.getStringExtra("bookid");
            if (mBook == null || tBookid == null) {
                return;
            }
            if (tBookid.equals(mBook.getBookId())) {
                mPraiseBtn.setText(getResources().getString(
                        R.string.book_detail_has_praised));
                mPraiseBtn.setTextColor(getResources().getColor(
                        R.color.book_detail_checked_font_color));
                Drawable drawable = getResources().getDrawable(
                        R.drawable.book_detail_praise_pressed);
                drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND,
                        PRAISE_COMMENT_BOUND);
                mPraiseBtn.setCompoundDrawables(drawable, null, null, null);
                mPraiseBtn.setEnabled(false);
            }
        }
    };

    private void handleReceiverOrTaskResult() {
        if (hasRereivedSyncAction && isUserHasPayForBookAndThenDownBook) {
            LogUtil.d("CursorError",
                    "摘要页 >> mSyncShelvesBookReceiver >> onReceive");
            DialogUtils.dismissProgressDialog();
            // 启动下载
            DownBookJob job = DownBookManager.getInstance().getJob(mBook);
            if (job != null) {
                LogUtil.d("CursorError",
                        "摘要页 >> mSyncShelvesBookReceiver >> 在书架内");
                Book shelfBook = job.getBook();
                // 在书架内了
                // 判断下载状态
                BookDownloadInfo downInfo = shelfBook.getDownloadInfo();
                if (Math.abs(downInfo.getProgress() - 1.0) < 0.0001
                        && job.getState() == DownBookJob.STATE_FINISHED) {
                    // 下载完成了
                    LogUtil.d("CursorError",
                            "摘要页 >> mSyncShelvesBookReceiver >> 下载完成了");

                    boolean isOnlineBook = shelfBook.isOnlineBook();
                    // 要判断当前当前书架内的书籍是txt还是epub，还要判断当前这本书是否是epub的
                    String filePath = shelfBook.getDownloadInfo().getFilePath();
                    boolean isFileEndWithDat = filePath != null
                            && filePath.endsWith(Book.BOOK_SUFFIX);

                    LogUtil.d("CursorError",
                            "摘要页 >> mSyncShelvesBookReceiver >> 下载完成了 >> isOnlineBook = "
                                    + isOnlineBook);
                    LogUtil.d("CursorError",
                            "摘要页 >> mSyncShelvesBookReceiver >> 下载完成了 >> isFileEndWithDat = "
                                    + isFileEndWithDat);

                    // 书架内的书籍是txt的，当前这本书籍是epub的
                    if (isOnlineBook || isFileEndWithDat && mBook.isEpub()) {
                        // 已经是完整的dat资源，说明这次下载的肯定是epub，直接覆盖
                        DownBookManager.getInstance().downBook(mBook, false,
                                true);
                    }
                } else if (job.getState() != DownBookJob.STATE_RUNNING) {
                    // 下载未完成
                    LogUtil.d("CursorError",
                            "摘要页 >> mSyncShelvesBookReceiver >> 下载未完成");
                    // 要判断书架内的书籍是txt的，还是epub的
                    if (shelfBook.isEpub() == mBook.isEpub()) {
                        // 都是epub的或者都是txt的，都直接重启
                        job.start();
                        DownloadBookNotification.getInstance().addNotification(
                                job.getBook());
                    } else {
                        // 另外一种可能就是书架的是txt，mBook是epub的(当前版本不可能是书架的是epub，
                        // 而这里是txt的，因为只要有epub的资源，摘要页都显示epub的)
                        // 覆盖txt下载epub
                        DownBookManager.getInstance().downBook(mBook, false,
                                true);
                    }
                }
            } else {
                LogUtil.d("CursorError",
                        "摘要页 >> mSyncShelvesBookReceiver >> 不在书架内，启动新的下载");
                // 不在书架内
                CloudSyncUtil.getInstance().add2Cloud(mContext, mBook);
                DownBookManager.getInstance().downBook(mBook, false, true);
            }
        }
        initDownLoadBtnText();
    }

    private boolean hasRereivedSyncAction = false;
    private boolean isUserHasPayForBookAndThenDownBook = false;

    private BroadcastReceiver mSyncShelvesBookReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            hasRereivedSyncAction = true;
            // 注销自己(防止下面的代码出现异常而没有反注册，放到最前面来)
            unregisterReceiver(this);
            // 这里收到广播了，但是不一定要下载，要判断
            handleReceiverOrTaskResult();
        }
    };

    private void setPraiseBtnDisable() {
        mPraiseBtn.setTextColor(getResources().getColor(
                R.color.book_detail_cannot_praise_color));
        Drawable drawable = getResources().getDrawable(
                R.drawable.book_detail_cannot_praise);
        drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND, PRAISE_COMMENT_BOUND);
        mPraiseBtn.setCompoundDrawables(drawable, null, null, null);
        mPraiseBtn.setEnabled(false);
    }

    public static boolean launch(Context c, Book book) {
        return launch(c, book, null, null);
    }

    public static boolean launch(Context c, Book book, String key) {
        return launch(c, book, key, null, null);
    }

    public static boolean launchNew(Context c, Book book, String eventKey) {
        return launch(c, book, null, eventKey, null);
    }

    public static boolean launch(Context c, Book book, String eventKey,
                                 String eventExtra) {
        return launch(c, book, null, eventKey, eventExtra);
    }

    public static boolean launch(Context c, Book book, String key,
                                 String eventKey, String eventExtra) {
        if (book == null) {
            return false;
        }

        Intent intent = new Intent();
        intent.setClass(c, BookDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        Bundle bundle = new Bundle();
        bundle.putString(BID, book.getBookId());
        bundle.putString(SID, book.getSid());
        bundle.putString(SRC, book.getBookSrc());
        bundle.putSerializable("readInfo", book.getReadInfo());
        bundle.putSerializable(FROM, eventKey);
        bundle.putSerializable(BOOK, book);

        if (key != null) {
            bundle.putString(KEY, key);
        }
        intent.putExtras(bundle);
        c.startActivity(intent);

        if (!TextUtils.isEmpty(eventKey)) {
            eventKey += "_" + book.getBookId();

            LogUtil.i("UserActionEventCount", "eventKey=" + eventKey
                    + ", eventExtra=" + eventExtra);
            UserActionManager.getInstance().recordEventNew(
                    Util.checkAndClip(eventKey), eventExtra);
        }

        return true;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.act_book_detail);

        initIntent();
        initTitle();
        initViews();
        initListener();

        reqBookInfo();
        reqAuthorInfo();
        reqBookPrice();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reqBookExt();
                reqComments();
                reqChapterList();
            }
        }, 800);

        // 注册推荐分享信息更新
        registerReceiver(mRmdReceiver, new IntentFilter(
                DownBookManager.ACTION_INTENT_RECOMMANDSTATE));

        // 书架同步更新
        registerReceiver(mSyncShelvesBookReceiver, new IntentFilter(
                CloudSyncUtil.ACTION_SYNC_COMPLETE));
    }

    protected void onStart() {
        // 注册下载监听
        registerReceiver(mReceiver, new IntentFilter(
                DownBookManager.ACTION_INTENT_DOWNSTATE));

        // 添加下载监听接口()
        HtmlDownManager.getInstance().addProgressListener(this);
        DownBookManager.getInstance().addProgressListener(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        initDownLoadBtnText();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFromWay = null;
        mReadActivityBook = null;
        unregisterReceiver(mRmdReceiver);
        // 已经注销过了的话不能重复注销
        if (!hasRereivedSyncAction) {
            unregisterReceiver(mSyncShelvesBookReceiver);
        }
        cancelDownload(true);
    }

    @Override
    protected void onStop() {
        // 取消下载回调接口
        unregisterReceiver(mReceiver);
        HtmlDownManager.getInstance().removeProgressListener(this);
        DownBookManager.getInstance().removeProgressListener(this);
        super.onStop();
    }

    public void cancelDownload() {
        cancelDownload(false);
    }

    public void cancelDownload(boolean isDeleteFile) {
        if (mBook != null && mBook.isHtmlRead()) {
            HtmlDownBookManager manager = HtmlDownManager.getInstance().getJob(mBook);
            DownBookJob job = DownBookManager.getInstance().getJob(mBook);
            if (manager != null) {
                manager.setIsRead(false);
                if (job == null) {
                    // 检测是否存在登录缓存数据
                    HtmlDownManager.getInstance().pauseJob(manager, true);
//					if(isDeleteFile){
//						// 没有加入书架，删除书籍文件
//						// 检测DB缓存是否存在该数据
//						boolean hasDBCache = false;
//						ArrayList<Book> list = DBService.getAllBookCache();
//						for(int i = 0; i < list.size(); ++i){
//							Book tmpBook = list.get(i);
//							if(tmpBook.equals(mBook)){
//								hasDBCache = true;
//								break;
//							}
//						}
//						
//						if(!disableDeleteFile && !hasDBCache){
//							manager.deleteBookFileCache();
//						}else{
//							disableDeleteFile = false;
//						}
//					}
                } else {
                    if (manager.getState() == DownBookJob.STATE_RUNNING) {
                        // 正在下载中，暂停下载
                        HtmlDownManager.getInstance().pauseJob(manager, true);
                    }
                }

//				if(isDeleteFile){
//					if(job == null){
//						// 没有加入书架，删除书籍文件
//						manager.deleteBookFileCache();
//					}
//				}
            }
        }
    }

    @Override
    public void onClickLeft() {
        this.finish();
    }

    @Override
    public void onClickNearRight() {
        shareBook();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void retry() {
        mErrorView.setVisibility(View.GONE);
        reqBookInfo();
        reqAuthorInfo();
        reqBookExt();
        reqComments();
        reqChapterList();
        reqBookPrice();
    }

    @Override
    public void onClickRight() {
        MainActivity.launch(this);
        this.finish();

        cancelDownload(true);

        UserActionManager.getInstance().recordEvent(Constants.CLICK_HOME);
    }

    @Override
    public void onTaskFinished(TaskResult taskResult) {
        RequestTask reqTask = (RequestTask) taskResult.task;
        String type = reqTask.getType();

        if (taskResult.retObj != null
                && taskResult.stateCode == HttpStatus.SC_OK) {
            if (taskResult.retObj instanceof BookDetailData) {
                // bookinfo
                BookDetailData data = (BookDetailData) taskResult.retObj;
                if (data.getCode() != BookDetailData.BOOK_STATE_NORMAL) {
                    if (!TextUtils.isEmpty(data.getMessage())) {
                        shortToast(data.getMessage());
                        finish();
                        return;
                    }
                }

                mBook = data.getBook();
                mBook.setReadInfo(mReadInfo);

                // CJL MONI(Start)
                // if (SinaBookApplication.myCount >= 1) {
                // SinaBookApplication.myBool_RewriteOldVersion = true;
                // }
                // if (SinaBookApplication.myBool_RewriteOldVersion &&
                // "205555".equals(mBook.getBookId())) {
                // mBook.setIsEpub(true);
                // }
                // SinaBookApplication.myCount++;
                //  CJL MONI(End)

                updateUIData();
                updateRelatedContent();

                // 目录
                updateChapterList();

                updateBookPrice(mUpdateBook);
                updatePraiseOrBuyState(type);
                mHttpReturnCount++;

                // 判断这本书籍是不是在H5赠书卡界面领取过赠书卡了
                if (CloudSyncUtil.getInstance().hasInH5GetCardBookIdLists(
                        mBookId)) {
                    // 书架上没有这本书时加入书架
                    if (!DownBookManager.getInstance().hasBook(mBook)) {
                        CloudSyncUtil.getInstance().removeH5GetCardBookId(
                                mBookId);
                        collect();
                    }
                }

            } else if (taskResult.retObj instanceof BookRelatedData) {
                mData = (BookRelatedData) taskResult.retObj;
                updateRelatedContent();

            } else if (taskResult.retObj instanceof CommentsResult) {
                CommentsResult result = (CommentsResult) taskResult.retObj;
                updateCommentList(result.getTotal(), result.getItems());

            } else if (taskResult.retObj instanceof ChapterList) {
                // 章节列表
                mChapterList = (ChapterList) taskResult.retObj;
                updateChapterList();
            } else if (taskResult.retObj instanceof AuthorInfo) {
                mAuthorInfo = (AuthorInfo) taskResult.retObj;
            } else if (taskResult.retObj instanceof BookPriceResult) {
                BookPriceResult result = (BookPriceResult) taskResult.retObj;
                mUpdateBook = result.getBook();
                updateBookPrice(mUpdateBook);
                updatePraiseOrBuyState(type);
            } else if (ConstantData.CODE_SUCCESS.equals(String
                    .valueOf(taskResult.retObj))) {
                if (REQ_PRAISE.equals(type)) {
                    updatePraiseData();
                } else if (REQ_SHARE_WEIBO.equals(type)) {
                    shortToast(R.string.share_weibo_success);
                }
                dismissProgress();

            } else {// 网络错误
                if (!REQ_CHAPTERS.equals(type) && !REQ_AUTO_READ.equals(type)
                        && mHttpReturnCount < 1) {
                    mErrorView.setVisibility(View.VISIBLE);
                }
            }

        } else {// 网络错误
            if (!REQ_CHAPTERS.equals(type) && !REQ_AUTO_READ.equals(type)
                    && mHttpReturnCount < 1) {
                mErrorView.setVisibility(View.VISIBLE);
            }
        }

        if (mHttpReturnCount >= 1) {
            mProgressView.setVisibility(View.GONE);
        }
    }

    @Override
    // 下载反馈
    public void onUpdate(Book book, boolean mustUpdate, boolean mustRefresh, int progress,
                         int stateCode) {
        if (mBook == null || !mBook.equals(book)) {
            return;
        }

        if (stateCode == DownBookJob.STATE_RECHARGE) {
            mBook.getBuyInfo().setHasBuy(false);
            initDownLoadBtnText();
            PayDialog.showBalanceDlg(this);
        } else if (stateCode == DownBookJob.STATE_RUNNING) {
            if (mBook.isHtmlRead()) {
                // html阅读
                initDownLoadBtnText();

                HtmlDownBookManager manager = HtmlDownManager.getInstance().getJob(mBook);
                if (manager.getState() == DownBookJob.STATE_FINISHED && manager.isRead()) {
                    float precent = book.getReadInfo().getLastReadPercent();
                    boolean hasPrecent = precent > 0.0f ? true : false;
                    String path = mBook.getDownloadInfo().getOriginalFilePath();
//					String path = book.getDownloadInfo().getOriginalFilePath();
//					FBReader.openBookActivity(
//							BookDetailActivity.this, path,
//							hasPrecent, book.getBookId());

//					disableDeleteFile = true;
                    FBReader.openBookActivity(
                            BookDetailActivity.this, path, book,
                            hasPrecent);
                }
            }
        } else if (stateCode == DownBookJob.STATE_FINISHED) {
            if (mBook.isHtmlRead()) {
                // html阅读
                initDownLoadBtnText();

//				Toast.makeText(this, "下载成功", Toast.LENGTH_SHORT).show();

                HtmlDownBookManager manager = HtmlDownManager.getInstance().getJob(mBook);
                if (manager != null) {
                    if (manager.getState() == DownBookJob.STATE_FINISHED && manager.isRead()) {
                        float precent = book.getReadInfo().getLastReadPercent();
                        boolean hasPrecent = precent > 0.0f ? true : false;
                        String path = mBook.getDownloadInfo().getOriginalFilePath();
//					String path = book.getDownloadInfo().getOriginalFilePath();

//						disableDeleteFile = true;
                        FBReader.openBookActivity(
                                BookDetailActivity.this, path, book,
                                hasPrecent);
//						FBReader.openBookActivity(
//								BookDetailActivity.this, path,
//								hasPrecent, book.getBookId());
                    }
                }
            }
        } else if (stateCode == DownBookJob.STATE_FAILED) {
            // 下载失败
            if (mBook.isHtmlRead()) {
                Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 初始化标题
     */
    private void initTitle() {
        TextView title = (TextView) LayoutInflater.from(this).inflate(
                R.layout.vw_title_textview, null);
        if (null == title) {
            title = new TextView(this);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(getResources().getColor(R.color.white));
            title.setTextSize(17.33f);
        }
        title.setText(R.string.book_detail);
        setTitleMiddle(title);

        View left = LayoutInflater.from(this).inflate(
                R.layout.vw_generic_title_back, null);
        setTitleLeft(left);

        View nearRight = LayoutInflater.from(this).inflate(
                R.layout.vw_generic_title_weibo, null);
        setTitleNearRight(nearRight);

        View right = LayoutInflater.from(this).inflate(
                R.layout.vw_generic_title_home, null);
        setTitleRight(right);
    }

    /**
     * 初始化UI控件
     */
    private void initViews() {
        // 图片背景平铺
        Bitmap dotHBitmap = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.list_divide_dot);
        mDotHDrawable = new BitmapDrawable(getResources(), dotHBitmap);
        mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        mDotHDrawable.setDither(true);

        View header = findViewById(R.id.book_detail_header);

        // 封面
        mBookHeadImg = (ImageView) header.findViewById(R.id.book_head_img);
        // 封面上的标签
        mBookTicketImage = (ImageView) header
                .findViewById(R.id.book_ticket_image);
        // 封面上文字
        mBookHeadTitle = (TextView) header.findViewById(R.id.book_head_title);

        // 书名
        mBookTitle = (TextView) header.findViewById(R.id.book_name);

        // 作者标题("作者：")
        mBookAuthorTitle = (TextView) header
                .findViewById(R.id.book_author_title);
        // 作者名称
        mBookAuthor = (TextView) header.findViewById(R.id.book_author);

        // 状态
        mBookState = (TextView) header.findViewById(R.id.book_state);
        // 价格
        mBookPrice = (TextView) header.findViewById(R.id.book_price);

        // 分类
        mBookCate = (TextView) header.findViewById(R.id.book_cate);
        mBookChapterNum = (TextView) header.findViewById(R.id.book_chapter_num);

        mBookInfo = (EllipsizeTextView) header.findViewById(R.id.book_info);

        mBookDownContainer = (ViewGroup) header.findViewById(R.id.book_down_container);
        mBookProgressContainer = (ViewGroup) header.findViewById(R.id.book_down_progress_container);
        mBookProgressBar = (ProgressBar) header.findViewById(R.id.book_down_btn_progress);

        mReadBtn = (Button) header.findViewById(R.id.book_down_btn);
        mBuyButton = (Button) header.findViewById(R.id.book_buy_btn);
        mCollectButton = (Button) header.findViewById(R.id.book_collect_btn);
        mEpubButton = (Button) header.findViewById(R.id.book_epub_btn);

        mMonthPayView = header.findViewById(R.id.month_pay_tip);
        mMonthPayTv = (TextView) mMonthPayView.findViewById(R.id.month_pay_tv);

        mCmreadBookTagView = header.findViewById(R.id.cmread_book_tag);

        mShowAllLayout = (LinearLayout) header
                .findViewById(R.id.book_detail_intro_layout);
        mShowPartLayout = (LinearLayout) header
                .findViewById(R.id.book_detail_intro_part_layout);
        mShowAllTv = (TextView) header.findViewById(R.id.show_more_info_tv);
        mShowAllIv = (ImageView) header.findViewById(R.id.show_more_info_iv);

        mBookIntroText = (TextView) header
                .findViewById(R.id.book_detail_intro_btn);
        mPraiseCommentBtn = (RelativeLayout) header
                .findViewById(R.id.book_detail_praise_comment_btn);
        mPraiseCommentLayout = (LinearLayout) header
                .findViewById(R.id.book_detail_praise_comment_layout);
        mPraiseBtn = (TextView) header
                .findViewById(R.id.book_detail_praise_btn);
//        mCommentBtn = (TextView) header
//                .findViewById(R.id.book_detail_comment_btn);
        mMoreCommentTv = (TextView) header
                .findViewById(R.id.show_more_comments_tv);
        mPraiseNum = (TextView) header
                .findViewById(R.id.book_detail_praise_text);
//        mCommentNum = (TextView) header
//                .findViewById(R.id.book_detail_comment_text);

        ImageView commentDivider = (ImageView) header
                .findViewById(R.id.book_detail_comment_divider);
        commentDivider.setBackgroundDrawable(mDotHDrawable);

        mProgressView = findViewById(R.id.rl_progress);
        mErrorView = findViewById(R.id.error_layout);

        mListView = (LinearLayoutListView) header
                .findViewById(R.id.book_detail_commonent_listview);
        mAdapter = new CommentListAdapter(this, R.drawable.divider_dot_real);
        mListView.setAdapter(mAdapter);

        mRelatedAuthorBook = header
                .findViewById(R.id.book_detail_related_author);
        mRelatedAuthorText = (TextView) mRelatedAuthorBook
                .findViewById(R.id.book_detail_related_author_name);
        mRelatedAuthorDivider = (ImageView) mRelatedAuthorBook
                .findViewById(R.id.book_detail_related_divider);
        mRelatedAuthorAdapter = new BookImageAdapter2(this, "书籍详情", "作者的书",
                mBookId);
        mRelatedAuthorAdapter.setActionType(Constants.CLICK_AUTHOR_BOOK);
        mRelatedAuthorListView = (HorizontalListView) mRelatedAuthorBook
                .findViewById(R.id.book_detail_related_book_listview);
        mRelatedAuthorListView.setAdapter(mRelatedAuthorAdapter);

        mRelatedPersonBook = header
                .findViewById(R.id.book_detail_related_person);
        mRelatedPersonText = (TextView) mRelatedPersonBook
                .findViewById(R.id.book_detail_related_author_name);
        mRelatedPersonDivider = (ImageView) mRelatedPersonBook
                .findViewById(R.id.book_detail_related_divider);
        mRelatedPersonAdapter = new BookImageAdapter2(this, "书籍详情",
                "喜欢本书的人还喜欢", mBookId);
        mRelatedPersonAdapter.setActionType(Constants.CLICK_RELATED_BOOK);
        mRelatedPersonListView = (HorizontalListView) mRelatedPersonBook
                .findViewById(R.id.book_detail_related_book_listview);
        mRelatedPersonListView.setAdapter(mRelatedPersonAdapter);

        mRelatedKeyWord = header.findViewById(R.id.book_detail_related_keyword);
        mRelateKeyWordTitle = mRelatedKeyWord
                .findViewById(R.id.book_detail_keywords_text);
        mRelatedKeyWordDivider = (ImageView) mRelatedKeyWord
                .findViewById(R.id.book_detail_keywords_divider);
        mRelatedKeyWordListView = (FreeTextListView) mRelatedKeyWord
                .findViewById(R.id.book_detail_keyword_listview);
        mRelatedKeyWordListView.setOnItemClickListener(this);

        mCatalog = header.findViewById(R.id.book_detail_catalog);
        mCatalogDivider = (ImageView) mCatalog
                .findViewById(R.id.book_detail_catalog_divider);
        mCatalogDivider.setBackgroundDrawable(mDotHDrawable);
        mCatalogLayout1 = (RelativeLayout) mCatalog
                .findViewById(R.id.book_detail_catalog_layout1);
        mCatalogLayout2 = (RelativeLayout) mCatalog
                .findViewById(R.id.book_detail_catalog_layout2);
        mCatalogBtn = (RelativeLayout) mCatalog
                .findViewById(R.id.book_detail_catalog_btn);
        mCatalogChapterBtn = (RelativeLayout) mCatalog
                .findViewById(R.id.book_detail_catalog_update_layout);
        mCatalogUpdateChapter = (TextView) mCatalog
                .findViewById(R.id.book_detail_new_chapter);
        mCatalogUpdateTime = (TextView) mCatalog
                .findViewById(R.id.book_detail_update_time);

    }

    /**
     * 初始化Intent
     */
    private void initIntent() {
        Intent intent = getIntent();
        // 优先判断scheme
        if (null != intent) {
            // boolean isFromWeibo = false;
            // if (!TextUtils.isEmpty(intent.getScheme())) {
            // isFromWeibo = parseSchemeData(intent);
            // }
            //
            // if (!isFromWeibo) {
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                mBookId = bundle.getString(BID);
                // BugID=21685,下架书籍《嫁衣》的ID
                // mBookId = "226877";
                // CJL 临时(只配置给图文精装作品下的两本书籍)
                // if ("244214".equals(mBookId) || "240402".equals(mBookId)) {
                // mBookId = "243473";
                // }
                mBookSid = bundle.getString(SID);
                mBookSrc = bundle.getString(SRC);
                mBookKey = bundle.getString(KEY);
                mFromWay = bundle.getString(FROM);
                mReadInfo = (BookReadInfo) bundle.getSerializable("readInfo");
                if (mFromWay != null && "阅读器_工具栏_01".equals(mFromWay)) {
                    mReadActivityBook = (Book) bundle.getSerializable(BOOK);
                }
            }
            // }
        }
    }

    // private boolean parseSchemeData(Intent intent) {
    // String data = intent.getDataString();
    // LogUtil.d("BookDetailActivity", "The data come from weibo : " + data);
    //
    // if (!TextUtils.isEmpty(data)) {
    // final HashMap<String, String> map = URLUtil.parseUrl(data);
    // mBookId = map.get("b_id");
    // return "weibo".equals(map.get("ftype"));
    // }
    // return false;
    // }

    /**
     * 初始化ui数据
     */
    private void updateUIData() {
        mBookTitle.setText(mBook.getTitle());

        // 和阅读书籍标志的显示判断
        // 首先得是百度渠道
        // 然后判断是不是，显示与否
        if (ConstantData.getChannelCode(SinaBookApplication.gContext) == ConstantData.CHANNEL_BAIDU_LIMITFREE) {
            if (mBook.isCmreadBook() && mBook.isCmreadBookAndNeedShow()) {
                mCmreadBookTagView.setVisibility(View.VISIBLE);
            }
        }

        if (mBook.getAuthor() != null) {
            mBookAuthorTitle.setVisibility(View.VISIBLE);
            mBookAuthor.setText(mBook.getAuthor());
            mBookAuthor.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            mBookAuthor.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mAuthorInfo != null) {
                        if (mAuthorInfo.isShow()) {
                            AuthorActivity.launch(
                                    mContext,
                                    ResourceUtil
                                            .getString(R.string.author_recommend_title),
                                    mAuthorInfo.getUid(),
                                    AuthorActivity.TYPE_AUTHOR);
                            cancelDownload();
                        } else {
                            Toast toast = Toast.makeText(mContext,
                                    R.string.see_author_info_note,
                                    Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }
                    UserActionManager.getInstance().recordEvent(
                            Constants.CLICK_AUTHOR);
                }
            });
        }

        if (mBook.getBookCate() != null) {
            mBookCate.setText(mBook.getBookCate());
            mBookCate
                    .setBackgroundResource(R.drawable.selector_book_detail_tag_bg);
            mBookCate.setPadding(CATE_TAG_PADDING_LEFT, 0,
                    CATE_TAG_PADDING_RIGHT, 0);
        }

        mBookChapterNum.setText(String.format(
                getString(R.string.book_chapter_num), mBook.getNum()));
        mBookState.setText(getString(R.string.state)
                + mBook.getBuyInfo().getStatusInfo());
        mPraiseNum.setText(String.valueOf(mBook.getPraiseNum()));

        mBookHeadTitle.setText(mBook.getTitle());
        ImageLoader.getInstance().load3(mBook.getDownloadInfo().getImageUrl(),
                mBookHeadImg, ImageLoader.TYPE_BIG_PIC,
                ImageLoader.getNoImgPic(), new IImageLoadListener() {

                    @Override
                    public void onImageLoaded(Bitmap bm, ImageView imageView,
                                              boolean loadSuccess) {
                        if (loadSuccess) {
                            mBookHeadTitle.setText("");
                        } else {
                            // 如果imageurl加载失败，默认书皮上会显示书籍标题
                            if (mBook != null) {
                                mBookHeadTitle.setText(mBook.getTitle());
                            }
                        }
                    }
                });

        // 显示书籍封皮右上角标签的信息
        mBookTicketImage.setVisibility(View.GONE);
        if (mBook.getPicType().isShow()) {
            switch (mBook.getPicType().getType()) {
                case BookPicType.TYPE_PIC_ONE:
                    mBookTicketImage.setVisibility(View.VISIBLE);
                    mBookTicketImage.setImageResource(R.drawable.ticket_icon1);
                    break;
                case BookPicType.TYPE_PIC_TWO:
                    mBookTicketImage.setVisibility(View.VISIBLE);
                    mBookTicketImage.setImageResource(R.drawable.ticket_icon2);
                    break;
                case BookPicType.TYPE_PIC_THREE:
                    mBookTicketImage.setVisibility(View.VISIBLE);
                    mBookTicketImage.setImageResource(R.drawable.ticket_icon3);
                    break;
            }
        }

        if (mBook.getIntro() == null || mBook.getIntro().length() == 0) {
            mBookInfo.setText(R.string.book_no_info);
        } else {
            mBookInfo.setText(mBook.getIntro());
        }

        if (mBookInfo.getTextLines() == 0) {
            mShowPartLayout.setVisibility(View.GONE);
        } else if (mBookInfo.getTextLines() > mBookInfo.getMaxLine()) {
            mShowPartLayout.setVisibility(View.VISIBLE);
            mShowAllTv.setTextColor(getResources().getColor(
                    R.color.book_detail_open_all_color));
            mShowAllIv.setImageResource(R.drawable.book_detail_arrow_open);
            mShowPartLayout.setEnabled(true);
        } else {
            mShowPartLayout.setVisibility(View.VISIBLE);
            mShowAllTv.setTextColor(getResources().getColor(
                    R.color.book_detail_cannot_open_color));
            mShowAllIv.setImageResource(R.drawable.book_detail_cannot_open);
            mShowPartLayout.setEnabled(false);
        }

        // 显示包月相关信息
        if (mBook.isSuite() && !mBook.hasBuySuite()
                && !mBook.getBuyInfo().isHasBuy()
                && mBook.getBuyInfo().getPayType() != Book.BOOK_TYPE_FREE) {
            String suiteTip;
            if (Utils.isEmptyString(mBook.getSuiteName())) {
                suiteTip = getString(R.string.open_payment_month_detail_tip1);
            } else {
                suiteTip = String.format(
                        getString(R.string.open_payment_month_detail_tip2),
                        mBook.getSuiteName());
            }
            mMonthPayTv.setText(suiteTip);
            mMonthPayView.setVisibility(View.VISIBLE);
            mMonthPayView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BookDetailActivity.this,
                            PaymentMonthDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    startActivity(intent);
                }
            });
        }

        if (LoginUtil.isValidAccessToken(BookDetailActivity.this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
            Drawable drawable = getResources().getDrawable(
                    R.drawable.selector_book_detail_praise);
            // Drawable drawable = getResources().getDrawable(
            // R.drawable.book_detail_cannot_praise);
            drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND, PRAISE_COMMENT_BOUND);
            mPraiseBtn.setCompoundDrawables(drawable, null, null, null);
            mPraiseBtn.setTextColor(getResources().getColorStateList(
                    R.drawable.selector_book_detail_praise_comment_font));
            // mPraiseBtn.setTextColor(getResources().getColor(
            // R.color.book_detail_cannot_praise_color));
        } else {
            if (mBook.hasPraised()) {
                mPraiseBtn.setText(getResources().getString(
                        R.string.book_detail_has_praised));
            } else {
                mPraiseBtn.setText(getResources().getString(
                        R.string.book_detail_praise_text));
            }

            if (mBook.isForbidden()) {
                mPraiseBtn.setTextColor(getResources().getColor(
                        R.color.book_detail_cannot_praise_color));

                Drawable drawable = getResources().getDrawable(
                        R.drawable.book_detail_cannot_praise);
                drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND,
                        PRAISE_COMMENT_BOUND);
                mPraiseBtn.setCompoundDrawables(drawable, null, null, null);
                mPraiseBtn.setEnabled(false);
            } else {
                if (mBook.hasPraised()) {
                    mPraiseBtn.setTextColor(getResources().getColor(
                            R.color.book_detail_checked_font_color));

                    Drawable drawable = getResources().getDrawable(
                            R.drawable.book_detail_praise_pressed);
                    drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND,
                            PRAISE_COMMENT_BOUND);
                    mPraiseBtn.setCompoundDrawables(drawable, null, null, null);

                    mPraiseBtn.setEnabled(false);
                } else {
                    mPraiseBtn
                            .setTextColor(getResources()
                                    .getColorStateList(
                                            R.drawable.selector_book_detail_praise_comment_font));

                    Drawable drawable = getResources().getDrawable(
                            R.drawable.selector_book_detail_praise);
                    drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND,
                            PRAISE_COMMENT_BOUND);
                    mPraiseBtn.setCompoundDrawables(drawable, null, null, null);
                    mPraiseBtn.setEnabled(true);
                }
            }
        }

        // 下架书籍(或者epub书籍暂时)不允许评论
//        if (mBook.isForbidden() || mBook.isEpub()) {
//            Drawable drawable = getResources().getDrawable(
//                    R.drawable.book_detail_cannot_comment);
//            drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND, PRAISE_COMMENT_BOUND);
//            mCommentBtn.setCompoundDrawables(drawable, null, null, null);
//
//            mCommentBtn.setTextColor(getResources().getColor(
//                    R.color.book_detail_cannot_praise_color));
//            mCommentBtn.setEnabled(false);
//        }

        // 修改几个按钮的状态
        initDownLoadBtnText();

        // 通知包月管理类去检查包月信息
        PaymentMonthMineUtil.getInstance().checkPaymentMonth(mBook);

        // epub书籍暂不能分享，不可推荐
        if (mBook.isEpub()) {
            getNearRightView().setVisibility(View.GONE);
            setPraiseBtnDisable();
        }
    }

    // epub书籍按钮的三种状态
    private final int EPUB_BTN_STATE_READ = 0x1;                            // 已经购买并且下载过了，可以直接进去阅读
    private final int EPUB_BTN_STATE_BUYANDDOWN = 0x2;                            // 未购买、未下载
    private final int EPUB_BTN_STATE_DOWN = 0x3;                            // 已经购买了但是还没下载
    private int epubBtnState = EPUB_BTN_STATE_BUYANDDOWN;

    /**
     * 设置阅读、下载按钮状态
     */
    private void initDownLoadBtnText() {
        if (mBook == null) {
            return;
        }

        String readText = null;

        // TODO:ouyang 测试
//		mBook.setIsHtmlRead(true);

        if (mBook.isHtmlRead()) {
            /*
             * Html图文阅读, 设置下载阅读和加入书架2个按钮
			 */
            mBookDownContainer.setVisibility(View.VISIBLE);
            mBuyButton.setVisibility(View.GONE);
            mEpubButton.setVisibility(View.GONE);
            mCollectButton.setVisibility(View.VISIBLE);

            Book book = HtmlDownManager.getInstance().getBook(mBook);
            Book dbook = DownBookManager.getInstance().getBook(mBook);

            boolean enableAddCollection = false;
            if (book != null && dbook == null) {
                // 下载  & 没有加入书架
                int state = book.getDownloadInfo().getDownLoadState();
                if (state == DownBookJob.STATE_FINISHED) {
                    // 下载完成
                    mBookProgressContainer.setVisibility(View.GONE);
                    mReadBtn.setVisibility(View.VISIBLE);
                    readText = getString(R.string.read);
                    mReadBtn.setText(readText);
                } else if (state == DownBookJob.STATE_RUNNING) {
                    // 下载中
                    enableAddCollection = true;
                    mBookProgressContainer.setVisibility(View.VISIBLE);
                    mReadBtn.setVisibility(View.GONE);
                    int max = 100;
                    mBookProgressBar.setMax(100);

                    double progressPer = book.getDownloadInfo().getProgress();
                    double progress = progressPer * 1.0F * max;
                    mBookProgressBar.setProgress((int) progress);
                } else {
                    // 下载停止状态
                    mBookProgressContainer.setVisibility(View.GONE);
                    mReadBtn.setVisibility(View.VISIBLE);
                    readText = getString(R.string.download_read);
                    mReadBtn.setText(readText);
                }
            } else {
                if (dbook == null) {
                    // 没有加入书架，也没有下载
                    mBookProgressContainer.setVisibility(View.GONE);
                    mReadBtn.setVisibility(View.VISIBLE);
                    readText = getString(R.string.download_read);
                    mReadBtn.setText(readText);
                } else {
                    //  书架上的书籍
                    if (dbook.isOnlineBook()) {
                        // 在线书籍
                        mBookProgressContainer.setVisibility(View.GONE);
                        mReadBtn.setVisibility(View.VISIBLE);
                        readText = getString(R.string.download_read);
                        mReadBtn.setText(readText);
                    } else {
                        int state = dbook.getDownloadInfo().getDownLoadState();
                        if (state == DownBookJob.STATE_FINISHED) {
                            // 下载完成
                            mBookProgressContainer.setVisibility(View.GONE);
                            mReadBtn.setVisibility(View.VISIBLE);
                            readText = getString(R.string.read);
                            mReadBtn.setText(readText);
                        } else if (state == DownBookJob.STATE_RUNNING) {
                            // 下载中
                            enableAddCollection = true;
                            mBookProgressContainer.setVisibility(View.VISIBLE);
                            mReadBtn.setVisibility(View.GONE);
                            int max = 100;
                            mBookProgressBar.setMax(100);

                            double progressPer = dbook.getDownloadInfo().getProgress();
                            double progress = progressPer * 1.0F * max;
                            mBookProgressBar.setProgress((int) progress);
                        } else {
                            // 下载停止状态
                            mBookProgressContainer.setVisibility(View.GONE);
                            mReadBtn.setVisibility(View.VISIBLE);
                            readText = getString(R.string.download_read);
                            mReadBtn.setText(readText);
                        }
                    }
                }
            }

            if (dbook != null) {
                mCollectButton.setText(R.string.has_collected);
                mCollectButton.setTextColor(getResources().getColor(
                        R.color.book_detail_btn_grayed_color));
                mCollectButton.setEnabled(false);
            } else {
                if (mBook.isForbidden() || enableAddCollection) {
                    // 书籍已下架 || 正在下载中
                    mCollectButton.setEnabled(false);
                    mCollectButton.setTextColor(getResources().getColor(
                            R.color.book_detail_btn_grayed_color));
                } else {
                    mCollectButton.setEnabled(true);
                    mCollectButton.setTextColor(getResources().getColor(
                            R.color.book_detail_btn_font_color));
                }
                mCollectButton.setText(R.string.cloud_collect);
            }
            return;
        }

        Book book = DownBookManager.getInstance().getBook(mBook);
        DownBookJob job = DownBookManager.getInstance().getJob(mBook);
        boolean hasBook = (job != null);

        // 下载，购买按钮状态
        String buyText = "";
        boolean enableBuyButton = true;
        boolean hasBuy = false;

        // 阅读按钮状态
        boolean downedOnShelfAndNotOnlineBook = hasBook
                && (job.getState() == DownBookJob.STATE_FINISHED)
                && book != null && !book.isOnlineBook();

        // epub书籍的判断逻辑，不与原来的交叉了，原来该怎么判断就怎么判断
        // 这里再做一次对epub书籍的判断并处理几个按钮的显示或隐藏即可。
        boolean isEpubBook = mBook.isEpub();
        if (isEpubBook) {
            mBookDownContainer.setVisibility(View.GONE);
            mEpubButton.setVisibility(View.VISIBLE);
            mReadBtn.setVisibility(View.GONE);
            mBuyButton.setVisibility(View.GONE);
            mCollectButton.setVisibility(View.GONE);

            if (hasBook) {
                if (book != null) {
                    mBook.getDownloadInfo().setDownLoadState(
                            book.getDownloadInfo().getDownLoadState());
                    mBook.getDownloadInfo().setProgress(
                            book.getDownloadInfo().getProgress());
                }
            }

            // 逻辑判断
            // 1.已经下载了并且在书架上并且不是在线书籍
            // 2.上面的判断逻辑还不太正确，遇到这种情况就不对了：
            // 在上一个版本中已经下载完成了这本书，肯定是dat文件后缀，并且下载状态等都是FINISHED的
            // 但是此时在新版本中，这本书籍变成了epub版本的，此时判断文件已经存在了是不对的，因为epub
            // 文件还没下载过，因此需要进一步判断
            // 3.判断逻辑：
            // 1）判断当前书籍的filePath路径信息是否是epub，如果是则说明书籍在当前新版本下已经下载过了，并且下载完成
            // 2）如果当前书籍的filePath路径信息不是epub的，而是temp(甚至dat)则此时并不知道temp文件是旧版本在下载txt时创建的还
            // 是在下载epub时创建的，因此，在重新下载书籍(不管恢复的下载还是重头开始的下载)时必须删除temp/dat文件并且下载
            // 进度，阅读进度等都要重置，这个逻辑在DownBookManager的startDownTask中实现了
            String filePath = hasBook ? book.getDownloadInfo().getFilePath()
                    : null;
            boolean isFileEndWithEpub = filePath != null
                    && filePath.endsWith(Book.EPUB_SUFFIX);
            downedOnShelfAndNotOnlineBook = downedOnShelfAndNotOnlineBook
                    && isFileEndWithEpub;
            if (downedOnShelfAndNotOnlineBook) {
                mEpubButton.setText(getString(R.string.read));
                mEpubButton.setEnabled(true);
                epubBtnState = EPUB_BTN_STATE_READ;
            }

            // 判断书籍购买类型
            if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
                // 免费
                mEpubButton.setText(getString(R.string.free_down));
                mEpubButton.setEnabled(true);
                epubBtnState = EPUB_BTN_STATE_DOWN;
                if (hasBook) {
                    if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                            || mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                        // 还在下载中
                        mEpubButton
                                .setText(getString(R.string.downloading_txt));
                        mEpubButton.setEnabled(false);
                    } else if (book != null
                            && !book.isOnlineBook()
                            && mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED
                            && isFileEndWithEpub) {
                        // 已经下载完成了
                        mEpubButton.setText(getString(R.string.read));
                        mEpubButton.setEnabled(true);
                        epubBtnState = EPUB_BTN_STATE_READ;
                    }
                }
            } else {
                // 收费(都是全本收费)
                hasBuy = PurchasedBookList.getInstance().isBuy(mBook)
                        || mBook.getBuyInfo().isHasBuy()
                        || DownBookManager.getInstance().hasBuy(mBook);
                mBook.getBuyInfo().setHasBuy(hasBuy);

                // 已经购买了书籍
                if (hasBuy) {
                    mEpubButton.setText(getString(R.string.free_down));
                    mEpubButton.setEnabled(true);
                    epubBtnState = EPUB_BTN_STATE_DOWN;
                    // 在书架上
                    if (hasBook) {
                        if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                                || mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                            // 还在下载中
                            mEpubButton
                                    .setText(getString(R.string.downloading_txt));
                            mEpubButton.setEnabled(false);
                        } else if (book != null
                                && !book.isOnlineBook()
                                && mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED
                                && isFileEndWithEpub) {
                            // 已经下载完成了
                            mEpubButton.setText(getString(R.string.read));
                            mEpubButton.setEnabled(true);
                            epubBtnState = EPUB_BTN_STATE_READ;
                        }
                    } else {
                        // 购买过了，但是还没有下载
                        epubBtnState = EPUB_BTN_STATE_DOWN;
                    }
                } else {
                    // 未购买
                    mEpubButton
                            .setText(getString(R.string.book_detail_epubbuyanddown));
                    mEpubButton.setEnabled(true);
                    epubBtnState = EPUB_BTN_STATE_BUYANDDOWN;
                }
            }
        } else {
            mBookDownContainer.setVisibility(View.VISIBLE);
            mBookProgressContainer.setVisibility(View.GONE);
            mReadBtn.setVisibility(View.VISIBLE);
            mEpubButton.setVisibility(View.GONE);
            mBuyButton.setVisibility(View.VISIBLE);
            mCollectButton.setVisibility(View.VISIBLE);

            if (downedOnShelfAndNotOnlineBook) {
                readText = getString(R.string.read);
            } else {
                readText = getString(R.string.free_read);
            }
            mReadBtn.setText(readText);

            // 加入书架（收藏）按钮状态
            if (DownBookManager.getInstance().hasBook(mBook)) {
                mCollectButton.setText(R.string.has_collected);
                mCollectButton.setTextColor(getResources().getColor(
                        R.color.book_detail_btn_grayed_color));
                mCollectButton.setEnabled(false);
            } else {
                if (mBook.isForbidden()) {
                    // 书籍已下架
                    mCollectButton.setEnabled(false);
                    mCollectButton.setTextColor(getResources().getColor(
                            R.color.book_detail_btn_grayed_color));
                } else {
                    mCollectButton.setEnabled(true);
                    mCollectButton.setTextColor(getResources().getColor(
                            R.color.book_detail_btn_font_color));
                }
                mCollectButton.setText(R.string.cloud_collect);
            }

            // 1 更新下载状态
            if (hasBook) {
                if (book != null) {
                    mBook.getDownloadInfo().setDownLoadState(
                            book.getDownloadInfo().getDownLoadState());
                    mBook.getDownloadInfo().setProgress(
                            book.getDownloadInfo().getProgress());
                }
            }

            // 2 设置按钮
            if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
                buyText = getString(R.string.free_buy_down);
                if (hasBook) {
                    if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                            || mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                        buyText = String
                                .format(getString(R.string.downloading_txt));
                        enableBuyButton = false;
                    } else if (book != null && !book.isOnlineBook()) {
                        buyText = String.format(getString(R.string.has_down));
                        enableBuyButton = false;
                    }
                }
            } else if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {
                buyText = getString(R.string.free_down);
                if (hasBook) {
                    if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                            || mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                        buyText = String
                                .format(getString(R.string.downloading_txt));
                        enableBuyButton = false;
                    } else if (book != null && !book.isOnlineBook()) {
                        buyText = String.format(getString(R.string.has_down));
                        enableBuyButton = false;
                    }
                }
            } else if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
                hasBuy = PurchasedBookList.getInstance().isBuy(mBook)
                        || mBook.getBuyInfo().isHasBuy()
                        || DownBookManager.getInstance().hasBuy(mBook);
                mBook.getBuyInfo().setHasBuy(hasBuy);
                if (hasBuy) {
                    buyText = getString(R.string.free_down);
                    if (hasBook) {
                        if (mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                                || mBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                            buyText = String
                                    .format(getString(R.string.downloading_txt));
                            enableBuyButton = false;
                        } else if (book != null && !book.isOnlineBook()) {
                            buyText = String
                                    .format(getString(R.string.has_down));
                            enableBuyButton = false;
                        }
                    }
                } else {
                    PriceTip tip = mBook.getBuyInfo().getPriceTip();
                    if (tip != null
                            && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
                        // 立即用券
                        buyText = getString(R.string.book_detail_usefee);
                    } else {
                        // 立即购买
                        buyText = getString(R.string.book_detail_buy);
                    }
                }
            }

            // 3 更新
            if (enableBuyButton) {
                if (mBook.isForbidden()) {
                    mBuyButton.setTextColor(getResources().getColor(
                            R.color.book_detail_btn_grayed_color));
                    mBuyButton.setEnabled(false);
                } else {
                    mBuyButton.setTextColor(getResources().getColor(
                            R.color.book_detail_btn_font_color));
                    mBuyButton.setEnabled(true);
                }
            } else {
                mBuyButton.setTextColor(getResources().getColor(
                        R.color.book_detail_btn_grayed_color));
                mBuyButton.setEnabled(false);
            }
            mBuyButton.setText(buyText);
        }
    }

    private void updateRelatedAuthorBook(List<Book> list) {
        if (list != null && list.size() > 0) {
            mRelatedAuthorAdapter.setList(list);
            mRelatedAuthorAdapter.notifyDataSetChanged();

            setHListLayout(mRelatedAuthorListView);
            mRelatedAuthorDivider.setBackgroundDrawable(mDotHDrawable);

            String authorText = String.format(
                    getResources().getString(R.string.book_detail_author_book),
                    mBook.getAuthor());
            mRelatedAuthorText.setText(authorText);

            mRelatedAuthorBook.setVisibility(View.VISIBLE);
        } else {
            mRelatedAuthorBook.setVisibility(View.GONE);
        }
    }

    private void updateRelatedPersonBook(List<Book> list) {
        if (list != null && list.size() > 0) {
            mRelatedPersonAdapter.setList(list);
            mRelatedPersonAdapter.notifyDataSetChanged();

            setHListLayout(mRelatedPersonListView);
            mRelatedPersonDivider.setBackgroundDrawable(mDotHDrawable);

            String personText = String.format(
                    getResources().getString(R.string.book_detail_person_book),
                    mBook.getAuthor());
            mRelatedPersonText.setText(personText);

            mRelatedPersonBook.setVisibility(View.VISIBLE);
        } else {
            mRelatedPersonBook.setVisibility(View.GONE);
        }
    }

    // 关键词的标签区块
    private void updateRelatedKeyWord(List<String> list) {
        if (list != null && list.size() != 0) {
            mKeyWords.addAll(list);
            mRelatedKeyWordListView.setString(list);
            mRelatedKeyWordListView.notifyChanged();

            mRelatedKeyWordDivider.setBackgroundDrawable(mDotHDrawable);

            mRelateKeyWordTitle.setVisibility(View.VISIBLE);
            mRelatedKeyWord.setVisibility(View.VISIBLE);
        } else {
            mRelateKeyWordTitle.setVisibility(View.GONE);
            mRelatedKeyWord.setVisibility(View.GONE);
        }
    }

    private void updateRelatedContent() {
        if (mBook != null && mData != null) {
            updateRelatedAuthorBook(mData.getAuthorItem());
            updateRelatedPersonBook(mData.getCateItems());
            updateRelatedKeyWord(mData.getTags());
        }
    }

    // 更新目录列表
    private void updateChapterList() {
        // 添加对epub的判断，如果是epub书籍，则不显示目录
        if (mBook != null && mChapterList != null
                && mChapterList.getChapters() != null
                && mChapterList.getChapters().size() > 0 && !mBook.isEpub()) {
            if (mBook.isHtmlRead()) {
                // html图文阅读，不显示目录UI
                mCatalog.setVisibility(View.GONE);
            } else {
                mCatalog.setVisibility(View.VISIBLE);
            }

            Book book = DownBookManager.getInstance().getBook(mBook);
            if (book != null) {
                // 已加入书架书籍
                if (!book.isOnlineBook()
                        && book.getDownloadInfo().getFilePath() != null
                        && book.getDownloadInfo().getFilePath()
                        .endsWith(Book.BOOK_SUFFIX)) {
                    // 已下载书籍
                    if (book == mBook) {
                        // 对象一致，不赋值，防止清掉下载书籍的章节信息(如索引，长度)

                    } else {
                        mBook.setChapters(mChapterList.getChapters(),
                                "[BookDetailActivity-updateChapterList]");
                    }
                } else {
                    // 在线书籍
                    mBook.setChapters(mChapterList.getChapters(),
                            "[BookDetailActivity-updateChapterList]");
                    if (book != mBook
                            && (book.getChapters() == null || book
                            .getChapters().size() == 0)) {
                        // 目录还没下载完成，就点击“加入书架”，书架书籍的章节列表有可能不存在
                        book.setChapters(mChapterList.getChapters(),
                                "[BookDetailActivity-updateChapterList]");
                    }
                }
            } else {
                // 没有加入书架的书籍
                mBook.setChapters(mChapterList.getChapters(),
                        "[BookDetailActivity-updateChapterList]");
            }

            // // 修复BugID=21356
            // Book book = DownBookManager.getInstance().getBook(mBook);
            // if (book != null && book == mBook) {
            // // 点击立即阅读进去了。
            // } else {
            // // CJL 暂时注释
            // mBook.setChapters(mChapterList.getChapters(),
            // "[BookDetailActivity-updateChapterList]");
            // }

            if (!mBook.isHtmlRead()) {
                updateCatalogUI();
            }
        } else {
            mCatalog.setVisibility(View.GONE);
        }
    }

    private void updateCatalogUI() {
        if (mBook.isUpdateChapter()) {
            mCatalogLayout1.setVisibility(View.GONE);
            mCatalogLayout2.setVisibility(View.VISIBLE);

            String updateTime = String.format(
                    getString(R.string.book_detail_update_time), Util
                            .getTimeToDisplay(mBook.getBookUpdateChapterInfo()
                                    .getUpdateTime()));

            mCatalogUpdateTime.setText(updateTime);
            mCatalogUpdateChapter.setText(mBook.getBookUpdateChapterInfo()
                    .getTitle());
        } else {
            mCatalogLayout1.setVisibility(View.VISIBLE);
            mCatalogLayout2.setVisibility(View.GONE);
        }
    }

    private void updateCommentList(int total, List<CommentItem> items) {
        mAdapter.setTotalAndPerpage(total, MAX_COMMENTS_ITEMS);
        // 限制评论最多显示6条
        List<CommentItem> comments = items;
        if (comments != null && comments.size() > 6) {
            List<CommentItem> newComments = new ArrayList<CommentItem>();
            for (int i = 0; i < 6; i++) {
                newComments.add(comments.get(i));
            }
            comments = newComments;
        }
        mAdapter.addList(comments);
        mAdapter.notifyDataSetChanged();
        mListView.notifyDataSetChanged();

        if (mAdapter.hasMore()) {
            mMoreCommentTv.setVisibility(View.VISIBLE);
        } else {
            mMoreCommentTv.setVisibility(View.GONE);
        }

        // 刷新评论的次数
//        mCommentNum.setText(String.valueOf(total));
    }

    private void updatePraiseData() {
        Toast toast = Toast.makeText(this, "   +1   ", Toast.LENGTH_SHORT);
        int location[] = new int[2];
        mPraiseBtn.getLocationOnScreen(location);
        int xOffset = location[0];
        int yOffset = location[1];
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, yOffset + 10);
//        toast.setGravity(Gravity.TOP, -xOffset + 10, yOffset + 10);
        toast.show();

        mPraiseBtn.setText(getResources().getString(
                R.string.book_detail_has_praised));
        mPraiseBtn.setTextColor(getResources().getColor(
                R.color.book_detail_checked_font_color));

        Drawable drawable = getResources().getDrawable(
                R.drawable.book_detail_praise_pressed);
        drawable.setBounds(0, 0, PRAISE_COMMENT_BOUND, PRAISE_COMMENT_BOUND);
        mPraiseBtn.setCompoundDrawables(drawable, null, null, null);

        mPraiseBtn.setEnabled(false);
        int praiseNum = Integer.parseInt((String) mPraiseNum.getText()) + 1;
        mPraiseNum.setText(praiseNum + "");
    }

    private void updateBookPrice(Book book) {
        if (mBook != null && book != null) {
            mBook.getBuyInfo().setPayType(book.getBuyInfo().getPayType());
            mBook.getBuyInfo().setPrice(book.getBuyInfo().getPrice());
            mBook.getBuyInfo().setDiscountPrice(
                    book.getBuyInfo().getDiscountPrice());
            mBook.getBuyInfo().setPriceTip(book.getBuyInfo().getPriceTip());
            mBook.getBuyInfo().setHasBuy(book.getBuyInfo().isHasBuy());

            if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
                mBookPrice.setVisibility(View.VISIBLE);
                mBookPrice.setText(getString(R.string.book_price));

                String price = getString(R.string.book_detail_free);
                int color = getResources().getColor(R.color.book_free_color);
                Spanned spanned = Util.highLight(price, color, 0,
                        price.length());
                mBookPrice.append(spanned);
            } else if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
                mBookPrice.setVisibility(View.VISIBLE);
                String price = getString(R.string.book_price)
                        + mBook.getBuyInfo().getPrice()
                        + getString(R.string.u_bi_name);
                mBookPrice.setText(price);
            } else if (mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {
                mBookPrice.setVisibility(View.GONE);
            }
        }
    }

    private void updatePraiseOrBuyState(String type) {
        if (mBook == null) {
            return;
        }

        if (KEY_SHOW_PRAISE.equals(type)) {
            if (mBook.hasPraised()) {
                shortToast(R.string.book_detail_has_praised_note);
            } else {
                praise();
            }
        } else if (KEY_SHOW_DIALOG.equals(type)) {
            onBuyButtonClick(true);
        }

    }

    private void initListener() {
        // 在线试读，立即阅读、下载阅读
        mReadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadBtnClick();
            }
        });

        // 立即下载、立即购买
        mBuyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuyButtonClick();
            }
        });

        // 加入书架
        mCollectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                collect();

                UserActionManager.getInstance().recordEvent(
                        Constants.CLICK_COLLECT);
            }
        });

        // epub书籍的“购买并下载”or“立即下载”or“阅读”
        mEpubButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 下架了
                if (mBook.isForbidden()) {
                    // Toast提示
                    shortToast(R.string.book_detail_has_forbidden);
                } else {
                    switch (epubBtnState) {
                        case EPUB_BTN_STATE_READ:
                            Book book = DownBookManager.getInstance()
                                    .getBook(mBook);
                            boolean handle = EpubHelper.i
                                    .checkFileExistsBeforeRead(
                                            BookDetailActivity.this, book,
                                            new EpubHelper.DefaultListener() {
                                                // 不需要实现移除书架方法，因为没这个功能需求
                                                // @Override
                                                // public void removeBook() {
                                                // initDownLoadBtnText();
                                                // }

                                                @Override
                                                public void redownloadBook() {
                                                    initDownLoadBtnText();
                                                }
                                            }, false);
                            if (!handle) {
                                if (book != null
                                        && book.getDownloadInfo()
                                        .getOriginalFilePath() != null) {
                                    // 进入阅读
                                    float precent = book.getReadInfo()
                                            .getLastReadPercent();
                                    boolean hasPrecent = precent > 0.0f ? true
                                            : false;
                                    String path = book.getDownloadInfo()
                                            .getOriginalFilePath();
                                    FBReader.openBookActivity(
                                            BookDetailActivity.this, path,
                                            hasPrecent, book.getBookId());
                                }
                            }
                            break;
                        case EPUB_BTN_STATE_BUYANDDOWN:
                            // 购买并下载
                            onBuyButtonClick();
                            break;
                        case EPUB_BTN_STATE_DOWN:
                            // 立即下载
                            downBook();
                            break;
                    }
                }
            }
        });

        mShowPartLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowPartLayoutClick();
            }
        });
        mPraiseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPraiseBtnClick();
            }
        });

        // 点击 ”我要评论“
//        mCommentBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onCommentBtnClick();
//            }
//        });
        mMoreCommentTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMoreCommentTvClick();
            }
        });
        mBookIntroText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBookIntroTextClick();
            }
        });
        mPraiseCommentBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onPraiseCommentBtnClick();
            }
        });

        // 书籍分类
        mBookCate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBookCateClick();
            }
        });
        mCatalogLayout1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                enterBookTagActivity();
            }
        });

        // 点击 目录
        mCatalogBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                enterBookTagActivity();
            }
        });

        // 最新章节
        mCatalogChapterBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onCatalogChapterBtnClick();
            }
        });
    }

    private void downBook() {
        boolean hasBook = false;
        Book shelfBook = DownBookManager.getInstance().getBook(mBook);
        if (shelfBook != null) {
            hasBook = true;
        }
        String filePath = hasBook ? shelfBook.getDownloadInfo().getFilePath()
                : null;
        boolean isFileEndWithDat = filePath != null
                && filePath.endsWith(Book.BOOK_SUFFIX);
        if (isFileEndWithDat) {
            // 已经是完整的dat资源，说明这次下载的肯定是epub，直接覆盖
            DownBookManager.getInstance().downBook(mBook, false, true);
        } else {
            DownBookJob job = DownBookManager.getInstance().getJob(mBook);
            // 书架上已经有下载任务了，并且没下完
            if (job != null && shelfBook.isEpub()) {
                // epub的书籍只下载了一部分，继续下载
                job.start();
                // 发出通知
                DownloadBookNotification.getInstance().addNotification(
                        job.getBook());
            } else {
                // 重新下载
                DownBookManager.getInstance().downBook(mBook, false, true);
            }
        }
        initDownLoadBtnText();
    }

    /**
     * 获取书籍信息
     */
    private void reqBookInfo() {
        reqBookInfo("");
    }

    /**
     * 发送搜索书本请求
     */
    private void reqBookInfo(String type) {
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO, mBookId,
                mBookSid, mBookSrc);
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }

        RequestTask reqTask = new RequestTask(new BookDetailParser());
        reqTask.setTaskFinishListener(this);
        reqTask.setType(type);
        reqTask.bindActivity(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
        mHttpReturnCount = 0;
        mProgressView.setVisibility(View.VISIBLE);
    }

    /**
     * 发送搜索书本相关书籍的请求
     */
    private void reqBookExt() {
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO_EXT, mBookId,
                mBookSid, mBookSrc);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }
        RequestTask reqTask = new RequestTask(new BookRelatedParser());
        reqTask.setTaskFinishListener(this);
        reqTask.bindActivity(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    /**
     * 发送获取评论请求
     */
    private void reqComments() {
        String reqUrl = String.format(ConstantData.URL_COMMENTS, mBookId,
                mBookSid, mBookSrc, 1, MAX_COMMENTS_ITEMS);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }

        RequestTask reqTask = new RequestTask(new CommentsParser());
        reqTask.bindActivity(this);
        reqTask.setTaskFinishListener(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    /**
     * 发送获取目录的请求
     */
    private void reqChapterList() {
        String reqUrl = String.format(ConstantData.URL_GET_CHAPTERS, mBookId,
                mBookSid, mBookSrc);
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }

        ChapterListParser chaptersParser = new ChapterListParser();
        if (mBook != null
                && mBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
            chaptersParser.setAllBookHasBuy(mBook.getBuyInfo().isHasBuy());
        }

        RequestTask reqTask = new RequestTask(chaptersParser);
        reqTask.bindActivity(this);
        reqTask.setTaskFinishListener(this);

        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    /**
     * 赞一个
     */
    private void reqPraise() {
        RequestTask reqTask = new RequestTask(new SimpleParser());
        reqTask.bindActivity(this);
        TaskParams params = new TaskParams();

        String url = ConstantData
                .addLoginInfoToUrl(ConstantData.URL_PRAISE_POST);
        params.put(RequestTask.PARAM_URL, url);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

        ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
        strNParams.add(new BasicNameValuePair("bid", mBook.getBookId()));
        strNParams.add(new BasicNameValuePair("src", mBook.getBookSrc()));
        strNParams.add(new BasicNameValuePair("sid", mBook.getSid()));

        reqTask.setPostParams(strNParams);
        reqTask.setTaskFinishListener(this);
        reqTask.setType(REQ_PRAISE);
        reqTask.execute(params);

        UserActionManager.getInstance().recordEvent(Constants.CLICK_PARISE);
    }

    /**
     * 请求作者信息
     */
    private void reqAuthorInfo() {
        String reqUrl = String.format(ConstantData.URL_AUTHOR_CHECK, mBookId);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }

        RequestTask reqTask = new RequestTask(new AuthorInfoParser());
        reqTask.bindActivity(this);
        reqTask.setTaskFinishListener(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    /**
     * 获取书籍价格
     */
    private void reqBookPrice() {
        reqBookPrice("");
    }

    /**
     * 请求书籍价格
     */
    private void reqBookPrice(String type) {
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO_CHECK,
                mBookId, mBookSid, mBookSrc);
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }

        RequestTask reqTask = new RequestTask(new BookPriceParser());
        reqTask.bindActivity(this);
        reqTask.setTaskFinishListener(this);
        reqTask.setType(type);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    private void exchangeBookFilePathInfo() {
        if (mReadActivityBook != null) {
            BookDownloadInfo info = mReadActivityBook.getDownloadInfo();
            if (info != null) {
                mBook.getDownloadInfo().setFilePath(info.getFilePath());
                mBook.getDownloadInfo().setOriginalFilePath(
                        info.getOriginalFilePath());
                mBook.setChapters(mReadActivityBook.getChapters(),
                        "[BookDetailActivity-exchangeBookFilePathInfo]");

                // 解决：BugID=21490
                // 保存书签，书摘，阅读进度等
                // mBook.getReadInfo().setLastReadJsonString(mReadActivityBook.getReadInfo().getLastReadJsonString());
                // mBook.setBookmarks(mReadActivityBook.getBookmarks());
                // mBook.setBookSummaries(mReadActivityBook.getBookSummaries());
            }
        }
    }

    // 加入书架 即收藏
    private void collect() {
        int chaptersSize = mBook.getNum();
        if (chaptersSize <= 0) {
            // 有的书籍0章节
            Toast.makeText(this, R.string.bookdetail_failed_text,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //
        exchangeBookFilePathInfo();

        CloudSyncUtil.getInstance().add2CloudAndShelves(mContext, mBook,
                new ITaskFinishListener() {
                    @Override
                    public void onTaskFinished(TaskResult taskResult) {
                        mCollectButton.setText(R.string.has_collected);
                        mCollectButton.setTextColor(getResources().getColor(
                                R.color.book_detail_btn_grayed_color));
                        mCollectButton.setEnabled(false);
                    }
                });
    }

    /**
     * 设置水平ListView的高度
     *
     * @param listView
     */
    private void setHListLayout(HorizontalListView listView) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = BookImageAdapter2.ITEM_HEIGHT;
        listView.setLayoutParams(params);

        listView.setVisibility(View.VISIBLE);
        float width = ReadStyleManager.getInstance(this).getScreenWidth() - 2
                * PixelUtil.dp2px(13);
        double dividerWidth = (width - 3.5 * BookImageAdapter2.ITEM_WIDTH) / 3;
        listView.setDividerWidth((int) dividerWidth);
    }

    @Override
    public void onItemClick(int position) {
        if (position >= 0 && position < mKeyWords.size()) {
            SearchResultActivity.launch(BookDetailActivity.this,
                    mKeyWords.get(position));
            cancelDownload();

            UserActionManager.getInstance().recordEvent(
                    Constants.CLICK_RELATED_WORD);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 推荐
    private void praise() {
        if (!HttpUtil.isConnected(this)) {
            shortToast(R.string.network_error);
            return;
        }

        showProgress(R.string.book_detail_praising, false);
        reqPraise();

        if (StorageUtil.getBoolean(StorageUtil.KEY_AUTO_WEIBO)) {
            autoShareWeibo();
        }
    }

    // 摘要页分享
    private void shareBook() {
        if (mBook == null || mBook.isForbidden()) {
            return;
        }

        new GenericTask() {
            WeiboContent mContent;

            @Override
            protected void onPreExecute() {
                showProgress(R.string.create_share_content, true);
            }

            @Override
            protected TaskResult doInBackground(TaskParams... params) {
                mContent = new WeiboContent(mBook,
                        WeiboContent.TYPE_BOOK_DETAIL);

                Bitmap cover = ImageLoader.getInstance().syncLoadBitmap(
                        mBook.getDownloadInfo().getImageUrl());
                if (null != cover && cover != ImageLoader.getDefaultPic()) {
                    mContent.setImagePath(Util.saveBitmap2file(cover,
                            mBook.getTitle(), 100));
                }

                mContent.setChapterId(Chapter.DEFAULT_GLOBAL_ID);
                mContent.setChapterOffset(0);

                // 其他书籍分享简介
                mContent.setMsg(mBook.getIntro());

                return null;
            }

            @Override
            protected void onPostExecute(TaskResult result) {
                dismissProgress();

                if (null != mContent) {
                    ShareDialog.show(BookDetailActivity.this, mContent);
                } else {
                    Toast.makeText(BookDetailActivity.this,
                            R.string.create_share_content_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /**
     * 自动发送微博
     */
    private void autoShareWeibo() {
        if (LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS)
            return;

        WeiboContent content = new WeiboContent(mBook, WeiboContent.TYPE_PRAISE);
        content.setMsg(mBook.getIntro());
        content.setChapterId(Chapter.DEFAULT_GLOBAL_ID);
        content.setChapterOffset(0);
        String text = content.getMsg();

        // 推荐的分享微博去掉图片，默认接口推图书card
        // Bitmap bitmap = ImageLoader.getInstance().syncLoadBitmap(
        // mBook.getDownloadInfo().getImageUrl());
        // bitmap = (null == bitmap) ? ImageLoader.getDefaultPic() : bitmap;

        RequestTask task;
        // if (null != bitmap && !bitmap.equals(ImageLoader.getDefaultPic())) {
        // task = new RequestTask(new SimpleParser(), bitmap);
        // } else {
        task = new RequestTask(new SimpleParser());
        // }

        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, ConstantData.URL_SHARE_WEIBO);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

        ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
        strNParams.add(new BasicNameValuePair("authcode",
                ConstantData.AUTH_CODE));
        strNParams.add(new BasicNameValuePair("u_id", LoginUtil.getLoginInfo()
                .getUID()));
        strNParams.add(new BasicNameValuePair("access_token", LoginUtil
                .getLoginInfo().getAccessToken()));
        strNParams.add(new BasicNameValuePair("sid", mBook.getSid()));
        strNParams.add(new BasicNameValuePair("b_id", mBook.getBookId()));
        strNParams.add(new BasicNameValuePair("b_src", mBook.getBookSrc()));
        strNParams.add(new BasicNameValuePair("c_offset", ""));
        strNParams.add(new BasicNameValuePair("u_comment", text));

        task.setPostParams(strNParams);
        task.setTaskFinishListener(this);
        task.setType(REQ_SHARE_WEIBO);
        task.bindActivity(this);
        task.execute(params);
    }

    // 下载管理器
    private HtmlDownBookManager manager;

    // html图书，判断是否不能删除sdcard数据
//	private boolean disableDeleteFile = false;
    private void onReadBtnClick() {
        ReadActivity.setChapterReadEntrance("书籍详情页-在线试读");
        if (mBook.isHtmlRead()) {
            /**
             * Html阅读，点击阅读按钮
             */
            if (mChapterList != null) {
                mBook.setChapters(mChapterList.getChapters());
            }
            Book book = HtmlDownManager.getInstance().getBook(mBook);
            Book dbook = DownBookManager.getInstance().getBook(mBook);
//			Book dbook = DBService.getBook(mBook);
            if (dbook == null) {
                /*
                 *  未加入书架
				 */
                if (book != null && book.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {
                    // 已经下载完成，立即阅读（FBReader）
                    float precent = 0.0f;
                    if (book.getReadInfo() != null) {
                        precent = book.getReadInfo().getLastReadPercent();
                    }
//					float precent = book.getReadInfo().getLastReadPercent();
                    boolean hasPrecent = precent > 0.0f ? true : false;

//					disableDeleteFile = true;
                    String path = book.getDownloadInfo().getOriginalFilePath();
                    FBReader.openBookActivity(
                            BookDetailActivity.this, path, book,
                            hasPrecent);
                } else {
                    // 书籍未下载完成或者为在线书籍，开始下载
                    String baseDir = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK);
                    String bookDirName = "/bd" + mBook.getBookId() + ".epub";
                    String path = baseDir + bookDirName;
                    mBook.getDownloadInfo().setDownloadTime(new Date().getTime());
                    mBook.getDownloadInfo().setOriginalFilePath(path);
                    mBook.getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
                    mBook.getDownloadInfo().setProgress(0.0);
                    mBook.setOnlineBook(false);

                    // 存储数据库
                    DownBookJob job = DownBookManager.getInstance().getJob(mBook);
                    if (job != null) {
                        job.getBook().setOnlineBook(false);
                        DBService.saveBook(job.getBook());
                    }

                    // 启动下载
                    HtmlDownManager.getInstance().downBook(mBook, true, true);
                }
            } else {
                /*
                 *  已加入书架
				 */
                boolean hasDownloadComplete = false;
                if (!dbook.isOnlineBook()
                        && dbook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED
                        && Math.abs(dbook.getDownloadInfo().getProgress() - 1.0) < 0.0001) {
                    // 下载完成
                    hasDownloadComplete = true;
                }

                if (hasDownloadComplete) {
                    // 判断是否更新
                    if (mChapterList == null) {
						/*
						 *  摘要页的目录接口还没返回，就点击了阅读按钮。
						 *  由于这时候的数据库状态是下载完成的，就当做没有最新章节处理。
						 */

                    } else {
						/*
						 * 下载完成状态下，摘要页的目录列表和数据库的目录列表比较。
						 * 章节数目不相等， 就认为是有更新章节，需要下载一遍，再进入阅读
						 */
                        ArrayList<Chapter> dChapterList = DBService.getAllChapter(dbook);
                        ArrayList<Chapter> cChapterList = mChapterList.getChapters();
                        if (dChapterList != null && cChapterList != null
                                && dChapterList.size() != cChapterList.size()) {
                            hasDownloadComplete = false;
                        }
                    }
                }

                if (hasDownloadComplete) {
                    float precent = 0.0f;
                    if (dbook.getReadInfo() != null) {
                        precent = dbook.getReadInfo().getLastReadPercent();
                    }
//					float precent = book.getReadInfo().getLastReadPercent();
                    boolean hasPrecent = precent > 0.0f ? true : false;

//					disableDeleteFile = true;
                    String path = dbook.getDownloadInfo().getOriginalFilePath();
                    FBReader.openBookActivity(
                            BookDetailActivity.this, path, dbook,
                            hasPrecent);
                } else {
                    // 在线书籍
                    String baseDir = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK);
                    String bookDirName = "/bd" + mBook.getBookId() + ".epub";
                    String path = baseDir + bookDirName;
                    mBook.getDownloadInfo().setDownloadTime(new Date().getTime());
                    mBook.getDownloadInfo().setOriginalFilePath(path);
                    mBook.getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
                    mBook.getDownloadInfo().setProgress(0.0);
                    mBook.setOnlineBook(false);

                    // 存储数据库
                    DownBookJob job = DownBookManager.getInstance().getJob(mBook);
                    if (job != null) {
                        job.getBook().setOnlineBook(false);
                        DBService.saveBook(job.getBook());
                    }

                    // 启动下载
                    HtmlDownManager.getInstance().downBook(mBook, true, true);
                }
            }
//			if (book != null && book.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {
//				// 已经下载完成，立即阅读（FBReader）
//				float precent = book.getReadInfo().getLastReadPercent();
//				boolean hasPrecent = precent > 0.0f ? true : false;
//
//				String path = book.getDownloadInfo().getOriginalFilePath();
//				FBReader.openBookActivity(
//						BookDetailActivity.this, path, book,
//						hasPrecent);
//			} else {
//				// 书籍未下载完成或者为在线书籍，开始下载
//				String baseDir = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK);
//				String bookDirName = "/bd" + mBook.getBookId() + ".epub";
//				String path = baseDir + bookDirName;
//				mBook.getDownloadInfo().setDownloadTime(new Date().getTime());
//				mBook.getDownloadInfo().setOriginalFilePath(path);
//				mBook.getDownloadInfo().setVDiskDownUrl(Book.HTML_READ_PROTOCOL);
//				mBook.getDownloadInfo().setProgress(0.0);
//				mBook.setOnlineBook(false);
//				
//				// 存储数据库
//				DownBookJob job = DownBookManager.getInstance().getJob(mBook);
//				if(job != null){
//					job.getBook().setOnlineBook(false);
//					DBService.saveBook(job.getBook());
//				}
//				
//				
//				// 启动下载
//				HtmlDownManager.getInstance().downBook(mBook, true, true);
//			}
            return;
        }

        Book book = DownBookManager.getInstance().getBook(mBook);
        if (book != null
                && !book.isOnlineBook()
                && book.getDownloadInfo().getFilePath() != null
                && book.getDownloadInfo().getFilePath()
                .endsWith(Book.BOOK_SUFFIX)) {
            // if (book != null && Math.abs(book.getDownloadInfo().getProgress()
            // - 1.0) < 0.0001
            // && book.getDownloadInfo().getDownLoadState() ==
            // DownBookJob.STATE_FINISHED) {
            // 如果是读的书架上的下载好的书，需要更新该book的最后阅读时间等属性
            mBook = book;
            ReadActivity.launch(mContext, mBook, false, null, false, mBookKey);
        } else {
            // 否则认为是在线试读
            ReadActivity.launch(mContext, mBook, true, null, false, mBookKey);
            UserActionManager.getInstance().recordEvent(
                    Constants.CLICK_READ_ONLINE);
        }
    }

    private void onBuyButtonClick() {
        onBuyButtonClick(false);
    }

    private void onBuyButtonClick(boolean waitSyncShelvesBookData) {
        int chaptersSize = mBook.getNum();
        if (chaptersSize <= 0 && mBook.isEpub()) {
            // 有的书籍0章节
            Toast.makeText(this, R.string.bookdetail_failed_text,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 无网络，拦截操作
        if(!HttpUtil.isConnected(this)){
            Toast.makeText(this, "网络异常，下载失败",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int payType = mBook.getBuyInfo().getPayType();
        if (payType == Book.BOOK_TYPE_FREE
                || payType == Book.BOOK_TYPE_CHAPTER_VIP) {
            exchangeBookFilePathInfo();
            // 全本免费或者章节收费
            CloudSyncUtil.getInstance().add2Cloud(mContext, mBook);
            DownBookManager.getInstance().downBook(mBook, false, true);
            initDownLoadBtnText();
        } else {
            // 整本收费
            if (!mBook.getBuyInfo().isHasBuy()) {
                // 执行购买流程
                mBook.setAutoDownBook(false);
                // CJL 把书籍的价格临时更改一下，让其重新去获取一下价格信息
                PriceTip tip = mBook.getBuyInfo().getPriceTip();

                if (tip == null
                        || tip.getBuyType() != PriceTip.TYPE_DISCOUNT_NINE) {
                    mBook.getBuyInfo().setPrice(BookBuyInfo.BOOK_DEFAUTL_PRICE);
                }

                PayDialog dlg = new PayDialog(BookDetailActivity.this, mBook,
                        null, null, true);
                dlg.setOnPayLoginSuccessListener(new PayDialog.PayLoginSuccessListener() {

                    @Override
                    public void onLoginSuccess() {
                        reqBookInfo();
                        reqBookPrice(KEY_SHOW_DIALOG);
                    }
                });
                dlg.setOnPayFinishListener(new PayDialog.PayFinishListener() {
                    public void onFinish(int code) {
                        if (PayDialog.CODE_SUCCESS == code) {
                            initDownLoadBtnText();

                            PriceTip tip = mBook.getBuyInfo().getPriceTip();
                            if (tip != null
                                    && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
                                // 兑书券订购成功，弹框提示
                                double price = mBook.getBuyInfo().getPrice();
                                String str_price = String.valueOf(price);

                                String str_bookcard = mContext
                                        .getString(R.string.buy_bookcard_success);
                                str_bookcard = String.format(str_bookcard,
                                        str_price);

                                int color = ResourceUtil
                                        .getColor(R.color.pay_dialog_price_font_color);
                                SpannableString spanString = new SpannableString(
                                        str_bookcard);
                                int end = str_bookcard.length() - 3;
                                int start = end - str_price.length();
                                ForegroundColorSpan span = new ForegroundColorSpan(
                                        color);
                                spanString.setSpan(span, start, end,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                String left = mContext
                                        .getString(R.string.iknowed);
                                CommonDialog.show(mContext, null, spanString,
                                        left, null,
                                        new CommonDialog.DefaultListener());
                            } else {
                                shortToast(R.string.buy_success);
                            }
                        }
                    }
                });
                dlg.show();
            } else {
                // 已经购买了，可以直接下载
                exchangeBookFilePathInfo();
                // 是否要等待用户书架数据同步完成
                // 书架同步完成后会发出一个广播，摘要页接收广播启动下载
                if (waitSyncShelvesBookData) {
                    // 用户已经购买过这本书籍了，此时可以直接下载
                    isUserHasPayForBookAndThenDownBook = true;
                    // 这里可能走的比CloudSyncUtil晚，因此判断一下
                    if (!hasRereivedSyncAction) {
                        LogUtil.d("CursorError",
                                "摘要页 >> DialogUtils >> 正在同步您的书架，请稍候...");
                        DialogUtils.showProgressDialog(this, "正在同步您的书架，请稍候...",
                                false, false, null, null);
                    }
                    // 因为先后顺序不知道，所以两个地方都要调用handleReceiverOrTaskResult方法
                    handleReceiverOrTaskResult();
                } else {
                    // Book shelfBook = DownBookManager.getInstance().getBook(
                    // mBook);
                    // if (shelfBook != null) {
                    // mBook = shelfBook;
                    // }
                    CloudSyncUtil.getInstance().add2Cloud(mContext, mBook);
                    DownBookManager.getInstance().downBook(mBook, false, true);
                    initDownLoadBtnText();
                }
            }
        }

        UserActionManager.getInstance()
                .recordEvent(Constants.CLICK_DOWN_OR_BUY);
    }

    // 评论点击
    private void onCommentBtnClick() {
        cancelDownload();

        Intent intent = new Intent();
        intent.setClass(mContext, SendCommentsPostActivity.class);
        intent.putExtra("book", mBook);
        startActivity(intent);

//		if (mBook != null && mBook.isHtmlRead()) {
//			// 如果是html的下载阅读，点击评论跳转activity后，下载还是保持，但是下载完成，阻止其进入阅读页
//			HtmlDownBookManager manager = HtmlDownManager.getInstance().getJob(mBook);
//			if(manager != null){
//				manager.setIsRead(false);
//			}
//		}

        UserActionManager.getInstance().recordEvent(
                Constants.CLICK_COMMENT_DETAIL);
    }

    private void onPraiseBtnClick() {
        if (LoginUtil.isValidAccessToken(BookDetailActivity.this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
            LoginDialog.launch(BookDetailActivity.this,
                    new LoginDialog.LoginStatusListener() {
                        @Override
                        public void onSuccess() {
                            reqBookInfo(KEY_SHOW_PRAISE);
                            reqBookPrice();
                        }

                        @Override
                        public void onFail() {

                        }
                    });
        } else {
            praise();
        }
    }

    private void onShowPartLayoutClick() {
        if (mBookInfo.isEllipsized()) {
            mBookInfo.setMaxLines(Integer.MAX_VALUE);
            mShowAllTv.setText(R.string.hide);
            mShowAllIv.setImageResource(R.drawable.book_detail_arrow_close);
        } else {
            mBookInfo.setMaxLines(MAX_LINE_INFO);
            mShowAllTv.setText(R.string.show_all);
            mShowAllIv.setImageResource(R.drawable.book_detail_arrow_open);
        }
        mBookInfo.setText(mBook.getIntro());
    }

    private void onMoreCommentTvClick() {
        if (mAdapter.hasMore()) {
            Intent intent = new Intent();
            intent.setClass(mContext, CommentListActivity.class);
            intent.putExtra("book", mBook);
            startActivity(intent);
        }
    }

    // 点击详情页推荐评论，显示推荐评论视图
    private void onPraiseCommentBtnClick() {
        mBookIntroText.setTextColor(mContext.getResources().getColor(
                R.color.book_detail_praise_unchoosed_color));
        mBookIntroText.setBackgroundDrawable(mContext.getResources()
                .getDrawable(R.drawable.book_detail_left_tab_bg_normal));
        mPraiseCommentBtn.setBackgroundDrawable(mContext.getResources()
                .getDrawable(R.drawable.book_detail_right_tab_bg_pressed));

        mPraiseNum.setTextColor(getResources().getColor(
                R.color.book_detail_praise_choosed_color));
//        mCommentNum.setTextColor(getResources().getColor(
//                R.color.book_detail_praise_choosed_color));

        Drawable drawable1 = getResources().getDrawable(
                R.drawable.book_detail_praise_num);
        drawable1.setBounds(0, 0, PRAISE_COMMENT_NUM_BOUND,
                PRAISE_COMMENT_NUM_BOUND);
        mPraiseNum.setCompoundDrawables(drawable1, null, null, null);

//        Drawable drawable2 = getResources().getDrawable(
//                R.drawable.book_detail_comment_num);
//        drawable2.setBounds(0, 0, PRAISE_COMMENT_NUM_BOUND,
//                PRAISE_COMMENT_NUM_BOUND);
//        mCommentNum.setCompoundDrawables(drawable2, null, null, null);

        mPraiseCommentLayout.setVisibility(View.VISIBLE);
        mShowAllLayout.setVisibility(View.GONE);
    }

    private void onBookIntroTextClick() {
        mBookIntroText.setTextColor(mContext.getResources().getColor(
                R.color.book_detail_praise_choosed_color));
        mBookIntroText.setBackgroundDrawable(mContext.getResources()
                .getDrawable(R.drawable.book_detail_left_tab_bg_pressed));
        mPraiseCommentBtn.setBackgroundDrawable(mContext.getResources()
                .getDrawable(R.drawable.book_detail_right_tab_bg_normal));

        mPraiseNum.setTextColor(getResources().getColor(
                R.color.book_detail_praise_unchoosed_color));
//        mCommentNum.setTextColor(getResources().getColor(
//                R.color.book_detail_praise_unchoosed_color));

        Drawable drawable1 = getResources().getDrawable(
                R.drawable.book_detail_praise_num_normal);
        drawable1.setBounds(0, 0, PRAISE_COMMENT_NUM_BOUND,
                PRAISE_COMMENT_NUM_BOUND);
        mPraiseNum.setCompoundDrawables(drawable1, null, null, null);

//        Drawable drawable2 = getResources().getDrawable(
//                R.drawable.book_detail_comment_num_normal);
//        drawable2.setBounds(0, 0, PRAISE_COMMENT_NUM_BOUND,
//                PRAISE_COMMENT_NUM_BOUND);
//        mCommentNum.setCompoundDrawables(drawable2, null, null, null);

        mShowAllLayout.setVisibility(View.VISIBLE);
        mPraiseCommentLayout.setVisibility(View.GONE);
    }

    private void onBookCateClick() {
        PartitionDetailActivity.launch(mContext, mBook.getBookCateId(),
                mBook.getBookCate(), 1);

        cancelDownload();

        UserActionManager.getInstance().recordEvent(Constants.CLICK_BOOK_CATE);
    }

    private void enterBookTagActivity() {
        Intent intent = new Intent();
        intent.setClass(BookDetailActivity.this, BookCatalogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        Bundle bundle = new Bundle();
        bundle.putSerializable("book", mBook);
        if (mBookKey != null) {
            bundle.putString(KEY, mBookKey);
        }
        intent.putExtras(bundle);
        startActivity(intent);

        UserActionManager.getInstance().recordEvent(Constants.CLICK_CATALOG);
    }

    //  最新章节--点击
    private void onCatalogChapterBtnClick() {
        Book book = DownBookManager.getInstance().getBook(mBook);
        boolean isOnline = true;
        if (book != null
                && !book.isOnlineBook()
                && book.getDownloadInfo().getFilePath() != null
                && book.getDownloadInfo().getFilePath()
                .endsWith(Book.BOOK_SUFFIX)) {
            // if (book != null && Math.abs(book.getDownloadInfo().getProgress()
            // - 1.0) < 0.0001
            // && book.getDownloadInfo().getDownLoadState() ==
            // DownBookJob.STATE_FINISHED) {
            // 如果是读的书架上的下载好的书，需要更新该book的最后阅读时间等属性
            if ((book.getBookUpdateChapterInfo() == null || book
                    .getBookUpdateChapterInfo().getGlobalId() == 0)
                    && (mBook.getBookUpdateChapterInfo() != null || mBook
                    .getBookUpdateChapterInfo().getGlobalId() != 0)) {
                book.setBookUpdateChapterInfo(mBook.getBookUpdateChapterInfo());
            }

            if (book.getChapters() == null || book.getChapters().isEmpty()) {
                ArrayList<Chapter> chapters = DBService.getAllChapter(book);
                if (chapters != null && !chapters.isEmpty()) {
                    book.setChapters(chapters);
                } else {
                    if (mBook.getChapters() != null
                            && !mBook.getChapters().isEmpty()) {
                        book.setChapters(mBook.getChapters());
                    }
                }
            }

            mBook = book;
            isOnline = false;
        }

        Chapter chapter = mBook.getBookUpdateChapterInfo();

		/*
		 * 如果该书籍是已下载&&已阅读，书架记录中有存储总页数和当前页数； 再次从详情页的目录或者
		 * 最新章节点击进入，当前页数残留了上次的页数，造成页数显示bug； 在此清楚下当前页码。
		 */
        mBook.getBookPage().setCurPage(-1);
        ReadActivity.setChapterReadEntrance("书籍详情页-最新章节");
        ReadActivity
                .launch(mContext, mBook, isOnline, chapter, false, mBookKey);
        UserActionManager.getInstance().recordEvent(
                Constants.CLICK_READ_CHAPTER);
    }
}
