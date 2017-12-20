package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.PaymentMonthPurchased;

public class PaymentMonthPurchasedParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        PaymentMonthPurchased item = new PaymentMonthPurchased();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONObject obj1 = obj.optJSONObject("suites");
        if (obj1 != null) {
            item.setPayId(obj1.optInt("suite_id"));
            item.setPayOpen(obj1.optString("suite_buy"));
        }
        
        return item;       
        
    }
    
}