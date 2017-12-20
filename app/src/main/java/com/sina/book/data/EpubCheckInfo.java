package com.sina.book.data;

public class EpubCheckInfo {

	public int code;
	public String msg;
    
    private String bookId;
    private String isEpub = "1";
    
    private boolean isHtml;
    
    public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
    public String getIsEpub() {
		return isEpub;
	}

	public void setIsEpub(String is_epub) {
		this.isEpub = is_epub;
	}
	
	public boolean isHtml(){
		return isHtml;
	}
	
	public void setIsHtml(boolean isHtml){
		this.isHtml = isHtml;
	}

}