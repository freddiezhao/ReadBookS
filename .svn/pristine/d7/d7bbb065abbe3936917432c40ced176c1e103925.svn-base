package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

public class RechargeParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		// TODO Auto-generated method stub
		parseDataContent(jsonString);
		JSONObject json = new JSONObject(jsonString);
		if (isKeyHasAvailableValue(json, "url")) {
			String url = json.getString("url");
			return url;
		}
		return null;
	}

}
