package com.sina.book.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.ConstantData;
import com.sina.book.data.FamousRecommend;
import com.sina.book.data.ListResult;

public class FamousRecommendParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        ListResult<FamousRecommend> result = new ListResult<FamousRecommend>();
        parseDataContent(jsonString);
        result.setMsg(getMsg());
        result.setRetcode(getCode());
        if (!getCode().equals(ConstantData.CODE_SUCCESS)) {
            return result;
        }
        JSONObject obj = new JSONObject(jsonString);
        JSONArray array = obj.optJSONArray("timeline");
        ArrayList<FamousRecommend> famouslists = new ArrayList<FamousRecommend>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            JSONObject itemUser = item.optJSONObject("user");
            FamousRecommend famousRecommend = new FamousRecommend();
            famousRecommend.setUid(itemUser.optString("uid"));
            famousRecommend.setHeadUrl(itemUser.optString("profile_image_url"));
            famousRecommend.setScreenName(itemUser.optString("screen_name"));
            famousRecommend.setListCount(itemUser.optInt("list_count"));

            JSONObject itemMessage = item.optJSONObject("message");
            famousRecommend.setDesc(itemMessage.optString("comment"));
            JSONObject itemBook = itemMessage.optJSONObject("book");
            if (itemBook != null) {
                famousRecommend.setRecoBookTitle(itemBook.optString("title"));
                famousRecommend.setRecoBookIntro(itemBook.optString("intro"));
            }
            famouslists.add(famousRecommend);
        }
        result.setList(famouslists);
        return result;
    }

}
