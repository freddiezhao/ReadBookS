package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

public class RechargeSubParser extends BaseParser {
    private static final String DEFAULT_URL = "http://3g.sina.com.cn/dpool/paycenter/?vt=21";

    @Override
    protected Object parse(String jsonString) throws JSONException {
        String url = DEFAULT_URL;
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONObject data = obj.getJSONObject("data");
        if (data != null) {
            url = data.optString("url");
        }
        return url;
    }

}
