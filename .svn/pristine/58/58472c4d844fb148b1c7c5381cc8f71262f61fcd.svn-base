package com.sina.book.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.ListResult;
import com.sina.book.data.RechargeDetail;
import com.sina.book.util.CalendarUtil;

public class RechargeDetailParser extends BaseParser {

	/*
	 * 按时间排序(最近的排在前面)
	 */
	private Comparator<RechargeDetail> comparatorPatition = new Comparator<RechargeDetail>() {

		@Override
		public int compare(RechargeDetail lhs, RechargeDetail rhs) {
			if (lhs == null || rhs == null)
				return 0;
			if (null == lhs.getTime() || null == rhs.getTime()) {
				return 0;
			}

			long lhsTime = CalendarUtil.parseFormatDateToTime(lhs.getTime());
			long rhsTime = CalendarUtil.parseFormatDateToTime(rhs.getTime());
			if (lhsTime - rhsTime < 0) {
				return 1;
			} else if (lhsTime - rhsTime > 0) {
				return -1;
			}
			return 0;
		}

	};

	@Override
	protected Object parse(String jsonString) throws JSONException {
		ListResult<RechargeDetail> result = new ListResult<RechargeDetail>();
		ArrayList<RechargeDetail> list = new ArrayList<RechargeDetail>();
		result.setList(list);
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		JSONObject realobj = obj.optJSONObject("consumelog");
		result.setTotalNum(realobj.optInt("total"));
		result.setHasNext(realobj.optInt("hasnext"));
		JSONArray dataArray = realobj.optJSONArray("data");
		if (dataArray != null) {
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject item = dataArray.getJSONObject(i);
				RechargeDetail rechargeDetail = new RechargeDetail();
				rechargeDetail.setNo(item.optString("order_id"));
				rechargeDetail.setTime(item.optString("create_time"));
				rechargeDetail.setDetail(item.optString("desc"));
				rechargeDetail.setPrice(item.optDouble("price"));
				list.add(rechargeDetail);
			}
			// 按时间排序
			Collections.sort(list, comparatorPatition);
		}
		return result;
	}

}
