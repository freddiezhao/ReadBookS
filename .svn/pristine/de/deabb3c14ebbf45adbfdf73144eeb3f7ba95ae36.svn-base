package com.sina.book.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.RecommendAuthorListItem;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.CommonListActivity;
import com.sina.book.ui.widget.XListView;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import com.sina.book.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 通用的书籍列表适配器
 *
 * @author MarkMjw
 * @see com.sina.book.ui.adapter.ListAdapter
 */
public class CommonListAdapter extends ExtraListAdapter {
    public static final String TYPE_HOT_VOTE = "hot_vote";
    public static final String TYPE_HOT_COMMENT = "hot_comment";
    // public static final String TYPE_HOT_BOOKS = "hot_books";
    // public static final String TYPE_HOT_NEW = "hot_new";
    // public static final String TYPE_HOT_READ_TOP = "read_top";
    // public static final String TYPE_FREE = "free";
    public static final String TYPE_RECOMMEND = "recommend";
    public static final String TYPE_PARTITION = "partition";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_AUTHOR = "author";
    public static final String TYPE_EPUB = "epub"; // 图文精装作品
    public static final String TYPE_GOOD_BOOK = "good_book";
    public static final String TYPE_STAR_AUTHORS = "star_authors";            // 明星作家列表
    public static final String TYPE_AUTHOR_BOOKS = "author_books";            // 明星作家作品列表
    public static final String TYPE_BOYANDGIRL = "boyandgirl";            // 男生爱看，女生爱看

    /**
     * 赞图标的高度、宽度
     */
    private final int PRAISE_NUM_BOUND = PixelUtil.dp2px(10.67f);
    /**
     * 赞图标的padding
     */
    private final int PADDING = PixelUtil.dp2px(8);

    private Context mContext;

    private ViewHolder mHolder;
    private StarItemViewHolder mStarItemHolder;
    private BitmapDrawable mDivider;
    private Date mNow;

    private String mType;

    /**
     * 构造方法
     *
     * @param context 上下文引用
     * @param type    数据类型
     */
    public CommonListAdapter(Context context, String type) {
        mContext = context;
        mType = type;

        mNow = new Date();
        decodeDivider();
    }

