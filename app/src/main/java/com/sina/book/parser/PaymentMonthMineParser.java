package com.sina.book.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.PaymentMonthMine;
import com.sina.book.data.PaymentMonthMineResult;


public class PaymentMonthMineParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        PaymentMonthMineResult result = new PaymentMonthMineResult();
        List<PaymentMonthMine> lists = new ArrayList<PaymentMonthMine>();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONObject obj1 = obj.optJSONObject("suites");
        // 保证一定能返回信息
        if (obj1 != null) {
            result.setCount(obj1.optInt("count")); 
            JSONArray array = obj1.optJSONArray("data");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    PaymentMonthMine item = new PaymentMonthMine();
                    JSONObject itemObj = array.getJSONObject(i);
                    item.setPayId(itemObj.optInt("suite_id"));
                    item.setPayType(itemObj.optString("suite_name"));
                    item.setPayOpen(itemObj.optString("is_buy"));
                    item.setBeginTime(itemObj.optString("begintime"));
                    item.setEndTime(itemObj.optString("endtime"));
                    item.setTimeRemain(itemObj.optString("time_remain"));
                    
                    lists.add(item);
                }
            }
        }
        result.addLists(lists);        
        
        return result;
    }
    
}