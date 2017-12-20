package com.sina.book.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.AuthorPageResult;
import com.sina.book.data.Book;
import com.sina.book.data.CardPostion;
import com.sina.book.data.ConstantData;
import com.sina.book.data.RecommendAuthorListItem;
import com.sina.book.data.RecommendAuthorListResult;
import com.sina.book.data.RecommendBannerItem;
import com.sina.book.data.RecommendCateItem;
import com.sina.book.data.RecommendCateResult;
import com.sina.book.data.RecommendFamousItem;
import com.sina.book.data.RecommendFreeResult;
import com.sina.book.data.RecommendHotResult;
import com.sina.book.data.RecommendMonthItem;
import com.sina.book.data.RecommendResult;
import com.sina.book.data.RecommendToday;
import com.sina.book.data.RecommendTodayFree;
import com.sina.book.data.RecomondBannerResult;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.RecommendParser;
import com.sina.book.ui.adapter.BookEpubAdapter;
import com.sina.book.ui.adapter.BookImageAdapter;
import com.sina.book.ui.adapter.CommonAdapter;
import com.sina.book.ui.adapter.CommonListAdapter;
import com.sina.book.ui.adapter.ImageAdapter;
import com.sina.book.ui.adapter.RecommendAuthorAdapter;
import com.sina.book.ui.adapter.RecommendClassicAdapter;
import com.sina.book.ui.adapter.RecommendFamousAdapter;
import com.sina.book.ui.adapter.RecommendMonthAdapter;
import com.sina.book.ui.adapter.ViewHolder;
import com.sina.book.ui.widget.CircleFlowIndicator;
import com.sina.book.ui.widget.ListenableScrollView;
import com.sina.book.ui.widget.ViewFlow;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.NumericHelper;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.Util;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 【书城】-推荐页
 *
 * @author MarkMjw
 * @date 2013-4-8
 */
public class RecommendActivity extends CustomTitleActivity implements ITaskFinishListener {
    // private static final String TAG = "RecommendActivity";

    /**
     * 热门推荐类型.
     */
    private final int TYPE_HOT = 1;
    /**
     * 选你喜欢类型.
     */
    private final int TYPE_NEW = 4;
    /**
     * 免费专区类型.
     */
    private final int TYPE_FREE = 3;
    /**
     * epub专区
     */
    private final int TYPE_EPUB = 5;

    private View mProgressView;
    private View mErrorView;

    private ListenableScrollView mScrollView;

    /**
     * 以下为推荐Banner相关变量.
     */
    private View mRecommendHeader;
    private ViewFlow mViewFlow;
    private ImageAdapter mBannerImageAdapter;

    /**
     * 以下为好书推荐相关变量.
     */
    private View mRecommendGoodBook;
    private TextView mRecommendGoodBookContent;

    /**
     * 以下为热门作家相关变量.
     */
    private View mRecommendAuthor;
    private View mRecommendAuthorTitle;
    private TextView mRecommendAuthorCount;
    private RelativeLayout mRecommendAuthorAll;
    private ListView mListViewAuthor;
    private RecommendAuthorAdapter mAdapterAuthor;

    /**
     * 以下为编辑推荐相关变量.
     */
    private View mRecommendHot;
    private View mRecommendHotTitleLayout;
    private TextView mRecommendHotCount;
    private TextView mRecommendHotTitle;
    private RelativeLayout mRecommendHotAll;
    private ListView mListViewHot;
    private BookImageAdapter mAdapterHot;

    /**
     * 今日限免
     */
    private View todayFreeModule;
    private TextView todayFreeTitle;
    private GridView todayFreeGridView;
    /**
     * 男生爱看
     */
    private View todayBoyModule;
    private TextView todayBoyTitle;
    private TextView todayBoyTotal;
    private ListView todayBoyListView;
    private View boyViewAll;
    private View boyTitleBar;
    /**
     * 女生爱看
     */
    private View todayGirlModule;
    private TextView todayGirlTitle;
    private TextView todayGirlTotal;
    private ListView todayGirlListView;
    private View girlViewAll;
    private View girlTitleBar;

    /**
     * 以下为图文作品相关变量.
     */
    private View mRecommendEpub;
    private View mRecommendEpubTitle;
    private TextView mRecommendEpubCount;
    private RelativeLayout mRecommendEpubAll;
    private ListView mListViewEpub;
    private BookEpubAdapter mAdapterEpub;

    /**
     * 以下为明星作家相关变量.
     */
    private View mRecommendStarAuthor;
    private View mRecommendStarAuthorTitle;
    private TextView mRecommendStarAuthorCount;
    private RelativeLayout mRecommendStarAuthorAll;
    private ListView mListViewStarAuthor;
    private BookImageAdapter mAdapterStarAuthor;
    private ViewGroup mRecommendStarAuthorExtraRoot;

    /**
     * 以下为名人推荐相关变量.
     */
    private View mRecommendFamous;
    private View mRecommendFamousTitle;
    private RelativeLayout mRecommendFamousAll;
    private ListView mListViewFamous;
    private RecommendFamousAdapter mAdapterFamous;

    /**
     * 以下为热门分类相关变量.
     */
    private View mRecommendClassic;
    private View mRecommendClassicTitle;
    private RelativeLayout mRecommendClassicAll;
    private ListView mListViewClassic;
    private RecommendClassicAdapter mAdapterClassic;

    /**
     * 以下为选你喜欢相关变量.
     */
    private View mRecommendNew;
    private View mRecommendNewTitle;
    private TextView mRecommendNewCount;
    private RelativeLayout mRecommendNewAll;
    private ListView mListViewNew;
    private BookImageAdapter mAdapterNew;

    /**
     * 以下为免费专区相关变量.
     */
    private View mRecommendFree;
    private View mRecommendFreeTitle;
    private TextView mRecommendFreeCount;
    private RelativeLayout mRecommendFreeAll;
    private ListView mListViewFree;
    private BookImageAdapter mAdapterFree;

    /**
     * 以下为和阅读专区相关变量.
     */
    private View mCmreadCard;
    private View mCmreadCardTitle;
    private TextView mCmreadCardCount;
    private RelativeLayout mCmreadCardAll;
    private ListView mListViewCmreadCard;
    private BookImageAdapter mAdapterCmreadCard;

    /**
     * 以下为包畅读相关变量.
     */
    private View mRecommendMonth;
    private View mRecommendMonthTitle;
    private RelativeLayout mRecommendMonthAll;
    private ListView mListViewMonth;
    private RecommendMonthAdapter mAdapterMonth;

    /**
     * 和阅读专区
     */
    private View mCmreadCardView;
    private TextView mCmreadCardTextView;

