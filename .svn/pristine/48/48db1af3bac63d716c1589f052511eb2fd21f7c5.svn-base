package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.EpubCheckInfo;

public class EpubCheckParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
    	EpubCheckInfo checkInfo = new EpubCheckInfo();
    	
        JSONObject result = new JSONObject(jsonString);
        
        JSONObject statusObj = result.optJSONObject("status");
        if(statusObj != null){
        	checkInfo.code = statusObj.optInt("code");
        	checkInfo.msg = 	statusObj.optString("msg");	
        }
        
        JSONObject data = result.optJSONObject("book_info");
        if (data != null) {
        	checkInfo.setBookId(data.optString("book_id", ""));
        	checkInfo.setIsEpub(data.optString("is_epub", "1"));
        	
        	int kind = data.optInt("kind");
        	checkInfo.setIsHtml(kind == 7);
        }
        return checkInfo;
    }
}
