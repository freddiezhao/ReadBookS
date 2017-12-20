package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.SearchData;

/**
 * 搜索页面数据解析
 * 
 * @author YangZhanDong
 * 
 */
public class SearchParser extends BaseParser
{

	@Override
	protected Object parse(String jsonString) throws JSONException
	{
		SearchData result = new SearchData();
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		result.setBook(parseBook(obj.optJSONObject("recommend"), result));
		result.setHotWords(parseHotWords(obj.optJSONObject("hot_word")));
		result.setHotBooks(parseCommonBooks(obj.optJSONObject("hot_books")));
		result.setBookTypeName(parseBookTypeName(obj.optJSONObject("hot_books")));
		result.setBookType("hot_books");

		return result;
	}

	private Book parseBook(JSONObject object, SearchData result) throws JSONException
	{
		Book book = new Book();
		JSONArray array = object.optJSONArray("books");
		if (null != array) {
			JSONObject item = array.getJSONObject(0);
			book.setSid(item.optString("sid"));
			book.setBookId(item.optString("bid"));
			book.setTitle(item.optString("title"));
			book.setAuthor(item.optString("author"));
			book.getDownloadInfo().setImageUrl(item.optString("img"));
			book.setBookCate(item.optString("cate_name"));
			book.setIntro(item.optString("intro"));
			book.getBuyInfo().setPayType(item.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
			book.getBuyInfo().setPrice(item.optDouble("price", 0));
			book.setPraiseNum(item.optLong("praise_num"));
		}

		if (isKeyHasAvailableValue(object, "name")) {
			String recommandName = object.getString("name");
			result.setRecommandName(recommandName);
		}

		return book;
	}

	private ArrayList<String> parseHotWords(JSONObject object) throws JSONException
	{
		ArrayList<String> lists = new ArrayList<String>();
		JSONArray array = object.optJSONArray("words");
		if (null != array) {
			for (int i = 0; i < array.length(); i++) {
				String string = array.getJSONObject(i).optString("word").replaceAll(BOOK_DETAIL_TAG_PATTERN, "");
				if (!string.equals("")) {
					lists.add(string);
				}
			}
		}

		return lists;
	}

	// private ArrayList<Book> parseHotBooks(JSONObject object) throws
	// JSONException
	// {
	// ArrayList<Book> lists = new ArrayList<Book>();
	// JSONArray array = object.optJSONArray("books");
	// if (null != array) {
	// for (int i = 0; i < array.length(); i++) {
	// JSONObject item = array.getJSONObject(i);
	// Book book = new Book();
	// book.setBookId(item.optString("bid"));
	// book.setTitle(item.optString("title"));
	// book.setAuthor(item.optString("author"));
	// book.getBuyInfo().setStatusInfo(item.optString("status"));
	// book.getDownloadInfo().setImageUrl(item.optString("img"));
	// book.setIntro(item.optString("intro"));
	// book.getBuyInfo().setPrice(item.optDouble("price", 0));
	// book.getBuyInfo().setPayType(item.optInt("paytype",
	// Book.BOOK_TYPE_CHAPTER_VIP));
	// book.setBookSrc(item.optString("src"));
	// book.setPraiseNum(item.optLong("praise_num"));
	// book.setCommentNum(item.optLong("comment_num"));
	// book.setNum(item.optInt("chapter_amount"));
	// book.setFlag(item.optString("flag"));
	//
	// book = parseLastChapter(item.optJSONObject("last_chapter"), book);
	//
	// book.setContentTag(parseTags(item.optJSONArray("tags")));
	//
	// lists.add(book);
	// }
	// }
	// return lists;
	// }
	//
	private String parseBookTypeName(JSONObject object)
	{
		String typeName = object.optString("name");
		return typeName;
	}
	//
	// private Book parseLastChapter(JSONObject chapterObject, Book book)
	// {
	// if (chapterObject != null) {
	//
	// book.getBookUpdateChapterInfo().setGlobalId(chapterObject.optInt("chapter_id"));
	// book.getBookUpdateChapterInfo().setTitle(chapterObject.optString("title"));
	// book.getBookUpdateChapterInfo().setVip(chapterObject.optString("is_vip"));
	// book.getBookUpdateChapterInfo().setUpdateTime(chapterObject.optString("updatetime"));
	// }
	//
	// return book;
	// }
	//
	// private String parseTags(JSONArray array) throws JSONException
	// {
	// StringBuilder str = new StringBuilder();
	//
	// if (null != array) {
	// for (int i = 0; i < array.length(); i++) {
	// String tag = array.getString(i).replaceAll(BOOK_DETAIL_TAG_PATTERN, "");
	// if (!TextUtils.isEmpty(tag)) {
	// str.append(tag);
	// str.append(" ");
	// }
	// }
	// }
	//
	// return str.toString();
	// }

}