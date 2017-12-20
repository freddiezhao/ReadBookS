package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
import android.text.TextUtils;

import com.sina.book.data.Book;
import com.sina.book.data.BookRelatedData;

public class BookRelatedParser extends BaseParser {

	private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";

	@Override
	protected Object parse(String jsonString) throws JSONException {
		BookRelatedData result = new BookRelatedData();
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);

		JSONArray array0 = obj.optJSONArray("author_books");
		if (array0 != null) {
			result.addAuthorItems(parserData(array0));
		}

		JSONArray array1 = obj.optJSONArray("cate_books");
		if (array1 != null) {
			result.addCateItems(parserData(array1));
		}

		JSONArray array2 = obj.optJSONArray("tags");
		if (array2 != null) {
			ArrayList<String> lists = new ArrayList<String>();
			for (int i = 0; i < array2.length(); i++) {
				String string = array2.getString(i).replaceAll(BOOK_DETAIL_TAG_PATTERN, "");
				if (!string.equals("")) {
					lists.add(string);
				}
			}
			result.addTags(lists);
		}

		return result;
	}

	private ArrayList<Book> parserData(JSONArray array) throws JSONException {
		ArrayList<Book> lists = new ArrayList<Book>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject item = array.getJSONObject(i);
			Book book = new Book();
			book.setSid(item.optString("sid"));
			book.setBookId(item.optString("book_id"));
			try {
				book.setTitle(Html.fromHtml(item.optString("title")).toString());
				book.setAuthor(Html.fromHtml(item.optString("author")).toString());
			} catch (Exception e) {
			}
			book.getDownloadInfo().setImageUrl(item.optString("img"));
			book.setIntro(item.optString("intro"));
			book.getBuyInfo().setStatusInfo(item.optString("status"));
			book.getBuyInfo().setPayType(item.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
			book.getBuyInfo().setPrice(item.optDouble("price", 0));
			book.setBookSrc(item.optString("src"));
			book.setPraiseNum(item.optLong("praise_num"));

			JSONArray tags = item.optJSONArray("tags");
			StringBuilder str = new StringBuilder();
			if (tags != null) {
				for (int j = 0; j < tags.length(); j++) {
					String tag = tags.getString(j);
					if (!TextUtils.isEmpty(tag)) {
						str.append(tag.replaceAll(BOOK_DETAIL_TAG_PATTERN, "")).append(" ");
					}
				}
				book.setContentTag(str.toString());
			}

			lists.add(book);
		}
		return lists;
	}
}