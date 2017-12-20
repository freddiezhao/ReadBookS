/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.book;

import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

public interface IBookCollection {
	public enum Status {
		NotStarted(false),
		Started(false),
		Succeeded(true),
		Failed(true);

		public final Boolean IsCompleted;

		Status(boolean completed) {
			IsCompleted = completed;
		}
	}

	/**
	 * 跟书籍操作有关系的监听回调
	 * @author chenjl
	 *
	 */
	public interface Listener {
		void onBookEvent(BookEvent event, Book book);
		void onBuildEvent(Status status);
	}

	public void addListener(Listener listener);
	public void removeListener(Listener listener);

	Status status();

	/**
	 * 全部书籍数量
	 * @return
	 */
	int size();

	/**
	 * 返回书籍列表
	 * @param query
	 * @return
	 */
	List<Book> books(BookQuery query);
	/**
	 * 判断有没有某书籍
	 * @param filter
	 * @return
	 */
	boolean hasBooks(Filter filter);
	/**
	 * 返回书籍的书名列表
	 * @param query
	 * @return
	 */
	List<String> titles(BookQuery query);

	/**
	 * 最近阅读的所有书籍列表
	 * @return
	 */
	List<Book> recentBooks();
	/**
	 * 最近阅读书籍列表index索引处的书籍
	 * @param index
	 * @return
	 */
	Book getRecentBook(int index);
	/**
	 * 将书籍book添加到最近阅读记录
	 * @param book
	 */
	void addBookToRecentList(Book book);

	/**
	 * 通过ZLFile对象找到对应Book对象
	 * @param file
	 * @return
	 */
	Book getBookByFile(ZLFile file);
	/**
	 * 从Books表中通过book_id查找Book对象
	 * @param id
	 * @return
	 */
	Book getBookById(long id);
	/**
	 * 查找所有书籍中匹配uid的书籍对象
	 * @param uid
	 * @return
	 */
	Book getBookByUid(UID uid);
	/**
	 * 通过hash从BookHash查出对应的所有book_id，<br>
	 * 再依次查找该book_id对应的书籍，书籍能找到并且对应的文件存在便立即返回
	 * 
	 * @param hash
	 * @return
	 */
	Book getBookByHash(String hash);

	/**
	 * 返回所有书籍的标签列表
	 * @return
	 */
	List<String> labels();
	/**
	 * 返回所有书籍的作者列表
	 * @return
	 */
	List<Author> authors();
	/**
	 * 暂时不知道这个Series是干啥的?
	 * @return
	 */
	boolean hasSeries();
	List<String> series();
	/**
	 * 跟{@link #labels()}类似，同属于获取书籍标签信息
	 * @return
	 */
	List<Tag> tags();
	/**
	 * 所有书籍标题的首字母列表
	 * @return
	 */
	List<String> firstTitleLetters();

	/**
	 * 保存书籍
	 * @param book
	 * @return
	 */
	boolean saveBook(Book book);
	/**
	 * 移除书籍
	 * @param book 要移除的书籍
	 * @param deleteFromDisk 是否将书籍文件从磁盘删除
	 */
	void removeBook(Book book, boolean deleteFromDisk);

	/**
	 * 获取书籍的Hash信息
	 * @param book
	 * @param force
	 * @return
	 */
	String getHash(Book book, boolean force);

	/**
	 * 从BookState表中获取bookId对应书籍的阅读数据
	 * @param bookId
	 * @return
	 */
	ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId);
	/**
	 * 存储阅读数据到BookState表中
	 * @param bookId
	 * @param position
	 */
	void storePosition(long bookId, ZLTextPosition position);

	/**
	 * 判断该linkId是否在已访问过列表中
	 * @param book
	 * @param linkId
	 * @return
	 */
	boolean isHyperlinkVisited(Book book, String linkId);
	/**
	 * 标记某linkId的链接为已访问过的链接
	 * @param book
	 * @param linkId
	 */
	void markHyperlinkAsVisited(Book book, String linkId);

	/**
	 * 获取书籍的封面信息
	 * @param book
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	ZLImage getCover(Book book, int maxWidth, int maxHeight);

	/**
	 * 获取书籍的书签列表
	 * @param query
	 * @return
	 */
	List<Bookmark> bookmarks(BookmarkQuery query);
	/**
	 * 保存某书签
	 * @param bookmark
	 */
	void saveBookmark(Bookmark bookmark);
	/**
	 * 删除某书签
	 * @param bookmark
	 */
	void deleteBookmark(Bookmark bookmark);

	/**
	 * 获取styleId对应的高亮样式信息(有三个预定义的，也可自定义)
	 * @param styleId
	 * @return
	 */
	HighlightingStyle getHighlightingStyle(int styleId);
	/**
	 * 获取所有的高亮样式信息
	 * @return
	 */
	List<HighlightingStyle> highlightingStyles();
	/**
	 * 保存高亮样式
	 * @param style
	 */
	void saveHighlightingStyle(HighlightingStyle style);

	/**
	 * 重新扫描path路径下的所有书籍
	 * @param path
	 */
	void rescan(String path);
}
