package com.sina.book.data;

import java.util.ArrayList;

public class UserInfoUb {
	private String balance;
	private int role;
	private String roleName;

	private String uid;
	private String name;

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

	public static class Activitys {
		public static final int TYPE_OTHER = 0;
		public static final int TYPE_CARD = 1;

		private String activityTip;
		private String activityName;
		private String activityUrl;
		private String activityEndTime;

		// 类型 1：赠书卡
		private int activityType;

		public String getActivityTip() {
			return activityTip;
		}

		public void setActivityTip(String activityTip) {
			this.activityTip = activityTip;
		}

		public int getActivityType() {
			return activityType;
		}

		public void setActivityType(int activityType) {
			this.activityType = activityType;
		}

		public String getActivityName() {
			return activityName;
		}

		public void setActivityName(String activityName) {
			this.activityName = activityName;
		}

		public String getActivityUrl() {
			return activityUrl;
		}

		public void setActivityUrl(String activityUrl) {
			this.activityUrl = activityUrl;
		}

		public String getActivityEndTime() {
			return activityEndTime;
		}

		public void setActivityEndTime(String activityEndTime) {
			this.activityEndTime = activityEndTime;
		}
	}

	// private String activityTip;
	// private String activityName;
	// private String activityUrl;
	// private String activityEndTime;

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
