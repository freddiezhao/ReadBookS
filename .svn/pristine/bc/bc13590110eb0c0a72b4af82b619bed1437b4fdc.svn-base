package com.sina.book.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.RechargeBean;

/**
 * 充值金额、类型选择解析器
 * 
 * @author Tsimle
 * 
 */
public class RechargeChooseParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        RechargeBean rechargeBean = new RechargeBean();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONObject data = obj.getJSONObject("data");
        if (data != null) {
            rechargeBean.setUserVb(data.optInt("user_vb", 0));
            JSONArray amountArray = data.optJSONArray("amount");
//            JSONArray payTypeArray = data.optJSONArray("usetype");

            if (amountArray != null) {
                for (int i = 0; i < amountArray.length(); i++) {
                    RechargeBean.Amount amount = new RechargeBean.Amount();
                    JSONObject amountObj = amountArray.getJSONObject(i);
                    amount.moneyFen = amountObj.optInt("num");
                    amount.money = amount.moneyFen / 100;
                    rechargeBean.addAmount(amount);
                }
            }
            
//            if (payTypeArray != null) {
//                ArrayList<RechargeBean.PayType> payTypes = new ArrayList<RechargeBean.PayType>();
//                for (int i = 0; i < payTypeArray.length(); i++) {
//                    RechargeBean.PayType payType = new RechargeBean.PayType();
//                    JSONObject payTypeObj = payTypeArray.getJSONObject(i);
//                    payType.type = payTypeObj.optString("type");
//                    payType.desc = payTypeObj.optString("desc");
//                    payType.maxMoneyFen = payTypeObj.optInt("max");
//                    payType.maxMoney = payType.maxMoneyFen / 100;
//                    payTypes.add(payType);
//                }
//                Collections.reverse(payTypes);
//                rechargeBean.setPayTypes(payTypes);
//            }
        }
        return rechargeBean;
    }

}
