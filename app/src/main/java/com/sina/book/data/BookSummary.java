package com.sina.book.data;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONObject;

import com.sina.book.util.Util;

/**
 * 书摘数据结构.
 * 
 * @author MarkMjw
 */
public class BookSummary implements Comparable<BookSummary>, Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private int bookId;

	private String book_Id;

	private long begin;

	private long end;

	private long offset;

	private long length;

	private String content;

	private String percent;

	private String time;

	private String chapterId;

	private String chapterTitle;

	private String date;

	/** JSON形式存取，通过章节id的方式存取书摘信息，用作下载等恢复 */
	private String mSummaryJsonString;

	public BookSummary() {
	}

	public BookSummary(String book_Id, int bookId, long begin, long end, long offset, long length, String content,
			String percent, String time, String chapterId, String chapterTitle, String date) {
		this.bookId = bookId;
		this.book_Id = book_Id;
		this.begin = begin;
		this.end = end;
		this.offset = offset;
		this.length = length;
		this.content = content;
		this.percent = percent;
		this.time = time;
		this.chapterId = chapterId;
		this.chapterTitle = chapterTitle;
		this.date = date;
	}

	public void setSummaryPosInfo(long begin, long end, long offset, long length, int lastChapterPos,
			int lastGlobalChapterId) {
		this.begin = begin;
		this.end = end;
		this.offset = offset;
		this.length = length;
		if (lastGlobalChapterId >= 0) {
			try {

				JSONObject lastSummaryObj = new JSONObject();
				lastSummaryObj.put("lastPosInChapter", begin - lastChapterPos);
				lastSummaryObj.put("lastOffsetInChapter", offset - lastChapterPos);
				lastSummaryObj.put("length", length);
				lastSummaryObj.put("lastGlobalChapterId", lastGlobalChapterId);
				mSummaryJsonString = lastSummaryObj.toString();
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	public boolean parsePosFromJson(Book relateBook) {
		if (!Util.isNullOrEmpty(mSummaryJsonString) && relateBook != null && relateBook.getChapters() != null
				&& relateBook.getChapters().size() > 0) {
			try {
				JSONObject lastSummaryObj = new JSONObject(mSummaryJsonString);
				int lastPosInChapter = lastSummaryObj.optInt("lastPosInChapter");
				int lastOffsetInChapter = lastSummaryObj.optInt("lastOffsetInChapter");
				int lengthIn = lastSummaryObj.optInt("length");
				int lastGlobalChapterId = lastSummaryObj.optInt("lastGlobalChapterId");
				if (lastGlobalChapterId > 0) {
					Chapter relateChapter = relateBook.getCurrentChapterById(lastGlobalChapterId);
					if (relateChapter != null && relateChapter.getLength() > 0) {
						int chapterBegin = Long.valueOf(relateChapter.getStartPos()).intValue();
						begin = lastPosInChapter + chapterBegin;
						offset = lastOffsetInChapter + chapterBegin;
						length = lengthIn;
					}
				} else if (lastGlobalChapterId == 0) {
					begin = lastPosInChapter;
					offset = lastOffsetInChapter;
					length = lengthIn;
				}

				if (relateBook.getDownloadInfo().getFileSize() < begin) {
					begin = 0;
					offset = 0;
					length = 0;
				}
				end = begin;
				return true;
			} catch (Exception e) {
				// do nothing
			}
		}
		return false;
	}

	public String getSummaryJsonString() {
		return mSummaryJsonString;
	}

	public void setSummaryJsonString(String summaryJsonString) {
		this.mSummaryJsonString = summaryJsonString;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public String getBookIdStr() {
		if (book_Id == null) {
			book_Id = "";
		}
		return book_Id;
	}

	public void setBookIdStr(String book_Id) {
		this.book_Id = book_Id;
	}

	public long getBegin() {
		return begin;
	}

	public void setBegin(long begin) {
		this.begin = begin;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPercent() {
		return percent;
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@SuppressWarnings("deprecation")
	public int compareTo(BookSummary another) {
		Date anotherDate = new Date(another.getTime());
		Date curDate = new Date(time);
		return curDate.compareTo(anotherDate);
	}

	public String getChapterId() {
		return chapterId;
	}

	public void setChapterId(String chapterId) {
		this.chapterId = chapterId;
	}

	public String getChapterTitle() {
		return chapterTitle;
	}

	public void setChapterTitle(String chapterTitle) {
		this.chapterTitle = chapterTitle;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public boolean equals1(Object object) {
		if (object != null && object instanceof BookSummary) {
			BookSummary markItem = (BookSummary) object;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bookId;
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + (int) (offset ^ (offset >>> 32));
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookSummary other = (BookSummary) obj;
		if (bookId != other.bookId)
			return false;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BookSummary [id=" + id + ", bookId=" + bookId + ", begin=" + begin + ", end=" + end + ", offset="
				+ offset + ", length=" + length + ", content=" + content + ", percent=" + percent + ", time=" + time
				+ ", chapterId=" + chapterId + ", chapterTitle=" + chapterTitle + ", date=" + date + "]";
	}
}
