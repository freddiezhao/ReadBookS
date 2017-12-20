package com.sina.book.reader.model;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Paint;

import com.sina.book.control.GenericTask;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.data.ScreenInfo;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.reader.charset.EncodingHelper;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

/**
 * 负责新浪书城的图书数据读取<br>
 * TODO 先按来的数据获取方式对结构重构
 * 
 * @author Tsimle
 * 
 */
public class SinaBookModel {
	private static final String TAG = "SinaBookModel";
	private static int BYTES_READ_LIMIT_FORWARD = 1500;

	/**
	 * 如果限制了往回读的长度，就会引起上一页的数据与实际的不符合，<br>
	 * 因为是按照回读到的位置为段首来生成的数据
	 */
	private static int BYTES_READ_LIMIT_BACK = 4500;

	/**
	 * 一次读取50KB的数据
	 */
	private static final int BLOCK_SIZE = 50000;

	/**
	 * 向前回读一页是很耗时的事情，所以缓存下来<br>
	 * key即这一页结束的字节位置
	 */
	private Map<Integer, Page> mPageCache;

	private String mContentCharset = "GBK";

	private Book mBook;
	private File mBookFile;

	private MappedByteBuffer mMbBuffer;
	private ArrayList<Byte> mBytesCache = new ArrayList<Byte>();
	private long mMbBufferLen = 0;
	private int mMbBufferBegin = 0;
	private int mMbBufferEnd = 0;

	private int mCurPage = 0;
	private int mTotalPage = 0;
	private GenericTask mPageTask;

	private boolean mIsfirstPage;
	private boolean mIslastPage;

	private float[] mMeasuredWidth;

	private RefreshMark mRefreshMark;
	private ReadStyleManager mReadStyleManager;

	private int BYTE_CACHE_SIZE_LIMIT = ScreenInfo.DEFAULT_LIMIT_SIZE;

	public SinaBookModel(Book book, ReadStyleManager readStyleManager) throws IOException {
		mBook = book;
		mReadStyleManager = readStyleManager;
		mMeasuredWidth = new float[] { 1 };
		mPageCache = new LinkedHashMap<Integer, Page>(5, 0.75f, true) {

			private static final long serialVersionUID = 5761625939456237813L;

			@Override
			protected boolean removeEldestEntry(Entry<Integer, Page> eldest) {

				return size() > 50;
			}
		};

		// 判断分辨率
		BYTE_CACHE_SIZE_LIMIT = ScreenInfo.getInstance().computeSinaBookModelLimitSize();

		openbook(mBook);
	}

	public Chapter getCurrentChapter() {
		return mBook.getCurrentChapter(mMbBufferBegin);
	}

	public int getCurrentChapterIndex() {
		return mBook.getCurrentChapterIndex(mMbBufferBegin);
	}

	/**
	 * 需要在异步任务完成后刷新文件，并加载这里记录的位置<br>
	 */
	public void prepareRefreshBookFile() {
		Chapter curChapter = getCurrentChapter();
		if (curChapter != null) {
			int globalChapterId = curChapter.getGlobalId();
			int beginOffset = mMbBufferBegin - (int) curChapter.getStartPos();
			int endOffset = mMbBufferEnd - (int) curChapter.getStartPos();
			if (globalChapterId >= 0 && beginOffset >= 0 && endOffset >= beginOffset) {
				mRefreshMark = new RefreshMark();
				mRefreshMark.chapterIndex = globalChapterId;
				mRefreshMark.beginOffset = beginOffset;
				mRefreshMark.endOffset = endOffset;
			}
		}
	}

