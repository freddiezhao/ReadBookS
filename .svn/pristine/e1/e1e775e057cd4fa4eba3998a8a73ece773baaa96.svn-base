package com.sina.book.reader.selector;

import java.util.List;

import com.sina.book.R;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.util.PixelUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 文字选择器菜单View
 * 
 * @author MarkMjw
 * @date 2013-3-1
 */
public class SelectorPopMenu extends PopupWindow implements View.OnClickListener {
//    private static final String TAG = "SelectorMenuView";
    
    private Context mContext;
    
    private View mParentView;
    
    private LinearLayout mMenuLayout;
    
    private List<String> mMenuList;

    private ImageView mUpArrow;
    private ImageView mDownArrow;
    
    private IMenuClickListener mClickListener;
    
    private ReadStyleManager mStyleManager;
    
    public SelectorPopMenu(Context context) {
        super(context);
        
        initView(context);
    }

    public SelectorPopMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        initView(context);
    }

    public SelectorPopMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        initView(context);
    }
    
    private void initView(Context context) {
        mContext = context;

        View layout = LayoutInflater.from(mContext).inflate(R.layout.vw_text_selector_layout, null);

        mMenuLayout = (LinearLayout) layout.findViewById(R.id.text_selector_layout);

        mUpArrow = (ImageView) layout.findViewById(R.id.text_selector_up_arrow);
        mDownArrow = (ImageView) layout.findViewById(R.id.text_selector_down_arrow);
        
        setContentView(layout);
        
        setWidth(LayoutParams.WRAP_CONTENT);
        setHeight(PixelUtil.dp2px(80));

        // 使其不能获得焦点
        setFocusable(false);
        // 设置允许在外点击消失
        setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
        setBackgroundDrawable(new BitmapDrawable(mContext.getResources()));
        setAnimationStyle(R.style.PopWindowAnimation);
    }
    
    private boolean listEmpty() {
        if(null == mMenuList || mMenuList.isEmpty()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 初始化，必须调用
     * 
     * @param parentView
     * @param menus
     */
    public SelectorPopMenu init(View parentView, List<String> menus, ReadStyleManager styleManager) {
        mParentView = parentView;
        mMenuList = menus;
        mStyleManager = styleManager;

        if (listEmpty()) {
            return this;
        }
        
        updateViews();
        
        return this;
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        mMenuLayout.removeAllViewsInLayout();

        for (int i = 0; i < mMenuList.size(); i++) {
            TextView txtMenu = (TextView) LayoutInflater.from(mContext).inflate(
                    R.layout.vw_text_selector_menu, null);
            ImageView divider = (ImageView) LayoutInflater.from(mContext).inflate(
                    R.layout.vw_text_selector_menu_divider, null);

            txtMenu.setOnClickListener(this);
            txtMenu.setText(mMenuList.get(i));

            LayoutParams lParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            
            lParams.width = PixelUtil.dp2px(53.33f);
            txtMenu.setLayoutParams(lParams);
            
            int padding = PixelUtil.dp2px(5);
            txtMenu.setPadding(padding, 0, padding, 0);

            mMenuLayout.addView(txtMenu);

            if (i + 1 != mMenuList.size()) {
                LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                lp.height = PixelUtil.dp2px(34.67f);
                divider.setLayoutParams(lp);
                divider.setScaleType(ScaleType.FIT_XY);
                divider.setImageDrawable(mStyleManager.getDrawableFromIdentifier(mContext,
                        R.drawable.reading_menu_button_view_split));

                mMenuLayout.addView(divider);
            }
        }
    }
    
    /**
     * 更新View
     */
    public void updateViews() {
        if (null == mStyleManager) {
            return;
        }

        mMenuLayout.setBackgroundDrawable(mStyleManager.getDrawableFromIdentifier(mContext,
                R.drawable.reading_text_selector_view_bg));

        mUpArrow.setImageDrawable(mStyleManager.getDrawableFromIdentifier(mContext,
                R.drawable.reading_text_selector_view_up_arrow));

        mDownArrow.setImageDrawable(mStyleManager.getDrawableFromIdentifier(mContext,
                R.drawable.reading_text_selector_view_down_arrow));
        
        refreshData();
    }
    
    /**
     * 设置菜单点击事件监听器
     * 
     * @param clickListener
     */
    public void setMenuClickListener(IMenuClickListener clickListener) {
        this.mClickListener = clickListener;
    }
    
    /**
     * 显示
     * 
     * @param x
     * @param y
     * @param arrowUp 箭头是否向上
     */
    public void show(int x, int y, boolean arrowUp) {
        
        if (arrowUp) {
            mUpArrow.setVisibility(View.VISIBLE);
            mDownArrow.setVisibility(View.GONE);

        } else {
            mUpArrow.setVisibility(View.GONE);
            mDownArrow.setVisibility(View.VISIBLE);

        }
        
        showAtLocation(mParentView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, y
                - getHeight());
    }

    @Override
    public void onClick(View v) {
        
        if(v instanceof TextView) {
            TextView view = (TextView) v;
            int index = mMenuList.indexOf(view.getText());
            
            if(null != mClickListener && -1 != index) {
                mClickListener.onMenuClick(index, view);
            }
        }
    }
    
    /**
     * 菜单按钮点击事件接口
     * 
     * @author MarkMjw
     */
    public interface IMenuClickListener{
        
        /**
         * 菜单点击事件
         * 
         * @param index 菜单索引
         * @param view 菜单对应View
         */
        public void onMenuClick(int index, View view);
    }
}