    private void decodeDivider() {
        int resId;
        if (TYPE_AUTHOR.equals(mType)) {
            // author类型使用实线
            resId = R.drawable.divider_dot_real;
        } else {
            resId = R.drawable.list_divide_dot;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
        mDivider = new BitmapDrawable(mContext.getResources(), bitmap);
        mDivider.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        mDivider.setDither(true);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (position == mDataList.size()) {// 获取更多信息
            if (!IsAdding()) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_generic_more, null);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_generic_loading, null);
            }
            return convertView;
        } else {
            // 明星作家
            if (TYPE_STAR_AUTHORS.equals(mType)) {
                if (convertView == null || convertView.getTag() == null) {
                    convertView = createStarItemView();
                }

                mStarItemHolder = (StarItemViewHolder) convertView.getTag();

                Book book = (Book) getItem(position);
                if (book != null) {
                    String suffix = "Ta的作品：";
                    String name = book.getTitle();
                    SpannableStringBuilder builder = new SpannableStringBuilder(suffix + name);
                    int color = mStarItemHolder.authorDesp.getCurrentTextColor();
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
                    builder.setSpan(colorSpan, 0, suffix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mStarItemHolder.bookName.setText(builder);
                }

                Object extraItemObj = getExtraItem(position);
                if (extraItemObj != null && extraItemObj instanceof RecommendAuthorListItem) {
                    RecommendAuthorListItem item = (RecommendAuthorListItem) extraItemObj;
                    final String authorId = String.valueOf(item.map.get("author_id"));
                    final String authorName = String.valueOf(item.map.get("author_name"));
                    String authorIntro = String.valueOf(item.map.get("author_intro"));
                    String profileUrl = String.valueOf(item.map.get("profile_url"));
                    mStarItemHolder.authorName.setText(authorName);
                    mStarItemHolder.authorDesp.setText(authorIntro);
                    ImageLoader.getInstance().load2(profileUrl, mStarItemHolder.authorImg, ImageLoader.TYPE_ROUND_PIC, ImageLoader.getDefaultMainAvatar());
                    // 进入作者作品列表
                    mStarItemHolder.authorRoot.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = String.format(ConstantData.RECOMMEND_AUTHORBOOKS, authorId, "%s", ConstantData.PAGE_SIZE);
                            CommonListActivity.launch(mContext, url, authorName + "的作品", "明星作家", CommonListAdapter.TYPE_AUTHOR_BOOKS);
                        }
                    });

                    // 因为没有给bookRoot设置点击事件，会把事件传递给整个ItemView
                    // 导致authorRoot的状态也发生了变化，产生了按下后的背景变化，因此这里设置点击事件，但是把处理事件时
                    // 调用performItemClick传递出去，Activity那边已经设置了onItemClickListener并做了处理。
                    final View _convertView = convertView;
                    final int _position = position + ((XListView) parent).getHeaderViewsCount();
                    mStarItemHolder.bookRoot.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            ((XListView) parent).performItemClick(_convertView, _position, _position);
                        }
                    });
                }
                if (position == getCount() - 1) {
                    convertView.setBackgroundResource(R.drawable.card_entire);
                } else {
                    convertView.setBackgroundResource(R.drawable.card_entire_top);
                }
            } else {
                // 其他
                if (convertView == null || convertView.getTag() == null) {
                    convertView = createView();
                }

                mHolder = (ViewHolder) convertView.getTag();
                Book book = (Book) getItem(position);

                updateCommonView(book);
                updateCostType(book);
                updateBookTrend(book);
                updateDifferentData(book);
            }
        }
        return convertView;
    }

    private void updateDifferentData(Book book) {
        int color = ResourceUtil.getColor(R.color.praise_num_color);

        if (TYPE_RECOMMEND.equals(mType) || TYPE_HOT_VOTE.equals(mType) || TYPE_AUTHOR_BOOKS.equals(mType)) {
            StringBuilder builder = new StringBuilder("推荐：");
            int start = builder.length();
            builder.append(book.getPraiseNum());
            int end = builder.length();
            builder.append("人");
            Spanned spanned = Util.highLight(builder, color, start, end);
            mHolder.chapterInfo.setText(spanned);

            showTags(book);
        } else if (TYPE_EPUB.equals(mType)) {
            String text = book.getType();
            mHolder.chapterInfo.setText(text);
            showTags(book);
        } else if (TYPE_HOT_COMMENT.equals(mType)) {
            StringBuilder builder = new StringBuilder("评论：");
            int start = builder.length();
            builder.append(book.getCommentNum());
            int end = builder.length();
            builder.append("次");
            Spanned spanned = Util.highLight(builder, color, start, end);
            mHolder.chapterInfo.setText(spanned);

            showTags(book);
        } else if (TYPE_BOYANDGIRL.equals(mType)) {
            mHolder.chapterInfo.setSingleLine(false);
            mHolder.chapterInfo.setMaxLines(2);
            mHolder.chapterInfo.setText(book.getIntro());
        } else {
            int payType = book.getBuyInfo().getPayType();
            if ((payType == Book.BOOK_TYPE_CHAPTER_VIP || payType == Book.BOOK_TYPE_FREE) && !TextUtils.isEmpty(book.getUpdateChapterNameServer())) {
                StringBuilder builder = new StringBuilder("最新：");
                int start = builder.length();
                builder.append(book.getUpdateChapterNameServer());
                int end = builder.length();
                Spanned spanned = Util.highLight(builder, color, start, end);
                mHolder.chapterInfo.setText(spanned);

                // 显示更新时间
                mHolder.updateInfo.setText("更新：" + Util.getTimeToDisplay(book.getUpdateTimeServer(), mNow));
                mHolder.updateInfo.setVisibility(View.VISIBLE);

            } else {
                StringBuilder builder = new StringBuilder("总共：");
                int start = builder.length();
                builder.append(book.getNum()).append("章");
                int end = builder.length();
                Spanned spanned = Util.highLight(builder, color, start, end);
                mHolder.chapterInfo.setText(spanned);

                showTags(book);
            }
        }
    }

    /**
     * 更新公用的View
     *
     * @param book
     */
    private void updateCommonView(Book book) {
        if (book.getDownloadInfo().getImageUrl() != null && !book.getDownloadInfo().getImageUrl().contains("http://")) {
            book.getDownloadInfo().setImageUrl(null);
        }
        ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), mHolder.headerImg, ImageLoader.TYPE_COMMON_BOOK_COVER, ImageLoader.getDefaultPic());

        mHolder.title.setText(book.getTitle());

        boolean skipMatchAuthor = false;
        if (TYPE_AUTHOR_BOOKS.equals(mType)) {
            String cateName = book.getBookCate();
            if (!TextUtils.isEmpty(cateName)) {
                mHolder.author.setVisibility(View.VISIBLE);
                mHolder.author.setText(cateName);
                skipMatchAuthor = true;
            }
        }

        if (!skipMatchAuthor) {
            if (book.getAuthor() != null && !book.getAuthor().equalsIgnoreCase("")) {
                mHolder.author.setVisibility(View.VISIBLE);
                mHolder.author.setText(mContext.getString(R.string.author) + book.getAuthor());
            } else {
                mHolder.author.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 显示书籍标签
     *
     * @param book
     */
    private void showTags(Book book) {
        // 获取书籍内容标签tag
        String tags = book.getContentTag();
        if (!TextUtils.isEmpty(tags)) {
            mHolder.updateInfo.setText("标签：" + tags);
            mHolder.updateInfo.setVisibility(View.VISIBLE);

        } else {
            mHolder.updateInfo.setVisibility(View.GONE);
        }
    }

    /**
     * 更新收费、连载等标签
     *
     * @param book
     */
    private void updateCostType(Book book) {
        // 如果为author类型，则显示书赞的人数
        if (TYPE_AUTHOR.equals(mType)) {
            mHolder.cost.setBackgroundResource(R.drawable.card_entire);

            Drawable drawable = ResourceUtil.getDrawable(R.drawable.book_detail_praise_num);
            drawable.setBounds(0, 0, PRAISE_NUM_BOUND, PRAISE_NUM_BOUND);
            mHolder.cost.setCompoundDrawables(drawable, null, null, null);
            mHolder.cost.setCompoundDrawablePadding(5);
            mHolder.cost.setPadding(PADDING, 0, PADDING, 0);

            mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_cost_color));
            mHolder.cost.setTextSize(9.33f);
            mHolder.cost.setText("" + book.getPraiseNum());
            mHolder.cost.setVisibility(View.VISIBLE);

        } else if (TYPE_GOOD_BOOK.equals(mType)) {
            // 初始化书籍缓存
            DownBookManager.getInstance().init();

            mHolder.cost.setBackgroundResource(R.drawable.selector_btn_bg_gray);
            mHolder.cost.setPadding(PADDING, 0, PADDING, 0);

            if (DownBookManager.getInstance().hasBook(book)) {
                mHolder.cost.setText(R.string.has_collected);
                mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_detail_btn_grayed_color));
                mHolder.cost.setEnabled(false);
            } else {
                mHolder.cost.setText(R.string.cloud_collect);
                mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_detail_btn_font_color));
                mHolder.cost.setEnabled(true);
            }

            ClickListener listener = new ClickListener(book, mHolder);
            mHolder.cost.setOnClickListener(listener);
        } else if (TYPE_BOYANDGIRL.equals(mType)) {
            mHolder.updateInfo.setVisibility(View.GONE);
            mHolder.cost.setVisibility(View.VISIBLE);
            mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_detail_btn_font_color));
            switch (book.getStatusType()) {
                case Book.STATUS_TYPE_FINISH:
                    mHolder.cost.setText("完结");
                    break;

                case Book.STATUS_TYPE_SERIAL:
                    mHolder.cost.setText("连载");
                    break;

                case Book.STATUS_TYPE_PAUSE:
                    mHolder.cost.setText("选载");
                    break;

                default:
                    mHolder.cost.setVisibility(View.GONE);
                    break;
            }

        } else {
            mHolder.cost.setTextSize(13.33f);
            mHolder.cost.setBackgroundDrawable(null);
            mHolder.cost.setCompoundDrawables(null, null, null, null);

            int payType = book.getBuyInfo().getPayType();

            if (payType == Book.BOOK_TYPE_FREE) {
                mHolder.cost.setVisibility(View.VISIBLE);
                mHolder.cost.setText("免费");

            } else if (book.isSuite()) {
                mHolder.cost.setVisibility(View.VISIBLE);
                mHolder.cost.setText("包月");

            } else if (payType == Book.BOOK_TYPE_CHAPTER_VIP) {
                mHolder.cost.setVisibility(View.VISIBLE);
                switch (book.getStatusType()) {
                    case Book.STATUS_TYPE_FINISH:
                        mHolder.cost.setText("完结");
                        break;

                    case Book.STATUS_TYPE_SERIAL:
                        mHolder.cost.setText("连载");
                        break;

                    case Book.STATUS_TYPE_PAUSE:
                        mHolder.cost.setText("选载");
                        break;

                    default:
                        mHolder.cost.setVisibility(View.GONE);
                        break;
                }

            } else if (payType == Book.BOOK_TYPE_VIP && book.getBuyInfo().getPrice() > 0) {
                mHolder.cost.setVisibility(View.VISIBLE);
                mHolder.cost.setText(book.getBuyInfo().getPrice() + mContext.getString(R.string.u_bi_name));

            } else {
                mHolder.cost.setVisibility(View.GONE);
            }

            // 如果是免费单独设置颜色
            if (payType == Book.BOOK_TYPE_FREE) {
                mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_free_color));
            } else {
                mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_cost_color));
            }
        }
    }

    /**
     * 更新书籍排行变化图
     *
     * @param book
     */
    private void updateBookTrend(Book book) {
        String flag = book.getFlag();
        if (!TextUtils.isEmpty(flag)) {
            if (Book.TREND_UP.equals(flag)) {
                mHolder.trend.setImageResource(R.drawable.up);
                mHolder.trend.setVisibility(View.VISIBLE);

            } else if (Book.TREND_DOWN.equals(flag)) {
                mHolder.trend.setImageResource(R.drawable.down);
                mHolder.trend.setVisibility(View.VISIBLE);

            } else if (Book.TREND_AVERAGE.equals(flag)) {
                mHolder.trend.setImageResource(R.drawable.right);
                mHolder.trend.setVisibility(View.VISIBLE);

            } else {
                mHolder.trend.setVisibility(View.GONE);
            }
        } else {
            mHolder.trend.setVisibility(View.GONE);
        }
    }

    public void clearList() {
        if (mDataList != null) {
            mDataList.clear();
        }
    }

    protected View createView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_new_book_item, null);

        ViewHolder holder = new ViewHolder();
        holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
        holder.title = (TextView) itemView.findViewById(R.id.title);
        holder.author = (TextView) itemView.findViewById(R.id.author);
        holder.chapterInfo = (TextView) itemView.findViewById(R.id.chapter_info);
        holder.updateInfo = (TextView) itemView.findViewById(R.id.update_info);
        holder.cost = (TextView) itemView.findViewById(R.id.cost_tv);
        holder.listDivide = itemView.findViewById(R.id.list_divide);
        holder.listDivide.setBackgroundDrawable(mDivider);
        holder.trend = (ImageView) itemView.findViewById(R.id.trend);

        if (TYPE_AUTHOR.equals(mType)) {
            itemView.setBackgroundResource(R.drawable.list_item_bg1);
        } else {
            itemView.setBackgroundResource(R.drawable.list_item_bg);
        }

        itemView.setTag(holder);
        return itemView;
    }

    protected View createStarItemView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_recommend_star_item, null);

        StarItemViewHolder holder = new StarItemViewHolder();
        holder.authorImg = (ImageView) itemView.findViewById(R.id.recommend_star_item_img);
        holder.authorName = (TextView) itemView.findViewById(R.id.recommend_star_item_authorname);
        holder.authorDesp = (TextView) itemView.findViewById(R.id.recommend_star_item_desp);
        holder.bookName = (TextView) itemView.findViewById(R.id.recommend_star_item_bookname);

        holder.authorRoot = itemView.findViewById(R.id.recommend_star_item_author_root);
        holder.bookRoot = itemView.findViewById(R.id.recommend_star_item_book_root);

        holder.middleDivider = itemView.findViewById(R.id.recommend_star_item_middle_divider);
        holder.middleDivider.setBackgroundDrawable(mDivider);

        itemView.setTag(holder);
        return itemView;
    }

    protected View createBoyAndGirlItemView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_new_book_item, null);

        ViewHolder holder = new ViewHolder();
        holder.headerImg = (ImageView) itemView.findViewById(R.id.header_img);
        holder.title = (TextView) itemView.findViewById(R.id.title);
        holder.author = (TextView) itemView.findViewById(R.id.author);
        holder.chapterInfo = (TextView) itemView.findViewById(R.id.chapter_info);
        holder.updateInfo = (TextView) itemView.findViewById(R.id.update_info);
        holder.cost = (TextView) itemView.findViewById(R.id.cost_tv);
        holder.listDivide = itemView.findViewById(R.id.list_divide);
        holder.listDivide.setBackgroundDrawable(mDivider);
        holder.trend = (ImageView) itemView.findViewById(R.id.trend);

        if (TYPE_AUTHOR.equals(mType)) {
            itemView.setBackgroundResource(R.drawable.list_item_bg1);
        } else {
            itemView.setBackgroundResource(R.drawable.list_item_bg);
        }

        itemView.setTag(holder);
        return itemView;
    }

    protected class ViewHolder {
        public ImageView headerImg;
        public TextView title;
        public TextView author;
        public TextView chapterInfo;
        public TextView updateInfo;
        public TextView cost;
        public View listDivide;
        public ImageView trend;
    }

    protected class StarItemViewHolder {
        public View authorRoot;
        public ImageView authorImg;
        public TextView authorName;
        public TextView authorDesp;

        public View middleDivider;

        public View bookRoot;
        public TextView bookName;
    }


    @Override
    protected List<Book> createList() {
        return new ArrayList<Book>();
    }

    private class ClickListener implements View.OnClickListener {

        private Book mBook;
        private ViewHolder mHolder;

        public ClickListener(Book book, ViewHolder holder) {
            this.mBook = book;
            this.mHolder = holder;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cost_tv:
                    if (mBook == null || mHolder == null) {
                        return;
                    }
                    addShelves();
                    break;

                default:
                    break;
            }
        }

        private void addShelves() {
            CloudSyncUtil.getInstance().add2CloudAndShelves(mContext, mBook, new ITaskFinishListener() {
                @Override
                public void onTaskFinished(TaskResult taskResult) {
                    mHolder.cost.setText(R.string.has_collected);
                    mHolder.cost.setTextColor(ResourceUtil.getColor(R.color.book_detail_btn_grayed_color));
                    mHolder.cost.setEnabled(false);
                }
            });
        }
    }

}
