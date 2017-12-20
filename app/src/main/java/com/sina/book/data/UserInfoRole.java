package com.sina.book.data;

public class UserInfoRole {
    /** 普通会员 */
    public static final int GENERAL_USER = 0;

    /** 白金会员 */
    public static final int SPECIAL_USER = 3;

    private int role = GENERAL_USER;
    private String roleName = "普通用户";

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

}