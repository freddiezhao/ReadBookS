package com.sina.book.parser;

import com.sina.book.data.PartitionLikedResult;
import com.sina.book.data.PartitionLikedItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PartitionLikedParser extends BaseParser {

	/**
	 * 添加一个搜索结果的简单排序，<br>
	 * 尽量的将vip收费的书往前靠
	 */
	private Comparator<PartitionLikedItem> comparatorPartition = new Comparator<PartitionLikedItem>() {

		@Override
		public int compare(PartitionLikedItem partitionLikedItem1,
				PartitionLikedItem partitionLikedItem2) {
			if (partitionLikedItem1 == null || partitionLikedItem2 == null) {
				return 0;
			}
			String partitionTitle1 = partitionLikedItem1.getTitle();
			String partitionTitle2 = partitionLikedItem2.getTitle();

			int partitionInt1 = getPartitionInt(partitionTitle1);
			int partitionInt2 = getPartitionInt(partitionTitle2);

			return partitionInt2 - partitionInt1;
		}

		public int getPartitionInt(String partitionTitle) {
			if (partitionTitle.equals("都市")) {
				return 5;
			} else if (partitionTitle.equals("言情")) {
				return 4;
			} else if (partitionTitle.equals("官场")) {
				return 3;
			} else if (partitionTitle.equals("军事")) {
				return 2;
			} else {
				return 1;
			}
		}
	};

	@Override
	protected Object parse(String jsonString) throws JSONException {
		PartitionLikedResult result = new PartitionLikedResult();
		ArrayList<PartitionLikedItem> lists = new ArrayList<PartitionLikedItem>();
		parseDataContent(jsonString);
		JSONObject obj = new JSONObject(jsonString);
		JSONArray array = obj.optJSONArray("types");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				PartitionLikedItem item = new PartitionLikedItem();
				JSONObject itemObj = array.getJSONObject(i);
				item.setId(itemObj.optString("id"));
				item.setTitle(itemObj.optString("title"));
				item.setIconUrl(itemObj.optString("icon"));
				item.setIsFavorite(itemObj.optBoolean("is_fav", false));
				lists.add(item);
			}
		}
		Collections.sort(lists, comparatorPartition);
		result.setItems(lists);
		return result;
	}

}
