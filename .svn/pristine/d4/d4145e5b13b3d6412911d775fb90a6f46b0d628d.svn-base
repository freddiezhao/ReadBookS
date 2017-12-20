package com.sina.book.control;

import com.sina.book.db.DBService;
import com.sina.book.util.Util;

/**
 * 数据缓存
 * 
 * @author Tsimle
 * 
 */
public class DataCacheBean {
    private int id;
    private String key;
    private String data;
    private long time;
    private long invalid;
    
    
    public DataCacheBean() {
    }
    
    /**
     * DataCacheBean 构造方法
     * 
     * @param url
     * @param data
     * @param invalidTime
     *            设置<=0表明不失效<br>
     */
    public DataCacheBean(String url, String data, long invalidTime) {
        this.key = generateKey(url);
        this.data = data;
        if (invalidTime > 0) {
            this.time = System.currentTimeMillis();
            this.invalid = invalidTime;
        } else {
            this.time = 0;
            this.invalid = 0;
        }
    }

    /**
     * 缓存是否有效
     * 
     * @return
     */
    public boolean isValid() {
        long now = System.currentTimeMillis();
        if (Util.isNullOrEmpty(data)) {
            return false;
        }
        if (time == 0) {
            return true;
        }
        if (now >= time + invalid) {
            return false;
        }
        // 时间有问题，清理所有缓存
        if (now < time - 10000) {
            DBService.clearAllDataCache();
        }
        return true;
    }

    public static String generateKey(String url) {
        if (null != url && !"".equals(url.trim())) {
            url = url.replace('/', '_').replace(':', '_').replace("?", "_");
            url = url.replaceAll("access_token=[^&]*[&]*", "");
            url = url.replaceAll("from_client=[^&]*[&]*", "");
            url = url.replaceAll("authcode=[^&]*[&]*", "");
            if (url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }
        }
        return url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getInvalid() {
        return invalid;
    }

    public void setInvalid(long invalid) {
        this.invalid = invalid;
    }
}
