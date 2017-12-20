package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.PaymentMonthDetail;

public class PaymentMonthDetailParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        ArrayList<PaymentMonthDetail> lists = new ArrayList<PaymentMonthDetail>();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONArray array = obj.optJSONArray("suites");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                PaymentMonthDetail item = new PaymentMonthDetail();
                JSONObject itemObj = array.getJSONObject(i);
                item.setPayId(itemObj.optString("suite_id"));
                item.setPayType(itemObj.optString("suite_name"));
                item.setPayOpen(itemObj.optString("is_buy"));
                item.setPayDetail(itemObj.optString("intro"));
                item.setBeginTime(itemObj.optLong("begintime"));
                item.setEndTime(itemObj.optLong("endtime"));
                
                lists.add(item);
            }
        }
        
        return lists;       
        
    }
    
}