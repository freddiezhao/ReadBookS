package com.sina.book.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.sina.book.data.Book;
import com.sina.book.data.MainBookItem;
import com.sina.book.data.MainBookResult;
import com.sina.book.data.PriceTip;
import com.sina.book.data.UserInfoRec;

/**
 * 主界面数据解析
 * 
 * @author Tsimle
 * 
 */
public class MainInfoParser extends BaseParser {

	private final String BOOK_DETAIL_TAG_PATTERN = "[\\s|　]";

	@Override
	protected Object parse(String jsonString) throws JSONException {
		Random r = new Random();

		MainBookResult mainBook = new MainBookResult();
		JSONObject obj = new JSONObject(jsonString);

		// 1 推荐语句解析
		// List<MainTipItem> mainTips = new ArrayList<MainTipItem>();
		// JSONObject recinfo = obj.optJSONObject("recintro");
		// if (recinfo != null) {
		// JSONArray mainTipArray = recinfo.optJSONArray("content");
		// if (mainTipArray != null && mainTipArray.length() > 0) {
		// for (int i = 0; i < mainTipArray.length(); i++) {
		// JSONObject tipObj = mainTipArray.getJSONObject(i);
		// MainTipItem mainTipItem = null;
		// ArrayList<Book> tipBooks = null;
		//
		// int recommendType = tipObj.optInt("recommend_type");
		// switch (recommendType) {
		// case MainTipItem.TIP_TYPE_BOOK:
		// mainTipItem = new MainTipItem();
		// mainTipItem.setRecommendType(recommendType);
		// mainTipItem.setComment(tipObj.optString("comment"));
		// Book tipBook = parserBook(tipObj.optJSONObject("book"));
		// tipBooks = new ArrayList<Book>();
		// tipBooks.add(tipBook);
		// mainTipItem.saveBooks(tipBooks);
		// break;
		// case MainTipItem.TIP_TYPE_URL:
		// mainTipItem = new MainTipItem();
		// mainTipItem.setRecommendType(recommendType);
		// mainTipItem.setComment(tipObj.optString("comment"));
		// mainTipItem.setUrl(tipObj.optString("url"));
		// break;
		// case MainTipItem.TIP_TYPE_BOOKLIST:
		// mainTipItem = new MainTipItem();
		// mainTipItem.setRecommendType(recommendType);
		// mainTipItem.setComment(tipObj.optString("comment"));
		// tipBooks = parserBooks(tipObj.optJSONArray("books"));
		// mainTipItem.saveBooks(tipBooks);
		// break;
		// default:
		// break;
		// }
		// if (mainTipItem != null) {
		// mainTips.add(mainTipItem);
		// }
		// }
		// }
		// }
		// mainInfo.setMainTips(mainTips);

		// // 2 排行信息解析
		// List<Book> rankBooks = null;
		// JSONObject readTop = obj.optJSONObject("sina_top");
		// if (readTop != null) {
		// rankBooks = parserBooks(readTop.optJSONArray("books"));
		// }
		// mainInfo.setRankBooks(rankBooks);
		//
		// // 3 名人推荐
		// ArrayList<FamousRecommend> famousRecommends = new
		// ArrayList<FamousRecommend>();
		// JSONObject timeLine = obj.optJSONObject("timeline");
		// if (timeLine != null) {
		// JSONArray users = timeLine.optJSONArray("users");
		// if (users != null && users.length() > 0) {
		// for (int i = 0; i < users.length(); i++) {
		// JSONObject itemUser = users.getJSONObject(i);
		// FamousRecommend famousRecommend = new FamousRecommend();
		// famousRecommend.setUid(itemUser.optString("uid"));
		// famousRecommend.setHeadUrl(itemUser
		// .optString("profile_image_url"));
		// famousRecommend
		// .setScreenName(itemUser.optString("screen_name"));
		// famousRecommend.setListCount(itemUser.optInt("list_count"));
		//
		// JSONObject itemMessage = itemUser.optJSONObject("message");
		// famousRecommend.setDesc(itemMessage.optString("comment"));
		// JSONObject itemBook = itemMessage.optJSONObject("book");
		// if (itemBook != null) {
		// famousRecommend.setRecoBookTitle(itemBook
		// .optString("title"));
		// famousRecommend.setRecoBookIntro(itemBook
		// .optString("intro"));
		// }
		// famousRecommends.add(famousRecommend);
		// }
		// }
		// }
		// mainInfo.setFamousRecommends(famousRecommends);

		// 1 精选推荐
		Book recommendBook = null;

		JSONObject recommend = obj.optJSONObject("index_recommend");
		if (recommend != null) {
			MainBookItem bookItem = new MainBookItem();
			JSONArray array1 = recommend.optJSONArray("data");
			if (array1 != null) {
				int randomInt1 = r.nextInt(array1.length());
				JSONObject randobj1 = array1.getJSONObject(randomInt1);

				JSONObject recommendItem = randobj1.optJSONArray("books").optJSONObject(0);
				if (recommendItem == null) {
					recommendItem = randobj1.optJSONObject("book");
				}
				if (recommendItem != null) {
					recommendBook = parserBook(recommendItem);
				}
				if (recommendBook != null) {
					recommendBook.setComment(randobj1.optString("name"));
				}

				bookItem.setBook(recommendBook);
				bookItem.setType(MainBookItem.TYPE_RECOMMEND);
			}

			if (recommendBook != null) {
				mainBook.addData(bookItem);
			}
		}

		// 2 畅销榜单
		Book sellFastBook = null;
		JSONObject sellFast = obj.optJSONObject("hot_top");
		if (sellFast != null) {
			MainBookItem bookItem = new MainBookItem();
			JSONArray array2 = sellFast.optJSONArray("data");
			if (array2 != null) {
				int randomInt2 = r.nextInt(array2.length());
				JSONObject randobj2 = array2.getJSONObject(randomInt2);

				JSONObject sellFastItem = randobj2.optJSONObject("books");
				if (sellFastItem != null) {
					sellFastBook = parserBook(sellFastItem);
				}
				if (sellFastBook != null) {
					sellFastBook.setComment(randobj2.optString("name"));
				}

				bookItem.setBook(sellFastBook);
				bookItem.setType(MainBookItem.TYPE_TOPLIST);
			}

			if (sellFastBook != null) {
				mainBook.addData(bookItem);
			}
		}

		// 3 分类浏览
		Book cateBook = null;
		JSONObject cate = obj.optJSONObject("cate_recommend");
		if (cate != null) {
			MainBookItem bookItem = new MainBookItem();
			JSONArray array3 = cate.optJSONArray("data");
			if (array3 != null) {
				int randomInt3 = r.nextInt(array3.length());
				JSONObject randobj3 = array3.getJSONObject(randomInt3);

				JSONObject cateItem = randobj3.optJSONObject("books");
				if (cateItem != null) {
					cateBook = parserBook(cateItem);
				}
				if (cateBook != null) {
					cateBook.setComment(randobj3.optString("cate_name"));
				}

				bookItem.setBook(cateBook);
				bookItem.setType(MainBookItem.TYPE_CATE);
			}

			if (cateBook != null) {
				mainBook.addData(bookItem);
			}
		}

		// 4 大家在读
		Book peopleRecommendBook = null;
		JSONObject pRecommend = obj.optJSONObject("recommend");
		UserInfoRec userInfoRecommend = null;
		String time = null;
		if (pRecommend != null) {
			MainBookItem bookItem = new MainBookItem();
			JSONObject pRecommendItem = pRecommend.optJSONObject("books");
			if (pRecommendItem != null) {
				peopleRecommendBook = parserBook(pRecommendItem);
				userInfoRecommend = parseUserInfoRec(pRecommendItem.optJSONObject("users"));
				time = pRecommendItem.optString("recommend_time");
			}
			if (peopleRecommendBook != null) {
				peopleRecommendBook.setComment(pRecommend.optString("name"));
			}

			bookItem.setBook(peopleRecommendBook);
			bookItem.setType(MainBookItem.TYPE_PEOPLE);
			bookItem.setPeopleRecommend(userInfoRecommend);
			bookItem.setPeopleRecommendTime(time);

			if (peopleRecommendBook != null) {
				mainBook.addData(bookItem);
			}
		}

		// 编辑推荐
		List<Book> editorRecommendBooks = new ArrayList<Book>();
		JSONObject eRecommend = obj.optJSONObject("editor_recommend");
		if (eRecommend != null) {
			JSONArray array = eRecommend.optJSONArray("data");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject editorObj = array.getJSONObject(i);
					if (editorObj != null) {
						JSONArray editorBookArray = editorObj.optJSONArray("books");
						if (editorBookArray != null) {
							String editorRecommendName = editorObj.optString("name");
							for (int j = 0; j < editorBookArray.length(); j++) {
								Book editorRecommendBook = null;
								JSONObject editorBookObj = editorBookArray.getJSONObject(j);
								editorRecommendBook = parserBook(editorBookObj);

								if (editorBookObj != null && editorRecommendBook != null) {
									JSONObject priceTipObj = editorBookObj.optJSONObject("price_tip");
									editorRecommendBook.getBuyInfo().setPriceTip(parsePriceTip(priceTipObj));
								}

								if (editorRecommendBook != null) {
									editorRecommendBook.setComment(editorRecommendName);
									editorRecommendBooks.add(editorRecommendBook);
								}
							}
						}
					}
				}
				mainBook.setEditorRecommend(editorRecommendBooks);
			}
		}

		return mainBook;
	}

	// private ArrayList<Book> parserBooks(JSONArray jsonArray)
	// throws JSONException {
	// ArrayList<Book> booksList = new ArrayList<Book>();
	// if (jsonArray != null && jsonArray.length() != 0) {
	// Book book;
	// for (int i = 0; i < jsonArray.length(); i++) {
	// JSONObject item = jsonArray.getJSONObject(i);
	// book = new Book();
	// book.setSid(item.optString("sid"));
	// book.setBookId(item.optString("bid"));
	// book.setTitle(item.optString("title"));
	// // book.setAuthor(item.optString("author"));
	// // try {
	// // book.getDownloadInfo()
	// // .setImageUrl(
	// // URLDecoder.decode(item.optString("img"),
	// // HTTP.UTF_8));
	// // } catch (UnsupportedEncodingException e) {
	// // book.getDownloadInfo().setImageUrl(item.optString("img"));
	// // }
	// // book.setIntro(item.optString("intro"));
	// book.setBookSrc(item.optString("src"));
	// booksList.add(book);
	// }
	// return booksList;
	// }
	// return booksList;
	// }

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
			
			// 是否图文书籍
			int kind = jsonObj.optInt("kind");
			book.setIsHtmlRead(kind == 7);
			
			// 是否Epub资源
			String strEpub = jsonObj.optString("is_epub");
			if(!TextUtils.isEmpty(strEpub) && "2".equals(strEpub)){
				book.setIsEpub(true);
			}else{
				book.setIsEpub(false);
			}
			
			// ----------- 解决：BugID=21446 -----------
			// book.setIntro(jsonObj.optString("intro"));
			book.setIntroRealNeed(jsonObj.optString("intro"));
			// ----------- ----------- ----------- ----
			book.setPraiseNum(jsonObj.optLong("praise_num"));
			book.setCommentNum(jsonObj.optLong("comment_num"));
			book.getBuyInfo().setPayType(jsonObj.optInt("paytype"));
			book.getBuyInfo().setPrice(jsonObj.optDouble("price", 0));
			book.setBookSrc(jsonObj.optString("src"));
			book.setRecommendIntro(jsonObj.optString("recommend_intro").replace(BOOK_DETAIL_TAG_PATTERN, ""));
			book.getBuyInfo().setHasBuy("Y".equalsIgnoreCase(jsonObj.optString("isbuy", "N")));

			JSONObject chapterObj = jsonObj.optJSONObject("last_chapter");
			if (null != chapterObj) {
				book.setUpdateTimeServer(chapterObj.optString("updatetime"));
				book.setUpdateChapterNameServer(chapterObj.optString("title"));
			}

			JSONArray tagArray = jsonObj.optJSONArray("tags");
			if (null != tagArray && 0 < tagArray.length()) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < tagArray.length(); i++) {
					String tag = tagArray.getString(i).replace(BOOK_DETAIL_TAG_PATTERN, "");
					sb.append(tag);
					if (i < tagArray.length() - 1) {
						sb.append(" ");
					}
				}
				book.setContentTag(sb.toString());
			}
		}
		return book;
	}

	// private Book parseBookInfo(JSONObject jsonObj) throws JSONException {
	// if (null == jsonObj) {
	// return null;
	// }
	//
	// Book book = new Book();
	// book.setBookId(jsonObj.optString("bid"));
	// book.setSid(jsonObj.optString("sid"));
	// book.setBookSrc(jsonObj.optString("src"));
	// book.setTitle(jsonObj.optString("title"));
	// book.setAuthor(jsonObj.optString("author"));
	// book.setIntro(jsonObj.optString("intro"));
	// try {
	// book.getDownloadInfo().setImageUrl(URLDecoder.decode(jsonObj.optString("img"),
	// HTTP.UTF_8));
	// } catch (UnsupportedEncodingException e) {
	// book.getDownloadInfo().setImageUrl(jsonObj.optString("img"));
	// }
	// book.setBookCate(jsonObj.optString("cate_name"));
	// book.setBookCateId(jsonObj.optString("cate_id"));
	// book.setNum(jsonObj.optInt("chapter_total"));
	// book.setPraiseNum(jsonObj.optLong("praise_num", 0));
	// book.setCommentNum(jsonObj.optLong("comment_num", 0));
	//
	// JSONObject chapterObj = jsonObj.optJSONObject("last_chapter");
	// if (null != chapterObj) {
	// book.setUpdateTimeServer(chapterObj.optString("updatetime"));
	// book.setUpdateChapterNameServer(chapterObj.optString("title"));
	// }
	//
	// return book;
	// }

	private UserInfoRec parseUserInfoRec(JSONObject jsonObj) throws JSONException {
		if (null == jsonObj) {
			return null;
		}
		UserInfoRec userInfoRec = new UserInfoRec();
		userInfoRec.setUid(jsonObj.optString("uid"));
		userInfoRec.setUserProfileUrl(jsonObj.optString("profile_image_url"));
		userInfoRec.setuName(jsonObj.optString("screen_name"));

		return userInfoRec;
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
