package com.sina.book.data;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONObject;

import com.sina.book.util.Util;

/**
 * 书签数据结构.
 * 
 * @author MarkMjw
 */
public class MarkItem implements Comparable<MarkItem>, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private int id;

	/** The bookId. */
	private int bookId;

	// 历史遗留问题，bookId被用来存储book表的递增id号,因此新增一个书籍id存储
	private String book_id;

	/** The begin. */
	private long begin;

	/** The end. */
	private long end;

	/** The content. */
	private String content;

	/** The percent. */
	private String percent;

	/** The time. */
	private String time;

	/** The chapter id. */
	private String chapterId;

	/** The chapter title. */
	private String chapterTitle;

	/** The date. */
	private String date;

	/** JSON形式存取，通过章节id的方式存取书签信息，用作下载等恢复 */
	private String mMarkJsonString;

	/**
	 * Instantiates a new mark item.
	 */
	public MarkItem() {
	}

	/**
	 * Instantiates a new mark item.
	 * 
	 * @param bookId
	 *            the book id
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @param content
	 *            the content
	 * @param percent
	 *            the percent
	 * @param time
	 *            the time
	 * @param chapterId
	 *            the chapter id
	 * @param chapterTitle
	 *            the chapter title
	 * @param date
	 *            the date
	 */
	public MarkItem(String book_id, int bookId, long begin, long end, String content, String percent, String time,
			String chapterId, String chapterTitle, String date) {
		this.bookId = bookId;
		this.book_id = book_id;
		this.begin = begin;
		this.end = end;
		this.content = content;
		this.time = time;
		this.percent = percent;
		this.chapterId = chapterId;
		this.chapterTitle = chapterTitle;
		this.date = date;
	}

	public MarkItem(MarkItem mark) {
		this.bookId = mark.bookId;
		this.begin = mark.begin;
		this.end = mark.end;
		this.content = mark.content;
		this.time = mark.time;
		this.percent = mark.percent;
		this.chapterId = mark.chapterId;
		this.chapterTitle = mark.chapterTitle;
		this.date = mark.date;
	}

	public void setMarkPosInfo(long begin, long end, int lastChapterPos, int lastGlobalChapterId) {
		this.begin = begin;
		this.end = end;
		if (lastGlobalChapterId >= 0) {
			try {

				JSONObject lastMarkObj = new JSONObject();
				lastMarkObj.put("lastPosInChapter", begin - lastChapterPos);
				lastMarkObj.put("lastGlobalChapterId", lastGlobalChapterId);
				mMarkJsonString = lastMarkObj.toString();
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public boolean parsePosFromJson(Book relateBook) {
		return parsePosFromJson(relateBook, true);
	}

	public boolean parsePosFromJson(Book relateBook, boolean compareFileSize) {
		if (!Util.isNullOrEmpty(mMarkJsonString) && relateBook != null && relateBook.getChapters() != null
				&& relateBook.getChapters().size() > 0) {
			try {
				JSONObject lastMarkObj = new JSONObject(mMarkJsonString);
				int lastPosInChapter = lastMarkObj.optInt("lastPosInChapter");
				int lastGlobalChapterId = lastMarkObj.optInt("lastGlobalChapterId");
				if (lastGlobalChapterId > 0) {
					Chapter relateChapter = relateBook.getCurrentChapterById(lastGlobalChapterId);
					if (relateChapter != null && relateChapter.getLength() > 0) {
						begin = lastPosInChapter + Long.valueOf(relateChapter.getStartPos()).intValue();
					}
				} else if (lastGlobalChapterId == 0) {
					begin = lastPosInChapter;
				}

				if (compareFileSize && relateBook.getDownloadInfo().getFileSize() < begin) {
					begin = 0;
				}
				end = begin;
				return true;
			} catch (Exception e) {
				// do nothing
			}
		}
		return false;
	}

	public String getMarkJsonString() {
		return mMarkJsonString;
	}

	public void setMarkJsonString(String markJsonString) {
		this.mMarkJsonString = markJsonString;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the book id.
	 * 
	 * @return the book id
	 */
	public int getBookId() {
		return bookId;
	}

	/**
	 * Sets the book id.
	 * 
	 * @param bookId
	 *            the new book id
	 */
	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public String getBookIdStr() {
		return book_id;
	}

	public void setBookIdStr(String book_id) {
		this.book_id = book_id;
	}

	/**
	 * Gets the begin.
	 * 
	 * @return the begin
	 */
	public long getBegin() {
		return begin;
	}

	/**
	 * Sets the begin.
	 * 
	 * @param begin
	 *            the new begin
	 */
	public void setBegin(long begin) {
		this.begin = begin;
	}

	/**
	 * Gets the end.
	 * 
	 * @return the end
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * Sets the end.
	 * 
	 * @param end
	 *            the new end
	 */
	public void setEnd(long end) {
		this.end = end;
	}

	/**
	 * Gets the content.
	 * 
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets the content.
	 * 
	 * @param content
	 *            the new content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Gets the percent.
	 * 
	 * @return the percent
	 */
	public String getPercent() {
		return percent;
	}

	/**
	 * Sets the percent.
	 * 
	 * @param percent
	 *            the new percent
	 */
	public void setPercent(String percent) {
		this.percent = percent;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * Sets the time.
	 * 
	 * @param time
	 *            the new time
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@SuppressWarnings("deprecation")
	public int compareTo(MarkItem another) {
		Date anotherDate = new Date(another.getTime());
		Date curDate = new Date(time);
		return curDate.compareTo(anotherDate);
	}

	/**
	 * Gets the chapter id.
	 * 
	 * @return the chapter id
	 */
	public String getChapterId() {
		return chapterId;
	}

	/**
	 * Sets the chapter id.
	 * 
	 * @param chapterId
	 *            the new chapter id
	 */
	public void setChapterId(String chapterId) {
		this.chapterId = chapterId;
	}

	/**
	 * Gets the chapter title.
	 * 
	 * @return the chapter title
	 */
	public String getChapterTitle() {
		return chapterTitle;
	}

	/**
	 * Sets the chapter title.
	 * 
	 * @param chapterTitle
	 *            the new chapter title
	 */
	public void setChapterTitle(String chapterTitle) {
		this.chapterTitle = chapterTitle;
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the new date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (begin ^ (begin >>> 32));
		result = prime * result + bookId;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof MarkItem) {
			MarkItem markItem = (MarkItem) object;
			if (this == markItem) {
				return true;
			}

			if (markItem.getBegin() == this.getBegin() && markItem.getBookId() == this.getBookId()) {
				return true;
			} else {
				return false;
			}
		}
		return super.equals(object);
	}

	@Override
	public String toString() {
		return "MarkItem [id=" + id + ", bookId=" + bookId + ", begin=" + begin + ", end=" + end + ", content="
				+ content + ", percent=" + percent + ", time=" + time + ", chapterId=" + chapterId + ", chapterTitle="
				+ chapterTitle + ", date=" + date + "]";
	}

}
