package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.CommentItem;
import com.sina.book.data.CommentsResult;

public class CommentsParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        CommentsResult result = new CommentsResult();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        result.setTotal(obj.optInt("total", 0));
        if (result.getTotal() > 0) {
            JSONArray array = obj.optJSONArray("comments");
            if (array != null) {
                result.addItems(parserData(array));
            }
        }
        return result;
    }

    private ArrayList<CommentItem> parserData(JSONArray array) throws JSONException {
        ArrayList<CommentItem> lists = new ArrayList<CommentItem>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            CommentItem dataitem = new CommentItem();
            dataitem.setUser(item.optString("user"));
            dataitem.setMsg(item.optString("msg"));
            dataitem.setTime(item.optString("time"));
            lists.add(dataitem);
        }
        return lists;
    }
}
