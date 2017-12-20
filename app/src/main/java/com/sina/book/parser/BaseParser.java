package com.sina.book.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.sina.book.control.RequestTask;
import com.sina.book.data.Book;
import com.sina.book.data.ConstantData;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;

public abstract class BaseParser implements IParser
{
	public static final String	TAG						= "BaseParser";

	protected final String		BOOK_DETAIL_TAG_PATTERN	= "[\\s|　]";

	protected String			code					= ConstantData.CODE_FAIL;
	protected String			msg						= "";
	private RequestTask			mTask;

	protected abstract Object parse(String jsonString) throws JSONException;

	@Override
	public String getCode()
	{
		return code;
	}

	protected void setCode(String code)
	{
		this.code = code;
	}

	@Override
	public String getMsg()
	{
		return msg;
	}

	protected void setMsg(String msg)
	{
		this.msg = msg;
	}

	public void setRequestTask(RequestTask task)
	{
		this.mTask = task;
	}

	@Override
	public Object parseString(String jsonString)
	{
		try {
			if (null != mTask && mTask.isCancelled()) {
				return null;
			}

			if (TextUtils.isEmpty(jsonString)) {
				return null;
			} else {
				return parse(jsonString);
			}
		} catch (JSONException e) {
			LogUtil.e(TAG, getClass().getSimpleName() + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object parse(InputStream in)
	{
		try {
			if (null == in) {
				return null;
			}
			String jsonString = HttpUtil.inputStream2String(in);

			if (null != mTask && mTask.isCancelled()) {
				return null;
			}

			if (TextUtils.isEmpty(jsonString)) {
				return null;
			} else {
				return parse(jsonString);
			}
		} catch (IOException e) {
			LogUtil.e(TAG, e.toString());
		} catch (JSONException e) {
			LogUtil.e(TAG, getClass().getSimpleName() + e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			LogUtil.e(TAG, e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				LogUtil.e(TAG, e.toString());
			}
		}
		return null;
	}

	/**
	 * 返回的json是"status":{ "code":"0", "msg":"成功" }, "data":{}的格式时
	 * 使用该法解析出code，判断code为成功时，返回data的内容
	 * 
	 * @param jsonString
	 * @return
	 * @throws JSONException
	 */
	protected void parseDataContent(String jsonString) throws JSONException
	{
		JSONObject result = new JSONObject(jsonString);
		JSONObject status = result.optJSONObject("status");
		if (status != null) {
			code = status.optString("code", ConstantData.CODE_FAIL);
			msg = status.optString("msg", "");
			LogUtil.d(TAG, getClass().getName() + " -> Response code : " + code);
			LogUtil.d(TAG, getClass().getName() + " -> Response message : " + msg);
		}
	}

	protected String parseCode(String jsonString) throws JSONException
	{
		JSONObject json = new JSONObject(jsonString);
		return parseCode(json);
	}

	protected String parseMsg(String jsonString) throws JSONException
	{
		JSONObject json = new JSONObject(jsonString);
		return parseMsg(json);
	}

	protected String parseCode(JSONObject json) throws JSONException
	{
		JSONObject status = json.optJSONObject("status");
		if (status != null) {
			String code = status.optString("code", ConstantData.CODE_FAIL);
			LogUtil.d(TAG, getClass().getName() + " -> parseCode code : " + code);
			return code;
		}
		return null;
	}

	protected String parseMsg(JSONObject json) throws JSONException
	{
		JSONObject status = json.optJSONObject("status");
		if (status != null) {
			String msg = status.optString("msg", "");
			LogUtil.d(TAG, getClass().getName() + " -> parseCode message : " + msg);
			return msg;
		}
		return null;
	}

	protected boolean isKeyHasAvailableValue(JSONObject jsonObj, String key)
	{
		if (jsonObj != null && !TextUtils.isEmpty(key)) {
			try {
				if (jsonObj.has(key) && jsonObj.get(key) != null) {
					return true;
				}
			} catch (JSONException e) {
				// e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	protected ArrayList<Book> parseCommonBooks(JSONObject object) throws JSONException
	{
		ArrayList<Book> lists = new ArrayList<Book>();
		JSONArray array = object.optJSONArray("books");
		if (null != array) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject item = array.getJSONObject(i);
				Book book = new Book();
				book.setBookId(item.optString("bid"));
				book.setTitle(item.optString("title"));
				book.setAuthor(item.optString("author"));
				book.getBuyInfo().setStatusInfo(item.optString("status"));
				book.getDownloadInfo().setImageUrl(item.optString("img"));
				book.setIntro(item.optString("intro"));
				book.getBuyInfo().setPrice(item.optDouble("price", 0));
				book.getBuyInfo().setPayType(item.optInt("paytype", Book.BOOK_TYPE_CHAPTER_VIP));
				book.setBookSrc(item.optString("src"));
				book.setPraiseNum(item.optLong("praise_num"));
				book.setCommentNum(item.optLong("comment_num"));
				book.setNum(item.optInt("chapter_amount"));
				book.setFlag(item.optString("flag"));

				book = parseLastChapter(item.optJSONObject("last_chapter"), book);

				book.setContentTag(parseTags(item.optJSONArray("tags")));

				lists.add(book);
			}
		}
		return lists;
	}

	private Book parseLastChapter(JSONObject chapterObject, Book book)
	{
		if (chapterObject != null) {

			book.getBookUpdateChapterInfo().setGlobalId(chapterObject.optInt("chapter_id"));
			book.getBookUpdateChapterInfo().setTitle(chapterObject.optString("title"));
			book.getBookUpdateChapterInfo().setVip(chapterObject.optString("is_vip"));
			book.getBookUpdateChapterInfo().setUpdateTime(chapterObject.optString("updatetime"));
		}

		return book;
	}

	private String parseTags(JSONArray array) throws JSONException
	{
		StringBuilder str = new StringBuilder();

		if (null != array) {
			for (int i = 0; i < array.length(); i++) {
				String tag = array.getString(i).replaceAll(BOOK_DETAIL_TAG_PATTERN, "");
				if (!TextUtils.isEmpty(tag)) {
					str.append(tag);
					str.append(" ");
				}
			}
		}

		return str.toString();
	}
}