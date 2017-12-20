package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.AuthorInfo;

public class AuthorInfoParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        AuthorInfo authorInfo = new AuthorInfo();
        parseDataContent(jsonString);

        JSONObject obj = new JSONObject(jsonString);
        if (obj != null) {
            JSONObject obj1 = obj.optJSONObject("data");
            if (obj1 != null) {
                authorInfo.setIsShow(obj1.optString("is_show"));
                authorInfo.setUid(obj1.optString("uid"));
            }
        }

        return authorInfo;
    }
}