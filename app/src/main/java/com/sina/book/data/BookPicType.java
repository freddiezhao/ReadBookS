package com.sina.book.data;

import java.io.Serializable;

public class BookPicType implements Serializable{
    private static final long serialVersionUID = -1806003765486456202L;
    
    /** 返券类型. */
    public static final int TYPE_PIC_ONE = 1;
    /** 限免类型. */
    public static final int TYPE_PIC_TWO = 2;
    /** 特价类型. */
    public static final int TYPE_PIC_THREE = 3;

    private boolean isShow;
    private int type;
    private String name;

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}