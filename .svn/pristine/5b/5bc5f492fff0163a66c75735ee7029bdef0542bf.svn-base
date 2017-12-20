package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.SquareResult;

public class SquareBookParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        SquareResult result = new SquareResult();
        ArrayList<Book> books = new ArrayList<Book>();
        JSONObject obj = new JSONObject(jsonString);
        result.setTotal(obj.optInt("total", 0));
        JSONArray array = obj.optJSONArray("books");
        if (array != null) {
            for (int j = 0; j < array.length(); j++) {
                Book book = new Book();
                JSONObject bitem = array.getJSONObject(j);
                book.setSid(bitem.optString("sid"));
                if (book.getSid() == null || book.getSid().trim().length() == 0) {
                    book.setSid(bitem.optString("sina_id"));
                }
                book.setBookId(bitem.optString("bid"));
                if (book.getBookId() == null || book.getBookId().trim().length() == 0) {
                    book.setBookId(bitem.optString("book_id"));
                }
                book.setTitle(bitem.optString("title"));
                book.setAuthor(bitem.optString("author"));
                book.setIntro(bitem.optString("intro"));
                book.getBuyInfo().setPrice(bitem.optDouble("price", 0));
                book.getBuyInfo().setPayType(bitem.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
                book.getBuyInfo().setStatusInfo(bitem.optString("status"));
                book.setType(bitem.optString("cate_name"));
                book.getDownloadInfo().setImageUrl(bitem.optString("img"));
                book.setBookSrc(bitem.optString("src"));
                book.setPraiseNum(bitem.optLong("praise_num"));
                book.setCommentNum(bitem.optLong("comment_num"));
                books.add(book);
            }
        }
        result.addItems(books);
        return result;
    }

}
