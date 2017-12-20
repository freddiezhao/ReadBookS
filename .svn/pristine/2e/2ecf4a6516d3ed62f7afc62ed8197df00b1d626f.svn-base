package com.sina.book.parser;

import com.sina.book.data.Book;
import com.sina.book.data.PartitionDetailTopResult;
import com.sina.book.ui.PartitionDetailActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PartitionDetailTopParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        PartitionDetailTopResult partitionDetailTopResult = new PartitionDetailTopResult();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray topBooksArray = obj.optJSONArray("tops");
        if (topBooksArray != null) {
            for (int i = 0; i < topBooksArray.length(); i++) {
                JSONObject booksObj = topBooksArray.optJSONObject(i);
                ArrayList<Book> books = parseBooks(booksObj);
                int total = booksObj.optInt("total");
                String type = booksObj.optString("type");
                if (PartitionDetailActivity.TYPE_FREE.equalsIgnoreCase(type)) {
                    partitionDetailTopResult.setFreeBooks(books);
                    partitionDetailTopResult.setFreeBooksTotal(total);
                } else if (PartitionDetailActivity.TYPE_NEW.equalsIgnoreCase(type)) {
                    partitionDetailTopResult.setNewBooks(books);
                    partitionDetailTopResult.setNewBooksTotal(total);
                } else if (PartitionDetailActivity.TYPE_RANK
                        .equalsIgnoreCase(type)) {
                    partitionDetailTopResult.setRankBooks(books);
                    partitionDetailTopResult.setRankBooksTotal(total);
                }
            }
        }
        return partitionDetailTopResult;
    }

    private ArrayList<Book> parseBooks(JSONObject booksObj)
            throws JSONException {
        ArrayList<Book> books = new ArrayList<Book>();

        JSONArray array = booksObj.optJSONArray("books");
        if (array != null) {
            for (int j = 0; j < array.length(); j++) {
                Book book = new Book();
                JSONObject bitem = array.getJSONObject(j);
                book.setSid(bitem.optString("sid"));
                if (book.getSid() == null || book.getSid().trim().length() == 0) {
                    book.setSid(bitem.optString("sina_id"));
                }
                book.setBookId(bitem.optString("bid"));
                if (book.getBookId() == null
                        || book.getBookId().trim().length() == 0) {
                    book.setBookId(bitem.optString("book_id"));
                }
                book.setTitle(bitem.optString("title"));
                book.setAuthor(bitem.optString("author"));
                book.setIntro(bitem.optString("intro"));
                book.getBuyInfo().setPrice(bitem.optDouble("price", 0));
                book.getBuyInfo().setPayType(
                        bitem.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
                book.getBuyInfo().setStatusInfo(bitem.optString("status"));
                book.getDownloadInfo().setImageUrl(bitem.optString("img"));
                book.setBookSrc(bitem.optString("src"));
                book.setNum(bitem.optInt("chapter_total"));
                book.setUpdateTimeServer(bitem.optString("updatetime"));
                JSONObject lastChapter = bitem.optJSONObject("last_chapter");
                if (lastChapter != null) {
                    book.setUpdateChapterNameServer(lastChapter
                            .optString("title"));
                }
                books.add(book);
            }
        }
        return books;
    }

}
