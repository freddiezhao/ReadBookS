package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.HotSearchResult;

public class HotSearchParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        HotSearchResult result = new HotSearchResult();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONArray array = obj.optJSONArray("words");
        ArrayList<String> lists = new ArrayList<String>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                lists.add(array.getString(i));
            }
        }
        result.addItems(lists);
        return result;
    }

}
