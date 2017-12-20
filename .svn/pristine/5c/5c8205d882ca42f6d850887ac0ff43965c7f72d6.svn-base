package com.sina.book.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;

import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.control.download.DownBookTask;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.db.DBService;
import com.sina.book.exception.InteruptSaxXmlException;
import com.sina.book.htmlspanner.HtmlDecoder;
import com.sina.book.reader.charset.EncodingHelper;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.LogUtil;
import com.sina.book.util.NumericHelper;
import com.sina.book.util.StorageUtil;

/**
 * 小说文件解析
 * 
 * @author Tsimle
 * 
 */
public class SinaXmlHandler {
	private static final String TAG = "SinaXmlHandler";
	private static final String ENCODE_GBK = "GBK";

	public static boolean convertXml2Dat(DownBookTask task, String debugTagInfo) {
		Book book = task.getJob().getBook();
		String tempFilePath = book.getDownloadInfo().getFilePath();
		// LogUtil.i(TAG, "convertFictionFile:" + tempFilePath);
		// LogUtil.i(TAG, "SinaXmlHandler >> debugTagInfo=" + debugTagInfo +
		// " {book=" + book + "}");
		// LogUtil.i(TAG, "SinaXmlHandler >> debugTagInfo=" + debugTagInfo +
		// " convertFictionFile:" + tempFilePath);

		if (tempFilePath.endsWith(Book.ONLINE_TMP_SUFFIX)) {
			tempFilePath = getTmpFileName(tempFilePath);
		}

		boolean flag = false;
		File file = new File(tempFilePath);
		if (file.exists() && file.length() > 0) {
			String contentFilePath = getBookContentFileName(tempFilePath);
			File txtFile = new File(contentFilePath);
			BufferedReader in = null;
			RandomAccessFile out = null;
			XmlParseHandler handler = null;
			String encoding = EncodingHelper.getOurBookEncoding(file);
			LogUtil.i(TAG, "file encoding:" + encoding);

			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
				out = new RandomAccessFile(txtFile, "rw");
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				XMLReader reader = parser.getXMLReader();
				handler = new XmlParseHandler(out, task);
				reader.setContentHandler(handler);
				InputSource source = new InputSource(in);

				reader.parse(source);

				// 将下载解析的chapter列表存储数据库
				DBService.updateAllChapter(book, handler.getChapters(), task, "SinaXmlHandler >> convertXml2Dat(1)"
						+ debugTagInfo);

				// 迁移oltmp中的收费章节
				int payType = book.getBuyInfo().getPayType();

				if (payType == Book.BOOK_TYPE_CHAPTER_VIP) {
					// TODO:按章收费
					DownBookManager.getInstance().onTaskUpdate(book, false, false, DownBookJob.STATE_PARSER);
					copyOltmpData(book, handler.getChapters());
					deleteTempFile(book);
				} else {
					// 全本收费或免费书籍不需要拼接oltmp
				}

				// 3 删除原文件
				if (task == null || !task.isCancelled()) {
					file.delete();
					flag = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				if (e instanceof InteruptSaxXmlException) {
					// 此异常可能为自己抛出用来中断解析task
					LogUtil.i("InteruptSaxXmlException");
				} else {
					DBService.updateAllChapter(task.getJob().getBook(), handler.getChapters(), task,
							"SinaXmlHandler >> SAXException, " + debugTagInfo);
					// 3 删除原文件
					if (task == null || !task.isCancelled()) {
						file.delete();
						flag = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return flag;
	}

	private static void deleteTempFile(Book book) {
		String path = book.getDownloadInfo().getFilePath();
		if (null != path && path.contains(StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK))) {

			int dotIndex = path.lastIndexOf(".");
			if (dotIndex > 0) {
				String oltmpPath = path.substring(0, dotIndex) + Book.ONLINE_TMP_SUFFIX;
				File bookOlFile = new File(oltmpPath);
				if (bookOlFile.exists()) {
					LogUtil.i(TAG, "SinaXmlHandler >> deleteTempFile:" + oltmpPath);
					bookOlFile.delete();
				}
			}
		}
	}

	private static void copyOltmpData(Book book, ArrayList<Chapter> list) {
		if (list == null || list.size() == 0) {
			return;
		}

		int freesize = list.size();
		// LogUtil.d("BugID=21413",
		// "SinaXmlHandler >> copyOltmpData >> {免费章节数 freesize=" + freesize +
		// "}");

		Chapter lastCp = list.get(freesize - 1);
		long mStartPos = lastCp.getLastPos();

		ArrayList<Chapter> chapters = book.getChapters();
		if (chapters != null && chapters.size() > 0) {
			int csize = chapters.size();

			for (int i = 0; i < csize; ++i) {
				Chapter chapter = chapters.get(i);
				int startPos = (int) chapter.getStartPos();
				int cpLength = (int) chapter.getLength();

				// LogUtil.d("BugID=21413",
				// "SinaXmlHandler >> copyOltmpData >> {chapter=" + chapter +
				// "}");
				// 将Vip章节数据添加到所有免费章节数据后面，并更新其startPos数据
				if (chapter.isVip() && cpLength > 0) {
					// LogUtil.e("BugID=21413",
					// "SinaXmlHandler >> copyOltmpData >> {chapter=" + chapter
					// + "}");
					String oltmpPath = getOltmpFileName(book.getDownloadInfo().getFilePath(), Book.ONLINE_TMP_SUFFIX);
					byte[] data = getVipOltmpData(oltmpPath, startPos, cpLength);
					if (data == null) {
						oltmpPath = getOltmpFileName(book.getDownloadInfo().getFilePath(), Book.ONLINE_DAT_SUFFIX);
						data = getVipOltmpData(oltmpPath, startPos, cpLength);
					}

					if (data != null && data.length > 0) {
						boolean result = saveVipChapterData(data, book);
						if (result) {
							// 修改chapter索引
							chapter.setStartPos(mStartPos);
							// 存储数据库
							DBService.insertChapter(book, chapter);

							mStartPos += cpLength;
						}
					}
				}
			}
		}
	}

	// 获取oltmp路径
	private static String getOltmpFileName(String tempFilePath, String suffix) {
		if (tempFilePath != null && tempFilePath.endsWith(Book.TMP_SUFFIX)) {
			int dotIndex = tempFilePath.lastIndexOf(".");
			StringBuilder sb = new StringBuilder(tempFilePath.substring(0, dotIndex));
			sb.append(suffix);
			return sb.toString();
		} else {
			return tempFilePath;
		}
	}

	private static String getTmpFileName(String tempFilePath) {
		if (tempFilePath != null && tempFilePath.endsWith(Book.ONLINE_TMP_SUFFIX)) {
			int dotIndex = tempFilePath.lastIndexOf(".");
			StringBuilder sb = new StringBuilder(tempFilePath.substring(0, dotIndex));
			sb.append(Book.TMP_SUFFIX);
			return sb.toString();
		} else {
			return tempFilePath;
		}
	}

	private static byte[] getVipOltmpData(String tempFilePath, int start, int length) {
		File file = new File(tempFilePath);
		byte[] data = null;
		if (file != null && file.exists()) {
			FileInputStream fis = null;
			data = new byte[length];
			try {
				fis = new FileInputStream(file);
				fis.skip(start);
				int len = 0;
				int offset = 0;
				while ((len = fis.read(data, offset, data.length - offset)) > 0) {
					offset += len;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					fis = null;
				}
			}
		}
		return data;
	}

	private static boolean saveVipChapterData(byte[] data, Book book) {
		String path = getBookContentFileName(book.getDownloadInfo().getFilePath());
		File file = new File(path);
		if (file != null && file.exists()) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file, true);
				fos.write(data);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					fos = null;
				}
			}
		}
		return false;
	}

	public static String getBookContentFileName(String tempFilePath) {
		int dotIndex = tempFilePath.lastIndexOf(".");
		StringBuilder sb = new StringBuilder(tempFilePath.substring(0, dotIndex));
		sb.append(Book.BOOK_SUFFIX);
		return sb.toString();
	}

	private static class XmlParseHandler extends DefaultHandler {
		private final String TAG_CHAPTERNUM = "serial_num";
		private final String TAG_CHAPTER = "chapter";
		private final String TAG_ITEM = "item";
		private final String TAG_CHAPTERID = "chapter_id";
		private final String TAG_TITLE = "title";
		private final String TAG_CONTENT = "content";
		private final String TAG_ISVIP = "is_vip";
		// private final String TAG_INFO = "info";
		// private final String TAG_BOOKID = "book_id";
		// private final String TAG_DATA = "data";

		private final int STATE_CHAPTERNUM = 1;
		private final int STATE_CHAPTERID = 2;
		private final int STATE_TITLE = 3;
		private final int STATE_CONTENT = 4;
		private final int STATE_ISVIP = 5;

		private int currentState = 0;

		private Chapter chapter;

		private int chapter_id;
		private Chapter lastChapter;
		private DownBookTask mTask;
		private ArrayList<Chapter> chapters;
		private RandomAccessFile contentOutputStream;
		private StringBuilder title;
		private StringBuilder content;
		private StringBuilder isVip;
		private StringBuilder globalChapterId;
		private long lastlength = 0;

		public XmlParseHandler(RandomAccessFile contentOutputStream, DownBookTask task) throws IOException {
			boolean mSkip = false;
			this.contentOutputStream = contentOutputStream;
			// TODO
			// lastChapter = task.getJob().getBook().getLastChapter();
			// this.lastlength = lastChapter.getStartPos() +
			// lastChapter.getLength();
			// this.chapter_id = lastChapter.getChapterId();

			// 默认值
			this.lastlength = 0;
			this.chapter_id = 0;

			String mUrl = task.getUrl();
			LogUtil.i("RequestTask-DownFileTask", "SinaXmlHandler >> mUrl=" + mUrl);
			if (mUrl != null) {
				String mStartNum = HttpUtil.getUrlKeyValue(mUrl, "s_num");
				LogUtil.i("RequestTask-DownFileTask", "SinaXmlHandler >> mStartNum=" + mStartNum);
				try {
					if (!TextUtils.isEmpty(mStartNum) && NumericHelper.isNumeric(mStartNum)) {
						int start_num = NumericHelper.parseInt(mStartNum, 0);
						LogUtil.i("RequestTask-DownFileTask", "SinaXmlHandler >> start_num=" + start_num);
						// 断章下载
						if (start_num >= 2) {
							// 获取偏移信息
							lastChapter = task.getJob().getBook().getLastChapter();
							this.lastlength = lastChapter.getStartPos() + lastChapter.getLength();
							this.chapter_id = lastChapter.getChapterId();
							// 没对上，需要进一步处理
							int preStartChapterId = start_num - 1;
							LogUtil.i("RequestTask-DownFileTask", "SinaXmlHandler >> chapter_id=" + chapter_id);
							if (chapter_id != preStartChapterId) {
								Chapter preChapter = task.getJob().getBook().getChapterByPosition(preStartChapterId);
								LogUtil.i("RequestTask-DownFileTask", "SinaXmlHandler >> preChapter=" + preChapter);
								if (preChapter != null && preChapter.getStartPos() != 0 && preChapter.getLength() != 0
										&& preStartChapterId == preChapter.getChapterId()) {
									lastChapter = preChapter;
									this.lastlength = lastChapter.getStartPos() + lastChapter.getLength();
									this.chapter_id = lastChapter.getChapterId();
								}
							}
							mSkip = true;
						} else {
							this.lastlength = 0;
							this.chapter_id = 0;
						}
					}
				} catch (NumberFormatException e) {
					// TODO: handle exception
					LogUtil.i("RequestTask-DownFileTask", "SinaXmlHandler >> NumberFormatException=" + e);
				}
			}

			LogUtil.i("SinaXmlHandler", "--4--SinaXmlHandler >> mSkip=" + mSkip);
			if (mSkip)
				contentOutputStream.seek(lastlength);

			this.mTask = task;
			chapters = new ArrayList<Chapter>();
			content = new StringBuilder(3000);
			title = new StringBuilder();
			isVip = new StringBuilder();
			globalChapterId = new StringBuilder();
		}

		public ArrayList<Chapter> getChapters() {
			return chapters;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equals(TAG_ITEM)) {
				if (mTask != null && mTask.isCancelled()) {
					throw new InteruptSaxXmlException();
				}
				chapter = new Chapter();
				currentState = 0;
				return;
			}
			if (localName.equals(TAG_CHAPTERID)) {
				currentState = STATE_CHAPTERID;
				return;
			}
			if (localName.equals(TAG_TITLE)) {
				currentState = STATE_TITLE;
				return;
			}
			if (localName.equals(TAG_CONTENT)) {
				currentState = STATE_CONTENT;
				return;
			}
			if (localName.equals(TAG_ISVIP)) {
				currentState = STATE_ISVIP;
				return;
			}
			if (localName.equals(TAG_CHAPTERNUM)) {
				currentState = STATE_CHAPTERNUM;
				return;
			}
			if (localName.equals(TAG_CHAPTER)) {
				currentState = 0;
				return;
			}
			currentState = 0;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals(TAG_ITEM)) {
				chapter.setHasBuy(true);
				chapters.add(chapter);
				return;
			} else if (localName.equals(TAG_CONTENT)) {

				LogUtil.i("SinaXmlHandler", "--5--TAG_CONTENT---contentOutputStream： " + contentOutputStream);
				byte[] contentBytes = null;
				int strLength = 0;
				try {
					String contentString = content.append("\n").toString();
					strLength = contentString.length();
					// TODO: 处理html特殊字符
					contentString = HtmlDecoder.decode(contentString);

					contentBytes = contentString.getBytes(ENCODE_GBK);
					// TODO 这里对每个byte进行了取反加密
					for (int i = 0; i < contentBytes.length; i++) {
						contentBytes[i] = (byte) ~contentBytes[i];
					}
					contentOutputStream.write(contentBytes);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				int length = 0;
				if (null != contentBytes) {
					length = contentBytes.length;
				}
				chapter.setLength(length);
				chapter.setStartPos(lastlength);
				lastlength = lastlength + length;
				content.delete(0, strLength);
			} else if (localName.equals(TAG_TITLE)) {
				chapter.setTitle(title.toString());
				title.delete(0, title.length());
			} else if (localName.equals(TAG_ISVIP)) {
				chapter.setVip(isVip.toString());
				isVip.delete(0, isVip.length());
			} else if (localName.equals(TAG_CHAPTERID)) {
				chapter_id++;
				chapter.setChapterId(chapter_id);

				chapter.setGlobalId(Integer.parseInt(globalChapterId.toString()));
				globalChapterId.delete(0, globalChapterId.length());
			}
			currentState = 0;
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			switch (currentState) {
			case STATE_CHAPTERID:
				globalChapterId.append(ch, start, length);
				break;
			case STATE_TITLE:
				title.append(ch, start, length);
				break;
			case STATE_CONTENT:
				content.append(ch, start, length);
				break;
			case STATE_ISVIP:
				isVip.append(ch, start, length);
				break;
			default:
				return;
			}
		}
	}
}
