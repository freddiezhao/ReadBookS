package com.sina.book.reader.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.sina.book.SinaBookApplication;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.reader.selector.Selection;

/**
 * 段落
 * 
 * @author Tsimle
 * 
 */
public abstract class Paragraph {

    /**
     * 一般的段落文字<br>
     * 不使用任何标识
     * 
     */
    /**
     * 带样式的段落文字<br>
     * [<(t)>]用来标识<br>
     * [s=1.2;b=0] s指代字体大小为一般大小的倍数，b指代是否加粗<br>
     * [<(t)>][s=1.2;b=0;]
     */
    public static final String TAG_STYLED_TEXT_START = "[<(t)>]";
    /**
     * 图片段落<br>
     * [<(i)>]用来标识<br>
     * [u=src;]<br>
     * u指代图片地址<br>
     * [<(i)>][u=src;]
     */
    public static final String TAG_IMG_START = "[<(i)>]";

    protected static final String ROW_DIV1 = "\n";
    protected static final String ROW_DIV2 = "\r\n";
    
    public static final String PARA_BEGIN_PATTERN = "^([\\s|　]+)";
    /** 段落开始空格. */
    public static final String PARA_BEGIN_SPACE = "　　";
    /** 段落开始单个空格. */
    public static final String PARA_SINGLE_SPACE = "　";

    protected ReadStyleManager mReadStyleManager;
    protected PageContent mPageContent;
    protected String mCharset;

    protected int mParaBegin = 0;
    protected int mParaEnd = 0;

    public Paragraph() {
        mReadStyleManager = ReadStyleManager
                .getInstance(SinaBookApplication.gContext);
    }

    public void setParaBegin(int paraBegin) {
        this.mParaBegin = paraBegin;
    }

    public void setParaEnd(int paraEnd) {
        this.mParaEnd = paraEnd;
    }

    public void setPageContent(PageContent pageContent) {
        this.mPageContent = pageContent;
    }

    public void setCharset(String charset) {
        this.mCharset = charset;
    }

    public static Paragraph create(ParagraphBlockData blockData,
            String charset, ParagraphCreateBean createBean) {
        Paragraph p = null;

        byte[] contentBytes = blockData.dataBytes;
        int startPos = blockData.startPos;
        int realLength = contentBytes.length - blockData.startPos;
//        LogUtil.d("cx", "contentBytes:" + contentBytes.length);
        String content = null;
        String realContent = null;
        try {
            content = new String(contentBytes, charset);
            if (startPos > 0) {
                realContent = new String(contentBytes, startPos, realLength,
                        charset);
            }
        } catch (UnsupportedEncodingException e) {
            // no possible
        }

        if(null == content) return new TextParagraph();

        // 1 解析段落样式信息，生成段落
        if (content.length() > 7) {
            String type = content.substring(0, 7);
//            LogUtil.d("cx", type);
            if (type.equals(TAG_STYLED_TEXT_START)) {
//                LogUtil.d("cx", "styled txt");
                List<BasicNameValuePair> styles = parseStyles(content);
                TextParagraph styledTextP = new TextParagraph();
                styledTextP.setCharset(charset);
                int styleBytesLength = 0;
                
                for (BasicNameValuePair style : styles) {
                    if (style.getName().equals("s")) {
                        styledTextP.setFontScale(Float.parseFloat(style
                                .getValue()));
                    } else if (style.getName().equals("b")) {
                        if (style.getValue().equals("1")) {
                            styledTextP.setBold(true);
                        }
                    } else if (style.getName().equals("contentStart")) {
                        // 为空说明含有段落开始部分的读
                        if (realContent == null) {
                            int contentStart = Integer.parseInt(style
                                    .getValue());
                            realContent = content.substring(contentStart);
                            try {
                                styleBytesLength = content.substring(0,
                                        contentStart).getBytes(charset).length;
                                realLength = realLength - styleBytesLength;
                            } catch (UnsupportedEncodingException e) {
                                // no possible
                            }
                        }
                    }
                }

                styledTextP.init(realContent, createBean, realLength,
                        styleBytesLength);
                p = styledTextP;
            } else if (type.equals(TAG_IMG_START)) {
//                LogUtil.d("cx", "img");
                List<BasicNameValuePair> styles = parseStyles(content);
                ImageParagraph imgP = new ImageParagraph();
                
                for (BasicNameValuePair style : styles) {
                    if (style.getName().equals("u")) {
                        imgP.setUrl(style.getValue());
                    } 
                }
                
                imgP.init(createBean, realLength);
                p = imgP;
                // TODO 会有加入空段落的情况
            }
        }

        // 2 无段落样式作为一般文字段落处理
        if (p == null) {
//            LogUtil.d("cx", "normal txt");
            TextParagraph normalTextP = new TextParagraph();
            normalTextP.setCharset(charset);

            if (realContent == null) {
                realContent = content;
            }
            normalTextP.init(realContent, createBean, realLength, 0);
            p = normalTextP;
        }
        // 3 返回生成的段落
        return p;
    }

    public abstract float getHeight();

    public abstract void draw(float startX, float startY, Canvas canvas);
    
    /**
     * 传入x,y坐标，查找该位置在段落中字符偏移数
     * 
     * @param paraRect 段落所在矩形
     * @param x x坐标
     * @param y y坐标
     * @return 该位置在段落中的字符偏移数
     */
    public abstract Selection findSelection(RectF paraRect, float x, float y);

    private static List<BasicNameValuePair> parseStyles(String content) {
        List<BasicNameValuePair> styles = new ArrayList<BasicNameValuePair>();

        int controlS = content.indexOf("[", 7);
        int controlE = content.indexOf("]", 7);
        if (controlE - controlS > 1) {
            String controlInfo = content.substring(controlS + 1, controlE - 1);
            String[] controlParams = controlInfo.split(";");
            for (String controlParam : controlParams) {
                String[] kvPair = controlParam.split("=");
                if (kvPair.length == 2) {
                    styles.add(new BasicNameValuePair(kvPair[0], kvPair[1]));
                }
            }
        }
        styles.add(new BasicNameValuePair("contentStart", String
                .valueOf(controlE + 1)));
        return styles;
    }
}
