package com.sina.book.parser;

import android.text.TextUtils;
import com.sina.book.data.Book;
import com.sina.book.data.TopicItem;
import com.sina.book.data.TopicResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 专题详情Parser
 *
 * @author MarkMjw
 * @date 13-12-12.
 */
public class TopicParser extends BaseParser {
    private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";

    @Override
    protected Object parse(String jsonString) throws JSONException {
        TopicResult result = new TopicResult();

        parseDataContent(jsonString);

        JSONObject obj = new JSONObject(jsonString);

        JSONObject topic = obj.optJSONObject("topic");
        JSONObject topicList = obj.optJSONObject("topic_list");

        if (null != topic) {
            // 加载更多专题时没有此项数据
            parseTopic(result, topic);
        }

        if (null != topicList) {
            // 最新专题才有数据
            parseTopicList(result, topicList);
        }

        return result;
    }

    private void parseTopic(TopicResult result, JSONObject topic) throws JSONException {
        TopicItem item = new TopicItem();
        item.setTitle(topic.optString("title"));
        item.setIntro(topic.optString("intro"));
        item.setImgUrl(topic.optString("img"));
        item.setTopicId(topic.optInt("tid"));

        JSONArray topicCard = topic.optJSONArray("topic_card");
        parseTopicCard(item, topicCard);

        result.setTopic(item);
    }

    private void parseTopicCard(TopicItem item, JSONArray array) throws JSONException {
        List<TopicItem.TopicBook> books = new ArrayList<TopicItem.TopicBook>();

        if (null != array) {
            for (int i = 0; i < array.length(); i++) {
                TopicItem.TopicBook book = item.newTopicBook();
                JSONObject json = array.getJSONObject(i);
                book.setName(json.optString("name"));
                book.setBooks(parseBookArray(json.optJSONArray("books")));

                books.add(book);
            }
        }

        item.setTopicBooks(books);
    }

    private void parseTopicList(TopicResult result, JSONObject topicList) throws JSONException {
        List<TopicItem> topics = new ArrayList<TopicItem>();

        JSONArray array = topicList.optJSONArray("list");
        if (null != array) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);

                TopicItem item = new TopicItem();
                item.setTitle(json.optString("title"));
                item.setIntro(json.optString("intro"));
                item.setImgUrl(json.optString("img"));
                item.setTopicId(json.optInt("tid"));

                topics.add(item);
            }
        }

        result.setTotal(topicList.optInt("total"));

        result.setTopics(topics);
    }

    private List<Book> parseBookArray(JSONArray bookArray) throws JSONException {
        List<Book> books = new ArrayList<Book>();
        if (null != bookArray) {
            for (int i = 0; i < bookArray.length(); i++) {
                Book book = new Book();
                JSONObject json = bookArray.getJSONObject(i);
                book.setSid(json.optString("sid"));
                if (book.getSid() == null || book.getSid().trim().length() == 0) {
                    book.setSid(json.optString("sina_id"));
                }
                book.setBookId(json.optString("bid"));
                if (book.getBookId() == null || book.getBookId().trim().length() == 0) {
                    book.setBookId(json.optString("book_id"));
                }
                book.setTitle(json.optString("title"));
                book.setAuthor(json.optString("author"));
                book.setIntro(json.optString("intro"));
                book.getBuyInfo().setPrice(json.optDouble("price", 0));
                book.getBuyInfo().setPayType(json.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
                book.getBuyInfo().setStatusInfo(json.optString("status"));
                book.setType(json.optString("cate_name"));
                book.getDownloadInfo().setImageUrl(json.optString("img"));
                book.setBookSrc(json.optString("src"));

                //确保解析出更新时间
                book.setUpdateTimeServer(json.optString("updatetime"));

                // maybe have
                book.setNum(json.optInt("chapter_total"));
                if (book.getNum() <= 0) {
                    book.setNum(json.optInt("chapter_amount"));
                }

                JSONObject lastChapter = json.optJSONObject("last_chapter");
                if (lastChapter != null) {
                    book.setUpdateChapterNameServer(lastChapter.optString("title"));
                    if (TextUtils.isEmpty(book.getUpdateTimeServer())) {
                        book.setUpdateTimeServer(lastChapter.optString("updatetime"));
                    }
                }

                book.setFlag(json.optString("flag"));
                book.setPraiseNum(json.optLong("praise_num"));
                book.setCommentNum(json.optLong("comment_num"));

                JSONArray tags = json.optJSONArray("tags");
                StringBuilder str = new StringBuilder();
                if (tags != null) {
                    for (int j = 0; j < tags.length(); j++) {
                        String tag = tags.getString(j);
                        if (!TextUtils.isEmpty(tag)) {
                            str.append(tag.replaceAll(BOOK_DETAIL_TAG_PATTERN, "")).append(" ");
                        }
                    }
                    book.setContentTag(str.toString());
                }

                books.add(book);
            }
        }
        return books;
    }

}
