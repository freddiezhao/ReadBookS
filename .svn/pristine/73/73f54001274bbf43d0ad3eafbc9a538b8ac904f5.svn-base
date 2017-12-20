package com.sina.book.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.http.protocol.HTTP;
import org.htmlcleaner.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.CloudSyncData;

public class SyncDataParser extends BaseParser {

	/**
	 * 根据服务器返回的顺序排序<br>
	 */
	private Comparator<Book> comparatorBook = new Comparator<Book>() {

		@Override
		public int compare(Book item1, Book item2) {
			if (item1 == null || item2 == null) {
				return 0;
			}

			int index1 = item1.getIndex();
			int index2 = item2.getIndex();

			return index1 - index2;
		}

	};

	@SuppressWarnings("unchecked")
	@Override
	protected Object parse(String jsonString) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);

		CloudSyncData cloudSyncData = new CloudSyncData();
		cloudSyncData.setEtag(obj.optString("etag", ""));

		ArrayList<Book> updateBooks = new ArrayList<Book>();
		ArrayList<String> delBooks = new ArrayList<String>();
		cloudSyncData.setDelBooks(delBooks);
		cloudSyncData.setUpdateBooks(updateBooks);

		JSONObject dataArray = obj.optJSONObject("data");
		if (dataArray != null && dataArray.length() != 0) {
			Iterator<String> iterator = dataArray.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				JSONObject item = dataArray.optJSONObject(key);
				if (item != null && !Utils.isEmptyString(item.optString("bid", ""))) {
					// Log.d("ouyang",
					// "--SyncDataParse--parse-----updateBooks++-- ");
					updateBooks.add(parserBook(item));
				} else {
					// Log.d("ouyang",
					// "--SyncDataParse--parse-----delBooks++-- ");
					delBooks.add(key);
				}
			}
		}
		Collections.sort(updateBooks, comparatorBook);
		return cloudSyncData;
	}

	private Book parserBook(JSONObject jsonObj) throws JSONException {
		Book book = null;
		if (jsonObj != null) {
			book = new Book();
			book.setSid(jsonObj.optString("sid"));
			book.setBookId(jsonObj.optString("bid"));
			book.setTitle(jsonObj.optString("title"));
			book.setAuthor(jsonObj.optString("author"));
			try {
				book.getDownloadInfo().setImageUrl(URLDecoder.decode(jsonObj.optString("img"), HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				book.getDownloadInfo().setImageUrl(jsonObj.optString("img"));
			}
			book.setIntroRealNeed(jsonObj.optString("intro"));
			book.setBookSrc(jsonObj.optString("src"));
			// 购买，价格信息
			book.getBuyInfo().setPayType(jsonObj.optInt("paytype"));
			book.getBuyInfo().setPrice(jsonObj.optDouble("price", 0));
			book.getBuyInfo().setStatusInfo(jsonObj.optString("status"));
			book.getBuyInfo().setHasBuy("Y".equalsIgnoreCase(jsonObj.optString("isbuy", "N")));

			book.setOnlineReadChapterId(jsonObj.optInt("cid"), "SyncDataParser-parserBook");
			book.setIndex(jsonObj.optInt("index", 9999));
			
			// 是否图文书籍
			book.setIsHtmlRead(jsonObj.optInt("kind") == 7);
		}
		return book;
	}
}
