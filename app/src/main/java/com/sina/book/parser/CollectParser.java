package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

public class CollectParser extends BaseParser{

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        JSONObject result = new JSONObject(jsonString);
        JSONObject collectObj = result.getJSONObject("collect");
        boolean isCollected = collectObj.optBoolean("status", false);
        return isCollected;
    }

}
