package com.sina.book.parser;

import android.text.TextUtils;
import com.sina.book.data.Book;
import com.sina.book.data.RankingListResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BookParser extends BaseParser {
    private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        RankingListResult result = new RankingListResult();
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
                
                //确保解析出更新时间
                book.setUpdateTimeServer(bitem.optString("updatetime"));
                
                // maybe have
                book.setNum(bitem.optInt("chapter_total"));
                if (book.getNum() <= 0) {
                    book.setNum(bitem.optInt("chapter_amount"));
                }

                JSONObject lastChapter = bitem.optJSONObject("last_chapter");
                if (lastChapter != null) {
                    book.setUpdateChapterNameServer(lastChapter.optString("title"));
                    if (TextUtils.isEmpty(book.getUpdateTimeServer())) {
                        book.setUpdateTimeServer(lastChapter.optString("updatetime"));
                    }
                }

                book.setFlag(bitem.optString("flag"));
                book.setPraiseNum(bitem.optLong("praise_num"));
                book.setCommentNum(bitem.optLong("comment_num"));

                JSONArray tags = bitem.optJSONArray("tags");
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

                books.add(book);
            }
        }
        result.addItems(books);
        return result;
    }

}
