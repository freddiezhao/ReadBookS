package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.SinaBookApplication;
import com.sina.book.data.GiftInfo;
import com.sina.book.data.ListResult;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LoginUtil;
import com.sina.book.util.Util;

/**
 * 礼包
 * 
 */
public class GiftParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		ListResult<GiftInfo> result = new ListResult<GiftInfo>();
		ArrayList<GiftInfo> list = new ArrayList<GiftInfo>();
		result.setList(list);
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);

		JSONObject jsonObject = obj.optJSONObject("data");
		result.setTotalNum(jsonObject.optInt("total"));

		JSONArray dataArray = jsonObject.optJSONArray("package");
		if (dataArray != null) {
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject item = dataArray.getJSONObject(i);
				GiftInfo info = new GiftInfo();
				info.setAid(item.optString("aid"));
				info.setGiftName(item.optString("name"));
				info.setDesc(item.optString("desc"));
				info.setImageUrl(item.optString("img"));
				info.setPicName(item.optString("pic_name"));

				String actvity_url = item.optString("actvity_url");

				if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
					String gsid = LoginUtil.getLoginInfo().getUserInfo().getGsid();
					if (!Util.isNullOrEmpty(gsid)) {
						actvity_url = HttpUtil.setURLParams(actvity_url, "gsid", gsid);
					} else {
						gsid = LoginUtil.i.syncRequestGsidHttpClient().getGsid();

						LoginUtil.getLoginInfo().getUserInfo().setGsid(gsid);
						LoginUtil.saveLoginGsid(gsid);

						actvity_url = HttpUtil.setURLParams(actvity_url, "gsid", gsid);
					}
				}

				info.setActvity_url(actvity_url);
				info.setShow(item.optString("is_show").equals("1") ? true : false);
				info.setPlace(item.optString("place"));
				list.add(info);
			}

		}
		return result;
	}

}
