package com.sina.book.parser;

import java.util.ArrayList;
import java.util.Comparator;

import org.htmlcleaner.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.SearchResult;

/**
 * 解析书本搜索结果
 * 
 * @author Tsimle
 * 
 */
public class SearchResultParser extends BaseParser {
	private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";
	/**
	 * 调用检索接口失败即未查到，构造一个空的搜索结果
	 */
	// private final String SEARCH_FAIL = "5";

	/**
	 * 添加一个搜索结果的简单排序，<br>
	 * 尽量的将vip收费的书往前靠
	 */
	private Comparator<Book> comparatorBook = new Comparator<Book>() {
		@Override
		public int compare(Book book1, Book book2) {
			if (book1 == null || book2 == null) {
				return 0;
			}
			int suiteId1 = book1.getSuiteId();
			int suiteId2 = book2.getSuiteId();
			if (suiteId2 > suiteId1) {
				return 1;
			} else if (suiteId2 == suiteId1) {
				return 0;
			} else {
				return -1;
			}
		}
	};

	@Override
	protected Object parse(String jsonString) throws JSONException {
		SearchResult result = new SearchResult();
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		int total = obj.optInt("total", 0);
		// result.setTotal(total);
		if (total > 0) {
			result.setStart(obj.optInt("start"));
			result.setEnd(result.getStart() + obj.optInt("count"));
			JSONArray array = obj.optJSONArray("data");
			if (array != null) {
				result.setItems(parserData(array));
			} else {
				result.setItems(new ArrayList<Book>());
			}
		} else {
			result.setItems(new ArrayList<Book>());
		}

		if (total < ConstantData.PAGE_SIZE && result.getItems().size() > 0) {
			total = result.getItems().size();
		}
		result.setTotal(total);

		return result;
	}

	private ArrayList<Book> parserData(JSONArray array) throws JSONException {
		ArrayList<Book> lists = new ArrayList<Book>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject item = array.getJSONObject(i);
			Book book = new Book();
			book.setBookId(item.optString("book_id"));
			book.setSid(item.optString("sina_id"));
			book.setTitle(item.optString("title"));
			// book.setIntro(item.optString("intro"));
			book.getDownloadInfo().setImageUrl(item.optString("img"));
			book.setAuthor(item.optString("author"));
			if (book.getAuthor() == null) {
				book.setAuthor(item.optString("penname"));
			}
			book.setBookCate(item.optString("cate_name"));
			book.setBookSrc(item.optString("src"));
			book.setIntro(item.optString("intro"));
			book.getBuyInfo().setPayType(item.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
			book.getBuyInfo().setPrice(item.optDouble("price", 0));
			// 修复：BugID=21473
			book.getBuyInfo().setStatusInfo(item.optString("status", Book.STATUS_FINISH_NEW));
			// 添加套餐id字段
			String suiteId = item.optString("suite_id");
			if (!Utils.isEmptyString(suiteId)) {
				book.setSuiteId(Integer.parseInt(suiteId));
				book.setOriginSuiteId(Integer.parseInt(suiteId));
			}

			JSONObject lastChapter = item.optJSONObject("last_chapter");
			if (lastChapter != null) {
				book.setUpdateChapterNameServer(lastChapter.optString("title"));
				book.setUpdateTimeServer(lastChapter.optString("updatetime"));
			}
			// book总共多少章，即更新到哪一章
			book.setNum(Integer.parseInt(item.optString("chapter_amount")));

			JSONArray array2 = item.optJSONArray("tags");
			StringBuilder str = new StringBuilder();
			if (array2 != null) {
				for (int j = 0; j < array2.length(); j++) {
					if (!"".equals(array2.getString(j))) {
						str.append(array2.getString(j).replaceAll(BOOK_DETAIL_TAG_PATTERN, "")).append(" ");
					}
				}
				book.setContentTag(str.toString());
			}

			lists.add(book);
		}
		// 直接返回结果，不进行排序
		// Collections.sort(lists, comparatorBook);
		return lists;
	}

}