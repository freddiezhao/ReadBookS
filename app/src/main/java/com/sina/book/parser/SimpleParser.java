package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.ConstantData;

/**
 * 简单数据类型解析器
 * 
 * @author MaXingliang
 */
public class SimpleParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		parseDataContent(jsonString);

		String result = ConstantData.CODE_FAIL;

		JSONObject obj = new JSONObject(jsonString);
		if (obj != null) {
			JSONObject o = obj.optJSONObject("status");
			if (o != null) {
				if ("0".equals(o.optString("code"))) {
					result = ConstantData.CODE_SUCCESS;
					code = result;
				} else {
					String msg = o.optString("msg", "");
					if (!"".equals(msg)) {
						// 判断是不是token失效了
						if (msg.contains(String.valueOf(ConstantData.ACCESSTOKEN_EXPIRED_CODE1))
								|| msg.contains(String.valueOf(ConstantData.ACCESSTOKEN_EXPIRED_CODE2))) {
							return ConstantData.ACCESSTOKEN_EXPIRED_CODE1;
						} else if (msg.contains(String.valueOf(ConstantData.WEIBO_SDK_ERROR_CODE_20019))) {
							return ConstantData.WEIBO_SDK_ERROR_CODE_20019;
						}
					}
				}
			}
		}
		return result;
	}
}
