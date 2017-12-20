package com.sina.book.data;

/**
 * 热词
 * 
 * @author liujie
 * 
 */
public class HotWord {

    /** 热词搜索状态 ：1：上升，2：保持，3：下降 */
    public static final String STATE_UP = "1";
    public static final String STATE_BALANCE = "2";
    public static final String STATE_DOWN = "3";

    private String name;
    private String state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
