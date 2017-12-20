package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.HotWord;
import com.sina.book.data.HotWordsResult;

public class HotWordsParser extends BaseParser {
    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        HotWordsResult result = null;
        JSONObject obj = new JSONObject(jsonString);

        if (obj != null) {
            result = new HotWordsResult();
            result.setTotal(obj.optInt("total"));
            JSONArray array = obj.optJSONArray("hot_word");
            if (array != null) {
                ArrayList<HotWord> hotWordList = new ArrayList<HotWord>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    HotWord hotWord = new HotWord();
                    hotWord.setName(item.optString("word"));
                    hotWord.setState(item.optString("flag"));
                    hotWordList.add(hotWord);
                }
                result.addItems(hotWordList);
            }
        }

        return result;
    }
}
