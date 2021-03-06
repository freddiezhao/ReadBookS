package com.sina.book.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.RankingListResult;
import com.sina.book.data.RecommendAuthorListItem;
import com.sina.book.data.RecommendAuthorListResult;
import com.sina.book.data.RecommendCmsResult;
import com.sina.book.image.ImageLoader;
import com.sina.book.image.PauseOnScrollListener;
import com.sina.book.parser.AuthorListParser;
import com.sina.book.parser.BaseParser;
import com.sina.book.parser.BookParser;
import com.sina.book.parser.RecommendCmsParser;
import com.sina.book.ui.adapter.BookAdapter;
import com.sina.book.ui.adapter.CommonListAdapter;
import com.sina.book.ui.adapter.ExtraListAdapter;
import com.sina.book.ui.widget.XListView;
import com.sina.book.ui.widget.XListView.IXListViewListener;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.NumericHelper;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的书籍列表界面
 *
 * @author MarkMjw
 * @date 2013-7-23
 */
public class CommonListActivity extends CustomTitleActivity implements OnItemClickListener, ITaskFinishListener,
        IXListViewListener {
    // private static final String TAG = "CommonListActivity";

    public static final String PARAM_URL = "Url";
    public static final String PARAM_TITLE = "Title";
    public static final String PARAM_EVENTKEY_TITLE = "EventKeyTitle";
    public static final String PARAM_TYPE = "Type";
    public static final String PARAM_PER_PAGE_COUNT = "perpageCount";

    private XListView mListView;
    private View mProgressView;
    private View mErrorView;

    private ExtraListAdapter mAdapter;

    private String mTitle;
    private String mEventKeyTitle;
    private String mReqUrl;

    private String mType;

    // private int mCurPage = 1;
    private int mPerPageCount = ConstantData.PAGE_SIZE;

    public static void launch(Context c, String url, String title, String type) {
        launch(c, url, title, title, type, ConstantData.PAGE_SIZE);
    }

    public static void launch(Context c, String url, String title, String eventKeyTitle, String type) {
        launch(c, url, title, eventKeyTitle, type, ConstantData.PAGE_SIZE);
    }

    /**
     * 启动
     *
     * @param c     上下文引用
     * @param url   请求Url
     * @param title 界面title
     * @param type  数据类型
     */
    public static void launch(Context c, String url, String title, String eventKeyTitle, String type, int perPageNum) {
        Intent intent = new Intent();
        intent.setClass(c, CommonListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        intent.putExtra(PARAM_URL, url);
        intent.putExtra(PARAM_TITLE, title);
        intent.putExtra(PARAM_EVENTKEY_TITLE, eventKeyTitle);
        intent.putExtra(PARAM_TYPE, type);
        intent.putExtra(PARAM_PER_PAGE_COUNT, perPageNum);

        c.startActivity(intent);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.act_common_book_list);

        getDataIntent();
        initTitle();
        initViews();
        reqData(1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position -= mListView.getHeaderViewsCount();

        // 点击获取更多
        if (position >= mAdapter.getDataSize()) {
            if (mAdapter.IsAdding()) {
                return;
            }

            if (mAdapter.hasMore()) {
                reqData(mAdapter.getCurrentPage() + 1);
            }
        } else if (position >= 0) {
            // 进入详情
            if (!TextUtils.isEmpty(mType) && mType.equals(CommonListAdapter.TYPE_GOOD_BOOK)) {
                enterBookDetailActivity((Book) mAdapter.getItem(position));
            } else {
                //
                String eventKey = mEventKeyTitle + "_01_" + Util.formatNumber(position + 1);
                String eventExtra = null;
                // 分类要截取类型，如都市，作为事件的Extra信息
                if (!TextUtils.isEmpty(mType) && mType.equals(CommonListAdapter.TYPE_PARTITION)) {
                    if (!TextUtils.isEmpty(mTitle)) {
                        int index = mTitle.indexOf("-");
                        if (index != -1) {
                            eventExtra = mTitle.substring(0, index);
                        }
                    }
                } else if (!TextUtils.isEmpty(mType) && mType.equals(CommonListAdapter.TYPE_AUTHOR_BOOKS)) {
                    eventExtra = "作家作品";
                } else if (!TextUtils.isEmpty(mType) && mType.equals(CommonListAdapter.TYPE_STAR_AUTHORS)) {
                    eventExtra = "明星作家";
                }
                BookDetailActivity.launch(this, ((Book) mAdapter.getItem(position)), eventKey, eventExtra);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
    }

    @Override
    protected void retry() {
        reqData(mAdapter.getCurrentPage());
    }

    @Override
    public void onClickLeft() {
        finish();
        super.onClickLeft();
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            mAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoadMore() {
        if (!HttpUtil.isConnected(this)) {
            shortToast(R.string.network_unconnected);
            mListView.stopLoadMore();
            return;
        }

        if (mAdapter.IsAdding()) {
            return;
        }

        if (mAdapter.hasMore()) {
            reqData(mAdapter.getCurrentPage() + 1);
        }
    }

    @Override
    public void onTaskFinished(TaskResult taskResult) {
        if (mProgressView != null) {
            mProgressView.setVisibility(View.GONE);
        }

        RequestTask task = (RequestTask) taskResult.task;
        int page = (Integer) task.getExtra();
        if (taskResult.retObj != null && taskResult.retObj instanceof RankingListResult) {
            // mCurPage = page;
            mAdapter.setCurrentPage(page);
            RankingListResult result = (RankingListResult) taskResult.retObj;

            int total = result.getTotal();
            List<Book> data = result.getItem();

            if (mAdapter.IsAdding()) {
                mAdapter.setAdding(false);
                mAdapter.addList(data);
                mAdapter.setTotal(total);
            } else {
                mAdapter.setList(data);
                mAdapter.setTotal(total);
            }

            // 书籍列表数目出错容错
            if (mAdapter.getCurrentPage() * mPerPageCount > mAdapter.getTotal() && data.size() == 0) {
                mAdapter.setTotal(mAdapter.getDataSize());
            }

            if (!mAdapter.hasMore()) {
                mListView.setPullLoadEnable(false);
            } else {
                mListView.setPullLoadEnable(true);
            }

            mAdapter.notifyDataSetChanged();
            return;

        } else if (taskResult.retObj != null && taskResult.retObj instanceof RecommendAuthorListResult) {
            RecommendAuthorListResult result = (RecommendAuthorListResult) taskResult.retObj;
            List<RecommendAuthorListItem> lists = result.getLists();
            if (lists != null && lists.size() > 0) {
                // 明星作家作品列表
                if (CommonListAdapter.TYPE_AUTHOR_BOOKS.equals(mType)) {
                    RecommendAuthorListItem item = lists.get(0);
                    int total = NumericHelper.parseInt(String.valueOf(item.map.get("books_total")), 0);
                    List<Book> data = item.getItems();

                    if (mAdapter.IsAdding()) {
                        mAdapter.setAdding(false);
                        mAdapter.addList(data);
                        mAdapter.setTotal(total);
                    } else {
                        mAdapter.setList(data);
                        mAdapter.setTotal(total);
                    }

                    // 书籍列表数目出错容错
                    if (mAdapter.getCurrentPage() * mPerPageCount > mAdapter.getTotal() && data.size() == 0) {
                        mAdapter.setTotal(mAdapter.getDataSize());
                    }

                    if (!mAdapter.hasMore()) {
                        mListView.setPullLoadEnable(false);
                    } else {
                        mListView.setPullLoadEnable(true);
                    }

                    mAdapter.notifyDataSetChanged();
                    return;
                } else if (CommonListAdapter.TYPE_STAR_AUTHORS.equals(mType)) {
                    // 明星作家列表
                    List<Book> data = new ArrayList<Book>();
                    for (int i = 0; i < lists.size(); i++) {
                        // A B C D
                        // A- B C D
                        // B C D
                        // B C- D
                        RecommendAuthorListItem item = lists.get(i);
                        if (item.getItems() != null && item.getItems().size() > 0) {
                            // 有书籍
                            Book book = item.getItems().get(0);
                            data.add(book);
                        } else {
                            lists.remove(i);
                            i--;
                        }
                    }

                    int total = result.getTotal();
                    if (mAdapter.IsAdding()) {
                        mAdapter.setAdding(false);
                        mAdapter.addList(data);
                        mAdapter.addExtraList(lists);
                        mAdapter.setTotal(total);
                    } else {
                        mAdapter.setList(data);
                        mAdapter.setExtraList(lists);
                        mAdapter.setTotal(total);
                    }

                    // 书籍列表数目出错容错
                    if (mAdapter.getCurrentPage() * mPerPageCount > mAdapter.getTotal() && data.size() == 0) {
                        mAdapter.setTotal(mAdapter.getDataSize());
                    }

                    if (!mAdapter.hasMore()) {
                        mListView.setPullLoadEnable(false);
                    } else {
                        mListView.setPullLoadEnable(true);
                    }

                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }
        } else if (taskResult.retObj != null && taskResult.retObj instanceof RecommendCmsResult) {//男生爱看，女生爱看
            mAdapter.setCurrentPage(page);
            RecommendCmsResult result = (RecommendCmsResult) taskResult.retObj;

            int total = result.getRecommendToday().total;
            List<Book> books = result.getRecommendToday().books;

            if (mAdapter.IsAdding()) {
                mAdapter.setAdding(false);
                mAdapter.addList(books);
                mAdapter.setTotal(total);
            } else {
                mAdapter.setList(books);
                mAdapter.setTotal(total);
            }

            // 书籍列表数目出错容错
            if (mAdapter.getCurrentPage() * mPerPageCount > mAdapter.getTotal() && books.size() == 0) {
                mAdapter.setTotal(mAdapter.getDataSize());
            }

            if (!mAdapter.hasMore()) {
                mListView.setPullLoadEnable(false);
            } else {
                mListView.setPullLoadEnable(true);
            }

            mAdapter.notifyDataSetChanged();
            return;

        } else {
            if (mAdapter.IsAdding()) {
                mAdapter.setAdding(false);
            }

            if (mAdapter.getDataSize() == 0) {
                mErrorView.setVisibility(View.VISIBLE);
            } else {
                shortToast(R.string.network_unconnected);
            }
        }
        mListView.stopLoadMore();
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        mTitle = intent.getStringExtra(PARAM_TITLE);
        mEventKeyTitle = intent.getStringExtra(PARAM_EVENTKEY_TITLE);

        mType = intent.getStringExtra(PARAM_TYPE);
        mPerPageCount = intent.getIntExtra(PARAM_PER_PAGE_COUNT, ConstantData.PAGE_SIZE);
        String urlFormat = intent.getStringExtra(PARAM_URL);

        if (null != urlFormat) {
            // 判断是否要分页
            if (urlFormat.contains("page=1")) {
                mReqUrl = urlFormat.replace("page=1", "page=%s");
            } else {
                mReqUrl = urlFormat;
            }
        } else {
            mReqUrl = "";
        }
    }

    private void initTitle() {
        TextView middleTv = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
        if (null != middleTv) {
            middleTv.setText(mTitle);
            setTitleMiddle(middleTv);
        }

        View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
        setTitleLeft(left);
    }

    private void initViews() {
        mListView = (XListView) findViewById(R.id.lv_books);
        mListView.setOnItemClickListener(this);
        mListView.setPullRefreshEnable(false);
        mListView.setPullLoadEnable(false);
        mListView.setXListViewListener(this);
        mListView.setDivider(null);

        mProgressView = findViewById(R.id.rl_progress);
        mErrorView = findViewById(R.id.error_layout);

        if (!TextUtils.isEmpty(mType)) {
            mAdapter = new CommonListAdapter(this, mType);
        } else {
            mAdapter = new BookAdapter(this);
        }
        mAdapter.setPerPage(mPerPageCount);

        // 当为“明星作家”时，为XListView添加Margins
        if (CommonListAdapter.TYPE_STAR_AUTHORS.equals(mType)) {
            LayoutParams params = (LayoutParams) mListView.getLayoutParams();
            int margin = PixelUtil.dp2px(5.0f);
            params.setMargins(margin, margin, margin, margin);
            mListView.setLayoutParams(params);
        }

        mListView.setAdapter(mAdapter);
    }

    private void reqData(int page) {
        if (page == 1) {
            if (!HttpUtil.isConnected(this)) {
                mProgressView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
                return;
            } else {
                mProgressView.setVisibility(View.VISIBLE);
                mErrorView.setVisibility(View.GONE);
            }
        } else {
            mAdapter.setAdding(true);
            mAdapter.notifyDataSetChanged();
        }

        if (TextUtils.isEmpty(mReqUrl)) {
            mProgressView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);

            shortToast(R.string.wrong_url);
            return;
        }

        BaseParser parser = null;
        if (!TextUtils.isEmpty(mType)) {
            // 1.明星作家的作品列表
            // 2.明星作家列表
            if (CommonListAdapter.TYPE_AUTHOR_BOOKS.equals(mType)
                    || CommonListAdapter.TYPE_STAR_AUTHORS.equals(mType)) {
                parser = new AuthorListParser();
            } else if (CommonListAdapter.TYPE_BOYANDGIRL.equals(mType)) {
                parser = new RecommendCmsParser();
            }
        }

        if (parser == null) {
            parser = new BookParser();
        }
        RequestTask requestTask = new RequestTask(parser);
        requestTask.setTaskFinishListener(this);
        requestTask.setExtra(page);

        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, mReqUrl.contains("%s") ? String.format(mReqUrl, page) : mReqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        requestTask.execute(params);
    }

    private void enterBookDetailActivity(Book book) {
        if (book == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, BookDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        Bundle bundle = new Bundle();
        bundle.putString("bid", book.getBookId());
        bundle.putString("sid", book.getSid());
        bundle.putString("src", book.getBookSrc());
        intent.putExtras(bundle);

        startActivityForResult(intent, 0);
    }
}
