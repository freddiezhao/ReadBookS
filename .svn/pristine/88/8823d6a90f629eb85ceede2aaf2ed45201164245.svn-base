package com.sina.book.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ConstantData;
import com.sina.book.htmlspanner.HtmlDecoder;
import com.sina.book.util.Base64;
import com.sina.book.util.LogUtil;

/**
 * 书籍内容解析并保存
 * 
 * @author MarkMjw
 * 
 */
public class BookContentParser extends BaseParser {
	private static final String TAG = "BookContentParser";
	private static final String ENCODE_GBK = "GBK";

	private Book mBook = null;
	private RandomAccessFile mOutFile = null;
	private long mLastlength = 0;
	private int mChapterId;

	public BookContentParser(Book book, int chapterId) {
		mBook = book;
		mChapterId = chapterId;
		init();
	}

	/*
	 * 初始化
	 */
	private void init() {
		if (null != mBook) {
			String contentFilePath = getBookContentFileName(mBook.getDownloadInfo().getFilePath());
			mBook.getDownloadInfo().setFilePath(contentFilePath);
			File txtFile = new File(contentFilePath);
			try {
				LogUtil.d("FileMissingLog_Read", "BookContentParser >> contentFilePath=" + contentFilePath);
				mOutFile = new RandomAccessFile(txtFile, "rw");
			} catch (FileNotFoundException e) {
				LogUtil.e(TAG, e.getMessage());
			}
		}
	}

	@Override
	protected Object parse(String jsonString) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		Chapter chapter = new Chapter();
		parseDataContent(jsonString);
		if (null != obj && null != mOutFile && ConstantData.CODE_SUCCESS.equals(code)) {
			// String book_id = obj.optString("book_id");
			// String chapter_num = obj.optString("chapter_num");
			String is_vip = obj.optString("is_vip");
			String title = obj.optString("title");
			String content = new String(Base64.decode(obj.optString("content"), Base64.DEFAULT));
			if (content == null || content.length() == 0) {
				code = ConstantData.CODE_FAIL;
				return null;
			}

			// length为本次写入的总数据长度
			int length = write2TxtFile(mChapterId, title, content, is_vip).length;

			chapter.setTitle(title);
			chapter.setGlobalId(mChapterId);
			chapter.setLength(length);
			chapter.setStartPos(mLastlength);
			chapter.setVip(is_vip);

			for (int i = 0; i < mBook.getChapters().size(); i++) {
				Chapter tempChapter = mBook.getChapters().get(i);
				// 更新mBook的章节数据
				if (tempChapter.equals(chapter)) {
					tempChapter.setChapterId(i + 1);
					tempChapter.setStartPos(chapter.getStartPos());
					tempChapter.setLength(chapter.getLength());
					tempChapter.setHasBuy(true);
					// TODO CJL 暂时注释掉，不缓存章节信息到数据库中
					// if (Book.BOOK_LOCAL ==
					// mBook.getDownloadInfo().getLocationType()) {
					// DBService.updateChapter(mBook, chapter2);
					// }
				} else if (tempChapter.getLength() <= 0) {
					// 这个没搞懂，唉
					tempChapter.setStartPos(chapter.getStartPos() + chapter.getLength());
				}
			}

			LogUtil.d("FileMissingLog_Read", "BookContentParser >> 写入完成，当前章节{chapter=" + chapter + "}");

			mLastlength += length;
			// 将章节信息存入数据库
			// DBService.addAllChapter(mBook, chapters);
			// TODO 应该加入更多错误判断，比如json信息有错的情况
			this.setCode(ConstantData.CODE_SUCCESS);
		} else {
			return code;
		}
		return chapter;
	}

	/**
	 * 写入txt文件
	 * 
	 * @param chapters
	 * @param serialNum
	 * @param title
	 * @param content
	 */
	private byte[] write2TxtFile(int serialNum, String title, String content, String isVip) {
		byte[] contentBytes = null;
		try {
			if (content != null) {
				content.trim();
			}
			String contentString = content;
			// TODO: 处理html特殊字符
			contentString = HtmlDecoder.decode(contentString);
			contentString += "\n";
			contentBytes = contentString.getBytes(ENCODE_GBK);

			// TODO 这里对每个byte进行了取反加密
			for (int i = 0; i < contentBytes.length; i++) {
				contentBytes[i] = (byte) ~contentBytes[i];
			}
			mLastlength = mOutFile.length();
			mOutFile.seek(mLastlength);// 移动指针到文件末尾

			mOutFile.write(contentBytes);
		} catch (UnsupportedEncodingException e1) {
			LogUtil.e(TAG, e1.getMessage());
		} catch (IOException e) {
			LogUtil.e(TAG, e.getMessage());
		} finally {
			if (mOutFile != null) {
				try {
					mOutFile.close();
				} catch (IOException e) {
					LogUtil.e(TAG, e.getMessage());
				}
			}
		}
		return contentBytes;
	}

	/**
	 * 获取书籍文件名（.txt）
	 * 
	 * @param tempFilePath
	 * @return
	 */
	private static String getBookContentFileName(String tempFilePath) {
		// 如果还未存到书架，使用在线书籍后缀
		if (tempFilePath != null && tempFilePath.endsWith(Book.TMP_SUFFIX)) {
			int dotIndex = tempFilePath.lastIndexOf(".");
			StringBuilder sb = new StringBuilder(tempFilePath.substring(0, dotIndex));
			sb.append(Book.ONLINE_TMP_SUFFIX);
			return sb.toString();
		} else {
			return tempFilePath;
		}
	}
}