package com.sina.book.data.util;

import com.sina.book.data.MainBookResult;

/**
 * 全局的静态信息又需要修改的放在这里<br>
 * 省去传递的痛苦
 * 
 * @author Tsimle
 * 
 */
public class StaticInfoKeeper {
    private static MainBookResult mainBookInfo;

    public static MainBookResult getMainBookInfo() {
        return mainBookInfo;
    }

    public static void setMainBookInfo(MainBookResult mainBookInfo) {
        if (StaticInfoKeeper.mainBookInfo != mainBookInfo && mainBookInfo != null) {
            StaticInfoKeeper.mainBookInfo = mainBookInfo;
        }
    }

}
