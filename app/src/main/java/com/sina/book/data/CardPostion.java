package com.sina.book.data;

/**
 * ScrollView中显示的card tip<br>
 * 名字，开始及结束位置
 * 
 * @author Tsimle
 * 
 */
public class CardPostion {

    private int startPos;
    private int endPos;
    private String title;

    public CardPostion(int startPos, int endPos, String title) {
        super();
        this.startPos = startPos;
        this.endPos = endPos;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean inRange(int postion) {
        if (postion >= startPos && postion <= endPos) {
            return true;
        }
        return false;
    }
}
