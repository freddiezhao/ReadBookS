package com.sina.book.parser;

import org.htmlcleaner.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
import android.text.TextUtils;

import com.sina.book.data.Book;
import com.sina.book.data.BookDetailData;
import com.sina.book.data.PriceTip;
import com.sina.book.util.NumericHelper;

public class BookDetailParser extends BaseParser {

	@Override
	protected Object parse(String jsonString) throws JSONException {
		BookDetailData result = new BookDetailData();
		parseDataContent(jsonString);
		/*
		 * { "status": { "code": 34, "msg": "此书已下架" } }
		 */
		result.setCode(NumericHelper.parseInt(getCode(), -1));
		result.setMessage(getMsg());
		JSONObject obj = new JSONObject(jsonString);
		if (isKeyHasAvailableValue(obj, "books")) {
			JSONObject bookObj = obj.getJSONObject("books");
			if (bookObj != null) {
				Book book = new Book();
				book.setBookId(bookObj.optString("book_id"));
				book.setSid(bookObj.optString("sina_id"));
				book.setBookSrc(bookObj.optString("src"));

				String forbidden = bookObj.optString("is_forbidden");
				if ("Y".equals(forbidden)) {
					book.setIsForbidden(true);
				} else {
					book.setIsForbidden(false);
				}

				// 和阅读标识数据读取
				JSONObject cmreadCard = bookObj.getJSONObject("cmread_card");
				if (cmreadCard != null) {
					String isCmread = cmreadCard.optString("is_cmread", "N");
					if ("Y".equals(isCmread)) {
						book.setCmreadBook(true);
					} else {
						book.setCmreadBook(false);
					}

					String isShow = cmreadCard.optString("is_show", "N");
					if ("Y".equals(isShow)) {
						book.setCmreadBookAndNeedShow(true);
					} else {
						book.setCmreadBookAndNeedShow(false);
					}
				}

				try {
					book.setTitle(Html.fromHtml(bookObj.optString("title")).toString());
					book.setAuthor(Html.fromHtml(bookObj.optString("author")).toString());
				} catch (Exception e) {
				}
				String chapNum = bookObj.optString("chapter_num", "0");
				if (!TextUtils.isEmpty(chapNum)) {
					book.setNum(Integer.parseInt(chapNum));
				} else {
					book.setNum(0);
				}
				book.getBuyInfo().setVip(bookObj.optString("is_vip"));
				book.getBuyInfo().setPayType(bookObj.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
				book.getBuyInfo().setBuyType(bookObj.optInt("buy_type", 0));
				book.getBuyInfo().setPrice(bookObj.optDouble("price", 0));
				book.getBuyInfo().setStatusFlag(bookObj.optString("status_flag"));
				book.getBuyInfo().setStatusInfo(bookObj.optString("status_info"));
				book.setBookCateId(bookObj.optString("cate_id"));
				book.setBookCate(bookObj.optString("cate_name"));
				book.setIntroRealNeed(Html.fromHtml(bookObj.optString("intro")).toString());
				book.getDownloadInfo().setImageUrl(bookObj.optString("img"));
				book.getBuyInfo().setHasBuy("Y".equalsIgnoreCase(bookObj.optString("isbuy", "N")));
				book.setPraiseNum(bookObj.optLong("praise_num"));
				book.setPraiseType(bookObj.optString("is_praise", "N"));
				// 1表示不含epub，2表示含有epub
				book.setIsEpub(bookObj.optInt("is_epub", 1) == 2);
				
				// 表示是否图文书籍 (kind=7表示图文)
				book.setIsHtmlRead(bookObj.optInt("kind", 1) == 7);
				
				// 添加书籍icon的信息
				book = parsePicType(bookObj.optJSONObject("pic_card"), book);

				// 添加包月套餐相关
				String suiteId = obj.optString("suite_id");
				if (!Utils.isEmptyString(suiteId)) {
					book.setSuiteId(Integer.parseInt(suiteId));
					book.setOriginSuiteId(Integer.parseInt(suiteId));
				}
				book.setSuiteName(obj.optString("suite_name"));
				// ==3标识已经包月了该套餐
				book.setHasBuySuite(obj.optInt("is_suite") == 3);

				book = parseLastChapter(obj.optJSONObject("last_chapter"), book);

				JSONObject priceTipObj = obj.optJSONObject("price_tip");
				book.getBuyInfo().setPriceTip(parsePriceTip(priceTipObj));

				result.setBook(book);

			}
		}
		return result;
	}

	private Book parseLastChapter(JSONObject chapterObject, Book book) {
		if (chapterObject != null) {

			book.getBookUpdateChapterInfo().setGlobalId(chapterObject.optInt("chapter_id"));
			book.getBookUpdateChapterInfo().setTitle(chapterObject.optString("title"));
			book.getBookUpdateChapterInfo().setVip(chapterObject.optString("is_vip"));
			book.getBookUpdateChapterInfo().setUpdateTime(chapterObject.optString("updatetime"));
		}

		return book;
	}

	private Book parsePicType(JSONObject picTypeObject, Book book) {
		if (picTypeObject != null) {

			book.getPicType().setShow("Y".equalsIgnoreCase(picTypeObject.optString("is_show", "N")));
			book.getPicType().setType(picTypeObject.optInt("script_type", 0));
			book.getPicType().setName(picTypeObject.optString("script_name"));
		}

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
