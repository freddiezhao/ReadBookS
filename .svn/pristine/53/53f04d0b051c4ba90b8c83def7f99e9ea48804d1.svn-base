package com.sina.book.data;

/**
 * 含有一般对象的解析结果
 * 
 * @author Tsimle
 * 
 * @param <T>
 */
public class ObjResult<T> {
    private String retcode;
    private String retMsg;
    private T obj;

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public String getMsg() {
        return retMsg;
    }

    public void setMsg(String msg) {
        this.retMsg = msg;
    }

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public boolean isSucc() {
        return retcode.equals(ConstantData.CODE_SUCCESS);
    }
}
