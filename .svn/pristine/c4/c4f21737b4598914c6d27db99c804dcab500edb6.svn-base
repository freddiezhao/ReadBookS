package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.PaymentMonthBookResult;

public class PaymentMonthBookParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		PaymentMonthBookResult result = new PaymentMonthBookResult();
		ArrayList<Book> lists = new ArrayList<Book>();
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		result.setTotal(obj.optInt("total"));
		JSONArray array = obj.optJSONArray("books");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				Book book = new Book();
				JSONObject itemObj = array.getJSONObject(i);
				book.setBookId(itemObj.optString("bid"));
				book.setBookSrc(itemObj.optString("src"));
				book.setTitle(itemObj.optString("title"));
				book.setAuthor(itemObj.optString("author"));
				book.setIntro(itemObj.optString("intro"));
				book.setSuiteImageUrl(itemObj.optString("img"));

				lists.add(book);
			}
		}

		if (isKeyHasAvailableValue(obj, "suite")) {
			JSONObject suiteJsonObj = obj.optJSONObject("suite");
			if (isKeyHasAvailableValue(suiteJsonObj, "suite_name")) {
				String suiteName = suiteJsonObj.getString("suite_name");
				result.setSuiteName(suiteName);
			}
		}

		result.addLists(lists);
		return result;
	}

}