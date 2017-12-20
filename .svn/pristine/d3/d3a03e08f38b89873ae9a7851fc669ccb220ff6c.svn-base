package com.sina.book.data;

import java.io.Serializable;

/**
 * 小说章节信息.
 * 
 * @author Tsimle
 */
public class Chapter implements Serializable {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant CHAPTER_PREPARE. */
	public static final int CHAPTER_PREPARE = 1000;

	/** The Constant CHAPTER_RUNNING. */
	public static final int CHAPTER_RUNNING = 1001;

	/** The Constant CHAPTER_SUCCESS. */
	public static final int CHAPTER_SUCCESS = 1002;

	/** The Constant CHAPTER_FAILED. */
	public static final int CHAPTER_FAILED = 1003;

	/** The Constant CHAPTER_NEED_BUY. */
	public static final int CHAPTER_NEED_BUY = 1004;

	/** The Constant CHAPTER_RECHARGE. */
	public static final int CHAPTER_RECHARGE = 1005;

	/** The Constant CHAPTER_DEFAUTL_PRICE. */
	public static final int CHAPTER_DEFAUTL_PRICE = -1;

	/** The Constant NORMAL. */
	public static final int NORMAL = 0;

	/** The Constant NEW. */
	public static final int NEW = 1;

	/** 没有该章节. */
	public static final int CHAPTER_NO = 0;

	/** VIP章节. */
	public static final int CHAPTER_VIP = 1;

	/** 免费章节. */
	public static final int CHAPTER_FREE = 2;

	/**
	 * 章节是否被购买<br>
	 */
	public static final int CHAPTER_FLAG_HASBUY = 0x001;

	/**
	 * 默认的GlobalChapterId.
	 */
	public static final int DEFAULT_GLOBAL_ID = 0;

	/** 数据库中的唯一标示. */
	private int id = -1;

	/** 在服务器数据库中的顺序标志 */
	private int serialNumber;

	/** The tag. */
	private int tag = NORMAL;

	/** The global chapter id. */
	private int globalChapterId = DEFAULT_GLOBAL_ID;

	/** The chapter id. */
	private int chapterId = 0;

	/** The title. */
	private String title;

	/** The start pos. */
	private long startPos;

	/** The length. */
	private long length;

	/** The is vip. */
	private String isVip = "N";

	/** 章节类容的下载状态. */
	private int state = CHAPTER_PREPARE;

	/** 价格. */
	private double price = CHAPTER_DEFAUTL_PRICE;

	/** 折扣价格. */
	private double discountPrice;

	private int chapterFlags = 0x000;

	/** 本章总页数. */
	private int totalPage = 1;

	/**
	 * 是否已经预取过数据
	 */
	private boolean isPreLoad = false;

	/** 章节更新时间. */
	private String updateTime;

	/** 章节折扣信息. */
	private PriceTip priceTip;

	/** 如目录选择章节，阅读页点击上一章下一章，拖动进度条 */
	public static final int DOWN_WAY_NORMAL = 1;
	/** 阅读时的预加载 */
	public static final int DOWN_WAY_PRELOAD = 2;
	/** 标识下载该章节数据的途径/入口 */
	private int downChapterWay = DOWN_WAY_NORMAL;

	public int getDownChapterWay() {
		return downChapterWay;
	}

	public void setDownChapterWay(int downChapterWay) {
		this.downChapterWay = downChapterWay;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getId() {
		return id;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public boolean hasBuy() {
		return (chapterFlags & CHAPTER_FLAG_HASBUY) == CHAPTER_FLAG_HASBUY;
	}

	public void setHasBuy(boolean hasBuy) {
		setAChapterFlag(hasBuy ? CHAPTER_FLAG_HASBUY : 0, CHAPTER_FLAG_HASBUY);
	}

	private void setAChapterFlag(int flags, int mask) {
		chapterFlags = (chapterFlags & ~mask) | (flags & mask);
	}

	public int getChapterFlags() {
		return chapterFlags;
	}

	public void setChapterFlags(int chapterFlags) {
		if (chapterFlags != 0) {
			this.chapterFlags = chapterFlags;
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(double discountPrice) {
		this.discountPrice = discountPrice;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getGlobalId() {
		return globalChapterId;
	}

	public void setGlobalId(int globalId) {
		this.globalChapterId = globalId;
	}

	public String getVip() {
		return isVip;
	}

	public void setVip(String isVip) {
		this.isVip = isVip;
	}

	public boolean isVip() {
		return "Y".equals(isVip) || "y".equals(isVip);
	}

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getStartPos() {
		return startPos;
	}

	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getLastPos() {
		return this.startPos + this.length;
	}

	public boolean isPreLoad() {
		return isPreLoad;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public void setPreLoad(boolean isPreLoad) {
		this.isPreLoad = isPreLoad;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public PriceTip getPriceTip() {
		return priceTip;
	}

	public void setPriceTip(PriceTip priceTip) {
		this.priceTip = priceTip;
	}

	public void clearLocalInfo() {
		startPos = 0;
		length = 0;
		totalPage = 1;
		isPreLoad = false;
		state = CHAPTER_PREPARE;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Chapter chapter = (Chapter) o;

		if (globalChapterId != chapter.globalChapterId)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return globalChapterId;
	}

	@Override
	public String toString() {
		// return "Chapter [id=" + id + ", serialNumber=" + serialNumber
		// + ", tag=" + tag + ", globalChapterId=" + globalChapterId
		// + ", chapterId=" + chapterId + ", title=" + title
		// + ", startPos=" + startPos + ", length=" + length + ", isVip="
		// + isVip + ", state=" + state + ", price=" + price
		// + ", discountPrice=" + discountPrice + ", chapterFlags="
		// + chapterFlags + ", totalPage=" + totalPage + ", isPreLoad="
		// + isPreLoad + ", updateTime=" + updateTime + ", priceTip="
		// + priceTip + "]";
		return "Chapter [id=" + id + ", globalChapterId=" + globalChapterId + ", chapterId=" + chapterId + ", title="
				+ title + ", startPos=" + startPos + ", length=" + length + ", isVip=" + isVip + ", state=" + state
				+ "]";
	}
}
