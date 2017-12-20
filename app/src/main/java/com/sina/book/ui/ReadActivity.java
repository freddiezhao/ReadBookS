package com.sina.book.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
import com.sina.book.control.download.IOnlineBookListener;
import com.sina.book.control.download.ITaskUpdateListener;
import com.sina.book.control.download.OnlineBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookBuyInfo;
import com.sina.book.data.BookDetailData;
import com.sina.book.data.BookDownloadInfo;
import com.sina.book.data.BookPriceResult;
import com.sina.book.data.BookReadInfo;
import com.sina.book.data.BookSummary;
import com.sina.book.data.Chapter;
import com.sina.book.data.ChapterList;
import com.sina.book.data.ChapterPriceResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.MarkItem;
import com.sina.book.data.PriceTip;
import com.sina.book.data.UserInfoRole;
import com.sina.book.data.WeiboContent;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.PaymentMonthMineUtil;
import com.sina.book.data.util.PurchasedBookList;
import com.sina.book.db.DBService;
import com.sina.book.image.ImageLoader;
import com.sina.book.image.ImageUtil;
import com.sina.book.parser.BookDetailParser;
import com.sina.book.parser.BookPriceParser;
import com.sina.book.parser.ChapterListParser;
import com.sina.book.parser.ChapterPriceParser;
import com.sina.book.parser.IParser;
import com.sina.book.parser.SimpleParser;
import com.sina.book.reader.ILongTouchListener;
import com.sina.book.reader.PageFactory;
import com.sina.book.reader.PageWidget;
import com.sina.book.reader.PageWidgetListener;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.reader.SinaBookPageFactory;
import com.sina.book.reader.model.BookSummaryPostion;
import com.sina.book.reader.selector.Selection;
import com.sina.book.reader.selector.SelectorPopMenu;
import com.sina.book.reader.selector.SelectorPopMenu.IMenuClickListener;
import com.sina.book.ui.notification.ReadBookNotification;
import com.sina.book.ui.view.CommonDialog;
import com.sina.book.ui.view.LoginDialog;
import com.sina.book.ui.view.LoginDialog.LoginStatusListener;
import com.sina.book.ui.view.PayDialog;
import com.sina.book.ui.view.ReadCompleteView;
import com.sina.book.ui.view.ReadCompleteView.IViewOnClickListener;
import com.sina.book.ui.view.ReadToolbar;
import com.sina.book.ui.view.ShareDialog;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.ReadCountManager;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.BrightnessUtil;
import com.sina.book.util.FileUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler;
import org.htmlcleaner.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.sina.book.data.Book.BOOK_LOCAL;
import static com.sina.book.data.Book.BOOK_ONLINE;
import static com.sina.book.data.Book.BOOK_SDCARD;
import static com.sina.book.data.Book.BOOK_TYPE_CHAPTER_VIP;
import static com.sina.book.data.Book.BOOK_TYPE_FREE;
import static com.sina.book.data.Book.BOOK_TYPE_VIP;
import static com.sina.book.data.Chapter.CHAPTER_FAILED;
import static com.sina.book.data.Chapter.CHAPTER_FREE;
import static com.sina.book.data.Chapter.CHAPTER_NEED_BUY;
import static com.sina.book.data.Chapter.CHAPTER_NO;
import static com.sina.book.data.Chapter.CHAPTER_PREPARE;
import static com.sina.book.data.Chapter.CHAPTER_VIP;
import static com.sina.book.data.ConstantData.CODE_FAIL_KEY;
import static com.sina.book.data.ConstantData.URL_GET_CHAPTERS;
import static com.sina.book.data.ConstantData.URL_GET_CHAPTERS_BY_PAGE;

