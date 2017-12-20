package com.sina.book.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.sina.book.data.AuthorPageResult;
import com.sina.book.data.Book;
import com.sina.book.data.RecommendAuthorResult;

/**
 * 作家推荐页parser
 * 
 * @author Tsimle
 * 
 */
public class AuthorRecommendParser extends BaseParser {
    private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        JSONObject wholeObj = new JSONObject(jsonString);
        RecommendAuthorResult authorResult = new RecommendAuthorResult();
        authorResult.setCount(wholeObj.optInt("total"));

        JSONArray authorArray = wholeObj.optJSONArray("authors");
        if (null != authorArray) {
            for (int i = 0; i < authorArray.length(); i++) {
                JSONObject userObj = authorArray.optJSONObject(i);

                AuthorPageResult author = new AuthorPageResult();
                parseAuthorInfo(author, userObj.optJSONObject("user"));
                parseBookArray(author, userObj.optJSONArray("books"));

                authorResult.addData(author);
            }
        }
        return authorResult;
    }

    private void parseAuthorInfo(AuthorPageResult result, JSONObject obj) {
        result.setUid(obj.optString("uid"));
        result.setName(obj.optString("screen_name"));
        result.setImgUrl(obj.optString("profile_image_url"));
        result.setIntro(obj.optString("intro"));
        result.setFansCount(obj.optInt("followers_count"));
        result.setBookCount(obj.optInt("list_count"));
        result.setTag(obj.optString("pic_tag"));
    }

    private void parseBookArray(AuthorPageResult result, JSONArray array) throws JSONException {
        if (array != null) {
            for (int j = 0; j < array.length(); j++) {
                Book book = new Book();
                JSONObject item = array.getJSONObject(j);

                book.setBookId(item.optString("bid"));
                book.setTitle(item.optString("title"));
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
