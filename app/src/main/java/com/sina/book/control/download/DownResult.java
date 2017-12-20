package com.sina.book.control.download;

public class DownResult {
    public int stateCode; // 状态码，一般为HTTP的响应码
    public DownFileTask task; // 任务对象本身
    public Object retObj; // 任务处理结果对象。也可以是错误消息

    public DownResult() {

    }

    public DownResult(int stateCode, DownFileTask task, Object result) {
        this.stateCode = stateCode;
        this.task = task;
        this.retObj = result;
    }
}
