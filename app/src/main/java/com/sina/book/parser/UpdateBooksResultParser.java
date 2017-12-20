package com.sina.book.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.Chapter;

public class UpdateBooksResultParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		parseDataContent(jsonString);

		List<Book> books = new ArrayList<Book>();
		JSONObject obj = new JSONObject(jsonString);

		JSONArray array = obj.optJSONArray("books");
		if (array != null) {
			for (int j = 0; j < array.length(); j++) {
				JSONObject object = array.getJSONObject(j);

				// 如果检查状态为false
				String checkStatus = object.optString("check_status");
				if (!"Y".equals(checkStatus) && !"y".equals(checkStatus)) {
					continue;
				}

				Book book = new Book();
				book.setBookId(object.optString("book_id"));
				if (book.getBookId() == null || book.getBookId().trim().length() == 0) {
					book.setBookId(object.optString("bid"));
				}

				// 来自服务器的章节总数跟本地不一致，直接忽略
				// book.setNum(object.optInt("chapter_num"));

				JSONArray chapterArray = object.optJSONArray("chapters");
				if (null != chapterArray) {
					ArrayList<Chapter> chapters = new ArrayList<Chapter>();
					for (int i = 0; i < chapterArray.length(); i++) {
						JSONObject chapterObject = chapterArray.getJSONObject(i);

						Chapter chapter = new Chapter();

						chapter.setSerialNumber(chapterObject.optInt("s_num"));
						chapter.setTitle(chapterObject.optString("title"));
						chapter.setGlobalId(chapterObject.optInt("c_id"));
						chapter.setVip(chapterObject.optString("vip"));

						chapters.add(chapter);
					}

					book.setChapters(chapters);
					books.add(book);
				}
			}
		}

		return books;
	}
}
