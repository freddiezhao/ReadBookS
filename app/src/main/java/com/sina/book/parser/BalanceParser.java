package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.UserInfoUb;
import com.sina.book.data.UserInfoUb.Activitys;
import com.sina.book.util.Util;

public class BalanceParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		UserInfoUb ub = new UserInfoUb();

		if (obj != null) {
			JSONObject accountInfo = obj.getJSONObject("userinfo");
			if (accountInfo == null) {
				return null;
			}

			String balance = accountInfo.optString("ub", "0.00");
			if (!Util.isDoubleValue(balance)) {
				balance = "0.00";
			}
			ub.setBalance(balance);
			String roleName = accountInfo.optString("role_name", "普通会员");
			ub.setRoleName(roleName);
			int role = accountInfo.optInt("role", 0);
			ub.setRole(role);

			String name = accountInfo.optString("name", "");
			ub.setName(name);
			String uid = accountInfo.optString("uid", "");
			ub.setUid(uid);

			// 可能没有
			JSONArray activityInfoArray = obj.optJSONArray("activity");
			if (activityInfoArray != null && activityInfoArray.length() > 0) {
				ArrayList<Activitys> activitys = new ArrayList<UserInfoUb.Activitys>();
				for (int i = 0; i < activityInfoArray.length(); i++) {
					JSONObject activityInfo = activityInfoArray.getJSONObject(i);
					if (activityInfo != null) {
						Activitys activity = new Activitys();
						String sname = activityInfo.optString("name");
						activity.setActivityName(sname);
						String tip = activityInfo.optString("tip");
						activity.setActivityTip(tip);
						String eTime = activityInfo.optString("etime");
						activity.setActivityEndTime(eTime);
						String url = activityInfo.optString("url");
						activity.setActivityUrl(url);
						int aType = activityInfo.optInt("atype", 0);
						if (aType == 1) {
							activity.setActivityType(Activitys.TYPE_CARD);
						} else {
							activity.setActivityType(Activitys.TYPE_OTHER);
						}
						// 或者
						if (url != null
								&& ((sname != null && sname.contains("赠书卡")) || (tip != null && tip.contains("赠书卡")))) {
							activity.setActivityType(Activitys.TYPE_CARD);
						}
						activitys.add(activity);
					}
				}
				ub.setActivitys(activitys);
			}

		}
		return ub;
	}
}
