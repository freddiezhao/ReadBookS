package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

public class TicketToUsedParser extends BaseParser {

    protected Object parse(String jsonString) throws JSONException {
        JSONObject result = new JSONObject(jsonString);
        
        /*
         * 1: 写入成功
         * 2：写入失败
         * 3: 不存在、channel错误、程序错误...
         */
        String code = result.optString("code");
        return code;
    }
}
