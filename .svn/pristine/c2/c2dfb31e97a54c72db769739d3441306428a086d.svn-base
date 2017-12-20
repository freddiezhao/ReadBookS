package com.sina.book.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.MyCollectedBooks;

public class CollectedBooksParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        if (jsonString != null) {
            JSONObject obj = new JSONObject(jsonString);
            MyCollectedBooks collectedBooks = new MyCollectedBooks();
            collectedBooks.setTotal(obj.getInt("total"));
            collectedBooks.setList(parserBooks(obj.getJSONArray("books")));
            return collectedBooks;
        }
        return null;
    }

    private List<Book> parserBooks(JSONArray jsonArray) throws JSONException {
        if (jsonArray != null && jsonArray.length() != 0) {
            List<Book> booksList = new ArrayList<Book>();
            Book book;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                book = new Book();
                book.setSid(item.optString("sid"));
                book.setBookId(item.optString("bid"));
                book.setTitle(item.optString("title"));
                book.setAuthor(item.optString("author"));
                try {
                    book.getDownloadInfo().setImageUrl(
                            URLDecoder.decode(item.optString("img"), HTTP.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    book.getDownloadInfo().setImageUrl(item.optString("img"));
                }
                book.setIntro(item.optString("intro"));
                book.setBookSrc(item.optString("src"));
                book.setBagId(item.optString("bag_id"));
                
                // 是否图文书籍
                book.setIsHtmlRead(item.optInt("kind") == 7);
                
                booksList.add(book);
            }
            return booksList;
        }
        return null;
    }

}
