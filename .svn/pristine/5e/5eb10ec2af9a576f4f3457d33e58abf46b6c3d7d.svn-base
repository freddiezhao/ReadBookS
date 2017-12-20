package com.sina.book.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.data.Book;
import com.sina.book.image.IImageLoadListener;
import com.sina.book.image.ImageLoader;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.StorageUtil;

import java.io.File;
import java.io.IOException;

/**
 * 将最近阅读这部分提炼成控件<br>
 * 
 * @author Tsimle
 * 
 */
public class LastReadBookListView extends RelativeLayout {
    /**
     * Gesture 参数
     */
    private static final int SWIPE_MAX_OFF_PATH = 250;
    /**
     * 这个参数根据布局文件调整
     */
    private static final float LAST_READING_PERCENT = 0.34f;
    private static final float BOOK_HEIGHT_WIDTH_SCALE = 0.73f;

    private GestureDetector mDetector = new GestureDetector(getContext(), new DefinedGestureListener());
    private BookDataAdapter mAdapter;

    private int mMinimumFlingVelocity;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mBookWidth;

    private ViewHolder[] mViewHolders;

    private RelativeLayout mTotalLayout;
    private RelativeLayout mBookContainer;
    private View mEmptyView;

    public LastReadBookListView(Context context) {
        super(context);
        init(context);
    }

    public LastReadBookListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LastReadBookListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if(isInEditMode()) {
            return;
        }

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        LayoutInflater.from(getContext()).inflate(R.layout.vw_last_read_book_lv, this);
        mEmptyView = findViewById(R.id.last_read_nobook);
        mTotalLayout = (RelativeLayout) findViewById(R.id.total_layout);
        mBookContainer = (RelativeLayout) findViewById(R.id.book_container);

