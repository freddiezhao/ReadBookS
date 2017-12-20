package com.sina.book.data;

/*
 * 消费记录->书籍内消费记录单元数据
 */
public class BuyItem
{

	private String	bookId;

	private String	chapterId;

	// 按本书籍则为书名 ，按章书籍为章节名
	private String	title;

	private String	time;

	private String	price;			// 消费价格

	private String	unit;			// 消费单位(如：阅读券)

	private int		payType;		// 书籍资费类型(2:按本 3：按章)

	private boolean	isOutOfService; // 此书已下架/不存在

	public boolean isOutOfService()
	{
		return isOutOfService;
	}

	public void setOutOfService(boolean isOutOfService)
	{
		this.isOutOfService = isOutOfService;
	}

	public BuyItem(String title, String bookId, String chapterId, String time, String price, int payType)
	{
		super();
		this.title = title;
		this.bookId = bookId;
		this.chapterId = chapterId;
		this.time = time;
		this.price = price;
		this.payType = payType;
	}

	public BuyItem()
	{
		super();
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getBookId()
	{
		return bookId;
	}

	public void setBookId(String bookId)
	{
		this.bookId = bookId;
	}

	public String getChapterId()
	{
		return chapterId;
	}

	public void setChapterId(String chapterId)
	{
		this.chapterId = chapterId;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public String getPrice()
	{
		return price;
	}

	public void setPrice(String price)
	{
		this.price = price;
	}

	public String getUnit()
	{
		return unit;
	}

	public void setUnit(String unit)
	{
		this.unit = unit;
	}

	public int getPayType()
	{
		return payType;
	}

	public void setPayType(int payType)
	{
		this.payType = payType;
	}
}
