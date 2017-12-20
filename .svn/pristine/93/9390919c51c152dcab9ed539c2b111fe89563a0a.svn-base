package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.image.ImageLoader;

public class PaymentMonthBookListAdapter extends ListAdapter<Book> {
    
    private Context mContext;
    private BitmapDrawable mDotHDrawable;

    private final String BOOK_CONTENT_PATTERN = "[\\s|　]";
    
    public PaymentMonthBookListAdapter(Context context) {
        this.mContext = context;
        Bitmap dotHBitmap = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.list_divide_dot);
        mDotHDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
        mDotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        mDotHDrawable.setDither(true);
    }
  
    public void clearList() {
        if (mDataList != null) {
            mDataList.clear();
        }
    }
       
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
       
        if (convertView == null || convertView.getTag() == null) {
            
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.vw_book_list_item, null);
            holder.mHeaderImg = (ImageView) convertView.findViewById(R.id.header_img);
            holder.mBookTitle = (TextView) convertView.findViewById(R.id.title);
            holder.mBookAuthor = (TextView) convertView.findViewById(R.id.author);
            holder.mBookInfo = (TextView) convertView.findViewById(R.id.book_info);
            ImageView listDivide = (ImageView) convertView.findViewById(R.id.list_divide);
            listDivide.setBackgroundDrawable(mDotHDrawable);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Book book = (Book) mDataList.get(position);
        
        if (book.getSuiteImageUrl() != null
                && !book.getSuiteImageUrl().contains("http://")) {
            book.setSuiteImageUrl(null);
        }
        ImageLoader.getInstance().load(book.getSuiteImageUrl(),
                holder.mHeaderImg, ImageLoader.TYPE_COMMON_BOOK_COVER,
                ImageLoader.getDefaultPic());

        holder.mBookTitle.setText(book.getTitle());
        holder.mBookAuthor.setText(mContext.getString(R.string.author) + book.getAuthor());
        holder.mBookInfo.setText(replaceAllSpace(book.getIntro()));
             
        return convertView;
    }

    @Override
    protected List<Book> createList() {
        return new ArrayList<Book>();
    }
    
    private String replaceAllSpace(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        String msgStr = sb.toString().replaceAll(BOOK_CONTENT_PATTERN, "");
        return msgStr;
    }
    
    private class ViewHolder {
        public ImageView mHeaderImg;
        public TextView mBookTitle;
        public TextView mBookAuthor;
        public TextView mBookInfo;
        
    }
    
}