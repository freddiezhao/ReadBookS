package com.sina.book.data;

/**
 * 礼包信息
 */
public class GiftInfo {
	private String aid;
	private String giftName;
	private String desc;
	private String imageUrl;
	private String picName;
	private String actvity_url;
	private boolean isShow;//是否显示
	private String place;//显示位置 ex:bookshelf
	
	
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	public String getGiftName() {
		return giftName;
	}
	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getPicName() {
		return picName;
	}
	public void setPicName(String picName) {
		this.picName = picName;
	}
	public String getActvity_url() {
		return actvity_url;
	}
	public void setActvity_url(String actvity_url) {
		this.actvity_url = actvity_url;
	}
	public boolean isShow() {
		return isShow;
	}
	public void setShow(boolean isShow) {
		this.isShow = isShow;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	
	
}
