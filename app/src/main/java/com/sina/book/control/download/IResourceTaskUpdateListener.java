package com.sina.book.control.download;

/**
 * 通知Html下载manager,资源下载成功或失败
 * @see
 */
public interface IResourceTaskUpdateListener {
    
    /**
     * isReplaceHtml: 是否对html进行地址替换
     * stateCode: 0:成功 -1：失败
     */
    public void onState(String chapterId, boolean isReplaceHtml,int stateCode);
    
}
