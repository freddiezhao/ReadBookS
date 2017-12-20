package org.geometerplus.fbreader.book;

import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.fbreader.bookmodel.BookReadingException;

public abstract class BookUtil {
	private static final WeakReference<ZLImage> NULL_IMAGE = new WeakReference<ZLImage>(
			null);
	private static final WeakHashMap<Book, WeakReference<ZLImage>> ourCovers = new WeakHashMap<Book, WeakReference<ZLImage>>();

	public static ZLImage getCover(Book book) {
		if (book == null) {
			return null;
		}
		synchronized (book) {
			WeakReference<ZLImage> cover = ourCovers.get(book);
			if (cover == NULL_IMAGE) {
				return null;
			} else if (cover != null) {
				final ZLImage image = cover.get();
				if (image != null) {
					return image;
				}
			}
			ZLImage image = null;
			try {
				image = book.getPlugin().readCover(book.File);
			} catch (BookReadingException e) {
				// ignore
			}
			ourCovers.put(book, image != null ? new WeakReference<ZLImage>(
					image) : NULL_IMAGE);
			return image;
		}
	}

	public static String getAnnotation(Book book) {
		try {
			return book.getPlugin().readAnnotation(book.File);
		} catch (BookReadingException e) {
			return null;
		}
	}

	// 获得帮助文档
	public static ZLResourceFile getHelpFile() {
		final Locale locale = Locale.getDefault();

//		ZLResourceFile file = ZLResourceFile.createResourceFile(
//				"data/help/11.epub"
//				);
//		if (file.exists()) {
//			return file;
//		}

		ZLResourceFile file = ZLResourceFile.createResourceFile(
			"data/help/MiniHelp." + locale.getLanguage() + "_" + locale.getCountry() + ".fb2"
		);
		if (file.exists()) {
			return file;
		}
		
		file = ZLResourceFile.createResourceFile(
			"data/help/MiniHelp." + locale.getLanguage() + ".fb2"
		);
		if (file.exists()) {
			return file;
		}

		return ZLResourceFile.createResourceFile("data/help/MiniHelp.en.fb2");
	}

	public static boolean canRemoveBookFile(Book book) {
		ZLFile file = book.File;
		if (file.getPhysicalFile() == null) {
			return false;
		}
		while (file instanceof ZLArchiveEntryFile) {
			file = file.getParent();
			if (file.children().size() != 1) {
				return false;
			}
		}
		return true;
	}

	public static UID createUid(ZLFile file, String algorithm) {
		InputStream stream = null;

		try {
			final MessageDigest hash = MessageDigest.getInstance(algorithm);
			stream = file.getInputStream();

			final byte[] buffer = new byte[2048];
			while (true) {
				final int nread = stream.read(buffer);
				if (nread == -1) {
					break;
				}
				hash.update(buffer, 0, nread);
			}

			final Formatter f = new Formatter();
			for (byte b : hash.digest()) {
				f.format("%02X", b & 0xFF);
			}
			return new UID(algorithm, f.toString());
		} catch (IOException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
