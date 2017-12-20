package com.sina.book.util;

import android.content.Context;
import com.sina.book.R;
import com.sina.book.SinaBookApplication;

import java.util.HashMap;

/**
 * 阅读页面不同字体的各个Dimension值<br>
 * 注意：该类仅适用于阅读界面
 * 
 * @author MarkMjw
 *
 */
public class ReadPageDimenUtil {
    /** 标题上部距离顶部区域 */
    public static final String TITILE_MARGIN_TOP = "title_margin_top";
    
    /** 书签标志距离右边区域 */
    public static final String BOOK_MARK_MARGIN_RIGHT = "book_mark_margin_right";
    
    /** 底部时间、页数文字距离底部的区域 */
    public static final String BOTTOM_MARGIN_BOTTOM = "bottom_text_margin_bottom";
    
    
    /** 正文底部距离底部文字距离 */
    public static final String CONTENT_MARGIN_BOTTOM = "content_margin_bottom";
    
    /** 正文左右间距 */
    public static final String CONTENT_MARGIN_LEFT_RIGHT = "content_margin_left_right";
    
    /** 正文顶部距离标题区域 */
    public static final String CONTENT_MARGIN_TOP = "content_margin_top";
    
    /** 正文行间距 */
    public static final String CONTENT_LINE_SPACE = "content_line_space";
    
    
    /** 标题文字字体大小 */
    public static final String TITILE_FONT_SIZE = "title_font_size";
    
    private static HashMap<Float, HashMap<String, Integer>> mDimensMap = 
            new HashMap<Float, HashMap<String, Integer>>();
    
    private static Context mContext = SinaBookApplication.gContext;
    
    static {
        String[] value12 = getStringArrayFromId(R.array.reading_fontsize_12_dimens);
        String[] value14 = getStringArrayFromId(R.array.reading_fontsize_14_dimens);
        String[] value16 = getStringArrayFromId(R.array.reading_fontsize_16_dimens);
        String[] value18 = getStringArrayFromId(R.array.reading_fontsize_18_dimens);
        String[] value20 = getStringArrayFromId(R.array.reading_fontsize_20_dimens);
        String[] value22 = getStringArrayFromId(R.array.reading_fontsize_22_dimens);
        String[] value24 = getStringArrayFromId(R.array.reading_fontsize_24_dimens);
        String[] value26 = getStringArrayFromId(R.array.reading_fontsize_26_dimens);
        String[] value28 = getStringArrayFromId(R.array.reading_fontsize_28_dimens);
        String[] value30 = getStringArrayFromId(R.array.reading_fontsize_30_dimens);

        mDimensMap.put(12.0f, getValueMap(value12));
        mDimensMap.put(14.0f, getValueMap(value14));
        mDimensMap.put(16.0f, getValueMap(value16));
        mDimensMap.put(18.0f, getValueMap(value18));
        mDimensMap.put(20.0f, getValueMap(value20));
        mDimensMap.put(22.0f, getValueMap(value22));
        mDimensMap.put(24.0f, getValueMap(value24));
        mDimensMap.put(26.0f, getValueMap(value26));
        mDimensMap.put(28.0f, getValueMap(value28));
        mDimensMap.put(30.0f, getValueMap(value30));
    }
    
    private static String[] getStringArrayFromId(int resId) {
        return mContext.getResources().getStringArray(resId);
    }
    
    private static HashMap<String, Integer> getValueMap(String[] vaules) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        
        map.put(TITILE_MARGIN_TOP, getPixelFromString(vaules[0]));
        map.put(BOOK_MARK_MARGIN_RIGHT, getPixelFromString(vaules[1]));
        map.put(BOTTOM_MARGIN_BOTTOM, getPixelFromString(vaules[2]));
        map.put(CONTENT_MARGIN_BOTTOM, getPixelFromString(vaules[3]));
        map.put(CONTENT_MARGIN_LEFT_RIGHT, getPixelFromString(vaules[4]));
        map.put(CONTENT_MARGIN_TOP, getPixelFromString(vaules[5]));
        map.put(CONTENT_LINE_SPACE, getPixelFromString(vaules[6]));
        map.put(TITILE_FONT_SIZE, getPixelFromString(vaules[7]));
        
        return map;
    }
    
    private static int getPixelFromString(String str) {
        int result = 0;
        
        if(str.endsWith("sp")) {
            result = PixelUtil.sp2px(Float.parseFloat(str.substring(0, str.length() - 2)));
        } else if(str.endsWith("dp")) {
            result = PixelUtil.dp2px(Float.parseFloat(str.substring(0, str.length() - 2)));
        }
        
        return result;
    }
    
    /**
     * 通过字体大小（sp）以及指定区域获取像素值
     * 
     * @param fontsizeSp 字体大小
     * @param key 指定区域<br>
     * {@link ReadPageDimenUtil#TITILE_MARGIN_TOP}<br>
     * {@link ReadPageDimenUtil#BOOK_MARK_MARGIN_RIGHT}<br>
     * {@link ReadPageDimenUtil#BOTTOM_MARGIN_BOTTOM}<br>
     * {@link ReadPageDimenUtil#CONTENT_MARGIN_BOTTOM}<br>
     * {@link ReadPageDimenUtil#CONTENT_MARGIN_LEFT_RIGHT}<br>
     * {@link ReadPageDimenUtil#CONTENT_MARGIN_TOP}<br>
     * {@link ReadPageDimenUtil#CONTENT_LINE_SPACE}<br>
     * {@link ReadPageDimenUtil#TITILE_FONT_SIZE}
     * 
     * @return -1 if don't find pixels by the font size(sp), otherwise return the pixel. 
     */
    public static int getPixel(float fontsizeSp, String key) {
        HashMap<String, Integer> pixelMap = mDimensMap.get(fontsizeSp);
        
        if(null == pixelMap) {
            return -1; 
        }
        
        return pixelMap.get(key);
    }
}
