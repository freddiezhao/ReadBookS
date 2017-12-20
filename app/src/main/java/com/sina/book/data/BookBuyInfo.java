package com.sina.book.data;

import java.io.Serializable;

import com.sina.book.SinaBookApplication;
import com.sina.book.util.LoginUtil;

/**
 * 书籍购买状态
 * 
 * @author MarkMjw
 * @date 2013-2-4
 */
public class BookBuyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int BOOK_DEFAUTL_PRICE = -1;

	/** 收费类型. */
	private int mPayType = Book.BOOK_TYPE_FREE;

	/** 是否VIP书籍. */
	private String mIsVip = "N";

	/** 价格. */
	private double mPrice = BOOK_DEFAUTL_PRICE;

	/** 折扣价格. */
	private double mDiscountPrice;

	/** 是否已经购买. */
	private boolean mHasBuy = false;

	/** 购买类型. */
	// // buyType 0，未购买；1，单章,单本购买；2，套餐内购买
	private int mBuyType;

	/** 购买的时间. */
	private String mBuyTime = "";

	/** 登录用户的uid，暂时用在v盘下载中为book进行设置. */
	private String mUid;

	/** 收费和连载状态. */
	private String mStatusInfo = Book.STATUS_FINISH_NEW;

	/** 收费和连载状态标志. */
	private String mStatusFlag = Book.STATUS_FINISH_EN;

	/** 是否自动购买. */
	private boolean mAutoBuy = false;

	/** 折扣信息. */
	private PriceTip mPriceTip;

	public String getUid() {
		return mUid;
	}

	public void setUid(String uid) {
		this.mUid = uid;
	}

	public int getBuyType() {
		return mBuyType;
	}

	public void setBuyType(int buyType) {
		this.mBuyType = buyType;
	}

	public String getStatusInfo() {
		return mStatusInfo;
	}

	public void setStatusInfo(String statusInfo) {
		this.mStatusInfo = statusInfo;
	}

	public int getPayType() {
		return mPayType;
	}

	public void setPayType(int payType) {
		this.mPayType = payType;
	}

	public double getPrice() {
		return mPrice;
	}

	public void setPrice(double price) {
		this.mPrice = price;
	}

	public double getDiscountPrice() {
		return mDiscountPrice;
	}

	public void setDiscountPrice(double discountPrice) {
		this.mDiscountPrice = discountPrice;
	}

	public String isVip() {
		return mIsVip;
	}

	public void setVip(String isVip) {
		this.mIsVip = isVip;
	}

	public String getStatusFlag() {
		return mStatusFlag;
	}

	public void setStatusFlag(String statusFlag) {
		this.mStatusFlag = statusFlag;
	}

	public boolean isHasBuy() {
		return mHasBuy;
	}

	public void setHasBuy(boolean hasBuy) {
		this.mHasBuy = hasBuy;
	}

	public String getBuyTime() {
		return mBuyTime;
	}

	public void setBuyTime(String buyTime) {
		this.mBuyTime = buyTime;
	}

	public boolean isAutoBuy() {
		return mAutoBuy;
	}

	public PriceTip getPriceTip() {
		return mPriceTip;
	}

	public void setPriceTip(PriceTip priceTip) {
		this.mPriceTip = priceTip;
	}

	public boolean canAutoBuy(Chapter chapter) {
		boolean canAutoBuy = false;

		// double minBal = 0d;
		// if (chapter != null && chapter.getPrice() > 0) {
		// minBal = chapter.getPrice();
		// }

		if (LoginUtil.isValidAccessToken(SinaBookApplication.gContext) == LoginUtil.TOKEN_TYPE_LOGIN_SUCCESS) {
			// 1.用户余额
			// String balance = LoginUtil.getLoginInfo().getBalance();
			// if (Util.isDoubleValue(balance)) {
			// if (Double.parseDouble(balance) > minBal) {
			// canAutoBuy = true;
			// }
			// }
			// 2.阅读券余额

			// 3.赠书卡

			// TODO 全部通过，扣费原则由服务器来决定
			canAutoBuy = true;
		}

		return isAutoBuy() && canAutoBuy;
	}

	public void setAutoBuy(boolean autoBuy) {
		this.mAutoBuy = autoBuy;
	}

	@Override
	public String toString() {
		return "BookBuyInfo [mPayType=" + mPayType + ", mIsVip=" + mIsVip + ", mPrice=" + mPrice + ", mDiscountPrice="
				+ mDiscountPrice + ", mHasBuy=" + mHasBuy + ", mBuyType=" + mBuyType + ", mBuyTime=" + mBuyTime
				+ ", mUid=" + mUid + ", mStatusInfo=" + mStatusInfo + ", mStatusFlag=" + mStatusFlag + ", mAutoBuy="
				+ mAutoBuy + ", mPriceTip=" + mPriceTip + "]";
	}
}
