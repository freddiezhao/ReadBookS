package com.sina.book.ui.view;

import java.util.List;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.ITaskUpdateListener;
import com.sina.book.data.Book;
import com.sina.book.data.BookPriceResult;
import com.sina.book.data.ConstantData;
import com.sina.book.data.MainBookItem;
import com.sina.book.data.MainBookResult;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.data.util.PurchasedBookList;
import com.sina.book.image.IImageLoadListener;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.BookPriceParser;
import com.sina.book.ui.BookDetailActivity;
import com.sina.book.ui.ReadActivity;
import com.sina.book.ui.adapter.MainTabAdapter;
import com.sina.book.ui.adapter.MainTabAdapter.IStateChangedListener;
import com.sina.book.ui.widget.ImageFlowIndicator;
import com.sina.book.ui.widget.NewMainRelativeLayout;
import com.sina.book.ui.widget.ViewFlow;
import com.sina.book.util.Util;

public class MainDialog extends Dialog implements IStateChangedListener {

    /** 播放时间间隔 */
    private final int UPDATE_TIME = 10000;

    private Context mContext;

    private NewMainRelativeLayout mMainDialogLayout;
    private LinearLayout mEditorLayout;
    private TextView mEditorTip;
    private ImageView mEditorBookImage;
    private TextView mEditorBookText;
    private TextView mEditorBookTitle;
    private TextView mEditorBookAuthor;
    private TextView mEditorBookContent;
    private Button mReadBtn;
    private Button mDownloadBtn;

    private ViewFlow mViewFlow;
    private MainTabAdapter mTabAdapter;

    private Book mEditorBook;
    private List<MainBookItem> mBookItems;