    private View mCardTip;
    private TextView mCardTitle;

    private RecommendResult mResult;

    /**
     * 每一个card的头部需要
     */
    private ArrayList<CardPostion> cardPostions = new ArrayList<CardPostion>();

    @Override
    protected void init(Bundle savedInstanceState) {
        setContentView(R.layout.act_recommend);
        initTitle();
        initValue();
        initViews();
        initListener();

        requestData();
    }

    @Override
    public void onClickLeft() {
        finish();
    }

    @Override
    public void onClickRight() {
        Intent intent = new Intent();
        intent.setClass(this, SearchActivity.class);
        startActivity(intent);
    }

    @Override
    protected void retry() {
        requestData();
    }

    @Override
    protected void onResume() {
        if (mBannerImageAdapter != null && mBannerImageAdapter.getCount() > 1) {
            mViewFlow.startAutoFlow();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mBannerImageAdapter != null && mBannerImageAdapter.getCount() > 1) {
            mViewFlow.stopAutoFlow();
        }
        super.onPause();
    }

    @Override
    public void onTaskFinished(TaskResult taskResult) {
        if (taskResult.stateCode == HttpStatus.SC_OK && taskResult.retObj != null) {
            if (taskResult.retObj instanceof RecommendResult) {
                mResult = (RecommendResult) taskResult.retObj;
                updateData();
            }

            mProgressView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);

        } else {
            mProgressView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化标题
     */
    private void initTitle() {
        TextView middleV = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
        if (null != middleV) {
            middleV.setText(R.string.recomond_title);
            setTitleMiddle(middleV);
        }

        View leftV = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_backmain_left, null);
        setTitleLeft(leftV);

        // 顶部右侧添加进入搜索页的入口
        View rightV = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_search, null);
        setTitleRight(rightV);

    }

    private void initValue() {
        Bitmap dotReal = BitmapFactory.decodeResource(getResources(), R.drawable.divider_dot_real);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), dotReal);
        drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        drawable.setDither(true);

        mBannerImageAdapter = new ImageAdapter(this);

        mAdapterAuthor = new RecommendAuthorAdapter(this, drawable);
        mAdapterFamous = new RecommendFamousAdapter(this, drawable);
        mAdapterClassic = new RecommendClassicAdapter(this, drawable);
        mAdapterMonth = new RecommendMonthAdapter(this, drawable);

        mAdapterHot = new BookImageAdapter(this, getText(R.string.recommend_hot).toString());
        mAdapterStarAuthor = new BookImageAdapter(this, getText(R.string.recommend_star_author).toString());
        mAdapterEpub = new BookEpubAdapter(this, getText(R.string.recommend_star_author).toString());
        mAdapterNew = new BookImageAdapter(this, getText(R.string.recommend_new).toString());
        mAdapterFree = new BookImageAdapter(this, getText(R.string.recommend_free).toString());
        mAdapterCmreadCard = new BookImageAdapter(this, getText(R.string.recommend_cmread).toString());
    }

    private void initViews() {
        mProgressView = findViewById(R.id.recommend_progress);
        mErrorView = findViewById(R.id.recommend_error_layout);

        mScrollView = (ListenableScrollView) findViewById(R.id.recommend_scrollview);
        mScrollView.setOnScrollListener(mListener);

        // 顶部广告位
        mRecommendHeader = findViewById(R.id.layout_recommend_header);
        mViewFlow = (ViewFlow) mRecommendHeader.findViewById(R.id.viewflow);
        CircleFlowIndicator mIndicator = (CircleFlowIndicator) mRecommendHeader.findViewById(R.id.viewflowindic);
        mViewFlow.setFlowIndicator(mIndicator);
        mViewFlow.setAdapter(mBannerImageAdapter);

        // 请求焦点，避免数据返回后页面开始位置不正确的问题
        mRecommendHeader.requestFocus();

        // 好书推荐
        mRecommendGoodBook = findViewById(R.id.layout_recommend_good_book);
        mRecommendGoodBookContent = (TextView) mRecommendGoodBook.findViewById(R.id.good_book_content);

        // 热门作家
        mRecommendAuthor = findViewById(R.id.layout_recommend_author);
        mRecommendAuthorTitle = mRecommendAuthor.findViewById(R.id.recommend_list_title_layout);
        mRecommendAuthorCount = (TextView) mRecommendAuthor.findViewById(R.id.recommend_more);
        mRecommendAuthorCount.setVisibility(View.VISIBLE);
        mRecommendAuthorAll = (RelativeLayout) mRecommendAuthor.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendAuthor.findViewById(R.id.recommend_title)).setText(R.string.recommend_author);

        mListViewAuthor = (ListView) mRecommendAuthor.findViewById(R.id.recommend_list);
        mListViewAuthor.setAdapter(mAdapterAuthor);

        // 编辑推荐
        mRecommendHot = findViewById(R.id.layout_recommend_hot);
        mRecommendHotTitleLayout = mRecommendHot.findViewById(R.id.recommend_list_title_layout);
        mRecommendHotCount = (TextView) mRecommendHot.findViewById(R.id.recommend_more);
        mRecommendHotCount.setVisibility(View.VISIBLE);
        mRecommendHotAll = (RelativeLayout) mRecommendHot.findViewById(R.id.recommend_all_btn);
        mRecommendHotTitle = (TextView) mRecommendHot.findViewById(R.id.recommend_title);

        mListViewHot = (ListView) mRecommendHot.findViewById(R.id.recommend_list);
        mListViewHot.setAdapter(mAdapterHot);

        //今日限免
        todayFreeModule = findViewById(R.id.layout_recommend_toady_free);
        todayFreeTitle = (TextView) todayFreeModule.findViewById(R.id.recommend_title);
        todayFreeGridView = (GridView) todayFreeModule.findViewById(R.id.recommend_today_gridview);

        //男生爱看
        todayBoyModule = findViewById(R.id.layout_recommend_toady_boy);
        todayBoyTitle = (TextView) todayBoyModule.findViewById(R.id.recommend_title);
        todayBoyTotal = (TextView) todayBoyModule.findViewById(R.id.recommend_total);
        todayBoyListView = (ListView) todayBoyModule.findViewById(R.id.recommend_today_listview);
        boyViewAll = todayBoyModule.findViewById(R.id.recommend_all_btn);
        boyTitleBar = todayBoyModule.findViewById(R.id.recommend_title_bar);

        //女生爱看
        todayGirlModule = findViewById(R.id.layout_recommend_toady_girl);
        todayGirlTitle = (TextView) todayGirlModule.findViewById(R.id.recommend_title);
        todayGirlTotal = (TextView) todayGirlModule.findViewById(R.id.recommend_total);
        todayGirlListView = (ListView) todayGirlModule.findViewById(R.id.recommend_today_listview);
        girlViewAll = todayGirlModule.findViewById(R.id.recommend_all_btn);
        girlTitleBar = todayGirlModule.findViewById(R.id.recommend_title_bar);

        // 明星作家
        mRecommendStarAuthor = findViewById(R.id.layout_recommend_star_author);
        mRecommendStarAuthorTitle = mRecommendStarAuthor.findViewById(R.id.recommend_list_title_layout);
        mRecommendStarAuthorCount = (TextView) mRecommendStarAuthor.findViewById(R.id.recommend_more);
        mRecommendStarAuthorCount.setVisibility(View.VISIBLE);
        mRecommendStarAuthorAll = (RelativeLayout) mRecommendStarAuthor.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendStarAuthor.findViewById(R.id.recommend_title)).setText(R.string.books);
        mRecommendStarAuthorExtraRoot = (ViewGroup) mRecommendStarAuthor.findViewById(R.id.recommend_extra_layout);

        mListViewStarAuthor = (ListView) mRecommendStarAuthor.findViewById(R.id.recommend_list);
        mListViewStarAuthor.setAdapter(mAdapterStarAuthor);

        // 图文专区
        mRecommendEpub = findViewById(R.id.layout_recommend_epub);
        mRecommendEpubTitle = mRecommendEpub.findViewById(R.id.recommend_list_title_layout);
        mRecommendEpubCount = (TextView) mRecommendEpub.findViewById(R.id.recommend_more);
        mRecommendEpubCount.setVisibility(View.VISIBLE);
        mRecommendEpubAll = (RelativeLayout) mRecommendEpub.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendEpub.findViewById(R.id.recommend_title)).setText(R.string.recommend_epub);
        mListViewEpub = (ListView) mRecommendEpub.findViewById(R.id.recommend_list);
        mListViewEpub.setAdapter(mAdapterEpub);

        // 名人推荐
        mRecommendFamous = findViewById(R.id.layout_recommend_famous);
        mRecommendFamousTitle = mRecommendFamous.findViewById(R.id.recommend_list_title_layout);
        mRecommendFamousAll = (RelativeLayout) mRecommendFamous.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendFamous.findViewById(R.id.recommend_title)).setText(R.string.recommend_famous);

        mListViewFamous = (ListView) mRecommendFamous.findViewById(R.id.recommend_list);
        mListViewFamous.setAdapter(mAdapterFamous);

        // 热门分类
        mRecommendClassic = findViewById(R.id.layout_recommend_classification);
        mRecommendClassicTitle = mRecommendClassic.findViewById(R.id.recommend_list_title_layout);
        mRecommendClassicAll = (RelativeLayout) mRecommendClassic.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendClassic.findViewById(R.id.recommend_title)).setText(R.string.recommend_classic);

        mListViewClassic = (ListView) mRecommendClassic.findViewById(R.id.recommend_list);
        mListViewClassic.setAdapter(mAdapterClassic);

        // 选你喜欢
        mRecommendNew = findViewById(R.id.layout_recommend_new);
        mRecommendNewTitle = mRecommendNew.findViewById(R.id.recommend_list_title_layout);
        mRecommendNewCount = (TextView) mRecommendNew.findViewById(R.id.recommend_more);
        mRecommendNewCount.setVisibility(View.VISIBLE);
        mRecommendNewAll = (RelativeLayout) mRecommendNew.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendNew.findViewById(R.id.recommend_title)).setText(R.string.recommend_new);

        mListViewNew = (ListView) mRecommendNew.findViewById(R.id.recommend_list);
        mListViewNew.setAdapter(mAdapterNew);

        // 免费专区
        mRecommendFree = findViewById(R.id.layout_recommend_free);
        mRecommendFreeTitle = mRecommendFree.findViewById(R.id.recommend_list_title_layout);
        mRecommendFreeCount = (TextView) mRecommendFree.findViewById(R.id.recommend_more);
        mRecommendFreeCount.setVisibility(View.VISIBLE);
        mRecommendFreeAll = (RelativeLayout) mRecommendFree.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendFree.findViewById(R.id.recommend_title)).setText(R.string.recommend_free);

        mListViewFree = (ListView) mRecommendFree.findViewById(R.id.recommend_list);
        mListViewFree.setAdapter(mAdapterFree);

        // 和阅读专区
        mCmreadCard = findViewById(R.id.layout_cmread_card);
        mCmreadCardTitle = mCmreadCard.findViewById(R.id.recommend_list_title_layout);
        mCmreadCardCount = (TextView) mCmreadCard.findViewById(R.id.recommend_more);
        mCmreadCardCount.setVisibility(View.VISIBLE);
        mCmreadCardAll = (RelativeLayout) mCmreadCard.findViewById(R.id.recommend_all_btn);
        ((TextView) mCmreadCard.findViewById(R.id.recommend_title)).setText(R.string.recommend_cmread);

        mListViewCmreadCard = (ListView) mCmreadCard.findViewById(R.id.recommend_list);
        mListViewCmreadCard.setAdapter(mAdapterCmreadCard);

        // 包月畅读
        mRecommendMonth = findViewById(R.id.layout_recommend_mounth);
        mRecommendMonthTitle = mRecommendMonth.findViewById(R.id.recommend_list_title_layout);
        mRecommendMonthAll = (RelativeLayout) mRecommendMonth.findViewById(R.id.recommend_all_btn);
        ((TextView) mRecommendMonth.findViewById(R.id.recommend_title)).setText(R.string.recommend_month);

        // 和阅读专区
        mCmreadCardView = findViewById(R.id.layout_cmread_card);
        mCmreadCardTextView = (TextView) mCmreadCardView.findViewById(R.id.cmread_card_text);

        mListViewMonth = (ListView) mRecommendMonth.findViewById(R.id.recommend_list);
        mListViewMonth.setAdapter(mAdapterMonth);

        mCardTip = findViewById(R.id.card_tip);
        mCardTitle = (TextView) mCardTip.findViewById(R.id.card_title);
    }

    private void initListener() {

        mRecommendGoodBook.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 去好书推荐
                // enterGoodBookActivity();

                UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_GOOD_BOOK);
            }
        });

        mRecommendAuthorTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去热门作家
                AuthorRecommendActivity.launch(mContext);

                UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_AUTHOR);
            }
        });

        mRecommendAuthorAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去热门作家
                AuthorRecommendActivity.launch(mContext);

                UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_AUTHOR);
            }
        });

        mRecommendHotTitleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去热门推荐
                enterChildRecommendActivity(TYPE_HOT, mResult.getHotBook().getTitle(), Constants.PAGE_RECOMMEND_HOT);
            }
        });

        mRecommendHotAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去热门推荐
                enterChildRecommendActivity(TYPE_HOT, mResult.getHotBook().getTitle(), Constants.PAGE_RECOMMEND_HOT);
            }
        });

        mRecommendStarAuthorTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去作者作品列表
                enterAuthorBooksActivity();
            }
        });

        mRecommendStarAuthorAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去明星作家列表
                enterStarAuthorsActivity();
            }
        });

        mRecommendEpubTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 去epub专区
                enterEpubActivity(TYPE_EPUB, R.string.recommend_epub, Constants.PAGE_RECOMMEND_EPUB);

            }
        });

        mRecommendEpubAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 去epub专区
                enterEpubActivity(TYPE_EPUB, R.string.recommend_epub, Constants.PAGE_RECOMMEND_EPUB);
            }
        });

        mRecommendFamousTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去名人推荐
                FamousRecommendActivity.launch(RecommendActivity.this);

                UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_FAMOUS);
            }
        });

        mRecommendFamousAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去名人推荐
                FamousRecommendActivity.launch(RecommendActivity.this);

                UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_FAMOUS);
            }
        });

        mRecommendClassicTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去分类
                enterClassicPage();
            }
        });

        mRecommendClassicAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去分类
                enterClassicPage();
            }
        });

        mRecommendNewTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去选你喜欢选书
                enterChildRecommendActivity(TYPE_NEW, R.string.recommend_new, Constants.PAGE_RECOMMEND_NEW);
            }
        });

        mRecommendNewAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去选你喜欢选书
                enterChildRecommendActivity(TYPE_NEW, R.string.recommend_new, Constants.PAGE_RECOMMEND_NEW);
            }
        });

        mRecommendFreeTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去免费专区
                enterFreeRecommendActivity(TYPE_FREE, mResult.getFreeBook().getTitle(), Constants.PAGE_RECOMMEND_FREE);
            }
        });

        mRecommendFreeAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去免费专区
                enterFreeRecommendActivity(TYPE_FREE, mResult.getFreeBook().getTitle(), Constants.PAGE_RECOMMEND_FREE);
