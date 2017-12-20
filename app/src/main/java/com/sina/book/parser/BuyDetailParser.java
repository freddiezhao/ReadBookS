package com.sina.book.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.BuyDetail;
import com.sina.book.data.ListResult;
import com.sina.book.util.CalendarUtil;

public class BuyDetailParser extends BaseParser {

	/*
	 * 按时间排序(最近的排在前面)
	 */
	private Comparator<BuyDetail> comparatorPatition = new Comparator<BuyDetail>() {

		@Override
		public int compare(BuyDetail lhs, BuyDetail rhs) {
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
		ListResult<BuyDetail> result = new ListResult<BuyDetail>();
		ArrayList<BuyDetail> list = new ArrayList<BuyDetail>();
		result.setList(list);
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		JSONObject realobj = obj.optJSONObject("consumelog");
		result.setTotalNum(realobj.optInt("total"));
		JSONArray dataArray = realobj.optJSONArray("data");
		if (dataArray != null) {
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject item = dataArray.getJSONObject(i);
				BuyDetail buyDetail = new BuyDetail();
				buyDetail.setBookId(item.optString("book_id"));
				buyDetail.setTitle(item.optString("title"));
				buyDetail.setTime(item.optString("time"));
				buyDetail.setProductType(item.optString("product_type"));
				buyDetail.setPayType(item.optInt("paytype"));
				list.add(buyDetail);
			}
			// 按时间排序
			Collections.sort(list, comparatorPatition);
		}

		return result;
	}

}
