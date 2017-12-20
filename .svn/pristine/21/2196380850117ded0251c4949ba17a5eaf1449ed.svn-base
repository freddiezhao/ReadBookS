package com.sina.book.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.sina.book.data.GeneralActivityInfo;

/**
 * 通用活动数据解析器
 * 
 * @author chenjianli
 * 
 */
public class GeneralActivityParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		// TODO Auto-generated method stub
		parseDataContent(jsonString);
		if (!TextUtils.isEmpty(jsonString)) {
			JSONObject json = new JSONObject(jsonString);
			if (isKeyHasAvailableValue(json, "data")) {
				GeneralActivityInfo info = new GeneralActivityInfo();
				JSONObject data = json.optJSONObject("data");
				info.setShow("y".equals(data.optString("show", "n")));
				info.setShowNum(data.optInt("shownum", 1));
				info.setActId(data.optInt("actid", -1));
				info.setTitle(data.optString("title", "活动"));
				info.setSubTitle(data.optString("subtitle", ""));
				info.setImgUrl(data.optString("img"));

				if (isKeyHasAvailableValue(data, "button")) {
					JSONArray buttons = data.getJSONArray("button");
					ArrayList<HashMap<String, String>> buttonList = info.getButtons();
					for (int i = 0; i < buttons.length(); i++) {
						JSONObject buttonJsonObj = (JSONObject) buttons.get(i);
						HashMap<String, String> map = new HashMap<String, String>();
						if (isKeyHasAvailableValue(buttonJsonObj, "type")) {
							map.put("type", buttonJsonObj.optString("type"));
						}
						if (isKeyHasAvailableValue(buttonJsonObj, "title")) {
							map.put("title", buttonJsonObj.optString("title"));
						}
						if (isKeyHasAvailableValue(buttonJsonObj, "url")) {
							map.put("url", buttonJsonObj.optString("url"));
						}
						buttonList.add(map);
					}
				}
				// info.setActivityUrl(data.optString("url"));
				return info;
			}
		}
		return null;
	}

}