    private IStateChangedListener mListener;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownBookManager.ACTION_INTENT_DOWNSTATE.equals(intent.getAction())) {
                updateBtnState();
            }
        }

    };

    private ITaskUpdateListener mUpdateListener = new ITaskUpdateListener() {

        @Override
        public void onUpdate(Book book, boolean mustUpdate, boolean mustRefresh, int progress, int stateCode) {
            if (mEditorBook == null || !mEditorBook.equals(book)) {
                return;
            }
            if (stateCode == DownBookJob.STATE_RECHARGE) {
                mEditorBook.getBuyInfo().setHasBuy(false);
                updateBtnState();
                PayDialog.showBalanceDlg(mContext);
            }
        }
    };

    public MainDialog(Context context, IStateChangedListener listener) {
        super(context, R.style.MenuDialog);

        mContext = context;
        mListener = listener;

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = Util.getDisplayMetrics(context).widthPixels;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        onWindowAttributesChanged(lp);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vw_main_dialog);
        initView();
        initListener();
    }

    @Override
    protected void onStart() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(DownBookManager.ACTION_INTENT_DOWNSTATE);
        mContext.registerReceiver(mReceiver, myIntentFilter);
        DownBookManager.getInstance().addProgressListener(mUpdateListener);

        mViewFlow.startAutoFlow(UPDATE_TIME);

        updateBtnState();

        super.onStart();
    }

    @Override
    protected void onStop() {
        mContext.unregisterReceiver(mReceiver);
        DownBookManager.getInstance().removeProgressListener(mUpdateListener);

        mViewFlow.stopAutoFlow();
        super.onStop();
    }

    private void initView() {
        mMainDialogLayout = (NewMainRelativeLayout) findViewById(R.id.main_dialog_layout);
        mEditorLayout = (LinearLayout) findViewById(R.id.main_operation_layout);
        mEditorTip = (TextView) findViewById(R.id.main_title);
        mEditorBookImage = (ImageView) findViewById(R.id.book_head_img);
        mEditorBookText = (TextView) findViewById(R.id.book_head_title);
        mEditorBookTitle = (TextView) findViewById(R.id.book_title);
        mEditorBookAuthor = (TextView) findViewById(R.id.book_author);
        mEditorBookContent = (TextView) findViewById(R.id.book_content);
        mReadBtn = (Button) findViewById(R.id.book_read_btn);
        mDownloadBtn = (Button) findViewById(R.id.book_download_btn);

        mTabAdapter = new MainTabAdapter(mContext);
        mViewFlow = (ViewFlow) findViewById(R.id.viewflow);
        ImageFlowIndicator mIndicator = (ImageFlowIndicator) findViewById(R.id.viewflowindic);
        mViewFlow.setFlowIndicator(mIndicator);
        mViewFlow.setAdapter(mTabAdapter);

        mTabAdapter.setStateChangedListener(mListener);
    }

    @Override
    public void show() {
        super.show();
    }

    public void show(MainBookResult mainBookInfo) {
        show();
        updateMainDialog(mainBookInfo);
    }

    private void mainDialogStateChanged() {
        if (null != mListener) {
            mListener.stateChanged();
        }
    }

    public void updateMainDialog(MainBookResult mainBookInfo) {
        if (null == mainBookInfo) {
            return;
        }

        // 更新编辑推荐
        mEditorBook = mainBookInfo.getEditorRecommend(false);
        if (null != mEditorBook) {
            mEditorTip.setText(mEditorBook.getComment());
            ImageLoader.getInstance().load(mEditorBook.getDownloadInfo().getImageUrl(), mEditorBookImage,
                    ImageLoader.TYPE_BIG_PIC, ImageLoader.getDefaultPic(), new IImageLoadListener() {

                        @Override
                        public void onImageLoaded(Bitmap bm, ImageView imageView, boolean loadSuccess) {
                            if (loadSuccess) {
                                mEditorBookText.setText("");
                            } else {
                                // 如果imageurl加载失败，默认书皮上会显示书籍标题
                                if (mEditorBook != null) {
                                    mEditorBookText.setText(mEditorBook.getTitle());
                                }
                            }
                        }
                    });

            mEditorTip.setText(mEditorBook.getComment());
            mEditorBookTitle.setText(mEditorBook.getTitle());
            mEditorBookAuthor.setText(mEditorBook.getAuthor());
            mEditorBookContent.setText(mEditorBook.getRecommendIntro());

            updateBtnState();
        }

        // 更新书城tab信息
        mBookItems = mainBookInfo.getMainBooks();
        if (null != mBookItems) {
            mTabAdapter.setData(mBookItems);
            mTabAdapter.notifyDataSetChanged();
        }
    }

    private void initListener() {
        mMainDialogLayout.setOnFlingDownListener(new NewMainRelativeLayout.OnFlingDownListener() {

            @Override
            public void onFlingDown() {
                mainDialogStateChanged();
            }
        });
        mMainDialogLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainDialogStateChanged();
            }
        });

        mEditorLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mEditorBook) {
                    BookDetailActivity.launch(mContext, mEditorBook);
                    mainDialogStateChanged();
                }
            }
        });

        mReadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null == mEditorBook) {
                    return;
                }

                Book book = DownBookManager.getInstance().getBook(mEditorBook);
                if (book != null && Math.abs(book.getDownloadInfo().getProgress() - 1.0) < 0.0001
                        && book.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_FINISHED) {
                    // 如果是读的书架上的下载好的书，需要更新该book的最后阅读时间等属性
                    mEditorBook = book;
                    ReadActivity.launch(mContext, mEditorBook, false,false);
                } else {
                    // 否则认为是在线试读
                    ReadActivity.launch(mContext, mEditorBook, true,false);
                }

                mainDialogStateChanged();
            }
        });

        mDownloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onDownloadBtnClick();

            }
        });
    }

    private void onDownloadBtnClick() {
        if (null == mEditorBook) {
            return;
        }

        if (mEditorBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE
                || mEditorBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {

        	CloudSyncUtil.getInstance().add2Cloud(mContext, mEditorBook);
            DownBookManager.getInstance().downBook(mEditorBook);
            updateBtnState();
        } else {
            if (!mEditorBook.getBuyInfo().isHasBuy()) {
                PayDialog dlg = new PayDialog((Activity) mContext, mEditorBook, null, null);
                dlg.setOnPayLoginSuccessListener(new PayDialog.PayLoginSuccessListener() {

                    @Override
                    public void onLoginSuccess() {
                        if (mEditorBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
                            reqBookPrice();
                            updateBtnState();
                        }
                    }
                });
                dlg.setOnPayFinishListener(new PayDialog.PayFinishListener() {
                    @Override
                    public void onFinish(int code) {
                        if (PayDialog.CODE_SUCCESS == code) {
                            updateBtnState();
                            Toast.makeText(mContext, R.string.buy_success, Toast.LENGTH_LONG).show();
                            CloudSyncUtil.getInstance().add2Cloud(mContext, mEditorBook);
                        }
                    }
                });
                dlg.show();
            } else {
                DownBookManager.getInstance().downBook(mEditorBook);
            }
        }
    }

    private void updateBtnState() {

        if (mEditorBook == null) {
            return;
        }
        Book book = DownBookManager.getInstance().getBook(mEditorBook);
        DownBookJob job = DownBookManager.getInstance().getJob(mEditorBook);
        boolean hasBook = (job != null);

        // 阅读按钮状态
        String readText;
        if (hasBook && (job.getState() == DownBookJob.STATE_FINISHED) && book != null && !book.isOnlineBook()) {
            readText = mContext.getString(R.string.main_has_read);
        } else {
            readText = mContext.getString(R.string.main_read);
        }
        mReadBtn.setText(readText);

        // 下载，购买按钮状态
        String buyText = "";
        boolean enableBuyButton = true;
        // 1 更新下载状态
        if (hasBook) {
            if (book != null) {
                mEditorBook.getDownloadInfo().setDownLoadState(book.getDownloadInfo().getDownLoadState());
                mEditorBook.getDownloadInfo().setProgress(book.getDownloadInfo().getProgress());
            }
        }

        // 2 设置按钮
        if (mEditorBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_FREE) {
            buyText = mContext.getString(R.string.main_download);
            if (hasBook) {
                if (mEditorBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                        || mEditorBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                    buyText = String.format(mContext.getString(R.string.downloading_txt));
                    enableBuyButton = false;
                } else if (book != null && !book.isOnlineBook()) {
                    buyText = String.format(mContext.getString(R.string.has_down));
                    enableBuyButton = false;
                }
            }
        } else if (mEditorBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_CHAPTER_VIP) {
            buyText = mContext.getString(R.string.main_download);
            if (hasBook) {
                if (mEditorBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                        || mEditorBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                    buyText = String.format(mContext.getString(R.string.downloading_txt));
                    enableBuyButton = false;
                } else if (book != null && !book.isOnlineBook()) {
                    buyText = String.format(mContext.getString(R.string.has_down));
                    enableBuyButton = false;
                }
            }
        } else if (mEditorBook.getBuyInfo().getPayType() == Book.BOOK_TYPE_VIP) {
            boolean hasBuy = PurchasedBookList.getInstance().isBuy(mEditorBook) || mEditorBook.getBuyInfo().isHasBuy()
                    || DownBookManager.getInstance().hasBuy(mEditorBook);
            mEditorBook.getBuyInfo().setHasBuy(hasBuy);

            if (hasBuy) {
                buyText = mContext.getString(R.string.main_download);
                if (hasBook) {
                    if (mEditorBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_RUNNING
                            || mEditorBook.getDownloadInfo().getDownLoadState() == DownBookJob.STATE_PREPARING) {
                        buyText = String.format(mContext.getString(R.string.downloading_txt));
                        enableBuyButton = false;
                    } else if (book != null && !book.isOnlineBook()) {
                        buyText = String.format(mContext.getString(R.string.has_down));
                        enableBuyButton = false;
                    }
                }
            } else {
                buyText = mContext.getString(R.string.book_detail_buy);
            }
        }

        // 3 更新
        mDownloadBtn.setEnabled(enableBuyButton);
        mDownloadBtn.setText(buyText);

    }

    @Override
    public void stateChanged() {

    }

    /**
     * 请求书籍价格
     */
    private void reqBookPrice() {
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO_CHECK, mEditorBook.getBookId(), mEditorBook.getSid(),
                mEditorBook.getBookSrc());
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        RequestTask reqTask = new RequestTask(new BookPriceParser());
        reqTask.setTaskFinishListener(new ITaskFinishListener() {

            @Override
            public void onTaskFinished(TaskResult taskResult) {
                if (null != taskResult && taskResult.stateCode == HttpStatus.SC_OK) {
                    if (taskResult.retObj instanceof BookPriceResult) {
                        BookPriceResult result = (BookPriceResult) taskResult.retObj;
                        Book book = result.getBook();
                        updateBookPrice(book);
                        updateBtnState();
                    }
                }
            }
        });
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    private void updateBookPrice(Book book) {
        if (null != mEditorBook && null != book) {
            mEditorBook.getBuyInfo().setPrice(book.getBuyInfo().getPrice());
            mEditorBook.getBuyInfo().setDiscountPrice(book.getBuyInfo().getDiscountPrice());
            mEditorBook.getBuyInfo().setPriceTip(book.getBuyInfo().getPriceTip());
        }
    }

}
