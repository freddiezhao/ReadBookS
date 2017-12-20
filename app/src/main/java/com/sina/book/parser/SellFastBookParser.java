package com.sina.book.parser;

import com.sina.book.data.Book;
import com.sina.book.data.SellFastItem;
import com.sina.book.data.SellFastOlder;
import com.sina.book.data.SellFastResult;
import com.sina.book.useraction.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SellFastBookParser extends BaseParser {
    ArrayList<SellFastOlder> olderList;

    private Comparator<SellFastItem> comparatorSellFast = new Comparator<SellFastItem>() {

        @Override
        public int compare(SellFastItem item1, SellFastItem item2) {
            if (null == item1 || null == item2) {
                return 0;
            }
            String type1 = item1.getItemType();
            String type2 = item2.getItemType();

            int sellFastInt1 = getSellFastInt(type1);
            int sellFastInt2 = getSellFastInt(type2);

            return sellFastInt1 - sellFastInt2;

        }

        public int getSellFastInt(String type) {
            if (null == olderList) {
                return Integer.MAX_VALUE;
            }

            for (int i = 0; i < olderList.size(); i++) {
                if (type.equals(olderList.get(i).getType())) {
                    return olderList.get(i).getIndex();
                }
            }

            return Integer.MAX_VALUE;
        }
    };

    @Override
    protected Object parse(String jsonString) throws JSONException {
        SellFastResult result = new SellFastResult();
        ArrayList<SellFastItem> lists = new ArrayList<SellFastItem>();
        parseDataContent(jsonString);

        JSONObject obj = new JSONObject(jsonString);

        JSONObject recommend = obj.optJSONObject("toplist_recommend");
        if (null != recommend) {
            result.setOperationBooks(parseObject1(recommend));
        }

        JSONObject olderObj = obj.optJSONObject("toplist_order");
        if (null != olderObj) {
            parseObject2(olderObj);
        }

        JSONObject sinaTop = obj.optJSONObject("sina_top");
        if (null != sinaTop) {
            lists.add(parseObject(sinaTop, "sina_top", Constants.PAGE_SELLWELL_SINA,
                    Constants.CLICK_SELLWELL_SINA));
        }

        JSONObject vote = obj.optJSONObject("hot_vote");
        if (null != vote) {
            lists.add(parseObject(vote, "hot_vote", Constants.PAGE_SELLWELL_RECOMMEND,
                    Constants.CLICK_SELLWELL_RECOMMEND));
        }

        JSONObject comment = obj.optJSONObject("hot_comment");
        if (null != comment) {
            lists.add(parseObject(comment, "hot_comment", Constants.PAGE_SELLWELL_COMMENT,
                    Constants.CLICK_SELLWELL_COMMENT));
        }

        JSONObject search = obj.optJSONObject("hot_books");
        if (null != search) {
            lists.add(parseObject(search, "hot_books", Constants.PAGE_SELLWELL_SEARCH,
                    Constants.CLICK_SELLWELL_SEARCH));
        }

        JSONObject newBook = obj.optJSONObject("hot_new");
        if (null != newBook) {
            lists.add(parseObject(newBook, "hot_new", Constants.PAGE_SELLWELL_NEW,
                    Constants.CLICK_SELLWELL_NEW));
        }

        JSONObject classic = obj.optJSONObject("hot_detail");
        if (null != classic) {
            lists.add(parseObject(classic, "hot_detail", Constants.PAGE_SELLWELL_CLASSIC,
                    Constants.CLICK_SELLWELL_CLASSIC));
        }

        JSONObject vipsale = obj.optJSONObject("hot_vipsale");
        if (null != vipsale) {
            lists.add(parseObject(vipsale, "hot_vipsale", Constants.PAGE_SELLWELL_READ,
                    Constants.CLICK_SELLWELL_READ));
        }

        JSONObject free = obj.optJSONObject("free");
        if (null != free) {
            lists.add(parseObject(free, "free", Constants.PAGE_SELLWELL_FREE,
                    Constants.CLICK_SELLWELL_FREE));
        }

        JSONObject all = obj.optJSONObject("hot_endsale");
        if (null != all) {
            lists.add(parseObject(all, "hot_endsale", Constants.PAGE_SELLWELL_ALL,
                    Constants.CLICK_SELLWELL_ALL));
        }

        Collections.sort(lists, comparatorSellFast);
        result.setItems(lists);

        return result;
    }

    private SellFastItem parseObject(JSONObject object, String string, String itemTag,
            String bookTag) throws JSONException {
        SellFastItem item = new SellFastItem();
        parseDataContent(object.toString());

        item.setItemType(string);
        item.setBookType(object.optString("name"));
        item.setBookCount(object.optInt("total"));
        item.setBooks(parseArray(object.optJSONArray("books")));

        item.setItemTag(itemTag);
        item.setBookTag(bookTag);

        return item;
    }

    private ArrayList<Book> parseObject1(JSONObject object) throws JSONException {
        parseDataContent(object.toString());
        JSONArray array = object.optJSONArray("recommend");
        return parseArray(array);
    }

    private void parseObject2(JSONObject object) throws JSONException {
        parseDataContent(object.toString());
        JSONArray olderArray = object.optJSONArray("order");
        if (null != olderArray) {
            olderList = new ArrayList<SellFastOlder>();
            for (int i = 0; i < olderArray.length(); i++) {
                SellFastOlder older = new SellFastOlder();
                JSONObject itemObj = olderArray.getJSONObject(i);
                older.setType(itemObj.optString("top_type"));
                older.setName(itemObj.optString("name"));
                older.setIndex(itemObj.optInt("index"));

                olderList.add(older);
            }
        }
    }

    private ArrayList<Book> parseArray(JSONArray array) throws JSONException {
        ArrayList<Book> books = new ArrayList<Book>();

        if (null != array) {
            for (int i = 0; i < array.length(); i++) {
                Book book = new Book();
                JSONObject itemObj = array.getJSONObject(i);
                book.setSid(itemObj.optString("sid"));
                book.setBookId(itemObj.optString("bid"));
                book.setTitle(itemObj.optString("title"));
                book.setAuthor(itemObj.optString("author"));
                book.setBookCate(itemObj.optString("cate_name"));
                book.setBookCateId(itemObj.optString("cate_id"));
                book.getDownloadInfo().setImageUrl(itemObj.optString("img"));
                book.setIntro(itemObj.optString("intro"));
                book.getBuyInfo().setPrice(itemObj.optDouble("price", 0));
                book.getBuyInfo().setPayType(itemObj.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
                book.setBookSrc(itemObj.optString("src"));
                book.getBuyInfo().setStatusInfo(itemObj.optString("status"));
                book.setPraiseNum(itemObj.optLong("praise_num"));
                book.setCommentNum(itemObj.optLong("comment_num"));

                books.add(book);
            }
        }

        return books;
    }

}