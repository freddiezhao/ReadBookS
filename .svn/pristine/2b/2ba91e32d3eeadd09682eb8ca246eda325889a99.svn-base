package com.sina.book.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.UpdateVersionInfo;

public class UpdateInfoParser extends BaseParser {

    @Override
    protected Object parse(String jsonString) throws JSONException {
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        JSONObject dataObject = obj.optJSONObject("data");
        if (dataObject != null) {
            UpdateVersionInfo updateInfo = new UpdateVersionInfo();
            if ("Y".equalsIgnoreCase(dataObject.optString("update"))) {
                updateInfo.setUpdate(true);
            } else {
                updateInfo.setUpdate(false);
            }

            if ("N".equalsIgnoreCase(dataObject.optString("force"))) {
                updateInfo.setForce(false);
            } else {
                updateInfo.setForce(true);
            }

            updateInfo.setUrl(dataObject.optString("url"));
            updateInfo.setUpdateInfo(dataObject.optString("info"));
            updateInfo.setIntro(dataObject.optString("intro"));
            return updateInfo;
        }
        return null;
    }

}
