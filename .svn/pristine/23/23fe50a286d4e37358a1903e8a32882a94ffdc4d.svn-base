package com.sina.book.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import com.sina.book.control.download.DownFileTask;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.db.DBService;
import com.sina.book.exception.UnparseEpubFileException;

/**
 * 处理下载下来的微盘文件的改名和读取章节，解析epub的功能
 * 
 * @author like
 * 
 */
public class VDiskFileHandler {
	private static final String TAG = "VDiskFileHandler";

	public static boolean convertTmp2Dat(Book book, DownFileTask task) {
		// LogUtil.d(TAG, "convertTmp2Dat:" +
		// book.getDownloadInfo().getFilePath());
		boolean flag = false;
		File file = new File(book.getDownloadInfo().getFilePath());
		// LogUtil.d("FileMissingLog",
		// "VDiskFileHandler >> convertTmp2Dat >> file path="
		// + book.getDownloadInfo().getFilePath());
		if (file.exists() && file.isFile()) {
			// LogUtil.d("FileMissingLog",
			// "VDiskFileHandler >> convertTmp2Dat >> file exists and is file");
			if (book.getDownloadInfo().getVDiskDownUrl().toLowerCase(Locale.CHINA).endsWith(".txt")) {
				// LogUtil.d("FileMissingLog",
				// "VDiskFileHandler >> convertTmp2Dat >> file exists and is file >> txt");
				book.setNum(1);
				Chapter chapter = new Chapter();
				chapter.setChapterId(1);
				chapter.setStartPos(0);
				chapter.setLength(file.length());
				chapter.setTitle(book.getTitle());
				ArrayList<Chapter> chapters = new ArrayList<Chapter>();
				chapters.add(chapter);
				book.setChapters(chapters);
				DBService.saveBook(book);
				DBService.updateAllChapter(book, book.getChapters());
				file.renameTo(new File(book.changeFileSuffix()));
				flag = true;
			} else if (book.getDownloadInfo().getVDiskDownUrl().toLowerCase(Locale.CHINA).endsWith(".epub")
					|| book.isEpub()) {
				// LogUtil.d("FileMissingLog",
				// "VDiskFileHandler >> convertTmp2Dat >> file exists and is file >> epub");
				try {
					flag = EpubFileHandler.convertEpub2Dat(book, task);
					// LogUtil.d("FileMissingLog",
					// "VDiskFileHandler >> convertTmp2Dat >> file exists and is file >> epub >> flag = "
					// + flag);
				} catch (UnparseEpubFileException e) {
					e.printStackTrace();
					// LogUtil.d("FileMissingLog",
					// "VDiskFileHandler >> convertTmp2Dat >> file exists and is file >> epub >> UnparseEpubFileException e = "
					// + e);
				}
				// 仅当转换成功再做后续操作
				if (flag) {
					book.setNum(book.getChapters().size());
					DBService.saveBook(book);
					DBService.updateAllChapter(book, book.getChapters());
					if (book.isEpub()) {
						// 更改文件名为.epub
						file.renameTo(new File(book.changeFileSuffix()));
					} else {
						// 由于epub文件已经转换了，所以不用改名，直接删除原tmp文件即可
						file.delete();
					}
				}
			}
		} else {
			// LogUtil.d("FileMissingLog",
			// "VDiskFileHandler >> convertTmp2Dat >> file not exists or is not a file");
		}
		return flag;
	}
}
