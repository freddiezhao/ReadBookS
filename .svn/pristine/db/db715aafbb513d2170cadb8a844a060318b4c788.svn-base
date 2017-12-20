package com.sina.book.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.MyPurchasedBooks;
import com.sina.book.data.PurchasedBook;
import com.sina.book.data.PurchasedChapter;
import com.sina.book.util.CalendarUtil;

public class PurchasedBooksParser extends BaseParser {

	/*
	 * 按时间排序(最近的排在前面)
	 */
	private Comparator<PurchasedBook> comparatorPatition = new Comparator<PurchasedBook>() {

		@Override
		public int compare(PurchasedBook lhs, PurchasedBook rhs) {
			if (lhs == null || rhs == null)
				return 0;
			if (null == lhs.getBuyTime() || null == rhs.getBuyTime()) {
				return 0;
			}

			long lhsTime = CalendarUtil.parseFormatDateToTime(lhs.getBuyTime());
			long rhsTime = CalendarUtil.parseFormatDateToTime(rhs.getBuyTime());
			if (lhsTime - rhsTime < 0) {
				return 1;
			} else if (lhsTime - rhsTime > 0) {
				return -1;
			}
			return 0;
		}

	};

	@Override
	protected Object parse(String jsonString) throws JSONException {
		parseDataContent(jsonString);
		if (jsonString != null) {
			JSONObject obj = new JSONObject(jsonString);
			MyPurchasedBooks purchasedBooks = new MyPurchasedBooks();
			purchasedBooks.setTotal(obj.optInt("total"));
			purchasedBooks.setList(parseBooks(obj.optJSONObject("books")));
			return purchasedBooks;
		}
		return null;
	}

	private List<PurchasedBook> parseBooks(JSONObject purchasedBooks) throws JSONException {
		List<PurchasedBook> purchasedList = new ArrayList<PurchasedBook>();

		if (purchasedBooks != null) {
			@SuppressWarnings("unchecked")
			Iterator<String> itemIterator = purchasedBooks.keys();

			while (itemIterator.hasNext()) {
				String bookId = itemIterator.next();
				JSONObject objItem = purchasedBooks.optJSONObject(bookId);
				PurchasedBook pBook = new PurchasedBook();
				pBook.setBookId(bookId);

				List<PurchasedChapter> chaptersInfo = parsePurchasedChapter(objItem.getJSONArray("item"));
				pBook.setPurchasedChapterList(chaptersInfo);

				JSONObject infoObj = objItem.optJSONObject("info");

				pBook.setBookId(infoObj.optString("book_id"));
				pBook.setSid(infoObj.optString("sina_id"));
				pBook.setIntro(infoObj.optString("intro"));
				pBook.setTitle(infoObj.optString("title"));
				pBook.setAuthor(infoObj.optString("author"));
				pBook.setImageUrl(infoObj.optString("img"));
				pBook.setBookCate(infoObj.optString("cate_name"));
				pBook.setBookCateId(infoObj.optString("cate_id"));
				pBook.setBuyTime(infoObj.optString("time"));
				pBook.setBookSrc(infoObj.optString("src"));
				purchasedList.add(pBook);
			}
			// 按时间排序
			Collections.sort(purchasedList, comparatorPatition);
		}
		return purchasedList;
	}

	private List<PurchasedChapter> parsePurchasedChapter(JSONArray jsonArray) throws JSONException {
		List<PurchasedChapter> chapterList = null;
		if (jsonArray != null && jsonArray.length() != 0) {
			chapterList = new ArrayList<PurchasedChapter>();
			PurchasedChapter chapter = null;
			JSONObject obj = null;
			for (int i = 0; i < jsonArray.length(); i++) {
				obj = jsonArray.getJSONObject(i);
				chapter = new PurchasedChapter();
				chapter.setPurchasedTime(obj.optString("time"));
				chapter.setChapterPrice(obj.optString("price"));
				chapter.setProductType(obj.optString("product_type"));
				chapter.setBookId(obj.optString("mainid"));
				chapter.setBookName(obj.optString("mainname"));
				chapter.setChapterId(obj.optString("subid"));
				chapter.setChapterName(obj.optString("subname"));
				chapterList.add(chapter);
			}
		}
		return chapterList;
	}

}
