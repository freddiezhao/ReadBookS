package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.LoginInfo;
import com.sina.book.data.UserInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class AutoLoginParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        LoginInfo logInfo = new LoginInfo();
        UserInfo userInfo = new UserInfo();
        logInfo.setUserInfo(userInfo);
        JSONObject obj = new JSONObject(jsonString);
        JSONObject userInfoObj = obj.getJSONObject("userinfo");
        logInfo.setUID(userInfoObj.optString("uid"));
        userInfo.setuName(userInfoObj.optString("screen_name"));
        userInfo.setUserProfileUrl(userInfoObj.optString("profile_image_url"));
        
        String token = userInfoObj.optString("token");
        String expires_in = userInfoObj.optString("expires");
        
        Oauth2AccessToken accessToken = new Oauth2AccessToken(token, expires_in);
        logInfo.setAccessToken(token);
        logInfo.setExpires(expires_in);
        logInfo.setExpires_time(accessToken.getExpiresTime());    
        return logInfo;
    }

}
