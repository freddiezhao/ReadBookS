package com.sina.book.data;

/**
 * 
 * @author MarkMjw
 * 
 */
public class UpdateVersionInfo {
	
	// 是否更新
    private boolean isUpdate = false;
    private String updateInfo;
    private String url;
    private String intro;
    
    // 是否强制
    private boolean isForce = false;

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isForce() {
    	return isForce;
    }

    public void setForce(boolean isForce) {
        this.isForce = isForce;
    }

    @Override
    public String toString() {
        return "UpdateVersionInfo [isUpdate=" + isUpdate + ", updateInfo=" + updateInfo + ", url="
                + url + ", isForce=" + isForce + "]";
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

}
