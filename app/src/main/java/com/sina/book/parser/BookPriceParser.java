package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.BookPriceResult;
import com.sina.book.data.PriceTip;
import com.sina.book.data.UserInfoRole;

public class BookPriceParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);

        BookPriceResult result = null;

        JSONObject object = new JSONObject(jsonString);

        if (object != null) {
            result = new BookPriceResult();
            
            JSONObject roleObj = object.optJSONObject("userinfo");
            result.setUserInfoRole(parseUserInfo(roleObj));
            
            JSONObject bookObj = object.optJSONObject("books");
            Book book = parseBook(bookObj);
            if (null != book) {
                JSONObject priceTipObj = object.optJSONObject("price_tip");
                book.getBuyInfo().setPriceTip(parsePriceTip(priceTipObj));
            }

            result.setBook(book);
        }

        return result;
    }

    private UserInfoRole parseUserInfo(JSONObject obj) {
        if (null == obj) {
            return null;
        }
        UserInfoRole userInfoRole = new UserInfoRole();
        userInfoRole.setRole(obj.optInt("role", UserInfoRole.GENERAL_USER));
        userInfoRole.setRoleName(obj.optString("role_name", "普通会员"));

        return userInfoRole;
    }

    private Book parseBook(JSONObject obj) {
        if (null == obj) {
            return null;
        }
        Book book = new Book();
        book.setBookId(obj.optString("book_id"));
        book.getBuyInfo().setPayType(obj.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
        book.getBuyInfo().setPrice(obj.optDouble("buy_price", 0));
        book.getBuyInfo().setDiscountPrice(obj.optDouble("price", 0));
        book.getBuyInfo().setHasBuy("Y".equalsIgnoreCase(obj.optString("isbuy", "N")));

        return book;
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
