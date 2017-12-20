package com.sina.book.util;

import java.io.File;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.ZLog;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.app.Activity;
import android.content.DialogInterface;

import com.sina.book.R;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookDownloadInfo;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.ui.view.CommonDialog;

/**
 * 处理epub的帮助类
 * 
 * @author chenjl
 * 
 */
public class EpubHelper {

	private EpubHelper() {
	}

	public static EpubHelper i;

	static {
		i = new EpubHelper();
	}

	public interface OnEpubFileCheckResultListener {
		public abstract void removeBook();

		public abstract void redownloadBook();
	}

	public static class DefaultListener implements
			OnEpubFileCheckResultListener {

		@Override
		public void removeBook() {

		}

		@Override
		public void redownloadBook() {

		}

	}

	public boolean checkFileExistsBeforeRead(final Activity activity,
			final Book book, final OnEpubFileCheckResultListener listener) {
		return checkFileExistsBeforeRead(activity, book, listener, true);
	}

	/**
	 * 阅读前检查epub书籍对应的文件是否存在
	 * 
	 * @param activity
	 * @param book
	 * @param listener
	 * @param removeable
	 *            书籍详情页调用时传入false，不显示“移除书架”，显示取消
	 * @return
	 */
	public boolean checkFileExistsBeforeRead(final Activity activity,
			final Book book, final OnEpubFileCheckResultListener listener,
			final boolean removeable) {
		BookDownloadInfo downInfo = book.getDownloadInfo();
		if (downInfo != null) {
			String path = book.getDownloadInfo().getOriginalFilePath();
			if (path.endsWith(".epub") || book.isEpub()) {
				File file = FileUtils.checkOrCreateFile(path, false);
				if (file == null || !file.exists()) {
					int leftBtnResId = R.string.cancel;
					if (removeable) {
						leftBtnResId = R.string.remove_book;
					}
					// 判断是线上的epub还是本地导入的或者是微盘下载的
					int rightBtnResId = R.string.cancel;
					final boolean isOurServerEpubBook = book
							.isOurServerEpubBook();
					if (isOurServerEpubBook) {
						rightBtnResId = R.string.redownload_book;
					}
					// 文件丢失
					CommonDialog.show(activity, R.string.file_not_exists,
							leftBtnResId, rightBtnResId,
							new CommonDialog.DefaultListener() {
								@Override
								public void onLeftClick(DialogInterface dialog) {
									if (removeable) {
										// 移除书籍
										removeBook(activity, book);
										if (listener != null) {
											listener.removeBook();
										}
									}
								}

								@Override
								public void onRightClick(DialogInterface dialog) {
									if (isOurServerEpubBook) {
										// 重新下载
										redownloadBook(activity, book);
										if (listener != null) {
											listener.redownloadBook();
										}
									}
								}
							});
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 移除书籍
	 * 
	 * @param activity
	 * @param book
	 */
	public void removeBook(Activity activity, final Book book) {
		// 从书架上移除
		DownBookJob job = DownBookManager.getInstance().getJob(book);
		if (job != null) {
			DownBookManager.getInstance().removeJob(job);
		}

		// SD卡上的书籍不能被删除
		if (book.getDownloadInfo().getImageUrl() != null
				&& !book.getDownloadInfo().getImageUrl()
						.startsWith(Book.SDCARD_PATH_IMG)) {
			StorageUtil.deleteFolder(book.getDownloadInfo().getFilePath());
			StorageUtil.deleteFolder(book.getDownloadInfo().getImageFolder());
		}

		// 删除Epub书籍
		// TODO:ouyang 删除epub书籍的阅读记录
		ZLog.d(ZLog.DeleteBook, "删除书籍的信息 >> " + book);
		final BookCollectionShadow collection = new BookCollectionShadow();
		collection.bindToService(activity, new Runnable() {
			@Override
			public void run() {
				// 干事吧
				String path = book.getDownloadInfo().getOriginalFilePath();
				collection.removeBook(
						collection.getBookByFile(ZLFile.createFileByPath(path)),
						false);
				collection.unbind();
			}
		});
		CloudSyncUtil.getInstance().delCloud(activity, book);
	}

	/**
	 * 重新下载书籍
	 * 
	 * @param activity
	 * @param book
	 */
	public void redownloadBook(Activity activity, final Book book) {
		DownBookJob job = DownBookManager.getInstance().getJob(book);
		if (job != null) {
			job.reset();
		}
		DownBookManager.getInstance().downBook(book);
		CloudSyncUtil.getInstance().add2Cloud(activity, book);
	}

	public int isCacheBookIsEpub(Book cache) {
		if (isEndWithSuffix(cache, Book.EPUB_CACHE_SUFFIX)) {
			return 0;
		} else if (isEndWithSuffix(cache, Book.EPUB_TMP_SUFFIX)) {
			return 1;
		}
		return -1;
	}

	private boolean isEndWithSuffix(Book book, String suffix) {
		if (book != null && book.getDownloadInfo() != null) {
			String filePath = book.getDownloadInfo().getFilePathOnly();
			if (filePath.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	public boolean beforCacheEpubBook2BookCache(Book cache) {
		boolean result = false;
		if (cache != null && cache.isEpub()) {
			String renameFileSuffix = null;
			if (isEndWithSuffix(cache, Book.EPUB_SUFFIX)) {
				// 下载完成，文件更名为ecache
				renameFileSuffix = Book.EPUB_CACHE_SUFFIX;
			} else {
				// 下载未完成，文件更名为etmp
				renameFileSuffix = Book.EPUB_TMP_SUFFIX;
			}
			File file = new File(cache.getDownloadInfo().getFilePath());
			// 修复：BugID=22787
			// 如果用户在注销之前把文件给删除了，下面的条件无法成立
			// 但是Book依然被缓存到BookCache表中，文件后缀为.epub
			// 这样将导致isCacheBookIsEpub方法无法正确判定
			// 因此把程序内部更名的操作放在条件前面执行，把renameTo放在条件内执行
			String newFileName = cache.changeFileSuffix(renameFileSuffix);
			cache.changeOriginalFile2FilePath();
			if (file.exists() && file.isFile() && file.length() > 0) {
				try {
					// 更改文件名为.ecache
					file.renameTo(new File(newFileName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result = true;
		}
		return result;
	}

	public boolean afterLoginHandleEpubBook(Book cache) {
		boolean result = false;
		String renameFileSuffix = null;
		int resultInt = isCacheBookIsEpub(cache);
		if (resultInt != -1) {
			if (resultInt == 0) {
				renameFileSuffix = Book.EPUB_SUFFIX;
			} else {
				renameFileSuffix = Book.TMP_SUFFIX;
			}
			File file = new File(cache.getDownloadInfo().getFilePath());
			String newFileName = cache.changeFileSuffix(renameFileSuffix);
			cache.changeOriginalFile2FilePath();
			if (file.exists() && file.isFile() && file.length() > 0) {
				try {
					// 更改文件名为.epub
					file.renameTo(new File(newFileName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 修复：BugID=22787
			// 即使文件丢失了，也要设置成是epub书籍
			// 用户点击时，会弹出对话框提示重新下载或者移除书架
			cache.setIsEpub(true);
			result = true;
		}
		return result;
	}

}