//				enterChildRecommendActivity(TYPE_FREE, R.string.recommend_free, Constants.PAGE_RECOMMEND_FREE);
            }
        });

        mRecommendMonthTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去包月厅
                enterMonthPage();
            }
        });

        mRecommendMonthAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 去包月厅
                enterMonthPage();
            }
        });
    }

    private void enterMonthPage() {
        Intent intent = new Intent(mContext, PaymentMonthDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivity(intent);

        UserActionManager.getInstance().recordEvent(Constants.PAGE_MONTH);
    }

    private void enterClassicPage() {
        MainTabActivity.launch(this, 2);
        UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_CLASSIC);
    }

    private void enterChildRecommendActivity(int type, int titleRes, String actionKey) {
        String url = String.format(ConstantData.URL_INDEX, type, "%s", ConstantData.PAGE_SIZE);
        CommonListActivity.launch(RecommendActivity.this, url, getString(titleRes), CommonListAdapter.TYPE_RECOMMEND);
        UserActionManager.getInstance().recordEvent(actionKey);
    }

    private void enterChildRecommendActivity(int type, String title, String actionKey) {
        String url = String.format(ConstantData.URL_INDEX, type, "%s", ConstantData.PAGE_SIZE);
        CommonListActivity.launch(RecommendActivity.this, url, title, CommonListAdapter.TYPE_RECOMMEND);
        UserActionManager.getInstance().recordEvent(actionKey);
    }

    private void enterFreeRecommendActivity(int type, String title, String actionKey) {
        String url = String.format(ConstantData.URL_INDEX, type, "%s", ConstantData.PAGE_SIZE);
        CommonListActivity.launch(RecommendActivity.this, url, title, CommonListAdapter.TYPE_RECOMMEND);
        UserActionManager.getInstance().recordEvent(actionKey);
    }

    private void enterEpubActivity(int type, int titleRes, String actionKey) {
        String url = String.format(ConstantData.URL_INDEX, type, "%s", ConstantData.PAGE_SIZE);
        CommonListActivity.launch(RecommendActivity.this, url, getString(titleRes), CommonListAdapter.TYPE_EPUB);
        UserActionManager.getInstance().recordEvent(actionKey);
    }

    // private void enterGoodBookActivity()
    // {
    // String url = PushHelper.getInstance().goodBookRecommendList();
    //
    // CommonListActivity.launch(RecommendActivity.this, url,
    // getString(R.string.good_book_recommend),
    // CommonListAdapter.TYPE_GOOD_BOOK);
    // }

    /**
     * Goto 明星作家作品列表
     */
    private void enterAuthorBooksActivity() {
        String authorId = String.valueOf(mResult.getRecommendAuthorListResult().getLists().get(0).map.get("author_id"));
        String authorName = String.valueOf(mResult.getRecommendAuthorListResult().getLists().get(0).map.get("author_name"));
        String url = String.format(ConstantData.RECOMMEND_AUTHORBOOKS, authorId, "%s", ConstantData.PAGE_SIZE);
        CommonListActivity.launch(RecommendActivity.this, url, authorName + "的作品", "明星作家", CommonListAdapter.TYPE_AUTHOR_BOOKS);

        UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_AUTHORBOOKS);
    }

    /**
     * Goto 明星作家==>人气作者
     */
    private void enterStarAuthorsActivity() {
        String url = String.format(ConstantData.RECOMMEND_AUTHORS, "%s", ConstantData.PAGE_SIZE);
        CommonListActivity.launch(RecommendActivity.this, url, mResult.getRecommendAuthorListResult().getTitle(), CommonListAdapter.TYPE_STAR_AUTHORS);

        UserActionManager.getInstance().recordEvent(Constants.PAGE_RECOMMEND_STARAUTHORS);
    }

    /**
     * 男生爱看，女生爱看
     */
    private void enterBoyAndGirlDetail(RecommendToday recommendToday, String actionKey) {
        int pageSize = ConstantData.PAGE_SIZE;
        String url = String.format(Locale.CHINA, ConstantData.URL_RECOMMEND_BoyAndGirl, recommendToday.getIndex_type(), "%s", pageSize);
        CommonListActivity.launch(RecommendActivity.this, url, recommendToday.getTitle(), recommendToday.getTitle(), CommonListAdapter.TYPE_BOYANDGIRL, pageSize);
        UserActionManager.getInstance().recordEvent(actionKey);
    }

    private void requestData() {
        if (HttpUtil.isConnected(this)) {
            mScrollView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            mScrollView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.VISIBLE);
            mProgressView.setVisibility(View.GONE);
            return;
        }

        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, ConstantData.URL_RECOMMEND);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);

        RequestTask task = new RequestTask(new RecommendParser());
        task.setTaskFinishListener(this);
        // 10分钟内请求，使用缓存
        task.executeWitchCache(params, 600000L);
    }

    private void updateData() {
        if (null == mResult) {
            return;
        }

        updateBannerData();// 顶部bannber
        updateGoodBookData();// 好书推荐
        updateAuthorData();

        // 更新图文混排专区
        updateEpub();
        updateRecommendData();// 编辑推荐
        upddateTodayFree();//今日限免
        updateTodayBoy();//男生爱看
        updateTodayGirl();//女生爱看
        updateStarAuthorData();// 人气作者
        updateFamousData();// 名人推荐
        updateClassicData();// 热门分类
        updateNewData();// 精品新书
        updateFreeData();// 免费专区
        updateCmreadData();// 和阅读专区
        updateMonthData();// 包月畅读

        // updateCmreadCardData();
    }

    private void updateTodayGirl() {
        final RecommendToday today = mResult.getRecommendGirl();
        if (today == null || today.getBooks() == null) {
            return;
        }
        final List<Book> books = today.getBooks();
        if (books.size() > 0) {
            todayGirlModule.setVisibility(View.VISIBLE);
            todayGirlTitle.setText(today.getTitle());
            String total = getResources().getString(R.string.recommend_book_count, today.getTotal());
            todayGirlTotal.setText(total);
            CommonAdapter<Book> bookCommonAdapter = new CommonAdapter<Book>(this, books, R.layout.recomment_today_boyandgirl_layout_item) {
                @Override
                public void convert(ViewHolder holder, Book book) {
                    holder.setimageUrl(R.id.book_cover, book.getDownloadInfo().getImageUrl());
                    holder.setText(R.id.book_title, book.getTitle());
                    holder.setText(R.id.book_author, book.getAuthor());
                    holder.setText(R.id.book_intro, book.getRecommendIntro());
                }
            };
            todayGirlListView.setAdapter(bookCommonAdapter);
            todayGirlListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String eventKey = "精选推荐_" + today.getTitle() + "_" + Util.formatNumber(position + 1);
                    BookDetailActivity.launch(RecommendActivity.this, books.get(position),null, eventKey,null);
                }
            });
            girlViewAll.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterBoyAndGirlDetail(today, Constants.CLICK_RECOMMEND_GIRL_LIKE);
                }
            });
            girlTitleBar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterBoyAndGirlDetail(today, Constants.CLICK_RECOMMEND_GIRL_LIKE);
                }
            });
        } else {
            todayGirlModule.setVisibility(View.GONE);
        }
    }

    private void updateTodayBoy() {
        final RecommendToday today = mResult.getRecommendBoy();
        if (today == null || today.getBooks() == null) {
            return;
        }
        final List<Book> books = today.getBooks();
        if (books.size() > 0) {
            todayBoyModule.setVisibility(View.VISIBLE);
            todayBoyTitle.setText(today.getTitle());
            String total = getResources().getString(R.string.recommend_book_count, today.getTotal());
            todayBoyTotal.setText(total);
            CommonAdapter<Book> bookCommonAdapter = new CommonAdapter<Book>(this, books, R.layout.recomment_today_boyandgirl_layout_item) {
                @Override
                public void convert(ViewHolder holder, Book book) {
                    holder.setimageUrl(R.id.book_cover, book.getDownloadInfo().getImageUrl());
                    holder.setText(R.id.book_title, book.getTitle());
                    holder.setText(R.id.book_author, book.getAuthor());
                    holder.setText(R.id.book_intro, book.getRecommendIntro());
                }
            };
            todayBoyListView.setAdapter(bookCommonAdapter);
            todayBoyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String eventKey = "精选推荐_" + today.getTitle() + "_" + Util.formatNumber(position + 1);
                    BookDetailActivity.launch(RecommendActivity.this, books.get(position), null,eventKey,null);
                }
            });
            boyViewAll.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterBoyAndGirlDetail(today, Constants.CLICK_RECOMMEND_BOY_LIKE);
                }
            });
            boyTitleBar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterBoyAndGirlDetail(today, Constants.CLICK_RECOMMEND_BOY_LIKE);
                }
            });
        } else {
            todayBoyModule.setVisibility(View.GONE);
        }
    }

    private void upddateTodayFree() {
        final RecommendTodayFree todayFree = mResult.getToadyFree();
        if (todayFree == null || todayFree.getBooks() == null) {
            return;
        }
        final List<Book> books = todayFree.getBooks();
        if (books.size() > 0) {
            todayFreeModule.setVisibility(View.VISIBLE);
            todayFreeTitle.setText(todayFree.getTitle());
            CommonAdapter<Book> adapter = new CommonAdapter<Book>(this, books, R.layout.recomment_today_layout_item) {
                @Override
                public void convert(ViewHolder holder, Book booksEntity) {
                    holder.setText(R.id.book_title, booksEntity.getTitle());
                    holder.setTextColor(R.id.book_price_now, Color.RED);
                    holder.setText(R.id.book_price_now, booksEntity.getRecommend_sub3());
                    ((TextView) holder.getView(R.id.book_price_normal)).getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中划线
                    holder.setText(R.id.book_price_normal, booksEntity.getRecommend_sub2());
                    holder.setimageUrl(R.id.book_cover, booksEntity.getDownloadInfo().getImageUrl());

                }
            };
            todayFreeGridView.setAdapter(adapter);
            todayFreeGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String eventKey = "精选推荐_" + todayFree.getTitle() + "_" + Util.formatNumber(position + 1);
                    BookDetailActivity.launch(RecommendActivity.this, books.get(position),null, eventKey,null);
                }
            });
        } else {
            todayFreeModule.setVisibility(View.GONE);
        }
    }

    /**
     * 明星作家==>人气作者
     */
    private void updateStarAuthorData() {
        RecommendAuthorListResult info = mResult.getRecommendAuthorListResult();
        if (info != null && info.code == 0 &&
                info.getTotal() > 0 && info.getLists() != null && info.getLists().size() > 0) {
            RecommendAuthorListItem firstItemInfo = info.getLists().get(0);
            if (firstItemInfo != null) {
                // 为“明星作家”添加作家信息模块
                View authorInfoView = View.inflate(this, R.layout.vw_recommend_author_star, null);
                mRecommendStarAuthorExtraRoot.addView(authorInfoView);

                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.list_divide_dot);
                BitmapDrawable divider = new BitmapDrawable(mContext.getResources(), bitmap);
                divider.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
                divider.setDither(true);

                String authorName = String.valueOf(firstItemInfo.map.get("author_name"));
                String authorIntro = String.valueOf(firstItemInfo.map.get("author_intro"));
                String profileUrl = String.valueOf(firstItemInfo.map.get("profile_url"));
                String booksTotal = String.valueOf(firstItemInfo.map.get("books_total"));
                ((TextView) authorInfoView.findViewById(R.id.recommend_author_star_title)).setText(info.getTitle());
                int _booksTotal = NumericHelper.parseInt(booksTotal, 0);

                ((TextView) authorInfoView.findViewById(R.id.recommend_author_star_name)).setText(authorName);
                ((TextView) authorInfoView.findViewById(R.id.recommend_author_star_desp)).setText(authorIntro);
                authorInfoView.findViewById(R.id.recommend_author_star_bottom_divider).setBackgroundDrawable(divider);
                ImageView authorImg = (ImageView) authorInfoView.findViewById(R.id.recommend_author_star_img);
                ImageLoader.getInstance().load2(profileUrl, authorImg, ImageLoader.TYPE_ROUND_PIC,
                        ImageLoader.getDefaultMainAvatar());

                authorInfoView.findViewById(R.id.recommend_author_star_title).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 去明星作家列表
                        enterStarAuthorsActivity();
                    }
                });
                authorInfoView.findViewById(R.id.recommend_author_star_item).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 该作家的作品列表
                        enterAuthorBooksActivity();
                    }
                });

                String recommendFreeCount = String.format(getResources().getString(R.string.recommend_book_count), _booksTotal);
                mRecommendStarAuthorCount.setText(recommendFreeCount);

                mRecommendStarAuthorExtraRoot.setVisibility(View.VISIBLE);
                mRecommendStarAuthor.setVisibility(View.VISIBLE);
                mRecommendStarAuthorAll.setVisibility(View.VISIBLE);

                List<Book> books = firstItemInfo.getItems();
                if (books != null && books.size() > 0) {
                    mRecommendStarAuthorTitle.setVisibility(View.VISIBLE);
                    mListViewStarAuthor.setVisibility(View.VISIBLE);

                    mAdapterStarAuthor.setData(getPartBooks(books));
                    mAdapterStarAuthor.setActionType(Constants.CLICK_RECOMMEND_STARAUTHOR);
                    mAdapterStarAuthor.notifyDataSetChanged();

                    Util.measureListViewHeight(mListViewStarAuthor);
                } else {
                    mRecommendStarAuthorTitle.setVisibility(View.GONE);
                    mListViewStarAuthor.setVisibility(View.GONE);
                    authorInfoView.findViewById(R.id.recommend_author_star_bottom_divider).setVisibility(View.GONE);
                }
            }
        }
    }

    private void updateCmreadCardData() {
        mCmreadCardView.setVisibility(View.GONE);
        mCmreadCardTextView.setOnClickListener(null);
        // mCmreadCardTextView
        List<RecommendBannerItem> banners = mResult.getCmreadCardBook().getItems();
        if (banners != null && banners.size() > 0) {
            final RecommendBannerItem banner = banners.get(0);
            if (banner != null) {
                final String tags = banner.getTags();
                if (!TextUtils.isEmpty(tags)) {
                    mCmreadCardTextView.setText(tags);
                    mCmreadCardTextView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            TopicActivity.launch(mContext, banner.getTags(), banner.getTopicId(),
                                    TopicActivity.TYPE_OLD);
                            UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_AD_TOPIC);
                        }
                    });
                    mCmreadCardView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 顶部banner
     */
    private void updateBannerData() {
        List<RecommendBannerItem> banners = mResult.getBannerBook().getItems();
        if (banners != null && banners.size() > 0) {
            mBannerImageAdapter.clearData();
            mBannerImageAdapter.addData(banners);

            // 添加包月Banner
            // RecommendBannerItem month = new RecommendBannerItem();
            // month.setType(RecommendBannerItem.BANNER_MONTH);
            // month.setImageResId(R.drawable.book_detail_arrow_close);
            // mBannerImageAdapter.add2End(month);

            // 添加员工推荐Banner
            // RecommendBannerItem staff = new RecommendBannerItem();
            // staff.setType(RecommendBannerItem.BANNER_STAFF);
            // staff.setImageResId(R.drawable.banner_staff);
            // mBannerImageAdapter.add2End(staff);

            mBannerImageAdapter.notifyDataSetChanged();

            // mViewFlow.startAutoFlow();
            mRecommendHeader.setVisibility(View.VISIBLE);

            // 横条图小于1个时，不自动滚动
            if (mBannerImageAdapter.getCount() <= 1) {
                mViewFlow.stopAutoFlow();
            } else {
                mViewFlow.startAutoFlow();
            }
        }
    }

    /**
     * 好书推荐
     */
    private void updateGoodBookData() {
        // String bookContent = mResult.getGoodBookResult().getBookContent();
        // if (!TextUtils.isEmpty(bookContent)) {
        // mRecommendGoodBookContent.setText(bookContent);
        // mRecommendGoodBook.setVisibility(View.VISIBLE);
        // }
    }

    private void updateAuthorData() {
        List<AuthorPageResult> authors = mResult.getAuthorResult().getDatas();
        if (null != authors && !authors.isEmpty()) {
            mAdapterAuthor.setList(authors);
            mAdapterAuthor.notifyDataSetChanged();

            Util.measureListViewHeight(mListViewAuthor);

            mRecommendAuthor.setVisibility(View.VISIBLE);
        }

        String count = String.format(getResources().getString(R.string.recommend_author_count), mResult
                .getAuthorResult().getCount());
        mRecommendAuthorCount.setText(count);
    }

    /**
     * 编辑推荐
     */
    private void updateRecommendData() {
        RecommendHotResult hotResult = mResult.getHotBook();
        List<Book> hotBooks = hotResult.getItems();
        if (hotBooks != null && hotBooks.size() > 0) {
            mAdapterHot.setData(getPartBooks(hotBooks));
            mAdapterHot.setActionType(Constants.CLICK_RECOMMEND_HOT);
            mAdapterHot.notifyDataSetChanged();

            Util.measureListViewHeight(mListViewHot);

            mRecommendHotAll.setVisibility(View.VISIBLE);
            mRecommendHot.setVisibility(View.VISIBLE);
        }
        mRecommendHotTitle.setText(hotResult.getTitle());
        String recommendHotCount = String.format(getResources().getString(R.string.recommend_book_count), mResult
                .getHotBook().getTotal());
        mRecommendHotCount.setText(recommendHotCount);
    }

    private void updateFamousData() {
        List<RecommendFamousItem> famous = mResult.getFamous().getDatas();
        if (null != famous && !famous.isEmpty()) {
            mAdapterFamous.setList(famous);
            mAdapterFamous.notifyDataSetChanged();

            Util.measureListViewHeight(mListViewFamous);

            mRecommendFamous.setVisibility(View.VISIBLE);
        }
    }

    private void updateClassicData() {
        RecommendCateResult recommendResult = mResult.getCates();
        List<RecommendCateItem> classic = recommendResult.getDatas();
        if (null != classic && !classic.isEmpty()) {
            mAdapterClassic.setList(classic);
            mAdapterClassic.notifyDataSetChanged();

            Util.measureListViewHeight(mListViewClassic);

            mRecommendClassic.setVisibility(View.VISIBLE);
            ((TextView) mRecommendClassic.findViewById(R.id.recommend_title)).setText(recommendResult.getTitle());
        } else {
            mRecommendClassic.setVisibility(View.GONE);
        }
    }

    // 精品新书
    private void updateNewData() {
        List<Book> newBooks = mResult.getNewBook().getItems();
        if (newBooks != null && newBooks.size() > 0) {
            mAdapterNew.setData(getPartBooks(newBooks));
            mAdapterNew.setActionType(Constants.CLICK_RECOMMEND_NEW);
            mAdapterNew.notifyDataSetChanged();

            Util.measureListViewHeight(mListViewNew);

            mRecommendNewAll.setVisibility(View.VISIBLE);
            mRecommendNew.setVisibility(View.VISIBLE);
        }
        String recommendNewCount = String.format(getResources().getString(R.string.recommend_book_count), mResult
                .getNewBook().getTotal());
        mRecommendNewCount.setText(recommendNewCount);
    }

    // 免费专区
    private void updateFreeData() {
        RecommendFreeResult recommendFreeResult = mResult.getFreeBook();
        List<Book> freeBooks = recommendFreeResult.getItems();
        if (freeBooks != null && freeBooks.size() > 0) {
            mAdapterFree.setData(getPartBooks(freeBooks));
            mAdapterFree.setActionType(Constants.CLICK_RECOMMEND_FREE);
            mAdapterFree.notifyDataSetChanged();

            Util.measureListViewHeight(mListViewFree);

            mRecommendFreeAll.setVisibility(View.VISIBLE);
            mRecommendFree.setVisibility(View.VISIBLE);
            ((TextView) mRecommendFree.findViewById(R.id.recommend_title)).setText(recommendFreeResult.getTitle());
        }
        String recommendFreeCount = String.format(getResources().getString(R.string.recommend_book_count), mResult
                .getFreeBook().getTotal());
        mRecommendFreeCount.setText(recommendFreeCount);
    }

    // 和阅读专区
    private void updateCmreadData() {
        RecomondBannerResult result = mResult.getCmreadCardBook();
        if (result != null) {
            List<RecommendBannerItem> bannerItems = result.getItems();
            if (bannerItems != null && bannerItems.size() > 0) {
                final RecommendBannerItem item = bannerItems.get(0);
                if (item != null) {
                    List<Book> cmreadBooks = item.getBooks();
                    if (cmreadBooks != null && cmreadBooks.size() > 0) {
                        mAdapterCmreadCard.setData(getPartBooks(cmreadBooks));
                        // 因为点击和阅读专区内的书籍时不需要再次发送CLICK_RECOMMEND_CMREAD统计事件
                        // 因此这里不设置Adapter的ActionType值
                        // mAdapterCmreadCard.setActionType(Constants.CLICK_RECOMMEND_CMREAD);
                        mAdapterCmreadCard.notifyDataSetChanged();

                        Util.measureListViewHeight(mListViewCmreadCard);

                        mCmreadCardAll.setVisibility(View.VISIBLE);
                        mCmreadCard.setVisibility(View.VISIBLE);
                    }

                    mCmreadCardTitle.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 去和阅读专区
                            enterCmreadCardTopicActivity(item);
                        }
                    });

                    mCmreadCardAll.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 去和阅读专区
                            enterCmreadCardTopicActivity(item);
                        }
                    });
                }

                // 标题优先读取服务器，没有则使用本地内置的
                String itemName = item.getItemName();
                if (TextUtils.isEmpty(itemName)) {
                    mAdapterCmreadCard.setChildRectName(itemName);
                    ((TextView) mCmreadCard.findViewById(R.id.recommend_title)).setText(itemName);

                    try {
                        // card里的layout修改对应修改这里
                        int topInterval = PixelUtil.dp2px(23);
                        int bottomInterval = PixelUtil.dp2px(43);
                        // 移除最后一个
                        if (cardPostions.size() > 0) {
                            cardPostions.remove(cardPostions.size() - 1);
                            CardPostion cpCmread = new CardPostion(mCmreadCard.getTop() + topInterval,
                                    mCmreadCard.getBottom() - bottomInterval, itemName);
                            cardPostions.add(cpCmread);
                        }
                    } catch (Exception e) {
                        LogUtil.d("cardPostions", "Exception : " + e.toString());
                    }
                }

                // 如果total返回0，则不做显示
                int total = item.getTotal();
                if (total > 0) {
                    String cmreadBookCount = String.format(getResources().getString(R.string.recommend_book_count),
                            total);
                    mCmreadCardCount.setText(cmreadBookCount);
                } else {
                    mCmreadCardCount.setVisibility(View.GONE);
                }
            }
        }
    }

    private void enterCmreadCardTopicActivity(final RecommendBannerItem item) {
        // 去和阅读专区
        // final String tags = item.getTags();
        // if (!TextUtils.isEmpty(tags)) {
        // mCmreadCardTextView.setText(tags);
        // mCmreadCardTextView.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        TopicActivity.launch(mContext, item.getTags(), item.getTopicId(), TopicActivity.TYPE_OLD);
        UserActionManager.getInstance().recordEvent(Constants.CLICK_RECOMMEND_CMREAD);
        // }
        // });
        // mCmreadCardView.setVisibility(View.VISIBLE);
        // }
    }

    // 包月畅读
    private void updateMonthData() {
        List<RecommendMonthItem> month = mResult.getMonth().getDatas();
        if (null != month && !month.isEmpty()) {
            mAdapterMonth.setList(month);
            mAdapterMonth.notifyDataSetChanged();

            Util.measureListViewHeight(mListViewMonth);

            mRecommendMonth.setVisibility(View.VISIBLE);
        }
    }

    private void updateEpub() {
        if (mResult.getEpubResult() != null) {
            List<Book> epubBooks = mResult.getEpubResult().getItems();
            if (epubBooks != null && epubBooks.size() > 0) {
                mAdapterEpub.setData(epubBooks);
                mAdapterEpub.setActionType(Constants.CLICK_RECOMMEND_EPUB);
                mAdapterEpub.notifyDataSetChanged();
                Util.measureListViewHeight(mListViewEpub);

                mRecommendEpub.setVisibility(View.VISIBLE);
                if (epubBooks.size() >= BookEpubAdapter.COLUMNS_NUM) {
                    mRecommendEpubAll.setVisibility(View.VISIBLE);
                } else {
                    mRecommendEpubAll.setVisibility(View.GONE);
                }

                // TODO:
                // mRecommendEpubTitleDivider.setVisibility(View.VISIBLE);
                // int resId = R.drawable.divider_dot_real;
                // Bitmap bitmap =
                // BitmapFactory.decodeResource(mContext.getResources(),
                // resId);
                // BitmapDrawable mDivider = new
                // BitmapDrawable(mContext.getResources(), bitmap);
                // mDivider.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
                // mDivider.setDither(true);
                // mRecommendEpubTitleDivider.setBackgroundDrawable(mDivider);

                String recommendEpubCount = String.format(getResources().getString(R.string.recommend_book_count), mResult
                        .getEpubResult().getTotal());
                mRecommendEpubCount.setText(recommendEpubCount);
                return;
            }
        }
        mRecommendEpub.setVisibility(View.GONE);
    }

    private List<Book> getPartBooks(List<Book> books) {
        List<Book> partBooks = new ArrayList<Book>();
        if (books != null && books.size() > 0) {
            int size = books.size() <= 6 ? books.size() : 6;
            for (int i = 0; i < size; i++) {
                partBooks.add(books.get(i));
            }
        }
        return partBooks;
    }

    /**
     * 确保cardPostions位置被初始化
     */
    private void ensureCardPostions() {
        if (cardPostions.size() > 0) {
            return;
        }
        // card里的layout修改对应修改这里
        int topInterval = PixelUtil.dp2px(23);
        int bottomInterval = PixelUtil.dp2px(43);


        CardPostion cpAuthor = new CardPostion(mRecommendAuthor.getTop() + topInterval, mRecommendAuthor.getBottom()//热门作家
                - bottomInterval, getString(R.string.recommend_author));
        cardPostions.add(cpAuthor);

        if (mResult.getHotBook() != null) {
            CardPostion cpHot = new CardPostion(mRecommendHot.getTop() + topInterval, mRecommendHot.getBottom()//编辑推荐
                    - bottomInterval, mResult.getHotBook().getTitle());
            cardPostions.add(cpHot);
        }

        if (mResult.getRecommendAuthorListResult() != null) {
            CardPostion cpStarAuthor = new CardPostion(mRecommendStarAuthor.getTop() + topInterval, mRecommendStarAuthor.getBottom()//人气作者
                    - bottomInterval, mResult.getRecommendAuthorListResult().getTitle());
            cardPostions.add(cpStarAuthor);
        }

        // 图文精装作品
        CardPostion cpEpub = new CardPostion(mRecommendEpub.getTop() + topInterval, mRecommendEpub.getBottom()
                - bottomInterval, getString(R.string.recommend_epub));
        cardPostions.add(cpEpub);

        CardPostion cpFamous = new CardPostion(mRecommendFamous.getTop() + topInterval, mRecommendFamous.getBottom()
                - bottomInterval, getString(R.string.recommend_famous));
        cardPostions.add(cpFamous);

        if (mResult.getCates() != null) {
            CardPostion cpCate = new CardPostion(mRecommendClassic.getTop() + topInterval, mRecommendClassic.getBottom()//热门分类
                    - bottomInterval, mResult.getCates().getTitle());
            cardPostions.add(cpCate);
        }

        CardPostion cpNew = new CardPostion(mRecommendNew.getTop() + topInterval, mRecommendNew.getBottom()
                - bottomInterval, getString(R.string.recommend_new));
        cardPostions.add(cpNew);

        CardPostion cpMonth = new CardPostion(mRecommendMonth.getTop() + topInterval, mRecommendMonth.getBottom()
                - bottomInterval, getString(R.string.recommend_month));
        cardPostions.add(cpMonth);

        if (mResult.getFreeBook() != null) {
            CardPostion cpFree = new CardPostion(mRecommendFree.getTop() + topInterval, mRecommendFree.getBottom()//免费试读
                    - bottomInterval, mResult.getFreeBook().getTitle());
            cardPostions.add(cpFree);
        }

        CardPostion cpCmread = new CardPostion(mCmreadCard.getTop() + topInterval, mCmreadCard.getBottom()
                - bottomInterval, getString(R.string.recommend_cmread));
        cardPostions.add(cpCmread);
        if (mResult.getToadyFree() != null) {//今日限免
            CardPostion todayFreeCard = new CardPostion(todayFreeModule.getTop() + topInterval, todayFreeModule.getBottom()
                    - bottomInterval, mResult.getToadyFree().getTitle());
            cardPostions.add(todayFreeCard);
        }
        if (mResult.getRecommendBoy() != null) {//男生爱看
            CardPostion boyCard = new CardPostion(todayBoyModule.getTop() + topInterval, todayBoyModule.getBottom()
                    - bottomInterval, mResult.getRecommendBoy().getTitle());
            cardPostions.add(boyCard);
        }
        if (mResult.getRecommendGirl() != null) {//今日限免
            CardPostion girlCard = new CardPostion(todayGirlModule.getTop() + topInterval, todayGirlModule.getBottom()
                    - bottomInterval, mResult.getRecommendGirl().getTitle());
            cardPostions.add(girlCard);
        }

    }

    private ListenableScrollView.OnScrollListener mListener = new ListenableScrollView.OnScrollListener() {
        @Override
        public void onScroll(int postion) {
            ensureCardPostions();
            CardPostion finded = null;
            for (CardPostion cp : cardPostions) {
                if (cp.inRange(postion)) {
                    finded = cp;
                    break;
                }
            }

            CharSequence text = mCardTitle.getText();
            if (finded != null && null != text) {
                final String origntitle = text.toString();
                final String findtitle = finded.getTitle();

                if (!origntitle.equals(findtitle)) {
                    Animation cardVisible = AnimationUtils.loadAnimation(mContext, R.anim.card_title_fade_in);
                    if (null != cardVisible) {
                        cardVisible.setAnimationListener(new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mCardTip.setVisibility(View.VISIBLE);
                                mCardTitle.setText(findtitle);
                            }
                        });
                        mCardTip.clearAnimation();
                        mCardTip.setAnimation(cardVisible);
                    }
                }
            } else {
                mCardTip.clearAnimation();
                mCardTitle.setText("");
                mCardTip.setVisibility(View.GONE);
            }
        }
    };
}