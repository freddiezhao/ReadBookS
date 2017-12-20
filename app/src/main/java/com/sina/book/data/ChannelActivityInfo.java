package com.sina.book.data;

/**
 * 渠道包获奖信息类
 * 
 * @author chenjianli
 * @date 2014-01-15
 */
public class ChannelActivityInfo {

	private int stateCode = -1;
	private String stateMsg;

	private int type = 1;
	private String title;
	private String desc;
	private String subDesc;
	private String buttonDesc;

	public ChannelActivityInfo(int stateCode, String desc) {
		this.stateCode = stateCode;
		this.desc = desc;
	}

	public ChannelActivityInfo(int stateCode, String stateMsg, String title, String desc, String subDesc,
			String buttonDesc) {
		this.stateCode = stateCode;
		this.stateMsg = stateMsg;
		this.title = title;
		this.desc = desc;
		this.subDesc = subDesc;
		this.buttonDesc = buttonDesc;
	}

	public String getStateMsg() {
		return stateMsg;
	}

	public void setStateMsg(String stateMsg) {
		this.stateMsg = stateMsg;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getSubDesc() {
		return subDesc;
	}

	public void setSubDesc(String subDesc) {
		this.subDesc = subDesc;
	}

	public String getButtonDesc() {
		return buttonDesc;
	}

	public void setButtonDesc(String buttonDesc) {
		this.buttonDesc = buttonDesc;
	}

	public int getStateCode() {
		return stateCode;
	}

}
