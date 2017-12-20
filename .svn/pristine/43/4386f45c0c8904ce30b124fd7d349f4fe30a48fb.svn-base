package com.sina.book.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.data.Chapter;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;

import java.util.List;

/**
 * 目录列表Adapter
 * 
 * @author MarkMjw
 */
public class ChapterListAdapter extends BaseExpandableListAdapter {
    // private static final String TAG = "ChapterListAdapter";
	
	public static final String KEY_BOOK_CATALOG_ACTIVITY = "book_catalog_activity";

    private Context mContext;
    private List<String> mGroupList;
    private List<List<Chapter>> mDataList;
    private int mCurPos = -1;
    
    private String mType = "";
    
    /** 当前章节标志图的高度 */
    private final int CUR_MARK_HEIGHT = PixelUtil.dp2px(42.67f);
    /** 当前章节标志图的宽度 */
    private final int CUR_MARK_WIDTH = PixelUtil.dp2px(2.0f);

    public ChapterListAdapter(Context context) {
        mContext = context;
    }
    
    public ChapterListAdapter(Context context, String type) {
        mContext = context;
        mType = type;
    }

    public void setCurPos(int pos) {
        mCurPos = pos;
    }

    public List<String> getGroupList() {
        return mGroupList;
    }

    public void setGroupList(List<String> groupList) {
        mGroupList = groupList;
    }

    public List<List<Chapter>> getDataList() {
        return mDataList;
    }

