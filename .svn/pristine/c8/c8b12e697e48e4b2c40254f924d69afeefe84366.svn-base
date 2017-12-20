package com.sina.book.parser;

import com.sina.book.data.Book;
import com.sina.book.data.PartitionDataResult;
import com.sina.book.data.PartitionItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PartitionParser extends BaseParser {

    /**
     * 添加一个搜索结果的简单排序，<br>
     * 尽量的将vip收费的书往前靠
     */
    private Comparator<PartitionItem> comparatorPartition = new Comparator<PartitionItem>() {

        @Override
        public int compare(PartitionItem partitionItem1,
                PartitionItem partitionItem2) {
            if (partitionItem1 == null || partitionItem2 == null) {
                return 0;
            }
            String partitionName1 = partitionItem1.getName();
            String partitionName2 = partitionItem2.getName();

            int partitionInt1 = getPartitionInt(partitionName1);
            int partitionInt2 = getPartitionInt(partitionName2);

            return partitionInt2 - partitionInt1;
        }

        public int getPartitionInt(String partitionName) {
            if (partitionName.equals("都市")) {
                return 5;
            } else if (partitionName.equals("言情")) {
                return 4;
            } else if (partitionName.equals("官场")) {
                return 3;
            } else if (partitionName.equals("军事")) {
                return 2;
            } else {
                return 1;
            }
        }
    };

    @Override
    protected Object parse(String jsonString) throws JSONException {
        PartitionDataResult result = new PartitionDataResult();
        ArrayList<PartitionItem> lists = new ArrayList<PartitionItem>();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONArray array = obj.optJSONArray("types");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                PartitionItem item = new PartitionItem();
                JSONObject itemObj = array.getJSONObject(i);
                item.setId(itemObj.optString("id"));
                item.setName(itemObj.optString("title"));
                item.setImg(itemObj.optString("icon"));
				item.setIsFavorite(itemObj.optBoolean("is_choose", false));
                JSONArray content = itemObj.optJSONArray("content");
                if (content != null) {
                    item.addBookToList(parserArray(content));
                }
                lists.add(item);
            }
        }
        Collections.sort(lists, comparatorPartition);
        result.addItems(lists);

        // 解析男生女生
        JSONArray recommends = obj.optJSONArray("today_recommend");
        if (recommends != null) {
            for (int i = 0; i < recommends.length(); i++) {
                JSONObject object = recommends.getJSONObject(i);

                PartitionDataResult.RecommendCate cate = result.newRcommendCateInstance();

                cate.name = object.optString("name");
                cate.type = object.optString("index_type");
                cate.total = object.optInt("total");

                JSONArray books = object.optJSONArray("books");
                cate.books = parserBookArray(books);

                result.addRecommendCate(cate);
            }
        }

        return result;
    }

    /**
     * @param bookArray
     * @throws JSONException
     */
    private ArrayList<Book> parserArray(JSONArray bookArray) throws JSONException {
        ArrayList<Book> books = new ArrayList<Book>();
        for (int j = 0; j < bookArray.length(); j++) {
            Book book = new Book();
            JSONObject bitem = bookArray.getJSONObject(j);
            book.setTitle(bitem.optString("title"));
            book.getDownloadInfo().setImageUrl(bitem.optString("img"));
            books.add(book);
        }
        return books;
    }

    private ArrayList<Book> parserBookArray(JSONArray bookArray) throws JSONException {
        ArrayList<Book> books = new ArrayList<Book>();
        if (null == bookArray) return books;

        for (int i = 0; i < bookArray.length(); i++) {
            Book book = new Book();
            JSONObject item = bookArray.optJSONObject(i);

            book.setBookId(item.optString("bid"));
            book.setSid(item.optString("sid"));
            book.setBookSrc(item.optString("src"));

            book.setTitle(item.optString("title"));
            book.setAuthor(item.optString("author"));
            book.getDownloadInfo().setImageUrl(item.optString("img"));

            books.add(book);
        }
        return books;
    }
}
