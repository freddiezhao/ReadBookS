package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

public class AutoDownBidParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        String bid = null;
        JSONObject result = new JSONObject(jsonString);
        JSONObject data = result.optJSONObject("data");
        if (data != null) {
            bid = data.optString("bid", null);
        }
        return bid;
    }

}