        mDisplayWidth = screenWidth - mTotalLayout.getPaddingLeft() - mTotalLayout.getPaddingRight();
        mDisplayHeight = (int) ((screenHeight - PixelUtil.dp2px(15)) * LAST_READING_PERCENT);
        mBookWidth = (int) (mDisplayHeight * BOOK_HEIGHT_WIDTH_SCALE);
    }

    public void setBookDataAdapter(BookDataAdapter bookDataAdapter) {
        mAdapter = bookDataAdapter;
        int viewTotalCount = mAdapter.getViewTotalCount();

        int bookMargin = mBookWidth - (mBookWidth * viewTotalCount - mDisplayWidth) / (viewTotalCount - 1);

        mViewHolders = new ViewHolder[viewTotalCount];
        for (int i = viewTotalCount - 1; i >= 0; i--) {
            ViewHolder holder = new ViewHolder();

            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.vw_main_last_book, null);
            holder.totalView = itemView;

            holder.bookImageView = (ImageView) itemView.findViewById(R.id.book_img);
            holder.bookTitleTextView = (TextView) itemView.findViewById(R.id.book_title);
            holder.bookNewChapter = (TextView) itemView.findViewById(R.id.book_new_chapter);
            holder.bookTipImageView = (ImageView) itemView.findViewById(R.id.book_tip);
            holder.bookImageShadow = itemView.findViewById(R.id.book_img_shadow);
            holder.bookImageShadow.setOnClickListener(new LastReadBookClickListener(i));
            mViewHolders[i] = holder;

            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(mBookWidth, LayoutParams.MATCH_PARENT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            if (i == viewTotalCount - 1) {
                rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            } else if (i == 0) {
                rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            } else {
                rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rl.rightMargin = (viewTotalCount - 1 - i) * bookMargin;
            }
            mBookContainer.addView(itemView, rl);
        }
    }

    public void notifyDataSetChanged() {
        int nowcount = mAdapter.getCount();
        if (nowcount > mAdapter.getViewTotalCount()) {
            nowcount = mAdapter.getViewTotalCount();
        }

        if (nowcount == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mBookContainer.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mBookContainer.setVisibility(View.VISIBLE);
            for (int i = 0; i < nowcount; i++) {
                final View lastReadItem = mViewHolders[i].totalView;
                final ImageView bookImageView = mViewHolders[i].bookImageView;
                final TextView bookTitleTextView = mViewHolders[i].bookTitleTextView;
                final TextView bookNewChapter = mViewHolders[i].bookNewChapter;
                final ImageView bookTipImageView = mViewHolders[i].bookTipImageView;

                lastReadItem.setVisibility(View.VISIBLE);
                bookImageView.setVisibility(View.VISIBLE);
                bookTitleTextView.setVisibility(View.VISIBLE);
                bookNewChapter.setVisibility(View.VISIBLE);
                bookTipImageView.setVisibility(View.VISIBLE);

                final DownBookJob job = mAdapter.getItem(i);
                final Book book = job.getBook();
                String imgUrl = book.getDownloadInfo().getImageUrl();
                if (imgUrl != null && imgUrl.startsWith(Book.LOCAL_PATH_IMG)) {
                    bookTitleTextView.setText("");
                    ImageLoader.getInstance().load(imgUrl, bookImageView, ImageLoader.getDefaultLocalBookPic());
                } else if (imgUrl != null && imgUrl.startsWith(Book.SDCARD_PATH_IMG)) {
                    bookTitleTextView.setText(book.getTitle());
                    bookImageView.setImageBitmap(ImageLoader.getDefaultLocalBookPic());
                } else {
                    bookTitleTextView.setText(book.getTitle());
                    ImageLoader.getInstance().load(imgUrl, mViewHolders[i].bookImageView, ImageLoader.TYPE_BIG_PIC,
                            ImageLoader.getDefaultLocalBookPic(), new IImageLoadListener() {

                                @Override
                                public void onImageLoaded(Bitmap bm, ImageView imageView, boolean loadSuccess) {
                                    if (loadSuccess) {
                                        bookTitleTextView.setText("");
                                    } else {
                                        // 如果imageurl加载失败，默认书皮上会显示书籍标题
                                        bookTitleTextView.setText(book.getTitle());
                                    }
                                }
                            });
                }

                if (Math.abs(book.getDownloadInfo().getProgress() - 1.0) < 0.0001
                        && job.getState() == DownBookJob.STATE_FINISHED) {
                    // 下载成功
                    String filePath = book.getDownloadInfo().getFilePath();
                    if (filePath.contains("file")) {
                        try {
                            filePath = StorageUtil.copyFile(filePath.substring(filePath.lastIndexOf('/')));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    File bookFile = new File(filePath);
                    if (!bookFile.exists() || !bookFile.isFile() || !bookFile.canRead() || bookFile.length() == 0) {
                        book.getDownloadInfo().setFileSize(0);
                    } else {
                        book.getDownloadInfo().setFileSize(bookFile.length());
                    }
                    if (book.getTag() == Book.IS_NEW) {
                        bookTipImageView.setVisibility(View.VISIBLE);
                        bookTipImageView.setBackgroundResource(R.drawable.new_icon);
                    } else {
                        bookTipImageView.setVisibility(View.GONE);
                    }
                    if (book.getUpdateChaptersNum() > 0) {
                        bookNewChapter.setVisibility(View.VISIBLE);
                        bookNewChapter.setText("" + book.getUpdateChaptersNum());
                    } else {
                        bookNewChapter.setVisibility(View.GONE);
                        bookNewChapter.setText("0");
                    }
                } else {
                    bookNewChapter.setVisibility(View.GONE);
                    bookNewChapter.setText("0");
                    bookTipImageView.setVisibility(View.GONE);
                }
            }

            for (int j = nowcount; j < mAdapter.getViewTotalCount(); j++) {
                mViewHolders[j].totalView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private class DefinedGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mAdapter == null || Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            // right to left swipe
            if (e1.getX() - e2.getX() > 30 && Math.abs(velocityX) > mMinimumFlingVelocity) {
                mAdapter.onEnterBookhome();
                return true;
            }

            return false;
        }
    }

    private class ViewHolder {
        View totalView;
        ImageView bookImageView;
        TextView bookTitleTextView;
        TextView bookNewChapter;
        ImageView bookTipImageView;
        View bookImageShadow;
    }

    private class LastReadBookClickListener implements View.OnClickListener {

        private int item;

        public LastReadBookClickListener(int item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemClick(item);
        }
    }

    public static interface BookDataAdapter {
        public void onEnterBookhome();

        public int getViewTotalCount();

        public int getCount();

        public DownBookJob getItem(int postion);

        public void onItemClick(int postion);
    }
}
