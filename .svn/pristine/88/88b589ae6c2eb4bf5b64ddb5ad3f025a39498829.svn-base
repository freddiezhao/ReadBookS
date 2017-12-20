package com.sina.book.reader.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import com.sina.book.image.ImageUtil;
import com.sina.book.reader.selector.Selection;
import com.sina.book.util.LogUtil;

/**
 * 图片段落
 * 
 * @author Tsimle
 * 
 */
public class ImageParagraph extends Paragraph {

    private Bitmap content;
    private String url;

    public ImageParagraph() {
    }

    public void init(ParagraphCreateBean createBean, int allLength) {
        boolean addNotSuccess = false;
        if (getHeight() <= createBean.availableHeight) {
//            LogUtil.d("cx", "imageFolder:" + createBean.imageFolder);
            LogUtil.d("cx", "img url:" + url);
            content = ImageUtil.getMustScaleBitmapFromFile(
                    createBean.imageFolder, url,
                    (int) mReadStyleManager.getVisibleWidth(),
                    (int) mReadStyleManager.getVisibleHeight());
        } else {
            addNotSuccess = true;
        }

        if (addNotSuccess) {
            createBean.byteUsed = 0;
            createBean.availableHeight = 0;
        } else {
            createBean.byteUsed = allLength;
            createBean.availableHeight = createBean.availableHeight
                    - getHeight();
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public float getHeight() {
        return mReadStyleManager.getVisibleHeight();
    }

    @Override
    public void draw(float startX, float startY, Canvas canvas) {
        if (content != null) {
            int x = (int) (mReadStyleManager.getVisibleWidth() - content.getWidth()) / 2;

            canvas.drawBitmap(content, startX + x, startY, null);
        }
    }

    @Override
    public Selection findSelection(RectF paraRect, float x, float y) {
        // TODO Auto-generated method stub
        return null;
    }

}
