package com.sina.book.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.Book;
import com.sina.book.data.PartitionItem;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.widget.EllipsizeTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类页数据Adapter
 *
 * @author MarkMjw
 * @date 2013-4-8
 */
public class PartitionAdapter extends BaseAdapter {
    public static final String DEFALUT_PARTITION = "default";

    private List<PartitionItem> mDatas;
    private Context mContext;

    private BitmapDrawable mDividerV;
    private BitmapDrawable mDividerH;

    public PartitionAdapter(Context context) {
        this.mContext = context;

        mDatas = new ArrayList<PartitionItem>();

        decodeDivider();

    }

    private void decodeDivider() {
        Bitmap dividerV = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.partition_divider_line_v);
        mDividerV = new BitmapDrawable(mContext.getResources(), dividerV);
        mDividerV.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        mDividerV.setDither(true);

        Bitmap dividerH = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.partition_divider_line_h);
        mDividerH = new BitmapDrawable(mContext.getResources(), dividerH);
        mDividerH.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        mDividerH.setDither(true);
    }

    public void setList(List<PartitionItem> data) {
        if (null == data || data.isEmpty()) {
            return;
        }

        mDatas = data;

        int size = mDatas.size();
        if (size % 2 != 0) {
            PartitionItem item = new PartitionItem();
            item.setName(DEFALUT_PARTITION);
            mDatas.add(item);
        }
    }

    @Override
    public int getCount() {
        if (mDatas == null || mDatas.size() == 0) {
            return 0;
        } else {
            return mDatas.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (position < mDatas.size() && position >= 0) {
            return mDatas.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || null == convertView.getTag()) {
            convertView = createView();
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        PartitionItem item = (PartitionItem) getItem(position);

        if (item.getIsFavorite()) {
            holder.choosedImage.setVisibility(View.VISIBLE);
        } else {
            holder.choosedImage.setVisibility(View.GONE);
        }

        // 换背景
        if ((position / 2) % 2 != 0) {
            holder.mLayout.setBackgroundResource(R.drawable.partition_light_color);

        } else {
            holder.mLayout.setBackgroundResource(R.drawable.partition_dark_color);
        }

        // 隐藏右侧divider
        if (position % 2 == 0) {
            holder.dividerV.setVisibility(View.VISIBLE);
        } else {
            holder.dividerV.setVisibility(View.GONE);
        }

        holder.dividerV.setImageDrawable(mDividerV);
        holder.dividerH.setImageDrawable(mDividerH);

        // 如果是默认值则隐藏
        if (DEFALUT_PARTITION.equalsIgnoreCase(item.getName())) {
            holder.typeName.setText(null);
            holder.bookTitle1.setText(null);
            holder.bookTitle2.setText(null);
            holder.bookCover.setBackgroundDrawable(null);
            holder.bookCover.setImageDrawable(null);

        } else {
            holder.typeName.setText(item.getName());

            List<Book> list = item.getBookLists();
            for (int i = 0; i < list.size(); i++) {
                String title = list.get(i).getTitle();
                switch (i) {
                    case 0:
                        ImageLoader.getInstance().load(list.get(i).getDownloadInfo().getImageUrl(), holder.bookCover,
                                ImageLoader.TYPE_SMALL_PIC, null);
                        break;

                    case 1:
                        holder.bookTitle1.setText(title);
                        break;

                    case 2:
                        holder.bookTitle2.setText(title);
                        break;
                }
            }
        }

        return convertView;
    }

    private View createView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.vw_partition_item, null);

        ViewHolder holder = new ViewHolder();
        holder.mLayout = (RelativeLayout) view.findViewById(R.id.partition_item_layout);
        holder.bookCover = (ImageView) view.findViewById(R.id.partition_img);
        holder.typeName = (TextView) view.findViewById(R.id.partition_type);
        holder.bookTitle1 = (EllipsizeTextView) view.findViewById(R.id.partition_book_1);
        holder.bookTitle2 = (EllipsizeTextView) view.findViewById(R.id.partition_book_2);
        holder.dividerV = (ImageView) view.findViewById(R.id.partition_divider_v);
        holder.dividerH = (ImageView) view.findViewById(R.id.partition_divider_h);
        holder.choosedImage = (ImageView) view.findViewById(R.id.choosed_image);
        view.setTag(holder);

        return view;
    }

    private class ViewHolder {
        public RelativeLayout mLayout;
        public ImageView bookCover;
        public TextView typeName;
        public EllipsizeTextView bookTitle1;
        public EllipsizeTextView bookTitle2;
        public ImageView dividerV;
        public ImageView dividerH;
        public ImageView choosedImage;
    }

}
