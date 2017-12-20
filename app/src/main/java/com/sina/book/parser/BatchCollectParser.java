package com.sina.book.parser;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class BatchCollectParser extends BaseParser
{

	@SuppressWarnings("unchecked")
	@Override
	protected Object parse(String jsonString) throws JSONException
	{
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		// ZLog.d(ZLog.FBReader, "jsonString:" + obj);

		JSONObject dataArray = obj.optJSONObject("date");
		HashMap<String, Integer> output = new HashMap<String, Integer>();
		if (dataArray != null && dataArray.length() != 0) {
			Iterator<String> iterator = dataArray.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				int value = dataArray.optInt(key, 0);
				output.put(key, value);
			}
		}
		return output;
	}

}
