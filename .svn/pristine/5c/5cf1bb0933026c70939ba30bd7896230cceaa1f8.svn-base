package com.sina.book.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sina.book.data.AuthorPageResult;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.data.RecommendAuthorListResult;
import com.sina.book.data.RecommendAuthorResult;
import com.sina.book.data.RecommendBannerItem;
import com.sina.book.data.RecommendCateItem;
import com.sina.book.data.RecommendCateResult;
import com.sina.book.data.RecommendEpubResult;
import com.sina.book.data.RecommendFamousItem;
import com.sina.book.data.RecommendFamousResult;
import com.sina.book.data.RecommendFreeResult;
import com.sina.book.data.RecommendGoodBookResult;
import com.sina.book.data.RecommendHotResult;
import com.sina.book.data.RecommendMonthItem;
import com.sina.book.data.RecommendMonthResult;
import com.sina.book.data.RecommendNewResult;
import com.sina.book.data.RecommendResult;
import com.sina.book.data.RecommendToday;
import com.sina.book.data.RecommendTodayFree;
import com.sina.book.data.RecomondBannerResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecommendParser extends BaseParser {
    private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";

    @Override
    protected Object parse(String jsonString) throws JSONException {
        RecommendResult result = new RecommendResult();
        parseDataContent(jsonString);

        JSONObject obj = new JSONObject(jsonString);

        JSONObject index = obj.optJSONObject("index");
        // 名人推荐
        JSONObject famous = obj.optJSONObject("timeline");
        // 包月畅读
        JSONObject month = obj.optJSONObject("suite");

        JSONArray today = obj.optJSONArray("today_recommend");//男生爱看，女生爱看
        // 热门分类
        JSONArray cates = obj.optJSONArray("types");
        JSONObject authors = obj.optJSONObject("authortimeline");
        // 明星作家
        JSONObject remAuthorList = obj.optJSONObject("reconmend_author");
        // 图文精装专区
        JSONObject epubRecommend = obj.optJSONObject("epub_recommend");
        //今日限免
        JSONObject toadyFreeModule = obj.optJSONObject("free_recommend");

        JSONObject cateModule = obj.optJSONObject("cate");//热门分类名称,为了与老版本兼容，单独写了一个空module

        result = parseRecommendIndex(result, index);

        result = parseRecommendFamous(result, famous);

        result = parseRecommendMounth(result, month);

        result = parseToadyFreeModule(result, toadyFreeModule);

        result = parseRecommendToday(result, today);//把出版加入到热门分类

        parseRecommendBoyAndGirl(result, today);

        result = parseRecommendCate(result, cates);
        RecommendCateResult cateResult = result.getCates();
        if (cateResult != null && cateModule != null) {
            cateResult.setTitle(cateModule.optString("title"));
        }

        result = parseRecommendAuthor(result, authors);

        // result = parseRecommendAuthorList(result, remAuthorList);
        if (remAuthorList != null) {
            RecommendAuthorListResult recommendAuthorListResult = new AuthorListParser().parse(remAuthorList.toString());
            result.setRecommendAuthorListResult(recommendAuthorListResult);
        }

        //解析图文精装专区
        result = parseRecommendEpub(result, epubRecommend);

        return result;
    }

    private void parseRecommendBoyAndGirl(RecommendResult result, JSONArray today) {
        RecommendToday recommendToday = null;
        Gson gson = new Gson();
        try {
            if (today != null && today.length() > 0) {
                for (int i = 0; i < today.length(); i++) {
//                    recommendToday = gson.fromJson(today.opt(i).toString(), RecommendToday.class);
                    recommendToday = new RecommendToday();
                    JSONObject jsonObject = (JSONObject) today.opt(i);
                    String indexTYpe = jsonObject.optString("index_type");
                    recommendToday.setIndex_type(indexTYpe);
                    recommendToday.setTitle(jsonObject.optString("title"));
                    recommendToday.setName(jsonObject.optString("name"));
                    recommendToday.setTotal(jsonObject.optInt("total"));
                    List<Book> books = parserBookArray(jsonObject.optJSONArray("books"));
                    recommendToday.setBooks(books);
                    if ("boy".equals(indexTYpe)) {
                        result.setRecommendBoy(recommendToday);
                    } else if ("girl".equals(indexTYpe)) {
                        result.setRecommendGirl(recommendToday);
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private RecommendResult parseToadyFreeModule(RecommendResult result, JSONObject toadyFreeModule) {
//        Gson gson = new Gson();
//        RecommendTodayFree todayFree = gson.fromJson(toadyFreeModule.toString(), RecommendTodayFree.class);
        if (toadyFreeModule == null)
            return result;
        try {
            RecommendTodayFree todayFree = new RecommendTodayFree();
            todayFree.setTitle(toadyFreeModule.optString("title"));
            todayFree.setName(toadyFreeModule.optString("name"));
            List<Book> books = parserBookArray(toadyFreeModule.optJSONArray("books"));
            todayFree.setBooks(books);
            result.setToadyFree(todayFree);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private RecommendResult parseRecommendIndex(RecommendResult result, JSONObject index) throws JSONException {
        RecomondBannerResult bannerResult = new RecomondBannerResult();
        RecomondBannerResult cmreadResult = new RecomondBannerResult();
        RecommendHotResult hotResult = new RecommendHotResult();
        RecommendNewResult newResult = new RecommendNewResult();
        RecommendFreeResult freeResult = new RecommendFreeResult();
        RecommendGoodBookResult bookResult = new RecommendGoodBookResult();

        ArrayList<RecommendBannerItem> banners = new ArrayList<RecommendBannerItem>();
        ArrayList<RecommendBannerItem> cmreads = new ArrayList<RecommendBannerItem>();
        ArrayList<Book> hotBooks = null;
        ArrayList<Book> perfectBooks = null;
        ArrayList<Book> freeBooks = null;

        JSONArray recommendArray = index.optJSONArray("recommend");

        JSONArray hotBooksArray = index.optJSONArray("items");

        JSONArray goodBookArray = index.optJSONArray("recommendpush");

        JSONArray cmreadArray = index.optJSONArray("cmread_card");

        if (recommendArray != null) {
            for (int i = 0; i < recommendArray.length(); i++) {
                RecommendBannerItem item = new RecommendBannerItem();

                JSONObject itemObj = recommendArray.optJSONObject(i);
                String type = itemObj.optString("type");
                ArrayList<Book> books = null;
                int topicId = -1;
                String url = "";

                if (RecommendBannerItem.BANNER_LIST.equals(type)) {
                    JSONArray bookArray = itemObj.optJSONArray("books");
                    if (bookArray != null) {
                        books = parserBookArray(bookArray);
                    } else {
                        books = new ArrayList<Book>();
                    }
                } else if (RecommendBannerItem.BANNER_ONE.equals(type)) {
                    books = new ArrayList<Book>();
                    Book book = new Book();
                    book.setBookId(itemObj.optString("bid"));
                    book.setSid(itemObj.optString("sid"));
                    book.setBookSrc(itemObj.optString("src"));
                    books.add(book);

                } else if (RecommendBannerItem.BANNER_TWO.equals(type)) {
                    topicId = itemObj.optInt("tid");

                } else if (RecommendBannerItem.BANNER_THREE.equals(type)) {
                    url = itemObj.optString("url");

                } else {
                    // ignore this type
                    continue;
                }
                item.setType(type);
                item.setTags(itemObj.optString("tags"));
                item.setImageUrl(itemObj.optString("img"));
                item.setBooks(books);
                item.setTopicId(topicId);
                item.setUrl(url);
                banners.add(item);
            }
        }

        if (cmreadArray != null) {
            for (int i = 0; i < cmreadArray.length(); i++) {
                RecommendBannerItem item = new RecommendBannerItem();

                JSONObject itemObj = cmreadArray.optJSONObject(i);
                String type = itemObj.optString("type");
                ArrayList<Book> books = null;
                int topicId = -1;
                String url = "";

                if (RecommendBannerItem.BANNER_LIST.equals(type)) {
                    JSONArray bookArray = itemObj.optJSONArray("books");
                    if (bookArray != null) {
                        books = parserBookArray(bookArray);
                    } else {
                        books = new ArrayList<Book>();
                    }
                } else if (RecommendBannerItem.BANNER_ONE.equals(type)) {
                    books = new ArrayList<Book>();
                    Book book = new Book();
                    book.setBookId(itemObj.optString("bid"));
                    book.setSid(itemObj.optString("sid"));
                    book.setBookSrc(itemObj.optString("src"));
                    books.add(book);

                } else if (RecommendBannerItem.BANNER_TWO.equals(type)) {
                    topicId = itemObj.optInt("tid");
                    JSONArray bookArray = itemObj.optJSONArray("books");
                    if (bookArray != null) {
                        books = parserBookArray(bookArray);
                    } else {
                        books = new ArrayList<Book>();
                    }
                } else if (RecommendBannerItem.BANNER_THREE.equals(type)) {
                    url = itemObj.optString("url");

                } else {
                    // ignore this type
                    continue;
                }
                item.setType(type);
                item.setTags(itemObj.optString("tags"));
                item.setImageUrl(itemObj.optString("img"));
                item.setItemName(itemObj.optString("item_name"));
                item.setTotal(itemObj.optInt("total", 0));
                item.setBooks(books);
                item.setTopicId(topicId);
                item.setUrl(url);
                cmreads.add(item);
            }
        }

        if (hotBooksArray != null) {
            for (int i = 0; i < hotBooksArray.length(); i++) {
                JSONObject itemObj = hotBooksArray.optJSONObject(i);
                if ("编辑推荐".equals(itemObj.optString("item_name"))) {
                    // if (i == 0) {
                    // 编辑推荐
                    JSONArray bookArray = itemObj.optJSONArray("books");
                    if (bookArray != null) {
                        hotBooks = parserBookArray(bookArray);
                    } else {
                        hotBooks = new ArrayList<Book>();
                    }
                    // } else if ("精品好书".equals(itemObj.optString("item_name")))
                    // {
                    hotResult.setTotal(itemObj.optInt("total"));
                    hotResult.setTitle(itemObj.optString("title"));
                } else if ("选你喜欢".equals(itemObj.optString("item_name"))) {
                    // } else if (i == 1) {
                    // 选你喜欢
                    JSONArray bookArray = itemObj.optJSONArray("books");
                    if (bookArray != null) {
                        perfectBooks = parserBookArray(bookArray);
                    } else {
                        perfectBooks = new ArrayList<Book>();
                    }
                    // } else if ("免费专区".equals(itemObj.optString("item_name")))
                    // {
                    newResult.setTotal(itemObj.optInt("total"));
                } else if ("免费专区".equals(itemObj.optString("item_name"))) {
                    // } else if (i == 2) {
                    // 免费专区
                    JSONArray bookArray = itemObj.optJSONArray("books");
                    if (bookArray != null) {
                        freeBooks = parserBookArray(bookArray);
                    } else {
                        freeBooks = new ArrayList<Book>();
                    }
                    freeResult.setTotal(itemObj.optInt("total"));
                    freeResult.setTitle(itemObj.optString("title"));
                }
            }
        }

        if (goodBookArray != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < goodBookArray.length(); i++) {
                JSONObject itemObj = goodBookArray.getJSONObject(i);
                String title = itemObj.optString("title");
                if (!TextUtils.isEmpty(title)) {
                    sb.append(title);
                }
                if (i < (goodBookArray.length() - 1)) {
                    sb.append(" | ");
                }
            }
            bookResult.setBookContent(sb.toString());
        }

        bannerResult.addItems(banners);
        cmreadResult.addItems(cmreads);
        hotResult.addItems(hotBooks);
        newResult.addItems(perfectBooks);
        freeResult.addItems(freeBooks);

        result.setHotBook(hotResult);
        result.setBannerBook(bannerResult);
        result.setCmreadCardBook(cmreadResult);
        result.setFreeBook(freeResult);
        result.setNewBook(newResult);
        result.setGoodBookResult(bookResult);

        return result;
    }

    private ArrayList<Book> parserBookArray(JSONArray bookArray) throws JSONException {
        ArrayList<Book> books = new ArrayList<Book>();
        if (null != bookArray) {
            for (int i = 0; i < bookArray.length(); i++) {
                Book book = new Book();
                JSONObject bitem = bookArray.optJSONObject(i);
                book.setBookId(bitem.optString("bid"));
                book.setSid(bitem.optString("sid"));
                book.setTitle(bitem.optString("title"));
                book.setStatus(bitem.optString("status"));
                book.setAuthor(bitem.optString("author"));
                book.getDownloadInfo().setImageUrl(bitem.optString("img"));
                book.setType(bitem.optString("cate_name"));
                book.setIntro(bitem.optString("intro"));
                book.getBuyInfo().setPayType(bitem.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
                book.getBuyInfo().setPrice(bitem.optDouble("price", 0));
                book.setBookSrc(bitem.optString("src"));
                book.setRecommendIntro(bitem.optString("recommend_intro"));
                book.setRecommend_sub2(bitem.optString("recommend_sub2"));
                book.setRecommend_sub3(bitem.optString("recommend_sub3"));
                book.setRecommend_name(bitem.optString("recommend_name"));
                // 添加套餐id字段
                String suiteId = bitem.optString("suite_id");
                if (!TextUtils.isEmpty(suiteId)) {
                    book.setSuiteId(Integer.parseInt(suiteId));
                    book.setOriginSuiteId(Integer.parseInt(suiteId));
                }

                // 添加是否epub资源
                try {
                    String strEpub = bitem.optString("is_epub");
                    if (!TextUtils.isEmpty(strEpub) && "2".equals(strEpub)) {
                        book.setIsEpub(true);
                    }
                } catch (Exception e) {
                    book.setIsEpub(false);
                }

                book.setPraiseNum(bitem.optLong("praise_num"));
                books.add(book);
            }
        }
        return books;
    }

    private RecommendResult parseRecommendFamous(RecommendResult result, JSONObject famous) throws JSONException {
        RecommendFamousResult famousResult = new RecommendFamousResult();

        famousResult.setName(famous.optString("name"));

        JSONArray users = famous.optJSONArray("users");
        if (null != users) {
            for (int i = 0; i < users.length(); i++) {
                JSONObject object = users.optJSONObject(i);

                RecommendFamousItem item = new RecommendFamousItem();

                item.setUid(object.optString("uid"));
                item.setHeadUrl(object.optString("profile_image_url"));
                item.setScreenName(object.optString("screen_name"));
                item.setIntro(object.optString("intro"));
                item.setBooks(parserBookArray(object.optJSONObject("message").optJSONArray("books")));

                famousResult.addData(item);
            }
        }

        result.setFamous(famousResult);
        return result;
    }

    private RecommendResult parseRecommendMounth(RecommendResult result, JSONObject month) throws JSONException {
        RecommendMonthResult monthResult = new RecommendMonthResult();

        monthResult.setName(month.optString("name"));

        JSONArray suites = month.optJSONArray("suites");
        if (null != suites) {
            for (int i = 0; i < suites.length(); i++) {
                JSONObject object = suites.optJSONObject(i);

                RecommendMonthItem item = new RecommendMonthItem();

                item.setId(object.optString("suite_id"));
                item.setName(object.optString("suite_name"));
                item.setPrice(object.optInt("suite_price", 1000));
                item.setBeginTime(object.optString("suite_begintime"));
                item.setEndTime(object.optString("suite_endtime"));
                item.setIntro(object.optString("intro"));
                item.setShowPrice(object.optString("show_price"));
                item.setTotal(object.optInt("total", 1000));
                item.setIsBuy(object.optString("is_buy"));
                item.setBooks(parserBookArray(object.optJSONArray("books")));

                monthResult.addData(item);
            }
        }

        result.setMounth(monthResult);
        return result;
    }

    private RecommendResult parseRecommendCate(RecommendResult result, JSONArray cates) throws JSONException {
        RecommendCateResult cateResult = result.getCates();

        if (null == cateResult) {
            cateResult = new RecommendCateResult();
        }

        if (null != cates) {
            for (int i = 0; i < cates.length(); i++) {
                JSONObject object = cates.optJSONObject(i);
                String code = parseCode(object);
                // 无数据
                if (ConstantData.CODE_DATA_NULL.equals(code))
                    continue;

                RecommendCateItem item = new RecommendCateItem();

                item.setId(object.optString("id"));
                item.setName(object.optString("title"));
                item.setBooks(parserBookArray(object.optJSONArray("content")));

                cateResult.addData(item);
            }
        }

        result.setCates(cateResult);
        return result;
    }

    /**
     * 解析今日推荐-男生女生等加入热门分类中
     *
     * @param result
     * @param today
     * @return
     * @throws JSONException
     */
    private RecommendResult parseRecommendToday(RecommendResult result, JSONArray today) throws JSONException {
        RecommendCateResult cateResult = result.getCates();

        if (null == cateResult) {
            cateResult = new RecommendCateResult();
        }

        if (null != today) {
            for (int i = 0; i < today.length(); i++) {
                JSONObject object = today.optJSONObject(i);
                String code = parseCode(object);
                // 无数据
                if (ConstantData.CODE_DATA_NULL.equals(code))
                    continue;

                RecommendCateItem item = new RecommendCateItem();

                item.setId(object.optString("index_type"));
                if (item.getId().equals("boy") || item.getId().equals("girl")) {//不要男生女生
                    continue;
                }
                item.setName(object.optString("name"));
                item.setBooks(parserBookArray(object.optJSONArray("books")));

                cateResult.addData(item);
            }
        }

        result.setCates(cateResult);
        return result;
    }

    private RecommendResult parseRecommendAuthor(RecommendResult result, JSONObject authors) throws JSONException {
        RecommendAuthorResult authorResult = result.getAuthorResult();

        if (null == authorResult) {
            authorResult = new RecommendAuthorResult();
        }

        authorResult.setName(authors.optString("name"));
        authorResult.setCount(authors.optInt("total"));

        JSONArray authorArray = authors.optJSONArray("authors");
        if (null != authorArray) {
            for (int i = 0; i < authorArray.length(); i++) {
                JSONObject obj = authorArray.optJSONObject(i);

                AuthorPageResult author = new AuthorPageResult();
                parseAuthorInfo(author, obj.optJSONObject("user"));
                parseBookArray(author, obj.optJSONArray("books"));

                authorResult.addData(author);
            }
        }
        result.setAuthorResult(authorResult);
        return result;
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

    // 解析epub专区
    private RecommendResult parseRecommendEpub(RecommendResult result, JSONObject epub) throws JSONException {
        RecommendEpubResult epubReslut = result.getEpubResult();
        if (null == epubReslut) {
            epubReslut = new RecommendEpubResult();
        }
        epubReslut.setName(epub.optString("name"));
        epubReslut.setTotal(epub.optInt("total"));

        JSONArray bookArray = epub.optJSONArray("books");
        ArrayList<Book> books = null;
        if (bookArray != null) {
            books = parserBookArray(bookArray);
        } else {
            books = new ArrayList<Book>();
        }
        epubReslut.addItems(books);
        result.setEpubResult(epubReslut);
        return result;
    }

}