/**
 * 阅读界面.
 *
 * @author Tsimle
 */
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class ReadActivity extends BaseActivity implements View.OnClickListener,
        IOnlineBookListener, ITaskUpdateListener, IViewOnClickListener,
        ReadToolbar.ISettingChangedListener {
    private static final String TAG = "ouyang";

    public static final String KEY = "prikey";

    /**
     * 同步锁关键字.
     */
    private final String SYNCHRONIZED_LOCK = "sync_lock";

    /**
     * 目录书签返回码
     */
    public static final int MARK_CHAPTER_CODE = 1;

    /**
     * 章节购买的情况，章节预取数目
     */
    public static final int CHAPTER_PRE_GET = 5;

    /**
     * 阅读在线图书.
     */
    public static final int READ_ONLINE_BOOK = 0x01;
    /**
     * 阅读本地图书.
     */
    public static final int READ_LOCAL_BOOK = 0x02;
    /**
     * 阅读来自微博呼起的图书.
     */
    public static final int READ_WEIBO_BOOK = 0x03;
    /**
     * 阅读购买的图书.
     */
    public static final int READ_PAY_BOOK = 0x04;

    /**
     * 在线阅读标志
     */
    private static final String TYPE_ONLINE = "online";

    /**
     * 默认超时时间200秒
     */
    private static final int DELAY = 200000;

    /**
     * 用户原来的设置
     */
    private int mDefTimeOut;
    /**
     * 用户原来的设置 ,调节亮度模式
     */
    private int mDefAutoBrightness;

    // 是否跳转.
    private boolean mIsJump = false;
    private int mJumpOffset = 0; // 跳转到的索引
    private int mJumpHistoryOffset = 0;

    private Chapter mSelectedChapter;

    /**
     * 是否是收费章节.
     */
    private boolean mIsVIPChapter = false;

    // private Chapter backupChapter;

    // 显示区域信息
    private int mDisplayWidth;
    private int mDisplayHeight;

    private PageWidget mPageWidget;
    private SinaBookPageFactory mPageFactory;
    private ReadStyleManager mReadStyleManager;

    private SelectorAnchor mSelectorAnchor;
    private List<String> mTextSelectorMenuList;

    private BookSummaryPopControl mSummaryPopControl;
    private List<String> mSummaryMenuList;

    public static Book gBook;
    private MarkItem mCurMark;
    /**
     * 用来存储章节ID，某一个章节ID对应该章节的章节内容任务，避免同一个章节任务重复请求
     */
    private ArrayList<Integer> mChapterIdList = new ArrayList<Integer>();
    private UserInfoRole mRole;

    /**
     * 网络错误View.
     */
    private View mErrorView;
    /**
     * 网络错误View之子View，返回按钮
     */
    private View mBackView;
    /**
     * 阅读页工具栏
     */
    private ReadToolbar mToolbar;
    /**
     * 阅读完成页面.
     */
    private ReadCompleteView mReadCompleteView;
    /**
     * 阅读完成页面是否正在动画.
     */
    private boolean mPlayingAnim = false;
    /**
     * 书签标志
     */
    private ImageView mBookmarkFlag;

    /**
     * 是否在线阅读，从详情页面进入.
     */
    private String mReadOnline = "";

    /**
     * 预取章节购买信息的task
     */
    private RequestTask mPreGetChapterTask;

    // 下拉添加书签相关View
    private View mPullDownLayout;
    private TextView mPullDownText;
    private ImageView mPullDownImage;

    /**
     * 从书籍详情、目录页面传入的章节
     */
    private Chapter mBookDetailChapter;

    /**
     * 是否处于正在打开书籍的状态，仅作用于在线阅读类型（包括微博呼起）
     */
    private boolean mCanFinish = false;

    /**
     * 请求章节列表的Task
     */
    private RequestTask mReqChapterListTask;

    /**
     * 设置进度条后，是否响应事件
     */
    private boolean mIsProgressChanged = true;

    // 阅读到第一页或最后一页时是否自动翻页
    private final int FLIP_DEFAULT = -1;
    private final int FLIP_NEXT_PAGE = 0;
    private final int FLIP_PRE_PAGE = 1;
    private int mAutoFlip = FLIP_DEFAULT;

    // 进入阅读页面后，是否有过购买的行为
    private boolean mEnterHasAutoBuy = false;

    private String mBookKey;
    private String mBookId;

    /**
     * 因为添加了音量键的翻页控制<br>
     * 弹出dialog时屏蔽它们对音量事件的处理，以及在线阅读首次显示的进度框处理返回按键事件
     */
    private DialogInterface.OnKeyListener mDialogKeyListener = new DialogInterface.OnKeyListener() {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // 在线类阅读，网络请求过慢则直接结束阅读页面
                if (mCanFinish) {
                    dismissProgress();
                    disToolBar();
                    finishActivity();
                    //
                    mIsJump = false;
                    return true;
                }
            }

            return keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN;
        }
    };

    /**
     * 改变字体，加载章节任务回来后需要刷新.
     */
    private ITaskFinishListener mFinishedRefreshListener = new ITaskFinishListener() {
        @Override
        public void onTaskFinished(TaskResult taskResult) {
            // 解决BugID=21176
            invalidateUI(true);
        }
    };

    private BatteryReceiver batteryReceiver;

    private boolean disableAddShelf = false;

    // 是否点击了加入书架对话框的 确定
    private boolean isClickAddShelf = false;

    /**
     * 电池广播接受者
     */
    private class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 判断它是否是为电量变化的Broadcast Action
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                // 获取当前电量
                int level = intent.getIntExtra("level", 0);
                // 电量的总刻度
                int scale = intent.getIntExtra("scale", 100);
                int value = level * 100 / scale;

                if (null != mPageFactory) {
                    mPageFactory.setBatteryValue(value);
                    invalidateUI(true);
                }
            }
        }
    }

    /**
     * 启动阅读界面
     *
     * @param context the content
     * @param book    the book
     * @param online  is online
     */
    public static void launch(final Context context, Book book, boolean online,
                              boolean isForResult) {
        launch(context, book, online, null, isForResult);
    }

    public static void launch(final Context context, Book book, boolean online,
                              Chapter curChapter, boolean isForResult) {
        launch(context, book, online, curChapter, isForResult, null);
    }

    /**
     * 启动该界面.
     *
     * @param context
     * the context
     * @param book
     * the book
     * @param online
     * the online
     * @param curChapter
     * the chapter
     */
    private boolean isLocalBookMissing = false;

    public static void launch(final Context context, Book book, boolean online,
                              Chapter curChapter, boolean isForResult, String offShlefKey) {


        // SD卡不存在
        if (!StorageUtil.isSDCardExist()) {
            Toast.makeText(context, R.string.no_sdcard, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // 1.9.0先屏蔽掉"包月开通"的弹出框，后续在vip章节计费和立即购买处判断
        // // 处理包月书籍，若处于不可读则返回
        // if (!handleMonthBook(context, book))
        // return;

        Intent intent = new Intent(context, ReadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        Bundle bundle = new Bundle();
        bundle.putSerializable("book", book);
        bundle.putSerializable("chapter", curChapter);
        bundle.putString(KEY, offShlefKey);
        intent.putExtras(bundle);

        if (online) {
            // 在线试读时，所有书籍状态都让它进入
            intent.putExtra(TYPE_ONLINE, TYPE_ONLINE);
        } else {
            // 打开本地书籍时，更新书籍的一些状态
            BookDownloadInfo downloadInfo = book.getDownloadInfo();

            if (downloadInfo.getDownLoadState() != DownBookJob.STATE_FINISHED) {
                Toast.makeText(context, R.string.downloading_waiting,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 显示的new标签消失
            book.setTag(Book.HAS_READ);
            // 更新最后阅读的时间
            book.getReadInfo().setLastReadTime(new Date().getTime());

            boolean isLocalBookMissing = false;
            if (!book.isOnlineBook()) {
                String filePath = downloadInfo.getFilePath();
                LogUtil.d(TAG, "File path is " + filePath);

                if (filePath.startsWith("file:///")) {
                    // 路径里有file的是asserts下的文件
                    if (!FileUtils.assertsFileExist(filePath)) {
                        // 解决bug，更新版本后的旧内置书籍打不开问题。
                        // 判断assets文件不存在后，sdcard文件在不在，在就继续阅读
                        int index = filePath.lastIndexOf('/');
                        String path = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK) + "/" +
                                filePath.substring(index + 1);
                        File file = FileUtils.checkOrCreateFile(path, false);
                        if (file == null || !file.exists()) {
                            Toast.makeText(context,
                                    R.string.reading_book_not_exist,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            downloadInfo.setFilePath(path);
                            DBService.saveBook(book);
                        }
                    }
                } else if (!downloadInfo.bookFileExist()) {
                    // 文件不存在时不再提示"书本不存在，请检查SD卡"，直接变成在线书籍即可
                    // Toast.makeText(context, R.string.reading_book_not_exist,
                    // Toast.LENGTH_SHORT).show();
                    // return;
                    isLocalBookMissing = true;
                }

//				String suffix = filePath.substring(filePath.lastIndexOf("."),
//						filePath.length());
//				// 如果文件后缀是tmp即临时文件时也返回
//				if (TMP_SUFFIX.equalsIgnoreCase(suffix)) {
//					return;
//				}

                if (isLocalBookMissing) {
                    book.setOnlineBook(true);
                    book.getDownloadInfo().setFilePath(null);
                    book.getReadInfo().setLastPos(0);
                    book.getReadInfo().setLastReadPercent(0.0f);
                    // 清除Chapter信息
                    DBService.deleteChapter(book);
                    intent.putExtra(TYPE_ONLINE, TYPE_ONLINE);
                    intent.putExtra("isLocalBookMissing", isLocalBookMissing);
                }
            }

            String lastBookTitle = StorageUtil
                    .getString(StorageUtil.KEY_READ_BOOK);
            String nowBookTitle = book.getTitle();
            if (nowBookTitle != null && !nowBookTitle.equals(lastBookTitle)) {
                StorageUtil.saveString(StorageUtil.KEY_READ_BOOK, nowBookTitle);
            }
        }


        if (isForResult) {
            ((Activity) context).startActivityForResult(intent, 1001);
        } else {
            context.startActivity(intent);
        }

        recordAction(book);

    }

    /**
     * 处理包月书籍
     *
     * @param context
     * @param book
     * @return true if can read, otherwise false
     */
    private static boolean handleMonthBook(final Context context, Book book) {
        if (DownBookManager.getInstance().hasBook(book)
                && book.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {
            if (!book.isOnlineBook()
                    && !PaymentMonthMineUtil.getInstance().canRead(book)) {
                String preText = "";
                if (!TextUtils.isEmpty(book.getSuiteName())) {
                    preText = String.format(
                            context.getString(R.string.payment_month_pretip),
                            book.getSuiteName());
                }

                if (LoginUtil.isValidAccessToken(context) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
                    // 如果未登录
                    String tip = preText
                            + context.getString(R.string.payment_month_nologin);
                    Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();

                } else if (!HttpUtil.isConnected(context)) {
                    // 如果无网络
                    String tip = preText
                            + context.getString(R.string.payment_month_already);
                    Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();

                } else {
                    if (preText.equals("")) {
                        preText = String.format(context
                                .getString(R.string.payment_month_pretip), "");
                    }

                    String tip = preText
                            + context
                            .getString(R.string.payment_month_old_content);

                    String title = context
                            .getString(R.string.payment_month_old_title);
                    CommonDialog.show(context, title, tip,
                            new CommonDialog.DefaultListener() {
                                @Override
                                public void onRightClick(DialogInterface dialog) {
                                    Intent intent = new Intent(context,
                                            PaymentMonthDetailActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                    context.startActivity(intent);
                                }
                            });
                }

                return false;
            }
        }
        return true;
    }

    /**
     * 统计阅读书籍类型
     *
     * @param book
     */
    private static void recordAction(Book book) {
        if (null != book) {
            if (book.isOurServerBook()) {
                UserActionManager.getInstance().recordEventValue(
                        Constants.KEY_READ_BOOK_PAY_TYPE
                                + book.getBuyInfo().getPayType());

                UserActionManager.getInstance().recordEventValue(
                        Constants.KEY_READ_BOOK_SRC_TYPE + "sina");
            } else if (book.isVDiskBook()) {
                UserActionManager.getInstance().recordEventValue(
                        Constants.KEY_READ_BOOK_SRC_TYPE + "vdisk");
            } else {
                UserActionManager.getInstance().recordEventValue(
                        Constants.KEY_READ_BOOK_SRC_TYPE + "sdcard");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("BugLog", "ReadActivity >> onCreate >> ");
        parseIntent(getIntent());

        // 注册电池电量广播
        batteryReceiver = new BatteryReceiver();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDisplayWidth = metrics.widthPixels;
        mDisplayHeight = metrics.heightPixels;

        // mDisplayWidth = getWindowManager().getDefaultDisplay().getWidth();
        // mDisplayHeight = getWindowManager().getDefaultDisplay().getHeight();

        mReadStyleManager = ReadStyleManager.getInstance(mDisplayWidth,
                mDisplayHeight);

        initViews();
        initViewListener();

        // 初始化阅读页面生成类
        mPageFactory = SinaBookPageFactory.getInstance(mDisplayWidth,
                mDisplayHeight);
        mPageFactory.setRefreshListener(mFinishedRefreshListener);
        // 初始化翻控件
        mPageWidget.init(mDisplayWidth, mDisplayHeight, mPageFactory);
        mPageWidget.setPageWidgetListener(new PageListener());

        if (mBookDetailChapter != null && mBookDetailChapter.getGlobalId() != 0) {
            // 外部传入章节
            initChapter(mBookDetailChapter);
        } else {
            // 本地书籍直接打开，在线书籍先去网络取信息
            if (gBook.getDownloadInfo().getLocationType() != BOOK_ONLINE) {
                // 本地书籍
                ArrayList<Chapter> chapters = gBook.getChapters();
                if (chapters != null && chapters.size() > 0) {
                    updatePageFactory();
                } else {
                    gBook.setOnlineBook(true);
                    gBook.getDownloadInfo().setFilePath(null);
                    gBook.getReadInfo().setLastPos(0);
                    gBook.getReadInfo().setLastReadPercent(0.0f);
                    // 清除Chapter信息
                    DBService.deleteChapter(gBook);
                    mReadOnline = TYPE_ONLINE;
                    prepareBookData();
                    // reqBookChapterList(null, true);
                }
            } else {
                // parseBookmarkFromJson2();
                prepareBookData();
            }
        }

        postInit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.d("BugLog", "ReadActivity >> onNewIntent >> ");

        // 如果是通知栏呼起事件，则直接返回
        if (null != intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.getBoolean(ReadBookNotification.TAG, false)) {
                    return;
                }
            }
        }

        // 判断返回的章节是不是与当前章节一致，一致则不需要处理
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Book sBook = (Book) bundle.getSerializable("book");
                Chapter sChapter = (Chapter) bundle.getSerializable("chapter");
                // String sKey = bundle.getString(KEY);
                // 判定是不是同一本书籍
                if (sBook != null && gBook != null && sBook.equals(gBook)) {
                    // 判断一下书籍是否被加入书架了
                    Book temp = DownBookManager.getInstance().getBook(gBook);
                    LogUtil.d("BugBugFuckU",
                            "ReadActivity >> onNewIntent >> temp=" + temp);
                    if (temp != null) {
                        // 转移相关属性
                        gBook.setId(temp.getId());
                        gBook.setOnlineBook(temp.isOnlineBook());
                    }

                    // 点击的是在线试读，没有传递Chapter数据
                    if (sChapter == null) {
                        return;
                    } else {
                        // 同一本书籍，判断是不是同一个章节
                        Chapter currentChapter = mPageFactory
                                .getCurrentChapter();
                        if (currentChapter != null
                                && currentChapter.equals(sChapter)) {
                            // 同一个章节
                            return;
                        } else {
                            // 不同章节
                            int globalChapterId = sChapter.getGlobalId();
                            Chapter chapter = gBook
                                    .getCurrentChapterById(globalChapterId);
                            if (chapter != null) {
                                if (chapter.getLength() > 0) {
                                    updateReadingView();
                                    mPageFactory.seek((int) (chapter
                                            .getStartPos()));
                                } else {
                                    mIsJump = true;
                                    mJumpOffset = 0;
                                    // updatePageFactory();

                                    // parseBookmarkFromJson2();
                                    judgeChapterStateAndLoad(chapter);
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }

        // 先释放以前的资源
        LogUtil.d("BugLog",
                "ReadActivity-onNewIntent(before parseIntent)-gBook=" + gBook);
        if (gBook != null) {
            release(false);
            clearSelectChapterInfo();
        }

        parseIntent(intent);
        LogUtil.d("BugLog",
                "ReadActivity-onNewIntent-(after parseIntent) gBook=" + gBook);

        // 判断末页推荐是否打开着，打开着则关闭
        dismissCompleteView(true);

        if (mBookDetailChapter != null && mBookDetailChapter.getGlobalId() != 0) {
            initChapter(mBookDetailChapter);
        } else {
            // 本地书籍直接打开，在线书籍先去网络取信息
            if (gBook.getDownloadInfo().getLocationType() != BOOK_ONLINE) {
                updatePageFactory();
            } else {
                prepareBookData();
            }
        }

        postInit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 请求一下书籍详情
        if (null != gBook && gBook.isOurServerBook()) {
            reqBookInfo();
            reqBookPrice();
        }
    }

    @Override
    protected void onResume() {
        LogUtil.d("BugLog", "ReadActivity >> onResume >> ");

        // 回到阅读页的时候重新注册对电池电量变化的监听
        if (batteryReceiver == null) {
            batteryReceiver = new BatteryReceiver();
        }
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, intentFilter);

        disToolBar();

        // 清理所有阅读时的通知信息
        ReadBookNotification.getInstance().clearNotification();

        mDefTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
        mDefAutoBrightness = BrightnessUtil.getBrightnessMode(this);

//        Settings.System.putInt(getContentResolver(),
//                Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
        setScreenBrightness();

        if (gBook != null) {
            int location = gBook.getDownloadInfo().getLocationType();

            // 在不关心的时候不listener download manager的事件
            // if (Book.BOOK_SDCARD != location && Book.BOOK_TYPE_CHAPTER_VIP !=
            // gBook.getBuyInfo().getPayType()) {
            DownBookManager.getInstance().addProgressListener(this);
            // }
        }

        // 不是在线书籍显示引导画面
        if (!TYPE_ONLINE.equals(mReadOnline)) {
            // 如果"赞"页面正好显示，则忽略，等下次进入时再显示
            if (!mReadCompleteView.isShowing()) {
                MaskGuideActivity.launch(this, StorageUtil.KEY_SHOW_GUIDE_READ,
                        R.layout.guide_read);
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d("BugBugFuckU", "ReadActivity >> onPause >> {gBook=" + gBook
                + "}");

        // =========================================================== //
        // **本地书籍在离开的时候保存一下当前各个章节的最新数据**
        // =========================================================== //
        // 避免由于内存不足，页面被清除，导致内存中的数据没有及时保存到数据库中
        // 造成数据错误
        // 情景：如BugID=21415所描述的情景，先下载所有的免费章节
        // 之后进来阅读，然后跳转到Vip章节时，Vip章节的数据追加到免费章节
        // 之后，该Vip章节的startPos也是紧跟着之前所有免费章节内容的末尾，
        // 但是由于没有及时的保存同步到数据库中，之后由于内存不足或者Bugfree中提到
        // 的操作，退出账户再进入账户的时候，这个Vip章节的数据并没有被保存到数据库中
        // 但是程序记录的lastPos确是正确的，此时便无法找到任何一个章节可以匹配上传入的lastPos
        // 造成无法正常读取数据的异常
        if (gBook != null && DownBookManager.getInstance().hasBook(gBook)
                && gBook.getDownloadInfo() != null) {
            String filePath = gBook.getDownloadInfo().getFilePathOnly();
            if (filePath != null) {
                if (filePath.endsWith(Book.BOOK_SUFFIX)) {
                    DBService.updateAllChapter(gBook, gBook.getChapters());
                    Book tbook = DownBookManager.getInstance().getBook(gBook);
                    if (gBook != tbook) {
                        tbook.setChapters(gBook.getChapters());
                    }
                }
            }
        }

        // 离开界面的时候注销对电池电量的监听
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }

        // 每次离开阅读页时重置mChapterReadEntrance
        ReadActivity.setChapterReadEntrance(null);

        // DownBookManager.getInstance().removeProgressListener(this);

        // 取消正在下载的任务和监听器
        OnlineBookManager.getInstance().release();

        // 如果请求章节列表的Task不为空则取消该任务
        if (null != mReqChapterListTask) {
            mReqChapterListTask.cancel(true);
        }

        // 保存阅读进度
        mPageFactory.addLastReadMark();

        if (gBook.getDownloadInfo().getLocationType() == Book.BOOK_LOCAL) {

            // 将书籍信息进行保存，同步到各处
            DownBookManager.getInstance().updateBook(gBook);
            StorageUtil.saveString(StorageUtil.KEY_READ_BOOK, gBook.getTitle());
            // 数据库的放入线程中，防止阻塞生命周期
            new GenericTask() {

                @Override
                protected TaskResult doInBackground(TaskParams... params) {
                    DBService.updateBook(gBook);
                    return null;
                }
            }.execute();
        } else if (gBook.isOnlineBook()) {
            // 将书籍信息进行保存，同步到各处
            // 数据库的放入线程中，防止阻塞生命周期
            new GenericTask() {

                @Override
                protected TaskResult doInBackground(TaskParams... params) {
                    DBService.updateBookReadInfo(gBook);
                    return null;
                }
            }.execute();
        }

        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, mDefTimeOut);
        BrightnessUtil.setScreenBrightnessMode(getApplicationContext(),
                mDefAutoBrightness);
    }

    @Override
    protected void onDestroy() {
        LogUtil.d("BugLog", "ReadActivity >> onDestroy >> ");
        DownBookManager.getInstance().removeProgressListener(this);
        release(true);
        System.gc();
        if (mEnterHasAutoBuy) {
            mEnterHasAutoBuy = false;
            LoginUtil.reqBalance(SinaBookApplication.gContext);
        }
        super.onDestroy();
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();

        gBook.getReadInfo().setLastReadTime(System.currentTimeMillis());
        Chapter chapter = mPageFactory.getCurrentChapter();

        ReadBookNotification.getInstance().showNotification(gBook, chapter);
    }

    /**
     * 从Intent中解析数据
     *
     * @return 是否成功
     */
    private void parseIntent(Intent intent) {
        // 首先初始化书籍缓存
        DownBookManager.getInstance().init();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // boolean isSkipAssignment = false;
                Book book = (Book) bundle.getSerializable("book");

                // if (null != book && null != gBook) {
                // String newBookId = book.getBookId();
                // String preBookId = gBook.getBookId();
                // if (!TextUtils.isEmpty(newBookId) &&
                // !TextUtils.isEmpty(preBookId) && newBookId.equals(preBookId))
                // {
                // isSkipAssignment = true;
                // }
                // }
                // if (!isSkipAssignment) {
                gBook = book;
                // }
                mBookDetailChapter = (Chapter) bundle
                        .getSerializable("chapter");
                mBookKey = bundle.getString(KEY);
                isLocalBookMissing = bundle.getBoolean("isLocalBookMissing");

                if (null != gBook) {
                    mBookId = gBook.getBookId();
                    mReadOnline = intent.getStringExtra(TYPE_ONLINE);

                    // 如果是在线阅读则请求书籍内容
                    if (TYPE_ONLINE.equals(mReadOnline)) {
                        // gBook.setOnlineBook(true);
                        // gBook.getDownloadInfo().setFilePath(null);
                        gBook.getDownloadInfo().setLocationType(BOOK_ONLINE);
                    }

                    handleLocalBook();
                }
            }
        }
    }

    // boolean isDatDismissAndNeedToDownAgain = false;

    /**
     * 如果是本地书籍,则对Book加以处理.
     *
     * @return 是否处理
     */
    private boolean handleLocalBook() {
        // isDatDismissAndNeedToDownAgain = false;
        boolean result = false;

        Book temp = DownBookManager.getInstance().getBook(gBook);
        if (null != temp) {
            // 仅当本地书架的书籍文件已经成功下载，使用本地书架文件
            if (temp.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {

                // if (!gBook.isOnlineBook()) {
                gBook = temp;
                // }

                // 添加书签信息
                if (gBook.getBookmarks().isEmpty()) {
                    gBook.setBookmarks(DBService.getAllBookMark(temp));
                }

                // 添加书摘信息
                if (gBook.getBookSummaries().isEmpty()) {
                    gBook.setBookSummaries(DBService.getAllBookSummary(temp));
                }

                String filePath = gBook.getDownloadInfo().getFilePath();
                // 书架上的在线书籍，还未存储书籍文件信息时
                if (gBook.isOnlineBook()
                        && (TextUtils.isEmpty(filePath)
                        || filePath.endsWith(Book.TMP_SUFFIX) || filePath
                        .endsWith(Book.ONLINE_TMP_SUFFIX))) {
                    // 之前是在线的，由于某种原因，导致SD卡内的dat文件丢失。
                    // if (temp.getDownloadInfo().getFilePath() != null
                    // &&
                    // temp.getDownloadInfo().getFilePath().endsWith(Book.BOOK_SUFFIX))
                    // {
                    // gBook.setOnlineBook(false);
                    // isDatDismissAndNeedToDownAgain = true;
                    // }

                    gBook.getDownloadInfo().setLocationType(BOOK_ONLINE);
                    mReadOnline = TYPE_ONLINE;

                    result = false;
                } else {
                    gBook.getDownloadInfo().setLocationType(BOOK_LOCAL);

                    // 添加章节信息
                    if (gBook.getChapters().isEmpty()) {
                        ArrayList<Chapter> chapters = DBService
                                .getAllChapter(gBook);
                        if (null != chapters && !chapters.isEmpty()) {
                            gBook.setChapters(chapters,
                                    "[ReadActivity-handleLocalBook]");
                        }
                    }
                    //
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * 请求书籍信息，仅针对在线阅读类型
     */
    private void prepareBookData() {
        // 如果是在线阅读则请求书籍内容
        if (TYPE_ONLINE.equals(mReadOnline)) {
            gBook.getDownloadInfo().setLocationType(BOOK_ONLINE);
            // 如果传入的书籍有下载路径，在线阅读时清除它
            // gBook.getDownloadInfo().setFilePath(null);
            // 请求章节列表
            reqChapterList(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 如果文字选择器正显示，则先隐藏
        if (null != mSelectorAnchor && mSelectorAnchor.isShowing()) {
            mSelectorAnchor.dismiss();
        }

        if (null != mSummaryPopControl && mSummaryPopControl.isShowing()) {
            mSummaryPopControl.dismiss();
        }

        showOrDisToolBar();
        return false;
    }

    @Override
    public void viewOnClick(View view) {
        switch (view.getId()) {
            case R.id.reading_complete_praise_txt:
                // 推荐
                if (!gBook.isForbidden()) {
                    praise();
                }
                break;

//            case R.id.reading_complete_comment_txt:
//                // 去评论
//                if (!gBook.isForbidden()) {
//                    enterCommentListActivity();
//                }
//                break;

            case R.id.reading_continue_button:
                // 继续阅读
                showProgress(R.string.get_price, true, mDialogKeyListener);
                if (gBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
                    reqBookPrice(true);
                } else {
                    reqChapterPrice();
                }
                break;

            default:
                break;
        }
    }

    // 推荐
    private void praise() {
        if (!HttpUtil.isConnected(this)) {
            shortToast(R.string.network_error);
            return;
        }

        // 第一次进行“赞”的操作：
        // （1）.没有登录微博的情况：弹出登陆对话框，若登陆成功后，进行“赞”，并判断是否开启“微博自动转发”，
        // 若开启，进行赞操作并发送一条微博；若未开启，只进行赞操作，并Toast提示赞成功/失败；
        // （2）.已经登录微博的情况：点击“赞”，需要判断是否开启“微博自动转发”的按钮；
        // 若开启，进行赞操作并发送一条微博；若未开启，只进行赞操作，并Toast提示赞成功/失败；
        if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
            reqPostPraise();
            // 请求的同时更改赞的状态
            updatePraiseBtnStatus();
            if (StorageUtil.getBoolean(StorageUtil.KEY_AUTO_WEIBO)) {
                autoShareWeibo();
            }
        } else {
            LoginDialog.launch(ReadActivity.this, new LoginStatusListener() {

                @Override
                public void onSuccess() {
                    reqBookInfo(true);
                    reqBookPrice();
                    if (gBook.getBookmarks().isEmpty()) {
                        List<MarkItem> list = DBService.getAllBookMark(gBook);
                        if (null != list && !list.isEmpty()) {
                            gBook.setBookmarks(list);
                        }
                    }
                    updateBookmarkFlag();
                }

                @Override
                public void onFail() {

                }
            });
        }
    }

    private void enterCommentListActivity() {
        Intent intent = new Intent();
        intent.setClass(this, CommentListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        intent.putExtra("book", gBook);
        startActivity(intent);

        UserActionManager.getInstance().recordEvent(
                Constants.CLICK_READ_COMMENT);
    }

    private void jumpPreChapter() {
        Chapter preChapter = gBook.getPreChapter(mPageFactory
                .getCurrentChapter());
        if (null == preChapter) {
            shortToast(getString(R.string.first_chapter));
            return;
        }
        if (preChapter.getLength() <= 0) {
            mIsJump = true;
            judgeChapterStateAndLoad(preChapter);
            // CJL 还没有下载回来，还是不要跳转进度条了
            // updateToolbar(preChapter);
        } else {
            mPageFactory.seek((int) preChapter.getStartPos());

            Chapter curChapter = mPageFactory.getCurrentChapter();
            updateToolbar(curChapter);
        }
        // CJL 解决那种通过点击工具栏的“上一章”，“下一章”进行页面跳转
        // 并且用户没有点击屏幕进行翻页行为，导致从未调用onPageTurned方法，
        // 因此mSelectedChapter未被赋值，此时点击顶部工具栏进入书籍详情页
        // 下载书籍，然后在未下载完成时点击在线试读再次进入该书籍，页面不动，
        // 等待下载完成，此时调用onUpdate
        // 在进行
        // Chapter chapter = null;
        // if (mSelectedChapter != null) {
        // chapter = mSelectedChapter;
        // } else {
        // chapter = mPageFactory.getCurrentChapter();
        // }
        // 代码判断时，因为mSelectedChapter==null。获取到的mSelectedChapter实际上
        // 是不对的。导致页面在下载完成时，出现跳章节的情况。
        mSelectedChapter = preChapter;

        invalidateUI(true);

        UserActionManager.getInstance()
                .recordEvent(Constants.CLICK_PRE_CHAPTER);
    }

    // 跳转下一章
    private void jumpNextChapter() {
        Chapter nextChapter = gBook.getNextChapter(mPageFactory
                .getCurrentChapter());
        if (null == nextChapter) {
            shortToast(getString(R.string.last_chapter));
            return;
        }

        if (nextChapter.getLength() <= 0) {
            // 下一章数据长度为0，发起鉴权及下载
            mIsJump = true;
            judgeChapterStateAndLoad(nextChapter);
            // CJL 还没有下载回来，还是不要跳转进度条了
            // updateToolbar(nextChapter);

        } else {
            mPageFactory.seek((int) nextChapter.getStartPos());
            Chapter curChapter = mPageFactory.getCurrentChapter();
            updateToolbar(curChapter);
        }

        // CJL 解决那种通过点击工具栏的“上一章”，“下一章”进行页面跳转
        // 并且用户没有点击屏幕进行翻页行为，导致从未调用onPageTurned方法，
        // 因此mSelectedChapter未被赋值，此时点击顶部工具栏进入书籍详情页
        // 下载书籍，然后在未下载完成时点击在线试读再次进入该书籍，页面不动，
        // 等待下载完成，此时调用onUpdate
        // 在进行
        // Chapter chapter = null;
        // if (mSelectedChapter != null) {
        // chapter = mSelectedChapter;
        // } else {
        // chapter = mPageFactory.getCurrentChapter();
        // }
        // 代码判断时，因为mSelectedChapter==null。获取到的mSelectedChapter实际上
        // 是不对的。导致页面在下载完成时，出现跳章节的情况。
        mSelectedChapter = nextChapter;

        invalidateUI(true);

        UserActionManager.getInstance().recordEvent(
                Constants.CLICK_NEXT_CHAPTER);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reading_toolbar_home_btn:// 返回按钮
                showIsAddToBookshelf();
                break;

            case R.id.reading_toolbar_catalog_btn:// 目录
                jump2Catalog();
                mToolbar.removeUpdateFlag();
                // 显示的更新章节数消失
                gBook.setUpdateChaptersNum(0);
                break;

            case R.id.reading_toolbar_down_btn:// 工具栏下载按钮
                disToolBar();
                downloadOrBuyBook();
                break;

            case R.id.reading_toolbar_bookmark_btn:// 书签
                addOrDelMark();
                break;

            case R.id.reading_toolbar_bookinfo_btn:// 去书籍详情页面
                DownBookManager.getInstance().addProgressListener(this);
                mPageFactory.addLastReadMark();// 保存阅读进度
                BookDetailActivity
                        .launch(this, gBook, mBookKey, "阅读器_工具栏_01", null);
                UserActionManager.getInstance().recordEvent(
                        Constants.CLICK_READ_DETAIL);
                break;

            case R.id.reading_bookmark_flag:// 书签标志
                if (null != mCurMark) {
                    deleteBookMark();
                    updateBookmarkFlag();
                    shortToast(R.string.delete_mark_succ);
                }
                break;

            case R.id.read_praise_btn:// 推荐
                if (!gBook.isForbidden()) {
                    praise();
                }
                break;

            case R.id.read_comment_btn:// 去评论列表页面
                if (!gBook.isForbidden()) {
                    enterCommentListActivity();
                }
                break;

            case R.id.pre_chapter_btn:// 跳转到前一章
                setChapterReadEntrance("阅读页-前后页");
                jumpPreChapter();
                break;

            case R.id.next_chapter_btn:// 跳转到下一章
                setChapterReadEntrance("阅读页-前后页");
                jumpNextChapter();
                break;
            case R.id.back_btn:// 网络错误-返回按钮
                finishActivity();
                break;
        }
    }

    /**
     * 添加或删除书签
     */
    private void addOrDelMark() {
        disToolBar();

        if (null == mCurMark) {
            addBookMark();
        } else {
            deleteBookMark();
        }
    }

    /**
     * 去书签目录界面
     */
    private void jump2Catalog() {
        Intent dirIntent = new Intent();
        dirIntent.setClass(ReadActivity.this, BookTagActivity.class);
        dirIntent.putExtra("curpos", mPageFactory.getCurrentChapterIndex());

        dirIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivityForResult(dirIntent, MARK_CHAPTER_CODE);
        overridePendingTransition(R.anim.push_left_in, R.anim.keep_loaction);

        disToolBarWithoutFull();

        UserActionManager.getInstance().recordEvent(
                Constants.CLICK_READ_CATALOG);
    }

    /**
     * 下载或者购买书籍
     */
    private void downloadOrBuyBook() {
        mPageFactory.prepareRefreshBookFile();
        switch (gBook.getBuyInfo().getPayType()) {

            case BOOK_TYPE_FREE:
                // 在阅读页下载本书时，增加检测book是否存在并已下载:如果是则直接隐藏下载按钮
                Book book = DownBookManager.getInstance().getBook(gBook);
                if (book != null
                        && ((book.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED
                        && book.getDownloadInfo().getFilePath() != null && book
                        .getDownloadInfo().getFilePath()
                        .endsWith(Book.BOOK_SUFFIX)) || book
                        .getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING)) {
                    disToolBar();
                    mToolbar.getToolbarDownBtn().setVisibility(View.GONE);
                    break;
                }

                CloudSyncUtil.getInstance().add2Cloud(mContext, gBook);
                int flag = DownBookManager.getInstance().downBook(gBook);

                if (flag == DownBookManager.FLAG_START_CACHE) {
                    mPageWidget.setTouchEnabled(false);

                    disToolBar();
                    showProgress(R.string.downloading_text, false,
                            mDialogKeyListener);

                    mToolbar.getToolbarDownBtn().setEnabled(false);
                }
                break;

            case BOOK_TYPE_VIP:
            case BOOK_TYPE_CHAPTER_VIP:
                PriceTip tip = gBook.getBuyInfo().getPriceTip();
                if (tip != null && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
                    isBookcard = true;
                } else {
                    isBookcard = false;
                }
                doPayProcess();
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果工具栏正显示，则先隐藏工具栏
            if (mToolbar.isShown()) {
                disToolBar();
                return true;
            }

            // 如果"赞"页面正显示，则先隐藏"赞"页面
            if (mReadCompleteView.isShowing()) {
                dismissCompleteView();
                return true;
            }

            // 如果错误页面正显示，则直接退出阅读页面
            if (mErrorView.isShown()) {
                finishActivity();
                return true;
            }

            // 如果文字选择器正显示，则先隐藏
            if (null != mSelectorAnchor && mSelectorAnchor.isShowing()) {
                mSelectorAnchor.dismiss();
                return true;
            }

            if (null != mSummaryPopControl && mSummaryPopControl.isShowing()) {
                mSummaryPopControl.dismiss();
            }

            showIsAddToBookshelf();
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // 如果文字选择器正显示，则先隐藏
            if (null != mSelectorAnchor && mSelectorAnchor.isShowing()) {
                mSelectorAnchor.dismiss();
            }

            if (mReadCompleteView.isShowing()) {
                dismissCompleteView();
            } else {
                mPageWidget.prePage();
            }
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 如果文字选择器正显示，则先隐藏
            if (null != mSelectorAnchor && mSelectorAnchor.isShowing()) {
                mSelectorAnchor.dismiss();
            }

            mPageWidget.nextPage();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MARK_CHAPTER_CODE && resultCode == MARK_CHAPTER_CODE) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int begin = (int) bundle.getLong("begin", -1);
                Serializable object = bundle.getSerializable("selectedChapter");
                Serializable object2 = bundle.getSerializable("bookmark");
                Serializable object3 = bundle.getSerializable("booksummary");
                boolean contentChanged = false;
                // 取出选择的章节
                // boolean isCHapterListIn = false;

                Book dbook = DownBookManager.getInstance().getBook(gBook);
                if (dbook == null) {
                    dbook = gBook;
                }
                gBook.setBookmarks(DBService.getAllBookMark(dbook));
                gBook.setBookSummaries(DBService.getAllBookSummary(dbook));

                if (object != null && object instanceof Chapter) {
                    // 点击目录章节项跳转章节
                    mSelectedChapter = (Chapter) object;
                    if (mSelectedChapter.getLength() <= 0) {
                        mIsJump = true;
                        judgeChapterStateAndLoad(mSelectedChapter);
                    } else {
                        if (begin >= 0) {
                            mPageFactory.seek(begin);
                        }
                    }
                } else if (object2 != null && object2 instanceof MarkItem) {
                    // 点击书签
                    String json = ((MarkItem) object2).getMarkJsonString();
                    int tmp_begin = parseHistoryJson(json);
                    if (tmp_begin >= 0) {
                        contentChanged = true;
                        mPageFactory.seek(tmp_begin);
                    } else {
                        // 请求联网
                        mIsJump = true;
                        judgeChapterStateAndLoad(mSelectedChapter);
                    }

                    // if (begin >= 0) {
                    // contentChanged = true;
                    // mPageFactory.seek(begin);
                    // }
                    // MarkItem item = (MarkItem) object2;
                    // contentChanged = jumpHistory(item.getChapterId(), begin);
                } else if (object3 != null && object3 instanceof BookSummary) {
                    // 点击书摘
                    String json = ((BookSummary) object3)
                            .getSummaryJsonString();
                    int tmp_begin = parseHistoryJson(json);
                    if (tmp_begin >= 0) {
                        contentChanged = true;
                        mPageFactory.seek(begin);
                    } else {
                        // 请求联网
                        mIsJump = true;
                        judgeChapterStateAndLoad(mSelectedChapter);
                    }

                    // BookSummary summary = (BookSummary) object3;
                    // contentChanged = jumpHistory(summary.getChapterId(),
                    // begin);
                }

                // if (null != object) {
                // contentChanged = true;
                // isCHapterListIn = true;
                // mSelectedChapter = (Chapter) object;
                //
                // if (mSelectedChapter.getLength() <= 0) {
                // mIsJump = true;
                // judgeChapterStateAndLoad(mSelectedChapter);
                // }
                // }
                // if (begin >= 0) {
                // contentChanged = true;
                // if (mSelectedChapter != null && isCHapterListIn) {
                // if (mSelectedChapter.getLength() > 0) {
                // mPageFactory.seek(begin);
                // }
                // } else {
                // mPageFactory.seek(begin);
                // }
                // }

                invalidateUI(contentChanged);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private int parseHistoryJson(String json) {
        JSONObject lastMarkObj = null;
        try {
            lastMarkObj = new JSONObject(json);
            int lastPosInChapter = lastMarkObj.optInt("lastPosInChapter");
            int lastGlobalChapterId = lastMarkObj.optInt("lastGlobalChapterId");
            if (lastGlobalChapterId > 0) {
                Chapter chapter = gBook
                        .getCurrentChapterById(lastGlobalChapterId);
                mSelectedChapter = chapter;
                if (chapter != null && chapter.getLength() > 0) {
                    // 数据存在
                    int begin = lastPosInChapter
                            + Long.valueOf(chapter.getStartPos()).intValue();
                    return begin;
                } else {
                    mJumpHistoryOffset = lastPosInChapter;
                }
            } else {
                return lastPosInChapter;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // private boolean jumpHistory(String strCp, int begin) {
    // boolean contentChanged = false;
    // // String strCp = mark.getChapterId();
    // try {
    // int chapterId = Integer.parseInt(strCp);
    // ArrayList<Chapter> list = gBook.getChapters();
    // if (list != null && list.size() > 0) {
    // for (int i = 0; i < list.size(); ++i) {
    // Chapter cp = list.get(i);
    // if (cp.getChapterId() == chapterId) {
    // if (cp.getLength() > 0) {
    // int start = (int) cp.getStartPos();
    // int end = (int) (start + cp.getLength());
    // contentChanged = true;
    // if (begin < end) {
    // mPageFactory.seek(begin);
    // } else {
    // mPageFactory.seek(start);
    // }
    // } else {
    // // 请求chapter
    // mSelectedChapter = cp;
    // mIsJump = true;
    // judgeChapterStateAndLoad(mSelectedChapter);
    // }
    // break;
    // }
    // }
    // }
    // } catch (Exception e) {
    // if (begin >= 0) {
    // contentChanged = true;
    // mPageFactory.seek(begin);
    // }
    // }
    // return contentChanged;
    // }

    @Override
    public void onError(int chapterId, int state) {
        updateChapterState(chapterId, state);
        dismissProgress();

        synchronized (SYNCHRONIZED_LOCK) {
            mChapterIdList.remove(Integer.valueOf(chapterId));
        }

        // boolean isShowErrorView = true;
        // if (gBook != null) {
        // Chapter downChapter = gBook.getCurrentChapterById(chapterId);
        // if (downChapter != null) {
        // // 预加载章节内容出现异常时不显示错误UI
        // if (downChapter.getDownChapterWay() == Chapter.DOWN_WAY_PRELOAD) {
        // isShowErrorView = false;
        // }
        // }
        // }

        boolean isPreloadChapter = isPreloadChapter(chapterId);

        LogUtil.d("ReadChapterCount",
                "ReadActivity >> onError >> isPreloadChapter="
                        + isPreloadChapter + ", chapterId=" + chapterId);
        if (!isPreloadChapter) {
            showErrorView();
        }
        // shortToast(R.string.downloading_failed_text);
    }

    private boolean isPreloadChapter(int chapterId) {
        if (gBook != null) {
            Chapter downChapter = gBook.getCurrentChapterById(chapterId);
            LogUtil.d("ReadChapterCount", "isPreloadChapter >> chapterId="
                    + chapterId);
            LogUtil.d("ReadChapterCount", "isPreloadChapter >> downChapter="
                    + downChapter);
            if (downChapter != null) {
                LogUtil.d("ReadChapterCount", "isPreloadChapter >> 1");
                // 预加载章节内容出现异常时不显示错误UI
                if (downChapter.getDownChapterWay() == Chapter.DOWN_WAY_PRELOAD) {
                    LogUtil.d("ReadChapterCount", "isPreloadChapter >> 2");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 免费图书下载完成回调方法.
     *
     * @param book        the book
     * @param mustUpdate  the must update
     * @param mustRefresh the must refresh
     * @param stateCode   the state code
     */
    @Override
    public void onUpdate(Book book, boolean mustUpdate, boolean mustRefresh, int progress,
                         int stateCode) {
        // 更新的书籍，我们不关心
        if (gBook == null || !gBook.equals(book)) {
            return;
        }

        // 我们并未等待回来的事件
        // if (!progressShowing()) {
        // return;
        // }

        // 仅当我们阅读的书籍跟书架书籍相关，处理
        switch (stateCode) {
            case DownBookJob.STATE_PARSER:
                book.setChapters(gBook.getChapters());
                // book.setBookmarks(gBook.getBookmarks());
                break;

            case DownBookJob.STATE_FINISHED:
                mPageWidget.setTouchEnabled(true);
                gBook.getDownloadInfo().setLocationType(BOOK_LOCAL);

                // 在线变成下载完成状态，书签数据重新更改，重新加载书签。
                Book dbook = DownBookManager.getInstance().getBook(gBook);
                if (dbook != null) {
                    gBook.fetchBook(dbook);

                    gBook.setBookmarks(DBService.getAllBookMark(dbook));
                    gBook.setBookSummaries(DBService.getAllBookSummary(dbook));
                }

                Chapter chapter = null;
                if (mSelectedChapter != null) {
                    chapter = mSelectedChapter;
                } else {
                    chapter = mPageFactory.getCurrentChapter();
                }

                if (chapter != null) {
                    // 在获取mJumpOffset之前，做一个判断，判断是否进行了章节补全
                    ArrayList<Chapter> chapters = DBService.getAllChapter(gBook);
                    if (null != chapters && !chapters.isEmpty()) {
                        gBook.setChapters(chapters,
                                "[ReadActivity-updateBookData-1]");
                    }

                    mIsJump = true;
                    updateChapterState(chapter.getGlobalId(),
                            Chapter.CHAPTER_SUCCESS);
                    updateBookData(book, chapter.getGlobalId());

                    // if (mVipBookSelectChapterId > 0) {
                    // mIsJump = true;
                    // updateChapterState(mVipBookSelectChapterId,
                    // Chapter.CHAPTER_SUCCESS);
                    // }
                    // updateBookData(book, mVipBookSelectChapterId);
                    // mVipBookSelectChapterId = -1;

                    mToolbar.getToolbarDownBtn().setVisibility(View.GONE);
                    dismissProgress();
                }
                break;

            case DownBookJob.STATE_FAILED:
                // mVipBookSelectChapterId = -1;
                mPageWidget.setTouchEnabled(true);
                dismissProgress();
                break;

            case DownBookJob.STATE_RUNNING:
            case DownBookJob.STATE_WAITING:
                // 解决：BugID=21483
                isClickAddShelf = true;
                break;

            case DownBookJob.STATE_RECHARGE:
                // mVipBookSelectChapterId = -1;
                gBook.getBuyInfo().setHasBuy(false);
                mPageWidget.setTouchEnabled(true);
                dismissProgress();
                PayDialog.showBalanceDlg(this);
                break;

            default:
                // 暂不处理
                dismissProgress();
                break;
        }
    }

    private void saveBookHistory(final Book book) {
        new GenericTask() {
            protected TaskResult doInBackground(TaskParams... params) {
                DBService.saveBookRelateInfo(book);
                return null;
            }
        }.execute();
    }

    private void parseBookmarkFromJson() {
        List<MarkItem> marklist = DBService.getAllBookMark(gBook);
        if (marklist != null && marklist.size() > 0) {
            if (marklist.size() > gBook.getBookmarks().size()) {
                gBook.setBookmarks(marklist);
                for (MarkItem bookmark : marklist) {
                    bookmark.parsePosFromJson(gBook, false);
                }
                // gBook.parsePosFromJson();
            }
        }
    }

    // private void parseBookmarkFromJson2() {
    // List<MarkItem> marklist = DBService.getAllBookMark(gBook);
    // if (marklist != null && marklist.size() > 0) {
    // gBook.setBookmarks(marklist);
    // for (MarkItem bookmark : marklist) {
    // bookmark.parsePosFromJson(gBook, false);
    // }
    // }
    // }

    /**
     * <p>
     * 以下情况会回调此方法：<br>
     * 1.从书籍详情进入阅读界面，在线阅读书籍 <br>
     * 2.书架中没有下载完或者连载的书籍阅读时<br>
     * 3.微博呼起，如果本地没有指定的书籍，下载章节内容时 <br>
     * 4.阅读时遇到付费章节，购买下载该章节内容时<br>
     * </p>
     *
     * @param chapterId the chapter id
     * @param state     the state
     * @param typeCode  下载类型 {@link ReadActivity#READ_ONLINE_BOOK}
     *                  {@link ReadActivity#READ_LOCAL_BOOK}
     *                  {@link ReadActivity#READ_WEIBO_BOOK}
     *                  {@link ReadActivity#READ_PAY_BOOK}
     */
    @Override
    public void onSuccess(int chapterId, int state, int typeCode) {
        LogUtil.i("ouyang", "ReadActivity >> onSuccess >> chapterId=" + chapterId);

        mCanFinish = false;
        // 如果book文件夹被删除，在线加载章节后，书签重新计算。
        if (isLocalBookMissing) {
            gBook.parsePosFromJson();
            saveBookHistory(gBook);
        } else {
            if (isDownChapterAfterLoginThenNeedToReloadCacheData) {
                // 在阅读页登陆成功并下载章节后，读取书签并重新计算书签索引
                parseBookmarkFromJson();
                // List<BookSummary> summarlist =
                // DBService.getAllBookSummary(gBook);
                // if (summarlist != null && summarlist.size() > 0) {
                // if (summarlist.size() > gBook.getBookSummaries().size()) {
                // gBook.setBookSummaries(summarlist);
                // for (BookSummary summar : summarlist) {
                // summar.parsePosFromJson(gBook);
                // }
                // }
                // }
                isDownChapterAfterLoginThenNeedToReloadCacheData = false;
            }
        }

        // CJL BugID=21492
        // if (isDownChapterAfterLoginThenNeedToReloadCacheData) {
        // 方案1：读取缓存
        // String ownerUid = LoginUtil.getLoginInfo().getUID();
        // List<Book> ownerBookCaches = DBService.getAllBookCache(ownerUid);
        // for (Book cacheBook : ownerBookCaches) {
        // if (cacheBook != null && cacheBook.equals(gBook)) {
        // List<MarkItem> markList = DBService.getAllBookMark(cacheBook);
        // if (markList != null) {
        // for (int i = 0; i < markList.size(); i++) {
        // MarkItem markItem = markList.get(i);
        // markItem.parsePosFromJson(gBook, false);
        // }
        // gBook.fetchBookmarks(markList);
        // }
        // break;
        // }
        // }

        // 方案2：读取书架
        // Book shelfBook = DownBookManager.getInstance().getBook(gBook);
        // if (shelfBook != null) {
        // List<MarkItem> markList = DBService.getAllBookMark(shelfBook);
        // if (markList != null) {
        // for (int i = 0; i < markList.size(); i++) {
        // MarkItem markItem = markList.get(i);
        // markItem.parsePosFromJson(gBook, false);
        // }
        // gBook.fetchBookmarks(markList);
        // }
        // }
        // isDownChapterAfterLoginThenNeedToReloadCacheData = false;
        // }

        // if (isDownChapterAfterLoginThenNeedToReloadCacheData) {
        // isDownChapterAfterLoginThenNeedToReloadCacheData = false;
        // gBook.parsePosFromJson();
        // }

        updateChapterState(chapterId, state);

        if (state == Chapter.CHAPTER_SUCCESS) {
            switch (typeCode) {
                case READ_ONLINE_BOOK:
                    LogUtil.i("ouyang", "ReadActivity >> onSuccess >> READ_ONLINE_BOOK");
                    updatePageFactory();
                    updateReadingView();
                    break;

                case READ_WEIBO_BOOK:
                    LogUtil.d(TAG, "READ_WEIBO_BOOK");
                    updatePageFactory();
                    updateReadingView();
                    break;

                case READ_PAY_BOOK:
                    LogUtil.i("ouyang", "ReadActivity >> onSuccess >> READ_PAY_BOOK");
                    updatePageFactory();
                    updateData();
                    dismissCompleteView();
                    break;

                default:
                    LogUtil.i("ouyang", "ReadActivity >> onSuccess >> READ_LOCAL_BOOK");
                    updateData();
                    break;
            }
        } else if (state == Chapter.CHAPTER_RECHARGE) {
            // 余额不足
            boolean isPreloadChapter = isPreloadChapter(chapterId);
            if (!isPreloadChapter) {
                PayDialog.showBalanceDlg(mContext);
            } else {
                // 预加载章节时，下载章节内容失败，如果是VIP章节
                // 那么很可能是用户勾选了“自动购买”CheckBox，结果
                // 余额不足了，无法扣款成功导致走到这里来
                // 此时要及时的将“自动购买”属性值取消掉，否则
                // 将在点击翻页时导致连续的获取该VIP章节内容
                // 造成恶意刷取章节内容的后果，服务器会返回"请勿连续刷取"字符
                // 客户端不断的Toast展示的后果
                if (gBook != null) {
                    BookBuyInfo buyInfo = gBook.getBuyInfo();
                    if (buyInfo != null) {
                        buyInfo.setAutoBuy(false);
                    }
                }
            }
        } else if (state == Chapter.CHAPTER_NEED_BUY) {
            // 判断登录信息
            if (LoginUtil.isValidAccessToken(this) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
                // if (LoginUtil.getLoginInfo() != null &&
                // TextUtils.isEmpty(LoginUtil.getLoginInfo().getUID())) {
                shortToast(String
                        .format(getString(R.string.need_login_buy), ""));
            } else {
                shortToast(String.format(getString(R.string.need_login_buy),
                        "登录"));
            }
            disableAddShelf = true;
        }

        Chapter chapter = gBook.getCurrentChapterById(chapterId);
        Chapter curChapter = mPageFactory.getCurrentChapter();

        LogUtil.d("ouyang",
                "ReadActivity >> onSuccess >> gBook =" + gBook);

        LogUtil.d("ouyang",
                "ReadActivity >> onSuccess >> {chapter=" + chapter + "}");
        LogUtil.d("ouyang",
                "ReadActivity >> onSuccess >> {curChapter=" + curChapter + "}");

        // 下载完成时更新进度条
        updateToolbar(curChapter);

        if (state == Chapter.CHAPTER_SUCCESS) {
            // 在线阅读至第一页或最后一页等待下一章下载完成后自动翻页
            switch (mAutoFlip) {
                case FLIP_NEXT_PAGE:// 自动翻到下一页
                    LogUtil.d("ouyang", "ReadActivity >> onSuccess >> {自动翻到下一页}");
                    // 先计算该章节页数
                    mPageFactory.calculateChapterPagesSync(chapter);
                    dismissProgress();
                    makeFullScreen(true);

                    mAutoFlip = FLIP_DEFAULT;
                    mPageWidget.nextPage();
                    break;

                case FLIP_PRE_PAGE:// 自动翻到上一页
                    LogUtil.d("ouyang", "ReadActivity >> onSuccess >> {自动翻到上一页}");
                    // 先计算该章节页数
                    mPageFactory.calculateChapterPagesSync(chapter);
                    dismissProgress();
                    makeFullScreen(true);

                    mAutoFlip = FLIP_DEFAULT;
                    mPageWidget.prePage();
                    break;

                default:
                    // 强制进行该章节页数计算，避免出现章节页数无法解析的问题
                    // LogUtil.d("ReadChapterCount",
                    // "ReadActivity >> onSuccess >> {state == Chapter.CHAPTER_SUCCESS >> default}");
                    mPageFactory.calculateChapterPages(chapter);
                    dismissProgress();
                    makeFullScreen(true);
                    break;
            }
        } else {
            // LogUtil.d("ReadChapterCount",
            // "ReadActivity >> onSuccess >> {state != Chapter.CHAPTER_SUCCESS}");
            // 强制进行该章节页数计算，避免出现章节页数无法解析的问题
            mPageFactory.calculateChapterPages(chapter);
            dismissProgress();
            makeFullScreen(true);
        }

        // 首次进入在线阅读需要显示引导画面
        if (TYPE_ONLINE.equals(mReadOnline)) {
            // 如果"赞"页面正好显示，则忽略，等下次进入时再显示
            if (!mReadCompleteView.isShowing()) {
                MaskGuideActivity.launch(this, StorageUtil.KEY_SHOW_GUIDE_READ,
                        R.layout.guide_read);
            }
        }

        synchronized (SYNCHRONIZED_LOCK) {
            mChapterIdList.remove(Integer.valueOf(chapterId));
        }
    }

    @SuppressLint("NewApi")
    private void initViews() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // makeFullScreen(true);

        setContentView(R.layout.act_read);

        // 阅读内容view
        mPageWidget = (PageWidget) findViewById(R.id.reading_pagewidget);

        mBookmarkFlag = (ImageView) findViewById(R.id.reading_bookmark_flag);

        // 阅读完的View
        mReadCompleteView = (ReadCompleteView) findViewById(R.id.reading_complete_view);

        // 错误View
        mErrorView = findViewById(R.id.reading_error_view);
        mBackView = mErrorView.findViewById(R.id.back_btn);
        // 网络设置
        mErrorView.findViewById(R.id.net_set_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(
                                Settings.ACTION_WIRELESS_SETTINGS));
                    }
                });

        // 工具栏
        mToolbar = (ReadToolbar) findViewById(R.id.book_tool_bar);
        mToolbar.setListener(this, this, new ReadSeekBarListener());
        mToolbar.init(mReadStyleManager);

        // 下拉增加书签View
        mPullDownLayout = findViewById(R.id.reading_add_mark_view);
        mPullDownText = (TextView) mPullDownLayout
                .findViewById(R.id.pulldown_text);
        mPullDownImage = (ImageView) mPullDownLayout
                .findViewById(R.id.pulldown_image);
    }

    private void postInit() {
        // 抛出一个异步任务进行其他初始化
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                initTextSelectorMenu();
                initBookSummaryPopControl();

                updateViews();
            }
        });
    }

    /**
     * 初始化文字选择器菜单
     */
    private void initTextSelectorMenu() {
        if (null != gBook) {
            if (gBook.isOurServerBook() || gBook.isVDiskBook()) {
                mTextSelectorMenuList = Arrays.asList(ResourceUtil
                        .getStringArrays(R.array.selector_menu_0));
            } else {
                mTextSelectorMenuList = Arrays.asList(ResourceUtil
                        .getStringArrays(R.array.selector_menu_1));
            }

            mSelectorAnchor = new SelectorAnchor(
                    mPageWidget.initTextSelectorMenu(mTextSelectorMenuList,
                            mReadStyleManager));
            mSelectorAnchor.initTextSelector();
        }
    }

    private void initBookSummaryPopControl() {
        if (null != gBook) {
            if (gBook.isOurServerBook() || gBook.isVDiskBook()) {
                mSummaryMenuList = Arrays.asList(ResourceUtil
                        .getStringArrays(R.array.summary_menu_0));
            } else {
                mSummaryMenuList = Arrays.asList(ResourceUtil
                        .getStringArrays(R.array.summary_menu_1));
            }
            mSummaryPopControl = new BookSummaryPopControl();
        }
    }

    private void initViewListener() {
        mBookmarkFlag.setOnClickListener(this);

        mReadCompleteView.setViewClickListener(this);
        mReadCompleteView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mReadCompleteView.isShowing()) {
                        dismissCompleteView();
                    }
                }
                return true;
            }
        });

        // 返回按钮的监听
        mBackView.setOnClickListener(this);
    }

    /**
     * 更新阅读页.
     */
    private void updatePageFactory() {
        try {
            LogUtil.i("ouyang", "ReadActivity >> updatePageFactory >> mPageFactory.openBook");
            mPageFactory.openBook(gBook);
        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage());
        }

        mPageWidget.forceInvalidate();
    }

    private void updateViews() {
        // 刷新文字选择器
        if (null != mSelectorAnchor) {
            mSelectorAnchor.refresh();
        }

        // 更新title按钮状态
        updateBookTypeBtn();
        updateBookmarkFlag();
    }

    /**
     * 更新阅读控件
     */
    private void updateReadingView() {
        if (mIsVIPChapter) {
            showCompleteView(ReadCompleteView.TYPE_READ_CHAPTER,
                    gBook.getTitle());
            mIsVIPChapter = false;
        } else {
            dismissCompleteView();
        }
    }

    /**
     * 显示继续阅读页面
     *
     * @param showType
     * @param title
     */
    private void showCompleteView(final int showType, final String title) {
        if (mPlayingAnim || mReadCompleteView.isShowing()) {
            return;
        }

        // 更新相关View的状态
        updateViews();
        // 隐藏进度条
        dismissProgress();
        mReadCompleteView.show(showType, title, mBookId);

        Animation animation = AnimationUtils.loadAnimation(ReadActivity.this,
                R.anim.push_right_in);
        if (null != animation) {
            animation.setDuration(400);
            animation.setFillAfter(true);
            animation.setFillEnabled(true);
            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    mPlayingAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mPlayingAnim = false;
                    mReadCompleteView.clearAnimation();
                }
            });
            mReadCompleteView.startAnimation(animation);
        }
    }

    private void dismissCompleteView() {
        dismissCompleteView(false);
    }

    /**
     * 隐藏继续阅读页面
     */
    private void dismissCompleteView(boolean diableAnimation) {
        if (mPlayingAnim || !mReadCompleteView.isShowing()) {
            return;
        }

        if (diableAnimation) {
            mPlayingAnim = false;
            mReadCompleteView.setVisibility(View.GONE);
            return;
        }

        Animation animation = AnimationUtils.loadAnimation(ReadActivity.this,
                R.anim.push_right_out);
        if (null != animation) {
            animation.setDuration(400);
            animation.setFillAfter(true);
            animation.setFillEnabled(true);
            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    mPlayingAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mPlayingAnim = false;
                    mReadCompleteView.setVisibility(View.GONE);
                    mReadCompleteView.clearAnimation();
                    // clearSelectChapterInfo();
                }
            });
            mReadCompleteView.startAnimation(animation);
        }
    }

    private void clearSelectChapterInfo() {
        mSelectedChapter = null;
    }

    /**
     * 显示错误页面
     */
    private void showErrorView() {

        mReadCompleteView.setVisibility(View.GONE);

        mPageWidget.setTouchEnabled(false);

        mErrorView.setVisibility(View.VISIBLE);
    }

    /**
     * 全屏
     *
     * @param enable 是否全屏
     */
    private void makeFullScreen(boolean enable) {
        if (enable) {
            // 如果工具栏或者进度条未隐藏，则直接返回
            if (mToolbar.isShown() || progressShowing()) {
                return;
            }

            hideSystemBar();
        } else {
            showSystemBar();
        }
    }

    private void hideSystemBar() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(lp);

        // 隐藏底部导航栏
//        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
//        if (Build.VERSION.SDK_INT >= 14) {
//            uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        }
//
//        if (Build.VERSION.SDK_INT >= 16) {
//            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//            uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//        }
//
//        if (Build.VERSION.SDK_INT >= 18) {
//            uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        }
//        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    private void showSystemBar() {
        WindowManager.LayoutParams attr = getWindow().getAttributes();
        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attr);

        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//            uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }


    /**
     * 设置屏幕亮度
     */
    private void setScreenBrightness() {
        if (StorageUtil.getBoolean(StorageUtil.KEY_AUTO_BRIGHTNESS, true)) {
            BrightnessUtil.setScreenBrightnessMode(this,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            BrightnessUtil.setCurrentScreenDefault(this);

        } else {
            BrightnessUtil.setScreenBrightnessMode(this,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            BrightnessUtil.setCurrentScreenBrightness(this,
                    StorageUtil.getBrightness());
        }
    }

    /**
     * 刷新数据.
     */
    public void invalidateUI(boolean contentChanged) {
        updateBookmarkFlag();
        mPageWidget.forceInvalidate(contentChanged);

        Chapter curChapter = mPageFactory.getCurrentChapter();
        // LogUtil.d("ReadChapterCount",
        // "ReadActivity >> mFinishedRefreshListener >> {curChapter=" +
        // curChapter + "}");
        updateToolbar(curChapter);
    }

    /**
     * 更新当前书签状态,同时更新章节信息.
     */
    private void updateBookmarkFlag() {
        mCurMark = mPageFactory.getCurBookMark();

        // 滑动翻页不显示书签
        if (mReadStyleManager.getReadAnim() == ReadStyleManager.ANIMATION_TYPE_SCROLL) {
            mBookmarkFlag.setVisibility(View.GONE);
            return;
        }

        if (mCurMark != null) {
            mBookmarkFlag.setVisibility(View.VISIBLE);
            mPullDownText.setText(R.string.pulldown_tv_del_normal);
        } else {
            mBookmarkFlag.setVisibility(View.GONE);
            mPullDownText.setText(R.string.pulldown_tv_add_normal);
        }

        rotateArrowDown();
    }

    /**
     * 旋转箭头向下
     */
    private void rotateArrowDown() {
        mPullDownImage.clearAnimation();
        Animation rotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateDownAnim.setDuration(180);
        rotateDownAnim.setFillAfter(true);
        mPullDownImage.startAnimation(rotateDownAnim);
    }

    /**
     * 旋转箭头向上
     */
    private void rotateArrowUp() {
        mPullDownImage.clearAnimation();
        Animation rotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateUpAnim.setDuration(180);
        rotateUpAnim.setFillAfter(true);
        mPullDownImage.startAnimation(rotateUpAnim);
    }

    /**
     * 添加书签.
     */
    private void addBookMark() {
        mCurMark = mPageFactory.addBookMark();
        if (mCurMark == null) {
            shortToast(R.string.add_mark_failed);
        } else {
            shortToast(R.string.add_mark_succ);
        }
        updateBookmarkFlag();
    }

    /**
     * 删除书签.
     */
    private void deleteBookMark() {
        mPageFactory.deleteBookMark(mCurMark);
        mCurMark = null;
        shortToast(R.string.delete_mark_succ);
        updateBookmarkFlag();
    }

    /**
     * 显示或隐藏工具栏.
     */
    private void showOrDisToolBar() {
        if (null != mToolbar) {
            if (!mToolbar.isShown()) {
                // 如果阅读完成页面正显示则不再弹出工具栏
                if (!mReadCompleteView.isShowing()) {
                    showToolBar();
                }
            } else {
                disToolBar();
            }
        }
    }

    /**
     * 显示工具栏.
     */
    private void showToolBar() {
        makeFullScreen(false);

        // float progress = 0;

        if (gBook == null) {
            return;
        }

        Chapter curChapter = mPageFactory.getCurrentChapter();
        // LogUtil.i("BugID=21356",
        // "ReadActivity >> showToolBar >> {curChapter =" + curChapter + "}");
        ArrayList<Chapter> chapters = gBook.getChapters();

        // if (gBook.isSingleChapterBook()) {
        // progress = mPageFactory.getCurrentPercent();
        // } else {
        // if (null != curChapter && chapters.size() > 0) {
        // progress = curChapter.getChapterId() * 100F / chapters.size();
        // }
        // }

        boolean showUpdateFlag = gBook.getUpdateChaptersNum() > 0;
        boolean hasMark = null != mPageFactory.getCurBookMark();
        boolean showWeibo = gBook.isOurServerBook();
        String chapterTitle = "";
        int progress = 0;
        if (null != curChapter) {
            progress = curChapter.getChapterId();
            chapterTitle = String.format(
                    getString(R.string.chapter_information), progress,
                    chapters.size());
        }
        mToolbar.show(progress, showUpdateFlag, showWeibo, hasMark,
                gBook.isSeriesBook(), chapterTitle, "2");
        // mToolbar.show((int) progress, showUpdateFlag, showWeibo, hasMark,
        // gBook.isSeriesBook(), chapterTitle);
    }

    /**
     * 隐藏工具栏.
     */
    private void disToolBar() {
        mToolbar.dismiss();
        hideSystemBar();
    }

    /**
     * 隐藏工具栏，但不全屏
     */
    private void disToolBarWithoutFull() {
        mToolbar.dismiss();
    }

    private void downloadChapter(Chapter chapter, boolean showProgress,
                                 int typeCode) {
        downloadChapter(chapter, showProgress, typeCode, false);
    }

    /**
     * 在线阅读的情况下，预先下载下一章节的内容.
     *
     * @param chapter      要下载的章节
     * @param showProgress 是否显示进度条
     * @param typeCode     下载类型 {@link ReadActivity#READ_ONLINE_BOOK}
     *                     {@link ReadActivity#READ_LOCAL_BOOK}
     *                     {@link ReadActivity#READ_WEIBO_BOOK}
     *                     {@link ReadActivity#READ_PAY_BOOK}
     */
    private void downloadChapter(Chapter chapter, boolean showProgress,
                                 int typeCode, boolean isPreLoad) {
        LogUtil.d("ReadChapterCount",
                "ReadActivity >> downloadChapter >> isPreLoad=" + isPreLoad
                        + ", chapter=" + chapter);
        LogUtil.d("BugID=21492",
                "ReadActivity >> downloadChapter >> isPreLoad=" + isPreLoad
                        + ", chapter=" + chapter);
        // 如果是SD卡上的书籍，则不需要下载
        if (BOOK_SDCARD == gBook.getDownloadInfo().getLocationType()) {
            return;
        }

        int chapterState = getChapterStates(chapter);
        if (CHAPTER_NO != chapterState) {
            if (CHAPTER_VIP == chapterState
                    && !gBook.getBuyInfo().canAutoBuy(chapter)
                    && !chapter.hasBuy() && !gBook.isForbidden()) {
                // 如果是收费章节，且不是自动购买，则直接返回
                LogUtil.i("ReadChapterCount",
                        "ReadActivity >> downloadChapter >> 1");
                return;
            }

            if (chapter.getLength() > 0) {
                // 如果内容已经存在，则直接返回
                LogUtil.i("ReadChapterCount",
                        "ReadActivity >> downloadChapter >> 2");
                return;
            }

            // 书籍正在下载当中
            if (showProgress && chapter.getState() == Chapter.CHAPTER_RUNNING) {
                showProgress(R.string.downloading_text, true,
                        mDialogKeyListener);
                // 书籍未下载成功
            } else if (chapter.getState() != Chapter.CHAPTER_SUCCESS) {
                if (showProgress) {
                    showProgress(R.string.downloading_text, true,
                            mDialogKeyListener);
                }
                // 避免重复请求
                if (mChapterIdList.contains(chapter.getGlobalId())) {
                    LogUtil.i("ReadChapterCount",
                            "ReadActivity >> downloadChapter >> 3");
                    return;
                }

                synchronized (SYNCHRONIZED_LOCK) {
                    mChapterIdList.add(chapter.getGlobalId());
                    if (isPreLoad) {
                        chapter.setDownChapterWay(Chapter.DOWN_WAY_PRELOAD);
                    } else {
                        chapter.setDownChapterWay(Chapter.DOWN_WAY_NORMAL);
                    }
                    chapter.setState(Chapter.CHAPTER_RUNNING);
                    if (!chapter.hasBuy()) {
                        chapter.setHasBuy(gBook.getBuyInfo().isAutoBuy());
                    }
                    // 免费的章节传true带上登录信息，服务器可以记录
                    if (getChapterStates(chapter) == CHAPTER_FREE) {
                        downloadChapterContent(chapter.getGlobalId(), true,
                                typeCode);
                    } else {
                        mEnterHasAutoBuy = true;
                        downloadChapterContent(chapter.getGlobalId(),
                                chapter.hasBuy(), typeCode);
                    }

                    // 在线书籍，记录在线阅读章节ID
                    if (gBook.isOnlineBook()) {
                        gBook.setOnlineReadChapterId(chapter.getGlobalId(),
                                "ReadActivity-downloadChapter");
                    }
                }
            }
        }
    }

    /**
     * 下载指定章节内容.
     *
     * @param globalId 章节Global Id
     * @param hasBuy   是否已购买
     * @param typeCode 下载类型 {@link ReadActivity#READ_ONLINE_BOOK}
     *                 {@link ReadActivity#READ_LOCAL_BOOK}
     *                 {@link ReadActivity#READ_WEIBO_BOOK}
     *                 {@link ReadActivity#READ_PAY_BOOK}
     */
    private void downloadChapterContent(int globalId, boolean hasBuy,
                                        int typeCode) {
        LogUtil.i("ReadChapterCount",
                "ReadActivity >> downloadChapterContent >> globalId="
                        + globalId + ", hasBuy=" + hasBuy + ", typeCode="
                        + typeCode);
        OnlineBookManager.getInstance().setListener(this);
        OnlineBookManager.getInstance().readChapter(this, gBook, globalId,
                hasBuy, typeCode, mBookKey);
    }

    /**
     * 判断章节状态类型，是否下载.
     *
     * @param chapter the chapter
     */
    private void judgeChapterStateAndLoad(Chapter chapter) {
        if (null == chapter) {
            return;
        }

        if (chapter.getLength() > 0) {
            // 如果已经下载，则直接返回
            return;
        }

        switch (getChapterStates(chapter)) {
            case CHAPTER_VIP:
                mSelectedChapter = chapter;
                downloadBookOrChapter(chapter, READ_PAY_BOOK);
                break;

            case CHAPTER_FREE:
                mSelectedChapter = chapter;
                downloadChapter(chapter, true, READ_LOCAL_BOOK);
                break;

            case CHAPTER_NO:
                shortToast(R.string.chapter_is_null);
                break;

            default:
                break;
        }
    }

    /**
     * 该方法用于区分整本收费和章节收费下载逻辑，仅适用于已知章节需要购买的情况.
     *
     * @param chapter  指定章节
     * @param typeCode 下载类型 {@link ReadActivity#READ_ONLINE_BOOK}
     *                 {@link ReadActivity#READ_LOCAL_BOOK}
     *                 {@link ReadActivity#READ_WEIBO_BOOK}
     *                 {@link ReadActivity#READ_PAY_BOOK}
     */
    private void downloadBookOrChapter(Chapter chapter, int typeCode) {
        int payType = gBook.getBuyInfo().getPayType();
        mSelectedChapter = chapter;

        if (BOOK_TYPE_CHAPTER_VIP == payType) {
            if (gBook.getBuyInfo().canAutoBuy(chapter) || chapter.hasBuy()) {
                chapter.setHasBuy(true);
                downloadChapter(chapter, true, typeCode);

            } else {
                mSelectedChapter = chapter;
                mIsVIPChapter = true;

                showCompleteView(ReadCompleteView.TYPE_READ_CHAPTER,
                        chapter.getTitle());
            }

        } else if (BOOK_TYPE_VIP == payType) {
            mSelectedChapter = chapter;

            if (gBook.isForbidden() && gBook.getBuyInfo().isHasBuy()) {
                mIsVIPChapter = true;
                mSelectedChapter.setHasBuy(true);
                downloadChapter(mSelectedChapter, true, READ_PAY_BOOK);
            } else {
                PriceTip tip = gBook.getBuyInfo().getPriceTip();
                if (tip != null
                        && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
                    isBookcard = true;
                } else {
                    isBookcard = false;
                }

                if (isBookcard) {
                    // 兑书卡，直接下载
                    doPayProcess();
                } else {
                    mIsVIPChapter = true;
                    showCompleteView(ReadCompleteView.TYPE_READ_CHAPTER,
                            chapter.getTitle());
                }

            }
        }
        disToolBar();
    }

    private boolean isBookcard;

    /**
     * 获取指定章节的状态.
     *
     * @param chapter the chapter
     * @return the chapter states{@link Chapter#CHAPTER_VIP}
     * {@link Chapter#CHAPTER_FREE} {@link Chapter#CHAPTER_NO}
     */
    private int getChapterStates(Chapter chapter) {
        if (null != chapter) {

            if (chapter.isVip()) {
                return CHAPTER_VIP;
            } else {
                return CHAPTER_FREE;
            }
        }
        return CHAPTER_NO;
    }

    /**
     * 更新工具栏下载/购买按钮.
     */
    private void updateBookTypeBtn() {
        if (BOOK_SDCARD != gBook.getDownloadInfo().getLocationType()) {
            boolean hasBook = DownBookManager.getInstance().hasBook(gBook);

            switch (gBook.getBuyInfo().getPayType()) {

                case BOOK_TYPE_FREE:
                    if ((!hasBook || gBook.isOnlineBook()) && !gBook.isForbidden()) {
                        mToolbar.getToolbarDownBtn().setVisibility(View.VISIBLE);
                        mToolbar.getToolbarDownBtn().setBackgroundResource(
                                R.drawable.read_down);
                    } else {
                        mToolbar.getToolbarDownBtn().setVisibility(View.GONE);
                    }
                    break;

                case BOOK_TYPE_VIP:
                    if (!gBook.isForbidden()) {
                        // if ((!hasBook || gBook.isOnlineBook()) &&
                        // !gBook.isForbidden()) {

                        if (!gBook.getBuyInfo().isHasBuy()) {
                            mToolbar.getToolbarDownBtn()
                                    .setVisibility(View.VISIBLE);
                            mToolbar.getToolbarDownBtn().setBackgroundResource(
                                    R.drawable.read_buy);
                        } else {
                            if (!hasBook || gBook.isOnlineBook()) {
                                mToolbar.getToolbarDownBtn().setVisibility(
                                        View.VISIBLE);
                                mToolbar.getToolbarDownBtn().setBackgroundResource(
                                        R.drawable.read_down);
                            } else {
                                mToolbar.getToolbarDownBtn().setVisibility(
                                        View.GONE);
                            }
                        }
                    } else {
                        mToolbar.getToolbarDownBtn().setVisibility(View.GONE);
                    }
                    break;

                case BOOK_TYPE_CHAPTER_VIP:
                    mToolbar.getToolbarDownBtn().setVisibility(View.GONE);
                    break;

                default:
                    mToolbar.getToolbarDownBtn().setVisibility(View.GONE);
                    break;
            }
        } else {
            mToolbar.getToolbarDownBtn().setVisibility(View.GONE);
        }
    }

    /**
     * 请求书籍详情，不需要toast提示的情况
     */
    private void reqBookInfo() {
        reqBookInfo(false);
    }

    /**
     * 后台请求书籍详情,及时更新部分属性值(是否赞、赞数量、包月信息以及购买信息等)
     */
    private void reqBookInfo(boolean showToast) {
        final boolean isShowToast = showToast;
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO,
                gBook.getBookId(), gBook.getSid(), gBook.getBookSrc());
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }

        RequestTask reqTask = new RequestTask(new BookDetailParser());
        reqTask.bindActivity(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

        reqTask.setTaskFinishListener(new ITaskFinishListener() {
            @Override
            public void onTaskFinished(TaskResult taskResult) {
                if (null != taskResult
                        && taskResult.stateCode == HttpStatus.SC_OK) {
                    if (taskResult.retObj instanceof BookDetailData) {
                        BookDetailData data = (BookDetailData) taskResult.retObj;
                        if (null != gBook) {
                            gBook.update(data.getBook());
                            mToolbar.updatePraiseStatus();

                            if (isShowToast) {
                                if (gBook.hasPraised()) {
                                    shortToast(R.string.book_detail_has_praised_note);
                                } else {
                                    praise();
                                }
                            }
                        }
                    }
                }
            }
        });
        reqTask.execute(params);
    }

    /**
     * 请求章节列表，完成后下载指定章节内容
     *
     * @param chapter
     */
    private void reqChapterList(Chapter chapter) {
        mCanFinish = true;
        showProgress(R.string.reading_opening, false, mDialogKeyListener);
        // 获取书本的章节列表信息
        ArrayList<Chapter> chapters = gBook.getChapters();
        // 若章节列表存在则直接下载章节内容
        if (null != chapters && chapters.size() > 0) {
            // 清除所有章节的本地信息
            for (Chapter chap : chapters) {
                chap.clearLocalInfo();
            }

            if (chapter == null && gBook.isOnlineBook()) {
                gBook.getReadInfo().parseLastChapterId(gBook);
            }

            // 判断章节是否收费
            switch (getChapterStates(chapter)) {
                case CHAPTER_VIP:
                    reqChapterContent();
                    downloadBookOrChapter(chapter, READ_PAY_BOOK);
                    break;

                case CHAPTER_FREE:
                    downloadChapterContent(chapter.getGlobalId(), true,
                            READ_ONLINE_BOOK);
                    break;

                default:
                    reqChapterContent();
                    break;
            }
            return;
        }

		/*
         * 解决在线书籍仍然有阅读进度(Book.getReadInfo有索引数据)，引起章节下载完成后，跳转对应索引发现超出章节长度， 造成白屏。
		 */
        if (gBook != null && gBook.isOnlineBook()
                && gBook.getReadInfo().getLastPos() > 0) {
            gBook.getReadInfo().setLastPos(0);
        }

        reqBookChapterList(chapter, false);

        // String reqUrl = String.format(URL_GET_CHAPTERS, gBook.getBookId(),
        // gBook.getSid(), gBook.getBookSrc());
        // reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        // if (mBookKey != null) {
        // reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        // }
        //
        // ChapterListParser chaptersParser = new ChapterListParser();
        // if (gBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
        // chaptersParser.setAllBookHasBuy(gBook.getBuyInfo().isHasBuy());
        // }
        //
        // if (null != mReqChapterListTask) {
        // mReqChapterListTask.cancel(true);
        // }
        //
        // mReqChapterListTask = new RequestTask(chaptersParser);
        // mReqChapterListTask.bindActivity(this);
        // mReqChapterListTask.setTaskFinishListener(new
        // ChapterListFinishListener(chapter));
        //
        // TaskParams params = new TaskParams();
        // params.put(RequestTask.PARAM_URL, reqUrl);
        // params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        // mReqChapterListTask.execute(params);
    }

    private void reqBookChapterList(Chapter chapter, boolean isLocal) {

        LogUtil.i("ReadChapterCount",
                "ReadActivity >> reqBookChapterList >> chapter=" + chapter);

        String reqUrl = String.format(URL_GET_CHAPTERS, gBook.getBookId(),
                gBook.getSid(), gBook.getBookSrc());
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }
        LogUtil.e("ReadChapterCount",
                "ReadActivity >> reqBookChapterList >> reqUrl=" + reqUrl);

        ChapterListParser chaptersParser = new ChapterListParser();
        if (gBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
            chaptersParser.setAllBookHasBuy(gBook.getBuyInfo().isHasBuy());
        }

        if (null != mReqChapterListTask) {
            mReqChapterListTask.cancel(true);
        }

        mReqChapterListTask = new RequestTask(chaptersParser);
        mReqChapterListTask.bindActivity(this);
        mReqChapterListTask
                .setTaskFinishListener(new ChapterListFinishListener(chapter,
                        isLocal));

        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        mReqChapterListTask.execute(params);
    }

    /**
     * 自动分享微博
     */
    private void autoShareWeibo() {
        Bitmap cover = ImageLoader.getInstance().syncLoadBitmap(
                gBook.getDownloadInfo().getImageUrl());
        cover = (null == cover) ? ImageLoader.getDefaultPic() : cover;

        Bitmap coverImage = ImageUtil
                .zoom(cover, mDisplayWidth, mDisplayHeight);

        Chapter curChapter = mPageFactory.getCurrentChapter();
        if (null != curChapter) {
            WeiboContent content = new WeiboContent(gBook,
                    WeiboContent.TYPE_PRAISE);
            content.setMsg(gBook.getIntro());
            content.setChapterId(curChapter.getGlobalId());
            content.setChapterOffset(0);
            String text = content.getMsg();

            reqPostWeibo(curChapter, text);
        } else {
            shortToast(R.string.share_weibo_failed);
        }

        if (coverImage != null) {
            coverImage.recycle();
        }
    }

    /**
     * 发送微博
     *
     * @param curChapter 当前章节
     * @param msg        信息
     */
    private void reqPostWeibo(Chapter curChapter, String msg) {
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
        strNParams.add(new BasicNameValuePair("sid", gBook.getSid()));
        strNParams.add(new BasicNameValuePair("b_id", gBook.getBookId()));
        strNParams.add(new BasicNameValuePair("b_src", gBook.getBookSrc()));
        strNParams.add(new BasicNameValuePair("c_id", ""
                + curChapter.getGlobalId()));
        strNParams.add(new BasicNameValuePair("c_offset", ""));
        strNParams.add(new BasicNameValuePair("u_comment", msg));
        if (mBookKey != null) {
            strNParams.add(new BasicNameValuePair(KEY, mBookKey));
        }

        RequestTask task = new RequestTask(new SimpleParser());
        task.bindActivity(this);
        task.setPostParams(strNParams);
        task.setTaskFinishListener(new PostWeiboFinishListener());
        task.execute(params);

        UserActionManager.getInstance().recordEvent(
                Constants.ACTION_SHARE_WEIBO);
    }

    /**
     * 赞一个
     */
    private void reqPostPraise() {
        TaskParams params = new TaskParams();

        String url = ConstantData
                .addLoginInfoToUrl(ConstantData.URL_PRAISE_POST);
        params.put(RequestTask.PARAM_URL, url);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_POST);

        ArrayList<NameValuePair> strNParams = new ArrayList<NameValuePair>();
        strNParams.add(new BasicNameValuePair("bid", gBook.getBookId()));
        strNParams.add(new BasicNameValuePair("src", gBook.getBookSrc()));
        strNParams.add(new BasicNameValuePair("sid", gBook.getSid()));
        if (mBookKey != null) {
            strNParams.add(new BasicNameValuePair(KEY, mBookKey));
        }

        RequestTask task = new RequestTask(new SimpleParser());
        task.bindActivity(this);
        task.setPostParams(strNParams);
        task.setTaskFinishListener(new ITaskFinishListener() {
            @Override
            public void onTaskFinished(TaskResult taskResult) {
                if (taskResult.stateCode == HttpStatus.SC_OK
                        && null != taskResult.retObj) {
                    if (ConstantData.CODE_SUCCESS.equals(String
                            .valueOf(taskResult.retObj))) {
                        gBook.setPraiseType("Y");

                        Intent intent = new Intent(
                                DownBookManager.ACTION_INTENT_RECOMMANDSTATE);
                        intent.putExtra("bookid", gBook.getBookId());
                        intent.putExtra("praise", true);
                        SinaBookApplication.gContext.sendBroadcast(intent);
                        return;
                    }
                }

                gBook.setPraiseType("N");
                reqPostPraise();
            }
        });
        task.execute(params);

        UserActionManager.getInstance().recordEvent(
                Constants.CLICK_READ_RECOMMEND);
    }

    /**
     * 请求章节价格
     */
    private void reqChapterPrice() {
        String reqUrl = String.format(
                ConstantData.URL_GET_SINGLE_CHAPTER_PRICE, gBook.getBookId(),
                gBook.getSid(), gBook.getBookSrc(),
                mSelectedChapter.getGlobalId());
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);

        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

        RequestTask task = new RequestTask(new ChapterPriceParser());
        task.bindActivity(this);
        task.setTaskFinishListener(new ITaskFinishListener() {
            @Override
            public void onTaskFinished(TaskResult taskResult) {
                dismissProgress();
                if (null != taskResult
                        && taskResult.stateCode == HttpStatus.SC_OK) {
                    if (taskResult.retObj instanceof ChapterPriceResult) {
                        ChapterPriceResult result = (ChapterPriceResult) taskResult.retObj;
                        updateChapter(result.getChapter());
                        updateUserInfoRole(result.getUserInfoRole());

                        doPayProcess();
                    }
                } else {
                    // 判断一下网络连接情况
                    if (HttpUtil.isConnected(SinaBookApplication.gContext)) {
                        shortToast(R.string.get_price_failed);
                    } else {
                        shortToast(R.string.network_unconnected);
                    }
                }
            }
        });
        task.execute(params);
    }

    /**
     * 请求书籍价格
     */
    private void reqBookPrice() {
        reqBookPrice(false);
    }

    /**
     * 请求书籍价格
     *
     * @param showDialog true:显示对话框 false:不显示对话框
     */
    private void reqBookPrice(boolean showDialog) {
        final boolean isShowDialog = showDialog;
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO_CHECK,
                gBook.getBookId(), gBook.getSid(), gBook.getBookSrc());
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        if (mBookKey != null) {
            reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
        }

        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

        RequestTask task = new RequestTask(new BookPriceParser());
        task.bindActivity(this);
        task.setTaskFinishListener(new ITaskFinishListener() {
            @Override
            public void onTaskFinished(TaskResult taskResult) {
                if (isShowDialog) {
                    dismissProgress();
                }

                if (null != taskResult
                        && taskResult.stateCode == HttpStatus.SC_OK) {
                    if (taskResult.retObj instanceof BookPriceResult) {
                        BookPriceResult result = (BookPriceResult) taskResult.retObj;
                        if (result != null) {
                            updateBookInfo(result.getBook());
                            updateUserInfoRole(result.getUserInfoRole());
                            updateBookTypeBtn();

                            if (isShowDialog) {
                                PriceTip tip = gBook.getBuyInfo().getPriceTip();
                                if (tip != null
                                        && tip.getBuyType() == PriceTip.TYPE_DISCOUNT_NINE) {
                                    isBookcard = true;
                                } else {
                                    isBookcard = false;
                                }
                                doPayProcess();
                            }
                        }
                    }
                } else {
                    if (isShowDialog) {
                        if (HttpUtil.isConnected(SinaBookApplication.gContext)) {
                            shortToast(R.string.get_price_failed);
                        } else {
                            shortToast(R.string.network_unconnected);
                        }
                    }
                }
            }
        });
        task.execute(params);
    }

    /**
     * 更新章节列表信息
     */
    private void updateChapter(Chapter chapter) {
        if (chapter != null) {
            if (mSelectedChapter.equals(chapter)) {
                mSelectedChapter.setPrice(chapter.getPrice());
                mSelectedChapter.setDiscountPrice(chapter.getDiscountPrice());
                mSelectedChapter.setHasBuy(chapter.hasBuy());
                mSelectedChapter.setPriceTip(chapter.getPriceTip());
            }
        }
    }

    /**
     * 更新书籍信息
     */
    private void updateBookInfo(Book book) {
        if (book != null) {
            gBook.setBookId(book.getBookId());
            gBook.getBuyInfo().setPayType(book.getBuyInfo().getPayType());
            gBook.getBuyInfo().setPrice(book.getBuyInfo().getPrice());
            gBook.getBuyInfo().setDiscountPrice(
                    book.getBuyInfo().getDiscountPrice());
            gBook.getBuyInfo().setHasBuy(book.getBuyInfo().isHasBuy());
            gBook.getBuyInfo().setPriceTip(book.getBuyInfo().getPriceTip());
        }
    }

    /**
     * 更新账号信息
     */
    private void updateUserInfoRole(UserInfoRole role) {
        if (role != null) {
            mRole = role;
        }
    }

    private boolean isDownChapterAfterLoginThenNeedToReloadCacheData = false;

    /**
     * 执行购买流程
     */
    private void doPayProcess() {
        int payType = gBook.getBuyInfo().getPayType();
        gBook.setAutoDownBook(false);
        if (mSelectedChapter != null) {
            mSelectedChapter.setState(Chapter.CHAPTER_PREPARE);
        }

        switch (payType) {
            case BOOK_TYPE_CHAPTER_VIP: // 章节收费
                if (mSelectedChapter.hasBuy()) {
                    // 已经购买过了
                    mIsJump = true;
                    downloadChapter(mSelectedChapter, true, READ_PAY_BOOK);
                } else {
                    final PayDialog payDialog = new PayDialog(this, gBook,
                            mSelectedChapter, mRole);
                    payDialog
                            .setOnPayFinishListener(new PayDialog.PayFinishListener() {
                                @Override
                                public void onFinish(int code) {
                                    if (code == PayDialog.CODE_SUCCESS) {
                                        // gBook.parsePosFromJson();
                                        // 解决：BugID=21452 陈建立
                                        // 如果不是预加载，则需要将跳转参数设置一下
                                        mIsJump = true;
                                        // DBService.getAllBookMark(gBook);
                                        // CJL BugID=21492
                                        if (isDownChapterAfterLoginThenNeedToReloadCacheData) {
                                            // 在下载章节之前读取书架上的数据
                                            // 如果书架上有这本书籍，则进行转移
                                            boolean isNeedToDownloadChapter = true;
                                            Book shelfBook = DownBookManager.getInstance().getBook(gBook);
                                            if (shelfBook != null) {
                                                // gBook = shelfBook;

                                                /**
                                                 * 解决bug：
                                                 * 1、A账号登陆，进入某本书的书籍详情页，点击“加入书架”后，再注销账号A；
                                                 * 2、未登录状态，从书城进入该书的详情页，在线试读，跳转收费章节，登录-付费-下载。
                                                 * 产生结果：
                                                 * 进度条不能滑动，显示当前作品只有一章，且底部一直显示解析中，目录页章节处显示null。
                                                 *
                                                 * 原因：
                                                 * 在线阅读的gbook对象和书架中shlefbook对象数据不吻合导致。
                                                 * 1、从书籍详情页添加书架时，shlefbook缺失了chapter列表，并且filePath对应.tmp路径，
                                                 * 2、由于是详情页加书架，书架上展示的是在线图标，.tmp文件因为没有产生过下载，实际不存在。
                                                 * 3、未登录的在线阅读时，gBook的filepath对应.oltmp路径，实际文件存储在线阅读过的章节数据。
                                                 * 4、登录后，gbook被简单的替换成了shelfbook，造成数据丢失，引发bug。
                                                 *
                                                 * 修改操作：
                                                 * 1、发现shelfbook的chapter列表不存在，则采用gbook的chapter列表；
                                                 * 2、发现gbook是在线阅读状态 &&  shelfbook的tmp文件不存在，使用gbook的filepath
                                                 */
                                                String gFilePath = gBook.getDownloadInfo().getFilePath();
                                                String sFilePath = shelfBook.getDownloadInfo().getFilePath();

                                                //修改操作 1
                                                ArrayList<Chapter> sChapters = shelfBook.getChapters();
                                                if (sChapters == null || sChapters.size() == 0) {
                                                    shelfBook.setChapters(gBook.getChapters());
                                                }

                                                gBook.fetchBook(shelfBook);
                                                handleLocalBook();

                                                //修改操作 2
                                                File file = FileUtils.checkOrCreateFile(sFilePath, false);
                                                if (gBook.isOnlineBook() && (file == null || !file.exists())) {
                                                    gBook.getDownloadInfo().setFilePath(gFilePath);
                                                }

                                                updatePageFactory();
                                                // 获取章节
                                                if (mSelectedChapter != null) {
                                                    int globalChapterId = mSelectedChapter
                                                            .getGlobalId();
                                                    Chapter chapter = gBook
                                                            .getCurrentChapterById(globalChapterId);
                                                    if (mSelectedChapter.hasBuy()) {
                                                        chapter.setHasBuy(true);
                                                    }
                                                    if (chapter != null) {
                                                        if (chapter.getLength() > 0) {
                                                            updateReadingView();
                                                            mPageFactory
                                                                    .seek((int) (chapter
                                                                            .getStartPos()));
                                                        } else {
                                                            mIsJump = true;
                                                            mJumpOffset = 0;
                                                            judgeChapterStateAndLoad(chapter);
                                                        }
                                                        isNeedToDownloadChapter = false;
                                                    }
                                                }
                                            }
                                            isDownChapterAfterLoginThenNeedToReloadCacheData = false;
                                            if (isNeedToDownloadChapter) {
                                                downloadChapter(mSelectedChapter,
                                                        true, READ_PAY_BOOK);
                                            }
                                        } else {
                                            downloadChapter(mSelectedChapter, true,
                                                    READ_PAY_BOOK);
                                        }

                                    }
                                }
                            });

                    payDialog
                            .setOnPayLoginSuccessListener(new PayDialog.PayLoginSuccessListener() {
                                public void onLoginSuccess() {
                                    // CJL BugID=21492
                                    isDownChapterAfterLoginThenNeedToReloadCacheData = true;
                                    payDialog.show();
                                }
                            });
                    payDialog.show();
                }
                break;

            case BOOK_TYPE_VIP: // 整本收费
                if (gBook.getBuyInfo().isHasBuy()) {
                    // mIsJump = true;
                    if (gBook.isForbidden()) {
                        // 下架书籍不让整本下载,单章在线阅读
                        mSelectedChapter.setHasBuy(true);
                        downloadChapter(mSelectedChapter, true, READ_PAY_BOOK);
                    } else {
                        boolean showToast = !DownBookManager.getInstance().hasBook(
                                gBook);
                        CloudSyncUtil.getInstance().add2Cloud(mContext, gBook);
                        DownBookManager.getInstance().downBook(gBook, showToast);

                        mPageFactory.prepareRefreshBookFile();
                        showProgress(R.string.downloading_text, false,
                                mDialogKeyListener);
                    }
                } else {
                    PayDialog payDialog = new PayDialog(this, gBook,
                            mSelectedChapter, mRole);

                    payDialog
                            .setOnPayLoginSuccessListener(new PayDialog.PayLoginSuccessListener() {

                                @Override
                                public void onLoginSuccess() {
                                    isDownChapterAfterLoginThenNeedToReloadCacheData = true;

                                    reqBookInfo();
                                    reqBookPrice(true);
                                }
                            });

                    payDialog
                            .setOnPayFinishListener(new PayDialog.PayFinishListener() {

                                @Override
                                public void onFinish(int code) {
                                    if (PayDialog.CODE_SUCCESS == code) {
                                        // mIsJump = true;
                                        mPageFactory.prepareRefreshBookFile();
                                        showProgress(R.string.downloading_text,
                                                false, mDialogKeyListener);

                                        // TODO:兑书券订购成功，弹框提示
                                        if (isBookcard) {
                                            isBookcard = false;
                                            LayoutInflater inflater = LayoutInflater
                                                    .from(mContext);
                                            View layout = inflater.inflate(
                                                    R.layout.act_bookcard, null);
                                            Toast toast = new Toast(mContext);
                                            toast.setGravity(Gravity.TOP, 0, 0);
                                            toast.setDuration(Toast.LENGTH_LONG);
                                            toast.setView(layout);
                                            toast.show();
                                        }

                                        // PriceTip tip =
                                        // gBook.getBuyInfo().getPriceTip();
                                        // if (tip != null && tip.getBuyType() ==
                                        // PriceTip.TYPE_DISCOUNT_NINE) {
                                        // LayoutInflater inflater =
                                        // LayoutInflater.from(mContext);
                                        // View layout =
                                        // inflater.inflate(R.layout.act_bookcard,
                                        // null);
                                        // Toast toast = new Toast(mContext);
                                        // toast.setGravity(Gravity.TOP, 0, 0);
                                        // toast.setDuration(Toast.LENGTH_LONG);
                                        // toast.setView(layout);
                                        // toast.show();
                                        // }
                                    }
                                }
                            });
                    payDialog.show();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 更新章节状态
     *
     * @param chapterId the chapter id
     * @param state     the state
     */
    private void updateChapterState(int chapterId, int state) {
        if (gBook != null && gBook.getChapters() != null) {
            for (int i = 0; i < gBook.getChapters().size(); i++) {
                if (gBook.getChapters().get(i).getGlobalId() == chapterId) {
                    final Chapter chapter = gBook.getChapters().get(i);
                    chapter.setState(state);

                    // VIP章节需要在以下状态设置chapter为未购买状态
                    if (chapter.isVip()
                            && (state == CHAPTER_PREPARE
                            || state == CHAPTER_FAILED || state == CHAPTER_NEED_BUY)) {
                        chapter.setHasBuy(false);
                    }

                    if (mIsJump) {
                        mJumpOffset = (int) gBook.getChapters().get(i)
                                .getStartPos();
                        LogUtil.d(TAG, "ReadActivity >> updateChapterState >> mJumpOffset = " + mJumpOffset);

                        if (mJumpHistoryOffset > 0) {
                            mJumpOffset += mJumpHistoryOffset;
                            mJumpHistoryOffset = 0;
                        }
                    }
                    LogUtil.d(TAG, chapter.toString() + " \nOffset : " + mJumpOffset);

                    break;
                }
            }
        }
    }

    /**
     * 章节内容下载成功后更新数据.
     */
    private void updateData() {
        LogUtil.i("ouyang", "ReadActivity >> updateData mIsJump: " + mIsJump);
        LogUtil.i("ouyang", "ReadActivity >> updateData mJumpOffset: " + mJumpOffset);
        try {
            mPageFactory.refreshBookFile();

            if (mIsJump && mJumpOffset != 0) {
                mPageFactory.seek(mJumpOffset);
                mIsJump = false;
                mJumpOffset = 0;
            }

            invalidateUI(true);
        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

    /**
     * 更新书籍信息
     */
    private void updateBookData(Book newbook, int chapterId) {
        LogUtil.d(TAG, "ReadActivity >> updateBookData >> chapterId = " + chapterId);
        try {
            ArrayList<Chapter> chapters = DBService.getAllChapter(gBook);
            if (null != chapters && !chapters.isEmpty()) {
                gBook.setChapters(chapters, "[ReadActivity-updateBookData]");
            }

            mPageFactory.refreshBookFile(newbook);
            if (chapterId > 0) {
                if (mIsJump && mJumpOffset != 0) {
                    mPageFactory.seek(mJumpOffset);
                    mIsJump = false;
                    mJumpOffset = 0;

                    LogUtil.d(TAG, "ReadActivity >> updateBookData >> mJumpOffset = " + mJumpOffset);
                }
            }

            if (mReadCompleteView.isShowing()) {
                dismissCompleteView();
            }

        } catch (IOException e) {
            LogUtil.e(TAG, e.getMessage());
        }

        invalidateUI(true);
    }

    /**
     * 更新赞的状态，保证显示已赞
     */
    private void updatePraiseBtnStatus() {
        shortToast(R.string.parise_success);
        gBook.setPraiseType("Y");
        mReadCompleteView.updatePraiseBtnStatus();
        mToolbar.updatePraiseStatus();
    }

    /**
     * 请求章节内容
     */
    private void reqChapterContent() {
        // 在线阅读
        if (gBook.isOnlineBook()) {
            // 如果是有章节记录的，肯定是买过的
            int gloableId = 0;
            if (isLocalBookMissing) {
                String json = gBook.getReadInfo().getLastReadJsonString();
                JSONObject lastReadObj = null;
                try {
                    lastReadObj = new JSONObject(json);
                    gloableId = lastReadObj.optInt("lastGlobalChapterId");
                    if (gloableId <= 0) {
                        if (gBook.getChapters() != null
                                && gBook.getChapters().size() > 0) {
                            gloableId = gBook.getChapters().get(0)
                                    .getGlobalId();

                            BookReadInfo readInfo = new BookReadInfo();
                            gBook.setReadInfo(readInfo);
                        }
                    }
                    downloadChapterContent(gloableId, true, READ_ONLINE_BOOK);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (gBook
                    .getOnlineReadChapterId("ReadActivity-reqChapterContent-1") > 0) {
                downloadChapterContent(
                        gBook.getOnlineReadChapterId("ReadActivity-reqChapterContent-2"),
                        true, READ_ONLINE_BOOK);
                return;
            }
        }

        int lastChapterIdx = gBook.getCurrentChapterIndex(gBook.getReadInfo()
                .getLastPos());
        if (lastChapterIdx < 0) {
            lastChapterIdx = 0;
        }
        downloadChapterContent(gBook.getChapters().get(lastChapterIdx)
                .getGlobalId(), false, READ_ONLINE_BOOK);
    }

    /**
     * 预取VIP章节情况，章节的购买信息
     */
    private void reqChapterBuyInfo(Chapter nowChapter) {
        if (nowChapter == null
                || mPreGetChapterTask != null
                || gBook.getChapters() == null
                || gBook.getBuyInfo().getPayType() != BOOK_TYPE_CHAPTER_VIP
                || LoginUtil.isValidAccessToken(this) != LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
            return;
        }
        boolean preLoadData = false;
        int chapterPage = (nowChapter.getChapterId() - 1) / CHAPTER_PRE_GET + 1;
        final ArrayList<Chapter> bookChapters = gBook.getChapters();

        // 判断是否需要预取数据,算出预取的chapterPage
        if (!nowChapter.isPreLoad()) {
            preLoadData = true;
        } else if (nowChapter.getChapterId() % CHAPTER_PRE_GET > 0) {
            preLoadData = false;
        } else {
            // 虽然已预取，但到达了预取的末尾条，取下一页
            if (nowChapter.getChapterId() == CHAPTER_PRE_GET * chapterPage) {
                int nextid = nowChapter.getChapterId() + 1;
                if (bookChapters.size() > nextid - 1 && nextid > 1
                        && !bookChapters.get(nextid - 1).isPreLoad()) {
                    preLoadData = true;
                    chapterPage = chapterPage + 1;
                }
                // 虽然已预取，但却是预取的开始条，取上一页
            } else if (nowChapter.getChapterId() == CHAPTER_PRE_GET
                    * (chapterPage - 1)) {
                int preid = nowChapter.getChapterId() - 1;
                if (bookChapters.size() > preid - 1 && preid > 1
                        && !bookChapters.get(preid - 1).isPreLoad()) {
                    preLoadData = true;
                    chapterPage = chapterPage - 1;
                }
            }
        }

        // 判断预取page的状态，确定是否预取
        if (preLoadData) {
            int chapterLast = CHAPTER_PRE_GET * chapterPage - 1;
            if (bookChapters.size() > chapterLast) {
                if (getChapterStates(bookChapters.get(chapterLast)) != CHAPTER_VIP) {
                    preLoadData = false;
                }
            }
        }

        if (preLoadData) {
            String reqUrl = String.format(URL_GET_CHAPTERS_BY_PAGE,
                    gBook.getBookId(), gBook.getSid(), gBook.getBookSrc(),
                    chapterPage, CHAPTER_PRE_GET);
            reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
            if (mBookKey != null) {
                reqUrl = HttpUtil.setURLParams(reqUrl, KEY, mBookKey);
            }

            ChapterListParser chaptersParser = new ChapterListParser();
            if (gBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
                chaptersParser.setAllBookHasBuy(gBook.getBuyInfo().isHasBuy());
            }
            mPreGetChapterTask = new RequestTask(chaptersParser);
            mPreGetChapterTask.setExtra(chapterPage);
            mPreGetChapterTask.setTaskFinishListener(new ITaskFinishListener() {

                @Override
                public void onTaskFinished(TaskResult taskResult) {

                    if (taskResult != null
                            && taskResult.stateCode == HttpStatus.SC_OK) {
                        if (taskResult.retObj instanceof ChapterList) {

                            ChapterList result = (ChapterList) taskResult.retObj;
                            ArrayList<Chapter> retChapters = result
                                    .getChapters();
                            if (retChapters != null) {
                                int reqPage = (Integer) mPreGetChapterTask
                                        .getExtra();
                                for (Chapter retChapter : retChapters) {
                                    for (int i = (reqPage - 1)
                                            * CHAPTER_PRE_GET; i < bookChapters
                                            .size(); i++) {
                                        Chapter bookChapter = bookChapters
                                                .get(i);
                                        if (bookChapter.equals(retChapter)) {
                                            bookChapter.setPreLoad(true);
                                            if (retChapter.hasBuy()) {
                                                bookChapter.setHasBuy(true);
                                                LogUtil.d(
                                                        TAG,
                                                        bookChapter
                                                                .getChapterId()
                                                                + " "
                                                                + "chapter set hasbuy");
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mPreGetChapterTask = null;
                }
            });
            TaskParams params = new TaskParams();
            params.put(RequestTask.PARAM_URL, reqUrl);
            params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
            mPreGetChapterTask.bindActivity(this);
            mPreGetChapterTask.execute(params);
        }
    }

    @Override
    public void readModeChanged(int readMode) {
        updateViews();
        invalidateUI(false);
    }

    @Override
    public void animModeChanged(int animMode) {
        mPageFactory.changeReadAnim();
        invalidateUI(true);
    }

    @Override
    public void fontSizeChanged(float fontSize) {
        mPageFactory.updateFontSize();
        invalidateUI(true);

        shortToast("当前字体：" + fontSize + "号");
    }

    @Override
    public void brightModeChanged(boolean isAuto) {
        if (isAuto) {
            // 设置系统亮度自动调节
            BrightnessUtil.setScreenBrightnessMode(this,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            StorageUtil.saveBoolean(StorageUtil.KEY_AUTO_BRIGHTNESS, true);

            // 设置为默认值
            BrightnessUtil.setCurrentScreenDefault(this);
        } else {
            // 设置系统亮度手动调节
            BrightnessUtil.setScreenBrightnessMode(this,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            StorageUtil.saveBoolean(StorageUtil.KEY_AUTO_BRIGHTNESS, false);

            BrightnessUtil.setCurrentScreenBrightness(this,
                    StorageUtil.getBrightness());
        }
    }

    @Override
    public void brightValueChanged(int brightValue) {
        // 仅调节当前屏幕
        BrightnessUtil.setCurrentScreenBrightness(this, brightValue);
        // 设置整个系统
        // SystemUtil.setScreenBrightnessValue(getApplicationContext(),
        // brightValue);
    }

    /**
     * 请求章节列表任务事件监听器
     */
    private class ChapterListFinishListener implements ITaskFinishListener {
        private Chapter mChapter;
        // 是否本地书籍补全章节
        private boolean mIsLocal;

        public ChapterListFinishListener(Chapter chapter, boolean isLocal) {
            mChapter = chapter;
            mIsLocal = isLocal;
        }

        @Override
        public void onTaskFinished(TaskResult taskResult) {
            RequestTask reqTask = (RequestTask) taskResult.task;
            if (null != taskResult && taskResult.stateCode == HttpStatus.SC_OK) {
                IParser parser = reqTask.getParser();
                String stateCode = CODE_FAIL_KEY + "";
                String msg = "请求失败";

                if (null != parser) {
                    stateCode = parser.getCode();
                    msg = parser.getMsg();
                }

                if (ConstantData.CODE_SUCCESS.equals(stateCode)
                        && taskResult.retObj != null) {
                    Object object = taskResult.retObj;

                    if (object instanceof ChapterList) {
                        ChapterList result = (ChapterList) object;
                        gBook.setChapters(result.getChapters(),
                                "[ReadActivity-ChapterListFinishListener-onTaskFinished]");
                        if (gBook.getNum() <= 1
                                && !Utils.isEmptyString(result.getChapterNum())) {
                            gBook.setNum(Integer.parseInt(result
                                    .getChapterNum()));
                        }

                        // 服务器存储的阅读记录-章节ID
                        if (gBook.isOnlineBook()) {
                            gBook.setOnlineReadChapterId(result.getCurCid(),
                                    "ReadActivity-ChapterListFinishListener-onTaskFinished");
                        }
                        LogUtil.i(TAG, "Chapters size : "
                                + gBook.getChapters().size());

                        if (mIsLocal) {
                            // 本地书籍
                            updatePageFactory();
                        } else {
                            if (gBook.getChapters() != null
                                    && !gBook.getChapters().isEmpty()) {
                                switch (getChapterStates(mChapter)) {
                                    case CHAPTER_VIP:
                                        reqChapterContent();
                                        downloadBookOrChapter(mChapter,
                                                READ_PAY_BOOK);
                                        break;

                                    case CHAPTER_FREE:
                                        downloadChapterContent(
                                                mChapter.getGlobalId(), true,
                                                READ_ONLINE_BOOK);
                                        break;

                                    default:
                                        reqChapterContent();
                                        break;
                                }
                            }
                        }

                    }
                } else {
                    if (!mIsLocal) {
                        mPageWidget.setTouchEnabled(false);
                        dismissProgress();
                        showErrorView();
                        shortToast(msg);
                    }
                }
            } else {
                dismissProgress();
                showErrorView();
            }
        }
    }

    /**
     * 发送微博任务事件监听器
     */
    private class PostWeiboFinishListener implements ITaskFinishListener {

        @Override
        public void onTaskFinished(TaskResult taskResult) {
            if (taskResult.stateCode == HttpStatus.SC_OK) {
                IParser parser = ((RequestTask) taskResult.task).getParser();
                String stateCode = CODE_FAIL_KEY + "";
                String msg = "请求失败";

                if (null != parser) {
                    stateCode = parser.getCode();
                    String msgStr = parser.getMsg();

                    // 分享微博直接返回了微博API的信息，需要特殊处理（这里只过滤了分享失败的情况）
                    if (!TextUtils.isEmpty(msgStr)) {
                        boolean error = msgStr.contains("error")
                                || msgStr.contains("ERROR")
                                || msgStr.contains("error_code")
                                || msgStr.contains("ERROR_CODE");
                        if (error) {
                            msg = getString(R.string.share_weibo_failed);
                        } else {
                            msg = msgStr;
                        }
                    }
                }

                if (stateCode.equals(ConstantData.CODE_SUCCESS)
                        && taskResult.retObj != null) {
                    if (taskResult.retObj instanceof String) {
                        if (ConstantData.CODE_SUCCESS.equals(String
                                .valueOf(taskResult.retObj))) {
                            shortToast(R.string.share_weibo_success);
                        } else {
                            shortToast(R.string.share_weibo_failed);
                        }
                    }
                } else {
                    shortToast(msg);
                }
            }
        }
    }

    private boolean isNeedToCache = true;

    /**
     * 增加到书架对话框
     *
     * @author Tsimle
     */
    private class DialogListener extends CommonDialog.DefaultListener {

        @Override
        public void onRightClick(DialogInterface dialog) {
            dialog.dismiss();
            add2Shelves();
        }

        /*
         * 增加到书架
		 */
        private void add2Shelves() {
            if (BOOK_LOCAL == gBook.getDownloadInfo().getLocationType()
                    || DownBookManager.getInstance().hasBook(gBook)) {
                return;
            }

            int payType = gBook.getBuyInfo().getPayType();
            boolean needDownBook = false;
            if (payType == BOOK_TYPE_FREE
                    && BOOK_ONLINE == gBook.getDownloadInfo().getLocationType()) {
                // 如果书籍是全本免费且是在线书籍，直接下载
                needDownBook = true;
            } else if (payType == BOOK_TYPE_CHAPTER_VIP
                    && BOOK_ONLINE == gBook.getDownloadInfo().getLocationType()) {
                // 如果书籍是章节免费且是在线书籍，直接下载
                needDownBook = true;
                isClickAddShelf = true;
            } else if (payType == BOOK_TYPE_VIP) {
                // 修复：BugID=21228
                // 如果书籍是全本购买且是在线书籍，直接下载
                needDownBook = true;
                if (!gBook.getBuyInfo().isHasBuy()
                        || !PurchasedBookList.getInstance().isBuy(gBook)) {
                    gBook.setAutoDownBook(true);
                }
            }

            isNeedToCache = false;
            // 同步到云端
            CloudSyncUtil.getInstance().add2Cloud(mContext, gBook);
            if (needDownBook) {
                DownBookManager.getInstance().downBook(gBook);
                finishActivity();
            } else {
                DownBookManager.getInstance().downBookLocal(gBook,
                        new ITaskFinishListener() {

                            @Override
                            public void onTaskFinished(TaskResult taskResult) {
                                finishActivity();
                            }
                        });
            }
        }

        @Override
        public void onLeftClick(DialogInterface dialog) {
            // 不加入书架，确定删除书签数据库
            DBService.deleteAllBookMark(gBook);

            dialog.dismiss();
            finishActivity();
            deleteTempFile();
        }
    }

    /*
     * 下载为本地书籍
	 */
    private void downloadCloudBook() {
        // if (!isDatDismissAndNeedToDownAgain &&
        // (!DownBookManager.getInstance().hasBook(gBook) ||
        // !gBook.isOnlineBook())) {
        LogUtil.d("BugBugFuckU",
                "ReadActivity >> downloadCloudBook >> 1 >> gBook=" + gBook);
        if (!DownBookManager.getInstance().hasBook(gBook)
                || !gBook.isOnlineBook()) {
            LogUtil.d("BugBugFuckU",
                    "ReadActivity >> downloadCloudBook >> 2 >> return");
            return;
        }

        int payType = gBook.getBuyInfo().getPayType();
        boolean needDownBook = false;
        if (payType == BOOK_TYPE_FREE) {
            needDownBook = true;
        } else if (payType == BOOK_TYPE_CHAPTER_VIP) {
            needDownBook = true;

            // 已加入书架书籍，在线阅读退出时，需要自动下载书籍。判断boolean为true，不删除oltmp文件
            isClickAddShelf = true;
        } else if (payType == BOOK_TYPE_VIP) {
            // 修复：BugID=21228
            // 如果书籍是全本购买且是在线书籍，直接下载
            needDownBook = true;
            if (!gBook.getBuyInfo().isHasBuy()) {
                gBook.setAutoDownBook(true);
            }
        }

        // 这里的逻辑和书籍长按该全本收费的书籍时，弹出对话框，点击右下角的“下载”为啥不一致处理 ?
        // else if (payType == BOOK_TYPE_VIP
        // && (gBook.getBuyInfo().isHasBuy() ||
        // PurchasedBookList.getInstance().isBuy(gBook))) {
        // needDownBook = true;
        // }

        // 同步到云端
        CloudSyncUtil.getInstance().add2Cloud(mContext, gBook);
        if (needDownBook) {
            // 直接下载
            // if (isDatDismissAndNeedToDownAgain) {
            // // gBook.setOnlineBook(true);
            // Book book = DownBookManager.getInstance().getBook(gBook);
            // if (book != null) {
            // book.setOnlineBook(true);
            // }
            // }
            DownBookManager.getInstance().downBook(gBook);
            finishActivity();
        } else {
            // 其它仅仅加入书架即可
            DownBookManager.getInstance().downBookLocal(gBook,
                    new ITaskFinishListener() {

                        @Override
                        public void onTaskFinished(TaskResult taskResult) {
                            finishActivity();
                        }
                    });
        }
    }

    /**
     * 删除缓存文件
     */
    private void deleteTempFile() {
        String path = gBook.getDownloadInfo().getFilePath();
        if (null != path
                && path.contains(StorageUtil
                .getDirByType(StorageUtil.DIR_TYPE_BOOK))) {
            // 修复：BugID=21316
            // 微盘的书籍文件丢失时进入阅读，以下两个条件成立
            boolean isOnlineTempFile = path.endsWith(Book.ONLINE_TMP_SUFFIX);
            if (gBook.isVDiskBook() && isOnlineTempFile) {
                return;
            }
            int dotIndex = path.lastIndexOf(".");
            if (dotIndex > 0) {
                String oltmpPath = path.substring(0, dotIndex)
                        + Book.ONLINE_TMP_SUFFIX;
                File bookOlFile = new File(oltmpPath);
                if (bookOlFile.exists()) {
                    bookOlFile.delete();
                }
            }
        }
    }

    /**
     * 显示是否加入书架的提示框.
     */
    private void showIsAddToBookshelf() {
        if (gBook.isForbidden() || disableAddShelf) {
            finishActivity();
            return;
        }

        //wuzp  本书(按章节)已经购买,且用户选择的是一键畅读方式:自动加入书架且下载到本地
        if (gBook.getBuyInfo().isAutoBuy() && mEnterHasAutoBuy && !DownBookManager.getInstance().hasBook(gBook)) {
            DialogListener mDialogListener = new DialogListener();
            CommonDialog mCommonDialog = new CommonDialog(this, null, null, mDialogListener);
            mDialogListener.onRightClick(mCommonDialog);
            mDialogListener = null;
            mCommonDialog = null;
            finishActivity();
            return;
        }

		/*
         * 阅读页点击右上进入摘要页，在摘要页下载该书，再返回到阅读页，2个book对象是不相同的,
		 * 为保证书签不丢失，将阅读页的书签赋给下载任务book。
		 */
        // Book book = DownBookManager.getInstance().getBook(gBook);
        // if (book != null && book != gBook) {
        // List<MarkItem> mark = gBook.getBookmarks();
        // List<BookSummary> bookSummary = gBook.getBookSummaries();
        // book.setBookmarks(mark);
        // book.setBookSummaries(bookSummary);
        // // 也要保存到数据库
        // DBService.saveBookRelateInfo(book);
        // book.parsePosFromJson();
        // }

        // //包月书籍 & 未付费 & 没有消费记录
        // // 不谈加入书架提醒，直接退出
        // int payType = gBook.getBuyInfo().getPayType();
        // if(payType == BOOK_TYPE_VIP
        // && (!gBook.getBuyInfo().isHasBuy() ||
        // !PurchasedBookList.getInstance().isBuy(gBook))){
        // finishActivity();
        // return;
        // }

        if (BOOK_LOCAL != gBook.getDownloadInfo().getLocationType()
                && !DownBookManager.getInstance().hasBook(gBook)) {
            CharSequence tip;
            if (gBook.isLocalImportBook()) {
                tip = String.format(getString(R.string.import_tip),
                        gBook.getTitle());
            } else {
                tip = Util.str2RedStr(String.format(getString(R.string.add_shelves_tip1),
                        gBook.getTitle()), String.format(getString(R.string.add_shelves_tip2),
                        gBook.getTitle()), "");
            }

            // 在线阅读书籍提示加入书架
            CommonDialog.show(this, null, tip,
                    getString(R.string.add_shelves_cancle),
                    getString(R.string.add_shelves_ok), new DialogListener());

        } else if ((DownBookManager.getInstance().hasBook(gBook) && !gBook
                .isDownloadDialogNotPop())) {
            // } else if ((DownBookManager.getInstance().hasBook(gBook) &&
            // gBook.isOnlineBook() && !gBook
            // .isDownloadDialogNotPop())) {
            Book mbook = DownBookManager.getInstance().getBook(gBook);
            if (mbook != null && mbook.isOnlineBook()) {
                gBook.setOnlineBook(true);
            }

            if (gBook.isOnlineBook()) {
                // 云端书籍直接下载
                downloadCloudBook();
            } else {
                finishActivity();
            }
            // // 云端书籍直接下载
            // downloadCloudBook();
        } else {
            finishActivity();
        }
    }

    private void updateToolbar(Chapter chapter) {
        updateToolbar(chapter, false);
    }

    private void updateToolbar(Chapter chapter, boolean ignoreSetProgress) {
        ArrayList<Chapter> chapters = gBook.getChapters();
        int chapSize = chapters.size();

        if (null != chapter && chapSize > 0) {
            mIsProgressChanged = false;

            String text = String.format(
                    getString(R.string.chapter_information),
                    chapter.getChapterId(), chapSize);
            mToolbar.setChapterTitle(text, "3");

            // int progress = (int) (chapter.getChapterId() * 100F /
            // chapters.size());
            // mToolbar.setSeekBarProgress(progress, ignoreSetProgress,
            // chapSize);
            mToolbar.setSeekBarProgress(chapter.getChapterId(),
                    ignoreSetProgress, chapSize);
        }

        mToolbar.invalidate();
    }

    // 标识章节阅读行为入口
    private static String mChapterReadEntrance;

    public static void setChapterReadEntrance(String chapterReadEntrance) {
        mChapterReadEntrance = chapterReadEntrance;
    }

    public void record1ChapterReadCount() {
        if (gBook == null) {
            return;
        }

        // 判断是在线书籍还是本地书籍
        String eventType = null;
        String bookId = null;
        String chapterId = null;

        if (gBook.isOurServerBook()) {
            if (gBook.isOnlineBook()) {
                eventType = "online";
            } else {
                eventType = "local";
            }

            bookId = gBook.getBookId();
            Chapter curChapter = mPageFactory.getCurrentChapter();
            if (curChapter != null) {
                // 有章节信息取章节ID
                chapterId = String.valueOf(curChapter.getGlobalId());
            } else {
                // 无法获取到当前阅读的百分比进度值
                chapterId = String.valueOf(gBook.getReadInfo()
                        .getLastReadPercent());
            }
        } else {
            // 本地导入/微盘
            eventType = "import";
            bookId = gBook.getTitle();
            if (gBook.isSingleChapterBook()) {
                // 单章节取百分比进度值
                chapterId = String.valueOf(gBook.getReadInfo()
                        .getLastReadPercent());
            } else {
                Chapter curChapter = mPageFactory.getCurrentChapter();
                if (curChapter != null) {
                    // 有章节信息取章节ID
                    chapterId = String.valueOf(curChapter.getChapterId());
                } else {
                    // 无法获取到当前阅读的百分比进度值
                    chapterId = String.valueOf(gBook.getReadInfo()
                            .getLastReadPercent());
                }
            }
        }

        String eventExtra = mChapterReadEntrance;
        ReadCountManager.getInstance(ReadActivity.this).addNewCount(bookId,
                chapterId, eventType, eventExtra);
    }

    /**
     * 阅读进度拖动变化监听类.
     *
     * @author MarkMjw
     */
    public class ReadSeekBarListener implements OnSeekBarChangeListener {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            disToolBar();
            int progress = seekBar.getProgress();
            float percent = progress / 100F;

            // 单章节书籍跳转取百分比进度值进行跳转
            if (gBook.isSingleChapterBook()) {
                mPageFactory.seek(percent);

                invalidateUI(true);
                mToolbar.invalidate();

                return;
            }

            ArrayList<Chapter> chapters = gBook.getChapters();
            int chapSize = chapters.size();
            Chapter preChapter = mPageFactory.getCurrentChapter();
            Chapter curChapter = preChapter;
            if (chapSize > 0) {
                // int index = (int) (chapSize * percent);
                if (progress >= chapSize) {
                    progress = chapSize - 1;
                }
                curChapter = chapters.get(progress);
            }

            ReadActivity.setChapterReadEntrance("阅读页-SeekBar");
            if (null != curChapter) {
                // 下载之前回归跳转进度
                updateToolbar(preChapter, true);
                if (curChapter.getLength() > 0) {
                    mPageFactory.seek((int) (curChapter.getStartPos()));
                } else {
                    //
                    mIsJump = true;
                    mJumpOffset = 0;
                    judgeChapterStateAndLoad(curChapter);
                }
            }
            // ***** 类似点击工具栏的“上一章”，“下一章”进行页面跳转 *****
            // CJL 解决那种通过点击工具栏的“上一章”，“下一章”进行页面跳转
            // 并且用户没有点击屏幕进行翻页行为，导致从未调用onPageTurned方法，
            // 因此mSelectedChapter未被赋值，此时点击顶部工具栏进入书籍详情页
            // 下载书籍，然后在未下载完成时点击在线试读再次进入该书籍，页面不动，
            // 等待下载完成，此时调用onUpdate
            // 在进行
            // Chapter chapter = null;
            // if (mSelectedChapter != null) {
            // chapter = mSelectedChapter;
            // } else {
            // chapter = mPageFactory.getCurrentChapter();
            // }
            // 代码判断时，因为mSelectedChapter==null。获取到的mSelectedChapter实际上
            // 是不对的。导致页面在下载完成时，出现跳章节的情况。
            mSelectedChapter = curChapter;

            invalidateUI(true);
            mToolbar.invalidate();

            UserActionManager.getInstance().recordEvent(
                    Constants.ACTION_DRAG_PROGRESS);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            float percent = progress / 100F;

            ArrayList<Chapter> chapters = gBook.getChapters();
            int chapSize = chapters.size();
            Chapter curChapter = mPageFactory.getCurrentChapter();
            if (chapSize > 0) {
                // int index = (int) (chapSize * percent);
                if (progress >= chapSize) {
                    progress = chapSize - 1;
                }
                if (mIsProgressChanged) {
                    curChapter = chapters.get(progress);
                }
            }

            if (null != curChapter && mIsProgressChanged) {
                mToolbar.setSeekInfo(curChapter.getTitle());

                String text = String.format(
                        getString(R.string.chapter_information),
                        curChapter.getChapterId(), chapSize);
                mToolbar.setChapterTitle(text, "1");
            }

            mIsProgressChanged = true;

            mToolbar.invalidate();
        }
    }

    /**
     * 阅读界面监听器
     *
     * @author Tsimle
     */
    private class PageListener implements PageWidgetListener {

        @Override
        public void onPrePage() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onPrePage");
            // 预先下载上一章节的内容
            Chapter curChapter = mPageFactory.getCurrentChapter();
            if (curChapter != null) {
                // 将标志置为正常
                curChapter.setTag(Chapter.NORMAL);
            }
            Chapter preChapter = gBook.getPreChapter(curChapter);
            if (null != preChapter) {
                reqChapterBuyInfo(preChapter);

                int chapterState = getChapterStates(preChapter);
                // 如果是免费章节，或已购买，或自动购买
                if (CHAPTER_FREE == chapterState || preChapter.hasBuy()
                        || gBook.getBuyInfo().canAutoBuy(preChapter)) {
                    downloadChapter(preChapter, false, READ_LOCAL_BOOK, true);
                }
            }
        }

        @Override
        public void onNextPage() {
            LogUtil.i("ReadChapterCount",
                    "ReadActivity >> PageWidgetListener >> onNextPage");
            // 预先下载下一章节的内容
            Chapter curChapter = mPageFactory.getCurrentChapter();
            if (curChapter != null) {
                // 将标志置为正常
                curChapter.setTag(Chapter.NORMAL);
            }

            Chapter nextChapter = gBook.getNextChapter(curChapter);
            if (null != nextChapter) {
                reqChapterBuyInfo(nextChapter);

                int chapterState = getChapterStates(nextChapter);
                // 如果是免费章节，或已购买，或自动购买
                if (CHAPTER_FREE == chapterState || nextChapter.hasBuy()
                        || gBook.getBuyInfo().canAutoBuy(nextChapter)) {
                    ReadActivity.setChapterReadEntrance("阅读页-自动翻页");
                    downloadChapter(nextChapter, false, READ_LOCAL_BOOK, true);
                }
            }
        }

        @Override
        public boolean isToolBarVisible() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> isToolBarVisible");
            return mToolbar.isShown();
        }

        @Override
        public void onToolBar() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onToolBar");
            updateBookmarkFlag();
            showOrDisToolBar();
        }

        @Override
        public void onPageTurned() {
            // ------------- 解决：BugID=21447 -------------
            // 翻页的时候判断当前是不是显示了VIP章节的“继续阅读”UI
            // 如果显示了，则不做mSelectedChapter的赋值处理
            if (mIsVIPChapter && mReadCompleteView.isShowing()) {

            } else {
                Chapter curChapter = mPageFactory.getCurrentChapter();
                mSelectedChapter = curChapter;

                // LogUtil.i("ReadChapterCount",
                // "ReadActivity >> PageWidgetListener >> onPageTurned >> {curChapter="
                // + curChapter + "}");

                // float progress = 0;

                // Chapter curChapter = mPageFactory.getCurrentChapter();
                ArrayList<Chapter> chapters = gBook.getChapters();
                //
                // if (gBook.isSingleChapterBook()) {
                // progress = mPageFactory.getCurrentPercent();
                // } else {
                // if (null != curChapter && chapters.size() > 0) {
                // progress = curChapter.getChapterId() * 100F /
                // chapters.size();
                // }
                // }
                // mToolbar.setSeekBarProgress((int) progress);

                // 空指针
                if (null != curChapter) {
                    mToolbar.setSeekBarProgress(curChapter.getChapterId(),
                            false, chapters.size());
                }

                updateBookmarkFlag();
            }
        }

        @Override
        public void onFirstOrLastPage(boolean isFirstPage) {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onFirstOrLastPage");
            Chapter curChapter = mPageFactory.getCurrentChapter();
            if (isFirstPage) {
                Chapter preChapter = gBook.getPreChapter(curChapter);
                switch (getChapterStates(preChapter)) {
                    case CHAPTER_VIP:// 前一章收费
                        downloadBookOrChapter(preChapter, READ_LOCAL_BOOK);
                        break;
                    case CHAPTER_FREE: // 前一章免费
                        mAutoFlip = FLIP_PRE_PAGE;
                        downloadChapter(preChapter, true, READ_LOCAL_BOOK);
                        break;
                    case CHAPTER_NO:// 没有前一章了
                        shortToast(R.string.first_page_alert);
                        break;
                    default:
                        shortToast(R.string.first_page_alert);
                        break;
                }
            } else {
                Chapter nextChapter = gBook.getNextChapter(curChapter);
                switch (getChapterStates(nextChapter)) {
                    case CHAPTER_VIP:// 下一章收费
                        downloadBookOrChapter(nextChapter, READ_LOCAL_BOOK);
                        break;
                    case CHAPTER_FREE: // 下一章免费
                        mAutoFlip = FLIP_NEXT_PAGE;
                        downloadChapter(nextChapter, true, READ_LOCAL_BOOK);
                        break;
                    case CHAPTER_NO:// 没有下一章了
                        if (gBook.isOurServerBook()) {
                            showCompleteView(ReadCompleteView.TYPE_ALL_BOOK,
                                    gBook.getTitle());
                        } else {
                            shortToast(R.string.last_page_alert);
                        }

                        break;
                    default:
                        shortToast(R.string.last_page_alert);
                        break;
                }
            }
        }

        @Override
        public void onPullStart() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onPullStart");
            mPullDownLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPullStateDown() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onPullStateDown");
            mCurMark = mPageFactory.getCurBookMark();

            if (mCurMark != null) {
                mBookmarkFlag.setVisibility(View.VISIBLE);
                mPullDownText.setText(R.string.pulldown_tv_del_ready);
            } else {
                mBookmarkFlag.setVisibility(View.GONE);
                mPullDownText.setText(R.string.pulldown_tv_add_ready);
            }

            rotateArrowUp();
        }

        @Override
        public void onPullStateUp() {
            mCurMark = mPageFactory.getCurBookMark();

            if (mCurMark != null) {
                mBookmarkFlag.setVisibility(View.VISIBLE);
                mPullDownText.setText(R.string.pulldown_tv_del_normal);
            } else {
                mBookmarkFlag.setVisibility(View.GONE);
                mPullDownText.setText(R.string.pulldown_tv_add_normal);
            }

            rotateArrowDown();
        }

        @Override
        public void onPullDown() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onPullDown");
            addOrDelMark();
        }

        @Override
        public void onPullEnd() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onPullEnd");
            mPullDownLayout.setVisibility(View.GONE);
        }

        @Override
        public boolean isBookSummaryPopVisible() {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> isBookSummaryPopVisible");
            return null != mSummaryPopControl && mSummaryPopControl.isShowing();
        }

        @Override
        public void onBookSummaryPop(BookSummaryPostion bookSummaryPostion) {
            // LogUtil.i("ReadChapterCount",
            // "ReadActivity >> PageWidgetListener >> onBookSummaryPop");
            if (null != mSummaryPopControl) {
                if (mSummaryPopControl.isShowing()) {
                    mSummaryPopControl.dismiss();
                } else {
                    mSummaryPopControl.show(bookSummaryPostion);
                }
            }
        }
    }

    /**
     * 文字选择器
     *
     * @author MarkMjw
     */
    private class SelectorAnchor implements OnTouchListener,
            ILongTouchListener, IMenuClickListener {
        /**
         * 游标padding值 .
         */
        private final float ROLL_BOX_PADDING = ResourceUtil
                .getDimens(R.dimen.reading_rollbox_padding);

        /**
         * 游标宽度 .
         */
        private final float ROLL_BOX_WIDTH = ResourceUtil
                .getDimens(R.dimen.reading_rollbox_width);

        /**
         * 游标高度 .
         */
        private final float ROLL_BOX_HEIGHT = ResourceUtil
                .getDimens(R.dimen.reading_rollbox_height);

        private ImageView mRollBoxDownView;
        private ImageView mRollBoxUpView;

        private RectF mContentRect;

        private SelectorPopMenu mSelectorPopMenu;

        public SelectorAnchor(SelectorPopMenu selectorPopMenu) {
            mRollBoxDownView = (ImageView) findViewById(R.id.rollbox_down_view);
            mRollBoxUpView = (ImageView) findViewById(R.id.rollbox_up_view);

            mSelectorPopMenu = selectorPopMenu;

            mContentRect = mReadStyleManager.getContentRectF();

            refresh();
        }

        /**
         * 是否显示
         *
         * @return
         */
        public boolean isShowing() {
            return mRollBoxDownView.isShown()
                    || mRollBoxUpView.isShown()
                    || (null != mSelectorPopMenu && mSelectorPopMenu
                    .isShowing());
        }

        /**
         * 隐藏
         */
        private void dismiss() {
            if (null != mSelectorPopMenu) {
                mSelectorPopMenu.dismiss();
            }

            // 结束长按状态
            mPageWidget.endLongTouch();
        }

        /**
         * 刷新
         */
        public void refresh() {
            mRollBoxUpView.setImageDrawable(mReadStyleManager
                    .getDrawableFromIdentifier(ReadActivity.this,
                            R.drawable.rollbox_up));
            mRollBoxDownView.setImageDrawable(mReadStyleManager
                    .getDrawableFromIdentifier(ReadActivity.this,
                            R.drawable.rollbox_down));

            mSelectorPopMenu.updateViews();
        }

        /**
         * 初始化文字选择器
         */
        private void initTextSelector() {
            mRollBoxDownView.setOnTouchListener(this);
            mRollBoxUpView.setOnTouchListener(this);

            mPageWidget.setLongTouchEnable(true);
            mPageWidget.setLongTouchListener(this);
            mPageWidget.setMenuClickListener(this);
        }

        /**
         * 更新文字选择器游标
         */
        private void updateRollboxView() {
            Selection startSelector = mPageFactory.getStartSelection();
            Selection endSelector = mPageFactory.getEndSelection();

            if (Selection.DEFAULT_VALUE == startSelector.getSelection()
                    || Selection.DEFAULT_VALUE == endSelector.getSelection()) {
                // 如果位置寻找失败则直接返回
                return;
            }

            int startX = (int) (startSelector.getSelectionX() - ROLL_BOX_WIDTH
                    / 2 - 2);
            int startY = (int) (startSelector.getSelectionY() - ROLL_BOX_HEIGHT
                    + ROLL_BOX_PADDING - 5);

            int endX = (int) (endSelector.getSelectionX()
                    + endSelector.getSelectionCharW() - ROLL_BOX_WIDTH / 2 + 2);
            int endY = (int) (endSelector.getSelectionY()
                    + endSelector.getSelectionCharH() - ROLL_BOX_PADDING);

            LayoutParams startParams = (LayoutParams) mRollBoxUpView
                    .getLayoutParams();
            if (null != startParams) {
                startParams.x = startX;
                startParams.y = startY;
                mRollBoxUpView.setLayoutParams(startParams);
            }

            LayoutParams endParams = (LayoutParams) mRollBoxDownView
                    .getLayoutParams();
            if (null != endParams) {
                endParams.x = endX;
                endParams.y = endY;
                mRollBoxDownView.setLayoutParams(endParams);
            }
        }

        /**
         * 显示弹出菜单
         */
        private void showPopupMenu() {
            int widgetHeight = mPageWidget.getHeight();
            int top = (int) (mRollBoxUpView.getTop() + ROLL_BOX_PADDING);
            int bottom = (int) (mRollBoxDownView.getBottom() - ROLL_BOX_PADDING);
            int popWindowHeight = mPageWidget.getPopWindowHeight();

            if (widgetHeight - bottom > popWindowHeight + 10) {
                mPageWidget.showPopupWindow(0, bottom + popWindowHeight, true);

            } else if (top > popWindowHeight + 10) {
                mPageWidget.showPopupWindow(0, top, false);

            } else {
                mPageWidget.showPopupWindow(0,
                        (widgetHeight + popWindowHeight) / 2, false);
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;

                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX();
                    float y = event.getRawY();

                    if (x < mContentRect.left) {
                        x = mContentRect.left;
                    }

                    if (x > mContentRect.right) {
                        x = mContentRect.right;
                    }

                    if (y < mContentRect.top) {
                        y = mContentRect.top;
                    }

                    if (y > mContentRect.bottom) {
                        y = mContentRect.bottom;
                    }

                    // 区别上游标和下游标
                    if (v.getId() == R.id.rollbox_down_view) {
                        mPageFactory.findSelection(x, y, PageFactory.INDEX_END);

                    } else if (v.getId() == R.id.rollbox_up_view) {
                        mPageFactory.findSelection(x, y, PageFactory.INDEX_START);
                    }

                    mPageWidget.forceInvalidate();
                    updateRollboxView();
                    break;

                case MotionEvent.ACTION_UP:
                    showPopupMenu();
                    break;
                default:
                    break;
            }

            return true;
        }

        @Override
        public void onLongTouch(boolean longPressMode) {
            if (mPageFactory.isVipChapter()) {
                return;
            }

            Selection startSelector = mPageFactory.getStartSelection();
            Selection endSelector = mPageFactory.getEndSelection();

            if (longPressMode
                    && startSelector.getSelection() != Selection.DEFAULT_VALUE
                    && endSelector.getSelection() != Selection.DEFAULT_VALUE) {
                updateRollboxView();

                mRollBoxDownView.setVisibility(View.VISIBLE);
                mRollBoxUpView.setVisibility(View.VISIBLE);
            } else {
                mRollBoxDownView.setVisibility(View.GONE);
                mRollBoxUpView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onTouchUp(MotionEvent event) {
            if (mPageFactory.isVipChapter()) {
                return;
            }

            Selection startSelector = mPageFactory.getStartSelection();
            Selection endSelector = mPageFactory.getEndSelection();

            if (startSelector.getSelection() != Selection.DEFAULT_VALUE
                    && endSelector.getSelection() != Selection.DEFAULT_VALUE) {

                showPopupMenu();
            }
        }

        @Override
        public void onMenuClick(int index, View view) {
            String selectText = mPageFactory.getSelectText();

            if (index < mTextSelectorMenuList.size()) {
                String menuTxt = mTextSelectorMenuList.get(index);

                if (ResourceUtil.getString(R.string.text_selector_copy).equals(
                        menuTxt)) {
                    // 复制
                    copyToClipboard(selectText);

                } else if (ResourceUtil.getString(R.string.text_selector_share)
                        .equals(menuTxt)) {
                    // 分享
                    share(selectText);

                } else if (ResourceUtil.getString(
                        R.string.text_selector_summary).equals(menuTxt)) {
                    // 书摘
                    if (null == mPageFactory.addBookSummary()) {
                        shortToast(R.string.add_summary_failed);
                    } else {
                        mPageWidget.forceInvalidate();
                        shortToast(R.string.add_summary_succ);
                    }
                }
            }

            dismiss();
        }
    }

    private class BookSummaryPopControl implements IMenuClickListener {
        private SelectorPopMenu mPopupWindow;
        private BookSummary mBookSummary;
        private boolean isShowing;

        public BookSummaryPopControl() {
            mPopupWindow = new SelectorPopMenu(ReadActivity.this);
            mPopupWindow.init(mPageWidget.getRootView(), mSummaryMenuList,
                    mReadStyleManager);
            mPopupWindow.setMenuClickListener(this);
        }

        @Override
        public void onMenuClick(int index, View view) {
            if (mBookSummary == null) {
                dismiss();
                return;
            }
            String selectText = mBookSummary.getContent();

            if (index < mSummaryMenuList.size()) {
                String menuTxt = mSummaryMenuList.get(index);

                if (ResourceUtil.getString(R.string.text_selector_copy).equals(
                        menuTxt)) {
                    // 复制
                    copyToClipboard(selectText);

                } else if (ResourceUtil.getString(R.string.text_selector_share)
                        .equals(menuTxt)) {
                    // 分享
                    share(selectText);

                } else if (ResourceUtil.getString(R.string.delete).equals(
                        menuTxt)) {
                    // 删除
                    mPageFactory.deleteBookSummary(mBookSummary);
                    mPageWidget.forceInvalidate();
                }
            }
            dismiss();
        }

        /**
         * 是否显示
         *
         * @return
         */
        public boolean isShowing() {
            return isShowing;
        }

        public void show(BookSummaryPostion bookSummaryPostion) {
            if (bookSummaryPostion == null) {
                return;
            }
            mBookSummary = bookSummaryPostion.relateBookSummary;

            // show popupwindow
            int popWindowHeight = mPopupWindow.getHeight();
            int widgetHeight = mPageWidget.getHeight();
            int top = (int) bookSummaryPostion.startTop;
            int bottom = (int) bookSummaryPostion.endBottom;

            if (widgetHeight - bottom > popWindowHeight + 10) {
                mPopupWindow.show(0, bottom + popWindowHeight + 10, true);

            } else if (top > popWindowHeight + 10) {
                mPopupWindow.show(0, top, false);

            } else {
                mPopupWindow.show(0, (widgetHeight + popWindowHeight) / 2,
                        false);
            }
            isShowing = true;
        }

        /**
         * 隐藏
         */
        public void dismiss() {
            if (null != mPopupWindow) {
                mPopupWindow.dismiss();
            }
            isShowing = false;
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setText(text);
        shortToast(R.string.copy_to_clipboard);

        UserActionManager.getInstance().recordEvent(Constants.ACTION_COPY);
    }

    private void share(final String text) {
        new GenericTask() {
            WeiboContent mContent;

            @Override
            protected void onPreExecute() {
                showProgress(R.string.create_share_content, true);
            }

            @Override
            protected TaskResult doInBackground(TaskParams... params) {
                mContent = new WeiboContent(gBook, WeiboContent.TYPE_READ_NOTE);

                Bitmap cover = ImageLoader.getInstance().syncLoadBitmap(
                        gBook.getDownloadInfo().getImageUrl());
                if (null != cover && cover != ImageLoader.getDefaultPic()) {
                    mContent.setImagePath(Util.saveBitmap2file(cover,
                            gBook.getTitle(), 100));
                }

                mContent.setChapterId(Chapter.DEFAULT_GLOBAL_ID);
                mContent.setChapterOffset(0);
                mContent.setMsg(text);

                return null;
            }

            @Override
            protected void onPostExecute(TaskResult result) {
                dismissProgress();

                if (null != mContent && !TextUtils.isEmpty(mContent.getMsg())) {
                    ShareDialog.show(ReadActivity.this, mContent);
                } else {
                    Toast.makeText(ReadActivity.this,
                            R.string.create_share_content_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /**
     * release页面占有的资源，尤其是异步任务
     */
    public void release(boolean force) {
        // 清理控制变量
        mIsJump = false;
        mIsVIPChapter = false;
        mPlayingAnim = false;
        mCanFinish = false;
        mReadOnline = "";
        mToolbar.setIsInitBarData(true);

        isDownChapterAfterLoginThenNeedToReloadCacheData = false;

        OnlineBookManager.getInstance().release();
        mChapterIdList.clear();

        // 书架上的在线书籍存下文件
        if (isNeedToCache && gBook.isOnlineBook()
                && BOOK_LOCAL != gBook.getDownloadInfo().getLocationType()) {
            DownBookManager.getInstance().cacheBookCloud(gBook, mPageFactory);
        }

        // 在线试读清除内存中的书签、书摘
        // 检测书签并删除
        if (!DownBookManager.getInstance().hasBook(gBook)) {
            gBook.setBookmarks(null);
            gBook.setBookSummaries(null);
            // DBService.deleteAllBookMark(gBook);
        }
        // if (!gBook.isOnlineBook() && BOOK_ONLINE ==
        // gBook.getDownloadInfo().getLocationType()
        // && gBook.getDownloadInfo().getDownLoadState() <=
        // DownBookJob.STATE_PREPARING) {
        // gBook.setBookmarks(null);
        // gBook.setBookSummaries(null);
        // }

        if (mPageFactory != null) {
            LogUtil.d("BugID=21356", "ReadActivity >> release >> force="
                    + force + ", mPageFactory release");
            mPageFactory.release(force);
        }

        if (mPreGetChapterTask != null) {
            mPreGetChapterTask.cancel(true);
        }

        // 在线书籍删除缓存文件
        if (!isClickAddShelf) {
            deleteTempFile();
        } else {
            isClickAddShelf = false;
        }
    }

    private void initChapter(Chapter chapter) {
        // 本地
        if (gBook.getDownloadInfo().getLocationType() != BOOK_ONLINE) {
            chapter = gBook.getChapter(chapter);
            if (chapter.getLength() <= 0) {
                // 本地-该章未下
                updatePageFactory();
                mIsJump = true;
                mJumpOffset = 0;
                if (getChapterStates(chapter) == CHAPTER_VIP) {
                    judgeChapterStateAndLoad(chapter);
                } else {
                    downloadChapter(chapter, true, READ_LOCAL_BOOK);
                }

            } else {
                // 本地-该章有下
                if (getChapterStates(chapter) == CHAPTER_VIP) {
                    updatePageFactory();
                    mPageFactory.seek((int) (chapter.getStartPos()));
                    invalidateUI(true);
                } else {
                    gBook.getReadInfo().setLastPos((int) chapter.getStartPos());
                    updatePageFactory();
                }
            }

        } else {
            // 在线
            // 保证章节信息不为空

			/*
             * 在线状态下，有可能出现章节长度不为空，造成直接不下载return，显示空白页
			 */
            chapter.setLength(0);
            chapter.setState(Chapter.CHAPTER_PREPARE);
            // parseBookmarkFromJson2();

            mSelectedChapter = chapter;
            if (getChapterStates(chapter) == CHAPTER_VIP) {
                mIsJump = true;
                reqChapterList(chapter);
            } else {
                downloadChapter(chapter, true, READ_ONLINE_BOOK);
            }
        }
    }

    private void finishActivity() {
        makeFullScreen(false);
        // if (gBook.hasPraised()) {
        // Intent intent = new Intent();
        // intent.putExtra("praised", true);
        // setResult(RESULT_OK, intent);
        // }
        finish();
    }

}