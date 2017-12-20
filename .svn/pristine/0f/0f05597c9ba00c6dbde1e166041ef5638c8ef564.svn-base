package com.sina.book.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.SeekBar;

/**
 * Seekbar 风格设置工具类
 * 
 * @author MarkMjw
 * 
 */
public class SeekbarUtil {

    /**
     * 设置SeekBar背景风格,无需改变的值设为
     * 
     * @param context
     *            Context
     * @param seekBar
     *            SeekBar对象
     * @param bgResId
     *            背景资源Id
     */
    public static void setBackground(Context context, SeekBar seekBar, int bgResId) {
        LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
        layerDrawable.setDrawableByLayerId(android.R.id.background, context.getResources()
                .getDrawable(bgResId));
        seekBar.setProgressDrawable(layerDrawable);
    }
    
    public static void setBackground(SeekBar seekBar, Drawable bgRes) {
        LayerDrawable layerDrawable = (LayerDrawable) seekBar
                .getProgressDrawable();
        layerDrawable.setDrawableByLayerId(android.R.id.background, bgRes);
        seekBar.setProgressDrawable(layerDrawable);
    }
    
    /**
     * 设置SeekBar进度条风格,无需改变的值设为
     * 
     * @param context
     *            Context
     * @param seekBar
     *            SeekBar对象
     * @param progressResId
     *            进度条资源Id
     */
    public static void setProgress(Context context, SeekBar seekBar, int progressResId) {
        LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
        layerDrawable.setDrawableByLayerId(android.R.id.progress, context.getResources()
                .getDrawable(progressResId));
        seekBar.setProgressDrawable(layerDrawable);
    }
}
