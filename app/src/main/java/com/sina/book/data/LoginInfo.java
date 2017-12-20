package com.sina.book.data;

import java.util.ArrayList;

import com.sina.book.data.UserInfoUb.Activitys;

/**
 * @ClassName: LoginInfo
 * @Description: 登录相关信息
 * @author Li Wen
 * @date 2012-11-6
 * 
 */
public class LoginInfo {
	public static final int CODE_SUCCEED = 1;
	public static final int PASSWORD_IS_WRONG = -100;
	public static final int ACCOUNT_EXCEPTION = -1005;

	/**
	 * 2.0 的access_token
	 */
	private String access_token;

	/**
	 * 登陆生效的时间
	 */
	private String remind_in;

	/**
	 * 登陆有效的时间戳
	 */
	private String expires_in;

	/**
	 * 登陆失效的时间
	 */
	private Long expires_time;

	/**
	 * userId
	 */
	private String uid;

	/**
	 * 余额
	 */
	private String balance = "0.00";

	/**
	 * user
	 */
	private UserInfo userInfo;

	/**
	 * 会员信息(保证在余额以及用户信息请求之前不为空)
	 */
	private UserInfoRole userInfoRole = new UserInfoRole();

	/**
	 * 活动相关
	 */
	// private String activityTip;
	// private String activityName;
	// private String activityUrl;
	// private String activityEndTime;

	/**
	 * 获取2.0的access_token
	 * 
	 * @return
	 */
	public String getAccessToken() {
		return access_token;
	}

	public void setAccessToken(String access_token) {
		this.access_token = access_token;
	}

	/**
	 * 获得生效的时间
	 * 
	 * @return
	 */
	public String getRemindIn() {
		return remind_in;
	}

	public void setRemindIn(String remind_in) {
		this.remind_in = remind_in;
	}

	/**
	 * 获得有效时间戳
	 * 
	 * @return
	 */
	public String getExpires() {
		return expires_in;
	}

	public void setExpires(String expires_in) {
		this.expires_in = expires_in;
	}

	/**
	 * 获得失效的时间
	 * 
	 * @return
	 */
	public Long getExpires_time() {
		return expires_time;
	}

	public void setExpires_time(Long expires_time) {
		this.expires_time = expires_time;
	}

	/**
	 * 获取用户id
	 * 
	 * @return
	 */
	public String getUID() {
		return uid;
	}

	public void setUID(String uid) {
		if (uid != null && uid.length() > 0) {
			this.uid = uid;
		}
	}

	public UserInfo getUserInfo() {
		if (userInfo == null) {
			userInfo = new UserInfo();
		}
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public UserInfoRole getUserInfoRole() {
		return userInfoRole;
	}

	public void setUserInfoRole(UserInfoRole userInfoRole) {
		this.userInfoRole = userInfoRole;
	}

	// public String getActivityTip() {
	// return activityTip;
	// }
	//
	// public String getActivityName() {
	// return activityName;
	// }
	//
	// public String getActivityUrl() {
	// return activityUrl;
	// }
	//
	// public String getActivityEndTime() {
	// return activityEndTime;
	// }

	// public void setActivity(String activityTip, String activityName, String
	// activityUrl, String activityEndTime) {
	// this.activityName = activityName;
	// this.activityTip = activityTip;
	// this.activityUrl = activityUrl;
	// this.activityEndTime = activityEndTime;
	// }

	private ArrayList<Activitys> activitys;

	public void setActivitys(ArrayList<Activitys> activitys) {
		this.activitys = activitys;
	}

	public ArrayList<Activitys> getActivitys() {
		if (activitys == null) {
			activitys = new ArrayList<UserInfoUb.Activitys>();
		}
		return activitys;
	}
}
