package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.ChannelActivityInfo;

public class ChannelActivityPaser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		parseDataContent(jsonString);
		JSONObject jsonObj = new JSONObject(jsonString);
		// 报异常了：org.json.JSONException: No value for popup_card
		if (jsonObj != null && isKeyHasAvailableValue(jsonObj, "popup_card")) {
			JSONObject date = jsonObj.getJSONObject("popup_card");
			if (date != null) {
				String showMsg = date.optString("desc", "");
				return new ChannelActivityInfo(Integer.valueOf(getCode()),
						showMsg);
			}
		}
		return null;
	}

}
