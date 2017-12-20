package com.sina.book.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.CommonRecommendItem;
import com.sina.book.data.CommonRecommendResult;
import com.sina.book.data.UserInfoRec;

public class CommonRecommendParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		parseDataContent(jsonString);
		CommonRecommendResult result = null;

		JSONObject object = new JSONObject(jsonString);
		if (null != object) {
			result = new CommonRecommendResult();
			result.setTotal(object.optInt("total"));
			JSONArray array = object.optJSONArray("data");

			if (null != array) {
				for (int i = 0; i < array.length(); i++) {
					CommonRecommendItem item = new CommonRecommendItem();
					JSONObject obj = array.getJSONObject(i);
					item.setRecommendTime(obj.optString("recommend_time"));
					item.setUserInfoRec(parseUserInfoRec(obj.optJSONObject("users")));
					item.setBook(parseBookInfo(obj));

					result.addItem(item);
				}
			}
		}

		// 解析完之后对数据进行一次判定
		if (result.getTotal() == 0 || result.getItem().size() == 0) {
			result = null;
		}

		return result;

	}

	private UserInfoRec parseUserInfoRec(JSONObject obj) throws JSONException {
		if (null == obj) {
			return null;
		}
		UserInfoRec userInfoRec = new UserInfoRec();
		userInfoRec.setUid(obj.optString("uid"));
		userInfoRec.setUserProfileUrl(obj.optString("profile_image_url"));
		userInfoRec.setuName(obj.optString("screen_name"));

		return userInfoRec;
	}

	private Book parseBookInfo(JSONObject obj) throws JSONException {
		if (null == obj) {
			return null;
		}

		Book book = new Book();

		book.setBookId(obj.optString("bid"));
		book.setSid(obj.optString("sid"));
		book.setBookSrc(obj.optString("src"));
		book.setTitle(obj.optString("title"));
		book.setAuthor(obj.optString("author"));
		book.setIntro(obj.optString("intro"));
		book.getDownloadInfo().setImageUrl(obj.optString("img"));
		book.setBookCate(obj.optString("cate_name"));
		book.setBookCateId(obj.optString("cate_id"));
		book.setNum(obj.optInt("chapter_total"));
		book.setPraiseNum(obj.optLong("praise_num", 0));
		book.setCommentNum(obj.optLong("comment_num", 0));

		JSONObject chapterObj = obj.optJSONObject("last_chapter");
		if (null != chapterObj) {
			book.setUpdateTimeServer(chapterObj.optString("updatetime"));
			book.setUpdateChapterNameServer(chapterObj.optString("title"));
		}

		return book;
	}

}