package com.sina.book.data;

public class PartitionLikedItem {
    private String mId;
    private String mTitle;
    private String mIconUrl;
    private boolean mIsFavorite;

	public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.mIconUrl = iconUrl;
    }
    
    public boolean getIsFavorite() {
		return mIsFavorite;
	}

	public void setIsFavorite(boolean isFavorite) {
		this.mIsFavorite = isFavorite;
	}
}
