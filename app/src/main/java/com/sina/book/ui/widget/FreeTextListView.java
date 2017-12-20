package com.sina.book.ui.widget;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sina.book.R;
import com.sina.book.util.PixelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 文字的自由布局
 * 
 * @author YangZhanDong
 * 
 */
public class FreeTextListView extends LinearLayout {

    private Context mContext;
    private ArrayList<String> mString;

    private OnItemClickListener mListener;

    private float textSize = 13.33f;
    private int mTextPaddingHorizontal;
    private int mTextPaddingVertical;
    private int mTextMarginHorizontal;

    public FreeTextListView(Context context) {
        super(context);
        initView(context);
    }

    public FreeTextListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        if (isInEditMode()) {
            return;
        }

        this.setOrientation(VERTICAL);

        this.mContext = context;

        mTextPaddingHorizontal = PixelUtil.dp2px(10);
        mTextPaddingVertical = PixelUtil.dp2px(4);
        mTextMarginHorizontal = PixelUtil.dp2px(12);
    }

    private void refreshData() {
        removeAllViews();

        int position = 0;
        while (mString.size() > position) {
            LinearLayout layout = new LinearLayout(mContext);
            layout.setPadding(0, mTextPaddingVertical, 0, mTextPaddingVertical);
            layout.setOrientation(HORIZONTAL);

            int totalWidth = 0;

            for (int i = position; i < mString.size(); i++) {
                String text = mString.get(i);
                TextView textView = new TextView(mContext);
                textView.setTextSize(textSize);
                textView.setTextColor(mContext.getResources().getColor(R.color.book_detail_tag_font_color));
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(mTextPaddingHorizontal, mTextPaddingVertical, mTextPaddingHorizontal,
                        mTextPaddingVertical);

                textView.setBackgroundResource(R.drawable.selector_book_detail_tag_bg);
                textView.setText(text);
                textView.setOnClickListener(new OnClickListener(i));

                TextPaint paint = textView.getPaint();
                float width = paint.measureText(text);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                params.rightMargin = mTextMarginHorizontal;
                params.width = (int) (width + mTextPaddingHorizontal * 2);
                textView.setLayoutParams(params);

                // 若一个tag的宽度大于该view的宽度，则跳出循环
                int textWidth = params.width + mTextMarginHorizontal;
                if (textWidth > getWidth()) {
                    position = i + 1;
                    layout.addView(textView);
                    break;
                }

                totalWidth += params.width + mTextMarginHorizontal;

                if (totalWidth < getWidth()) {
                    position = i + 1;

                    layout.addView(textView);
                } else {
                    break;
                }
            }

            this.addView(layout);
        }
    }

    public void setString(List<String> string) {
        if (string == null) {
            return;
        }
        if (mString == null) {
            mString = new ArrayList<String>();
        }
        mString.clear();
        mString.addAll(string);
    }

    public void notifyChanged() {
        refreshData();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    private class OnClickListener implements View.OnClickListener {
        private int mPosition;

        public OnClickListener(int position) {
            this.mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(mPosition);
            }
        }
    }

    /**
     * 点击事件监听器
     * 
     * @author YangZhanDong
     * 
     */
    public interface OnItemClickListener {

        /**
         * 
         * @param position
         *            具体点击的某项
         */
        void onItemClick(int position);
    }

}