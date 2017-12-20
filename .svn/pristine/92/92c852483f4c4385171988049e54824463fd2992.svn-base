package com.sina.book.util;

import java.security.MessageDigest;
import java.util.*;

/**
 * URL工具类
 *
 * @author MarkMjw
 */
public class URLUtil {
    public static final int VALUE_NULL_CODE = -100;

    /**
     * 解析出url请求的路径，包括页面
     *
     * @param strURL url地址
     * @return url路径
     */
    public static String urlPage(String strURL) {
        String strPage = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase(Locale.CHINA);

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 0) {
            if (arrSplit.length > 1) {
                if (arrSplit[0] != null) {
                    strPage = arrSplit[0];
                }
            }
        }

        return strPage;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String truncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase(Locale.CHINA);

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param url url地址
     * @return url请求参数部分
     */
    public static HashMap<String, String> parseUrl(String url) {
        HashMap<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = truncateUrlPage(url);
        if (strUrlParam == null) {
            return mapRequest;
        }
        
        // 每个键值为一组
        arrSplit = strUrlParam.split("[&]");
        if (null != arrSplit) {
            for (String strSplit : arrSplit) {
                String[] arrSplitEqual = null;
                arrSplitEqual = strSplit.split("[=]");
                
                if (arrSplitEqual.length > 1) {
                    mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
                }
                
//                else {
//                    if (!"".equals(arrSplitEqual[0])) {
//                        mapRequest.put(arrSplitEqual[0], VALUE_NULL_CODE + "");
//                    }
//                }
            }
        }
        return mapRequest;
    }

    /**
     * 新的md5签名，首尾放secret。
     *
     * @param params 参数Map
     * @param secret 分配给您的APP_SECRET
     */
    public static String md5Signature(TreeMap<String, String> params, String secret) {
        String result = null;
        StringBuffer orgin = getBeforeSign(params, new StringBuffer(secret));
        if (orgin == null) return result;
        orgin.append(secret);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
        } catch (Exception e) {
            throw new java.lang.RuntimeException("MD5Signature error !");
        }
        return result;
    }

    /**
     * 二行制转字符串
     *
     * @param b
     */
    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer();
        String temp;
        for (int n = 0; n < b.length; n++) {
            temp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (temp.length() == 1) hs.append("0").append(temp);
            else hs.append(temp);
        }
        return hs.toString().toLowerCase(Locale.CHINA);
    }

    /**
     * 添加参数的封装方法
     *
     * @param params
     * @param orgin
     */
    private static StringBuffer getBeforeSign(TreeMap<String, String> params, StringBuffer orgin) {
        if (params == null) return null;
        Map<String, String> treeMap = new TreeMap<String, String>();
        treeMap.putAll(params);
        Iterator<String> iterator = treeMap.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            orgin.append(name).append(params.get(name));
        }
        return orgin;
    }

    /**
     * URL参数MD5签名
     *
     * @param string 参数连接字符串
     * @return 签名值
     */
    public static String md5Signature(String string) {
        String sign;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            sign = byte2hex(md.digest(string.getBytes("utf-8")));
        } catch (Exception e) {
            throw new java.lang.RuntimeException("MD5Signature error !");
        }

        return sign;
    }
}
