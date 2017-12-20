package com.sina.book.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;

public class MainBoxView extends RelativeLayout {

    private TextView mEmpty;
    private RelativeLayout mCardTitleLayout;
    private TextView mCardTitle;
    private View mBookLayout;
    private ImageView mHead;
    private TextView mHeadTitle;
    private TextView mHeadContent;
    private TextView mHeadReason;

    private Book mBook;

    public MainBoxView(Context context) {
        super(context);
        init(context);
    }

    public MainBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(getContext()).inflate(R.layout.vw_main_box_view, this);
        mEmpty = (TextView) findViewById(R.id.empty);
        mBookLayout = findViewById(R.id.book_layout);
        mHead = (ImageView) mBookLayout.findViewById(R.id.head);
        mHeadTitle = (TextView) mBookLayout.findViewById(R.id.head_title);
        mHeadContent = (TextView) mBookLayout.findViewById(R.id.head_content);
        mHeadReason = (TextView) mBookLayout.findViewById(R.id.head_reason);
        mCardTitleLayout = (RelativeLayout) findViewById(R.id.title_layout);
        mCardTitle = (TextView) findViewById(R.id.card_title);
        Bitmap dotReal = BitmapFactory.decodeResource(getResources(), R.drawable.divider_dot_real);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), dotReal);
        drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        drawable.setDither(true);
        findViewById(R.id.divide).setBackgroundDrawable(drawable);

        supportScale();
        setBook(null);
    }

    private void supportScale() {
        int h = getContext().getResources().getDisplayMetrics().heightPixels;
        float scale = getContext().getResources().getDisplayMetrics().densityDpi / 160;

        float defaultHdps = 533.3f;
        float percent = h / scale / defaultHdps;

        // 校对
        if (h > 800) {
            if (percent < 1) {
                percent = 1;
            }
        } else if (h <= 800 && h > 680) {
            if (percent >= 1f) {
                percent = 0.9f;
            }
        } else if (h <= 680 && h >= 480) {
            mHeadTitle.setSingleLine(true);
            if (percent >= 0.95f) {
                percent = 0.85f;
            }
        } else {
            mHeadTitle.setSingleLine(true);
            if (percent >= 0.9f) {
                percent = 0.8f;
            }
        }

        if (percent < 1) {
            float nowHeadSize = mHeadTitle.getTextSize() * percent;
            mHeadTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, nowHeadSize);

            float nowContentSize = mHeadContent.getTextSize() * percent;
            mHeadContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, nowContentSize);

            float nowReasonSize = mHeadReason.getTextSize() * percent;
            mHeadReason.setTextSize(TypedValue.COMPLEX_UNIT_PX, nowReasonSize);

            float nowCardSize = mCardTitle.getTextSize() * percent;
            mCardTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, nowCardSize);

            int paddingNow = (int) (mBookLayout.getPaddingBottom() * percent);
            mBookLayout.setPadding(paddingNow, paddingNow, paddingNow, paddingNow);
            mCardTitleLayout.setPadding(paddingNow, paddingNow, paddingNow, paddingNow);
        }
        
        if (percent > 1.2f) {
            percent = 1.2f;
        }
        int nowHeadWidth = (int) (mHead.getLayoutParams().width * percent);
        int nowHeadHeight = (int) (mHead.getLayoutParams().height * percent);

        mHead.getLayoutParams().width = nowHeadWidth;
        mHead.getLayoutParams().height = nowHeadHeight;
    }

    public void setTitle(String title, String emptyText) {
        mCardTitle.setText(title);
        mEmpty.setText(emptyText);
    }

    public void setBook(Book book) {
        setBook(book, false);
    }
    
    public void setBook(Book book,boolean showPraise) {
        this.mBook = book;
        if (mBook == null) {
            mEmpty.setVisibility(View.VISIBLE);
            mBookLayout.setVisibility(View.GONE);
        } else {
            mEmpty.setVisibility(View.GONE);
            mBookLayout.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().load(mBook.getDownloadInfo().getImageUrl(), mHead, ImageLoader.getDefaultPic());
            mHeadTitle.setText(mBook.getTitle());
            mHeadContent.setText(mBook.getAuthor());
            if (showPraise) {
                mHeadReason.setText(mBook.getPraiseNum()+"人推荐");
            } else {
                mHeadReason.setText(mBook.getComment());
            }
        }
    }
}
