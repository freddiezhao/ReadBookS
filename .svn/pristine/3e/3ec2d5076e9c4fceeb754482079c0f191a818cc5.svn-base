package com.sina.book.parser;

import com.sina.book.data.Book;
import com.sina.book.data.RecommendAuthorListItem;
import com.sina.book.data.RecommendAuthorListResult;
import com.sina.book.util.NumericHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AuthorListParser extends BaseParser {

    private RecommendAuthorListResult mResult;

    @Override
    protected RecommendAuthorListResult parse(String jsonString) throws JSONException {
        if (jsonString != null && jsonString.startsWith("{") && jsonString.endsWith("}")) {
            parseDataContent(jsonString);
            JSONObject json = new JSONObject(jsonString);
            mResult = parseRecommendAuthorList(json);
        }
        return mResult;
    }

    private RecommendAuthorListResult parseRecommendAuthorList(JSONObject json) throws JSONException {
        RecommendAuthorListResult remAuthorResult = new RecommendAuthorListResult();
        remAuthorResult.code = NumericHelper.parseInt(parseCode(json), -1);
        remAuthorResult.msg = parseMsg(json);

        remAuthorResult.setTitle(json.optString("title"));
        remAuthorResult.setName(json.optString("name"));
        remAuthorResult.setTotal(json.optInt("author_total"));

        // 解析Map数据
        // "author_id": "3",
        // "author_name": "张小娴",
        // "author_intro": "工作聯繫：陳仲明",
        // "profile_url":
        // "http://tp2.sinaimg.cn/1816011541/50/40037057877/0",
        // "is_show": 1,
        // "books_total": 3,
        Object authorListObj = json.get("author_list");
        if (authorListObj != null && authorListObj instanceof JSONArray) {
            JSONArray authorArray = (JSONArray) authorListObj;
            for (int i = 0; i < authorArray.length(); i++) {
                JSONObject obj = authorArray.optJSONObject(i);
                remAuthorResult.addItem(parserSingleAuthorItem(obj));
            }
        } else if (authorListObj != null && authorListObj instanceof JSONObject) {
            JSONObject obj = (JSONObject) authorListObj;
            remAuthorResult.addItem(parserSingleAuthorItem(obj));
        }
        return remAuthorResult;
    }

    private RecommendAuthorListItem parserSingleAuthorItem(JSONObject obj) throws JSONException {
        RecommendAuthorListItem item = new RecommendAuthorListItem();
        item.map.put("author_id", obj.optString("author_id"));
        item.map.put("author_name", obj.optString("author_name"));
        item.map.put("author_intro", obj.optString("author_intro"));
        item.map.put("profile_url", obj.optString("profile_url"));
        item.map.put("is_show", obj.optInt("is_show"));
        item.map.put("books_total", obj.optInt("books_total"));
        // 解析书籍
        item.addItems(parseBooks(obj));
        return item;
    }

    private ArrayList<Book> parseBooks(JSONObject object) throws JSONException {
        ArrayList<Book> lists = new ArrayList<Book>();
        JSONArray array = object.optJSONArray("books");
        if (null != array) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Book book = new Book();
                book.setBookId(item.optString("book_id"));
                book.getBuyInfo().setVip(item.optString("is_vip"));
                book.getBuyInfo().setPayType(item.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
                book.getBuyInfo().setPrice(item.optDouble("price", 0));
                book.setIntro(item.optString("intro"));
                book.setTitle(item.optString("title"));
                book.setUpdateTimeServer(item.optString("updatetime"));
                book.getDownloadInfo().setImageUrl(item.optString("img"));
                book.getBuyInfo().setStatusInfo(item.optString("status"));
                book.setPraiseNum(item.optLong("praise_num"));
                book.setBookSrc(item.optString("src"));
                book.setSid(item.optString("sina_id"));
                book = parseLastChapter(item.optJSONObject("last_chapter"), book);
                book.setBookCate(item.optString("cate_name"));
                book.setBookCateId(item.optString("cate_id"));
                // maybe have
                book.setNum(item.optInt("chapter_total"));
                if (book.getNum() <= 0) {
                    book.setNum(item.optInt("chapter_num"));
                }
                if (book.getNum() <= 0) {
                    book.setNum(item.optInt("chapter_amount"));
                }
                book.setAuthor(item.optString("author"));
                lists.add(book);
            }
        }
        return lists;
    }

    private Book parseLastChapter(JSONObject chapterObject, Book book) {
        if (chapterObject != null) {

            book.getBookUpdateChapterInfo().setGlobalId(chapterObject.optInt("chapter_id"));
            book.getBookUpdateChapterInfo().setTitle(chapterObject.optString("title"));
            book.getBookUpdateChapterInfo().setVip(chapterObject.optString("is_vip"));
            book.getBookUpdateChapterInfo().setUpdateTime(chapterObject.optString("updatetime"));
        }
        return book;
    }
}
