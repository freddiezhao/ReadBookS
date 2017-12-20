package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ChapterPriceResult;
import com.sina.book.data.PriceTip;
import com.sina.book.data.UserInfoRole;

/**
 * 章节价格信息解析器
 * 
 * @author MarkMjw
 */
public class ChapterPriceParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        ChapterPriceResult result = null;

        JSONObject object = new JSONObject(jsonString);
        if (null != object) {
            result = new ChapterPriceResult();

            JSONObject obj2 = object.optJSONObject("userinfo");
            result.setUserInfoRole(parseUserInfo(obj2));

            JSONObject obj1 = object.optJSONObject("chapter");
            result.setBook(parseBook(obj1));

            Chapter chapter = parseChapter(obj1);
            if (null != chapter) {
                JSONObject priceTipObj = object.optJSONObject("price_tip");
                chapter.setPriceTip(parsePriceTip(priceTipObj));
            }

            result.setChapter(chapter);
        }

        return result;
    }

    private UserInfoRole parseUserInfo(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        UserInfoRole userInfoRole = new UserInfoRole();
        userInfoRole.setRole(obj.optInt("role", UserInfoRole.GENERAL_USER));
        userInfoRole.setRoleName(obj.optString("role_name", "普通会员"));

        return userInfoRole;
    }

    private Book parseBook(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        Book book = new Book();
        book.setBookId(obj.optString("book_id"));
        book.setBookSrc(obj.optString("src"));
        book.setSid(obj.optString("sina_id"));

        return book;
    }

    private Chapter parseChapter(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        Chapter chapter = new Chapter();
        String buyStr = obj.optString("has_buy", "N");
        boolean hasBuy = false;
        if (buyStr.equals("Y") || buyStr.equals("y")) {
            hasBuy = true;
        }
        chapter.setHasBuy(hasBuy);
        chapter.setGlobalId(obj.optInt("c_id"));
        chapter.setPrice(obj.optDouble("buy_price", 0));
        chapter.setDiscountPrice(obj.optDouble("price", 0));
        chapter.setTitle(obj.optString("title"));
        chapter.setVip(obj.optString("is_vip"));

        return chapter;
    }

    private PriceTip parsePriceTip(JSONObject obj) {
        if (null == obj) {
            return null;
        }

        PriceTip priceTip = new PriceTip();
        priceTip.setButType(obj.optInt("buy_type", 0));
        priceTip.setShowTip(obj.optString("tip"));
        priceTip.setPriceShow("Y".equalsIgnoreCase(obj.optString("price_show", "N")));
        priceTip.setTipShow("Y".equalsIgnoreCase(obj.optString("tip_show", "N")));
        priceTip.setPrice(obj.optDouble("buy_price", 0));
        priceTip.setDiscountPrice(obj.optDouble("price", 0));

        return priceTip;
    }

}
