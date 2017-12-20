package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.FamousRecommendDetail;
import com.sina.book.data.ObjResult;

public class FamousDetailParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        ObjResult<FamousRecommendDetail> result = new ObjResult<FamousRecommendDetail>();
        parseDataContent(jsonString);
        result.setMsg(getMsg());
        result.setRetcode(getCode());
        if (!getCode().equals(ConstantData.CODE_SUCCESS)) {
            return result;
        }
        JSONObject obj = new JSONObject(jsonString);

        // user
        JSONObject itemUser = obj.optJSONObject("user");
        FamousRecommendDetail famousRecommend = new FamousRecommendDetail();
        famousRecommend.setUid(itemUser.optString("uid"));
        famousRecommend.setHeadUrl(itemUser.optString("profile_image_url"));
        famousRecommend.setScreenName(itemUser.optString("screen_name"));
        famousRecommend.setIntro(itemUser.optString("intro"));
        famousRecommend.setListCount(itemUser.optInt("list_count"));

        // books
        JSONArray array = obj.optJSONArray("timeline");
        ArrayList<Book> books = new ArrayList<Book>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            JSONObject bookItem = item.optJSONObject("book");
            Book book = new Book();
            book.setBookId(bookItem.optString("bid"));
            book.setSid(bookItem.optString("sid"));
            book.setBookSrc(bookItem.optString("src"));
            book.setTitle(bookItem.optString("title"));
            book.getDownloadInfo().setImageUrl(bookItem.optString("img"));
            book.getBuyInfo().setPayType(bookItem.optInt("paytype",
                    Book.BOOK_TYPE_CHAPTER_VIP));
            book.setIntro(bookItem.optString("intro"));
            book.setComment(item.optString("comment"));
            books.add(book);
        }
        famousRecommend.addBooks(books);
        result.setObj(famousRecommend);
        return result;
    }
}
