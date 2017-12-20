package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;
import com.sina.book.util.Util;

public class SearchAdapter extends ListAdapter<Book> {

    private Context mContext;
    private ViewHolder mHolder;
    private ViewHolder1 mHolder1;

    public SearchAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position == 0) {
            convertView = createView1();
        }

        if (convertView == null || convertView.getTag() == null) {
            if (position != 0) {
                convertView = createView();
            }
        }

        mHolder = (ViewHolder) convertView.getTag();

        Book book = (Book) mDataList.get(position);

        if (position == 0) {

            if (null == mHolder1) {
                return null;
            }

            if (book.getDownloadInfo().getImageUrl() != null
                    && !book.getDownloadInfo().getImageUrl().contains("http://")) {
                book.getDownloadInfo().setImageUrl(null);
            }
            ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(),
                    mHolder1.mHeaderImg, ImageLoader.TYPE_COMMON_BOOK_COVER,
                    ImageLoader.getDefaultPic());

            mHolder1.mTitle.setText(book.getTitle());
            mHolder1.mAuthor.setText(String.format(
                    mContext.getResources().getString(R.string.search_author), book.getAuthor()));

            String text1 = "";
            String text2 = "";
            if (book.isUpdateChapter()) {
                text1 = mContext.getResources().getString(R.string.search_new);
                text2 = book.getBookUpdateChapterInfo().getTitle() + "/"
                        + Util.getTimeToDisplay(book.getBookUpdateChapterInfo().getUpdateTime());
            } else {
                text1 = mContext.getResources().getString(R.string.search_all);
                text2 = book.getNum() + "章";
            }
            mHolder1.mChapterInfo.setText(text1);
            SpannableStringBuilder chapterBuilder = new SpannableStringBuilder();
            chapterBuilder.append(text2);
            chapterBuilder.setSpan(new ForegroundColorSpan(0xFFE55939), 0, text2.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mHolder1.mChapterInfo.append(chapterBuilder);

            if (null != book.getContentTag()) {
                String text = String.format(mContext.getString(R.string.search_tag),
                        book.getContentTag());
                mHolder1.mTagInfo.setText(text);
            }

            if (Book.TREND_UP.equals(book.getFlag())) {
                mHolder1.mFlag.setImageResource(R.drawable.up);
            } else if (Book.TREND_AVERAGE.equals(book.getFlag())) {
                mHolder1.mFlag.setImageResource(R.drawable.right);
            } else if (Book.TREND_DOWN.equals(book.getFlag())) {
                mHolder1.mFlag.setImageResource(R.drawable.down);
            }

            mHolder1.mCost.setText(book.getBuyInfo().getStatusInfo());

        } else {
            mHolder.mNumber.setText(String.valueOf(position + 1));
            if (position == 1) {
                mHolder.mNumber.setBackgroundResource(R.drawable.number_2);
            } else if (position == 2) {
                mHolder.mNumber.setBackgroundResource(R.drawable.number_3);
            } else {
                mHolder.mNumber.setBackgroundResource(R.drawable.number_more);
            }

            if (Book.TREND_UP.equals(book.getFlag())) {
                mHolder.mFlag.setImageResource(R.drawable.up);
            } else if (Book.TREND_AVERAGE.equals(book.getFlag())) {
                mHolder.mFlag.setImageResource(R.drawable.right);
            } else if (Book.TREND_DOWN.equals(book.getFlag())) {
                mHolder.mFlag.setImageResource(R.drawable.down);
            }

            mHolder.mTitle.setText(book.getTitle());
            mHolder.mAuthor.setText(book.getAuthor());
        }

        return convertView;
    }

    private View createView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_search_item, null);

        ViewHolder holder = new ViewHolder();

        holder.mNumber = (TextView) itemView.findViewById(R.id.search_item_number);
        holder.mTitle = (TextView) itemView.findViewById(R.id.search_item_title);
        holder.mAuthor = (TextView) itemView.findViewById(R.id.search_item_author);
        holder.mFlag = (ImageView) itemView.findViewById(R.id.search_item_flag);

        itemView.setTag(holder);

        return itemView;
    }

    private View createView1() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_hot_book_item, null);

        mHolder1 = new ViewHolder1();

        mHolder1.mHeaderImg = (ImageView) itemView.findViewById(R.id.header_img);
        mHolder1.mTitle = (TextView) itemView.findViewById(R.id.title);
        mHolder1.mAuthor = (TextView) itemView.findViewById(R.id.author);
        mHolder1.mChapterInfo = (TextView) itemView.findViewById(R.id.chapter_info);
        mHolder1.mTagInfo = (TextView) itemView.findViewById(R.id.tag_info);
        mHolder1.mCost = (TextView) itemView.findViewById(R.id.cost_tv);
        mHolder1.mFlag = (ImageView) itemView.findViewById(R.id.flag_iv);

        return itemView;
    }

    @Override
    protected List<Book> createList() {
        return new ArrayList<Book>();
    }

    private class ViewHolder {
        public TextView mNumber;
        public TextView mTitle;
        public TextView mAuthor;
        public ImageView mFlag;
    }

    private class ViewHolder1 {
        public ImageView mHeaderImg;
        public TextView mTitle;
        public TextView mAuthor;
        public TextView mChapterInfo;
        public TextView mTagInfo;
        public TextView mCost;
        public ImageView mFlag;
    }

}
