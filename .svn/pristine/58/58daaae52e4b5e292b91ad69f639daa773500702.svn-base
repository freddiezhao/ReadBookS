package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.UserInfo;

public class UserInfoParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        UserInfo userInfo = new UserInfo();
        JSONObject obj = new JSONObject(jsonString);
        JSONObject userInfoObj = obj.getJSONObject("userinfo");
        userInfo.setuName(userInfoObj.getString("screen_name"));
        userInfo.setUserProfileUrl(userInfoObj.getString("profile_image_url"));
        return userInfo;
    }

}
