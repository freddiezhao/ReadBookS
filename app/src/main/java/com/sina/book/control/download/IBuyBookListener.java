package com.sina.book.control.download;

/**
 * Interface IBuyBookListener.
 *
 * @author MarkMjw
 * @date 13-12-18
 */
public interface IBuyBookListener {

    /**
     * 完成
     *
     * @param state 购买状态
     */
    public void onFinish(int state);
}
