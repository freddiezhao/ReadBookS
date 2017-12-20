package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.BuyItem;
import com.sina.book.data.ListResult;

/*
 * 消费记录-单本详情
 */
public class BuyDetailItemParser extends BaseParser
{

	protected Object parse(String jsonString) throws JSONException
	{
		ListResult<BuyItem> result = new ListResult<BuyItem>();
		ArrayList<BuyItem> list = new ArrayList<BuyItem>();
		result.setList(list);
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		JSONObject realobj = obj.optJSONObject("consumelog");
		result.setTotalNum(realobj.optInt("total"));
		// 消费记录详细查询接口
		// 新增字段is_removed: 1为已下架。 is_removed:0为未下架。
		boolean isRemoved = realobj.optInt("is_removed") == 1;
		JSONArray dataArray = realobj.optJSONArray("data");
		if (dataArray != null) {
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject item = dataArray.getJSONObject(i);
				BuyItem buyItem = new BuyItem();
				buyItem.setBookId(item.optString("book_id"));
				buyItem.setChapterId(item.optString("charpter_id"));
				buyItem.setTitle(item.optString("title"));
				buyItem.setTime(item.optString("time"));
				buyItem.setUnit(item.optString("unit"));
				buyItem.setPrice(String.valueOf(item.optDouble("price")));
				buyItem.setPayType(item.optInt("paytype"));
				buyItem.setOutOfService(isRemoved);
				list.add(buyItem);
			}
		}
		return result;
	}
}