    public void setDataList(List<List<Chapter>> dataList) {
        this.mDataList = dataList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (null != mDataList) {
            return mDataList.get(groupPosition).get(childPosition);
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition + groupPosition * 100;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = getChildView();
        }

        ChildViewHolder holder = (ChildViewHolder) convertView.getTag();
        Chapter item = mDataList.get(groupPosition).get(childPosition);

        if (mCurPos == childPosition + groupPosition * 100) {
            holder.mChapter.setTextColor(ResourceUtil.getColor(R.color.current_chapter_color));
            Drawable drawable = mContext.getResources().getDrawable(
                    R.drawable.current_chapter_mark_normal);
            drawable.setBounds(0, 0, CUR_MARK_WIDTH, CUR_MARK_HEIGHT);
            holder.mChapter.setCompoundDrawables(drawable, null, null, null);
            
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mChapter
                    .getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            holder.mChapter.setLayoutParams(params);
        } else {
			if (mType.equals(KEY_BOOK_CATALOG_ACTIVITY)) {
				holder.mChapter.setTextColor(mContext.getResources().getColor(
						R.color.book_chapter_info_font_color));
			} else {
        		holder.mChapter.setTextColor(ReadStyleManager.getInstance(mContext)
        				.getColorFromIdentifier(mContext, R.color.book_chapter_info_font_color));
        	}
            holder.mChapter.setCompoundDrawables(null, null, null, null);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mChapter
                    .getLayoutParams();
            params.setMargins(18, 0, 0, 0);
            holder.mChapter.setLayoutParams(params);
        }

        if (item.isVip() && !item.hasBuy() && item.getLength() <= 0) {
            holder.mChapter.setTextColor(Color.GRAY);
            holder.mChapter.setText("VIP" + item.getTitle().trim());
        } else {
            holder.mChapter.setText(item.getTitle().trim());
        }

        if (Chapter.NEW == item.getTag()) {
            holder.mIsNew.setVisibility(View.VISIBLE);
        } else {
            holder.mIsNew.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return null == mDataList ? 0 : mDataList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null == mGroupList ? null : mGroupList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return null == mGroupList ? 0 : mGroupList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = getGroupView();
        }

        GroupViewHolder holder = (GroupViewHolder) convertView.getTag();
        holder.mChapterGroup.setText(mGroupList.get(groupPosition).trim());
        holder.mChapterGroupIcon.setImageResource(isExpanded ? R.drawable.expand_y_normal
                : R.drawable.expand_n_normal);

        if (hasNewChapter(groupPosition)) {
            holder.mChapterGroup.setTextColor(ResourceUtil
                    .getColor(R.color.book_tag_radio_font_color_checked));
            holder.mChapterGroupNew.setVisibility(View.VISIBLE);
        } else {
            if (mType.equals(KEY_BOOK_CATALOG_ACTIVITY)) {
                holder.mChapterGroup.setTextColor(mContext.getResources().getColor(
                        R.color.book_chapter_title_font_color));
            } else {
                holder.mChapterGroup.setTextColor(ReadStyleManager.getInstance(mContext)
                        .getColorFromIdentifier(mContext, R.color.book_chapter_title_font_color));
            }
            holder.mChapterGroupNew.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * 是否含有更新章节
     *
     * @param groupPosition
     * @return
     */
    public boolean hasNewChapter(int groupPosition) {
        if (mDataList == null) {
            return false;
        }
        // 查找该组下面是否含有新增章节
        boolean hasNewChapter = false;
        for (Chapter chapter : mDataList.get(groupPosition)) {
            if (Chapter.NEW == chapter.getTag()) {
                hasNewChapter = true;
                break;
            }
        }
        return hasNewChapter;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private View getGroupView() {
        View groupView = LayoutInflater.from(mContext).inflate(R.layout.vw_chapter_group, null);
        GroupViewHolder holder = new GroupViewHolder();

        holder.mChapterGroup = (TextView) groupView.findViewById(R.id.chapter_group);
        holder.mChapterGroupIcon = (ImageView) groupView.findViewById(R.id.chapter_group_icon);
        holder.mChapterGroupNew = (ImageView) groupView.findViewById(R.id.chapter_group_new);

        ReadStyleManager readStyleManager = ReadStyleManager.getInstance(mContext);

        if (mType.equals(KEY_BOOK_CATALOG_ACTIVITY)) {
            groupView.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.mark_list_item_bg));
            
			holder.mChapterGroup.setTextColor(mContext.getResources().getColor(
					R.color.book_chapter_title_font_color));
        } else {
        	if (ReadStyleManager.READ_MODE_NIGHT == readStyleManager.getReadMode()) {
        		groupView.setBackgroundDrawable(mContext.getResources().getDrawable(
        				R.drawable.mark_list_item_bg_night));
        	} else {
        		groupView.setBackgroundDrawable(mContext.getResources().getDrawable(
        				R.drawable.mark_list_item_bg));
        	}
        	
        	holder.mChapterGroup.setTextColor(readStyleManager.getColorFromIdentifier(mContext,
        			R.color.book_chapter_title_font_color));
        }

        groupView.setTag(holder);
        return groupView;
    }

    private View getChildView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_chapter_item, null);
        ChildViewHolder holder = new ChildViewHolder();

        holder.mChapter = (TextView) itemView.findViewById(R.id.chapter);
        holder.mIsNew = (ImageView) itemView.findViewById(R.id.is_new);

        ReadStyleManager readStyleManager = ReadStyleManager.getInstance(mContext);

        if (mType.equals(KEY_BOOK_CATALOG_ACTIVITY)) {
            itemView.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.mark_list_item_bg));
            
        	holder.mChapter.setTextColor(mContext.getResources().getColor(
        			R.color.book_chapter_info_font_color));
        } else {
        	if (ReadStyleManager.READ_MODE_NIGHT == readStyleManager.getReadMode()) {
        		itemView.setBackgroundDrawable(mContext.getResources().getDrawable(
        				R.drawable.mark_list_item_bg_night));
        	} else {
        		itemView.setBackgroundDrawable(mContext.getResources().getDrawable(
        				R.drawable.mark_list_item_bg));
        	}
        	
        	holder.mChapter.setTextColor(readStyleManager.getColorFromIdentifier(mContext,
        			R.color.book_chapter_info_font_color));
        }

        itemView.setTag(holder);
        return itemView;
    }

    private class GroupViewHolder {
        public TextView mChapterGroup;
        public ImageView mChapterGroupIcon;
        public ImageView mChapterGroupNew;
    }

    private class ChildViewHolder {
        public TextView mChapter;
        public ImageView mIsNew;
    }
}