	/**
	 * 刷新文件
	 * 
	 * @throws java.io.IOException
	 */
	public void refreshBookFile() throws IOException {
		mMbBufferLen = mBookFile.length();
		RandomAccessFile raf = new RandomAccessFile(mBookFile, "r");
		mMbBuffer = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0L, mMbBufferLen);
		if (mRefreshMark != null) {
			Chapter lastChapter = mBook.getCurrentChapterById(mRefreshMark.chapterIndex);
			mMbBufferBegin = (int) lastChapter.getStartPos() + mRefreshMark.beginOffset;
			mMbBufferEnd = (int) lastChapter.getStartPos() + mRefreshMark.endOffset;
			mRefreshMark = null;
		}
		raf.close();
	}

	/**
	 * 如果书本的文件已经发生变化，重新打开文件<br>
	 * 否则执行refreshBookFile()
	 * 
	 * @throws java.io.IOException
	 */
	public void refreshOrReOpenBookFile(Book book) throws IOException {
		if (book == null) {
			return;
		}
		if (book.getDownloadInfo().getFilePath().equals(mBookFile.getAbsolutePath())) {
			refreshBookFile();
		} else {
			String newFilePath = book.getDownloadInfo().getFilePath();
			File newfile = new File(newFilePath);
			if (newfile.exists()) {
				mBook.getDownloadInfo().setFilePath(newFilePath);
				mBookFile = newfile;
				mMbBufferLen = mBookFile.length();
				RandomAccessFile raf = new RandomAccessFile(mBookFile, "r");
				mMbBuffer = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0L, mMbBufferLen);
				if (mRefreshMark != null) {
					Chapter lastChapter = mBook.getCurrentChapterById(mRefreshMark.chapterIndex);
					if (lastChapter != null) {
						mMbBufferBegin = (int) lastChapter.getStartPos() + mRefreshMark.beginOffset;
						mMbBufferEnd = (int) lastChapter.getStartPos() + mRefreshMark.endOffset;
						mRefreshMark = null;
					} else {
						mMbBufferBegin = 0;
						mMbBufferEnd = 0;
					}
				} else {
					/*
					 * 解决微博呼起阅读页，进入摘要页->书架下载，下载完成调用update时段落缓存索引丢失。
					 * 书摘添加不上(因为end为0了，判断条件不成立了)
					 */
					// mMbBufferBegin = 0;
					// mMbBufferEnd = 0;
				}
				mPageCache.clear();
				raf.close();
			}
		}
	}

	/**
	 * 映射书本文件
	 * 
	 * @param book
	 * @throws java.io.IOException
	 */
	private void openbook(Book book) throws IOException {
		mBook = book;
		String filePath = mBook.getDownloadInfo().getFilePath();
		LogUtil.d("SQLiteUpdateErrorLog", "SinaBookModle >> openbook >1> filePath=" + filePath);
		// 打包到客户端内的内置书籍，需要将其从assets文件夹中导入到手机内存卡中再进行读取
		if (filePath.startsWith("file:///")) {
			filePath = StorageUtil.copyFile(filePath.substring(filePath.lastIndexOf('/')));
			mBook.getDownloadInfo().setFilePath(filePath);
		}
		LogUtil.d("SQLiteUpdateErrorLog", "SinaBookModle >> openbook >2> filePath=" + filePath);

		LogUtil.d("FileMissingLog", "SinaBookModle >> openbook >> filePath=" + filePath);

		mBookFile = new File(filePath);
		mContentCharset = EncodingHelper.getEncoding(mBook);
		mMbBufferLen = mBookFile.length();
		LogUtil.d("FileMissingLog", "SinaBookModle >> openbook >> mMbBufferLen=" + mMbBufferLen);
		RandomAccessFile raf = new RandomAccessFile(mBookFile, "r");
		mMbBuffer = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0L, mMbBufferLen);
		raf.close();
	}

	/**
	 * 释放pageFactory正在进行的异步操作，算页数
	 */
	public void release() {
		if (mPageTask != null) {
			mPageTask.cancel(true);
			mPageTask = null;
		}
	}

	/**
	 * 往回翻页
	 */
	public PageContent prePage() {
		PageContent pageContent = new PageContent();

		Chapter chapter = getCurrentChapter();
		if (chapter != null) {
			int pageSize = mMbBufferEnd - mMbBufferBegin;
			if (mMbBufferBegin < chapter.getStartPos() + chapter.getLength()
					&& mMbBufferBegin >= (chapter.getStartPos() + pageSize / 2)) {
				if (mMbBufferBegin < (chapter.getStartPos() + pageSize)) {

				}
			} else if (mMbBufferBegin == chapter.getStartPos()) {
				Chapter preChapter = mBook.getPreChapter(chapter);
				if (preChapter != null) {
					if (preChapter.getLength() > 0) {
						mMbBufferBegin = (int) (preChapter.getStartPos() + preChapter.getLength() - 1);
						return prePage();
					} else {
						mIsfirstPage = true;
						return pageContent;
					}
				} else {
					mIsfirstPage = true;
					return pageContent;
				}
			}
		}
		if (mMbBufferBegin <= 0 || mMbBufferBegin >= mMbBufferLen) {
			mMbBufferBegin = 0;
			mIsfirstPage = true;
			return pageContent;
		}

		mIsfirstPage = false;
		if (mTotalPage != 0) {
			mCurPage--;
			if (mCurPage < 1) {
				mCurPage = 1;
			}
		}

		Page p = mPageCache.get(mMbBufferBegin);
		if (p != null) {
			mMbBufferBegin = p.begin;
			mMbBufferEnd = mMbBufferBegin;
		} else {
			p = mPageCache.get(mMbBufferBegin + 1);
			if (p != null && (p.end - p.begin > 1)) {
				mMbBufferBegin = p.begin;
				mMbBufferEnd = mMbBufferBegin;
			} else {
				pageUp(chapter);
			}
		}
		pageContent = pageDown();

		if (mMbBufferBegin <= 0) {
			if (mTotalPage != 0) {
				mCurPage = 1;
			}
		}
		return pageContent;
	}

	/**
	 * 往前翻页
	 */
	public PageContent nextPage() {
		PageContent pageContent = new PageContent();

		Chapter chapter = getCurrentChapter();
		if (chapter != null) {
			if (mMbBufferEnd >= chapter.getStartPos() + chapter.getLength()) {
				Chapter nextChapter = mBook.getNextChapter(chapter);
				if (nextChapter != null) {
					if (nextChapter.getLength() > 0) {
						// LogUtil.e(TAG, nextChapter.toString());
						mMbBufferBegin = (int) nextChapter.getStartPos();
						mMbBufferEnd = mMbBufferBegin;
					} else {
						mIslastPage = true;
						return pageContent;
					}
				} else {
					mIslastPage = true;
					return pageContent;
				}
			}
		}
		if (mMbBufferEnd >= mMbBufferLen) {
			mIslastPage = true;
			return pageContent;
		}
		mIslastPage = false;

		if (mTotalPage != 0) {
			mCurPage++;
		}

		mMbBufferBegin = mMbBufferEnd;
		pageContent = pageDown();
		Page p = new Page(mMbBufferBegin, mMbBufferEnd);
		mPageCache.put(p.end, p);
		if (mMbBufferEnd >= mMbBufferLen) {
			if (mTotalPage != 0) {
				if (mCurPage != mTotalPage) {
					mTotalPage = mCurPage;
					mBook.getBookPage().setTotalPage(mTotalPage);
					mBook.getBookPage().setFontSize(mReadStyleManager.getCurReadFontSizeInSp());
				}
			}
		}
		return pageContent;
	}

	/**
	 * 返回当前页的内容
	 */
	public PageContent curPage() {
		mMbBufferEnd = mMbBufferBegin;
		return pageDown();
	}

	/**
	 * 得到当前的阅读进度百分比
	 * 
	 * @return
	 */
	public float getReadPercent() {
		float fPercent;
		if (mMbBufferLen > 0 && mMbBufferBegin > 0) {
			fPercent = (float) (mMbBufferEnd * 1.0 / mMbBufferLen);
		} else {
			fPercent = 0f;
		}
		return fPercent;
	}

	/**
	 * 获取当前页码
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		if (mBook.isComplete()) {
			return mCurPage;
		} else {
			// 针对在线阅读，每次都计算当前页
			return calculateChapterCurPage(getCurrentChapter());
		}
	}

	/**
	 * 获取该章节的当前页码
	 * 
	 * @return
	 */
	public int getCurrentPage(Chapter chapter) {
		return calculateChapterCurPage(chapter);
	}

	/**
	 * 获取总页码
	 * 
	 * @return
	 */
	public int getTotalPage() {
		if (mCurPage > mTotalPage) {
			mTotalPage = mCurPage;
		}
		return mTotalPage;
	}

	/**
	 * 获取该章节的总页码
	 * 
	 * @return
	 */
	public int getTotalPage(Chapter chapter) {
		int page = chapter.getTotalPage();
		return page;
	}

	/**
	 * 跳转百分比
	 * 
	 * @param percent
	 */
	public PageContent seek(float percent) {
		PageContent pageContent = null;

		int pageSize = mMbBufferEnd - mMbBufferBegin;

		mMbBufferBegin = Float.valueOf(mMbBufferLen * percent).intValue();

		if (mMbBufferBegin == mMbBufferLen) {
			mMbBufferBegin = (int) (mMbBufferLen - pageSize);
			if (mMbBufferBegin < 0) {
				mMbBufferBegin = mMbBufferEnd - pageSize;
			}
		}

		Chapter chapter = mBook.getCurrentChapter(mMbBufferBegin);
		findVaildSeekPos(chapter);
		pageContent = pageDown();
		if (mTotalPage != 0) {
			caculateCurPage();
		}
		return pageContent;
	}

	/**
	 * 跳转到某个位置
	 * 
	 * @param bufferBegin
	 */
	public PageContent seek(int bufferBegin) {
		PageContent pageContent = null;
		mMbBufferBegin = bufferBegin;
		mMbBufferEnd = mMbBufferBegin;
		pageContent = pageDown();

		LogUtil.d("FileMissingLog", "SinaBookModel >> seek >> pageContent size is " + pageContent.getParagraphsSize());
		if (mTotalPage != 0) {
			caculateCurPage();
		}
		return pageContent;
	}

	/**
	 * 更新字体
	 */
	public PageContent updateFontSize() {
		PageContent pageContent = null;

		mPageCache.clear();
		mMbBufferEnd = mMbBufferBegin;
		pageContent = pageDown();
		mTotalPage = 0;
		mCurPage = 0;
		return pageContent;
	}

	/**
	 * 更新字体
	 */
	public PageContent updateFontSize(int bufferBegin) {
		PageContent pageContent = null;

		mPageCache.clear();
		mMbBufferBegin = bufferBegin;
		mMbBufferEnd = mMbBufferBegin;
		pageContent = pageDown();
		mTotalPage = 0;
		mCurPage = 0;
		return pageContent;
	}

	/**
	 * 更新页数信息
	 * 
	 * @param parsePageEndListener
	 */
	public void updateTotalPage(ITaskFinishListener parsePageEndListener, boolean force, boolean isFirstOpen) {
		if (mBook.isComplete()) {
			if (force) {
				mBook.getBookPage().setTotalPage(0);
			}
			if (mBook.getBookPage().getTotalPage() > 0
					&& mBook.getBookPage().getFontSize() == mReadStyleManager.getCurReadFontSizeInSp()) {
				mTotalPage = mBook.getBookPage().getTotalPage();
				if (mBook.getBookPage().getCurPage() > 0 && isFirstOpen) {
					mCurPage = mBook.getBookPage().getCurPage();
				} else {
					caculateCurPage();
				}
			} else {
				release();
				mPageTask = new ParseTotalPageTask();
				mPageTask.setTaskFinishListener(parsePageEndListener);
				mPageTask.execute();
			}
		} else {
			calculateChapterPages(mBook.getCurrentChapter(mMbBufferBegin), parsePageEndListener);
		}
	}

	/**
	 * 获取当前页开始位置
	 * 
	 * @return
	 */
	public int getMbBufferBegin() {
		return mMbBufferBegin;
	}

	/**
	 * 获取当前页结束位置
	 * 
	 * @return
	 */
	public int getMbBufferEnd() {
		return mMbBufferEnd;
	}

	/**
	 * 是否第一页
	 * 
	 * @return
	 */
	public boolean isfirstPage() {
		return mIsfirstPage;
	}

	/**
	 * 是否最后一页
	 * 
	 * @return
	 */
	public boolean islastPage() {
		return mIslastPage;
	}

	protected ParagraphBlockData readParagraphBack(long nFromPos, Chapter chapter) {
		BYTES_READ_LIMIT_BACK = mReadStyleManager.getMaxBytesApage() * 3;
		if (BYTES_READ_LIMIT_BACK < 3000) {
			BYTES_READ_LIMIT_BACK = 3000;
		}
		int nEnd = (int) nFromPos;
		int i;
		byte b0, b1;
		long offset = 0;
		if (chapter != null && chapter.getStartPos() < nEnd && chapter.getLength() > 0) {
			offset = chapter.getStartPos();
		}

		boolean isOnlineBook = mBook.isEncryptedBook();
		boolean isStyledBook = mBook.isStyledBook();
		ParagraphBlockData blockData = new ParagraphBlockData();
		blockData.isParagrahBegin = true;

		mBytesCache.clear();
		if (mContentCharset.equals("GBK")) {
			i = nEnd - 1;
			while (i >= offset) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i) : mMbBuffer.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {
					i++;
					break;
				}
				if (!isStyledBook) {
					if (nEnd - i > BYTES_READ_LIMIT_BACK) {
						// GBK中0和0x81之间肯定是低字节
						if (b0 > 0 && b0 < 0x81) {
							i++;
							blockData.isParagrahBegin = false;
							break;
						}
					}
				}

				mBytesCache.add(b0);
				i--;
			}
		} else if (mContentCharset.equals("UTF-16LE")) {
			i = nEnd - 2;
			while (i >= offset) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i) : mMbBuffer.get(i);
				b1 = isOnlineBook ? (byte) ~mMbBuffer.get(i + 1) : mMbBuffer.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				mBytesCache.add(b1);
				i--;
			}

		} else if (mContentCharset.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i >= offset) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i) : mMbBuffer.get(i);
				b1 = isOnlineBook ? (byte) ~mMbBuffer.get(i + 1) : mMbBuffer.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
					i += 2;
					break;
				}
				mBytesCache.add(b1);
				i--;
			}
		} else {
			i = nEnd - 1;
			while (i >= offset) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i) : mMbBuffer.get(i);
				// if (b0 == 0x0a && i != nEnd - 1) {
				if ((b0 == 0x0a || mBytesCache.size() >= BYTE_CACHE_SIZE_LIMIT) && i != nEnd - 1) {
					i++;
					break;
				}
				mBytesCache.add(b0);
				i--;
			}
		}

		int nParaSize = mBytesCache.size();
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = mBytesCache.get(nParaSize - 1 - i);
		}

		blockData.dataBytes = buf;
		blockData.startPos = 0;
		return blockData;
	}

	protected ParagraphBlockData readParagraphForward(int nFromPos, Chapter chapter) {
		BYTES_READ_LIMIT_FORWARD = mReadStyleManager.getMaxBytesApage();
		int nStart = nFromPos;
		int i = nStart;
		int j = nStart;
		byte b0, b1;

		long nEnd = mMbBufferLen;
		if (chapter != null && chapter.getLength() > 0) {
			// 只读取本章节内的数据
			nEnd = chapter.getStartPos() + chapter.getLength();
		}

		// 是否是加密的内容
		boolean isOnlineBook = mBook.isEncryptedBook();
		boolean isStyledBook = mBook.isStyledBook();
		// 是否是段首的位置
		boolean isParaBegin = false;
		int prePos = mMbBufferEnd - 1;
		if (prePos < 0) {
			prePos = 0;
		}
		if (prePos == 0 || (isOnlineBook ? (byte) ~mMbBuffer.get(prePos) : mMbBuffer.get(prePos)) == 0x0a) {
			isParaBegin = true;
		}

		ParagraphBlockData blockData = new ParagraphBlockData();
		blockData.isParagrahBegin = isParaBegin;

		mBytesCache.clear();
		// 如果非段首，且是带样式的书
		if (!isParaBegin && isStyledBook) {
			long offset = 0;
			if (chapter != null && chapter.getStartPos() < nStart && chapter.getLength() > 0) {
				offset = chapter.getStartPos();
			}

			ArrayList<Byte> tempCache = new ArrayList<Byte>();
			// 找到段首，保证完整段落
			if (mContentCharset.equals("GBK")) {
				j = nStart - 1;
				while (j >= offset) {
					b0 = isOnlineBook ? (byte) ~mMbBuffer.get(j) : mMbBuffer.get(j);
					if (b0 == 0x0a && j != nEnd - 1) {
						j++;
						break;
					}
					tempCache.add(b0);
					j--;
				}
			} else if (mContentCharset.equals("UTF-16LE")) {
				j = nStart - 2;
				while (j >= offset) {
					b0 = isOnlineBook ? (byte) ~mMbBuffer.get(j) : mMbBuffer.get(j);
					b1 = isOnlineBook ? (byte) ~mMbBuffer.get(j + 1) : mMbBuffer.get(j + 1);
					if (b0 == 0x0a && b1 == 0x00 && j != nEnd - 2) {
						j += 2;
						break;
					}
					tempCache.add(b1);
					j--;
				}

			} else if (mContentCharset.equals("UTF-16BE")) {
				j = nStart - 2;
				while (j >= offset) {
					b0 = isOnlineBook ? (byte) ~mMbBuffer.get(j) : mMbBuffer.get(j);
					b1 = isOnlineBook ? (byte) ~mMbBuffer.get(j + 1) : mMbBuffer.get(j + 1);
					if (b0 == 0x00 && b1 == 0x0a && j != nEnd - 2) {
						j += 2;
						break;
					}
					tempCache.add(b1);
					j--;
				}
			} else {
				j = nStart - 1;
				while (j >= offset) {
					b0 = isOnlineBook ? (byte) ~mMbBuffer.get(j) : mMbBuffer.get(j);
					if (b0 == 0x0a && j != nEnd - 1) {
						j++;
						break;
					}
					tempCache.add(b0);
					j--;
				}
			}

			for (j = tempCache.size() - 1; j >= 0; j--) {
				mBytesCache.add(tempCache.get(j));
			}
			blockData.startPos = tempCache.size();
		} else {
			blockData.startPos = 0;
		}

		// 0x0a 换行符\n
		if (mContentCharset.equals("GBK")) {
			while (i < nEnd) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i++) : mMbBuffer.get(i++);
				// TODO 解决书籍<<恶魔总裁的陷阱>>章节中的小段落都是以\t\n结尾的情况
				// 正常书籍以\n结尾
				// 0x0a -> \n
				// 0x20 -> \t
				if (b0 == 0x09) {
					b0 = 0x20;
				}
				mBytesCache.add(b0);
				if (b0 == 0x0a) {
					break;
				}
				if (!isStyledBook) {
					if (i - nStart > BYTES_READ_LIMIT_FORWARD) {
						// GBK中0和0x81之间肯定是低字节
						if (b0 > 0 && b0 < 0x81) {
							break;
						}
					}
				}
			}
		} else if (mContentCharset.equals("UTF-16LE")) {
			while (i < nEnd - 1) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i++) : mMbBuffer.get(i++);
				b1 = isOnlineBook ? (byte) ~mMbBuffer.get(i++) : mMbBuffer.get(i++);
				mBytesCache.add(b0);
				mBytesCache.add(b1);
				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (mContentCharset.equals("UTF-16BE")) {
			while (i < nEnd - 1) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i++) : mMbBuffer.get(i++);
				b1 = isOnlineBook ? (byte) ~mMbBuffer.get(i++) : mMbBuffer.get(i++);
				mBytesCache.add(b0);
				mBytesCache.add(b1);
				if (b0 == 0x00 && b1 == 0x0a) {
					break;
				}
			}
		} else {
			while (i < nEnd) {
				b0 = isOnlineBook ? (byte) ~mMbBuffer.get(i++) : mMbBuffer.get(i++);
				mBytesCache.add(b0);
				LogUtil.d("ClipOnlyOneParagrahBook", "i=" + i + ", b0=" + b0 + ", nEnd=" + nEnd
						+ ", mBytesCache.size()=" + mBytesCache.size());
				if (b0 == 0x0a || mBytesCache.size() >= BYTE_CACHE_SIZE_LIMIT) {//
					LogUtil.e("ClipOnlyOneParagrahBook", "break");
					break;
				}
			}
		}

		// 转移数据
		int nParaSize = mBytesCache.size();
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = mBytesCache.get(i);
		}
		blockData.dataBytes = buf;

		return blockData;
	}

	protected PageContent pageDown() {
		// 屏幕区域内可绘制内容区域的高度值
		float visibleHeight = mReadStyleManager.getVisibleHeight();
		PageContent pageContent = new PageContent();
		// 根据要跳转的索引值指向对应的章节
		// 经常在这里出现问题：章节列表的startPos和length由于各种原因导致其数据不正确，而无法获取到指定的章节
		// 具体的比对规则看mBook.getCurrentChapter方法
		Chapter chapter = mBook.getCurrentChapter(mMbBufferBegin);
		long nextChapStartPos = mMbBufferLen;
		LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 1 >> chapter=" + chapter);
		if (null != chapter && chapter.getLength() != 0) {
			// 下一章节的起始索引
			nextChapStartPos = chapter.getStartPos() + chapter.getLength();
			LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 1 >> nextChapStartPos=" + nextChapStartPos);
		}

		ParagraphCreateBean createBean = new ParagraphCreateBean();
		createBean.availableHeight = visibleHeight;
		createBean.towardsFoward = true;
		if (mBook.isStyledBook()) {
			createBean.imageFolder = mBook.getDownloadInfo().getImageFolder();
		}

		LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 2 >> createBean.availableHeight="
				+ createBean.availableHeight);
		LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 2 >> mMbBufferBegin=" + mMbBufferBegin);
		LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 2 >> mMbBufferEnd=" + mMbBufferEnd);
		LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 2 >> mMbBufferLen=" + mMbBufferLen);
		LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 2 >> nextChapStartPos=" + nextChapStartPos);

		while (createBean.availableHeight > 0 && mMbBufferEnd < mMbBufferLen && mMbBufferEnd < nextChapStartPos) {

			// 1 读取一段的bytes
			ParagraphBlockData blockData = readParagraphForward(mMbBufferEnd, chapter);

			// 2 构造参数
			createBean.isParagrahBegin = blockData.isParagrahBegin;
			createBean.byteUsed = 0;

			// 3 产生一个段落
			Paragraph p = Paragraph.create(blockData, mContentCharset, createBean);
			LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> p begin is " + p.mParaBegin + " and end is "
					+ p.mParaEnd + " [createBean.byteUsed=" + createBean.byteUsed + "]");
			if (createBean.byteUsed > 0) {
				pageContent.addParagraph(p);
				LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> add");
			} else {
				LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> no add");
			}

			// 4 添加段落，更新createBean及mMbBufferEnd
			p.setParaBegin(mMbBufferEnd);
			mMbBufferEnd = mMbBufferEnd + createBean.byteUsed;
			p.setParaEnd(mMbBufferEnd);
			LogUtil.e("ClipOnlyOneParagrahBook", "p >> mParaBegin=" + p.mParaBegin + ", mParaEnd=" + p.mParaEnd);
			// LogUtil.d("cx", "a mMbBufferEnd:" + mMbBufferEnd + " byteUsed:"
			// + createBean.byteUsed);
			// LogUtil.d("cx", "b availableHeight:" +
			// createBean.availableHeight);
		}
		LogUtil.d("FileMissingLog", "SinaBookModel >> pageDown >> 2");

		if (mMbBufferEnd == nextChapStartPos) {
			Chapter nextChapter = mBook.getNextChapter(chapter);

			if (nextChapter != null && nextChapter.getLength() > 0) {
				calculateChapterPages(nextChapter, null);
			}
		}

		pageContent.setPageBegin(mMbBufferBegin);
		pageContent.setPageEnd(mMbBufferEnd);
		pageContent.setCharset(mContentCharset);
		LogUtil.e("ScreenInfo", "return << pageContent >> getPageBegin=" + pageContent.getPageBegin() + ", getPageEnd="
				+ pageContent.getPageEnd() + ", page size is "
				+ (pageContent.getPageEnd() - pageContent.getPageBegin()));
		return pageContent;
	}

	protected void pageUp(Chapter chapter) {
		if (mMbBufferBegin < 0) {
			mMbBufferBegin = 0;
		}

		float visibleHeight = mReadStyleManager.getVisibleHeight();
		PageContent pageContent = new PageContent();

		ParagraphCreateBean createBean = new ParagraphCreateBean();
		createBean.availableHeight = visibleHeight;
		createBean.towardsFoward = false;

		long begin = 0;
		if (chapter != null) {
			begin = chapter.getStartPos();
		}

		while (createBean.availableHeight > 0 && mMbBufferBegin > begin && mMbBufferBegin < mMbBufferLen) {

			// 1 读取一段的bytes
			ParagraphBlockData blockData = readParagraphBack(mMbBufferBegin, chapter);

			// 2 构造参数
			createBean.isParagrahBegin = blockData.isParagrahBegin;
			createBean.byteUsed = 0;

			// 3 产生一个段落
			Paragraph p = Paragraph.create(blockData, mContentCharset, createBean);
			if (createBean.byteUsed > 0) {
				pageContent.addParagraph(p);
			}

			// 4 添加段落，更新createBean及mMbBufferEnd
			mMbBufferBegin = mMbBufferBegin - createBean.byteUsed;
		}

		mMbBufferEnd = mMbBufferBegin;
		if (null != chapter && mMbBufferBegin == chapter.getStartPos()) {
			Chapter preChapter = mBook.getPreChapter(chapter);
			if (preChapter != null && preChapter.getLength() > 0) {
				calculateChapterPages(preChapter, null);
			}
		}
		return;
	}

	protected void findVaildSeekPos(Chapter chapter) {
		if (mMbBufferBegin < 0) {
			mMbBufferBegin = 0;
		}

		long begin = 0;
		if (chapter != null) {
			begin = chapter.getStartPos();
		}

		if (mMbBufferBegin > begin && mMbBufferBegin < mMbBufferLen) {

			// 1 读取一段的bytes
			ParagraphBlockData blockData = readParagraphBack(mMbBufferBegin, chapter);

			// 2 切换位置
			mMbBufferBegin = mMbBufferBegin - blockData.dataBytes.length;
		}

		mMbBufferEnd = mMbBufferBegin;
		if (null != chapter && mMbBufferBegin == chapter.getStartPos()) {
			Chapter preChapter = mBook.getPreChapter(chapter);
			if (preChapter != null && preChapter.getLength() > 0) {
				calculateChapterPages(preChapter, null);
			}
		}
		return;
	}

	/**
	 * 《本地阅读》修正当前页
	 */
	private int correctCurPage(int curPage) {
		if (curPage < 1) {
			curPage = 1;
		} else if (curPage > mTotalPage) {

			if (mBook.isComplete()) {
				curPage = mTotalPage;
			} else {
				curPage = 1;
			}
		}
		return curPage;
	}

	/**
	 * 《本地阅读》计算当前页
	 */
	private void caculateCurPage() {
		if (mMbBufferBegin == 0) {
			mCurPage = 1;
			return;
		}

		float contentPercent = (float) (mMbBufferEnd * 1.0 / mMbBufferLen);
		if (contentPercent >= 0) {
			mCurPage = correctCurPage(Float.valueOf(Math.round(mTotalPage * contentPercent)).intValue());
		}
	}

	/**
	 * 《在线阅读》计算当前位置在章节中的页码 run in ui thread
	 * 
	 * @param chapter
	 */
	private int calculateChapterCurPage(Chapter chapter) {
		int curPage = 1;
		if (null == chapter || chapter.getLength() <= 0) {
			return curPage;
		}

		long bufferLen = chapter.getLength();
		long startPos = chapter.getStartPos();

		float contentPercent = (float) ((mMbBufferEnd - startPos) * 1.0 / bufferLen);

		if (chapter.getTotalPage() > 0) {
			if (contentPercent > 0) {
				curPage = Float.valueOf(chapter.getTotalPage() * contentPercent).intValue();
			} else if (contentPercent == 0) {
				curPage = chapter.getTotalPage();
			}
		}
		if (curPage < 1) {
			curPage = 1;
		}
		if (curPage > chapter.getTotalPage()) {
			chapter.setTotalPage(curPage);
		}

		return curPage;
	}

	/**
	 * 《在线阅读》同步执行计算指定章节页数,UI线程执行（慎用）
	 * 
	 * @param chapter
	 */
	public void calculateChapterPages(final Chapter chapter) {
		if (null == chapter || chapter.getLength() <= 0) {
			return;
		}

		int totalLines = exactCalculateTotalLines(chapter, true);
		int totalPage = 1;
		if (totalLines > 0) {
			int lineCount = mReadStyleManager.getLineCount();
			if (totalLines % lineCount > 0) {
				totalPage = totalLines / lineCount + 1;
			} else {
				totalPage = totalLines / lineCount;
			}
		}
		chapter.setTotalPage(totalPage);
	}

	/**
	 * 《在线阅读》计算指定章节页数
	 * 
	 * @param chapter
	 */
	public void calculateChapterPages(final Chapter chapter, ITaskFinishListener listener) {
		if (null == chapter || chapter.getLength() <= 0) {
			return;
		}

		release();

		mPageTask = new GenericTask() {

			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				int totalLines = exactCalculateTotalLines(chapter, true);
				int totalPage = 1;
				if (totalLines > 0) {
					int lineCount = mReadStyleManager.getLineCount();
					if (totalLines % lineCount > 0) {
						totalPage = totalLines / lineCount + 1;
					} else {
						totalPage = totalLines / lineCount;
					}
				}

				chapter.setTotalPage(totalPage);
				return null;
			}
		};

		mPageTask.setTaskFinishListener(listener);
		mPageTask.execute();
	}

	/**
	 * 精确计算行数
	 * 
	 * @param chapter
	 *            指定章节
	 * @param isAllChapter
	 *            是否为整个章节，如果为false默认使用当前位<br>
	 *            置在当前章节的相对位置来取数据
	 * @return 总行数
	 */
	private int exactCalculateTotalLines(Chapter chapter, boolean isAllChapter) {
		long bufferLen = 0;
		long startPos = 0;

		if (null != chapter && chapter.getLength() > 0) {
			startPos = chapter.getStartPos();

			if (isAllChapter) {
				bufferLen = chapter.getLength();
			} else {
				bufferLen = mMbBufferEnd - startPos;
			}
		} else {
			return 0;
		}

		RandomAccessFile raf = null;
		InputStreamReader reader = null;
		FileInputStream fileInputStream = null;
		ByteArrayInputStream byteInputStream = null;

		try {
			raf = new RandomAccessFile(mBookFile, "r");
			raf.skipBytes((int) startPos);
			byte[] bs = new byte[(int) bufferLen];
			raf.read(bs);

			// 对加密的数据进行解密
			if (mBook.isEncryptedBook()) {
				for (int i = 0; i < bufferLen; i++) {
					bs[i] = (byte) ~bs[i];
				}
			}

			byteInputStream = new ByteArrayInputStream(bs);
			reader = new InputStreamReader(byteInputStream, mContentCharset);

		} catch (FileNotFoundException e) {
			LogUtil.e(TAG, e.getMessage());
		} catch (IOException e) {
			LogUtil.e(TAG, e.getMessage());
		} finally {

			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}

				if (raf != null) {
					raf.close();
				}

				if (byteInputStream != null) {
					byteInputStream.close();
				}
			} catch (IOException e) {
				LogUtil.w(TAG, e.getMessage());
			}
		}

		return calculateTotalLines(reader);
	}

	/**
	 * 精确计算指定输入流的行数，仅适用于小量数据，<br>
	 * 如果数据太大容易造成UI线程阻塞，慎用
	 * 
	 * @param inputStreamReader
	 *            输入流
	 * @return 行数
	 */
	private int calculateTotalLines(InputStreamReader inputStreamReader) {
		if (null == inputStreamReader) {
			return -1;
		}
		Paint readPaint = mReadStyleManager.getReadPaint();
		int totalLine = 0;
		int minLineLength = readPaint.breakText("测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试", true,
				mReadStyleManager.getVisibleWidth(), null) - 3;
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(inputStreamReader);
			String line = reader.readLine();
			if (null != line) {
				line = line.replaceFirst(Paragraph.PARA_BEGIN_PATTERN, "");
				line = Paragraph.PARA_BEGIN_SPACE + line;
			}

			while (line != null) {
				int lineLength = line.length();
				if (lineLength < minLineLength) {
					totalLine++;
				} else {
					int size = 0;
					int remainSize = 0;
					char[] linechars = line.toCharArray();
					while (size < lineLength) {
						remainSize = lineLength - size;
						// 剩下的字符最多一行，剪枝
						if (remainSize < minLineLength) {
							totalLine++;
							break;
						}
						int alinesize = readPaint.breakText(linechars, size, remainSize,
								mReadStyleManager.getVisibleWidth(), mMeasuredWidth);
						size = size + alinesize;
						totalLine++;
					}
				}

				line = reader.readLine();
				if (null != line) {
					line = line.replaceFirst(Paragraph.PARA_BEGIN_PATTERN, "");
					line = Paragraph.PARA_BEGIN_SPACE + line;
				}
			}

		} catch (FileNotFoundException e) {
			LogUtil.w(TAG, "exactCaculatePage FileNotFound error:", e);
		} catch (IOException e) {
			LogUtil.w(TAG, "exactCaculatePage IO error:", e);
		} finally {

			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					// 不处理
				}
			}
		}

		return totalLine;
	}

	private class ParseTotalPageTask extends GenericTask {
		private Paint _paint;

		@Override
		protected void onPreExecute() {
			_paint = mReadStyleManager.getReadPaint();
			super.onPreExecute();
		}

		@Override
		protected TaskResult doInBackground(TaskParams... params) {
			return generalCaculateTotalPage();
		}

		/**
		 * 模糊计算总页数
		 */
		private TaskResult generalCaculateTotalPage() {
			// LogUtil.d(TAG, "generalCaculate total page");
			if (mMbBufferLen == 0) {
				mTotalPage = 1;
				mCurPage = 1;
				return null;
			}

			InputStream inputstream = null;
			FileInputStream fileInputStream = null;

			byte[] readBuffer = null;

			if (mMbBufferLen < BLOCK_SIZE) {
				readBuffer = new byte[(int) mMbBufferLen];
			} else {
				readBuffer = new byte[BLOCK_SIZE];
			}
			int minLineLength = _paint.breakText("测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试", true,
					mReadStyleManager.getVisibleWidth(), null) - 3;
			int totalLine = 0;

			try {
				fileInputStream = new FileInputStream(mBookFile);
				inputstream = new BufferedInputStream(fileInputStream, 8192);
				inputstream.read(readBuffer, 0, readBuffer.length);

				int oneBlockLines = readOneBlock(readBuffer, 0, readBuffer.length, minLineLength);
				if (oneBlockLines == -1) {
					return null;
				} else {
					totalLine = oneBlockLines;
				}

				if (mMbBufferLen > BLOCK_SIZE) {
					totalLine = (int) (totalLine * (mMbBufferLen / (double) BLOCK_SIZE));
				}
			} catch (FileNotFoundException e) {
				LogUtil.w(TAG, "genCaculatePage FileNotFound error:", e);
			} catch (IOException e) {
				LogUtil.w(TAG, "genCaculatePage IO error:", e);
			} finally {

				try {
					if (inputstream != null) {
						inputstream.close();
					}

					if (fileInputStream != null) {
						fileInputStream.close();
					}
				} catch (IOException e) {
					LogUtil.w(TAG, "genCaculatePage IO error:", e);
				}
			}
			int lineCount = mReadStyleManager.getLineCount();
			if (totalLine % lineCount > 0) {
				mTotalPage = totalLine / lineCount + 1;
			} else {
				mTotalPage = totalLine / lineCount;
			}

			mBook.getBookPage().setTotalPage(mTotalPage);
			mBook.getBookPage().setFontSize(mReadStyleManager.getCurReadFontSizeInSp());

			caculateCurPage();
			// LogUtil.d(TAG, "GeneralCaculate total page end: " + mTotalPage);
			return null;
		}

		/*
		 * 读取一个block的数据
		 * 
		 * @return
		 */
		private int readOneBlock(byte[] readBuffer, int offset, int length, int minLineLength) {
			int totalLine = 0;
			BufferedReader byteArrayReader = null;
			try {
				byteArrayReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(readBuffer, offset,
						length), mContentCharset), 8192);
				String line = byteArrayReader.readLine();
				while (line != null) {
					int lineLength = line.length();
					if (lineLength < minLineLength) {
						totalLine++;
					} else {
						int size = 0;
						int remainSize = 0;
						char[] linechars = line.toCharArray();

						int totalSize = linechars.length;
						// 如果每个自然行的字符数目大于500，则进行分段计算
						if (totalSize > 500) {
							int count = 0;
							int _start = 0;
							int _end = 500;
							do {
								char[] chars = Arrays.copyOfRange(linechars, _start, _end);
								size = 0;
								lineLength = chars.length;
								int backCharSize = 0;
								while (size < lineLength) {
									remainSize = lineLength - size;
									// 剩下的字符最多一行，裁剪
									if (remainSize < minLineLength) {
										if (_end == totalSize) {
											totalLine++;
										} else {
											backCharSize = remainSize;
										}
										break;
									}
									int alinesize = _paint.breakText(chars, size, remainSize,
											mReadStyleManager.getVisibleWidth(), mMeasuredWidth);
									size = size + alinesize;
									totalLine++;
								}

								count += lineLength;
								_start += lineLength;
								_start -= backCharSize;
								_end = _start + 500;
								if (_end > totalSize) {
									_end = totalSize;
								}
							} while (count < totalSize);
						} else {
							while (size < lineLength) {
								remainSize = lineLength - size;
								// 剩下的字符最多一行，裁剪
								if (remainSize < minLineLength) {
									totalLine++;
									break;
								}
								int alinesize = _paint.breakText(linechars, size, remainSize,
										mReadStyleManager.getVisibleWidth(), mMeasuredWidth);
								size = size + alinesize;
								totalLine++;
							}
						}

					}
					if (isCancelled()) {
						return -1;
					}
					line = byteArrayReader.readLine();
				}
			} catch (UnsupportedEncodingException e) {
				LogUtil.w(TAG, "genCaculatePage Encoding error:", e);
			} catch (IOException e) {
				LogUtil.w(TAG, "genCaculatePage readOneBlock IO error:", e);
			} finally {
				try {
					if (byteArrayReader != null) {
						byteArrayReader.close();
					}
				} catch (IOException e) {
					LogUtil.w(TAG, "genCaculatePage readOneBlock IO error:", e);
				}
			}
			return totalLine;
		}
	}

	/**
	 * 获取当前编码格式
	 * 
	 * @return
	 */
	public String getContentCharset() {
		return mContentCharset;
	}

	private class Page {
		int begin;
		int end;

		public Page(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}
	}

	private class RefreshMark {
		int chapterIndex;
		int beginOffset;
		int endOffset;
	}
}
