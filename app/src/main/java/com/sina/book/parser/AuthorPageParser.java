package com.sina.book.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.sina.book.data.AuthorPageResult;
import com.sina.book.data.Book;

/**
 * 作者主页数据解析器
 * 
 * @author MarkMjw
 * @date 13-9-25.
 */
public class AuthorPageParser extends BaseParser {
	private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";

	@Override
	protected Object parse(String jsonString) throws JSONException {
		parseDataContent(jsonString);

		AuthorPageResult result = new AuthorPageResult();

		JSONObject obj = new JSONObject(jsonString);

		if (isKeyHasAvailableValue(obj, "user")) {
			JSONObject author = obj.optJSONObject("user");
			parseAuthorInfo(result, author);
		}

		if (isKeyHasAvailableValue(obj, "books")) {
			JSONArray books = obj.optJSONArray("books");
			parseBookArray(result, books);
		}

		return result;
	}

	private void parseAuthorInfo(AuthorPageResult result, JSONObject obj) {
		result.setUid(obj.optString("uid"));
		result.setName(obj.optString("screen_name"));
		result.setImgUrl(obj.optString("profile_image_url"));
		result.setIntro(obj.optString("intro"));
		result.setFansCount(obj.optInt("followers_count"));
		result.setBookCount(obj.optInt("list_count"));
	}

	private void parseBookArray(AuthorPageResult result, JSONArray array) throws JSONException {
		if (array != null && array.length() != 0) {
			for (int j = 0; j < array.length(); j++) {
				Book book = new Book();
				Object obj = array.get(j);
				if (obj != null && obj instanceof JSONObject) {
					JSONObject item = array.getJSONObject(j);

					book.setSid(item.optString("sid"));
					book.setBookId(item.optString("bid"));
					book.setBookSrc(item.optString("src"));

					book.setTitle(item.optString("title"));
					book.setAuthor(item.optString("author"));
					book.setIntro(item.optString("intro"));
					book.getDownloadInfo().setImageUrl(item.optString("img"));

					book.getBuyInfo().setVip(item.optString("is_vip"));
					book.getBuyInfo().setStatusInfo(item.optString("status"));

					book.setBookCate(item.optString("cate_name"));
					book.setBookCateId(item.optString("cate_id"));

					book.getBuyInfo().setPrice(item.optDouble("price", 0));
					book.getBuyInfo().setPayType(item.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));

					// 确保解析出更新时间
					book.setUpdateTimeServer(item.optString("updatetime"));

					// maybe have
					book.setNum(item.optInt("chapter_total"));
					if (book.getNum() <= 0) {
						book.setNum(item.optInt("chapter_amount"));
					}

					JSONObject lastChapter = item.optJSONObject("last_chapter");
					if (lastChapter != null) {
						book.setUpdateChapterNameServer(lastChapter.optString("title"));
						if (TextUtils.isEmpty(book.getUpdateTimeServer())) {
							book.setUpdateTimeServer(lastChapter.optString("updatetime"));
						}
					}

					book.setPraiseNum(item.optLong("praise_num"));
					book.setCommentNum(item.optLong("comment_num"));

					JSONArray tags = item.optJSONArray("tags");
					StringBuilder str = new StringBuilder();
					if (tags != null) {
						for (int i = 0; i < tags.length(); i++) {
							String tag = tags.getString(i);
							if (!TextUtils.isEmpty(tag)) {
								str.append(tag.replaceAll(BOOK_DETAIL_TAG_PATTERN, "")).append(" ");
							}
						}
						book.setContentTag(str.toString());
					}

					result.addBook(book);
				}
			}
		}
	}
}
