package com.sina.book.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.BookSummary;
import com.sina.book.reader.ReadStyleManager;

/**
 * 书摘列表Adapter
 * 
 * @author MarkMjw
 */
public class BookSummaryAdapter extends BaseAdapter {

    private Context mContext;
    private List<BookSummary> mDataList;

    public BookSummaryAdapter(Context context) {
        mContext = context;
    }

    public List<BookSummary> getDataList() {
        return mDataList;
    }

    public void setDataList(List<BookSummary> dataList) {
        this.mDataList = dataList;
    }

    public void clearDataList() {
        this.mDataList.clear();
    }

    @Override
    public int getCount() {
        if (mDataList != null) {
            return mDataList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mDataList != null) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = createView();
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        BookSummary item = mDataList.get(position);

        holder.mChapterTitle.setText(item.getChapterTitle());
        holder.mTime.setText(item.getDate() + " " + item.getTime());
        holder.mContent.setText(item.getContent());

        return convertView;
    }

    protected View createView() {
        View itemView = LayoutInflater.from(mContext).inflate(
                R.layout.vw_bookmark_item, null);
        ViewHolder holder = new ViewHolder();
        holder.mChapterTitle = (TextView) itemView
                .findViewById(R.id.chapter_title);
        holder.mTime = (TextView) itemView.findViewById(R.id.time);
        holder.mContent = (TextView) itemView.findViewById(R.id.markcontent);
        itemView.setTag(holder);

        ReadStyleManager readStyleManager = ReadStyleManager
                .getInstance(mContext);
        
        if (ReadStyleManager.READ_MODE_NIGHT == readStyleManager.getReadMode()) {
            itemView.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.mark_list_item_bg_night));
        } else {
            itemView.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.mark_list_item_bg));
        }

        int textColor = readStyleManager.getColorFromIdentifier(mContext,
                R.color.book_mark_chapter_color);
        holder.mChapterTitle.setTextColor(textColor);
        holder.mTime.setTextColor(textColor);
        
        int textContentColor = readStyleManager.getColorFromIdentifier(mContext,
                R.color.book_mark_font_color);
        holder.mContent.setTextColor(textContentColor);

        return itemView;
    }

    protected class ViewHolder {
        public TextView mChapterTitle;
        public TextView mTime;
        public TextView mContent;
    }
}
