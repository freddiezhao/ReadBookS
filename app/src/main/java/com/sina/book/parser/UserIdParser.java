package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.ConstantData;
import com.sina.book.util.LogUtil;

public class UserIdParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        LogUtil.d(jsonString);

        int return_code = obj.optInt("error_code");
        
        LogUtil.d(TAG, getClass().getName() + " ret :" + return_code);
        
        if (ConstantData.ACCESSTOKEN_EXPIRED_CODE1 == return_code || ConstantData.ACCESSTOKEN_EXPIRED_CODE2 == return_code) {
            return ConstantData.ACCESSTOKEN_EXPIRED_CODE1;
        }

        return obj.getString("uid");
    }

}
