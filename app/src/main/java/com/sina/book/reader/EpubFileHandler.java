package com.sina.book.reader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

import org.htmlcleaner.Utils;

import com.sina.book.control.ICancelable;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.exception.InteruptParseEpubException;
import com.sina.book.exception.UnparseEpubFileException;
import com.sina.book.htmlspanner.HtmlSpanner;
import com.sina.book.util.FileUtils;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * Epub文件解析器
 * 
 * @author MarkMjw
 * @time 2012-10-30 上午11:16:42
 */
public class EpubFileHandler {
	private static final String TAG = "EpubFileHandler";
	private static final String ENCODE_GBK = "GBK";

	/**
	 * 如果你不需要解析过程在你task中断时中断<br>
	 * 一般说来你都需要中断
	 * 
	 * @param book
	 * @return
	 * @throws UnparseEpubFileException
	 */
	public static boolean convertEpub2Dat(Book book)
			throws UnparseEpubFileException {
		return convertEpub2Dat(book, null);
	}

	/**
	 * 转换epub格式文件到Txt文件
	 * 
	 * @param task
	 * @return true if convert success, or false.
	 */
	public static boolean convertEpub2Dat(Book book, ICancelable task)
			throws UnparseEpubFileException {
		String tempFilePath = book.getDownloadInfo().getOriginalFilePath();
		// LogUtil.d(TAG, "convertEpub2Dat:" + tempFilePath);
		boolean flag = false;
		File file = new File(tempFilePath);
		// LogUtil.d("FileMissingLog", "EpubFileHandler >> convertEpub2Dat:" +
		// tempFilePath);
		if (file.exists()) {
			// LogUtil.d("FileMissingLog", "EpubFileHandler >> file exists");
			File txtFile = new File(getBookContentFileName(book));
			// LogUtil.d("FileMissingLog", "EpubFileHandler >> txtFile:" +
			// txtFile != null ? txtFile.getAbsolutePath()
			// : "file is null ");
			String imageFolder = book.getDownloadInfo().getImageFolder();
			if (txtFile.exists()) {
				txtFile.delete();
			}
			RandomAccessFile out = null;
			XhtmlParser handler = null;
			try {
				out = new RandomAccessFile(txtFile, "rw");
				handler = new XhtmlParser(file.getPath(), out, book);
				handler.parseXhtmlFile(task, imageFolder);
				// 更新filesize,filepath
				if (task == null || !task.isCancelled()) {
					book.getDownloadInfo().setFileSize(out.length());
					book.getDownloadInfo().setFilePath(
							txtFile.getAbsolutePath());
					book.setStyledBook(true);
					flag = true;
				}
			} catch (IOException e) {
				// LogUtil.e("FileMissingLog", "IOException >> e=" + e);
			} catch (InteruptParseEpubException e) {
				// Task 被中断
				// LogUtil.e("FileMissingLog",
				// "InteruptParseEpubException >> e=" + e);
			} catch (Exception e) {
				// LogUtil.e("FileMissingLog", "Exception >> e=" + e);
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					// LogUtil.e("FileMissingLog", "finally Exception >> e=" +
					// e.getMessage());
					if (txtFile.exists()) {
						LogUtil.d(
								TAG,
								"Delete temp file(*.epub) : "
										+ txtFile.getAbsolutePath() + ":"
										+ txtFile.delete());
					}
					LogUtil.e(TAG, e.getMessage());
				}
			}
		} else {
			// LogUtil.d("FileMissingLog",
			// "EpubFileHandler >> file not exists");
		}
		return flag;
	}

	public static String getBookContentFileName(Book book) {
		String tempFilePath = book.getDownloadInfo().getFilePath();
		int dotIndex = tempFilePath.lastIndexOf(".");
		StringBuilder sb = new StringBuilder(
				tempFilePath.substring(0, dotIndex));
		sb.append(Book.BOOK_SUFFIX);
		return sb.toString();
	}

	/**
	 * xhtml 文件解析器
	 * 
	 * @author MarkMjw
	 * 
	 */
	private static class XhtmlParser {
		private String CHINESE_MATCH = "^[\\s|　]*第[0-9 | 一 | 二  | 三 | 四 | 五 | 六 | 七 | 八 | 九 | 十 | 零]+[章|节]";
		private String ENGLISH_MATCH = "^[\\s|　]*[c|C]hapter";

		private Book mBook;
		private Chapter mLastChapter;
		private ArrayList<Chapter> mChapters;
		private String mFilePath;
		private RandomAccessFile mOutFile;
		private long mLastlength = 0;

		private Pattern mChineseTitlePattern;
		private Pattern mEnglishTitlePattern;

		public XhtmlParser(String filePath, RandomAccessFile outFile, Book book)
				throws IOException {
			this.mFilePath = filePath;
			this.mOutFile = outFile;
			mBook = book;
			mLastChapter = book.getLastChapter();
			this.mLastlength = mLastChapter.getStartPos()
					+ mLastChapter.getLength();
			mChapters = new ArrayList<Chapter>();
			outFile.seek(mLastlength);
			mChineseTitlePattern = Pattern.compile(CHINESE_MATCH);
			mEnglishTitlePattern = Pattern.compile(ENGLISH_MATCH);
		}

		/**
		 * 解析xhtml文件
		 * 
		 * @throws InteruptParseEpubException
		 *             中断异常
		 * @throws UnparseEpubFileException
		 *             无法解析Epub文件异常
		 */
		public void parseXhtmlFile(ICancelable task, String imageFolder)
				throws UnparseEpubFileException, InteruptParseEpubException {
			try {
				EpubReader epubReader = new EpubReader();

				MediaType[] lazyTypes = { MediatypeService.CSS,
						MediatypeService.GIF, MediatypeService.JPG,
						MediatypeService.PNG, MediatypeService.SVG,
						MediatypeService.OPENTYPE, MediatypeService.TTF,
						MediatypeService.XPGT };
				// LogUtil.e("FileMissingLog",
				// "EpubFileHandler >> parseXhtmlFile >> mFilePath = " +
				// mFilePath);
				nl.siegmann.epublib.domain.Book book = epubReader.readEpubLazy(
						mFilePath, "UTF-8", Arrays.asList(lazyTypes));
				if (task != null && task.isCancelled()) {
					throw new InteruptParseEpubException();
				}
				readImages(book, imageFolder);
				if (task != null && task.isCancelled()) {
					throw new InteruptParseEpubException();
				}
				
//				readChaptersInfo(book, task);
//				if (task != null && task.isCancelled()) {
//					throw new InteruptParseEpubException();
//				}
				readBookInfo(book);// 必须在读取章节信息之后执行
				if (task != null && task.isCancelled()) {
					throw new InteruptParseEpubException();
				}
			} catch (IOException e) {
				LogUtil.e(TAG, e.getMessage());
			}
		}

		/**
		 * 读取章节信息
		 * 
		 * @param book
		 * @throws IOException
		 * @throws Exception
		 */
		private void readChaptersInfo(nl.siegmann.epublib.domain.Book book,
				ICancelable task) throws UnparseEpubFileException, IOException,
				InteruptParseEpubException {
			List<Resource> lists = book.getContents();
			int chapId = 1;
			int count = 0;
			HtmlSpanner titleSpanner = new HtmlSpanner(false);
			HtmlSpanner bodySpanner = new HtmlSpanner(true);

			for (Resource resource : lists) {
				if (task != null && task.isCancelled()) {
					throw new InteruptParseEpubException();
				}
				Chapter chapter = new Chapter();
				String data = resource.getDataString();
				StringBuilder head = titleSpanner.fromHtml(data);
				StringBuilder body = bodySpanner.fromHtml(data);

				if (null != head && null != body) {
					String title = head.toString().trim();
					String content = body.toString().trim();
					// LogUtil.d("FileMissingLog", "title = " + title +
					// ", content = " + content);

					if ("".equals(title)) {
						// LogUtil.i("FileMissingLog", "matchTitleFromContent");
						title = matchTitleFromContent(content, chapId);
					}

					if (!"".equals(content) && content.length() > 1) {
						// LogUtil.i("FileMissingLog", "add chapter ");
						addChapter(chapter, chapId++, title, content);
					} else {
						count++;
						// LogUtil.i("FileMissingLog", "count++ (1)");
					}
				} else {
					count++;
					// LogUtil.i("FileMissingLog", "count++ (2)");
				}
				// LogUtil.e("FileMissingLog", "count = " + count);
				// 尽早释放资源中的数据
				resource.close();
			}
			// LogUtil.e("FileMissingLog", "count = " + count +
			// ", lists.size() = " + lists.size());
			if (count >= lists.size()) {
				throw new UnparseEpubFileException("Unparse the file (*.epub).");
			}
		}

		/**
		 * 增加章节到书籍对象
		 * 
		 * @param chapter
		 * @param chapId
		 * @param title
		 * @param contentStr
		 * @throws UnsupportedEncodingException
		 */
		private void addChapter(Chapter chapter, int chapId, String title,
				String contentStr) throws UnsupportedEncodingException {
			byte[] contentBytes = null;
			byte[] newLineBytes = null;
			try {
				contentBytes = contentStr.getBytes(ENCODE_GBK);
				long oldFileLength = mOutFile.length();
				mOutFile.seek(oldFileLength);// 移动指针到文件末尾
				mOutFile.write(contentBytes);
				newLineBytes = "\n".getBytes(ENCODE_GBK);
				mOutFile.write(newLineBytes);
				int length = contentBytes.length + newLineBytes.length;
				chapter.setLength(length);
				chapter.setStartPos(mLastlength);
				mLastlength += length;

				chapter.setTitle(title);
				chapter.setChapterId(chapId++);
				mChapters.add(chapter);
			} catch (UnsupportedEncodingException e1) {
				LogUtil.e(TAG, e1.getMessage());
			} catch (IOException e) {
				LogUtil.e(TAG, e.getMessage());
			}
		}

		/**
		 * 设置书籍信息
		 * 
		 * @param book
		 */
		private void readBookInfo(nl.siegmann.epublib.domain.Book book) {
			if (null == book)
				return;

			List<Author> authors = book.getMetadata().getAuthors();
			String authorName = "";
			if (null != authors && authors.size() > 0) {
				for (Author author : authors) {
					authorName += author.getFirstname() + author.getLastname();
					authorName += ";";
				}
			}
			mBook.setAuthor(authorName);
			if (null == book.getTitle() || "".equalsIgnoreCase(book.getTitle())) {
				int startIndex = mFilePath.lastIndexOf("/");
				if (startIndex == -1) {
					startIndex = 0;
				} else {
					startIndex = startIndex + 1;
				}
				int endIndex = mFilePath.lastIndexOf(".");
				if (endIndex == -1 || endIndex <= startIndex) {
					endIndex = mFilePath.length();
				}
				mBook.setTitle(mFilePath.substring(startIndex, endIndex));
			} else {
				mBook.setTitle(book.getTitle());
			}
			mBook.setChapters(mChapters);
			mBook.setNum(mChapters.size());
			List<String> descriptions = book.getMetadata().getDescriptions();
			if (null != descriptions && descriptions.size() > 0) {
				mBook.setIntroRealNeed(descriptions.get(0));
			}
		}

		private void readImages(final nl.siegmann.epublib.domain.Book book,
				final String imageFolder) {
			if (null == book) {
				return;
			}
			// 先在UI线程存储cover的路径
			final Resource coverResource = book.getCoverImage();
			if (coverResource != null && coverResource.getHref() != null) {
				String fileName = imageFolder
						+ Util.getPathName(coverResource.getHref());
				mBook.getDownloadInfo().setImageUrl(fileName);

				// 另启线程存储所有图片
				new Thread(new Runnable() {
					public void run() {
						InputStream inputStream = null;
						FileOutputStream fos = null;
						byte[] data = new byte[1024];
						int len = 0;

						try {
							if (StorageUtil.isSDCardExist()) {
								File folder = new File(imageFolder);
								if (!folder.exists()) {
									folder.mkdir();
								}

								String href = coverResource.getHref();
								if (Utils.isEmptyString(href)) {
									return;
								}
								String fileName = imageFolder
										+ Util.getPathName(href);
								File imgFile = new File(fileName);
								if (!imgFile.exists()) {
									FileUtils.checkAndCreateFile(fileName);
									inputStream = coverResource.getInputStream();
									fos = new FileOutputStream(imgFile);
									while ((len = inputStream.read(data, 0,
											data.length)) != -1) {
										fos.write(data, 0, len);
									}
									if (fos != null) {
										fos.close();
									}
									if (inputStream != null) {
										inputStream.close();
									}
								}

								coverResource.close();
							}
						} catch (IOException e) {
						} finally {
							try {
								if (null != inputStream) {
									inputStream.close();
								}
							} catch (IOException e) {
							}
						}
					}
				}).start();
			}

			// // 另启线程存储所有图片
			// new Thread(new Runnable() {
			// public void run() {
			// InputStream inputStream = null;
			// FileOutputStream fos = null;
			// byte[] data = new byte[1024];
			// int len = 0;
			//
			// try {
			// if (StorageUtil.isSDCardExist()) {
			// File folder = new File(imageFolder);
			// if (!folder.exists()) {
			// folder.mkdir();
			// }
			//
			// List<Resource> resources = book.getImageResources();
			// if (null != resources) {
			// for (Resource resource : resources) {
			// if (null != resource) {
			// String href = resource.getHref();
			// if (Utils.isEmptyString(href)) {
			// continue;
			// }
			// String fileName = imageFolder + Util.getPathName(href);
			// File imgFile = new File(fileName);
			// if (!imgFile.exists()) {
			// // TODO
			// // imgFile.createNewFile();
			// FileUtils.checkAndCreateFile(fileName);
			// inputStream = resource.getInputStream();
			// fos = new FileOutputStream(imgFile);
			// while ((len = inputStream.read(data, 0, data.length)) != -1) {
			// fos.write(data, 0, len);
			// }
			// if (fos != null) {
			// fos.close();
			// }
			// if (inputStream != null) {
			// inputStream.close();
			// }
			// }
			//
			// resource.close();
			// }
			// }
			// }
			// }
			// } catch (IOException e) {
			// LogUtil.w(TAG, e.getMessage());
			// } finally {
			// try {
			// if (null != inputStream) {
			// inputStream.close();
			// }
			// } catch (IOException e) {
			// LogUtil.w(TAG, e.getMessage());
			// }
			// }
			// }
			// }).start();
		}

		/**
		 * 针对epub格式的章节名称做匹配和新建
		 * 
		 * @param content
		 * @param chapId
		 * @return
		 */
		private String matchTitleFromContent(String content, int chapId) {
			if (content == null || content.length() == 0) {
				return "chapter" + chapId;
			}
			int lineIndex = content.indexOf("\n");
			if (lineIndex <= 0) {
				return "chapter" + chapId;
			}
			String maybeTitle = content.substring(0, lineIndex);
			// 替换掉样式头
			maybeTitle = maybeTitle.replaceFirst("\\[<\\(.\\)>\\]\\[.+\\]", "");
			Matcher m = null;
			m = mChineseTitlePattern.matcher(maybeTitle);
			if (m.find()) {
				return maybeTitle.trim();
			}
			m = mEnglishTitlePattern.matcher(maybeTitle);
			if (m.find()) {
				return maybeTitle.trim();
			}
			// 实在匹配不到新建一个
			return "chapter" + chapId;
		}
	}
}
