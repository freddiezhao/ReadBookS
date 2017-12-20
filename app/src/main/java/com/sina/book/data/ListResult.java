package com.sina.book.data;

import java.util.ArrayList;

/**
 * 含有列表的解析结果
 * 
 * @author Tsimle
 * 
 * @param <T>
 */
public class ListResult<T> {
    private String retcode;
    private String retMsg;
    private int totalNum;
    private int hasNext;
    private ArrayList<T> list;

    public ListResult() {
    }

    public ListResult(ArrayList<T> list) {
        this.retcode = ConstantData.CODE_SUCCESS;
        this.totalNum = list.size();
        this.list = list;
    }

    public ListResult(String retcode, int totalNum, ArrayList<T> list) {
        this.retcode = retcode;
        this.totalNum = totalNum;
        this.list = list;
    }

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public int getHasNext() {
    	return hasNext;
    }
    
    public void setHasNext(int hasNext) {
    	this.hasNext = hasNext;
    }
    
    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalnum) {
        this.totalNum = totalnum;
    }

    public String getMsg() {
        return retMsg;
    }

    public void setMsg(String msg) {
        this.retMsg = msg;
    }

    public ArrayList<T> getList() {
        return list;
    }

    public void setList(ArrayList<T> list) {
        this.list = list;
    }

    public boolean isSucc() {
        return retcode.equals(ConstantData.CODE_SUCCESS);
    }
}
